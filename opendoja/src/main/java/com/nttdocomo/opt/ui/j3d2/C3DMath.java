package com.nttdocomo.opt.ui.j3d2;

/**
 * Fixed-point math helpers for the Mitsubishi j3d2/Z3D APIs.
 */
public final class C3DMath {
    public C3DMath() {
    }

    public static int acos(int value) {
        return C3DData.radiansToFixedDegrees(java.lang.Math.acos(C3DData.fixedToDouble(value)));
    }

    public static int abs(int value) {
        return java.lang.Math.abs(value);
    }

    public static int asin(int value) {
        return C3DData.radiansToFixedDegrees(java.lang.Math.asin(C3DData.fixedToDouble(value)));
    }

    public static int atan(int value) {
        return C3DData.radiansToFixedDegrees(java.lang.Math.atan(C3DData.fixedToDouble(value)));
    }

    public static int atan2(int y, int x) {
        return C3DData.radiansToFixedDegrees(java.lang.Math.atan2(C3DData.fixedToDouble(y), C3DData.fixedToDouble(x)));
    }

    public static int cos(int value) {
        return C3DData.doubleToFixed(java.lang.Math.cos(C3DData.fixedDegreesToRadians(value)));
    }

    public static int div(int a, int b) {
        return C3DData.doubleToFixed(C3DData.fixedToDouble(a) / C3DData.fixedToDouble(b));
    }

    public static int log(int value) {
        return C3DData.doubleToFixed(java.lang.Math.log(C3DData.fixedToDouble(value)));
    }

    public static int mul(int a, int b) {
        return C3DData.doubleToFixed(C3DData.fixedToDouble(a) * C3DData.fixedToDouble(b));
    }

    public static int pow(int a, int b) {
        return C3DData.doubleToFixed(java.lang.Math.pow(C3DData.fixedToDouble(a), C3DData.fixedToDouble(b)));
    }

    public static int sin(int value) {
        return C3DData.doubleToFixed(java.lang.Math.sin(C3DData.fixedDegreesToRadians(value)));
    }

    public static int sqrt(int value) {
        return C3DData.doubleToFixed(java.lang.Math.sqrt(C3DData.fixedToDouble(value)));
    }

    public static int tan(int value) {
        return C3DData.doubleToFixed(java.lang.Math.tan(C3DData.fixedDegreesToRadians(value)));
    }
}
