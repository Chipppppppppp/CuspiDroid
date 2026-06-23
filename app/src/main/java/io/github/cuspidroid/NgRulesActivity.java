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
import android.widget.HorizontalScrollView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        allRules.addAll(MainActivity.readNgRules(preferences));
        Intent intent = getIntent();
        if (intent != null) {
            targetUrl = MainActivity.normalizeNgTargetUrl(intent.getStringExtra(MainActivity.EXTRA_NG_TARGET_URL));
            targetTitle = intent.getStringExtra(MainActivity.EXTRA_NG_TARGET_TITLE);
        }
        if (targetTitle == null) {
            targetTitle = "";
        }
        loadPageRules();
        buildLayout();
        renderRules();
    }

    @Override
    protected void onPause() {
        saveRules();
        super.onPause();
    }

    private void loadPageRules() {
        pageRules.clear();
        for (MainActivity.ScopedNgRule rule : allRules) {
            if (rule.targetUrl.equals(targetUrl)) {
                pageRules.add(rule);
            }
        }
    }

    private void buildLayout() {
        Theme.applySystemBars(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(16), dp(14), 0);
        root.setBackgroundColor(Theme.background(this));
        setContentView(root);

        root.addView(scopeSelector());

        root.addView(textView(targetUrl.isEmpty()
                ? MainActivity.text("\u3059\u3079\u3066\u306e\u30b9\u30ec\u306eNG\u7ba1\u7406", "NG rules for all threads")
                : MainActivity.text("\u30b9\u30ec\u3054\u3068\u306eNG\u7ba1\u7406", "Thread NG rules"), 24, Theme.text(this)));

        if (!targetUrl.isEmpty()) {
            TextView threadName = textView(targetTitle.trim().isEmpty()
                    ? MainActivity.text("\u30b9\u30ec", "Thread") : targetTitle.trim(), 16, Theme.text(this));
            threadName.setMaxLines(2);
            threadName.setPadding(dp(4), dp(10), dp(4), dp(4));
            root.addView(threadName);
            targetInput = field(MainActivity.text("\u30b9\u30ecURL", "Thread URL"));
            targetInput.setText(targetUrl);
            root.addView(targetInput, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));
        } else {
            TextView description = textView(
                    MainActivity.text("\u3053\u3053\u306eNG\u30eb\u30fc\u30eb\u306f\u3059\u3079\u3066\u306e\u30b9\u30ec\u306b\u9069\u7528\u3055\u308c\u307e\u3059",
                            "Rules on this page apply to every thread."), 13, Theme.muted(this));
            description.setPadding(dp(4), dp(8), dp(4), dp(8));
            root.addView(description);
        }

        LinearLayout tabs = new LinearLayout(this);
        categoryButtons.clear();
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
                String value = input.getText().toString().trim();
                if (value.isEmpty()) {
                    Toast.makeText(this, MainActivity.text("\u30eb\u30fc\u30eb\u3092\u5165\u529b", "Enter a rule."), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (regexType.isChecked()) {
                    try {
                        Pattern.compile(value);
                    } catch (Exception error) {
                        Toast.makeText(this, MainActivity.text("\u6b63\u898f\u8868\u73fe\u304c\u4e0d\u6b63\u3067\u3059", "Invalid regex."), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                MainActivity.ScopedNgRule rule = new MainActivity.ScopedNgRule(
                        currentCategory, value, regexType.isChecked(), currentTargetUrl(), targetTitle);
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
            if (!rule.targetUrl.equals(targetUrl)) {
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

    private HorizontalScrollView scopeSelector() {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, dp(10));
        Map<String, String> targets = new LinkedHashMap<>();
        targets.put("", MainActivity.text("\u3059\u3079\u3066\u306e\u30b9\u30ec", "All threads"));
        for (MainActivity.ScopedNgRule rule : allRules) {
            if (!rule.targetUrl.isEmpty() && !targets.containsKey(rule.targetUrl)) {
                targets.put(rule.targetUrl, scopeLabel(rule.targetTitle, rule.targetUrl));
            }
        }
        if (!targetUrl.isEmpty()) {
            targets.put(targetUrl, scopeLabel(targetTitle, targetUrl));
        }
        for (Map.Entry<String, String> entry : targets.entrySet()) {
            String url = entry.getKey();
            String label = entry.getValue();
            String title = scopeTitleForUrl(url);
            Button button = new Button(this);
            button.setAllCaps(false);
            button.setText(label);
            button.setTextSize(13);
            button.setTextColor(Theme.text(this));
            button.setBackground(tabBackground(url.equals(targetUrl)));
            button.setOnClickListener(v -> switchScope(url, title));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, dp(42));
            params.setMargins(0, 0, dp(6), 0);
            row.addView(button, params);
        }
        scroll.addView(row);
        return scroll;
    }

    private String scopeTitleForUrl(String url) {
        if (url.isEmpty()) {
            return "";
        }
        if (url.equals(targetUrl) && !targetTitle.trim().isEmpty()) {
            return targetTitle.trim();
        }
        for (MainActivity.ScopedNgRule rule : allRules) {
            if (rule.targetUrl.equals(url) && !rule.targetTitle.trim().isEmpty()) {
                return rule.targetTitle.trim();
            }
        }
        return scopeLabel("", url);
    }

    private void switchScope(String url, String title) {
        if (url.equals(targetUrl)) {
            return;
        }
        saveRules();
        targetUrl = url;
        targetTitle = url.isEmpty() ? "" : title;
        targetInput = null;
        loadPageRules();
        buildLayout();
        renderRules();
    }

    private String scopeLabel(String title, String url) {
        if (title != null && !title.trim().isEmpty()) {
            String value = title.trim();
            return value.length() > 24 ? value.substring(0, 23) + "..." : value;
        }
        String value = url == null ? "" : url.replaceFirst("https?://", "");
        return value.length() > 24 ? value.substring(0, 23) + "..." : value;
    }

    private LinearLayout ruleRow(String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(8), dp(8), dp(8));
        row.setBackground(boxBackground());
        row.addView(textView(value, 14, Theme.text(this)),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return row;
    }

    private TextView actionRow(String value) {
        TextView view = textView("+  " + value, 16, Theme.text(this));
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

    private TextView textView(String value, float size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
