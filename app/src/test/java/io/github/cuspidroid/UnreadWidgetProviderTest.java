package io.github.cuspidroid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnreadWidgetProviderTest {
    @Test
    public void displayCount_keepsFiveDigitTotals() {
        assertEquals("99999", UnreadWidgetProvider.displayCount(99999));
    }

    @Test
    public void displayCount_compactsTotalsAboveFiveDigits() {
        assertEquals("99K+", UnreadWidgetProvider.displayCount(100000));
    }

    @Test
    public void isThreadUrl_rejectsBoardUrl() {
        assertEquals(false, UnreadWidgetProvider.isThreadUrl("https://example.test/news/"));
    }

    @Test
    public void isThreadUrl_acceptsReadCgiAndShortThreadUrls() {
        assertEquals(true, UnreadWidgetProvider.isThreadUrl(
                "https://example.test/test/read.cgi/news/1234567890/"));
        assertEquals(true, UnreadWidgetProvider.isThreadUrl(
                "https://example.test/news/1234567890/"));
    }
}
