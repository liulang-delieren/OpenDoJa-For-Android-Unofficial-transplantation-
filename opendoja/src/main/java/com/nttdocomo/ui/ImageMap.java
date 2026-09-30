package com.nttdocomo.ui;

/**
 * Defines an image map that treats images arranged vertically and horizontally
 * as one large image.
 * The unit to which images are assigned is called a cell.
 * The cell size is fixed and is specified in the constructor.
 * By arranging cells vertically and horizontally and assigning an image to
 * each one, a large image suitable for things such as a game background can
 * be created.
 *
 * <p>Which image is assigned to which cell is specified by map data.
 * The map data is a one-dimensional array, and the relationship between the
 * array index and the cell coordinate is {@code index = mapWidth * y + x}.</p>
 *
 * <p>When the map data is set, the image array is specified together with the
 * map data.
 * If {@code concat} is {@code false}, each element of the image array is a
 * single image assigned to a cell.
 * If {@code concat} is {@code true}, each element of the image array is an
 * image that concatenates multiple cell images.
 * In either case, the cell selection is recalculated every time
 * {@link Graphics#drawImageMap(ImageMap, int, int)} is called.</p>
 *
 * <p>If the map width or height is {@code 0}, drawing the image map draws
 * nothing.
 * A cell is not drawn if the corresponding map-data value is negative, if it
 * is outside the image-array range, or if the corresponding image is
 * {@code null}.
 * If the corresponding image has already been disposed, the drawing method
 * throws an exception.</p>
 *
 * <p>Introduced in DoJa-3.5 (900i).</p>
 *
 * @see Graphics#drawImageMap(ImageMap, int, int)
 */
public class ImageMap {
    private int windowX;
    private int windowY;
    private int windowWidth;
    private int windowHeight;
    private int cellWidth = 16;
    private int cellHeight = 16;
    private int mapWidth;
    private int mapHeight;
    private int[] mapData = new int[0];
    private Image[] images = new Image[0];
    private boolean concatenated;

    /**
     * Applications cannot call this constructor directly to create an object.
     */
    protected ImageMap() {
    }

    /**
     * Creates an image map with the specified cell width and height.
     *
     * @param cellWidth the cell width in dots
     * @param cellHeight the cell height in dots
     * @throws IllegalArgumentException if either or both of {@code cellWidth}
     *         and {@code cellHeight} are less than or equal to {@code 0}
     */
    public ImageMap(int cellWidth, int cellHeight) {
        validateCellSize(cellWidth, cellHeight);
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }

    /**
     * Creates an image map by specifying the cell width and height and the map
     * data.
     * This constructor is deprecated because it can use only up to 128 images;
     * use {@link #ImageMap(int, int, int, int, int[], Image[])} instead.
     * This constructor behaves the same as converting {@code data} to an
     * {@code int[]} and calling
     * {@link #ImageMap(int, int, int, int, int[], Image[], boolean)} with
     * {@code concat == false}.
     *
     * @param cellWidth the cell width in dots
     * @param cellHeight the cell height in dots
     * @param mapWidth the map width
     * @param mapHeight the map height
     * @param data the map-data array
     * @param images the image array
     * @throws NullPointerException if {@code data} or {@code images} is
     *         {@code null}
     * @throws IllegalArgumentException if either or both of
     *         {@code cellWidth} and {@code cellHeight} are less than or equal
     *         to {@code 0}, if either or both of {@code mapWidth} and
     *         {@code mapHeight} are negative, or if {@code data.length} is
     *         shorter than {@code mapWidth * mapHeight}
     */
    public ImageMap(int cellWidth, int cellHeight, int mapWidth, int mapHeight, byte[] data, Image[] images) {
        this(cellWidth, cellHeight, mapWidth, mapHeight, toIntArray(data), images, false);
    }

