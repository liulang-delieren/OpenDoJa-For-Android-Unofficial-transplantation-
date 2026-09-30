package opendoja.probes;

import opendoja.g3d.MascotFigure;
import opendoja.g3d.MbacModel;
import opendoja.g3d.Software3DContext;
import opendoja.g3d.SoftwareTexture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public final class MbacQuadStripProbe {
    private static final int WIDTH = 192;
    private static final int HEIGHT = 144;

    private MbacQuadStripProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        DemoLog.enableInfoLogging();

        SoftwareTexture texture = makeTexture();
        int[] quadPixels = render(makeQuadModel(), texture);
        int[] stripPixels = render(makeTriangleStripEquivalentModel(), texture);
        if (!Arrays.equals(quadPixels, stripPixels)) {
            throw new IllegalStateException("MBAC quad render diverged from its triangle-strip equivalent");
        }

        DemoLog.info(MbacQuadStripProbe.class, "MBAC quad matches triangle-strip equivalent");
    }

    private static int[] render(MbacModel model, SoftwareTexture texture) {
        MascotFigure figure = new MascotFigure(model);
        figure.setTexture(texture);

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            Software3DContext context = new Software3DContext();
            context.setOptScreenCenter(WIDTH / 2, HEIGHT / 2);
            context.setOptPerspective(20, 8191, 603);
            context.setOptViewTransform(identity());
            context.renderOptFigure(graphics, image, 0, 0, WIDTH, HEIGHT, figure);
        } finally {
            graphics.dispose();
        }
        return image.getRGB(0, 0, WIDTH, HEIGHT, null, 0, WIDTH);
    }

    private static MbacModel makeQuadModel() {
        int[] vertices = vertices();
        MbacModel.Polygon quad = new MbacModel.Polygon(
                new int[]{0, 1, 2, 3},
                new float[]{
                        0f, 0f,
                        0f, 255f,
                        255f, 0f,
                        255f, 255f
                },
                0xFFFFFFFF,
                0,
                0,
                0,
                true,
                false
        );
        return new MbacModel(vertices, vertices.clone(), new MbacModel.Polygon[]{quad}, 1, new MbacModel.Bone[0]);
    }

    private static MbacModel makeTriangleStripEquivalentModel() {
        int[] vertices = vertices();
        MbacModel.Polygon first = new MbacModel.Polygon(
                new int[]{0, 1, 2},
                new float[]{
                        0f, 0f,
                        0f, 255f,
                        255f, 0f
                },
                0xFFFFFFFF,
                0,
                0,
                0,
                true,
                false
        );
        MbacModel.Polygon second = new MbacModel.Polygon(
                new int[]{2, 1, 3},
                new float[]{
                        255f, 0f,
                        0f, 255f,
                        255f, 255f
                },
                0xFFFFFFFF,
                0,
                0,
                0,
                true,
                false
        );
        return new MbacModel(vertices, vertices.clone(), new MbacModel.Polygon[]{first, second}, 1, new MbacModel.Bone[0]);
    }

    private static int[] vertices() {
        return new int[]{
                -64, -48, 256,
                -64, 48, 256,
                64, -48, 320,
                64, 48, 320
        };
    }

    private static SoftwareTexture makeTexture() throws Exception {
        BufferedImage texture = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < texture.getHeight(); y++) {
            for (int x = 0; x < texture.getWidth(); x++) {
                int red = x;
                int green = y;
                int blue = (x + y) >>> 1;
                texture.setRGB(x, y, 0xFF000000 | (red << 16) | (green << 8) | blue);
            }
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(texture, "png", output);
        return new SoftwareTexture(output.toByteArray(), true);
    }

    private static float[] identity() {
        return new float[]{
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
        };
    }
}
