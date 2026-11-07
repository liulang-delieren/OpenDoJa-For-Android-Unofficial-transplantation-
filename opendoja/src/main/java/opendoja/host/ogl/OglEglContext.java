package opendoja.host.ogl;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimal EGL bootstrap for the hardware OGL backend: an OpenGL ES 1.x context rendered
 * into an offscreen pbuffer, read back into the software surface at frame boundaries.
 * A single context is shared across calling threads (the backend serializes access).
 */
final class OglEglContext {
    private static final AtomicInteger CONTEXT_SEQ = new AtomicInteger();

    private EGLDisplay display = EGL14.EGL_NO_DISPLAY;
    private EGLContext context = EGL14.EGL_NO_CONTEXT;
    private EGLSurface surface = EGL14.EGL_NO_SURFACE;
    private int surfaceWidth = -1;
    private int surfaceHeight = -1;
    private Thread boundThread;

    boolean ensure(int width, int height) {
        if (context != EGL14.EGL_NO_CONTEXT && surfaceWidth == width && surfaceHeight == height) {
            return true;
        }
        destroy();
        try {
            display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (display == EGL14.EGL_NO_DISPLAY) {
                return fail("eglGetDisplay");
            }
            int[] version = new int[2];
            if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
                return fail("eglInitialize");
            }
            int[] configAttribs = {
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES_BIT,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_DEPTH_SIZE, 24,
                    EGL14.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] configCount = new int[1];
            if (!EGL14.eglChooseConfig(display, configAttribs, 0, configs, 0, 1, configCount, 0)
                    || configCount[0] == 0) {
                return fail("eglChooseConfig");
            }
            EGLConfig config = configs[0];
            int[] contextAttribs = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 1,
                    EGL14.EGL_NONE
            };
            context = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, contextAttribs, 0);
            if (context == EGL14.EGL_NO_CONTEXT) {
                return fail("eglCreateContext es1");
            }
            int[] pbufferAttribs = {
                    EGL14.EGL_WIDTH, Math.max(1, width),
                    EGL14.EGL_HEIGHT, Math.max(1, height),
                    EGL14.EGL_NONE
            };
            surface = EGL14.eglCreatePbufferSurface(display, config, pbufferAttribs, 0);
            if (surface == EGL14.EGL_NO_SURFACE) {
                return fail("eglCreatePbufferSurface");
            }
            if (!EGL14.eglMakeCurrent(display, surface, surface, context)) {
                return fail("eglMakeCurrent");
            }
            boundThread = Thread.currentThread();
            surfaceWidth = width;
            surfaceHeight = height;
            return true;
        } finally {
            if (surfaceWidth != width || surfaceHeight != height) {
                // Leave a partially initialized context for destroy() on the next attempt.
            }
        }
    }

    /** Makes the context current on the calling thread, switching threads if needed. */
    boolean makeCurrent() {
        if (context == EGL14.EGL_NO_CONTEXT || surface == EGL14.EGL_NO_SURFACE) {
            return false;
        }
        if (boundThread == Thread.currentThread()) {
            return true;
        }
        if (boundThread != null) {
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            boundThread = null;
        }
        if (!EGL14.eglMakeCurrent(display, surface, surface, context)) {
            return false;
        }
        boundThread = Thread.currentThread();
        return true;
    }

    EGLDisplay display() {
        return display;
    }

    void destroy() {
        if (display != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            if (surface != EGL14.EGL_NO_SURFACE) {
                EGL14.eglDestroySurface(display, surface);
            }
            if (context != EGL14.EGL_NO_CONTEXT) {
                EGL14.eglDestroyContext(display, context);
            }
            EGL14.eglTerminate(display);
        }
        display = EGL14.EGL_NO_DISPLAY;
        context = EGL14.EGL_NO_CONTEXT;
        surface = EGL14.EGL_NO_SURFACE;
        surfaceWidth = -1;
        surfaceHeight = -1;
        boundThread = null;
    }

    int contextGeneration() {
        return CONTEXT_SEQ.get();
    }

    private static boolean fail(String what) {
        int error = EGL14.eglGetError();
        System.err.println("[opendoja] EGL " + what + " failed: 0x" + Integer.toHexString(error));
        return false;
    }
}
