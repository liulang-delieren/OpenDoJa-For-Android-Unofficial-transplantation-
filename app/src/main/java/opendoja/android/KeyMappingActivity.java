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
 */
public class KeyMappingActivity extends Activity {
    static final String PREFS_NAME = "keymap";

    private SharedPreferences prefs;
    private LinearLayout rowsContainer;
    private TextView headerView;
    private HostControlAction capturing;
    private View capturingRow;
    private boolean swallowBackUp;
    private final Map<String, TextView> bindingViews = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        float density = getResources().getDisplayMetrics().density;
        int pad = (int) (16 * density);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("按键映射");
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
        name.setText(action.displayName());
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

    private static String describeDispatch(HostControlAction action) {
        if (action.dispatchKind() == HostControlAction.DispatchKind.HOST_SOFT_KEY) {
            return "软键 " + (action.dispatchCode() + 1);
        }
        return "DoJa key " + action.dispatchCode();
    }

    private String bindingLabel(HostControlAction action) {
        int keyCode = prefs.getInt(action.id(), -1);
        if (keyCode < 0) {
            return "未绑定";
        }
        return keyLabel(keyCode);
    }

    static String keyLabel(int keyCode) {
        switch (keyCode) {
            case android.view.KeyEvent.KEYCODE_VOLUME_UP:
                return "音量+";
            case android.view.KeyEvent.KEYCODE_VOLUME_DOWN:
                return "音量-";
            case android.view.KeyEvent.KEYCODE_VOLUME_MUTE:
                return "静音";
            case android.view.KeyEvent.KEYCODE_BACK:
                return "返回";
            case android.view.KeyEvent.KEYCODE_HOME:
                return "Home";
            case android.view.KeyEvent.KEYCODE_ENTER:
                return "回车";
            case android.view.KeyEvent.KEYCODE_SPACE:
                return "空格";
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
            headerView.setText("正在绑定: " + target.displayName() + " — 按下实体键（返回键取消）");
            headerView.setTextColor(0x66CCFFFF);
        } else {
            headerView.setText("点选一行后按实体键进行绑定，长按清除；游戏内按系统返回键退出游戏。");
            headerView.setTextColor(0xAAFFFFFF);
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
