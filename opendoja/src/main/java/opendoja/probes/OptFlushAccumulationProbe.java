package opendoja.probes;

import com.nttdocomo.opt.ui.j3d.Graphics3D;
import com.nttdocomo.opt.ui.j3d.PrimitiveArray;
import com.nttdocomo.ui.*;
import opendoja.host.DesktopLauncher;

import java.util.concurrent.TimeUnit;

/**
 * Verifies that repeated opt flushes inside one locked Canvas frame do not each add a display-sync
 * wait. Titles like Chase HQ 3D split one frame into multiple opt passes and depend on flush being
 * a pass boundary, not an extra frame throttle.
 */
public final class OptFlushAccumulationProbe {
    private OptFlushAccumulationProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ProbeApp app = (ProbeApp) DesktopLauncher.launch(ProbeApp.class);
        try {
            app.canvas.awaitCompletion();
            double singleFlushAvgMs = app.canvas.averageFrameMillis(1);
            double fourFlushAvgMs = app.canvas.averageFrameMillis(4);

            System.out.println("singleFlushAvgMs=" + formatMillis(singleFlushAvgMs)
                    + " fourFlushAvgMs=" + formatMillis(fourFlushAvgMs));

            if (fourFlushAvgMs > singleFlushAvgMs + 20.0) {
                throw new IllegalStateException("opt flush pacing accumulated per pass: single="
                        + singleFlushAvgMs + " four=" + fourFlushAvgMs);
            }
        } finally {
            app.canvas.stopLoop();
            app.terminate();
        }
    }

    private static String formatMillis(double millis) {
        return String.format("%.3f", millis);
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
        private static final int WARMUP_FRAMES = 2;
        private static final int MEASURED_FRAMES = 6;
        private static final int TOTAL_FRAMES_PER_MODE = WARMUP_FRAMES + MEASURED_FRAMES;

        private final Object stateMonitor = new Object();
        private final Thread loopThread = new Thread(this, "opt-flush-accumulation-probe");
        private final long[] singleFlushDurations = new long[MEASURED_FRAMES];
        private final long[] fourFlushDurations = new long[MEASURED_FRAMES];
        private final PrimitiveArray primitive = makePrimitive();
        private volatile boolean running = true;
        private boolean completed;

        void startLoop() {
            loopThread.setDaemon(true);
            loopThread.start();
        }

        void stopLoop() throws InterruptedException {
            running = false;
            loopThread.interrupt();
            loopThread.join(TimeUnit.SECONDS.toMillis(2L));
        }

        void awaitCompletion() throws InterruptedException {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5L);
            synchronized (stateMonitor) {
                while (!completed) {
                    long remainingNanos = deadline - System.nanoTime();
                    if (remainingNanos <= 0L) {
                        throw new IllegalStateException("Timed out waiting for opt flush probe completion");
                    }
                    TimeUnit.NANOSECONDS.timedWait(stateMonitor, remainingNanos);
                }
            }
        }

        double averageFrameMillis(int flushesPerFrame) {
            long[] samples = flushesPerFrame == 1 ? singleFlushDurations : fourFlushDurations;
            long total = 0L;
            for (long sample : samples) {
                total += sample;
            }
            return (total / (double) samples.length) / 1_000_000.0;
        }

        @Override
        public void run() {
            Graphics graphics = getGraphics();
            Graphics3D graphics3D = (Graphics3D) graphics;
            try {
                graphics3D.setScreenCenter(Display.getWidth() / 2, Display.getHeight() / 2);
                graphics3D.setScreenScale(4096, 4096);

                renderMode(graphics, graphics3D, 1, singleFlushDurations);
                renderMode(graphics, graphics3D, 4, fourFlushDurations);

                synchronized (stateMonitor) {
                    completed = true;
                    stateMonitor.notifyAll();
                }
            } finally {
                graphics.dispose();
            }
        }

        @Override
        public void paint(Graphics g) {
            return;
        }

        private void renderMode(Graphics graphics, Graphics3D graphics3D, int flushesPerFrame, long[] durations) {
            for (int frame = 0; running && frame < TOTAL_FRAMES_PER_MODE; frame++) {
                long startedAt = System.nanoTime();
                graphics.lock();
                try {
                    graphics.clearRect(0, 0, Display.getWidth(), Display.getHeight());
                    for (int pass = 0; pass < flushesPerFrame; pass++) {
                        graphics3D.renderPrimitives(primitive, 0);
                        graphics3D.flush();
                    }
                } finally {
                    graphics.unlock(true);
                }
                if (frame >= WARMUP_FRAMES) {
                    durations[frame - WARMUP_FRAMES] = System.nanoTime() - startedAt;
                }
            }
        }

        private static PrimitiveArray makePrimitive() {
            PrimitiveArray primitive = new PrimitiveArray(Graphics3D.PRIMITIVE_QUADS, Graphics3D.COLOR_PER_COMMAND, 1);
            int[] vertices = primitive.getVertexArray();
            writeVertex(vertices, 0, -24, 24, 0);
            writeVertex(vertices, 1, 24, 24, 0);
            writeVertex(vertices, 2, 24, -24, 0);
            writeVertex(vertices, 3, -24, -24, 0);
            primitive.getColorArray()[0] = 0xFFFFFFFF;
            return primitive;
        }

        private static void writeVertex(int[] vertices, int index, int x, int y, int z) {
            int offset = index * 3;
            vertices[offset] = x;
            vertices[offset + 1] = y;
            vertices[offset + 2] = z;
        }
    }
}
