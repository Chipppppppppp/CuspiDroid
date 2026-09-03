package io.github.cuspidroid;

final class ThreadScrollPosition {
    private ThreadScrollPosition() {
    }

    static int centeredBoundary(int boundaryTop, int viewportHeight, int maxScrollY) {
        return clamp(boundaryTop - viewportHeight / 2, maxScrollY);
    }

    static int newPostPeek(int firstNewPostTop, int viewportHeight, int visibleHeight, int maxScrollY) {
        return clamp(firstNewPostTop - viewportHeight + visibleHeight, maxScrollY);
    }

    private static int clamp(int value, int maxScrollY) {
        return Math.max(0, Math.min(value, Math.max(0, maxScrollY)));
    }
}
