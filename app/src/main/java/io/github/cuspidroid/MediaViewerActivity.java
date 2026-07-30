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
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Full-screen thread media gallery with zoom, swipe navigation and sensitive-media gating. */
public class MediaViewerActivity extends Activity {
    private static final String EXTRA_ORIGINAL_URLS = "original_urls";
    private static final String EXTRA_MEDIA_URLS = "media_urls";
    private static final String EXTRA_VIDEOS = "videos";
    private static final String EXTRA_SENSITIVE = "sensitive";
    private static final String EXTRA_INDEX = "index";

    static final class MediaItem {
        final String originalUrl;
        final String mediaUrl;
        final boolean video;
        final boolean sensitive;

        MediaItem(String originalUrl, String mediaUrl, boolean video, boolean sensitive) {
            this.originalUrl = originalUrl;
            this.mediaUrl = mediaUrl;
            this.video = video;
            this.sensitive = sensitive;
        }
    }

    private ExecutorService executor;
    private FrameLayout root;
    private final List<MediaItem> items = new ArrayList<>();
    private final List<Integer> revealed = new ArrayList<>();
    private int index;
    private int loadGeneration;
    private ZoomImageView currentZoom;
    private float gestureDownX;
    private float gestureDownY;
    private boolean gestureMultiTouch;
    private boolean gestureSwiping;
    private boolean swipeAnimating;
    private int touchSlop;

    static void open(Context context, String originalUrl, String mediaUrl, boolean video) {
        List<MediaItem> items = new ArrayList<>();
        items.add(new MediaItem(originalUrl, mediaUrl, video, false));
        open(context, items, 0);
    }

    static void open(Context context, List<MediaItem> mediaItems, int selectedIndex) {
        if (mediaItems == null || mediaItems.isEmpty()) {
            return;
        }
        ArrayList<String> originals = new ArrayList<>();
        ArrayList<String> media = new ArrayList<>();
        boolean[] videos = new boolean[mediaItems.size()];
        boolean[] sensitive = new boolean[mediaItems.size()];
        for (int i = 0; i < mediaItems.size(); i++) {
            MediaItem item = mediaItems.get(i);
            originals.add(item.originalUrl);
            media.add(item.mediaUrl);
            videos[i] = item.video;
            sensitive[i] = item.sensitive;
        }
        Intent intent = new Intent(context, MediaViewerActivity.class);
        intent.putStringArrayListExtra(EXTRA_ORIGINAL_URLS, originals);
        intent.putStringArrayListExtra(EXTRA_MEDIA_URLS, media);
        intent.putExtra(EXTRA_VIDEOS, videos);
        intent.putExtra(EXTRA_SENSITIVE, sensitive);
        intent.putExtra(EXTRA_INDEX, Math.max(0, Math.min(selectedIndex, mediaItems.size() - 1)));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        executor = Executors.newSingleThreadExecutor();
        touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        readItems();
        if (items.isEmpty()) {
            finish();
            return;
        }
        index = Math.max(0, Math.min(getIntent().getIntExtra(EXTRA_INDEX, 0), items.size() - 1));
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        root.setClickable(true);
        setContentView(root);
        showCurrent();
    }

