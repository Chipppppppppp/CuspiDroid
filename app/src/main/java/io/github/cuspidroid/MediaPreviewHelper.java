package io.github.cuspidroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MediaPreviewHelper {
    static final int SENSITIVE_MODEL_VERSION = 5;

    interface ImageClassifier {
        boolean isSensitive(Bitmap bitmap);
    }

    interface Callback {
        void openImage(String originalUrl, String mediaUrl, boolean sensitive);
        void openVideo(String originalUrl, String mediaUrl, boolean sensitive);
        void openExternal(String url);
    }

    private MediaPreviewHelper() {
    }

    static View create(Activity activity, SharedPreferences preferences, Executor executor, Handler mainHandler,
                       String originalUrl, String mediaUrl, boolean video, int cellSize,
                       Runnable longClickAction, Callback callback) {
        return create(activity, preferences, executor, mainHandler, originalUrl, mediaUrl, video,
                cellSize, longClickAction, false, null, callback);
    }

    static View create(Activity activity, SharedPreferences preferences, Executor executor, Handler mainHandler,
                       String originalUrl, String mediaUrl, boolean video, int cellSize,
                       Runnable longClickAction, boolean forceSensitive,
                       ImageClassifier classifier, Callback callback) {
        FrameLayout frame = new SquareMediaFrame(activity, cellSize);
        final String[] activeMediaUrl = {mediaUrl};
        final boolean[] activeSensitive = {forceSensitive};
        frame.setClickable(true);
        frame.setClipChildren(true);
        frame.setClipToPadding(true);
        frame.setBackgroundColor(video ? Color.BLACK : mediaBackground(activity));
        frame.setMinimumWidth(cellSize);
        frame.setMinimumHeight(cellSize);
        if (longClickAction != null) {
            frame.setOnLongClickListener(v -> {
                longClickAction.run();
                return true;
            });
        }

        ImageView image = new ContainedMediaView(activity);
        image.setVisibility(View.GONE);
        image.setOnClickListener(v -> {
            String openUrl = activeMediaUrl[0];
            if (video || isVideoUrl(openUrl)) {
                callback.openVideo(originalUrl, openUrl, activeSensitive[0]);
            } else {
                callback.openImage(originalUrl, openUrl, activeSensitive[0]);
            }
        });
        if (longClickAction != null) {
            image.setOnLongClickListener(v -> {
                longClickAction.run();
                return true;
            });
        }
        frame.addView(image, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ProgressBar spinner = new ProgressBar(activity);
        spinner.setIndeterminate(true);
        spinner.setAlpha(0.6f);
        FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(dp(activity, 28), dp(activity, 28));
        spinnerParams.gravity = Gravity.CENTER;
        frame.addView(spinner, spinnerParams);

        TextView error = centeredLabel(activity, video
                ? MainActivity.text("動画を表示できません", "Video unavailable")
                : MainActivity.text("画像を表示できません", "Image unavailable"));
        error.setVisibility(View.GONE);
        error.setOnClickListener(v -> callback.openExternal(originalUrl));
        frame.addView(error, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView play = playOverlay(activity);
        play.setVisibility(video ? View.VISIBLE : View.GONE);
        play.setOnClickListener(v -> {
            String openUrl = activeMediaUrl[0];
            if (video || isVideoUrl(openUrl)) {
                callback.openVideo(originalUrl, openUrl, activeSensitive[0]);
            } else {
                callback.openImage(originalUrl, openUrl, activeSensitive[0]);
            }
        });
        frame.addView(play, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Button reveal = new Button(activity);
        reveal.setText(MainActivity.text("\u95b2\u89a7\u6ce8\u610f", "Sensitive"));
        reveal.setTextSize(11);
        reveal.setTextColor(Color.WHITE);
        reveal.setBackgroundColor(Color.argb(200, 15, 23, 42));
        reveal.setMinWidth(0);
        reveal.setMinHeight(0);
        reveal.setPadding(dp(activity, 4), 0, dp(activity, 4), 0);
        reveal.setVisibility(View.GONE);
        FrameLayout.LayoutParams revealParams = new FrameLayout.LayoutParams(
                Math.min(dp(activity, 112), Math.max(1, cellSize - dp(activity, 8))),
                Math.min(dp(activity, 42), Math.max(1, cellSize - dp(activity, 8))),
                Gravity.CENTER);
        frame.addView(reveal, revealParams);

        frame.setOnClickListener(v -> {
            String openUrl = activeMediaUrl[0];
            if (video || isVideoUrl(openUrl)) {
                callback.openVideo(originalUrl, openUrl, activeSensitive[0]);
            } else {
                callback.openImage(originalUrl, openUrl, activeSensitive[0]);
            }
        });

        executor.execute(() -> {
            Bitmap bitmap = null;
            Drawable drawable = null;
            boolean sensitive = forceSensitive;
            try {
                boolean gif = isGifUrl(mediaUrl);
                if (video || isVideoUrl(mediaUrl)) {
                    byte[] cached = AppCache.read(activity, preferences, "media", "video:" + mediaUrl, ".png");
                    if (cached != null) {
                        bitmap = BitmapFactory.decodeByteArray(cached, 0, cached.length);
                    }
                    if (bitmap == null) {
                        bitmap = videoPosterBitmap(mediaUrl);
                        if (bitmap != null) {
                            byte[] bytes = bitmapToPng(bitmap);
                            AppCache.write(activity, preferences, "media", "video:" + mediaUrl, ".png", bytes);
                        }
                    }
                } else {
                    String loadUrl = mediaUrl;
                    String resolvedCacheKey = "media_resolved:" + mediaUrl;
                    if (!isDirectMediaUrl(mediaUrl)) {
                        String cachedResolved = preferences.getString(resolvedCacheKey, null);
                        if (cachedResolved != null && !cachedResolved.trim().isEmpty()) {
                            loadUrl = cachedResolved;
                            activeMediaUrl[0] = cachedResolved;
                        }
                    }
                    boolean loadGif = isGifUrl(loadUrl);
                    byte[] bytes = AppCache.read(activity, preferences, "media", "image:" + loadUrl,
                            loadGif ? ".gif" : ".img");
                    if (bytes == null) {
                        DownloadedMedia downloaded = downloadResolvedBytes(loadUrl,
                                loadGif ? 32 * 1024 * 1024 : 40 * 1024 * 1024);
                        bytes = downloaded.bytes;
                        activeMediaUrl[0] = downloaded.url;
                        if (!isDirectMediaUrl(mediaUrl)) {
                            preferences.edit().putString(resolvedCacheKey, downloaded.url).apply();
                        }
                        boolean downloadedGif = isGifUrl(downloaded.url);
                        AppCache.write(activity, preferences, "media", "image:" + downloaded.url,
                                downloadedGif ? ".gif" : ".img", bytes);
                    }
                    if (isGifUrl(activeMediaUrl[0]) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        drawable = ImageDecoder.decodeDrawable(ImageDecoder.createSource(ByteBuffer.wrap(bytes)));
                        if (drawable instanceof AnimatedImageDrawable) {
                            ((AnimatedImageDrawable) drawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
                        }
                    }
                    bitmap = decodePreviewBitmap(bytes, cellSize);
                }
                boolean activeGif = isGifUrl(activeMediaUrl[0]);
                boolean checkSensitive = classifier != null && preferences.getBoolean(
                        MainActivity.PREF_BLUR_IMGUR, true)
                        && (!video || preferences.getBoolean(
                                MainActivity.PREF_BLUR_VIDEO_THUMBNAILS, true))
                        && (!activeGif || preferences.getBoolean(
                                MainActivity.PREF_BLUR_GIF_THUMBNAILS, true));
                if (!sensitive && bitmap != null && checkSensitive) {
                    Boolean cached = readSensitive(preferences, activeMediaUrl[0]);
                    sensitive = cached != null ? cached : classifier.isSensitive(bitmap);
                    if (cached == null) {
                        saveSensitive(preferences, activeMediaUrl[0], sensitive);
                    }
                }
            } catch (Exception ignored) {
                bitmap = null;
                drawable = null;
            }
            Bitmap finalBitmap = bitmap;
            Drawable finalDrawable = drawable;
            boolean finalSensitive = sensitive;
            boolean gif = isGifUrl(activeMediaUrl[0]);
            mainHandler.post(() -> runWhenAttached(frame, () -> {
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
                activeSensitive[0] = finalSensitive;
                spinner.setVisibility(View.GONE);
                if (finalSensitive) {
                    if (finalBitmap != null) {
                        image.setImageBitmap(blurredBitmap(finalBitmap));
                        image.setVisibility(View.VISIBLE);
                    }
                    play.setVisibility(View.GONE);
                    reveal.setVisibility(View.VISIBLE);
                    reveal.bringToFront();
                    reveal.setOnClickListener(v -> {
                        reveal.setVisibility(View.GONE);
                        if (finalBitmap == null) {
                            play.setVisibility(video || gif ? View.VISIBLE : View.GONE);
                            error.setVisibility(video || gif ? View.GONE : View.VISIBLE);
                        } else if (finalDrawable != null && gif && autoplayGifs(preferences)) {
                            image.setImageDrawable(finalDrawable);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                                    && finalDrawable instanceof AnimatedImageDrawable) {
                                ((AnimatedImageDrawable) finalDrawable).start();
                            }
                            play.setVisibility(View.GONE);
                        } else {
                            image.setImageBitmap(finalBitmap);
                            play.setVisibility(video || (gif && !autoplayGifs(preferences))
                                    ? View.VISIBLE : View.GONE);
                            if (gif && !autoplayGifs(preferences) && finalDrawable != null) {
                                play.setOnClickListener(playView -> {
                                    image.setImageDrawable(finalDrawable);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                                            && finalDrawable instanceof AnimatedImageDrawable) {
                                        ((AnimatedImageDrawable) finalDrawable).start();
                                    }
                                    play.setVisibility(View.GONE);
                                });
                            }
                        }
                    });
                } else if (finalDrawable != null && gif && autoplayGifs(preferences)) {
                    image.setImageDrawable(finalDrawable);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && finalDrawable instanceof AnimatedImageDrawable) {
                        ((AnimatedImageDrawable) finalDrawable).start();
                    }
                    image.setVisibility(View.VISIBLE);
                    play.setVisibility(View.GONE);
                } else if (finalBitmap != null) {
                    image.setImageBitmap(finalBitmap);
                    image.setVisibility(View.VISIBLE);
                    play.setVisibility(video || (gif && !autoplayGifs(preferences)) ? View.VISIBLE : View.GONE);
                    if (gif && !autoplayGifs(preferences) && finalDrawable != null) {
                        play.setOnClickListener(v -> {
                            image.setImageDrawable(finalDrawable);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && finalDrawable instanceof AnimatedImageDrawable) {
                                ((AnimatedImageDrawable) finalDrawable).start();
                            }
                            play.setVisibility(View.GONE);
                        });
                    }
                } else {
                    error.setVisibility(video || (gif && !autoplayGifs(preferences)) ? View.GONE : View.VISIBLE);
                    play.setVisibility(video || (gif && !autoplayGifs(preferences)) ? View.VISIBLE : View.GONE);
                }
            }));
        });
        return frame;
    }

    static Boolean readSensitive(SharedPreferences preferences, String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            JSONObject root = new JSONObject(preferences.getString(MainActivity.PREF_IMGUR_META, "{}"));
            JSONObject item = root.optJSONObject(url);
            return item == null || !item.has("sensitive")
                    || item.optInt("sensitiveModelVersion", 0) != SENSITIVE_MODEL_VERSION
                    ? null : item.optBoolean("sensitive", false);
        } catch (Exception ignored) {
            return null;
        }
    }

    static void saveSensitive(SharedPreferences preferences, String url, boolean sensitive) {
        if (url == null || url.isEmpty()) {
            return;
        }
        try {
            JSONObject root = new JSONObject(preferences.getString(MainActivity.PREF_IMGUR_META, "{}"));
            JSONObject item = root.optJSONObject(url);
            if (item == null) {
                item = new JSONObject();
            }
            item.put("sensitive", sensitive);
            item.put("sensitiveModelVersion", SENSITIVE_MODEL_VERSION);
            item.put("savedAt", System.currentTimeMillis());
            root.put(url, item);
            preferences.edit().putString(MainActivity.PREF_IMGUR_META, root.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    static Bitmap blurredBitmap(Bitmap bitmap) {
        int width = Math.max(1, bitmap.getWidth() / 24);
        int height = Math.max(1, bitmap.getHeight() / 24);
        Bitmap small = Bitmap.createScaledBitmap(bitmap, width, height, true);
        Bitmap softened = boxBlur(small, 2);
        if (softened != small && small != bitmap) {
            small.recycle();
        }
        Bitmap blurred = Bitmap.createScaledBitmap(softened,
                bitmap.getWidth(), bitmap.getHeight(), true);
        if (blurred != softened) {
            softened.recycle();
        }
        return blurred;
    }

    private static Bitmap boxBlur(Bitmap source, int iterations) {
        Bitmap current = source.copy(Bitmap.Config.ARGB_8888, true);
        int width = current.getWidth();
        int height = current.getHeight();
        if (width < 3 || height < 3) {
            return current;
        }
        int[] pixels = new int[width * height];
        int[] blurred = new int[width * height];
        for (int pass = 0; pass < iterations; pass++) {
            current.getPixels(pixels, 0, width, 0, 0, width, height);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int a = 0;
                    int r = 0;
                    int g = 0;
                    int b = 0;
                    int count = 0;
                    for (int dy = -1; dy <= 1; dy++) {
                        int yy = y + dy;
                        if (yy < 0 || yy >= height) {
                            continue;
                        }
                        for (int dx = -1; dx <= 1; dx++) {
                            int xx = x + dx;
                            if (xx < 0 || xx >= width) {
                                continue;
                            }
                            int color = pixels[yy * width + xx];
                            a += Color.alpha(color);
                            r += Color.red(color);
                            g += Color.green(color);
                            b += Color.blue(color);
                            count++;
                        }
                    }
                    blurred[y * width + x] = Color.argb(
                            a / count, r / count, g / count, b / count);
                }
            }
            current.setPixels(blurred, 0, width, 0, 0, width, height);
        }
        return current;
    }

    private static void runWhenAttached(View view, Runnable action) {
        if (view.isAttachedToWindow()) {
            action.run();
            return;
        }
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View attached) {
                attached.removeOnAttachStateChangeListener(this);
                action.run();
            }

            @Override
            public void onViewDetachedFromWindow(View detached) {
            }
        });
    }

    private static DownloadedMedia downloadResolvedBytes(String url, int limit) throws Exception {
        return downloadResolvedBytes(url, limit, 0);
    }

    private static DownloadedMedia downloadResolvedBytes(String url, int limit, int depth) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "CuspiDroid/0.1");
        try {
            String contentType = valueOr(connection.getContentType(), "").toLowerCase(Locale.ROOT);
            int readLimit = contentType.contains("text/html") ? 512 * 1024 : limit;
            byte[] bytes;
            try (InputStream input = connection.getInputStream()) {
                bytes = readBytes(input, readLimit);
            }
            if (depth < 2 && looksLikeHtml(contentType, bytes)) {
                String html = new String(bytes, StandardCharsets.UTF_8);
                String mediaUrl = extractMetaMediaUrl(html, connection.getURL().toString());
                if (mediaUrl != null && !mediaUrl.equals(url)) {
                    return downloadResolvedBytes(mediaUrl, limit, depth + 1);
                }
            }
            return new DownloadedMedia(bytes, connection.getURL().toString());
        } finally {
            connection.disconnect();
        }
    }

    private static boolean looksLikeHtml(String contentType, byte[] bytes) {
        if (contentType.contains("text/html") || contentType.contains("application/xhtml")) {
            return true;
        }
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        String head = new String(bytes, 0, Math.min(bytes.length, 256), StandardCharsets.UTF_8)
                .trim().toLowerCase(Locale.ROOT);
        return head.startsWith("<!doctype html") || head.startsWith("<html") || head.contains("<head");
    }

    private static String extractMetaMediaUrl(String html, String baseUrl) {
        if (html == null || html.isEmpty()) {
            return null;
        }
        String[] names = {
                "og:video:secure_url", "og:video:url", "og:video",
                "og:image:secure_url", "og:image", "twitter:image", "twitter:image:src"
        };
        for (String name : names) {
            String value = metaContent(html, name);
            String resolved = resolveUrl(baseUrl, value);
            if (resolved != null) {
                return resolved;
            }
        }
        Matcher linkMatcher = Pattern.compile(
                "<link\\b(?=[^>]*\\brel=[\"'][^\"']*image_src[^\"']*[\"'])(?=[^>]*\\bhref=[\"']([^\"']+)[\"'])[^>]*>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(html);
        if (linkMatcher.find()) {
            return resolveUrl(baseUrl, htmlDecode(linkMatcher.group(1)));
        }
        return null;
    }

    private static String metaContent(String html, String property) {
        String quoted = Pattern.quote(property);
        Pattern attrFirst = Pattern.compile(
                "<meta\\b(?=[^>]*(?:property|name)=[\"']" + quoted + "[\"'])(?=[^>]*content=[\"']([^\"']+)[\"'])[^>]*>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = attrFirst.matcher(html);
        if (matcher.find()) {
            return htmlDecode(matcher.group(1));
        }
        return null;
    }

    private static String resolveUrl(String baseUrl, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            String trimmed = value.trim();
            if (trimmed.startsWith("//")) {
                URL base = new URL(baseUrl);
                return base.getProtocol() + ":" + trimmed;
            }
            return new URL(new URL(baseUrl), trimmed).toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String htmlDecode(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }

    private static TextView playOverlay(Activity activity) {
        TextView play = new TextView(activity);
        play.setText("\u25b6");
        play.setTextColor(Color.WHITE);
        play.setTextSize(34);
        play.setGravity(Gravity.CENTER);
        play.setBackgroundColor(Color.argb(82, 0, 0, 0));
        return play;
    }

    private static TextView centeredLabel(Activity activity, String message) {
        TextView label = new TextView(activity);
        label.setText(message);
        label.setTextColor(Theme.muted(activity));
        label.setGravity(Gravity.CENTER);
        label.setTextSize(15);
        label.setPadding(dp(activity, 8), dp(activity, 8), dp(activity, 8), dp(activity, 8));
        label.setBackgroundColor(mediaBackground(activity));
        return label;
    }

    private static int mediaBackground(Activity activity) {
        return Theme.dark(activity) ? Color.rgb(15, 23, 42) : Color.rgb(241, 245, 249);
    }

    private static boolean autoplayGifs(SharedPreferences preferences) {
        return preferences.getBoolean(MainActivity.PREF_AUTOPLAY_GIFS, false);
    }

    private static boolean isGifUrl(String url) {
        String lower = url == null ? "" : url.toLowerCase(Locale.ROOT);
        return lower.endsWith(".gif") || lower.contains(".gif?");
    }

    private static boolean isVideoUrl(String url) {
        String lower = url == null ? "" : url.toLowerCase(Locale.ROOT);
        return lower.matches(".*\\.(mp4|m4v|webm|mov)(\\?.*)?");
    }

    private static boolean isDirectMediaUrl(String url) {
        String lower = url == null ? "" : url.toLowerCase(Locale.ROOT);
        int query = lower.indexOf('?');
        if (query >= 0) {
            lower = lower.substring(0, query);
        }
        int fragment = lower.indexOf('#');
        if (fragment >= 0) {
            lower = lower.substring(0, fragment);
        }
        return lower.matches(".*\\.(jpe?g|png|webp|gif|bmp|avif|mp4|m4v|webm|mov)$");
    }

    private static byte[] downloadBytes(String url, int limit) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "CuspiDroid/0.1");
        try (InputStream input = connection.getInputStream()) {
            return readBytes(input, limit);
        } finally {
            connection.disconnect();
        }
    }

    private static byte[] readBytes(InputStream input, int limit) throws Exception {
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

    private static Bitmap decodePreviewBitmap(byte[] bytes, int targetSize) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }
        int sampleSize = 1;
        int requested = Math.max(targetSize * 2, 1);
        while (Math.max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= requested) {
            sampleSize *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    static ViewerMedia loadForViewer(String url, int targetWidth, int targetHeight) throws Exception {
        DownloadedMedia downloaded = downloadResolvedBytes(url, 40 * 1024 * 1024);
        Drawable drawable = null;
        if (isGifUrl(downloaded.url) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            drawable = ImageDecoder.decodeDrawable(ImageDecoder.createSource(ByteBuffer.wrap(downloaded.bytes)));
            if (drawable instanceof AnimatedImageDrawable) {
                ((AnimatedImageDrawable) drawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
            }
        }
        Bitmap bitmap = decodePreviewBitmap(downloaded.bytes, Math.max(targetWidth, targetHeight));
        return new ViewerMedia(downloaded.url, downloaded.bytes, bitmap, drawable);
    }

    private static String valueOr(String value, String fallback) {
        return value == null ? fallback : value;
    }

    static Bitmap videoPosterBitmap(String videoUrl) {
        for (String candidate : videoPosterCandidates(videoUrl)) {
            try {
                byte[] bytes = downloadBytes(candidate, 4 * 1024 * 1024);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    return bitmap;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static List<String> videoPosterCandidates(String videoUrl) {
        List<String> candidates = new ArrayList<>();
        String value = videoUrl == null ? "" : videoUrl;
        int query = value.indexOf('?');
        String baseWithExtension = query >= 0 ? value.substring(0, query) : value;
        String queryPart = query >= 0 ? value.substring(query) : "";
        int slash = baseWithExtension.lastIndexOf('/');
        int dot = baseWithExtension.lastIndexOf('.');
        if (dot <= slash) {
            return candidates;
        }
        String base = baseWithExtension.substring(0, dot);
        candidates.add(base + ".jpg" + queryPart);
        candidates.add(base + ".webp" + queryPart);
        candidates.add(base + ".png" + queryPart);
        return candidates;
    }

    private static byte[] bitmapToPng(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, output);
        return output.toByteArray();
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class SquareMediaFrame extends FrameLayout {
        private final int size;

        SquareMediaFrame(Activity activity, int size) {
            super(activity);
            this.size = Math.max(1, size);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int exact = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
            super.onMeasure(exact, exact);
            setMeasuredDimension(size, size);
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            int save = canvas.save();
            canvas.clipRect(0, 0, getWidth(), getHeight());
            super.dispatchDraw(canvas);
            canvas.restoreToCount(save);
        }
    }

    private static class ContainedMediaView extends ImageView {
        private final Matrix fitMatrix = new Matrix();

        ContainedMediaView(Activity activity) {
            super(activity);
            setScaleType(ScaleType.MATRIX);
            setAdjustViewBounds(false);
            setCropToPadding(false);
        }

        @Override
        public void setImageBitmap(Bitmap bitmap) {
            super.setImageBitmap(bitmap);
            post(this::fitDrawable);
        }

        @Override
        public void setImageDrawable(Drawable drawable) {
            super.setImageDrawable(drawable);
            post(this::fitDrawable);
        }

        @Override
        protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            super.onSizeChanged(width, height, oldWidth, oldHeight);
            fitDrawable();
        }

        private void fitDrawable() {
            Drawable drawable = getDrawable();
            int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
            if (drawable == null || availableWidth <= 0 || availableHeight <= 0) {
                return;
            }
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();
            if (drawableWidth <= 0 || drawableHeight <= 0) {
                return;
            }
            float scale = Math.min(availableWidth / (float) drawableWidth,
                    availableHeight / (float) drawableHeight);
            float left = getPaddingLeft() + (availableWidth - drawableWidth * scale) / 2f;
            float top = getPaddingTop() + (availableHeight - drawableHeight * scale) / 2f;
            fitMatrix.reset();
            fitMatrix.postScale(scale, scale);
            fitMatrix.postTranslate(left, top);
            setImageMatrix(fitMatrix);
        }
    }

    private static class DownloadedMedia {
        final byte[] bytes;
        final String url;

        DownloadedMedia(byte[] bytes, String url) {
            this.bytes = bytes;
            this.url = url;
        }
    }

    static class ViewerMedia {
        final String url;
        final byte[] bytes;
        final Bitmap bitmap;
        final Drawable drawable;

        ViewerMedia(String url, byte[] bytes, Bitmap bitmap, Drawable drawable) {
            this.url = url;
            this.bytes = bytes;
            this.bitmap = bitmap;
            this.drawable = drawable;
        }
    }
}
