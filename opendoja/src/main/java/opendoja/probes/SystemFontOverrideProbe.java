package opendoja.probes;

import opendoja.host.LaunchConfig;
import opendoja.host.OpenDoJaLaunchArgs;

import java.awt.AWTError;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

public final class SystemFontOverrideProbe {
    private static final String GAME_LAUNCH_SELECTION_CLASS_NAME = "opendoja.launcher.GameLaunchSelection";
    private static final String LAUNCHER_PROCESS_SUPPORT_CLASS_NAME = "opendoja.launcher.LauncherProcessSupport";
    private static final String LAUNCHER_SETTINGS_CLASS_NAME = "opendoja.launcher.LauncherSettings";

    private SystemFontOverrideProbe() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        verifyLauncherCommandForwardsSystemFontOverride();
        verifyRuntimeHonorsSystemFontOverride();
        System.out.println("System font override probe OK");
    }

    private static void verifyLauncherCommandForwardsSystemFontOverride() throws Exception {
        String overrideFamily = "Probe Override";
        Object settings = invokeNoArgStatic(launcherSettingsClass(), "defaults");
        settings = invoke(settings, "withFontType", new Class<?>[]{String.class}, LaunchConfig.FontType.SYSTEM.id);
        settings = invoke(settings, "withSystemFontOverride", new Class<?>[]{String.class}, overrideFamily);
        Object selection = newGameLaunchSelection(Path.of("probe.jam"), Path.of("probe.jar"));
        @SuppressWarnings("unchecked")
        List<String> command = (List<String>) invoke(newLauncherProcessSupport(), "buildLaunchCommand",
                new Class<?>[]{gameLaunchSelectionClass(), launcherSettingsClass()}, selection, settings);
        String expectedArgument = "-D" + OpenDoJaLaunchArgs.SYSTEM_FONT_OVERRIDE + "=" + overrideFamily;
        check(command.contains(expectedArgument),
                "launch command should forward the system font override as " + expectedArgument + " but was " + command);
    }

    private static void verifyRuntimeHonorsSystemFontOverride() throws Exception {
        String overrideFamily = firstAvailableFamily();
        OpenDoJaLaunchArgs.set(OpenDoJaLaunchArgs.FONT_TYPE, LaunchConfig.FontType.SYSTEM.id);
        OpenDoJaLaunchArgs.set(OpenDoJaLaunchArgs.SYSTEM_FONT_OVERRIDE, overrideFamily);
        com.nttdocomo.ui.Font font = com.nttdocomo.ui.Font.getFont(com.nttdocomo.ui.Font.FACE_SYSTEM | com.nttdocomo.ui.Font.STYLE_PLAIN, 12);
        Field awtFontField = com.nttdocomo.ui.Font.class.getDeclaredField("awtFont");
        awtFontField.setAccessible(true);
        java.awt.Font awtFont = (java.awt.Font) awtFontField.get(font);
        check(overrideFamily.equals(awtFont.getFamily()),
                "runtime font override should resolve to " + overrideFamily + " but was " + awtFont.getFamily());
    }

    private static String firstAvailableFamily() {
        try {
            String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            if (families.length > 0) {
                return families[0];
            }
        } catch (HeadlessException | AWTError ignored) {
        }
        return java.awt.Font.DIALOG;
    }

    private static Object newGameLaunchSelection(Path jamPath, Path gameJarPath) throws Exception {
        Constructor<?> constructor = gameLaunchSelectionClass().getDeclaredConstructor(Path.class, Path.class);
        constructor.setAccessible(true);
        return constructor.newInstance(jamPath, gameJarPath);
    }

    private static Object newLauncherProcessSupport() throws Exception {
        Constructor<?> constructor = launcherProcessSupportClass().getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static Object invokeNoArgStatic(Class<?> type, String methodName) throws Exception {
        Method method = type.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(null);
    }

    private static Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static Class<?> gameLaunchSelectionClass() throws ClassNotFoundException {
        return Class.forName(GAME_LAUNCH_SELECTION_CLASS_NAME);
    }

    private static Class<?> launcherProcessSupportClass() throws ClassNotFoundException {
        return Class.forName(LAUNCHER_PROCESS_SUPPORT_CLASS_NAME);
    }

    private static Class<?> launcherSettingsClass() throws ClassNotFoundException {
        return Class.forName(LAUNCHER_SETTINGS_CLASS_NAME);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