    private void readItems() {
        ArrayList<String> originals = getIntent().getStringArrayListExtra(EXTRA_ORIGINAL_URLS);
        ArrayList<String> media = getIntent().getStringArrayListExtra(EXTRA_MEDIA_URLS);
        boolean[] videos = getIntent().getBooleanArrayExtra(EXTRA_VIDEOS);
        boolean[] sensitive = getIntent().getBooleanArrayExtra(EXTRA_SENSITIVE);
        if (media == null) {
            return;
        }
        for (int i = 0; i < media.size(); i++) {
            String mediaUrl = media.get(i);
            if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
                continue;
            }
            String originalUrl = originals != null && i < originals.size()
                    ? originals.get(i) : mediaUrl;
            items.add(new MediaItem(originalUrl, mediaUrl,
                    videos != null && i < videos.length && videos[i],
                    sensitive != null && i < sensitive.length && sensitive[i]));
        }
    }

    @Override
    protected void onDestroy() {
        loadGeneration++;
        if (executor != null) {
            executor.shutdownNow();
        }
        super.onDestroy();
    }

    private void showCurrent() {
        int generation = ++loadGeneration;
        root.animate().cancel();
        root.setTranslationX(0f);
        currentZoom = null;
        root.removeAllViews();
        MediaItem item = items.get(index);
        if (item.video) {
            showVideo(item, generation);
        } else {
            showImage(item, generation);
        }
        addViewerActions(item);
        addPositionIndicator();
        root.setAlpha(0.7f);
        root.animate().alpha(1f).setDuration(120).start();
    }

    private void showImage(MediaItem item, int generation) {
        ZoomImageView image = new ZoomImageView(this);
        currentZoom = image;
        root.addView(image, matchParentParams());
        ProgressBar spinner = spinner();
        TextView play = playOverlay();

        executor.execute(() -> {
            MediaPreviewHelper.ViewerMedia loaded = null;
            try {
                loaded = MediaPreviewHelper.loadForViewer(item.mediaUrl,
                        getResources().getDisplayMetrics().widthPixels * 3,
                        getResources().getDisplayMetrics().heightPixels * 3);
            } catch (Exception ignored) {
            }
            MediaPreviewHelper.ViewerMedia result = loaded;
            boolean sensitive = item.sensitive;
            if (result != null && result.bitmap != null
                    && shouldCheckWithModel(item)) {
                Boolean cached = MediaPreviewHelper.readSensitive(
                        getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE),
                        item.mediaUrl);
                boolean modelSensitive = cached != null ? cached
                        : SensitiveImageClassifier.get(this)
                                .isSensitive(result.bitmap);
                if (cached == null) {
                    MediaPreviewHelper.saveSensitive(
                            getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE),
                            item.mediaUrl, modelSensitive);
                }
                sensitive = sensitive || modelSensitive;
            }
            boolean finalSensitive = sensitive;
            runOnUiThread(() -> {
                if (!isCurrent(generation)) {
                    return;
                }
                spinner.setVisibility(View.GONE);
                if (result == null || (result.bitmap == null && result.drawable == null)) {
                    loadFailed(MainActivity.text("\u753b\u50cf\u3092\u8868\u793a\u3067\u304d\u307e\u305b\u3093",
                            "Image failed to load."));
                    return;
                }
                if (finalSensitive && result.bitmap != null
                        && !revealed.contains(index)) {
                    image.setImageBitmap(MediaPreviewHelper.blurredBitmap(result.bitmap));
                    addRevealButton(() -> displayImage(image, play, result));
                } else {
                    displayImage(image, play, result);
                }
            });
        });
    }

    private void displayImage(ZoomImageView image, TextView play,
                              MediaPreviewHelper.ViewerMedia result) {
        boolean animated = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                && result.drawable instanceof AnimatedImageDrawable;
        boolean autoplay = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
                .getBoolean(MainActivity.PREF_AUTOPLAY_GIFS, false);
        if (result.drawable != null && animated && !autoplay) {
            image.setImageBitmap(result.bitmap);
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
    }

    private void showVideo(MediaItem item, int generation) {
        ImageView poster = new ImageView(this);
        poster.setScaleType(ImageView.ScaleType.FIT_CENTER);
        poster.setBackgroundColor(Color.BLACK);
        root.addView(poster, matchParentParams());
        ProgressBar spinner = spinner();
        executor.execute(() -> {
            Bitmap bitmap = null;
            try {
                bitmap = MediaPreviewHelper.videoPosterBitmap(item.mediaUrl);
            } catch (Exception ignored) {
            }
            boolean sensitive = item.sensitive;
            if (bitmap != null && shouldCheckWithModel(item)) {
                Boolean cached = MediaPreviewHelper.readSensitive(
                        getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE),
                        item.mediaUrl);
                boolean modelSensitive = cached != null ? cached
                        : SensitiveImageClassifier.get(this).isSensitive(bitmap);
                if (cached == null) {
                    MediaPreviewHelper.saveSensitive(
                            getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE),
                            item.mediaUrl, modelSensitive);
                }
                sensitive = sensitive || modelSensitive;
            }
            Bitmap finalBitmap = bitmap;
            boolean finalSensitive = sensitive;
            runOnUiThread(() -> {
                if (!isCurrent(generation)) {
                    return;
                }
                spinner.setVisibility(View.GONE);
                if (finalSensitive && !revealed.contains(index)) {
                    if (finalBitmap != null) {
                        poster.setImageBitmap(MediaPreviewHelper.blurredBitmap(finalBitmap));
                    }
                    addRevealButton(() -> startVideo(item, generation));
                } else {
                    startVideo(item, generation);
                }
            });
        });
    }

    private void startVideo(MediaItem item, int generation) {
        if (!isCurrent(generation)) {
            return;
        }
        root.removeAllViews();
        VideoView video = new VideoView(this);
        video.setVideoURI(Uri.parse(item.mediaUrl));
        root.addView(video, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        MediaController controller = new MediaController(this);
        controller.setAnchorView(video);
        video.setMediaController(controller);
        ProgressBar spinner = spinner();
        addViewerActions(item);
        addPositionIndicator();
        video.setOnPreparedListener(player -> {
            spinner.setVisibility(View.GONE);
            centerVideo(video, player.getVideoWidth(), player.getVideoHeight());
            video.start();
            controller.show();
        });
        video.setOnErrorListener((player, what, extra) -> {
            spinner.setVisibility(View.GONE);
            loadFailed(MainActivity.text("\u52d5\u753b\u3092\u8868\u793a\u3067\u304d\u307e\u305b\u3093",
                    "Video failed to load."));
            return true;
        });
    }

    private void addRevealButton(Runnable revealAction) {
        Button reveal = new Button(this);
        reveal.setText(MainActivity.text("\u95b2\u89a7\u6ce8\u610f", "Sensitive"));
        reveal.setTextSize(15);
        reveal.setTextColor(Color.WHITE);
        reveal.setBackgroundColor(Color.argb(190, 15, 23, 42));
        reveal.setMinWidth(0);
        reveal.setMinHeight(0);
        reveal.setPadding(dp(16), 0, dp(16), 0);
        reveal.setOnClickListener(v -> {
            if (!revealed.contains(index)) {
                revealed.add(index);
            }
            root.removeView(reveal);
            revealAction.run();
        });
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                dp(156), dp(52), Gravity.CENTER);
        root.addView(reveal, params);
        reveal.setElevation(dp(12));
        reveal.bringToFront();
    }

    private boolean shouldCheckWithModel(MediaItem item) {
        android.content.SharedPreferences preferences = getSharedPreferences(
                MainActivity.PREFS_NAME, MODE_PRIVATE);
        if (!preferences.getBoolean(MainActivity.PREF_BLUR_IMGUR, true)) {
            return false;
        }
        if (item.video && !preferences.getBoolean(
                MainActivity.PREF_BLUR_VIDEO_THUMBNAILS, true)) {
            return false;
        }
        return !isGifUrl(item.mediaUrl) || preferences.getBoolean(
                MainActivity.PREF_BLUR_GIF_THUMBNAILS, true);
    }

    private boolean isGifUrl(String url) {
        String lower = url == null ? "" : url.toLowerCase(Locale.ROOT);
        return lower.endsWith(".gif") || lower.contains(".gif?");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (swipeAnimating) {
            return true;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                gestureDownX = event.getX();
                gestureDownY = event.getY();
                gestureMultiTouch = false;
                gestureSwiping = false;
                root.animate().cancel();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                gestureMultiTouch = true;
                gestureSwiping = false;
                root.animate().translationX(0f).setDuration(120).start();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX() - gestureDownX;
                float moveY = event.getY() - gestureDownY;
                boolean canDrag = currentZoom == null || currentZoom.atMinimumScale();
                if (!gestureMultiTouch && canDrag
                        && (gestureSwiping || (Math.abs(moveX) > touchSlop
                        && Math.abs(moveX) > Math.abs(moveY) * 1.2f))) {
                    gestureSwiping = true;
                    boolean atEdge = (moveX > 0 && index == 0)
                            || (moveX < 0 && index == items.size() - 1);
                    root.setTranslationX(atEdge ? moveX * 0.28f : moveX);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                float dx = event.getX() - gestureDownX;
                if (gestureSwiping) {
                    int direction = dx < 0 ? 1 : -1;
                    boolean canMove = direction > 0 ? index + 1 < items.size() : index > 0;
                    if (canMove && Math.abs(dx) >= Math.max(dp(56), root.getWidth() * 0.16f)) {
                        animateToItem(direction);
                    } else {
                        root.animate().translationX(0f).setDuration(160).start();
                    }
                    gestureSwiping = false;
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (gestureSwiping) {
                    root.animate().translationX(0f).setDuration(160).start();
                    gestureSwiping = false;
                    return true;
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private void animateToItem(int direction) {
        float width = Math.max(1, root.getWidth());
        swipeAnimating = true;
        root.animate()
                .translationX(direction > 0 ? -width : width)
                .setDuration(160)
                .withEndAction(() -> {
                    index += direction;
                    showCurrent();
                    root.setTranslationX(direction > 0 ? width : -width);
                    root.animate().translationX(0f).setDuration(180)
                            .withEndAction(() -> swipeAnimating = false)
                            .start();
                })
                .start();
    }

    private boolean isCurrent(int generation) {
        return generation == loadGeneration && !isFinishing() && !isDestroyed();
    }

    private ProgressBar spinner() {
        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        root.addView(spinner, new FrameLayout.LayoutParams(dp(44), dp(44), Gravity.CENTER));
        return spinner;
    }

    private TextView playOverlay() {
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

    private void addPositionIndicator() {
        if (items.size() <= 1) {
            return;
        }
        TextView indicator = new TextView(this);
        indicator.setText((index + 1) + " / " + items.size());
        indicator.setTextColor(Color.WHITE);
        indicator.setTextSize(14);
        indicator.setGravity(Gravity.CENTER);
        indicator.setBackgroundColor(Color.argb(110, 0, 0, 0));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(72), dp(36));
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.setMargins(dp(14), dp(24), 0, 0);
        root.addView(indicator, params);
    }

    private void addViewerActions(MediaItem item) {
        addAction(R.drawable.ic_close, MainActivity.text("\u9589\u3058\u308b", "Close"),
                14, v -> finish());
        addAction(R.drawable.ic_arrow_forward,
                MainActivity.text("\u30e1\u30c7\u30a3\u30a2\u30ea\u30f3\u30af\u3092\u958b\u304f", "Open media link"),
                68, v -> openExternal(item.originalUrl));
        addAction(R.drawable.ic_download,
                MainActivity.text("\u30e1\u30c7\u30a3\u30a2\u3092\u4fdd\u5b58", "Download media"),
                122, v -> saveMedia(item.mediaUrl, item.video));
    }

    private void addAction(int icon, String description, int rightMargin,
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
            Toast.makeText(this, MainActivity.text("\u30ea\u30f3\u30af\u3092\u958b\u3051\u307e\u305b\u3093",
                    "Cannot open link."), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMedia(String mediaUrl, boolean video) {
        Toast.makeText(this, MainActivity.text("\u30e1\u30c7\u30a3\u30a2\u3092\u4fdd\u5b58\u4e2d",
                "Saving media..."), Toast.LENGTH_SHORT).show();
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
                error = exception.getMessage();
            }
            String finalName = savedName;
            String finalError = error;
            runOnUiThread(() -> Toast.makeText(this,
                    finalError == null
                            ? MainActivity.text("\u30e1\u30c7\u30a3\u30a2\u3092\u4fdd\u5b58\u3057\u307e\u3057\u305f",
                                    "Media saved.") + "\n" + finalName
                            : MainActivity.text("\u4fdd\u5b58\u306b\u5931\u6557\u3057\u307e\u3057\u305f",
                                    "Save failed.") + (finalError == null ? "" : ": " + finalError),
                    finalError == null ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show());
        });
    }

    private void writeMedia(String name, String mime, byte[] bytes, boolean video) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mime);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    (video ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES)
                            + "/CuspiDroid");
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
        File directory = getExternalFilesDir(
                video ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES);
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
        String extension = mime == null ? "bin"
                : mime.toLowerCase(Locale.ROOT).replace("image/", "").replace("video/", "");
        return "jpeg".equals(extension) ? "cuspidroid-" + System.currentTimeMillis() + ".jpg"
                : "cuspidroid-" + System.currentTimeMillis() + "." + extension;
    }

    private void centerVideo(VideoView video, int videoWidth, int videoHeight) {
        if (videoWidth <= 0 || videoHeight <= 0) {
            return;
        }
        int availableWidth = getResources().getDisplayMetrics().widthPixels;
        int availableHeight = getResources().getDisplayMetrics().heightPixels;
        float scale = Math.min(availableWidth / (float) videoWidth,
                availableHeight / (float) videoHeight);
        video.setLayoutParams(new FrameLayout.LayoutParams(
                Math.max(1, Math.round(videoWidth * scale)),
                Math.max(1, Math.round(videoHeight * scale)), Gravity.CENTER));
    }

    private void loadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
            scaleDetector = new ScaleGestureDetector(context,
                    new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        @Override
                        public boolean onScale(ScaleGestureDetector detector) {
                            float next = Math.max(minScale,
                                    Math.min(minScale * 5f, scale * detector.getScaleFactor()));
                            float factor = next / scale;
                            scale = next;
                            matrix.postScale(factor, factor,
                                    detector.getFocusX(), detector.getFocusY());
                            constrain();
                            setImageMatrix(matrix);
                            return true;
                        }
                    });
        }

        boolean atMinimumScale() {
            return scale <= minScale * 1.01f;
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
            matrix.postTranslate((getWidth() - width * fit) / 2f,
                    (getHeight() - height * fit) / 2f);
            setImageMatrix(matrix);
        }

        private void constrain() {
            if (getDrawable() == null) {
                return;
            }
            android.graphics.RectF rect = new android.graphics.RectF(
                    0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
            matrix.mapRect(rect);
            float dx = rect.width() <= getWidth()
                    ? (getWidth() - rect.width()) / 2f - rect.left
                    : rect.left > 0 ? -rect.left
                    : rect.right < getWidth() ? getWidth() - rect.right : 0;
            float dy = rect.height() <= getHeight()
                    ? (getHeight() - rect.height()) / 2f - rect.top
                    : rect.top > 0 ? -rect.top
                    : rect.bottom < getHeight() ? getHeight() - rect.bottom : 0;
            matrix.postTranslate(dx, dy);
        }
    }
}
