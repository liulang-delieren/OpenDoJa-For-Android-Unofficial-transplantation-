package opendoja.probes;

import com.nttdocomo.ui.Canvas;
import com.nttdocomo.ui.Frame;
import com.nttdocomo.ui.IApplication;
import opendoja.host.DoJaRuntime;
import opendoja.host.JamLauncher;

import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;

public final class BearPuzzleTitleProbe {
    private BearPuzzleTitleProbe() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: BearPuzzleTitleProbe <jam-path>");
        }
        System.setProperty("java.awt.headless", "true");
        Path jamPath = Path.of(args[0]);
        IApplication app = JamLauncher.launch(jamPath, false);
        if (app == null) {
            throw new IllegalStateException("Jam launch returned null application");
        }
        try {
            waitForTitleImages();
            BufferedImage image = captureCurrentCanvas();
            if (image == null) {
                throw new IllegalStateException("No current canvas image available");
            }
            if (countNonBlackPixels(image) == 0) {
                throw new IllegalStateException("Bear Puzzle title canvas is still all black");
            }
            System.out.println("BearPuzzleTitleProbe OK");
        } finally {
            DoJaRuntime runtime = DoJaRuntime.current();
            if (runtime != null) {
                runtime.shutdown();
            }
        }
    }

    private static void waitForTitleImages() throws Exception {
        long deadline = System.currentTimeMillis() + 8000L;
        while (System.currentTimeMillis() < deadline) {
            if (titleImagesReady()) {
                return;
            }
            Thread.sleep(50L);
        }
        throw new IllegalStateException("Bear Puzzle title images never became ready");
    }

    private static boolean titleImagesReady() throws Exception {
        Class<?> sysMainType = Class.forName("SysMain");
        Field prgModeField = sysMainType.getDeclaredField("PrgMode");
        prgModeField.setAccessible(true);
        Field stepModeField = sysMainType.getDeclaredField("StepMode");
        stepModeField.setAccessible(true);
        if (((Number) prgModeField.get(null)).intValue() != 1 || ((Number) stepModeField.get(null)).intValue() < 1) {
            return false;
        }

        Class<?> mainType = Class.forName("Main");
        Field imgField = mainType.getDeclaredField("Img");
        imgField.setAccessible(true);
        Object imgs = imgField.get(null);
        return imgs != null
                && Array.get(imgs, 0) != null
                && Array.get(imgs, 5) != null;
    }

    private static BufferedImage captureCurrentCanvas() throws Exception {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime == null) {
            return null;
        }
        Frame frame = runtime.getCurrentFrame();
        if (!(frame instanceof Canvas canvas)) {
            return null;
        }
        Method surfaceMethod = Canvas.class.getDeclaredMethod("surface");
        surfaceMethod.setAccessible(true);
        Object surface = surfaceMethod.invoke(canvas);
        if (surface == null) {
            return null;
        }
        Method imageMethod = surface.getClass().getDeclaredMethod("image");
        imageMethod.setAccessible(true);
        BufferedImage image = (BufferedImage) imageMethod.invoke(surface);
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.getGraphics().drawImage(image, 0, 0, null);
        return copy;
    }

    private static int countNonBlackPixels(BufferedImage image) {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                if ((pixel & 0x00FFFFFF) != 0) {
                    count++;
                }
            }
        }
        return count;
    }
}
