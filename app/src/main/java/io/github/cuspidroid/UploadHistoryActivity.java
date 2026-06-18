package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadHistoryActivity extends Activity {
    private static final Charset UTF8 = Charset.forName("UTF-8");

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

        ImageView thumbnail = new ImageView(this);
        thumbnail.setBackground(thumbnailBackground());
        thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int mediaSize = dp(108);
        LinearLayout.LayoutParams thumbParams = new LinearLayout.LayoutParams(mediaSize, mediaSize);
        thumbParams.setMargins(0, 0, dp(10), 0);
        row.addView(thumbnail, thumbParams);
        loadThumbnail(thumbnail, item.optString("url", ""));

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

        TextView remoteDelete = actionButton(MainActivity.text("ImgBBから削除", "Delete from ImgBB"), R.drawable.ic_delete, Color.rgb(190, 50, 50));
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
                .setTitle(MainActivity.text("ImgBBから削除", "Delete from ImgBB"))
                .setMessage(MainActivity.text(
                        "ImgBB上の画像を削除します。アップロード履歴は残ります。",
                        "This removes the image from ImgBB. The upload history stays in the app."))
                .setNegativeButton(MainActivity.text("キャンセル", "Cancel"), null)
                .setPositiveButton(MainActivity.text("削除", "Delete"), (dialog, which) -> {
                    Toast.makeText(this, MainActivity.text("ImgBBから削除中...", "Deleting from ImgBB..."), Toast.LENGTH_SHORT).show();
                    executor.execute(() -> {
                        try {
                            deleteFromImgbb(deleteUrl, item.optString("url", ""));
                            runOnUiThread(() -> Toast.makeText(this,
                                    MainActivity.text("ImgBBから削除しました。", "Deleted from ImgBB."),
                                    Toast.LENGTH_SHORT).show());
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(this,
                                    MainActivity.text("ImgBBから削除できませんでした: ", "Could not delete from ImgBB: ") + e.getMessage(),
                                    Toast.LENGTH_LONG).show());
                        }
                    });
                })
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

    private void loadThumbnail(ImageView target, String url) {
        if (url == null || url.isEmpty()) {
            return;
        }
        executor.execute(() -> {
            Bitmap bitmap = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setInstanceFollowRedirects(true);
                try (InputStream input = connection.getInputStream()) {
                    byte[] data = readLimited(input, 4 * 1024 * 1024);
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                } finally {
                    connection.disconnect();
                }
            } catch (Exception ignored) {
                bitmap = null;
            }
            Bitmap finalBitmap = bitmap;
            runOnUiThread(() -> {
                if (finalBitmap != null) {
                    target.setImageBitmap(finalBitmap);
                }
            });
        });
    }

    private byte[] readLimited(InputStream input, int limit) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > limit) {
                break;
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private void deleteFromImgbb(String deleteUrl, String imageUrl) throws Exception {
        Exception lastError = null;
        try {
            requestDeleteUrl(deleteUrl, "DELETE", null);
            if (isDeletedFromRemote(imageUrl)) {
                return;
            }
        } catch (Exception exception) {
            lastError = exception;
        }
        try {
            postJsonDelete(deleteUrl);
            if (isDeletedFromRemote(imageUrl)) {
                return;
            }
        } catch (Exception exception) {
            lastError = exception;
        }
        throw new IllegalStateException(lastError == null
                ? MainActivity.text("削除後も画像が残っています。", "The image still exists after deletion.")
                : lastError.getMessage());
    }

    private String requestDeleteUrl(String deleteUrl, String method, String cookie) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(deleteUrl).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 CuspiDroid");
        connection.setRequestProperty("Referer", "https://ibb.co/");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        if (cookie != null && !cookie.isEmpty()) {
            connection.setRequestProperty("Cookie", cookie);
        }

        int code = connection.getResponseCode();
        InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String response = "";
        if (stream != null) {
            response = new String(readLimited(stream, 512 * 1024), UTF8);
            stream.close();
        }
        connection.disconnect();
        if (code >= 400) {
            throw new IllegalStateException("HTTP " + code);
        }
        return response;
    }

    private void postJsonDelete(String deleteUrl) throws Exception {
        Uri uri = Uri.parse(deleteUrl);
        List<String> segments = uri.getPathSegments();
        if (segments.size() < 2) {
            throw new IllegalArgumentException(MainActivity.text("削除URLを解析できません。", "Could not parse delete URL."));
        }
        String imageId = segments.get(0);
        String imageHash = segments.get(1);
        String pathname = "/" + imageId + "/" + imageHash;
        String cookie = collectCookies(deleteUrl);
        String boundary = "----CuspiDroidImgBBDelete" + System.currentTimeMillis();

        HttpURLConnection connection = (HttpURLConnection) new URL("https://ibb.co/json").openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("Accept", "application/json, text/plain, */*");
        connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        connection.setRequestProperty("Origin", "https://ibb.co");
        connection.setRequestProperty("Referer", deleteUrl);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 CuspiDroid");
        if (!cookie.isEmpty()) {
            connection.setRequestProperty("Cookie", cookie);
        }

        try (OutputStream output = connection.getOutputStream()) {
            writeMultipartField(output, boundary, "pathname", pathname);
            writeMultipartField(output, boundary, "action", "delete");
            writeMultipartField(output, boundary, "delete", "image");
            writeMultipartField(output, boundary, "from", "resource");
            writeMultipartField(output, boundary, "deleting[id]", imageId);
            writeMultipartField(output, boundary, "deleting[hash]", imageHash);
            output.write(("--" + boundary + "--\r\n").getBytes(UTF8));
        }

        int code = connection.getResponseCode();
        InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String response = stream == null ? "" : new String(readLimited(stream, 512 * 1024), UTF8);
        if (stream != null) {
            stream.close();
        }
        connection.disconnect();
        if (code >= 400 || response.contains("\"error\"")) {
            throw new IllegalStateException("HTTP " + code);
        }
    }

    private String collectCookies(String deleteUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(deleteUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 CuspiDroid");
            connection.getResponseCode();
            List<String> cookies = new ArrayList<>();
            for (String value : connection.getHeaderFields().getOrDefault("Set-Cookie", new ArrayList<>())) {
                int semicolon = value.indexOf(';');
                cookies.add(semicolon >= 0 ? value.substring(0, semicolon) : value);
            }
            connection.disconnect();
            return String.join("; ", cookies);
        } catch (Exception ignored) {
            return "";
        }
    }

    private boolean isDeletedFromRemote(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return true;
        }
        try {
            Thread.sleep(1200);
            HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 CuspiDroid");
            int code = connection.getResponseCode();
            connection.disconnect();
            if (code == 404 || code == 410) {
                return true;
            }
            if (code == 405) {
                return isDeletedByGet(imageUrl);
            }
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isDeletedByGet(String imageUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 CuspiDroid");
            int code = connection.getResponseCode();
            InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
            if (stream != null) {
                readLimited(stream, 64 * 1024);
                stream.close();
            }
            connection.disconnect();
            return code == 404 || code == 410;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void writeMultipartField(OutputStream output, String boundary, String name, String value) throws Exception {
        output.write(("--" + boundary + "\r\n").getBytes(UTF8));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(UTF8));
        output.write((value == null ? "" : value).getBytes(UTF8));
        output.write("\r\n".getBytes(UTF8));
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
