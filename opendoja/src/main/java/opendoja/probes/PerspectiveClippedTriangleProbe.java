package opendoja.probes;

import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.Image;
import com.nttdocomo.ui.graphics3d.DrawableObject3D;
import com.nttdocomo.ui.graphics3d.Primitive;

import java.util.Arrays;

public final class PerspectiveClippedTriangleProbe {
    private static final int IMAGE_SIZE = 96;
    private static final int COLOR = 0xFFFFFFFF;

    private PerspectiveClippedTriangleProbe() {
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        DemoLog.enableInfoLogging();

        int[] clippedTrianglePixels = render(clippedTriangle());
        int[] explicitQuadPixels = render(explicitNearPlaneQuad());
        if (!Arrays.equals(clippedTrianglePixels, explicitQuadPixels)) {
            throw new IllegalStateException("Near-plane clipped triangle coverage differed from explicit clipped quad");
        }
        if (!hasOpaquePixel(clippedTrianglePixels)) {
            throw new IllegalStateException("Near-plane clipped triangle rendered no pixels");
        }

        DemoLog.info(PerspectiveClippedTriangleProbe.class, "near-plane clipped triangle matches explicit quad");
    }

    private static Primitive clippedTriangle() {
        Primitive primitive = new Primitive(Primitive.PRIMITIVE_TRIANGLES, Primitive.COLOR_PER_PRIMITIVE, 1);
        int[] vertices = primitive.getVertexArray();
        setVertex(vertices, 0, -10, -10, 0);
        setVertex(vertices, 1, 10, -10, 4);
        setVertex(vertices, 2, 2, 10, 4);
        primitive.getColorArray()[0] = COLOR;
        return primitive;
    }

    private static Primitive explicitNearPlaneQuad() {
        Primitive primitive = new Primitive(Primitive.PRIMITIVE_QUADS, Primitive.COLOR_PER_PRIMITIVE, 1);
        int[] vertices = primitive.getVertexArray();
        // The clipped triangle above intersects the z=1 near plane at these points:
        // A(-10,-10,0) -> B(10,-10,4) gives (-5,-10,1)
        // A(-10,-10,0) -> C(2,10,4) gives (-7,-5,1)
        setVertex(vertices, 0, -5, -10, 1);
        setVertex(vertices, 1, 10, -10, 4);
        setVertex(vertices, 2, 2, 10, 4);
        setVertex(vertices, 3, -7, -5, 1);
        primitive.getColorArray()[0] = COLOR;
        return primitive;
    }

    private static int[] render(DrawableObject3D object) {
        Image image = Image.createImage(IMAGE_SIZE, IMAGE_SIZE);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Graphics.getColorOfRGB(0, 0, 0, 255));
        graphics.fillRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);
        graphics.setPerspectiveView(1f, 100f, 60f);
        graphics.setClipRectFor3D(0, 0, IMAGE_SIZE, IMAGE_SIZE);
        graphics.renderObject3D(object, null);
        graphics.flushBuffer();
        return graphics.getRGBPixels(0, 0, IMAGE_SIZE, IMAGE_SIZE, null, 0);
    }

    private static boolean hasOpaquePixel(int[] pixels) {
        for (int pixel : pixels) {
            if ((pixel >>> 24) != 0) {
                return true;
            }
        }
        return false;
    }

    private static void setVertex(int[] vertices, int index, int x, int y, int z) {
        int offset = index * 3;
        vertices[offset] = x;
        vertices[offset + 1] = y;
        vertices[offset + 2] = z;
    }
}
