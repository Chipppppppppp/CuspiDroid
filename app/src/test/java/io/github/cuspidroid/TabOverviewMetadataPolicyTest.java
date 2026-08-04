package io.github.cuspidroid;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TabOverviewMetadataPolicyTest {
    @Test
    public void boardName_existingNameWinsWithoutCallingResolver() {
        AtomicInteger calls = new AtomicInteger();

        String result = TabOverviewMetadataPolicy.boardName("Saved board", true, () -> {
            calls.incrementAndGet();
            return "Resolved board";
        });

        assertEquals("Saved board", result);
        assertEquals(0, calls.get());
    }

    @Test
    public void boardName_notRequiredDoesNotCallResolver() {
        AtomicInteger calls = new AtomicInteger();

        String result = TabOverviewMetadataPolicy.boardName("", false, () -> {
            calls.incrementAndGet();
            return "Resolved board";
        });

        assertEquals("", result);
        assertEquals(0, calls.get());
    }

    @Test
    public void boardName_requiredResolvesMissingNameOnce() {
        AtomicInteger calls = new AtomicInteger();

        String result = TabOverviewMetadataPolicy.boardName("  ", true, () -> {
            calls.incrementAndGet();
            return "Resolved board";
        });

        assertEquals("Resolved board", result);
        assertEquals(1, calls.get());
    }

    @Test
    public void hasReadHistory_readPostNumberSkipsLookup() {
        AtomicInteger calls = new AtomicInteger();

        boolean result = TabOverviewMetadataPolicy.hasReadHistory(1, 0, () -> {
            calls.incrementAndGet();
            return false;
        });

        assertTrue(result);
        assertEquals(0, calls.get());
    }

    @Test
    public void hasReadHistory_responsesSkipLookup() {
        AtomicInteger calls = new AtomicInteger();

        boolean result = TabOverviewMetadataPolicy.hasReadHistory(0, 10, () -> {
            calls.incrementAndGet();
            return false;
        });

        assertTrue(result);
        assertEquals(0, calls.get());
    }

    @Test
    public void hasReadHistory_withoutKnownActivityUsesLookup() {
        AtomicInteger calls = new AtomicInteger();

        boolean found = TabOverviewMetadataPolicy.hasReadHistory(0, 0, () -> {
            calls.incrementAndGet();
            return true;
        });
        boolean missing = TabOverviewMetadataPolicy.hasReadHistory(0, 0, () -> {
            calls.incrementAndGet();
            return false;
        });

        assertTrue(found);
        assertFalse(missing);
        assertEquals(2, calls.get());
    }
}
