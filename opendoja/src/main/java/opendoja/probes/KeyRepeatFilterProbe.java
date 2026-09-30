package opendoja.probes;

import com.nttdocomo.ui.Display;
import opendoja.host.DesktopKeyInputAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class KeyRepeatFilterProbe {
    private KeyRepeatFilterProbe() {
    }

    public static void main(String[] args) {
        ManualScheduler scheduler = new ManualScheduler();
        List<String> events = new ArrayList<>();
        DesktopKeyInputAdapter adapter = new DesktopKeyInputAdapter(
                scheduler,
                (dojaKey, eventType) -> events.add(eventName(eventType) + ":" + dojaKey + "@" + scheduler.nowMillis()),
                25);

        adapter.keyPressed(Display.KEY_RIGHT);
        adapter.keyReleased(Display.KEY_RIGHT);
        scheduler.advanceTo(5);
        adapter.keyPressed(Display.KEY_RIGHT);
        scheduler.advanceTo(30);
        require(events.equals(List.of("press:18@0")), "repeat pair leaked events: " + events);

        adapter.keyReleased(Display.KEY_RIGHT);
        scheduler.advanceTo(60);
        require(events.equals(List.of("press:18@0", "release:18@55")), "final release missing: " + events);

        adapter.keyPressed(Display.KEY_LEFT);
        adapter.keyReleased(Display.KEY_LEFT);
        scheduler.advanceTo(90);
        require(events.equals(List.of("press:18@0", "release:18@55", "press:16@60", "release:16@85")),
                "real tap was filtered incorrectly: " + events);

        adapter.keyPressed(Display.KEY_UP);
        adapter.releaseAll();
        require(events.equals(List.of(
                        "press:18@0",
                        "release:18@55",
                        "press:16@60",
                        "release:16@85",
                        "press:17@90",
                        "release:17@90")),
                "focus-loss release did not flush held key: " + events);

        System.out.println(String.join(", ", events));
    }

    private static String eventName(int eventType) {
        return eventType == Display.KEY_PRESSED_EVENT ? "press" : "release";
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static final class ManualScheduler implements DesktopKeyInputAdapter.Scheduler {
        private final List<ScheduledTask> tasks = new ArrayList<>();
        private long nowMillis;

        @Override
        public DesktopKeyInputAdapter.PendingRelease schedule(int delayMillis, Runnable task) {
            ScheduledTask scheduledTask = new ScheduledTask(nowMillis + java.lang.Math.max(0, delayMillis), task);
            tasks.add(scheduledTask);
            tasks.sort(Comparator.comparingLong(ScheduledTask::runAtMillis));
            return () -> scheduledTask.canceled = true;
        }

        long nowMillis() {
            return nowMillis;
        }

        void advanceTo(long targetMillis) {
            while (true) {
                ScheduledTask next = nextRunnableAtOrBefore(targetMillis);
                if (next == null) {
                    nowMillis = targetMillis;
                    return;
                }
                nowMillis = next.runAtMillis();
                next.task().run();
            }
        }

        private ScheduledTask nextRunnableAtOrBefore(long targetMillis) {
            for (int i = 0; i < tasks.size(); i++) {
                ScheduledTask task = tasks.get(i);
                if (task.canceled || task.runAtMillis() > targetMillis) {
                    continue;
                }
                tasks.remove(i);
                return task;
            }
            tasks.removeIf(task -> task.canceled && task.runAtMillis() <= targetMillis);
            return null;
        }
    }

    private static final class ScheduledTask {
        private final long runAtMillis;
        private final Runnable task;
        private boolean canceled;

        private ScheduledTask(long runAtMillis, Runnable task) {
            this.runAtMillis = runAtMillis;
            this.task = task;
        }

        private long runAtMillis() {
            return runAtMillis;
        }

        private Runnable task() {
            return task;
        }
    }
}
