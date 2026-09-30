package opendoja.probes;

import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.Image;
import com.nttdocomo.ui.graphics3d.Primitive;
import opendoja.host.DesktopSurface;

import java.lang.reflect.Field;

public final class ProjectionDepthPassProbe {
    private static final Field SURFACE_FIELD = accessibleField(Graphics.class, "surface");
    private static final Field DEPTH_FRAME_ACTIVE_FIELD = accessibleField(DesktopSurface.class, "depthFrameActive");

    private ProjectionDepthPassProbe() {
    }

    public static void main(String[] args) throws Exception {
        DemoLog.enableInfoLogging();

        Graphics graphics = Image.createImage(64, 64).getGraphics();
        graphics.setParallelView(64, 64);
        graphics.renderObject3D(coloredQuad(), null);
        check(depthFrameActive(graphics), "initial render did not activate a depth frame");

        graphics.setParallelView(32, 64);
        check(depthFrameActive(graphics), "parallel projection parameter change ended the depth frame");

        graphics.setPerspectiveView(1f, 100f, 60f);
        check(!depthFrameActive(graphics), "parallel-to-perspective projection change kept stale depth");

        graphics.renderObject3D(coloredQuad(), null);
        check(depthFrameActive(graphics), "render after projection change did not activate a fresh depth frame");

        graphics.setPerspectiveView(1f, 100f, 45f);
        check(depthFrameActive(graphics), "perspective projection parameter change ended the depth frame");

        graphics.setParallelView(64, 64);
        check(!depthFrameActive(graphics), "perspective-to-parallel projection change kept stale depth");

        DemoLog.info(ProjectionDepthPassProbe.class, "projection depth pass boundary ok");
    }

    private static boolean depthFrameActive(Graphics graphics) throws IllegalAccessException {
        DesktopSurface surface = (DesktopSurface) SURFACE_FIELD.get(graphics);
        return DEPTH_FRAME_ACTIVE_FIELD.getBoolean(surface);
    }

    private static Field accessibleField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Primitive coloredQuad() {
        Primitive primitive = new Primitive(
                Primitive.PRIMITIVE_QUADS,
                Primitive.COLOR_PER_PRIMITIVE,
                1);
        int[] vertices = primitive.getVertexArray();
        setVertex(vertices, 0, -16, -16, 0);
        setVertex(vertices, 1, 16, -16, 0);
        setVertex(vertices, 2, 16, 16, 0);
        setVertex(vertices, 3, -16, 16, 0);
        primitive.getColorArray()[0] = 0xFFFFFFFF;
        return primitive;
    }

    private static void setVertex(int[] vertices, int index, int x, int y, int z) {
        int offset = index * 3;
        vertices[offset] = x;
        vertices[offset + 1] = y;
        vertices[offset + 2] = z;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
