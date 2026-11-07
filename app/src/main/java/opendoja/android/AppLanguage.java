package opendoja.android;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * App-wide UI language override.
 *
 * <p>{@link #SYSTEM} follows the device locale; {@link #ZH} and {@link #EN} force
 * Simplified Chinese or English. Every activity wraps its base context through
 * {@link #wrap(Context)} so dialogs, toasts and every resource lookup use the
 * chosen locale, and {@code recreate()} applies a change immediately.
 */
final class AppLanguage {
    static final String PREFS = "app_settings";
    static final String KEY = "language";

    static final String SYSTEM = "system";
    static final String ZH = "zh";
    static final String EN = "en";

    private AppLanguage() {
    }

    /** Selected language id: {@link #SYSTEM}, {@link #ZH} or {@link #EN}. */
    static String current(Context context) {
        String value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, SYSTEM);
        return (ZH.equals(value) || EN.equals(value)) ? value : SYSTEM;
    }

    static void set(Context context, String language) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, language).apply();
    }

    /** Wraps the base context so resource lookups use the selected language. */
    static Context wrap(Context base) {
        String language = current(base);
        if (SYSTEM.equals(language)) {
            return base;
        }
        Locale locale = ZH.equals(language) ? Locale.SIMPLIFIED_CHINESE : Locale.ENGLISH;
        Configuration configuration = new Configuration(base.getResources().getConfiguration());
        configuration.setLocale(locale);
        return base.createConfigurationContext(configuration);
    }
}
