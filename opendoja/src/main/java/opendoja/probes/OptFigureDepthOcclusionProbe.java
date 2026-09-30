package opendoja.probes;

import com.nttdocomo.opt.ui.j3d.Graphics3D;
import com.nttdocomo.opt.ui.j3d.PrimitiveArray;
import opendoja.g3d.MascotFigure;
import opendoja.g3d.MbacModel;
import opendoja.g3d.Software3DContext;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public final class OptFigureDepthOcclusionProbe {
    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;
    private static final int CENTER_X = WIDTH / 2;
    private static final int CENTER_Y = HEIGHT / 2;
    private static final int NEAR_Z = 256;
    private static final int FAR_Z = 512;
    private static final int FRONT_COLOR = 0xFF20C040;
    private static final int REAR_FIGURE_COLOR = 0xFFC03030;
    private static final int REAR_PRIMITIVE_COLOR = 0xFF3050D0;

    private OptFigureDepthOcclusionProbe() {
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        DemoLog.enableInfoLogging();

        verifyFigureOccludesLaterFigure();
        verifyFigureOccludesLaterPrimitive();

        DemoLog.info(OptFigureDepthOcclusionProbe.class, "opt figure depth occlusion ok");
    }

    private static void verifyFigureOccludesLaterFigure() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            Software3DContext context = configuredContext();
            context.renderOptFigure(graphics, image, 0, 0, WIDTH, HEIGHT, quadFigure(FRONT_COLOR, NEAR_Z));
            context.renderOptFigure(graphics, image, 0, 0, WIDTH, HEIGHT, quadFigure(REAR_FIGURE_COLOR, FAR_Z));
        } finally {
            graphics.dispose();
        }
        checkColor(image.getRGB(CENTER_X, CENTER_Y), FRONT_COLOR, "later rear figure must stay behind the front figure");
    }

    private static void verifyFigureOccludesLaterPrimitive() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            Software3DContext context = configuredContext();
            context.renderOptFigure(graphics, image, 0, 0, WIDTH, HEIGHT, quadFigure(FRONT_COLOR, NEAR_Z));
            context.renderOptPrimitives(graphics, image, 0, 0, WIDTH, HEIGHT, quadPrimitive(FAR_Z, REAR_PRIMITIVE_COLOR), 0);
        } finally {
            graphics.dispose();
        }
        checkColor(image.getRGB(CENTER_X, CENTER_Y), FRONT_COLOR, "later rear primitive must stay behind the front figure");
    }

    private static Software3DContext configuredContext() {
        Software3DContext context = new Software3DContext();
        context.setOptScreenCenter(CENTER_X, CENTER_Y);
        context.setOptPerspective(20, 8191, 603);
        context.setOptViewTransform(identity());
        float[] depthBuffer = new float[WIDTH * HEIGHT];
        Arrays.fill(depthBuffer, Float.NEGATIVE_INFINITY);
        context.setFrameDepthBuffer(depthBuffer);
        return context;
    }

    private static MascotFigure quadFigure(int color, int z) {
        int[] vertices = new int[]{
                -48, -48, z,
                -48, 48, z,
                48, -48, z,
                48, 48, z
        };
        MbacModel.Polygon polygon = new MbacModel.Polygon(
                new int[]{0, 1, 2, 3},
                null,
                color,
                0,
                0,
                0,
                true,
                false
        );
        return new MascotFigure(new MbacModel(vertices, vertices.clone(), new MbacModel.Polygon[]{polygon}, 1, new MbacModel.Bone[0]));
    }

    private static PrimitiveArray quadPrimitive(int z, int color) {
        PrimitiveArray primitive = new PrimitiveArray(Graphics3D.PRIMITIVE_QUADS, Graphics3D.COLOR_PER_FACE, 1);
        int[] vertices = primitive.getVertexArray();
        int[] colors = primitive.getColorArray();
        int[] quadVertices = new int[]{
                -48, -48, z,
                -48, 48, z,
                48, -48, z,
                48, 48, z
        };
        System.arraycopy(quadVertices, 0, vertices, 0, quadVertices.length);
        colors[0] = color;
        return primitive;
    }

    private static float[] identity() {
        return new float[]{
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
        };
    }

    private static void checkColor(int actual, int expected, String message) {
        if (actual != expected) {
            throw new IllegalStateException(message + ": expected=0x" + Integer.toHexString(expected)
                    + " actual=0x" + Integer.toHexString(actual));
        }
    }
}
