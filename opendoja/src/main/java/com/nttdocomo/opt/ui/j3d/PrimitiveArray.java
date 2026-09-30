package com.nttdocomo.opt.ui.j3d;

/**
 * Defines a primitive-array class that stores vertex information and other
 * primitive data used for primitive rendering.
 * A primitive array can store vertices, normals, colors, texture coordinates,
 * and point-sprite information as arrays.
 *
 * <p>All primitive arrays always contain vertex information. The other kinds of
 * primitive information can be specified independently. Introduced in
 * DoJa-3.0.</p>
 */
public class PrimitiveArray {
    private final int type;
    private final int param;
    private final int size;
    private final int[] vertexArray;
    private final int[] normalArray;
    private final int[] colorArray;
    private final int[] textureCoordArray;
    private final int[] pointSpriteArray;

    /**
     * Creates a primitive-array object.
     *
     * @param type the primitive type to store
     * @param param the kinds of primitive information to store
     * @param size the number of primitives
     * @throws IllegalArgumentException if {@code type} is invalid, if
     *         {@code param} contains an invalid combination for {@code type}, or
     *         if {@code size} is less than or equal to {@code 0} or greater
     *         than or equal to {@code 256}
     */
    public PrimitiveArray(int type, int param, int size) {
        if (size <= 0 || size >= 256) {
            throw new IllegalArgumentException("size");
        }
        this.type = type;
        this.param = param;
        this.size = size;

        int verticesPerPrimitive = switch (type) {
            case Graphics3D.PRIMITIVE_POINTS, Graphics3D.PRIMITIVE_POINT_SPRITES -> 1;
            case Graphics3D.PRIMITIVE_LINES -> 2;
            case Graphics3D.PRIMITIVE_TRIANGLES -> 3;
            case Graphics3D.PRIMITIVE_QUADS -> 4;
            default -> throw new IllegalArgumentException("type");
        };
        int vertexCount = size * verticesPerPrimitive;
        this.vertexArray = new int[vertexCount * 3];

        int normalMode = param & 0x0300;
        int colorMode = param & 0x0C00;
        int textureOrPointMode = param & 0x3000;

        if (type == Graphics3D.PRIMITIVE_POINTS || type == Graphics3D.PRIMITIVE_LINES) {
            if (normalMode != Graphics3D.NORMAL_NONE
                    || textureOrPointMode != 0
                    || (param & 0xC000) != 0) {
                throw new IllegalArgumentException("param");
            }
            normalArray = null;
            pointSpriteArray = null;
            textureCoordArray = null;
            colorArray = switch (colorMode) {
                case Graphics3D.COLOR_NONE -> null;
                case Graphics3D.COLOR_PER_COMMAND -> new int[1];
                case Graphics3D.COLOR_PER_FACE -> new int[size];
                default -> throw new IllegalArgumentException("param");
            };
            return;
        }

        if (type == Graphics3D.PRIMITIVE_POINT_SPRITES) {
            if (normalMode != Graphics3D.NORMAL_NONE
                    || colorMode != Graphics3D.COLOR_NONE) {
                throw new IllegalArgumentException("param");
            }
            normalArray = null;
            colorArray = null;
            textureCoordArray = null;
            pointSpriteArray = switch (textureOrPointMode) {
                case Graphics3D.POINT_SPRITE_PER_COMMAND -> new int[8];
                case Graphics3D.POINT_SPRITE_PER_VERTEX -> new int[size * 8];
                default -> throw new IllegalArgumentException("param");
            };
            return;
        }

        if (colorMode != Graphics3D.COLOR_NONE
                && textureOrPointMode == Graphics3D.TEXTURE_COORD_PER_VERTEX) {
            throw new IllegalArgumentException("param");
        }
        normalArray = switch (normalMode) {
            case Graphics3D.NORMAL_NONE -> null;
            case Graphics3D.NORMAL_PER_FACE -> new int[size * 3];
            case Graphics3D.NORMAL_PER_VERTEX -> new int[vertexCount * 3];
            default -> throw new IllegalArgumentException("param");
        };
        colorArray = switch (colorMode) {
            case Graphics3D.COLOR_NONE -> null;
            case Graphics3D.COLOR_PER_COMMAND -> new int[1];
            case Graphics3D.COLOR_PER_FACE -> new int[size];
            default -> throw new IllegalArgumentException("param");
        };
        textureCoordArray = switch (textureOrPointMode) {
            case Graphics3D.TEXTURE_COORD_NONE -> null;
            case Graphics3D.TEXTURE_COORD_PER_VERTEX -> new int[vertexCount * 2];
            default -> throw new IllegalArgumentException("param");
        };
        pointSpriteArray = null;
    }

    /**
     * Gets the primitive type stored in this array.
     *
     * @return the primitive type
     */
    public int getType() {
        return type;
    }

    /**
     * Gets the kinds of primitive information stored in this array.
     *
     * @return the kinds of primitive information
     */
    public int getParam() {
        return param;
    }

    /**
     * Gets the number of primitives stored in this array.
     *
     * @return the number of primitives
     */
    public int size() {
        return size;
    }

    /**
     * Gets the stored vertex-information array.
     * Vertex coordinates are stored in the order
     * {@code {x0, y0, z0, x1, y1, z1, ...}}.
     *
     * @return the vertex-information array
     */
    public int[] getVertexArray() {
        return vertexArray;
    }

    /**
     * Gets the stored normal-information array.
     *
     * @return the normal-information array, or {@code null} if this primitive
     *         array does not have normal information
     */
    public int[] getNormalArray() {
        return normalArray;
    }

    /**
     * Gets the stored color-information array.
     *
     * @return the color-information array, or {@code null} if this primitive
     *         array does not have color information
     */
    public int[] getColorArray() {
        return colorArray;
    }

    /**
     * Gets the stored texture-coordinate-information array.
     *
     * @return the texture-coordinate-information array, or {@code null} if this
     *         primitive array does not have texture-coordinate information
     */
    public int[] getTextureCoordArray() {
        return textureCoordArray;
    }

    /**
     * Gets the stored point-sprite-information array.
     *
     * @return the point-sprite-information array, or {@code null} if this
     *         primitive array does not have point-sprite information
     */
    public int[] getPointSpriteArray() {
        return pointSpriteArray;
    }
}
