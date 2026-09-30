package opendoja.probes;

import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.IApplication;
import com.nttdocomo.ui.TextBox;
import opendoja.host.DoJaRuntime;
import opendoja.host.LaunchConfig;
import opendoja.host.OpenDoJaLaunchArgs;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Verifies that Chocomate's IME request path returns immediately and completes asynchronously.
 *
 * <p>Run with `resources/sample_games/Chocomate/chocomate.jar` on the classpath.</p>
 */
public final class ChocomateImeCompatibilityProbe {
    private ChocomateImeCompatibilityProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        String previousImeResponse = System.getProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE);
        System.setProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE, "42");

        DoJaRuntime runtime = DoJaRuntime.bootstrap(LaunchConfig.builder(ProbeApp.class).build());
        try {
            Object canvas = newChocomateCanvas();
            configureImeScenario(canvas);
            Display.setCurrent((com.nttdocomo.ui.Frame) canvas);

            CountDownLatch invocationComplete = new CountDownLatch(1);
            Throwable[] failure = new Throwable[1];
            Thread invocation = new Thread(() -> {
                try {
                    invokeF8(canvas, 20);
                } catch (Throwable throwable) {
                    failure[0] = throwable;
                } finally {
                    invocationComplete.countDown();
                }
            }, "chocomate-ime-probe");
            invocation.start();

            if (!invocationComplete.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Chocomate Dm.f8(20) did not return promptly");
            }
            if (failure[0] != null) {
                throw propagate(failure[0]);
            }

            waitForImeCommit(canvas, 42, Duration.ofSeconds(2));
            System.out.println("chocomate-ime-compatibility-probe-ok");
        } finally {
            runtime.shutdown();
            if (previousImeResponse == null) {
                System.clearProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE);
            } else {
                System.setProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE, previousImeResponse);
            }
        }
    }

    private static Object newChocomateCanvas() throws Exception {
        Class<?> cmClass = Class.forName("chocomate.Cm");
        Constructor<?> cmConstructor = cmClass.getDeclaredConstructor();
        cmConstructor.setAccessible(true);
        Object cm = cmConstructor.newInstance();

        Class<?> dmClass = Class.forName("chocomate.Dm");
        Constructor<?> dmConstructor = dmClass.getDeclaredConstructor(cmClass, int.class);
        dmConstructor.setAccessible(true);
        return dmConstructor.newInstance(cm, 0);
    }

    private static void configureImeScenario(Object canvas) throws Exception {
        setField(canvas, "pStat", 2);
        setField(canvas, "dReqInt", new int[]{0, 0, 0, 0});

        byte[] request = new byte[10];
        request[0] = 4;
        request[1] = 0;
        request[6] = (byte) TextBox.NUMBER;
        setField(canvas, "dReqBytes", new byte[][]{request});

        int[] mInt = (int[]) getField(canvas, "mInt");
        mInt[2] = 0;

        int[] numbers = (int[]) getField(canvas, "gi");
        numbers[0] = 7;
    }

    private static void invokeF8(Object canvas, int padRequest) throws Exception {
        Method f8 = canvas.getClass().getDeclaredMethod("f8", int.class);
        f8.setAccessible(true);
        f8.invoke(canvas, padRequest);
    }

    private static void waitForImeCommit(Object canvas, int expected, Duration timeout) throws Exception {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            int[] numbers = (int[]) getField(canvas, "gi");
            int imeReq = (int) getField(canvas, "imeReq");
            boolean isIme = (boolean) getField(canvas, "isIme");
            if (numbers[0] == expected && imeReq == 0 && !isIme) {
                return;
            }
            Thread.sleep(20L);
        }
        throw new IllegalStateException("Timed out waiting for Chocomate IME completion");
    }

    private static Object getField(Object target, String name) throws Exception {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static Exception propagate(Throwable throwable) {
        if (throwable instanceof Exception exception) {
            return exception;
        }
        return new IllegalStateException("Probe invocation failed", throwable);
    }

    public static final class ProbeApp extends IApplication {
        @Override
        public void start() {
        }
    }
}
