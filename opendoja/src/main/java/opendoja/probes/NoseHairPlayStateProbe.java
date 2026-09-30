package opendoja.probes;

import com.nttdocomo.ui.Display;
import opendoja.host.DoJaRuntime;
import opendoja.host.JamLauncher;

import java.lang.reflect.Field;
import java.nio.file.Path;

public final class NoseHairPlayStateProbe {
    private NoseHairPlayStateProbe() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: NoseHairPlayStateProbe <jam-path>");
        }
        Path jamPath = Path.of(args[0]);
        Thread launchThread = new Thread(() -> {
            try {
                JamLauncher.launch(jamPath);
            } catch (Throwable throwable) {
                throwable.printStackTrace(System.err);
            }
        }, "nose-hair-play-state-probe-launch");
        launchThread.setDaemon(true);
        launchThread.start();

        Throwable failure = null;
        try {
            waitForRuntime();
            Thread.sleep(5000L);
            DoJaRuntime runtime = requireRuntime();
            runtime.dispatchSyntheticKey(Display.KEY_SELECT, Display.KEY_PRESSED_EVENT);
            Thread.sleep(200L);
            runtime.dispatchSyntheticKey(Display.KEY_SELECT, Display.KEY_RELEASED_EVENT);
            Thread.sleep(3000L);

            int[] state = readState(runtime);
            int q8 = state[8];
            int q9 = state[9];
            System.out.println("q8=" + q8 + " q9=" + q9);
            if (q9 == 10) {
                throw new IllegalStateException("PLAY transition is still stuck at state 10");
            }
        } catch (Throwable throwable) {
            failure = throwable;
            throwable.printStackTrace(System.err);
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

    private static DoJaRuntime requireRuntime() {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime == null) {
            throw new IllegalStateException("DoJa runtime exited before probe completed");
        }
        return runtime;
    }

    private static int[] readState(DoJaRuntime runtime) throws ReflectiveOperationException {
        ClassLoader loader = runtime.application().getClass().getClassLoader();
        Class<?> canvasClass = Class.forName("HanaGeCanvas", false, loader);
        Field stateField = canvasClass.getDeclaredField("Q");
        stateField.setAccessible(true);
        return ((int[]) stateField.get(null)).clone();
    }
}
