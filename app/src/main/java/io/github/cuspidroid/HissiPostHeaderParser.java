package io.github.cuspidroid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses the text portion of a Hissi post header without depending on Android APIs. */
final class HissiPostHeaderParser {
    private static final Pattern POST_NUMBER = Pattern.compile("(\\d+)\\s*[\\uFF1A:]");

    private HissiPostHeaderParser() {
    }

    static Result parse(String plainHeader, String threadTitle, String rawName) {
        String remainder = removeFirst(safe(plainHeader).trim(), safe(threadTitle).trim());
        Matcher numberMatcher = POST_NUMBER.matcher(remainder);
        int number = 0;
        String meta = remainder;
        if (numberMatcher.find()) {
            number = positiveInt(numberMatcher.group(1));
            meta = remainder.substring(numberMatcher.end()).trim();
        }

        String originalName = safe(rawName).trim();
        String name = stripDuplicateNumberPrefix(originalName, number);
        meta = removeLeading(meta, originalName);
        meta = removeLeading(meta, name);
        meta = stripDuplicateNumberPrefix(meta, number);
        return new Result(number, name, meta.trim());
    }

    static String stripDuplicateNumberPrefix(String value, int number) {
        String text = safe(value).trim();
        if (number <= 0 || text.isEmpty()) return text;
        String token = "(?:>>\\s*)?" + number;
        return text.replaceFirst("^\\s*(?:" + token + "\\s+)*" + token
                + "\\s*[\\uFF1A:]\\s*", "").trim();
    }

    private static String removeFirst(String value, String target) {
        if (target.isEmpty()) return value;
        int index = value.indexOf(target);
        if (index < 0) return value;
        return (value.substring(0, index) + value.substring(index + target.length())).trim();
    }

    private static String removeLeading(String value, String prefix) {
        if (prefix.isEmpty()) return value;
        return value.replaceFirst("^\\s*" + Pattern.quote(prefix) + "\\s*", "").trim();
    }

    private static int positiveInt(String value) {
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    static final class Result {
        final int number;
        final String name;
        final String meta;

        Result(int number, String name, String meta) {
            this.number = number;
            this.name = name;
            this.meta = meta;
        }
    }
}
