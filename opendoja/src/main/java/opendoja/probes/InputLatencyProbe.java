package opendoja.probes;

import com.nttdocomo.ui.*;
import opendoja.host.DesktopLauncher;
import opendoja.host.DoJaRuntime;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class InputLatencyProbe {
    private InputLatencyProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        LatencyApp app = (LatencyApp) DesktopLauncher.launch(LatencyApp.class);
        try {
            app.canvas.reset();
            long pressStart = System.nanoTime();
            DoJaRuntime.current().dispatchSyntheticKey(Display.KEY_RIGHT, Display.KEY_PRESSED_EVENT);
            long pressLatency = app.canvas.awaitPress(Display.KEY_RIGHT) - pressStart;

            long releaseStart = System.nanoTime();
            DoJaRuntime.current().dispatchSyntheticKey(Display.KEY_RIGHT, Display.KEY_RELEASED_EVENT);
            long releaseLatency = app.canvas.awaitRelease(Display.KEY_RIGHT) - releaseStart;

            long selectPressStart = System.nanoTime();
            DoJaRuntime.current().dispatchSyntheticKey(Display.KEY_SELECT, Display.KEY_PRESSED_EVENT);
            long selectPressLatency = app.canvas.awaitPress(Display.KEY_SELECT) - selectPressStart;

            long selectReleaseStart = System.nanoTime();
            DoJaRuntime.current().dispatchSyntheticKey(Display.KEY_SELECT, Display.KEY_RELEASED_EVENT);
            long selectReleaseLatency = app.canvas.awaitRelease(Display.KEY_SELECT) - selectReleaseStart;

            long selectStateRelease = app.canvas.measureSelectStateReleaseNanos();
            boolean fastTapObserved = app.canvas.observeFastSelectTap(10L, 60L, 250L);

            System.out.println(
                    "rightPressMs=" + nanosToMillis(pressLatency)
                            + " rightReleaseMs=" + nanosToMillis(releaseLatency)
                            + " selectPressMs=" + nanosToMillis(selectPressLatency)
                            + " selectReleaseMs=" + nanosToMillis(selectReleaseLatency)
                            + " selectStateReleaseMs=" + nanosToMillis(selectStateRelease)
                            + " fastTapObserved=" + fastTapObserved);
            if (!fastTapObserved) {
                throw new IllegalStateException("Fast select tap was not visible to a 60 ms polling loop");
            }
        } finally {
            app.terminate();
        }
    }

    private static String nanosToMillis(long nanos) {
        return String.format("%.3f", nanos / 1_000_000.0);
    }

    public static final class LatencyApp extends IApplication {
        final ProbeCanvas canvas = new ProbeCanvas();

        @Override
        public void start() {
            Display.setCurrent((Frame) canvas);
        }
    }

    static final class ProbeCanvas extends Canvas {
        private volatile int lastPressedKey = Integer.MIN_VALUE;
        private volatile int lastReleasedKey = Integer.MIN_VALUE;
        private volatile long lastPressedAt;
        private volatile long lastReleasedAt;
        private CountDownLatch pressLatch = new CountDownLatch(1);
        private CountDownLatch releaseLatch = new CountDownLatch(1);

        @Override
        public void paint(Graphics g) {
            g.lock();
            g.clearRect(0, 0, Display.getWidth(), Display.getHeight());
            g.unlock(true);
        }

        @Override
        public void processEvent(int type, int param) {
            long now = System.nanoTime();
            if (type == Display.KEY_PRESSED_EVENT) {
                lastPressedKey = param;
                lastPressedAt = now;
                pressLatch.countDown();
            } else if (type == Display.KEY_RELEASED_EVENT) {
                lastReleasedKey = param;
                lastReleasedAt = now;
                releaseLatch.countDown();
            }
        }

        void reset() {
            pressLatch = new CountDownLatch(1);
            releaseLatch = new CountDownLatch(1);
            lastPressedKey = Integer.MIN_VALUE;
            lastReleasedKey = Integer.MIN_VALUE;
            lastPressedAt = 0L;
            lastReleasedAt = 0L;
        }

        long awaitPress(int key) throws InterruptedException {
            if (!pressLatch.await(2L, TimeUnit.SECONDS) || lastPressedKey != key) {
                throw new IllegalStateException("Did not receive key press for " + key);
            }
            pressLatch = new CountDownLatch(1);
            return lastPressedAt;
        }

        long awaitRelease(int key) throws InterruptedException {
            if (!releaseLatch.await(2L, TimeUnit.SECONDS) || lastReleasedKey != key) {
                throw new IllegalStateException("Did not receive key release for " + key);
            }
            releaseLatch = new CountDownLatch(1);
            return lastReleasedAt;
        }

        long measureSelectStateReleaseNanos() throws InterruptedException {
            long releaseStart = System.nanoTime();
            while ((getKeypadState() & (1 << Display.KEY_SELECT)) != 0) {
                if (System.nanoTime() - releaseStart > TimeUnit.SECONDS.toNanos(2L)) {
                    throw new IllegalStateException("Select state stayed latched for too long");
                }
                Thread.sleep(1L);
            }
            return System.nanoTime() - releaseStart;
        }

        boolean observeFastSelectTap(long pressMillis, long pollIntervalMillis, long windowMillis) throws InterruptedException {
            CountDownLatch observed = new CountDownLatch(1);
            Thread poller = new Thread(() -> {
                long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(windowMillis);
                while (System.nanoTime() < deadline) {
                    if ((getKeypadState() & (1 << Display.KEY_SELECT)) != 0) {
                        observed.countDown();
                        return;
                    }
                    try {
                        Thread.sleep(pollIntervalMillis);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }, "input-latency-probe-poller");
            poller.setDaemon(true);
            poller.start();
            DoJaRuntime.current().dispatchSyntheticKey(Display.KEY_SELECT, Display.KEY_PRESSED_EVENT);
            Thread.sleep(pressMillis);
            DoJaRuntime.current().dispatchSyntheticKey(Display.KEY_SELECT, Display.KEY_RELEASED_EVENT);
            boolean seen = observed.await(windowMillis + 100L, TimeUnit.MILLISECONDS);
            poller.join(TimeUnit.MILLISECONDS.toMillis(windowMillis) + 100L);
            return seen;
        }
    }
}
