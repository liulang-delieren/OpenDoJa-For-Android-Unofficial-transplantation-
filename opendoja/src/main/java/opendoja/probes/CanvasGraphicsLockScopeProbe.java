package opendoja.probes;

import com.nttdocomo.ui.Canvas;
import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Frame;
import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.IApplication;
import opendoja.host.DesktopSurface;
import opendoja.host.DesktopLauncher;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Verifies that an unbalanced lock on one Canvas graphics object does not block
 * a later graphics object for the same canvas.
 */
public final class CanvasGraphicsLockScopeProbe {
    private CanvasGraphicsLockScopeProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ProbeApp app = (ProbeApp) DesktopLauncher.launch(ProbeApp.class);
        try {
            if (!app.canvas.awaitCompletion()) {
                throw new IllegalStateException("Timed out waiting for second canvas graphics lock");
            }
        } finally {
            app.canvas.stopLoop();
            app.terminate();
        }
    }

    public static final class ProbeApp extends IApplication {
        final ProbeCanvas canvas = new ProbeCanvas();

        @Override
        public void start() {
            Display.setCurrent((Frame) canvas);
            canvas.lockStartupGraphics();
            canvas.startLoop();
        }
    }

    static final class ProbeCanvas extends Canvas implements Runnable {
        private final CountDownLatch completed = new CountDownLatch(1);
        private final Thread loopThread = new Thread(this, "canvas-graphics-lock-scope-probe");
        private volatile boolean running = true;
        private volatile Throwable failure;
        private Graphics startupGraphics;

        void lockStartupGraphics() {
            startupGraphics = getGraphics();
            startupGraphics.lock();
        }

        void startLoop() {
            loopThread.setDaemon(true);
            loopThread.start();
        }

        boolean awaitCompletion() throws InterruptedException {
            if (!completed.await(5L, TimeUnit.SECONDS)) {
                return false;
            }
            if (failure != null) {
                throw new IllegalStateException("Canvas graphics lock scope probe failed", failure);
            }
            return true;
        }

        void stopLoop() throws InterruptedException {
            running = false;
            loopThread.interrupt();
            loopThread.join(TimeUnit.SECONDS.toMillis(2L));
            if (startupGraphics != null) {
                startupGraphics.dispose();
            }
        }

        @Override
        public void run() {
            Graphics graphics = getGraphics();
            try {
                if (!running) {
                    return;
                }
                AtomicInteger flushCount = installFlushCounter();
                Thread.sleep(100L);
                int baselineFlushCount = flushCount.get();
                graphics.setColor(Graphics.getColorOfRGB(255, 0, 0));
                graphics.fillRect(0, 0, 8, 8);
                if (flushCount.get() != baselineFlushCount) {
                    throw new IllegalStateException("Outside-lock draw flushed while another graphics object was locked");
                }
                graphics.lock();
                try {
                    graphics.setColor(Graphics.getColorOfRGB(255, 255, 255));
                    graphics.fillRect(0, 0, 8, 8);
                } finally {
                    graphics.unlock(true);
                }
                if (flushCount.get() != baselineFlushCount + 1) {
                    throw new IllegalStateException("Explicit unlock did not flush exactly one frame: "
                            + (flushCount.get() - baselineFlushCount));
                }
            } catch (Throwable throwable) {
                failure = throwable;
            } finally {
                graphics.dispose();
                completed.countDown();
            }
        }

        @Override
        public void paint(Graphics graphics) {
            return;
        }

        private AtomicInteger installFlushCounter() throws Exception {
            Method surfaceMethod = Canvas.class.getDeclaredMethod("surface");
            surfaceMethod.setAccessible(true);
            DesktopSurface surface = (DesktopSurface) surfaceMethod.invoke(this);
            AtomicInteger flushCount = new AtomicInteger();
            surface.setRepaintHook((frame, frameBoundary) -> flushCount.incrementAndGet());
            return flushCount;
        }
    }
}
