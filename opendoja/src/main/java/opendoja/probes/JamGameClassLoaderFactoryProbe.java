package opendoja.probes;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class JamGameClassLoaderFactoryProbe {
    private JamGameClassLoaderFactoryProbe() {
    }

    public static void main(String[] args) throws Exception {
        verifyJarResourcesRemainReadableFromBangPath();
        System.out.println("Jam game class loader factory probe OK");
    }

    private static void verifyJarResourcesRemainReadableFromBangPath() throws Exception {
        Path root = Files.createTempDirectory("jam-game-loader!");
        Path jar = root.resolve("Game.jar");
        byte[] expected = "bang-path-resource".getBytes(StandardCharsets.ISO_8859_1);
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new ZipEntry("asset.bin"));
            output.write(expected);
            output.closeEntry();
        }

        try (URLClassLoader loader = createGameLoader(jar);
             InputStream in = loader.getResourceAsStream("asset.bin")) {
            check(in != null, "resource stream should resolve from jar paths containing '!'");
            check(Arrays.equals(expected, in.readAllBytes()),
                    "resource bytes should survive bang-path jar loading");
        }
    }

    private static URLClassLoader createGameLoader(Path jar) throws Exception {
        Class<?> factoryClass = Class.forName("opendoja.host.JamGameClassLoaderFactory");
        Method create = factoryClass.getDeclaredMethod("create", Path.class, ClassLoader.class);
        create.setAccessible(true);
        return (URLClassLoader) create.invoke(null, jar, JamGameClassLoaderFactoryProbe.class.getClassLoader());
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
