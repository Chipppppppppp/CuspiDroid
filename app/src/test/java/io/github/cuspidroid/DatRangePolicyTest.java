package io.github.cuspidroid;

import org.junit.Test;

import static org.junit.Assert.*;

public class DatRangePolicyTest {
    @Test public void acceptsNewBytesAndExactEndOfFile() {
        DatRangePolicy.validate(206, "bytes 100-149/150", 100);
        DatRangePolicy.validate(416, "bytes */100", 100);
        assertEquals(150, DatRangePolicy.nextOffset("bytes 100-149/150", 50));
    }

    @Test public void rejectsIgnoredRangesAndOverlappingOldBytes() {
        rejects(200, null, 100);
        rejects(206, "bytes 0-149/150", 100);
        rejects(206, "bytes 101-149/150", 100);
        rejects(206, null, 100);
        rejects(206, "bytes 100-150/150", 100);
    }

    @Test public void doesNotTreatTruncatedOrUnknownFilesAsUnchanged() {
        rejects(416, "bytes */90", 100);
        rejects(416, "bytes */110", 100);
        rejects(416, null, 100);
        rejects(500, null, 100);
    }

    @Test public void advancesOnlyThroughBytesActuallyReturned() {
        DatRangePolicy.validate(206, "bytes 100-149/200", 100);
        assertEquals(150, DatRangePolicy.nextOffset("bytes 100-149/200", 50));
        assertThrows(IllegalStateException.class,
                () -> DatRangePolicy.nextOffset("bytes 100-149/200", 20));
    }

    private void rejects(int status, String range, long start) {
        assertThrows(IllegalStateException.class, () -> DatRangePolicy.validate(status, range, start));
    }
}
