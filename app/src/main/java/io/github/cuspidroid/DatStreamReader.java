package io.github.cuspidroid;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

final class DatStreamReader {
    interface LineBatchListener {
        void onLines(List<String> lines, long bytesRead) throws Exception;
    }

    private DatStreamReader() {
    }

    static byte[] read(InputStream input, Charset charset, int batchSize,
                       LineBatchListener listener) throws Exception {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        List<String> lines = new ArrayList<>(Math.max(1, batchSize));
        byte[] buffer = new byte[8192];
        long bytesRead = 0;
        int read;
        while ((read = input.read(buffer)) != -1) {
            body.write(buffer, 0, read);
            for (int i = 0; i < read; i++) {
                byte value = buffer[i];
                bytesRead++;
                if (value == '\n') {
                    addLine(lines, line, charset);
                    if (listener != null && lines.size() >= batchSize) {
                        listener.onLines(new ArrayList<>(lines), bytesRead);
                        lines.clear();
                    }
                } else {
                    line.write(value);
                }
            }
        }
        if (line.size() > 0) {
            addLine(lines, line, charset);
        }
        if (listener != null && !lines.isEmpty()) {
            listener.onLines(new ArrayList<>(lines), bytesRead);
        }
        return body.toByteArray();
    }

    private static void addLine(List<String> lines, ByteArrayOutputStream line, Charset charset) {
        byte[] bytes = line.toByteArray();
        int length = bytes.length;
        if (length > 0 && bytes[length - 1] == '\r') {
            length--;
        }
        lines.add(new String(bytes, 0, length, charset));
        line.reset();
    }
}
