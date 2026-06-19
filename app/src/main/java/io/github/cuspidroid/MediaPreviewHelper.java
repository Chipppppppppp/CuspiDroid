package io.github.cuspidroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

final class MediaPreviewHelper {
    interface Callback {
        void openImage(String originalUrl, String mediaUrl);
        void openVideo(String originalUrl, String mediaUrl);
        void openExternal(String url);
    }

    private MediaPreviewHelper() {
    }

    static View create(Activity activity, SharedPreferences preferences, Executor executor, Handler mainHandler,
                       String originalUrl, String mediaUrl, boolean video, int cellSize,
                       Runnable longClickAction, Callback callback) {
        FrameLayout frame = new FrameLayout(activity);
        frame.setClickable(true);
        frame.setBackgroundColor(video ? Color.BLACK : mediaBackground(activity));
        frame.setMinimumWidth(cellSize);
        frame.setMinimumHeight(cellSize);
        if (longClickAction != null) {
            frame.setOnLongClickListener(v -> {
                longClickAction.run();
                return true;
            });
        }

        ImageView image = new ImageView(activity);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setVisibility(View.GONE);
        image.setOnClickListener(v -> {
            if (video) {
                callback.openVideo(originalUrl, mediaUrl);
            } else {
                callback.openImage(originalUrl, mediaUrl);
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
            if (video) {
                callback.openVideo(originalUrl, mediaUrl);
            } else {
                callback.openImage(originalUrl, mediaUrl);
            }
        });
        frame.addView(play, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        frame.setOnClickListener(v -> {
            if (video) {
                callback.openVideo(originalUrl, mediaUrl);
            } else {
                callback.openImage(originalUrl, mediaUrl);
            }
        });

        executor.execute(() -> {
            Bitmap bitmap = null;
            Drawable drawable = null;
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
                } else if (gif && !autoplayGifs(preferences)) {
                    byte[] cached = AppCache.read(activity, preferences, "media", "gif-poster:" + mediaUrl, ".png");
                    if (cached != null) {
                        bitmap = BitmapFactory.decodeByteArray(cached, 0, cached.length);
                    }
                    if (bitmap == null) {
                        bitmap = videoPosterBitmap(mediaUrl);
                        if (bitmap != null) {
                            byte[] bytes = bitmapToPng(bitmap);
                            AppCache.write(activity, preferences, "media", "gif-poster:" + mediaUrl, ".png", bytes);
                        }
                    }
                } else {
                    byte[] bytes = AppCache.read(activity, preferences, "media", "image:" + mediaUrl,
                            gif ? ".gif" : ".img");
                    if (bytes == null) {
                        bytes = downloadBytes(mediaUrl, gif ? 16 * 1024 * 1024 : 4 * 1024 * 1024);
                        AppCache.write(activity, preferences, "media", "image:" + mediaUrl,
                                gif ? ".gif" : ".img", bytes);
                    }
                    if (isGifUrl(mediaUrl) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        drawable = ImageDecoder.decodeDrawable(ImageDecoder.createSource(ByteBuffer.wrap(bytes)));
                        if (drawable instanceof AnimatedImageDrawable) {
                            ((AnimatedImageDrawable) drawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
                        }
                    }
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                }
            } catch (Exception ignored) {
                bitmap = null;
                drawable = null;
            }
            Bitmap finalBitmap = bitmap;
            Drawable finalDrawable = drawable;
            boolean gif = isGifUrl(mediaUrl);
            mainHandler.post(() -> {
                if (!frame.isAttachedToWindow()) {
                    return;
                }
                spinner.setVisibility(View.GONE);
                if (finalDrawable != null && gif && autoplayGifs(preferences)) {
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
            });
        });
        return frame;
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

    private static byte[] downloadBytes(String url, int limit) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "CuspiDroid/0.1");
        try (InputStream input = connection.getInputStream()) {
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
        } finally {
            connection.disconnect();
        }
    }

    private static Bitmap videoPosterBitmap(String videoUrl) {
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
}
