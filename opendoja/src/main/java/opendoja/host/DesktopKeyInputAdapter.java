package opendoja.host;

import com.nttdocomo.ui.Display;

import java.util.*;

public final class DesktopKeyInputAdapter {
    private final Scheduler scheduler;
    private final KeySink sink;
    private final int releaseDebounceMs;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Map<Integer, PendingRelease> pendingReleases = new HashMap<>();

    public DesktopKeyInputAdapter(Scheduler scheduler, KeySink sink, int releaseDebounceMs) {
        this.scheduler = scheduler;
        this.sink = sink;
        this.releaseDebounceMs = java.lang.Math.max(0, releaseDebounceMs);
    }

    public void keyPressed(int dojaKey) {
        PendingRelease pendingRelease = pendingReleases.remove(dojaKey);
        if (pendingRelease != null) {
            // Desktop auto-repeat can surface as a same-key release/press pair while the key is
            // still physically held. Cancel the deferred release and keep the hold active.
            pendingRelease.cancel();
            return;
        }
        if (!pressedKeys.add(dojaKey)) {
            return;
        }
        sink.dispatch(dojaKey, Display.KEY_PRESSED_EVENT);
    }

    public void keyReleased(int dojaKey) {
        if (!pressedKeys.contains(dojaKey)) {
            return;
        }
        PendingRelease existing = pendingReleases.remove(dojaKey);
        if (existing != null) {
            existing.cancel();
        }
        final PendingRelease[] holder = new PendingRelease[1];
        PendingRelease pendingRelease = scheduler.schedule(releaseDebounceMs, () -> {
            PendingRelease scheduled = holder[0];
            if (scheduled == null || !pendingReleases.remove(dojaKey, scheduled)) {
                return;
            }
            if (!pressedKeys.remove(dojaKey)) {
                return;
            }
            sink.dispatch(dojaKey, Display.KEY_RELEASED_EVENT);
        });
        holder[0] = pendingRelease;
        pendingReleases.put(dojaKey, pendingRelease);
    }

    public void releaseAll() {
        for (PendingRelease pendingRelease : pendingReleases.values()) {
            pendingRelease.cancel();
        }
        pendingReleases.clear();
        List<Integer> keysToRelease = pressedKeys.stream().sorted().collect(java.util.stream.Collectors.toList());
        pressedKeys.clear();
        for (int dojaKey : keysToRelease) {
            sink.dispatch(dojaKey, Display.KEY_RELEASED_EVENT);
        }
    }

    public interface Scheduler {
        PendingRelease schedule(int delayMillis, Runnable task);
    }

    public interface PendingRelease {
        void cancel();
    }

    public interface KeySink {
        void dispatch(int dojaKey, int eventType);
    }
}
