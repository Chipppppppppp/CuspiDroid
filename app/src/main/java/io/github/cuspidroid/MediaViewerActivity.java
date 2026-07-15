package io.github.cuspidroid;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Shared full-screen viewer used by thread media and upload history media. */
public class MediaViewerActivity extends Activity {
    private static final String EXTRA_ORIGINAL_URL = "original_url";
    private static final String EXTRA_MEDIA_URL = "media_url";
    private static final String EXTRA_VIDEO = "video";

    private ExecutorService executor;

    static void open(Context context, String originalUrl, String mediaUrl, boolean video) {
        Intent intent = new Intent(context, MediaViewerActivity.class);
        intent.putExtra(EXTRA_ORIGINAL_URL, originalUrl);
        intent.putExtra(EXTRA_MEDIA_URL, mediaUrl);
        intent.putExtra(EXTRA_VIDEO, video);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        executor = Executors.newSingleThreadExecutor();
        String originalUrl = getIntent().getStringExtra(EXTRA_ORIGINAL_URL);
        String mediaUrl = getIntent().getStringExtra(EXTRA_MEDIA_URL);
        if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
            finish();
            return;
        }
        if (getIntent().getBooleanExtra(EXTRA_VIDEO, false)) {
            showVideo(originalUrl, mediaUrl);
        } else {
            showImage(originalUrl, mediaUrl);
        }
    }

    @Override
    protected void onDestroy() {
        if (executor != null) {
            executor.shutdownNow();
        }
        super.onDestroy();
    }

    private void showImage(String originalUrl, String mediaUrl) {
        FrameLayout root = viewerRoot();
        ZoomImageView image = new ZoomImageView(this);
        root.addView(image, matchParentParams());

        ProgressBar spinner = spinner(root);
        TextView play = playOverlay(root);
        addViewerActions(root, originalUrl, mediaUrl, false);

        executor.execute(() -> {
            MediaPreviewHelper.ViewerMedia loaded = null;
            try {
                loaded = MediaPreviewHelper.loadForViewer(mediaUrl,
                        getResources().getDisplayMetrics().widthPixels * 3,
                        getResources().getDisplayMetrics().heightPixels * 3);
            } catch (Exception ignored) {
            }
            MediaPreviewHelper.ViewerMedia result = loaded;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                spinner.setVisibility(View.GONE);
                if (result == null || (result.bitmap == null && result.drawable == null)) {
                    Toast.makeText(this, MainActivity.text("画像を表示できません", "Image failed to load."),
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                boolean animated = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                        && result.drawable instanceof AnimatedImageDrawable;
                boolean autoplay = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
                        .getBoolean(MainActivity.PREF_AUTOPLAY_GIFS, false);
                if (result.drawable != null && animated && !autoplay) {
                    if (result.bitmap != null) {
                        image.setImageBitmap(result.bitmap);
                    } else {
                        image.setImageDrawable(result.drawable);
                    }
                    play.setVisibility(View.VISIBLE);
                    play.setOnClickListener(v -> {
                        image.setImageDrawable(result.drawable);
                        ((AnimatedImageDrawable) result.drawable).start();
                        play.setVisibility(View.GONE);
                    });
                } else if (result.drawable != null) {
                    image.setImageDrawable(result.drawable);
                    if (animated) {
                        ((AnimatedImageDrawable) result.drawable).start();
                    }
                } else {
                    image.setImageBitmap(result.bitmap);
                }
            });
        });
    }

    private void showVideo(String originalUrl, String mediaUrl) {
        FrameLayout root = viewerRoot();
        VideoView video = new VideoView(this);
        video.setVideoURI(Uri.parse(mediaUrl));
        root.addView(video, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        MediaController controller = new MediaController(this);
        controller.setAnchorView(video);
        video.setMediaController(controller);
        ProgressBar spinner = spinner(root);
        addViewerActions(root, originalUrl, mediaUrl, true);

        video.setOnPreparedListener(player -> {
            spinner.setVisibility(View.GONE);
            centerVideo(video, player.getVideoWidth(), player.getVideoHeight());
            video.start();
            controller.show();
        });
        video.setOnErrorListener((player, what, extra) -> {
            spinner.setVisibility(View.GONE);
            Toast.makeText(this, MainActivity.text("動画を表示できません", "Video failed to load."),
                    Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private FrameLayout viewerRoot() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        root.setClickable(true);
        setContentView(root);
        return root;
    }

    private ProgressBar spinner(FrameLayout root) {
        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(44), dp(44), Gravity.CENTER);
        root.addView(spinner, params);
        return spinner;
    }

    private TextView playOverlay(FrameLayout root) {
        TextView play = new TextView(this);
        play.setText("\u25b6");
        play.setTextColor(Color.WHITE);
        play.setTextSize(34);
        play.setGravity(Gravity.CENTER);
        play.setBackgroundColor(Color.argb(82, 0, 0, 0));
        play.setVisibility(View.GONE);
        root.addView(play, matchParentParams());
        return play;
    }

    private void addViewerActions(FrameLayout root, String originalUrl, String mediaUrl, boolean video) {
        addAction(root, R.drawable.ic_close,
                MainActivity.text(video ? "動画を閉じる" : "画像を閉じる", video ? "Close video" : "Close image"),
                14, v -> finish());
        addAction(root, R.drawable.ic_arrow_forward,
                MainActivity.text(video ? "動画リンクを開く" : "画像リンクを開く",
                        video ? "Open video link" : "Open image link"),
                68, v -> openExternal(originalUrl));
        addAction(root, R.drawable.ic_download,
                MainActivity.text(video ? "動画を保存" : "画像を保存", video ? "Download video" : "Download image"),
                122, v -> saveMedia(mediaUrl, video));
    }

    private void addAction(FrameLayout root, int icon, String description, int rightMargin,
                           View.OnClickListener listener) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(icon);
        button.setContentDescription(description);
        button.setColorFilter(Color.WHITE);
        button.setBackgroundColor(Color.argb(80, 0, 0, 0));
        button.setPadding(dp(10), dp(10), dp(10), dp(10));
        button.setOnClickListener(listener);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(48), dp(48));
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        params.setMargins(0, dp(18), dp(rightMargin), 0);
        root.addView(button, params);
    }

    private void openExternal(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception ignored) {
            Toast.makeText(this, MainActivity.text("リンクを開けません", "Cannot open link."), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMedia(String mediaUrl, boolean video) {
        Toast.makeText(this, MainActivity.text(video ? "動画を保存中" : "画像を保存中",
                video ? "Saving video..." : "Saving image..."), Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            String savedName = null;
            String error = null;
            try {
                MediaPreviewHelper.ViewerMedia media = MediaPreviewHelper.loadForViewer(mediaUrl, 1, 1);
                String mime = URLConnection.guessContentTypeFromName(media.url);
                if (mime == null) {
                    mime = video ? "video/mp4" : "image/jpeg";
                }
                savedName = fileName(media.url, mime);
                writeMedia(savedName, mime, media.bytes, video);
            } catch (Exception exception) {
                error = exception.getMessage() == null
                        ? MainActivity.text("保存に失敗しました", "Save failed.")
                        : exception.getMessage();
            }
            String finalName = savedName;
            String finalError = error;
            runOnUiThread(() -> Toast.makeText(this,
                    finalError == null
                            ? MainActivity.text("メディアを保存しました", "Media saved.") + "\n" + finalName
                            : MainActivity.text("保存に失敗しました", "Save failed.")
                                    + (finalError == null ? "" : ": " + finalError),
                    finalError == null ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show());
        });
    }

    private void writeMedia(String name, String mime, byte[] bytes, boolean video) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mime);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    (video ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES) + "/CuspiDroid");
            Uri collection = video ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri destination = getContentResolver().insert(collection, values);
            if (destination == null) {
                throw new IllegalStateException("Could not create destination.");
            }
            try (OutputStream output = getContentResolver().openOutputStream(destination)) {
                if (output == null) {
                    throw new IllegalStateException("Could not open destination.");
                }
                output.write(bytes);
            }
            return;
        }
        File directory = getExternalFilesDir(video ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES);
        if (directory == null || (!directory.exists() && !directory.mkdirs())) {
            throw new IllegalStateException("Could not open destination.");
        }
        try (OutputStream output = new FileOutputStream(new File(directory, name))) {
            output.write(bytes);
        }
    }

    private String fileName(String url, String mime) {
        String path = Uri.parse(url).getLastPathSegment();
        if (path != null && !path.trim().isEmpty() && path.contains(".")) {
            return path.replaceAll("[^A-Za-z0-9._-]", "_");
        }
        String extension = mime == null ? "bin" : mime.toLowerCase(Locale.ROOT).replace("image/", "").replace("video/", "");
        if ("jpeg".equals(extension)) {
            extension = "jpg";
        }
        return "cuspidroid-" + System.currentTimeMillis() + "." + extension;
    }

    private void centerVideo(VideoView video, int videoWidth, int videoHeight) {
        if (videoWidth <= 0 || videoHeight <= 0) {
            return;
        }
        int availableWidth = getResources().getDisplayMetrics().widthPixels;
        int availableHeight = getResources().getDisplayMetrics().heightPixels;
        float scale = Math.min(availableWidth / (float) videoWidth, availableHeight / (float) videoHeight);
        video.setLayoutParams(new FrameLayout.LayoutParams(
                Math.max(1, Math.round(videoWidth * scale)),
                Math.max(1, Math.round(videoHeight * scale)), Gravity.CENTER));
    }

    private FrameLayout.LayoutParams matchParentParams() {
        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class ZoomImageView extends ImageView {
        private final Matrix matrix = new Matrix();
        private final ScaleGestureDetector scaleDetector;
        private float scale = 1f;
        private float minScale = 1f;
        private float lastX;
        private float lastY;
        private boolean dragging;

        ZoomImageView(Context context) {
            super(context);
            setScaleType(ScaleType.MATRIX);
            setBackgroundColor(Color.BLACK);
            scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    float next = Math.max(minScale, Math.min(minScale * 5f, scale * detector.getScaleFactor()));
                    float factor = next / scale;
                    scale = next;
                    matrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
                    constrain();
                    setImageMatrix(matrix);
                    return true;
                }
            });
        }

        @Override
        public void setImageBitmap(Bitmap bitmap) {
            super.setImageBitmap(bitmap);
            post(this::fitImage);
        }

        @Override
        public void setImageDrawable(Drawable drawable) {
            super.setImageDrawable(drawable);
            post(this::fitImage);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            post(this::fitImage);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            scaleDetector.onTouchEvent(event);
            if (event.getPointerCount() > 1) {
                dragging = false;
                return true;
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = event.getX();
                    lastY = event.getY();
                    dragging = true;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (dragging && scale > minScale) {
                        matrix.postTranslate(event.getX() - lastX, event.getY() - lastY);
                        constrain();
                        setImageMatrix(matrix);
                    }
                    lastX = event.getX();
                    lastY = event.getY();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dragging = false;
                    return true;
                default:
                    return true;
            }
        }

        private void fitImage() {
            if (getDrawable() == null || getWidth() <= 0 || getHeight() <= 0) {
                return;
            }
            int width = getDrawable().getIntrinsicWidth();
            int height = getDrawable().getIntrinsicHeight();
            if (width <= 0 || height <= 0) {
                return;
            }
            float fit = Math.min(getWidth() / (float) width, getHeight() / (float) height);
            minScale = fit;
            scale = fit;
            matrix.reset();
            matrix.postScale(fit, fit);
            matrix.postTranslate((getWidth() - width * fit) / 2f, (getHeight() - height * fit) / 2f);
            setImageMatrix(matrix);
        }

        private void constrain() {
            if (getDrawable() == null) {
                return;
            }
            android.graphics.RectF rect = new android.graphics.RectF(
                    0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
            matrix.mapRect(rect);
            float dx = rect.width() <= getWidth() ? (getWidth() - rect.width()) / 2f - rect.left
                    : rect.left > 0 ? -rect.left : rect.right < getWidth() ? getWidth() - rect.right : 0;
            float dy = rect.height() <= getHeight() ? (getHeight() - rect.height()) / 2f - rect.top
                    : rect.top > 0 ? -rect.top : rect.bottom < getHeight() ? getHeight() - rect.bottom : 0;
            matrix.postTranslate(dx, dy);
        }
    }
}
