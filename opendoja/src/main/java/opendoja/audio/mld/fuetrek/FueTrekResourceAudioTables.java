package opendoja.audio.mld.fuetrek;

import java.io.IOException;
import java.io.InputStream;

/**
 * Native top-level resource-audio lookup tables recovered from the FueTrek
 * lib002 path.
 */
public final class FueTrekResourceAudioTables {
    private static final String RESOURCE_NAME = "fuetrek-resource-audio-tables.bin";
    private static final int MAGIC = 0x54524146; // FART
    private static final int VERSION = 1;
    private static final Tables TABLES = loadTables();
    private static final int[] LEVEL_Q16 = TABLES.levelQ16;
    private static final int[] PAN_LEFT_Q16 = TABLES.panLeftQ16;
    private static final int[] PAN_RIGHT_Q16 = TABLES.panRightQ16;

    private FueTrekResourceAudioTables() {
    }

    public static int levelQ16(int value) {
        return LEVEL_Q16[clamp(value, 0, LEVEL_Q16.length - 1)];
    }

    public static int panLeftQ16(int value) {
        return PAN_LEFT_Q16[clamp(value, 0, PAN_LEFT_Q16.length - 1)];
    }

    public static int panRightQ16(int value) {
        return PAN_RIGHT_Q16[clamp(value, 0, PAN_RIGHT_Q16.length - 1)];
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private static Tables loadTables() {
        byte[] data = readResourceBytes(RESOURCE_NAME);
        Reader reader = new Reader(data);
        int magic = reader.u32();
        if (magic != MAGIC) {
            throw new IllegalStateException(
                "Invalid FueTrek resource-audio table magic: 0x" +
                    Integer.toHexString(magic));
        }
        int version = reader.u16();
        if (version != VERSION) {
            throw new IllegalStateException(
                "Unsupported FueTrek resource-audio table version: " +
                    version);
        }
        int levelCount = reader.u16();
        int panLeftCount = reader.u16();
        int panRightCount = reader.u16();
        reader.u16(); // reserved
        int[] levelQ16 = new int[levelCount];
        int[] panLeftQ16 = new int[panLeftCount];
        int[] panRightQ16 = new int[panRightCount];
        for (int i = 0; i < levelQ16.length; i++) {
            levelQ16[i] = reader.u16();
        }
        for (int i = 0; i < panLeftQ16.length; i++) {
            panLeftQ16[i] = reader.u16();
        }
        for (int i = 0; i < panRightQ16.length; i++) {
            panRightQ16[i] = reader.u16();
        }
        reader.expectFullyConsumed();
        if (levelCount != 128 || panLeftCount != 128 || panRightCount != 128) {
            throw new IllegalStateException(
                "Unexpected FueTrek resource-audio table sizes: level=" +
                    levelCount + " left=" + panLeftCount + " right=" +
                    panRightCount);
        }
        return new Tables(levelQ16, panLeftQ16, panRightQ16);
    }

    private static byte[] readResourceBytes(String resourceName) {
        try (InputStream input =
            FueTrekResourceAudioTables.class.getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new IllegalStateException(
                    "Missing FueTrek resource " + resourceName);
            }
            return opendoja.host.ByteStreams.readAll(input);
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Unable to load FueTrek resource " + resourceName, exception);
        }
    }

    private static final class Tables {
        private final int[] levelQ16;
        private final int[] panLeftQ16;
        private final int[] panRightQ16;

        private Tables(int[] levelQ16, int[] panLeftQ16, int[] panRightQ16) {
            this.levelQ16 = levelQ16;
            this.panLeftQ16 = panLeftQ16;
            this.panRightQ16 = panRightQ16;
        }
    }

    private static final class Reader {
        private final byte[] data;
        private int offset;

        private Reader(byte[] data) {
            this.data = data;
        }

        private int u16() {
            int value =
                (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8);
            offset += 2;
            return value;
        }

        private int u32() {
            int value = u16();
            return value | (u16() << 16);
        }

        private void expectFullyConsumed() {
            if (offset != data.length) {
                throw new IllegalStateException(
                    "FueTrek resource-audio table resource has trailing " +
                        "data: " + (data.length - offset));
            }
        }
    }
}
