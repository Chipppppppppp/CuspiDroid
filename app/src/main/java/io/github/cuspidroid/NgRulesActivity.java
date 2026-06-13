package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NgRulesActivity extends Activity {
    private static final int TEXT = Color.rgb(31, 41, 55);
    private static final int MUTED = Color.rgb(79, 91, 103);
    private static final int SURFACE = Color.rgb(247, 248, 250);
    private static final int ACTIVE = Color.rgb(224, 242, 241);
    private static final int BORDER = Color.rgb(215, 221, 226);
    private static final String[] CATEGORIES = {"NGWord", "NGName", "NGID", "NGBe", "NGThread"};

    private SharedPreferences preferences;
    private final Map<String, List<NgRule>> rules = new LinkedHashMap<>();
    private final Map<String, Button> categoryButtons = new LinkedHashMap<>();
    private LinearLayout list;
    private String currentCategory = CATEGORIES[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        loadRules();
        buildLayout();
        renderRules();
    }

    private void buildLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        setContentView(root);

        TextView title = new TextView(this);
        title.setText(MainActivity.text("NG\u8a2d\u5b9a", "NG Rules"));
        title.setTextColor(TEXT);
        title.setTextSize(24);
        title.setPadding(dp(18), dp(18), dp(18), dp(10));
        root.addView(title);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(dp(12), 0, dp(12), dp(8));
        root.addView(tabs, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        for (String category : CATEGORIES) {
            Button button = new Button(this);
            button.setAllCaps(false);
            button.setText(category);
            button.setTextSize(12);
            button.setTextColor(TEXT);
            button.setOnClickListener(v -> {
                currentCategory = category;
                renderRules();
            });
            categoryButtons.put(category, button);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(42), 1);
            params.setMargins(dp(2), 0, dp(2), 0);
            tabs.addView(button, params);
        }

        Button add = new Button(this);
        add.setText(MainActivity.text("\u8ffd\u52a0", "Add"));
        add.setAllCaps(false);
        add.setOnClickListener(v -> showRuleDialog(null, -1));
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        addParams.setMargins(dp(18), 0, dp(18), dp(8));
        root.addView(add, addParams);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(18), 0, dp(18), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
        List<NgRule> items = rules.get(currentCategory);
        if (items == null || items.isEmpty()) {
            list.addView(helperText(MainActivity.text("\u30eb\u30fc\u30eb\u306a\u3057", "No rules.")));
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            NgRule rule = items.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(8), dp(8), dp(8));
            row.setBackground(rowBackground());

            TextView text = helperText((rule.regex
                    ? MainActivity.text("\u6b63\u898f\u8868\u73fe", "Regex")
                    : MainActivity.text("\u6587\u5b57\u5217", "Text")) + "\n" + rule.value);
            text.setTextColor(TEXT);
            row.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            int index = i;
            ImageButton edit = iconButton(R.drawable.ic_edit, MainActivity.text("\u7de8\u96c6", "Edit"));
            edit.setOnClickListener(v -> showRuleDialog(rule, index));
            row.addView(edit, iconParams());

            ImageButton delete = iconButton(R.drawable.ic_close, MainActivity.text("\u524a\u9664", "Delete"));
            delete.setOnClickListener(v -> {
                items.remove(index);
                saveRules();
                renderRules();
            });
            row.addView(delete, iconParams());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, dp(8));
            list.addView(row, params);
        }
    }

    private void showRuleDialog(NgRule existing, int index) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(4), dp(12), 0);

        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton textType = new RadioButton(this);
        textType.setText(MainActivity.text("\u6587\u5b57\u5217", "Text"));
        RadioButton regexType = new RadioButton(this);
        regexType.setText(MainActivity.text("\u6b63\u898f\u8868\u73fe", "Regex"));
        group.addView(textType);
        group.addView(regexType);
        content.addView(group);

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(15);
        input.setTextColor(TEXT);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setBackground(fieldBackground());
        input.setPadding(dp(12), 0, dp(12), 0);
        content.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));

        if (existing == null || !existing.regex) {
            textType.setChecked(true);
        } else {
            regexType.setChecked(true);
        }
        if (existing != null) {
            input.setText(existing.value);
            input.setSelection(input.length());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existing == null
                        ? MainActivity.text("\u30eb\u30fc\u30eb\u3092\u8ffd\u52a0", "Add rule")
                        : MainActivity.text("\u30eb\u30fc\u30eb\u3092\u7de8\u96c6", "Edit rule"))
                .setView(content)
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(existing == null
                        ? MainActivity.text("\u8ffd\u52a0", "Add")
                        : MainActivity.text("\u66f4\u65b0", "Update"), null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) {
                Toast.makeText(this, MainActivity.text("\u30eb\u30fc\u30eb\u3092\u5165\u529b", "Enter a rule."), Toast.LENGTH_SHORT).show();
                return;
            }
            NgRule rule = new NgRule(value, regexType.isChecked());
            List<NgRule> items = rules.get(currentCategory);
            if (items == null) {
                items = new ArrayList<>();
                rules.put(currentCategory, items);
            }
            if (index >= 0 && index < items.size()) {
                items.set(index, rule);
            } else {
                items.add(rule);
            }
            saveRules();
            renderRules();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void loadRules() {
        for (String category : CATEGORIES) {
            rules.put(category, new ArrayList<>());
        }
        try {
            JSONObject root = new JSONObject(preferences.getString(MainActivity.PREF_NG_RULES, "{}"));
            for (String category : CATEGORIES) {
                JSONObject item = root.optJSONObject(category);
                if (item == null) {
                    continue;
                }
                addSavedLines(category, item.optString("text", ""), false);
                addSavedLines(category, item.optString("regex", ""), true);
            }
        } catch (Exception ignored) {
        }
        addSavedLines("NGWord", preferences.getString(MainActivity.PREF_NG_WORDS, ""), false);
    }

    private void addSavedLines(String category, String saved, boolean regex) {
        List<NgRule> items = rules.get(category);
        if (items == null || saved == null) {
            return;
        }
        for (String line : saved.split("\\r?\\n")) {
            String value = line.trim();
            if (!value.isEmpty()) {
                items.add(new NgRule(value, regex));
            }
        }
    }

    private void saveRules() {
        JSONObject root = new JSONObject();
        try {
            for (String category : CATEGORIES) {
                StringBuilder text = new StringBuilder();
                StringBuilder regex = new StringBuilder();
                List<NgRule> items = rules.get(category);
                if (items != null) {
                    for (NgRule rule : items) {
                        StringBuilder target = rule.regex ? regex : text;
                        if (target.length() > 0) {
                            target.append('\n');
                        }
                        target.append(rule.value);
                    }
                }
                JSONObject item = new JSONObject();
                item.put("text", text.toString());
                item.put("regex", regex.toString());
                root.put(category, item);
            }
        } catch (Exception ignored) {
        }
        preferences.edit()
                .putString(MainActivity.PREF_NG_RULES, root.toString())
                .putString(MainActivity.PREF_NG_WORDS, "")
                .apply();
    }

    private TextView helperText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(MUTED);
        view.setTextSize(14);
        view.setPadding(0, dp(4), 0, dp(4));
        return view;
    }

    private ImageButton iconButton(int iconRes, String description) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(iconRes);
        button.setContentDescription(description);
        button.setColorFilter(TEXT);
        button.setBackground(iconButtonBackground());
        button.setPadding(dp(10), dp(10), dp(10), dp(10));
        button.setScaleType(ImageButton.ScaleType.CENTER);
        return button;
    }

    private LinearLayout.LayoutParams iconParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(42), dp(40));
        params.setMargins(dp(8), 0, 0, 0);
        return params;
    }

    private GradientDrawable rowBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(SURFACE);
        drawable.setStroke(dp(1), BORDER);
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private GradientDrawable tabBackground(boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(selected ? ACTIVE : SURFACE);
        drawable.setStroke(dp(1), selected ? Color.rgb(20, 184, 166) : BORDER);
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private GradientDrawable fieldBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(SURFACE);
        drawable.setStroke(dp(1), BORDER);
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private GradientDrawable iconButtonBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.TRANSPARENT);
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class NgRule {
        final String value;
        final boolean regex;

        NgRule(String value, boolean regex) {
            this.value = value;
            this.regex = regex;
        }
    }
}
