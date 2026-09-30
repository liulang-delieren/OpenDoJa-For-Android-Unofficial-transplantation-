package com.nttdocomo.opt.ui.j3d2;

import com.nttdocomo.lang.XString;
import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.Image;
import com.nttdocomo.ui.ogl.ByteBuffer;
import com.nttdocomo.ui.ogl.DirectBufferFactory;
import com.nttdocomo.ui.ogl.GraphicsOGL;
import opendoja.host.OpenDoJaLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mitsubishi-specific j3d2/Z3D rendering facade backed by openDoJa's current
 * 2D and OpenGL-compatible graphics implementations.
 */
public final class C3DGraphics extends Graphics {
    private static final boolean DEBUG_3D = opendoja.host.OpenDoJaLaunchArgs.getBoolean(opendoja.host.OpenDoJaLaunchArgs.DEBUG3D);
    private static final boolean TRACE_3D_CALLS = opendoja.host.OpenDoJaLaunchArgs.getBoolean(opendoja.host.OpenDoJaLaunchArgs.DEBUG3D_CALLS);
    private static final int GL_QUADS = 0x0007;
    private static final int GL_QUAD_STRIP = 0x0008;
    private static final int GL_POLYGON = 0x0009;
    private static final int GL_CLAMP = 0x2900;
    private static final int GL_LIGHT_MODEL_TWO_SIDE = 0x0B52;
    private static final int GL_RGB4 = 0x804F;
    private static final int GL_RGB5 = 0x8050;
    private static final int GL_RGB8 = 0x8051;
    private static final int GL_RGBA4 = 0x8056;
    private static final int GL_RGB5_A1 = 0x8057;
    private static final int GL_RGBA8 = 0x8058;
    private static final int GL_SPHERE_MAP = 0x2402;
    private static final int[] CLEAR_MASKS = {
            0,
            GraphicsOGL.GL_COLOR_BUFFER_BIT,
            GraphicsOGL.GL_DEPTH_BUFFER_BIT,
            0,
            GraphicsOGL.GL_STENCIL_BUFFER_BIT
    };
    private static final int[] C3DL_TO_GL = {
            0,
            GraphicsOGL.GL_NEVER,
            GraphicsOGL.GL_ALWAYS,
            GraphicsOGL.GL_LESS,
            GraphicsOGL.GL_LEQUAL,
            GraphicsOGL.GL_EQUAL,
            GraphicsOGL.GL_GEQUAL,
            GraphicsOGL.GL_GREATER,
            GraphicsOGL.GL_NOTEQUAL,
            GraphicsOGL.GL_POINTS,
            GraphicsOGL.GL_LINES,
            GraphicsOGL.GL_LINE_STRIP,
            GraphicsOGL.GL_LINE_LOOP,
            GraphicsOGL.GL_TRIANGLES,
            GraphicsOGL.GL_TRIANGLE_STRIP,
            GraphicsOGL.GL_TRIANGLE_FAN,
            GL_QUADS,
            GL_QUAD_STRIP,
            GL_POLYGON,
            GraphicsOGL.GL_ZERO,
            GraphicsOGL.GL_ONE,
            GraphicsOGL.GL_SRC_COLOR,
            GraphicsOGL.GL_ONE_MINUS_SRC_COLOR,
            GraphicsOGL.GL_DST_COLOR,
            GraphicsOGL.GL_SRC_ALPHA,
            GraphicsOGL.GL_ONE_MINUS_SRC_ALPHA,
            GraphicsOGL.GL_ONE_MINUS_DST_COLOR,
            GraphicsOGL.GL_COLOR_BUFFER_BIT,
            GraphicsOGL.GL_DEPTH_BUFFER_BIT,
            GraphicsOGL.GL_STENCIL_BUFFER_BIT,
            GraphicsOGL.GL_TRUE_I,
            GraphicsOGL.GL_FALSE_I,
            GraphicsOGL.GL_FRONT_AND_BACK,
            GraphicsOGL.GL_AMBIENT,
            GraphicsOGL.GL_DIFFUSE,
            GraphicsOGL.GL_AMBIENT_AND_DIFFUSE,
            GraphicsOGL.GL_SPECULAR,
            GraphicsOGL.GL_EMISSION,
            GraphicsOGL.GL_BLEND,
            GraphicsOGL.GL_ALPHA_TEST,
            GraphicsOGL.GL_COLOR_MATERIAL,
            GraphicsOGL.GL_CULL_FACE,
            GraphicsOGL.GL_DEPTH_TEST,
            GraphicsOGL.GL_DITHER,
            GraphicsOGL.GL_FOG,
            GraphicsOGL.GL_LIGHT0,
            GraphicsOGL.GL_LIGHT1,
            GraphicsOGL.GL_LIGHT2,
            GraphicsOGL.GL_LIGHT3,
            GraphicsOGL.GL_LIGHT4,
            GraphicsOGL.GL_LIGHT5,
            GraphicsOGL.GL_LIGHT6,
            GraphicsOGL.GL_LIGHT7,
            GraphicsOGL.GL_LIGHTING,
            GraphicsOGL.GL_LINE_SMOOTH,
            GraphicsOGL.GL_SCISSOR_TEST,
            GraphicsOGL.GL_STENCIL_TEST,
            GraphicsOGL.GL_TEXTURE_2D,
            0x0C00,
            0x0C01,
            GraphicsOGL.GL_FOG_START,
            GraphicsOGL.GL_FOG_END,
            GraphicsOGL.GL_FOG_COLOR,
            GraphicsOGL.GL_CW,
            GraphicsOGL.GL_CCW,
            GraphicsOGL.GL_VENDOR,
            GraphicsOGL.GL_VERSION,
            GraphicsOGL.GL_POSITION,
            GraphicsOGL.GL_SPOT_CUTOFF,
            GraphicsOGL.GL_SPOT_DIRECTION,
            GraphicsOGL.GL_SPOT_EXPONENT,
            GraphicsOGL.GL_CONSTANT_ATTENUATION,
            GraphicsOGL.GL_LINEAR_ATTENUATION,
            GraphicsOGL.GL_SHININESS,
            GraphicsOGL.GL_MODELVIEW,
            GraphicsOGL.GL_PROJECTION,
            GraphicsOGL.GL_TEXTURE,
            0x1300,
            0,
            0,
            GraphicsOGL.GL_KEEP,
            GraphicsOGL.GL_REPLACE,
            GraphicsOGL.GL_INCR,
            GraphicsOGL.GL_DECR,
            GraphicsOGL.GL_INVERT,
            GraphicsOGL.GL_TEXTURE_ENV,
            GraphicsOGL.GL_TEXTURE_ENV_COLOR,
            GraphicsOGL.GL_TEXTURE_ENV_MODE,
            GraphicsOGL.GL_MODULATE,
            GraphicsOGL.GL_DECAL,
            0x2501,
            0x2502,
            0x2500,
            0x2401,
            0x2400,
            0x2000,
            0x2001,
            GraphicsOGL.GL_RGB,
            GraphicsOGL.GL_RGBA,
            GL_RGB4,
            GL_RGB5,
            GL_RGB8,
            GL_RGBA4,
            GL_RGBA8,
            GL_RGB5_A1,
            GraphicsOGL.GL_UNSIGNED_BYTE,
            GraphicsOGL.GL_TEXTURE_MIN_FILTER,
            GraphicsOGL.GL_TEXTURE_MAG_FILTER,
            GraphicsOGL.GL_TEXTURE_WRAP_S,
            GraphicsOGL.GL_TEXTURE_WRAP_T,
            GraphicsOGL.GL_NEAREST,
            GraphicsOGL.GL_LINEAR,
            GL_CLAMP,
            GraphicsOGL.GL_REPEAT,
            GraphicsOGL.GL_NO_ERROR,
            GraphicsOGL.GL_INVALID_VALUE,
            GraphicsOGL.GL_INVALID_OPERATION,
            GraphicsOGL.GL_OUT_OF_MEMORY,
            GL_SPHERE_MAP,
            0x81F8,
            0x81FA,
            0x81F9,
            GraphicsOGL.GL_FOG_MODE,
            GraphicsOGL.GL_EXP,
            GraphicsOGL.GL_EXP2,
            GraphicsOGL.GL_FOG_DENSITY
    };

    private final C3DCanvas owner;
    private final DirectBufferFactory bufferFactory = DirectBufferFactory.getFactory();
    private final Map<Integer, List<Command>> displayLists = new HashMap<>();
    private final Map<Integer, Integer> textures = new HashMap<>();
    private final Set<Integer> enabledCaps = new HashSet<>();
    private Graphics graphics;
    private boolean borrowedOwnerGraphics;
    private int borrowedOwnerWidth = -1;
    private int borrowedOwnerHeight = -1;
    private boolean locked;
    private boolean drawSessionActive;
    private int frontFace = GraphicsOGL.GL_CCW;
    private int currentMatrixMode = GraphicsOGL.GL_MODELVIEW;
    private int currentBeginMode = -1;
    private final ArrayList<ImmediateVertex> currentVertices = new ArrayList<>();
    private final float[] currentColor = {1f, 1f, 1f, 1f};
    private final float[] currentNormal = {0f, 0f, 1f};
    private final float[] currentTexCoord = {0f, 0f};
    private int softwareColor = Graphics.BLACK;
    private Font softwareFont = Font.getDefaultFont();
    private int softwareOriginX;
    private int softwareOriginY;
    private Integer recordingListId;
    private ArrayList<Command> recordingCommands;
    private int figureTextureName = -1;
    private int frameImmediateDraws;
    private int frameImmediateVertices;
    private int frameFigureRenders;
    private int frameDisplayListCalls;
    private int frameTextureUploads;
    private int frameActionApplications;

