package io.github.cuspidroid;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FavoritePostsStoreTest {
    @Test
    public void canonicalThreadUrl_removesPostRangeAndQuery() {
        assertEquals("https://example.test/test/read.cgi/news/1234567890",
                FavoritePostsStore.canonicalThreadUrl(
                        "https://example.test/test/read.cgi/news/1234567890/50-100?foo=bar#post"));
    }

    @Test
    public void postKey_keepsPostNumberSeparateFromThreadUrl() {
        assertEquals("https://example.test/test/read.cgi/news/1234567890#42",
                FavoritePostsStore.postKey(
                        "https://example.test/test/read.cgi/news/1234567890/l50", 42));
    }

    @Test
    public void sortPostsNewestFirst_ordersBySavedAtDescending() {
        FavoritePostsStore.FavoritePost oldPost = post(100L);
        FavoritePostsStore.FavoritePost newestPost = post(300L);
        FavoritePostsStore.FavoritePost middlePost = post(200L);
        List<FavoritePostsStore.FavoritePost> posts = new ArrayList<>(
                Arrays.asList(oldPost, newestPost, middlePost));

        FavoritePostsStore.sortPostsNewestFirst(posts);

        assertEquals(Arrays.asList(newestPost, middlePost, oldPost), posts);
    }

    private FavoritePostsStore.FavoritePost post(long savedAt) {
        return new FavoritePostsStore.FavoritePost("category", "https://example.test/thread",
                "title", 1, "name", "date", "body", savedAt);
    }
}
