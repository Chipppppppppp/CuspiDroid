package io.github.cuspidroid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Validate headers before reading a response, so ignored ranges never download old posts. */
final class DatRangePolicy {
    private static final Pattern RANGE = Pattern.compile("bytes (\\d+)-(\\d+)/(\\d+)");

    static void validate(int status, String contentRange, long start) {
        if (status == 416 && ("bytes */" + start).equals(contentRange)) return;
        Matcher match = RANGE.matcher(contentRange == null ? "" : contentRange);
        if (status == 206 && match.matches()) {
            try {
                long first = Long.parseLong(match.group(1));
                long last = Long.parseLong(match.group(2));
                long total = Long.parseLong(match.group(3));
                if (first == start && last >= first && total > last) return;
            } catch (NumberFormatException ignored) {
            }
        }
        throw new IllegalStateException("Incremental update unavailable: invalid Content-Range");
    }

    static long nextOffset(String contentRange, long bytesRead) {
        Matcher match = RANGE.matcher(contentRange == null ? "" : contentRange);
        if (!match.matches()) throw new IllegalStateException("Invalid incremental response");
        long first = Long.parseLong(match.group(1));
        long last = Long.parseLong(match.group(2));
        if (bytesRead != last - first + 1) {
            throw new IllegalStateException("Incomplete incremental response");
        }
        return last + 1;
    }
}