    C3DGraphics(C3DCanvas owner) {
        this.owner = owner;
    }

    void attach(Graphics graphics) {
        releaseBorrowedGraphics(graphics);
        this.graphics = graphics;
        this.borrowedOwnerGraphics = false;
        this.borrowedOwnerWidth = -1;
        this.borrowedOwnerHeight = -1;
        if (TRACE_3D_CALLS) {
            OpenDoJaLog.debug(C3DGraphics.class, () -> "C3D attach graphics=" + graphics.getClass().getName());
        }
    }

    @Override
    public void lock() {
        resetFrameCounters();
        borrowOwnerGraphicsIfNeeded();
        Graphics target = attachedGraphics();
        if (target != null) {
            target.lock();
            replaySoftwareState(target);
        } else {
            super.lock();
        }
        locked = true;
        trace3d("lock");
    }

    @Override
    public void unlock(boolean present) {
        if (drawSessionActive) {
            graphics.endDrawing();
            drawSessionActive = false;
        }
        Graphics target = attachedGraphics();
        if (!locked) {
            if (present) {
                if (target != null) {
                    target.flush();
                } else {
                    super.flush();
                }
            }
            return;
        }
        if (target != null) {
            target.unlock(present);
        } else {
            super.unlock(present);
        }
        logFrameSummary(present);
        locked = false;
        if (borrowedOwnerGraphics) {
            releaseBorrowedGraphics(null);
        }
    }

    @Override
    public void beginDrawing() {
        Graphics target = attachedGraphics();
        if (target != null) {
            target.beginDrawing();
        } else {
            super.beginDrawing();
        }
    }

    @Override
    public void endDrawing() {
        Graphics target = attachedGraphics();
        if (target != null) {
            target.endDrawing();
        } else {
            super.endDrawing();
        }
    }

    @Override
    public void clearClip() {
        Graphics target = attachedGraphics();
        if (target != null) {
            target.clearClip();
        } else {
            super.clearClip();
        }
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        Graphics target = attachedGraphics();
        if (target != null) {
            target.clipRect(x, y, width, height);
        } else {
            super.clipRect(x, y, width, height);
        }
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        Graphics target = attachedGraphics();
        if (target != null) {
            target.setClip(x, y, width, height);
        } else {
            super.setClip(x, y, width, height);
        }
    }

    @Override
    public void setOrigin(int x, int y) {
        softwareOriginX = x;
        softwareOriginY = y;
        Graphics target = attachedGraphics();
        if (target != null) {
            target.setOrigin(x, y);
        } else {
            super.setOrigin(x, y);
        }
    }

    public void set3DMode(boolean enabled) {
        trace3d("set3DMode enabled=" + enabled);
        if (!enabled && drawSessionActive) {
            ensureGraphics();
            graphics.endDrawing();
            drawSessionActive = false;
        }
    }

    @Override
    public void setColor(int color) {
        softwareColor = normalizeSoftwareColor(color);
        Graphics target = attachedGraphics();
        if (target != null) {
            target.setColor(softwareColor);
        } else {
            super.setColor(softwareColor);
        }
    }

    @Override
    public void setFont(Font font) {
        if (font != null) {
            softwareFont = font;
        }
        Graphics target = attachedGraphics();
        if (target != null) {
            target.setFont(font);
        } else {
            super.setFont(font);
        }
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        Graphics target = attachedGraphics();
        if (target != null) {
            target.fillRect(x, y, width, height);
        } else {
            super.fillRect(x, y, width, height);
        }
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        Graphics target = attachedGraphics();
        if (target != null) {
            target.clearRect(x, y, width, height);
        } else {
            super.clearRect(x, y, width, height);
        }
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
        Graphics target = attachedGraphics();
        if (target != null) {
            target.drawRect(x, y, width, height);
        } else {
            super.drawRect(x, y, width, height);
        }
    }

    @Override
    public void drawString(String text, int x, int y) {
        Graphics target = attachedGraphics();
        if (target != null) {
            target.drawString(text, x, y);
        } else {
            super.drawString(text, x, y);
        }
    }

    @Override
    public void drawString(XString text, int x, int y) {
        Graphics target = attachedGraphics();
        if (target != null) {
            target.drawString(text, x, y);
        } else {
            super.drawString(text, x, y);
        }
    }

    @Override
    public void drawString(XString text, int x, int y, int offset, int length) {
        Graphics target = attachedGraphics();
        if (target != null) {
            target.drawString(text, x, y, offset, length);
        } else {
            super.drawString(text, x, y, offset, length);
        }
    }

    @Override
    public void drawImage(Image image, int x, int y) {
        Graphics target = attachedGraphics();
        if (target != null) {
            target.drawImage(image, x, y);
        } else {
            super.drawImage(image, x, y);
        }
    }

    @Override
    public void drawImage(Image image, int x, int y, int sx, int sy, int width, int height) {
        Graphics target = attachedGraphics();
        if (target != null) {
            target.drawImage(image, x, y, sx, sy, width, height);
        } else {
            super.drawImage(image, x, y, sx, sy, width, height);
        }
    }

    public void clear(int mask) {
        ensure3D();
        graphics.glClear(clearMask(mask));
    }

    public void clear(int maskA, int maskB) {
        ensure3D();
        if (maskA >= 0 && maskA < CLEAR_MASKS.length) {
            graphics.glClear(CLEAR_MASKS[maskA]);
        }
        if (maskB >= 0 && maskB < CLEAR_MASKS.length) {
            graphics.glClear(CLEAR_MASKS[maskB]);
        }
    }

    public void clearColor(int r, int g, int b, int a) {
        recordOrRun(gfx -> gfx.clearColor(r, g, b, a), () -> {
            ensure3D();
            graphics.glClearColor(C3DData.fixedToFloat(r), C3DData.fixedToFloat(g), C3DData.fixedToFloat(b), C3DData.fixedToFloat(a));
        });
    }

    public void clearDepth(int depth) {
        recordOrRun(gfx -> gfx.clearDepth(depth), () -> {
            ensure3D();
            graphics.glClearDepthf((float) C3DData.fixedToDouble(depth));
        });
    }

    public void clearStencil(int stencil) {
        recordOrRun(gfx -> gfx.clearStencil(stencil), () -> {
            ensure3D();
            graphics.glClearStencil(stencil);
        });
    }

    public void alphaFunc(int func, int ref) {
        recordOrRun(gfx -> gfx.alphaFunc(func, ref), () -> {
            ensure3D();
            graphics.glAlphaFunc(mapCompareFunction(func), C3DData.fixedToFloat(ref));
        });
    }

    public void begin(int mode) {
        if (recordingCommands != null) {
            recordingCommands.add(gfx -> gfx.begin(mode));
            return;
        }
        currentBeginMode = mapPrimitiveMode(mode);
        currentVertices.clear();
        trace3dCalls("begin mode=" + mode + " glMode=0x" + Integer.toHexString(currentBeginMode));
    }

    public void end() {
        if (recordingCommands != null) {
            recordingCommands.add(C3DGraphics::end);
            return;
        }
        if (currentBeginMode < 0) {
            return;
        }
        ensure3D();
        trace3dCalls("end mode=0x" + Integer.toHexString(currentBeginMode) + " vertices=" + currentVertices.size());
        renderImmediate(currentBeginMode, currentVertices);
        currentVertices.clear();
        currentBeginMode = -1;
    }

    public void vertex(int x, int y, int z) {
        if (recordingCommands != null) {
            recordingCommands.add(gfx -> gfx.vertex(x, y, z));
            return;
        }
        if (currentBeginMode < 0) {
            return;
        }
        currentVertices.add(new ImmediateVertex(
                C3DData.fixedToFloat(x),
                C3DData.fixedToFloat(y),
                C3DData.fixedToFloat(z),
                currentNormal[0],
                currentNormal[1],
                currentNormal[2],
                currentTexCoord[0],
                currentTexCoord[1],
                currentColor[0],
                currentColor[1],
                currentColor[2],
                currentColor[3]
        ));
    }

    public void vertexv(int[] value) {
        if (value == null || value.length < 3) {
            throw new IllegalArgumentException("value");
        }
        vertex(value[0], value[1], value[2]);
    }

    public void color(int r, int g, int b, int a) {
        recordOrRun(gfx -> gfx.color(r, g, b, a), () -> {
            currentColor[0] = C3DData.fixedToFloat(r);
            currentColor[1] = C3DData.fixedToFloat(g);
            currentColor[2] = C3DData.fixedToFloat(b);
            currentColor[3] = C3DData.fixedToFloat(a);
            ensure3D();
            graphics.glColor4f(currentColor[0], currentColor[1], currentColor[2], currentColor[3]);
        });
    }

