package opendoja.probes;

import com.nttdocomo.ui.AudioPresenter;
import com.nttdocomo.ui.Font;
import com.nttdocomo.ui.MediaImage;
import com.nttdocomo.ui.MediaManager;
import opendoja.host.DoJaRuntime;
import opendoja.host.LaunchConfig;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;

public final class BmsPlayerAudioClockProbe {
    private BmsPlayerAudioClockProbe() {
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
            loadBmsList(canClass, can);

            setInt(can, "select", 0);
            invoke(canClass, can, "init");

            AudioPresenter presenter = (AudioPresenter) getField(can, "ap");
            Method mathCount = canClass.getDeclaredMethod("mathCount");
            mathCount.setAccessible(true);

            presenter.play(0);
            setLong(can, "start_time", System.currentTimeMillis());
            setBoolean(can, "music_play", true);

            for (int i = 0; i < 8; i++) {
                Thread.sleep(250L);
                int currentTime = presenter.getCurrentTime();
                int count = (Integer) mathCount.invoke(can);
                Object playNote = getField(can, "playNote");
                int playNoteTime = playNote == null ? Integer.MIN_VALUE : (Integer) getField(playNote, "time");
                System.out.printf("sample=%d currentTime=%d count=%d music_play=%s playNoteTime=%d activeKind=%s playing=%s sequencer=%s%n",
                        i, currentTime, count, getField(can, "music_play"), playNoteTime,
                        getField(presenter, "activeSoundKind"),
                        getField(presenter, "playing"),
                        getField(presenter, "sequencer") == null ? "null" : "set");
            }
        } finally {
            runtime.shutdown();
            DoJaRuntime.clearPreparedLaunch();
        }
    }

    private static void initializeFonts(Class<?> canClass, Object can) throws Exception {
        Font font = Font.getDefaultFont();
        setField(can, "SYSTEM", font);
        setField(can, "L_BOLD", font);
        setInt(can, "stringh", font.getHeight());
        setInt(can, "stringhLB", font.getHeight());
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

    private static void loadBmsList(Class<?> canClass, Object can) throws Exception {
        Class<?> zipUtilClass = Class.forName("ZipUtil");
        Object zipUtil = instantiate(zipUtilClass, int.class, String.class, 0, "BMS.ZIP");
        invoke(zipUtilClass, zipUtil, "useSD");
        Object fileList = invoke(zipUtilClass, zipUtil, "getList");

        setField(can, "zu", zipUtil);
        setField(can, "fileList", fileList);
        setField(can, "bmsList", instantiate(Class.forName("ArrayListOR")));

        int fileListCount = Array.getLength(fileList);
        int loadedCount = (Integer) invoke(canClass, can, "loadAllBmsData", boolean.class, false);
        if (loadedCount <= 0) {
            throw new IllegalStateException("No BMS entries loaded from " + fileListCount + " ZIP entries");
        }
    }

    private static void setInt(Object target, String name, int value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.setInt(target, value);
    }

    private static void setLong(Object target, String name, long value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.setLong(target, value);
    }

    private static void setBoolean(Object target, String name, boolean value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.setBoolean(target, value);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
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
