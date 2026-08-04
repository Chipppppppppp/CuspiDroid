package io.github.cuspidroid;

final class TabOverviewMetadataPolicy {
    interface BoardNameResolver {
        String resolve();
    }

    interface HistoryLookup {
        boolean contains();
    }

    private TabOverviewMetadataPolicy() {
    }

    static String boardName(String existingName, boolean required, BoardNameResolver resolver) {
        if (existingName != null && !existingName.trim().isEmpty()) {
            return existingName;
        }
        if (!required || resolver == null) {
            return "";
        }
        String resolved = resolver.resolve();
        return resolved == null ? "" : resolved;
    }

    static boolean hasReadHistory(int readPostNumber, int responses, HistoryLookup historyLookup) {
        if (readPostNumber > 0 || responses > 0) {
            return true;
        }
        return historyLookup != null && historyLookup.contains();
    }
}
