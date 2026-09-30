package com.nttdocomo.ui;

/**
 * Defines a sprite.
 * A sprite is an object that has an image to display and a position at which
 * to display it.
 * A sprite can also be set visible or invisible.
 *
 * <p>The sprite object keeps the reference to the image object passed in a
 * constructor or method argument.
 * Therefore, if that image object is changed, sprite drawing is also
 * affected.
 * If that image object is disposed, drawing the sprite throws an exception.</p>
 *
 * <p>The settings held by a sprite object, such as the image, display
 * position, and visible state, are independent of one another.
 * For example, even if the image is reset with {@link #setImage(Image)}, other
 * settings such as those made by {@link #setLocation(int, int)} are not
 * affected.</p>
 *
 * <p>Introduced in DoJa-3.5 (900i).</p>
 *
 * @see SpriteSet
 */
public class Sprite {
    private Image image;
    private int sourceX;
    private int sourceY;
    private int width;
    private int height;
    private int x;
    private int y;
    private boolean visible = true;
    private int flipMode;
    private int[] rotation;

    /**
     * Applications cannot call this constructor directly to create an object.
     */
    protected Sprite() {
    }

    /**
     * Creates a sprite object with the specified image.
     * By default, it is initialized to the visible state at display position
     * {@code (0, 0)}.
     *
     * @param image the image
     * @throws NullPointerException if {@code image} is {@code null}
     * @throws UIException if {@code image} has already been disposed
     *         ({@link UIException#ILLEGAL_STATE})
     */
    public Sprite(Image image) {
        this(image, 0, 0, image == null ? 0 : image.getWidth(), image == null ? 0 : image.getHeight());
    }

    /**
     * Creates a sprite object by specifying part of an image.
     * By default, it is initialized to the visible state at display position
     * {@code (0, 0)}.
     * {@code width} and {@code height} may be {@code 0}; in that state,
     * drawing the sprite draws nothing.
     *
     * @param image the image
     * @param x the X coordinate of the upper-left corner of the image rectangle
     *          used as the sprite
     * @param y the Y coordinate of the upper-left corner of the image rectangle
     *          used as the sprite
     * @param width the width of the image rectangle used as the sprite
     * @param height the height of the image rectangle used as the sprite
     * @throws NullPointerException if {@code image} is {@code null}
     * @throws IllegalArgumentException if either or both of {@code width} and
     *         {@code height} are negative, or if the specified rectangle
     *         extends outside {@code image}
     * @throws UIException if {@code image} has already been disposed
     *         ({@link UIException#ILLEGAL_STATE})
     */
    public Sprite(Image image, int x, int y, int width, int height) {
        setImage(image, x, y, width, height);
    }

