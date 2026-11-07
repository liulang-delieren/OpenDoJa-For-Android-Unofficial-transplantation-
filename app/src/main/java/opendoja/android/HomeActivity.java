package opendoja.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import opendoja.host.JamMetadataResolver;
import opendoja.host.OpenDoJaLog;

/**
 * Launcher: title bar with an options menu (load JAM / key mapping / delete
 * game) plus a PPI-scaled grid of every installed i-mode game. Tapping a tile
 * launches it; icons come from the JAM AppIcon entry inside the game jar.
 */
public class HomeActivity extends Activity {
    private static final String TAG = "OpenDoJa";
    private static final int REQUEST_IMPORT_FOLDER = 1;
    private static final int[] MENU_ITEM_RES = {
            R.string.menu_load_jam,
            R.string.menu_key_mapping,
            R.string.menu_delete_game,
            R.string.menu_language,
            R.string.menu_toggle_theme,
            R.string.menu_about,
    };
    private static final int MENU_ACTION_LOAD_JAM = 0;
    private static final int MENU_ACTION_KEY_MAPPING = 1;
    private static final int MENU_ACTION_DELETE_GAME = 2;
    private static final int MENU_ACTION_LANGUAGE = 3;
    private static final int MENU_ACTION_THEME = 4;
    private static final int MENU_ACTION_ABOUT = 5;

