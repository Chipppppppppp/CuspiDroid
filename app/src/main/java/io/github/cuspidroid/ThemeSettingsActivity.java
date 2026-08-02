package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
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
import java.util.Map;

/** Selects, edits, imports, and exports application color themes. */
public class ThemeSettingsActivity extends Activity {
    private static final int REQUEST_IMPORT = 4301;
    private static final int REQUEST_EXPORT = 4302;
    private static final int MAX_THEME_FILE_BYTES = 128 * 1024;

    private SharedPreferences preferences;
    private LinearLayout customThemeList;
    private LinearLayout normalSelector;
    private LinearLayout privateSelector;
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
                "通常時とプライベートブラウジング時のテーマを個別に選択できます。組み込みテーマを元に色を編集し、JSONファイルで共有できます。",
                "Choose themes independently for normal and private browsing. Customize colors from a built-in theme and share them as JSON files."),
                14, Theme.muted(this));
        intro.setPadding(0, dp(4), 0, dp(16));
        root.addView(intro);

        root.addView(sectionLabel(MainActivity.text("使用するテーマ", "Theme assignments")));
        root.addView(fieldLabel(MainActivity.text("通常のブラウジング", "Normal browsing")));
        normalSelector = themeSelector(true);
        root.addView(normalSelector, selectorParams());
        root.addView(fieldLabel(MainActivity.text("プライベートブラウジング", "Private browsing")));
        privateSelector = themeSelector(false);
        root.addView(privateSelector, selectorParams());

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
        renderSelector(normalSelector, selectedChoice(true));
        renderSelector(privateSelector, selectedChoice(false));
    }

    private List<Choice> choices(boolean includeSystem) {
        List<Choice> result = new ArrayList<>();
        if (includeSystem) {
            result.add(new Choice(Theme.MODE_SYSTEM, Theme.displayName(this, Theme.MODE_SYSTEM),
                    Theme.previewPalette(this, Theme.MODE_SYSTEM)));
        }
        for (Theme.Palette palette : Theme.selectablePalettes(this)) {
            result.add(new Choice(palette.id, Theme.displayName(this, palette.id), palette));
        }
        return result;
    }

    private Choice selectedChoice(boolean normal) {
        List<Choice> choices = choices(normal);
        String selectedId = normal ? Theme.normalSelection(this) : Theme.privateSelection(this);
        for (Choice choice : choices) {
            if (choice.id.equals(selectedId)) return choice;
        }
        return choices.get(0);
    }

    private LinearLayout themeSelector(boolean normal) {
        LinearLayout selector = new LinearLayout(this);
        selector.setOrientation(LinearLayout.HORIZONTAL);
        selector.setGravity(Gravity.CENTER_VERTICAL);
        selector.setPadding(dp(12), dp(8), dp(10), dp(8));
        selector.setBackground(fieldBackground());
        selector.setClickable(true);
        selector.setFocusable(true);
        selector.setContentDescription(normal
                ? MainActivity.text("通常のブラウジング用テーマを選択", "Choose normal browsing theme")
                : MainActivity.text("プライベートブラウジング用テーマを選択", "Choose private browsing theme"));
        selector.setOnClickListener(v -> showThemeDropdown(selector, normal));
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

    private void showThemeDropdown(View anchor, boolean normal) {
        List<Choice> choices = choices(normal);
        String selectedId = normal ? Theme.normalSelection(this) : Theme.privateSelection(this);

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
            list.addView(themeChoiceRow(choice, choice.id.equals(selectedId), popup, normal), dropdownRowParams());
        }
        int width = Math.max(anchor.getWidth(), dp(280));
        scroll.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(dp(420), View.MeasureSpec.AT_MOST));
        popup.setWidth(width);
        popup.setHeight(Math.min(scroll.getMeasuredHeight(), dp(420)));
        popup.showAsDropDown(anchor, 0, dp(4));
    }

    private View themeChoiceRow(Choice choice, boolean selected, PopupWindow popup, boolean normal) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(9), dp(12), dp(9));
        row.setBackground(dropdownRowBackground(selected));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> {
            String current = normal ? Theme.normalSelection(this) : Theme.privateSelection(this);
            if (choice.id.equals(current)) {
                popup.dismiss();
                return;
            }
            preferences.edit().putString(normal ? Theme.PREF_NORMAL_THEME : Theme.PREF_PRIVATE_THEME,
                    choice.id).apply();
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
            return MainActivity.text("端末設定に合わせてライト／ダークを自動切替",
                    "Automatically switches between Light and Dark");
        }
        if (choice.id.startsWith(Theme.CUSTOM_PREFIX)) {
            return MainActivity.text("カスタムテーマ", "Custom theme");
        }
        if (Theme.ID_BLUE.equals(choice.id)) {
            return MainActivity.text("組み込みサンプル・ライト系", "Built-in sample · Light");
        }
        return choice.palette.dark
                ? MainActivity.text("組み込みテーマ・ダーク系", "Built-in theme · Dark")
                : MainActivity.text("組み込みテーマ・ライト系", "Built-in theme · Light");
    }

    private LinearLayout swatchStrip(Theme.Palette palette, int height) {
        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        int[] colors = {palette.background, palette.surface, palette.text, palette.active, palette.accent};
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
        int[] colors = {palette.background, palette.surface, palette.text, palette.accent, palette.active};
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
        List<Theme.Palette> builtIns = new ArrayList<>();
        builtIns.add(Theme.paletteById(this, Theme.MODE_LIGHT));
        builtIns.add(Theme.paletteById(this, Theme.MODE_DARK));
        builtIns.add(Theme.paletteById(this, Theme.ID_PRIVATE));
        builtIns.add(Theme.paletteById(this, Theme.ID_BLUE));
        String[] labels = {
                Theme.displayName(this, Theme.MODE_LIGHT),
                Theme.displayName(this, Theme.MODE_DARK),
                Theme.displayName(this, Theme.ID_PRIVATE),
                Theme.displayName(this, Theme.ID_BLUE)
        };
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

        Map<String, EditText> inputs = new LinkedHashMap<>();
        for (String key : Theme.COLOR_KEYS) {
            form.addView(fieldLabel(colorLabel(key)));
            EditText input = editField(Theme.Palette.colorHex(palette.color(key)));
            inputs.put(key, input);
            form.addView(input, fieldParams());
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
            case "active": return MainActivity.text("選択・検索強調", "Selection and search highlight");
            case "linkHighlight": return MainActivity.text("リンク強調", "Link highlight");
            case "accent": return MainActivity.text("アクセント", "Accent");
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
