package io.github.cuspidroid;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HissiPostHeaderParserTest {
    @Test
    public void parse_standardHeaderSeparatesThreadNumberNameAndMeta() {
        HissiPostHeaderParser.Result result = HissiPostHeaderParser.parse(
                "Thread title 819 : Anonymous [sage] : 2026/08/04 ID:test",
                "Thread title", "Anonymous");

        assertEquals(819, result.number);
        assertEquals("Anonymous", result.name);
        assertEquals("[sage] : 2026/08/04 ID:test", result.meta);
    }

    @Test
    public void parse_duplicateLinkedNumberKeepsOnlyOneNumber() {
        HissiPostHeaderParser.Result result = HissiPostHeaderParser.parse(
                "Thread title 819 819: Anonymous (ﾜｯﾁｮｲ) 2026/08/04 ID:test",
                "Thread title", "Anonymous");

        assertEquals(819, result.number);
        assertEquals("Anonymous", result.name);
        assertEquals("(ﾜｯﾁｮｲ) 2026/08/04 ID:test", result.meta);
    }

    @Test
    public void stripDuplicateNumberPrefix_acceptsFullwidthColon() {
        assertEquals("Anonymous", HissiPostHeaderParser.stripDuplicateNumberPrefix(
                "819\uff1a Anonymous", 819));
    }

    @Test
    public void stripDuplicateNumberPrefix_removesRepeatedNumberFromCachedPost() {
        assertEquals("Anonymous", HissiPostHeaderParser.stripDuplicateNumberPrefix(
                "819 819: Anonymous", 819));
    }
}
