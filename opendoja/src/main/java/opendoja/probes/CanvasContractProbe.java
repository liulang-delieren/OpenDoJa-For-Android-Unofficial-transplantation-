package opendoja.probes;

import com.nttdocomo.ui.Canvas;
import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.Frame;
import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.IApplication;
import com.nttdocomo.ui.MApplication;
import com.nttdocomo.ui.PhoneSystem;
import com.nttdocomo.ui.TextBox;
import com.nttdocomo.ui.UIException;
import opendoja.host.DesktopLauncher;
import opendoja.host.DoJaRuntime;
import opendoja.host.OpenDoJaLaunchArgs;

import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Focused verification for the documented Canvas contract.
 */
public final class CanvasContractProbe {
    private CanvasContractProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        verifyGetGraphicsInitialState();
        verifyRepaintValidation();
        verifyNegativeKeypadGroup();
        verifyKeypadGroupEnabledBeforeFirstCurrent();
        verifyHigherKeypadGroupAccepted();
        verifyKeypadGroupIgnoredAfterFirstCurrent();
        verifyImeIgnoredWhenNotCurrent();
        verifyImeInactiveState();
        verifyImeBusyResource();
        verifyImeIgnoredWhileDialogShowing();
        System.out.println("canvas-contract-probe-ok");
    }

    private static void verifyGetGraphicsInitialState() throws Exception {
        GraphicsProbeApp app = (GraphicsProbeApp) DesktopLauncher.launch(GraphicsProbeApp.class);
        try {
            Graphics first = app.canvas.getGraphics();
            first.setColor(Graphics.getColorOfName(Graphics.RED));
            first.setFont(Font.getFont(Font.FACE_SYSTEM | Font.STYLE_BOLD, Font.SIZE_LARGE));
            first.setOrigin(7, 9);
            first.setClip(3, 4, 5, 6);

            Graphics second = app.canvas.getGraphics();
            if (first == second) {
                throw new IllegalStateException("Canvas.getGraphics() reused the same Graphics object");
            }
            if (colorOf(second) != Graphics.getColorOfName(Graphics.BLACK)) {
                throw new IllegalStateException("Fresh Canvas graphics did not reset color");
            }
            if (fontOf(second) != Font.getDefaultFont()) {
                throw new IllegalStateException("Fresh Canvas graphics did not reset font");
            }
            if (originXOf(second) != 0 || originYOf(second) != 0) {
                throw new IllegalStateException("Fresh Canvas graphics did not reset origin");
            }
            Rectangle clip = clipBoundsOf(second);
            if (clip == null || clip.x != 0 || clip.y != 0
                    || clip.width != Display.getWidth() || clip.height != Display.getHeight()) {
                throw new IllegalStateException("Fresh Canvas graphics did not reset clip");
            }
            first.dispose();
            second.dispose();
        } finally {
            app.terminate();
        }
    }

    private static void verifyRepaintValidation() throws Exception {
        GraphicsProbeApp app = (GraphicsProbeApp) DesktopLauncher.launch(GraphicsProbeApp.class);
        try {
            app.canvas.awaitPaints(1);
            app.canvas.resetPaints();
            app.canvas.repaint(0, 0, 0, 0);
            Thread.sleep(100L);
            if (app.canvas.paintCount != 0) {
                throw new IllegalStateException("Zero-area repaint unexpectedly triggered paint()");
            }
            app.canvas.repaint(3, 4, 5, 6);
            app.canvas.awaitPaints(1);
            Rectangle clip = app.canvas.lastPaintClip;
            if (clip == null || clip.x != 3 || clip.y != 4 || clip.width != 5 || clip.height != 6) {
                throw new IllegalStateException("Partial repaint did not deliver the requested clip: " + clip);
            }
            expectIllegalArgument(() -> app.canvas.repaint(0, 0, -1, 0), "negative repaint width");
            expectIllegalArgument(() -> app.canvas.repaint(0, 0, 0, -1), "negative repaint height");
        } finally {
            app.terminate();
        }
    }

    private static void verifyNegativeKeypadGroup() throws Exception {
        GraphicsProbeApp app = (GraphicsProbeApp) DesktopLauncher.launch(GraphicsProbeApp.class);
        try {
            expectIllegalArgument(() -> app.canvas.getKeypadState(-1), "negative keypad group");
        } finally {
            app.terminate();
        }
    }

    private static void verifyKeypadGroupEnabledBeforeFirstCurrent() throws Exception {
        KeypadBeforeApp app = (KeypadBeforeApp) DesktopLauncher.launch(KeypadBeforeApp.class);
        try {
            DoJaRuntime runtime = requireRuntime();
            runtime.dispatchSyntheticKey(Display.KEY_MAIL, Display.KEY_PRESSED_EVENT);
            app.canvas.awaitEvents(1);

            int group0 = app.canvas.getKeypadState();
            int group1 = app.canvas.getKeypadState(1);
            if (group0 != app.canvas.getKeypadState(0)) {
                throw new IllegalStateException("Canvas.getKeypadState() diverged from group 0");
            }
            if (group0 != 0) {
                throw new IllegalStateException("Group 0 state leaked group 1 key bits");
            }
            if (group1 != (1 << (Display.KEY_MAIL - 32))) {
                throw new IllegalStateException("Unexpected group 1 keypad state: " + group1);
            }
            if (app.canvas.lastEventType != Display.KEY_PRESSED_EVENT || app.canvas.lastEventParam != Display.KEY_MAIL) {
                throw new IllegalStateException("Enabled group 1 key did not dispatch the expected event");
            }

            runtime.dispatchSyntheticKey(Display.KEY_MAIL, Display.KEY_RELEASED_EVENT);
            if (app.canvas.getKeypadState(1) != 0) {
                throw new IllegalStateException("Group 1 keypad state did not clear after release");
            }
        } finally {
            app.terminate();
        }
    }

    private static void verifyHigherKeypadGroupAccepted() throws Exception {
        KeypadHigherGroupApp app = (KeypadHigherGroupApp) DesktopLauncher.launch(KeypadHigherGroupApp.class);
        try {
            DoJaRuntime runtime = requireRuntime();
            if (!runtime.isKeypadGroupEnabled(2)) {
                throw new IllegalStateException("DEV_KEYPAD group 2 was rejected as if group 1 were the maximum");
            }
            if (app.canvas.getKeypadState(2) != 0) {
                throw new IllegalStateException("Unmapped keypad group 2 reported nonzero state");
            }
        } finally {
            app.terminate();
        }
    }

    private static void verifyKeypadGroupIgnoredAfterFirstCurrent() throws Exception {
        KeypadAfterApp app = (KeypadAfterApp) DesktopLauncher.launch(KeypadAfterApp.class);
        try {
            DoJaRuntime runtime = requireRuntime();
            runtime.dispatchSyntheticKey(Display.KEY_MAIL, Display.KEY_PRESSED_EVENT);
            Thread.sleep(100L);
            if (app.canvas.eventCount != 0) {
                throw new IllegalStateException("Late DEV_KEYPAD enable incorrectly allowed group 1 events");
            }
            if (app.canvas.getKeypadState(1) != 0) {
                throw new IllegalStateException("Late DEV_KEYPAD enable incorrectly exposed group 1 keypad state");
            }
        } finally {
            app.terminate();
        }
    }

    private static void verifyImeIgnoredWhenNotCurrent() throws Exception {
        ImeIgnoreApp app = (ImeIgnoreApp) DesktopLauncher.launch(ImeIgnoreApp.class);
        String previousResponse = System.getProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE);
        System.setProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE, "ignored");
        try {
            app.inactiveCanvas.imeOn("seed", TextBox.DISPLAY_ANY, TextBox.ALPHA);
            Thread.sleep(100L);
            if (app.inactiveCanvas.imeEventCount != 0) {
                throw new IllegalStateException("Non-current canvas unexpectedly received an IME callback");
            }
        } finally {
            restoreProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE, previousResponse);
            app.terminate();
        }
    }

    private static void verifyImeInactiveState() throws Exception {
        InactiveImeApp app = (InactiveImeApp) DesktopLauncher.launch(InactiveImeApp.class);
        String previousResponse = System.getProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE);
        System.setProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE, "inactive");
        try {
            app.deactivate();
            try {
                app.canvas.imeOn("seed", TextBox.DISPLAY_ANY, TextBox.ALPHA);
                throw new IllegalStateException("Inactive MApplication canvas accepted imeOn()");
            } catch (com.nttdocomo.lang.IllegalStateException expected) {
                // Expected path.
            }
        } finally {
            restoreProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE, previousResponse);
            app.terminate();
        }
    }

    private static void verifyImeBusyResource() throws Exception {
        GraphicsProbeApp app = (GraphicsProbeApp) DesktopLauncher.launch(GraphicsProbeApp.class);
        String previousResponse = System.getProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE);
        String previousDelay = System.getProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_DELAY_MS);
        System.setProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE, "busy");
        System.setProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_DELAY_MS, "200");
        try {
            app.canvas.resetIme();
            app.canvas.imeOn("seed", TextBox.DISPLAY_ANY, TextBox.ALPHA);
            try {
                app.canvas.imeOn("seed2", TextBox.DISPLAY_ANY, TextBox.ALPHA);
                throw new IllegalStateException("Concurrent IME requests did not raise BUSY_RESOURCE");
            } catch (UIException expected) {
                if (expected.getStatus() != UIException.BUSY_RESOURCE) {
                    throw expected;
                }
            }
            app.canvas.awaitImeEvents(1);
        } finally {
            restoreProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE, previousResponse);
            restoreProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_DELAY_MS, previousDelay);
            app.terminate();
        }
    }

    private static void verifyImeIgnoredWhileDialogShowing() throws Exception {
        GraphicsProbeApp app = (GraphicsProbeApp) DesktopLauncher.launch(GraphicsProbeApp.class);
        String previousResponse = System.getProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE);
        System.setProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE, "dialog");
        DoJaRuntime runtime = requireRuntime();
        Method beginModalDialog = DoJaRuntime.class.getDeclaredMethod("beginModalDialog");
        Method endModalDialog = DoJaRuntime.class.getDeclaredMethod("endModalDialog");
        beginModalDialog.setAccessible(true);
        endModalDialog.setAccessible(true);
        try {
            app.canvas.resetIme();
            beginModalDialog.invoke(runtime);
            app.canvas.imeOn("seed", TextBox.DISPLAY_ANY, TextBox.ALPHA);
            Thread.sleep(100L);
            if (app.canvas.imeEventCount != 0) {
                throw new IllegalStateException("Canvas IME did not ignore the active dialog state");
            }
        } finally {
            endModalDialog.invoke(runtime);
            restoreProperty(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE, previousResponse);
            app.terminate();
        }
    }

    private static DoJaRuntime requireRuntime() {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime == null) {
            throw new IllegalStateException("No active DoJa runtime");
        }
        return runtime;
    }

    private static int colorOf(Graphics graphics) throws Exception {
        Field field = Graphics.class.getDeclaredField("color");
        field.setAccessible(true);
        return field.getInt(graphics);
    }

    private static Font fontOf(Graphics graphics) throws Exception {
        Field field = Graphics.class.getDeclaredField("font");
        field.setAccessible(true);
        return (Font) field.get(graphics);
    }

    private static int originXOf(Graphics graphics) throws Exception {
        Method method = Graphics.class.getDeclaredMethod("getOriginX");
        method.setAccessible(true);
        return (Integer) method.invoke(graphics);
    }

    private static int originYOf(Graphics graphics) throws Exception {
        Method method = Graphics.class.getDeclaredMethod("getOriginY");
        method.setAccessible(true);
        return (Integer) method.invoke(graphics);
    }

    private static Rectangle clipBoundsOf(Graphics graphics) throws Exception {
        Field delegateField = Graphics.class.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        java.awt.Graphics2D delegate = (java.awt.Graphics2D) delegateField.get(graphics);
        return delegate.getClipBounds();
    }

    private static void expectIllegalArgument(ThrowingRunnable runnable, String message) throws Exception {
        try {
            runnable.run();
            throw new IllegalStateException(message);
        } catch (IllegalArgumentException expected) {
            // Expected path.
        }
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static final class GraphicsProbeApp extends IApplication {
        final ProbeCanvas canvas = new ProbeCanvas();

        @Override
        public void start() {
            Display.setCurrent((Frame) canvas);
        }
    }

    public static final class KeypadBeforeApp extends IApplication {
        final KeypadCanvas canvas = new KeypadCanvas();

        @Override
        public void start() {
            PhoneSystem.setAttribute(PhoneSystem.DEV_KEYPAD, 1);
            Display.setCurrent((Frame) canvas);
        }
    }

    public static final class KeypadHigherGroupApp extends IApplication {
        final KeypadCanvas canvas = new KeypadCanvas();

        @Override
        public void start() {
            PhoneSystem.setAttribute(PhoneSystem.DEV_KEYPAD, 2);
            Display.setCurrent((Frame) canvas);
        }
    }

    public static final class KeypadAfterApp extends IApplication {
        final KeypadCanvas canvas = new KeypadCanvas();

        @Override
        public void start() {
            Display.setCurrent((Frame) canvas);
            PhoneSystem.setAttribute(PhoneSystem.DEV_KEYPAD, 1);
        }
    }

    public static final class ImeIgnoreApp extends IApplication {
        final ProbeCanvas currentCanvas = new ProbeCanvas();
        final ProbeCanvas inactiveCanvas = new ProbeCanvas();

        @Override
        public void start() {
            Display.setCurrent((Frame) currentCanvas);
        }
    }

    public static final class InactiveImeApp extends MApplication {
        final ProbeCanvas canvas = new ProbeCanvas();

        @Override
        public void start() {
            Display.setCurrent((Frame) canvas);
        }
    }

    static class ProbeCanvas extends Canvas {
        volatile int imeEventCount;
        volatile int paintCount;
        volatile Rectangle lastPaintClip;

        @Override
        public void paint(Graphics g) {
            paintCount++;
            try {
                lastPaintClip = clipBoundsOf(g);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }

        @Override
        public void processIMEEvent(int type, String text) {
            imeEventCount++;
        }

        void resetIme() {
            imeEventCount = 0;
        }

        void resetPaints() {
            paintCount = 0;
            lastPaintClip = null;
        }

        void awaitImeEvents(int count) throws InterruptedException {
            if (count <= 0) {
                return;
            }
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2L);
            while (imeEventCount < count) {
                if (System.nanoTime() >= deadline) {
                    throw new IllegalStateException("Timed out waiting for IME callback");
                }
                Thread.sleep(10L);
            }
        }

        void awaitPaints(int count) throws InterruptedException {
            if (count <= 0) {
                return;
            }
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2L);
            while (paintCount < count) {
                if (System.nanoTime() >= deadline) {
                    throw new IllegalStateException("Timed out waiting for paint()");
                }
                Thread.sleep(10L);
            }
        }
    }

    static final class KeypadCanvas extends Canvas {
        private final CountDownLatch eventLatch = new CountDownLatch(1);
        volatile int eventCount;
        volatile int lastEventType;
        volatile int lastEventParam;

        @Override
        public void paint(Graphics g) {
            return;
        }

        @Override
        public void processEvent(int type, int param) {
            eventCount++;
            lastEventType = type;
            lastEventParam = param;
            eventLatch.countDown();
        }

        void awaitEvents(int count) throws InterruptedException {
            if (count <= 0) {
                return;
            }
            if (!eventLatch.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for keypad event");
            }
        }
    }
}
