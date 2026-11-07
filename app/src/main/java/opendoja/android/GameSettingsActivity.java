package opendoja.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import opendoja.host.OpenGlesRendererMode;

/**
 * Per-game settings hub, opened by long-pressing a game tile on the home grid.
 * Exposes the per-game key mapping and the OGL renderer choice (hardware is the
 * app default, software is the fallback for games that misrender on GLES).
 * Settings live in SharedPreferences namespaced by the game's jam basename, so
 * deleting a game can wipe its settings cleanly.
 */
public class GameSettingsActivity extends Activity {
    static final String EXTRA_JAM_PATH = "jamPath";
    static final String EXTRA_GAME_NAME = "gameName";

    private static final String PREFS_PREFIX = "gamesettings_";
    private static final String KEY_RENDERER = "renderer";
    private static final String KEY_DISPLAY_SCALE = "displayScale";
    private static final String KEY_KEYPAD_VISIBLE = "keypadVisible";

    /** Game screen is stretched over the whole screen (keypad drawn on top). */
    static final String SCALE_FILL = "fill";
    /**
     * Aspect ratio kept: grows to the screen edge when the game resolution divides the
     * screen exactly, otherwise to just under the available area. Never cropped.
     */
    static final String SCALE_COVER = "cover";

    private TextView keymapSummary;
    private TextView keypadSummary;
    private TextView rendererSummary;
    private TextView scaleSummary;
    private String gameKey;

    /** Explicit per-game renderer id, or null when the launch default applies. */
    static String rendererOverride(Context context, String gameKey) {
        if (gameKey == null) {
            return null;
        }
        String value = context.getSharedPreferences(PREFS_PREFIX + gameKey, Context.MODE_PRIVATE)
                .getString(KEY_RENDERER, null);
        if (OpenGlesRendererMode.HARDWARE.id().equals(value)
                || OpenGlesRendererMode.SOFTWARE.id().equals(value)) {
            return value;
        }
        return null;
    }

    /** Drops the per-game settings layer; called when the game is deleted. */
    static void clearGameSettings(Context context, String gameKey) {
        if (gameKey != null) {
            context.getSharedPreferences(PREFS_PREFIX + gameKey, Context.MODE_PRIVATE)
                    .edit().clear().apply();
        }
    }

    /**
     * Per-game screen scaling: {@link #SCALE_FILL} stretches the game picture over the whole
     * screen, {@link #SCALE_COVER} keeps the aspect ratio and grows the picture until it
     * nearly (but never fully) covers the screen, {@code null} keeps the aspect ratio
     * letterboxed (the default). The virtual keypad is drawn on top of the picture in every
     * mode.
     */
    static String displayScaleMode(Context context, String gameKey) {
        if (gameKey == null) {
            return null;
        }
        String value = context.getSharedPreferences(PREFS_PREFIX + gameKey, Context.MODE_PRIVATE)
                .getString(KEY_DISPLAY_SCALE, null);
        if (SCALE_FILL.equals(value)) {
            return SCALE_FILL;
        }
        if (SCALE_COVER.equals(value)) {
            return SCALE_COVER;
        }
        return null;
    }

    /**
     * Whether this game shows the virtual keypad on top of the picture. Defaults to
     * {@code true}; the in-game BACK menu still offers a temporary toggle per session.
     */
    static boolean isKeypadVisible(Context context, String gameKey) {
        if (gameKey == null) {
            return true;
        }
        return context.getSharedPreferences(PREFS_PREFIX + gameKey, Context.MODE_PRIVATE)
                .getBoolean(KEY_KEYPAD_VISIBLE, true);
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(AppLanguage.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppTheme.apply(this);
        super.onCreate(savedInstanceState);

        String jamPath = getIntent() != null ? getIntent().getStringExtra(EXTRA_JAM_PATH) : null;
        String gameName = getIntent() != null ? getIntent().getStringExtra(EXTRA_GAME_NAME) : null;
        gameKey = KeyMappingActivity.gameKeyFromJamPath(jamPath);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText(R.string.settings_title);
        title.setTextSize(22f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(AppTheme.primaryText(this));
        root.addView(title, matchWrap());

        TextView name = new TextView(this);
        name.setText(gameName != null ? gameName : String.valueOf(jamPath));
        name.setTextSize(13f);
        name.setTextColor(AppTheme.secondaryText(this));
        LinearLayout.LayoutParams nameParams = matchWrap();
        nameParams.topMargin = dp(4);
        root.addView(name, nameParams);

        LinearLayout rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rowsParams = matchWrap();
        rowsParams.topMargin = dp(16);
        root.addView(rows, rowsParams);

        keymapSummary = new TextView(this);
        rows.addView(buildRow(getString(R.string.settings_keymap), keymapSummary,
                v -> openKeyMapping(gameName)), matchWrap());

        keypadSummary = new TextView(this);
        rows.addView(buildRow(getString(R.string.settings_keypad), keypadSummary,
                v -> chooseKeypadVisibility()), matchWrap());

        rendererSummary = new TextView(this);
        rows.addView(buildRow(getString(R.string.settings_renderer), rendererSummary,
                v -> chooseRenderer()), matchWrap());

        scaleSummary = new TextView(this);
        rows.addView(buildRow(getString(R.string.settings_scale), scaleSummary,
                v -> chooseDisplayScale()), matchWrap());

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSummaries();
    }

    private View buildRow(String label, TextView summary, View.OnClickListener onClick) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), dp(14), dp(8), dp(14));
        row.setClickable(true);
        row.setFocusable(true);
        row.setForeground(new RippleDrawable(
                ColorStateList.valueOf(AppTheme.ripplePress(this)), null, null));

