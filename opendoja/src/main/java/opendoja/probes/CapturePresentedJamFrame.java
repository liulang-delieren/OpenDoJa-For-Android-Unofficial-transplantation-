package opendoja.probes;

import opendoja.host.DoJaRuntime;
import opendoja.host.JamLauncher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CapturePresentedJamFrame {
    private CapturePresentedJamFrame() {
    }

    public static void main(String[] args) throws Exception {
        DemoLog.enableInfoLogging();
        if (args.length != 3) {
            throw new IllegalArgumentException("Usage: CapturePresentedJamFrame <jam-path> <delay-ms> <output-png>");
        }
        Path jamPath = Path.of(args[0]);
        long delayMillis = Long.parseLong(args[1]);
        Path output = Path.of(args[2]);
        DemoLog.info(CapturePresentedJamFrame.class, () -> "launching " + jamPath);
        Thread launchThread = new Thread(() -> {
            try {
                JamLauncher.launch(jamPath);
            } catch (Throwable throwable) {
                DemoLog.error(CapturePresentedJamFrame.class, "Launch failed", throwable);
            }
        }, "capture-presented-launch");
        launchThread.setDaemon(true);
        launchThread.start();
        Throwable failure = null;
        try {
            waitForRuntime();
            Thread.sleep(Math.max(0L, delayMillis));
            BufferedImage image = waitForPresentedFrame();
            if (image == null) {
                throw new IllegalStateException("No presented frame available");
            }
            if (output.getParent() != null) {
                Files.createDirectories(output.getParent());
            }
            ImageIO.write(image, "png", output.toFile());
            DemoLog.info(CapturePresentedJamFrame.class, () -> output.toAbsolutePath().toString());
            DemoLog.info(CapturePresentedJamFrame.class, "written");
        } catch (Throwable throwable) {
            failure = throwable;
            DemoLog.error(CapturePresentedJamFrame.class, "Capture failed", throwable);
        } finally {
            DoJaRuntime runtime = DoJaRuntime.current();
            if (runtime != null) {
                runtime.shutdown();
            }
            System.exit(failure == null ? 0 : 1);
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

    private static BufferedImage waitForPresentedFrame() throws Exception {
        long deadline = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < deadline) {
            BufferedImage image = capturePresentedFrame();
            if (image != null) {
                return image;
            }
            Thread.sleep(50L);
        }
        return capturePresentedFrame();
    }

    private static BufferedImage capturePresentedFrame() throws Exception {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime == null) {
            return null;
        }
        Field presentedFrameField = DoJaRuntime.class.getDeclaredField("presentedFrame");
        presentedFrameField.setAccessible(true);
        BufferedImage image = (BufferedImage) presentedFrameField.get(runtime);
        if (image == null) {
            return null;
        }
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.getGraphics().drawImage(image, 0, 0, null);
        return copy;
    }
}
