package opendoja.probes;

import com.nttdocomo.io.ConnectionException;
import com.nttdocomo.ui.IApplication;
import opendoja.host.DoJaRuntime;
import opendoja.host.JamLauncher;

import javax.microedition.io.Connector;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;

public final class DragonBallRacingScratchpadProbe {
    private DragonBallRacingScratchpadProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: DragonBallRacingScratchpadProbe <jam-path>");
        }
        Path jamPath = Path.of(args[0]);
        Thread launchThread = new Thread(() -> {
            try {
                JamLauncher.launch(jamPath);
            } catch (Throwable throwable) {
                throwable.printStackTrace(System.out);
            }
        }, "dragon-ball-racing-launch");
        launchThread.setDaemon(true);
        launchThread.start();

        waitForRuntime();
        try {
            verifyOutOfBoundsScratchpadOpen();
            verifyLaunchThreadIsOrIsNotStuckInBbProbe(launchThread);
        } finally {
            DoJaRuntime runtime = DoJaRuntime.current();
            if (runtime != null) {
                runtime.shutdown();
            }
        }
    }

    private static void waitForRuntime() throws Exception {
        long deadline = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < deadline) {
            DoJaRuntime runtime = DoJaRuntime.current();
            if (runtime != null) {
                IApplication application = runtime.application();
                if (application != null) {
                    return;
                }
            }
            Thread.sleep(20L);
        }
        throw new IllegalStateException("DoJa runtime did not initialize");
    }

    private static void verifyOutOfBoundsScratchpadOpen() throws Exception {
        try (InputStream input = Connector.openInputStream("scratchpad:///0;pos=800000,length=1")) {
            int first = input.read();
            System.out.println("out_of_bounds_open=unexpected_success first_byte=" + first);
        } catch (ConnectionException e) {
            System.out.println("out_of_bounds_open=rejected status=" + e.getStatus()
                    + " type=" + e.getClass().getName()
                    + " message=" + e.getMessage());
        } catch (IOException e) {
            System.out.println("out_of_bounds_open=rejected " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static void verifyLaunchThreadIsOrIsNotStuckInBbProbe(Thread launchThread) throws Exception {
        Thread.sleep(1000L);
        StackTraceElement[] stack = launchThread.getStackTrace();
        boolean containsBbProbe = Arrays.stream(stack)
                .anyMatch(frame -> frame.getClassName().equals("bb"));
        System.out.println("launch_thread_in_bb=" + containsBbProbe);
        for (int i = 0; i < Math.min(stack.length, 12); i++) {
            System.out.println("stack[" + i + "]=" + stack[i]);
        }
    }
}
