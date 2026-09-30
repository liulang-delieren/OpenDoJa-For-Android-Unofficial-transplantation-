package opendoja.probes;

import com.nttdocomo.opt.ui.j3d.Graphics3D;
import com.nttdocomo.opt.ui.j3d.PrimitiveArray;
import opendoja.g3d.Software3DContext;
import opendoja.g3d.SoftwareTexture;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Verifies opt point-sprite texture bounds against command-list glyph cells.
 */
public final class OptPointSpriteTextureBoundsProbe {
    private OptPointSpriteTextureBoundsProbe() {
    }

    public static void main(String[] args) {
        Software3DContext context = new Software3DContext();
        context.setOptScreenCenter(0, 0);
        context.setOptScreenScale(4096, 4096);
        context.setPrimitiveTextures(new SoftwareTexture[]{makeTexture()});
        context.setPrimitiveTexture(0);

        PrimitiveArray primitive = new PrimitiveArray(
                Graphics3D.PRIMITIVE_POINT_SPRITES,
                Graphics3D.POINT_SPRITE_PER_VERTEX,
                1);
        int[] vertices = primitive.getVertexArray();
        vertices[0] = 5;
        vertices[1] = 5;
        vertices[2] = 0;

        int[] sprite = primitive.getPointSpriteArray();
        sprite[0] = 10;
        sprite[1] = 10;
        sprite[2] = 0;
        sprite[3] = 0;
        sprite[4] = 0;
        sprite[5] = 11;
        sprite[6] = 11;
        sprite[7] = 0;

        BufferedImage target = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();
        try {
            context.renderOptPrimitives(graphics, target, 0, 0, 10, 10, primitive, 0);
        } finally {
            graphics.dispose();
        }

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                int expected = colorFor(x, y);
                int actual = target.getRGB(x, y);
                if (actual != expected) {
                    throw new AssertionError("point sprite sampled wrong texel at " + x + "," + y
                            + ": expected=0x" + Integer.toHexString(expected)
                            + " actual=0x" + Integer.toHexString(actual));
                }
            }
        }

        System.out.println("opt-point-sprite-texture-bounds-ok");
    }

    private static SoftwareTexture makeTexture() {
        int[] palette = new int[144];
        byte[] pixels = new byte[144];
        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 12; x++) {
                int index = y * 12 + x;
                palette[index] = colorFor(x, y);
                pixels[index] = (byte) index;
            }
        }
        return SoftwareTexture.fromIndexed(12, 12, palette, pixels, true);
    }

    private static int colorFor(int x, int y) {
        return 0xFF000000 | ((x * 16) << 16) | ((y * 16) << 8) | 0x55;
    }
}
