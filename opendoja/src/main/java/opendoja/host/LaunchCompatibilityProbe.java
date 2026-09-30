package opendoja.host;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Verifies the self-reexec command builders preserve the expected JVM arguments for compatibility
 * relaunches, including the narrow {@code --add-opens} set needed by the SJIS hook.
 */
public final class LaunchCompatibilityProbe {
    private LaunchCompatibilityProbe() {
    }

    public static void main(String[] args) throws Exception {
        String expectedEncoding = args.length == 0 ? DoJaEncoding.defaultCharsetName() : args[0];
        verifyCompatibilityCommand(expectedEncoding);
        verifyVerifyFallbackCommand(expectedEncoding);
        verifyRequiredAddOpensCommand(expectedEncoding);
    }

    private static void verifyCompatibilityCommand(String expectedEncoding) throws Exception {
        Method method = LaunchCompatibility.class.getDeclaredMethod("buildCompatibilityCommand",
                boolean.class, boolean.class, boolean.class, boolean.class, String.class, String[].class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> command = (List<String>) method.invoke(null,
                false, false, false, false, "opendoja.host.JamLauncher", new String[]{"sample.jam"});
        verifyFileEncoding(command, expectedEncoding, "compatibility");
    }

    private static void verifyVerifyFallbackCommand(String expectedEncoding) throws Exception {
        Method method = LaunchCompatibility.class.getDeclaredMethod("buildVerifyFallbackCommand",
                String.class, String[].class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> command = (List<String>) method.invoke(null,
                "opendoja.host.JamLauncher", new String[]{"sample.jam"});
        verifyFileEncoding(command, expectedEncoding, "verify fallback");
    }

    private static void verifyRequiredAddOpensCommand(String expectedEncoding) throws Exception {
        Method method = LaunchCompatibility.class.getDeclaredMethod("buildRequiredAddOpensCommand",
                String.class, String[].class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> command = (List<String>) method.invoke(null,
                "opendoja.host.JamLauncher", new String[]{"sample.jam"});
        verifyFileEncoding(command, expectedEncoding, "required add-opens");
        // The relaunch must carry both the charset opens and the existing named-module bridge open.
        check(command.contains("--add-opens=java.base/java.nio.charset=ALL-UNNAMED"),
                "required add-opens command should include java.nio.charset open: " + command);
        check(command.contains("--add-opens=java.base/sun.nio.cs=ALL-UNNAMED"),
                "required add-opens command should include sun.nio.cs open: " + command);
        check(command.contains(JamNamedModuleResourceBridge.requiredAddOpensArgument()),
                "required add-opens command should include named-module bridge open: " + command);
        System.out.println("required add-opens command OK");
    }

    private static void verifyFileEncoding(List<String> command, String expectedEncoding, String label) {
        String expectedArgument = "-Dfile.encoding=" + expectedEncoding;
        long matches = command.stream().filter(arg -> arg.startsWith("-Dfile.encoding=")).count();
        check(matches == 1L, label + " command should contain exactly one file.encoding argument: " + command);
        check(command.contains(expectedArgument),
                label + " command should contain " + expectedArgument + " but was " + command);
        System.out.println(label + " command OK: " + expectedArgument);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
