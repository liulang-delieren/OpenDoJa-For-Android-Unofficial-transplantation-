package com.nttdocomo.device;

import com.nttdocomo.system.InterruptedOperationException;
import com.nttdocomo.ui.MediaImage;
import com.nttdocomo.ui.MediaManager;
import com.nttdocomo.ui.UIException;
import opendoja.host.device.DoJaCameraSupport;

import opendoja.compat.imageio.IIOImage;
import opendoja.compat.imageio.ImageIO;
import opendoja.compat.imageio.ImageWriteParam;
import opendoja.compat.imageio.ImageWriter;
import opendoja.compat.imageio.stream.MemoryCacheImageOutputStream;
import opendoja.compat.awt.*;
import opendoja.compat.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

/**
 * Provides the means for calling the handset's native camera function and
 * accessing still-image or movie capture.
 */
public class Camera {
    /** Indicates continuous-shot mode (=0). */
    public static final int DEV_CONTINUOUS_SHOT = 0;

    /** Attribute value meaning that continuous-shot mode is disabled (=0). */
    public static final int ATTR_CONTINUOUS_SHOT_OFF = 0;

    /** Attribute value meaning that continuous-shot mode is enabled (=1). */
    public static final int ATTR_CONTINUOUS_SHOT_ON = 1;

    /** Indicates picture quality (=1). */
    public static final int DEV_QUALITY = 1;

    /** Attribute value meaning priority is given to quality (=0). */
    public static final int ATTR_QUALITY_HIGH = 0;

    /** Attribute value meaning standard quality (=1). */
    public static final int ATTR_QUALITY_STANDARD = 1;

    /** Attribute value meaning priority is given to recording time or data size (=2). */
    public static final int ATTR_QUALITY_LOW = 2;

    /** Indicates microphone-input volume adjustment (=2). */
    public static final int DEV_SOUND = 2;

    /** The minimum microphone-input volume (=0). */
    public static final int ATTR_VOLUME_MIN = 0;

    /** The maximum microphone-input volume (=127). */
    public static final int ATTR_VOLUME_MAX = 127;

    /** Indicates the maximum burst count when continuous-shot mode is used (=3). */
    public static final int DEV_CONTINUOUS_IMAGES = 3;

    /** Indicates whether frame-image still capture is possible (=4). */
    public static final int DEV_FRAME_SHOT = 4;

    /** Attribute value meaning that frame-image still capture is not possible (=0). */
    public static final int ATTR_FRAME_OFF = 0;

    /** Attribute value meaning that frame-image still capture is possible (=1). */
    public static final int ATTR_FRAME_ON = 1;

    /** Focus mode meaning the focus mechanism is hardware-controlled (=-1). */
    public static final int FOCUS_HARDWARE_SWITCH = -1;

    /** Focus mode meaning normal focus (=0). */
    public static final int FOCUS_NORMAL_MODE = 0;

    /** Focus mode meaning macro focus (=1). */
    public static final int FOCUS_MACRO_MODE = 1;

    private static final int DEV_IMAGE_ENCODER = 128;
    private static final int ATTR_DEFAULT_IMAGE_ENCODER = 0;
    private static final int ATTR_RAW_IMAGE_ENCODER = 1;
    private static final int ATTR_JPEG_IMAGE_ENCODER = 2;
    private static final int DEFAULT_SOUND_VOLUME = 100;

    private static final Map<Integer, Camera> CAMERAS = new HashMap<>();

    private final Object imageLock = new Object();
    private final int id;
    private final List<CapturedImage> capturedImages = new ArrayList<>();

    private int requestedWidth;
    private int requestedHeight;
    private int continuousShot = ATTR_CONTINUOUS_SHOT_OFF;
    private int quality = ATTR_QUALITY_STANDARD;
    private int sound = DEFAULT_SOUND_VOLUME;
    private int imageEncoder = ATTR_DEFAULT_IMAGE_ENCODER;
    private int focusMode = FOCUS_NORMAL_MODE;
    private MediaImage frameImage;

    /**
     * Applications cannot create camera objects directly with this
     * constructor.
     */
    protected Camera() {
        this(-1);
    }

    Camera(int id) {
        this.id = id;
    }

