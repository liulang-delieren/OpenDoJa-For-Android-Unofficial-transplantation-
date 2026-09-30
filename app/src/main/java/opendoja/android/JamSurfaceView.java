package opendoja.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Frame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opendoja.compat.awt.image.BufferedImage;

/**
 * Presents the DoJa framebuffer and hosts the full virtual keypad in the letterbox
 * area below the game: d-pad + select, 12-key dial pad (0-9, *, #), soft keys,
 * menu and clear - the same action set as the desktop host keybind profile.
 * Touch zones are mapped into the runtime through {@link JamInputController}.
 */
public class JamSurfaceView extends View {
    private static final String TAG = "OpenDoJa";

    private static final int CTRL_NONE = 0;

    private static final int CTRL_UP = 1;
    private static final int CTRL_DOWN = 2;
    private static final int CTRL_LEFT = 3;
    private static final int CTRL_RIGHT = 4;
    private static final int CTRL_SELECT = 5;
    private static final int CTRL_SOFT1 = 6;
    private static final int CTRL_SOFT2 = 7;
    private static final int CTRL_MENU = 8;
    private static final int CTRL_CLEAR = 9;
    private static final int CTRL_DIAL_BASE = 10;

    private static final String[] DIAL_LABELS = {
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "*", "0", "#",
    };

    private static final int[] DIAL_KEYS = {
            Display.KEY_1, Display.KEY_2, Display.KEY_3,
            Display.KEY_4, Display.KEY_5, Display.KEY_6,
            Display.KEY_7, Display.KEY_8, Display.KEY_9,
            Display.KEY_ASTERISK, Display.KEY_0, Display.KEY_POUND,
    };

    private static final class KeyDef {
        final int id;
        final String label;
        final int code;
        final boolean soft;
        final RectF rect = new RectF();

        KeyDef(int id, String label, int code, boolean soft) {
            this.id = id;
            this.label = label;
            this.code = code;
            this.soft = soft;
        }
    }

    private final Object lock = new Object();
    private Bitmap upscaleBitmap;
    private int[] upPixels;
    private int[] upSrcPixels;
    private int[] upColSrc;
    private int[] upRowSrc;
    private int upSrcWidth;
    private int upSrcHeight;
    private int upscaledVersion = -1;
    private boolean upscaleLogged;
    private final Paint keyFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint keyStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint keyTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final JamInputController input;
    private final Map<Integer, Integer> pointerControls = new HashMap<>();
    private final Set<Integer> activeControls = new HashSet<>();

    private final List<KeyDef> keys = new ArrayList<>();
    private final Map<Integer, KeyDef> keysById = new HashMap<>();
    private final KeyDef upKey;
    private final KeyDef downKey;
    private final KeyDef leftKey;
    private final KeyDef rightKey;
    private final KeyDef selectKey;

    private Bitmap screen;
    private int screenWidth;
    private int screenHeight;
    private int[] pixels;
    private int screenVersion;
    private boolean keypadVisible = true;

    private final RectF padRect = new RectF();
    private final RectF gameRect = new RectF();
    private int gamePixelW;
    private int gamePixelH;
    private Integer pointerFinger;
    private int lastPointerX;
    private int lastPointerY;

    public JamSurfaceView(Context context, JamInputController input) {
        super(context);
        this.input = input;
        setBackgroundColor(Color.BLACK);
        setKeepScreenOn(true);
        setFocusable(true);

        upKey = addKey(CTRL_UP, "▲", Display.KEY_UP, false);
        downKey = addKey(CTRL_DOWN, "▼", Display.KEY_DOWN, false);
        leftKey = addKey(CTRL_LEFT, "◀", Display.KEY_LEFT, false);
        rightKey = addKey(CTRL_RIGHT, "▶", Display.KEY_RIGHT, false);
        selectKey = addKey(CTRL_SELECT, "●", Display.KEY_SELECT, false);
        addKey(CTRL_SOFT1, "SOFT1", Frame.SOFT_KEY_1, true);
        addKey(CTRL_SOFT2, "SOFT2", Frame.SOFT_KEY_2, true);
        addKey(CTRL_MENU, "MENU", Display.KEY_MENU, false);
        addKey(CTRL_CLEAR, "CLEAR", Display.KEY_CLEAR, false);
        for (int i = 0; i < DIAL_LABELS.length; i++) {
            addKey(CTRL_DIAL_BASE + i, DIAL_LABELS[i], DIAL_KEYS[i], false);
        }

        keyFillPaint.setColor(0x40FFFFFF);
        keyFillPaint.setStyle(Paint.Style.FILL);
        keyStrokePaint.setColor(0x99FFFFFF);
        keyStrokePaint.setStyle(Paint.Style.STROKE);
        keyStrokePaint.setStrokeWidth(2f);
        keyTextPaint.setColor(0xF0FFFFFF);
        keyTextPaint.setTextAlign(Paint.Align.CENTER);
        keyTextPaint.setFakeBoldText(true);
    }

