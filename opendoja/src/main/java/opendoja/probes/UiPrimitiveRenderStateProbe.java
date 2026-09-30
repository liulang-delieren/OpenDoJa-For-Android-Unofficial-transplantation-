package opendoja.probes;

import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.Image;
import com.nttdocomo.ui.graphics3d.Primitive;

import java.lang.reflect.Method;

public final class UiPrimitiveRenderStateProbe {
    private UiPrimitiveRenderStateProbe() {
    }

    public static void main(String[] args) throws Exception {
        DemoLog.enableInfoLogging();
        int doubleSidedPixel = renderBackFacingTriangle(true);
        int culledPixel = renderBackFacingTriangle(false);
        if ((doubleSidedPixel & 0x00FFFFFF) != 0x00FFFFFF) {
            throw new IllegalStateException(String.format(
                    "double-sided primitive did not render: pixel=%08x", doubleSidedPixel));
        }
        if ((culledPixel & 0x00FFFFFF) != 0x00000000) {
            throw new IllegalStateException(String.format(
                    "single-sided back-facing primitive was not culled: pixel=%08x", culledPixel));
        }
        DemoLog.info(UiPrimitiveRenderStateProbe.class, String.format(
                "doubleSided=%08x culled=%08x", doubleSidedPixel, culledPixel));
    }

    private static int renderBackFacingTriangle(boolean doubleSided) throws Exception {
        Image image = Image.createImage(64, 64);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Graphics.getColorOfRGB(0, 0, 0, 255));
        graphics.fillRect(0, 0, 64, 64);
        graphics.setParallelView(64, 64);
        Primitive primitive = new Primitive(
                Primitive.PRIMITIVE_TRIANGLES,
                Primitive.COLOR_PER_PRIMITIVE,
                1);
        int[] vertices = primitive.getVertexArray();
        setVertex(vertices, 0, -20, -20, 0);
        setVertex(vertices, 1, 20, -20, 0);
        setVertex(vertices, 2, 0, 20, 0);
        primitive.getColorArray()[0] = 0xFFFFFFFF;
        Method setDoubleSided = Primitive.class.getDeclaredMethod("setDoubleSided", boolean.class);
        setDoubleSided.setAccessible(true);
        setDoubleSided.invoke(primitive, doubleSided);
        graphics.renderObject3D(primitive, null);
        graphics.flushBuffer();
        return graphics.getRGBPixel(32, 25);
    }

    private static void setVertex(int[] vertices, int index, int x, int y, int z) {
        int offset = index * 3;
        vertices[offset] = x;
        vertices[offset + 1] = y;
        vertices[offset + 2] = z;
    }
}
