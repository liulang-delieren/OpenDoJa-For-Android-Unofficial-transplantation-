package com.nttdocomo.ui;

/**
 * Defines a class that holds and manages multiple sprites.
 * It is used to draw sprites and perform collision detection.
 * To draw sprites, set sprite objects in a sprite-set object and call
 * {@link Graphics#drawSpriteSet(SpriteSet)} with that sprite-set object.
 * Invisible sprites and sprites that hold disposed image objects are not
 * targets for drawing.
 *
 * <p>In sprite collision detection, it is checked whether sprites managed by
 * the sprite set overlap each other.
 * The overlap check is performed using the image rectangles held by the
 * sprites.
 * The sprite flip-display setting and two-dimensional linear-transform setting
 * are not reflected.
 * Invisible sprites and sprites holding disposed image objects are not targets
 * for collision detection.
 * If collision detection is performed on the same sprite, the result is always
 * treated as not colliding.</p>
 *
 * <p>Introduced in DoJa-3.5 (900i).</p>
 *
 * @see Sprite
 * @see Graphics#drawSpriteSet(SpriteSet)
 */
public class SpriteSet {
    private final Sprite[] sprites;
    private final int[] collisionFlags;

    /**
     * Creates a sprite set by specifying an array of sprite objects.
     * Sprites with larger array indices have higher priority and are displayed
     * in front.
     * The sprite-set object keeps the reference to the array passed as the
     * argument.
     * If an element of the sprite-object array is {@code null}, that sprite is
     * not a target for drawing or collision detection.
     * By default, all collision-detection flags are initialized to {@code 0}.
     *
     * @param sprites the sprite-object array
     * @throws NullPointerException if {@code sprites} is {@code null}
     * @throws IllegalArgumentException if the number of elements is {@code 0}
     *         or greater than {@code 32}
     */
    public SpriteSet(Sprite[] sprites) {
        if (sprites == null) {
            throw new NullPointerException("sprites");
        }
        if (sprites.length == 0 || sprites.length > 32) {
            throw new IllegalArgumentException("sprites.length");
        }
        this.sprites = sprites;
        this.collisionFlags = new int[sprites.length];
    }

    /**
     * Gets the number of sprites.
     * This returns the same value as {@code getSprites().length}.
     *
     * @return the number of sprites
     */
    public int getCount() {
        return sprites.length;
    }

    /**
     * Gets the array of sprite objects.
     *
     * @return the array of sprite objects
     */
    public Sprite[] getSprites() {
        return sprites;
    }

    /**
     * Gets a sprite object by specifying its index.
     * This returns the same value as {@code getSprites()[index]}.
     *
     * @param index the index of the sprite object to obtain
     * @return the sprite object at the specified index
     * @throws ArrayIndexOutOfBoundsException if {@code index} is negative or is
     *         greater than or equal to the number of sprites
     */
    public Sprite getSprite(int index) {
        return sprites[index];
    }

    /**
     * Performs mutual collision detection for all sprites.
     * This is the same as calling {@link #setCollisionOf(int)} for every
     * sprite.
     * The detection-result flags can be obtained by
     * {@link #getCollisionFlag(int)}.
     */
    public void setCollisionAll() {
        for (int i = 0; i < sprites.length; i++) {
            collisionFlags[i] = computeCollisionFlag(i);
        }
    }

    /**
     * Performs mutual collision detection for one sprite.
     * Only the detection-result flag for the sprite at the specified index is
     * updated and kept until this method is called again for the same index or
     * {@link #setCollisionAll()} is called.
     *
     * @param index the index of the sprite for which collision detection is
     *              performed
     * @throws ArrayIndexOutOfBoundsException if {@code index} is negative or is
     *         greater than or equal to the number of sprites
     */
    public void setCollisionOf(int index) {
        collisionFlags[index] = computeCollisionFlag(index);
    }

    /**
     * Gets the collision-detection flag for two sprites.
     * It checks the mutual-collision-detection result flags set by
     * {@link #setCollisionAll()} and {@link #setCollisionOf(int)} and returns
     * whether the two specified sprites are colliding.
     * If the value of {@code getCollisionFlag(index1) & (1 << index2)} is
     * non-zero, {@code true} is returned; if it is zero, {@code false} is
     * returned.
     *
     * @param leftIndex the index of one sprite
     * @param rightIndex the index of the other sprite
     * @return {@code true} if the sprites are colliding, or {@code false} if
     *         they are not; if the same sprite is specified, {@code false} is
     *         returned
     * @throws ArrayIndexOutOfBoundsException if either index is negative or is
     *         greater than or equal to the number of sprites
     */
    public boolean isCollision(int leftIndex, int rightIndex) {
        if (leftIndex == rightIndex) {
            return false;
        }
        return (getCollisionFlag(leftIndex) & (1 << rightIndex)) != 0;
    }

    /**
     * Gets the mutual-collision-detection flag for one sprite.
     * The {@code n}-th bit ({@code 1 << n}) of the flag is {@code 1} if the
     * sprite collides with the sprite whose index value is {@code n}; it is
     * {@code 0} if it does not collide.
     *
     * @param index the index of the sprite whose collision-detection flag is
     *              obtained
     * @return the mutual-collision-detection flag of the specified sprite
     * @throws ArrayIndexOutOfBoundsException if {@code index} is negative or is
     *         greater than or equal to the number of sprites
     */
    public int getCollisionFlag(int index) {
        return collisionFlags[index];
    }

    private int computeCollisionFlag(int index) {
        Sprite left = sprites[index];
        if (!isCollisionTarget(left)) {
            return 0;
        }
        int flag = 0;
        for (int i = 0; i < sprites.length; i++) {
            if (i == index) {
                continue;
            }
            Sprite right = sprites[i];
            if (!isCollisionTarget(right)) {
                continue;
            }
            if (overlaps(left, right)) {
                flag |= 1 << i;
            }
        }
        return flag;
    }

    private static boolean overlaps(Sprite left, Sprite right) {
        return left.getX() < right.getX() + right.getWidth()
                && left.getX() + left.getWidth() > right.getX()
                && left.getY() < right.getY() + right.getHeight()
                && left.getY() + left.getHeight() > right.getY();
    }

    private static boolean isCollisionTarget(Sprite sprite) {
        if (sprite == null || !sprite.isVisible()) {
            return false;
        }
        Image image = sprite.image();
        if (image == null) {
            return false;
        }
        try {
            image.getWidth();
            image.getHeight();
            return true;
        } catch (UIException e) {
            return false;
        }
    }
}
