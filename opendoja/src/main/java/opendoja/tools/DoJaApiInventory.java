package opendoja.tools;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parses the official DoJa 5.1 javadocs and compares the extracted API
 * surface against the current compiled runtime classes.
 */
public final class DoJaApiInventory {
    private static final Charset SHIFT_JIS = Charset.forName("Shift_JIS");
    private static final Pattern CLASS_LINK_PATTERN =
            Pattern.compile("<A HREF=\"([^\"]+\\.html)\" title=\"([^\"]+)\">(?:<I>)?([^<]+)(?:</I>)?</A>",
                    Pattern.CASE_INSENSITIVE);
    private static final Pattern PAGE_HEADER_PATTERN =
            Pattern.compile("<H2>\\s*<FONT SIZE=\"-1\">\\s*([^<]+)\\s*</FONT>\\s*<BR>\\s*([^<]+)\\s+(.*?)</H2>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern DETAIL_HEADER_PATTERN =
            Pattern.compile("<A NAME=\"([^\"]+)\"><!-- --></A><H3>\\s*(.*?)\\s*</H3>\\s*<PRE>\\s*(.*?)</PRE>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TOP_DECLARATION_PATTERN =
            Pattern.compile("<HR>\\s*<DL>(.*?)</DL>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern PACKAGE_NAME_PATTERN =
            Pattern.compile("パッケージ\\s+(com\\.nttdocomo\\.[^<\\s]+|com\\.nttdocomo)",
                    Pattern.CASE_INSENSITIVE);
    private static final List<String> DEFAULT_JAVADOC_ROOTS = List.of(
            "resources/doja-51-javadocs/jguidefordoja5_1_apiref_120706/javadoc",
            "resources/doja-51-javadocs/jguidefordoja5_x_apiref_opt_081024/javadoc",
            "resources/doja-51-javadocs/jguidefordoja5_x_apiref_opt_070423"
    );
    private static final String DEFAULT_CURRENT_CLASSES = "out/classes";
    private static final String DEFAULT_OUTPUT_DIR = "out/reports/doja51";

    private DoJaApiInventory() {
    }

    public static void main(String[] args) throws Exception {
        Config config = Config.parse(args);
        List<PackageDoc> packages = parsePackages(config.javadocRoots());
        List<ClassDoc> classes = parseClasses(config.javadocRoots());

        Files.createDirectories(config.outputDir());
        Coverage coverage = collectCoverage(config.currentClasses(), classes);
        writeInventory(config.outputDir().resolve("api-inventory.tsv"), packages, classes);
        writeClassStatusReport(config.outputDir().resolve("class-status.tsv"), coverage);
        writeVerifiedCompleteClasses(config.outputDir().resolve("verified-complete-classes.txt"), coverage);
        writeCoverageReport(config.outputDir().resolve("coverage-report.md"), packages, classes, coverage);

        System.out.println("Wrote DoJa 5.1 inventory to " + config.outputDir().toAbsolutePath());
    }

    private static List<PackageDoc> parsePackages(List<Path> javadocRoots) throws IOException {
        Map<String, PackageDoc> packages = new TreeMap<>();
        for (Path javadocRoot : javadocRoots) {
            try (Stream<Path> stream = Files.walk(javadocRoot.resolve("com/nttdocomo"))) {
                stream.filter(path -> path.getFileName().toString().equals("package-summary.html"))
                        .sorted()
                        .map(path -> readShiftJis(path))
                        .map(DoJaApiInventory::parsePackageDoc)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(packageDoc -> packages.putIfAbsent(packageDoc.name(), packageDoc));
            }
        }
        return new ArrayList<>(packages.values());
    }

    private static Optional<PackageDoc> parsePackageDoc(String html) {
        Matcher matcher = PACKAGE_NAME_PATTERN.matcher(html);
        if (!matcher.find()) {
            return Optional.empty();
        }
        String packageName = matcher.group(1).trim();
        String summary = extractPackageSummary(html);
        return Optional.of(new PackageDoc(packageName, summary));
    }

    private static List<ClassDoc> parseClasses(List<Path> javadocRoots) throws IOException {
        Map<String, ClassDoc> classes = new TreeMap<>();
        for (Path javadocRoot : javadocRoots) {
            Path allClasses = javadocRoot.resolve("allclasses-noframe.html");
            String indexHtml = readShiftJis(allClasses);
            Set<String> refs = new LinkedHashSet<>();
            Matcher linkMatcher = CLASS_LINK_PATTERN.matcher(indexHtml);
            while (linkMatcher.find()) {
                String href = linkMatcher.group(1);
                if (href.endsWith("package-summary.html") || href.endsWith("package-tree.html") || href.endsWith("package-frame.html")) {
                    continue;
                }
                if (!href.startsWith("com/nttdocomo/")) {
                    continue;
                }
                refs.add(href);
            }

            for (String ref : refs) {
                Path classPage = javadocRoot.resolve(ref);
                String html = readShiftJis(classPage);
                ClassDoc classDoc = parseClassDoc(ref, html);
                classes.putIfAbsent(classDoc.qualifiedName(), classDoc);
            }
        }
        return new ArrayList<>(classes.values());
    }

    private static ClassDoc parseClassDoc(String ref, String html) {
        Matcher headerMatcher = PAGE_HEADER_PATTERN.matcher(html);
        if (!headerMatcher.find()) {
            throw new IllegalArgumentException("Failed to parse class header for " + ref);
        }

        String packageName = cleanupText(headerMatcher.group(1));
        String typeKind = cleanupText(headerMatcher.group(2));
        String simpleName = cleanupText(headerMatcher.group(3));
        String qualifiedName = packageName + "." + simpleName;

        String declaration = "";
        Matcher declarationMatcher = TOP_DECLARATION_PATTERN.matcher(html);
        if (declarationMatcher.find()) {
            declaration = cleanupText(declarationMatcher.group(1));
        }

        int declarationEnd = declarationMatcher.find(0) ? declarationMatcher.end() : -1;
        if (declarationEnd < 0) {
            declarationEnd = html.indexOf("<P>");
        }
        String classSummary = declarationEnd >= 0
                ? cleanupText(sliceBetween(html, declarationEnd, html.indexOf("<HR>", declarationEnd)))
                : "";

        List<MemberDoc> fields = parseMemberSection(html, "field_detail", List.of("constructor_detail", "method_detail", "<!-- ========= END OF CLASS DATA"), MemberKind.FIELD);
        List<MemberDoc> constructors = parseMemberSection(html, "constructor_detail", List.of("method_detail", "<!-- ========= END OF CLASS DATA"), MemberKind.CONSTRUCTOR);
        List<MemberDoc> methods = parseMemberSection(html, "method_detail", List.of("<!-- ========= END OF CLASS DATA"), MemberKind.METHOD);

        return new ClassDoc(ref, qualifiedName, packageName, simpleName, typeKind, declaration, classSummary, fields, constructors, methods);
    }

    private static List<MemberDoc> parseMemberSection(String html, String startAnchor, List<String> endAnchors, MemberKind kind) {
        int start = html.indexOf("<A NAME=\"" + startAnchor + "\"");
        if (start < 0) {
            return List.of();
        }
        int end = html.length();
        for (String endAnchor : endAnchors) {
            int candidate = html.indexOf(endAnchor, start);
            if (candidate >= 0) {
                end = Math.min(end, candidate);
            }
        }
        String section = html.substring(start, end);
        Matcher matcher = DETAIL_HEADER_PATTERN.matcher(section);
        List<MemberDoc> members = new ArrayList<>();
        while (matcher.find()) {
            int dlStart = section.indexOf("<DL>", matcher.end());
            if (dlStart < 0) {
                continue;
            }
            String detailDl = extractBalancedTag(section, dlStart, "DL");
            String anchor = cleanupText(matcher.group(1));
            String name = cleanupText(matcher.group(2));
            String declaration = cleanupText(matcher.group(3));
            String documentation = cleanupText(detailDl);
            members.add(new MemberDoc(kind, anchor, name, declaration, documentation));
        }
        return members;
    }

    private static String extractBalancedTag(String html, int startIndex, String tagName) {
        String openTag = "<" + tagName;
        String closeTag = "</" + tagName + ">";
        int depth = 0;
        int index = startIndex;
        while (index < html.length()) {
            int nextOpen = html.indexOf(openTag, index);
            int nextClose = html.indexOf(closeTag, index);
            if (nextClose < 0) {
                return html.substring(startIndex);
            }
            if (nextOpen >= 0 && nextOpen < nextClose) {
                depth++;
                index = html.indexOf(">", nextOpen);
                if (index < 0) {
                    return html.substring(startIndex);
                }
                index++;
                continue;
            }
            depth--;
            index = nextClose + closeTag.length();
            if (depth == 0) {
                return html.substring(startIndex, index);
            }
        }
        return html.substring(startIndex);
    }

    private static void writeInventory(Path output, List<PackageDoc> packages, List<ClassDoc> classes) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("element_type\towner\tname\tkind\tanchor\tdeclaration\tsummary_or_detail");
        for (PackageDoc packageDoc : packages) {
            lines.add(tsv("package", "", packageDoc.name(), "package", "", "", packageDoc.summary()));
        }
        for (ClassDoc classDoc : classes) {
            lines.add(tsv("class", classDoc.packageName(), classDoc.simpleName(), classDoc.typeKind(), "",
                    classDoc.declaration(), classDoc.summary()));
            for (MemberDoc field : classDoc.fields()) {
                lines.add(tsv("field", classDoc.qualifiedName(), field.name(), "field", field.anchor(),
                        field.declaration(), field.documentation()));
            }
            for (MemberDoc constructor : classDoc.constructors()) {
                lines.add(tsv("constructor", classDoc.qualifiedName(), constructor.name(), "constructor",
                        constructor.anchor(), constructor.declaration(), constructor.documentation()));
            }
            for (MemberDoc method : classDoc.methods()) {
                lines.add(tsv("method", classDoc.qualifiedName(), method.name(), "method", method.anchor(),
                        method.declaration(), method.documentation()));
            }
        }
        Files.write(output, lines, StandardCharsets.UTF_8);
    }

    private static void writeCoverageReport(
            Path output,
            List<PackageDoc> packages,
            List<ClassDoc> classes,
            Coverage coverage
    ) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# DoJa 5.1 Coverage Report");
        lines.add("");
        lines.add("Official package count: " + packages.size());
        lines.add("Official class/interface page count: " + classes.size());
        lines.add("Current compiled class count resolved from javadocs: " + coverage.presentClasses());
        lines.add("Surface-complete class/interface count: " + coverage.completeClasses().size());
        lines.add("Present but member-incomplete class/interface count: " + coverage.incompleteClasses().size());
        lines.add("Missing class/interface count: " + coverage.missingClasses().size());
        lines.add("Missing field count across present classes: " + coverage.missingFields());
        lines.add("Missing constructor count across present classes: " + coverage.missingConstructors());
        lines.add("Missing method count across present classes: " + coverage.missingMethods());
        lines.add("Per-class status report: `out/reports/doja51/class-status.tsv`");
        lines.add("Surface-complete class list: `out/reports/doja51/verified-complete-classes.txt`");
        lines.add("");

        if (!coverage.missingClasses().isEmpty()) {
            lines.add("## Missing Classes");
            lines.add("");
            for (String missingClass : coverage.missingClasses()) {
                lines.add("- " + missingClass);
            }
            lines.add("");
        }

        if (!coverage.missingMembersByClass().isEmpty()) {
            lines.add("## Missing Members In Existing Classes");
            lines.add("");
            for (Map.Entry<String, List<String>> entry : coverage.missingMembersByClass().entrySet()) {
                lines.add("### " + entry.getKey());
                lines.add("");
                for (String missingMember : entry.getValue()) {
                    lines.add("- " + missingMember);
                }
                lines.add("");
            }
        }

        Files.write(output, lines, StandardCharsets.UTF_8);
    }

    private static void writeClassStatusReport(Path output, Coverage coverage) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("qualified_name\tpackage\tname\tkind\tstatus\tdocumented_fields\tdocumented_constructors\tdocumented_methods\tmissing_members");
        for (ClassStatus classStatus : coverage.classStatuses()) {
            ClassDoc classDoc = classStatus.classDoc();
            lines.add(tsv(
                    classDoc.qualifiedName(),
                    classDoc.packageName(),
                    classDoc.simpleName(),
                    classDoc.typeKind(),
                    classStatus.status().name(),
                    Integer.toString(classDoc.fields().size()),
                    Integer.toString(classDoc.constructors().size()),
                    Integer.toString(classDoc.methods().size()),
                    String.join(" | ", classStatus.missingMembers())
            ));
        }
        Files.write(output, lines, StandardCharsets.UTF_8);
    }

