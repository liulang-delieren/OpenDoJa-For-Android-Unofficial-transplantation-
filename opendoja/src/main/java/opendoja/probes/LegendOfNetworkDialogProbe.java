package opendoja.probes;

import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.Image;
import opendoja.host.DoJaEncoding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class LegendOfNetworkDialogProbe {
    private static final String RESOURCE = "/data/new/sys_mes2.dat";
    private static final int RECORD_SIZE = 90;
    private static final int LINE_SIZE = 30;

    private LegendOfNetworkDialogProbe() {
    }

    public static void main(String[] args) throws IOException {
        byte[] line = firstLine();
        check(line.length >= 4, "expected at least two multibyte characters in the first LoN system line");

        String malformed = new String(line, 0, 3, DoJaEncoding.DEFAULT_CHARSET);
        String expectedTrimmed = new String(line, 0, 2, DoJaEncoding.DEFAULT_CHARSET);
        String complete = new String(line, 0, 4, DoJaEncoding.DEFAULT_CHARSET);

        check(malformed.endsWith("\uFFFD"),
                "expected a malformed trailing decode for the 3-byte LoN prefix but got " + quote(malformed));

        Font font = Font.getFont(Font.FACE_SYSTEM | Font.STYLE_PLAIN, 20);
        check(renderHash(font, malformed) == renderHash(font, expectedTrimmed),
                "malformed LoN prefix should render like the completed leading character only");
        check(font.stringWidth(malformed) == font.stringWidth(expectedTrimmed),
                "malformed LoN prefix width should match the completed leading character");
        check(renderHash(font, complete) != renderHash(font, expectedTrimmed),
                "completed LoN prefix should render more than the trimmed leading character");

        System.out.println("Legend of Network dialog probe OK"
                + " charset=" + DoJaEncoding.defaultCharsetName()
                + " malformed=" + quote(malformed)
                + " trimmed=" + quote(expectedTrimmed)
                + " complete=" + quote(complete));
    }

    private static byte[] firstLine() throws IOException {
        byte[] record = new byte[RECORD_SIZE];
        try (InputStream in = LegendOfNetworkDialogProbe.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IOException("Missing Legend of Network resource: " + RESOURCE);
            }
            int read = in.readNBytes(record, 0, RECORD_SIZE);
            if (read != RECORD_SIZE) {
                throw new IOException("Expected " + RECORD_SIZE + " bytes but read " + read);
            }
        }
        int lineLength = 0;
        while (lineLength < LINE_SIZE && record[lineLength] != 0) {
            lineLength++;
        }
        return Arrays.copyOf(record, lineLength);
    }

    private static int renderHash(Font font, String text) {
        Image image = Image.createImage(64, 64);
        Graphics graphics = image.getGraphics();
        graphics.setFont(font);
        graphics.setColor(0xFFFFFFFF);
        graphics.drawString(text, 0, font.getAscent());
        int hash = 1;
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                hash = 31 * hash + graphics.getPixel(x, y);
            }
        }
        return hash;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static String quote(String value) {
        return '"' + value + '"';
    }
}
