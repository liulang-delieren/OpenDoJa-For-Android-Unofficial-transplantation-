package opendoja.host;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class JamNamedModuleResourceBridgeProbe {
    private JamNamedModuleResourceBridgeProbe() {
    }

    public static void main(String[] args) {
        System.out.println("Jam named-module resource bridge probe skipped: desktop JVM only");
    }
    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
