package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Selects, edits, imports, and exports application color themes. */
public class ThemeSettingsActivity extends Activity {
    private static final int REQUEST_IMPORT = 4301;
    private static final int REQUEST_EXPORT = 4302;
    private static final int MAX_THEME_FILE_BYTES = 128 * 1024;
    private static final int SELECT_APP_THEME = 0;
    private static final int SELECT_SYSTEM_LIGHT_THEME = 1;
    private static final int SELECT_SYSTEM_DARK_THEME = 2;

    private SharedPreferences preferences;
    private LinearLayout customThemeList;
    private LinearLayout normalSelector;
    private LinearLayout systemThemeSelectors;
    private LinearLayout systemLightSelector;
    private LinearLayout systemDarkSelector;
    private Theme.Palette pendingExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        if (savedInstanceState != null) {
            pendingExport = Theme.paletteById(this,
                    savedInstanceState.getString("pending_export_theme_id", ""));
        }
        buildLayout();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (pendingExport != null) outState.putString("pending_export_theme_id", pendingExport.id);
    }

    private void buildLayout() {
        Theme.applySystemBars(this);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Theme.background(this));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);

        TextView title = text(MainActivity.text("テーマ", "Themes"), 28, Theme.text(this));
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        root.addView(title);
        TextView intro = text(MainActivity.text(
                "通常時とプライベートブラウジング時に共通するテーマを選択できます。組み込みテーマを元に色を編集し、JSONファイルで共有できます。",
                "Choose one theme shared by normal and private browsing. Customize colors from a built-in theme and share them as JSON files."),
                14, Theme.muted(this));
        intro.setPadding(0, dp(4), 0, dp(16));
        root.addView(intro);

        root.addView(sectionLabel(MainActivity.text("使用するテーマ", "App theme")));
        normalSelector = themeSelector(SELECT_APP_THEME);
        root.addView(normalSelector, selectorParams());

        systemThemeSelectors = new LinearLayout(this);
        systemThemeSelectors.setOrientation(LinearLayout.VERTICAL);
        systemThemeSelectors.addView(sectionLabel(MainActivity.text(
                "端末テーマごとの配色", "Themes for each device mode")));
        TextView systemHelp = text(MainActivity.text(
                "「端末のテーマに従う」場合に、ライトモード時とダークモード時に使うテーマを指定します。",
                "Choose the themes used in light and dark device modes when following the device theme."),
                13, Theme.muted(this));
        systemHelp.setPadding(0, 0, 0, dp(5));
        systemThemeSelectors.addView(systemHelp);
        systemThemeSelectors.addView(fieldLabel(MainActivity.text(
                "ライトモード時", "In light mode")));
        systemLightSelector = themeSelector(SELECT_SYSTEM_LIGHT_THEME);
        systemThemeSelectors.addView(systemLightSelector, selectorParams());
        systemThemeSelectors.addView(fieldLabel(MainActivity.text(
                "ダークモード時", "In dark mode")));
        systemDarkSelector = themeSelector(SELECT_SYSTEM_DARK_THEME);
        systemThemeSelectors.addView(systemDarkSelector, selectorParams());
        root.addView(systemThemeSelectors);

        root.addView(sectionLabel(MainActivity.text("カスタムテーマ", "Custom themes")));
        root.addView(actionButton(MainActivity.text("カスタムテーマを作成", "Create custom theme"),
                v -> chooseBaseTheme()));
        root.addView(actionButton(MainActivity.text("テーマファイルをインポート", "Import theme file"),
                v -> openImportPicker()));

        customThemeList = new LinearLayout(this);
        customThemeList.setOrientation(LinearLayout.VERTICAL);
        root.addView(customThemeList);

        refreshSelectors();
        renderCustomThemes();
    }

    private void refreshSelectors() {
        renderSelector(normalSelector, selectedChoice(SELECT_APP_THEME));
        renderSelector(systemLightSelector, selectedChoice(SELECT_SYSTEM_LIGHT_THEME));
        renderSelector(systemDarkSelector, selectedChoice(SELECT_SYSTEM_DARK_THEME));
        systemThemeSelectors.setVisibility(Theme.MODE_SYSTEM.equals(Theme.normalSelection(this))
                ? View.VISIBLE : View.GONE);
    }

    private List<Choice> choices(int target) {
        List<Choice> result = new ArrayList<>();
        if (target == SELECT_APP_THEME) {
            result.add(new Choice(Theme.MODE_SYSTEM, Theme.displayName(this, Theme.MODE_SYSTEM),
                    Theme.previewPalette(this, Theme.MODE_SYSTEM)));
        }
        for (Theme.Palette palette : Theme.selectablePalettes(this)) {
            if (target == SELECT_SYSTEM_LIGHT_THEME && palette.dark) continue;
            if (target == SELECT_SYSTEM_DARK_THEME && !palette.dark) continue;
            result.add(new Choice(palette.id, Theme.displayName(this, palette.id), palette));
        }
        return result;
    }

    private Choice selectedChoice(int target) {
        List<Choice> choices = choices(target);
        String selectedId = selectedId(target);
        for (Choice choice : choices) {
            if (choice.id.equals(selectedId)) return choice;
        }
        return choices.get(0);
    }

    private String selectedId(int target) {
        if (target == SELECT_SYSTEM_LIGHT_THEME) return Theme.systemLightSelection(this);
        if (target == SELECT_SYSTEM_DARK_THEME) return Theme.systemDarkSelection(this);
        return Theme.normalSelection(this);
    }

    private LinearLayout themeSelector(int target) {
        LinearLayout selector = new LinearLayout(this);
        selector.setOrientation(LinearLayout.HORIZONTAL);
        selector.setGravity(Gravity.CENTER_VERTICAL);
        selector.setPadding(dp(12), dp(8), dp(10), dp(8));
        selector.setBackground(fieldBackground());
        selector.setClickable(true);
        selector.setFocusable(true);
        selector.setContentDescription(target == SELECT_APP_THEME
                ? MainActivity.text("アプリのテーマを選択", "Choose app theme")
                : target == SELECT_SYSTEM_LIGHT_THEME
                ? MainActivity.text("ライトモード時のテーマを選択", "Choose light-mode theme")
                : MainActivity.text("ダークモード時のテーマを選択", "Choose dark-mode theme"));
        selector.setOnClickListener(v -> showThemeDropdown(selector, target));
        return selector;
    }

    private void renderSelector(LinearLayout selector, Choice choice) {
        selector.removeAllViews();
        selector.addView(swatchStrip(choice.palette, dp(32)),
                new LinearLayout.LayoutParams(dp(104), dp(32)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        TextView title = text(choice.label, 16, Theme.text(this));
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        labels.addView(title);
        labels.addView(text(choiceSubtitle(choice), 12, Theme.muted(this)));
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        labelParams.setMargins(dp(12), 0, dp(6), 0);
        selector.addView(labels, labelParams);

        ImageView arrow = new ImageView(this);
        arrow.setImageResource(R.drawable.ic_arrow_down);
        arrow.setColorFilter(Theme.muted(this));
        selector.addView(arrow, new LinearLayout.LayoutParams(dp(22), dp(22)));
    }

    private void showThemeDropdown(View anchor, int target) {
        List<Choice> choices = choices(target);
        String selectedId = selectedId(target);

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(8), dp(8), dp(8), dp(8));
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackground(dropdownBackground());
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        PopupWindow popup = new PopupWindow(scroll, anchor.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setElevation(dp(10));
        for (Choice choice : choices) {
            list.addView(themeChoiceRow(choice, choice.id.equals(selectedId), popup, target),
                    dropdownRowParams());
        }
        int width = Math.max(anchor.getWidth(), dp(280));
        scroll.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(dp(420), View.MeasureSpec.AT_MOST));
        popup.setWidth(width);
        popup.setHeight(Math.min(scroll.getMeasuredHeight(), dp(420)));
        popup.showAsDropDown(anchor, 0, dp(4));
    }

    private View themeChoiceRow(Choice choice, boolean selected, PopupWindow popup, int target) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(9), dp(12), dp(9));
        row.setBackground(dropdownRowBackground(selected));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> {
            String current = selectedId(target);
            if (choice.id.equals(current)) {
                popup.dismiss();
                return;
            }
            String preferenceKey = target == SELECT_SYSTEM_LIGHT_THEME
                    ? Theme.PREF_SYSTEM_LIGHT_THEME
                    : target == SELECT_SYSTEM_DARK_THEME
                    ? Theme.PREF_SYSTEM_DARK_THEME : Theme.PREF_NORMAL_THEME;
            preferences.edit().putString(preferenceKey, choice.id).apply();
            Theme.invalidateCache();
            popup.dismiss();
            recreate();
        });

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text(choice.label, 16, Theme.text(this));
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        if (selected) {
            ImageView check = new ImageView(this);
            check.setImageResource(R.drawable.ic_check);
            check.setColorFilter(Theme.accent(this));
            header.addView(check, new LinearLayout.LayoutParams(dp(22), dp(22)));
        }
        row.addView(header);
        TextView subtitle = text(choiceSubtitle(choice), 12, Theme.muted(this));
        subtitle.setPadding(0, dp(1), 0, dp(7));
        row.addView(subtitle);
        row.addView(swatchStrip(choice.palette, dp(24)), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(24)));
        return row;
    }

    private String choiceSubtitle(Choice choice) {
        if (Theme.MODE_SYSTEM.equals(choice.id)) {
            return String.format(Locale.getDefault(), MainActivity.text(
                            "ライト: %1$s / ダーク: %2$s", "Light: %1$s / Dark: %2$s"),
                    Theme.displayName(this, Theme.systemLightSelection(this)),
                    Theme.displayName(this, Theme.systemDarkSelection(this)));
        }
        if (choice.id.startsWith(Theme.CUSTOM_PREFIX)) {
            return MainActivity.text("カスタムテーマ", "Custom theme");
        }
        return choice.palette.dark
                ? MainActivity.text("組み込みテーマ・ダーク系", "Built-in theme · Dark")
                : MainActivity.text("組み込みテーマ・ライト系", "Built-in theme · Light");
    }

    private LinearLayout swatchStrip(Theme.Palette palette, int height) {
        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        int[] colors = {palette.background, palette.surface, palette.text, palette.myPostMarker,
                palette.treeConnector, palette.sidebarUnread, palette.metricLow, palette.metricHigh};
        for (int i = 0; i < colors.length; i++) {
            View swatch = new View(this);
            GradientDrawable background = new GradientDrawable();
            background.setColor(colors[i]);
            background.setStroke(dp(1), palette.border);
            background.setCornerRadius(dp(4));
            swatch.setBackground(background);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, height, 1);
            if (i > 0) params.setMargins(dp(3), 0, 0, 0);
            strip.addView(swatch, params);
        }
        return strip;
    }

    private void renderCustomThemes() {
        customThemeList.removeAllViews();
        List<Theme.Palette> custom = Theme.customPalettes(this);
        if (custom.isEmpty()) {
            TextView empty = text(MainActivity.text("カスタムテーマはまだありません。", "No custom themes yet."),
                    14, Theme.muted(this));
            empty.setPadding(0, dp(14), 0, dp(4));
            customThemeList.addView(empty);
            return;
        }
        for (Theme.Palette palette : custom) customThemeList.addView(themeCard(palette));
    }

    private View themeCard(Theme.Palette palette) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.setBackground(cardBackground());

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = text(palette.name, 17, Theme.text(this));
        name.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        titleRow.addView(name, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView type = text(palette.dark
                        ? MainActivity.text("ダーク系", "Dark")
                        : MainActivity.text("ライト系", "Light"),
                12, Theme.muted(this));
        titleRow.addView(type);
        card.addView(titleRow);

        LinearLayout swatches = new LinearLayout(this);
        swatches.setOrientation(LinearLayout.HORIZONTAL);
        swatches.setPadding(0, dp(8), 0, dp(8));
        int[] colors = {palette.background, palette.surface, palette.text, palette.myPostMarker,
                palette.treeConnector, palette.sidebarUnread, palette.metricLow, palette.metricHigh};
        for (int color : colors) {
            View swatch = new View(this);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(color);
            drawable.setStroke(dp(1), Theme.border(this));
            drawable.setCornerRadius(dp(5));
            swatch.setBackground(drawable);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(28), 1);
            params.setMargins(0, 0, dp(6), 0);
            swatches.addView(swatch, params);
        }
        card.addView(swatches);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.addView(smallButton(MainActivity.text("編集", "Edit"), v -> showEditor(palette.copy())), actionParams());
        actions.addView(smallButton(MainActivity.text("エクスポート", "Export"), v -> openExportPicker(palette)), actionParams());
        actions.addView(smallButton(MainActivity.text("削除", "Delete"), v -> confirmDelete(palette)), actionParams());
        card.addView(actions);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(10), 0, 0);
        card.setLayoutParams(params);
        return card;
    }

    private void chooseBaseTheme() {
        List<Theme.Palette> builtIns = Theme.builtInPalettes();
        String[] labels = new String[builtIns.size()];
        for (int i = 0; i < builtIns.size(); i++) {
            labels[i] = Theme.displayName(this, builtIns.get(i).id);
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("元にするテーマ", "Base theme"))
                .setItems(labels, (d, which) -> {
                    Theme.Palette source = builtIns.get(which);
                    showEditor(Theme.newCustomPalette(source,
                            Theme.displayName(this, source.id) + MainActivity.text(" カスタム", " Custom")));
                })
                .setNegativeButton(MainActivity.text("キャンセル", "Cancel"), null)
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private void showEditor(Theme.Palette palette) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(18), dp(6), dp(18), dp(8));
        form.setBackgroundColor(Theme.surface(this));
        scroll.addView(form);

        EditText name = editField(palette.name);
        form.addView(fieldLabel(MainActivity.text("テーマ名", "Theme name")));
        form.addView(name, fieldParams());

        CheckBox dark = new CheckBox(this);
        dark.setText(MainActivity.text("ダーク系（ステータスバーの明るいアイコンを使用）",
                "Dark theme (use light system-bar icons)"));
        dark.setTextColor(Theme.text(this));
        dark.setChecked(palette.dark);
        Theme.tintCompoundButton(this, dark);
        form.addView(dark);

        TextView colorHelp = text(MainActivity.text(
                "#RRGGBBを直接入力するか、各欄の右にある色ボタンからGUIで選択できます。",
                "Enter #RRGGBB directly, or use the color button beside each field to choose visually."),
                13, Theme.muted(this));
        colorHelp.setPadding(0, dp(6), 0, dp(4));
        form.addView(colorHelp);

        Map<String, EditText> inputs = new LinkedHashMap<>();
        for (String key : Theme.EDITABLE_COLOR_KEYS) {
            form.addView(fieldLabel(colorLabel(key)));
            EditText input = editField(Theme.Palette.colorHex(palette.color(key)));
            inputs.put(key, input);
            form.addView(colorEditRow(input, palette.color(key)));
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("テーマを編集", "Edit theme"))
                .setView(scroll)
                .setNegativeButton(MainActivity.text("キャンセル", "Cancel"), null)
                .setPositiveButton(MainActivity.text("保存", "Save"), null)
                .create();
        dialog.setOnShowListener(d -> {
            Theme.styleDialog(dialog, this);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                try {
                    String nextName = name.getText().toString().trim();
                    if (nextName.isEmpty()) throw new IllegalArgumentException(
                            MainActivity.text("テーマ名を入力してください", "Enter a theme name"));
                    palette.name = nextName;
                    palette.dark = dark.isChecked();
                    for (Map.Entry<String, EditText> entry : inputs.entrySet()) {
                        String value = entry.getValue().getText().toString().trim();
                        if (!value.matches("#[0-9a-fA-F]{6}")) {
                            throw new IllegalArgumentException(colorLabel(entry.getKey()) + ": #RRGGBB");
                        }
                        palette.setColor(entry.getKey(), Color.parseColor(value));
                    }
                    Theme.saveCustomPalette(this, palette);
                    dialog.dismiss();
                    Toast.makeText(this, MainActivity.text("テーマを保存しました", "Theme saved"),
                            Toast.LENGTH_SHORT).show();
                    recreate();
                } catch (Exception error) {
                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
        dialog.show();
    }

    private String colorLabel(String key) {
        switch (key) {
            case "background": return MainActivity.text("画面背景", "Screen background");
            case "topBar": return MainActivity.text("トップ・ボトムバー", "Top and bottom bars");
            case "surface": return MainActivity.text("カード・パネル", "Cards and panels");
            case "post": return MainActivity.text("投稿背景", "Post background");
            case "unread": return MainActivity.text("未読投稿背景", "Unread post background");
            case "field": return MainActivity.text("入力欄", "Input fields");
            case "menu": return MainActivity.text("メニュー", "Menus");
            case "text": return MainActivity.text("本文", "Primary text");
            case "muted": return MainActivity.text("補助テキスト", "Secondary text");
            case "subtle": return MainActivity.text("弱い補助表示", "Subtle content");
            case "border": return MainActivity.text("境界線", "Borders");
            case "strongBorder": return MainActivity.text("強い境界線", "Strong borders");
            case "active": return MainActivity.text("選択・検索・優先ワード強調", "Selection, search, and priority-word highlight");
            case "linkHighlight": return MainActivity.text("リンク強調", "Link highlight");
            case "accent": return MainActivity.text("アクセント", "Accent");
            case "sidebar": return MainActivity.text("スクロールサイドバー", "Scroll sidebar");
            case "sidebarThumb": return MainActivity.text("サイドバーつまみ", "Sidebar thumb");
            case "sidebarUnread": return MainActivity.text("サイドバー未読範囲", "Sidebar unread range");
            case "myPostMarker": return MainActivity.text("自分の投稿の左バー", "My-post left bar");
            case "replyPostMarker": return MainActivity.text("自分への返信の左バー", "Reply-to-me left bar");
            case "treeConnector": return MainActivity.text("ツリー接続線", "Tree connector lines");
            case "metricLow": return MainActivity.text("情報グラデーション（弱）", "Metric gradient (low)");
            case "metricHigh": return MainActivity.text("情報グラデーション（強）", "Metric gradient (high)");
            default: return key;
        }
    }

    private void confirmDelete(Theme.Palette palette) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("テーマを削除", "Delete theme"))
                .setMessage(MainActivity.text("「" + palette.name + "」を削除しますか？",
                        "Delete “" + palette.name + "”?"))
                .setNegativeButton(MainActivity.text("キャンセル", "Cancel"), null)
                .setPositiveButton(MainActivity.text("削除", "Delete"), (d, which) -> {
                    try {
                        Theme.deleteCustomPalette(this, palette.id);
                        recreate();
                    } catch (Exception error) {
                        Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private void openImportPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, REQUEST_IMPORT);
    }

    private void openExportPicker(Theme.Palette palette) {
        pendingExport = palette.copy();
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, safeFileName(palette.name) + ".cuspidroid-theme.json");
        startActivityForResult(intent, REQUEST_EXPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        if (requestCode == REQUEST_IMPORT) importTheme(data.getData());
        else if (requestCode == REQUEST_EXPORT && pendingExport != null) exportTheme(data.getData());
    }

    private void importTheme(Uri uri) {
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) throw new IllegalArgumentException("Unable to open file");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int total = 0;
            int read;
            while ((read = input.read(buffer)) >= 0) {
                total += read;
                if (total > MAX_THEME_FILE_BYTES) throw new IllegalArgumentException(
                        MainActivity.text("テーマファイルが大きすぎます", "Theme file is too large"));
                output.write(buffer, 0, read);
            }
            Theme.Palette palette = Theme.importJson(new JSONObject(
                    output.toString(StandardCharsets.UTF_8.name())));
            Theme.saveCustomPalette(this, palette);
            Toast.makeText(this, MainActivity.text("テーマをインポートしました", "Theme imported"),
                    Toast.LENGTH_SHORT).show();
            recreate();
        } catch (Exception error) {
            Toast.makeText(this, MainActivity.text("テーマを読み込めませんでした: ",
                    "Could not import theme: ") + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void exportTheme(Uri uri) {
        try (OutputStream output = getContentResolver().openOutputStream(uri, "wt")) {
            if (output == null) throw new IllegalArgumentException("Unable to open file");
            byte[] bytes = Theme.exportJson(pendingExport).toString(2).getBytes(StandardCharsets.UTF_8);
            output.write(bytes);
            output.flush();
            Toast.makeText(this, MainActivity.text("テーマをエクスポートしました", "Theme exported"),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception error) {
            Toast.makeText(this, MainActivity.text("テーマを書き出せませんでした: ",
                    "Could not export theme: ") + error.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            pendingExport = null;
        }
    }

    private View colorEditRow(EditText input, int initialColor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(input, new LinearLayout.LayoutParams(0, dp(48), 1));

        ImageView picker = new ImageView(this);
        picker.setImageResource(R.drawable.ic_edit);
        picker.setPadding(dp(12), dp(12), dp(12), dp(12));
        picker.setContentDescription(MainActivity.text("GUIで色を選択", "Choose color visually"));
        updateColorButton(picker, initialColor);
        picker.setOnClickListener(v -> showColorPicker(input));
        LinearLayout.LayoutParams pickerParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        pickerParams.setMargins(dp(8), 0, 0, 0);
        row.addView(picker, pickerParams);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String value = editable.toString().trim();
                if (value.matches("#[0-9a-fA-F]{6}")) {
                    updateColorButton(picker, Color.parseColor(value));
                }
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        params.setMargins(0, dp(2), 0, dp(7));
        row.setLayoutParams(params);
        return row;
    }

    private void updateColorButton(ImageView picker, int color) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setStroke(dp(2), Theme.strongBorder(this));
        background.setCornerRadius(dp(10));
        picker.setBackground(background);
        picker.setColorFilter(Theme.contrastingText(color));
    }

    private void showColorPicker(EditText target) {
        int initialColor;
        String current = target.getText().toString().trim();
        try {
            initialColor = current.matches("#[0-9a-fA-F]{6}")
                    ? Color.parseColor(current) : Theme.accent(this);
        } catch (IllegalArgumentException ignored) {
            initialColor = Theme.accent(this);
        }

        float[] initialHsv = new float[3];
        Color.colorToHSV(initialColor, initialHsv);
        int[] selectedColor = {initialColor};

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(18), dp(8), dp(18), dp(10));
        form.setBackgroundColor(Theme.surface(this));

        View preview = new View(this);
        preview.setBackground(colorPreviewBackground(initialColor));
        form.addView(preview, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(72)));

        TextView hex = text(Theme.Palette.colorHex(initialColor), 17, Theme.text(this));
        hex.setGravity(Gravity.CENTER);
        hex.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        hex.setPadding(0, dp(6), 0, dp(4));
        form.addView(hex);

        form.addView(fieldLabel(MainActivity.text("色相プリセット", "Hue presets")));
        LinearLayout presets = new LinearLayout(this);
        presets.setOrientation(LinearLayout.HORIZONTAL);
        int[] presetColors = {
                Color.rgb(239, 68, 68), Color.rgb(249, 115, 22), Color.rgb(234, 179, 8),
                Color.rgb(34, 197, 94), Color.rgb(20, 184, 166), Color.rgb(14, 165, 233),
                Color.rgb(37, 99, 235), Color.rgb(126, 34, 206), Color.rgb(219, 39, 119)
        };

        TextView hueValue = text("", 13, Theme.muted(this));
        TextView saturationValue = text("", 13, Theme.muted(this));
        TextView brightnessValue = text("", 13, Theme.muted(this));
        SeekBar hue = pickerSeekBar(359, Math.round(initialHsv[0]));
        SeekBar saturation = pickerSeekBar(100, Math.round(initialHsv[1] * 100));
        SeekBar brightness = pickerSeekBar(100, Math.round(initialHsv[2] * 100));

        Runnable update = () -> {
            float[] hsv = {hue.getProgress(), saturation.getProgress() / 100f,
                    brightness.getProgress() / 100f};
            int color = Color.HSVToColor(hsv);
            selectedColor[0] = color;
            preview.setBackground(colorPreviewBackground(color));
            hex.setText(Theme.Palette.colorHex(color));
            hueValue.setText(String.format(Locale.getDefault(),
                    MainActivity.text("色相: %d°", "Hue: %d°"), hue.getProgress()));
            saturationValue.setText(String.format(Locale.getDefault(),
                    MainActivity.text("彩度: %d%%", "Saturation: %d%%"), saturation.getProgress()));
            brightnessValue.setText(String.format(Locale.getDefault(),
                    MainActivity.text("明度: %d%%", "Brightness: %d%%"), brightness.getProgress()));
            ColorStateList tint = ColorStateList.valueOf(color);
            hue.setThumbTintList(tint);
            saturation.setProgressTintList(tint);
            saturation.setThumbTintList(tint);
            brightness.setProgressTintList(tint);
            brightness.setThumbTintList(tint);
        };

        for (int presetColor : presetColors) {
            View swatch = new View(this);
            swatch.setBackground(colorPreviewBackground(presetColor));
            swatch.setContentDescription(MainActivity.text("色相プリセット", "Hue preset"));
            swatch.setFocusable(true);
            swatch.setOnClickListener(v -> {
                float[] hsv = new float[3];
                Color.colorToHSV(presetColor, hsv);
                hue.setProgress(Math.round(hsv[0]));
                saturation.setProgress(Math.round(hsv[1] * 100));
                brightness.setProgress(Math.round(hsv[2] * 100));
                update.run();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(32), 1);
            params.setMargins(0, 0, dp(4), 0);
            presets.addView(swatch, params);
        }
        form.addView(presets, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(32)));

        form.addView(hueValue);
        form.addView(hue);
        form.addView(saturationValue);
        form.addView(saturation);
        form.addView(brightnessValue);
        form.addView(brightness);
        bindPickerSeekBar(hue, update);
        bindPickerSeekBar(saturation, update);
        bindPickerSeekBar(brightness, update);
        update.run();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("色を選択", "Choose color"))
                .setView(form)
                .setNegativeButton(MainActivity.text("キャンセル", "Cancel"), null)
                .setPositiveButton(MainActivity.text("適用", "Apply"), (d, which) ->
                        target.setText(Theme.Palette.colorHex(selectedColor[0])))
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private SeekBar pickerSeekBar(int max, int progress) {
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(max);
        seekBar.setProgress(progress);
        return seekBar;
    }

    private void bindPickerSeekBar(SeekBar seekBar, Runnable update) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                update.run();
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
            }
        });
    }

    private GradientDrawable colorPreviewBackground(int color) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setStroke(dp(1), Theme.strongBorder(this));
        background.setCornerRadius(dp(8));
        return background;
    }

    private EditText editField(String value) {
        EditText input = new EditText(this);
        input.setText(value);
        input.setTextColor(Theme.text(this));
        input.setHintTextColor(Theme.muted(this));
        input.setTextSize(15);
        input.setSingleLine(true);
        input.setPadding(dp(12), 0, dp(12), 0);
        input.setBackground(fieldBackground());
        return input;
    }

    private TextView sectionLabel(String value) {
        TextView view = text(value, 20, Theme.text(this));
        view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        view.setPadding(0, dp(14), 0, dp(6));
        return view;
    }

    private TextView fieldLabel(String value) {
        TextView view = text(value, 14, Theme.text(this));
        view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        view.setPadding(0, dp(7), 0, dp(2));
        return view;
    }

    private TextView actionButton(String value, View.OnClickListener listener) {
        TextView view = text(value, 15, Theme.contrastingText(Theme.accent(this)));
        view.setGravity(Gravity.CENTER);
        view.setOnClickListener(listener);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Theme.accent(this));
        background.setCornerRadius(dp(10));
        view.setBackground(background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        params.setMargins(0, dp(5), 0, dp(5));
        view.setLayoutParams(params);
        return view;
    }

    private TextView smallButton(String value, View.OnClickListener listener) {
        TextView view = text(value, 13, Theme.accent(this));
        view.setGravity(Gravity.CENTER);
        view.setOnClickListener(listener);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.TRANSPARENT);
        background.setStroke(dp(1), Theme.accent(this));
        background.setCornerRadius(dp(8));
        view.setBackground(background);
        return view;
    }

    private LinearLayout.LayoutParams actionParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(38), 1);
        params.setMargins(0, 0, dp(6), 0);
        return params;
    }

    private LinearLayout.LayoutParams fieldParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        params.setMargins(0, dp(2), 0, dp(7));
        return params;
    }

    private LinearLayout.LayoutParams selectorParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(68));
        params.setMargins(0, dp(2), 0, dp(9));
        return params;
    }

    private LinearLayout.LayoutParams dropdownRowParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(6));
        return params;
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private GradientDrawable fieldBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.field(this));
        drawable.setStroke(dp(1), Theme.border(this));
        drawable.setCornerRadius(dp(10));
        return drawable;
    }

    private GradientDrawable cardBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.surface(this));
        drawable.setStroke(dp(1), Theme.border(this));
        drawable.setCornerRadius(dp(12));
        return drawable;
    }

    private GradientDrawable dropdownBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.menu(this));
        drawable.setStroke(dp(2), Theme.strongBorder(this));
        drawable.setCornerRadius(dp(12));
        return drawable;
    }

    private GradientDrawable dropdownRowBackground(boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(selected ? Theme.active(this) : Theme.surface(this));
        drawable.setStroke(dp(selected ? 2 : 1),
                selected ? Theme.accent(this) : Theme.border(this));
        drawable.setCornerRadius(dp(9));
        return drawable;
    }

    private String safeFileName(String value) {
        String safe = value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return safe.isEmpty() ? "theme" : safe;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class Choice {
        final String id;
        final String label;
        final Theme.Palette palette;

        Choice(String id, String label, Theme.Palette palette) {
            this.id = id;
            this.label = label;
            this.palette = palette;
        }
    }
}
