package io.github.cuspidroid;

final class ThreadReadBoundary {
    private ThreadReadBoundary() {
    }

    static boolean isUnread(int postNumber, int readPostNumber) {
        return postNumber > readPostNumber;
    }

    static boolean canAttachToParent(int postNumber, int parentNumber, int readPostNumber) {
        return !isUnread(postNumber, readPostNumber)
                || isUnread(parentNumber, readPostNumber);
    }
}
