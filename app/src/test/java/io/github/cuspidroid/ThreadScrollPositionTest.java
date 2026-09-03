package io.github.cuspidroid;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThreadScrollPositionTest {
    @Test
    public void centeredBoundaryPlacesBoundaryAtViewportCenter() {
        assertEquals(700, ThreadScrollPosition.centeredBoundary(1200, 1000, 3000));
    }

    @Test
    public void centeredBoundaryClampsNearTop() {
        assertEquals(0, ThreadScrollPosition.centeredBoundary(200, 1000, 3000));
    }

    @Test
    public void newPostPeekShowsOnlyRequestedLeadingArea() {
        assertEquals(240, ThreadScrollPosition.newPostPeek(1200, 1000, 40, 3000));
    }

    @Test
    public void newPostPeekClampsToContentBottom() {
        assertEquals(300, ThreadScrollPosition.newPostPeek(1400, 1000, 40, 300));
    }
}
