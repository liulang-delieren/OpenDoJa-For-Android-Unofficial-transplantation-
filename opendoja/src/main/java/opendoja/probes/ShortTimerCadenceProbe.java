package opendoja.probes;

import com.nttdocomo.ui.*;
import opendoja.host.DesktopLauncher;
import opendoja.host.DoJaRuntime;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class ShortTimerCadenceProbe {
    private ShortTimerCadenceProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ProbeApp app = (ProbeApp) DesktopLauncher.launch(ProbeApp.class);
        try {
            long[] intervals = app.canvas.awaitIntervals();
            for (int i = 0; i < intervals.length; i++) {
                long interval = intervals[i];
                if (interval < 35L || interval > 140L) {
                    throw new IllegalStateException("Unexpected short-timer interval[" + i + "]=" + interval + "ms");
                }
            }
            System.out.println("ShortTimer cadence probe OK");
        } finally {
            DoJaRuntime runtime = DoJaRuntime.current();
            if (runtime != null) {
                runtime.shutdown();
            }
        }
    }

    public static final class ProbeApp extends IApplication {
        final ProbeCanvas canvas = new ProbeCanvas();

        @Override
        public void start() {
            Display.setCurrent((Frame) canvas);
            canvas.startTimer();
        }
    }

    static final class ProbeCanvas extends Canvas {
        private static final int SAMPLE_COUNT = 4;
        private final CountDownLatch latch = new CountDownLatch(SAMPLE_COUNT + 1);
        private final long[] stamps = new long[SAMPLE_COUNT + 1];
        private volatile int stampCount;
        private ShortTimer timer;

        @Override
        public void paint(Graphics g) {
        }

        void startTimer() {
            timer = ShortTimer.getShortTimer(this, 7, 50, true);
            timer.start();
        }

        @Override
        public void processEvent(int type, int param) {
            if (type != Display.TIMER_EXPIRED_EVENT || param != 7) {
                return;
            }
            int index = stampCount;
            if (index >= stamps.length) {
                return;
            }
            stamps[index] = System.nanoTime();
            stampCount = index + 1;
            latch.countDown();
            if (stampCount == stamps.length && timer != null) {
                timer.stop();
                timer.dispose();
            }
        }

        long[] awaitIntervals() throws InterruptedException {
            if (!latch.await(3L, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for short-timer samples");
            }
            long[] intervals = new long[SAMPLE_COUNT];
            for (int i = 1; i < stamps.length; i++) {
                intervals[i - 1] = TimeUnit.NANOSECONDS.toMillis(stamps[i] - stamps[i - 1]);
            }
            return intervals;
        }
    }
}
