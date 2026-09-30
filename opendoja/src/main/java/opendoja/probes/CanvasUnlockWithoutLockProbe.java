package opendoja.probes;

import com.nttdocomo.ui.*;
import opendoja.host.DesktopLauncher;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Verifies that `Graphics.unlock(true)` is a no-op before the first `lock()`.
 */
public final class CanvasUnlockWithoutLockProbe {
    private CanvasUnlockWithoutLockProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ProbeApp app = (ProbeApp) DesktopLauncher.launch(ProbeApp.class);
        try {
            if (!app.canvas.awaitCompletion()) {
                throw new IllegalStateException("Timed out waiting for unlock-without-lock probe");
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
            canvas.startLoop();
        }
    }

    static final class ProbeCanvas extends Canvas implements Runnable {
        private final CountDownLatch completed = new CountDownLatch(1);
        private final Thread loopThread = new Thread(this, "canvas-unlock-without-lock-probe");
        private volatile boolean running = true;
        private volatile Throwable failure;

        void startLoop() {
            loopThread.setDaemon(true);
            loopThread.start();
        }

        boolean awaitCompletion() throws InterruptedException {
            if (!completed.await(5L, TimeUnit.SECONDS)) {
                return false;
            }
            if (failure != null) {
                throw new IllegalStateException("Unlock-without-lock probe failed", failure);
            }
            return true;
        }

        void stopLoop() throws InterruptedException {
            running = false;
            loopThread.interrupt();
            loopThread.join(TimeUnit.SECONDS.toMillis(2L));
        }

        @Override
        public void run() {
            Graphics g = getGraphics();
            try {
                g.unlock(true);
                if (!running) {
                    return;
                }
                g.lock();
                try {
                    g.clearRect(0, 0, Display.getWidth(), Display.getHeight());
                    g.drawString("unlock-first", 0, 0);
                } finally {
                    g.unlock(true);
                }
            } catch (Throwable throwable) {
                failure = throwable;
            } finally {
                g.dispose();
                completed.countDown();
            }
        }

        @Override
        public void paint(Graphics g) {
            return;
        }
    }
}