    private KeyDef addKey(int id, String label, int code, boolean soft) {
        KeyDef def = new KeyDef(id, label, code, soft);
        keys.add(def);
        keysById.put(id, def);
        return def;
    }

    public void present(BufferedImage frame) {
        if (frame == null) {
            return;
        }
        int w = frame.getWidth();
        int h = frame.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        synchronized (lock) {
            if (pixels == null || pixels.length < w * h) {
                pixels = new int[w * h];
            }
            frame.getRGB(0, 0, w, h, pixels, 0, w);
            if (screen == null || screen.getWidth() != w || screen.getHeight() != h) {
                screen = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            }
            screen.setPixels(pixels, 0, w, 0, 0, w, h);
            screenWidth = w;
            screenHeight = h;
            // Lets onDraw skip re-running the CPU nearest-neighbor upscale when the
            // framebuffer content did not change (e.g. touch-only repaints).
            screenVersion++;
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bmp;
        int sourceVersion;
        synchronized (lock) {
            bmp = screen;
            sourceVersion = screenVersion;
        }
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        if (viewWidth <= 0f || viewHeight <= 0f) {
            return;
        }
        computeZones();
        if (bmp != null && !bmp.isRecycled()) {
            drawGameUpscaled(canvas, bmp, sourceVersion);
        }
        if (keypadVisible) {
            drawKeypad(canvas);
        }
    }

    public boolean isKeypadVisible() {
        return keypadVisible;
    }

    /** Hides/shows the whole on-screen keypad (d-pad, dial pad, soft keys). */
    public void setKeypadVisible(boolean visible) {
        if (keypadVisible == visible) {
            return;
        }
        keypadVisible = visible;
        if (!visible) {
            // Never leave virtual keys stuck down when the pad disappears.
            releasePointers();
        }
        postInvalidate();
    }

    /**
     * Scales the game framebuffer into the letterbox area with nearest-neighbor
     * sampling done on the CPU (per-pixel index mapping into an int[] that is
     * pushed with setPixels), then blits the result 1:1. Both hardware renderers
     * and the software Canvas ignore {@code Paint.FILTER_BITMAP} and resample
     * with linear filtering, which shows up as a blurry picture; writing the
     * scaled pixels ourselves removes any dependency on the graphics stack.
     */
    private void drawGameUpscaled(Canvas canvas, Bitmap bmp, int sourceVersion) {
        int dstLeft = Math.round(gameRect.left);
        int dstTop = Math.round(gameRect.top);
        int dstWidth = Math.max(1, Math.round(gameRect.width()));
        int dstHeight = Math.max(1, Math.round(gameRect.height()));
        int srcWidth = bmp.getWidth();
        int srcHeight = bmp.getHeight();
        if (srcWidth <= 0 || srcHeight <= 0) {
            return;
        }
        Bitmap scaled = upscaleBitmap;
        if (scaled == null || scaled.isRecycled()
                || scaled.getWidth() != dstWidth || scaled.getHeight() != dstHeight
                || upSrcWidth != srcWidth || upSrcHeight != srcHeight) {
            scaled = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
            upscaleBitmap = scaled;
            upPixels = new int[dstWidth * dstHeight];
            upSrcPixels = new int[srcWidth * srcHeight];
            upColSrc = new int[dstWidth];
            for (int x = 0; x < dstWidth; x++) {
                upColSrc[x] = (int) ((long) x * srcWidth / dstWidth);
            }
            upRowSrc = new int[dstHeight];
            for (int y = 0; y < dstHeight; y++) {
                upRowSrc[y] = (int) ((long) y * srcHeight / dstHeight);
            }
            upSrcWidth = srcWidth;
            upSrcHeight = srcHeight;
            upscaledVersion = -1;
            upscaleLogged = false;
        }
        if (upscaledVersion != sourceVersion) {
            bmp.getPixels(upSrcPixels, 0, srcWidth, 0, 0, srcWidth, srcHeight);
            if (dstWidth == srcWidth && dstHeight == srcHeight) {
                System.arraycopy(upSrcPixels, 0, upPixels, 0, srcWidth * srcHeight);
            } else {
                int dstRow = 0;
                for (int y = 0; y < dstHeight; y++) {
                    int srcRow = upRowSrc[y] * srcWidth;
                    int out = dstRow;
                    for (int x = 0; x < dstWidth; x++) {
                        upPixels[out++] = upSrcPixels[srcRow + upColSrc[x]];
                    }
                    dstRow += dstWidth;
                }
            }
            scaled.setPixels(upPixels, 0, dstWidth, 0, 0, dstWidth, dstHeight);
            upscaledVersion = sourceVersion;
        }
        if (!upscaleLogged) {
            upscaleLogged = true;
            Log.i(TAG, "upscale nearest: src=" + srcWidth + "x" + srcHeight
                    + " dst=" + dstWidth + "x" + dstHeight
                    + " at " + dstLeft + "," + dstTop
                    + " hwAccel=" + canvas.isHardwareAccelerated());
        }
        canvas.drawBitmap(scaled, dstLeft, dstTop, null);
    }

    private void computeZones() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        if (viewWidth <= 0f || viewHeight <= 0f) {
            return;
        }
        int gameW;
        int gameH;
        synchronized (lock) {
            gameW = screenWidth > 0 ? screenWidth : 240;
            gameH = screenHeight > 0 ? screenHeight : 240;
        }
        float scale = Math.min(viewWidth / gameW, viewHeight / gameH);
        float dstWidth = gameW * scale;
        float dstHeight = gameH * scale;
        float gameLeft = (viewWidth - dstWidth) / 2f;
        float gameTop = (viewHeight - dstHeight) / 2f;
        gameRect.set(gameLeft, gameTop, gameLeft + dstWidth, gameTop + dstHeight);
        gamePixelW = gameW;
        gamePixelH = gameH;
        float bandTop = gameTop + dstHeight;
        float bandHeight = viewHeight - bandTop;
        if (bandHeight < viewHeight * 0.15f) {
            // Almost no letterbox room: overlay the bottom strip of the screen instead.
            bandTop = viewHeight * 0.74f;
            bandHeight = viewHeight - bandTop;
        }
        padRect.set(0f, bandTop, viewWidth, viewHeight);

        float pad = Math.min(bandHeight, viewWidth) * 0.05f;
        float innerLeft = padRect.left + pad;
        float innerTop = padRect.top + pad;
        float innerRight = padRect.right - pad;
        float innerBottom = padRect.bottom - pad;
        float innerWidth = innerRight - innerLeft;
        float innerHeight = innerBottom - innerTop;
        if (innerWidth <= 0f || innerHeight <= 0f) {
            return;
        }

        float gap = innerWidth * 0.018f;
        float dpadWidth = innerWidth * 0.40f;
        float rightWidth = innerWidth * 0.245f;
        float dialWidth = innerWidth - dpadWidth - rightWidth - 2f * gap;

        // D-pad cross with select in the center.
        float side = Math.min(innerHeight, dpadWidth);
        float centerDx = innerLeft + dpadWidth / 2f;
        float centerDy = innerTop + innerHeight / 2f;
        float arm = side / 3f;
        upKey.rect.set(centerDx - arm / 2f, centerDy - side / 2f, centerDx + arm / 2f, centerDy);
        downKey.rect.set(centerDx - arm / 2f, centerDy, centerDx + arm / 2f, centerDy + side / 2f);
        leftKey.rect.set(centerDx - side / 2f, centerDy - arm / 2f, centerDx, centerDy + arm / 2f);
        rightKey.rect.set(centerDx, centerDy - arm / 2f, centerDx + side / 2f, centerDy + arm / 2f);
        selectKey.rect.set(centerDx - arm / 2f, centerDy - arm / 2f, centerDx + arm / 2f, centerDy + arm / 2f);

        // Dial pad: 4x3 grid between the d-pad and the right column.
        float dialLeft = innerLeft + dpadWidth + gap;
        float cellWidth = dialWidth / 3f;
        float cellHeight = innerHeight / 4f;
        float inset = Math.min(cellWidth, cellHeight) * 0.05f;
        for (int i = 0; i < DIAL_LABELS.length; i++) {
            int col = i % 3;
            int row = i / 3;
            KeyDef def = keysById.get(CTRL_DIAL_BASE + i);
            def.rect.set(
                    dialLeft + col * cellWidth + inset,
                    innerTop + row * cellHeight + inset,
                    dialLeft + (col + 1) * cellWidth - inset,
                    innerTop + (row + 1) * cellHeight - inset);
        }

        // Right column: soft keys, menu, clear.
        float rightLeft = innerRight - rightWidth;
        KeyDef[] column = {
                keysById.get(CTRL_SOFT1),
                keysById.get(CTRL_SOFT2),
                keysById.get(CTRL_MENU),
                keysById.get(CTRL_CLEAR),
        };
        for (int row = 0; row < column.length; row++) {
            column[row].rect.set(
                    rightLeft,
                    innerTop + row * cellHeight + inset,
                    innerRight,
                    innerTop + (row + 1) * cellHeight - inset);
        }
    }

