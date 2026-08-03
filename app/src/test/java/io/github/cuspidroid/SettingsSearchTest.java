package io.github.cuspidroid;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SettingsSearchTest {
    @Test
    public void matchesJapaneseSubstring() {
        assertTrue(SettingsSearch.matches("\u30d0\u30c3\u30af", "\u4fdd\u5b58\u5bb9\u91cf\u3068\u30d0\u30c3\u30af\u30a2\u30c3\u30d7"));
    }

    @Test
    public void matchesAllSpaceSeparatedTermsIgnoringCase() {
        assertTrue(SettingsSearch.matches("media blur", "Blur media in warning posts"));
        assertTrue(SettingsSearch.matches("SYNC2CH\u3000PASSWORD", "Sync2ch API connection password"));
        assertFalse(SettingsSearch.matches("media sync", "Blur media in warning posts"));
    }

    @Test
    public void emptyQueryMatchesEverything() {
        assertTrue(SettingsSearch.matches("  ", "Any setting"));
    }
}
