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
}
