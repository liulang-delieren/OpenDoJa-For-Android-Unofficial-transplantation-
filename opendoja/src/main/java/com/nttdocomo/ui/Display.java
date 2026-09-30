package com.nttdocomo.ui;

import opendoja.host.DoJaRuntime;

/**
 * Defines the device view.
 * The Display class abstracts the terminal screen and keypad and is used to
 * obtain information about them.
 */
public class Display {
    /** Event type that indicates a key-down event (=0). */
    public static final int KEY_PRESSED_EVENT = 0;
    /** Event type that indicates a key-up event (=1). */
    public static final int KEY_RELEASED_EVENT = 1;
    /** Event type that indicates a resume event (=4). */
    public static final int RESUME_VM_EVENT = 4;
    /**
     * Event type that indicates a reset event (=5).
     * In practice this event does not occur.
     */
    public static final int RESET_VM_EVENT = 5;
    /** Event type that indicates an update event (=6). */
    public static final int UPDATE_VM_EVENT = 6;
    /** Event type that indicates a timer event (=7). */
    public static final int TIMER_EXPIRED_EVENT = 7;
    /**
     * Event type that indicates a media event (=8).
     * These events relate to media playback start and end.
     */
    public static final int MEDIA_EVENT = 8;
    /**
     * Minimum vendor event value (=64).
     * Vendor-defined events must use values from this constant through
     * {@link #MAX_VENDOR_EVENT}.
     */
    protected static final int MIN_VENDOR_EVENT = 64;
    /**
     * Maximum vendor event value (=127).
     * Vendor-defined events must use values from {@link #MIN_VENDOR_EVENT}
     * through this constant.
     */
    protected static final int MAX_VENDOR_EVENT = 127;
    /**
     * Event type that indicates the start of a pointing-device operation (=64).
     * This is a vendor-defined event and may be unsupported on some terminals.
     */
    public static final int POINTER_MOVED_EVENT = 64;
    /**
     * Event type that indicates a fingerprint-sensor event (=0x41).
     * This is a vendor-defined event and may be unsupported on some terminals.
     */
    public static final int FINGER_MOVED_EVENT = 65;
    /** Numeric key 0 (=0x00). */
    public static final int KEY_0 = 0;
    /** Numeric key 1 (=0x01). */
    public static final int KEY_1 = 1;
    /** Numeric key 2 (=0x02). */
    public static final int KEY_2 = 2;
    /** Numeric key 3 (=0x03). */
    public static final int KEY_3 = 3;
    /** Numeric key 4 (=0x04). */
    public static final int KEY_4 = 4;
    /** Numeric key 5 (=0x05). */
    public static final int KEY_5 = 5;
    /** Numeric key 6 (=0x06). */
    public static final int KEY_6 = 6;
    /** Numeric key 7 (=0x07). */
    public static final int KEY_7 = 7;
    /** Numeric key 8 (=0x08). */
    public static final int KEY_8 = 8;
    /** Numeric key 9 (=0x09). */
    public static final int KEY_9 = 9;
    /** Asterisk key (=0x0a). */
    public static final int KEY_ASTERISK = 10;
    /** Pound key (=0x0b). */
    public static final int KEY_POUND = 11;
    /** Left direction key (=0x10). */
    public static final int KEY_LEFT = 16;
    /** Up direction key (=0x11). */
    public static final int KEY_UP = 17;
    /** Right direction key (=0x12). */
    public static final int KEY_RIGHT = 18;
    /** Down direction key (=0x13). */
    public static final int KEY_DOWN = 19;
    /** Select or decision key (=0x14). */
    public static final int KEY_SELECT = 20;
    /** Soft key 1 (=0x15). */
    public static final int KEY_SOFT1 = 21;
    /** Soft key 2 (=0x16). */
    public static final int KEY_SOFT2 = 22;
    /** Standby i-appli switch key (=0x18). */
    public static final int KEY_IAPP = 24;
    /**
     * Deprecated minimum vendor-defined key-code value (=0x1a).
     * In DoJa-2.0 and later this was replaced by {@link #MIN_OPTION_KEY}.
     */
    protected static final int MIN_VENDOR_KEY = 0x1a;
    /**
     * Deprecated maximum vendor-defined key-code value (=0x1f).
     * In DoJa-2.0 and later this was replaced by {@link #MAX_OPTION_KEY}.
     */
    protected static final int MAX_VENDOR_KEY = 0x1f;
    /**
     * Minimum option-key value (=0x1a).
     * Optional key codes use values from this constant through
     * {@link #MAX_OPTION_KEY}.
     */
    protected static final int MIN_OPTION_KEY = 0x1a;
    /**
     * Maximum option-key value (=0x3f).
     * Optional key codes use values from {@link #MIN_OPTION_KEY} through this
     * constant.
     */
    protected static final int MAX_OPTION_KEY = 0x3f;
    /** Upper-left key (=0x1a). This key may be unsupported on some terminals. */
    public static final int KEY_UPPER_LEFT = 26;
    /** Upper-right key (=0x1b). This key may be unsupported on some terminals. */
    public static final int KEY_UPPER_RIGHT = 27;
    /** Lower-right key (=0x1c). This key may be unsupported on some terminals. */
    public static final int KEY_LOWER_RIGHT = 28;
    /** Lower-left key (=0x1d). This key may be unsupported on some terminals. */
    public static final int KEY_LOWER_LEFT = 29;
    /**
     * Page-up key (=0x1e).
     * Some terminals do not provide this key.
     */
    public static final int KEY_PAGE_UP = 30;
    /**
     * Page-down key (=0x1f).
     * Some terminals do not provide this key.
     */
    public static final int KEY_PAGE_DOWN = 31;
    /** Clear key (=0x20). Some terminals do not provide this key. */
    public static final int KEY_CLEAR = 32;
    /** Mail key (=0x21). Some terminals do not provide this key. */
    public static final int KEY_MAIL = 33;
    /** Memo key (=0x22). Some terminals do not provide this key. */
    public static final int KEY_MEMO = 34;
    /** Menu key (=0x23). Some terminals do not provide this key. */
    public static final int KEY_MENU = 35;
    /** i-mode key (=0x24). Some terminals do not provide this key. */
    public static final int KEY_I_MODE = 36;
    /** Phone-book key (=0x25). Some terminals do not provide this key. */
    public static final int KEY_PHONE_BOOK = 37;
    /** Calendar key (=0x26). Some terminals do not provide this key. */
    public static final int KEY_CALENDAR = 38;
    /** Voice key (=0x27). Some terminals do not provide this key. */
    public static final int KEY_VOICE = 39;
    /** Manner-mode key (=0x28). Some terminals do not provide this key. */
    public static final int KEY_MANNER_MODE = 40;
    /** Drive-mode key (=0x29). Some terminals do not provide this key. */
    public static final int KEY_DRIVE_MODE = 41;
    /** GPS key (=0x2a). Some terminals do not provide this key. */
    public static final int KEY_GPS = 42;
    /** Left roll key (=0x30). Some terminals do not provide this key. */
    public static final int KEY_ROLL_LEFT = 48;
    /** Right roll key (=0x31). Some terminals do not provide this key. */
    public static final int KEY_ROLL_RIGHT = 49;
    /**
     * Back-side key 1 (=0x32).
     * This is a key that can be used while a foldable terminal is closed.
     */
    public static final int KEY_SUB1 = 50;
    /**
     * Back-side key 2 (=0x33).
     * This is a key that can be used while a foldable terminal is closed.
     */
    public static final int KEY_SUB2 = 51;
    /**
     * Back-side key 3 (=0x34).
     * This is a key that can be used while a foldable terminal is closed.
     */
    public static final int KEY_SUB3 = 52;
    /** My Select key (=0x35). Some terminals do not provide this key. */
    public static final int KEY_MY_SELECT = 53;
    /** Camera key (=0x38). Some terminals do not provide this key. */
    public static final int KEY_CAMERA = 56;
    /** Camera zoom-in key (=0x39). Some terminals do not provide this key. */
    public static final int KEY_CAMERA_ZOOM_IN = 57;
    /** Camera zoom-out key (=0x3a). Some terminals do not provide this key. */
    public static final int KEY_CAMERA_ZOOM_OUT = 58;
    /**
     * Camera select or decision key (=0x3b).
     * Some terminals do not provide this key.
     */
    public static final int KEY_CAMERA_SELECT = 59;
    /** Camera light key (=0x3c). Some terminals do not provide this key. */
    public static final int KEY_CAMERA_LIGHT = 60;
    /** Camera shot key (=0x3d). Some terminals do not provide this key. */
    public static final int KEY_CAMERA_SHOT = 61;

