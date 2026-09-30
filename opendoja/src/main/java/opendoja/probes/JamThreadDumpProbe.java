package opendoja.probes;

import com.nttdocomo.ui.Canvas;
import com.nttdocomo.ui.Frame;
import opendoja.host.DoJaRuntime;
import opendoja.host.JamLauncher;

import java.nio.file.Path;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Launches a JAM and dumps the active frame plus live Java thread stacks.
 */
public final class JamThreadDumpProbe {
    private JamThreadDumpProbe() {
    }

    public static void main(String[] args) throws Exception {
        DemoLog.enableInfoLogging();
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: JamThreadDumpProbe <jam-path> <delay-ms>");
        }
        Path jamPath = Path.of(args[0]);
        long delayMillis = Long.parseLong(args[1]);

        Thread launchThread = new Thread(() -> {
            try {
                JamLauncher.launch(jamPath);
            } catch (Throwable throwable) {
                DemoLog.error(JamThreadDumpProbe.class, "Launch failed", throwable);
            }
        }, "jam-thread-dump-launch");
        launchThread.setDaemon(true);
        launchThread.start();

        Throwable failure = null;
        try {
            waitForRuntime();
            Thread.sleep(Math.max(0L, delayMillis));
            dumpRuntimeState();
            dumpThreads();
        } catch (Throwable throwable) {
            failure = throwable;
            DemoLog.error(JamThreadDumpProbe.class, "Probe failed", throwable);
        } finally {
            DoJaRuntime runtime = DoJaRuntime.current();
            if (runtime != null) {
                runtime.shutdown();
            }
            System.exit(failure == null ? 0 : 1);
        }
    }

    private static void waitForRuntime() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000L;
        while (DoJaRuntime.current() == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(20L);
        }
        if (DoJaRuntime.current() == null) {
            throw new IllegalStateException("DoJa runtime did not initialize");
        }
    }

    private static void dumpRuntimeState() {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime == null) {
            System.out.println("runtime=null");
            return;
        }
        Frame frame = runtime.getCurrentFrame();
        System.out.println("runtime.currentFrame=" + describe(frame));
        if (frame instanceof Canvas canvas) {
            System.out.println("runtime.currentCanvas=" + canvas.getClass().getName());
        }
    }

    private static String describe(Object value) {
        if (value == null) {
            return "null";
        }
        return value.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(value));
    }

    private static void dumpThreads() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadBean.dumpAllThreads(true, true);
        List<ThreadInfo> entries = new ArrayList<>(List.of(threadInfos));
        entries.sort(Comparator.comparing(ThreadInfo::getThreadName));
        for (ThreadInfo info : entries) {
            System.out.println();
            System.out.println('"' + info.getThreadName() + "\" id=" + info.getThreadId()
                    + " state=" + info.getThreadState()
                    + lockOwnerDescription(info));
            for (StackTraceElement element : info.getStackTrace()) {
                System.out.println("    at " + element);
            }
            for (MonitorInfo monitor : info.getLockedMonitors()) {
                System.out.println("    - locked monitor " + monitor);
            }
            for (LockInfo synchronizer : info.getLockedSynchronizers()) {
                System.out.println("    - locked synchronizer " + synchronizer);
            }
        }
    }

    private static String lockOwnerDescription(ThreadInfo info) {
        if (info.getLockName() == null) {
            return "";
        }
        if (info.getLockOwnerName() == null) {
            return " waitingOn=" + info.getLockName();
        }
        return " waitingOn=" + info.getLockName()
                + " owner=\"" + info.getLockOwnerName() + "\""
                + " ownerId=" + info.getLockOwnerId();
    }
}
