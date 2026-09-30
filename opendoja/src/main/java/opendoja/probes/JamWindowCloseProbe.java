package opendoja.probes;

import com.nttdocomo.ui.Display;
import opendoja.host.DoJaRuntime;
import opendoja.host.JamLauncher;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.nio.file.Path;

/**
 * Launches a JAM, optionally sends a timed key sequence, then dispatches a host
 * window close event and verifies that the runtime shuts down.
 */
public final class JamWindowCloseProbe {
    private JamWindowCloseProbe() {
    }

    public static void main(String[] args) throws Exception {
        DemoLog.enableInfoLogging();
        if (args.length < 2 || ((args.length - 2) % 2) != 0) {
            throw new IllegalArgumentException(
                    "Usage: JamWindowCloseProbe <jam-path> <initial-delay-ms> [<key> <after-ms> ...]");
        }

        Path jamPath = Path.of(args[0]);
        long initialDelay = Long.parseLong(args[1]);

        Thread launchThread = new Thread(() -> {
            try {
                JamLauncher.launch(jamPath);
            } catch (Throwable throwable) {
                DemoLog.error(JamWindowCloseProbe.class, "Launch failed", throwable);
            }
        }, "jam-window-close-probe-launch");
        launchThread.setDaemon(true);
        launchThread.start();

        Throwable failure = null;
        try {
            waitForRuntime();
            Thread.sleep(Math.max(0L, initialDelay));
            for (int i = 2; i < args.length; i += 2) {
                dispatchKey(parseKey(args[i]));
                Thread.sleep(Math.max(0L, Long.parseLong(args[i + 1])));
            }
            dispatchWindowClose();
            waitForShutdown();
            DemoLog.info(JamWindowCloseProbe.class, "window close delivered");
        } catch (Throwable throwable) {
            failure = throwable;
            DemoLog.error(JamWindowCloseProbe.class, "Probe failed", throwable);
        } finally {
            DoJaRuntime runtime = DoJaRuntime.current();
            if (runtime != null) {
                runtime.shutdown();
            }
            System.exit(failure == null ? 0 : 1);
        }
    }

    private static int parseKey(String keyName) {
        return switch (keyName.toUpperCase()) {
            case "SELECT", "ENTER" -> Display.KEY_SELECT;
            case "LEFT" -> Display.KEY_LEFT;
            case "RIGHT" -> Display.KEY_RIGHT;
            case "UP" -> Display.KEY_UP;
            case "DOWN" -> Display.KEY_DOWN;
            case "SOFT1" -> Display.KEY_SOFT1;
            case "SOFT2" -> Display.KEY_SOFT2;
            default -> throw new IllegalArgumentException("Unsupported key: " + keyName);
        };
    }

    private static void waitForRuntime() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10_000L;
        while (DoJaRuntime.current() == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(20L);
        }
        if (DoJaRuntime.current() == null) {
            throw new IllegalStateException("DoJa runtime did not initialize");
        }
    }

    private static void dispatchKey(int key) throws InterruptedException {
        DoJaRuntime runtime = requireRuntime();
        runtime.dispatchSyntheticKey(key, Display.KEY_PRESSED_EVENT);
        Thread.sleep(200L);
        runtime.dispatchSyntheticKey(key, Display.KEY_RELEASED_EVENT);
    }

    private static void dispatchWindowClose() throws Exception {
        DoJaRuntime runtime = requireRuntime();
        Field frameWindowField = DoJaRuntime.class.getDeclaredField("frameWindow");
        frameWindowField.setAccessible(true);
        Object frameWindow = frameWindowField.get(runtime);
        if (!(frameWindow instanceof java.awt.Window window)) {
            throw new IllegalStateException("No host window available");
        }
        SwingUtilities.invokeAndWait(() ->
                window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING)));
    }

    private static DoJaRuntime requireRuntime() {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime == null) {
            throw new IllegalStateException("DoJa runtime exited before probe completed");
        }
        return runtime;
    }

    private static void waitForShutdown() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5_000L;
        while (DoJaRuntime.current() != null && System.currentTimeMillis() < deadline) {
            Thread.sleep(20L);
        }
        if (DoJaRuntime.current() != null) {
            throw new IllegalStateException("DoJa runtime did not shut down after window close");
        }
    }
}
