package io.github.cuspidroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

final class Sync2chClient {
    private static final String ENDPOINT = "https://sync2ch.com/api/sync3";
    private static final String CLIENT_NAME = "CuspiDroid";
    private static final String CLIENT_VERSION = "1.2.3";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private Sync2chClient() {
    }

    static Result sync(Context context, SharedPreferences preferences) throws Exception {
        String id = preferences.getString(MainActivity.PREF_SYNC2CH_ID, "").trim();
        String password = preferences.getString(MainActivity.PREF_SYNC2CH_API_PASSWORD, "").trim();
        if (id.isEmpty() || password.isEmpty()) {
            throw new IllegalStateException(MainActivity.text(
                    "Sync2ch ID\u3068API\u63a5\u7d9a\u7528\u30d1\u30b9\u30ef\u30fc\u30c9\u3092\u5165\u529b\u3057\u3066\u304f\u3060\u3055\u3044\u3002",
                    "Enter your Sync2ch ID and API connection password."));
        }

        Snapshot snapshot = readSnapshot(preferences);
        HttpURLConnection connection = (HttpURLConnection) new URL(ENDPOINT).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestProperty("Encoding", "gzip");
        connection.setRequestProperty("Accept-Encoding", "gzip");
        connection.setRequestProperty("User-Agent", CLIENT_NAME + "/" + CLIENT_VERSION);
        String credential = id + ":" + password;
        String encoded = android.util.Base64.encodeToString(credential.getBytes(UTF_8), android.util.Base64.NO_WRAP);
        connection.setRequestProperty("Authorization", "Basic " + encoded);

        try (OutputStream raw = connection.getOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(raw);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(gzip, UTF_8))) {
            writer.write(buildRequestXml(context, preferences, snapshot));
        }

        int code = connection.getResponseCode();
        InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (stream == null) {
            throw new IllegalStateException("Sync2ch HTTP " + code);
        }
        String encoding = connection.getHeaderField("Content-Encoding");
        if (encoding != null && encoding.toLowerCase(Locale.ROOT).contains("gzip")) {
            stream = new GZIPInputStream(stream);
        }
        if (code != HttpURLConnection.HTTP_OK) {
            String message = readText(stream);
            throw new IllegalStateException("Sync2ch HTTP " + code + "\n" + message);
        }

