package io.github.cuspidroid;

import org.junit.Test;
import static org.junit.Assert.*;

public class ShitarabaDatTest {
    @Test public void requestsNextNumberAndSuppressesFirstPost() {
        assertEquals("https://jbbs.shitaraba.net/bbs/rawmode.cgi/game/123/1234567890/101-n",
                ShitarabaDat.newPostsUrl("https://jbbs.shitaraba.net/bbs/rawmode.cgi/game/123/1234567890/", 100));
    }

    @Test public void acceptsNewPostsWithDeletedNumberGapsAndEmptyFields() {
        ShitarabaDat.validate("101<>名前<><>日付<>本文<><>ID\n103<><><><><><>\n", 100);
    }

    @Test public void acceptsNoNewPosts() {
        ShitarabaDat.validate("", 100);
        ShitarabaDat.validate("\n", 100);
    }

    @Test public void rejectsOldDuplicateTruncatedAndErrorResponses() {
        String record = "101<>名前<><>日付<>本文<><>ID\n";
        for (String body : new String[]{"1<>名前<><>日付<>本文<><>ID\n",
                record + record, record.trim(), "ERROR: THREAD NOT FOUND\n", "<html>error</html>\n",
                "101<>名前<>メール<>日付\n"}) {
            assertThrows(IllegalStateException.class, () -> ShitarabaDat.validate(body, 100));
        }
    }
}