    /**
     * Applications cannot directly create instances of this class.
     */
    protected Display() {
    }

    /**
     * Gets the frame currently displayed on the screen.
     *
     * @return the current frame
     */
    public static Frame getCurrent() {
        DoJaRuntime runtime = DoJaRuntime.current();
        return runtime == null ? null : runtime.getCurrentFrame();
    }

    /**
     * Gets the screen width.
     *
     * @return the screen width in pixels
     */
    public static int getWidth() {
        DoJaRuntime runtime = DoJaRuntime.current();
        return runtime == null ? 240 : runtime.displayWidth();
    }

    /**
     * Gets the screen height.
     *
     * @return the screen height in pixels
     */
    public static int getHeight() {
        DoJaRuntime runtime = DoJaRuntime.current();
        return runtime == null ? 240 : runtime.displayHeight();
    }

    /**
     * Tests whether the screen supports color display.
     *
     * @return {@code true} if the screen supports color display, or
     *         {@code false} otherwise
     */
    public static boolean isColor() {
        return numColors() > 2;
    }

    /**
     * Gets the number of colors available on the screen.
     *
     * @return the number of colors, or the number of gray levels if the screen
     *         is not color
     */
    public static int numColors() {
        return 1 << 24;
    }

    /**
     * Sets the frame displayed on the screen.
     *
     * @param frame the frame to display
     */
    public static void setCurrent(Frame frame) {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime != null) {
            runtime.setCurrentFrame(frame);
        }
    }
}
