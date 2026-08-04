package io.github.cuspidroid;

final class BookmarkOverviewStartupPolicy {
    interface Loader<T> {
        T load();
    }

    private BookmarkOverviewStartupPolicy() {
    }

    static <T> T immediate(T memorySnapshot, Loader<T> persistedSeed, Loader<T> canonicalSeed) {
        if (memorySnapshot != null) {
            return memorySnapshot;
        }
        T persisted = persistedSeed == null ? null : persistedSeed.load();
        if (persisted != null) {
            return persisted;
        }
        return canonicalSeed == null ? null : canonicalSeed.load();
    }
}
