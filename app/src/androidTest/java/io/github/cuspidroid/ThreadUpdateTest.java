package io.github.cuspidroid;

import android.content.Intent;
import android.test.InstrumentationTestCase;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.*;
import java.util.*;

/** Real HTTP and production parsers, with deterministic local BBS responses. */
public class ThreadUpdateTest extends InstrumentationTestCase {
    private MainActivity activity;
    private Fixture server;

    @Override protected void setUp() throws Exception {
        super.setUp();
        Intent intent = new Intent(getInstrumentation().getTargetContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity = (MainActivity) getInstrumentation().startActivitySync(intent);
        getInstrumentation().waitForIdleSync();
        server = new Fixture();
    }

    @Override protected void tearDown() throws Exception {
        server.close();
        getInstrumentation().runOnMainSync(() -> activity.finish());
        super.tearDown();
    }

    public void testFutabaHtmlRefreshKeepsLoadedObjectsAndAbsoluteNumbers() throws Exception {
        String url = server.base() + "/b/res/100.htm";
        Object old = invoke("parseThread", url, futaba(100));
        Object first = posts(old).get(0);
        server.html = futaba(100, 105, 110);
        Object result = invoke("downloadNewThreadPosts", url, old);
        assertEquals(Arrays.asList(100, 105, 110), numbers(result));
        assertSame(first, posts(result).get(0));
        assertEquals(1, posts(old).size());
        assertEquals(2, get(result, "newPostCount"));
        Object unchanged = invoke("downloadNewThreadPosts", url, result);
        assertEquals(0, get(unchanged, "newPostCount"));
        assertEquals(numbers(result), numbers(unchanged));
    }

    public void testIgnoredRangeFallsBackToFullDatWithoutDuplicatingOldPosts() throws Exception {
        Object old = datPage();
        server.dat = dat(3);
        Object result = invoke("downloadNewThreadPosts", threadUrl(), old);
        assertEquals(Arrays.asList(1, 2, 3), numbers(result));
        assertSame(posts(old).get(0), posts(result).get(0));
        assertEquals((long) server.dat.getBytes(StandardCharsets.UTF_8).length, get(result, "datByteLength"));
        assertTrue(server.requests.contains("range"));
        assertTrue(server.requests.contains("full-dat"));
    }

    public void testValidByteRangeStillUsesOnlyTheDifference() throws Exception {
        Object old = datPage();
        server.dat = dat(3);
        server.supportRange = true;
        Object result = invoke("downloadNewThreadPosts", threadUrl(), old);
        assertEquals(Arrays.asList(1, 2, 3), numbers(result));
        assertEquals(Collections.singletonList("range"), server.requests);
    }

    public void testUnchangedDatRangeKeepsOffset() throws Exception {
        Object old = datPage();
        server.dat = dat(2);
        server.supportRange = true;
        Object result = invoke("downloadNewThreadPosts", threadUrl(), old);
        assertEquals(numbers(old), numbers(result));
        assertEquals(get(old, "datByteLength"), get(result, "datByteLength"));
        assertEquals(0, get(result, "newPostCount"));
        assertEquals(Collections.singletonList("range"), server.requests);
    }

    public void testStaleByteOffsetRecoversThroughFullDat() throws Exception {
        Object old = datPage();
        set(old, "datByteLength", 99999L);
        server.dat = dat(3);
        server.supportRange = true;
        Object result = invoke("downloadNewThreadPosts", threadUrl(), old);
        assertEquals(Arrays.asList(1, 2, 3), numbers(result));
        assertEquals((long) server.dat.getBytes(StandardCharsets.UTF_8).length, get(result, "datByteLength"));
    }

    public void testMachiHtmlRefreshPreservesDeletedNumberGaps() throws Exception {
        String url = server.base() + "/bbs/read.cgi/board/1234567890/";
        String one = "<div class=\"res\">1 : <b>Name</b><hr class=\"reshr\">old</div>";
        Object old = invoke("parseThread", url, one);
        server.html = one + "<div class=\"res\">3 : <b>Name</b><hr class=\"reshr\">new</div>";
        Object result = invoke("downloadNewThreadPosts", url, old);
        assertEquals(Arrays.asList(1, 3), numbers(result));
        assertSame(posts(old).get(0), posts(result).get(0));
    }

    public void testHtmlCachedTwoChStyleThreadCanRefreshAndRecoverDatMetadata() throws Exception {
        String html = "<title>Thread</title><dt>1 : <b>Name</b></dt><dd>old</dd>";
        Object old = invoke("parseThread", threadUrl() + "l50", html);
        server.dat = dat(3);
        Object result = invoke("downloadNewThreadPosts", threadUrl() + "l50", old);
        assertEquals(Arrays.asList(1, 2, 3), numbers(result));
        assertTrue(((String) get(result, "datUrl")).endsWith("/board/dat/1234567890.dat"));
    }

    public void testFailedHtmlRefreshDoesNotDiscardLoadedPosts() throws Exception {
        String url = server.base() + "/b/res/100.htm";
        Object old = invoke("parseThread", url, futaba(100, 105));
        server.html = "<html>Server error without posts</html>";
        try {
            invoke("downloadNewThreadPosts", url, old);
            fail("Invalid HTML must not be treated as no new posts");
        } catch (InvocationTargetException expected) {
            assertTrue(expected.getCause() instanceof IllegalStateException);
        }
        assertEquals(Arrays.asList(100, 105), numbers(old));
    }

    public void testCustomDatCandidatesStayOnTheirOwnHost() throws Exception {
        for (String url : Arrays.asList("https://machi.to/bbs/read.cgi/tawara/1234567890/",
                "https://jbbs.shitaraba.net/bbs/read.cgi/game/123/1234567890/",
                "https://mercury.bbspink.org/test/read.cgi/erobbs/1234567890/",
                "https://mercury.bbspink.com/test/read.cgi/erobbs/1234567890/",
                "https://hayabusa.open2ch.net/test/read.cgi/news4vip/1234567890/",
                "https://bbs.eddibb.cc/liveedge/1234567890/",
                "https://afternoontea.st/board/1234567890/",
                "https://tomcat.2ch.sc/test/read.cgi/livejupiter/1234567890/",
                "https://tomcat.2ch.net/test/read.cgi/livejupiter/1234567890/",
                "https://example.org/test/read.cgi/board/1234567890/")) {
            Object address = invoke("datAddress", url);
            assertNotNull(url, address);
            assertEquals(url, invoke("threadHtmlUrl", url));
            List<?> candidates = (List<?>) invoke("datCandidates", address);
            assertFalse(candidates.isEmpty());
            for (Object candidate : candidates) assertEquals(url, new URL(url).getHost(), new URL((String) candidate).getHost());
        }
    }

    private Object datPage() throws Exception {
        Object page = invoke("parseDatThread", threadUrl(), dat(2));
        set(page, "datUrl", server.base() + "/board/dat/1234567890.dat");
        set(page, "datByteLength", (long) dat(2).getBytes(StandardCharsets.UTF_8).length);
        return page;
    }
    private String threadUrl() { return server.base() + "/test/read.cgi/board/1234567890/"; }
    private static String dat(int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 1; i <= count; i++) result.append("名前<>sage<>date<>body").append(i).append("<>Thread\n");
        return result.toString();
    }
    private static String futaba(int... numbers) {
        StringBuilder result = new StringBuilder("<html><title>Thread</title>GazouBBS");
        for (int number : numbers) result.append("<span class=\"cno\">No.").append(number)
                .append("</span><blockquote>body</blockquote>");
        return result.append("<hr></html>").toString();
    }
    private Object invoke(String name, Object... args) throws Exception {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) types[i] = args[i].getClass();
        Method method = MainActivity.class.getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method.invoke(activity, args);
    }
    private static Object get(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(target);
    }
    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); field.set(target, value);
    }
    private static List<?> posts(Object page) throws Exception { return (List<?>) get(page, "posts"); }
    private static List<Integer> numbers(Object page) throws Exception {
        List<Integer> result = new ArrayList<>();
        for (Object post : posts(page)) result.add((Integer) get(post, "number"));
        return result;
    }

    private static final class Fixture implements Closeable {
        final ServerSocket socket = new ServerSocket(0, 10, InetAddress.getByName("127.0.0.1"));
        final List<String> requests = Collections.synchronizedList(new ArrayList<>());
        volatile String html = "", dat;
        volatile boolean supportRange;
        Fixture() throws IOException {
            Thread worker = new Thread(() -> {
                while (!socket.isClosed()) {
                    try (Socket client = socket.accept()) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.US_ASCII));
                        String request = input.readLine(), line, range = null;
                        while ((line = input.readLine()) != null && !line.isEmpty()) {
                            if (line.toLowerCase(Locale.ROOT).startsWith("range:")) range = line.substring(6).trim();
                        }
                        boolean isDat = request.contains("/board/dat/");
                        boolean isHtml = request.contains("/read.cgi/") || request.contains("/res/");
                        int status = isDat && dat != null || isHtml ? 200 : 404;
                        byte[] body = (isDat && dat != null ? dat : isHtml ? html : "missing").getBytes(StandardCharsets.UTF_8);
                        String extra = "";
                        if (isDat) {
                            requests.add(range == null ? "full-dat" : "range");
                            if (range != null && supportRange) {
                                int start = Integer.parseInt(range.substring(6, range.length() - 1));
                                if (start >= body.length) {
                                    extra = "Content-Range: bytes */" + body.length + "\r\n";
                                    body = new byte[0]; status = 416;
                                } else {
                                    extra = "Content-Range: bytes " + start + "-" + (body.length - 1) + "/" + body.length + "\r\n";
                                    body = Arrays.copyOfRange(body, start, body.length); status = 206;
                                }
                            }
                        }
                        OutputStream output = client.getOutputStream();
                        output.write(("HTTP/1.1 " + status + " Test\r\nContent-Type: text/plain; charset=UTF-8\r\n" + extra
                                + "Content-Length: " + body.length + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
                        output.write(body); output.flush();
                    } catch (IOException error) { if (!socket.isClosed()) throw new RuntimeException(error); }
                }
            }, "BBS fixture");
            worker.setDaemon(true); worker.start();
        }
        String base() { return "http://127.0.0.1:" + socket.getLocalPort(); }
        @Override public void close() throws IOException { socket.close(); }
    }
}