    public void color(int[] value) {
        if (value == null || value.length < 3) {
            throw new IllegalArgumentException("value");
        }
        color(
                value[0],
                value[1],
                value[2],
                value.length >= 4 ? value[3] : 65536
        );
    }

    public void colorv(int[] value) {
        if (value == null || value.length < 4) {
            throw new IllegalArgumentException("value");
        }
        color(value[0], value[1], value[2], value[3]);
    }

    public void normal(int x, int y, int z) {
        recordOrRun(gfx -> gfx.normal(x, y, z), () -> {
            currentNormal[0] = C3DData.fixedToFloat(x);
            currentNormal[1] = C3DData.fixedToFloat(y);
            currentNormal[2] = C3DData.fixedToFloat(z);
        });
    }

    public void normalv(int[] value) {
        if (value == null || value.length < 3) {
            throw new IllegalArgumentException("value");
        }
        normal(value[0], value[1], value[2]);
    }

    public void texCoord(int s, int t) {
        recordOrRun(gfx -> gfx.texCoord(s, t), () -> {
            currentTexCoord[0] = C3DData.fixedToFloat(s);
            currentTexCoord[1] = C3DData.fixedToFloat(t);
        });
    }

    public void texCoordv(int[] value) {
        if (value == null || value.length < 2) {
            throw new IllegalArgumentException("value");
        }
        texCoord(value[0], value[1]);
    }

    public void matrixMode(int mode) {
        recordOrRun(gfx -> gfx.matrixMode(mode), () -> {
            ensure3D();
            setMatrixMode(mapMatrixMode(mode));
        });
    }

    public void loadIdentity() {
        recordOrRun(C3DGraphics::loadIdentity, () -> {
            ensure3D();
            graphics.glLoadIdentity();
        });
    }

    public void loadMatrix(int[] matrix) {
        if (matrix == null || matrix.length < 16) {
            throw new IllegalArgumentException("matrix");
        }
        recordOrRun(gfx -> gfx.loadMatrix(matrix), () -> {
            ensure3D();
            graphics.glLoadMatrixf(toFloatMatrix(matrix));
        });
    }

    public void multiMatrix(int[] matrix) {
        if (matrix == null || matrix.length < 16) {
            throw new IllegalArgumentException("matrix");
        }
        recordOrRun(gfx -> gfx.multiMatrix(matrix), () -> {
            ensure3D();
            graphics.glMultMatrixf(toFloatMatrix(matrix));
        });
    }

