package io.github.cuspidroid;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThreadReadBoundaryTest {
    @Test
    public void unreadPostCannotAttachToReadParent() {
        assertFalse(ThreadReadBoundary.canAttachToParent(11, 5, 10));
    }

    @Test
    public void unreadPostCanAttachToUnreadParent() {
        assertTrue(ThreadReadBoundary.canAttachToParent(12, 11, 10));
    }

    @Test
    public void readPostCanAttachWithinReadTree() {
        assertTrue(ThreadReadBoundary.canAttachToParent(9, 5, 10));
    }
}
