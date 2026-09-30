package opendoja.host;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public final class OpenDoJaPaths {
    private static volatile Path hostDataRootOverride;

    private OpenDoJaPaths() {
    }

    /**
     * Pins the host data root to a writable location. On Android the default
     * artifact-derived root lands inside the read-only APK directory, which makes
     * SD-card storage initialization fail and titles spin forever on their
     * "reading SD" screen, so the embedder points this at its files directory.
     */
    public static void setHostDataRoot(Path path) {
        hostDataRootOverride = path == null ? null : path.toAbsolutePath().normalize();
    }

    public static Path hostDataRoot() {
        Path override = hostDataRootOverride;
        if (override != null) {
            return override;
        }
        return artifactDirectory().resolve(".opendoja").normalize();
    }

    public static Path artifactDirectory() {
        try {
            URL location = OpenDoJaPaths.class.getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                Path path = Path.of(location.toURI()).toAbsolutePath().normalize();
                if (Files.isRegularFile(path)) {
                    Path parent = path.getParent();
                    return parent == null ? path : parent;
                }
                if (Files.isDirectory(path)) {
                    return path;
                }
            }
        } catch (URISyntaxException | IllegalArgumentException | SecurityException ignored) {
        }
        return Path.of("").toAbsolutePath().normalize();
    }
}
