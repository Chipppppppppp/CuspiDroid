package io.github.cuspidroid;

import org.junit.Test;
import static org.junit.Assert.*;

public class BbsMenuFastPathTest {
    @Test public void ordinaryBoardAndJapaneseLabelNeedNoHtmlParser() {
        assertEquals("android", BbsMenuFastPath.boardName("/android/"));
        assertEquals("Android", BbsMenuFastPath.plainLabel(" Android "));
        assertEquals("ニュース速報", BbsMenuFastPath.plainLabel("ニュース速報"));
        assertEquals("PC ABC", BbsMenuFastPath.plainLabel("PC ABC"));
    }

    @Test public void nonBoardPathsDelegateToExistingValidation() {
        for (String path : new String[]{null, "", "/", "/android", "/test/read.cgi/android/123/",
                "/bbsmenu.html/", "/cdn-cgi/", "/IMAGE/", "/sp/", "/category/123/"}) {
            assertNull(path, BbsMenuFastPath.boardName(path));
        }
    }

    @Test public void entitiesMarkupAndHtmlWhitespaceKeepFullDecoding() {
        for (String label : new String[]{"A&amp;B", "<b>板</b>", "A  B", "A\tB", "A\nB", "A\u00a0B"}) {
            assertNull(label, BbsMenuFastPath.plainLabel(label));
        }
    }
}
