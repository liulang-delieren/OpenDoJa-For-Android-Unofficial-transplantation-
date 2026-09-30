package opendoja.probes;

import opendoja.host.DoJaRuntime;
import opendoja.host.JamLauncher;
import opendoja.host.JamNamedModuleResourceBridge;

import java.io.DataInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MidnightBowlingStateProbe {
    private MidnightBowlingStateProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: MidnightBowlingStateProbe <jam-path>");
        }
        Path jamPath = Path.of(args[0]);
        reexecWithNamedModuleBridgeIfNeeded(jamPath);
        Thread launchThread = new Thread(() -> {
            try {
                JamLauncher.launch(jamPath);
            } catch (Throwable throwable) {
                throwable.printStackTrace(System.out);
            }
        }, "midnight-bowling-launch");
        launchThread.setDaemon(true);
        launchThread.start();

        waitForRuntime();
        try {
            Class<?> gameClass = waitForGameClass();
            Field stateField = field(gameClass, "a");
            Field stageField = field(gameClass, "F");
            Field timerField = field(gameClass, "i");
            Field progressField = field(gameClass, "fF");
            Field indexField = field(gameClass, "fI");
            Field streamField = field(gameClass, "fE");
            Method loadTextBlock = method(gameClass, "aQ", int.class);
            Method openPackedFile = method(gameClass, "j", String.class);
            Method loadPackedIndex = method(gameClass, "cQ");
            Method seekPackedOffset = method(gameClass, "a", long.class);
            Method readPackedShort = method(gameClass, "cS");
            Method closePackedFile = method(gameClass, "cP");

            int lastState = Integer.MIN_VALUE;
            int lastStage = Integer.MIN_VALUE;
            int lastTimer = Integer.MIN_VALUE;
            int lastProgress = Integer.MIN_VALUE;
            long lastStateChange = System.currentTimeMillis();
            boolean inspectedStall = false;

            long deadline = System.currentTimeMillis() + 8000L;
            while (System.currentTimeMillis() < deadline) {
                int state = stateField.getInt(null);
                int stage = stageField.getInt(null);
                int timer = timerField.getInt(null);
                int progress = progressField.getInt(null);
                if (state != lastState || stage != lastStage || timer != lastTimer || progress != lastProgress) {
                    System.out.println("state=" + state
                            + " stage=" + stage
                            + " timer=" + timer
                            + " progress=" + progress);
                    if (state != lastState || stage != lastStage || progress != lastProgress) {
                        lastStateChange = System.currentTimeMillis();
                    }
                    lastState = state;
                    lastStage = stage;
                    lastTimer = timer;
                    lastProgress = progress;
                }
                if (!inspectedStall
                        && state == 1
                        && stage == 1
                        && System.currentTimeMillis() - lastStateChange >= 1500L) {
                    inspectedStall = true;
                    inspectTextBlock(loadTextBlock, 1);
                    inspectTextBlockRaw(openPackedFile, loadPackedIndex, seekPackedOffset,
                            readPackedShort, closePackedFile, indexField, streamField, 1);
                }
                Thread.sleep(100L);
            }
        } finally {
            DoJaRuntime runtime = DoJaRuntime.current();
            if (runtime != null) {
                runtime.shutdown();
            }
        }
    }

    private static void reexecWithNamedModuleBridgeIfNeeded(Path jamPath) throws Exception {
        if (JamNamedModuleResourceBridge.hasRequiredAddOpens()) {
            return;
        }
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.add(JamNamedModuleResourceBridge.requiredAddOpensArgument());
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(MidnightBowlingStateProbe.class.getName());
        command.add(jamPath.toString());
        Process process = new ProcessBuilder(command)
                .inheritIO()
                .start();
        int exit = process.waitFor();
        System.exit(exit);
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

    private static Class<?> waitForGameClass() throws Exception {
        long deadline = System.currentTimeMillis() + 5000L;
        Throwable lastFailure = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                DoJaRuntime runtime = DoJaRuntime.current();
                if (runtime != null && runtime.application() != null) {
                    ClassLoader loader = runtime.application().getClass().getClassLoader();
                    return Class.forName("e", false, loader);
                }
            } catch (Throwable throwable) {
                lastFailure = throwable;
            }
            Thread.sleep(20L);
        }
        throw new IllegalStateException("Game class did not load", lastFailure);
    }

    private static Field field(Class<?> owner, String name) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static Method method(Class<?> owner, String name, Class<?>... parameterTypes) throws Exception {
        Method method = owner.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    private static void inspectTextBlock(Method loadTextBlock, int index) throws Exception {
        try {
            Object value = loadTextBlock.invoke(null, index);
            int length = value instanceof String[] strings ? strings.length : -1;
            System.out.println("aQ(" + index + ") succeeded, strings=" + length);
        } catch (InvocationTargetException invocationTargetException) {
            Throwable cause = invocationTargetException.getCause();
            System.out.println("aQ(" + index + ") failed: " + cause);
            cause.printStackTrace(System.out);
        }
    }

    private static void inspectTextBlockRaw(Method openPackedFile,
                                            Method loadPackedIndex,
                                            Method seekPackedOffset,
                                            Method readPackedShort,
                                            Method closePackedFile,
                                            Field indexField,
                                            Field streamField,
                                            int index) throws Exception {
        try {
            openPackedFile.invoke(null, "/JP.pak");
            loadPackedIndex.invoke(null);
            int[] offsets = (int[]) indexField.get(null);
            int offset = offsets[index];
            System.out.println("raw aQ(" + index + ") offset=" + offset);
            seekPackedOffset.invoke(null, (long) offset);
            int count = ((Short) readPackedShort.invoke(null)).intValue() & 0xffff;
            System.out.println("raw aQ(" + index + ") count=" + count);
            DataInputStream input = (DataInputStream) streamField.get(null);
            for (int i = 0; i < count; i++) {
                String value = input.readUTF();
                System.out.println("raw aQ(" + index + ") string[" + i + "]=" + value);
            }
            System.out.println("raw aQ(" + index + ") completed");
        } catch (InvocationTargetException invocationTargetException) {
            Throwable cause = invocationTargetException.getCause();
            System.out.println("raw aQ(" + index + ") failed: " + cause);
            cause.printStackTrace(System.out);
        } catch (Throwable throwable) {
            System.out.println("raw aQ(" + index + ") failed: " + throwable);
            throwable.printStackTrace(System.out);
        } finally {
            closePackedFile.invoke(null);
        }
    }
}
