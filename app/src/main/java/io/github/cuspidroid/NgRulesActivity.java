package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class NgRulesActivity extends Activity {
    private static final String[] CATEGORIES = {"NGWord", "NGName", "NGID", "NGBe", "NGThread"};
    private SharedPreferences preferences;
    private final List<MainActivity.ScopedNgRule> allRules = new ArrayList<>();
    private final List<MainActivity.ScopedNgRule> pageRules = new ArrayList<>();
    private final Map<String, Button> categoryButtons = new LinkedHashMap<>();
    private LinearLayout list;
    private EditText targetInput;
    private String targetUrl = "";
    private String targetTitle = "";
    private String currentCategory = CATEGORIES[0];
    private String presetValue = "";
    private boolean targetSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        allRules.addAll(MainActivity.readNgRules(preferences));
        Intent intent = getIntent();
        targetSelected = intent != null && intent.hasExtra(MainActivity.EXTRA_NG_TARGET_URL);
        if (targetSelected) {
            targetUrl = MainActivity.normalizeNgTargetUrl(intent.getStringExtra(MainActivity.EXTRA_NG_TARGET_URL));
            targetTitle = value(intent.getStringExtra(MainActivity.EXTRA_NG_TARGET_TITLE));
            currentCategory = validCategory(value(intent.getStringExtra(MainActivity.EXTRA_NG_PRESET_CATEGORY)));
            presetValue = value(intent.getStringExtra(MainActivity.EXTRA_NG_PRESET_VALUE)).trim();
            loadPageRules();
        }
        buildLayout();
        if (targetSelected) {
            renderRules();
            if (!presetValue.isEmpty()) {
                list.post(() -> showRuleDialog(null, -1, presetValue));
            }
        }
    }

    @Override
    protected void onPause() {
        if (targetSelected) {
            saveRules();
        }
        super.onPause();
    }

    private void buildLayout() {
        Theme.applySystemBars(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(16), dp(14), 0);
        root.setBackgroundColor(Theme.background(this));
        setContentView(root);
        if (!targetSelected) {
            buildTargetList(root);
            return;
        }
        buildEditor(root);
    }

    private void buildTargetList(LinearLayout root) {
        TextView title = textView(MainActivity.text("NG\u7ba1\u7406", "NG management"), 24, Theme.text(this));
        title.setPadding(dp(4), 0, dp(4), dp(14));
        root.addView(title);
        ScrollView scroll = new ScrollView(this);
        LinearLayout targets = new LinearLayout(this);
        targets.setOrientation(LinearLayout.VERTICAL);
        addTargetRow(targets, "", MainActivity.text("\u5168\u677f\u5171\u901a", "All boards"));
        List<String> seen = new ArrayList<>();
        for (MainActivity.ScopedNgRule rule : allRules) {
            if (rule.targetUrl.isEmpty() || seen.contains(rule.targetUrl)) {
                continue;
            }
            seen.add(rule.targetUrl);
            addTargetRow(targets, rule.targetUrl, scopeLabel(rule.targetTitle, rule.targetUrl));
        }
        addNewTargetRow(targets);
        scroll.addView(targets);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private void addTargetRow(LinearLayout parent, String url, String title) {
        LinearLayout row = ruleRow(title + (url.isEmpty() ? "" : "\n" + url));
        row.setOnClickListener(v -> {
            Intent intent = new Intent(this, NgRulesActivity.class);
            intent.putExtra(MainActivity.EXTRA_NG_TARGET_URL, url);
            intent.putExtra(MainActivity.EXTRA_NG_TARGET_TITLE, title);
            startActivity(intent);
        });
        parent.addView(row, targetRowParams());
    }

    private void addNewTargetRow(LinearLayout parent) {
        TextView add = actionRow(MainActivity.text("\u677fURL\u3092\u8ffd\u52a0", "Add board URL"));
        add.setOnClickListener(v -> showAddTargetDialog());
        parent.addView(add, targetRowParams());
    }

    private void showAddTargetDialog() {
        EditText input = field(MainActivity.text("\u677fURL", "Board URL"));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("\u5bfe\u8c61\u677f\u3092\u8ffd\u52a0", "Add target board"))
                .setView(input)
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u8ffd\u52a0", "Add"), null)
                .create();
        dialog.setOnShowListener(d -> {
            Theme.styleDialog(dialog, this);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String url = MainActivity.normalizeNgTargetUrl(input.getText().toString());
                if (url.isEmpty()) {
                    Toast.makeText(this, MainActivity.text("\u677fURL\u3092\u5165\u529b", "Enter a board URL."), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(this, NgRulesActivity.class);
                intent.putExtra(MainActivity.EXTRA_NG_TARGET_URL, url);
                intent.putExtra(MainActivity.EXTRA_NG_TARGET_TITLE, "");
                startActivity(intent);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void buildEditor(LinearLayout root) {
        root.addView(textView(MainActivity.text("NG\u7ba1\u7406", "NG management"), 24, Theme.text(this)));
        if (!targetUrl.isEmpty()) {
            TextView name = textView(targetTitle.isEmpty()
                    ? MainActivity.text("\u677f", "Board") : targetTitle, 16, Theme.text(this));
            name.setMaxLines(2);
            name.setPadding(dp(4), dp(10), dp(4), dp(4));
            root.addView(name);
            targetInput = field(MainActivity.text("\u677fURL", "Board URL"));
            targetInput.setText(targetUrl);
            root.addView(targetInput, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));
        } else {
            TextView description = textView(
                    MainActivity.text("\u5168\u677f\u306b\u9069\u7528\u3059\u308bNG\u7ba1\u7406", "NG management applied to all boards"),
                    13, Theme.muted(this));
            description.setPadding(dp(4), dp(8), dp(4), dp(8));
            root.addView(description);
        }
        categoryButtons.clear();
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(0, dp(8), 0, dp(6));
        for (String category : CATEGORIES) {
            Button button = new Button(this);
            button.setAllCaps(false);
            button.setText(category);
            button.setTextSize(11);
            button.setTextColor(Theme.text(this));
            button.setOnClickListener(v -> {
                currentCategory = category;
                renderRules();
            });
            categoryButtons.put(category, button);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(42), 1);
            params.setMargins(dp(2), 0, dp(2), 0);
            tabs.addView(button, params);
        }
        root.addView(tabs);
        TextView add = actionRow(MainActivity.text("NG\u30eb\u30fc\u30eb\u3092\u8ffd\u52a0", "Add NG rule"));
        add.setOnClickListener(v -> showRuleDialog(null, -1));
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        addParams.setMargins(dp(4), 0, dp(4), dp(8));
        root.addView(add, addParams);
        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(4), 0, dp(4), dp(24));
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private void loadPageRules() {
        pageRules.clear();
        for (MainActivity.ScopedNgRule rule : allRules) {
            if (MainActivity.normalizeNgTargetUrl(rule.targetUrl).equals(targetUrl)) {
                pageRules.add(rule);
            }
        }
    }

    private void renderRules() {
        for (Map.Entry<String, Button> entry : categoryButtons.entrySet()) {
            boolean selected = entry.getKey().equals(currentCategory);
            entry.getValue().setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
            entry.getValue().setBackground(tabBackground(selected));
        }
        list.removeAllViews();
        List<Integer> indexes = categoryIndexes();
        if (indexes.isEmpty()) {
            list.addView(textView(MainActivity.text("\u30eb\u30fc\u30eb\u306a\u3057", "No rules."), 14, Theme.muted(this)));
            return;
        }
        for (int pageIndex : indexes) {
            MainActivity.ScopedNgRule rule = pageRules.get(pageIndex);
            LinearLayout row = ruleRow((rule.regex
                    ? MainActivity.text("\u6b63\u898f\u8868\u73fe", "Regex")
                    : MainActivity.text("\u6587\u5b57\u5217", "Text")) + "\n" + rule.value);
            ImageButton edit = iconButton(R.drawable.ic_edit, MainActivity.text("\u7de8\u96c6", "Edit"));
            edit.setOnClickListener(v -> showRuleDialog(rule, pageIndex));
            row.addView(edit, iconParams());
            ImageButton delete = iconButton(R.drawable.ic_close, MainActivity.text("\u524a\u9664", "Delete"));
            delete.setOnClickListener(v -> {
                pageRules.remove(pageIndex);
                saveRules();
                renderRules();
            });
            row.addView(delete, iconParams());
            list.addView(row, rowParams());
        }
    }

    private List<Integer> categoryIndexes() {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < pageRules.size(); i++) {
            if (currentCategory.equals(pageRules.get(i).category)) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    private void showRuleDialog(MainActivity.ScopedNgRule existing, int pageIndex) {
        showRuleDialog(existing, pageIndex, null);
    }

    private void showRuleDialog(MainActivity.ScopedNgRule existing, int pageIndex, String preset) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(4), dp(12), 0);
        content.setBackgroundColor(Theme.surface(this));
        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton textType = radio(MainActivity.text("\u6587\u5b57\u5217", "Text"));
        RadioButton regexType = radio(MainActivity.text("\u6b63\u898f\u8868\u73fe", "Regex"));
        group.addView(textType);
        group.addView(regexType);
        content.addView(group);
        EditText input = field(MainActivity.text("NG\u30eb\u30fc\u30eb", "NG rule"));
        content.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));
        (existing != null && existing.regex ? regexType : textType).setChecked(true);
        if (existing != null) {
            input.setText(existing.value);
            input.setSelection(input.length());
        } else if (preset != null && !preset.trim().isEmpty()) {
            input.setText(preset.trim());
            input.setSelection(input.length());
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existing == null
                        ? MainActivity.text("NG\u30eb\u30fc\u30eb\u3092\u8ffd\u52a0", "Add NG rule")
                        : MainActivity.text("NG\u30eb\u30fc\u30eb\u3092\u7de8\u96c6", "Edit NG rule"))
                .setView(content)
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(existing == null
                        ? MainActivity.text("\u8ffd\u52a0", "Add")
                        : MainActivity.text("\u66f4\u65b0", "Update"), null)
                .create();
        dialog.setOnShowListener(d -> {
            Theme.styleDialog(dialog, this);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String word = input.getText().toString().trim();
                if (word.isEmpty()) {
                    Toast.makeText(this, MainActivity.text("\u30eb\u30fc\u30eb\u3092\u5165\u529b", "Enter a rule."), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (regexType.isChecked()) {
                    try {
                        Pattern.compile(word);
                    } catch (Exception error) {
                        Toast.makeText(this, MainActivity.text("\u6b63\u898f\u8868\u73fe\u304c\u4e0d\u6b63\u3067\u3059", "Invalid regex."), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                MainActivity.ScopedNgRule rule = new MainActivity.ScopedNgRule(
                        currentCategory, word, regexType.isChecked(), currentTargetUrl(), targetTitle);
                if (pageIndex >= 0 && pageIndex < pageRules.size()) {
                    pageRules.set(pageIndex, rule);
                } else {
                    pageRules.add(rule);
                }
                saveRules();
                renderRules();
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private String currentTargetUrl() {
        return targetInput == null ? "" : MainActivity.normalizeNgTargetUrl(targetInput.getText().toString());
    }

    private void saveRules() {
        List<MainActivity.ScopedNgRule> saved = new ArrayList<>();
        for (MainActivity.ScopedNgRule rule : allRules) {
            if (!MainActivity.normalizeNgTargetUrl(rule.targetUrl).equals(targetUrl)) {
                saved.add(rule);
            }
        }
        String nextTarget = currentTargetUrl();
        for (MainActivity.ScopedNgRule rule : pageRules) {
            saved.add(new MainActivity.ScopedNgRule(
                    rule.category, rule.value, rule.regex, nextTarget, targetTitle));
        }
        targetUrl = nextTarget;
        allRules.clear();
        allRules.addAll(saved);
        MainActivity.saveNgRules(preferences, saved);
    }

    private String scopeLabel(String title, String url) {
        if (title != null && !title.trim().isEmpty()) {
            String label = title.trim();
            return label.length() > 60 ? label.substring(0, 59) + "..." : label;
        }
        String label = value(url).replaceFirst("https?://", "");
        return label.length() > 60 ? label.substring(0, 59) + "..." : label;
    }

    private String validCategory(String value) {
        for (String category : CATEGORIES) {
            if (category.equals(value)) {
                return category;
            }
        }
        return CATEGORIES[0];
    }

    private LinearLayout ruleRow(String label) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(9), dp(8), dp(9));
        row.setBackground(boxBackground());
        row.addView(textView(label, 14, Theme.text(this)),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return row;
    }

    private TextView actionRow(String label) {
        TextView view = textView("+  " + label, 16, Theme.text(this));
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(dp(12), 0, dp(12), 0);
        view.setBackground(boxBackground());
        return view;
    }

    private EditText field(String hint) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextColor(Theme.text(this));
        input.setHintTextColor(Theme.muted(this));
        input.setHint(hint);
        input.setTextSize(14);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setPadding(dp(12), 0, dp(12), 0);
        input.setBackground(boxBackground());
        return input;
    }

    private RadioButton radio(String label) {
        RadioButton button = new RadioButton(this);
        button.setText(label);
        button.setTextColor(Theme.text(this));
        Theme.tintCompoundButton(this, button);
        return button;
    }

    private TextView textView(String label, float size, int color) {
        TextView view = new TextView(this);
        view.setText(label);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private ImageButton iconButton(int icon, String description) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(icon);
        button.setContentDescription(description);
        button.setColorFilter(Theme.text(this));
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setPadding(dp(10), dp(10), dp(10), dp(10));
        return button;
    }

    private LinearLayout.LayoutParams iconParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(42), dp(40));
        params.setMargins(dp(6), 0, 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams rowParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(8));
        return params;
    }

    private LinearLayout.LayoutParams targetRowParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(68));
        params.setMargins(0, 0, 0, dp(8));
        return params;
    }

    private GradientDrawable boxBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.surface(this));
        drawable.setStroke(dp(1), Theme.border(this));
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private GradientDrawable tabBackground(boolean selected) {
        GradientDrawable drawable = boxBackground();
        drawable.setColor(selected ? Theme.active(this) : Theme.surface(this));
        return drawable;
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
