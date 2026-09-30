package opendoja.probes;

import com.nttdocomo.ui.MediaImage;
import com.nttdocomo.ui.MediaManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class ReversiScratchpadJpegProbe {
    private static final int DECLARED_SCRATCHPAD_SIZE = 204_800;
    private static final int HEADER_BYTES = 64;
    private static final int HEADER_ENTRIES = 16;
    private static final int RESOURCE_TABLE_OFFSET = 128;
    private static final int FIRST_JPEG_INDEX = 135;
    private static final int LAST_JPEG_INDEX = 142;
    private static final Path[] SCRATCHPADS = {
            Path.of("resources/sample_games/Underwater Board Game Match/N2701 Version/reversi.sp"),
            Path.of("resources/sample_games/Underwater Board Game Match/N2051 Version/reversi.sp")
    };

    private ReversiScratchpadJpegProbe() {
    }

    public static void main(String[] args) throws Exception {
        for (Path scratchpad : SCRATCHPADS) {
            verifyScratchpad(scratchpad);
        }
        System.out.println("Reversi scratchpad JPEG probe OK");
    }

    private static void verifyScratchpad(Path scratchpad) throws Exception {
        byte[] fileBytes = Files.readAllBytes(scratchpad);
        int payloadStart = payloadStart(fileBytes);
        byte[] payload = Arrays.copyOfRange(fileBytes, payloadStart, payloadStart + DECLARED_SCRATCHPAD_SIZE);
        int[] positions = parseResourcePositions(payload);
        for (int index = FIRST_JPEG_INDEX; index <= LAST_JPEG_INDEX; index++) {
            byte[] exactBytes = Arrays.copyOfRange(payload, positions[index], positions[index + 1]);
            byte[] trailingBytes = Arrays.copyOfRange(payload, positions[index], payload.length);
            MediaImage exactImage = MediaManager.getImage(exactBytes);
            MediaImage trailingImage = MediaManager.getImage(trailingBytes);
            check(exactImage.getImage().getWidth() == trailingImage.getImage().getWidth(),
                    scratchpad + " resource " + index + " width mismatch");
            check(exactImage.getImage().getHeight() == trailingImage.getImage().getHeight(),
                    scratchpad + " resource " + index + " height mismatch");
        }
    }

    private static int payloadStart(byte[] bytes) {
        if (bytes.length == DECLARED_SCRATCHPAD_SIZE) {
            return 0;
        }
        if (bytes.length >= DECLARED_SCRATCHPAD_SIZE + HEADER_BYTES && hasMatchingHeader(bytes)) {
            return HEADER_BYTES;
        }
        throw new IllegalStateException("Unexpected scratchpad layout: " + bytes.length);
    }

    private static boolean hasMatchingHeader(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, HEADER_BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (int index = 0; index < HEADER_ENTRIES; index++) {
            int actual = buffer.getInt(index * Integer.BYTES);
            int expected = index == 0 ? DECLARED_SCRATCHPAD_SIZE : -1;
            if (actual != expected) {
                return false;
            }
        }
        return true;
    }

    private static int[] parseResourcePositions(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN);
        int count = buffer.getShort(RESOURCE_TABLE_OFFSET) & 0xffff;
        int[] positions = new int[count + 1];
        int position = RESOURCE_TABLE_OFFSET + ((count + 1) * Short.BYTES);
        positions[0] = position;
        for (int index = 0; index < count; index++) {
            int length = buffer.getShort(RESOURCE_TABLE_OFFSET + Short.BYTES + (index * Short.BYTES)) & 0xffff;
            position += length;
            positions[index + 1] = position;
        }
        return positions;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
