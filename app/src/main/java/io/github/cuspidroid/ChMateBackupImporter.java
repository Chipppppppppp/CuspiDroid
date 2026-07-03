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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class ChMateBackupImporter {
    private static final Charset MS932 = Charset.forName("MS932");
    private static final Pattern THREAD_URL = Pattern.compile("https?://([^/]+)/+(?:test|bbs)/read\\.cgi/+([^/]+)/+(\\d+)", Pattern.CASE_INSENSITIVE);

    private ChMateBackupImporter() {
    }

    static Result importBackup(Context context, SharedPreferences preferences, Uri uri) throws Exception {
        BackupFiles backupFiles = readBackupFiles(context, uri);
        if (backupFiles.database == null || !backupFiles.database.exists()) {
            throw new IllegalStateException(MainActivity.text(
                    "databases/roidon.sqlite\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3002ChMate\u306e\u30d0\u30c3\u30af\u30a2\u30c3\u30d7zip\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002",
                    "databases/roidon.sqlite was not found. Select the ChMate backup zip."));
        }
        SQLiteDatabase database = null;
        try {
            database = SQLiteDatabase.openDatabase(backupFiles.database.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            Result result = new Result();
            importThreads(database, preferences, backupFiles.datHosts, result);
            importNgRules(database, preferences, backupFiles.ngFiles, result);
            importPostDataList(preferences, backupFiles.postDataList, result);
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
            readZipBackup(context, uri, backupFiles);
            if (backupFiles.database == null) {
                backupFiles.database = copyToTempDatabase(context, uri);
            }
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
                } else if ("postDataList.json".equalsIgnoreCase(name)) {
                    backupFiles.postDataList = readPostDataList(readBytes(context, childUri, 8 * 1024 * 1024));
                } else if (isChMateNgJsonName(name)) {
                    backupFiles.ngFiles.put(name, readJsonArray(readBytes(context, childUri, 1024 * 1024)));
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

    private static void readZipBackup(Context context, Uri uri, BackupFiles backupFiles) {
        try (InputStream raw = context.getContentResolver().openInputStream(uri);
             ZipInputStream zip = raw == null ? null : new ZipInputStream(raw)) {
            if (zip == null) {
                return;
            }
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zip.closeEntry();
                    continue;
                }
                String name = entry.getName();
                String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
                String displayName = name == null ? "" : name.substring(name.lastIndexOf('/') + 1);
                if (lower.endsWith("/databases/roidon.sqlite") || lower.equals("databases/roidon.sqlite")
                        || lower.endsWith("roidon.sqlite")) {
                    backupFiles.database = copyZipEntryToTempDatabase(context, zip);
                } else if (lower.endsWith("/files/postdatalist.json") || lower.equals("files/postdatalist.json")
                        || lower.endsWith("postdatalist.json")) {
                    backupFiles.postDataList = readPostDataList(readZipEntryBytes(zip, 8 * 1024 * 1024));
                } else if ((lower.contains("/ng/") || lower.startsWith("ng/")) && isChMateNgJsonName(displayName)) {
                    backupFiles.ngFiles.put(displayName, readJsonArray(readZipEntryBytes(zip, 1024 * 1024)));
                } else if (lower.contains("/dat/") && lower.endsWith(".dat") || lower.startsWith("dat/") && lower.endsWith(".dat")) {
                    readDatHost(readZipEntryBytes(zip, 512 * 1024), displayName, backupFiles);
                }
                zip.closeEntry();
            }
        } catch (Exception ignored) {
        }
    }

    private static void importNgRules(SQLiteDatabase database, SharedPreferences preferences,
                                      Map<String, JSONArray> ngFiles, Result result) {
        if (ngFiles == null || ngFiles.isEmpty()) {
            return;
        }
        BoardLookup boards = readBoards(database);
        java.util.List<MainActivity.ScopedNgRule> rules = MainActivity.readNgRules(preferences);
        Set<String> identities = new LinkedHashSet<>();
        for (MainActivity.ScopedNgRule rule : rules) {
            identities.add(ngRuleIdentity(rule.category, rule.value, rule.regex, rule.targetUrl));
        }
        for (Map.Entry<String, JSONArray> entry : ngFiles.entrySet()) {
            String category = ngCategoryFromChMateFile(entry.getKey());
            JSONArray array = entry.getValue();
            if (category.isEmpty() || array == null) {
                continue;
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                String value = item.optString("w", item.optString("value", "")).trim();
                if (isBlank(value)) {
                    continue;
                }
                String board = boardName(item.optString("b", item.optString("board", "")));
                String targetUrl = "";
                if (!isBlank(board)) {
                    String server = boards.serverFor(board);
                    targetUrl = boardUrl(board, server);
                }
                boolean regex = item.optBoolean("regex", false);
                String identity = ngRuleIdentity(category, value, regex, targetUrl);
                if (identities.contains(identity)) {
                    continue;
                }
                rules.add(new MainActivity.ScopedNgRule(
                        category, value, regex, targetUrl, board,
                        MainActivity.ngModeFromChMateFlag(item.optInt("f", MainActivity.chMateNgFlag(MainActivity.NG_DISPLAY_OMIT))),
                        item.optLong("ct", 0L), board));
                identities.add(identity);
                result.addedNgRules++;
            }
        }
        if (result.addedNgRules > 0) {
            MainActivity.saveNgRules(preferences, rules);
        }
    }

    private static String ngRuleIdentity(String category, String value, boolean regex, String targetUrl) {
        return (category == null ? "" : category) + "\n"
                + (regex ? "1" : "0") + "\n"
                + (value == null ? "" : value.trim()) + "\n"
                + MainActivity.normalizeNgTargetUrl(targetUrl);
    }

    private static boolean isChMateNgJsonName(String name) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        return lower.startsWith("_") && lower.endsWith(".json") && !ngCategoryFromChMateFile(lower).isEmpty();
    }

    private static String ngCategoryFromChMateFile(String name) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (lower.endsWith("_id.json") || lower.equals("_id.json")) return "NGID";
        if (lower.endsWith("_name.json") || lower.equals("_name.json")) return "NGName";
        if (lower.endsWith("_word.json") || lower.endsWith("_body.json")
                || lower.equals("_word.json") || lower.equals("_body.json")) return "NGWord";
        if (lower.endsWith("_be.json") || lower.equals("_be.json")) return "NGBe";
        if (lower.endsWith("_thread.json") || lower.endsWith("_title.json")
                || lower.equals("_thread.json") || lower.equals("_title.json")) return "NGThread";
        return "";
    }

    private static String boardUrl(String board, String server) {
        board = boardName(board);
        if (isBlank(board) || isBlank(server)) {
            return "";
        }
        String value = server.trim();
        value = value.replaceFirst("(?i)^https?://", "");
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return "https://" + value + "/" + board + "/";
    }

    private static File copyZipEntryToTempDatabase(Context context, InputStream input) throws Exception {
        File file = new File(context.getCacheDir(), "chmate-restore-" + System.currentTimeMillis() + ".sqlite");
        try (FileOutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
        }
        return file;
    }

    private static byte[] readZipEntryBytes(InputStream input, int limit) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[32 * 1024];
        int total = 0;
        int read;
        while ((read = input.read(buffer)) >= 0 && total < limit) {
            int count = Math.min(read, limit - total);
            output.write(buffer, 0, count);
            total += count;
        }
        return output.toByteArray();
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
            readDatHost(bytes, board, key, backupFiles);
        } catch (Exception ignored) {
        }
    }

    private static void readDatHost(byte[] bytes, String name, BackupFiles backupFiles) {
        try {
            String base = name.substring(0, name.length() - 4);
            int sep = base.lastIndexOf('_');
            if (sep <= 0 || sep + 1 >= base.length()) {
                return;
            }
            readDatHost(bytes, base.substring(0, sep), base.substring(sep + 1), backupFiles);
        } catch (Exception ignored) {
        }
    }

    private static void readDatHost(byte[] bytes, String board, String key, BackupFiles backupFiles) throws Exception {
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

        Set<String> bookmarkUrls = savedIdentitySet(bookmarks);
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
                String folder = rankFolder(flag);
                String bookmarkIdentity = savedItemIdentity(url, folder);
                if (!bookmarkUrls.contains(bookmarkIdentity)) {
                    JSONObject item = new JSONObject();
                    item.put("title", cleanTitle);
                    item.put("url", url);
                    item.put("folder", folder);
                    bookmarks.put(item);
                    bookmarkUrls.add(bookmarkIdentity);
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
                .putString(MainActivity.PREF_THREAD_BOOKMARKS, dedupeSavedItems(bookmarks).toString())
                .putString(MainActivity.PREF_HISTORY, history.toString())
                .putString(MainActivity.PREF_READ_POSTS, reads.toString())
                .putString(MainActivity.PREF_TABS, tabs.toString())
                .putLong(MainActivity.PREF_SYNC2CH_UPDATED_AT, System.currentTimeMillis())
                .apply();
    }

    private static void importPostDataList(SharedPreferences preferences, JSONArray postDataList, Result result) {
        if (postDataList == null || postDataList.length() == 0) {
            return;
        }
        try {
            JSONObject myPosts = jsonObject(preferences.getString(MainActivity.PREF_MY_POSTS, "{}"));
            for (int i = 0; i < postDataList.length(); i++) {
                JSONObject item = postDataList.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                String url = item.optString("url", "").trim();
                String body = item.optString("body", "");
                String hash = item.optString("bodyHash", "").trim();
                if (isBlank(hash)) {
                    hash = item.optString("hash", "").trim();
                }
                if (isBlank(hash)) {
                    hash = postBodyHash(body);
                }
                if (isBlank(url) || isBlank(hash)) {
                    continue;
                }
                JSONArray old = myPosts.optJSONArray(url);
                JSONArray next = new JSONArray();
                JSONObject imported = new JSONObject();
                imported.put("hash", hash);
                imported.put("bodyHash", hash);
                imported.put("body", normalizeOwnPostBody(body));
                imported.put("number", item.optInt("number", item.optInt("postNumber", item.optInt("resNumber", 0))));
                long postedAt = item.optLong("postedAt", 0L);
                if (postedAt <= 0) {
                    postedAt = item.optLong("posted", item.optLong("time", 0L));
                }
                imported.put("postedAt", postedAt);
                imported.put("posted", postedAt);
                imported.put("title", item.optString("title", item.optString("targetTitle", "")));
                imported.put("targetTitle", item.optString("targetTitle", item.optString("title", "")));
                next.put(imported);
                boolean exists = false;
                if (old != null) {
                    for (int j = 0; j < old.length() && next.length() < 80; j++) {
                        Object value = old.opt(j);
                        String valueHash = myPostHash(value);
                        if (hash.equals(valueHash)) {
                            exists = true;
                        } else if (!isBlank(valueHash)) {
                            next.put(value);
                        }
                    }
                }
                if (!exists) {
                    result.addedPostHistory++;
                }
                myPosts.put(url, next);
            }
            preferences.edit()
                    .putString(MainActivity.PREF_MY_POSTS, myPosts.toString())
                    .putLong(MainActivity.PREF_SYNC2CH_UPDATED_AT, System.currentTimeMillis())
                    .apply();
        } catch (Exception ignored) {
        }
    }

    private static boolean shouldRestoreAsTab(int flag) {
        return (flag & 4) != 0;
    }

    private static String rankFolder(int flag) {
        int rank = flag & 7;
        if (rank <= 0) {
            rank = 1;
        } else if (rank > 5) {
            rank = 5;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rank; i++) {
            builder.append('\u2605');
        }
        return builder.toString();
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

    private static Set<String> savedIdentitySet(JSONArray array) {
        Set<String> values = new LinkedHashSet<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item != null) {
                String url = item.optString("url", "");
                if (!isBlank(url)) {
                    values.add(savedItemIdentity(url, item.optString("folder", "")));
                }
            }
        }
        return values;
    }

    private static JSONArray dedupeSavedItems(JSONArray array) {
        JSONArray next = new JSONArray();
        Set<String> seen = new LinkedHashSet<>();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                String url = item.optString("url", "").trim();
                String folder = normalizeFolder(item.optString("folder", ""));
                String title = item.optString("title", "").trim();
                String identity = savedItemIdentity(url, folder);
                if (isBlank(url) || seen.contains(identity)) {
                    continue;
                }
                JSONObject copy = new JSONObject();
                copy.put("title", isBlank(title) ? url : title);
                copy.put("url", url);
                copy.put("folder", folder);
                next.put(copy);
                seen.add(identity);
            }
        } catch (Exception ignored) {
        }
        return next;
    }

    private static String savedItemIdentity(String url, String folder) {
        return normalizeFolder(folder) + "\n" + (url == null ? "" : url.trim());
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

    private static String normalizeFolder(String folder) {
        if (folder == null) {
            return "";
        }
        String value = folder.trim().replace('\\', '/');
        while (value.contains("//")) {
            value = value.replace("//", "/");
        }
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
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

    private static JSONArray readPostDataList(byte[] bytes) {
        return readJsonArray(bytes);
    }

    private static JSONArray readJsonArray(byte[] bytes) {
        try {
            return new JSONArray(new String(bytes == null ? new byte[0] : bytes, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    private static String postBodyHash(String body) {
        String normalized = normalizeOwnPostBody(body);
        if (normalized.isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : bytes) {
                builder.append(String.format(Locale.ROOT, "%02x", value & 0xff));
            }
            return builder.toString();
        } catch (Exception ignored) {
            return normalized;
        }
    }

    private static String myPostHash(Object value) {
        if (value instanceof JSONObject) {
            JSONObject object = (JSONObject) value;
            String hash = object.optString("hash", "").trim();
            if (isBlank(hash)) {
                hash = object.optString("bodyHash", "").trim();
            }
            return hash;
        }
        return value instanceof String ? ((String) value).trim() : "";
    }

    private static String normalizeOwnPostBody(String body) {
        if (body == null) {
            return "";
        }
        return stripHtml(body).replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private static String stripHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("<br>", "\n")
                .replace("<br/>", "\n")
                .replace("<br />", "\n")
                .replaceAll("<[^>]+>", "")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .trim();
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
        int addedPostHistory;
        int addedNgRules;
    }

    private static final class BackupFiles {
        File database;
        final JSONObject datHosts = new JSONObject();
        JSONArray postDataList = new JSONArray();
        final Map<String, JSONArray> ngFiles = new LinkedHashMap<>();

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
