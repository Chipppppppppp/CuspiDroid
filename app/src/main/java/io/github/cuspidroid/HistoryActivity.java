package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

public class HistoryActivity extends Activity {
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
        topBar.setPadding(dp(18), 0, dp(10), 0);
        topBar.setBackground(topBarBackground());
        TextView title = new TextView(this);
        title.setText(MainActivity.text("\u30b9\u30ec\u5c65\u6b74", "Thread History"));
        title.setTextColor(textColor());
        title.setTextSize(22);
        topBar.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        ImageButton clear = iconButton(R.drawable.ic_delete, MainActivity.text("\u5c65\u6b74\u3092\u5168\u524a\u9664", "Clear history"));
        clear.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(MainActivity.text("\u30b9\u30ec\u5c65\u6b74\u3092\u5168\u524a\u9664", "Clear thread history"))
                    .setMessage(MainActivity.text("\u3059\u3079\u3066\u306e\u30b9\u30ec\u5c65\u6b74\u3092\u524a\u9664\u3057\u307e\u3059\u304b\uff1f", "Clear all thread history?"))
                    .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                    .setPositiveButton(MainActivity.text("\u524a\u9664", "Delete"), (dialog, which) -> {
                        MainActivity.clearThreadHistory(preferences);
                        renderHistory();
                        Toast.makeText(this, MainActivity.text("\u30b9\u30ec\u5c65\u6b74\u3092\u524a\u9664", "Thread history cleared."), Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });
        topBar.addView(clear, new LinearLayout.LayoutParams(dp(46), dp(44)));
        root.addView(topBar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56), Gravity.TOP));
    }

    private void renderHistory() {
        list.removeAllViews();
        List<MainActivity.ThreadHistoryItem> history = MainActivity.readThreadHistory(preferences);
        if (history.isEmpty()) {
            TextView empty = helperText(MainActivity.text("\u30b9\u30ec\u5c65\u6b74\u306a\u3057", "No thread history."));
            list.addView(empty);
            return;
        }
        for (MainActivity.ThreadHistoryItem item : history) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(8), dp(8), dp(8));
            row.setBackground(rowBackground());

            String viewedAt = item.lastViewedAt > 0
                    ? "\n" + MainActivity.text("\u6700\u7d42\u95b2\u89a7: ", "Last viewed: ") + MainActivity.formatHistoryTime(item.lastViewedAt)
                    : "";
            TextView text = helperText(item.title + "\n" + item.url + viewedAt);
            text.setTextColor(textColor());
            row.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            ImageButton delete = iconButton(R.drawable.ic_close, MainActivity.text("\u5c65\u6b74\u3092\u524a\u9664", "Delete history item"));
            delete.setOnClickListener(v -> {
                MainActivity.removeThreadHistory(preferences, item.url);
                renderHistory();
            });
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(dp(42), dp(40));
            deleteParams.setMargins(dp(8), 0, 0, 0);
            row.addView(delete, deleteParams);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, dp(8));
            list.addView(row, rowParams);
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
