package io.github.cuspidroid;

/** Bounds cumulative snapshot copies during fast downloads while keeping slow ones visible. */
final class DatProgressPolicy {
    private int publishedPosts;
    private long publishedAt;

    boolean shouldPublish(int posts, long nowMillis) {
        if (posts <= publishedPosts) {
            return false;
        }
        if (publishedPosts > 0 && posts < (long) publishedPosts * 2
                && nowMillis - publishedAt < 100) {
            return false;
        }
        publishedPosts = posts;
        publishedAt = nowMillis;
        return true;
    }
}