    private final Map<String, Bitmap> iconCache = new ConcurrentHashMap<>();
    private ExecutorService worker = Executors.newFixedThreadPool(2);
    private LinearLayout gridContainer;
    private TextView emptyView;
    private PopupWindow menuPopup;
    private int columns = 4;
    private int iconSizePx;

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(AppLanguage.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppTheme.apply(this);
        super.onCreate(savedInstanceState);
        OpenDoJaLog.configureIfUnset(OpenDoJaLog.Level.DEBUG);
        computeGridMetrics();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.addView(buildTitleBar(), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));
        root.addView(buildDivider(), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(12);
        content.setPadding(pad, pad, pad, pad);

        emptyView = new TextView(this);
        emptyView.setText(R.string.home_empty);
        emptyView.setTextColor(AppTheme.secondaryText(this));
        emptyView.setTextSize(15f);
        emptyView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams emptyParams = matchWrap();
        emptyParams.topMargin = dp(96);
        content.addView(emptyView, emptyParams);

        gridContainer = new LinearLayout(this);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(gridContainer, matchWrap());

        scroll.addView(content, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshGames();
    }

    @Override
    protected void onDestroy() {
        worker.shutdownNow();
        super.onDestroy();
    }

    private LinearLayout buildTitleBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(AppTheme.titleBar(this));
        bar.setPadding(0, 0, dp(4), 0);

        TextView title = new TextView(this);
        title.setText(R.string.home_title);
        title.setTextColor(AppTheme.primaryText(this));
        title.setTextSize(17f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        title.setPadding(dp(16), 0, dp(8), 0);
        bar.addView(title, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        TextView menuButton = new TextView(this);
        menuButton.setText("⋮");
        menuButton.setTextSize(26f);
        menuButton.setTextColor(AppTheme.primaryText(this));
        menuButton.setGravity(Gravity.CENTER);
        menuButton.setPadding(dp(16), 0, dp(16), 0);
        menuButton.setClickable(true);
        menuButton.setFocusable(true);
        menuButton.setBackground(new RippleDrawable(
                ColorStateList.valueOf(AppTheme.ripplePress(this)), null,
                new ColorDrawable(AppTheme.pick(this, 0x1FFFFFFF, 0x10000000))));
        menuButton.setOnClickListener(this::showOptionsMenu);
        bar.addView(menuButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        return bar;
    }

    private View buildDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(AppTheme.divider(this));
        return divider;
    }

    private void showOptionsMenu(View anchor) {
        if (menuPopup != null && menuPopup.isShowing()) {
            menuPopup.dismiss();
            menuPopup = null;
            return;
        }
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setMinimumWidth(dp(180));
        menu.setPadding(dp(4), dp(4), dp(4), dp(4));
        GradientDrawable panel = new GradientDrawable();
        panel.setColor(AppTheme.menuBackground(this));
        panel.setStroke(dp(1), AppTheme.pick(this, 0x00000000, 0xFFD7DADF));
        panel.setCornerRadius(dp(8));
        menu.setBackground(panel);

        for (int i = 0; i < MENU_ITEM_RES.length; i++) {
            final int menuAction = i;
            TextView item = new TextView(this);
            item.setText(MENU_ITEM_RES[i]);
            item.setTextColor(AppTheme.rowText(this));
            item.setTextSize(16f);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(dp(18), dp(14), dp(18), dp(14));
            item.setClickable(true);
            item.setForeground(new RippleDrawable(
                    ColorStateList.valueOf(AppTheme.ripplePress(this)), null, null));
            item.setOnClickListener(v -> {
                if (menuPopup != null) {
                    menuPopup.dismiss();
                    menuPopup = null;
                }
                onMenuAction(menuAction);
            });
            menu.addView(item, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        menu.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        PopupWindow popup = new PopupWindow(menu,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.setElevation(dp(8));
        popup.showAsDropDown(anchor, -(menu.getMeasuredWidth() - anchor.getWidth()), 0);
        menuPopup = popup;
    }

    private void onMenuAction(int action) {
        switch (action) {
            case MENU_ACTION_LOAD_JAM:
                onLoadJamClicked();
                break;
            case MENU_ACTION_KEY_MAPPING:
                startActivity(new Intent(this, KeyMappingActivity.class));
                break;
            case MENU_ACTION_DELETE_GAME:
                onDeleteClicked();
                break;
            case MENU_ACTION_LANGUAGE:
                chooseLanguage();
                break;
            case MENU_ACTION_THEME:
                AppTheme.toggle(this);
                break;
            case MENU_ACTION_ABOUT:
                showAboutDialog();
                break;
            default:
                break;
        }
    }

    /** Language picker: follow the system locale, or force Simplified Chinese / English. */
    private void chooseLanguage() {
        final String[] values = {AppLanguage.SYSTEM, AppLanguage.ZH, AppLanguage.EN};
        String[] labels = {
                getString(R.string.language_system),
                getString(R.string.language_zh),
                getString(R.string.language_en),
        };
        String current = AppLanguage.current(this);
        int checked = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                checked = i;
                break;
            }
        }
        final int[] selected = {checked};
        new AlertDialog.Builder(this)
                .setTitle(R.string.language_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> selected[0] = which)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    String target = values[selected[0]];
                    if (!target.equals(AppLanguage.current(this))) {
                        AppLanguage.set(this, target);
                        recreate();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setMessage(getString(R.string.about_message, appVersion()))
                .setPositiveButton(R.string.close, null)
                .show();
    }

    /** Installed versionName, e.g. {@code 0.2.5-port.2(20261107)}. */
    private String appVersion() {
        try {
            String name = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            return name != null ? name : "?";
        } catch (android.content.pm.PackageManager.NameNotFoundException exception) {
            return "?";
        }
    }

    /** Icon size derives from physical PPI; the grid is a fixed 5 columns per row. */
    private void computeGridMetrics() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float xdpi = dm.xdpi > 1f ? dm.xdpi : dm.densityDpi;
        columns = 5;
        float usable = dm.widthPixels - 24f * dm.density;
        int cellPx = (int) (usable / columns);
        int icon = Math.min(Math.round(15f * xdpi / 25.4f), (int) (cellPx * 0.78f));
        iconSizePx = Math.max(icon, dp(48));
    }

    private void refreshGames() {
        worker.execute(() -> {
            List<GameEntry> games = scanGames();
            if (isFinishing() || isDestroyed()) {
                return;
            }
            runOnUiThread(() -> populateGrid(games));
        });
    }

    private List<GameEntry> scanGames() {
        File gamesDir = JamAssets.gamesDir(this);
        File[] jams = gamesDir.listFiles((dir, name) ->
                name.toLowerCase(Locale.ROOT).endsWith(".jam"));
        if (jams == null) {
            jams = new File[0];
        }
        List<GameEntry> games = new ArrayList<>(jams.length);
        for (File jam : jams) {
            String name = stripExtension(jam.getName());
            try {
                Properties properties = JamMetadataResolver.loadJamProperties(jam.toPath());
                String appName = properties.getProperty("AppName");
                if (appName != null && !appName.isBlank()) {
                    name = appName.trim();
                }
            } catch (IOException | RuntimeException exception) {
                Log.w(TAG, "cannot read jam metadata: " + jam.getName(), exception);
            }
            games.add(new GameEntry(jam, name, loadGameIcon(jam, name)));
        }
        games.sort(Comparator.comparing(entry -> entry.displayName,
                String.CASE_INSENSITIVE_ORDER));
        return games;
    }

    private Bitmap loadGameIcon(File jam, String displayName) {
        File jar = siblingFile(jam, ".jar");
        String key = jam.getAbsolutePath() + ":" + jam.lastModified()
                + ":" + (jar != null && jar.exists() ? jar.lastModified() : 0);
        Bitmap cached = iconCache.get(key);
        if (cached != null) {
            return cached;
        }
        Bitmap icon = null;
        if (jar != null && jar.exists()) {
            try {
                Properties properties = JamMetadataResolver.loadJamProperties(jam.toPath());
                String appIcon = properties.getProperty("AppIcon");
                if (appIcon != null && !appIcon.isBlank()) {
                    String[] candidates = appIcon.split(",");
                    // DoJa lists 48x48 first and 96x96 second: try the bigger one first.
                    for (int i = candidates.length - 1; i >= 0; i--) {
                        icon = decodeJarImage(jar, candidates[i].trim());
                        if (icon != null) {
                            break;
                        }
                    }
                }
            } catch (IOException | RuntimeException exception) {
                Log.w(TAG, "cannot read app icon: " + jam.getName(), exception);
            }
        }
        if (icon == null) {
            icon = placeholderIcon(displayName);
        }
        iconCache.put(key, icon);
        return icon;
    }

    private Bitmap decodeJarImage(File jar, String entryName) {
        try (ZipFile zip = new ZipFile(jar)) {
            ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) {
                return null;
            }
            try (InputStream in = zip.getInputStream(entry)) {
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                if (bitmap == null) {
                    return null;
                }
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                float scale = iconSizePx / (float) Math.max(width, height);
                if (Math.abs(scale - 1f) < 0.02f) {
                    return bitmap;
                }
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap,
                        Math.max(1, Math.round(width * scale)),
                        Math.max(1, Math.round(height * scale)), true);
                if (scaled != bitmap) {
                    bitmap.recycle();
                }
                return scaled;
            }
        } catch (IOException | RuntimeException exception) {
            Log.w(TAG, "cannot decode " + entryName + " from " + jar.getName(), exception);
            return null;
        }
    }

    private Bitmap placeholderIcon(String displayName) {
        int size = iconSizePx;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float[] hsv = new float[3];
        hsv[0] = Math.floorMod(displayName.hashCode(), 360);
        hsv[1] = 0.45f;
        hsv[2] = 0.62f;
        Paint tilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tilePaint.setColor(Color.HSVToColor(hsv));
        float radius = size / 6f;
        canvas.drawRoundRect(0f, 0f, size - 1f, size - 1f, radius, radius, tilePaint);

        String initial = displayName.isEmpty()
                ? "?"
                : new StringBuilder().appendCodePoint(displayName.codePointAt(0)).toString();
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size * 0.46f);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        float baseline = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(initial, size / 2f, baseline, textPaint);
        return bitmap;
    }

