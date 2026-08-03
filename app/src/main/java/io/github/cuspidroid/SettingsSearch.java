package io.github.cuspidroid;

import java.util.Locale;

/** Text matching used by the settings overview search. */
final class SettingsSearch {
    private SettingsSearch() {
    }

    static boolean matches(String query, String candidate) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isEmpty()) {
            return true;
        }
        String normalizedCandidate = normalize(candidate);
        for (String term : normalizedQuery.split(" ")) {
            if (!term.isEmpty() && !normalizedCandidate.contains(term)) {
                return false;
            }
        }
        return true;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replace('\u3000', ' ')
                .trim()
                .replaceAll("\\s+", " ");
    }
}
