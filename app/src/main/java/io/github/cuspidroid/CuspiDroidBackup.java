package io.github.cuspidroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

final class CuspiDroidBackup {
    private static final int FORMAT_VERSION = 3;
    private static final String MANIFEST = "manifest.json";
    private static final String PREFERENCES = "preferences.json";
    private static final String THEMES_DIRECTORY = "themes/";

    private CuspiDroidBackup() {
    }

    static void exportBackup(Context context, SharedPreferences preferences, Uri uri) throws Exception {
        JSONObject prefs = preferencesJson(preferences);
        try (OutputStream raw = context.getContentResolver().openOutputStream(uri);
             ZipOutputStream zip = raw == null ? null : new ZipOutputStream(raw)) {
            if (zip == null) {
                throw new IllegalStateException(MainActivity.text(
                        "\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u30d5\u30a1\u30a4\u30eb\u3092\u958b\u3051\u307e\u305b\u3093\u3002",
                        "Could not open the backup file."));
            }
            writeJson(zip, MANIFEST, manifestJson());
            writeJson(zip, PREFERENCES, prefs);
            writeThemeFiles(zip, Theme.customPalettes(context));
            writeRawPreferenceJson(zip, "bookmarks.json", preferences, MainActivity.PREF_THREAD_BOOKMARKS, "[]");
            writeRawPreferenceJson(zip, "history.json", preferences, MainActivity.PREF_HISTORY, "[]");
            writeRawPreferenceJson(zip, "read_posts.json", preferences, MainActivity.PREF_READ_POSTS, "{}");
            writeRawPreferenceJson(zip, "tabs.json", preferences, MainActivity.PREF_TABS, "");
            writeRawPreferenceJson(zip, "upload_history.json", preferences, MainActivity.PREF_IMGBB_UPLOADS, "[]");
            writeRawPreferenceJson(zip, "write_identity_history.json", preferences, MainActivity.PREF_WRITE_IDENTITY_HISTORY, "[]");
            writeRawPreferenceJson(zip, "favorite_post_categories.json", preferences,
                    FavoritePostsStore.PREF_CATEGORIES, "[]");
            writeRawPreferenceJson(zip, "favorite_posts.json", preferences,
                    FavoritePostsStore.PREF_POSTS, "[]");
            writeJson(zip, "prefs/cuspidroid_settings.json", prefs);
            writeRawPreferenceJson(zip, "files/bookmarks.json", preferences, MainActivity.PREF_THREAD_BOOKMARKS, "[]");
            writeRawPreferenceJson(zip, "files/history.json", preferences, MainActivity.PREF_HISTORY, "[]");
            writeRawPreferenceJson(zip, "files/readPosts.json", preferences, MainActivity.PREF_READ_POSTS, "{}");
            writeRawPreferenceJson(zip, "files/tabs.json", preferences, MainActivity.PREF_TABS, "");
            writeRawPreferenceJson(zip, "files/uploadHistory.json", preferences, MainActivity.PREF_IMGBB_UPLOADS, "[]");
            writeRawPreferenceJson(zip, "files/writeIdentityHistory.json", preferences, MainActivity.PREF_WRITE_IDENTITY_HISTORY, "[]");
            writeRawPreferenceJson(zip, "files/myPosts.json", preferences, MainActivity.PREF_MY_POSTS, "{}");
            writeRawPreferenceJson(zip, "files/favoritePostCategories.json", preferences,
                    FavoritePostsStore.PREF_CATEGORIES, "[]");
            writeRawPreferenceJson(zip, "files/favoritePosts.json", preferences,
                    FavoritePostsStore.PREF_POSTS, "[]");
            writePostDataList(zip, preferences);
            writeNgFiles(zip, preferences);
        }
    }

