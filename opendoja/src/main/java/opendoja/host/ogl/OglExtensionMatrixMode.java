package opendoja.host.ogl;

enum OglExtensionMatrixMode {
    ACRODEA_WORLD(Vendor.ACRODEA, 1),
    ACRODEA_CAMERA(Vendor.ACRODEA, 2);

    enum Vendor {
        ACRODEA
    }

    private final Vendor vendor;
    private final int glMatrixMode;

    OglExtensionMatrixMode(Vendor vendor, int glMatrixMode) {
        this.vendor = vendor;
        this.glMatrixMode = glMatrixMode;
    }

    Vendor vendor() {
        return vendor;
    }

    int glMatrixMode() {
        return glMatrixMode;
    }

    static OglExtensionMatrixMode fromGlMatrixMode(int glMatrixMode) {
        for (OglExtensionMatrixMode mode : values()) {
            if (mode.glMatrixMode == glMatrixMode) {
                return mode;
            }
        }
        return null;
    }
}
