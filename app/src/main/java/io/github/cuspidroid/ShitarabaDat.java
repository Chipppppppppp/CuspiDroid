package io.github.cuspidroid;

/** rawmode.cgi uses post ranges, not HTTP byte ranges. */
final class ShitarabaDat {
    static String newPostsUrl(String baseUrl, int lastPostNumber) {
        if (lastPostNumber < 1 || lastPostNumber == Integer.MAX_VALUE) {
            throw new IllegalStateException("Invalid incremental post number");
        }
        return baseUrl + (baseUrl.endsWith("/") ? "" : "/") + (lastPostNumber + 1) + "-n";
    }

    /** Reject error pages, truncated records and unexpected old posts before merging. */
    static void validate(String body, int lastPostNumber) {
        if (body.isEmpty()) return;
        if (!body.endsWith("\n")) {
            throw new IllegalStateException("Incomplete incremental Shitaraba response");
        }
        int previous = lastPostNumber;
        for (String line : body.split("\n")) {
            if (line.trim().isEmpty()) continue;
            String[] fields = line.split("<>", -1);
            int number;
            try {
                number = Integer.parseInt(fields[0]);
            } catch (NumberFormatException error) {
                throw new IllegalStateException("Invalid incremental Shitaraba response", error);
            }
            if (fields.length < 7 || number <= previous) {
                throw new IllegalStateException("Invalid incremental Shitaraba post sequence");
            }
            previous = number;
        }
    }
}
