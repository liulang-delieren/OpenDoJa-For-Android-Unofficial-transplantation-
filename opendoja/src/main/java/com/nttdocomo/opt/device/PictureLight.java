package com.nttdocomo.opt.device;

import com.nttdocomo.ui.Graphics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides the means to control the picture light.
 */
public class PictureLight {
    /** Flash-duration value meaning a short duration (=1). */
    public static final int FLASH_SHORT = 1;
    /** Flash-duration value meaning a medium duration (=2). */
    public static final int FLASH_MIDDLE = 2;
    /** Flash-duration value meaning a long duration (=3). */
    public static final int FLASH_LONG = 3;

    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "openDoJa-picture-light");
        thread.setDaemon(true);
        return thread;
    });
    private static final PictureLight INSTANCE = new PictureLight();

    private volatile boolean lightOn;
    private volatile boolean flashBusy;
    private volatile int color = Graphics.getColorOfRGB(255, 255, 255);
    private volatile int time = FLASH_MIDDLE;
    private volatile long flashGeneration;

    /**
     * Applications cannot create instances of this class directly.
     */
    protected PictureLight() {
    }

    /**
     * Gets the picture-light object.
     * By default, the color is white and the high-intensity flash time is set
     * to {@link #FLASH_MIDDLE}.
     *
     * @return the picture-light object
     */
    public static PictureLight getPictureLight() {
        return INSTANCE;
    }

    /**
     * Sets the lighted state of the picture light.
     *
     * @param b {@code true} to turn the picture light on, {@code false} to
     *        turn it off
     */
    public synchronized void light(boolean b) {
        if (b) {
            lightOn = true;
            return;
        }
        lightOn = false;
        flashBusy = false;
        flashGeneration++;
    }

    /**
     * Gets whether the picture light is currently on.
     *
     * @return {@code true} if the picture light is on or performing a flash
     */
    public boolean isLightOn() {
        return lightOn || flashBusy;
    }

    /**
     * Sets the color of the picture light.
     * If this method is called during high-intensity flashing, the request is
     * ignored.
     *
     * @param color the color value obtained from
     *        {@link Graphics#getColorOfRGB(int, int, int)}
     */
    public synchronized void setColor(int color) {
        if (flashBusy) {
            return;
        }
        this.color = color;
    }

    /**
     * Gets the color set for the picture light.
     *
     * @return the configured color
     */
    public int getColor() {
        return color;
    }

    /**
     * Performs high-intensity flashing.
     * If flashing is already in progress, or if flashing is still cooling down,
     * this call is ignored.
     */
    public synchronized void flash() {
        if (flashBusy) {
            return;
        }
        lightOn = true;
        flashBusy = true;
        long generation = ++flashGeneration;
        long busyMillis = switch (time) {
            case FLASH_SHORT -> 250L;
            case FLASH_LONG -> 1000L;
            default -> 500L;
        };
        EXECUTOR.schedule(() -> {
            synchronized (PictureLight.this) {
                if (flashGeneration == generation) {
                    flashBusy = false;
                }
            }
        }, busyMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets whether high-intensity flashing is still busy.
     *
     * @return {@code true} if the next flash cannot yet be started
     */
    public boolean isFlashBusy() {
        return flashBusy;
    }

    /**
     * Sets the high-intensity flash duration.
     *
     * @param time one of {@link #FLASH_SHORT}, {@link #FLASH_MIDDLE}, or
     *        {@link #FLASH_LONG}
     * @throws IllegalArgumentException if {@code time} is invalid
     */
    public void setTime(int time) {
        if (time != FLASH_SHORT && time != FLASH_MIDDLE && time != FLASH_LONG) {
            throw new IllegalArgumentException("Unsupported flash time: " + time);
        }
        this.time = time;
    }

    /**
     * Gets the high-intensity flash duration setting.
     *
     * @return the flash-duration value
     */
    public int getTime() {
        return time;
    }
}
