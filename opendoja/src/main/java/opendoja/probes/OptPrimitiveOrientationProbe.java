package opendoja.probes;

import com.nttdocomo.opt.ui.j3d.AffineTrans;
import com.nttdocomo.opt.ui.j3d.Graphics3D;
import com.nttdocomo.opt.ui.j3d.PrimitiveArray;
import com.nttdocomo.opt.ui.j3d._Opt3DInternalAccess;
import opendoja.g3d.Software3DContext;
import opendoja.g3d.SoftwareTexture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * Verifies that opt PrimitiveArray batches keep the expected texture orientation for the default
 * basis, the translation-only identity-basis path used by titles like Ridge Racers, and a quad
 * whose submitted geometry itself is vertically mirrored.
 */
public final class OptPrimitiveOrientationProbe {
    private OptPrimitiveOrientationProbe() {
    }

    public static void main(String[] args) throws Exception {
        DemoLog.enableInfoLogging();

        assertUpright("identity", identity(), false);
        assertUpright("identity-basis-translation", translatedIdentity(580f), false);
        assertUpright("dragon-quest-rotateX-4048", dragonQuestTerrainTransform(), false);
        assertOrientation("mirrored-geometry", identity(), true, 0xFF0000FF, 0xFFFF0000);
    }

    private static void assertUpright(String label, float[] transform, boolean mirroredGeometry) throws Exception {
        assertOrientation(label, transform, mirroredGeometry, 0xFFFF0000, 0xFF0000FF);
    }

    private static void assertOrientation(String label, float[] transform, boolean mirroredGeometry,
                                          int expectedTop, int expectedBottom) throws Exception {
        PrimitiveArray primitives = new PrimitiveArray(Graphics3D.PRIMITIVE_QUADS, Graphics3D.TEXTURE_COORD_PER_VERTEX, 1);
        int[] vertices = primitives.getVertexArray();
        int[] uvs = primitives.getTextureCoordArray();
        writeQuad(vertices, -64, 64, -48, 48, mirroredGeometry ? 0 : 512, mirroredGeometry);
        writeFullUvs(uvs);

        Software3DContext context = new Software3DContext();
        context.setOptScreenCenter(96, 72);
        context.setOptScreenScale(4096, 4096);
        context.setOptViewTransform(transform);
        context.setPrimitiveTextures(new SoftwareTexture[]{makeTexture()});
        context.setPrimitiveTexture(0);

        BufferedImage image = new BufferedImage(192, 144, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            context.renderOptPrimitivesRange(graphics, image, 0, 0, image.getWidth(), image.getHeight(), primitives, 0, 1, 0);
        } finally {
            graphics.dispose();
        }

        RenderBounds bounds = findRenderedBounds(image);
        int sampleX = (bounds.minX + bounds.maxX) >>> 1;
        int topY = bounds.minY + java.lang.Math.min(2, java.lang.Math.max(0, bounds.maxY - bounds.minY));
        int bottomY = bounds.maxY - java.lang.Math.min(2, java.lang.Math.max(0, bounds.maxY - bounds.minY));
        int topColor = image.getRGB(sampleX, topY);
        int bottomColor = image.getRGB(sampleX, bottomY);
        if (topColor != expectedTop || bottomColor != expectedBottom) {
            throw new IllegalStateException(String.format(
                    "Unexpected opt primitive orientation %s bounds=[%d,%d]-[%d,%d] top=%08x bottom=%08x",
                    label,
                    bounds.minX,
                    bounds.minY,
                    bounds.maxX,
                    bounds.maxY,
                    topColor,
                    bottomColor
            ));
        }
        DemoLog.info(OptPrimitiveOrientationProbe.class, String.format(
                "%s top=%08x bottom=%08x",
                label,
                topColor,
                bottomColor
        ));
    }

    private static void assertUpright(String label, boolean mirroredGeometry) throws Exception {
        assertUpright(label, identity(), mirroredGeometry);
    }

    private static void writeQuad(int[] vertices, int left, int right, int top, int bottom, int z, boolean mirroredGeometry) {
        int effectiveTop = mirroredGeometry ? bottom : top;
        int effectiveBottom = mirroredGeometry ? top : bottom;
        vertices[0] = left;
        vertices[1] = effectiveTop;
        vertices[2] = z;
        vertices[3] = right;
        vertices[4] = effectiveTop;
        vertices[5] = z;
        vertices[6] = right;
        vertices[7] = effectiveBottom;
        vertices[8] = z;
        vertices[9] = left;
        vertices[10] = effectiveBottom;
        vertices[11] = z;
    }

    private static void writeFullUvs(int[] uvs) {
        uvs[0] = 0;
        uvs[1] = 0;
        uvs[2] = 255;
        uvs[3] = 0;
        uvs[4] = 255;
        uvs[5] = 255;
        uvs[6] = 0;
        uvs[7] = 255;
    }

    private static SoftwareTexture makeTexture() throws Exception {
        BufferedImage texture = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < texture.getHeight(); y++) {
            int color = y < 128 ? 0xFFFF0000 : 0xFF0000FF;
            for (int x = 0; x < texture.getWidth(); x++) {
                texture.setRGB(x, y, color);
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

    private static float[] translatedIdentity(float z) {
        return new float[]{
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, z,
                0f, 0f, 0f, 1f
        };
    }

    private static float[] dragonQuestTerrainTransform() {
        AffineTrans transform = new AffineTrans();
        transform.setIdentity();
        transform.setRotateX(4048);
        transform.m23 = 2560;
        return _Opt3DInternalAccess.toFloatMatrix(transform);
    }

    private static RenderBounds findRenderedBounds(BufferedImage image) {
        int minX = image.getWidth();
        int minY = image.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) == 0) {
                    continue;
                }
                minX = java.lang.Math.min(minX, x);
                minY = java.lang.Math.min(minY, y);
                maxX = java.lang.Math.max(maxX, x);
                maxY = java.lang.Math.max(maxY, y);
            }
        }
        if (maxX < minX || maxY < minY) {
            throw new IllegalStateException("Opt primitive probe rendered no non-transparent pixels");
        }
        return new RenderBounds(minX, minY, maxX, maxY);
    }

    private record RenderBounds(int minX, int minY, int maxX, int maxY) {
    }

}
