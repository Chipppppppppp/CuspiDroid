package io.github.cuspidroid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class IncrementalPostsTest {
    @Test public void snapshotAddsOnlyNewNumbersIncludingFutabaNumberGaps() {
        assertEquals(Arrays.asList(105, 110), IncrementalPosts.newPostsFromSnapshot(
                Arrays.asList(100, 102), Arrays.asList(100, 105, 110), n -> n));
    }

    @Test public void unchangedSnapshotDoesNotDuplicatePosts() {
        assertTrue(IncrementalPosts.newPostsFromSnapshot(
                Arrays.asList(1, 2), Arrays.asList(1, 2), n -> n).isEmpty());
    }

    @Test public void invalidSnapshotsCannotChangeLoadedPosts() {
        List<Integer> loaded = Arrays.asList(1, 2);
        for (List<Integer> snapshot : Arrays.asList(Arrays.<Integer>asList(), Arrays.asList(1),
                Arrays.asList(1, 3, 3), Arrays.asList(2, 1, 3))) {
            assertThrows(IllegalStateException.class,
                    () -> IncrementalPosts.newPostsFromSnapshot(loaded, snapshot, n -> n));
            assertEquals(Arrays.asList(1, 2), loaded);
        }
    }
    private static class Post {
        final int number;
        Post(int number) { this.number = number; }
    }

    @Test public void keepsLoadedObjectsAndAllowsGapsInPostNumbers() {
        Post old = new Post(3);
        Post added = new Post(5);
        List<Post> posts = new ArrayList<>(Arrays.asList(old));
        Map<Integer, Post> index = new LinkedHashMap<>();
        index.put(3, old);
        IncrementalPosts.append(posts, index, Arrays.asList(added), p -> p.number);
        assertSame(old, posts.get(0));
        assertSame(old, index.get(3));
        assertSame(added, posts.get(1));
        assertSame(added, index.get(5));
    }

    @Test public void malformedBatchDoesNotPartiallyChangeLoadedPosts() {
        Post old = new Post(3);
        List<Post> posts = new ArrayList<>(Arrays.asList(old));
        Map<Integer, Post> index = new LinkedHashMap<>();
        index.put(3, old);
        assertThrows(IllegalStateException.class, () -> IncrementalPosts.append(
                posts, index, Arrays.asList(new Post(4), new Post(3)), p -> p.number));
        assertEquals(1, posts.size());
        assertEquals(1, index.size());
        assertSame(old, posts.get(0));
    }

    @Test public void rejectsDuplicatesWithinNewPosts() {
        assertThrows(IllegalStateException.class, () -> IncrementalPosts.append(
                new ArrayList<>(), new LinkedHashMap<>(),
                Arrays.asList(new Post(4), new Post(4)), p -> p.number));
    }
}
