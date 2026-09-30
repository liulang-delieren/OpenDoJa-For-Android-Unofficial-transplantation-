package opendoja.probes;

import opendoja.g3d.MascotFigure;
import opendoja.g3d.MbacModel;
import opendoja.g3d.Software3DContext;
import opendoja.g3d.SoftwareTexture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public final class UiFigureBlendDepthProbe {
    private static final int WIDTH = 64;
    private static final int HEIGHT = 64;
    private static final int UI_FIGURE_QUAD_HALF_EXTENT = 1280;
    private static final int MASCOT_BLEND_ADD = 0x04;

    private UiFigureBlendDepthProbe() {
    }

    public static void main(String[] args) {
        DemoLog.enableInfoLogging();

        int blendedPixel = renderBlendedOverlapPixel();
        if ((blendedPixel & 0x00FFFFFF) != 0x00FFFFFF) {
            throw new IllegalStateException(String.format(
                    "UI blended figure still writes depth into later light pass, pixel=%08x",
                    blendedPixel));
        }

        int opaquePixel = renderOpaqueOverlapPixel();
        if ((opaquePixel & 0x00FFFFFF) != 0x00FFFFFF) {
            throw new IllegalStateException(String.format(
                    "UI opaque figure no longer writes depth, pixel=%08x",
                    opaquePixel));
        }

        DemoLog.info(UiFigureBlendDepthProbe.class, String.format(
                "blended=%08x opaque=%08x",
                blendedPixel,
                opaquePixel));
    }

    private static int renderBlendedOverlapPixel() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(0xFF000000, true));
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
            Software3DContext context = makeContext();
            context.renderUiFigure(graphics, image, 0, 0, WIDTH, HEIGHT, maskedAdditiveQuad(), identityTransform(0f), 0, 1f);
            context.renderUiFigure(graphics, image, 0, 0, WIDTH, HEIGHT, solidQuad(0xFFFFFFFF, MASCOT_BLEND_ADD), identityTransform(8f), 0, 1f);
        } finally {
            graphics.dispose();
        }
        // Sample outside the front quad's visible white center. If blended figures still claim
        // depth there through their black backdrop texels, the later light pass disappears.
        return image.getRGB(48, HEIGHT / 2);
    }

    private static int renderOpaqueOverlapPixel() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(0xFF000000, true));
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
            Software3DContext context = makeContext();
            context.renderUiFigure(graphics, image, 0, 0, WIDTH, HEIGHT, solidQuad(0xFFFFFFFF, 0), identityTransform(0f), 0, 1f);
            context.renderUiFigure(graphics, image, 0, 0, WIDTH, HEIGHT, solidQuad(0xFFFF0000, 0), identityTransform(8f), 0, 1f);
        } finally {
            graphics.dispose();
        }
        return image.getRGB(WIDTH / 2, HEIGHT / 2);
    }

    private static Software3DContext makeContext() {
        Software3DContext context = new Software3DContext();
        context.setUiParallelView(WIDTH, HEIGHT);
        float[] depth = new float[WIDTH * HEIGHT];
        Arrays.fill(depth, Float.NEGATIVE_INFINITY);
        context.setFrameDepthBuffer(depth);
        return context;
    }

    private static MascotFigure maskedAdditiveQuad() {
        int[] palette = {
                0xFF000000,
                0xFFFFFFFF
        };
        byte[] pixels = new byte[16 * 16];
        for (int y = 4; y < 12; y++) {
            for (int x = 4; x < 12; x++) {
                pixels[y * 16 + x] = 1;
            }
        }
        return texturedQuad(SoftwareTexture.fromIndexed(16, 16, palette, pixels, true), MASCOT_BLEND_ADD);
    }

    private static MascotFigure solidQuad(int argb, int blendMode) {
        return texturedQuad(SoftwareTexture.fromIndexed(16, 16, new int[]{argb}, new byte[16 * 16], true), blendMode);
    }

    private static MascotFigure texturedQuad(SoftwareTexture texture, int blendMode) {
        int[] vertices = {
                -UI_FIGURE_QUAD_HALF_EXTENT, UI_FIGURE_QUAD_HALF_EXTENT, 0,
                UI_FIGURE_QUAD_HALF_EXTENT, UI_FIGURE_QUAD_HALF_EXTENT, 0,
                -UI_FIGURE_QUAD_HALF_EXTENT, -UI_FIGURE_QUAD_HALF_EXTENT, 0,
                UI_FIGURE_QUAD_HALF_EXTENT, -UI_FIGURE_QUAD_HALF_EXTENT, 0
        };
        MbacModel.Polygon polygon = new MbacModel.Polygon(
                new int[]{0, 1, 2, 3},
                new float[]{0f, 0f, 15f, 0f, 0f, 15f, 15f, 15f},
                0xFFFFFFFF,
                0,
                0,
                blendMode,
                true,
                false);
        MascotFigure figure = new MascotFigure(new MbacModel(vertices, vertices.clone(), new MbacModel.Polygon[]{polygon}, 1, new MbacModel.Bone[0]));
        figure.setTexture(texture);
        return figure;
    }

    private static float[] identityTransform(float z) {
        return new float[]{
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, z
        };
    }
}
