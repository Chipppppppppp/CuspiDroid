package io.github.cuspidroid;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class BookmarkOverviewStartupPolicyTest {
    @Test
    public void immediate_memorySnapshotSkipsPersistentReads() {
        AtomicInteger persistedCalls = new AtomicInteger();
        AtomicInteger canonicalCalls = new AtomicInteger();

        String result = BookmarkOverviewStartupPolicy.immediate(
                "memory",
                () -> {
                    persistedCalls.incrementAndGet();
                    return "persisted";
                },
                () -> {
                    canonicalCalls.incrementAndGet();
                    return "canonical";
                });

        assertEquals("memory", result);
        assertEquals(0, persistedCalls.get());
        assertEquals(0, canonicalCalls.get());
    }

    @Test
    public void immediate_persistedSeedSkipsCanonicalRead() {
        AtomicInteger canonicalCalls = new AtomicInteger();

        String result = BookmarkOverviewStartupPolicy.immediate(
                null,
                () -> "persisted",
                () -> {
                    canonicalCalls.incrementAndGet();
                    return "canonical";
                });

        assertEquals("persisted", result);
        assertEquals(0, canonicalCalls.get());
    }

    @Test
    public void immediate_missingOrBrokenCacheFallsBackToCanonicalSeed() {
        AtomicInteger canonicalCalls = new AtomicInteger();

        String result = BookmarkOverviewStartupPolicy.immediate(
                null,
                () -> null,
                () -> {
                    canonicalCalls.incrementAndGet();
                    return "canonical";
                });

        assertEquals("canonical", result);
        assertEquals(1, canonicalCalls.get());
    }
}