    static Result importBackup(Context context, SharedPreferences preferences, Uri uri) throws Exception {
        JSONObject prefs = readPreferences(context, uri);
        if (prefs == null) {
            throw new IllegalStateException(MainActivity.text(
                    "preferences.json \u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002CuspiDroid \u306e\u30d0\u30c3\u30af\u30a2\u30c3\u30d7zip\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002",
                    "preferences.json was not found. Select a CuspiDroid backup zip."));
        }
        SharedPreferences.Editor editor = preferences.edit().clear();
        int restored = 0;
        JSONArray entries = prefs.optJSONArray("entries");
        if (entries != null) {
            for (int i = 0; i < entries.length(); i++) {
                JSONObject entry = entries.optJSONObject(i);
                if (entry == null) {
                    continue;
                }
                String key = entry.optString("key", "");
                String type = entry.optString("type", "");
                if (key.isEmpty()) {
                    continue;
                }
                if ("boolean".equals(type)) {
                    editor.putBoolean(key, entry.optBoolean("value", false));
                } else if ("int".equals(type)) {
                    editor.putInt(key, entry.optInt("value", 0));
                } else if ("long".equals(type)) {
                    editor.putLong(key, entry.optLong("value", 0L));
                } else if ("float".equals(type)) {
                    editor.putFloat(key, (float) entry.optDouble("value", 0));
                } else if ("string_set".equals(type)) {
                    java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
                    JSONArray values = entry.optJSONArray("value");
                    if (values != null) {
                        for (int j = 0; j < values.length(); j++) {
                            set.add(values.optString(j, ""));
                        }
                    }
                    editor.putStringSet(key, set);
                } else {
                    editor.putString(key, entry.optString("value", ""));
                }
                restored++;
            }
        } else {
            java.util.Iterator<String> keys = prefs.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                editor.putString(key, prefs.optString(key, ""));
                restored++;
            }
        }
        editor.apply();
        // Page payloads are caches and are intentionally not part of a backup.
        new TabPayloadStore(context).clear();
        return new Result(restored);
    }

    private static JSONObject manifestJson() throws Exception {
        JSONObject object = new JSONObject();
        object.put("app", "CuspiDroid");
        object.put("format", "cuspidroid-backup");
        object.put("formatVersion", FORMAT_VERSION);
        object.put("createdAt", System.currentTimeMillis());
        object.put("preferencesEntry", PREFERENCES);
        object.put("themesDirectory", THEMES_DIRECTORY);
        return object;
    }

    private static JSONObject preferencesJson(SharedPreferences preferences) throws Exception {
        JSONObject root = new JSONObject();
        root.put("format", "cuspidroid-preferences");
        root.put("formatVersion", FORMAT_VERSION);
        JSONArray entries = new JSONArray();
        Map<String, ?> sorted = new TreeMap<>(preferences.getAll());
        for (Map.Entry<String, ?> item : sorted.entrySet()) {
            JSONObject entry = new JSONObject();
            entry.put("key", item.getKey());
            Object value = item.getValue();
            if (value instanceof Boolean) {
                entry.put("type", "boolean");
                entry.put("value", value);
            } else if (value instanceof Integer) {
                entry.put("type", "int");
                entry.put("value", value);
            } else if (value instanceof Long) {
                entry.put("type", "long");
                entry.put("value", value);
            } else if (value instanceof Float) {
                entry.put("type", "float");
                entry.put("value", ((Float) value).doubleValue());
            } else if (value instanceof Set) {
                entry.put("type", "string_set");
                JSONArray values = new JSONArray();
                for (Object setValue : (Set<?>) value) {
                    values.put(String.valueOf(setValue));
                }
                entry.put("value", values);
            } else {
                entry.put("type", "string");
                entry.put("value", value == null ? "" : String.valueOf(value));
            }
            entries.put(entry);
        }
        root.put("entries", entries);
        return root;
    }

    private static void writeRawPreferenceJson(ZipOutputStream zip, String name,
                                               SharedPreferences preferences, String key, String fallback) throws Exception {
        String raw = preferences.getString(key, fallback);
        if (raw == null || raw.isEmpty()) {
            raw = fallback;
        }
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        zip.write(raw.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static void writeJson(ZipOutputStream zip, String name, JSONObject object) throws Exception {
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        zip.write(object.toString(2).getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static void writePostDataList(ZipOutputStream zip, SharedPreferences preferences) throws Exception {
        JSONObject source = new JSONObject(preferences.getString(MainActivity.PREF_MY_POSTS, "{}"));
        JSONArray array = new JSONArray();
        java.util.Iterator<String> urls = source.keys();
        while (urls.hasNext()) {
            String url = urls.next();
            JSONArray hashes = source.optJSONArray(url);
            if (hashes == null) {
                continue;
            }
            for (int i = 0; i < hashes.length(); i++) {
                Object value = hashes.opt(i);
                String hash = myPostHash(value);
                if (hash.isEmpty()) {
                    continue;
                }
                JSONObject item = new JSONObject();
                item.put("url", url);
                item.put("body", myPostBody(value));
                item.put("bodyHash", hash);
                item.put("posted", myPostPostedAt(value));
                item.put("number", myPostNumber(value));
                item.put("title", myPostTitle(value));
                item.put("targetTitle", myPostTitle(value));
                array.put(item);
            }
        }
        ZipEntry entry = new ZipEntry("files/postDataList.json");
        zip.putNextEntry(entry);
        zip.write(array.toString(2).getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static void writeThemeFiles(ZipOutputStream zip, List<Theme.Palette> palettes) throws Exception {
        for (int i = 0; i < palettes.size(); i++) {
            Theme.Palette palette = palettes.get(i);
            String fileName = String.format(Locale.ROOT, "%03d-%s.cuspidroid-theme.json",
                    i + 1, safeThemeFileName(palette.name));
            writeJson(zip, THEMES_DIRECTORY + fileName, Theme.exportJson(palette));
        }
    }

    private static String safeThemeFileName(String value) {
        String safe = value == null ? "" : value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return safe.isEmpty() ? "theme" : safe;
    }

    private static void writeNgFiles(ZipOutputStream zip, SharedPreferences preferences) throws Exception {
        Map<String, JSONArray> files = new TreeMap<>();
        for (MainActivity.ScopedNgRule rule : MainActivity.readNgRules(preferences)) {
            if (rule == null || rule.value == null || rule.value.trim().isEmpty() || rule.regex) {
                continue;
            }
            String name = chMateNgFileName(rule.category);
            if (name.isEmpty()) {
                continue;
            }
            JSONArray array = files.get(name);
            if (array == null) {
                array = new JSONArray();
                files.put(name, array);
            }
            JSONObject item = new JSONObject();
            item.put("w", rule.value.trim());
            item.put("f", MainActivity.chMateNgFlag(rule.mode));
            item.put("ct", rule.createdAt > 0 ? rule.createdAt : System.currentTimeMillis());
            String board = rule.boardName == null || rule.boardName.trim().isEmpty()
                    ? MainActivity.chMateBoardNameFromNgTarget(rule.targetUrl) : rule.boardName.trim();
            if (!board.isEmpty()) {
                item.put("b", board);
            }
            array.put(item);
        }
        for (Map.Entry<String, JSONArray> entry : files.entrySet()) {
            ZipEntry zipEntry = new ZipEntry("ng/" + entry.getKey());
            zip.putNextEntry(zipEntry);
            zip.write(entry.getValue().toString(2).getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
    }

    private static String chMateNgFileName(String category) {
        if ("NGID".equals(category)) return "_id.json";
        if ("NGName".equals(category)) return "_name.json";
        if ("NGWord".equals(category)) return "_word.json";
        if ("NGBe".equals(category)) return "_be.json";
        if ("NGThread".equals(category)) return "_thread.json";
        return "";
    }

    private static String myPostHash(Object value) {
        if (value instanceof JSONObject) {
            JSONObject object = (JSONObject) value;
            String hash = object.optString("hash", "").trim();
            if (hash.isEmpty()) {
                hash = object.optString("bodyHash", "").trim();
            }
            return hash;
        }
        return value instanceof String ? ((String) value).trim() : "";
    }

    private static String myPostBody(Object value) {
        return value instanceof JSONObject ? ((JSONObject) value).optString("body", "") : "";
    }

    private static String myPostTitle(Object value) {
        if (!(value instanceof JSONObject)) {
            return "";
        }
        JSONObject object = (JSONObject) value;
        String title = object.optString("title", "").trim();
        if (title.isEmpty()) {
            title = object.optString("targetTitle", "").trim();
        }
        return title;
    }

    private static int myPostNumber(Object value) {
        return value instanceof JSONObject ? ((JSONObject) value).optInt("number", 0) : 0;
    }

    private static long myPostPostedAt(Object value) {
        if (!(value instanceof JSONObject)) {
            return 0L;
        }
        JSONObject object = (JSONObject) value;
        long postedAt = object.optLong("postedAt", 0L);
        return postedAt > 0 ? postedAt : object.optLong("posted", 0L);
    }

    private static JSONObject readPreferences(Context context, Uri uri) throws Exception {
        try (InputStream raw = context.getContentResolver().openInputStream(uri);
             ZipInputStream zip = raw == null ? null : new ZipInputStream(raw)) {
            if (zip == null) {
                return null;
            }
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if (!entry.isDirectory() && name != null
                        && (PREFERENCES.equals(name) || name.endsWith("/" + PREFERENCES))) {
                    return new JSONObject(new String(readEntry(zip), StandardCharsets.UTF_8));
                }
                zip.closeEntry();
            }
        }
        return null;
    }

    private static byte[] readEntry(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[32 * 1024];
        int read;
        while ((read = input.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    static final class Result {
        final int restoredPreferences;

        Result(int restoredPreferences) {
            this.restoredPreferences = restoredPreferences;
        }
    }
}
