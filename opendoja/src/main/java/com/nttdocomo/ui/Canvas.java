package com.nttdocomo.ui;

import com.nttdocomo.lang.IllegalStateException;
import opendoja.host.DesktopSurface;
import opendoja.host.DoJaRuntime;
import opendoja.host.HostTextInput;

/**
 * Defines the display surface for the low-level API.
 * A canvas is the frame class used by the low-level API and does not provide a
 * scrolling function.
 */
public abstract class Canvas extends Frame {
    /**
     * Indicates that IME input has been committed (=0).
     * This value is passed as the {@code type} argument of
     * {@link #processIMEEvent(int, String)}.
     */
    public static final int IME_COMMITTED = 0;
    /**
     * Indicates that IME input has been canceled (=1).
     * This value is passed as the {@code type} argument of
     * {@link #processIMEEvent(int, String)}.
     */
    public static final int IME_CANCELED = 1;

    private DesktopSurface surface;

    /**
     * Creates a canvas object.
     */
    public Canvas() {
    }

    /**
     * Gets the graphics object used to draw on the canvas.
     *
     * @return the graphics object for the canvas
     */
    public Graphics getGraphics() {
        return createGraphics();
    }

    Graphics runtimeGraphics() {
        return createGraphics();
    }

    private Graphics createGraphics() {
        ensureSurface(Display.getWidth(), Display.getHeight());
        surface.setBackgroundColor(backgroundColor());
        // Capture the runtime that owns this canvas. After that runtime shuts down (game
        // exited) a leaked game thread must not keep feeding frames into a newer runtime
        // through the static current() lookup.
        final DoJaRuntime hookRuntime = DoJaRuntime.current();
        surface.setRepaintHook((frame, frameBoundary) -> {
            if (hookRuntime == null || hookRuntime.isShutdown()) {
                return;
            }
            hookRuntime.notifySurfaceFlush(this, frame, frameBoundary);
        });
        return Graphics.createPlatformGraphics(surface);
    }

    /**
     * Paints the canvas.
     *
     * @param g the graphics object to use for painting
     */
    public abstract void paint(Graphics g);

    /**
     * Requests that the whole canvas be repainted.
     */
    public void repaint() {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime != null) {
            runtime.requestRepaint(this);
        }
    }

    /**
     * Requests that a rectangular region of the canvas be repainted.
     *
     * @param x the X coordinate of the region
     * @param y the Y coordinate of the region
     * @param width the region width
     * @param height the region height
     */
    public void repaint(int x, int y, int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("width/height");
        }
        if (width == 0 || height == 0) {
            return;
        }
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime != null) {
            runtime.requestRepaint(this, x, y, width, height);
        }
    }

    /**
     * Called when a low-level event is delivered to the canvas.
     * The default implementation does nothing.
     *
     * @param type the event type
     * @param param the event parameter
     */
    public void processEvent(int type, int param) {
        return;
    }

    /**
     * Performs process Soft Key Event.
     */
    @Override
    public void processSoftKeyEvent(int type, int key) {
        int dojaKey = switch (key) {
            case SOFT_KEY_1 -> Display.KEY_SOFT1;
            case SOFT_KEY_2 -> Display.KEY_SOFT2;
            default -> -1;
        };
        if (dojaKey < 0) {
            return;
        }
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime != null) {
            runtime.dispatchSyntheticKey(dojaKey, type);
        }
    }

    /**
     * Gets the keypad state.
     *
     * @return the keypad state bit mask
     */
    public int getKeypadState() {
        DoJaRuntime runtime = DoJaRuntime.current();
        return runtime == null ? 0 : runtime.keypadState();
    }

    /**
     * Gets the keypad state for the specified key group.
     *
     * @param group the key group to inspect
     * @return the keypad state bit mask for the group
     */
    public int getKeypadState(int group) {
        if (group < 0) {
            throw new IllegalArgumentException("group");
        }
        DoJaRuntime runtime = DoJaRuntime.current();
        return runtime == null ? 0 : runtime.keypadState(group);
    }

    /**
     * Starts IME on the canvas.
     *
     * @param text the initial text passed into IME, or {@code null}
     * @param displayMode the display mode
     * @param inputMode the input mode
     */
    public void imeOn(String text, int displayMode, int inputMode) {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime == null || Display.getCurrent() != this || runtime.isDialogShowing()) {
            return;
        }
        ensureImeCallableState(runtime);
        validateDisplayMode(displayMode);
        validateInputMode(inputMode);
        HostTextInput.requestIme(this, text, displayMode, inputMode, 0);
    }

    /**
     * Starts IME on the canvas with an input-size limit.
     *
     * @param text the initial text passed into IME, or {@code null}
     * @param displayMode the display mode
     * @param inputMode the input mode
     * @param inputSize the maximum input size
     */
    public void imeOn(String text, int displayMode, int inputMode, int inputSize) {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime == null || Display.getCurrent() != this || runtime.isDialogShowing()) {
            return;
        }
        ensureImeCallableState(runtime);
        validateDisplayMode(displayMode);
        validateInputMode(inputMode);
        if (inputSize <= 0) {
            throw new IllegalArgumentException("inputSize");
        }
        String normalized = text == null ? "" : text;
        if (HostTextInput.inputUnits(normalized) > inputSize) {
            throw new IllegalArgumentException("text");
        }
        HostTextInput.requestIme(this, text, displayMode, inputMode, inputSize);
    }

    /**
     * Called when an IME event is delivered to the canvas.
     * The default implementation does nothing.
     *
     * @param type the IME event type
     * @param text the committed text, or {@code null}
     */
    public void processIMEEvent(int type, String text) {
        return;
    }

    private static void validateDisplayMode(int displayMode) {
        if (displayMode != TextBox.DISPLAY_ANY && displayMode != TextBox.DISPLAY_PASSWORD) {
            throw new IllegalArgumentException("displayMode");
        }
    }

    private static void validateInputMode(int inputMode) {
        if (inputMode != TextBox.NUMBER && inputMode != TextBox.ALPHA && inputMode != TextBox.KANA) {
            throw new IllegalArgumentException("inputMode");
        }
    }

    private static void ensureImeCallableState(DoJaRuntime runtime) {
        if (runtime.application() instanceof MApplication application && !application.isActive()) {
            throw new IllegalStateException("Application is inactive");
        }
    }

    /**
     * Sets the background color.
     *
     * @param color the integer value that represents the background color
     */
    @Override
    public void setBackground(int color) {
        super.setBackground(color);
        if (surface != null) {
            surface.setBackgroundColor(color);
        }
    }

    DesktopSurface surface() {
        return surface;
    }

    void ensureSurface(int width, int height) {
        if (surface == null) {
            surface = new DesktopSurface(width, height);
        } else {
            surface.resize(width, height);
        }
        surface.setBackgroundColor(backgroundColor());
    }
}