        TextView name = new TextView(this);
        name.setText(label);
        name.setTextColor(AppTheme.rowText(this));
        name.setTextSize(16f);
        row.addView(name, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        summary.setTextColor(AppTheme.secondaryText(this));
        summary.setTextSize(12f);
        summary.setGravity(Gravity.END);
        row.addView(summary, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        row.setOnClickListener(onClick);
        return row;
    }

    private void openKeyMapping(String gameName) {
        Intent intent = new Intent(this, KeyMappingActivity.class);
        intent.putExtra(KeyMappingActivity.EXTRA_GAME_KEY, gameKey);
        intent.putExtra(KeyMappingActivity.EXTRA_GAME_NAME, gameName);
        startActivity(intent);
    }

    private void chooseRenderer() {
        if (gameKey == null) {
            return;
        }
        String current = rendererOverride(this, gameKey);
        String[] items = {
                getString(R.string.renderer_default),
                getString(R.string.renderer_hardware),
                getString(R.string.renderer_software),
        };
        int checked = current == null ? 0
                : (OpenGlesRendererMode.SOFTWARE.id().equals(current) ? 2 : 1);
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_renderer)
                .setSingleChoiceItems(items, checked, (dialog, which) -> {
                    SharedPreferences.Editor editor = getSharedPreferences(
                            PREFS_PREFIX + gameKey, MODE_PRIVATE).edit();
                    if (which == 0) {
                        editor.remove(KEY_RENDERER);
                    } else if (which == 1) {
                        editor.putString(KEY_RENDERER, OpenGlesRendererMode.HARDWARE.id());
                    } else {
                        editor.putString(KEY_RENDERER, OpenGlesRendererMode.SOFTWARE.id());
                    }
                    editor.apply();
                    dialog.dismiss();
                    refreshSummaries();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Per-game keypad visibility: whether the on-screen d-pad/dial pad is drawn over the
     * game picture. The BACK menu can still toggle it temporarily while playing.
     */
    private void chooseKeypadVisibility() {
        if (gameKey == null) {
            return;
        }
        boolean visible = isKeypadVisible(this, gameKey);
        String[] items = {
                getString(R.string.show),
                getString(R.string.hide),
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_keypad)
                .setSingleChoiceItems(items, visible ? 0 : 1, (dialog, which) -> {
                    SharedPreferences.Editor editor = getSharedPreferences(
                            PREFS_PREFIX + gameKey, MODE_PRIVATE).edit();
                    if (which == 0) {
                        editor.remove(KEY_KEYPAD_VISIBLE);
                    } else {
                        editor.putBoolean(KEY_KEYPAD_VISIBLE, false);
                    }
                    editor.apply();
                    dialog.dismiss();
                    refreshSummaries();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Screen scaling picker: keep the picture's aspect ratio letterboxed (default), stretch
     * it over the whole screen, or keep the ratio and grow it to the screen edge when the
     * resolution divides evenly (otherwise nearly fill the screen, never cropping).
     */
    private void chooseDisplayScale() {
        if (gameKey == null) {
            return;
        }
        String[] items = {
                getString(R.string.scale_uniform_desc),
                getString(R.string.scale_fill_desc),
                getString(R.string.scale_cover_desc),
        };
        String mode = displayScaleMode(this, gameKey);
        int checked = SCALE_FILL.equals(mode) ? 1 : (SCALE_COVER.equals(mode) ? 2 : 0);
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_scale)
                .setSingleChoiceItems(items, checked, (dialog, which) -> {
                    SharedPreferences.Editor editor = getSharedPreferences(
                            PREFS_PREFIX + gameKey, MODE_PRIVATE).edit();
                    if (which == 0) {
                        editor.remove(KEY_DISPLAY_SCALE);
                    } else if (which == 1) {
                        editor.putString(KEY_DISPLAY_SCALE, SCALE_FILL);
                    } else {
                        editor.putString(KEY_DISPLAY_SCALE, SCALE_COVER);
                    }
                    editor.apply();
                    dialog.dismiss();
                    refreshSummaries();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void refreshSummaries() {
        if (gameKey == null) {
            String unknown = getString(R.string.summary_unknown_game);
            if (keymapSummary != null) {
                keymapSummary.setText(unknown);
            }
            if (keypadSummary != null) {
                keypadSummary.setText(unknown);
            }
            if (rendererSummary != null) {
                rendererSummary.setText(unknown);
            }
            if (scaleSummary != null) {
                scaleSummary.setText(unknown);
            }
            return;
        }
        if (keymapSummary != null) {
            int count = getSharedPreferences(
                    KeyMappingActivity.prefsNameForGame(gameKey), MODE_PRIVATE).getAll().size();
            keymapSummary.setText(count == 0
                    ? getString(R.string.summary_keymap_none)
                    : getString(R.string.summary_keymap_count, count));
        }
        if (keypadSummary != null) {
            keypadSummary.setText(isKeypadVisible(this, gameKey)
                    ? R.string.show
                    : R.string.hide);
        }
        if (rendererSummary != null) {
            String renderer = rendererOverride(this, gameKey);
            if (renderer == null) {
                rendererSummary.setText(R.string.renderer_default);
            } else if (OpenGlesRendererMode.SOFTWARE.id().equals(renderer)) {
                rendererSummary.setText(R.string.renderer_software);
            } else {
                rendererSummary.setText(R.string.renderer_hardware);
            }
        }
        if (scaleSummary != null) {
            String mode = displayScaleMode(this, gameKey);
            if (SCALE_FILL.equals(mode)) {
                scaleSummary.setText(R.string.scale_fill);
            } else if (SCALE_COVER.equals(mode)) {
                scaleSummary.setText(R.string.scale_cover);
            } else {
                scaleSummary.setText(R.string.scale_uniform);
            }
        }
    }

    private static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
