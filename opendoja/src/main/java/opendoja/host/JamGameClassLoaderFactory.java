package opendoja.host;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public final class JamGameClassLoaderFactory {
    /**
     * Host hook for choosing the class loader that defines JAM application classes.
     * On the desktop this stays {@code null} and a plain {@link URLClassLoader} over the
     * game jar is used. On Android the JVM bytecode must be converted to dex first, so the
     * host installs a provider that returns a {@code DexClassLoader} instead. Returning
     * {@code null} from the provider falls back to the default jar loader.
     */
    public interface GameClassLoaderProvider {
        ClassLoader create(Path gameJarPath, ClassLoader parent) throws Exception;
    }

    private static volatile GameClassLoaderProvider provider;

    private JamGameClassLoaderFactory() {
    }

    public static void setProvider(GameClassLoaderProvider newProvider) {
        provider = newProvider;
    }

    static ClassLoader createLoader(Path gameJarPath, ClassLoader parent) {
        GameClassLoaderProvider current = provider;
        if (current != null) {
            try {
                ClassLoader provided = current.create(gameJarPath, parent);
                if (provided != null) {
                    return provided;
                }
            } catch (Exception exception) {
                OpenDoJaLog.warn(JamGameClassLoaderFactory.class,
                        () -> "Game class loader provider failed, falling back to jar loader: " + exception);
            }
        }
        return create(gameJarPath, parent);
    }

    static URLClassLoader create(Path gameJarPath, ClassLoader parent) {
        if (gameJarPath == null) {
            throw new IllegalArgumentException("gameJarPath must not be null");
        }
        try {
            String encodedFileUrl = gameJarPath.toAbsolutePath().normalize().toUri().toASCIIString().replace("!", "%21");
            return new URLClassLoader(new URL[]{new URL(encodedFileUrl)}, parent);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not create class loader for " + gameJarPath, e);
        }
    }
}
