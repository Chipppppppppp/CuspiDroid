package io.github.cuspidroid;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class FavoritePostsStore {
    static final String PREF_CATEGORIES = "favorite_post_categories";
    static final String PREF_POSTS = "favorite_posts";
    private static final Pattern READ_CGI_THREAD = Pattern.compile(
            "(?i)^(https?://[^/]+/(?:test/)?read\\.cgi/[^/]+/[^/?#]+)");

    private FavoritePostsStore() {
    }

    static Snapshot read(SharedPreferences preferences) {
        List<Category> categories = new ArrayList<>();
        List<FavoritePost> posts = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString(PREF_CATEGORIES, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) continue;
                String id = item.optString("id", "").trim();
                if (id.isEmpty()) continue;
                categories.add(new Category(id, item.optString("name", ""),
                        item.optInt("color", Color.rgb(15, 118, 110)), item.optLong("createdAt", 0L)));
            }
        } catch (Exception ignored) {
        }
        try {
            JSONArray array = new JSONArray(preferences.getString(PREF_POSTS, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) continue;
                String categoryId = item.optString("categoryId", "").trim();
                String url = item.optString("url", "").trim();
                int number = item.optInt("number", 0);
                if (categoryId.isEmpty() || url.isEmpty() || number <= 0) continue;
                posts.add(new FavoritePost(categoryId, url, item.optString("title", ""), number,
                        item.optString("name", ""), item.optString("date", ""),
                        item.optString("body", ""), item.optLong("savedAt", 0L)));
            }
        } catch (Exception ignored) {
        }
        return new Snapshot(categories, posts);
    }

    static Category addCategory(SharedPreferences preferences, String name, int color) {
        Snapshot snapshot = read(preferences);
        Category category = new Category(UUID.randomUUID().toString(), cleanName(name), color,
                System.currentTimeMillis());
        snapshot.categories.add(category);
        write(preferences, snapshot);
        return category;
    }

    static void updateCategory(SharedPreferences preferences, String id, String name, int color) {
        Snapshot snapshot = read(preferences);
        for (int i = 0; i < snapshot.categories.size(); i++) {
            Category old = snapshot.categories.get(i);
            if (old.id.equals(id)) {
                snapshot.categories.set(i, new Category(old.id, cleanName(name), color, old.createdAt));
                write(preferences, snapshot);
                return;
            }
        }
    }

    static void deleteCategory(SharedPreferences preferences, String id) {
        Snapshot snapshot = read(preferences);
        for (int i = snapshot.categories.size() - 1; i >= 0; i--) {
            if (snapshot.categories.get(i).id.equals(id)) snapshot.categories.remove(i);
        }
        for (int i = snapshot.posts.size() - 1; i >= 0; i--) {
            if (snapshot.posts.get(i).categoryId.equals(id)) snapshot.posts.remove(i);
        }
        write(preferences, snapshot);
    }

    static void savePost(SharedPreferences preferences, FavoritePost post) {
        Snapshot snapshot = read(preferences);
        String key = postKey(post.url, post.number);
        removePostByKey(snapshot.posts, key);
        snapshot.posts.add(0, post);
        write(preferences, snapshot);
    }

    static void removePost(SharedPreferences preferences, String url, int number) {
        Snapshot snapshot = read(preferences);
        String key = postKey(url, number);
        removePostByKey(snapshot.posts, key);
        write(preferences, snapshot);
    }

    static void moveCategory(SharedPreferences preferences, String id, int targetIndex) {
        Snapshot snapshot = read(preferences);
        int sourceIndex = -1;
        for (int i = 0; i < snapshot.categories.size(); i++) {
            if (snapshot.categories.get(i).id.equals(id)) {
                sourceIndex = i;
                break;
            }
        }
        if (sourceIndex < 0) return;
        Category category = snapshot.categories.remove(sourceIndex);
        if (sourceIndex < targetIndex) targetIndex--;
        snapshot.categories.add(Math.max(0, Math.min(targetIndex, snapshot.categories.size())), category);
        write(preferences, snapshot);
    }

    static void movePost(SharedPreferences preferences, String categoryId, String url, int number,
                         int targetIndex) {
        Snapshot snapshot = read(preferences);
        List<FavoritePost> categoryPosts = new ArrayList<>();
        for (FavoritePost post : snapshot.posts) {
            if (post.categoryId.equals(categoryId)) categoryPosts.add(post);
        }
        String key = postKey(url, number);
        int sourceIndex = -1;
        for (int i = 0; i < categoryPosts.size(); i++) {
            FavoritePost post = categoryPosts.get(i);
            if (postKey(post.url, post.number).equals(key)) {
                sourceIndex = i;
                break;
            }
        }
        if (sourceIndex < 0) return;
        FavoritePost moved = categoryPosts.remove(sourceIndex);
        if (sourceIndex < targetIndex) targetIndex--;
        categoryPosts.add(Math.max(0, Math.min(targetIndex, categoryPosts.size())), moved);
        int categoryIndex = 0;
        for (int i = 0; i < snapshot.posts.size(); i++) {
            if (snapshot.posts.get(i).categoryId.equals(categoryId)) {
                snapshot.posts.set(i, categoryPosts.get(categoryIndex++));
            }
        }
        write(preferences, snapshot);
    }

    private static void removePostByKey(List<FavoritePost> posts, String key) {
        for (int i = posts.size() - 1; i >= 0; i--) {
            FavoritePost item = posts.get(i);
            if (postKey(item.url, item.number).equals(key)) posts.remove(i);
        }
    }

    static Category category(Snapshot snapshot, String id) {
        if (snapshot == null || id == null) return null;
        for (Category category : snapshot.categories) {
            if (category.id.equals(id)) return category;
        }
        return null;
    }

    static FavoritePost find(Snapshot snapshot, String url, int number) {
        if (snapshot == null || number <= 0) return null;
        String key = postKey(url, number);
        for (FavoritePost post : snapshot.posts) {
            if (postKey(post.url, post.number).equals(key)) return post;
        }
        return null;
    }

    static String postKey(String url, int number) {
        return canonicalThreadUrl(url) + "#" + number;
    }

    static String canonicalThreadUrl(String url) {
        String value = url == null ? "" : url.trim();
        Matcher matcher = READ_CGI_THREAD.matcher(value);
        if (matcher.find()) value = matcher.group(1);
        try {
            Uri uri = Uri.parse(value);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            String path = uri.getPath() == null ? "" : uri.getPath();
            while (path.endsWith("/") && path.length() > 1) path = path.substring(0, path.length() - 1);
            if (!scheme.isEmpty() && !host.isEmpty()) return scheme + "://" + host + path;
        } catch (Exception ignored) {
        }
        return value.replaceAll("[?#].*$", "").replaceAll("/+$", "");
    }

    private static String cleanName(String name) {
        String value = name == null ? "" : name.trim();
        return value.length() <= 80 ? value : value.substring(0, 80);
    }

    private static void write(SharedPreferences preferences, Snapshot snapshot) {
        JSONArray categories = new JSONArray();
        for (Category category : snapshot.categories) {
            JSONObject item = new JSONObject();
            try {
                item.put("id", category.id);
                item.put("name", category.name);
                item.put("color", category.color);
                item.put("createdAt", category.createdAt);
                categories.put(item);
            } catch (Exception ignored) {
            }
        }
        JSONArray posts = new JSONArray();
        for (FavoritePost post : snapshot.posts) {
            JSONObject item = new JSONObject();
            try {
                item.put("categoryId", post.categoryId);
                item.put("url", post.url);
                item.put("title", post.title);
                item.put("number", post.number);
                item.put("name", post.name);
                item.put("date", post.date);
                item.put("body", post.body);
                item.put("savedAt", post.savedAt);
                posts.put(item);
            } catch (Exception ignored) {
            }
        }
        preferences.edit().putString(PREF_CATEGORIES, categories.toString())
                .putString(PREF_POSTS, posts.toString()).apply();
    }

    static final class Snapshot {
        final List<Category> categories;
        final List<FavoritePost> posts;

        Snapshot(List<Category> categories, List<FavoritePost> posts) {
            this.categories = categories;
            this.posts = posts;
        }

        Map<String, List<FavoritePost>> postsByCategory() {
            Map<String, List<FavoritePost>> result = new LinkedHashMap<>();
            for (Category category : categories) result.put(category.id, new ArrayList<>());
            for (FavoritePost post : posts) {
                List<FavoritePost> list = result.get(post.categoryId);
                if (list != null) list.add(post);
            }
            return result;
        }
    }

    static final class Category {
        final String id;
        final String name;
        final int color;
        final long createdAt;

        Category(String id, String name, int color, long createdAt) {
            this.id = id;
            this.name = name;
            this.color = color;
            this.createdAt = createdAt;
        }

        String displayName() {
            return name.isEmpty() ? MainActivity.text("名前なし", "Unnamed") : name;
        }
    }

    static final class FavoritePost {
        final String categoryId;
        final String url;
        final String title;
        final int number;
        final String name;
        final String date;
        final String body;
        final long savedAt;

        FavoritePost(String categoryId, String url, String title, int number, String name,
                     String date, String body, long savedAt) {
            this.categoryId = categoryId;
            this.url = url;
            this.title = title;
            this.number = number;
            this.name = name;
            this.date = date;
            this.body = body;
            this.savedAt = savedAt;
        }
    }
}
