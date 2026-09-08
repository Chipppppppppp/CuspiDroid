package io.github.cuspidroid;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DatStreamReaderTest {
    @Test
    public void emitsCompleteLinesInBatchesAndReturnsOriginalBody() throws Exception {
        Charset charset = Charset.forName("MS932");
        byte[] source = "名前<>mail<>日付<>本文<>題名\r\n二人目<>mail<>日付<>本文<>\n末尾<>mail<>日付<>本文<>"
                .getBytes(charset);
        List<List<String>> batches = new ArrayList<>();

        byte[] result = DatStreamReader.read(oneByteAtATime(source), charset, 2,
                (lines, bytesRead) -> batches.add(lines));

        assertArrayEquals(source, result);
        assertEquals(2, batches.size());
        assertEquals(Arrays.asList(
                "名前<>mail<>日付<>本文<>題名",
                "二人目<>mail<>日付<>本文<>"), batches.get(0));
        assertEquals(Arrays.asList("末尾<>mail<>日付<>本文<>"), batches.get(1));
    }

    @Test
    public void reportsBytesConsumedAtEachCompleteBatch() throws Exception {
        byte[] source = "one\ntwo\nthree\n".getBytes(Charset.forName("UTF-8"));
        List<Long> positions = new ArrayList<>();

        DatStreamReader.read(new ByteArrayInputStream(source), Charset.forName("UTF-8"), 2,
                (lines, bytesRead) -> positions.add(bytesRead));

        assertEquals(Arrays.asList(8L, 14L), positions);
    }

    private InputStream oneByteAtATime(byte[] source) {
        return new FilterInputStream(new ByteArrayInputStream(source)) {
            @Override
            public int read(byte[] buffer, int offset, int length) throws IOException {
                return super.read(buffer, offset, Math.min(1, length));
            }
        };
    }
}