    /**
     * Gets the camera object for the specified camera ID.
     * When this method is called for that camera ID for the first time, a
     * camera object is created and returned.
     * After that, a reference to the same object is always returned for the
     * same camera ID.
     *
     * @param id the camera ID
     * @return the camera object
     * @throws IllegalArgumentException if {@code id} is negative or is greater
     *         than or equal to the number of camera devices controllable from
     *         Java
     * @throws DeviceException if the camera device cannot be secured
     */
    public static Camera getCamera(int id) {
        DoJaCameraSupport.validateCameraId(id);
        synchronized (CAMERAS) {
            return CAMERAS.computeIfAbsent(id, Camera::new);
        }
    }

    /**
     * Calls the camera function in still-image capture mode.
     * On this desktop host, the capture succeeds synchronously and stores a
     * synthetic image that reflects the current camera settings.
     *
     * @throws InterruptedOperationException never thrown by this desktop host
     * @throws UIException if a configured frame image is invalid
     * @throws DeviceException if the shared camera device is busy
     */
    public void takePicture() throws InterruptedOperationException {
        clearCapturedImages(false);
        validateFrameImageForCapture();
        Runnable release = DoJaCameraSupport.beginNativeOperation(id, "takePicture");
        try {
            CodeReader.clearResultsForId(id);
            Dimension captureSize = effectiveCaptureSize();
            int imageCount = continuousShot == ATTR_CONTINUOUS_SHOT_ON
                    ? DoJaCameraSupport.getMaxContinuousImages(frameImage != null)
                    : 1;
            List<CapturedImage> newImages = new ArrayList<>(imageCount);
            for (int i = 0; i < imageCount; i++) {
                newImages.add(createSyntheticCapture(captureSize.width, captureSize.height, i, imageCount));
            }
            synchronized (imageLock) {
                capturedImages.clear();
                capturedImages.addAll(newImages);
            }
        } catch (IOException exception) {
            throw new DeviceException(DeviceException.UNDEFINED,
                    "Failed to generate synthetic still image: " + exception.getMessage());
        } finally {
            release.run();
        }
    }

    /**
     * Calls the camera function in movie-capture mode.
     * On the desktop host, movie capture is simulated by retaining a single
     * captured item whose display image is a poster frame and whose input
     * stream exposes placeholder movie bytes.
     *
     * @throws InterruptedOperationException never thrown by this desktop host
     * @throws DeviceException if the shared camera device is busy
     */
    public void takeMovie() throws InterruptedOperationException {
        clearCapturedImages(false);
        Runnable release = DoJaCameraSupport.beginNativeOperation(id, "takeMovie");
        try {
            CodeReader.clearResultsForId(id);
            Dimension captureSize = effectiveCaptureSize();
            CapturedImage capture = createSyntheticCapture(captureSize.width, captureSize.height, 0, 1);
            byte[] streamBytes = capture.streamBytes();
            if (streamBytes == null) {
                streamBytes = capture.displayBytes();
            }
            synchronized (imageLock) {
                capturedImages.clear();
                capturedImages.add(new CapturedImage(capture.displayBytes(), streamBytes));
            }
        } catch (IOException exception) {
            throw new DeviceException(DeviceException.UNDEFINED,
                    "Failed to generate synthetic movie capture: " + exception.getMessage());
        } finally {
            release.run();
        }
    }

    /**
     * Gets the number of captured images currently held by this camera object.
     *
     * @return the number of retained images
     */
    public int getNumberOfImages() {
        synchronized (imageLock) {
            return capturedImages.size();
        }
    }

    /**
     * Gets the data size, in bytes, of the captured image at the specified
     * index.
     *
     * @param index the image index
     * @return the data size of the specified captured image
     * @throws ArrayIndexOutOfBoundsException if {@code index} is invalid
     */
    public long getImageLength(int index) {
        return imageAt(index).streamLength();
    }

    /**
     * Gets the maximum data size, in bytes, that can be captured as a movie.
     *
     * @return {@code 0}, because movie capture is not supported by this host
     */
    public long getMaxImageLength() {
        return DoJaCameraSupport.getMaxMovieLength();
    }

    /**
     * Gets the captured image at the specified index as a media-image object.
     * Each call returns a different media-image instance.
     *
     * @param index the image index
     * @return the captured image
     * @throws ArrayIndexOutOfBoundsException if {@code index} is invalid
     */
    public MediaImage getImage(int index) {
        return MediaManager.getImage(imageAt(index).displayBytes());
    }

