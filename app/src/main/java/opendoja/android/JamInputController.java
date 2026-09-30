package opendoja.android;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Frame;

import opendoja.host.DesktopKeyInputAdapter;
import opendoja.host.DoJaRuntime;

/**
 * Bridges Android touch/key input into the DoJa runtime using the same
 * DesktopKeyInputAdapter press/release machinery the desktop host uses
 * (debounce, dedup, select latch all preserved).
 */
public final class JamInputController {
    private static final String TAG = "OpenDoJa";
    private static final int RELEASE_DEBOUNCE_MS = 50;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final DesktopKeyInputAdapter keyAdapter;
    private final DesktopKeyInputAdapter softAdapter;
    private int logCount;

    public JamInputController() {
        keyAdapter = new DesktopKeyInputAdapter(this::schedule,
                (dojaKey, eventType) -> {
                    DoJaRuntime runtime = DoJaRuntime.current();
                    if (runtime != null) {
                        runtime.dispatchSyntheticKey(dojaKey, eventType);
                    }
                },
                RELEASE_DEBOUNCE_MS);
        softAdapter = new DesktopKeyInputAdapter(this::schedule,
                (softKey, eventType) -> {
                    DoJaRuntime runtime = DoJaRuntime.current();
                    if (runtime != null) {
                        runtime.dispatchHostSoftKey(softKey, eventType);
                    }
                },
                RELEASE_DEBOUNCE_MS);
    }

    public void pressDojaKey(int dojaKey) {
        log("press key " + dojaKey);
        keyAdapter.keyPressed(dojaKey);
    }

    public void releaseDojaKey(int dojaKey) {
        keyAdapter.keyReleased(dojaKey);
    }

    public void pressSoft(int softKey) {
        log("press soft " + softKey);
        softAdapter.keyPressed(softKey);
    }

    public void releaseSoft(int softKey) {
        softAdapter.keyReleased(softKey);
    }

    public void releaseAll() {
        keyAdapter.releaseAll();
        softAdapter.releaseAll();
    }

    public void pressPointer(int canvasX, int canvasY) {
        log("pointer press " + canvasX + "," + canvasY);
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime != null) {
            runtime.dispatchPointerEvent(DoJaRuntime.TOUCH_PRESSED_EVENT, canvasX, canvasY);
        }
    }

    public void releasePointer(int canvasX, int canvasY) {
        log("pointer release " + canvasX + "," + canvasY);
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime != null) {
            runtime.dispatchPointerEvent(DoJaRuntime.TOUCH_RELEASED_EVENT, canvasX, canvasY);
        }
    }

    public void movePointer(int canvasX, int canvasY) {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime != null) {
            runtime.dispatchPointerEvent(Display.POINTER_MOVED_EVENT, canvasX, canvasY);
        }
    }

    private void log(String message) {
        if (logCount < 200 || logCount % 100 == 0) {
            Log.i(TAG, "input#" + logCount + " " + message);
        }
        logCount++;
    }

    private DesktopKeyInputAdapter.PendingRelease schedule(int delayMillis, Runnable task) {
        handler.postDelayed(task, delayMillis);
        return () -> handler.removeCallbacks(task);
    }
}
