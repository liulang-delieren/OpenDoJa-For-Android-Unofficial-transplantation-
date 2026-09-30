package opendoja.probes;

import com.nttdocomo.ui.Image;
import opendoja.host.DesktopSurface;
import opendoja.host.OpenDoJaLaunchArgs;
import opendoja.host.OpenGlesRendererMode;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public final class HardwareOutsideLockSoftwareMutationProbe {
    private HardwareOutsideLockSoftwareMutationProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty(OpenDoJaLaunchArgs.OPEN_GLES_RENDERER, OpenGlesRendererMode.HARDWARE.id());
        verifyFullScreenImageDrawMarksHardwareSurfaceDirty();
        verifyPartialImageDrawCapturesOverlayAndMarksSurfaceDirty();
        System.out.println("Hardware outside-lock software mutation probe OK");
    }

    private static void verifyFullScreenImageDrawMarksHardwareSurfaceDirty() throws Exception {
        DesktopSurface surface = new DesktopSurface(8, 8);
        surface.setRepaintHook((frame, frameBoundary) -> { });
        com.nttdocomo.ui.Graphics graphics = newGraphics(surface);
        Object backend = hardwareBackend(graphics);
        setBooleanField(backend, "surfaceDirty", false);

        Image source = solidImage(8, 8, com.nttdocomo.ui.Graphics.RED);
        graphics.drawImage(source, 0, 0);

        check(getBooleanField(backend, "surfaceDirty"),
                "full-screen outside-lock image draw should dirty the hardware surface for the next GL pass");
    }

    private static void verifyPartialImageDrawCapturesOverlayAndMarksSurfaceDirty() throws Exception {
        DesktopSurface surface = new DesktopSurface(8, 8);
        surface.setRepaintHook((frame, frameBoundary) -> { });
        com.nttdocomo.ui.Graphics graphics = newGraphics(surface);
        Object backend = hardwareBackend(graphics);
        setBooleanField(backend, "surfaceDirty", false);

        Image source = solidImage(4, 4, com.nttdocomo.ui.Graphics.BLUE);
        graphics.drawImage(source, 2, 2);

        check(getBooleanField(backend, "surfaceDirty"),
                "partial outside-lock image draw should dirty the hardware surface for the next GL pass");
        check(getFieldValue(backend, "outsideLockOverlaySnapshot") != null,
                "partial outside-lock image draw should capture an overlay snapshot");
        Rectangle bounds = (Rectangle) getFieldValue(backend, "outsideLockOverlayBounds");
        check(bounds != null && bounds.x <= 2 && bounds.y <= 2 && bounds.contains(6, 6),
                "partial outside-lock image draw should retain overlay coverage for the drawn image but was " + bounds);
    }

    private static Image solidImage(int width, int height, int color) {
        Image image = Image.createImage(width, height);
        com.nttdocomo.ui.Graphics graphics = image.getGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
        return image;
    }

    private static com.nttdocomo.ui.Graphics newGraphics(DesktopSurface surface) throws Exception {
        Constructor<com.nttdocomo.ui.Graphics> constructor = com.nttdocomo.ui.Graphics.class
                .getDeclaredConstructor(DesktopSurface.class);
        constructor.setAccessible(true);
        return constructor.newInstance(surface);
    }

    private static Object hardwareBackend(com.nttdocomo.ui.Graphics graphics) throws Exception {
        Object renderer = getFieldValue(graphics, "oglRenderer");
        return getFieldValue(renderer, "hardware");
    }

    private static boolean getBooleanField(Object target, String name) throws Exception {
        return findField(target.getClass(), name).getBoolean(target);
    }

    private static void setBooleanField(Object target, String name, boolean value) throws Exception {
        findField(target.getClass(), name).setBoolean(target, value);
    }

    private static Object getFieldValue(Object target, String name) throws Exception {
        return findField(target.getClass(), name).get(target);
    }

    private static Field findField(Class<?> type, String name) throws Exception {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
