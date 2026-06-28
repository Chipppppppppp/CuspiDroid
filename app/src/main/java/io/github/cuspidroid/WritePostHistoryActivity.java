package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class WritePostHistoryActivity extends Activity {
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
        renderHistory();
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
        topBar.setPadding(dp(10), 0, dp(10), 0);
        topBar.setBackground(topBarBackground());

        ImageButton back = iconButton(R.drawable.ic_arrow_back, MainActivity.text("\u623b\u308b", "Back"));
        back.setOnClickListener(v -> finish());
        topBar.addView(back, new LinearLayout.LayoutParams(dp(44), dp(44)));

        TextView title = new TextView(this);
        title.setText(MainActivity.text("\u66f8\u304d\u8fbc\u307f\u5c65\u6b74", "Post History"));
        title.setTextColor(textColor());
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        titleParams.setMargins(dp(8), 0, 0, 0);
        topBar.addView(title, titleParams);

        ImageButton clear = iconButton(R.drawable.ic_delete, MainActivity.text("\u66f8\u304d\u8fbc\u307f\u5c65\u6b74\u3092\u5168\u524a\u9664", "Clear post history"));
        clear.setOnClickListener(v -> confirmClear());
        topBar.addView(clear, new LinearLayout.LayoutParams(dp(46), dp(44)));

        root.addView(topBar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56), Gravity.TOP));
    }

    private void renderHistory() {
        list.removeAllViews();
        List<MainActivity.WritePostHistoryItem> items = MainActivity.readWritePostHistory(preferences);
        if (items.isEmpty()) {
            list.addView(helperText(MainActivity.text("\u66f8\u304d\u8fbc\u307f\u5c65\u6b74\u306a\u3057", "No post history.")));
            return;
        }
        for (MainActivity.WritePostHistoryItem item : items) {
            list.addView(historyRow(item), rowParams());
        }
    }

    private LinearLayout historyRow(MainActivity.WritePostHistoryItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(9), dp(8), dp(9));
        row.setBackground(rowBackground());

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setOnClickListener(v -> openHistoryItem(item));
        row.addView(content, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView title = new TextView(this);
        title.setText(item.title);
        title.setTextColor(textColor());
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(title);

        TextView meta = helperText(postNumberLabel(item) + postedAtLabel(item));
        meta.setTextColor(Theme.accent(this));
        content.addView(meta);

        String body = item.body.trim().isEmpty()
                ? MainActivity.text("\u672c\u6587\u306f\u4fdd\u5b58\u3055\u308c\u3066\u3044\u307e\u305b\u3093", "Message body was not saved.")
                : compact(item.body, 180);
        TextView bodyView = helperText(body);
        bodyView.setTextColor(textColor());
        content.addView(bodyView);

        TextView url = helperText(item.url);
        content.addView(url);

        ImageButton jump = iconButton(R.drawable.ic_arrow_forward, MainActivity.text("\u66f8\u304d\u8fbc\u307f\u306b\u79fb\u52d5", "Jump to post"));
        jump.setOnClickListener(v -> openHistoryItem(item));
        row.addView(jump, iconParams());

        ImageButton delete = iconButton(R.drawable.ic_close, MainActivity.text("\u66f8\u304d\u8fbc\u307f\u5c65\u6b74\u3092\u524a\u9664", "Delete post history"));
        delete.setColorFilter(mutedColor());
        delete.setOnClickListener(v -> confirmDelete(item));
        row.addView(delete, iconParams());

        return row;
    }

    private void openHistoryItem(MainActivity.WritePostHistoryItem item) {
        if (item == null || item.url == null || item.url.trim().isEmpty()) {
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(item.url));
        if (item.number > 0) {
            intent.putExtra(MainActivity.EXTRA_JUMP_POST_NUMBER, item.number);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void confirmDelete(MainActivity.WritePostHistoryItem item) {
        if (item == null) {
            return;
        }
        String target = item.title + "\n" + postNumberLabel(item);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("\u66f8\u304d\u8fbc\u307f\u5c65\u6b74\u3092\u524a\u9664", "Delete post history"))
                .setMessage(MainActivity.text("\u3053\u306e\u66f8\u304d\u8fbc\u307f\u5c65\u6b74\u3092\u524a\u9664\u3057\u307e\u3059\u304b\uff1f\n", "Delete this post history item?\n") + target)
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u524a\u9664", "Delete"), (d, which) -> {
                    MainActivity.removeWritePostHistory(preferences, item);
                    renderHistory();
                })
                .create();
        dialog.show();
        Theme.styleDialog(dialog, this);
    }

    private void confirmClear() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("\u66f8\u304d\u8fbc\u307f\u5c65\u6b74\u3092\u5168\u524a\u9664", "Clear post history"))
                .setMessage(MainActivity.text("\u3059\u3079\u3066\u306e\u66f8\u304d\u8fbc\u307f\u5c65\u6b74\u3092\u524a\u9664\u3057\u307e\u3059\u304b\uff1f", "Clear all post history?"))
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u524a\u9664", "Delete"), (d, which) -> {
                    MainActivity.clearWritePostHistory(preferences);
                    renderHistory();
                    Toast.makeText(this, MainActivity.text("\u66f8\u304d\u8fbc\u307f\u5c65\u6b74\u3092\u524a\u9664", "Post history cleared."), Toast.LENGTH_SHORT).show();
                })
                .create();
        dialog.show();
        Theme.styleDialog(dialog, this);
    }

    private String postNumberLabel(MainActivity.WritePostHistoryItem item) {
        return item.number > 0
                ? MainActivity.text("\u30ec\u30b9: ", "Post: ") + ">>" + item.number
                : MainActivity.text("\u30ec\u30b9\u756a\u53f7\u672a\u78ba\u5b9a", "Post number unknown");
    }

    private String postedAtLabel(MainActivity.WritePostHistoryItem item) {
        String value = MainActivity.formatHistoryTime(item.postedAt);
        return value.isEmpty() ? "" : "  " + value;
    }

    private String compact(String value, int max) {
        String text = value == null ? "" : value.replace('\r', '\n').trim();
        text = text.replaceAll("\\n{3,}", "\n\n");
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, Math.max(0, max - 1)).trim() + "\u2026";
    }

    private TextView helperText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(mutedColor());
        view.setTextSize(13);
        view.setPadding(0, dp(2), 0, dp(2));
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(40), dp(40));
        params.setMargins(dp(6), 0, 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams rowParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(8));
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
}
