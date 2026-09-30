package com.nttdocomo.opt.device;

import com.nttdocomo.device.Camera;

/**
 * Defines optional camera attributes for the native camera function.
 * The attributes defined by this class are used with instances of
 * {@link Camera}.
 */
public class Camera2 extends Camera {
    /** Attribute kind meaning the image-encoder format (=128). */
    public static final int DEV_IMAGE_ENCODER = 128;

    /** Attribute value meaning the default encoder format (=0). */
    public static final int ATTR_DEFAULT_IMAGE_ENCODER = 0;

    /** Attribute value meaning that no encoder is applied and the internal format is kept (=1). */
    public static final int ATTR_RAW_IMAGE_ENCODER = 1;

    /** Attribute value meaning JPEG (JFIF baseline) encoding (=2). */
    public static final int ATTR_JPEG_IMAGE_ENCODER = 2;

    /**
     * Applications cannot create instances of this class directly.
     */
    protected Camera2() {
    }
}