    /**
     * Creates an image map by specifying the cell width and height and the map
     * data.
     * This is the same as calling
     * {@link #ImageMap(int, int, int, int, int[], Image[], boolean)} with
     * {@code concat == false}.
     *
     * @param cellWidth the cell width in dots
     * @param cellHeight the cell height in dots
     * @param mapWidth the map width
     * @param mapHeight the map height
     * @param data the map-data array
     * @param images the image array referred to by the map data
     * @throws NullPointerException if {@code data} or {@code images} is
     *         {@code null}
     * @throws IllegalArgumentException if either or both of
     *         {@code cellWidth} and {@code cellHeight} are less than or equal
     *         to {@code 0}, if either or both of {@code mapWidth} and
     *         {@code mapHeight} are negative, or if {@code data.length} is
     *         shorter than {@code mapWidth * mapHeight}
     */
    public ImageMap(int cellWidth, int cellHeight, int mapWidth, int mapHeight, int[] data, Image[] images) {
        this(cellWidth, cellHeight, mapWidth, mapHeight, data, images, false);
    }

    /**
     * Creates an image map by specifying the cell width and height, the map
     * data, and whether the image array contains concatenated images.
     * The image-map object keeps the references to {@code data} and
     * {@code images}.
     * The window is initialized to the same size as the map.
     *
     * @param cellWidth the cell width in dots
     * @param cellHeight the cell height in dots
     * @param mapWidth the map width
     * @param mapHeight the map height
     * @param data the map-data array
     * @param images the image array referred to by the map data, or the
     *               concatenated-image array
     * @param concat {@code true} if each image in {@code images} is a
     *               concatenation of images referred to from the map data;
     *               {@code false} if each image is a single image
     * @throws NullPointerException if {@code data} or {@code images} is
     *         {@code null}
     * @throws IllegalArgumentException if either or both of
     *         {@code cellWidth} and {@code cellHeight} are less than or equal
     *         to {@code 0}, if either or both of {@code mapWidth} and
     *         {@code mapHeight} are negative, or if {@code data.length} is
     *         shorter than {@code mapWidth * mapHeight}
     */
    public ImageMap(int cellWidth, int cellHeight, int mapWidth, int mapHeight, int[] data, Image[] images, boolean concat) {
        validateMapConfiguration(cellWidth, cellHeight, mapWidth, mapHeight, data, images);
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.mapData = data;
        this.images = images;
        this.concatenated = concat;
        this.windowX = 0;
        this.windowY = 0;
        this.windowWidth = mapWidth;
        this.windowHeight = mapHeight;
    }

    /**
     * Sets the map data.
     * This method is deprecated because it can use only up to 128 images; use
     * {@link #setImageMap(int, int, int[], Image[])} instead.
     * This method behaves the same as converting {@code data} to an
     * {@code int[]} and calling
     * {@link #setImageMap(int, int, int[], Image[], boolean)} with
     * {@code concat == false}.
     *
     * @param mapWidth the map width
     * @param mapHeight the map height
     * @param data the map-data array
     * @param images the image array
     * @throws NullPointerException if {@code data} or {@code images} is
     *         {@code null}
     * @throws IllegalArgumentException if either or both of
     *         {@code mapWidth} and {@code mapHeight} are negative, or if
     *         {@code data.length} is shorter than {@code mapWidth * mapHeight}
     */
    public void setImageMap(int mapWidth, int mapHeight, byte[] data, Image[] images) {
        setImageMap(mapWidth, mapHeight, toIntArray(data), images, false);
    }

    /**
     * Sets the map data.
     * This is the same as calling
     * {@link #setImageMap(int, int, int[], Image[], boolean)} with
     * {@code concat == false}.
     *
     * @param mapWidth the map width
     * @param mapHeight the map height
     * @param data the map-data array
     * @param images the image array referred to by the map data
     * @throws NullPointerException if {@code data} or {@code images} is
     *         {@code null}
     * @throws IllegalArgumentException if either or both of
     *         {@code mapWidth} and {@code mapHeight} are negative, or if
     *         {@code data.length} is shorter than {@code mapWidth * mapHeight}
     */
    public void setImageMap(int mapWidth, int mapHeight, int[] data, Image[] images) {
        setImageMap(mapWidth, mapHeight, data, images, false);
    }

