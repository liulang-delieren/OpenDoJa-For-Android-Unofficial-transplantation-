package opendoja.host.ogl;

import android.opengl.GLES10;
import android.opengl.GLES11;

import com.nttdocomo.ui.ogl.GraphicsOGL;
import opendoja.compat.awt.Rectangle;
import opendoja.host.DesktopSurface;
import opendoja.host.OpenDoJaLaunchArgs;
import opendoja.host.OpenDoJaLog;
import opendoja.compat.awt.image.BufferedImage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Android hardware backend for the DoJa OGL API. Renders through an offscreen
 * OpenGL ES 1.x context (fixed function, matching the upstream desktop JOGL design)
 * and reads the finished frame back into the software surface at flush boundaries,
 * so the 2D/Canvas presentation pipeline stays unchanged.
 *
 * <p>Ported from the upstream openDoJa JOGL implementation; GL2 calls map to
 * GLES10/GLES11, immediate-mode quads became client-array draws, and BGRA
 * surface swaps became RGBA swizzles.</p>
 */
final class OglHardwareBackend {
    private static final int GL_BGRA_EXT = 0x80E1;

    private final OglRenderer owner;
    private final Map<Integer, HardwareTexture> textureCache = new HashMap<>();
    private final Object glLock = new Object();
    private final OglEglContext egl = new OglEglContext();
    private boolean available;
    private boolean failureLogged;
    private boolean bgraReadDetected;
    private long nextInitAttemptNanos;
    private boolean surfaceDirty = true;
    private boolean readbackPending;
    private int drawableWidth = -1;
    private int drawableHeight = -1;
    private int surfaceTextureId;
    private int surfaceTextureWidth = -1;
    private int surfaceTextureHeight = -1;
    private ByteBuffer readbackBuffer;
    private ByteBuffer surfaceUploadBuffer;
    private ByteBuffer textureUploadBuffer;
    private float[] vertexScratch = new float[0];
    private float[] normalScratch = new float[0];
    private float[] texCoordScratch = new float[0];
    private byte[] colorScratch = new byte[0];
    private short[] indexScratch = new short[0];
    private ByteBuffer vertexBufferBytes;
    private ByteBuffer normalBufferBytes;
    private ByteBuffer texCoordBufferBytes;
    private ByteBuffer colorBufferBytes;
    private ByteBuffer indexBufferBytes;
    private FloatBuffer quadPositionBuffer;
    private FloatBuffer quadTexCoordBuffer;
    private int resolveTextureId;
    private int resolveTextureWidth = -1;
    private int resolveTextureHeight = -1;
    private int[] lastHardwareSnapshot;
    private int[] outsideLockOverlaySnapshot;
    private Rectangle outsideLockOverlayBounds;

    OglHardwareBackend(OglRenderer owner) {
        this.owner = owner;
    }

    boolean clear(int mask) {
        if ((mask & (GraphicsOGL.GL_COLOR_BUFFER_BIT | GraphicsOGL.GL_DEPTH_BUFFER_BIT)) == 0) {
            return true;
        }
        return withContext(() -> {
            prepareFramebuffer();
            applyViewportState();
            applyClipState();
            int glMask = 0;
            if ((mask & GraphicsOGL.GL_COLOR_BUFFER_BIT) != 0) {
                float[] clearColor = owner.unpackColor(owner.oglClearColor());
                GLES10.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
                glMask |= GraphicsOGL.GL_COLOR_BUFFER_BIT;
            }
            if ((mask & GraphicsOGL.GL_DEPTH_BUFFER_BIT) != 0) {
                GLES10.glDepthMask(true);
                GLES10.glClearDepthf(0f);
                glMask |= GraphicsOGL.GL_DEPTH_BUFFER_BIT;
            }
            if (glMask != 0) {
                GLES10.glClear(glMask);
                if ((mask & GraphicsOGL.GL_DEPTH_BUFFER_BIT) != 0) {
                    GLES10.glDepthMask(owner.oglState().depthMask);
                }
                if ((mask & GraphicsOGL.GL_COLOR_BUFFER_BIT) != 0) {
                    readbackPending = true;
                }
            }
            return GLES10.glGetError() == GraphicsOGL.GL_NO_ERROR;
        });
    }

    boolean draw(int mode, int first, int count, OglRenderer.OglIndexSource indexSource) {
        int primitiveCount = indexSource == null ? count : Math.min(Math.max(0, count), indexSource.elementCount());
        if (primitiveCount <= 0) {
            return true;
        }
        int glMode = toHardwarePrimitiveMode(mode);
        if (glMode == -1) {
            logUnsupportedModeOnce(mode);
            return false;
        }
        PreparedArrayState arrayState = prepareArrayState(first, primitiveCount, indexSource);
        if (arrayState == null) {
            if (loggedArrayStateFailures < 3) {
                loggedArrayStateFailures++;
                OpenDoJaLog.warn(OglRenderer.class, "Hardware backend: prepareArrayState failed, mode=" + mode
                        + " first=" + first + " count=" + primitiveCount + ", falling back to software draw");
            }
            return false;
        }
        logSmallDrawState(mode, primitiveCount);
        OglRenderer.OglState effectState = owner.oglState();
        boolean effectDraw = effectState.textureEnvMode == GraphicsOGL.GL_COMBINE
                || (effectState.blendCapEnabled && effectState.blendDstFactor == GraphicsOGL.GL_ONE);
        if (CAPTURE_FRAMES && effectDraw) {
            frameContainsEffect = true;
        }
        return withContext(() -> {
            prepareFramebuffer();
            applyClipState();
            applyViewportState();
            applyRenderState(arrayState.emulateMatrixPalette());
            applyTextureState();
            bindArrayState(arrayState);
            if (effectDraw) {
                applyEffectDebugOverride();
            }
            try {
                if (arrayState.indexBuffer() == null) {
                    GLES10.glDrawArrays(glMode, arrayState.drawFirst(), arrayState.drawCount());
                } else {
                    GLES10.glDrawElements(glMode, arrayState.drawCount(), GraphicsOGL.GL_UNSIGNED_SHORT,
                            arrayState.indexBuffer());
                }
            } finally {
                unbindArrayState();
            }
            readbackPending = true;
            int glError = GLES10.glGetError();
            if (glError != GraphicsOGL.GL_NO_ERROR && loggedDrawErrors < 30) {
                loggedDrawErrors++;
                System.out.println("INFO [jam-launch] [HWDIAG] glError=0x" + Integer.toHexString(glError)
                        + " mode=" + mode + " count=" + primitiveCount + " -> software fallback");
            }
            return glError == GraphicsOGL.GL_NO_ERROR;
        });
    }

    void endDrawing() {
        flush();
    }

    void prepareForSoftwareMutation() {
        if (readbackPending) {
            flush();
        }
    }

    void flush() {
        if (!readbackPending) {
            return;
        }
        withContext(() -> {
            GLES10.glFlush();
            DesktopSurface surface = owner.host().surface();
            if (drawableWidth != renderWidth() || drawableHeight != renderHeight()) {
                if (loggedReadbackMismatches < 20) {
                    loggedReadbackMismatches++;
                    OpenDoJaLog.warn(OglRenderer.class, "Hardware OGL readback size mismatch: context "
                            + drawableWidth + "x" + drawableHeight + ", surface " + surface.width() + "x"
                            + surface.height() + " (render " + renderWidth() + "x" + renderHeight() + ")");
                }
            }
            int[] framePixels = readBackColorBuffer(surface);
            int[] currentHardwareSnapshot = Arrays.copyOf(framePixels, framePixels.length);
            reapplyOutsideLockOverlay(lastHardwareSnapshot, outsideLockOverlaySnapshot, framePixels);
            // Publish immediately: subsequent 2D draws mutate the shared bitmap directly,
            // so the readback must not linger behind the OpenGL-frame dirty flag.
            surface.markOglFrameDirty();
            surface.flushOglFrameToBitmap();
            readbackPending = false;
            surfaceDirty = false;
            lastHardwareSnapshot = currentHardwareSnapshot;
            if (CAPTURE_FRAMES && frameContainsEffect && capturedFrames < 24) {
                capturedFrames++;
                dumpCapture("frame_" + capturedFrames + "_" + surface.width() + "x" + surface.height(),
                        framePixels);
            }
            frameContainsEffect = false;
            return true;
        });
    }

    void onTextureDeleted(int textureId) {
        HardwareTexture removed = textureCache.remove(textureId);
        if (removed == null || removed.textureId() == 0) {
            return;
        }
        withContext(() -> {
            GLES10.glDeleteTextures(1, new int[]{removed.textureId()}, 0);
            return true;
        });
    }

    void onSoftwareSurfaceMutation() {
        surfaceDirty = true;
    }

    void onPresentedSoftwareOverlay(Rectangle bounds) {
        outsideLockOverlaySnapshot = copySurfacePixels();
        Rectangle surfaceBounds = new Rectangle(0, 0, owner.host().surface().width(), owner.host().surface().height());
        if (bounds != null) {
            outsideLockOverlayBounds = bounds.intersection(surfaceBounds);
        } else {
            Rectangle clip = owner.host().delegate().getClipBounds();
            outsideLockOverlayBounds = clip == null ? surfaceBounds : clip.intersection(surfaceBounds);
        }
        surfaceDirty = true;
    }

    void onHostDelegateRecreated() {
        surfaceDirty = true;
    }

    boolean hasBufferedHardwarePresentation() {
        return readbackPending || lastHardwareSnapshot != null;
    }

