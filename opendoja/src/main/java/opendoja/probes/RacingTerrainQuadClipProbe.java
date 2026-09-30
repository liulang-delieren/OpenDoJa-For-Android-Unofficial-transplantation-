package opendoja.probes;

import opendoja.g3d.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class RacingTerrainQuadClipProbe {
    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;
    private static final int NEAR = 20;
    private static final Path RIDGE_SP = Path.of("resources/sample_games/Ridge_Racers_doja/NEC_Version/sp/Ridge_Racers.sp");
    private static final int[] RIDGE_SP_SIZES = {56, 200, 409344};
    private static final Path CHOKKAN_SP = Path.of("resources/sample_games/Chokkan___Shutokou_Battle__C1_Loop_doja/sp/Chokkan___Shutokou_Battle__C1_Loop.sp");
    private static final int[] CHOKKAN_SP_SIZES = {1024, 174080, 65536, 73728, 515584, 5120, 512, 8192};

    private RacingTerrainQuadClipProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        DemoLog.enableInfoLogging();

        List<BinaryModel> ridgeModels = loadRidgeModels();
        List<BinaryModel> chokkanModels = loadChokkanModels();
        DemoLog.info(RacingTerrainQuadClipProbe.class, terrainStats("Ridge", ridgeModels));
        DemoLog.info(RacingTerrainQuadClipProbe.class, terrainStats("Chokkan", chokkanModels));

        Divergence ridge = compareQuadClipCase(ridgeModels, "map.dat#0", 10, 0, 1, 2);
        Divergence chokkan = compareQuadClipCase(chokkanModels, "sp4@63837/mbac.bin#0.4940.0", 3, 0, 1, 2);
        if (ridge.differentPixels() != 0 || chokkan.differentPixels() != 0) {
            throw new IllegalStateException("MBAC clipped quad topology diverges from triangle-expanded reference: "
                    + ridge.describe() + "; " + chokkan.describe());
        }

        DemoLog.info(RacingTerrainQuadClipProbe.class, "binary circuit clipped quads match triangle-expanded MBAC topology");
    }

    private static Divergence compareQuadClipCase(List<BinaryModel> models, String source, int polygonIndex,
                                                  int xAxis, int yAxis, int zAxis) throws Exception {
        for (BinaryModel binaryModel : models) {
            if (!binaryModel.source().equals(source)) {
                continue;
            }
            Candidate candidate = Candidate.from(binaryModel, polygonIndex, xAxis, yAxis, zAxis);
            if (candidate == null) {
                throw new IllegalStateException(binaryModel.title() + " " + source
                        + " polygon=" + polygonIndex + " did not cross the near plane in the probe transform");
            }
            SoftwareTexture texture = makeTexture();
            boolean textured = binaryModel.model().polygons()[polygonIndex].textureCoords() != null;
            int[] quad = render(candidate.quadModel(), texture, textured);
            int[] triangles = render(candidate.triangleModel(), texture, textured);
            return new Divergence(candidate, countDifferentPixels(quad, triangles),
                    countOpaquePixels(quad), countOpaquePixels(triangles));
        }
        throw new IllegalStateException("Missing decoded MBAC model " + source);
    }

    private static int[] render(MbacModel model, SoftwareTexture texture, boolean textured) {
        MascotFigure figure = new MascotFigure(model);
        if (textured) {
            figure.setTexture(texture);
        }
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            Software3DContext context = new Software3DContext();
            context.setOptScreenCenter(WIDTH / 2, HEIGHT / 2);
            context.setOptPerspective(NEAR, 8191, 603);
            context.setOptViewTransform(identity());
            context.renderOptFigure(graphics, image, 0, 0, WIDTH, HEIGHT, figure);
        } finally {
            graphics.dispose();
        }
        return image.getRGB(0, 0, WIDTH, HEIGHT, null, 0, WIDTH);
    }

    private static List<BinaryModel> loadRidgeModels() throws IOException {
        byte[] scratchpad = readPackedScratchpad(RIDGE_SP, RIDGE_SP_SIZES, 2);
        List<byte[]> records = readChecksumRecords(scratchpad);
        List<BinaryModel> models = new ArrayList<>();
        if (!records.isEmpty()) {
            byte[] mapDat = unzipEntry(records.get(0), "map.dat");
            addLengthPrefixedMbacTables(models, "Ridge", "map.dat", mapDat);
        }
        if (records.size() > 1) {
            byte[] fmapDat = unzipEntry(records.get(1), "fmap.dat");
            byte[] mapObjDat = unzipEntry(records.get(1), "mapobj.dat");
            addLengthPrefixedMbacTables(models, "Ridge", "fmap.dat", fmapDat);
            addLengthPrefixedMbacTables(models, "Ridge", "mapobj.dat", mapObjDat);
        }
        return models;
    }

    private static List<BinaryModel> loadChokkanModels() throws IOException, NoSuchAlgorithmException {
        byte[] scratchpad = readPackedScratchpad(CHOKKAN_SP, CHOKKAN_SP_SIZES, 4);
        List<BinaryModel> models = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        collectChokkanMbacEntries(models, seen, scratchpad, "sp4", 0);
        return models;
    }

    private static void addLengthPrefixedMbacTables(List<BinaryModel> models, String title, String source, byte[] data) throws IOException {
        if (data == null) {
            return;
        }
        int position = 0;
        int ordinal = 0;
        while (position + 4 <= data.length) {
            int length = readInt(data, position);
            position += 4;
            if (length <= 0 || position + length > data.length) {
                break;
            }
            byte[] entry = Arrays.copyOfRange(data, position, position + length);
            if (startsWithMbac(entry)) {
                models.add(new BinaryModel(title, source + "#" + ordinal, MascotLoader.loadFigure(entry)));
            }
            position += length;
            ordinal++;
        }
    }

    private static void collectChokkanMbacEntries(List<BinaryModel> models, Set<String> seen, byte[] data,
                                                  String source, int depth) throws IOException, NoSuchAlgorithmException {
        if (depth > 4) {
            return;
        }
        if (startsWithMbac(data)) {
            String hash = sha256(data);
            if (seen.add(hash)) {
                models.add(new BinaryModel("Chokkan", source, MascotLoader.loadFigure(data)));
            }
        }
        for (int offset = 0; offset + 4 <= data.length; offset++) {
            if (data[offset] != 'P' || data[offset + 1] != 'K' || data[offset + 2] != 3 || data[offset + 3] != 4) {
                continue;
            }
            try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data, offset, data.length - offset))) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    byte[] entryBytes = readAll(zip);
                    String nestedSource = source + "@" + offset + "/" + entry.getName();
                    if (entry.getName().endsWith("mbac.bin")) {
                        addChokkanMbacTable(models, seen, entryBytes, nestedSource);
                    } else if (startsWithMbac(entryBytes)) {
                        String hash = sha256(entryBytes);
                        if (seen.add(hash)) {
                            models.add(new BinaryModel("Chokkan", nestedSource, MascotLoader.loadFigure(entryBytes)));
                        }
                    }
                    if (containsZipHeader(entryBytes)) {
                        collectChokkanMbacEntries(models, seen, entryBytes, nestedSource, depth + 1);
                    }
                }
            } catch (IOException ignored) {
                // Scanning starts at every local ZIP header signature; false starts are ignored.
            }
        }
    }

    private static void addChokkanMbacTable(List<BinaryModel> models, Set<String> seen, byte[] data, String source)
            throws IOException, NoSuchAlgorithmException {
        if (data.length < 2) {
            return;
        }
        int groupCount = readUnsignedShort(data, 0);
        int position = 2;
        for (int group = 0; group < groupCount && position + 6 <= data.length; group++) {
            position += 2;
            int variant = readUnsignedShort(data, position);
            position += 2;
            int slotCount = readUnsignedShort(data, position);
            position += 2;
            for (int slot = 0; slot < slotCount && position + 2 <= data.length; slot++) {
                int length = readUnsignedShort(data, position);
                position += 2;
                if (length < 0 || position + length > data.length) {
                    return;
                }
                if (length > 0) {
                    byte[] entry = Arrays.copyOfRange(data, position, position + length);
                    if (startsWithMbac(entry)) {
                        String hash = sha256(entry);
                        if (seen.add(hash)) {
                            models.add(new BinaryModel("Chokkan", source + "#" + group + "." + variant + "." + slot,
                                    MascotLoader.loadFigure(entry)));
                        }
                    }
                }
                position += length;
            }
        }
    }

    private static String terrainStats(String title, List<BinaryModel> models) {
        int polygons = 0;
        int quads = 0;
        int texturedQuads = 0;
        for (BinaryModel model : models) {
            for (MbacModel.Polygon polygon : model.model().polygons()) {
                polygons++;
                if (polygon.indices().length == 4) {
                    quads++;
                    if (polygon.textureCoords() != null) {
                        texturedQuads++;
                    }
                }
            }
        }
        return title + " models=" + models.size() + " polygons=" + polygons + " quads=" + quads
                + " texturedQuads=" + texturedQuads;
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
            int length = readInt(scratchpad, position);
            if (length <= 2 || position + 4 + length - 2 > scratchpad.length) {
                break;
            }
            records.add(Arrays.copyOfRange(scratchpad, position + 4, position + 4 + length - 2));
            position += 4 + length;
        }
        return records;
    }

    private static byte[] unzipEntry(byte[] zipBytes, String name) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                byte[] data = readAll(zip);
                if (entry.getName().equals(name)) {
                    return data;
                }
            }
        }
        return null;
    }

    private static byte[] readAll(ZipInputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        input.transferTo(output);
        return output.toByteArray();
    }

    private static boolean startsWithMbac(byte[] data) {
        return data.length >= 4 && data[0] == 'M' && data[1] == 'B' && data[3] == 0;
    }

    private static boolean containsZipHeader(byte[] data) {
        for (int i = 0; i + 4 <= data.length; i++) {
            if (data[i] == 'P' && data[i + 1] == 'K' && data[i + 2] == 3 && data[i + 3] == 4) {
                return true;
            }
        }
        return false;
    }

    private static String sha256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(data));
    }

    private static int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24)
                | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8)
                | (data[offset + 3] & 0xFF);
    }

    private static int readUnsignedShort(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    }

    private static int countOpaquePixels(int[] pixels) {
        int count = 0;
        for (int pixel : pixels) {
            if ((pixel >>> 24) != 0) {
                count++;
            }
        }
        return count;
    }

    private static int countDifferentPixels(int[] a, int[] b) {
        int count = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                count++;
            }
        }
        return count;
    }

    private static SoftwareTexture makeTexture() {
        int[] palette = new int[256];
        byte[] indices = new byte[256 * 256];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = 0xFF000000 | (i << 16) | ((255 - i) << 8) | (i >>> 1);
        }
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                indices[y * 256 + x] = (byte) ((x + y) & 0xFF);
            }
        }
        return SoftwareTexture.fromIndexed(256, 256, palette, indices, true);
    }

    private static float[] identity() {
        return new float[]{
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
        };
    }

    private record BinaryModel(String title, String source, MbacModel model) {
    }

    private record Divergence(Candidate candidate, int differentPixels, int quadPixels, int trianglePixels) {
        private String describe() {
            return candidate.binaryModel().title() + " " + candidate.binaryModel().source()
                    + " polygon=" + candidate.polygonIndex()
                    + " axes=" + candidate.xAxis() + "," + candidate.yAxis() + "," + candidate.zAxis()
                    + " differentPixels=" + differentPixels
                    + " quadPixels=" + quadPixels
                    + " trianglePixels=" + trianglePixels
                    + " originalIndices=" + Arrays.toString(candidate.originalIndices())
                    + " transformedVertices=" + Arrays.toString(candidate.transformedVertices());
        }
    }

    private record Candidate(BinaryModel binaryModel, int polygonIndex, int xAxis, int yAxis, int zAxis,
                             int[] originalIndices, int[] transformedVertices, MbacModel quadModel,
                             MbacModel triangleModel) {
        private static Candidate from(BinaryModel binaryModel, int polygonIndex, int xAxis, int yAxis, int zAxis) {
            MbacModel model = binaryModel.model();
            MbacModel.Polygon polygon = model.polygons()[polygonIndex];
            int[] originalVertices = model.rawVertices();
            int[] originalIndices = polygon.indices();
            float minX = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            float minZ = Float.POSITIVE_INFINITY;
            float maxZ = Float.NEGATIVE_INFINITY;
            for (int index : originalIndices) {
                int offset = index * 3;
                float x = originalVertices[offset + xAxis];
                float y = originalVertices[offset + yAxis];
                float z = originalVertices[offset + zAxis];
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
                minZ = Math.min(minZ, z);
                maxZ = Math.max(maxZ, z);
            }
            float extentX = maxX - minX;
            float extentY = maxY - minY;
            float extentZ = maxZ - minZ;
            if (extentX < 1f || extentY < 1f || extentZ < 1f) {
                return null;
            }
            float scaleX = 80f / extentX;
            float scaleY = 64f / extentY;
            float scaleZ = 80f / extentZ;
            float centerX = (minX + maxX) * 0.5f;
            float centerY = (minY + maxY) * 0.5f;
            float centerZ = (minZ + maxZ) * 0.5f;
            int[] transformed = new int[12];
            boolean hasNearInside = false;
            boolean hasNearOutside = false;
            for (int i = 0; i < originalIndices.length; i++) {
                int source = originalIndices[i] * 3;
                int destination = i * 3;
                transformed[destination] = Math.round((originalVertices[source + xAxis] - centerX) * scaleX);
                transformed[destination + 1] = Math.round((originalVertices[source + yAxis] - centerY) * scaleY);
                transformed[destination + 2] = Math.round(NEAR + (originalVertices[source + zAxis] - centerZ) * scaleZ);
                hasNearInside |= transformed[destination + 2] >= NEAR;
                hasNearOutside |= transformed[destination + 2] < NEAR;
            }
            if (!hasNearInside || !hasNearOutside) {
                return null;
            }
            MbacModel quad = makeQuadModel(transformed, polygon);
            MbacModel triangles = makeTriangleModel(transformed, polygon);
            return new Candidate(binaryModel, polygonIndex, xAxis, yAxis, zAxis, originalIndices.clone(),
                    transformed, quad, triangles);
        }

        private static MbacModel makeQuadModel(int[] vertices, MbacModel.Polygon source) {
            MbacModel.Polygon polygon = new MbacModel.Polygon(
                    new int[]{0, 1, 2, 3},
                    source.textureCoords() == null ? null : source.textureCoords().clone(),
                    source.textureCoords() == null ? 0xFFFFFFFF : source.color(),
                    source.textureIndex(),
                    0,
                    source.blendMode(),
                    true,
                    source.transparent()
            );
            return new MbacModel(vertices, vertices.clone(), new MbacModel.Polygon[]{polygon}, 1, new MbacModel.Bone[0]);
        }

        private static MbacModel makeTriangleModel(int[] vertices, MbacModel.Polygon source) {
            float[] uv = source.textureCoords();
            MbacModel.Polygon first = new MbacModel.Polygon(
                    new int[]{0, 1, 2},
                    uv == null ? null : new float[]{uv[0], uv[1], uv[2], uv[3], uv[4], uv[5]},
                    uv == null ? 0xFFFFFFFF : source.color(),
                    source.textureIndex(),
                    0,
                    source.blendMode(),
                    true,
                    source.transparent()
            );
            MbacModel.Polygon second = new MbacModel.Polygon(
                    new int[]{2, 1, 3},
                    uv == null ? null : new float[]{uv[4], uv[5], uv[2], uv[3], uv[6], uv[7]},
                    uv == null ? 0xFFFFFFFF : source.color(),
                    source.textureIndex(),
                    0,
                    source.blendMode(),
                    true,
                    source.transparent()
            );
            return new MbacModel(vertices, vertices.clone(), new MbacModel.Polygon[]{first, second}, 1, new MbacModel.Bone[0]);
        }
    }
}
