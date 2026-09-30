package com.nttdocomo.opt.ui.j3d2;

import com.nttdocomo.ui.UIException;
import opendoja.host.OpenDoJaLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

final class C3DData {
    private static final boolean DEBUG_3D = opendoja.host.OpenDoJaLaunchArgs.getBoolean(opendoja.host.OpenDoJaLaunchArgs.DEBUG3D);
    private static final int FIXED_ONE = 65536;
    private static final double PI = java.lang.Math.PI;

    private C3DData() {
    }

    static FigureData parseFigure(byte[] source) {
        BinaryData data = inflate(source);
        int length = bigInt(data.words()[3]) + 5;
        int off = 5;
        ArrayList<FigureNode> nodes = new ArrayList<>();
        LinkedHashMap<Integer, CoordSet> coordSets = new LinkedHashMap<>();
        int boundTextureSet = -1;
        while (off < length) {
            int word = bigInt(data.words()[off]);
            int opcode = word & 0xFF;
            switch (opcode) {
                case 0x29 -> {
                    off++;
                    float[] values = new float[12];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = Float.intBitsToFloat(bigInt(data.words()[off++]));
                    }
                    MaterialNode node = new MaterialNode(values);
                    if (boundTextureSet >= 0) {
                        coordSet(coordSets, boundTextureSet).material = node;
                    } else {
                        nodes.add(node);
                    }
                }
                case 0x44 -> {
                    int bindingWord = bigInt(data.words()[off + 1]);
                    TextureBindingNode node = new TextureBindingNode(bindingWord & 0xFF,
                            ((bindingWord >> 16) & 0xFF) != 0,
                            (bindingWord >>> 24) != 0);
                    if (boundTextureSet >= 0) {
                        coordSet(coordSets, boundTextureSet).texture = node;
                    } else {
                        nodes.add(node);
                    }
                    off += 2;
                }
                case 0x10, 0x11 -> {
                    int flags = word >>> 8;
                    off++;
                    int start = off;
                    while (bigInt(data.words()[off]) > 0) {
                        off += bigInt(data.words()[off]) + 1;
                    }
                    off++;
                    int[] payload = new int[off - start];
                    for (int i = 0; i < payload.length; i++) {
                        payload[i] = bigInt(data.words()[start + i]);
                    }
                    nodes.add(new ShapeNode(opcode, flags, payload));
                }
                case 0x36, 0x38 -> {
                    boolean objectSpace = (word & 0x8000_0000) != 0;
                    off++;
                    nodes.add(new Float3Node(objectSpace ? (opcode | 0x100) : opcode,
                            Float.intBitsToFloat(bigInt(data.words()[off++])),
                            Float.intBitsToFloat(bigInt(data.words()[off++])),
                            Float.intBitsToFloat(bigInt(data.words()[off++]))));
                }
                case 0x18, 0x37 -> {
                    off++;
                    nodes.add(new Float4Node(opcode,
                            Float.intBitsToFloat(bigInt(data.words()[off++])),
                            Float.intBitsToFloat(bigInt(data.words()[off++])),
                            Float.intBitsToFloat(bigInt(data.words()[off++])),
                            Float.intBitsToFloat(bigInt(data.words()[off++]))));
                }
                case 0x56 -> {
                    int coordSetId = word >>> 24;
                    int count = bigInt(data.words()[off + 1]);
                    off += 2;
                    float[] values = new float[count];
                    for (int i = 0; i < count; i++) {
                        values[i] = Float.intBitsToFloat(bigInt(data.words()[off++]));
                    }
                    coordSet(coordSets, coordSetId).data = values;
                }
                case 0x51, 0x53 -> {
                    nodes.add(new CoordRefNode(opcode, word >>> 24));
                    off++;
                }
                case 0x54, 0x47, 0x42, 0x43 -> {
                    nodes.add(new CoordRefNode(opcode, word >>> 16));
                    off++;
                }
                case 0x34, 0x35, 0x39 -> {
                    nodes.add(new CoordRefNode(opcode, word >>> 8));
                    off++;
                }
                case 0x50 -> {
                    boundTextureSet = word >>> 24;
                    off++;
                }
                case 0x52 -> {
                    boundTextureSet = -1;
                    off++;
                }
                case 0x00, 0x3A, 0x40, 0x41, 0x57 -> off++;
                default -> throw new UIException(UIException.UNSUPPORTED_FORMAT,
                        "Unsupported j3d2 figure opcode 0x" + Integer.toHexString(opcode));
            }
        }
        FigureData parsed = new FigureData(List.copyOf(nodes), Map.copyOf(coordSets));
        if (DEBUG_3D) {
            OpenDoJaLog.debug(C3DData.class, () -> {
                StringBuilder summary = new StringBuilder("C3D parseFigure bytes=")
                        .append(source.length)
                        .append(" nodes=")
                        .append(parsed.nodes().size())
                        .append(" coordSets=")
                        .append(parsed.coordSets().size());
                int materialIndex = 0;
                for (FigureNode node : parsed.nodes()) {
                    if (!(node instanceof MaterialNode material)) {
                        continue;
                    }
                    float[] values = material.data();
                    summary.append(" [m")
                            .append(materialIndex++)
                            .append(" a=")
                            .append(trim3(values[0]))
                            .append(" alpha=")
                            .append(trim3(values[1]))
                            .append(" rgb=")
                            .append(trim3(values[2])).append(',')
                            .append(trim3(values[3])).append(',')
                            .append(trim3(values[4]))
                            .append(']');
                }
                return summary.toString();
            });
        }
        return parsed;
    }

    static ActionTableData parseActionTable(byte[] source) {
        BinaryData data = inflate(source);
        int actionCount = bigInt(data.words()[5]);
        if (actionCount <= 0) {
            throw new UIException(UIException.UNSUPPORTED_FORMAT, "Empty j3d2 action table");
        }
        ActionInfo[] actions = new ActionInfo[actionCount];
        int off = 6 + (actionCount << 1);
        int actionOffsetsBase = off;
        for (int i = 0; i < actionCount; i++) {
            int limit = actionOffsetsBase + bigInt(data.words()[6 + (i << 1)]);
            float duration = Float.intBitsToFloat(bigInt(data.words()[7 + (i << 1)]));
            ArrayList<ActionPart> parts = new ArrayList<>();
            int currentPartId = 0;
            while (off < limit) {
                int word = bigInt(data.words()[off]);
                int opcode = word & 0xFF;
                switch (opcode) {
                    case 0x32, 0x61, 0x62 -> {
                        int type = opcode;
                        off++;
                        float partDuration;
                        int keyCount;
                        int valuesPerKey;
                        if (opcode == 0x32) {
                            partDuration = 0f;
                            keyCount = 0;
                            valuesPerKey = 16;
                        } else {
                            partDuration = Float.intBitsToFloat(bigInt(data.words()[off++]));
                            keyCount = bigInt(data.words()[off++]);
                            valuesPerKey = opcode == 0x61 ? 5 : 4;
                            if (opcode == 0x62 && ((word >> 16) & 0xFF) != 0x01) {
                                type |= 0x100;
                            }
                        }
                        float[] values = new float[valuesPerKey * java.lang.Math.max(1, keyCount == 0 ? 1 : keyCount)];
                        for (int v = 0; v < values.length; v++) {
                            values[v] = Float.intBitsToFloat(bigInt(data.words()[off++]));
                        }
                        parts.add(new ActionPart(currentPartId, type, partDuration, keyCount, values));
                    }
                    case 0x64 -> {
                        currentPartId = word >>> 16;
                        off++;
                    }
                    case 0x65 -> off++;
                    default -> throw new UIException(UIException.UNSUPPORTED_FORMAT,
                            "Unsupported j3d2 action opcode 0x" + Integer.toHexString(opcode));
                }
            }
            actions[i] = new ActionInfo(duration, List.copyOf(parts));
        }
        ActionTableData parsed = new ActionTableData(actions);
        if (DEBUG_3D) {
            OpenDoJaLog.debug(C3DData.class, () -> "C3D parseActionTable bytes=" + source.length
                    + " actions=" + parsed.actions().length);
        }
        return parsed;
    }

    static TextureData parseTexture(byte[] source) {
        BinaryData data = inflate(source);
        int textureCount = bigInt(data.words()[5]);
        if (textureCount < 0) {
            throw new UIException(UIException.UNSUPPORTED_FORMAT, "Invalid j3d2 texture count");
        }
        TextureImage[] images = new TextureImage[textureCount];
        for (int i = 0; i < textureCount; i++) {
            int off = bigInt(data.words()[6 + i]);
            int wh = data.words()[off + 1];
            int width = bigShort(wh & 0xFFFF);
            int height = bigShort((wh >>> 16) & 0xFFFF);
            if (width <= 0 || height <= 0) {
                throw new UIException(UIException.UNSUPPORTED_FORMAT, "Invalid j3d2 texture size");
            }
            int flags = data.words()[off + 2];
            int[] argb = new int[width * height];
            int pixelOffset = (off + 4) * Integer.BYTES;
            if ((flags & 0x2000) != 0) {
                boolean indexed8 = (flags & 0x1000) != 0;
                int paletteOffset = pixelOffset + (((width * height) >> (indexed8 ? 2 : 3)) * Integer.BYTES);
                if (indexed8) {
                    for (int p = 0; p < argb.length; p++) {
                        int index = data.bytes()[pixelOffset + p] & 0xFF;
                        argb[p] = color16To32(readBigEndianShort(data.bytes(), paletteOffset + index * 2));
                    }
                } else {
                    for (int p = 0; p < argb.length / 2; p++) {
                        int value = data.bytes()[pixelOffset + p] & 0xFF;
                        argb[p * 2] = color16To32(readBigEndianShort(data.bytes(), paletteOffset + ((value >>> 4) & 0x0F) * 2));
                        argb[p * 2 + 1] = color16To32(readBigEndianShort(data.bytes(), paletteOffset + (value & 0x0F) * 2));
                    }
                }
            } else {
                for (int p = 0; p < argb.length; p++) {
                    argb[p] = color16To32(readBigEndianShort(data.bytes(), pixelOffset + p * 2));
                }
            }
            images[i] = new TextureImage(width, height, argb);
        }
        TextureData parsed = new TextureData(images);
        if (DEBUG_3D) {
            OpenDoJaLog.debug(C3DData.class, () -> {
                StringBuilder summary = new StringBuilder("C3D parseTexture bytes=")
                        .append(source.length)
                        .append(" images=")
                        .append(parsed.images().length);
                for (int imageIndex = 0; imageIndex < parsed.images().length; imageIndex++) {
                    TextureImage image = parsed.images()[imageIndex];
                    int opaque = 0;
                    int transparent = 0;
                    int translucent = 0;
                    for (int argb : image.argb()) {
                        int alpha = (argb >>> 24) & 0xFF;
                        if (alpha == 0) {
                            transparent++;
                        } else if (alpha == 0xFF) {
                            opaque++;
                        } else {
                            translucent++;
                        }
                    }
                    summary.append(" [")
                            .append(imageIndex)
                            .append(' ')
                            .append(image.width())
                            .append('x')
                            .append(image.height())
                            .append(" o=")
                            .append(opaque)
                            .append(" t=")
                            .append(transparent)
                            .append(" x=")
                            .append(translucent)
                            .append(']');
                }
                return summary.toString();
            });
        }
        return parsed;
    }

    static BinaryData inflate(byte[] source) {
        if (source == null) {
            throw new NullPointerException("source");
        }
        byte[] bytes = source;
        if (source.length >= 2 && (source[0] & 0xFF) == 0x1F && (source[1] & 0xFF) == 0x8B) {
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(source));
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                gzip.transferTo(out);
                bytes = out.toByteArray();
            } catch (IOException e) {
                throw new UIException(UIException.UNSUPPORTED_FORMAT, "Invalid compressed j3d2 resource");
            }
        } else {
            bytes = source.clone();
        }
        if ((bytes.length & 3) != 0) {
            throw new UIException(UIException.UNSUPPORTED_FORMAT, "Invalid j3d2 resource alignment");
        }
        int[] words = new int[bytes.length / Integer.BYTES];
        for (int i = 0; i < words.length; i++) {
            int base = i * Integer.BYTES;
            words[i] = (bytes[base] & 0xFF)
                    | ((bytes[base + 1] & 0xFF) << 8)
                    | ((bytes[base + 2] & 0xFF) << 16)
                    | ((bytes[base + 3] & 0xFF) << 24);
        }
        return new BinaryData(bytes, words);
    }

    static CoordSet coordSet(Map<Integer, CoordSet> sets, int id) {
        CoordSet existing = sets.get(id);
        if (existing != null) {
            return existing;
        }
        CoordSet created = new CoordSet(id);
        sets.put(id, created);
        return created;
    }

    static int bigShort(int value) {
        return ((value << 8) & 0xFF00) | ((value >>> 8) & 0x00FF);
    }

    static int bigInt(int value) {
        return ((value << 24) & 0xFF00_0000)
                | ((value << 8) & 0x00FF_0000)
                | ((value >>> 8) & 0x0000_FF00)
                | ((value >>> 24) & 0x0000_00FF);
    }

    static int color16To32(int value) {
        int color = (value & 0x8000) != 0 ? 0xFF00_0000 : 0;
        color |= (value & 0x7C00) << 9;
        color |= (value & 0x03E0) << 6;
        color |= (value & 0x001F) << 3;
        return color;
    }

    static int readBigEndianShort(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF);
    }

    static float fixedToFloat(int value) {
        return value / (float) FIXED_ONE;
    }

    static double fixedToDouble(int value) {
        return value / (double) FIXED_ONE;
    }

    private static String trim3(float value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }

    static double fixedDegreesToRadians(int value) {
        return fixedToDouble(value) * PI / 180.0;
    }

    static int doubleToFixed(double value) {
        return (int) (value * FIXED_ONE);
    }

    static int radiansToFixedDegrees(double value) {
        return doubleToFixed(value / PI * 180.0);
    }

    static int floatSecondsToMillis(float seconds) {
        return (int) (seconds * 1000f);
    }

    record BinaryData(byte[] bytes, int[] words) {
    }

    record FigureData(List<FigureNode> nodes, Map<Integer, CoordSet> coordSets) {
    }

    record ActionTableData(ActionInfo[] actions) {
    }

    record TextureData(TextureImage[] images) {
    }

    record TextureImage(int width, int height, int[] argb) {
    }

    record ActionInfo(float duration, List<ActionPart> parts) {
    }

    record ActionPart(int id, int type, float duration, int keyCount, float[] data) {
    }

    sealed interface FigureNode permits MaterialNode, TextureBindingNode, ShapeNode, Float3Node, Float4Node, CoordRefNode {
    }

    record MaterialNode(float[] data) implements FigureNode {
    }

    record TextureBindingNode(int textureIndex, boolean repeatS, boolean repeatT) implements FigureNode {
    }

    record ShapeNode(int type, int flags, int[] data) implements FigureNode {
    }

    record Float3Node(int type, float x, float y, float z) implements FigureNode {
    }

    record Float4Node(int type, float x, float y, float z, float w) implements FigureNode {
    }

    record CoordRefNode(int type, int coordSet) implements FigureNode {
    }

    static final class CoordSet {
        final int id;
        float[] data;
        MaterialNode material;
        TextureBindingNode texture;

        CoordSet(int id) {
            this.id = id;
        }
    }
}
