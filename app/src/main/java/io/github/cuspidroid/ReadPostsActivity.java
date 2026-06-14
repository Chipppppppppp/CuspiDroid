package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReadPostsActivity extends Activity {
    private static final int TEXT = Color.rgb(31, 41, 55);
    private static final int MUTED = Color.rgb(79, 91, 103);
    private static final int SURFACE = Color.rgb(247, 248, 250);
    private static final int BORDER = Color.rgb(215, 221, 226);

    private SharedPreferences preferences;
    private LinearLayout list;

    private int bgColor() {
        return Theme.background(this);
    }

    private int surfaceColor() {
        return Theme.surface(this);
    }

    private int textColor() {
        return Theme.text(this);
    }

    private int mutedColor() {
        return Theme.muted(this);
    }

    private int borderColor() {
        return Theme.border(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        buildLayout();
        renderReadPosts();
    }

    private void buildLayout() {
        Theme.applySystemBars(this);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(bgColor());
        setContentView(root);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(18), dp(72), dp(18), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(18), 0, dp(10), 0);
        topBar.setBackground(topBarBackground());
        TextView title = new TextView(this);
        title.setText(MainActivity.text("\u65e2\u8aad", "Read Positions"));
        title.setTextColor(textColor());
        title.setTextSize(22);
        topBar.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        ImageButton clear = iconButton(R.drawable.ic_delete, MainActivity.text("\u65e2\u8aad\u3092\u5168\u524a\u9664", "Clear read positions"));
        clear.setOnClickListener(v -> confirmClear());
        topBar.addView(clear, new LinearLayout.LayoutParams(dp(46), dp(44)));
        root.addView(topBar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56), Gravity.TOP));
    }

    private void renderReadPosts() {
        list.removeAllViews();
        List<ReadItem> items = readItems();
        if (items.isEmpty()) {
            list.addView(helperText(MainActivity.text("\u65e2\u8aad\u306a\u3057", "No read positions.")));
            return;
        }
        for (ReadItem item : items) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(8), dp(8), dp(8));
            row.setBackground(rowBackground());

            TextView text = helperText(item.title + "\n" + item.url + "\n"
                    + MainActivity.text("\u65e2\u8aad: ", "Read through: ") + item.number);
            text.setTextColor(textColor());
            row.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            ImageButton edit = iconButton(R.drawable.ic_edit, MainActivity.text("\u65e2\u8aad\u3092\u7de8\u96c6", "Edit read position"));
            edit.setOnClickListener(v -> showEditDialog(item));
            row.addView(edit, iconParams());

            ImageButton delete = iconButton(R.drawable.ic_close, MainActivity.text("\u65e2\u8aad\u3092\u524a\u9664", "Delete read position"));
            delete.setOnClickListener(v -> {
                removeReadPost(item.url);
                renderReadPosts();
            });
            row.addView(delete, iconParams());

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, dp(8));
            list.addView(row, rowParams);
        }
    }

    private void showEditDialog(ReadItem item) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(String.valueOf(item.number));
        input.setSelectAllOnFocus(true);
        input.setTextSize(16);
        input.setTextColor(textColor());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setBackground(rowBackground());
        input.setPadding(dp(12), 0, dp(12), 0);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("\u65e2\u8aad\u756a\u53f7\u3092\u7de8\u96c6", "Edit read number"))
                .setView(input)
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u66f4\u65b0", "Update"), null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                saveReadPost(item.url, Math.max(0, Integer.parseInt(input.getText().toString().trim())));
                renderReadPosts();
                dialog.dismiss();
            } catch (Exception ignored) {
                Toast.makeText(this, MainActivity.text("\u6570\u5024\u3092\u5165\u529b", "Enter a number."), Toast.LENGTH_SHORT).show();
            }
        }));
        dialog.show();
    }

    private void confirmClear() {
        new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("\u65e2\u8aad\u3092\u5168\u524a\u9664", "Clear read positions"))
                .setMessage(MainActivity.text("\u3059\u3079\u3066\u306e\u65e2\u8aad\u60c5\u5831\u3092\u524a\u9664\u3057\u307e\u3059\u304b\uff1f", "Clear all read positions?"))
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u524a\u9664", "Delete"), (dialog, which) -> {
                    preferences.edit().remove(MainActivity.PREF_READ_POSTS).apply();
                    renderReadPosts();
                    Toast.makeText(this, MainActivity.text("\u65e2\u8aad\u3092\u524a\u9664", "Read positions cleared."), Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private List<ReadItem> readItems() {
        Map<String, String> titles = new LinkedHashMap<>();
        for (MainActivity.ThreadHistoryItem item : MainActivity.readThreadHistory(preferences)) {
            titles.put(item.url, item.title);
        }
        Map<String, Integer> reads = new LinkedHashMap<>();
        try {
            JSONObject object = new JSONObject(preferences.getString(MainActivity.PREF_READ_POSTS, "{}"));
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String url = keys.next();
                reads.put(url, object.optInt(url, 0));
            }
        } catch (Exception ignored) {
        }

        List<ReadItem> items = new ArrayList<>();
        for (String url : titles.keySet()) {
            Integer number = reads.remove(url);
            if (number != null) {
                items.add(new ReadItem(titles.get(url), url, number));
            }
        }
        for (Map.Entry<String, Integer> entry : reads.entrySet()) {
            items.add(new ReadItem(entry.getKey(), entry.getKey(), entry.getValue()));
        }
        return items;
    }

    private void saveReadPost(String url, int number) {
        try {
            JSONObject object = new JSONObject(preferences.getString(MainActivity.PREF_READ_POSTS, "{}"));
            object.put(url, number);
            preferences.edit().putString(MainActivity.PREF_READ_POSTS, object.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private void removeReadPost(String url) {
        try {
            JSONObject object = new JSONObject(preferences.getString(MainActivity.PREF_READ_POSTS, "{}"));
            object.remove(url);
            preferences.edit().putString(MainActivity.PREF_READ_POSTS, object.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private TextView helperText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(mutedColor());
        view.setTextSize(14);
        view.setPadding(0, dp(4), 0, dp(4));
        return view;
    }

    private ImageButton iconButton(int iconRes, String description) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(iconRes);
        button.setContentDescription(description);
        button.setColorFilter(textColor());
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

    private GradientDrawable topBarBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(bgColor());
        drawable.setStroke(dp(1), borderColor());
        return drawable;
    }

    private GradientDrawable rowBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(surfaceColor());
        drawable.setStroke(dp(1), borderColor());
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

    private static class ReadItem {
        final String title;
        final String url;
        final int number;

        ReadItem(String title, String url, int number) {
            this.title = title == null || title.isEmpty() ? url : title;
            this.url = url;
            this.number = number;
        }
    }
}