    private void populateGrid(List<GameEntry> games) {
        gridContainer.removeAllViews();
        if (games.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            gridContainer.setVisibility(View.GONE);
            return;
        }
        emptyView.setVisibility(View.GONE);
        gridContainer.setVisibility(View.VISIBLE);

        LinearLayout row = null;
        for (int i = 0; i < games.size(); i++) {
            if (i % columns == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                gridContainer.addView(row, matchWrap());
            }
            LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            cellParams.setMargins(dp(5), dp(5), dp(5), dp(5));
            row.addView(createGameCell(games.get(i)), cellParams);
        }
    }

    private LinearLayout createGameCell(GameEntry game) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER_HORIZONTAL);
        cell.setPadding(dp(6), dp(10), dp(6), dp(10));
        cell.setClickable(true);
        cell.setFocusable(true);

        // No tile background: icons sit directly on the grid background. The
        // rounded drawable is kept only as a ripple mask so the press
        // highlight still follows the tile shape.
        GradientDrawable rippleMask = new GradientDrawable();
        rippleMask.setColor(0xFFFFFFFF);
        rippleMask.setCornerRadius(dp(10));
        cell.setForeground(new RippleDrawable(
                ColorStateList.valueOf(AppTheme.ripplePress(this)), null, rippleMask));

        ImageView icon = new ImageView(this);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setBackgroundColor(Color.WHITE);
        icon.setImageBitmap(game.icon);
        cell.addView(icon, new LinearLayout.LayoutParams(iconSizePx, iconSizePx));

        TextView name = new TextView(this);
        name.setText(game.displayName);
        name.setTextColor(AppTheme.pick(this, 0xFFE0E0E0, 0xFF1F2328));
        name.setTextSize(13f);
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(2);
        name.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams nameParams = matchWrap();
        nameParams.topMargin = dp(6);
        cell.addView(name, nameParams);

        cell.setOnClickListener(v -> launchJam(game.jam));
        cell.setOnLongClickListener(v -> {
            openGameSettings(game);
            return true;
        });
        return cell;
    }

    /** Long-press on a game tile opens that game's own settings (key mapping, ...). */
    private void openGameSettings(GameEntry game) {
        Intent intent = new Intent(this, GameSettingsActivity.class);
        intent.putExtra(GameSettingsActivity.EXTRA_JAM_PATH, game.jam.getAbsolutePath());
        intent.putExtra(GameSettingsActivity.EXTRA_GAME_NAME, game.displayName);
        startActivity(intent);
    }

    private static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void onLoadJamClicked() {
        showJamPicker();
    }

    private void showJamPicker() {
        File gamesDir = JamAssets.gamesDir(this);
        File[] jams = gamesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jam"));
        if (jams == null) {
            jams = new File[0];
        }
        Arrays.sort(jams, Comparator.comparing(File::getName));
        List<File> jamList = Arrays.asList(jams);

        String[] labels = new String[jamList.size() + 1];
        for (int i = 0; i < jamList.size(); i++) {
            labels[i] = describeJam(jamList.get(i));
        }
        labels[jamList.size()] = getString(R.string.import_from_folder);

        new AlertDialog.Builder(this)
                .setTitle(R.string.load_jam_title)
                .setItems(labels, (dialog, which) -> {
                    if (which < jamList.size()) {
                        launchJam(jamList.get(which));
                    } else {
                        pickImportFolder();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private String describeJam(File jam) {
        String name = jam.getName();
        try {
            Properties properties = JamMetadataResolver.loadJamProperties(jam.toPath());
            String appName = properties.getProperty("AppName");
            if (appName != null && !appName.isBlank()) {
                return appName + " (" + name + ")";
            }
        } catch (IOException ignored) {
        }
        return name;
    }

    private void launchJam(File jam) {
        Intent intent = new Intent(this, EntryActivity.class);
        intent.putExtra("jamPath", jam.getAbsolutePath());
        startActivity(intent);
    }

    private void onDeleteClicked() {
        File gamesDir = JamAssets.gamesDir(this);
        File[] found = gamesDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jam"));
        final File[] jams = found == null ? new File[0] : found;
        Arrays.sort(jams, Comparator.comparing(File::getName));
        if (jams.length == 0) {
            Toast.makeText(this, R.string.toast_no_installed_games, Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = new String[jams.length];
        for (int i = 0; i < jams.length; i++) {
            labels[i] = describeJam(jams[i]);
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_game_title)
                .setItems(labels, (dialog, which) -> confirmDelete(jams[which]))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete(File jam) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_game_title)
                .setMessage(getString(R.string.delete_confirm_message, describeJam(jam)))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    int deleted = deleteGameFiles(jam);
                    Toast.makeText(this, getString(R.string.toast_deleted_files, deleted),
                            Toast.LENGTH_SHORT).show();
                    refreshGames();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** Removes the jam plus every sibling/cache file that shares its basename. */
    private int deleteGameFiles(File jam) {
        String base = stripExtension(jam.getName()).toLowerCase();
        int deleted = 0;
        File[] siblings = jam.getParentFile().listFiles();
        if (siblings != null) {
            for (File sibling : siblings) {
                String name = sibling.getName().toLowerCase();
                if (name.equals(base + ".jam") || name.equals(base + ".jar")
                        || name.equals(base + ".sp") || name.equals(base + ".dex")) {
                    if (sibling.delete()) {
                        deleted++;
                    }
                }
            }
        }
        File dexCache = new File(getCodeCacheDir(), "jam-dex");
        deleted += deleteMatchingRecursive(dexCache, base);
        // Drop this game's own keymap layer so a re-imported game with the same
        // name does not inherit stale per-game bindings.
        String gameKey = KeyMappingActivity.gameKeyFromJamPath(jam.getAbsolutePath());
        if (gameKey != null) {
            getSharedPreferences(KeyMappingActivity.prefsNameForGame(gameKey), MODE_PRIVATE)
                    .edit().clear().apply();
            GameSettingsActivity.clearGameSettings(this, gameKey);
        }
        return deleted;
    }

    private static int deleteMatchingRecursive(File file, String baseLower) {
        if (!file.exists()) {
            return 0;
        }
        if (file.isDirectory()) {
            int deleted = 0;
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (child.getName().toLowerCase().startsWith(baseLower)) {
                        deleted += deleteRecursively(child);
                    } else {
                        deleted += deleteMatchingRecursive(child, baseLower);
                    }
                }
            }
            return deleted;
        }
        return file.getName().toLowerCase().startsWith(baseLower) && file.delete() ? 1 : 0;
    }

    private static int deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        return file.delete() ? 1 : 0;
    }

    private void pickImportFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        try {
            startActivityForResult(intent, REQUEST_IMPORT_FOLDER);
        } catch (RuntimeException exception) {
            Toast.makeText(this, R.string.toast_no_folder_picker, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_IMPORT_FOLDER || resultCode != RESULT_OK || data == null) {
            return;
        }
        Uri treeUri = data.getData();
        if (treeUri == null) {
            return;
        }
        ImportResult result = importJamFolder(treeUri);
        if (result.games > 0) {
            String message = result.errors.isEmpty()
                    ? getString(R.string.toast_imported_games, result.games)
                    : getString(R.string.toast_imported_games_partial, result.games,
                            String.join("\n", result.errors));
            Toast.makeText(this, message,
                    result.errors.isEmpty() ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
            refreshGames();
        } else if (result.errors.isEmpty()) {
            Toast.makeText(this, R.string.toast_nothing_importable, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.toast_import_failed,
                    String.join("\n", result.errors)), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Imports games from the picked folder. A game is only imported when its
     * .jam, .jar and (if the jam declares SPsize) .sp files all live in that same
     * folder - i-mode titles load their assets from the scratchpad .sp file, so an
     * incomplete folder would install a broken game. Imported files are renamed to
     * a canonical shared basename so the runtime can resolve jar/sp next to the jam.
     */
    private ImportResult importJamFolder(Uri treeUri) {
        ImportResult result = new ImportResult();
        ContentResolver resolver = getContentResolver();
        String treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri);
        Uri children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocumentId);

        Map<String, Child> childDocs = new LinkedHashMap<>();
        try (Cursor cursor = resolver.query(children, new String[]{
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
        }, null, null, null)) {
            if (cursor == null) {
                result.errors.add(getString(R.string.error_cannot_read_folder));
                return result;
            }
            while (cursor.moveToNext()) {
                String documentId = cursor.getString(0);
                String displayName = cursor.getString(1);
                String mimeType = cursor.getString(2);
                if (displayName == null || mimeType == null
                        || DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType)) {
                    continue;
                }
                childDocs.put(displayName.toLowerCase(), new Child(documentId, displayName));
            }
        } catch (RuntimeException exception) {
            Log.w(TAG, "folder listing failed", exception);
            result.errors.add(getString(R.string.error_cannot_read_folder_detail,
                    exception.getMessage()));
            return result;
        }

        List<String> jamNames = namesEndingIn(childDocs, ".jam");
        if (jamNames.isEmpty()) {
            result.errors.add(getString(R.string.error_no_jam_files));
            return result;
        }
        List<String> jarNames = namesEndingIn(childDocs, ".jar");

        // destName(lower) -> source child; canonical names keep jam/jar/sp resolvable.
        Map<String, Child> toCopy = new LinkedHashMap<>();
        for (String jamName : jamNames) {
            String base = stripExtension(jamName);
            List<String> reasons = new ArrayList<>();

            String jarName = findChild(childDocs, base + ".jar");
            if (jarName == null) {
                if (jarNames.isEmpty()) {
                    reasons.add(getString(R.string.error_missing_jar));
                } else if (jarNames.size() == 1) {
                    jarName = jarNames.get(0);
                } else {
                    reasons.add(getString(R.string.error_no_matching_jar));
                }
            }

            String spName = findChild(childDocs, base + ".sp");
            if (spName == null && jamDeclaresScratchpad(treeUri, childDocs.get(jamName.toLowerCase()))) {
                reasons.add(getString(R.string.error_missing_sp));
            }

            if (!reasons.isEmpty()) {
                result.errors.add(getString(R.string.error_game_skipped, jamName,
                        String.join(getString(R.string.list_separator), reasons)));
                continue;
            }

            toCopy.put(jamName.toLowerCase(), childDocs.get(jamName.toLowerCase()));
            toCopy.put((base + ".jar").toLowerCase(), childDocs.get(jarName.toLowerCase()));
            if (spName != null) {
                toCopy.put((base + ".sp").toLowerCase(), childDocs.get(spName.toLowerCase()));
            }
            result.games++;
        }

        if (toCopy.isEmpty()) {
            return result;
        }

        File gamesDir = JamAssets.gamesDir(this);
        for (Map.Entry<String, Child> entry : toCopy.entrySet()) {
            String destName = entry.getKey();
            Child source = entry.getValue();
            Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, source.documentId);
            try (InputStream in = resolver.openInputStream(documentUri);
                 OutputStream out = new FileOutputStream(new File(gamesDir, destName))) {
                if (in == null) {
                    result.errors.add(getString(R.string.error_cannot_read_file, source.displayName));
                    continue;
                }
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }
                result.files++;
            } catch (IOException | SecurityException exception) {
                Log.w(TAG, "cannot import " + source.displayName, exception);
                result.errors.add(getString(R.string.error_copy_failed, source.displayName));
            }
        }
        return result;
    }

    private boolean jamDeclaresScratchpad(Uri treeUri, Child jamChild) {
        if (jamChild == null) {
            return true;
        }
        Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, jamChild.documentId);
        try (InputStream in = getContentResolver().openInputStream(documentUri)) {
            if (in == null) {
                return true;
            }
            Properties properties = new Properties();
            properties.load(in);
            String spSize = properties.getProperty("SPsize");
            return spSize != null && !spSize.isBlank();
        } catch (IOException | RuntimeException exception) {
            Log.w(TAG, "cannot read jam properties of " + jamChild.displayName, exception);
            // Be strict: when in doubt the game needs its .sp next to the .jam.
            return true;
        }
    }

    private static File siblingFile(File jam, String extension) {
        String name = stripExtension(jam.getName());
        return new File(jam.getParentFile(), name + extension);
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }

    private static List<String> namesEndingIn(Map<String, Child> childDocs, String suffix) {
        List<String> names = new ArrayList<>();
        for (Child child : childDocs.values()) {
            if (child.displayName.toLowerCase().endsWith(suffix)) {
                names.add(child.displayName);
            }
        }
        return names;
    }

    private static String findChild(Map<String, Child> childDocs, String lowerName) {
        Child child = childDocs.get(lowerName.toLowerCase());
        return child == null ? null : child.displayName;
    }

    private static final class GameEntry {
        final File jam;
        final String displayName;
        final Bitmap icon;

        GameEntry(File jam, String displayName, Bitmap icon) {
            this.jam = jam;
            this.displayName = displayName;
            this.icon = icon;
        }
    }

    private static final class Child {
        final String documentId;
        final String displayName;

        Child(String documentId, String displayName) {
            this.documentId = documentId;
            this.displayName = displayName;
        }
    }

    private static final class ImportResult {
        int games;
        int files;
        final List<String> errors = new ArrayList<>();
    }
}
