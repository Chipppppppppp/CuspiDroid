package io.github.cuspidroid;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shared post-list presentation used by Hissi, post history, and favorite posts. */
final class PostListItemView {
    private PostListItemView() {
    }

    static FrameLayout create(Activity activity, String title, String fallbackTitle, int postNumber,
                              String metaSuffix, String body, int markerColor,
                              Runnable openThreadAction, Runnable openPostAction,
                              Runnable deleteAction) {
        FrameLayout shell = new FrameLayout(activity);
        shell.setBackground(postBackground(activity));

        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(activity, 10), dp(activity, 8), dp(activity, 8), dp(activity, 10));
        shell.addView(card, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView source = threadHeader(activity, title, fallbackTitle,
                openThreadAction, null);
        LinearLayout.LayoutParams sourceParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sourceParams.setMargins(0, 0, 0, dp(activity, 7));
        card.addView(source, sourceParams);

        LinearLayout metaRow = new LinearLayout(activity);
        metaRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView meta = postMeta(activity, postNumber, metaSuffix, markerColor);
        meta.setOnClickListener(v -> openPostAction.run());
        metaRow.addView(meta, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        ImageButton jump = iconButton(activity, R.drawable.ic_arrow_forward,
                MainActivity.text("\u66f8\u304d\u8fbc\u307f\u306b\u79fb\u52d5", "Jump to post"));
        jump.setOnClickListener(v -> openPostAction.run());
        metaRow.addView(jump, new LinearLayout.LayoutParams(dp(activity, 34), dp(activity, 34)));

        ImageButton delete = iconButton(activity, R.drawable.ic_close,
                MainActivity.text("\u4e00\u89a7\u304b\u3089\u524a\u9664", "Remove from list"));
        delete.setColorFilter(Theme.muted(activity));
        delete.setOnClickListener(v -> deleteAction.run());
        metaRow.addView(delete, new LinearLayout.LayoutParams(dp(activity, 34), dp(activity, 34)));
        card.addView(metaRow);

        TextView bodyView = new TextView(activity);
        bodyView.setText(compact(body, 180));
        bodyView.setTextColor(Theme.text(activity));
        bodyView.setTextSize(15);
        bodyView.setLineSpacing(0, 1.15f);
        bodyView.setPadding(0, dp(activity, 2), 0, 0);
        bodyView.setOnClickListener(v -> openPostAction.run());
        card.addView(bodyView);
        return shell;
    }

    static TextView threadHeader(Activity activity, String title, String fallbackTitle,
                                 Runnable openThreadAction,
                                 View.OnLongClickListener longClickListener) {
        TextView view = new TextView(activity);
        view.setText(displayTitle(title, fallbackTitle));
        view.setTextColor(Theme.accent(activity));
        view.setTextSize(13);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        view.setMaxLines(2);
        view.setEllipsize(TextUtils.TruncateAt.END);
        view.setIncludeFontPadding(false);
        view.setPadding(dp(activity, 8), dp(activity, 5), dp(activity, 8), dp(activity, 5));
        view.setBackground(threadHeaderBackground(activity));
        if (openThreadAction != null) {
            view.setOnClickListener(v -> openThreadAction.run());
        }
        if (longClickListener != null) {
            view.setOnLongClickListener(longClickListener);
        }
        return view;
    }

    static void stylePostNumber(SpannableString text, int numberEnd, int color) {
        if (text == null || numberEnd <= 0 || numberEnd > text.length()) return;
        text.setSpan(new StyleSpan(Typeface.BOLD), 0, numberEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(color), 0, numberEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    static String formatMeta(String name, String date) {
        StringBuilder value = new StringBuilder();
        String cleanName = safe(name).trim();
        String cleanDate = safe(date).trim();
        if (!cleanName.isEmpty()) value.append("  ").append(cleanName);
        if (!cleanDate.isEmpty()) value.append("  ").append(cleanDate);
        return value.toString();
    }

    private static TextView postMeta(Activity activity, int postNumber, String suffix,
                                     int markerColor) {
        String number = postNumber > 0 ? String.valueOf(postNumber)
                : MainActivity.text("\u30ec\u30b9\u756a\u53f7\u672a\u78ba\u5b9a", "Post number unknown");
        SpannableString value = new SpannableString(number + safe(suffix));
        if (postNumber > 0) {
            stylePostNumber(value, number.length(), markerColor);
        }
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextColor(Theme.muted(activity));
        view.setTextSize(14);
        view.setPadding(0, dp(activity, 2), 0, dp(activity, 4));
        return view;
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

    private static ImageButton iconButton(Activity activity, int iconRes, String description) {
        ImageButton button = new ImageButton(activity);
        button.setImageResource(iconRes);
        button.setContentDescription(description);
        button.setColorFilter(Theme.accent(activity));
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setPadding(dp(activity, 7), dp(activity, 7), dp(activity, 7), dp(activity, 7));
        return button;
    }

    private static GradientDrawable postBackground(Activity activity) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.post(activity));
        drawable.setCornerRadius(dp(activity, 12));
        return drawable;
    }

    private static GradientDrawable threadHeaderBackground(Activity activity) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.menu(activity));
        drawable.setStroke(dp(activity, 1), Theme.border(activity));
        drawable.setCornerRadius(dp(activity, 8));
        return drawable;
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }
}
