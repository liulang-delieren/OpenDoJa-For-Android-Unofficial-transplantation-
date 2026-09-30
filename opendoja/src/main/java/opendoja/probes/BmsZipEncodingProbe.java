package opendoja.probes;

import com.nttdocomo.util.JarInflater;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BmsZipEncodingProbe {
    private static final Path BMS_ZIP = Path.of("resources/sample_games/BMS Player/BMS.ZIP");
    private static final String ASCII_ENTRY = "kar_2dx11_sasori_5.bms";
    private static final String SHIFT_JIS_ENTRY = "yassu_\u6642\u306e\u56de\u5ECA_n.bms";

    private BmsZipEncodingProbe() {
    }

    public static void main(String[] args) throws Exception {
        byte[] data = Files.readAllBytes(BMS_ZIP);
        JarInflater inflater = new JarInflater(data);
        try {
            check(inflater.getSize(ASCII_ENTRY) > 0, "ASCII entry should remain readable");
            check(inflater.getSize(SHIFT_JIS_ENTRY) > 0, "Shift_JIS entry should decode under the DoJa charset");

            try (InputStream in = inflater.getInputStream(SHIFT_JIS_ENTRY)) {
                check(in != null, "Shift_JIS entry stream should be available");
                check(in.readNBytes(16).length > 0, "Shift_JIS entry should yield data");
            }
        } finally {
            inflater.close();
        }

        System.out.println("BMS ZIP encoding probe OK");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