    void close() {
        synchronized (glLock) {
            if (!available && !egl.makeCurrent()) {
                egl.destroy();
                resetState();
                return;
            }
            try {
                int[] textureIds = new int[textureCache.size() + (surfaceTextureId == 0 ? 0 : 1) + (resolveTextureId == 0 ? 0 : 1)];
                int offset = 0;
                for (HardwareTexture texture : textureCache.values()) {
                    if (texture.textureId() != 0) {
                        textureIds[offset++] = texture.textureId();
                    }
                }
                if (surfaceTextureId != 0) {
                    textureIds[offset++] = surfaceTextureId;
                }
                if (resolveTextureId != 0) {
                    textureIds[offset++] = resolveTextureId;
                }
                if (offset > 0) {
                    GLES10.glDeleteTextures(offset, textureIds, 0);
                }
            } finally {
                egl.destroy();
            }
        }
        resetState();
    }

    private void resetState() {
        textureCache.clear();
        surfaceTextureId = 0;
        surfaceTextureWidth = -1;
        surfaceTextureHeight = -1;
        readbackBuffer = null;
        surfaceUploadBuffer = null;
        textureUploadBuffer = null;
        drawableWidth = -1;
        drawableHeight = -1;
        resolveTextureId = 0;
        resolveTextureWidth = -1;
        resolveTextureHeight = -1;
        available = false;
        surfaceDirty = true;
        readbackPending = false;
        lastHardwareSnapshot = null;
        outsideLockOverlaySnapshot = null;
        outsideLockOverlayBounds = null;
        bgraReadDetected = false;
        nextInitAttemptNanos = 0L;
        glResourcesValid = true;
    }

    /**
     * Forgets every GPU-side object. GL object names are only valid for the lifetime of the EGL
     * context, so a recreated context has to create them again; the decoded pixels retained by
     * {@link OglRenderer.OglTexture} let the next bind upload the content once more instead of
     * leaving the game sampling whatever name the new context handed out.
     *
     * @return number of cached hardware textures that were dropped
     */
    private int invalidateGlResources() {
        int invalidated = textureCache.size();
        textureCache.clear();
        surfaceTextureId = 0;
        surfaceTextureWidth = -1;
        surfaceTextureHeight = -1;
        resolveTextureId = 0;
        resolveTextureWidth = -1;
        resolveTextureHeight = -1;
        return invalidated;
    }

    private boolean withContext(HardwareGlCall call) {
        if (!ensureDrawable()) {
            return false;
        }
        synchronized (glLock) {
            try {
                if (!egl.makeCurrent()) {
                    return false;
                }
                return call.execute();
            } catch (RuntimeException exception) {
                available = false;
                if (!failureLogged) {
                    failureLogged = true;
                    OpenDoJaLog.warn(OglRenderer.class, "OpenGLES hardware backend unavailable, falling back to software", exception);
                }
                return false;
            }
        }
    }

    private boolean ensureDrawable() {
        int width = renderWidth();
        int height = renderHeight();
        if (available && drawableWidth == width && drawableHeight == height) {
            return true;
        }
        long now = System.nanoTime();
        if (!available && now < nextInitAttemptNanos) {
            return false;
        }
        synchronized (glLock) {
            if (drawableWidth == width && drawableHeight == height && egl.makeCurrent()) {
                if (!glResourcesValid) {
                    invalidateGlResources();
                    glResourcesValid = true;
                }
                available = true;
                return true;
            }
            glResourcesValid = false;
            egl.destroy();
            try {
                if (!egl.ensure(width, height)) {
                    drawableWidth = width;
                    drawableHeight = height;
                    available = false;
                    nextInitAttemptNanos = now + 500_000_000L;
                    if (!failureLogged) {
                        failureLogged = true;
                        OpenDoJaLog.warn(OglRenderer.class, "Failed to initialize OpenGLES hardware backend (pbuffer "
                                + width + "x" + height + "), falling back to software");
                    }
                    return false;
                }
                int previousWidth = drawableWidth;
                int previousHeight = drawableHeight;
                drawableWidth = width;
                drawableHeight = height;
                available = true;
                surfaceDirty = true;
                readbackPending = false;
                lastHardwareSnapshot = null;
                bgraReadDetected = false;
                int invalidatedTextures = invalidateGlResources();
                glResourcesValid = true;
                if (loggedContextRecreations < 40) {
                    loggedContextRecreations++;
                    OpenDoJaLog.warn(OglRenderer.class, "Hardware OGL context (re)created "
                            + previousWidth + "x" + previousHeight + " -> " + width + "x" + height
                            + (previousWidth == width && previousHeight == height ? " [same size]" : " [resize]")
                            + ", invalidated " + invalidatedTextures + " cached textures");
                }
                return true;
            } catch (RuntimeException exception) {
                drawableWidth = width;
                drawableHeight = height;
                available = false;
                nextInitAttemptNanos = now + 500_000_000L;
                if (!failureLogged) {
                    failureLogged = true;
                    OpenDoJaLog.warn(OglRenderer.class, "Failed to initialize OpenGLES hardware backend", exception);
                }
                return false;
            }
        }
    }