    /**
     * Sets the map data, specifying whether the image array contains
     * concatenated images.
     * The image-map object keeps the references to {@code data} and
     * {@code images}.
     * The window is initialized to the same size as the map.
     *
     * @param mapWidth the map width
     * @param mapHeight the map height
     * @param data the map-data array
     * @param images the image array referred to by the map data, or the
     *               concatenated-image array
     * @param concat {@code true} if each image in {@code images} is a
     *               concatenation of images referred to from the map data;
     *               {@code false} if each image is a single image
     * @throws NullPointerException if {@code data} or {@code images} is
     *         {@code null}
     * @throws IllegalArgumentException if either or both of
     *         {@code mapWidth} and {@code mapHeight} are negative, or if
     *         {@code data.length} is shorter than {@code mapWidth * mapHeight}
     */
    public void setImageMap(int mapWidth, int mapHeight, int[] data, Image[] images, boolean concat) {
        validateMapData(mapWidth, mapHeight, data, images);
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.mapData = data;
        this.images = images;
        this.concatenated = concat;
        this.windowX = 0;
        this.windowY = 0;
        this.windowWidth = mapWidth;
        this.windowHeight = mapHeight;
    }

    /**
     * Sets the region displayed from the image map.
     * The region is specified in map cells.
     * When the image map is drawn by
     * {@link Graphics#drawImageMap(ImageMap, int, int)}, only the cells in the
     * specified window region are drawn.
     *
     * @param x the X coordinate of the upper-left corner of the displayed
     *          region
     * @param y the Y coordinate of the upper-left corner of the displayed
     *          region
     * @param width the width of the displayed region
     * @param height the height of the displayed region
     * @throws IllegalArgumentException if {@code x} or {@code y} is negative,
     *         if {@code width} or {@code height} is negative, or if the window
     *         extends outside the map
     */
    public void setWindow(int x, int y, int width, int height) {
        validateWindow(x, y, width, height, mapWidth, mapHeight);
        this.windowX = x;
        this.windowY = y;
        this.windowWidth = width;
        this.windowHeight = height;
    }

    /**
     * Sets only the coordinates of the region displayed from the image map,
     * without changing the width and height of that region.
     * The coordinates are specified in map cells.
     *
     * @param x the X coordinate of the upper-left corner of the displayed
     *          region
     * @param y the Y coordinate of the upper-left corner of the displayed
     *          region
     * @throws IllegalArgumentException if {@code x} or {@code y} is negative,
     *         or if the window extends outside the map
     */
    public void setWindowLocation(int x, int y) {
        setWindow(x, y, windowWidth, windowHeight);
    }

    /**
     * Sets only the coordinates of the region displayed from the image map by
     * specifying the relative position.
     * The coordinates are specified in map cells.
     *
     * @param dx the increment of the X coordinate of the upper-left corner of
     *           the displayed region; a positive value moves it to the right,
     *           and a negative value moves it to the left
     * @param dy the increment of the Y coordinate of the upper-left corner of
     *           the displayed region; a positive value moves it downward, and a
     *           negative value moves it upward
     * @throws IllegalArgumentException if the moved window extends outside the
     *         map
     */
    public void moveWindowLocation(int dx, int dy) {
        setWindowLocation(windowX + dx, windowY + dy);
    }