    private static void writeVerifiedCompleteClasses(Path output, Coverage coverage) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# DoJa 5.1 Surface-Complete Classes");
        lines.add("# Count: " + coverage.completeClasses().size() + " / "
                + coverage.classStatuses().size() + " documented classes/interfaces");
        lines.add("");
        for (String className : coverage.completeClasses()) {
            lines.add(className);
        }
        Files.write(output, lines, StandardCharsets.UTF_8);
    }

    private static Coverage collectCoverage(Path currentClasses, List<ClassDoc> classes) throws Exception {
        if (!Files.isDirectory(currentClasses)) {
            return Coverage.empty(classes.stream().map(ClassDoc::qualifiedName).toList());
        }

        List<ClassStatus> classStatuses = new ArrayList<>();
        List<String> missingClasses = new ArrayList<>();
        List<String> completeClasses = new ArrayList<>();
        List<String> incompleteClasses = new ArrayList<>();
        Map<String, List<String>> missingMembersByClass = new TreeMap<>();
        int presentClasses = 0;
        int missingFields = 0;
        int missingConstructors = 0;
        int missingMethods = 0;

        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{currentClasses.toUri().toURL()})) {
            for (ClassDoc classDoc : classes) {
                Optional<Class<?>> currentClass = tryLoadCurrentClass(classLoader, classDoc.qualifiedName());
                if (currentClass.isEmpty()) {
                    missingClasses.add(classDoc.qualifiedName());
                    classStatuses.add(new ClassStatus(classDoc, ClassCoverageStatus.MISSING, List.of()));
                    continue;
                }

                presentClasses++;
                Set<String> currentFields = Arrays.stream(currentClass.get().getDeclaredFields())
                        .filter(field -> !field.isSynthetic())
                        .map(field -> field.getName())
                        .collect(Collectors.toCollection(TreeSet::new));

                Set<String> currentConstructors = Arrays.stream(currentClass.get().getDeclaredConstructors())
                        .filter(constructor -> !constructor.isSynthetic())
                        .map(constructor -> constructorAnchor(classDoc.simpleName(), constructor.getParameterTypes()))
                        .collect(Collectors.toCollection(TreeSet::new));

                Set<String> currentMethods = Arrays.stream(currentClass.get().getDeclaredMethods())
                        .filter(method -> !method.isSynthetic() && !method.isBridge())
                        .map(method -> methodAnchor(method.getName(), method.getParameterTypes()))
                        .collect(Collectors.toCollection(TreeSet::new));

                List<String> missingMembers = new ArrayList<>();

                for (MemberDoc field : classDoc.fields()) {
                    if (!currentFields.contains(field.anchor())) {
                        missingMembers.add("field " + field.anchor());
                        missingFields++;
                    }
                }
                for (MemberDoc constructor : classDoc.constructors()) {
                    if (!currentConstructors.contains(constructor.anchor())) {
                        missingMembers.add("constructor " + constructor.anchor());
                        missingConstructors++;
                    }
                }
                for (MemberDoc method : classDoc.methods()) {
                    if (!currentMethods.contains(method.anchor())) {
                        missingMembers.add("method " + method.anchor());
                        missingMethods++;
                    }
                }

                if (!missingMembers.isEmpty()) {
                    incompleteClasses.add(classDoc.qualifiedName());
                    missingMembersByClass.put(classDoc.qualifiedName(), missingMembers);
                    classStatuses.add(new ClassStatus(classDoc, ClassCoverageStatus.INCOMPLETE, List.copyOf(missingMembers)));
                } else {
                    completeClasses.add(classDoc.qualifiedName());
                    classStatuses.add(new ClassStatus(classDoc, ClassCoverageStatus.COMPLETE, List.of()));
                }
            }
        }

        return new Coverage(
                presentClasses,
                List.copyOf(completeClasses),
                List.copyOf(incompleteClasses),
                List.copyOf(missingClasses),
                Map.copyOf(missingMembersByClass),
                missingFields,
                missingConstructors,
                missingMethods,
                List.copyOf(classStatuses)
        );
    }

    private static Optional<Class<?>> tryLoadCurrentClass(ClassLoader classLoader, String qualifiedName) {
        List<String> candidates = new ArrayList<>();
        candidates.add(qualifiedName);
        int packageSeparator = qualifiedName.lastIndexOf('.');
        String packagePrefix = packageSeparator >= 0 ? qualifiedName.substring(0, packageSeparator) : "";
        String simple = packageSeparator >= 0 ? qualifiedName.substring(packageSeparator + 1) : qualifiedName;
        String[] parts = simple.split("\\.");
        for (int i = 1; i < parts.length; i++) {
            StringBuilder binary = new StringBuilder(packagePrefix);
            if (!packagePrefix.isEmpty()) {
                binary.append('.');
            }
            binary.append(parts[0]);
            for (int j = 1; j < parts.length; j++) {
                binary.append(j <= i ? '$' : '.').append(parts[j]);
            }
            candidates.add(binary.toString());
        }

        for (String candidate : candidates) {
            try {
                return Optional.of(Class.forName(candidate, false, classLoader));
            } catch (ReflectiveOperationException ignored) {
                // Try the next binary name candidate.
            } catch (LinkageError ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static String constructorAnchor(String simpleName, Class<?>[] parameterTypes) {
        return simpleName + "(" + joinTypeNames(parameterTypes) + ")";
    }

    private static String methodAnchor(String name, Class<?>[] parameterTypes) {
        return name + "(" + joinTypeNames(parameterTypes) + ")";
    }

    private static String joinTypeNames(Class<?>[] parameterTypes) {
        return Arrays.stream(parameterTypes)
                .map(DoJaApiInventory::typeName)
                .collect(Collectors.joining(", "));
    }

    private static String typeName(Class<?> type) {
        if (type.isArray()) {
            return typeName(type.getComponentType()) + "[]";
        }
        return type.getName().replace('$', '.');
    }

    private static String tsv(String... columns) {
        return Arrays.stream(columns)
                .map(column -> column == null ? "" : column.replace("\t", " ").replace('\n', ' ').trim())
                .collect(Collectors.joining("\t"));
    }

    private static String sliceBetween(String input, int start, int end) {
        if (start < 0 || end < 0 || start >= end) {
            return "";
        }
        return input.substring(start, end);
    }

    private static String extractPackageSummary(String html) {
        int headingEnd = html.indexOf("</H2>");
        if (headingEnd < 0) {
            return "";
        }
        int nextBlock = html.indexOf("<P>", headingEnd);
        int nextTable = html.indexOf("<TABLE", headingEnd);
        int end = nextTable >= 0 ? nextTable : html.length();
        if (nextBlock >= 0 && nextBlock < end) {
            end = nextBlock;
        }
        return cleanupText(html.substring(headingEnd, end));
    }

    private static String cleanupText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        String text = html
                .replaceAll("(?is)<script.*?</script>", " ")
                .replaceAll("(?is)<style.*?</style>", " ")
                .replaceAll("(?is)<!--.*?-->", " ")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?i)</tr>", "\n")
                .replaceAll("(?i)</dt>", " ")
                .replaceAll("(?i)</dd>", "\n")
                .replaceAll("(?is)<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("\r", "");
        text = decodeNumericEntities(text);
        return Arrays.stream(text.split("\n"))
                .map(line -> line.replace('\u00a0', ' ').trim())
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining("\n"))
                .replaceAll("[ \t]+", " ")
                .trim();
    }

    private static String decodeNumericEntities(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '&' && text.startsWith("&#", i)) {
                int end = text.indexOf(';', i);
                if (end > i) {
                    String number = text.substring(i + 2, end);
                    try {
                        int codePoint = number.startsWith("x") || number.startsWith("X")
                                ? Integer.parseInt(number.substring(1), 16)
                                : Integer.parseInt(number);
                        result.appendCodePoint(codePoint);
                        i = end;
                        continue;
                    } catch (NumberFormatException ignored) {
                        // Fall through and keep the original text.
                    }
                }
            }
            result.append(ch);
        }
        return result.toString();
    }

    private static String readShiftJis(Path path) {
        try {
            return Files.readString(path, SHIFT_JIS);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + path, e);
        }
    }

    private record Config(List<Path> javadocRoots, Path currentClasses, Path outputDir) {
        private static Config parse(String[] args) {
            List<Path> javadocRoots = DEFAULT_JAVADOC_ROOTS.stream().map(Paths::get).collect(Collectors.toCollection(ArrayList::new));
            Path currentClasses = Paths.get(DEFAULT_CURRENT_CLASSES);
            Path outputDir = Paths.get(DEFAULT_OUTPUT_DIR);

            for (String arg : args) {
                if (arg.startsWith("--javadoc-root=")) {
                    javadocRoots = Arrays.stream(arg.substring("--javadoc-root=".length()).split(","))
                            .map(String::trim)
                            .filter(value -> !value.isEmpty())
                            .map(Paths::get)
                            .collect(Collectors.toCollection(ArrayList::new));
                } else if (arg.startsWith("--current-classes=")) {
                    currentClasses = Paths.get(arg.substring("--current-classes=".length()));
                } else if (arg.startsWith("--output-dir=")) {
                    outputDir = Paths.get(arg.substring("--output-dir=".length()));
                } else {
                    throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            }

            return new Config(javadocRoots, currentClasses, outputDir);
        }
    }

    private record PackageDoc(String name, String summary) {
    }

    private record ClassDoc(
            String ref,
            String qualifiedName,
            String packageName,
            String simpleName,
            String typeKind,
            String declaration,
            String summary,
            List<MemberDoc> fields,
            List<MemberDoc> constructors,
            List<MemberDoc> methods
    ) {
    }

    private enum MemberKind {
        FIELD,
        CONSTRUCTOR,
        METHOD
    }

    private record MemberDoc(
            MemberKind kind,
            String anchor,
            String name,
            String declaration,
            String documentation
    ) {
    }

    private record Coverage(
            int presentClasses,
            List<String> completeClasses,
            List<String> incompleteClasses,
            List<String> missingClasses,
            Map<String, List<String>> missingMembersByClass,
            int missingFields,
            int missingConstructors,
            int missingMethods,
            List<ClassStatus> classStatuses
    ) {
        private static Coverage empty(List<String> missingClasses) {
            return new Coverage(0, List.of(), List.of(), new ArrayList<>(missingClasses), Map.of(), 0, 0, 0, List.of());
        }
    }

    private record ClassStatus(
            ClassDoc classDoc,
            ClassCoverageStatus status,
            List<String> missingMembers
    ) {
    }

    private enum ClassCoverageStatus {
        COMPLETE,
        INCOMPLETE,
        MISSING
    }
}
