package opendoja.host;

import opendoja.compat.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Makes {@code Charset.forName("SJIS")} and {@code Charset.forName("Shift_JIS")} resolve to
 * OpenDoJa's {@code SJIS_i} implementation during JAM launches.
 *
 * <p>Java consults the built-in standard charset provider before installed providers, so the
 * OpenDoJa {@link java.nio.charset.spi.CharsetProvider} would otherwise never be selected for
 * these historical names.</p>
 */
final class DoJaSjisCompatibility {
    private static final String OPEN_CHARSET_PACKAGE = "--add-opens=java.base/java.nio.charset=ALL-UNNAMED";
    private static final String OPEN_STANDARD_PROVIDER_PACKAGE = "--add-opens=java.base/sun.nio.cs=ALL-UNNAMED";
    private static final String SHIFT_JIS_CANONICAL_KEY = "shift_jis";
    private static volatile boolean installed;

    private DoJaSjisCompatibility() {
    }

    static void install() {
        if (installed || !hasRequiredAddOpens()) {
            return;
        }
        synchronized (DoJaSjisCompatibility.class) {
            if (installed || !hasRequiredAddOpens()) {
                return;
            }
            try {
                CharsetProvider standardProvider = standardProvider();
                // Remove only the built-in Shift_JIS binding. Charset lookup will then fall
                // through to the OpenDoJa provider registered for SJIS/SJIS_i aliases.
                replaceStandardProviderClassMap(standardProvider);
                clearStandardProviderCache(standardProvider);
                clearCharsetLookupCaches();
                installed = true;
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not install DoJa SJIS compatibility", exception);
            }
        }
    }

    static boolean hasRequiredAddOpens() {
        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        return inputArguments.contains(OPEN_CHARSET_PACKAGE) && inputArguments.contains(OPEN_STANDARD_PROVIDER_PACKAGE);
    }

    static List<String> requiredAddOpensArguments() {
        return List.of(OPEN_CHARSET_PACKAGE, OPEN_STANDARD_PROVIDER_PACKAGE);
    }

    private static CharsetProvider standardProvider() throws ReflectiveOperationException {
        Field field = Charset.class.getDeclaredField("standardProvider");
        field.setAccessible(true);
        return (CharsetProvider) field.get(null);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> standardProviderClassMap(CharsetProvider provider) throws ReflectiveOperationException {
        Field field = provider.getClass().getDeclaredField("classMap");
        field.setAccessible(true);
        Map<String, String> classMap = (Map<String, String>) field.get(provider);
        if (classMap == null) {
            java.lang.reflect.Method method = provider.getClass().getDeclaredMethod("classMap");
            method.setAccessible(true);
            classMap = (Map<String, String>) method.invoke(provider);
        }
        return classMap;
    }

    private static void replaceStandardProviderClassMap(CharsetProvider provider) throws ReflectiveOperationException {
        Field field = provider.getClass().getDeclaredField("classMap");
        field.setAccessible(true);
        Map<String, String> classMap = standardProviderClassMap(provider);
        // Some JDKs expose this table as an immutable PreHashedMap, so patch a mutable copy and
        // swap the whole field back in instead of mutating in place.
        LinkedHashMap<String, String> patched = new LinkedHashMap<>(classMap);
        patched.remove(SHIFT_JIS_CANONICAL_KEY);
        field.set(provider, patched);
    }

    private static void clearStandardProviderCache(CharsetProvider provider) throws ReflectiveOperationException {
        Field field = provider.getClass().getDeclaredField("cache");
        field.setAccessible(true);
        field.set(provider, null);
    }

    private static void clearCharsetLookupCaches() throws ReflectiveOperationException {
        // Charset keeps a tiny process-wide lookup cache in addition to the provider cache.
        Field cache1 = Charset.class.getDeclaredField("cache1");
        cache1.setAccessible(true);
        cache1.set(null, null);
        Field cache2 = Charset.class.getDeclaredField("cache2");
        cache2.setAccessible(true);
        cache2.set(null, null);
    }
}
