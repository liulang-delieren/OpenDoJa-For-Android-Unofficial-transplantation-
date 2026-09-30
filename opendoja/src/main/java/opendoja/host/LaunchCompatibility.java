package opendoja.host;

import java.io.IOException;
import opendoja.compat.management.ManagementFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class LaunchCompatibility {
    private LaunchCompatibility() {
    }

    static void reexecJamLauncherWithRequiredAddOpensIfNeeded(Path jamPath) throws IOException, InterruptedException {
        if (JamNamedModuleResourceBridge.hasRequiredAddOpens() && DoJaSjisCompatibility.hasRequiredAddOpens()) {
            return;
        }
        // Both the named-module resource bridge and the DoJa-specific SJIS compatibility hook rely on
        // narrow reflective access into JDK internals. Re-exec once with only those explicit
        // opens so the default desktop launch path stays unchanged.
        Process process = new ProcessBuilder(buildRequiredAddOpensCommand(
                        JamLauncher.class.getName(),
                        new String[]{jamPath.toString()}))
                .inheritIO()
                .start();
        int exit = process.waitFor();
        System.exit(exit);
    }

    static void reexecJamLauncherIfNeeded(Path jamPath) throws IOException, InterruptedException {
        if (OpenDoJaLaunchArgs.getBoolean(OpenDoJaLaunchArgs.LAUNCH_COMPAT_APPLIED)) {
            return;
        }
        System.setProperty(DoJaProfile.CURRENT_JAM_PATH_PROPERTY, jamPath.toAbsolutePath().normalize().toString());
        boolean disableExplicitGc = shouldDisableExplicitGc();
        boolean limitHotSpotTier = shouldLimitHotSpotTier();
        boolean disableOnStackReplacement = shouldDisableOnStackReplacement();
        boolean disableLoopInvariantCodeMotion = shouldDisableLoopInvariantCodeMotion();
        if (!disableExplicitGc && !limitHotSpotTier
                && !disableOnStackReplacement
                && !disableLoopInvariantCodeMotion) {
            return;
        }

        Process process = new ProcessBuilder(buildCompatibilityCommand(
                        disableExplicitGc,
                        limitHotSpotTier,
                        disableOnStackReplacement,
                        disableLoopInvariantCodeMotion,
                        JamLauncher.class.getName(),
                        new String[]{jamPath.toString()}))
                .inheritIO()
                .start();
        int exit = process.waitFor();
        System.exit(exit);
    }

    static boolean reexecJamLauncherOnVerifyError(Path jamPath) throws IOException, InterruptedException {
        if (OpenDoJaLaunchArgs.getBoolean(OpenDoJaLaunchArgs.VERIFY_FALLBACK_APPLIED) || explicitVerificationArgument() != null) {
            return false;
        }
        // TODO: https://github.com/GrenderG/openDoJa/issues/9 Find a better/clean way?
        // This fallback is intentionally JVM-wide because bytecode verification is also JVM-wide.
        // Keep it as a one-time startup retry only after the title has actually failed with
        // VerifyError, rather than weakening verification for every launch.
        Process process = new ProcessBuilder(buildVerifyFallbackCommand(JamLauncher.class.getName(),
                        new String[]{jamPath.toString()}))
                .inheritIO()
                .start();
        int exit = process.waitFor();
        System.exit(exit);
        return true;
    }

    private static List<String> buildCompatibilityCommand(boolean disableExplicitGc, boolean limitHotSpotTier,
            boolean disableOnStackReplacement, boolean disableLoopInvariantCodeMotion,
            String mainClass, String[] args) {
        List<String> command = new ArrayList<>();
        command.add(Path.of(OpenDoJaLaunchArgs.get("java.home"), "bin", "java").toString());
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.startsWith("-D" + OpenDoJaLaunchArgs.LAUNCH_COMPAT_APPLIED + "=")
                    || arg.startsWith("-XX:TieredStopAtLevel=")
                    || arg.equals("-XX:+UseOnStackReplacement")
                    || arg.equals("-XX:-UseOnStackReplacement")
                    || arg.equals("-XX:+UseLoopInvariantCodeMotion")
                    || arg.equals("-XX:-UseLoopInvariantCodeMotion")
                    || arg.equals("-XX:+DisableExplicitGC")
                    || arg.equals("-XX:-DisableExplicitGC")) {
                continue;
            }
            command.add(arg);
        }
        appendCurrentOpenDoJaProperties(command);
        appendFileEncoding(command);
        command.add("-D" + OpenDoJaLaunchArgs.LAUNCH_COMPAT_APPLIED + "=true");
        if (disableExplicitGc) {
            // Games issue System.gc() liberally around UI/resource transitions as a lightweight
            // handset-era memory hint. On desktop HotSpot that becomes a blocking full GC, which
            // stalls the single game thread and drags audio down with it.
            command.add("-XX:+DisableExplicitGC");
        }
        if (limitHotSpotTier) {
            // The official emulator runs on JBlend rather than HotSpot C2. Stopping at tier 1
            // keeps legacy empty polling loops observable without per-title deoptimization.
            command.add("-XX:TieredStopAtLevel=1");
        }
        if (disableOnStackReplacement) {
            // Older DoJa titles sometimes exchange control through unsynchronized busy-spin
            // handoffs. The proven HotSpot-specific failure is OSR compiling those loops into
            // stale-value spins, while whole-JVM interpretation regresses timing-sensitive apps.
            command.add("-XX:-UseOnStackReplacement");
        }
        if (disableLoopInvariantCodeMotion) {
            // Under the tier-1 compatibility path, C1 can still hoist unsynchronized latch reads
            // out of empty busy-wait loops. Keeping those reads inside the loop preserves the
            // handset-era handoff behavior without falling back to whole-JVM interpretation.
            command.add("-XX:-UseLoopInvariantCodeMotion");
        }
        command.add("-cp");
        command.add(OpenDoJaLaunchArgs.get("java.class.path"));
        command.add(mainClass);
        for (String arg : args) {
            command.add(arg);
        }
        return command;
    }

    private static List<String> buildVerifyFallbackCommand(String mainClass, String[] args) {
        List<String> command = new ArrayList<>();
        command.add(Path.of(OpenDoJaLaunchArgs.get("java.home"), "bin", "java").toString());
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.startsWith("-D" + OpenDoJaLaunchArgs.VERIFY_FALLBACK_APPLIED + "=")
                    || arg.startsWith("-Xverify:")
                    || arg.equals("-noverify")) {
                continue;
            }
            command.add(arg);
        }
        appendCurrentOpenDoJaProperties(command);
        appendFileEncoding(command);
        command.add("-D" + OpenDoJaLaunchArgs.VERIFY_FALLBACK_APPLIED + "=true");
        command.add("-Xverify:none");
        command.add("-cp");
        command.add(OpenDoJaLaunchArgs.get("java.class.path"));
        command.add(mainClass);
        for (String arg : args) {
            command.add(arg);
        }
        return command;
    }

    private static List<String> buildRequiredAddOpensCommand(String mainClass, String[] args) {
        List<String> command = new ArrayList<>();
        command.add(Path.of(OpenDoJaLaunchArgs.get("java.home"), "bin", "java").toString());
        command.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        appendCurrentOpenDoJaProperties(command);
        appendFileEncoding(command);
        if (!DoJaSjisCompatibility.hasRequiredAddOpens()) {
            command.addAll(DoJaSjisCompatibility.requiredAddOpensArguments());
        }
        if (!JamNamedModuleResourceBridge.hasRequiredAddOpens()) {
            command.add(JamNamedModuleResourceBridge.requiredAddOpensArgument());
        }
        command.add("-cp");
        command.add(OpenDoJaLaunchArgs.get("java.class.path"));
        command.add(mainClass);
        for (String arg : args) {
            command.add(arg);
        }
        return command;
    }

    private static void appendCurrentOpenDoJaProperties(List<String> command) {
        for (String name : System.getProperties().stringPropertyNames()) {
            if (!name.startsWith("opendoja.")) {
                continue;
            }
            if (name.equals(OpenDoJaLaunchArgs.LAUNCH_COMPAT_APPLIED)
                    || name.equals(OpenDoJaLaunchArgs.VERIFY_FALLBACK_APPLIED)) {
                continue;
            }
            String value = System.getProperty(name);
            if (value != null) {
                command.add("-D" + name + "=" + value);
            }
        }
    }

    private static void appendFileEncoding(List<String> command) {
        for (String arg : command) {
            if (arg.startsWith("-Dfile.encoding=")) {
                return;
            }
        }
        String fileEncoding = DoJaEncoding.explicitFileEncodingLaunchArgument();
        if (fileEncoding == null || fileEncoding.isBlank()) {
            fileEncoding = DoJaEncoding.defaultCharsetName();
        }
        if (fileEncoding == null || fileEncoding.isBlank()) {
            return;
        }
        command.add("-Dfile.encoding=" + fileEncoding);
    }

    private static boolean shouldDisableExplicitGc() {
        if (OpenDoJaLaunchArgs.getBoolean(OpenDoJaLaunchArgs.KEEP_EXPLICIT_GC)) {
            return false;
        }
        return explicitGcArgument() == null;
    }

    private static String explicitGcArgument() {
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.equals("-XX:+DisableExplicitGC") || arg.equals("-XX:-DisableExplicitGC")) {
                return arg;
            }
        }
        return null;
    }

    private static String explicitVerificationArgument() {
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.startsWith("-Xverify:") || arg.equals("-noverify")) {
                return arg;
            }
        }
        return null;
    }

    private static boolean shouldLimitHotSpotTier() {
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.startsWith("-XX:TieredStopAtLevel=")
                    || arg.equals("-XX:+TieredCompilation")
                    || arg.equals("-XX:-TieredCompilation")) {
                return false;
            }
        }
        return true;
    }

    private static boolean shouldDisableOnStackReplacement() {
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.equals("-XX:+UseOnStackReplacement")
                    || arg.equals("-XX:-UseOnStackReplacement")) {
                return false;
            }
        }
        return true;
    }

    private static boolean shouldDisableLoopInvariantCodeMotion() {
        if (!shouldLimitHotSpotTier()) {
            return false;
        }
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.equals("-XX:+UseLoopInvariantCodeMotion")
                    || arg.equals("-XX:-UseLoopInvariantCodeMotion")) {
                return false;
            }
        }
        return true;
    }
}
