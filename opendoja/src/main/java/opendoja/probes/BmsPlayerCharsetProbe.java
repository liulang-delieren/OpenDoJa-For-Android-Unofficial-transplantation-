package opendoja.probes;

import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.MediaImage;
import com.nttdocomo.ui.MediaManager;
import opendoja.host.DoJaRuntime;
import opendoja.host.LaunchConfig;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Path;

public final class BmsPlayerCharsetProbe {
    private BmsPlayerCharsetProbe() {
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Class<?> rawAppClass = Class.forName("mdj");
        Class<? extends com.nttdocomo.ui.IApplication> appClass =
                (Class<? extends com.nttdocomo.ui.IApplication>) rawAppClass;
        LaunchConfig config = LaunchConfig.builder(appClass)
                .externalFrameEnabled(false)
                .parameter("UseStorage", "1")
                .scratchpadSizes(new int[]{1024, 10240, 102400})
                .scratchpadPackedFile(Path.of("resources/sample_games/BMS Player/BMS Player/mdj_qvga.sp"))
                .build();
        DoJaRuntime.prepareLaunch(config);
        DoJaRuntime runtime = DoJaRuntime.bootstrap(config);
        try {
            runtime.attachApplication((com.nttdocomo.ui.IApplication) appClass.getDeclaredConstructor().newInstance());

            Class<?> canClass = Class.forName("can");
            Object can = instantiate(canClass);

            initializeFonts(canClass, can);
            loadBuiltInImages(canClass, can);

            Class<?> zipUtilClass = Class.forName("ZipUtil");
            Object zipUtil = instantiate(zipUtilClass, int.class, String.class, 0, "BMS.ZIP");
            invoke(zipUtilClass, zipUtil, "useSD");
            Object fileList = invoke(zipUtilClass, zipUtil, "getList");

            Field zuField = canClass.getDeclaredField("zu");
            zuField.setAccessible(true);
            zuField.set(can, zipUtil);

            Field fileListField = canClass.getDeclaredField("fileList");
            fileListField.setAccessible(true);
            fileListField.set(can, fileList);

            Field bmsListField = canClass.getDeclaredField("bmsList");
            bmsListField.setAccessible(true);
            bmsListField.set(can, instantiate(Class.forName("ArrayListOR")));

            int fileListCount = Array.getLength(fileList);
            int loadedCount = (Integer) invoke(canClass, can, "loadAllBmsData", boolean.class, false);
            Object bmsList = bmsListField.get(can);
            int bmsListSize = (Integer) invoke(bmsList.getClass(), bmsList, "size");

            Object firstEntry = bmsListSize == 0 ? null : invoke(bmsList.getClass(), bmsList, "get", int.class, 0);
            String firstFilePass = firstEntry == null ? "<none>" : (String) getField(firstEntry, "FilePass");
            String firstTitle = firstEntry == null ? "<none>" : (String) getField(firstEntry, "title");

            Field selectField = canClass.getDeclaredField("select");
            selectField.setAccessible(true);
            selectField.setInt(can, 0);

            System.out.printf(
                    "runtimeCharset=%s fileList=%d loaded=%d bmsList=%d firstFile=%s firstTitle=%s%n",
                    Charset.defaultCharset().name(),
                    fileListCount,
                    loadedCount,
                    bmsListSize,
                    firstFilePass,
                    firstTitle);
        } finally {
            runtime.shutdown();
            DoJaRuntime.clearPreparedLaunch();
        }
    }

    private static void initializeFonts(Class<?> canClass, Object can) throws Exception {
        Field systemField = canClass.getDeclaredField("SYSTEM");
        systemField.setAccessible(true);
        systemField.set(can, Font.getDefaultFont());
    }

    private static void loadBuiltInImages(Class<?> canClass, Object can) throws Exception {
        Field imgField = canClass.getDeclaredField("img");
        imgField.setAccessible(true);
        Object[] img = (Object[]) imgField.get(can);
        for (int i = 0; i <= 26; i++) {
            String name = "resource:///" + i + ".gif";
            try {
                MediaImage image = MediaManager.getImage(name);
                image.use();
                img[i] = image.getImage();
            } catch (Exception ignored) {
                img[i] = null;
            }
        }
    }

    private static Object getField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static Object instantiate(Class<?> type, Class<?> argType, Object arg) throws Exception {
        Constructor<?> constructor = type.getDeclaredConstructor(argType);
        constructor.setAccessible(true);
        return constructor.newInstance(arg);
    }

    private static Object instantiate(Class<?> type, Class<?> argType1, Class<?> argType2, Object arg1, Object arg2)
            throws Exception {
        Constructor<?> constructor = type.getDeclaredConstructor(argType1, argType2);
        constructor.setAccessible(true);
        return constructor.newInstance(arg1, arg2);
    }

    private static Object instantiate(Class<?> type) throws Exception {
        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static Object invoke(Class<?> type, Object target, String name, Class<?> argType, Object arg) throws Exception {
        Method method = type.getDeclaredMethod(name, argType);
        method.setAccessible(true);
        return method.invoke(target, arg);
    }

    private static Object invoke(Class<?> type, Object target, String name) throws Exception {
        Method method = type.getDeclaredMethod(name);
        method.setAccessible(true);
        return method.invoke(target);
    }
}