    /**
     * Gets an input stream for reading the captured image at the specified
     * index.
     * Each call returns a different stream instance.
     *
     * @param index the image index
     * @return the input stream for the captured image, or {@code null} if the
     *         current image-encoder setting keeps the image only as a media
     *         image
     * @throws ArrayIndexOutOfBoundsException if {@code index} is invalid
     */
    public InputStream getInputStream(int index) {
        byte[] streamBytes = imageAt(index).streamBytes();
        return streamBytes == null ? null : new ByteArrayInputStream(streamBytes.clone());
    }

    /**
     * Disposes all captured images held by this camera object.
     * If a frame image is set, this has the same effect as calling
     * {@code setFrameImage(null)}.
     */
    public void disposeImages() {
        clearCapturedImages(true);
    }

    /**
     * Sets the image used as a frame image when the camera captures a still
     * image.
     * The specified media image must be in the use state, must be a
     * system-provided media image, and must have the same size as the actual
     * still-image capture size.
     *
     * @param image the frame image, or {@code null} to disable frame-image
     *        capture
     * @throws UIException if the frame image is invalid or unsupported
     */
    public void setFrameImage(MediaImage image) {
        validateFrameImage(image);
        frameImage = image;
    }

    /**
     * Gets the frame-image sizes that can be used for still-image capture.
     *
     * @return a copy of the supported frame-image sizes, or {@code null} if
     *         frame-image capture is not available
     */
    public int[][] getAvailableFrameSizes() {
        return getAttribute(DEV_FRAME_SHOT) == ATTR_FRAME_ON
                ? DoJaCameraSupport.getAvailableFrameSizes()
                : null;
    }

    /**
     * Gets the still-image sizes that can be captured.
     *
     * @return a copy of the supported picture sizes
     */
    public int[][] getAvailablePictureSizes() {
        return DoJaCameraSupport.getAvailablePictureSizes();
    }

    /**
     * Gets the movie sizes that can be captured.
     *
     * @return {@code null}, because movie capture is not supported by this
     *         host
     */
    public int[][] getAvailableMovieSizes() {
        return DoJaCameraSupport.getAvailableMovieSizes();
    }