        Result result = parseResponse(preferences, snapshot, stream);
        applyResponse(preferences, snapshot, result);
        return result;
    }

    private static String buildRequestXml(Context context, SharedPreferences preferences, Snapshot snapshot) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
        xml.append("<sync2ch_request sync_number=\"")
                .append(escape(preferences.getString(MainActivity.PREF_SYNC2CH_SYNC_NUMBER, "0")))
                .append("\" client_name=\"").append(CLIENT_NAME)
                .append("\" client_id=\"")
                .append(escape(preferences.getString(MainActivity.PREF_SYNC2CH_CLIENT_ID, "0")))
                .append("\" client_version=\"").append(CLIENT_VERSION)
                .append("\" os=\"Android ").append(escape(Build.VERSION.RELEASE)).append("\">\n");

        appendGroup(xml, "open", snapshot.openThreads);
        appendGroup(xml, "favorite", snapshot.favoriteThreads);
        xml.append("<entities>\n");
        for (ThreadState thread : snapshot.entities.values()) {
            xml.append("<th id=\"").append(thread.id).append("\" url=\"").append(escape(thread.url))
                    .append("\" title=\"").append(escape(thread.title))
                    .append("\" read=\"").append(thread.read)
                    .append("\" now=\"").append(thread.now)
                    .append("\" count=\"").append(thread.count)
                    .append("\"/>\n");
        }
        xml.append("</entities>\n");
        xml.append("</sync2ch_request>");
        return xml.toString();
    }

    private static void appendGroup(StringBuilder xml, String category, List<ThreadState> threads) {
        xml.append("<thread_group abbr=\"true\" struct=\"default\" category=\"")
                .append(category).append("\" id_list=\"");
        for (int i = 0; i < threads.size(); i++) {
            if (i > 0) {
                xml.append(',');
            }
            xml.append(threads.get(i).id);
        }
        xml.append("\" />\n");
    }

    private static Result parseResponse(SharedPreferences preferences, Snapshot snapshot, InputStream stream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(stream);
        Element root = document.getDocumentElement();
        Result result = new Result();
        if (root == null) {
            return result;
        }
        result.syncNumber = root.getAttribute("sync_number");
        result.clientId = root.getAttribute("client_id");

        Map<String, ThreadState> entities = new LinkedHashMap<>();
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            if (!"entities".equals(element.getTagName())) {
                continue;
            }
            NodeList entityNodes = element.getChildNodes();
            for (int j = 0; j < entityNodes.getLength(); j++) {
                Node entityNode = entityNodes.item(j);
                if (entityNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element th = (Element) entityNode;
                if (!"th".equals(th.getTagName())) {
                    continue;
                }
                String id = th.getAttribute("id");
                ThreadState local = snapshot.byId.get(id);
                ThreadState state = local == null ? new ThreadState() : local.copy();
                state.id = id;
                if (!"n".equals(th.getAttribute("s"))) {
                    state.url = valueOr(th.getAttribute("url"), state.url);
                    state.title = valueOr(th.getAttribute("title"), state.title);
                    state.read = intOr(th.getAttribute("read"), state.read);
                    state.now = intOr(th.getAttribute("now"), state.now);
                    state.count = intOr(th.getAttribute("count"), state.count);
                }
                if (!isBlank(state.url)) {
                    entities.put(id, state);
                }
            }
        }

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            if (!"thread_group".equals(element.getTagName()) || "n".equals(element.getAttribute("s"))) {
                continue;
            }
            String category = element.getAttribute("category");
            if ("open".equals(category)) {
                collectThreads(element, "", entities, result.openThreads);
            } else if ("favorite".equals(category)) {
                collectThreads(element, "", entities, result.favoriteThreads);
            }
        }
        return result;
    }

    private static void collectThreads(Element parent, String folder, Map<String, ThreadState> entities,
                                       List<ThreadState> out) {
        String idList = parent.getAttribute("id_list");
        if (!isBlank(idList)) {
            String[] ids = idList.split(",");
            for (String id : ids) {
                ThreadState state = entities.get(id.trim());
                if (state != null) {
                    ThreadState copy = state.copy();
                    copy.folder = folder;
                    out.add(copy);
                }
            }
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            if ("th".equals(element.getTagName())) {
                ThreadState state = entities.get(element.getAttribute("id"));
                if (state != null) {
                    ThreadState copy = state.copy();
                    copy.folder = folder;
                    out.add(copy);
                }
            } else if ("dir".equals(element.getTagName())) {
                String name = normalizeFolder(element.getAttribute("name"));
                String child = folder.isEmpty() ? name : folder + "/" + name;
                collectThreads(element, child, entities, out);
            }
        }
    }

    private static void applyResponse(SharedPreferences preferences, Snapshot snapshot, Result result) {
        SharedPreferences.Editor editor = preferences.edit();
        if (!isBlank(result.syncNumber)) {
            editor.putString(MainActivity.PREF_SYNC2CH_SYNC_NUMBER, result.syncNumber);
        }
        if (!isBlank(result.clientId)) {
            editor.putString(MainActivity.PREF_SYNC2CH_CLIENT_ID, result.clientId);
        }
        applyReadStates(editor, preferences, result);
        result.addedBookmarks = applyBookmarks(editor, preferences, result.favoriteThreads);
        result.addedOpenThreads = applyTabs(editor, preferences, snapshot, result.openThreads);
        editor.putLong(MainActivity.PREF_SYNC2CH_UPDATED_AT, System.currentTimeMillis());
        editor.apply();
    }

    private static void applyReadStates(SharedPreferences.Editor editor, SharedPreferences preferences, Result result) {
        try {
            JSONObject reads = new JSONObject(preferences.getString(MainActivity.PREF_READ_POSTS, "{}"));
            for (ThreadState state : result.allThreads()) {
                if (isBlank(state.url)) {
                    continue;
                }
                int existing = reads.optInt(state.url, 0);
                reads.put(state.url, Math.max(existing, state.read));
            }
            editor.putString(MainActivity.PREF_READ_POSTS, reads.toString());
        } catch (Exception ignored) {
        }
    }

    private static int applyBookmarks(SharedPreferences.Editor editor, SharedPreferences preferences,
                                       List<ThreadState> remote) {
        int added = 0;
        try {
            JSONArray array = new JSONArray(preferences.getString(MainActivity.PREF_THREAD_BOOKMARKS, "[]"));
            Set<String> urls = new LinkedHashSet<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item != null) {
                    urls.add(item.optString("url", ""));
                }
            }
            Set<String> folders = new LinkedHashSet<>();
            JSONArray folderArray = new JSONArray(preferences.getString(MainActivity.PREF_THREAD_BOOKMARKS + "_folders", "[]"));
            for (int i = 0; i < folderArray.length(); i++) {
                String folder = normalizeFolder(folderArray.optString(i, ""));
                if (!folder.isEmpty()) {
                    folders.add(folder);
                }
            }
            for (ThreadState state : remote) {
                if (isBlank(state.url) || urls.contains(state.url)) {
                    continue;
                }
                JSONObject item = new JSONObject();
                item.put("title", fallbackTitle(state));
                item.put("url", state.url);
                item.put("folder", normalizeFolder(state.folder));
                array.put(item);
                urls.add(state.url);
                added++;
            }
            for (ThreadState state : remote) {
                String folder = normalizeFolder(state.folder);
                while (!folder.isEmpty()) {
                    folders.add(folder);
                    int sep = folder.lastIndexOf('/');
                    folder = sep < 0 ? "" : folder.substring(0, sep);
                }
            }
            JSONArray nextFolders = new JSONArray();
            for (String folder : folders) {
                nextFolders.put(folder);
            }
            editor.putString(MainActivity.PREF_THREAD_BOOKMARKS, array.toString());
            editor.putString(MainActivity.PREF_THREAD_BOOKMARKS + "_folders", nextFolders.toString());
        } catch (Exception ignored) {
        }
        return added;
    }

    private static int applyTabs(SharedPreferences.Editor editor, SharedPreferences preferences,
                                  Snapshot snapshot, List<ThreadState> remote) {
        int added = 0;
        try {
            Map<String, ThreadState> remoteByUrl = new LinkedHashMap<>();
            for (ThreadState state : remote) {
                if (!isBlank(state.url)) {
                    remoteByUrl.put(state.url, state);
                }
            }
            JSONArray array = new JSONArray(preferences.getString(MainActivity.PREF_TABS, "[]"));
            Set<String> existing = new LinkedHashSet<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject tab = array.optJSONObject(i);
                if (tab == null) {
                    continue;
                }
                String url = tab.optString("url", "");
                if (!isBlank(url)) {
                    existing.add(url);
                }
                ThreadState state = remoteByUrl.containsKey(url) ? remoteByUrl.get(url) : snapshot.byUrl.get(url);
                if (state != null) {
                    tab.put("cachedUnreadCount", Math.max(0, Math.max(state.count, state.now) - state.read));
                    tab.put("knownPostCount", Math.max(state.count, state.now));
                    tab.put("knownMaxPostNumber", Math.max(state.count, state.now));
                    tab.put("hasThreadStats", true);
                }
            }
            for (ThreadState state : remote) {
                if (isBlank(state.url) || existing.contains(state.url)) {
                    continue;
                }
                JSONObject tab = new JSONObject();
                tab.put("url", state.url);
                tab.put("title", fallbackTitle(state));
                tab.put("privateBrowsing", false);
                tab.put("bookmarkOverviewTab", false);
                tab.put("nativeKind", "thread");
                tab.put("cachedUnreadCount", Math.max(0, Math.max(state.count, state.now) - state.read));
                tab.put("knownPostCount", Math.max(state.count, state.now));
                tab.put("knownMaxPostNumber", Math.max(state.count, state.now));
                tab.put("hasThreadStats", true);
                tab.put("knownThreadArchived", false);
                tab.put("navigationIndex", 0);
                JSONArray history = new JSONArray();
                history.put(state.url);
                tab.put("navigationHistory", history);
                array.put(tab);
                existing.add(state.url);
                added++;
            }
            editor.putString(MainActivity.PREF_TABS, array.toString());
        } catch (Exception ignored) {
        }
        return added;
    }

    private static Snapshot readSnapshot(SharedPreferences preferences) {
        Snapshot snapshot = new Snapshot();
        readBookmarks(preferences, snapshot);
        readTabs(preferences, snapshot);
        readHistory(preferences, snapshot);
        readReadPositions(preferences, snapshot);
        int id = 0;
        for (ThreadState state : snapshot.entities.values()) {
            state.id = String.valueOf(id++);
            snapshot.byId.put(state.id, state);
        }
        return snapshot;
    }

    private static void readBookmarks(SharedPreferences preferences, Snapshot snapshot) {
        try {
            JSONArray array = new JSONArray(preferences.getString(MainActivity.PREF_THREAD_BOOKMARKS, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                ThreadState state = snapshot.thread(item.optString("url", ""));
                if (state == null) {
                    continue;
                }
                state.title = valueOr(item.optString("title", ""), state.title);
                state.folder = normalizeFolder(item.optString("folder", ""));
                snapshot.favoriteThreads.add(state);
            }
        } catch (Exception ignored) {
        }
    }

    private static void readTabs(SharedPreferences preferences, Snapshot snapshot) {
        try {
            JSONArray array = new JSONArray(preferences.getString(MainActivity.PREF_TABS, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject tab = array.optJSONObject(i);
                if (tab == null || tab.optBoolean("privateBrowsing", false)
                        || tab.optBoolean("bookmarkOverviewTab", false)) {
                    continue;
                }
                String url = tab.optString("url", "");
                if (!isThreadUrl(url)) {
                    continue;
                }
                ThreadState state = snapshot.thread(url);
                if (state == null) {
                    continue;
                }
                state.title = valueOr(tab.optString("title", ""), state.title);
                state.count = Math.max(state.count, tab.optInt("knownPostCount", tab.optInt("knownMaxPostNumber", 0)));
                state.now = Math.max(state.now, state.count);
                snapshot.openThreads.add(state);
            }
        } catch (Exception ignored) {
        }
    }

    private static void readHistory(SharedPreferences preferences, Snapshot snapshot) {
        try {
            JSONArray array = new JSONArray(preferences.getString(MainActivity.PREF_HISTORY, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                ThreadState state = snapshot.thread(item.optString("url", ""));
                if (state != null) {
                    state.title = valueOr(item.optString("title", ""), state.title);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void readReadPositions(SharedPreferences preferences, Snapshot snapshot) {
        try {
            JSONObject reads = new JSONObject(preferences.getString(MainActivity.PREF_READ_POSTS, "{}"));
            JSONArray names = reads.names();
            if (names == null) {
                return;
            }
            for (int i = 0; i < names.length(); i++) {
                String url = names.optString(i, "");
                ThreadState state = snapshot.thread(url);
                if (state != null) {
                    state.read = Math.max(state.read, reads.optInt(url, 0));
                    state.now = Math.max(state.now, state.read);
                    state.count = Math.max(state.count, state.read);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static String fallbackTitle(ThreadState state) {
        if (!isBlank(state.title)) {
            return state.title;
        }
        return state.url == null ? "" : state.url;
    }

    private static boolean isThreadUrl(String url) {
        if (url == null) {
            return false;
        }
        String lower = url.toLowerCase(Locale.ROOT);
        return lower.contains("/test/read.cgi/") || lower.contains("/bbs/read.cgi/");
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

    private static String valueOr(String value, String fallback) {
        return isBlank(value) ? (fallback == null ? "" : fallback) : value.trim();
    }

    private static int intOr(String value, int fallback) {
        try {
            return isBlank(value) ? fallback : Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return TextUtils.htmlEncode(value);
    }

    private static String readText(InputStream stream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, UTF_8));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append('\n');
        }
        return builder.toString().trim();
    }

    static final class Result {
        int addedOpenThreads;
        int addedBookmarks;
        String syncNumber;
        String clientId;
        final List<ThreadState> openThreads = new ArrayList<>();
        final List<ThreadState> favoriteThreads = new ArrayList<>();

        List<ThreadState> allThreads() {
            List<ThreadState> all = new ArrayList<>();
            all.addAll(openThreads);
            all.addAll(favoriteThreads);
            return all;
        }
    }

    private static final class Snapshot {
        final Map<String, ThreadState> entities = new LinkedHashMap<>();
        final Map<String, ThreadState> byUrl = new LinkedHashMap<>();
        final Map<String, ThreadState> byId = new LinkedHashMap<>();
        final List<ThreadState> openThreads = new ArrayList<>();
        final List<ThreadState> favoriteThreads = new ArrayList<>();

        ThreadState thread(String url) {
            if (isBlank(url) || !isThreadUrl(url)) {
                return null;
            }
            ThreadState state = byUrl.get(url);
            if (state == null) {
                state = new ThreadState();
                state.url = url.trim();
                byUrl.put(state.url, state);
                entities.put(state.url, state);
            }
            return state;
        }
    }

    private static final class ThreadState {
        String id = "";
        String title = "";
        String url = "";
        String folder = "";
        int read;
        int now;
        int count;

        ThreadState copy() {
            ThreadState copy = new ThreadState();
            copy.id = id;
            copy.title = title;
            copy.url = url;
            copy.folder = folder;
            copy.read = read;
            copy.now = now;
            copy.count = count;
            return copy;
        }
    }
}
