package opendoja.host;

import opendoja.compat.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Shared DoJa text-encoding resolution.
 */
public final class DoJaEncoding {
    // Probe these in order and use the first charset the host JVM exposes.
    private static final List<String> DEFAULT_ENCODING_CANDIDATES = List.of("windows-31j", "Shift_JIS", "MS932", "UTF-8");
    public static final Charset DEFAULT_CHARSET = resolveDefaultCharset();

    private DoJaEncoding() {
    }

    public static Charset defaultCharset() {
        return DEFAULT_CHARSET;
    }

    public static String defaultCharsetName() {
        return DEFAULT_CHARSET.name();
    }

    public static List<String> defaultEncodingCandidates() {
        return DEFAULT_ENCODING_CANDIDATES;
    }

    private static Charset resolveDefaultCharset() {
        if (explicitFileEncodingLaunchArgument() != null) {
            return Charset.defaultCharset();
        }
        for (String candidate : DEFAULT_ENCODING_CANDIDATES) {
            try {
                return Charset.forName(candidate);
            } catch (RuntimeException ignored) {
            }
        }
        return Charset.defaultCharset();
    }

    public static String explicitFileEncodingLaunchArgument() {
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.startsWith("-Dfile.encoding=")) {
                String value = arg.substring("-Dfile.encoding=".length()).trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * On hosts that booted without {@code -Dfile.encoding} (the Android port cannot pass JVM
     * flags), {@link Charset#defaultCharset()} caches UTF-8 during process startup, so DoJa
     * titles that decode with {@code new String(byte[])} turn into mojibake. Overwrite the
     * cached charset with the DoJa {@code SJIS_i} implementation before game code runs.
     * Desktop launches that already set {@code -Dfile.encoding} keep their resolved charset.
     */
    public static void installDefaultCharsetOverride() {
        try {
            if (!"UTF-8".equalsIgnoreCase(Charset.defaultCharset().name())) {
                return;
            }
            attemptHiddenApiExemption();
            java.lang.reflect.Field field = Charset.class.getDeclaredField("defaultCharset");
            field.setAccessible(true);
            field.set(null, DoJaSjisCharsetProvider.sjisCharset());
            Charset effective = Charset.defaultCharset();
            OpenDoJaLog.info(DoJaEncoding.class,
                    () -> "default charset override installed: " + effective.name());
        } catch (Throwable throwable) {
            StringBuilder fields = new StringBuilder();
            for (java.lang.reflect.Field field : Charset.class.getDeclaredFields()) {
                fields.append(field.getName()).append(':')
                        .append(field.getType().getSimpleName()).append(' ');
            }
            OpenDoJaLog.warn(DoJaEncoding.class,
                    () -> "cannot override default charset cache: " + throwable
                            + "; Charset fields: " + fields);
        }
    }

    /**
     * Best-effort meta-reflection exemption so {@code Charset.defaultCharset}'s backing field
     * stays visible on devices that filter non-SDK interfaces. Patched builds simply throw
     * and the caller falls back to logging the field list.
     */
    private static void attemptHiddenApiExemption() {
        try {
            java.lang.reflect.Method getDeclaredMethod = Class.class
                    .getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
            java.lang.reflect.Method forName = Class.class.getDeclaredMethod("forName", String.class);
            Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
            java.lang.reflect.Method getRuntime =
                    (java.lang.reflect.Method) getDeclaredMethod.invoke(
                            vmRuntimeClass, "getRuntime", new Class[0]);
            java.lang.reflect.Method setHiddenApiExemptions = (java.lang.reflect.Method) getDeclaredMethod
                    .invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
            Object vmRuntime = getRuntime.invoke(null);
            setHiddenApiExemptions.invoke(vmRuntime, new Object[]{new String[]{"L"}});
            OpenDoJaLog.info(DoJaEncoding.class,
                    () -> "hidden API exemptions installed");
        } catch (Throwable throwable) {
            // Not fatal: field access may still succeed (or fail with a useful log below).
            OpenDoJaLog.warn(DoJaEncoding.class,
                    () -> "hidden API exemption failed: " + throwable);
        }
    }
}