    /**
     * Sets the requested capture-image size.
     * The actual capture size becomes the smallest supported size that can
     * contain the requested width and height. When this method is called, the
     * frame-image setting is discarded.
     *
     * @param width the requested width
     * @param height the requested height
     * @throws IllegalArgumentException if {@code width} or {@code height} is 0
     *         or less
     */
    public void setImageSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("image size must be positive");
        }
        requestedWidth = width;
        requestedHeight = height;
        frameImage = null;
    }

    /**
     * Determines whether the specified camera attribute can be controlled by
     * {@link #setAttribute(int, int)}.
     *
     * @param attr the attribute kind
     * @return {@code true} if the attribute is controllable
     */
    public boolean isAvailable(int attr) {
        return switch (attr) {
            case DEV_CONTINUOUS_SHOT, DEV_QUALITY, DEV_IMAGE_ENCODER -> true;
            case DEV_SOUND, DEV_CONTINUOUS_IMAGES, DEV_FRAME_SHOT -> false;
            default -> false;
        };
    }

    /**
     * Sets the value of a camera attribute.
     * If a non-existent or uncontrollable attribute is specified, the request
     * is ignored.
     *
     * @param attr the attribute kind
     * @param value the attribute value
     * @throws IllegalArgumentException if {@code value} is invalid for a
     *         controllable attribute
     */
    public void setAttribute(int attr, int value) {
        if (!isControllableAttribute(attr)) {
            return;
        }
        if (!isAvailable(attr)) {
            return;
        }
        switch (attr) {
            case DEV_CONTINUOUS_SHOT -> {
                if (value != ATTR_CONTINUOUS_SHOT_OFF && value != ATTR_CONTINUOUS_SHOT_ON) {
                    throw new IllegalArgumentException("Unsupported continuous-shot value: " + value);
                }
                continuousShot = value;
            }
            case DEV_QUALITY -> {
                if (value != ATTR_QUALITY_HIGH
                        && value != ATTR_QUALITY_STANDARD
                        && value != ATTR_QUALITY_LOW) {
                    throw new IllegalArgumentException("Unsupported quality value: " + value);
                }
                quality = value;
            }
            case DEV_SOUND -> {
                if (value < ATTR_VOLUME_MIN || value > ATTR_VOLUME_MAX) {
                    throw new IllegalArgumentException("Sound attribute out of range: " + value);
                }
                sound = value;
            }
            case DEV_IMAGE_ENCODER -> {
                if (value != ATTR_DEFAULT_IMAGE_ENCODER
                        && value != ATTR_RAW_IMAGE_ENCODER
                        && value != ATTR_JPEG_IMAGE_ENCODER) {
                    throw new IllegalArgumentException("Unsupported image encoder: " + value);
                }
                imageEncoder = value;
            }
            default -> {
            }
        }
    }

    /**
     * Gets the current value of a camera attribute.
     * If a non-existent attribute is specified, {@code -1} is returned.
     *
     * @param attr the attribute kind
     * @return the attribute value, or {@code -1} if the attribute is unknown
     */
    public int getAttribute(int attr) {
        return switch (attr) {
            case DEV_CONTINUOUS_SHOT -> continuousShot;
            case DEV_QUALITY -> quality;
            case DEV_SOUND -> sound;
            case DEV_CONTINUOUS_IMAGES -> DoJaCameraSupport.getMaxContinuousImages(frameImage != null);
            case DEV_FRAME_SHOT -> DoJaCameraSupport.getAvailableFrameSizes() == null ? ATTR_FRAME_OFF : ATTR_FRAME_ON;
            case DEV_IMAGE_ENCODER -> imageEncoder;
            default -> -1;
        };
    }

    /**
     * Gets the list of focus modes that can be set on this host.
     *
     * @return a copy of the supported focus-mode list
     */
    public int[] getAvailableFocusModes() {
        return DoJaCameraSupport.getAvailableFocusModes();
    }

    /**
     * Sets the focus mode.
     *
     * @param mode the focus mode
     * @throws IllegalArgumentException if {@code mode} is not one of the
     *         values returned by {@link #getAvailableFocusModes()}
     */
    public void setFocusMode(int mode) {
        for (int availableMode : getAvailableFocusModes()) {
            if (availableMode == mode) {
                if (mode != FOCUS_HARDWARE_SWITCH) {
                    focusMode = mode;
                }
                return;
            }
        }
        throw new IllegalArgumentException("Unsupported focus mode: " + mode);
    }

    /**
     * Gets the current focus-mode setting.
     *
     * @return the current focus mode
     */
    public int getFocusMode() {
        return focusMode;
    }

    static void clearImagesForId(int id) {
        synchronized (CAMERAS) {
            Camera camera = CAMERAS.get(id);
            if (camera != null) {
                camera.clearCapturedImages(false);
            }
        }
    }

    private boolean isControllableAttribute(int attr) {
        return switch (attr) {
            case DEV_CONTINUOUS_SHOT, DEV_QUALITY, DEV_SOUND, DEV_IMAGE_ENCODER -> true;
            default -> false;
        };
    }

    private void clearCapturedImages(boolean clearFrameImage) {
        synchronized (imageLock) {
            capturedImages.clear();
        }
        if (clearFrameImage) {
            frameImage = null;
        }
    }

    private Dimension effectiveCaptureSize() {
        return DoJaCameraSupport.resolveCaptureSize(requestedWidth, requestedHeight);
    }

    private CapturedImage imageAt(int index) {
        synchronized (imageLock) {
            if (index < 0 || index >= capturedImages.size()) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            return capturedImages.get(index);
        }
    }

    private void validateFrameImageForCapture() {
        validateFrameImage(frameImage);
    }

    private void validateFrameImage(MediaImage image) {
        if (image == null) {
            return;
        }
        if (getAttribute(DEV_FRAME_SHOT) != ATTR_FRAME_ON) {
            throw new UIException(UIException.ILLEGAL_STATE,
                    "Frame-image still capture is not available for the current settings");
        }
        if (!isSystemMediaImage(image)) {
            throw new UIException(UIException.UNSUPPORTED_FORMAT,
                    "Only system-provided MediaImage implementations can be used as frame images");
        }
        if (!isMediaImageUsed(image)) {
            throw new UIException(UIException.ILLEGAL_STATE,
                    "Frame image must be in the use state");
        }
        if (image.getImage() == null) {
            throw new UIException(UIException.UNSUPPORTED_FORMAT,
                    "Frame image does not expose a drawable image");
        }
        Dimension captureSize = effectiveCaptureSize();
        if (image.getWidth() != captureSize.width || image.getHeight() != captureSize.height) {
            throw new UIException(UIException.UNSUPPORTED_FORMAT,
                    "Frame image size must match the actual capture size");
        }
        BufferedImage rendered = renderMediaImage(image);
        if (rendered == null || !hasTransparency(rendered)) {
            throw new UIException(UIException.UNSUPPORTED_FORMAT,
                    "Frame image must contain transparent areas");
        }
    }

    private boolean isSystemMediaImage(MediaImage image) {
        return image.getClass().getName().startsWith("com.nttdocomo.ui.");
    }

    private boolean isMediaImageUsed(MediaImage image) {
        Class<?> type = image.getClass();
        while (type != null) {
            try {
                Method method = type.getDeclaredMethod("isUsed");
                method.setAccessible(true);
                Object value = method.invoke(image);
                return Boolean.TRUE.equals(value);
            } catch (NoSuchMethodException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException | InvocationTargetException exception) {
                return false;
            }
        }
        return false;
    }

    private BufferedImage renderMediaImage(MediaImage image) {
        try {
            Method renderMethod = image.getImage().getClass().getSuperclass().getDeclaredMethod("renderForDisplay");
            renderMethod.setAccessible(true);
            Object rendered = renderMethod.invoke(image.getImage());
            return rendered instanceof BufferedImage bufferedImage ? bufferedImage : null;
        } catch (NoSuchMethodException exception) {
            try {
                Method renderMethod = image.getImage().getClass().getDeclaredMethod("renderForDisplay");
                renderMethod.setAccessible(true);
                Object rendered = renderMethod.invoke(image.getImage());
                return rendered instanceof BufferedImage bufferedImage ? bufferedImage : null;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                return null;
            }
        } catch (IllegalAccessException | InvocationTargetException exception) {
            return null;
        }
    }

    private boolean hasTransparency(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (((image.getRGB(x, y) >>> 24) & 0xFF) != 0xFF) {
                    return true;
                }
            }
        }
        return false;
    }

    private CapturedImage createSyntheticCapture(int width, int height, int burstIndex, int burstCount) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        try {
            int shade = 32 + java.lang.Math.min(96, burstIndex * 24);
            g.setColor(new Color(shade, shade, shade));
            g.fillRect(0, 0, width, height);
            if (burstCount > 1) {
                g.setColor(new Color(255, 255, 255, 64));
                int bandTop = (height / java.lang.Math.max(1, burstCount)) * burstIndex;
                g.fillRect(0, bandTop, width, java.lang.Math.max(1, height / java.lang.Math.max(1, burstCount)));
            }
            BufferedImage overlay = frameImage == null ? null : renderMediaImage(frameImage);
            if (overlay != null) {
                g.drawImage(overlay, 0, 0, null);
            }
        } finally {
            g.dispose();
        }

        if (imageEncoder == ATTR_RAW_IMAGE_ENCODER) {
            return new CapturedImage(encodePng(bufferedImage), null);
        }
        byte[] encoded = encodeJpeg(bufferedImage, jpegCompressionQuality());
        return new CapturedImage(encoded, encoded);
    }

    private float jpegCompressionQuality() {
        return switch (quality) {
            case ATTR_QUALITY_HIGH -> 1.0f;
            case ATTR_QUALITY_LOW -> 0.35f;
            default -> 0.75f;
        };
    }

    private byte[] encodePng(BufferedImage image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private byte[] encodeJpeg(BufferedImage image, float compressionQuality) throws IOException {
        BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgbImage.createGraphics();
        try {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer is available");
        }

        ImageWriter writer = writers.next();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (MemoryCacheImageOutputStream imageOutput = new MemoryCacheImageOutputStream(output)) {
            writer.setOutput(imageOutput);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(compressionQuality);
            }
            writer.write(null, new IIOImage(rgbImage, null, null), param);
        } finally {
            writer.dispose();
        }
        return output.toByteArray();
    }

    private record CapturedImage(byte[] displayBytes, byte[] streamBytes) {
        long streamLength() {
            return streamBytes == null ? 0 : streamBytes.length;
        }
    }
}
