package com.nttdocomo.opt.device;

import com.nttdocomo.device.CodeReader;

/**
 * Defines optional code kinds for the code-recognition function.
 * The code kinds defined by this class are used with instances of
 * {@link CodeReader}.
 */
public class CodeReader2 extends CodeReader {
    /** Code kind meaning object recognition (=9). */
    public static final int CODE_ER = 9;

    /** Code kind meaning FP Code (=10). */
    public static final int CODE_FP = 10;

    /**
     * Applications cannot create instances of this class directly.
     */
    protected CodeReader2() {
    }
}
