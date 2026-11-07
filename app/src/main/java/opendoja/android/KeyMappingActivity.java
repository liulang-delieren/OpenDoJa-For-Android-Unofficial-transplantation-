package opendoja.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import opendoja.host.HostControlAction;

/**
 * Binds physical hardware keys (volume keys etc.) to the same DoJa actions the
 * Windows host exposes in its keybind dialog. Tap a row, then press a hardware
 * key to bind it; long-press clears the binding. The system back key is always
 * reserved for force-quitting the game back to the title screen.
 *
 * Without extras this edits the global mapping (SharedPreferences "keymap").
 * With {@link #EXTRA_GAME_KEY} it edits that game's own layer
 * ("keymap_<gameKey>"): the per-game layer is consulted first at runtime and
 * falls back to the global layer per action, so clearing a per-game row simply
 * makes that action follow the global binding again.
 */
public class KeyMappingActivity extends Activity {
    static final String PREFS_NAME = "keymap";
    static final String EXTRA_GAME_KEY = "gameKey";
    static final String EXTRA_GAME_NAME = "gameName";

    /** Per-game prefs file for the given game key; matches EntryActivity lookup. */
    static String prefsNameForGame(String gameKey) {
        return PREFS_NAME + "_" + gameKey;
    }

    /** Derives the stable game key (jam basename, lowercase) from a jam path. */
    static String gameKeyFromJamPath(String jamPath) {
        if (jamPath == null || jamPath.isBlank()) {
            return null;
        }
        String name = jamPath;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        return name.isEmpty() ? null : name.toLowerCase(java.util.Locale.ROOT);
    }

    private SharedPreferences prefs;
    private String gameKey;
    private LinearLayout rowsContainer;
    private TextView headerView;
    private HostControlAction capturing;
    private View capturingRow;
    private boolean swallowBackUp;
    private final Map<String, TextView> bindingViews = new LinkedHashMap<>();

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(AppLanguage.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppTheme.apply(this);
        super.onCreate(savedInstanceState);
        String key = getIntent() != null ? getIntent().getStringExtra(EXTRA_GAME_KEY) : null;
        gameKey = (key == null || key.isBlank()) ? null : key;
        prefs = getSharedPreferences(
                gameKey == null ? PREFS_NAME : prefsNameForGame(gameKey), MODE_PRIVATE);

        float density = getResources().getDisplayMetrics().density;
        int pad = (int) (16 * density);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        String gameName = getIntent() != null ? getIntent().getStringExtra(EXTRA_GAME_NAME) : null;
        TextView title = new TextView(this);
        title.setText(gameKey == null
                ? getString(R.string.keymap_title)
                : getString(R.string.keymap_title_game,
                        gameName != null ? gameName : gameKey));
        title.setTextSize(24f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title, matchWrap());

        headerView = new TextView(this);
        headerView.setTextSize(13f);
        headerView.setPadding(0, pad / 2, 0, pad);
        root.addView(headerView, matchWrap());
        updateHeader(null);

        rowsContainer = new LinearLayout(this);
        rowsContainer.setOrientation(LinearLayout.VERTICAL);
        for (HostControlAction action : HostControlAction.values()) {
            rowsContainer.addView(buildRow(action));
        }
        root.addView(rowsContainer, matchWrap());

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(root);
        setContentView(scroll);
    }

    private static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private View buildRow(HostControlAction action) {
        float density = getResources().getDisplayMetrics().density;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding((int) (8 * density), (int) (10 * density), (int) (8 * density), (int) (10 * density));

        TextView name = new TextView(this);
        name.setText(actionLabel(action));
        name.setTextSize(16f);
        row.addView(name, weighted());

        TextView summary = new TextView(this);
        summary.setText(describeDispatch(action));
        summary.setTextSize(12f);
        summary.setAlpha(0.7f);
        row.addView(summary, weighted());

        TextView binding = new TextView(this);
        binding.setText(bindingLabel(action));
        binding.setTextSize(14f);
        binding.setGravity(Gravity.END);
        row.addView(binding, weighted());
        bindingViews.put(action.id(), binding);

        row.setOnClickListener(v -> beginCapture(action, row));
        row.setOnLongClickListener(v -> {
            prefs.edit().remove(action.id()).apply();
            refreshBindings();
            updateHeader(null);
            return true;
        });
        row.setTag(action);
        return row;
    }