    private void drawKeypad(Canvas canvas) {
        if (padRect.height() <= 0f) {
            return;
        }
        for (KeyDef def : keys) {
            drawKey(canvas, def);
        }
    }

    private void drawKey(Canvas canvas, KeyDef def) {
        RectF rect = def.rect;
        if (rect.width() <= 0f || rect.height() <= 0f) {
            return;
        }
        float radius = Math.min(rect.width(), rect.height()) * 0.18f;
        boolean pressed = activeControls.contains(def.id);
        if (pressed) {
            keyFillPaint.setColor(0x99FFFF90);
            keyStrokePaint.setColor(0xFFFFFFFF);
        } else {
            keyFillPaint.setColor(0x40FFFFFF);
            keyStrokePaint.setColor(0x99FFFFFF);
        }
        canvas.drawRoundRect(rect, radius, radius, keyFillPaint);
        canvas.drawRoundRect(rect, radius, radius, keyStrokePaint);
        keyTextPaint.setTextSize(Math.min(rect.height() * 0.42f, rect.width() * 0.5f));
        canvas.drawText(def.label, rect.centerX(),
                rect.centerY() + keyTextPaint.getTextSize() * 0.34f, keyTextPaint);
    }

    private int hitTest(float x, float y) {
        // The select key sits inside the d-pad cross, so probe it first.
        if (selectKey.rect.contains(x, y)) {
            return CTRL_SELECT;
        }
        for (KeyDef def : keys) {
            if (def.rect.contains(x, y)) {
                return def.id;
            }
        }
        return CTRL_NONE;
    }

