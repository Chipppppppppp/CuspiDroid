package io.github.cuspidroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.DocumentsContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ChMateBackupImporter {
    private static final Charset MS932 = Charset.forName("MS932");
    private static final Pattern THREAD_URL = Pattern.compile("https?://([^/]+)/+(?:test|bbs)/read\\.cgi/+([^/]+)/+(\\d+)", Pattern.CASE_INSENSITIVE);

    private ChMateBackupImporter() {
    }

    static Result importBackup(Context context, SharedPreferences preferences, Uri uri) throws Exception {
        BackupFiles backupFiles = readBackupFiles(context, uri);
        if (backupFiles.database == null || !backupFiles.database.exists()) {
            throw new IllegalStateException(MainActivity.text(
                    "databases/roidon.sqlite\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002ChMate\u306e\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u30d5\u30a9\u30eb\u30c0\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002",
                    "databases/roidon.sqlite was not found. Select the ChMate backup folder."));
        }
        SQLiteDatabase database = null;
        try {
            database = SQLiteDatabase.openDatabase(backupFiles.database.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            Result result = new Result();
            importThreads(database, preferences, backupFiles.datHosts, result);
            return result;
        } finally {
            if (database != null) {
                database.close();
            }
            backupFiles.delete();
        }
    }

    private static BackupFiles readBackupFiles(Context context, Uri uri) throws Exception {
        BackupFiles backupFiles = new BackupFiles();
        if (isTreeUri(uri)) {
            walkTree(context, uri, DocumentsContract.getTreeDocumentId(uri), backupFiles);
        } else {
            backupFiles.database = copyToTempDatabase(context, uri);
        }
        return backupFiles;
    }

    private static boolean isTreeUri(Uri uri) {
        return uri != null && DocumentsContract.isTreeUri(uri);
    }

    private static void walkTree(Context context, Uri treeUri, String documentId, BackupFiles backupFiles) throws Exception {
        Uri children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId);
        Cursor cursor = context.getContentResolver().query(children, new String[]{
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
        }, null, null, null);
        if (cursor == null) {
            return;
        }
        try {
            while (cursor.moveToNext()) {
                String childId = cursor.getString(0);
                String name = cursor.getString(1);
                String mime = cursor.getString(2);
                Uri childUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childId);
                if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
                    walkTree(context, treeUri, childId, backupFiles);
                } else if ("roidon.sqlite".equalsIgnoreCase(name)) {
                    backupFiles.database = copyToTempDatabase(context, childUri);
                } else if (name != null && name.toLowerCase(Locale.ROOT).endsWith(".dat")) {
                    readDatHost(context, childUri, name, backupFiles);
                }
            }
        } finally {
            cursor.close();
        }
    }

    private static File copyToTempDatabase(Context context, Uri uri) throws Exception {
        File file = new File(context.getCacheDir(), "chmate-restore-" + System.currentTimeMillis() + ".sqlite");
        try (InputStream input = context.getContentResolver().openInputStream(uri);
             FileOutputStream output = new FileOutputStream(file)) {
            if (input == null) {
                throw new IllegalStateException(MainActivity.text(
                        "ChMate\u306eSQLite\u30d5\u30a1\u30a4\u30eb\u3092\u958b\u3051\u307e\u305b\u3093\u3002",
                        "Could not open the ChMate SQLite file."));
            }
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
        }
        return file;
    }

    private static void readDatHost(Context context, Uri uri, String name, BackupFiles backupFiles) {
        try {
            String base = name.substring(0, name.length() - 4);
            int sep = base.lastIndexOf('_');
            if (sep <= 0 || sep + 1 >= base.length()) {
                return;
            }
            String board = base.substring(0, sep);
            String key = base.substring(sep + 1);
            byte[] bytes = readBytes(context, uri, 512 * 1024);
            String text = new String(bytes, MS932);
            Matcher matcher = THREAD_URL.matcher(text);
            while (matcher.find()) {
                if (board.equals(matcher.group(2)) && key.equals(matcher.group(3))) {
                    backupFiles.datHosts.put(board + "/" + key, matcher.group(1));
                    return;
                }
                if (board.equals(matcher.group(2)) && !backupFiles.datHosts.has(board + "/" + key)) {
                    backupFiles.datHosts.put(board + "/" + key, matcher.group(1));
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static byte[] readBytes(Context context, Uri uri, int limit) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input == null) {
                return new byte[0];
            }
            byte[] buffer = new byte[32 * 1024];
            int total = 0;
            int read;
            while ((read = input.read(buffer)) >= 0 && total < limit) {
                int count = Math.min(read, limit - total);
                output.write(buffer, 0, count);
                total += count;
            }
        }
        return output.toByteArray();
    }

    private static void importThreads(SQLiteDatabase database, SharedPreferences preferences, JSONObject datHosts, Result result) {
        if (!hasTable(database, "bookmarks")) {
            return;
        }
        JSONArray bookmarks = jsonArray(preferences.getString(MainActivity.PREF_THREAD_BOOKMARKS, "[]"));
        JSONArray history = jsonArray(preferences.getString(MainActivity.PREF_HISTORY, "[]"));
        JSONObject reads = jsonObject(preferences.getString(MainActivity.PREF_READ_POSTS, "{}"));
        JSONArray tabs = jsonArray(preferences.getString(MainActivity.PREF_TABS, "[]"));

        Set<String> bookmarkUrls = urlSet(bookmarks);
        Set<String> historyUrls = urlSet(history);
        Set<String> tabUrls = urlSet(tabs);
        BoardLookup boards = readBoards(database);

        Cursor cursor = database.rawQuery(
                "SELECT name, created, title, read_count, res_count, server_res_count, access, flag FROM bookmarks ORDER BY bookmark_order, _id",
                null);
        try {
            while (cursor.moveToNext()) {
                String board = cursor.getString(0);
                long created = cursor.getLong(1);
                String title = cursor.getString(2);
                int read = cursor.getInt(3);
                int count = Math.max(cursor.getInt(4), cursor.getInt(5));
                long access = cursor.getLong(6);
                int flag = cursor.getInt(7);
                String server = boards.serverFor(board);
                if (isBlank(server)) {
                    server = datHosts.optString(board + "/" + created, "");
                }
                String url = threadUrl(board, server, created);
                if (isBlank(url)) {
                    result.skippedThreads++;
                    continue;
                }
                String cleanTitle = isBlank(title) ? url : title;
                if (!bookmarkUrls.contains(url)) {
                    JSONObject item = new JSONObject();
                    item.put("title", cleanTitle);
                    item.put("url", url);
                    item.put("folder", "");
                    bookmarks.put(item);
                    bookmarkUrls.add(url);
                    result.addedBookmarks++;
                }
                int existingRead = reads.optInt(url, 0);
                int nextRead = Math.max(existingRead, read);
                if (nextRead != existingRead) {
                    reads.put(url, nextRead);
                    result.updatedReadPositions++;
                }
                if (!historyUrls.contains(url)) {
                    JSONObject item = new JSONObject();
                    item.put("title", cleanTitle);
                    item.put("url", url);
                    item.put("lastViewedAt", access > 0 ? access : System.currentTimeMillis());
                    history.put(item);
                    historyUrls.add(url);
                    result.addedHistory++;
                }
                if (!tabUrls.contains(url) && shouldRestoreAsTab(flag)) {
                    JSONObject tab = new JSONObject();
                    tab.put("url", url);
                    tab.put("title", cleanTitle);
                    tab.put("privateBrowsing", false);
                    tab.put("bookmarkOverviewTab", false);
                    tab.put("nativeKind", "thread");
                    tab.put("knownMaxPostNumber", count);
                    tab.put("knownPostCount", count);
                    tab.put("cachedUnreadCount", Math.max(0, count - nextRead));
                    tab.put("hasThreadStats", count > 0);
                    tab.put("knownThreadArchived", false);
                    tab.put("navigationIndex", 0);
                    JSONArray nav = new JSONArray();
                    nav.put(url);
                    tab.put("navigationHistory", nav);
                    tabs.put(tab);
                    tabUrls.add(url);
                    result.addedTabs++;
                }
            }
        } catch (Exception ignored) {
        } finally {
            cursor.close();
        }
        preferences.edit()
                .putString(MainActivity.PREF_THREAD_BOOKMARKS, bookmarks.toString())
                .putString(MainActivity.PREF_HISTORY, history.toString())
                .putString(MainActivity.PREF_READ_POSTS, reads.toString())
                .putString(MainActivity.PREF_TABS, tabs.toString())
                .putLong(MainActivity.PREF_SYNC2CH_UPDATED_AT, System.currentTimeMillis())
                .apply();
    }

    private static boolean shouldRestoreAsTab(int flag) {
        return (flag & 4) != 0;
    }

    private static BoardLookup readBoards(SQLiteDatabase database) {
        BoardLookup lookup = new BoardLookup();
        if (!hasTable(database, "boards")) {
            return lookup;
        }
        Cursor cursor = database.rawQuery("SELECT name, server FROM boards", null);
        try {
            while (cursor.moveToNext()) {
                String name = boardName(cursor.getString(0));
                String server = cursor.getString(1);
                if (!isBlank(name) && !isBlank(server)) {
                    lookup.put(name, server);
                }
            }
        } finally {
            cursor.close();
        }
        return lookup;
    }

    private static boolean hasTable(SQLiteDatabase database, String table) {
        Cursor cursor = database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{table});
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    private static Set<String> urlSet(JSONArray array) {
        Set<String> values = new LinkedHashSet<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item != null) {
                String url = item.optString("url", "");
                if (!isBlank(url)) {
                    values.add(url);
                }
            }
        }
        return values;
    }

    private static String threadUrl(String board, String server, long created) {
        board = boardName(board);
        if (isBlank(board) || isBlank(server) || created <= 0) {
            return "";
        }
        if (server.toLowerCase(Locale.ROOT).endsWith("machi.to")) {
            return "https://" + server + "/bbs/read.cgi/" + board + "/" + created + "/";
        }
        return "https://" + server + "/test/read.cgi/" + board + "/" + created + "/";
    }

    private static String boardName(String value) {
        if (value == null) {
            return "";
        }
        try {
            value = URLDecoder.decode(value, "UTF-8");
        } catch (Exception ignored) {
        }
        int slash = value.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < value.length()) {
            value = value.substring(slash + 1);
        }
        return value.trim();
    }

    private static JSONArray jsonArray(String value) {
        try {
            return new JSONArray(value == null ? "[]" : value);
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    private static JSONObject jsonObject(String value) {
        try {
            return new JSONObject(value == null ? "{}" : value);
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static final class Result {
        int addedBookmarks;
        int addedHistory;
        int addedTabs;
        int updatedReadPositions;
        int skippedThreads;
    }

    private static final class BackupFiles {
        File database;
        final JSONObject datHosts = new JSONObject();

        void delete() {
            if (database != null) {
                database.delete();
            }
        }
    }

    private static final class BoardLookup {
        private final JSONObject values = new JSONObject();

        void put(String board, String server) {
            try {
                values.put(board, server);
            } catch (Exception ignored) {
            }
        }

        String serverFor(String board) {
            return values.optString(boardName(board), "");
        }
    }
}