    private LinearLayout.LayoutParams weighted() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.rightMargin = (int) (8 * getResources().getDisplayMetrics().density);
        return params;
    }

    /** Localized display name for a DoJa control action (falls back to the shared name). */
    private String actionLabel(HostControlAction action) {
        switch (action.id()) {
            case "digit0":
                return getString(R.string.action_digit0);
            case "digit1":
                return getString(R.string.action_digit1);
            case "digit2":
                return getString(R.string.action_digit2);
            case "digit3":
                return getString(R.string.action_digit3);
            case "digit4":
                return getString(R.string.action_digit4);
            case "digit5":
                return getString(R.string.action_digit5);
            case "digit6":
                return getString(R.string.action_digit6);
            case "digit7":
                return getString(R.string.action_digit7);
            case "digit8":
                return getString(R.string.action_digit8);
            case "digit9":
                return getString(R.string.action_digit9);
            case "asterisk":
                return getString(R.string.action_asterisk);
            case "pound":
                return getString(R.string.action_pound);
            case "moveLeft":
                return getString(R.string.action_move_left);
            case "moveUp":
                return getString(R.string.action_move_up);
            case "moveRight":
                return getString(R.string.action_move_right);
            case "moveDown":
                return getString(R.string.action_move_down);
            case "select":
                return getString(R.string.action_select);
            case "clear":
                return getString(R.string.action_clear);
            case "softKeyLeft":
                return getString(R.string.action_soft_key_left);
            case "softKeyRight":
                return getString(R.string.action_soft_key_right);
            case "menu":
                return getString(R.string.action_menu);
            case "camera":
                return getString(R.string.action_camera);
            default:
                return action.displayName();
        }
    }

    private String describeDispatch(HostControlAction action) {
        if (action.dispatchKind() == HostControlAction.DispatchKind.HOST_SOFT_KEY) {
            return getString(R.string.dispatch_soft_key, action.dispatchCode() + 1);
        }
        return getString(R.string.dispatch_doja_key, action.dispatchCode());
    }

    private String bindingLabel(HostControlAction action) {
        int keyCode = prefs.getInt(action.id(), -1);
        if (keyCode >= 0) {
            return keyLabel(keyCode);
        }
        if (gameKey != null) {
            // Per-game row is empty: show what the game will actually use
            // (EntryActivity falls back to the global binding per action).
            int global = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(action.id(), -1);
            if (global >= 0) {
                return getString(R.string.binding_follow_global, keyLabel(global));
            }
        }
        return getString(R.string.binding_none);
    }

    String keyLabel(int keyCode) {
        switch (keyCode) {
            case android.view.KeyEvent.KEYCODE_VOLUME_UP:
                return getString(R.string.key_volume_up);
            case android.view.KeyEvent.KEYCODE_VOLUME_DOWN:
                return getString(R.string.key_volume_down);
            case android.view.KeyEvent.KEYCODE_VOLUME_MUTE:
                return getString(R.string.key_volume_mute);
            case android.view.KeyEvent.KEYCODE_BACK:
                return getString(R.string.key_back);
            case android.view.KeyEvent.KEYCODE_HOME:
                return getString(R.string.key_home);
            case android.view.KeyEvent.KEYCODE_ENTER:
                return getString(R.string.key_enter);
            case android.view.KeyEvent.KEYCODE_SPACE:
                return getString(R.string.key_space);
            default:
                break;
        }
        String name = android.view.KeyEvent.keyCodeToString(keyCode);
        return name.startsWith("KEYCODE_") ? name.substring("KEYCODE_".length()) : name;
    }

    private void beginCapture(HostControlAction action, View row) {
        if (capturingRow != null) {
            capturingRow.setBackgroundColor(Color.TRANSPARENT);
        }
        capturing = action;
        capturingRow = row;
        row.setBackgroundColor(0x403080FF);
        updateHeader(action);
    }

    private void endCapture() {
        if (capturingRow != null) {
            capturingRow.setBackgroundColor(Color.TRANSPARENT);
        }
        capturing = null;
        capturingRow = null;
        updateHeader(null);
    }

    private void updateHeader(HostControlAction target) {
        if (target != null) {
            headerView.setText(getString(R.string.header_binding, actionLabel(target)));
            headerView.setTextColor(AppTheme.accentText(this));
        } else if (gameKey != null) {
            headerView.setText(R.string.header_game);
            headerView.setTextColor(AppTheme.secondaryText(this));
        } else {
            headerView.setText(R.string.header_global);
            headerView.setTextColor(AppTheme.secondaryText(this));
        }
    }

    private void refreshBindings() {
        for (HostControlAction action : HostControlAction.values()) {
            TextView view = bindingViews.get(action.id());
            if (view != null) {
                view.setText(bindingLabel(action));
            }
        }
    }

    private void bindKey(HostControlAction action, int keyCode) {
        SharedPreferences.Editor editor = prefs.edit();
        // One physical key drives exactly one action: drop stale duplicates first.
        for (Map.Entry<String, ?> entry : new ArrayList<>(prefs.getAll().entrySet())) {
            if (entry.getValue() instanceof Integer && (Integer) entry.getValue() == keyCode) {
                editor.remove(entry.getKey());
            }
        }
        editor.putInt(action.id(), keyCode);
        editor.apply();
        refreshBindings();
    }

    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean down = event.getAction() == android.view.KeyEvent.ACTION_DOWN;
        if (down && event.getRepeatCount() == 0) {
            if (capturing != null) {
                if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                    endCapture();
                    swallowBackUp = true;
                } else {
                    bindKey(capturing, keyCode);
                    endCapture();
                }
                return true;
            }
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                finish();
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
        if (capturing != null) {
            return true;
        }
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            if (!down && swallowBackUp) {
                swallowBackUp = false;
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