    private PreparedArrayState prepareArrayState(int first, int primitiveCount, OglRenderer.OglIndexSource indexSource) {
        OglRenderer.OglState ogl = owner.oglState();
        OglRenderer.OglPointer vertexPointer = ogl.vertexPointer;
        if (vertexPointer == null) {
            return null;
        }
        boolean emulateMatrixPalette = ogl.usesMatrixPalette();
        int vertexCount = indexSource == null ? primitiveCount : resolveVertexCount(primitiveCount, indexSource);
        if (vertexCount <= 0) {
            return null;
        }
        int vertexFloatCount = vertexCount * 4;
        float[] vertexValues = ensureFloatArray(vertexScratch, vertexFloatCount);
        vertexScratch = vertexValues;
        int normalFloatCount = vertexCount * 3;
        float[] normalValues = (ogl.lightingEnabled() || ogl.normalArrayEnabled)
                ? ensureFloatArray(normalScratch, normalFloatCount) : null;
        if (normalValues != null) {
            normalScratch = normalValues;
        }
        int texCoordFloatCount = vertexCount * 2;
        float[] texCoordValues = (ogl.texCoordArrayEnabled && ogl.texCoordPointer != null)
                ? ensureFloatArray(texCoordScratch, texCoordFloatCount) : null;
        if (texCoordValues != null) {
            texCoordScratch = texCoordValues;
        }
        int colorByteCount = vertexCount * 4;
        byte[] colorValues = (ogl.colorArrayEnabled && ogl.colorPointer != null)
                ? ensureByteArray(colorScratch, colorByteCount) : null;
        if (colorValues != null) {
            colorScratch = colorValues;
        }
        OglRenderer.ClipVector clip = emulateMatrixPalette ? new OglRenderer.ClipVector() : null;
        OglRenderer.ClipVector eye = emulateMatrixPalette ? new OglRenderer.ClipVector() : null;
        OglRenderer.ClipVector normal = (emulateMatrixPalette && normalValues != null) ? new OglRenderer.ClipVector() : null;
        boolean effectDraw = ogl.textureEnvMode == GraphicsOGL.GL_COMBINE
                || (ogl.blendCapEnabled && ogl.blendDstFactor == GraphicsOGL.GL_ONE);
        boolean traceGeometry = TRACE_DRAW_GEOMETRY && loggedGeometrySignatures.size() < 300;
        boolean traceClipSpace = traceGeometry || effectDraw;
        boolean traceColors = (traceGeometry || effectDraw) && colorValues != null;
        OglRenderer.ClipVector traceClip = traceClipSpace ? new OglRenderer.ClipVector() : null;
        OglRenderer.ClipVector traceEye = traceClipSpace ? new OglRenderer.ClipVector() : null;
        float[] clipBounds = traceClipSpace ? new float[]{Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE,
                -Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE} : null;
        float[] ndcBounds = effectDraw ? new float[]{Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE} : null;
        float[] texBounds = ((traceGeometry || (effectDraw && texCoordValues != null)) && texCoordValues != null)
                ? new float[]{Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE} : null;
        int behindW = 0;
        int[] colorMin = traceColors ? new int[]{255, 255, 255, 255} : null;
        int[] colorMax = traceColors ? new int[]{0, 0, 0, 0} : null;
        long[] colorSum = traceColors ? new long[4] : null;
        int sourceBase = indexSource == null ? first : 0;
        boolean extensionMatrices = traceClipSpace && ogl.usesExtensionMatrices();
        for (int i = 0; i < vertexCount; i++) {
            int sourceVertexIndex = sourceBase + i;
            populateHardwareVertex(vertexValues, i, sourceVertexIndex, emulateMatrixPalette, clip, eye);
            if (ogl.lastError != GraphicsOGL.GL_NO_ERROR) {
                return null;
            }
            if (traceClipSpace) {
                int positionSize = Math.max(1, ogl.vertexPointer.size());
                float traceX = owner.readFloatComponent(ogl.vertexPointer, sourceVertexIndex, 0);
                float traceY = positionSize > 1 ? owner.readFloatComponent(ogl.vertexPointer, sourceVertexIndex, 1) : 0f;
                float traceZ = positionSize > 2 ? owner.readFloatComponent(ogl.vertexPointer, sourceVertexIndex, 2) : 0f;
                owner.transformVertex(traceClip, traceEye, traceX, traceY, traceZ, sourceVertexIndex, extensionMatrices);
                clipBounds[0] = Math.min(clipBounds[0], traceClip.x);
                clipBounds[1] = Math.max(clipBounds[1], traceClip.x);
                clipBounds[2] = Math.min(clipBounds[2], traceClip.y);
                clipBounds[3] = Math.max(clipBounds[3], traceClip.y);
                clipBounds[4] = Math.min(clipBounds[4], traceClip.z);
                clipBounds[5] = Math.max(clipBounds[5], traceClip.z);
                clipBounds[6] = Math.min(clipBounds[6], traceClip.w);
                clipBounds[7] = Math.max(clipBounds[7], traceClip.w);
                if (traceClip.w <= 0f) {
                    behindW++;
                }
                if (ndcBounds != null && traceClip.w > 0f) {
                    float ndcX = traceClip.x / traceClip.w;
                    float ndcY = traceClip.y / traceClip.w;
                    ndcBounds[0] = Math.min(ndcBounds[0], ndcX);
                    ndcBounds[1] = Math.max(ndcBounds[1], ndcX);
                    ndcBounds[2] = Math.min(ndcBounds[2], ndcY);
                    ndcBounds[3] = Math.max(ndcBounds[3], ndcY);
                }
            }
            if (texBounds != null) {
                int texOffset = i * 2;
                texBounds[0] = Math.min(texBounds[0], texCoordValues[texOffset]);
                texBounds[1] = Math.max(texBounds[1], texCoordValues[texOffset]);
                texBounds[2] = Math.min(texBounds[2], texCoordValues[texOffset + 1]);
                texBounds[3] = Math.max(texBounds[3], texCoordValues[texOffset + 1]);
            }
            if (texCoordValues != null) {
                populateHardwareTexCoord(texCoordValues, i, sourceVertexIndex);
                if (ogl.lastError != GraphicsOGL.GL_NO_ERROR) {
                    return null;
                }
            }
            if (colorValues != null) {
                populateHardwareColor(colorValues, i, sourceVertexIndex);
                if (ogl.lastError != GraphicsOGL.GL_NO_ERROR) {
                    return null;
                }
                if (colorMin != null) {
                    int colorOffset = i * 4;
                    for (int c = 0; c < 4; c++) {
                        int value = colorValues[colorOffset + c] & 0xFF;
                        colorMin[c] = Math.min(colorMin[c], value);
                        colorMax[c] = Math.max(colorMax[c], value);
                        colorSum[c] += value;
                    }
                }
            }
            if (normalValues != null) {
                populateHardwareNormal(normalValues, i, sourceVertexIndex, emulateMatrixPalette, normal);
                if (ogl.lastError != GraphicsOGL.GL_NO_ERROR) {
                    return null;
                }
            }
        }
        short[] indexValues = null;
        if (indexSource != null) {
            indexValues = ensureShortArray(indexScratch, primitiveCount);
            indexScratch = indexValues;
            for (int i = 0; i < primitiveCount; i++) {
                indexValues[i] = (short) indexSource.indexAt(i);
            }
        }
        if (traceGeometry) {
            int signature = java.util.Objects.hash(primitiveCount, ogl.boundTextureId, ogl.textureEnvMode,
                    ogl.blendSrcFactor, ogl.blendDstFactor, ogl.alphaTestEnabled, emulateMatrixPalette);
            if (loggedGeometrySignatures.add(signature)) {
                lastGeometryTrace = " trace{mp=" + (emulateMatrixPalette ? 1 : 0)
                        + " ext=" + (extensionMatrices ? 1 : 0)
                        + " vcount=" + vertexCount
                        + " cw=" + clipBounds[6] + ".." + clipBounds[7]
                        + " cx=" + clipBounds[0] + ".." + clipBounds[1]
                        + " cy=" + clipBounds[2] + ".." + clipBounds[3]
                        + " cz=" + clipBounds[4] + ".." + clipBounds[5]
                        + " behindW=" + behindW
                        + (texBounds == null ? " uv=-" : " u=" + texBounds[0] + ".." + texBounds[1]
                        + " v=" + texBounds[2] + ".." + texBounds[3]) + "}";
                if (colorMin != null) {
                    lastGeometryTrace += " col min=" + colorMin[0] + "," + colorMin[1] + "," + colorMin[2] + ","
                            + colorMin[3] + " max=" + colorMax[0] + "," + colorMax[1] + "," + colorMax[2] + ","
                            + colorMax[3] + " avg=" + (colorSum[0] / vertexCount) + "," + (colorSum[1] / vertexCount)
                            + "," + (colorSum[2] / vertexCount) + "," + (colorSum[3] / vertexCount);
                }
            }
        }
        if (effectDraw && loggedEffectDraws < 48) {
            int effectSignature = java.util.Objects.hash(primitiveCount, ogl.boundTextureId,
                    ogl.colorArrayEnabled, colorValues == null ? -1 : java.util.Arrays.hashCode(
                            new int[]{colorMin[0], colorMax[0], colorMin[1], colorMax[1], colorMin[3], colorMax[3]}),
                    texBounds == null ? -2 : Math.round(texBounds[0] * 8f));
            if (loggedEffectSignatures.add(effectSignature)) {
                loggedEffectDraws++;
                System.out.println("INFO [jam-launch] [HWDIAG] effdraw"
                        + " count=" + primitiveCount + " vcount=" + vertexCount
                        + " id=" + ogl.boundTextureId
                        + " blend=" + (ogl.blendCapEnabled ? 1 : 0) + "/" + hex(ogl.blendSrcFactor)
                        + "/" + hex(ogl.blendDstFactor)
                        + " env=" + hex(ogl.textureEnvMode) + " cRgb=" + hex(ogl.combineRgb)
                        + " arr=" + (ogl.colorArrayEnabled ? 1 : 0)
                        + " cur=" + hex(ogl.color)
                        + (colorValues == null ? " col=none"
                        : " col min=" + colorMin[0] + "," + colorMin[1] + "," + colorMin[2] + "," + colorMin[3]
                        + " max=" + colorMax[0] + "," + colorMax[1] + "," + colorMax[2] + "," + colorMax[3]
                        + " avg=" + (colorSum[0] / vertexCount) + "," + (colorSum[1] / vertexCount)
                        + "," + (colorSum[2] / vertexCount) + "," + (colorSum[3] / vertexCount))
                        + (texBounds == null ? " uv=-" : " u=" + texBounds[0] + ".." + texBounds[1]
                        + " v=" + texBounds[2] + ".." + texBounds[3])
                        + (ndcBounds == null || ndcBounds[0] > ndcBounds[1] ? " ndc=-"
                        : " ndc x=" + Math.round(ndcBounds[0] * 100f) / 100f + ".." + Math.round(ndcBounds[1] * 100f) / 100f
                        + " y=" + Math.round(ndcBounds[2] * 100f) / 100f + ".." + Math.round(ndcBounds[3] * 100f) / 100f)
                        + " w=" + clipBounds[6] + ".." + clipBounds[7]
                        + (lastGeometryTrace == null ? "" : lastGeometryTrace));
            }
        }
        return new PreparedArrayState(
                toReusableFloatBuffer(vertexValues, vertexFloatCount, BufferType.VERTEX),
                normalValues == null ? null : toReusableFloatBuffer(normalValues, normalFloatCount, BufferType.NORMAL),
                texCoordValues == null ? null : toReusableFloatBuffer(texCoordValues, texCoordFloatCount, BufferType.TEX_COORD),
                colorValues == null ? null : toReusableByteBuffer(colorValues, colorByteCount, BufferType.COLOR),
                indexValues == null ? null : toReusableShortBuffer(indexValues, primitiveCount),
                0,
                primitiveCount,
                emulateMatrixPalette
        );
    }

    private void populateHardwareVertex(float[] destination, int vertexSlot, int sourceVertexIndex,
                                        boolean emulateMatrixPalette, OglRenderer.ClipVector clip, OglRenderer.ClipVector eye) {
        OglRenderer.OglState ogl = owner.oglState();
        int positionSize = Math.max(1, ogl.vertexPointer.size());
        float x = owner.readFloatComponent(ogl.vertexPointer, sourceVertexIndex, 0);
        float y = positionSize > 1 ? owner.readFloatComponent(ogl.vertexPointer, sourceVertexIndex, 1) : 0f;
        float z = positionSize > 2 ? owner.readFloatComponent(ogl.vertexPointer, sourceVertexIndex, 2) : 0f;
        float w = positionSize > 3 ? owner.readFloatComponent(ogl.vertexPointer, sourceVertexIndex, 3) : 1f;
        int destinationOffset = vertexSlot * 4;
        if (emulateMatrixPalette) {
            owner.transformVertex(clip, eye, x, y, z, sourceVertexIndex, false);
            destination[destinationOffset] = eye.x;
            destination[destinationOffset + 1] = eye.y;
            destination[destinationOffset + 2] = eye.z;
            destination[destinationOffset + 3] = eye.w;
            return;
        }
        destination[destinationOffset] = x;
        destination[destinationOffset + 1] = y;
        destination[destinationOffset + 2] = z;
        destination[destinationOffset + 3] = w;
    }

    private void populateHardwareTexCoord(float[] destination, int vertexSlot, int sourceVertexIndex) {
        OglRenderer.OglPointer texCoordPointer = owner.oglState().texCoordPointer;
        if (texCoordPointer == null) {
            return;
        }
        int size = Math.max(1, texCoordPointer.size());
        int destinationOffset = vertexSlot * 2;
        destination[destinationOffset] = owner.readFloatComponent(texCoordPointer, sourceVertexIndex, 0);
        destination[destinationOffset + 1] = size > 1 ? owner.readFloatComponent(texCoordPointer, sourceVertexIndex, 1) : 0f;
    }

