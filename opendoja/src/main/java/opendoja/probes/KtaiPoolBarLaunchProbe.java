package opendoja.probes;

import com.nttdocomo.ui.IApplication;
import opendoja.host.DoJaRuntime;
import opendoja.host.JamLauncher;

import java.lang.reflect.Field;
import java.nio.file.Path;

/**
 * Launches the bundled K-tai POOL BAR sample far enough to prove scratchpad
 * resource initialization completes instead of falling into the app's startup
 * error screen.
 */
public final class KtaiPoolBarLaunchProbe {
    private static final Path DEFAULT_JAM = Path.of("resources/sample_games/K-tai POOL BAR/breakShot.jam");

    private KtaiPoolBarLaunchProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        Path jam = args.length == 0 ? DEFAULT_JAM : Path.of(args[0]);
        Throwable failure = null;
        try {
            IApplication app = JamLauncher.launch(jam, false);
            waitForLoadedBootState(app.getClass());
            System.out.println("K-tai POOL BAR launch probe OK");
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

    private static void waitForLoadedBootState(Class<?> gameClass) throws Exception {
        Field bootState = accessibleField(gameClass, "m_bootState");
        Field section = accessibleField(gameClass, "m_section");
        Field errorString = accessibleField(gameClass, "m_errString");
        long deadline = System.currentTimeMillis() + 8000L;
        while (System.currentTimeMillis() < deadline) {
            int boot = bootState.getInt(null);
            int currentSection = section.getInt(null);
            String error = (String) errorString.get(null);
            if (boot >= 2) {
                return;
            }
            if (boot == -1 && currentSection == -1 && error != null && !error.isEmpty()) {
                throw new IllegalStateException("Startup failed with game error: " + error);
            }
            Thread.sleep(50L);
        }
        throw new IllegalStateException("Timed out waiting for resource initialization; bootState="
                + bootState.getInt(null) + ", section=" + section.getInt(null)
                + ", error=" + errorString.get(null));
    }

    private static Field accessibleField(Class<?> type, String name) throws NoSuchFieldException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
