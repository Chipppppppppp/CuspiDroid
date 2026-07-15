package io.github.cuspidroid;

import android.content.Context;
import android.util.AtomicFile;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;

/** Stores heavyweight tab page caches outside SharedPreferences. */
final class TabPayloadStore {
    private static final String DIRECTORY = "tab_payloads";
    private static final String SUFFIX = ".json";
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9_-]{1,80}");

    private final File directory;

    TabPayloadStore(Context context) {
        directory = new File(context.getFilesDir(), DIRECTORY);
    }

    synchronized String read(String id) {
        File file = payloadFile(id);
        if (file == null || !file.isFile()) {
            return "";
        }
        try {
            return new String(new AtomicFile(file).readFully(), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return "";
        }
    }

    synchronized boolean write(String id, String json) {
        File target = payloadFile(id);
        if (target == null || json == null || json.isEmpty()) {
            return false;
        }
        if (!directory.isDirectory() && !directory.mkdirs()) {
            return false;
        }
        AtomicFile atomicFile = new AtomicFile(target);
        FileOutputStream output = null;
        try {
            output = atomicFile.startWrite();
            output.write(json.getBytes(StandardCharsets.UTF_8));
            atomicFile.finishWrite(output);
            return true;
        } catch (Exception ignored) {
            if (output != null) {
                atomicFile.failWrite(output);
            }
            return false;
        }
    }

    synchronized void removeStale(Set<String> activeIds) {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(SUFFIX));
        if (files == null) {
            return;
        }
        for (File file : files) {
            String name = file.getName();
            String id = name.substring(0, name.length() - SUFFIX.length());
            if (activeIds == null || !activeIds.contains(id)) {
                file.delete();
            }
        }
    }

    synchronized void clear() {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            file.delete();
        }
    }

    private File payloadFile(String id) {
        if (id == null || !SAFE_ID.matcher(id).matches()) {
            return null;
        }
        return new File(directory, id + SUFFIX);
    }
}
