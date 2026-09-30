package com.nttdocomo.opt.ui.j3d2;

import com.nttdocomo.ui.Canvas;
import com.nttdocomo.ui.Graphics;

/**
 * Mitsubishi-specific canvas for the j3d2/Z3D API.
 */
public abstract class C3DCanvas extends Canvas {
    private final C3DGraphics c3dGraphics = new C3DGraphics(this);

    protected C3DCanvas() {
    }

    public final C3DGraphics getC3DGraphics() {
        return c3dGraphics;
    }

    public abstract void paint(C3DGraphics graphics);

    @Override
    public final void paint(Graphics graphics) {
        c3dGraphics.attach(graphics);
        paint(c3dGraphics);
    }
}