    private void populateHardwareColor(byte[] destination, int vertexSlot, int sourceVertexIndex) {
        OglRenderer.OglPointer colorPointer = owner.oglState().colorPointer;
        if (colorPointer == null) {
            return;
        }
        int size = Math.max(1, colorPointer.size());
        int destinationOffset = vertexSlot * 4;
        int red = owner.readColorComponent(colorPointer, sourceVertexIndex, 0);
        int green = size > 1 ? owner.readColorComponent(colorPointer, sourceVertexIndex, 1) : red;
        int blue = size > 2 ? owner.readColorComponent(colorPointer, sourceVertexIndex, 2) : red;
        int alpha = size > 3 ? owner.readColorComponent(colorPointer, sourceVertexIndex, 3) : 255;
        destination[destinationOffset] = (byte) red;
        destination[destinationOffset + 1] = (byte) green;
        destination[destinationOffset + 2] = (byte) blue;
        destination[destinationOffset + 3] = (byte) alpha;
    }

    private void populateHardwareNormal(float[] destination, int vertexSlot, int sourceVertexIndex,
                                        boolean emulateMatrixPalette, OglRenderer.ClipVector normal) {
        OglRenderer.OglState ogl = owner.oglState();
        int destinationOffset = vertexSlot * 3;
        if (emulateMatrixPalette) {
            float nx = ogl.normalArrayEnabled && ogl.normalPointer != null
                    ? owner.readNormalComponent(ogl.normalPointer, sourceVertexIndex, 0)
                    : ogl.currentNormal[0];
            float ny = ogl.normalArrayEnabled && ogl.normalPointer != null
                    ? owner.readNormalComponent(ogl.normalPointer, sourceVertexIndex, 1)
                    : ogl.currentNormal[1];
            float nz = ogl.normalArrayEnabled && ogl.normalPointer != null
                    ? owner.readNormalComponent(ogl.normalPointer, sourceVertexIndex, 2)
                    : ogl.currentNormal[2];
            if (ogl.lastError != GraphicsOGL.GL_NO_ERROR) {
                return;
            }
            owner.transformNormal(normal, nx, ny, nz, sourceVertexIndex);
            destination[destinationOffset] = normal.x;
            destination[destinationOffset + 1] = normal.y;
            destination[destinationOffset + 2] = normal.z;
            return;
        }
        destination[destinationOffset] = ogl.normalArrayEnabled && ogl.normalPointer != null
                ? owner.readNormalComponent(ogl.normalPointer, sourceVertexIndex, 0)
                : ogl.currentNormal[0];
        destination[destinationOffset + 1] = ogl.normalArrayEnabled && ogl.normalPointer != null
                ? owner.readNormalComponent(ogl.normalPointer, sourceVertexIndex, 1)
                : ogl.currentNormal[1];
        destination[destinationOffset + 2] = ogl.normalArrayEnabled && ogl.normalPointer != null
                ? owner.readNormalComponent(ogl.normalPointer, sourceVertexIndex, 2)
                : ogl.currentNormal[2];
    }

    private int resolveVertexCount(int primitiveCount, OglRenderer.OglIndexSource indexSource) {
        int maxIndex = -1;
        for (int i = 0; i < primitiveCount; i++) {
            maxIndex = Math.max(maxIndex, indexSource.indexAt(i));
        }
        return maxIndex + 1;
    }

    private int loggedUnsupportedModes;
    private int loggedArrayStateFailures;
    private final java.util.Set<Integer> loggedStateSignatures = new java.util.HashSet<>();
    private final java.util.Set<Integer> loggedGeometrySignatures = new java.util.HashSet<>();
    private final java.util.Set<Integer> loggedEffectSignatures = new java.util.HashSet<>();
    private int loggedEffectDraws;
    private static final boolean TRACE_DRAW_GEOMETRY = Boolean.getBoolean("opendoja.traceDrawGeometry");
    private static final boolean CAPTURE_FRAMES = Boolean.getBoolean("opendoja.captureFrames");
    private static final int EFFECT_DEBUG = Integer.getInteger("opendoja.effectDebug", 0);
    private boolean effectDebugLogged;
    private static final java.io.File CAPTURE_DIR =
            opendoja.host.OpenDoJaPaths.hostDataRoot().resolve("cap").toFile();
    private boolean frameContainsEffect;
    private int capturedFrames;
    private int capturedTextures;
    private String lastGeometryTrace;
    private int loggedUploads;
    private int loggedDrawErrors;
    private int loggedContextRecreations;
    private int loggedReadbackMismatches;
    private boolean glResourcesValid = true;

