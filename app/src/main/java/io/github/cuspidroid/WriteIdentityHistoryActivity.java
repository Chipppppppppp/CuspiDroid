package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WriteIdentityHistoryActivity extends Activity {
    static final String EXTRA_PICK_MODE = "pick_mode";
    static final String EXTRA_NAME = "name";
    static final String EXTRA_MAIL = "mail";

    private SharedPreferences preferences;
    private LinearLayout list;
    private boolean pickMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        pickMode = getIntent().getBooleanExtra(EXTRA_PICK_MODE, false);
        buildLayout();
        renderHistory();
    }

    private void buildLayout() {
        Theme.applySystemBars(this);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Theme.background(this));

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        root.addView(column, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.setPadding(dp(10), dp(10), dp(10), dp(6));
        column.addView(titleRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageButton back = iconButton(android.R.drawable.ic_menu_revert, MainActivity.text("戻る", "Back"));
        back.setOnClickListener(v -> finish());
        titleRow.addView(back, new LinearLayout.LayoutParams(dp(42), dp(42)));

        TextView title = new TextView(this);
        title.setText(MainActivity.text("名前・メール履歴", "Name/Mail History"));
        title.setTextColor(Theme.text(this));
        title.setTextSize(20);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        titleRow.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        ImageButton clear = iconButton(android.R.drawable.ic_menu_delete, MainActivity.text("履歴を全削除", "Clear history"));
        clear.setOnClickListener(v -> confirmClearHistory());
        titleRow.addView(clear, new LinearLayout.LayoutParams(dp(42), dp(42)));

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(6), dp(12), dp(14));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        column.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        setContentView(root);
    }

    private void renderHistory() {
        list.removeAllViews();
        List<IdentityItem> items = readHistory();
        if (items.isEmpty()) {
            TextView empty = helperText(MainActivity.text("名前・メール履歴なし", "No name/mail history."));
            list.addView(empty, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            list.addView(historyRow(items.get(i), i), new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private View historyRow(IdentityItem item, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(9), dp(8), dp(9));
        row.setBackground(rowBackground());

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView name = rowText(item.name.isEmpty() ? MainActivity.text("(名前なし)", "(No name)") : item.name, 16, true);
        TextView mail = rowText(item.mail.isEmpty() ? MainActivity.text("(メールなし)", "(No mail)") : item.mail, 13, false);
        texts.addView(name);
        texts.addView(mail);
        row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        ImageButton delete = iconButton(android.R.drawable.ic_menu_close_clear_cancel,
                MainActivity.text("履歴を削除", "Delete history item"));
        delete.setOnClickListener(v -> confirmDelete(index));
        row.addView(delete, new LinearLayout.LayoutParams(dp(40), dp(40)));

        row.setOnClickListener(v -> {
            if (!pickMode) {
                return;
            }
            Intent data = new Intent();
            data.putExtra(EXTRA_NAME, item.name);
            data.putExtra(EXTRA_MAIL, item.mail);
            setResult(RESULT_OK, data);
            finish();
        });

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams space = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(8));
        TextView spacer = new TextView(this);
        wrapper.addView(spacer, space);
        return wrapper;
    }

    private void confirmDelete(int index) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("履歴を削除", "Delete history"))
                .setMessage(MainActivity.text("この名前・メール履歴を削除しますか？", "Delete this name/mail history item?"))
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", (d, which) -> {
                    deleteHistory(index);
                    Toast.makeText(this, MainActivity.text("履歴を削除", "History deleted."), Toast.LENGTH_SHORT).show();
                })
                .create();
        dialog.show();
        Theme.styleDialog(dialog, this);
    }

    private void confirmClearHistory() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("履歴を全削除", "Clear history"))
                .setMessage(MainActivity.text("すべての名前・メール履歴を削除しますか？", "Clear all name/mail history?"))
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", (d, which) -> {
                    preferences.edit()
                            .remove(MainActivity.PREF_WRITE_IDENTITY_HISTORY)
                            .remove(MainActivity.PREF_WRITE_NAME_HISTORY)
                            .remove(MainActivity.PREF_WRITE_MAIL_HISTORY)
                            .apply();
                    renderHistory();
                    Toast.makeText(this, MainActivity.text("履歴を削除", "History cleared."), Toast.LENGTH_SHORT).show();
                })
                .create();
        dialog.show();
        Theme.styleDialog(dialog, this);
    }

    private void deleteHistory(int index) {
        List<IdentityItem> items = readHistory();
        if (index < 0 || index >= items.size()) {
            return;
        }
        items.remove(index);
        JSONArray array = new JSONArray();
        try {
            for (IdentityItem item : items) {
                JSONObject object = new JSONObject();
                object.put("name", item.name);
                object.put("mail", item.mail);
                array.put(object);
            }
        } catch (Exception ignored) {
        }
        preferences.edit()
                .putString(MainActivity.PREF_WRITE_IDENTITY_HISTORY, array.toString())
                .remove(MainActivity.PREF_WRITE_NAME_HISTORY)
                .remove(MainActivity.PREF_WRITE_MAIL_HISTORY)
                .apply();
        renderHistory();
    }

    private List<IdentityItem> readHistory() {
        List<IdentityItem> items = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString(MainActivity.PREF_WRITE_IDENTITY_HISTORY, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                addUnique(items, object.optString("name", ""), object.optString("mail", ""));
            }
        } catch (Exception ignored) {
        }
        if (items.isEmpty()) {
            List<String> names = readLegacy(MainActivity.PREF_WRITE_NAME_HISTORY);
            List<String> mails = readLegacy(MainActivity.PREF_WRITE_MAIL_HISTORY);
            int count = Math.max(names.size(), mails.size());
            for (int i = 0; i < count; i++) {
                addUnique(items, i < names.size() ? names.get(i) : "", i < mails.size() ? mails.get(i) : "");
            }
        }
        return items;
    }

    private List<String> readLegacy(String key) {
        List<String> values = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString(key, "[]"));
            for (int i = 0; i < array.length(); i++) {
                String value = array.optString(i, "").trim();
                if (!value.isEmpty() && !values.contains(value)) {
                    values.add(value);
                }
            }
        } catch (Exception ignored) {
        }
        return values;
    }

    private void addUnique(List<IdentityItem> items, String name, String mail) {
        name = name == null ? "" : name.trim();
        mail = mail == null ? "" : mail.trim();
        if (name.isEmpty() && mail.isEmpty()) {
            return;
        }
        for (IdentityItem item : items) {
            if (item.name.equals(name) && item.mail.equals(mail)) {
                return;
            }
        }
        items.add(new IdentityItem(name, mail));
    }

    private TextView rowText(String value, int sp, boolean strong) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(strong ? Theme.text(this) : Theme.muted(this));
        view.setTextSize(sp);
        view.setSingleLine(true);
        view.setEllipsize(TextUtils.TruncateAt.END);
        if (strong) {
            view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }
        return view;
    }

    private TextView helperText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(Theme.muted(this));
        view.setTextSize(14);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(10), dp(32), dp(10), dp(32));
        return view;
    }

    private ImageButton iconButton(int icon, String description) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(icon);
        button.setContentDescription(description);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setColorFilter(Theme.muted(this));
        button.setPadding(dp(9), dp(9), dp(9), dp(9));
        return button;
    }

    private GradientDrawable rowBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.surface(this));
        drawable.setStroke(dp(1), Theme.border(this));
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class IdentityItem {
        final String name;
        final String mail;

        IdentityItem(String name, String mail) {
            this.name = name;
            this.mail = mail;
        }
    }
}
