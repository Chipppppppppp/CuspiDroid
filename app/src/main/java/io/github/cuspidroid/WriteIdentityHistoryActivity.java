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
        setContentView(root);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(18), dp(72), dp(18), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.setPadding(dp(18), 0, dp(10), 0);
        titleRow.setBackground(topBarBackground());

        TextView title = new TextView(this);
        title.setText(MainActivity.text("\u540d\u524d\u30fb\u30e1\u30fc\u30eb\u5c65\u6b74", "Name/Mail History"));
        title.setTextColor(Theme.text(this));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        ImageButton clear = iconButton(R.drawable.ic_delete, MainActivity.text("\u5c65\u6b74\u3092\u5168\u524a\u9664", "Clear history"));
        clear.setOnClickListener(v -> confirmClearHistory());
        titleRow.addView(clear, new LinearLayout.LayoutParams(dp(46), dp(44)));
        root.addView(titleRow, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56), Gravity.TOP));
    }

    private void renderHistory() {
        list.removeAllViews();
        List<IdentityItem> items = readHistory();
        if (items.isEmpty()) {
            TextView empty = helperText(MainActivity.text("\u540d\u524d\u30fb\u30e1\u30fc\u30eb\u5c65\u6b74\u306a\u3057", "No name/mail history."));
            list.addView(empty);
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, dp(8));
            list.addView(historyRow(items.get(i), i), rowParams);
        }
    }

    private View historyRow(IdentityItem item, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(8), dp(8), dp(8));
        row.setBackground(rowBackground());

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView name = rowText(item.name.isEmpty() ? MainActivity.text("(\u540d\u524d\u306a\u3057)", "(No name)") : item.name, 16, true);
        TextView mail = rowText(item.mail.isEmpty() ? MainActivity.text("(\u30e1\u30fc\u30eb\u306a\u3057)", "(No mail)") : item.mail, 13, false);
        texts.addView(name);
        texts.addView(mail);
        row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        ImageButton delete = iconButton(R.drawable.ic_close,
                MainActivity.text("\u5c65\u6b74\u3092\u524a\u9664", "Delete history item"));
        delete.setOnClickListener(v -> confirmDelete(index));
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(dp(42), dp(40));
        deleteParams.setMargins(dp(8), 0, 0, 0);
        row.addView(delete, deleteParams);

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

        return row;
    }

    private void confirmDelete(int index) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("\u5c65\u6b74\u3092\u524a\u9664", "Delete history"))
                .setMessage(MainActivity.text("\u3053\u306e\u540d\u524d\u30fb\u30e1\u30fc\u30eb\u5c65\u6b74\u3092\u524a\u9664\u3057\u307e\u3059\u304b\uff1f", "Delete this name/mail history item?"))
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u524a\u9664", "Delete"), (d, which) -> {
                    deleteHistory(index);
                    Toast.makeText(this, MainActivity.text("\u5c65\u6b74\u3092\u524a\u9664", "History deleted."), Toast.LENGTH_SHORT).show();
                })
                .create();
        dialog.show();
        Theme.styleDialog(dialog, this);
    }

    private void confirmClearHistory() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("\u5c65\u6b74\u3092\u5168\u524a\u9664", "Clear history"))
                .setMessage(MainActivity.text("\u3059\u3079\u3066\u306e\u540d\u524d\u30fb\u30e1\u30fc\u30eb\u5c65\u6b74\u3092\u524a\u9664\u3057\u307e\u3059\u304b\uff1f", "Clear all name/mail history?"))
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u524a\u9664", "Delete"), (d, which) -> {
                    preferences.edit()
                            .remove(MainActivity.PREF_WRITE_IDENTITY_HISTORY)
                            .remove(MainActivity.PREF_WRITE_NAME_HISTORY)
                            .remove(MainActivity.PREF_WRITE_MAIL_HISTORY)
                            .apply();
                    renderHistory();
                    Toast.makeText(this, MainActivity.text("\u5c65\u6b74\u3092\u524a\u9664", "History cleared."), Toast.LENGTH_SHORT).show();
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
        view.setPadding(0, dp(4), 0, dp(4));
        return view;
    }

    private ImageButton iconButton(int icon, String description) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(icon);
        button.setContentDescription(description);
        button.setColorFilter(Theme.text(this));
        button.setBackground(iconButtonBackground());
        button.setPadding(dp(10), dp(10), dp(10), dp(10));
        button.setScaleType(ImageButton.ScaleType.CENTER);
        return button;
    }

    private GradientDrawable topBarBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.background(this));
        drawable.setStroke(dp(1), Theme.border(this));
        return drawable;
    }

    private GradientDrawable rowBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.surface(this));
        drawable.setStroke(dp(1), Theme.border(this));
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

    private static class IdentityItem {
        final String name;
        final String mail;

        IdentityItem(String name, String mail) {
            this.name = name;
            this.mail = mail;
        }
    }
}
