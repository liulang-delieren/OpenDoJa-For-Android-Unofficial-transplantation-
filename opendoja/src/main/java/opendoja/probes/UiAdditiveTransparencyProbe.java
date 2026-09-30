package opendoja.probes;

import com.nttdocomo.opt.ui.j3d.Graphics3D;
import com.nttdocomo.opt.ui.j3d.PrimitiveArray;
import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.Image;
import com.nttdocomo.ui.graphics3d.DrawableObject3D;
import com.nttdocomo.ui.graphics3d.Primitive;
import com.nttdocomo.ui.util3d.Transform;

public final class UiAdditiveTransparencyProbe {
    private UiAdditiveTransparencyProbe() {
    }

    public static void main(String[] args) {
        DemoLog.enableInfoLogging();
        int alphaPixel = renderUiAlphaPixel(1f);
        int alphaHalfPixel = renderUiAlphaPixel(0.5f);
        int percentPixel = renderCenterPixel(50f);
        int fractionalPixel = renderCenterPixel(0.5f);
        int helperPixel = renderHelperPixel(78f);
        int optHalfPixel = renderOptHalfPixel();
        if ((alphaPixel & 0x00FFFFFF) != 0x00FFFFFF) {
            throw new IllegalStateException(String.format(
                    "ui alpha blend failed pixel=%08x",
                    alphaPixel));
        }
        if ((alphaHalfPixel & 0x00FFFFFF) != 0x00808080) {
            throw new IllegalStateException(String.format(
                    "ui half alpha blend failed pixel=%08x",
                    alphaHalfPixel));
        }
        if ((percentPixel & 0x00FFFFFF) != 0x00808080) {
            throw new IllegalStateException(String.format(
                    "percent transparency failed pixel=%08x",
                    percentPixel));
        }
        if ((fractionalPixel & 0x00FFFFFF) != 0x00808080) {
            throw new IllegalStateException(String.format(
                    "fractional transparency failed pixel=%08x",
                    fractionalPixel));
        }
        if ((helperPixel & 0x00FFFFFF) != 0x00C7C7C7) {
            throw new IllegalStateException(String.format(
                    "helper transparency failed pixel=%08x",
                    helperPixel));
        }
        if ((optHalfPixel & 0x00FFFFFF) != 0x007F7F7F) {
            throw new IllegalStateException(String.format(
                    "opt half blend failed pixel=%08x",
                    optHalfPixel));
        }
        DemoLog.info(UiAdditiveTransparencyProbe.class, String.format(
                "alpha=%08x alphaHalf=%08x percent=%08x fractional=%08x helper=%08x optHalf=%08x",
                alphaPixel,
                alphaHalfPixel,
                percentPixel,
                fractionalPixel,
                helperPixel,
                optHalfPixel));
    }

    private static int renderUiAlphaPixel(float transparency) {
        Image image = Image.createImage(64, 64);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Graphics.getColorOfRGB(0, 0, 0, 255));
        graphics.fillRect(0, 0, 64, 64);
        graphics.setParallelView(64, 64);
        graphics.renderObject3D(alphaQuad(transparency), null);
        graphics.flushBuffer();
        return graphics.getRGBPixel(32, 32);
    }

    private static int renderCenterPixel(float transparency) {
        Image image = Image.createImage(64, 64);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Graphics.getColorOfRGB(0, 0, 0, 255));
        graphics.fillRect(0, 0, 64, 64);
        graphics.setParallelView(64, 64);
        graphics.renderObject3D(overlayQuad(transparency), null);
        graphics.flushBuffer();
        return graphics.getRGBPixel(32, 32);
    }

    private static Primitive alphaQuad(float transparency) {
        Primitive primitive = new Primitive(
                Primitive.PRIMITIVE_QUADS,
                Primitive.COLOR_PER_PRIMITIVE,
                1);
        int[] vertices = primitive.getVertexArray();
        setVertex(vertices, 0, -32, 32, 0);
        setVertex(vertices, 1, 32, 32, 0);
        setVertex(vertices, 2, 32, -32, 0);
        setVertex(vertices, 3, -32, -32, 0);
        primitive.getColorArray()[0] = 0xFFFFFFFF;
        primitive.setBlendMode(DrawableObject3D.BLEND_ALPHA);
        primitive.setTransparency(transparency);
        return primitive;
    }

    private static Primitive overlayQuad(float transparency) {
        Primitive primitive = new Primitive(
                Primitive.PRIMITIVE_QUADS,
                Primitive.COLOR_PER_PRIMITIVE,
                1);
        int[] vertices = primitive.getVertexArray();
        setVertex(vertices, 0, -32, 32, 0);
        setVertex(vertices, 1, 32, 32, 0);
        setVertex(vertices, 2, 32, -32, 0);
        setVertex(vertices, 3, -32, -32, 0);
        primitive.getColorArray()[0] = 0xFFFFFFFF;
        primitive.setBlendMode(DrawableObject3D.BLEND_ADD);
        primitive.setTransparency(transparency);
        return primitive;
    }

    private static int renderHelperPixel(float transparency) {
        Image image = Image.createImage(64, 64);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Graphics.getColorOfRGB(0, 0, 0, 255));
        graphics.fillRect(0, 0, 64, 64);
        graphics.setParallelView(64, 64);
        Transform transform = new Transform();
        transform.setIdentity();
        transform.translate(-32f, -32f, 0f);
        graphics.setTransform(transform);
        Primitive primitive = new Primitive(
                Primitive.PRIMITIVE_QUADS,
                Primitive.COLOR_PER_PRIMITIVE,
                1);
        int[] vertices = primitive.getVertexArray();
        setVertex(vertices, 0, 0, 0, 0);
        setVertex(vertices, 1, 64, 0, 0);
        setVertex(vertices, 2, 64, 64, 0);
        setVertex(vertices, 3, 0, 64, 0);
        primitive.getColorArray()[0] = 0xFFFFFFFF;
        primitive.setBlendMode(DrawableObject3D.BLEND_ADD);
        primitive.setTransparency(transparency);
        graphics.renderObject3D(primitive, null);
        graphics.flushBuffer();
        return graphics.getRGBPixel(32, 32);
    }

    private static int renderOptHalfPixel() {
        Image image = Image.createImage(64, 64);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Graphics.getColorOfRGB(0, 0, 0, 255));
        graphics.fillRect(0, 0, 64, 64);
        graphics.setScreenCenter(32, 32);
        graphics.setScreenScale(4096, 4096);
        graphics.enableSemiTransparent(true);
        PrimitiveArray primitives = new PrimitiveArray(
                Graphics3D.PRIMITIVE_QUADS,
                Graphics3D.COLOR_PER_COMMAND,
                1);
        int[] vertices = primitives.getVertexArray();
        writeOptVertex(vertices, 0, -32, 32, 0);
        writeOptVertex(vertices, 1, 32, 32, 0);
        writeOptVertex(vertices, 2, 32, -32, 0);
        writeOptVertex(vertices, 3, -32, -32, 0);
        primitives.getColorArray()[0] = 0x00FFFFFF;
        graphics.renderPrimitives(primitives, Graphics3D.ATTR_BLEND_HALF);
        graphics.flush();
        return graphics.getRGBPixel(32, 32);
    }

    private static void setVertex(int[] vertices, int index, int x, int y, int z) {
        int offset = index * 3;
        vertices[offset] = x;
        vertices[offset + 1] = y;
        vertices[offset + 2] = z;
    }

    private static void writeOptVertex(int[] vertices, int index, int x, int y, int z) {
        setVertex(vertices, index, x, y, z);
    }
}
