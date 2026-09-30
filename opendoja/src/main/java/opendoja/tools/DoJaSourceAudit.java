package opendoja.tools;

import com.sun.source.tree.*;
import com.sun.source.util.DocTrees;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Audits the documented DoJa source tree for missing English javadocs and
 * obvious placeholder method bodies.
 */
public final class DoJaSourceAudit {
    private static final Path DEFAULT_SOURCE_ROOT = Paths.get("src/main/java");
    private static final Path DEFAULT_INVENTORY = Paths.get("out/reports/doja51/api-inventory.tsv");
    private static final Path DEFAULT_OUTPUT_DIR = Paths.get("out/reports/doja51/source-audit");
    private static final Path DEFAULT_CLASSPATH = Paths.get("out/classes");
    private static final Set<String> DOCUMENTED_DEFAULT_NO_OPS = Set.of(
            "com.nttdocomo.ui.Canvas.processEvent(int, int)",
            "com.nttdocomo.ui.Canvas.processIMEEvent(int, String)",
            "com.nttdocomo.ui.Dialog.setSoftLabel(int, String)",
            "com.nttdocomo.ui.IApplication.resume()",
            "com.nttdocomo.ui.MApplication.processSystemEvent(int, int)"
    );
    private static final Set<String> DOCUMENTED_ALWAYS_UNSUPPORTED = Set.of(
            "com.nttdocomo.device.felica.Felica.activate()",
            "com.nttdocomo.device.felica.Felica.inactivate()",
            "com.nttdocomo.device.felica.ThruRWOfflineFelica.read(InputPINParameters, ReadParameters)",
            "com.nttdocomo.device.felica.ThruRWOfflineFelica.write(InputPINParameters, WriteParameters)",
            "com.nttdocomo.ui.Dialog.setSoftLabelVisible(boolean)",
            "com.nttdocomo.ui.PalettedImage.getGraphics()",
            "com.nttdocomo.ui.PalettedImage.getTransparentColor()",
            "com.nttdocomo.ui.PalettedImage.setTransparentColor(int)"
    );

    private DoJaSourceAudit() {
    }

    public static void main(String[] args) throws Exception {
        Config config = Config.parse(args);
        Inventory inventory = Inventory.read(config.inventoryPath());
        AuditResult result = audit(config.sourceRoot(), inventory);

        Files.createDirectories(config.outputDir());
        writeIssues(config.outputDir().resolve("missing-package-javadocs.tsv"), result.missingPackageJavadocs());
        writeIssues(config.outputDir().resolve("missing-javadocs.tsv"), result.missingJavadocs());
        writeIssues(config.outputDir().resolve("stub-candidates.tsv"), result.stubCandidates());
        writeSummary(config.outputDir().resolve("summary.md"), result);

        System.out.println("Audited DoJa sources to " + config.outputDir().toAbsolutePath());
        System.out.println("Missing package javadocs: " + result.missingPackageJavadocs().size());
        System.out.println("Missing class/member javadocs: " + result.missingJavadocs().size());
        System.out.println("Stub candidates: " + result.stubCandidates().size());
    }

