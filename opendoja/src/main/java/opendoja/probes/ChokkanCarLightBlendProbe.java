package opendoja.probes;

import opendoja.g3d.MascotLoader;
import opendoja.g3d.MbacModel;
import opendoja.g3d.SoftwareTexture;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ChokkanCarLightBlendProbe {
    private static final Path CHOKKAN_SP = Path.of("resources/sample_games/Chokkan___Shutokou_Battle__C1_Loop_doja/sp/Chokkan___Shutokou_Battle__C1_Loop.sp");
    private static final int[] CHOKKAN_SP_SIZES = {1024, 174080, 65536, 73728, 515584, 5120, 512, 8192};

    private ChokkanCarLightBlendProbe() {
    }

    public static void main(String[] args) throws Exception {
        DemoLog.enableInfoLogging();

        List<CarPack> carPacks = loadChokkanCarPacks();
        int lightBlend06 = 0;
        int lightBlend04 = 0;
        int lightBlend02 = 0;
        int lightTransparent = 0;
        int opaqueBlackInLightBounds = 0;
        int transparentInLightBounds = 0;
        for (CarPack carPack : carPacks) {
            lightBlend02 += countBlendMode(carPack.figure(4), 0x02) + countBlendMode(carPack.figure(5), 0x02);
            lightBlend04 += countBlendMode(carPack.figure(4), 0x04) + countBlendMode(carPack.figure(5), 0x04);
            lightBlend06 += countBlendMode(carPack.figure(4), 0x06) + countBlendMode(carPack.figure(5), 0x06);
            lightTransparent += countTransparent(carPack.figure(4)) + countTransparent(carPack.figure(5));
            opaqueBlackInLightBounds += countLightBoundPixels(carPack, PixelKind.OPAQUE_BLACK);
            transparentInLightBounds += countLightBoundPixels(carPack, PixelKind.TRANSPARENT);
        }
        if (lightBlend06 == 0 && lightBlend04 == 0) {
            throw new IllegalStateException("Chokkan car light figures did not expose any non-normal mascot blend materials");
        }
        if (opaqueBlackInLightBounds == 0) {
            throw new IllegalStateException("Chokkan car light UV bounds did not expose any opaque black backdrop texels");
        }
        DemoLog.info(ChokkanCarLightBlendProbe.class,
                "car light material counts blend02=" + lightBlend02
                        + " blend04=" + lightBlend04
                        + " blend06=" + lightBlend06
                        + " transparent=" + lightTransparent
                        + " opaqueBlackInLightBounds=" + opaqueBlackInLightBounds
                        + " transparentInLightBounds=" + transparentInLightBounds
                        + " packs=" + carPacks.size());
    }

    private static List<CarPack> loadChokkanCarPacks() throws IOException {
        byte[] scratchpad = readPackedScratchpad(CHOKKAN_SP, CHOKKAN_SP_SIZES, 3);
        List<byte[]> records = readChecksumRecords(scratchpad);
        List<CarPack> carPacks = new ArrayList<>();
        for (byte[] record : records) {
            byte[] carBin = unzipEntry(record, "car.bin");
            if (carBin != null) {
                try {
                    carPacks.add(parseCarPack(carBin));
                } catch (IOException ignored) {
                    // Chokkan stores multiple car payload layouts in this segment. Keep only the
                    // packs that match the figure layout used by the light-bearing runtime path.
                }
            }
        }
        if (carPacks.isEmpty()) {
            throw new IllegalStateException("Failed to locate any Chokkan car.bin payloads");
        }
        return carPacks;
    }

    private static CarPack parseCarPack(byte[] carBin) throws IOException {
        if (carBin.length < 6) {
            throw new IOException("car.bin too small");
        }
        MbacModel[] figures = new MbacModel[6];
        int position = 2;
        int length0 = readBigEndianInt(carBin, position);
        position += 4;
        figures[0] = loadFigureSlice(carBin, position, length0);
        position += length0;
        for (int slot = 1; slot <= 3; slot++) {
            int figureLength = readBigEndianInt(carBin, position);
            position += 4;
            figures[slot] = loadFigureSlice(carBin, position, figureLength);
            position += figureLength;
            int actionLength = readBigEndianInt(carBin, position);
            position += 4 + actionLength;
        }
        for (int slot = 4; slot <= 5; slot++) {
            int figureLength = readBigEndianInt(carBin, position);
            position += 4;
            figures[slot] = loadFigureSlice(carBin, position, figureLength);
            position += figureLength;
        }
        List<SoftwareTexture> textures = new ArrayList<>();
        while (position + 4 <= carBin.length) {
            int textureLength = readBigEndianInt(carBin, position);
            position += 4;
            if (textureLength <= 0 || position + textureLength > carBin.length) {
                break;
            }
            byte[] bytes = Arrays.copyOfRange(carBin, position, position + textureLength);
            position += textureLength;
            if (bytes.length >= 2 && bytes[0] == 'B' && bytes[1] == 'M') {
                textures.add(new SoftwareTexture(bytes, true));
            }
        }
        return new CarPack(figures, textures.toArray(SoftwareTexture[]::new));
    }

    private static MbacModel loadFigureSlice(byte[] source, int position, int length) throws IOException {
        if (length <= 0 || position < 0 || position + length > source.length) {
            throw new IOException("Invalid car figure slice");
        }
        byte[] bytes = Arrays.copyOfRange(source, position, position + length);
        return MascotLoader.loadFigure(bytes);
    }

    private static int countBlendMode(MbacModel model, int blendMode) {
        if (model == null) {
            return 0;
        }
        int count = 0;
        for (MbacModel.Polygon polygon : model.polygons()) {
            if (polygon.blendMode() == blendMode) {
                count++;
            }
        }
        return count;
    }

    private static int countTransparent(MbacModel model) {
        if (model == null) {
            return 0;
        }
        int count = 0;
        for (MbacModel.Polygon polygon : model.polygons()) {
            if (polygon.transparent()) {
                count++;
            }
        }
        return count;
    }

    private static int countLightBoundPixels(CarPack carPack, PixelKind kind) {
        SoftwareTexture[] textures = carPack.textures();
        if (textures.length == 0) {
            return 0;
        }
        int count = 0;
        for (int slot = 4; slot <= 5; slot++) {
            MbacModel model = carPack.figure(slot);
            if (model == null) {
                continue;
            }
            for (MbacModel.Polygon polygon : model.polygons()) {
                float[] uv = polygon.textureCoords();
                if (uv == null || uv.length < 6) {
                    continue;
                }
                SoftwareTexture texture = textures[Math.max(0, Math.min(polygon.textureIndex(), textures.length - 1))];
                int minU = clamp((int) Math.floor(minCoord(uv, 0)), 0, texture.width() - 1);
                int maxU = clamp((int) Math.ceil(maxCoord(uv, 0)), 0, texture.width() - 1);
                int minV = clamp((int) Math.floor(minCoord(uv, 1)), 0, texture.height() - 1);
                int maxV = clamp((int) Math.ceil(maxCoord(uv, 1)), 0, texture.height() - 1);
                for (int y = minV; y <= maxV; y++) {
                    for (int x = minU; x <= maxU; x++) {
                        int pixel = texture.sampleColor(x, y, false);
                        if (kind.matches(pixel)) {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    private static float minCoord(float[] uv, int axis) {
        float min = Float.POSITIVE_INFINITY;
        for (int i = axis; i < uv.length; i += 2) {
            min = Math.min(min, uv[i]);
        }
        return min;
    }

    private static float maxCoord(float[] uv, int axis) {
        float max = Float.NEGATIVE_INFINITY;
        for (int i = axis; i < uv.length; i += 2) {
            max = Math.max(max, uv[i]);
        }
        return max;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static byte[] readPackedScratchpad(Path path, int[] sizes, int index) throws IOException {
        byte[] data = Files.readAllBytes(path);
        int offset = 64;
        for (int i = 0; i < index; i++) {
            offset += sizes[i];
        }
        return Arrays.copyOfRange(data, offset, offset + sizes[index]);
    }

    private static List<byte[]> readChecksumRecords(byte[] scratchpad) {
        List<byte[]> records = new ArrayList<>();
        int position = 0;
        while (position + 4 <= scratchpad.length) {
            int length = readBigEndianInt(scratchpad, position);
            if (length <= 2 || position + 4 + length - 2 > scratchpad.length) {
                break;
            }
            records.add(Arrays.copyOfRange(scratchpad, position + 4, position + 4 + length - 2));
            position += 4 + length;
        }
        return records;
    }

    private static byte[] unzipEntry(byte[] zipBytes, String name) throws IOException {
        for (int offset = 0; offset + 4 <= zipBytes.length; offset++) {
            if (zipBytes[offset] != 'P' || zipBytes[offset + 1] != 'K' || zipBytes[offset + 2] != 3 || zipBytes[offset + 3] != 4) {
                continue;
            }
            try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes, offset, zipBytes.length - offset))) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    byte[] data = zip.readAllBytes();
                    if (entry.getName().equals(name)) {
                        return data;
                    }
                }
            } catch (IOException ignored) {
                // False-positive local ZIP headers inside compressed payloads are ignored.
            }
        }
        return null;
    }

    private static int readBigEndianInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24)
                | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8)
                | (data[offset + 3] & 0xFF);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private record CarPack(MbacModel[] figures, SoftwareTexture[] textures) {
        private MbacModel figure(int index) {
            return figures[index];
        }
    }

    private enum PixelKind {
        OPAQUE_BLACK {
            @Override
            boolean matches(int pixel) {
                return ((pixel >>> 24) & 0xFF) == 0xFF && (pixel & 0x00FFFFFF) == 0;
            }
        },
        TRANSPARENT {
            @Override
            boolean matches(int pixel) {
                return ((pixel >>> 24) & 0xFF) == 0;
            }
        };

        abstract boolean matches(int pixel);
    }
}
