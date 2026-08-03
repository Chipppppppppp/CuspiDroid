package io.github.cuspidroid;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

final class SavedPostRowView {
    private SavedPostRowView() {
    }

    static FrameLayout create(Activity activity, String title, String fallbackTitle, int postNumber,
                              String metaSuffix, String body, int markerColor,
                              Runnable openAction, Runnable deleteAction) {
        FrameLayout shell = new FrameLayout(activity);
        shell.setBackground(rowBackground(activity));

        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(activity, 12), dp(activity, 9), dp(activity, 7), dp(activity, 9));
        shell.addView(row, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setOnClickListener(v -> openAction.run());
        row.addView(content, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView titleView = new TextView(activity);
        titleView.setText(displayTitle(title, fallbackTitle));
        titleView.setTextColor(Theme.text(activity));
        titleView.setTextSize(15);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(titleView);

        String number = postNumber > 0 ? ">>" + postNumber
                : MainActivity.text("\u30ec\u30b9\u756a\u53f7\u672a\u78ba\u5b9a", "Post number unknown");
        TextView metaView = helperText(activity, number + safe(metaSuffix));
        metaView.setTextColor(markerColor);
        content.addView(metaView);

        TextView bodyView = helperText(activity, compact(body, 180));
        bodyView.setTextColor(Theme.text(activity));
        content.addView(bodyView);

        ImageButton jump = iconButton(activity, R.drawable.ic_arrow_forward,
                MainActivity.text("\u66f8\u304d\u8fbc\u307f\u306b\u79fb\u52d5", "Jump to post"));
        jump.setOnClickListener(v -> openAction.run());
        row.addView(jump, new LinearLayout.LayoutParams(dp(activity, 40), dp(activity, 40)));

        ImageButton delete = iconButton(activity, R.drawable.ic_close,
                MainActivity.text("\u4e00\u89a7\u304b\u3089\u524a\u9664", "Remove from list"));
        delete.setColorFilter(Theme.muted(activity));
        delete.setOnClickListener(v -> deleteAction.run());
        row.addView(delete, new LinearLayout.LayoutParams(dp(activity, 40), dp(activity, 40)));
        return shell;
    }

    private static String displayTitle(String title, String fallbackTitle) {
        String value = safe(title).trim();
        return value.isEmpty() ? safe(fallbackTitle) : value;
    }

    private static String compact(String value, int max) {
        String text = safe(value).replace('\r', '\n').trim();
        text = text.replaceAll("\\n{3,}", "\n\n");
        if (text.length() <= max) return text;
        return text.substring(0, Math.max(0, max - 1)).trim() + "\u2026";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static TextView helperText(Activity activity, String value) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextColor(Theme.muted(activity));
        view.setTextSize(13);
        view.setPadding(0, dp(activity, 2), 0, dp(activity, 2));
        return view;
    }

    private static ImageButton iconButton(Activity activity, int iconRes, String description) {
        ImageButton button = new ImageButton(activity);
        button.setImageResource(iconRes);
        button.setContentDescription(description);
        button.setColorFilter(Theme.accent(activity));
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setPadding(dp(activity, 10), dp(activity, 10), dp(activity, 10), dp(activity, 10));
        return button;
    }

    private static GradientDrawable rowBackground(Activity activity) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.surface(activity));
        drawable.setStroke(dp(activity, 1), Theme.border(activity));
        drawable.setCornerRadius(dp(activity, 8));
        return drawable;
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }
}
