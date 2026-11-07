package opendoja.android;

import android.app.Activity;
import android.content.Context;

/**
 * Day/night appearance for the launcher UI.
 *
 * <p>Night is the default (the original dark look); the home menu can switch to
 * a bright theme and the choice is persisted. Activities call
 * {@link #apply(Activity)} before {@code super.onCreate} so the platform theme
 * (window background, dialogs, default text colour) matches, and use the colour
 * helpers below instead of hard-coded ARGB values.
 */
final class AppTheme {
    private static final String PREFS = "app_settings";
    private static final String KEY_NIGHT = "night_mode";

    private AppTheme() {
    }

    static boolean isNight(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_NIGHT, true);
    }

    static void setNight(Context context, boolean night) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_NIGHT, night).apply();
    }

    /** Applies the matching platform theme; call before {@code super.onCreate}. */
    static void apply(Activity activity) {
        activity.setTheme(isNight(activity) ? R.style.AppThemeDark : R.style.AppThemeLight);
    }

    /** Flips between night and light, recreating the activity in place. */
    static void toggle(Activity activity) {
        setNight(activity, !isNight(activity));
        activity.recreate();
    }

    static int pick(Context context, int night, int light) {
        return isNight(context) ? night : light;
    }

    static int titleBar(Context context) {
        return pick(context, 0xFF1A1C22, 0xFFF3F4F6);
    }

    static int primaryText(Context context) {
        return pick(context, 0xFFFFFFFF, 0xFF1F2328);
    }

    static int secondaryText(Context context) {
        return pick(context, 0xAAFFFFFF, 0xFF5F6368);
    }

    static int rowText(Context context) {
        return pick(context, 0xFFE8E8E8, 0xFF1F2328);
    }

    static int divider(Context context) {
        return pick(context, 0xFF3A3D45, 0xFFD7DADF);
    }

    static int menuBackground(Context context) {
        return pick(context, 0xF020242E, 0xFFFFFFFF);
    }

    static int ripplePress(Context context) {
        return pick(context, 0x33FFFFFF, 0x22000000);
    }

    static int accentText(Context context) {
        return pick(context, 0xFF66CCFF, 0xFF0067C0);
    }
}