    private int[] toCanvas(float x, float y, boolean clampToGame) {
        if (gamePixelW <= 0 || gamePixelH <= 0 || gameRect.width() <= 0f || gameRect.height() <= 0f) {
            return null;
        }
        float fx = (x - gameRect.left) * gamePixelW / gameRect.width();
        float fy = (y - gameRect.top) * gamePixelH / gameRect.height();
        if (!clampToGame && (fx < 0f || fy < 0f || fx >= gamePixelW || fy >= gamePixelH)) {
            return null;
        }
        int cx = (int) Math.floor(fx);
        int cy = (int) Math.floor(fy);
        cx = Math.max(0, Math.min(gamePixelW - 1, cx));
        cy = Math.max(0, Math.min(gamePixelH - 1, cy));
        return new int[]{cx, cy};
    }

    private void pressGamePointer(int pointerId, float x, float y) {
        int[] canvas = toCanvas(x, y, false);
        if (canvas == null || pointerFinger != null) {
            return;
        }
        pointerFinger = pointerId;
        lastPointerX = canvas[0];
        lastPointerY = canvas[1];
        input.pressPointer(canvas[0], canvas[1]);
    }

    private void moveGamePointer(int pointerId, float x, float y) {
        if (pointerFinger == null || pointerFinger != pointerId) {
            return;
        }
        int[] canvas = toCanvas(x, y, true);
        if (canvas == null) {
            return;
        }
        lastPointerX = canvas[0];
        lastPointerY = canvas[1];
        input.movePointer(canvas[0], canvas[1]);
    }

