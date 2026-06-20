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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

final class CuspiDroidBackup {
    private static final int FORMAT_VERSION = 1;
    private static final String MANIFEST = "manifest.json";
    private static final String PREFERENCES = "preferences.json";

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
            writeRawPreferenceJson(zip, "bookmarks.json", preferences, MainActivity.PREF_THREAD_BOOKMARKS, "[]");
            writeRawPreferenceJson(zip, "history.json", preferences, MainActivity.PREF_HISTORY, "[]");
            writeRawPreferenceJson(zip, "read_posts.json", preferences, MainActivity.PREF_READ_POSTS, "{}");
            writeRawPreferenceJson(zip, "tabs.json", preferences, MainActivity.PREF_TABS, "");
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
        return new Result(restored);
    }

    private static JSONObject manifestJson() throws Exception {
        JSONObject object = new JSONObject();
        object.put("app", "CuspiDroid");
        object.put("format", "cuspidroid-backup");
        object.put("formatVersion", FORMAT_VERSION);
        object.put("createdAt", System.currentTimeMillis());
        object.put("preferencesEntry", PREFERENCES);
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
