package io.github.cuspidroid;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

/** Home-screen widget showing the total known unread posts in bookmarked threads. */
public final class UnreadWidgetProvider extends AppWidgetProvider {
    private static final int DEFAULT_WIDTH_DP = 220;
    private static final int DEFAULT_HEIGHT_DP = 96;

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            update(context, manager, appWidgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager,
                                          int appWidgetId, Bundle newOptions) {
        update(context, manager, appWidgetId);
    }

    static void updateAll(Context context) {
        if (context == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
        ComponentName provider = new ComponentName(appContext, UnreadWidgetProvider.class);
        int[] appWidgetIds = manager.getAppWidgetIds(provider);
        for (int appWidgetId : appWidgetIds) {
            update(appContext, manager, appWidgetId);
        }
    }

    private static void update(Context context, AppWidgetManager manager, int appWidgetId) {
        SharedPreferences preferences = context.getSharedPreferences(
                MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int unread = totalUnread(preferences);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_unread);
        views.setImageViewBitmap(R.id.widget_background,
                cardBackground(context, manager.getAppWidgetOptions(appWidgetId)));
        views.setTextViewText(R.id.widget_unread_count, displayCount(unread));
        views.setTextColor(R.id.widget_unread_count, Theme.text(context));
        views.setTextColor(R.id.widget_unread_label, Theme.muted(context));
        views.setViewVisibility(R.id.widget_unread_label, unread == 0 ? View.GONE : View.VISIBLE);
        views.setViewVisibility(R.id.widget_unread_zero, unread == 0 ? View.VISIBLE : View.GONE);
        views.setTextColor(R.id.widget_unread_zero, Theme.accent(context));

        String description = context.getString(R.string.widget_unread_description, unread);
        views.setContentDescription(R.id.widget_root, description);
        views.setOnClickPendingIntent(R.id.widget_root, launchIntent(context, appWidgetId));
        manager.updateAppWidget(appWidgetId, views);
    }

    private static PendingIntent launchIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, MainActivity.class)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static Bitmap cardBackground(Context context, Bundle options) {
        float density = context.getResources().getDisplayMetrics().density;
        int widthDp = option(options, AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
                AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, DEFAULT_WIDTH_DP);
        int heightDp = option(options, AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
                AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, DEFAULT_HEIGHT_DP);
        int width = Math.max(1, Math.min(1200, Math.round(widthDp * density)));
        int height = Math.max(1, Math.min(600, Math.round(heightDp * density)));
        float radius = Math.min(height / 2f, 22f * density);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        RectF bounds = new RectF(density, density, width - density, height - density);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Theme.surface(context));
        canvas.drawRoundRect(bounds, radius, radius, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, density));
        paint.setColor(Theme.border(context));
        canvas.drawRoundRect(bounds, radius, radius, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Theme.accent(context));
        RectF accent = new RectF(density, radius, 7f * density, height - radius);
        canvas.drawRoundRect(accent, 3f * density, 3f * density, paint);
        return bitmap;
    }

    private static int option(Bundle options, String preferred, String fallback, int defaultValue) {
        if (options == null) {
            return defaultValue;
        }
        int value = options.getInt(preferred, 0);
        if (value <= 0) {
            value = options.getInt(fallback, 0);
        }
        return value > 0 ? value : defaultValue;
    }

    private static String displayCount(int unread) {
        return unread > 9999 ? "9999+" : String.valueOf(unread);
    }

    private static int totalUnread(SharedPreferences preferences) {
        long total = 0;
        try {
            JSONArray bookmarks = new JSONArray(preferences.getString(
                    MainActivity.PREF_THREAD_BOOKMARKS, "[]"));
            JSONObject statuses = new JSONObject(preferences.getString(
                    MainActivity.PREF_BOOKMARK_OVERVIEW_STATUS, "{}"));
            boolean readHistoryEnabled = !preferences.getBoolean(MainActivity.PREF_DISABLE_HISTORY, false)
                    && preferences.getBoolean(MainActivity.PREF_SAVE_READ_HISTORY, true);
            JSONObject readPosts = readHistoryEnabled
                    ? new JSONObject(preferences.getString(MainActivity.PREF_READ_POSTS, "{}"))
                    : new JSONObject();
            for (int index = 0; index < bookmarks.length(); index++) {
                JSONObject bookmark = bookmarks.optJSONObject(index);
                if (bookmark == null) {
                    continue;
                }
                String url = bookmark.optString("url", "").trim();
                if (url.isEmpty()) {
                    continue;
                }
                String statusKey = trimTrailingSlashes(normalizeUrl(url));
                JSONObject status = statuses.optJSONObject(statusKey);
                if (status == null) {
                    status = statuses.optJSONObject(url);
                }
                int responseCount = status == null ? 0 : Math.max(0, status.optInt("responseCount", 0));
                int read = Math.max(0, readPosts.optInt(url, 0));
                total += Math.max(0, responseCount - read);
                if (total >= Integer.MAX_VALUE) {
                    return Integer.MAX_VALUE;
                }
            }
        } catch (Exception ignored) {
            return 0;
        }
        return (int) total;
    }

    private static String normalizeUrl(String value) {
        String normalized = value.trim();
        String lower = normalized.toLowerCase(java.util.Locale.ROOT);
        if (lower.startsWith("ttps://")) {
            normalized = "h" + normalized;
        } else if (lower.startsWith("ttp://")) {
            normalized = "h" + normalized;
        } else if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        return normalized;
    }

    private static String trimTrailingSlashes(String value) {
        while (value.endsWith("/") && value.length() > "https://x".length()) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