    private void releaseGamePointer(int pointerId, float x, float y) {
        if (pointerFinger == null || pointerFinger != pointerId) {
            return;
        }
        pointerFinger = null;
        int[] canvas = toCanvas(x, y, true);
        if (canvas == null) {
            canvas = new int[]{lastPointerX, lastPointerY};
        }
        input.releasePointer(canvas[0], canvas[1]);
    }

    private void releaseGamePointerNow() {
        if (pointerFinger == null) {
            return;
        }
        pointerFinger = null;
        input.releasePointer(lastPointerX, lastPointerY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        computeZones();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                int index = event.getActionIndex();
                int control = keypadVisible
                        ? hitTest(event.getX(index), event.getY(index))
                        : CTRL_NONE;
                if (control != CTRL_NONE) {
                    updatePointer(event.getPointerId(index), control);
                    return true;
                }
                if (pointerFinger == null && toCanvas(event.getX(index), event.getY(index), false) != null) {
                    pressGamePointer(event.getPointerId(index), event.getX(index), event.getY(index));
                    return true;
                }
                return false;
            }
            case MotionEvent.ACTION_MOVE: {
                boolean handled = false;
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int pointerId = event.getPointerId(i);
                    if (pointerControls.containsKey(pointerId)) {
                        updatePointer(pointerId, hitTest(event.getX(i), event.getY(i)));
                        handled = true;
                    } else if (pointerFinger != null && pointerId == pointerFinger) {
                        moveGamePointer(pointerId, event.getX(i), event.getY(i));
                        handled = true;
                    }
                }
                return handled;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int index = event.getActionIndex();
                int pointerId = event.getPointerId(index);
                if (pointerFinger != null && pointerId == pointerFinger) {
                    releaseGamePointer(pointerId, event.getX(index), event.getY(index));
                } else {
                    updatePointer(pointerId, CTRL_NONE);
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                int pointerId = event.getPointerId(0);
                if (pointerFinger != null && pointerId == pointerFinger) {
                    releaseGamePointer(pointerId, event.getX(0), event.getY(0));
                } else {
                    updatePointer(pointerId, CTRL_NONE);
                }
                performClick();
                return true;
            }
            case MotionEvent.ACTION_CANCEL: {
                releaseGamePointerNow();
                releasePointers();
                return true;
            }
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void updatePointer(int pointerId, int control) {
        Integer previous = pointerControls.put(pointerId, control);
        if (previous != null && previous == control) {
            return;
        }
        if (previous != null && previous != CTRL_NONE) {
            releaseControl(previous);
        }
        if (control != CTRL_NONE) {
            pressControl(control);
        }
    }

    private void releasePointers() {
        for (Integer control : pointerControls.values()) {
            if (control != null && control != CTRL_NONE) {
                releaseControl(control);
            }
        }
        pointerControls.clear();
    }

    private void pressControl(int control) {
        if (!activeControls.add(control)) {
            return;
        }
        KeyDef def = keysById.get(control);
        if (def != null) {
            if (def.soft) {
                input.pressSoft(def.code);
            } else {
                input.pressDojaKey(def.code);
            }
        }
        postInvalidate();
    }

    private void releaseControl(int control) {
        if (!activeControls.remove(control)) {
            return;
        }
        KeyDef def = keysById.get(control);
        if (def != null) {
            if (def.soft) {
                input.releaseSoft(def.code);
            } else {
                input.releaseDojaKey(def.code);
            }
        }
        postInvalidate();
    }

    public void releaseAllControls() {
        releaseGamePointerNow();
        releasePointers();
        input.releaseAll();
    }
}