    public void lookAt(int eyeX, int eyeY, int eyeZ, int centerX, int centerY, int centerZ, int upX, int upY, int upZ) {
        recordOrRun(gfx -> gfx.lookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ), () -> {
            ensure3D();
            setMatrixMode(GraphicsOGL.GL_MODELVIEW);
            graphics.glLoadIdentity();
            trace3dCalls("lookAt eye=(" + fixed3(eyeX) + "," + fixed3(eyeY) + "," + fixed3(eyeZ)
                    + ") center=(" + fixed3(centerX) + "," + fixed3(centerY) + "," + fixed3(centerZ)
                    + ") up=(" + fixed3(upX) + "," + fixed3(upY) + "," + fixed3(upZ) + ")");
            graphics.glMultMatrixf(lookAtMatrix(
                    C3DData.fixedToFloat(eyeX), C3DData.fixedToFloat(eyeY), C3DData.fixedToFloat(eyeZ),
                    C3DData.fixedToFloat(centerX), C3DData.fixedToFloat(centerY), C3DData.fixedToFloat(centerZ),
                    C3DData.fixedToFloat(upX), C3DData.fixedToFloat(upY), C3DData.fixedToFloat(upZ)));
        });
    }

    public void perspective(int fovy, int aspect, int zNear, int zFar) {
        recordOrRun(gfx -> gfx.perspective(fovy, aspect, zNear, zFar), () -> {
            ensure3D();
            float near = C3DData.fixedToFloat(zNear);
            float top = (float) (near * java.lang.Math.tan(C3DData.fixedDegreesToRadians(fovy) * 0.5));
            float right = top * C3DData.fixedToFloat(aspect);
            setMatrixMode(GraphicsOGL.GL_PROJECTION);
            graphics.glLoadIdentity();
            trace3dCalls("perspective fovy=" + fixed3(fovy) + " aspect=" + fixed3(aspect)
                    + " near=" + fixed3(zNear) + " far=" + fixed3(zFar));
            graphics.glFrustumf(-right, right, -top, top, near, C3DData.fixedToFloat(zFar));
            setMatrixMode(GraphicsOGL.GL_MODELVIEW);
            graphics.glLoadIdentity();
        });
    }

    public void pushMatrix() {
        recordOrRun(C3DGraphics::pushMatrix, () -> {
            ensure3D();
            graphics.glPushMatrix();
        });
    }

    public void popMatrix() {
        recordOrRun(C3DGraphics::popMatrix, () -> {
            ensure3D();
            graphics.glPopMatrix();
        });
    }

    public void translate(int x, int y, int z) {
        recordOrRun(gfx -> gfx.translate(x, y, z), () -> {
            ensure3D();
            graphics.glTranslatef(C3DData.fixedToFloat(x), C3DData.fixedToFloat(y), C3DData.fixedToFloat(z));
        });
    }

    public void rotate(int angle, int x, int y, int z) {
        recordOrRun(gfx -> gfx.rotate(angle, x, y, z), () -> {
            ensure3D();
            graphics.glRotatef(C3DData.fixedToFloat(angle), C3DData.fixedToFloat(x), C3DData.fixedToFloat(y), C3DData.fixedToFloat(z));
        });
    }

    public void scale(int x, int y, int z) {
        recordOrRun(gfx -> gfx.scale(x, y, z), () -> {
            ensure3D();
            graphics.glScalef(C3DData.fixedToFloat(x), C3DData.fixedToFloat(y), C3DData.fixedToFloat(z));
        });
    }

    public void enable(int cap) {
        recordOrRun(gfx -> gfx.enable(cap), () -> {
            ensure3D();
            int glCap = mapCapability(cap);
            if (glCap != 0) {
                enabledCaps.add(glCap);
                graphics.glEnable(glCap);
            }
        });
    }

    public void disable(int cap) {
        recordOrRun(gfx -> gfx.disable(cap), () -> {
            ensure3D();
            int glCap = mapCapability(cap);
            if (glCap != 0) {
                enabledCaps.remove(glCap);
                graphics.glDisable(glCap);
            }
        });
    }

    public void colorMaterial(int face, int mode) {
        recordOrRun(gfx -> gfx.colorMaterial(face, mode), () -> {
            if (mode == 0) {
                return;
            }
            if (enabledCaps.contains(GraphicsOGL.GL_COLOR_MATERIAL)) {
                ensure3D();
                graphics.glColor4f(currentColor[0], currentColor[1], currentColor[2], currentColor[3]);
            }
        });
    }

    public void depthMask(int enabled) {
        recordOrRun(gfx -> gfx.depthMask(enabled), () -> {
            ensure3D();
            graphics.glDepthMask(mapEnum(enabled) != 0);
        });
    }

    public void depthFunc(int func) {
        recordOrRun(gfx -> gfx.depthFunc(func), () -> {
            ensure3D();
            graphics.glDepthFunc(mapCompareFunction(func));
        });
    }

    public void blendFunc(int src, int dst) {
        recordOrRun(gfx -> gfx.blendFunc(src, dst), () -> {
            ensure3D();
            graphics.glBlendFunc(mapEnum(src), mapEnum(dst));
        });
    }

    public void fog(int pname, int value) {
        recordOrRun(gfx -> gfx.fog(pname, value), () -> {
            ensure3D();
            int glPname = mapEnum(pname);
            if (glPname == GraphicsOGL.GL_FOG_MODE) {
                graphics.glFogf(glPname, mapEnum(value));
            } else {
                graphics.glFogf(glPname, C3DData.fixedToFloat(value));
            }
        });
    }

    public void fogv(int pname, int[] values) {
        if (values == null) {
            throw new NullPointerException("values");
        }
        recordOrRun(gfx -> gfx.fogv(pname, values), () -> {
            ensure3D();
            graphics.glFogfv(mapEnum(pname), toFloatArray(values));
        });
    }

    public void light(int light, int pname, int[] values) {
        if (values == null) {
            throw new NullPointerException("values");
        }
        recordOrRun(gfx -> gfx.light(light, pname, values), () -> {
            ensure3D();
            int glLight = mapLight(light);
            int glPname = mapLightParameter(pname);
            if (values.length == 1) {
                graphics.glLightf(glLight, glPname, C3DData.fixedToFloat(values[0]));
            } else {
                graphics.glLightfv(glLight, glPname, toFloatArray(values));
            }
        });
    }

    public void material(int face, int pname, int[] values) {
        if (values == null) {
            throw new NullPointerException("values");
        }
        recordOrRun(gfx -> gfx.material(face, pname, values), () -> {
            ensure3D();
            int glFace = mapMaterialFace(face);
            int glPname = mapMaterialParameter(pname);
            if (values.length == 1) {
                graphics.glMaterialf(glFace, glPname, C3DData.fixedToFloat(values[0]));
            } else {
                graphics.glMaterialfv(glFace, glPname, toFloatArray(values));
            }
        });
    }

    public void lightModeli(int pname, int value) {
        recordOrRun(gfx -> gfx.lightModeli(pname, value), () -> {
            ensure3D();
            graphics.glLightModelf(mapLightModelParameter(pname), mapLightModelValue(value));
        });
    }

    public void frontFace(int mode) {
        recordOrRun(gfx -> gfx.frontFace(mode), () -> {
            ensure3D();
            frontFace = mapFrontFaceMode(mode);
            graphics.glFrontFace(frontFace);
        });
    }

    public int getError() {
        ensure3D();
        return graphics.glGetError();
    }

    public String getString(int name) {
        ensure3D();
        return graphics.glGetString(mapEnum(name));
    }

    public void bindTexture(int target, int textureId) {
        recordOrRun(gfx -> gfx.bindTexture(target, textureId), () -> {
            ensure3D();
            graphics.glBindTexture(mapTextureTarget(target), textureName(textureId));
        });
    }

    public void texImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, byte[] data) {
        if (data == null) {
            throw new NullPointerException("data");
        }
        recordOrRun(gfx -> gfx.texImage2D(target, level, internalFormat, width, height, border, format, type, data), () -> {
            ensure3D();
            int pixelFormat = inferTextureUploadFormat(width, height, data, format);
            int normalizedInternalFormat = normalizeInternalFormat(mapEnum(internalFormat));
            byte[] uploadData = normalizeTextureUploadData(width, height, data, pixelFormat);
            ByteBuffer pixels = bufferFactory.allocateByteBuffer(uploadData);
            graphics.glTexImage2D(mapTextureTarget(target), level, normalizedInternalFormat,
                    width, height, border, pixelFormat, mapTexturePixelType(type), pixels);
        });
    }

    public void texSubImage2D0(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, byte[] data) {
        if (data == null) {
            throw new NullPointerException("data");
        }
        recordOrRun(gfx -> gfx.texSubImage2D0(target, level, xoffset, yoffset, width, height, format, type, data), () -> {
            ensure3D();
            int pixelFormat = inferTextureUploadFormat(width, height, data, format);
            byte[] uploadData = normalizeTextureUploadData(width, height, data, pixelFormat);
            ByteBuffer pixels = bufferFactory.allocateByteBuffer(uploadData);
            graphics.glTexSubImage2D(mapTextureTarget(target), level, xoffset, yoffset, width, height, pixelFormat, mapTexturePixelType(type), pixels);
        });
    }

    public void texParameter(int target, int pname, int param) {
        recordOrRun(gfx -> gfx.texParameter(target, pname, param), () -> {
            ensure3D();
            graphics.glTexParameteri(
                    mapTextureTarget(target),
                    mapTextureParameterName(pname),
                    mapTextureParameterValue(param));
        });
    }

    public void texEnvf(int target, int pname, int param) {
        recordOrRun(gfx -> gfx.texEnvf(target, pname, param), () -> {
            ensure3D();
            graphics.glTexEnvf(mapShiftedEnum(target), mapShiftedEnum(pname), C3DData.fixedToFloat(param));
        });
    }

    public void texEnvi(int target, int pname, int param) {
        recordOrRun(gfx -> gfx.texEnvi(target, pname, param), () -> {
            ensure3D();
            graphics.glTexEnvi(mapShiftedEnum(target), mapShiftedEnum(pname), normalizeTextureParameter(mapShiftedEnum(param)));
        });
    }

    public void texGenf(int coord, int pname, int param) {
        recordOrRun(gfx -> gfx.texGenf(coord, pname, param), () -> {
        });
    }

    public void texGeni0(int coord, int pname, int param) {
        recordOrRun(gfx -> gfx.texGeni0(coord, pname, param), () -> {
        });
    }

    public void colorMask(int r, int g, int b, int a) {
        recordOrRun(gfx -> gfx.colorMask(r, g, b, a), () -> {
            ensure3D();
            graphics.glColorMask(mapEnum(r) != 0, mapEnum(g) != 0, mapEnum(b) != 0, mapEnum(a) != 0);
        });
    }

    public void scissorMethod(int x, int y, int width, int height) {
        recordOrRun(gfx -> gfx.scissorMethod(x, y, width, height), () -> {
            ensure3D();
            graphics.glScissor(x, y, width, height);
        });
    }

    public void stencilFunc(int func, int ref, int mask) {
        recordOrRun(gfx -> gfx.stencilFunc(func, ref, mask), () -> {
            ensure3D();
            graphics.glStencilFunc(mapEnum(func), ref, mask);
        });
    }

    public void stencilMask(int mask) {
        recordOrRun(gfx -> gfx.stencilMask(mask), () -> {
            ensure3D();
            graphics.glStencilMask(mask);
        });
    }

    public void stencilOp(int fail, int zFail, int zPass) {
        recordOrRun(gfx -> gfx.stencilOp(fail, zFail, zPass), () -> {
            ensure3D();
            graphics.glStencilOp(mapEnum(fail), mapEnum(zFail), mapEnum(zPass));
        });
    }

    public void viewport(int x, int y, int width, int height) {
        recordOrRun(gfx -> gfx.viewport(x, y, width, height), () -> {
            ensure3D();
            trace3dCalls("viewport x=" + x + " y=" + y + " w=" + width + " h=" + height);
            graphics.glViewport(x, y, width, height);
        });
    }

    public void finish() {
        ensure3D();
        graphics.glFlush();
    }

    @Override
    public void flush() {
        ensure3D();
        graphics.glFlush();
        graphics.flush();
    }

    public void newList(int id, int mode) {
        if (recordingCommands != null) {
            throw new IllegalStateException("Nested display-list compilation is not supported");
        }
        recordingListId = id;
        recordingCommands = new ArrayList<>();
        trace3d("newList id=" + id + " mode=" + mode);
    }

    public void endList() {
        if (recordingListId == null || recordingCommands == null) {
            return;
        }
        int recordedId = recordingListId;
        int commandCount = recordingCommands.size();
        displayLists.put(recordingListId, List.copyOf(recordingCommands));
        recordingListId = null;
        recordingCommands = null;
        trace3d("endList id=" + recordedId + " commands=" + commandCount);
    }

    public void deleteLists(int id) {
        displayLists.remove(id);
        trace3d("deleteLists id=" + id);
    }

    public void deleteLists(int id, int range) {
        if (range <= 0) {
            trace3d("deleteLists id=" + id + " range=" + range + " skipped");
            return;
        }
        for (int current = id; current < id + range; current++) {
            displayLists.remove(current);
        }
        trace3d("deleteLists id=" + id + " range=" + range);
    }

    public void deleteTextures(int[] ids) {
        if (ids == null) {
            throw new NullPointerException("ids");
        }
        ensure3D();
        int[] actual = new int[ids.length];
        int count = 0;
        for (int id : ids) {
            Integer actualId = textures.remove(id);
            if (actualId != null) {
                actual[count++] = actualId;
            }
        }
        if (count > 0) {
            int[] trimmed = new int[count];
            System.arraycopy(actual, 0, trimmed, 0, count);
            graphics.glDeleteTextures(trimmed);
        }
    }

    public void deleteTextures(int count, int[] ids) {
        if (ids == null) {
            throw new NullPointerException("ids");
        }
        if (count <= 0) {
            return;
        }
        if (count >= ids.length) {
            deleteTextures(ids);
            return;
        }
        int[] trimmed = new int[count];
        System.arraycopy(ids, 0, trimmed, 0, count);
        deleteTextures(trimmed);
    }

    public void callList(int id) {
        if (recordingCommands != null) {
            recordingCommands.add(gfx -> gfx.callList(id));
            return;
        }
        List<Command> commands = displayLists.get(id);
        if (commands == null) {
            trace3d("callList missing id=" + id);
            return;
        }
        frameDisplayListCalls++;
        trace3dCalls("callList id=" + id + " commands=" + commands.size());
        for (Command command : commands) {
            command.execute(this);
        }
    }

    public void callList(C3DFigure figure) {
        if (figure == null) {
            return;
        }
        if (recordingCommands != null) {
            recordingCommands.add(gfx -> gfx.callList(figure));
            return;
        }
        frameFigureRenders++;
        trace3d("callList figure nodes=" + figure.data().nodes().size()
                + " coordSets=" + figure.data().coordSets().size()
                + " textureSet=" + (figure.texture() != null)
                + " actionTable=" + (figure.actionTable() != null)
                + " action=" + figure.action()
                + " timeMs=" + figure.getAnimationTime());
        renderFigure(figure);
    }

    public void sphere() {
        if (recordingCommands != null) {
            recordingCommands.add(C3DGraphics::sphere);
            return;
        }
        ensure3D();
        int slices = 40;
        int stacks = 40;
        for (int stack = 0; stack < stacks; stack++) {
            float v0 = stack / (float) stacks;
            float v1 = (stack + 1) / (float) stacks;
            float phi0 = (float) java.lang.Math.PI * (v0 - 0.5f);
            float phi1 = (float) java.lang.Math.PI * (v1 - 0.5f);
            ArrayList<ImmediateVertex> strip = new ArrayList<>();
            for (int slice = 0; slice <= slices; slice++) {
                float u = slice / (float) slices;
                float theta = (float) (u * java.lang.Math.PI * 2.0);
                strip.add(sphereVertex(theta, phi0, u, v0));
                strip.add(sphereVertex(theta, phi1, u, v1));
            }
            renderImmediate(GraphicsOGL.GL_TRIANGLE_STRIP, strip);
        }
    }

    private ImmediateVertex sphereVertex(float theta, float phi, float u, float v) {
        float cosPhi = (float) java.lang.Math.cos(phi);
        float sinPhi = (float) java.lang.Math.sin(phi);
        float cosTheta = (float) java.lang.Math.cos(theta);
        float sinTheta = (float) java.lang.Math.sin(theta);
        float x = cosPhi * cosTheta;
        float y = -sinPhi;
        float z = cosPhi * sinTheta;
        return new ImmediateVertex(
                x, y, z,
                x, y, z,
                u, v,
                currentColor[0], currentColor[1], currentColor[2], currentColor[3]
        );
    }

    private void renderFigure(C3DFigure figure) {
        ensure3D();
        boolean textureEnabled = enabledCaps.contains(GraphicsOGL.GL_TEXTURE_2D);
        boolean cullFaceEnabled = enabledCaps.contains(GraphicsOGL.GL_CULL_FACE);
        int coordSetCount = figure.data().coordSets().size();
        boolean preserveCullFace = figure.actionTable() != null && coordSetCount <= 24;
        boolean preserveFrontFace = figure.actionTable() != null;
        int savedFrontFace = frontFace;
        if (cullFaceEnabled && !preserveCullFace) {
            // Large Mitsubishi stage figures still have inconsistent winding in
            // the host pipeline. Disable culling for those passes so world
            // geometry is not dropped while we preserve the caller's state.
            disable(GraphicsOGL.GL_CULL_FACE);
        }
        if (!preserveFrontFace) {
            frontFace(GraphicsOGL.GL_CW);
        }
        disable(GraphicsOGL.GL_TEXTURE_2D);
        pushMatrix();
        C3DData.TextureData textures = figure.texture() == null ? null : figure.texture().data();
        C3DData.ActionTableData actionTable = figure.actionTable() == null ? null : figure.actionTable().data();
        float[] coordSet = null;
        boolean noMaterial = true;
        ArrayList<TextureTransform> textureStack = new ArrayList<>();
        textureStack.add(new TextureTransform());
        int shapeCount = 0;
        int texturedShapeCount = 0;
        int skippedShapesWithoutCoords = 0;
        for (C3DData.FigureNode node : figure.data().nodes()) {
            if (node instanceof C3DData.MaterialNode material) {
                noMaterial = false;
                useMaterial(material);
                continue;
            }
            if (node instanceof C3DData.TextureBindingNode texture) {
                useTexture(texture, textures);
                continue;
            }
            if (node instanceof C3DData.Float3Node float3) {
                if (float3.type() == 0x136) {
                    translate(C3DData.doubleToFixed(float3.x()), C3DData.doubleToFixed(float3.y()), C3DData.doubleToFixed(float3.z()));
                } else if (float3.type() == 0x138) {
                    scale(C3DData.doubleToFixed(float3.x()), C3DData.doubleToFixed(float3.y()), C3DData.doubleToFixed(float3.z()));
                } else if (float3.type() == 0x36) {
                    TextureTransform transform = textureStack.get(textureStack.size() - 1);
                    transform.translate(float3.x(), float3.y(), float3.z());
                } else if (float3.type() == 0x38) {
                    TextureTransform transform = textureStack.get(textureStack.size() - 1);
                    transform.scale(float3.x(), float3.y(), float3.z());
                }
                continue;
            }
            if (node instanceof C3DData.Float4Node float4) {
                if (float4.type() == 0x37) {
                    graphics.glRotatef(float4.w(), float4.x(), float4.y(), float4.z());
                }
                continue;
            }
            if (node instanceof C3DData.CoordRefNode ref) {
                switch (ref.type()) {
                    case 0x34 -> {
                        pushMatrix();
                        textureStack.add(new TextureTransform(textureStack.get(textureStack.size() - 1)));
                    }
                    case 0x35 -> {
                        if (textureStack.size() > 1) {
                            textureStack.remove(textureStack.size() - 1);
                        }
                        popMatrix();
                    }
                    case 0x39 -> frontFace((ref.coordSet() & 1) != 0 ? 63 : 62);
                    case 0x43 -> noMaterial = true;
                    case 0x51, 0x53 -> {
                        C3DData.CoordSet set = figure.data().coordSets().get(ref.coordSet());
                        if (set == null) {
                            trace3d("renderFigure missing coordSet=" + ref.coordSet() + " type=0x" + Integer.toHexString(ref.type()));
                            continue;
                        }
                        if (ref.type() == 0x53) {
                            coordSet = set.data;
                        } else {
                            if (set.material != null) {
                                noMaterial = false;
                                useMaterial(set.material);
                            }
                            if (set.texture != null) {
                                useTexture(set.texture, textures);
                            }
                        }
                    }
                    case 0x54 -> {
                        if (actionTable != null) {
                            applyActionTable(actionTable, figure.action(), figure.timeSeconds(), ref.coordSet());
                        }
                    }
                    default -> {
                    }
                }
                continue;
            }
            if (!(node instanceof C3DData.ShapeNode shape)) {
                continue;
            }
            shapeCount++;
            if (coordSet == null) {
                skippedShapesWithoutCoords++;
                continue;
            }
            int off = 0;
            while (off < shape.data().length && shape.data()[off] != 0) {
                int count = shape.data()[off++];
                boolean textured = (shape.flags() & 0x4) != 0;
                if (textured) {
                    texturedShapeCount++;
                }
                if (textured) {
                    enable(GraphicsOGL.GL_TEXTURE_2D);
                } else {
                    disable(GraphicsOGL.GL_TEXTURE_2D);
                }
                texEnvi(84, 86, noMaterial ? 80 : 87);
                begin((shape.flags() & 0x1000) != 0 ? 18 : 14);
                TextureTransform transform = textureStack.get(textureStack.size() - 1);
                for (int i = 0; i < count; i++) {
                    int vertexIndex = shape.data()[off++] & 0xFFFF;
                    if (textured) {
                        texCoord(C3DData.doubleToFixed(transform.transformU(coordSet[vertexIndex << 3], coordSet[(vertexIndex << 3) + 1])),
                                C3DData.doubleToFixed(transform.transformV(coordSet[vertexIndex << 3], coordSet[(vertexIndex << 3) + 1])));
                        normal(C3DData.doubleToFixed(coordSet[(vertexIndex << 3) + 2]),
                                C3DData.doubleToFixed(coordSet[(vertexIndex << 3) + 3]),
                                C3DData.doubleToFixed(coordSet[(vertexIndex << 3) + 4]));
                        vertex(C3DData.doubleToFixed(coordSet[(vertexIndex << 3) + 5]),
                                C3DData.doubleToFixed(coordSet[(vertexIndex << 3) + 6]),
                                C3DData.doubleToFixed(coordSet[(vertexIndex << 3) + 7]));
                    } else {
                        normal(C3DData.doubleToFixed(coordSet[vertexIndex * 6]),
                                C3DData.doubleToFixed(coordSet[vertexIndex * 6 + 1]),
                                C3DData.doubleToFixed(coordSet[vertexIndex * 6 + 2]));
                        vertex(C3DData.doubleToFixed(coordSet[vertexIndex * 6 + 3]),
                                C3DData.doubleToFixed(coordSet[vertexIndex * 6 + 4]),
                                C3DData.doubleToFixed(coordSet[vertexIndex * 6 + 5]));
                    }
                }
                end();
            }
        }
        popMatrix();
        // Action-table figures may change winding while rendering, but those
        // changes should not leak into the next figure or frame.
        frontFace(savedFrontFace == GraphicsOGL.GL_CW ? 62 : 63);
        if (cullFaceEnabled && !preserveCullFace) {
            enable(GraphicsOGL.GL_CULL_FACE);
        }
        if (textureEnabled) {
            enable(GraphicsOGL.GL_TEXTURE_2D);
        } else {
            disable(GraphicsOGL.GL_TEXTURE_2D);
        }
        trace3d("renderFigure done shapes=" + shapeCount
                + " texturedShapes=" + texturedShapeCount
                + " skippedNoCoords=" + skippedShapesWithoutCoords
                + " actionApplications=" + frameActionApplications);
    }

    private void applyActionTable(C3DData.ActionTableData table, int action, float time, int point) {
        if (table == null) {
            return;
        }
        C3DData.ActionInfo[] actions = table.actions();
        if (action < 0 || action >= actions.length) {
            return;
        }
        for (C3DData.ActionPart part : actions[action].parts()) {
            if (part.id() != point) {
                continue;
            }
            frameActionApplications++;
            trace3dCalls("applyAction partId=" + point + " type=0x" + Integer.toHexString(part.type())
                    + " time=" + time + " keyCount=" + part.keyCount());
            switch (part.type()) {
                case 0x32 -> graphics.glMultMatrixf(part.data());
                case 0x61 -> {
                    float[] rotation = interpolateKeys(part, time, 5);
                    graphics.glRotatef(rotation[3], rotation[0], rotation[1], rotation[2]);
                }
                case 0x62 -> {
                    float[] translation = interpolateKeys(part, time, 4);
                    graphics.glTranslatef(translation[0], translation[1], translation[2]);
                }
                case 0x162 -> {
                    float[] scale = interpolateKeys(part, time, 4);
                    graphics.glScalef(scale[0], scale[1], scale[2]);
                }
                default -> {
                }
            }
        }
    }

    private float[] interpolateKeys(C3DData.ActionPart part, float time, int valuesPerKey) {
        if (part.keyCount() <= 0) {
            float[] direct = new float[valuesPerKey - 1];
            System.arraycopy(part.data(), 0, direct, 0, direct.length);
            return direct;
        }
        int keyIndex = 0;
        while (keyIndex < part.keyCount() && time >= part.data()[keyIndex * valuesPerKey]) {
            keyIndex++;
        }
        if (keyIndex <= 0) {
            float[] first = new float[valuesPerKey - 1];
            System.arraycopy(part.data(), 1, first, 0, first.length);
            return first;
        }
        if (keyIndex >= part.keyCount()) {
            int base = (part.keyCount() - 1) * valuesPerKey;
            float[] last = new float[valuesPerKey - 1];
            System.arraycopy(part.data(), base + 1, last, 0, last.length);
            return last;
        }
        int previous = (keyIndex - 1) * valuesPerKey;
        int next = keyIndex * valuesPerKey;
        float startTime = part.data()[previous];
        float endTime = part.data()[next];
        float factor = endTime == startTime ? 0f : (time - startTime) / (endTime - startTime);
        float[] result = new float[valuesPerKey - 1];
        for (int i = 0; i < result.length; i++) {
            float start = part.data()[previous + i + 1];
            float end = part.data()[next + i + 1];
            result[i] = start + (end - start) * factor;
        }
        return result;
    }

    private void useMaterial(C3DData.MaterialNode material) {
        float[] values = material.data();
        float[] ambient = {values[0] * values[2], values[0] * values[3], values[0] * values[4], 1f};
        float[] diffuse = {values[2], values[3], values[4], values[1]};
        float[] specular = {values[5], values[6], values[7], 1f};
        float[] emission = {values[8], values[9], values[10], 1f};
        graphics.glMaterialfv(GraphicsOGL.GL_FRONT_AND_BACK, GraphicsOGL.GL_AMBIENT, ambient);
        graphics.glMaterialfv(GraphicsOGL.GL_FRONT_AND_BACK, GraphicsOGL.GL_DIFFUSE, diffuse);
        graphics.glMaterialfv(GraphicsOGL.GL_FRONT_AND_BACK, GraphicsOGL.GL_SPECULAR, specular);
        graphics.glMaterialf(GraphicsOGL.GL_FRONT_AND_BACK, GraphicsOGL.GL_SHININESS, values[11]);
        graphics.glMaterialfv(GraphicsOGL.GL_FRONT_AND_BACK, GraphicsOGL.GL_EMISSION, emission);
    }

    private void useTexture(C3DData.TextureBindingNode binding, C3DData.TextureData textures) {
        if (textures == null || binding.textureIndex() < 0 || binding.textureIndex() >= textures.images().length) {
            trace3d("useTexture skipped index=" + binding.textureIndex()
                    + " textureSetPresent=" + (textures != null));
            return;
        }
        ensureFigureTextureBound();
        C3DData.TextureImage image = textures.images()[binding.textureIndex()];
        frameTextureUploads++;
        trace3d("useTexture index=" + binding.textureIndex() + " size=" + image.width() + "x" + image.height()
                + " repeatS=" + binding.repeatS() + " repeatT=" + binding.repeatT());
        graphics.glTexParameteri(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_WRAP_S,
                binding.repeatS() ? GraphicsOGL.GL_REPEAT : GraphicsOGL.GL_CLAMP_TO_EDGE);
        graphics.glTexParameteri(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_WRAP_T,
                binding.repeatT() ? GraphicsOGL.GL_REPEAT : GraphicsOGL.GL_CLAMP_TO_EDGE);
        graphics.glTexParameteri(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_MAG_FILTER, GraphicsOGL.GL_NEAREST);
        graphics.glTexParameteri(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_MIN_FILTER, GraphicsOGL.GL_NEAREST);
        graphics.glTexImage2D(GraphicsOGL.GL_TEXTURE_2D, 0, GraphicsOGL.GL_RGBA,
                image.width(), image.height(), 0, GraphicsOGL.GL_RGBA, GraphicsOGL.GL_UNSIGNED_BYTE,
                bufferFactory.allocateByteBuffer(toRgbaBytes(image.argb())));
    }

    private void ensureFigureTextureBound() {
        if (figureTextureName < 0) {
            int[] ids = new int[1];
            graphics.glGenTextures(ids);
            figureTextureName = ids[0];
        }
        graphics.glBindTexture(GraphicsOGL.GL_TEXTURE_2D, figureTextureName);
    }

    private static byte[] toRgbaBytes(int[] argb) {
        byte[] bytes = new byte[argb.length * 4];
        for (int i = 0; i < argb.length; i++) {
            int value = argb[i];
            int base = i * 4;
            bytes[base] = (byte) (value >>> 16);
            bytes[base + 1] = (byte) (value >>> 8);
            bytes[base + 2] = (byte) value;
            bytes[base + 3] = (byte) (value >>> 24);
        }
        return bytes;
    }

    private void renderImmediate(int mode, List<ImmediateVertex> vertices) {
        if (vertices.isEmpty()) {
            return;
        }
        frameImmediateDraws++;
        frameImmediateVertices += vertices.size();
        trace3dCalls("drawArrays mode=0x" + Integer.toHexString(mode) + " vertices=" + vertices.size());
        float[] positionArray;
        float[] normalArray;
        float[] texCoordArray;
        float[] colorArray;
        if (mode == GL_QUADS || mode == GL_QUAD_STRIP || mode == GL_POLYGON) {
            TriangulatedVertices triangulated = triangulate(mode, vertices);
            positionArray = triangulated.positions();
            normalArray = triangulated.normals();
            texCoordArray = triangulated.texCoords();
            colorArray = triangulated.colors();
            mode = GraphicsOGL.GL_TRIANGLES;
        } else {
            positionArray = new float[vertices.size() * 3];
            normalArray = new float[vertices.size() * 3];
            texCoordArray = new float[vertices.size() * 2];
            colorArray = new float[vertices.size() * 4];
            for (int i = 0; i < vertices.size(); i++) {
                ImmediateVertex vertex = vertices.get(i);
                int pos = i * 3;
                int tex = i * 2;
                int col = i * 4;
                positionArray[pos] = vertex.x();
                positionArray[pos + 1] = vertex.y();
                positionArray[pos + 2] = vertex.z();
                normalArray[pos] = vertex.nx();
                normalArray[pos + 1] = vertex.ny();
                normalArray[pos + 2] = vertex.nz();
                texCoordArray[tex] = vertex.s();
                texCoordArray[tex + 1] = vertex.t();
                colorArray[col] = vertex.r();
                colorArray[col + 1] = vertex.g();
                colorArray[col + 2] = vertex.b();
                colorArray[col + 3] = vertex.a();
            }
        }
        graphics.glEnableClientState(GraphicsOGL.GL_VERTEX_ARRAY);
        graphics.glEnableClientState(GraphicsOGL.GL_NORMAL_ARRAY);
        graphics.glEnableClientState(GraphicsOGL.GL_COLOR_ARRAY);
        graphics.glEnableClientState(GraphicsOGL.GL_TEXTURE_COORD_ARRAY);
        graphics.glVertexPointer(3, GraphicsOGL.GL_FLOAT, 0, bufferFactory.allocateFloatBuffer(positionArray));
        graphics.glNormalPointer(GraphicsOGL.GL_FLOAT, 0, bufferFactory.allocateFloatBuffer(normalArray));
        graphics.glColorPointer(4, GraphicsOGL.GL_UNSIGNED_BYTE, 0, bufferFactory.allocateByteBuffer(toColorBytes(colorArray)));
        graphics.glTexCoordPointer(2, GraphicsOGL.GL_FLOAT, 0, bufferFactory.allocateFloatBuffer(texCoordArray));
        graphics.glDrawArrays(mode, 0, positionArray.length / 3);
        graphics.glDisableClientState(GraphicsOGL.GL_TEXTURE_COORD_ARRAY);
        graphics.glDisableClientState(GraphicsOGL.GL_COLOR_ARRAY);
        graphics.glDisableClientState(GraphicsOGL.GL_NORMAL_ARRAY);
        graphics.glDisableClientState(GraphicsOGL.GL_VERTEX_ARRAY);
    }

    private static byte[] toColorBytes(float[] colorArray) {
        byte[] packed = new byte[colorArray.length];
        for (int i = 0; i < colorArray.length; i++) {
            packed[i] = (byte) Math.round(Math.max(0f, Math.min(1f, colorArray[i])) * 255f);
        }
        return packed;
    }

    private static TriangulatedVertices triangulate(int mode, List<ImmediateVertex> vertices) {
        ArrayList<ImmediateVertex> result = new ArrayList<>();
        if (mode == GL_QUADS) {
            for (int i = 0; i + 3 < vertices.size(); i += 4) {
                addQuad(result, vertices.get(i), vertices.get(i + 1), vertices.get(i + 2), vertices.get(i + 3));
            }
        } else if (mode == GL_QUAD_STRIP) {
            for (int i = 0; i + 3 < vertices.size(); i += 2) {
                addQuad(result, vertices.get(i), vertices.get(i + 1), vertices.get(i + 3), vertices.get(i + 2));
            }
        } else {
            for (int i = 1; i + 1 < vertices.size(); i++) {
                result.add(vertices.get(0));
                result.add(vertices.get(i));
                result.add(vertices.get(i + 1));
            }
        }
        float[] positions = new float[result.size() * 3];
        float[] normals = new float[result.size() * 3];
        float[] texCoords = new float[result.size() * 2];
        float[] colors = new float[result.size() * 4];
        for (int i = 0; i < result.size(); i++) {
            ImmediateVertex vertex = result.get(i);
            int pos = i * 3;
            int tex = i * 2;
            int col = i * 4;
            positions[pos] = vertex.x();
            positions[pos + 1] = vertex.y();
            positions[pos + 2] = vertex.z();
            normals[pos] = vertex.nx();
            normals[pos + 1] = vertex.ny();
            normals[pos + 2] = vertex.nz();
            texCoords[tex] = vertex.s();
            texCoords[tex + 1] = vertex.t();
            colors[col] = vertex.r();
            colors[col + 1] = vertex.g();
            colors[col + 2] = vertex.b();
            colors[col + 3] = vertex.a();
        }
        return new TriangulatedVertices(positions, normals, texCoords, colors);
    }

    private static void addQuad(List<ImmediateVertex> result, ImmediateVertex a, ImmediateVertex b, ImmediateVertex c, ImmediateVertex d) {
        result.add(a);
        result.add(b);
        result.add(c);
        result.add(a);
        result.add(c);
        result.add(d);
    }

    private Graphics attachedGraphics() {
        borrowOwnerGraphicsIfNeeded();
        if (graphics != null) {
            return graphics;
        }
        if (owner == null) {
            return null;
        }
        Graphics attached = owner.getGraphics();
        if (attached == this) {
            return null;
        }
        graphics = attached;
        return attached;
    }

    private void borrowOwnerGraphicsIfNeeded() {
        if (owner == null) {
            return;
        }
        if (graphics != null && borrowedOwnerGraphics) {
            boolean ownerSizeChanged = borrowedOwnerWidth != owner.getWidth()
                    || borrowedOwnerHeight != owner.getHeight();
            // Keep the borrowed canvas graphics stable for the duration of one
            // direct C3D frame. Re-borrowing a fresh wrapper on every call can
            // split one logical lock/unlock slice across multiple Graphics
            // instances, which is especially fragile for titles that mix 2D and
            // 3D inside one `getC3DGraphics()` frame.
            if (!ownerSizeChanged || locked) {
                return;
            }
            releaseBorrowedGraphics(null);
        }
        if (graphics == null) {
            Graphics borrowed = owner.getGraphics();
            if (borrowed != this) {
                graphics = borrowed;
                borrowedOwnerGraphics = true;
                borrowedOwnerWidth = owner.getWidth();
                borrowedOwnerHeight = owner.getHeight();
                trace3d("borrow owner graphics size=" + borrowedOwnerWidth + "x" + borrowedOwnerHeight);
            }
        }
    }

    private void releaseBorrowedGraphics(Graphics replacement) {
        if (!borrowedOwnerGraphics || graphics == null || graphics == replacement) {
            return;
        }
        graphics.dispose();
        graphics = null;
        borrowedOwnerGraphics = false;
        borrowedOwnerWidth = -1;
        borrowedOwnerHeight = -1;
    }

    private Graphics ensureGraphics() {
        Graphics target = attachedGraphics();
        if (target == null) {
            throw new IllegalStateException("C3DGraphics is not attached to a canvas graphics context yet");
        }
        return target;
    }

    private void replaySoftwareState(Graphics target) {
        target.setColor(softwareColor);
        target.setFont(softwareFont);
        target.setOrigin(softwareOriginX, softwareOriginY);
    }

    private static int normalizeSoftwareColor(int color) {
        if (color >= Graphics.BLACK && color <= Graphics.SILVER) {
            return Graphics.getColorOfName(color);
        }
        if ((color & 0xFF000000) == 0) {
            return 0xFF000000 | (color & 0x00FFFFFF);
        }
        return color;
    }

    private void setMatrixMode(int glMode) {
        if (glMode == 0) {
            return;
        }
        currentMatrixMode = glMode;
        graphics.glMatrixMode(glMode);
    }

    private void ensure3D() {
        ensureGraphics();
        if (!drawSessionActive) {
            trace3d("beginDrawing session");
            graphics.beginDrawing();
            drawSessionActive = true;
        }
    }

    private void resetFrameCounters() {
        frameImmediateDraws = 0;
        frameImmediateVertices = 0;
        frameFigureRenders = 0;
        frameDisplayListCalls = 0;
        frameTextureUploads = 0;
        frameActionApplications = 0;
    }

    private void logFrameSummary(boolean present) {
        if (!DEBUG_3D) {
            return;
        }
        if (frameImmediateDraws == 0 && frameFigureRenders == 0 && frameDisplayListCalls == 0 && frameTextureUploads == 0) {
            return;
        }
        OpenDoJaLog.debug(C3DGraphics.class, () -> "C3D frame present=" + present
                + " figures=" + frameFigureRenders
                + " lists=" + frameDisplayListCalls
                + " draws=" + frameImmediateDraws
                + " vertices=" + frameImmediateVertices
                + " textureUploads=" + frameTextureUploads
                + " actionApplications=" + frameActionApplications);
    }

    private static void trace3d(String message) {
        if (DEBUG_3D) {
            OpenDoJaLog.debug(C3DGraphics.class, message);
        }
    }

    private static void trace3dCalls(String message) {
        if (TRACE_3D_CALLS) {
            OpenDoJaLog.debug(C3DGraphics.class, message);
        }
    }


    private static String fixed3(int value) {
        return String.format(java.util.Locale.ROOT, "%.3f", C3DData.fixedToDouble(value));
    }

    private int textureName(int id) {
        Integer existing = textures.get(id);
        if (existing != null) {
            return existing;
        }
        int[] actual = new int[1];
        graphics.glGenTextures(actual);
        textures.put(id, actual[0]);
        return actual[0];
    }

    private void recordOrRun(Command command, Runnable action) {
        if (recordingCommands != null) {
            recordingCommands.add(command);
        } else {
            action.run();
        }
    }

    private static int clearMask(int mask) {
        if (mask < 0 || mask >= CLEAR_MASKS.length) {
            return 0;
        }
        return CLEAR_MASKS[mask];
    }

    private static int mapEnum(int value) {
        if (value >= 0 && value < C3DL_TO_GL.length) {
            return C3DL_TO_GL[value];
        }
        return value;
    }

    private static int mapShiftedEnum(int value) {
        // Mitsubishi j3d2 state/light/material/texture enums line up with the
        // OpenGL table from index 31 onward, but shifted by one slot.
        if (value >= 31 && value + 1 < C3DL_TO_GL.length) {
            return C3DL_TO_GL[value + 1];
        }
        return mapEnum(value);
    }

    private static int mapPrimitiveMode(int value) {
        return switch (value) {
            case 8 -> GraphicsOGL.GL_POINTS;
            case 9 -> GraphicsOGL.GL_LINES;
            case 12 -> GraphicsOGL.GL_TRIANGLES;
            case 14 -> GraphicsOGL.GL_TRIANGLE_STRIP;
            case 15 -> GL_QUADS;
            default -> mapEnum(value);
        };
    }

    private static int mapCompareFunction(int value) {
        return switch (value) {
            case 1 -> GraphicsOGL.GL_ALWAYS;
            case 2 -> GraphicsOGL.GL_NEVER;
            case 3 -> GraphicsOGL.GL_LESS;
            case 4 -> GraphicsOGL.GL_LEQUAL;
            case 5 -> GraphicsOGL.GL_GREATER;
            case 6 -> GraphicsOGL.GL_GEQUAL;
            case 7 -> GraphicsOGL.GL_EQUAL;
            case 8 -> GraphicsOGL.GL_NOTEQUAL;
            default -> mapEnum(value);
        };
    }

    private static int mapCapability(int value) {
        return switch (value) {
            case 37 -> GraphicsOGL.GL_ALPHA_TEST;
            case 38 -> GraphicsOGL.GL_BLEND;
            case 39 -> GraphicsOGL.GL_COLOR_MATERIAL;
            case 40 -> GraphicsOGL.GL_DEPTH_TEST;
            case 41 -> GraphicsOGL.GL_CULL_FACE;
            case 44 -> GraphicsOGL.GL_LIGHT0;
            case 45 -> GraphicsOGL.GL_LIGHT1;
            case 46 -> GraphicsOGL.GL_LIGHT2;
            case 47 -> GraphicsOGL.GL_LIGHT3;
            case 48 -> GraphicsOGL.GL_LIGHT4;
            case 49 -> GraphicsOGL.GL_LIGHT5;
            case 50 -> GraphicsOGL.GL_LIGHT6;
            case 51 -> GraphicsOGL.GL_LIGHT7;
            case 52 -> GraphicsOGL.GL_LIGHTING;
            case 56 -> GraphicsOGL.GL_TEXTURE_2D;
            default -> mapShiftedEnum(value);
        };
    }

    private static int mapMatrixMode(int value) {
        return switch (value) {
            case 73 -> GraphicsOGL.GL_MODELVIEW;
            case 74 -> GraphicsOGL.GL_PROJECTION;
            case 75 -> GraphicsOGL.GL_TEXTURE;
            default -> mapEnum(value);
        };
    }

    private static int mapFrontFaceMode(int value) {
        return switch (value) {
            // The shared software OGL rasterizer determines facing after the
            // final viewport transform, where screen-space Y is already flipped.
            // Mitsubishi Z3D titles authored for j3d2 therefore need the
            // requested winding compensated here instead of in the generic
            // renderer.
            case 62 -> GraphicsOGL.GL_CW;
            case 63 -> GraphicsOGL.GL_CCW;
            default -> mapShiftedEnum(value);
        };
    }

    private static int mapLight(int value) {
        return switch (value) {
            case 44 -> GraphicsOGL.GL_LIGHT0;
            case 45 -> GraphicsOGL.GL_LIGHT1;
            case 46 -> GraphicsOGL.GL_LIGHT2;
            case 47 -> GraphicsOGL.GL_LIGHT3;
            case 48 -> GraphicsOGL.GL_LIGHT4;
            case 49 -> GraphicsOGL.GL_LIGHT5;
            case 50 -> GraphicsOGL.GL_LIGHT6;
            case 51 -> GraphicsOGL.GL_LIGHT7;
            default -> mapShiftedEnum(value);
        };
    }

    private static int mapLightParameter(int value) {
        return switch (value) {
            case 32 -> GraphicsOGL.GL_AMBIENT;
            case 33 -> GraphicsOGL.GL_DIFFUSE;
            case 35 -> GraphicsOGL.GL_SPECULAR;
            case 66 -> GraphicsOGL.GL_POSITION;
            default -> mapShiftedEnum(value);
        };
    }

    private static int mapMaterialFace(int value) {
        return switch (value) {
            case 31 -> GraphicsOGL.GL_FRONT_AND_BACK;
            default -> mapShiftedEnum(value);
        };
    }

    private static int mapMaterialParameter(int value) {
        return switch (value) {
            case 34 -> GraphicsOGL.GL_AMBIENT_AND_DIFFUSE;
            case 35 -> GraphicsOGL.GL_SPECULAR;
            case 36 -> GraphicsOGL.GL_EMISSION;
            case 72 -> GraphicsOGL.GL_SHININESS;
            default -> mapShiftedEnum(value);
        };
    }

    private static int mapLightModelParameter(int value) {
        return switch (value) {
            case 118 -> GL_LIGHT_MODEL_TWO_SIDE;
            default -> mapShiftedEnum(value);
        };
    }

    private static float mapLightModelValue(int value) {
        return switch (value) {
            case 119 -> 0f;
            case 120 -> 1f;
            default -> mapShiftedEnum(value);
        };
    }

    private static int mapTextureTarget(int value) {
        return switch (value) {
            case 56 -> GraphicsOGL.GL_TEXTURE_2D;
            default -> mapShiftedEnum(value);
        };
    }

    private static int mapTextureParameterName(int value) {
        return switch (value) {
            case 105 -> GraphicsOGL.GL_TEXTURE_WRAP_S;
            case 106 -> GraphicsOGL.GL_TEXTURE_WRAP_T;
            default -> mapShiftedEnum(value);
        };
    }

    private static int mapTextureParameterValue(int value) {
        return switch (value) {
            case 109 -> GraphicsOGL.GL_REPEAT;
            case 110 -> GraphicsOGL.GL_CLAMP_TO_EDGE;
            default -> normalizeTextureParameter(mapShiftedEnum(value));
        };
    }

    private static int mapTexturePixelType(int value) {
        return switch (value) {
            case 104 -> GraphicsOGL.GL_UNSIGNED_BYTE;
            default -> mapShiftedEnum(value);
        };
    }

    private static int inferTextureUploadFormat(int width, int height, byte[] data, int fallback) {
        int pixelCount = Math.max(1, width * height);
        int bytesPerPixel = data.length / pixelCount;
        if (bytesPerPixel == 4) {
            return GraphicsOGL.GL_RGBA;
        }
        if (bytesPerPixel == 3) {
            return GraphicsOGL.GL_RGB;
        }
        if (fallback == 97) {
            return GraphicsOGL.GL_RGB;
        }
        if (fallback == 98) {
            return GraphicsOGL.GL_RGBA;
        }
        return mapEnum(fallback);
    }

    private static byte[] normalizeTextureUploadData(int width, int height, byte[] data, int pixelFormat) {
        int pixelCount = Math.max(1, width * height);
        int bytesPerPixel = pixelCount == 0 ? 0 : data.length / pixelCount;
        if (pixelFormat == GraphicsOGL.GL_RGB && bytesPerPixel == 4 && data.length >= pixelCount * 4) {
            byte[] rgb = new byte[pixelCount * 3];
            for (int pixel = 0; pixel < pixelCount; pixel++) {
                int source = pixel * 4;
                int dest = pixel * 3;
                rgb[dest] = data[source];
                rgb[dest + 1] = data[source + 1];
                rgb[dest + 2] = data[source + 2];
            }
            return rgb;
        }
        return data;
    }

    private static int normalizeTextureParameter(int value) {
        if (value == GL_CLAMP) {
            return GraphicsOGL.GL_CLAMP_TO_EDGE;
        }
        return value;
    }

    private static int normalizeInternalFormat(int value) {
        return switch (value) {
            case GL_RGB4, GL_RGB5, GL_RGB8 -> GraphicsOGL.GL_RGB;
            case GL_RGBA4, GL_RGBA8, GL_RGB5_A1 -> GraphicsOGL.GL_RGBA;
            default -> value;
        };
    }

    private static float[] toFloatArray(int[] values) {
        float[] converted = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            converted[i] = C3DData.fixedToFloat(values[i]);
        }
        return converted;
    }

    private static float[] toFloatMatrix(int[] matrix) {
        float[] converted = new float[16];
        for (int i = 0; i < 16; i++) {
            converted[i] = C3DData.fixedToFloat(matrix[i]);
        }
        return converted;
    }

    private static float[] lookAtMatrix(float eyeX, float eyeY, float eyeZ,
                                        float centerX, float centerY, float centerZ,
                                        float upX, float upY, float upZ) {
        float fx = centerX - eyeX;
        float fy = centerY - eyeY;
        float fz = centerZ - eyeZ;
        float fLength = (float) java.lang.Math.sqrt(fx * fx + fy * fy + fz * fz);
        if (fLength == 0f) {
            fLength = 1f;
        }
        fx /= fLength;
        fy /= fLength;
        fz /= fLength;

        float sx = fy * upZ - fz * upY;
        float sy = fz * upX - fx * upZ;
        float sz = fx * upY - fy * upX;
        float sLength = (float) java.lang.Math.sqrt(sx * sx + sy * sy + sz * sz);
        if (sLength == 0f) {
            sLength = 1f;
        }
        sx /= sLength;
        sy /= sLength;
        sz /= sLength;

        float ux = sy * fz - sz * fy;
        float uy = sz * fx - sx * fz;
        float uz = sx * fy - sy * fx;

        return new float[]{
                sx, ux, -fx, 0f,
                sy, uy, -fy, 0f,
                sz, uz, -fz, 0f,
                -(sx * eyeX + sy * eyeY + sz * eyeZ),
                -(ux * eyeX + uy * eyeY + uz * eyeZ),
                fx * eyeX + fy * eyeY + fz * eyeZ,
                1f
        };
    }

    @FunctionalInterface
    private interface Command {
        void execute(C3DGraphics graphics);
    }

    private record ImmediateVertex(
            float x, float y, float z,
            float nx, float ny, float nz,
            float s, float t,
            float r, float g, float b, float a) {
    }

    private record TriangulatedVertices(float[] positions, float[] normals, float[] texCoords, float[] colors) {
    }

    private static final class TextureTransform {
        private float scaleU = 1f;
        private float scaleV = 1f;
        private float translateU;
        private float translateV;

        TextureTransform() {
        }

        TextureTransform(TextureTransform source) {
            this.scaleU = source.scaleU;
            this.scaleV = source.scaleV;
            this.translateU = source.translateU;
            this.translateV = source.translateV;
        }

        void translate(float u, float v, float ignored) {
            translateU += u;
            translateV += v;
        }

        void scale(float u, float v, float ignored) {
            scaleU *= u;
            scaleV *= v;
        }

        float transformU(float u, float ignoredV) {
            return u * scaleU + translateU;
        }

        float transformV(float ignoredU, float v) {
            return v * scaleV + translateV;
        }
    }
}