    private static AuditResult audit(Path sourceRoot, Inventory inventory) throws IOException {
        List<Issue> missingPackageJavadocs = auditPackageDocs(sourceRoot, inventory.documentedPackages());
        List<Path> sourceFiles;
        try (Stream<Path> stream = Files.walk(sourceRoot.resolve("com/nttdocomo"))) {
            sourceFiles = stream
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .toList();
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No system Java compiler available");
        }

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> sources = fileManager.getJavaFileObjectsFromPaths(sourceFiles);
            JavacTask task = (JavacTask) compiler.getTask(
                    null,
                    fileManager,
                    null,
                    List.of("-proc:none", "-implicit:none", "-classpath", DEFAULT_CLASSPATH.toString()),
                    null,
                    sources
            );
            Iterable<? extends CompilationUnitTree> units = task.parse();
            task.analyze();

            DocTrees docTrees = DocTrees.instance(task);
            AuditScanner scanner = new AuditScanner(docTrees, inventory.documentedClasses());
            for (CompilationUnitTree unit : units) {
                scanner.scan(unit, null);
            }
            return new AuditResult(
                    List.copyOf(missingPackageJavadocs),
                    List.copyOf(scanner.missingJavadocs),
                    List.copyOf(scanner.stubCandidates)
            );
        }
    }

    private static List<Issue> auditPackageDocs(Path sourceRoot, Set<String> documentedPackages) {
        List<Issue> issues = new ArrayList<>();
        for (String packageName : new TreeSet<>(documentedPackages)) {
            Path packageInfo = sourceRoot.resolve(packageName.replace('.', '/')).resolve("package-info.java");
            if (!Files.exists(packageInfo)) {
                issues.add(new Issue(
                        "package",
                        packageName,
                        packageName,
                        packageInfo.toString(),
                        0,
                        "missing-package-info"
                ));
                continue;
            }
            try {
                String source = Files.readString(packageInfo, StandardCharsets.UTF_8);
                if (!source.stripLeading().startsWith("/**")) {
                    issues.add(new Issue(
                            "package",
                            packageName,
                            packageName,
                            packageInfo.toString(),
                            1,
                            "package-info without javadoc"
                    ));
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to read " + packageInfo, exception);
            }
        }
        return issues;
    }

    private static void writeIssues(Path output, List<Issue> issues) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("kind\towner\tsignature\tsource\tline\treason");
        for (Issue issue : issues.stream()
                .sorted(Comparator.comparing(Issue::source).thenComparingInt(Issue::line).thenComparing(Issue::signature))
                .toList()) {
            lines.add(tsv(
                    issue.kind(),
                    issue.owner(),
                    issue.signature(),
                    issue.source(),
                    Integer.toString(issue.line()),
                    issue.reason()
            ));
        }
        Files.write(output, lines, StandardCharsets.UTF_8);
    }

    private static void writeSummary(Path output, AuditResult result) throws IOException {
        List<String> lines = List.of(
                "# DoJa Source Audit",
                "",
                "Missing package javadocs: " + result.missingPackageJavadocs().size(),
                "Missing class/member javadocs: " + result.missingJavadocs().size(),
                "Stub candidates: " + result.stubCandidates().size(),
                "",
                "Reports:",
                "- `out/reports/doja51/source-audit/missing-package-javadocs.tsv`",
                "- `out/reports/doja51/source-audit/missing-javadocs.tsv`",
                "- `out/reports/doja51/source-audit/stub-candidates.tsv`"
        );
        Files.write(output, lines, StandardCharsets.UTF_8);
    }

    private static String tsv(String... values) {
        return Stream.of(values)
                .map(value -> value == null ? "" : value.replace('\t', ' ').replace('\n', ' ').trim())
                .collect(Collectors.joining("\t"));
    }

    private record Config(Path sourceRoot, Path inventoryPath, Path outputDir) {
        private static Config parse(String[] args) {
            Path sourceRoot = DEFAULT_SOURCE_ROOT;
            Path inventoryPath = DEFAULT_INVENTORY;
            Path outputDir = DEFAULT_OUTPUT_DIR;
            for (String arg : args) {
                if (arg.startsWith("--source-root=")) {
                    sourceRoot = Paths.get(arg.substring("--source-root=".length()));
                } else if (arg.startsWith("--inventory=")) {
                    inventoryPath = Paths.get(arg.substring("--inventory=".length()));
                } else if (arg.startsWith("--output-dir=")) {
                    outputDir = Paths.get(arg.substring("--output-dir=".length()));
                } else {
                    throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            }
            return new Config(sourceRoot, inventoryPath, outputDir);
        }
    }

    private record Inventory(Set<String> documentedPackages, Set<String> documentedClasses) {
        private static Inventory read(Path inventoryPath) throws IOException {
            if (!Files.exists(inventoryPath)) {
                throw new IllegalStateException("Inventory file not found: " + inventoryPath);
            }
            Set<String> packages = new LinkedHashSet<>();
            Set<String> classes = new LinkedHashSet<>();
            for (String line : Files.readAllLines(inventoryPath, StandardCharsets.UTF_8)) {
                if (line.startsWith("element_type\t")) {
                    continue;
                }
                String[] columns = line.split("\t", -1);
                if (columns.length < 3) {
                    continue;
                }
                String elementType = columns[0];
                if ("package".equals(elementType)) {
                    packages.add(columns[2]);
                } else if ("class".equals(elementType)) {
                    String owner = columns[1];
                    String name = columns[2];
                    classes.add(owner + "." + name);
                }
            }
            return new Inventory(Set.copyOf(packages), Set.copyOf(classes));
        }
    }

    private record AuditResult(
            List<Issue> missingPackageJavadocs,
            List<Issue> missingJavadocs,
            List<Issue> stubCandidates
    ) {
    }

    private record Issue(
            String kind,
            String owner,
            String signature,
            String source,
            int line,
            String reason
    ) {
    }

    private static final class AuditScanner extends TreePathScanner<Void, Void> {
        private final DocTrees docTrees;
        private final Set<String> documentedClasses;
        private final Deque<String> classStack = new ArrayDeque<>();
        private final List<Issue> missingJavadocs = new ArrayList<>();
        private final List<Issue> stubCandidates = new ArrayList<>();

        private CompilationUnitTree currentUnit;
        private String currentPackage;

        private AuditScanner(DocTrees docTrees, Set<String> documentedClasses) {
            this.docTrees = docTrees;
            this.documentedClasses = documentedClasses;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, Void unused) {
            currentUnit = node;
            currentPackage = node.getPackageName() == null ? "" : node.getPackageName().toString();
            return super.visitCompilationUnit(node, unused);
        }

        @Override
        public Void visitClass(ClassTree node, Void unused) {
            if (!classStack.isEmpty()) {
                return null;
            }
            String qualifiedName = currentPackage.isEmpty()
                    ? node.getSimpleName().toString()
                    : currentPackage + "." + node.getSimpleName();
            boolean documented = documentedClasses.contains(qualifiedName);
            classStack.push(documented ? qualifiedName : "");
            if (documented) {
                recordMissingJavadoc("class", qualifiedName, qualifiedName, getCurrentPath(), node);
            }
            try {
                return super.visitClass(node, unused);
            } finally {
                classStack.pop();
            }
        }

        @Override
        public Void visitVariable(VariableTree node, Void unused) {
            String owner = currentDocumentedClass();
            if (owner != null && isPublicOrProtected(node.getModifiers())) {
                recordMissingJavadoc("field", owner, node.getName().toString(), getCurrentPath(), node);
            }
            return super.visitVariable(node, unused);
        }

        @Override
        public Void visitMethod(MethodTree node, Void unused) {
            String owner = currentDocumentedClass();
            if (owner != null && isPublicOrProtected(node.getModifiers())) {
                String signature = signature(node);
                recordMissingJavadoc(node.getReturnType() == null ? "constructor" : "method", owner, signature, getCurrentPath(), node);
                inspectStubCandidate(owner, signature, node, getCurrentPath());
            }
            return super.visitMethod(node, unused);
        }

        private void recordMissingJavadoc(String kind, String owner, String signature, TreePath path, Tree tree) {
            if (docTrees.getDocCommentTree(path) != null) {
                return;
            }
            missingJavadocs.add(new Issue(
                    kind,
                    owner,
                    signature,
                    currentUnit.getSourceFile().getName(),
                    lineOf(tree),
                    "missing-javadoc"
            ));
        }

        private void inspectStubCandidate(String owner, String signature, MethodTree node, TreePath path) {
            if (DOCUMENTED_DEFAULT_NO_OPS.contains(owner + "." + signature)) {
                return;
            }
            if (DOCUMENTED_ALWAYS_UNSUPPORTED.contains(owner + "." + signature)) {
                return;
            }
            if (node.getBody() == null) {
                return;
            }
            BlockTree body = node.getBody();
            List<? extends StatementTree> statements = body.getStatements();
            if (statements.isEmpty()) {
                stubCandidates.add(issue("method", owner, signature, body, "empty-body"));
                return;
            }
            if (statements.size() != 1) {
                return;
            }
            StatementTree only = statements.get(0);
            switch (only.getKind()) {
                case RETURN -> inspectReturnStub(owner, signature, (ReturnTree) only);
                case THROW -> {
                    String rendered = only.toString();
                    String lower = rendered.toLowerCase(Locale.ROOT);
                    if (lower.contains("unsupportedoperationexception")
                            || lower.contains("not implemented")
                            || lower.contains("not supported")
                            || lower.contains("unsupported")) {
                        stubCandidates.add(issue("method", owner, signature, only, "unsupported-throw"));
                    }
                }
                default -> {
                    String rendered = only.toString();
                    String lower = rendered.toLowerCase(Locale.ROOT);
                    if (lower.contains("not implemented") || lower.contains("not supported")) {
                        stubCandidates.add(issue("method", owner, signature, only, "placeholder-body"));
                    }
                }
            }
        }

        private void inspectReturnStub(String owner, String signature, ReturnTree returnTree) {
            ExpressionTree expression = returnTree.getExpression();
            if (expression == null) {
                stubCandidates.add(issue("method", owner, signature, returnTree, "empty-return"));
                return;
            }
            switch (expression.getKind()) {
                case NULL_LITERAL -> stubCandidates.add(issue("method", owner, signature, returnTree, "return-null"));
                case BOOLEAN_LITERAL, INT_LITERAL, LONG_LITERAL, FLOAT_LITERAL, DOUBLE_LITERAL, CHAR_LITERAL, STRING_LITERAL ->
                        stubCandidates.add(issue("method", owner, signature, returnTree, "return-literal"));
                case NEW_CLASS -> {
                    NewClassTree newClass = (NewClassTree) expression;
                    String type = newClass.getIdentifier().toString();
                    String lower = type.toLowerCase(Locale.ROOT);
                    if (lower.contains("unsupported") || lower.contains("runtimeexception")) {
                        stubCandidates.add(issue("method", owner, signature, returnTree, "return-exception-object"));
                    }
                }
                default -> {
                    String rendered = expression.toString().trim();
                    if ("false".equals(rendered) || "true".equals(rendered) || "0".equals(rendered) || "null".equals(rendered)) {
                        stubCandidates.add(issue("method", owner, signature, returnTree, "return-default"));
                    }
                }
            }
        }

        private Issue issue(String kind, String owner, String signature, Tree tree, String reason) {
            return new Issue(kind, owner, signature, currentUnit.getSourceFile().getName(), lineOf(tree), reason);
        }

        private int lineOf(Tree tree) {
            LineMap lineMap = currentUnit.getLineMap();
            long position = docTrees.getSourcePositions().getStartPosition(currentUnit, tree);
            return position < 0 ? 0 : (int) lineMap.getLineNumber(position);
        }

        private boolean isPublicOrProtected(ModifiersTree modifiers) {
            return modifiers.getFlags().stream().anyMatch(flag -> flag.name().equals("PUBLIC") || flag.name().equals("PROTECTED"));
        }

        private String signature(MethodTree node) {
            String params = node.getParameters().stream()
                    .map(parameter -> parameter.getType().toString())
                    .collect(Collectors.joining(", "));
            if (node.getReturnType() == null) {
                String simpleName = ownerSimpleName(currentDocumentedClass());
                return simpleName + "(" + params + ")";
            }
            return node.getName() + "(" + params + ")";
        }

        private String ownerSimpleName(String owner) {
            int separator = owner == null ? -1 : owner.lastIndexOf('.');
            return separator >= 0 ? owner.substring(separator + 1) : owner;
        }

        private String currentDocumentedClass() {
            for (String className : classStack) {
                if (className != null && !className.isEmpty()) {
                    return className;
                }
            }
            return null;
        }
    }
}
