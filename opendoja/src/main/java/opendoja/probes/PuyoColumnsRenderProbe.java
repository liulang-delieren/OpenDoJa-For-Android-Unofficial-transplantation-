package opendoja.probes;

import com.nttdocomo.ui.Canvas;
import com.nttdocomo.ui.Frame;
import opendoja.host.DoJaRuntime;
import opendoja.host.JamLauncher;

import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public final class PuyoColumnsRenderProbe {
    private PuyoColumnsRenderProbe() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: PuyoColumnsRenderProbe <jam-path> <delay-ms>");
        }
        Path jamPath = Path.of(args[0]);
        long delayMillis = Long.parseLong(args[1]);
        AtomicReference<Throwable> launchFailure = new AtomicReference<>();
        Thread launchThread = new Thread(() -> {
            try {
                JamLauncher.launch(jamPath);
            } catch (Throwable throwable) {
                launchFailure.set(throwable);
            }
        }, "puyo-columns-render-launch");
        launchThread.setDaemon(true);
        launchThread.start();

        try {
            waitForRuntime();
            Thread.sleep(Math.max(0L, delayMillis));
            Throwable failure = launchFailure.get();
            if (failure != null) {
                throw new IllegalStateException("Launch failed", failure);
            }
            BufferedImage image = currentCanvasImage();
            if (image == null) {
                throw new IllegalStateException("No current canvas image available");
            }
            PixelStats stats = pixelStats(image);
            System.out.println("puyo-columns-render " + jamPath
                    + " size=" + image.getWidth() + "x" + image.getHeight()
                    + " opaque=" + stats.opaquePixels
                    + " nonBlackOpaque=" + stats.nonBlackOpaquePixels
                    + " colorsAtLeast=" + stats.colorsAtLeast);
            if (stats.opaquePixels == 0 || stats.nonBlackOpaquePixels == 0 || stats.colorsAtLeast < 2) {
                throw new IllegalStateException("Puyo/Columns frame is still blank");
            }
        } finally {
            DoJaRuntime runtime = DoJaRuntime.current();
            if (runtime != null) {
                runtime.shutdown();
            }
        }
    }

    private static void waitForRuntime() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000L;
        while (DoJaRuntime.current() == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(20L);
        }
        if (DoJaRuntime.current() == null) {
            throw new IllegalStateException("DoJa runtime did not initialize");
        }
    }

    private static BufferedImage currentCanvasImage() throws Exception {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime == null) {
            return null;
        }
        Frame frame = runtime.getCurrentFrame();
        if (!(frame instanceof Canvas canvas)) {
            throw new IllegalStateException("Current frame is " + (frame == null ? "null" : frame.getClass().getName()));
        }
        Method surfaceMethod = Canvas.class.getDeclaredMethod("surface");
        surfaceMethod.setAccessible(true);
        Object surface = surfaceMethod.invoke(canvas);
        if (surface == null) {
            return null;
        }
        Method imageMethod = surface.getClass().getDeclaredMethod("image");
        imageMethod.setAccessible(true);
        return (BufferedImage) imageMethod.invoke(surface);
    }

    private static PixelStats pixelStats(BufferedImage image) {
        int first = image.getRGB(0, 0);
        int colorsAtLeast = 1;
        int opaque = 0;
        int nonBlackOpaque = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if (argb != first) {
                    colorsAtLeast = 2;
                }
                if ((argb >>> 24) != 0) {
                    opaque++;
                    if ((argb & 0x00FFFFFF) != 0) {
                        nonBlackOpaque++;
                    }
                }
            }
        }
        return new PixelStats(colorsAtLeast, opaque, nonBlackOpaque);
    }

    private record PixelStats(int colorsAtLeast, int opaquePixels, int nonBlackOpaquePixels) {
    }
}