    void draw(Graphics graphics, int drawX, int drawY) {
        if (mapWidth == 0 || mapHeight == 0 || windowWidth == 0 || windowHeight == 0) {
            return;
        }
        for (int row = 0; row < windowHeight; row++) {
            for (int column = 0; column < windowWidth; column++) {
                int imageIndex = mapData[(mapWidth * (windowY + row)) + windowX + column];
                if (imageIndex < 0) {
                    continue;
                }
                if (concatenated) {
                    drawConcatenatedCell(graphics, drawX, drawY, column, row, imageIndex);
                } else {
                    drawSingleCell(graphics, drawX, drawY, column, row, imageIndex);
                }
            }
        }
    }

    private static int[] toIntArray(byte[] data) {
        if (data == null) {
            throw new NullPointerException("data");
        }
        int[] ints = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            ints[i] = data[i] & 0xFF;
        }
        return ints;
    }

    private void drawSingleCell(Graphics graphics, int drawX, int drawY, int column, int row, int imageIndex) {
        if (imageIndex >= images.length) {
            return;
        }
        Image image = images[imageIndex];
        if (image == null) {
            return;
        }
        int width = java.lang.Math.min(cellWidth, image.getWidth());
        int height = java.lang.Math.min(cellHeight, image.getHeight());
        if (width <= 0 || height <= 0) {
            return;
        }
        graphics.drawImage(image, drawX + (column * cellWidth), drawY + (row * cellHeight), 0, 0, width, height);
    }

    private void drawConcatenatedCell(Graphics graphics, int drawX, int drawY, int column, int row, int imageIndex) {
        AtlasCell atlasCell = locateAtlasCell(imageIndex);
        if (atlasCell == null || atlasCell.image == null) {
            return;
        }
        graphics.drawImage(
                atlasCell.image,
                drawX + (column * cellWidth),
                drawY + (row * cellHeight),
                atlasCell.sourceX,
                atlasCell.sourceY,
                cellWidth,
                cellHeight
        );
    }

    private AtlasCell locateAtlasCell(int imageIndex) {
        int remaining = imageIndex;
        for (Image image : images) {
            if (image == null) {
                continue;
            }
            int tilesAcross = image.getWidth() / cellWidth;
            int tilesDown = image.getHeight() / cellHeight;
            int tileCount = tilesAcross * tilesDown;
            if (tileCount <= 0) {
                continue;
            }
            if (remaining < tileCount) {
                return new AtlasCell(
                        image,
                        (remaining % tilesAcross) * cellWidth,
                        (remaining / tilesAcross) * cellHeight
                );
            }
            remaining -= tileCount;
        }
        return null;
    }

    private static void validateCellSize(int cellWidth, int cellHeight) {
        if (cellWidth <= 0 || cellHeight <= 0) {
            throw new IllegalArgumentException("cellWidth/cellHeight");
        }
    }

    private static void validateMapConfiguration(int cellWidth, int cellHeight, int mapWidth, int mapHeight, int[] data, Image[] images) {
        validateCellSize(cellWidth, cellHeight);
        validateMapData(mapWidth, mapHeight, data, images);
    }

    private static void validateMapData(int mapWidth, int mapHeight, int[] data, Image[] images) {
        if (data == null) {
            throw new NullPointerException("data");
        }
        if (images == null) {
            throw new NullPointerException("images");
        }
        if (mapWidth < 0 || mapHeight < 0) {
            throw new IllegalArgumentException("mapWidth/mapHeight");
        }
        if (data.length < mapWidth * mapHeight) {
            throw new IllegalArgumentException("data.length");
        }
        for (Image image : images) {
            if (image != null) {
                _ImageInternalAccess.requireUsable(image);
            }
        }
    }

    private static void validateWindow(int x, int y, int width, int height, int mapWidth, int mapHeight) {
        if (x < 0 || y < 0 || width < 0 || height < 0) {
            throw new IllegalArgumentException("window");
        }
        if (x + width > mapWidth || y + height > mapHeight) {
            throw new IllegalArgumentException("window outside map");
        }
    }

    private record AtlasCell(Image image, int sourceX, int sourceY) {
    }
}
