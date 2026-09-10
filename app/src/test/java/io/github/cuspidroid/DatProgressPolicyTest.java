package io.github.cuspidroid;

import org.junit.Test;

import static org.junit.Assert.*;

public class DatProgressPolicyTest {
    @Test
    public void fastDownloadCopiesOnlyGeometricallyGrowingSnapshots() {
        DatProgressPolicy policy = new DatProgressPolicy();
        int copiedPosts = 0;
        for (int posts = 40; posts <= 10000; posts += 40) {
            if (policy.shouldPublish(posts, 0)) {
                copiedPosts += posts;
            }
        }
        assertTrue(copiedPosts < 20000);
    }

    @Test
    public void slowDownloadPublishesWithoutWaitingForDoubleThePosts() {
        DatProgressPolicy policy = new DatProgressPolicy();
        assertFalse(policy.shouldPublish(0, 0));
        assertTrue(policy.shouldPublish(40, 0));
        assertTrue(policy.shouldPublish(80, 1));
        assertFalse(policy.shouldPublish(120, 100));
        assertTrue(policy.shouldPublish(120, 101));
        assertFalse(policy.shouldPublish(120, 300));
    }
}
