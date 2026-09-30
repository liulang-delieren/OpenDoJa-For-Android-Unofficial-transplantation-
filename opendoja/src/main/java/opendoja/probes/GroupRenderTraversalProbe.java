package opendoja.probes;

import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.Image;
import com.nttdocomo.ui.graphics3d.Group;
import com.nttdocomo.ui.graphics3d.Primitive;
import com.nttdocomo.ui.util3d.Transform;

import java.util.Arrays;

public final class GroupRenderTraversalProbe {
    private GroupRenderTraversalProbe() {
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        DemoLog.enableInfoLogging();

        Primitive primitive = coloredTriangle();
        Transform parentTransform = new Transform();
        parentTransform.translate(8f, -6f, 0f);

        Transform groupTransform = new Transform();
        groupTransform.translate(10f, 12f, 0f);

        Group group = new Group();
        group.setTransform(groupTransform);
        group.addElement(primitive);

        int[] groupedPixels = render(group, parentTransform);
        int[] directPixels = render(primitive, compose(parentTransform, groupTransform));
        if (!Arrays.equals(groupedPixels, directPixels)) {
            throw new IllegalStateException("Grouped render did not match explicitly composed transform output");
        }
        if (!hasColoredPixel(groupedPixels, 0xFFFF8040)) {
            throw new IllegalStateException("Grouped render produced no triangle pixels");
        }

        DemoLog.info(GroupRenderTraversalProbe.class, "group render traversal OK");
    }

    private static Primitive coloredTriangle() {
        Primitive primitive = new Primitive(
                Primitive.PRIMITIVE_TRIANGLES,
                Primitive.COLOR_PER_PRIMITIVE,
                1);
        int[] vertices = primitive.getVertexArray();
        setVertex(vertices, 0, -8, 8, 0);
        setVertex(vertices, 1, 8, 8, 0);
        setVertex(vertices, 2, 0, -8, 0);
        primitive.getColorArray()[0] = 0xFFFF8040;
        return primitive;
    }

    private static int[] render(com.nttdocomo.ui.graphics3d.DrawableObject3D object, Transform transform) {
        Image image = Image.createImage(64, 64);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Graphics.getColorOfRGB(0, 0, 0, 255));
        graphics.fillRect(0, 0, 64, 64);
        graphics.setParallelView(64, 64);
        graphics.renderObject3D(object, transform);
        graphics.flushBuffer();
        return graphics.getRGBPixels(0, 0, 64, 64, null, 0);
    }

    private static Transform compose(Transform parent, Transform child) {
        Transform combined = new Transform(parent);
        combined.multiply(child);
        return combined;
    }

    private static boolean hasColoredPixel(int[] pixels, int color) {
        for (int pixel : pixels) {
            if (pixel == color) {
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