    /**
     * Sets the display position of the sprite.
     *
     * @param x the X coordinate of the display position
     * @param y the Y coordinate of the display position
     */
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets the image of the sprite.
     *
     * @param image the image
     * @throws NullPointerException if {@code image} is {@code null}
     * @throws UIException if {@code image} has already been disposed
     *         ({@link UIException#ILLEGAL_STATE})
     */
    public void setImage(Image image) {
        if (image == null) {
            throw new NullPointerException("image");
        }
        setImage(image, 0, 0, image.getWidth(), image.getHeight());
    }

    /**
     * Sets the image of the sprite.
     * {@code width} and {@code height} may be {@code 0}; in that state,
     * drawing the sprite draws nothing.
     *
     * @param image the image
     * @param x the X coordinate of the upper-left corner of the image rectangle
     *          used as the sprite
     * @param y the Y coordinate of the upper-left corner of the image rectangle
     *          used as the sprite
     * @param width the width of the image rectangle used as the sprite
     * @param height the height of the image rectangle used as the sprite
     * @throws NullPointerException if {@code image} is {@code null}
     * @throws IllegalArgumentException if either or both of {@code width} and
     *         {@code height} are negative, or if the specified rectangle
     *         extends outside {@code image}
     * @throws UIException if {@code image} has already been disposed
     *         ({@link UIException#ILLEGAL_STATE})
     */
    public void setImage(Image image, int x, int y, int width, int height) {
        validateImageRegion(image, x, y, width, height);
        this.image = image;
        this.sourceX = x;
        this.sourceY = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Sets whether the sprite is visible.
     * A sprite set invisible is not a target for drawing or collision
     * detection.
     *
     * @param visible {@code true} to make it visible, or {@code false} to
     *                make it invisible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Gets whether the sprite is visible.
     *
     * @return {@code true} if visible, or {@code false} if invisible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets whether the sprite is displayed flipped.
     * By default, {@link Graphics#FLIP_NONE} is set.
     * If a two-dimensional linear-transform matrix is set by
     * {@link #setRotation(int[])}, the setting made by this method is ignored.
     *
     * @param flipMode the flip mode; one of
     *                 {@link Graphics#FLIP_NONE},
     *                 {@link Graphics#FLIP_HORIZONTAL},
     *                 {@link Graphics#FLIP_VERTICAL},
     *                 {@link Graphics#FLIP_ROTATE},
     *                 {@link Graphics#FLIP_ROTATE_LEFT},
     *                 {@link Graphics#FLIP_ROTATE_RIGHT},
     *                 {@link Graphics#FLIP_ROTATE_RIGHT_HORIZONTAL}, or
     *                 {@link Graphics#FLIP_ROTATE_RIGHT_VERTICAL}
     * @throws IllegalArgumentException if {@code flipMode} is invalid
     */
    public void setFlipMode(int flipMode) {
        if (flipMode < Graphics.FLIP_NONE || flipMode > Graphics.FLIP_ROTATE_RIGHT_VERTICAL) {
            throw new IllegalArgumentException("flipMode");
        }
        this.flipMode = flipMode;
    }

    /**
     * Gets the X coordinate of the display position of the sprite.
     *
     * @return the X coordinate of the sprite
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the Y coordinate of the display position of the sprite.
     *
     * @return the Y coordinate of the sprite
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the width of the sprite.
     * This matches the width of the rectangle that is the target of collision
     * detection in {@code SpriteSet}.
     *
     * @return the sprite width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the sprite.
     * This matches the height of the rectangle that is the target of collision
     * detection in {@code SpriteSet}.
     *
     * @return the sprite height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the two-dimensional linear-transform matrix used when the sprite is
     * displayed.
     * When this matrix is set, the sprite is linearly transformed around the
     * center of the sprite rectangle and then displayed at the position set by
     * {@link #setLocation(int, int)}.
     * In that case, the flip-display setting of {@link #setFlipMode(int)} is
     * ignored.
     * To disable the setting, call this method with {@code null}.
     * The array contents are copied internally, so changing the array after the
     * call does not affect the sprite.
     *
     * @param rotation the matrix elements of the two-dimensional linear
     *                 transform; the first four elements specify
     *                 {@code m00}, {@code m01}, {@code m10}, and {@code m11}
     *                 in order. If the array length is {@code 5} or more, the
     *                 fifth and later elements are ignored.
     * @throws ArrayIndexOutOfBoundsException if {@code rotation.length < 4}
     */
    public void setRotation(int[] rotation) {
        if (rotation == null) {
            this.rotation = null;
            return;
        }
        if (rotation.length < 4) {
            throw new ArrayIndexOutOfBoundsException();
        }
        this.rotation = new int[] {rotation[0], rotation[1], rotation[2], rotation[3]};
    }

    Image image() {
        return image;
    }

    int sourceX() {
        return sourceX;
    }

    int sourceY() {
        return sourceY;
    }

    private static void validateImageRegion(Image image, int x, int y, int width, int height) {
        if (image == null) {
            throw new NullPointerException("image");
        }
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("width/height");
        }
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        if (x < 0 || y < 0 || x + width > imageWidth || y + height > imageHeight) {
            throw new IllegalArgumentException("rectangle outside image");
        }
    }
}
