package opendoja.android;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;

/**
 * On-device jar-to-dex conversion for JAM games, based on the dexlib (dx) port from
 * JL-Mod/J2MELoader. ART cannot execute JVM bytecode, so every game jar is converted to
 * a dex file once and then loaded with a {@link DexClassLoader}.
 */
public final class JamDexHost {
    private static final String TAG = "OpenDoJa";

    private JamDexHost() {
    }

    public static File dexFor(File jarFile) {
        String name = jarFile.getName();
        int dot = name.lastIndexOf('.');
        String base = dot < 0 ? name : name.substring(0, dot);
        return new File(jarFile.getParentFile(), base + ".dex");
    }

    /** Returns the dex file for the jar, converting it first when missing or stale. */
    public static File ensureDex(File jarFile) throws IOException {
        File dexFile = dexFor(jarFile);
        if (dexFile.exists() && dexFile.length() > 0 && dexFile.lastModified() >= jarFile.lastModified()) {
            return dexFile;
        }
        if (dexFile.exists() && !dexFile.setWritable(true)) {
            //noinspection ResultOfMethodCallIgnored
            dexFile.delete();
        }
        long start = SystemClock.uptimeMillis();
        com.android.dx.command.dexer.Main.main(new String[]{
                "--no-optimize",
                "--output=" + dexFile.getAbsolutePath(),
                jarFile.getAbsolutePath()});
        long elapsed = SystemClock.uptimeMillis() - start;
        if (!dexFile.exists() || dexFile.length() == 0) {
            throw new IOException("jar2dex produced no output for " + jarFile);
        }
        // ART refuses to open dex files that are still writable on newer Android builds.
        //noinspection ResultOfMethodCallIgnored
        dexFile.setReadOnly();
        Log.i(TAG, "jar2dex " + jarFile.getName() + " -> " + dexFile.getName()
                + " (" + dexFile.length() + " bytes) in " + elapsed + "ms");
        return dexFile;
    }

    public static ClassLoader createLoader(File dexFile, ClassLoader parent, Context context) {
        File optimized = new File(context.getCodeCacheDir(), "jam-dex");
        //noinspection ResultOfMethodCallIgnored
        optimized.mkdirs();
        if (dexFile.canWrite()) {
            if (!dexFile.setReadOnly()) {
                Log.w(TAG, "cannot mark dex read-only: " + dexFile);
            }
        }
        return new GameResourceClassLoader(dexFile.getAbsolutePath(), optimized.getAbsolutePath(),
                null, parent, siblingJar(dexFile));
    }

    /** The game jar that sits next to the converted dex (same basename). */
    private static File siblingJar(File dexFile) {
        String name = dexFile.getName();
        int dot = name.lastIndexOf('.');
        String base = dot < 0 ? name : name.substring(0, dot);
        File jar = new File(dexFile.getParentFile(), base + ".jar");
        return jar.isFile() ? jar : null;
    }

    /**
     * DexClassLoader only exposes entries of the dex/zip files on its dex path; game assets
     * (.dat images etc.) live inside the original game jar, which is not on that path.
     * The desktop host uses a URLClassLoader over the jar, where resources resolve natively.
     * This loader restores that behaviour by falling back to the game jar for resource lookups.
     */
    private static final class GameResourceClassLoader extends DexClassLoader {
        private final File jarFile;
        private ZipFile zipFile;

        GameResourceClassLoader(String dexPath, String optimizedDirectory,
                                String librarySearchPath, ClassLoader parent, File jarFile) {
            super(dexPath, optimizedDirectory, librarySearchPath, parent);
            this.jarFile = jarFile;
        }

        @Override
        public URL getResource(String name) {
            URL resource = super.getResource(name);
            if (resource != null) {
                return resource;
            }
            if (jarEntry(name) != null) {
                try {
                    return new URL("jar:file:" + jarFile.getAbsolutePath() + "!/" + name);
                } catch (java.net.MalformedURLException ignored) {
                }
            }
            return null;
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            InputStream stream = super.getResourceAsStream(name);
            if (stream != null) {
                return stream;
            }
            try {
                ZipEntry entry = jarEntry(name);
                return entry == null ? null : openZip().getInputStream(entry);
            } catch (IOException exception) {
                Log.w(TAG, "game jar resource read failed: " + name, exception);
                return null;
            }
        }

        private ZipEntry jarEntry(String name) {
            if (jarFile == null || name == null || name.isEmpty()) {
                return null;
            }
            try {
                return openZip().getEntry(name);
            } catch (IOException exception) {
                Log.w(TAG, "cannot open game jar " + jarFile, exception);
                return null;
            }
        }

        private ZipFile openZip() throws IOException {
            synchronized (this) {
                if (zipFile == null) {
                    zipFile = new ZipFile(jarFile);
                }
                return zipFile;
            }
        }
    }
}
