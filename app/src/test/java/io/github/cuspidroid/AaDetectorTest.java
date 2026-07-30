package io.github.cuspidroid;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AaDetectorTest {
    @Test
    public void whitespaceOnlyLinesAreNotCounted() {
        String body = "  a\n\t\tb\n.,c\n│─d\n   \n\u3000\u3000\n\u200B\uFEFF";

        AaDetector.Metrics metrics = AaDetector.metrics(body);

        assertEquals(4, metrics.lineCount);
        assertEquals(8, metrics.score);
        assertTrue(metrics.aa);
    }

    @Test
    public void mixedLeadingLayoutCharactersScoreTwoPoints() {
        String body = ".,a\n，。b\n'\"c\n／＼d";

        assertTrue(AaDetector.isLikelyAa(body));
    }

    @Test
    public void boxDrawingAndFullwidthVariantsAreAccepted() {
        String body = "┌─a\n│｜b\n＿￣c\n；：d";

        assertTrue(AaDetector.isLikelyAa(body));
    }

    @Test
    public void logicalAndSetOperatorSymbolsAreAccepted() {
        String body = "∧∨a\n∩∪b\n∨∩c\n∪∧d";

        assertTrue(AaDetector.isLikelyAa(body));
    }

    @Test
    public void fewerThanFourNonblankLinesAreRejected() {
        String body = ".,a\n.,b\n.,c\n   \n\u3000";

        assertFalse(AaDetector.isLikelyAa(body));
    }

    @Test
    public void ratioMustBeGreaterThanPointSevenFive() {
        String body = ".,a\nplain  b\nplain c\nplain d";

        assertFalse(AaDetector.isLikelyAa(body));
    }

    @Test
    public void ratioAbovePointSevenFiveIsAccepted() {
        String body = ".,a\n.,b\nplain c\nplain d\nplain e";

        assertTrue(AaDetector.isLikelyAa(body));
    }
}