    private void applyEffectDebugOverride() {
        int debugMode = EFFECT_DEBUG;
        if (debugMode == 0) {
            return;
        }
        if (!effectDebugLogged) {
            effectDebugLogged = true;
            System.out.println("INFO [jam-launch] [HWDIAG] effectDebug mode=" + debugMode);
        }
        switch (debugMode) {
            case 1 -> GLES10.glDisable(GraphicsOGL.GL_DEPTH_TEST);
            case 2 -> {
                GLES10.glDisable(GraphicsOGL.GL_ALPHA_TEST);
                GLES10.glDisable(GraphicsOGL.GL_TEXTURE_2D);
                GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
                GLES10.glColor4f(1f, 0f, 1f, 1f);
            }
            case 3 -> GLES10.glDisable(GraphicsOGL.GL_ALPHA_TEST);
            case 4 -> {
                GLES10.glDisable(GraphicsOGL.GL_ALPHA_TEST);
                GLES10.glEnable(GraphicsOGL.GL_BLEND);
                GLES10.glBlendFunc(GLES10.GL_ONE, GLES10.GL_ONE);
            }
            case 5 -> GLES10.glDepthFunc(GraphicsOGL.GL_LEQUAL);
            case 7 -> GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_TEXTURE_ENV_MODE,
                    GraphicsOGL.GL_MODULATE);
            case 8 -> {
                GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
                GLES10.glColor4f(1f, 1f, 1f, 1f);
            }
            case 9 -> {
                GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_TEXTURE_ENV_MODE,
                        GraphicsOGL.GL_MODULATE);
                GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
                GLES10.glColor4f(1f, 1f, 1f, 1f);
            }
            default -> {
            }
        }
    }

    private void logSmallDrawState(int mode, int count) {
        OglRenderer.OglState ogl = owner.oglState();
        int signature = java.util.Objects.hash(mode, count, ogl.blendCapEnabled, ogl.blendSrcFactor,
                ogl.blendDstFactor, ogl.depthEnabled(), ogl.depthFunc, ogl.depthMask,
                ogl.alphaTestEnabled, ogl.alphaFunc, Float.floatToIntBits(ogl.alphaRef),
                ogl.textureEnabled(), ogl.boundTextureId, ogl.textureEnvMode,
                ogl.lightingEnabled(), ogl.cullFaceEnabled, ogl.colorArrayEnabled, ogl.color,
                ogl.combineRgb, ogl.combineAlpha, ogl.rgbScale, ogl.alphaScale,
                ogl.textureEnvColor,
                java.util.Arrays.hashCode(ogl.srcRgb), java.util.Arrays.hashCode(ogl.operandRgb),
                java.util.Arrays.hashCode(ogl.srcAlpha), java.util.Arrays.hashCode(ogl.operandAlpha));
        if (loggedStateSignatures.contains(signature) || loggedStateSignatures.size() >= 400) {
            return;
        }
        loggedStateSignatures.add(signature);
        OglRenderer.OglTexture boundTex = ogl.boundTexture();
        System.out.println("INFO [jam-launch] [HWDIAG] draw mode=" + mode + " count=" + count
                + " blend=" + (ogl.blendCapEnabled ? 1 : 0) + "/" + hex(ogl.blendSrcFactor)
                + "/" + hex(ogl.blendDstFactor)
                + " depth=" + (ogl.depthEnabled() ? 1 : 0) + "/" + hex(ogl.depthFunc)
                + " dmask=" + (ogl.depthMask ? 1 : 0)
                + " alpha=" + (ogl.alphaTestEnabled ? 1 : 0) + "/" + hex(ogl.alphaFunc)
                + "/" + ogl.alphaRef
                + " tex=" + (ogl.textureEnabled() ? 1 : 0) + " id=" + ogl.boundTextureId
                + " tf=" + (boundTex == null ? "-" : hex(boundTex.baseFormat())
                        + (boundTex.pixels.length == 0 ? "EMPTY" : ""))
                + " env=" + hex(ogl.textureEnvMode)
                + " envColor=" + hex(ogl.textureEnvColor)
                + " cRgb=" + hex(ogl.combineRgb) + " cA=" + hex(ogl.combineAlpha)
                + " srcR=" + hex(ogl.srcRgb[0]) + "," + hex(ogl.srcRgb[1]) + "," + hex(ogl.srcRgb[2])
                + " opR=" + hex(ogl.operandRgb[0]) + "," + hex(ogl.operandRgb[1]) + "," + hex(ogl.operandRgb[2])
                + " srcA=" + hex(ogl.srcAlpha[0]) + "," + hex(ogl.srcAlpha[1]) + "," + hex(ogl.srcAlpha[2])
                + " opA=" + hex(ogl.operandAlpha[0]) + "," + hex(ogl.operandAlpha[1]) + "," + hex(ogl.operandAlpha[2])
                + " rgbS=" + ogl.rgbScale + " aS=" + ogl.alphaScale
                + " light=" + (ogl.lightingEnabled() ? 1 : 0)
                + " cull=" + (ogl.cullFaceEnabled ? 1 : 0)
                + " colorArr=" + (ogl.colorArrayEnabled ? 1 : 0)
                + " curColor=" + hex(ogl.color)
                + (lastGeometryTrace == null ? "" : lastGeometryTrace));
    }

    private static String hex(int value) {
        return "0x" + Integer.toHexString(value);
    }

    private static void dumpCapture(String name, int[] pixels) {
        try {
            if (!CAPTURE_DIR.isDirectory() && !CAPTURE_DIR.mkdirs() && !CAPTURE_DIR.isDirectory()) {
                return;
            }
            java.io.File target = new java.io.File(CAPTURE_DIR, name + ".argb");
            try (java.io.DataOutputStream out = new java.io.DataOutputStream(
                    new java.io.BufferedOutputStream(new java.io.FileOutputStream(target)))) {
                for (int pixel : pixels) {
                    out.writeInt(pixel);
                }
            }
            System.out.println("INFO [jam-launch] [HWDIAG] capture " + target.getAbsolutePath()
                    + " n=" + pixels.length);
        } catch (Exception exception) {
            System.out.println("INFO [jam-launch] [HWDIAG] capture failed " + name + ": " + exception);
        }
    }

    private int toHardwarePrimitiveMode(int mode) {
        return switch (mode) {
            case GraphicsOGL.GL_TRIANGLES -> GraphicsOGL.GL_TRIANGLES;
            case GraphicsOGL.GL_TRIANGLE_STRIP -> GraphicsOGL.GL_TRIANGLE_STRIP;
            case GraphicsOGL.GL_TRIANGLE_FAN -> GraphicsOGL.GL_TRIANGLE_FAN;
            case GraphicsOGL.GL_LINE_LOOP -> GraphicsOGL.GL_LINE_LOOP;
            case GraphicsOGL.GL_LINE_STRIP -> GraphicsOGL.GL_LINE_STRIP;
            case GraphicsOGL.GL_LINES -> GraphicsOGL.GL_LINES;
            case GraphicsOGL.GL_POINTS -> GraphicsOGL.GL_POINTS;
            default -> -1;
        };
    }

    private void logUnsupportedModeOnce(int mode) {
        if (mode < 0 || mode >= 31) {
            return;
        }
        int bit = 1 << mode;
        if ((loggedUnsupportedModes & bit) == 0) {
            loggedUnsupportedModes |= bit;
            OpenDoJaLog.warn(OglRenderer.class,
                    "Hardware backend: unsupported primitive mode=" + mode + ", falling back to software draw");
        }
    }

    private void prepareFramebuffer() {
        if (!surfaceDirty) {
            return;
        }
        syncSoftwareSurface();
        surfaceDirty = false;
    }

    private void syncSoftwareSurface() {
        DesktopSurface surface = owner.host().surface();
        int width = surface.width();
        int height = surface.height();
        int[] surfacePixels = new int[width * height];
        surface.image().getBitmap().getPixels(surfacePixels, 0, width, 0, 0, width, height);
        ByteBuffer uploadBuffer = toRgbaByteBuffer(surfacePixels, width, height, true, surfaceUploadBuffer);
        surfaceUploadBuffer = uploadBuffer;
        if (surfaceTextureId == 0) {
            int[] textureIds = new int[1];
            GLES10.glGenTextures(1, textureIds, 0);
            surfaceTextureId = textureIds[0];
        }
        GLES10.glBindTexture(GraphicsOGL.GL_TEXTURE_2D, surfaceTextureId);
        GLES10.glTexParameterx(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_MIN_FILTER, GraphicsOGL.GL_NEAREST);
        GLES10.glTexParameterx(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_MAG_FILTER, GraphicsOGL.GL_NEAREST);
        GLES10.glTexParameterx(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_WRAP_S, GraphicsOGL.GL_CLAMP_TO_EDGE);
        GLES10.glTexParameterx(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_WRAP_T, GraphicsOGL.GL_CLAMP_TO_EDGE);
        GLES10.glPixelStorei(GLES10.GL_UNPACK_ALIGNMENT, 1);
        if (surfaceTextureWidth != width || surfaceTextureHeight != height) {
            GLES10.glTexImage2D(GraphicsOGL.GL_TEXTURE_2D, 0, GLES10.GL_RGBA, width, height, 0,
                    GLES10.GL_RGBA, GLES10.GL_UNSIGNED_BYTE, uploadBuffer);
            surfaceTextureWidth = width;
            surfaceTextureHeight = height;
        } else {
            GLES10.glTexSubImage2D(GraphicsOGL.GL_TEXTURE_2D, 0, 0, 0, width, height,
                    GLES10.GL_RGBA, GLES10.GL_UNSIGNED_BYTE, uploadBuffer);
        }
        GLES10.glDisable(GraphicsOGL.GL_SCISSOR_TEST);
        GLES10.glViewport(0, 0, renderWidth(), renderHeight());
        GLES10.glMatrixMode(GraphicsOGL.GL_PROJECTION);
        GLES10.glLoadIdentity();
        GLES10.glMatrixMode(GraphicsOGL.GL_MODELVIEW);
        GLES10.glLoadIdentity();
        GLES10.glMatrixMode(GraphicsOGL.GL_TEXTURE);
        GLES10.glLoadIdentity();
        GLES10.glDisable(GraphicsOGL.GL_BLEND);
        GLES10.glDisable(GraphicsOGL.GL_ALPHA_TEST);
        GLES10.glDisable(GraphicsOGL.GL_LIGHTING);
        GLES10.glDisable(GraphicsOGL.GL_CULL_FACE);
        GLES10.glDisable(GraphicsOGL.GL_DEPTH_TEST);
        GLES10.glDepthMask(false);
        GLES10.glEnable(GraphicsOGL.GL_TEXTURE_2D);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_TEXTURE_ENV_MODE, GraphicsOGL.GL_REPLACE);
        GLES10.glColor4f(1f, 1f, 1f, 1f);
        drawFullscreenQuad();
        clearOutsideLockOverlay();
    }

    /** Fullscreen textured quad via client arrays (GLES has no immediate mode). */
    private void drawFullscreenQuad() {
        if (quadPositionBuffer == null) {
            quadPositionBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            quadPositionBuffer.put(new float[]{-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f}).position(0);
            quadTexCoordBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            quadTexCoordBuffer.put(new float[]{0f, 0f, 1f, 0f, 0f, 1f, 1f, 1f}).position(0);
        }
        GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);
        GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
        GLES10.glVertexPointer(2, GLES10.GL_FLOAT, 0, quadPositionBuffer);
        GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, quadTexCoordBuffer);
        GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4);
        GLES10.glDisableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
        GLES10.glDisableClientState(GLES10.GL_VERTEX_ARRAY);
    }

    private void clearOutsideLockOverlay() {
        outsideLockOverlaySnapshot = null;
        outsideLockOverlayBounds = null;
    }

    private void applyClipState() {
        Rectangle clip = owner.host().delegate().getClipBounds();
        if (clip == null) {
            GLES10.glDisable(GraphicsOGL.GL_SCISSOR_TEST);
            return;
        }
        Rectangle clipped = clip.intersection(new Rectangle(0, 0, owner.host().surface().width(), owner.host().surface().height()));
        if (clipped.isEmpty()) {
            GLES10.glEnable(GraphicsOGL.GL_SCISSOR_TEST);
            GLES10.glScissor(0, 0, 0, 0);
            return;
        }
        int scale = supersampleScale();
        GLES10.glEnable(GraphicsOGL.GL_SCISSOR_TEST);
        GLES10.glScissor(
                clipped.x * scale,
                renderHeight() - ((clipped.y + clipped.height) * scale),
                clipped.width * scale,
                clipped.height * scale);
    }

    private void applyViewportState() {
        OglRenderer.OglState ogl = owner.oglState();
        int scale = supersampleScale();
        GLES10.glViewport(
                ogl.viewportX * scale,
                ogl.viewportY * scale,
                Math.max(1, ogl.viewportWidth * scale),
                Math.max(1, ogl.viewportHeight * scale));
    }

    private void applyRenderState(boolean emulateMatrixPalette) {
        OglRenderer.OglState ogl = owner.oglState();
        float[] modelViewMatrix = emulateMatrixPalette ? OglRenderer.OglState.identityMatrix() : currentHardwareModelViewMatrix();
        float[] projectionMatrix = hardwareProjectionMatrix(
                emulateMatrixPalette ? ogl.projectionMatrix : currentHardwareProjectionMatrix());

        GLES10.glMatrixMode(GraphicsOGL.GL_PROJECTION);
        GLES10.glLoadMatrixf(projectionMatrix, 0);
        GLES10.glMatrixMode(GraphicsOGL.GL_MODELVIEW);
        GLES10.glLoadIdentity();
        if (ogl.depthEnabled()) {
            GLES10.glEnable(GraphicsOGL.GL_DEPTH_TEST);
            GLES10.glDepthFunc(toHardwareDepthFunc(ogl.depthFunc));
        } else {
            GLES10.glDisable(GraphicsOGL.GL_DEPTH_TEST);
        }
        GLES10.glDepthMask(ogl.depthMask);
        GLES10.glDepthRangef(1.0f - ogl.depthRangeNear, 1.0f - ogl.depthRangeFar);
        if (ogl.blendEnabled()) {
            GLES10.glEnable(GraphicsOGL.GL_BLEND);
            GLES10.glBlendFunc(ogl.blendSrcFactor, ogl.blendDstFactor);
        } else {
            GLES10.glDisable(GraphicsOGL.GL_BLEND);
        }
        if (ogl.alphaTestEnabled) {
            GLES10.glEnable(GraphicsOGL.GL_ALPHA_TEST);
            GLES10.glAlphaFunc(ogl.alphaFunc, ogl.alphaRef);
        } else {
            GLES10.glDisable(GraphicsOGL.GL_ALPHA_TEST);
        }
        if (ogl.cullFaceEnabled) {
            GLES10.glEnable(GraphicsOGL.GL_CULL_FACE);
            GLES10.glFrontFace(ogl.frontFace);
            GLES10.glCullFace(ogl.cullFace);
        } else {
            GLES10.glDisable(GraphicsOGL.GL_CULL_FACE);
        }
        if (ogl.normalizeEnabled) {
            GLES10.glEnable(GraphicsOGL.GL_NORMALIZE);
        } else {
            GLES10.glDisable(GraphicsOGL.GL_NORMALIZE);
        }
        if (ogl.rescaleNormalEnabled) {
            GLES11.glEnable(GraphicsOGL.GL_RESCALE_NORMAL);
        } else {
            GLES11.glDisable(GraphicsOGL.GL_RESCALE_NORMAL);
        }
        GLES10.glShadeModel(ogl.shadeModel);
        float[] currentColor = owner.unpackColor(ogl.color);
        GLES10.glColor4f(currentColor[0], currentColor[1], currentColor[2], currentColor[3]);
        GLES10.glNormal3f(ogl.currentNormal[0], ogl.currentNormal[1], ogl.currentNormal[2]);
        if (ogl.colorMaterialEnabled) {
            GLES10.glEnable(GraphicsOGL.GL_COLOR_MATERIAL);
            // GLES10 omits glColorMaterial; its ES default (FRONT_AND_BACK + AMBIENT_AND_DIFFUSE)
            // is exactly the state the desktop backend set explicitly.
        } else {
            GLES10.glDisable(GraphicsOGL.GL_COLOR_MATERIAL);
        }
        GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_AMBIENT, ogl.materialAmbient, 0);
        GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_DIFFUSE, ogl.materialDiffuse, 0);
        GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_SPECULAR, ogl.materialSpecular, 0);
        GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_EMISSION, ogl.materialEmission, 0);
        GLES10.glMaterialf(GLES10.GL_FRONT_AND_BACK, GLES10.GL_SHININESS, ogl.materialShininess);
        GLES10.glLightModelfv(GLES10.GL_LIGHT_MODEL_AMBIENT, ogl.lightModelAmbient, 0);
        GLES10.glLightModelx(GLES10.GL_LIGHT_MODEL_TWO_SIDE, ogl.lightModelTwoSide ? 1 : 0);
        for (int i = 0; i < ogl.lights.length; i++) {
            int lightEnum = GraphicsOGL.GL_LIGHT0 + i;
            OglRenderer.OglLight light = ogl.lights[i];
            if (ogl.lightEnabled[i]) {
                GLES10.glEnable(lightEnum);
            } else {
                GLES10.glDisable(lightEnum);
            }
            GLES10.glLightfv(lightEnum, GLES10.GL_AMBIENT, light.ambient, 0);
            GLES10.glLightfv(lightEnum, GLES10.GL_DIFFUSE, light.diffuse, 0);
            GLES10.glLightfv(lightEnum, GLES10.GL_SPECULAR, light.specular, 0);
            GLES10.glLightfv(lightEnum, GLES10.GL_POSITION, light.position, 0);
            GLES10.glLightfv(lightEnum, GLES10.GL_SPOT_DIRECTION, light.spotDirection, 0);
            GLES10.glLightf(lightEnum, GLES10.GL_SPOT_EXPONENT, light.spotExponent);
            GLES10.glLightf(lightEnum, GLES10.GL_SPOT_CUTOFF, light.spotCutoff);
            GLES10.glLightf(lightEnum, GLES10.GL_CONSTANT_ATTENUATION, light.constantAttenuation);
            GLES10.glLightf(lightEnum, GLES10.GL_LINEAR_ATTENUATION, light.linearAttenuation);
            GLES10.glLightf(lightEnum, GLES10.GL_QUADRATIC_ATTENUATION, light.quadraticAttenuation);
        }
        if (ogl.lightingEnabled()) {
            GLES10.glEnable(GraphicsOGL.GL_LIGHTING);
        } else {
            GLES10.glDisable(GraphicsOGL.GL_LIGHTING);
        }
        GLES10.glMatrixMode(GraphicsOGL.GL_MODELVIEW);
        GLES10.glLoadMatrixf(modelViewMatrix, 0);
    }

    private float[] hardwareProjectionMatrix(float[] baseProjectionMatrix) {
        return baseProjectionMatrix;
    }

    private float[] currentHardwareModelViewMatrix() {
        if (owner.oglState().usesExtensionMatrices()) {
            float[] matrix = owner.extensionWorldMatrix();
            return matrix == null ? OglRenderer.OglState.identityMatrix() : matrix;
        }
        return owner.oglState().modelViewMatrix;
    }

    private float[] currentHardwareProjectionMatrix() {
        if (owner.oglState().usesExtensionMatrices()) {
            float[] matrix = owner.extensionCameraMatrix();
            return matrix == null ? OglRenderer.OglState.identityMatrix() : matrix;
        }
        return owner.oglState().projectionMatrix;
    }

    private void applyTextureState() {
        OglRenderer.OglState ogl = owner.oglState();
        if (!ogl.textureEnabled()) {
            GLES10.glDisable(GraphicsOGL.GL_TEXTURE_2D);
            return;
        }
        OglRenderer.OglTexture texture = ogl.boundTexture();
        if (texture == null) {
            GLES10.glDisable(GraphicsOGL.GL_TEXTURE_2D);
            return;
        }
        GLES10.glEnable(GraphicsOGL.GL_TEXTURE_2D);
        HardwareTexture hardwareTexture = textureCache.computeIfAbsent(ogl.boundTextureId,
                ignored -> new HardwareTexture(0, -1, -1, -1, -1));
        int textureId = hardwareTexture.textureId();
        if (textureId == 0) {
            int[] textureIds = new int[1];
            GLES10.glGenTextures(1, textureIds, 0);
            textureId = textureIds[0];
            hardwareTexture = new HardwareTexture(textureId, -1, -1, -1, -1);
            textureCache.put(ogl.boundTextureId, hardwareTexture);
        }
        GLES10.glBindTexture(GraphicsOGL.GL_TEXTURE_2D, textureId);
        GLES10.glPixelStorei(GLES10.GL_UNPACK_ALIGNMENT, 1);
        if (hardwareTexture.uploadedRevision() != texture.uploadRevision()) {
            TextureUpload textureUpload = prepareTextureUpload(texture, false);
            if (loggedUploads < 150) {
                loggedUploads++;
                int zeroAlpha = 0;
                int maxAlpha = 0;
                long sumAlpha = 0;
                int sampled = 0;
                int step = Math.max(1, texture.pixels.length / 4096);
                for (int p = 0; p < texture.pixels.length; p += step) {
                    int a = (texture.pixels[p] >>> 24) & 0xFF;
                    if (a == 0) {
                        zeroAlpha++;
                    }
                    if (a > maxAlpha) {
                        maxAlpha = a;
                    }
                    sumAlpha += a;
                    sampled++;
                }
                System.out.println("INFO [jam-launch] [HWDIAG] tex upload id=" + ogl.boundTextureId
                        + " new=" + texture.width + "x" + texture.height
                        + " stored=" + hardwareTexture.width() + "x" + hardwareTexture.height()
                        + " fmt=0x" + Integer.toHexString(textureUpload.format())
                        + " rev=" + texture.uploadRevision()
                        + " wasRev=" + hardwareTexture.uploadedRevision()
                        + " a0=" + zeroAlpha + "/" + sampled
                        + " aMax=" + maxAlpha
                        + " aAvg=" + (sampled == 0 ? 0 : sumAlpha / sampled));
            }
            if (CAPTURE_FRAMES && capturedTextures < 40 && hardwareTexture.uploadedRevision() < 0) {
                capturedTextures++;
                dumpCapture("tex_" + capturedTextures + "_id" + ogl.boundTextureId
                        + "_" + texture.width + "x" + texture.height, texture.pixels);
            }
            if (hardwareTexture.matches(texture.uploadRevision(), texture.width, texture.height, textureUpload.format())) {                GLES10.glTexSubImage2D(GraphicsOGL.GL_TEXTURE_2D, 0, 0, 0, texture.width, texture.height,
                        textureUpload.format(), GLES10.GL_UNSIGNED_BYTE, textureUpload.buffer());
            } else {
                GLES10.glTexImage2D(GraphicsOGL.GL_TEXTURE_2D, 0, textureUpload.format(), texture.width, texture.height, 0,
                        textureUpload.format(), GLES10.GL_UNSIGNED_BYTE, textureUpload.buffer());
            }
            hardwareTexture = new HardwareTexture(textureId, texture.uploadRevision(),
                    texture.width, texture.height, textureUpload.format());
            textureCache.put(ogl.boundTextureId, hardwareTexture);
        }
        GLES10.glTexParameterx(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_MIN_FILTER,
                sanitizeHardwareMinFilter(texture.minFilter));
        GLES10.glTexParameterx(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_MAG_FILTER,
                sanitizeHardwareMagFilter(texture.magFilter));
        GLES10.glTexParameterx(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_WRAP_S,
                sanitizeHardwareWrap(texture.wrapS, texture.width));
        GLES10.glTexParameterx(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_WRAP_T,
                sanitizeHardwareWrap(texture.wrapT, texture.height));
        GLES10.glMatrixMode(GraphicsOGL.GL_TEXTURE);
        GLES10.glLoadMatrixf(ogl.textureMatrix, 0);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_TEXTURE_ENV_MODE, ogl.textureEnvMode);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_COMBINE_RGB, ogl.combineRgb);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_COMBINE_ALPHA, ogl.combineAlpha);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_SRC0_RGB, ogl.srcRgb[0]);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_SRC1_RGB, ogl.srcRgb[1]);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_SRC2_RGB, ogl.srcRgb[2]);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_SRC0_ALPHA, ogl.srcAlpha[0]);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_SRC1_ALPHA, ogl.srcAlpha[1]);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_SRC2_ALPHA, ogl.srcAlpha[2]);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_OPERAND0_RGB, ogl.operandRgb[0]);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_OPERAND1_RGB, ogl.operandRgb[1]);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_OPERAND2_RGB, ogl.operandRgb[2]);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_OPERAND0_ALPHA, ogl.operandAlpha[0]);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_OPERAND1_ALPHA, ogl.operandAlpha[1]);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_OPERAND2_ALPHA, ogl.operandAlpha[2]);
        GLES10.glTexEnvfv(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_TEXTURE_ENV_COLOR, owner.unpackColor(ogl.textureEnvColor), 0);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_RGB_SCALE, ogl.rgbScale);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_ALPHA_SCALE, ogl.alphaScale);
        GLES10.glMatrixMode(GraphicsOGL.GL_MODELVIEW);
    }

    private void bindArrayState(PreparedArrayState arrayState) {
        OglRenderer.OglState ogl = owner.oglState();
        GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);
        GLES10.glVertexPointer(4, GLES10.GL_FLOAT, 0, arrayState.vertexBuffer());
        if (arrayState.normalBuffer() != null && ogl.lightingEnabled()) {
            GLES10.glEnableClientState(GLES10.GL_NORMAL_ARRAY);
            GLES10.glNormalPointer(GLES10.GL_FLOAT, 0, arrayState.normalBuffer());
        } else {
            GLES10.glDisableClientState(GLES10.GL_NORMAL_ARRAY);
        }
        if (arrayState.colorBuffer() != null) {
            GLES10.glEnableClientState(GLES10.GL_COLOR_ARRAY);
            GLES10.glColorPointer(4, GLES10.GL_UNSIGNED_BYTE, 0, arrayState.colorBuffer());
        } else {
            GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
        }
        if (arrayState.texCoordBuffer() != null && ogl.textureEnabled()) {
            GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
            GLES10.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, arrayState.texCoordBuffer());
        } else {
            GLES10.glDisableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
        }
    }

    private void unbindArrayState() {
        GLES10.glDisableClientState(GLES10.GL_VERTEX_ARRAY);
        GLES10.glDisableClientState(GLES10.GL_NORMAL_ARRAY);
        GLES10.glDisableClientState(GLES10.GL_COLOR_ARRAY);
        GLES10.glDisableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
    }

    /** Reads the color buffer back into the surface OGL framebuffer (top-down rows). */
    private int[] readBackColorBuffer(DesktopSurface surface) {
        int width = surface.width();
        int height = surface.height();
        int scale = supersampleScale();
        if (scale > 1) {
            resolveSupersampledReadback(width, height, scale);
        }
        int byteCount = Math.max(1, width * height * 4);
        if (readbackBuffer == null || readbackBuffer.capacity() < byteCount) {
            readbackBuffer = ByteBuffer.allocateDirect(byteCount).order(ByteOrder.nativeOrder());
        }
        readbackBuffer.clear();
        readbackBuffer.limit(byteCount);
        if (!bgraReadDetected) {
            String extensions = GLES10.glGetString(GLES10.GL_EXTENSIONS);
            bgraReadDetected = extensions != null && extensions.contains("GL_EXT_read_format_bgra");
        }
        if (bgraReadDetected) {
            GLES10.glReadPixels(0, 0, width, height, GL_BGRA_EXT, GLES10.GL_UNSIGNED_BYTE, readbackBuffer);
            if (GLES10.glGetError() != GraphicsOGL.GL_NO_ERROR) {
                bgraReadDetected = false;
                readbackBuffer.clear();
                GLES10.glReadPixels(0, 0, width, height, GLES10.GL_RGBA, GLES10.GL_UNSIGNED_BYTE, readbackBuffer);
            }
        } else {
            GLES10.glReadPixels(0, 0, width, height, GLES10.GL_RGBA, GLES10.GL_UNSIGNED_BYTE, readbackBuffer);
        }
        int[] destination = surface.acquireOglFramePixelsForReplace();
        if (bgraReadDetected) {
            IntBuffer sourcePixels = readbackBuffer.asIntBuffer();
            for (int y = 0; y < height; y++) {
                int destinationRow = y * width;
                int sourceRow = (height - 1 - y) * width;
                sourcePixels.position(sourceRow);
                sourcePixels.get(destination, destinationRow, width);
            }
        } else {
            readbackBuffer.rewind();
            byte[] raw = new byte[width * 4];
            for (int y = 0; y < height; y++) {
                readbackBuffer.position((height - 1 - y) * width * 4);
                readbackBuffer.get(raw);
                int destinationRow = y * width;
                int offset = 0;
                for (int x = 0; x < width; x++) {
                    int r = raw[offset++] & 0xFF;
                    int g = raw[offset++] & 0xFF;
                    int b = raw[offset++] & 0xFF;
                    int a = raw[offset++] & 0xFF;
                    destination[destinationRow + x] = (a << 24) | (r << 16) | (g << 8) | b;
                }
            }
        }
        return destination;
    }

    private void resolveSupersampledReadback(int width, int height, int scale) {
        int renderWidth = width * scale;
        int renderHeight = height * scale;
        if (resolveTextureId == 0) {
            int[] textureIds = new int[1];
            GLES10.glGenTextures(1, textureIds, 0);
            resolveTextureId = textureIds[0];
        }
        GLES10.glBindTexture(GraphicsOGL.GL_TEXTURE_2D, resolveTextureId);
        GLES10.glTexParameterx(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_MIN_FILTER, GraphicsOGL.GL_NEAREST);
        GLES10.glTexParameterx(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_MAG_FILTER, GraphicsOGL.GL_NEAREST);
        GLES10.glTexParameterx(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_WRAP_S, GraphicsOGL.GL_CLAMP_TO_EDGE);
        GLES10.glTexParameterx(GraphicsOGL.GL_TEXTURE_2D, GraphicsOGL.GL_TEXTURE_WRAP_T, GraphicsOGL.GL_CLAMP_TO_EDGE);
        if (resolveTextureWidth != renderWidth || resolveTextureHeight != renderHeight) {
            GLES10.glCopyTexImage2D(GraphicsOGL.GL_TEXTURE_2D, 0, GLES10.GL_RGBA, 0, 0, renderWidth, renderHeight, 0);
            resolveTextureWidth = renderWidth;
            resolveTextureHeight = renderHeight;
        } else {
            GLES10.glCopyTexSubImage2D(GraphicsOGL.GL_TEXTURE_2D, 0, 0, 0, 0, 0, renderWidth, renderHeight);
        }
        GLES10.glDisable(GraphicsOGL.GL_SCISSOR_TEST);
        GLES10.glViewport(0, 0, width, height);
        GLES10.glMatrixMode(GraphicsOGL.GL_PROJECTION);
        GLES10.glLoadIdentity();
        GLES10.glMatrixMode(GraphicsOGL.GL_MODELVIEW);
        GLES10.glLoadIdentity();
        GLES10.glDisable(GraphicsOGL.GL_ALPHA_TEST);
        GLES10.glDisable(GraphicsOGL.GL_LIGHTING);
        GLES10.glDisable(GraphicsOGL.GL_CULL_FACE);
        GLES10.glDisable(GraphicsOGL.GL_DEPTH_TEST);
        GLES10.glDepthMask(false);
        GLES10.glEnable(GraphicsOGL.GL_TEXTURE_2D);
        GLES10.glTexEnvx(GraphicsOGL.GL_TEXTURE_ENV, GraphicsOGL.GL_TEXTURE_ENV_MODE, GraphicsOGL.GL_MODULATE);
        float sampleWeight = 1f / (scale * scale);
        for (int sy = 0; sy < scale; sy++) {
            for (int sx = 0; sx < scale; sx++) {
                float translateS = ((sx + 0.5f) / renderWidth) - (0.5f / width);
                float translateT = ((sy + 0.5f) / renderHeight) - (0.5f / height);
                GLES10.glMatrixMode(GraphicsOGL.GL_TEXTURE);
                GLES10.glLoadIdentity();
                GLES10.glTranslatef(translateS, translateT, 0f);
                GLES10.glColor4f(sampleWeight, sampleWeight, sampleWeight, sampleWeight);
                if (sx == 0 && sy == 0) {
                    GLES10.glDisable(GraphicsOGL.GL_BLEND);
                } else {
                    GLES10.glEnable(GraphicsOGL.GL_BLEND);
                    GLES10.glBlendFunc(GLES10.GL_ONE, GLES10.GL_ONE);
                }
                drawFullscreenQuad();
            }
        }
        GLES10.glMatrixMode(GraphicsOGL.GL_TEXTURE);
        GLES10.glLoadIdentity();
        GLES10.glMatrixMode(GraphicsOGL.GL_MODELVIEW);
        GLES10.glDisable(GraphicsOGL.GL_BLEND);
    }

    private int reapplyOutsideLockOverlay(int[] previousHardwareSnapshot, int[] overlaySnapshot, int[] destination) {
        if (previousHardwareSnapshot == null || overlaySnapshot == null || outsideLockOverlayBounds == null
                || outsideLockOverlayBounds.isEmpty()) {
            return 0;
        }
        int applied = 0;
        int width = owner.host().surface().width();
        int left = Math.max(0, outsideLockOverlayBounds.x);
        int top = Math.max(0, outsideLockOverlayBounds.y);
        int right = Math.min(width, outsideLockOverlayBounds.x + outsideLockOverlayBounds.width);
        int bottom = Math.min(owner.host().surface().height(), outsideLockOverlayBounds.y + outsideLockOverlayBounds.height);
        for (int y = top; y < bottom; y++) {
            int row = y * width;
            for (int x = left; x < right; x++) {
                int i = row + x;
                if (i >= destination.length || i >= overlaySnapshot.length || i >= previousHardwareSnapshot.length) {
                    continue;
                }
                int overlayPixel = overlaySnapshot[i];
                if (overlayPixel == previousHardwareSnapshot[i]) {
                    continue;
                }
                destination[i] = overlayPixel;
                applied++;
            }
        }
        return applied;
    }

    private int sanitizeHardwareMinFilter(int filter) {
        return switch (filter) {
            case GraphicsOGL.GL_LINEAR,
                    GraphicsOGL.GL_LINEAR_MIPMAP_NEAREST,
                    GraphicsOGL.GL_LINEAR_MIPMAP_LINEAR -> GraphicsOGL.GL_LINEAR;
            default -> GraphicsOGL.GL_NEAREST;
        };
    }

    private int sanitizeHardwareMagFilter(int filter) {
        return filter == GraphicsOGL.GL_LINEAR ? GraphicsOGL.GL_LINEAR : GraphicsOGL.GL_NEAREST;
    }

    private boolean npotRepeatChecked;
    private boolean npotRepeatSupported;

    private synchronized boolean supportsNpotRepeat() {
        if (!npotRepeatChecked) {
            String extensions = GLES10.glGetString(GLES10.GL_EXTENSIONS);
            npotRepeatSupported = extensions != null && extensions.contains("GL_OES_texture_npot");
            npotRepeatChecked = true;
        }
        return npotRepeatSupported;
    }

    private int sanitizeHardwareWrap(int wrap, int size) {
        if (wrap == GraphicsOGL.GL_REPEAT && (isPowerOfTwo(size) || supportsNpotRepeat())) {
            return GraphicsOGL.GL_REPEAT;
        }
        return GraphicsOGL.GL_CLAMP_TO_EDGE;
    }

    private static boolean isPowerOfTwo(int value) {
        return value > 0 && (value & (value - 1)) == 0;
    }

    private int toHardwareDepthFunc(int depthFunc) {
        return switch (depthFunc) {
            case GraphicsOGL.GL_LESS -> GraphicsOGL.GL_GREATER;
            case GraphicsOGL.GL_LEQUAL -> GraphicsOGL.GL_GEQUAL;
            case GraphicsOGL.GL_GREATER -> GraphicsOGL.GL_LESS;
            case GraphicsOGL.GL_GEQUAL -> GraphicsOGL.GL_LEQUAL;
            default -> depthFunc;
        };
    }

    private TextureUpload prepareTextureUpload(OglRenderer.OglTexture texture, boolean flipVertically) {
        TextureUpload upload = toTextureUpload(texture, flipVertically, textureUploadBuffer);
        textureUploadBuffer = upload.buffer();
        return upload;
    }

    private float[] ensureFloatArray(float[] current, int length) {
        return current.length >= length ? current : new float[length];
    }

    private byte[] ensureByteArray(byte[] current, int length) {
        return current.length >= length ? current : new byte[length];
    }

    private short[] ensureShortArray(short[] current, int length) {
        return current.length >= length ? current : new short[length];
    }

    private FloatBuffer toReusableFloatBuffer(float[] values, int count, BufferType type) {
        int byteCount = count * Float.BYTES;
        ByteBuffer storage = ensureBuffer(type, byteCount);
        storage.clear();
        storage.limit(byteCount);
        FloatBuffer buffer = storage.asFloatBuffer();
        buffer.clear();
        buffer.put(values, 0, count);
        buffer.flip();
        return buffer;
    }

    private ByteBuffer toReusableByteBuffer(byte[] values, int count, BufferType type) {
        ByteBuffer storage = ensureBuffer(type, count);
        storage.clear();
        storage.limit(count);
        storage.put(values, 0, count);
        storage.flip();
        return storage;
    }

    private ShortBuffer toReusableShortBuffer(short[] values, int count) {
        int byteCount = count * Short.BYTES;
        ByteBuffer storage = ensureBuffer(BufferType.INDEX, byteCount);
        storage.clear();
        storage.limit(byteCount);
        ShortBuffer buffer = storage.asShortBuffer();
        buffer.clear();
        buffer.put(values, 0, count);
        buffer.flip();
        return buffer;
    }

    private ByteBuffer ensureBuffer(BufferType type, int byteCount) {
        ByteBuffer storage = switch (type) {
            case VERTEX -> vertexBufferBytes;
            case NORMAL -> normalBufferBytes;
            case TEX_COORD -> texCoordBufferBytes;
            case COLOR -> colorBufferBytes;
            case INDEX -> indexBufferBytes;
        };
        if (storage == null || storage.capacity() < byteCount) {
            storage = ByteBuffer.allocateDirect(byteCount).order(ByteOrder.nativeOrder());
            switch (type) {
                case VERTEX -> vertexBufferBytes = storage;
                case NORMAL -> normalBufferBytes = storage;
                case TEX_COORD -> texCoordBufferBytes = storage;
                case COLOR -> colorBufferBytes = storage;
                case INDEX -> indexBufferBytes = storage;
            }
        }
        return storage;
    }

    /** ARGB ints → RGBA bytes with optional vertical flip (GLES upload order). */
    private static ByteBuffer toRgbaByteBuffer(int[] pixels, int width, int height, boolean flipVertically,
                                               ByteBuffer reusableBuffer) {
        int byteCount = width * height * 4;
        ByteBuffer buffer = reusableBuffer != null && reusableBuffer.capacity() >= byteCount
                ? reusableBuffer
                : ByteBuffer.allocateDirect(byteCount).order(ByteOrder.nativeOrder());
        buffer.clear();
        for (int y = 0; y < height; y++) {
            int sourceY = flipVertically ? (height - 1 - y) : y;
            int rowOffset = sourceY * width;
            for (int x = 0; x < width; x++) {
                int packed = pixels[rowOffset + x];
                buffer.put((byte) (packed >>> 16));
                buffer.put((byte) (packed >>> 8));
                buffer.put((byte) packed);
                buffer.put((byte) (packed >>> 24));
            }
        }
        buffer.position(byteCount);
        buffer.flip();
        return buffer;
    }

    private int[] copySurfacePixels() {
        DesktopSurface surface = owner.host().surface();
        BufferedImage image = surface.image();
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getBitmap().getPixels(pixels, 0, width, 0, 0, width, height);
        return pixels;
    }

    private static TextureUpload toTextureUpload(OglRenderer.OglTexture texture, boolean flipVertically,
                                                 ByteBuffer reusableBuffer) {
        int width = Math.max(1, texture.width);
        int height = Math.max(1, texture.height);
        int format = texture.baseFormat();
        int bytesPerPixel = switch (format) {
            case GraphicsOGL.GL_ALPHA, GraphicsOGL.GL_LUMINANCE -> 1;
            case GraphicsOGL.GL_LUMINANCE_ALPHA -> 2;
            case GraphicsOGL.GL_RGB -> 3;
            default -> 4;
        };
        int byteCount = width * height * bytesPerPixel;
        ByteBuffer buffer = reusableBuffer != null && reusableBuffer.capacity() >= byteCount
                ? reusableBuffer
                : ByteBuffer.allocateDirect(byteCount).order(ByteOrder.nativeOrder());
        buffer.clear();
        for (int y = 0; y < height; y++) {
            int sourceY = flipVertically ? (height - 1 - y) : y;
            int rowOffset = sourceY * width;
            for (int x = 0; x < width; x++) {
                int packed = texture.pixels[rowOffset + x];
                int alpha = (packed >>> 24) & 0xFF;
                int red = (packed >>> 16) & 0xFF;
                int green = (packed >>> 8) & 0xFF;
                int blue = packed & 0xFF;
                switch (format) {
                    case GraphicsOGL.GL_ALPHA -> buffer.put((byte) alpha);
                    case GraphicsOGL.GL_LUMINANCE -> buffer.put((byte) red);
                    case GraphicsOGL.GL_LUMINANCE_ALPHA -> {
                        buffer.put((byte) red);
                        buffer.put((byte) alpha);
                    }
                    case GraphicsOGL.GL_RGB -> {
                        buffer.put((byte) red);
                        buffer.put((byte) green);
                        buffer.put((byte) blue);
                    }
                    default -> {
                        buffer.put((byte) red);
                        buffer.put((byte) green);
                        buffer.put((byte) blue);
                        buffer.put((byte) alpha);
                    }
                }
            }
        }
        buffer.flip();
        return new TextureUpload(format, buffer);
    }

    private int supersampleScale() {
        return OpenDoJaLaunchArgs.openGlesSupersampleScale();
    }

    private int renderWidth() {
        return Math.max(1, owner.host().surface().width() * supersampleScale());
    }

    private int renderHeight() {
        return Math.max(1, owner.host().surface().height() * supersampleScale());
    }

    private interface HardwareGlCall {
        boolean execute();
    }

    private record PreparedArrayState(FloatBuffer vertexBuffer,
                                      FloatBuffer normalBuffer,
                                      FloatBuffer texCoordBuffer,
                                      ByteBuffer colorBuffer,
                                      ShortBuffer indexBuffer,
                                      int drawFirst,
                                      int drawCount,
                                      boolean emulateMatrixPalette) {
    }

    private enum BufferType {
        VERTEX,
        NORMAL,
        TEX_COORD,
        COLOR,
        INDEX
    }

    private record HardwareTexture(int textureId, int uploadedRevision, int width, int height, int format) {
        boolean matches(int revision, int width, int height, int format) {
            return uploadedRevision == revision && this.width == width && this.height == height && this.format == format;
        }
    }

    private record TextureUpload(int format, ByteBuffer buffer) {
    }
}
