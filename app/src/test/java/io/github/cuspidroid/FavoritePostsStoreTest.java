package io.github.cuspidroid;

import org.junit.Test;

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
}
