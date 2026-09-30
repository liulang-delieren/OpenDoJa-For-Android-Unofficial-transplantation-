package opendoja.probes;

import com.nttdocomo.opt.ui.j3d.Graphics3D;
import com.nttdocomo.opt.ui.j3d.PrimitiveArray;
import opendoja.g3d.Software3DContext;
import opendoja.g3d.SoftwareTexture;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Verifies that adjacent triangles do not leave ownership gaps on shared edges when
 * the source mesh contains mixed winding, as seen in MBAC circuit terrain chunks.
 */
public final class TriangleCoverageProbe {
    private TriangleCoverageProbe() {
    }

    public static void main(String[] args) {
        DemoLog.enableInfoLogging();

        BufferedImage coloredImage = renderMixedWindingRectangle(false);
        int holes = countTransparentInteriorPixels(coloredImage, 10, 10, 50, 50);
        if (holes != 0) {
            throw new IllegalStateException("Colored mixed-winding rectangle has " + holes + " transparent interior pixels");
        }

        BufferedImage texturedImage = renderMixedWindingRectangle(true);
        holes = countTransparentInteriorPixels(texturedImage, 10, 10, 50, 50);
        if (holes != 0) {
            throw new IllegalStateException("Textured mixed-winding rectangle has " + holes + " transparent interior pixels");
        }

        DemoLog.info(TriangleCoverageProbe.class, "mixed-winding rectangle coverage is solid");
    }

    private static BufferedImage renderMixedWindingRectangle(boolean textured) {
        PrimitiveArray primitives = new PrimitiveArray(
                Graphics3D.PRIMITIVE_TRIANGLES,
                textured ? Graphics3D.TEXTURE_COORD_PER_VERTEX : Graphics3D.COLOR_PER_FACE,
                2
        );
        int[] vertices = primitives.getVertexArray();

        // The two triangles cover a 40x40 rectangle and intentionally submit the
        // shared diagonal in the same direction. Handsets render this as one
        // continuous double-sided surface; the raster edge rule must still give
        // the shared edge to exactly one triangle.
        writeVertex(vertices, 0, 50, 50, 0);
        writeVertex(vertices, 1, 10, 10, 0);
        writeVertex(vertices, 2, 50, 10, 0);
        writeVertex(vertices, 3, 50, 50, 0);
        writeVertex(vertices, 4, 10, 10, 0);
        writeVertex(vertices, 5, 10, 50, 0);

        if (textured) {
            fillZeroTextureCoords(primitives.getTextureCoordArray());
        } else {
            int[] colors = primitives.getColorArray();
            colors[0] = 0x00FF00;
            colors[1] = 0x00FF00;
        }

        Software3DContext context = new Software3DContext();
        context.setOptScreenCenter(0, 0);
        context.setOptScreenView(64, 64);
        if (textured) {
            context.setPrimitiveTextures(new SoftwareTexture[]{
                    SoftwareTexture.fromIndexed(1, 1, new int[]{0xFF00FF00}, new byte[]{0}, true)
            });
            context.setPrimitiveTexture(0);
        }

        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            context.renderOptPrimitivesRange(
                    graphics,
                    image,
                    0,
                    0,
                    image.getWidth(),
                    image.getHeight(),
                    primitives,
                    0,
                    primitives.size(),
                    0
            );
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static void writeVertex(int[] vertices, int index, int x, int y, int z) {
        int offset = index * 3;
        vertices[offset] = x;
        vertices[offset + 1] = y;
        vertices[offset + 2] = z;
    }

    private static void fillZeroTextureCoords(int[] uvs) {
        for (int i = 0; i < uvs.length; i++) {
            uvs[i] = 0;
        }
    }

    private static int countTransparentInteriorPixels(BufferedImage image, int left, int top, int right, int bottom) {
        int holes = 0;
        for (int y = top + 1; y < bottom - 1; y++) {
            for (int x = left + 1; x < right - 1; x++) {
                if ((image.getRGB(x, y) >>> 24) == 0) {
                    holes++;
                }
            }
        }
        return holes;
    }
}
