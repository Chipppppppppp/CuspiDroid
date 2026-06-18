package io.github.cuspidroid;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class AppCache {
    static final int DEFAULT_MAX_MB = 256;
    static final int MIN_MAX_MB = 32;
    static final int MAX_MAX_MB = 2048;

    private AppCache() {
    }

    static boolean enabled(SharedPreferences preferences) {
        return preferences == null || preferences.getBoolean(MainActivity.PREF_CACHE_ENABLED, true);
    }

    static long maxBytes(SharedPreferences preferences) {
        int mb = preferences == null
                ? DEFAULT_MAX_MB
                : preferences.getInt(MainActivity.PREF_CACHE_MAX_MB, DEFAULT_MAX_MB);
        mb = Math.max(MIN_MAX_MB, Math.min(MAX_MAX_MB, mb));
        return mb * 1024L * 1024L;
    }

    static File root(Context context) {
        return new File(context.getCacheDir(), "app_cache");
    }

    static File dir(Context context, String name) {
        return new File(root(context), name);
    }

    static File file(Context context, String name, String key, String extension) throws Exception {
        return new File(dir(context, name), sha256(key) + extension);
    }

    static byte[] read(Context context, SharedPreferences preferences, String name, String key, String extension) {
        if (!enabled(preferences)) {
            return null;
        }
        try {
            File file = file(context, name, key, extension);
            if (!file.exists() || file.length() <= 0) {
                return null;
            }
            file.setLastModified(System.currentTimeMillis());
            return readBytes(file);
        } catch (Exception ignored) {
            return null;
        }
    }

    static void write(Context context, SharedPreferences preferences, String name, String key,
                      String extension, byte[] bytes) {
        if (!enabled(preferences) || bytes == null || bytes.length == 0) {
            return;
        }
        try {
            File dir = dir(context, name);
            if (!dir.exists() && !dir.mkdirs()) {
                return;
            }
            File file = file(context, name, key, extension);
            try (FileOutputStream output = new FileOutputStream(file)) {
                output.write(bytes);
            }
            file.setLastModified(System.currentTimeMillis());
            prune(context, preferences);
        } catch (Exception ignored) {
        }
    }

    static long size(Context context) {
        return sizeOf(root(context))
                + sizeOf(new File(context.getCacheDir(), "threads"))
                + sizeOf(new File(context.getCacheDir(), "imgur"));
    }

    static void clear(Context context) {
        delete(root(context));
        delete(new File(context.getCacheDir(), "threads"));
        delete(new File(context.getCacheDir(), "imgur"));
    }

    static void prune(Context context, SharedPreferences preferences) {
        long max = maxBytes(preferences);
        List<File> files = new ArrayList<>();
        collectFiles(root(context), files);
        collectFiles(new File(context.getCacheDir(), "threads"), files);
        collectFiles(new File(context.getCacheDir(), "imgur"), files);
        long total = 0L;
        for (File file : files) {
            total += Math.max(0L, file.length());
        }
        if (total <= max) {
            return;
        }
        files.sort(Comparator.comparingLong(File::lastModified));
        for (File file : files) {
            long length = Math.max(0L, file.length());
            if (file.delete()) {
                total -= length;
            }
            if (total <= max) {
                break;
            }
        }
    }

    static String formatBytes(long bytes) {
        if (bytes >= 1024L * 1024L * 1024L) {
            return String.format(Locale.ROOT, "%.1f GB", bytes / (1024f * 1024f * 1024f));
        }
        if (bytes >= 1024L * 1024L) {
            return String.format(Locale.ROOT, "%.1f MB", bytes / (1024f * 1024f));
        }
        if (bytes >= 1024L) {
            return String.format(Locale.ROOT, "%.1f KB", bytes / 1024f);
        }
        return bytes + " B";
    }

    private static byte[] readBytes(File file) throws Exception {
        try (InputStream input = new FileInputStream(file)) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }

    private static long sizeOf(File file) {
        if (file == null || !file.exists()) {
            return 0L;
        }
        if (file.isFile()) {
            return Math.max(0L, file.length());
        }
        long total = 0L;
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                total += sizeOf(child);
            }
        }
        return total;
    }

    private static void collectFiles(File file, List<File> files) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isFile()) {
            files.add(file);
            return;
        }
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                collectFiles(child, files);
            }
        }
    }

    private static void delete(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    delete(child);
                }
            }
        }
        file.delete();
    }

    private static String sha256(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((value == null ? "" : value).getBytes("UTF-8"));
        StringBuilder builder = new StringBuilder();
        for (byte b : hash) {
            builder.append(String.format(Locale.ROOT, "%02x", b));
        }
        return builder.toString();
    }
}
