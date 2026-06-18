package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadHistoryActivity extends Activity {
    private SharedPreferences preferences;
    private LinearLayout list;
    private ExecutorService executor;

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
        executor = Executors.newFixedThreadPool(3);
        buildLayout();
        renderUploads();
    }

    @Override
    protected void onDestroy() {
        if (executor != null) {
            executor.shutdownNow();
        }
        super.onDestroy();
    }

    private void buildLayout() {
        Theme.applySystemBars(this);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(bgColor());
        setContentView(root);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(70), dp(12), dp(20));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(10), 0, dp(10), 0);
        topBar.setBackground(topBarBackground());

        ImageButton back = iconButton(R.drawable.ic_arrow_back, MainActivity.text("戻る", "Back"));
        back.setOnClickListener(v -> finish());
        topBar.addView(back, new LinearLayout.LayoutParams(dp(44), dp(44)));

        TextView title = new TextView(this);
        title.setText(MainActivity.text("アップロード履歴", "Upload History"));
        title.setTextColor(textColor());
        title.setTextSize(21);
        title.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        titleParams.setMargins(dp(8), 0, 0, 0);
        topBar.addView(title, titleParams);

        root.addView(topBar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56), Gravity.TOP));
    }

    private void renderUploads() {
        list.removeAllViews();
        JSONArray uploads = readUploads();
        if (uploads.length() == 0) {
            TextView empty = helperText(MainActivity.text("アップロード履歴なし", "No upload history."));
            list.addView(empty);
            return;
        }

        TextView note = helperText(MainActivity.text(
                "「ImgBBから削除」は画像そのものを削除します。「履歴から削除」はアプリ内の記録だけを削除します。",
                "Delete from ImgBB removes the image. Delete from history only removes the local record."));
        note.setPadding(0, 0, 0, dp(10));
        list.addView(note);

        for (int i = 0; i < uploads.length(); i++) {
            JSONObject item = uploads.optJSONObject(i);
            if (item == null) {
                continue;
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, dp(8));
            list.addView(uploadRow(item, i), params);
        }
    }

    private LinearLayout uploadRow(JSONObject item, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        row.setPadding(dp(8), dp(8), dp(8), dp(8));
        row.setBackground(rowBackground());

        int mediaSize = dp(108);
        String mediaUrl = item.optString("url", "");
        View thumbnail = MediaPreviewHelper.create(this, preferences, executor,
                new android.os.Handler(android.os.Looper.getMainLooper()),
                mediaUrl, mediaUrl, isVideoUrl(mediaUrl, item.optString("mime", "")),
                mediaSize, null, mediaPreviewCallbacks());
        LinearLayout.LayoutParams thumbParams = new LinearLayout.LayoutParams(mediaSize, mediaSize);
        thumbParams.setMargins(0, 0, dp(10), 0);
        row.addView(thumbnail, thumbParams);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        row.addView(content, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        ImageButton historyDelete = iconButton(R.drawable.ic_close, MainActivity.text("履歴から削除", "Delete history"));
        historyDelete.setColorFilter(mutedColor());
        historyDelete.setOnClickListener(v -> confirmHistoryDelete(index));
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(dp(38), dp(38));
        deleteParams.setMargins(dp(6), 0, 0, 0);
        row.addView(historyDelete, deleteParams);

        TextView name = new TextView(this);
        name.setText(item.optString("name", MainActivity.text("画像", "Image")));
        name.setTextColor(textColor());
        name.setTextSize(15);
        name.setSingleLine(false);
        content.addView(name);

        TextView meta = helperText(formatUploadTime(item.optLong("time", 0))
                + expirationLabel(item.optInt("expiration", 0)));
        content.addView(meta);

        TextView url = helperText(item.optString("url", ""));
        url.setTextColor(Theme.accent(this));
        url.setOnClickListener(v -> openUrl(item.optString("url", "")));
        content.addView(url);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actionParams.setMargins(0, dp(6), 0, 0);
        content.addView(actions, actionParams);

        TextView copy = actionButton(MainActivity.text("URLコピー", "Copy URL"), R.drawable.ic_copy, Theme.accent(this));
        copy.setOnClickListener(v -> copyUrl(item.optString("url", "")));
        actions.addView(copy, buttonParams());

        TextView remoteDelete = actionButton(MainActivity.text("削除", "Delete"), R.drawable.ic_delete, Color.rgb(190, 50, 50));
        remoteDelete.setOnClickListener(v -> confirmRemoteDelete(item));
        actions.addView(remoteDelete, buttonParams());

        return row;
    }

    private void confirmRemoteDelete(JSONObject item) {
        String deleteUrl = item.optString("delete_url", "");
        if (deleteUrl.isEmpty()) {
            Toast.makeText(this, MainActivity.text("削除URLが保存されていません。", "No delete URL was saved."), Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("削除URLを開く", "Open delete URL"))
                .setMessage(MainActivity.text(
                        "ImgBBの公式APIではアップロードのみが案内されているため、ImgBBから返された削除URLを開きます。開いたページで削除を確定してください。",
                        "ImgBB's official API documents uploads only; this opens the delete URL returned by ImgBB. Confirm deletion on the opened page."))
                .setNegativeButton(MainActivity.text("キャンセル", "Cancel"), null)
                .setPositiveButton(MainActivity.text("開く", "Open"), (dialog, which) -> openUrl(deleteUrl))
                .show();
    }
    private void confirmHistoryDelete(int index) {
        new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("履歴から削除", "Delete from history"))
                .setMessage(MainActivity.text(
                        "アプリ内のアップロード履歴からだけ削除します。ImgBB上の画像は削除されません。",
                        "This only removes the local upload history. The image remains on ImgBB."))
                .setNegativeButton(MainActivity.text("キャンセル", "Cancel"), null)
                .setPositiveButton(MainActivity.text("削除", "Delete"), (dialog, which) -> {
                    removeHistoryAt(index);
                    renderUploads();
                })
                .show();
    }

    private void copyUrl(String value) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("ImgBB URL", value));
            Toast.makeText(this, MainActivity.text("URLをコピーしました。", "URL copied."), Toast.LENGTH_SHORT).show();
        }
    }

    private void openUrl(String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(value)));
        } catch (Exception exception) {
            Toast.makeText(this, MainActivity.text("リンクを開けません。", "Cannot open link."), Toast.LENGTH_SHORT).show();
        }
    }

    private MediaPreviewHelper.Callback mediaPreviewCallbacks() {
        return new MediaPreviewHelper.Callback() {
            @Override
            public void openImage(String originalUrl, String mediaUrl) {
                openUrl(mediaUrl);
            }

            @Override
            public void openVideo(String originalUrl, String mediaUrl) {
                openUrl(mediaUrl);
            }

            @Override
            public void openExternal(String url) {
                openUrl(url);
            }
        };
    }

    private boolean isVideoUrl(String url, String mime) {
        String value = ((url == null ? "" : url) + " " + (mime == null ? "" : mime)).toLowerCase(java.util.Locale.ROOT);
        return value.contains("video/") || value.matches(".*\\.(mp4|m4v|webm|mov)(\\?.*)?(\\s.*)?");
    }

    private JSONArray readUploads() {
        try {
            return new JSONArray(preferences.getString(MainActivity.PREF_IMGBB_UPLOADS, "[]"));
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    private void saveUploads(JSONArray uploads) {
        preferences.edit().putString(MainActivity.PREF_IMGBB_UPLOADS, uploads.toString()).apply();
    }

    private void removeHistoryAt(int index) {
        JSONArray current = readUploads();
        JSONArray next = new JSONArray();
        for (int i = 0; i < current.length(); i++) {
            if (i != index) {
                JSONObject item = current.optJSONObject(i);
                if (item != null) {
                    next.put(item);
                }
            }
        }
        saveUploads(next);
    }

    private String formatUploadTime(long time) {
        if (time <= 0) {
            return "";
        }
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(time));
    }

    private String expirationLabel(int seconds) {
        if (seconds <= 0) {
            return MainActivity.text(" / 期限なし", " / no expiration");
        }
        if (seconds < 3600) {
            return MainActivity.text(" / 期限: " + (seconds / 60) + "分", " / expires: " + (seconds / 60) + " min");
        }
        if (seconds < 86400) {
            return MainActivity.text(" / 期限: " + (seconds / 3600) + "時間", " / expires: " + (seconds / 3600) + " h");
        }
        return MainActivity.text(" / 期限: " + (seconds / 86400) + "日", " / expires: " + (seconds / 86400) + " d");
    }

    private TextView helperText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(mutedColor());
        view.setTextSize(13);
        view.setPadding(0, dp(2), 0, dp(2));
        return view;
    }

    private TextView actionButton(String label, int iconRes, int color) {
        TextView view = new TextView(this);
        view.setText(label);
        view.setTextColor(color);
        view.setTextSize(12);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(7), dp(6), dp(7), dp(6));
        view.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
        view.setCompoundDrawablePadding(dp(4));
        for (Drawable drawable : view.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
        }
        view.setBackground(actionButtonBackground(color));
        return view;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, dp(6), 0);
        return params;
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

    private GradientDrawable thumbnailBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.dark(this) ? Color.rgb(30, 36, 43) : Color.rgb(236, 240, 244));
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(dp(6));
        return drawable;
    }

    private GradientDrawable iconButtonBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.TRANSPARENT);
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private GradientDrawable actionButtonBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(dp(1), color);
        drawable.setCornerRadius(dp(6));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
