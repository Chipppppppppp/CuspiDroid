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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/** Home-screen widget showing total unread posts across normal and bookmark tabs. */
public final class UnreadWidgetProvider extends AppWidgetProvider {
    private static final int DEFAULT_SIZE_DP = 72;

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
        views.setImageViewBitmap(R.id.widget_icon,
                widgetIcon(context, manager.getAppWidgetOptions(appWidgetId), displayCount(unread)));

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

    private static Bitmap widgetIcon(Context context, Bundle options, String count) {
        float density = context.getResources().getDisplayMetrics().density;
        int widthDp = option(options, AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
                AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, DEFAULT_SIZE_DP);
        int heightDp = option(options, AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
                AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, DEFAULT_SIZE_DP);
        int size = Math.max(1, Math.min(512, Math.round(Math.min(widthDp, heightDp) * density)));
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable icon = context.getDrawable(R.drawable.ic_launcher_foreground);
        if (icon != null) {
            int overscan = Math.round(size * 0.30f);
            icon.setBounds(-overscan, -overscan, size + overscan, size + overscan);
            icon.draw(canvas);
        }

        float badgeWidth = size * 0.48f;
        float badgeHeight = size * 0.25f;
        float centerX = size * 0.50f;
        float centerY = size * 0.75f;
        RectF badge = new RectF(centerX - badgeWidth / 2f, centerY - badgeHeight / 2f,
                centerX + badgeWidth / 2f, centerY + badgeHeight / 2f);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Theme.accent(context));
        canvas.drawRoundRect(badge, badgeHeight / 2f, badgeHeight / 2f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, density * 0.75f));
        paint.setColor(Theme.surface(context));
        canvas.drawRoundRect(badge, badgeHeight / 2f, badgeHeight / 2f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Theme.contrastingText(Theme.accent(context)));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        float textSize = size * 0.15f;
        paint.setTextSize(textSize);
        float availableWidth = badgeWidth * 0.82f;
        while (paint.measureText(count) > availableWidth && textSize > size * 0.075f) {
            textSize -= size * 0.01f;
            paint.setTextSize(textSize);
        }
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float baseline = centerY - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(count, centerX, baseline, paint);
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

    static String displayCount(int unread) {
        return unread > 99999 ? "99K+" : String.valueOf(unread);
    }

    private static int totalUnread(SharedPreferences preferences) {
        long total = 0;
        try {
            JSONArray tabs = savedTabs(preferences.getString(MainActivity.PREF_TABS, ""));
            Map<String, Integer> normalTabResponses = new HashMap<>();
            for (int index = 0; index < tabs.length(); index++) {
                JSONObject tab = tabs.optJSONObject(index);
                if (tab == null || tab.optBoolean("privateBrowsing", false) || isBookmarkTab(tab)) {
                    continue;
                }
                boolean threadTab = "thread".equals(tab.optString("nativeKind", ""));
                if (threadTab && tab.optBoolean("hasThreadStats", false)) {
                    total += Math.max(0, tab.optInt("cachedUnreadCount", 0));
                    if (total >= Integer.MAX_VALUE) {
                        return Integer.MAX_VALUE;
                    }
                }
                if (threadTab) {
                    String identity = threadIdentity(tab.optString("url", ""));
                    int responses = Math.max(tab.optInt("knownMaxPostNumber", 0),
                            tab.optInt("knownPostCount", 0));
                    if (!identity.isEmpty() && responses > 0) {
                        normalTabResponses.put(identity,
                                Math.max(responses, normalTabResponses.getOrDefault(identity, 0)));
                    }
                }
            }

            JSONArray bookmarks = jsonArray(preferences.getString(
                    MainActivity.PREF_THREAD_BOOKMARKS, "[]"));
            JSONObject statuses = jsonObject(preferences.getString(
                    MainActivity.PREF_BOOKMARK_OVERVIEW_STATUS, "{}"));
            boolean readHistoryEnabled = !preferences.getBoolean(MainActivity.PREF_DISABLE_HISTORY, false)
                    && preferences.getBoolean(MainActivity.PREF_SAVE_READ_HISTORY, true);
            JSONObject readPosts = readHistoryEnabled
                    ? jsonObject(preferences.getString(MainActivity.PREF_READ_POSTS, "{}"))
                    : new JSONObject();
            for (int index = 0; index < bookmarks.length(); index++) {
                JSONObject bookmark = bookmarks.optJSONObject(index);
                if (bookmark == null) {
                    continue;
                }
                String url = bookmark.optString("url", "").trim();
                if (!isThreadUrl(url)) {
                    continue;
                }
                String statusKey = trimTrailingSlashes(normalizeUrl(url));
                JSONObject status = statuses.optJSONObject(statusKey);
                if (status == null) {
                    status = statuses.optJSONObject(url);
                }
                int responseCount = status == null ? 0 : Math.max(0, status.optInt("responseCount", 0));
                responseCount = Math.max(responseCount,
                        normalTabResponses.getOrDefault(threadIdentity(url), 0));
                int read = Math.max(0, readPosts.optInt(url, 0));
                total += Math.max(0, responseCount - read);
                if (total >= Integer.MAX_VALUE) {
                    return Integer.MAX_VALUE;
                }
            }
        } catch (Exception ignored) {
            return 0;
        }
        return total >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;
    }

    private static JSONArray savedTabs(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new JSONArray();
        }
        try {
            if (raw.trim().startsWith("{")) {
                JSONArray legacy = new JSONObject(raw).optJSONArray("tabs");
                return legacy == null ? new JSONArray() : legacy;
            }
            return new JSONArray(raw);
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    private static JSONArray jsonArray(String raw) {
        try {
            return new JSONArray(raw == null ? "[]" : raw);
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    private static JSONObject jsonObject(String raw) {
        try {
            return new JSONObject(raw == null ? "{}" : raw);
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    private static boolean isBookmarkTab(JSONObject tab) {
        String scope = tab.optString("tabScope", "");
        return "BOOKMARK".equals(scope)
                || (scope.isEmpty() && tab.optBoolean("bookmarkOverviewTab", false));
    }

    private static String threadIdentity(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "";
        }
        return trimTrailingSlashes(normalizeUrl(url));
    }

    static boolean isThreadUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            URI uri = new URI(normalizeUrl(url));
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(java.util.Locale.ROOT);
            String path = uri.getPath() == null ? "" : uri.getPath();
            String lowerPath = path.toLowerCase(java.util.Locale.ROOT);
            if (lowerPath.contains("/test/read.cgi/") || lowerPath.contains("/bbs/read.cgi/")) {
                return true;
            }
            if ((host.equals("2chan.net") || host.endsWith(".2chan.net"))
                    && lowerPath.matches("/[^/]+/res/\\d+\\.htm(?:/.*)?")) {
                return true;
            }
            String[] rawParts = path.split("/");
            int partCount = 0;
            String secondPart = "";
            for (String part : rawParts) {
                if (part.isEmpty()) {
                    continue;
                }
                if (partCount == 1) {
                    secondPart = part;
                }
                partCount++;
            }
            return partCount >= 2 && secondPart.matches("\\d{9,13}");
        } catch (Exception ignored) {
            return false;
        }
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
