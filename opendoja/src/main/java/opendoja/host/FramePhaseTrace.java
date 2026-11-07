package opendoja.host;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Temporary frame-phase timing instrumentation for performance work.
 * Enabled with {@code -Dopendoja.traceFramePhases=true} (the Android host sets the
 * property from the launching intent's {@code traceFramePhases} extra). Every
 * REPORT_INTERVAL_FRAMES presented frames one averaged report line is logged;
 * when disabled every hook reduces to a static boolean check.
 */
public final class FramePhaseTrace {
    public static final boolean ENABLED = Boolean.getBoolean("opendoja.traceFramePhases");
    private static final int REPORT_INTERVAL_FRAMES = 60;

    public static final AtomicLong RASTER = new AtomicLong();
    public static final AtomicLong FLUSH = new AtomicLong();
    public static final AtomicLong PRESENT = new AtomicLong();
    public static final AtomicLong UPSCALE = new AtomicLong();

    private static final AtomicLong triangles = new AtomicLong();
    private static final AtomicLong testedPixels = new AtomicLong();
    private static final AtomicLong writtenPixels = new AtomicLong();
    private static final AtomicLong genericBlendPixels = new AtomicLong();
    private static final AtomicLong linearFilterPixels = new AtomicLong();
    private static final AtomicLong nonModulateEnvPixels = new AtomicLong();
    private static final AtomicLong edgeFailPixels = new AtomicLong();
    private static final AtomicLong depthFailPixels = new AtomicLong();

    private static final AtomicLong frames = new AtomicLong();
    private static volatile long windowStartNanos;

    static {
        OpenDoJaLog.info(FramePhaseTrace.class, "clinit ENABLED=" + ENABLED);
    }

    private FramePhaseTrace() {
    }

    public static void addTriangles(long count) {
        if (ENABLED) {
            triangles.addAndGet(count);
        }
    }

    public static void addRasterPixels(long tested, long written, long genericBlend,
                                       long linearFilter, long nonModulateEnv,
                                       long edgeFail, long depthFail) {
        if (ENABLED) {
            testedPixels.addAndGet(tested);
            writtenPixels.addAndGet(written);
            genericBlendPixels.addAndGet(genericBlend);
            linearFilterPixels.addAndGet(linearFilter);
            nonModulateEnvPixels.addAndGet(nonModulateEnv);
            edgeFailPixels.addAndGet(edgeFail);
            depthFailPixels.addAndGet(depthFail);
        }
    }

    public static long begin() {
        return ENABLED ? System.nanoTime() : 0L;
    }

    public static void end(AtomicLong phase, long startNanos) {
        if (ENABLED) {
            phase.addAndGet(System.nanoTime() - startNanos);
        }
    }

    /** Called once per frame presented to the platform surface. */
    public static void framePresented() {
        if (!ENABLED) {
            return;
        }
        long now = System.nanoTime();
        if (windowStartNanos == 0L) {
            windowStartNanos = now;
        }
        if (frames.incrementAndGet() % REPORT_INTERVAL_FRAMES != 0) {
            return;
        }
        long windowNanos = now - windowStartNanos;
        windowStartNanos = now;
        double fps = windowNanos > 0L ? REPORT_INTERVAL_FRAMES * 1_000_000_000.0 / windowNanos : 0.0;
        OpenDoJaLog.info(FramePhaseTrace.class, String.format(Locale.ROOT,
                "frames=%d fps=%.1f raster=%.2fms flush=%.2fms present=%.2fms upscale=%.2fms tris=%.0f tested=%.0f written=%.0f efail=%.0f dfail=%.0f gblend=%.0f lin=%.0f nmod=%.0f (avg per frame)",
                REPORT_INTERVAL_FRAMES, fps,
                avgFrameMillis(RASTER), avgFrameMillis(FLUSH),
                avgFrameMillis(PRESENT), avgFrameMillis(UPSCALE),
                avgCount(triangles), avgCount(testedPixels), avgCount(writtenPixels),
                avgCount(edgeFailPixels), avgCount(depthFailPixels),
                avgCount(genericBlendPixels), avgCount(linearFilterPixels),
                avgCount(nonModulateEnvPixels)));
    }

    private static double avgFrameMillis(AtomicLong phase) {
        return phase.getAndSet(0L) / (double) REPORT_INTERVAL_FRAMES / 1_000_000.0;
    }

    private static double avgCount(AtomicLong counter) {
        return counter.getAndSet(0L) / (double) REPORT_INTERVAL_FRAMES;
    }
}
