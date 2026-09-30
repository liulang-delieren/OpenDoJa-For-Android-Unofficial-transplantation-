package opendoja.host;

import opendoja.compat.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DesktopExternalVideoPlayback implements AutoCloseable {
    private static final long COMPLETE_SLACK_MILLIS = 250L;

    private final DoJaRuntime runtime;
    private final byte[] data;
    private final String extension;
    private final long durationMillis;
    private final Runnable completeCallback;
    private final AtomicBoolean closed = new AtomicBoolean();
    private volatile Path tempFile;
    private volatile ScheduledFuture<?> completionFuture;

    public DesktopExternalVideoPlayback(DoJaRuntime runtime, byte[] data, String extension, long durationMillis,
                                        Runnable completeCallback) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.data = data == null ? new byte[0] : data.clone();
        this.extension = extension == null || extension.isBlank() ? "3gp" : extension.trim();
        this.durationMillis = durationMillis;
        this.completeCallback = Objects.requireNonNull(completeCallback, "completeCallback");
    }

    @SuppressWarnings("resource")
    public void start() {
        if (closed.get()) {
            return;
        }
        runtime.registerShutdownResource(this);
        long completeAfterMillis = resolveCompletionDelayMillis();
        completionFuture = runtime.scheduler().schedule(
                () -> runtime.postApplicationCallback(this::completeFromTimer),
                completeAfterMillis,
                TimeUnit.MILLISECONDS);
        try {
            Path extracted = Files.createTempFile("opendoja-video-", "." + extension);
            Files.write(extracted, data, StandardOpenOption.TRUNCATE_EXISTING);
            tempFile = extracted;
            if (!openDefaultPlayer(extracted)) {
                OpenDoJaLog.warn(DesktopExternalVideoPlayback.class,
                        "No default video player available; using completion shim only");
            }
        } catch (IOException e) {
            OpenDoJaLog.warn(DesktopExternalVideoPlayback.class,
                    "Failed to export external video playback file", e);
        }
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        runtime.unregisterShutdownResource(this);
        ScheduledFuture<?> future = completionFuture;
        completionFuture = null;
        if (future != null) {
            future.cancel(false);
        }
        Path extracted = tempFile;
        tempFile = null;
        if (extracted != null) {
            try {
                Files.deleteIfExists(extracted);
            } catch (IOException ignored) {
            }
        }
    }

    private void completeFromTimer() {
        if (closed.get()) {
            return;
        }
        close();
        completeCallback.run();
    }

    private long resolveCompletionDelayMillis() {
        long configuredFallback = Math.max(0L,
                OpenDoJaLaunchArgs.getLong(OpenDoJaLaunchArgs.VISUAL_PLAYER_FALLBACK_DELAY_MS, 2000L));
        long base = durationMillis > 0L ? durationMillis : configuredFallback;
        return Math.max(0L, base + COMPLETE_SLACK_MILLIS);
    }

    private static boolean openDefaultPlayer(Path file) {
        try {
            if (!Desktop.isDesktopSupported()) {
                return false;
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.OPEN)) {
                return false;
            }
            desktop.open(file.toFile());
            return true;
        } catch (IOException e) {
            OpenDoJaLog.warn(DesktopExternalVideoPlayback.class,
                    "Failed to open video with the default desktop player", e);
            return false;
        } catch (RuntimeException e) {
            OpenDoJaLog.warn(DesktopExternalVideoPlayback.class,
                    "Default desktop video player is unavailable", e);
            return false;
        }
    }
}
