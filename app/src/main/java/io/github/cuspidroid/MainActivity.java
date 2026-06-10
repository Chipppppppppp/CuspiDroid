package io.github.cuspidroid;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.Intent;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    private static final String HOME_URL = "https://find.5ch.net/";
    private static final int TEAL = Color.rgb(15, 118, 110);
    private static final int SURFACE = Color.rgb(247, 248, 250);
    private static final int BORDER = Color.rgb(215, 221, 226);
    private static final int TEXT = Color.rgb(31, 41, 55);

    private final List<CuspTab> tabs = new ArrayList<>();
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private LinearLayout tabStrip;
    private EditText addressBar;
    private FrameLayout contentFrame;
    private ProgressBar progressBar;
    private int currentIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView.setWebContentsDebuggingEnabled(true);
        buildLayout();

        String launchUrl = null;
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            launchUrl = intent.getData().toString();
        }
        createTab(launchUrl == null ? HOME_URL : launchUrl, true);
    }

    @Override
    protected void onDestroy() {
        for (CuspTab tab : tabs) {
            tab.webView.destroy();
        }
        ioExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        CuspTab tab = currentTab();
        if (tab != null && !tab.readerMode && tab.webView.canGoBack()) {
            tab.webView.goBack();
            return;
        }
        if (tabs.size() > 1) {
            closeCurrentTab();
            return;
        }
        super.onBackPressed();
    }

    private void buildLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        setContentView(root);

        HorizontalScrollView tabScroll = new HorizontalScrollView(this);
        tabScroll.setHorizontalScrollBarEnabled(false);
        tabScroll.setFillViewport(false);
        tabScroll.setBackgroundColor(SURFACE);
        tabStrip = new LinearLayout(this);
        tabStrip.setOrientation(LinearLayout.HORIZONTAL);
        tabStrip.setPadding(dp(6), dp(4), dp(6), dp(0));
        tabScroll.addView(tabStrip, new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(42)));
        root.addView(tabScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(6), dp(5), dp(6), dp(5));
        toolbar.setBackgroundColor(Color.WHITE);
        root.addView(toolbar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));

        toolbar.addView(iconButton("<", v -> goBack()));
        toolbar.addView(iconButton(">", v -> goForward()));
        toolbar.addView(iconButton("R", v -> reload()));

        addressBar = new EditText(this);
        addressBar.setSingleLine(true);
        addressBar.setTextSize(15);
        addressBar.setTextColor(TEXT);
        addressBar.setHint("URL or search keywords");
        addressBar.setSelectAllOnFocus(true);
        addressBar.setImeOptions(EditorInfo.IME_ACTION_GO);
        addressBar.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        addressBar.setBackgroundColor(Color.rgb(241, 245, 249));
        addressBar.setPadding(dp(12), 0, dp(12), 0);
        addressBar.setOnEditorActionListener((v, actionId, event) -> {
            boolean enter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_UP;
            if (actionId == EditorInfo.IME_ACTION_GO || enter) {
                openFromAddressBar(false);
                return true;
            }
            return false;
        });
        toolbar.addView(addressBar, new LinearLayout.LayoutParams(0, dp(40), 1));

        toolbar.addView(iconButton("Go", v -> openFromAddressBar(false)));
        toolbar.addView(iconButton("Find", v -> openFromAddressBar(true)));
        toolbar.addView(iconButton("+", v -> createTab(HOME_URL, true)));
        toolbar.addView(iconButton("X", v -> closeCurrentTab()));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        root.addView(progressBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(3)));

        contentFrame = new FrameLayout(this);
        root.addView(contentFrame, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private Button iconButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(13);
        button.setTextColor(TEXT);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(dp(6), 0, dp(6), 0);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(label.length() > 1 ? 52 : 38), dp(40));
        params.setMargins(dp(2), 0, dp(2), 0);
        button.setLayoutParams(params);
        return button;
    }

    private void createTab(String url, boolean select) {
        CuspTab tab = new CuspTab();
        tab.title = "New tab";
        tab.url = normalizeUrl(url);
        tab.webView = new WebView(this);
        configureWebView(tab);
        tabs.add(tab);
        if (select) {
            switchToTab(tabs.size() - 1);
            openInCurrentTab(tab.url);
        }
        renderTabs();
    }

    private void configureWebView(CuspTab tab) {
        WebSettings settings = tab.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        tab.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return interceptNavigation(tab, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return interceptNavigation(tab, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                tab.url = url;
                tab.title = cleanTitle(view.getTitle(), url);
                if (tab == currentTab()) {
                    addressBar.setText(url);
                    progressBar.setVisibility(View.GONE);
                }
                renderTabs();
            }
        });
    }

    private boolean interceptNavigation(CuspTab tab, String url) {
        if (isThreadUrl(url)) {
            int index = tabs.indexOf(tab);
            if (index >= 0) {
                switchToTab(index);
            }
            loadThread(tab, normalizeUrl(url));
            return true;
        }
        tab.readerMode = false;
        progressBar.setVisibility(View.VISIBLE);
        return false;
    }

    private void switchToTab(int index) {
        if (index < 0 || index >= tabs.size()) {
            return;
        }
        currentIndex = index;
        CuspTab tab = tabs.get(index);
        contentFrame.removeAllViews();
        if (tab.readerMode && tab.readerView != null) {
            contentFrame.addView(tab.readerView);
        } else {
            contentFrame.addView(tab.webView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        addressBar.setText(tab.url);
        renderTabs();
    }

    private void renderTabs() {
        if (tabStrip == null) {
            return;
        }
        tabStrip.removeAllViews();
        for (int i = 0; i < tabs.size(); i++) {
            CuspTab tab = tabs.get(i);
            TextView tabView = new TextView(this);
            tabView.setText(shorten(tab.title == null ? "Tab" : tab.title, 22));
            tabView.setGravity(Gravity.CENTER);
            tabView.setTextSize(13);
            tabView.setSingleLine(true);
            tabView.setTextColor(i == currentIndex ? Color.WHITE : TEXT);
            tabView.setBackgroundColor(i == currentIndex ? TEAL : Color.rgb(229, 233, 238));
            tabView.setPadding(dp(12), 0, dp(12), 0);
            int target = i;
            tabView.setOnClickListener(v -> switchToTab(target));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(148), dp(36));
            params.setMargins(dp(3), 0, dp(3), 0);
            tabStrip.addView(tabView, params);
        }
    }

    private void openFromAddressBar(boolean forceSearch) {
        String input = addressBar.getText().toString().trim();
        if (input.isEmpty()) {
            return;
        }
        hideKeyboard();
        String url = forceSearch || !looksLikeUrl(input) ? searchUrl(input) : normalizeUrl(input);
        openInCurrentTab(url);
    }

    private void openInCurrentTab(String url) {
        CuspTab tab = currentTab();
        if (tab == null) {
            createTab(url, true);
            return;
        }
        url = normalizeUrl(url);
        if (isThreadUrl(url)) {
            loadThread(tab, url);
            return;
        }
        tab.readerMode = false;
        tab.readerView = null;
        tab.url = url;
        tab.title = hostTitle(url);
        switchToTab(currentIndex);
        progressBar.setVisibility(View.VISIBLE);
        tab.webView.loadUrl(url);
        renderTabs();
    }

    private void loadThread(CuspTab tab, String url) {
        tab.readerMode = true;
        tab.url = url;
        tab.title = hostTitle(url);
        tab.readerView = loadingView("Loading thread...");
        switchToTab(tabs.indexOf(tab));
        progressBar.setVisibility(View.VISIBLE);

        ioExecutor.execute(() -> {
            ThreadPage page;
            try {
                String html = download(url);
                page = parseThread(url, html);
            } catch (Exception error) {
                page = ThreadPage.error(url, error.getMessage());
            }
            ThreadPage result = page;
            runOnUiThread(() -> {
                tab.title = result.title;
                tab.readerView = buildThreadView(result);
                progressBar.setVisibility(View.GONE);
                if (tab == currentTab()) {
                    switchToTab(currentIndex);
                }
                renderTabs();
            });
        });
    }

    private View loadingView(String message) {
        LinearLayout box = new LinearLayout(this);
        box.setGravity(Gravity.CENTER);
        box.setOrientation(LinearLayout.VERTICAL);
        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(TEXT);
        text.setTextSize(16);
        box.addView(text);
        return box;
    }

    private View buildThreadView(ThreadPage page) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText(page.title);
        title.setTextColor(TEXT);
        title.setTextSize(20);
        title.setGravity(Gravity.START);
        title.setPadding(0, 0, 0, dp(10));
        list.addView(title);

        if (page.error != null) {
            TextView error = postText(page.error);
            error.setTextColor(Color.rgb(185, 28, 28));
            list.addView(error);
            list.addView(postText("The page can still be opened in WebView with the R button."));
            return scroll;
        }

        for (Post post : page.posts) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(10), dp(8), dp(10), dp(10));
            card.setBackgroundColor(Color.rgb(250, 251, 252));
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, dp(8));

            TextView meta = new TextView(this);
            meta.setText(post.number + "  " + post.name + "  " + post.date);
            meta.setTextColor(Color.rgb(79, 91, 103));
            meta.setTextSize(12);
            meta.setPadding(0, 0, 0, dp(5));
            card.addView(meta);

            TextView body = postText(post.body);
            card.addView(body);
            list.addView(card, cardParams);
        }

        if (page.posts.isEmpty()) {
            list.addView(postText("No posts were parsed. Use R to reload, or open another URL."));
        }
        return scroll;
    }

    private TextView postText(String value) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextColor(TEXT);
        text.setTextSize(15);
        text.setLineSpacing(0, 1.15f);
        text.setTextIsSelectable(true);
        return text;
    }

    private void goBack() {
        CuspTab tab = currentTab();
        if (tab != null && !tab.readerMode && tab.webView.canGoBack()) {
            tab.webView.goBack();
        }
    }

    private void goForward() {
        CuspTab tab = currentTab();
        if (tab != null && !tab.readerMode && tab.webView.canGoForward()) {
            tab.webView.goForward();
        }
    }

    private void reload() {
        CuspTab tab = currentTab();
        if (tab == null) {
            return;
        }
        if (tab.readerMode) {
            loadThread(tab, tab.url);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            tab.webView.reload();
        }
    }

    private void closeCurrentTab() {
        if (tabs.isEmpty()) {
            return;
        }
        CuspTab removed = tabs.remove(currentIndex);
        removed.webView.destroy();
        if (tabs.isEmpty()) {
            createTab(HOME_URL, true);
            return;
        }
        currentIndex = Math.max(0, Math.min(currentIndex, tabs.size() - 1));
        switchToTab(currentIndex);
    }

    private CuspTab currentTab() {
        if (currentIndex < 0 || currentIndex >= tabs.size()) {
            return null;
        }
        return tabs.get(currentIndex);
    }

    private String download(String urlText) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlText).openConnection();
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(16000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Linux; Android) CuspiDroid/0.1");
        int code = connection.getResponseCode();
        InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (stream == null) {
            throw new IllegalStateException("HTTP " + code);
        }
        Charset charset = Charset.forName("UTF-8");
        String contentType = connection.getContentType();
        if (contentType != null) {
            Matcher matcher = Pattern.compile("charset=([^;]+)", Pattern.CASE_INSENSITIVE).matcher(contentType);
            if (matcher.find()) {
                charset = Charset.forName(matcher.group(1).trim());
            }
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append('\n');
        }
        reader.close();
        if (code >= 400) {
            throw new IllegalStateException("HTTP " + code + "\n" + stripTags(builder.toString()));
        }
        return builder.toString();
    }

    private ThreadPage parseThread(String url, String html) {
        ThreadPage page = new ThreadPage();
        page.url = url;
        page.title = firstMatch(html, "<title[^>]*>(.*?)</title>");
        if (page.title == null || page.title.trim().isEmpty()) {
            page.title = hostTitle(url);
        }
        page.title = cleanText(page.title).replace("５ちゃんねる", "5ch");

        parseModernPosts(html, page.posts);
        if (page.posts.isEmpty()) {
            parseClassicPosts(html, page.posts);
        }
        return page;
    }

    private void parseModernPosts(String html, List<Post> posts) {
        Pattern pattern = Pattern.compile(
                "<div[^>]+class=[\"'][^\"']*post[^\"']*[\"'][^>]*>(.*?)</div>\\s*</div>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        int fallbackNumber = 1;
        while (matcher.find()) {
            String block = matcher.group(1);
            String body = firstMatch(block, "<div[^>]+class=[\"'][^\"']*(?:message|escaped)[^\"']*[\"'][^>]*>(.*?)</div>");
            if (body == null) {
                continue;
            }
            Post post = new Post();
            post.number = valueOr(firstMatch(block, "class=[\"'][^\"']*(?:number|no)[^\"']*[\"'][^>]*>(.*?)<"), String.valueOf(fallbackNumber));
            post.name = valueOr(firstMatch(block, "class=[\"'][^\"']*name[^\"']*[\"'][^>]*>(.*?)<"), "anonymous");
            post.date = valueOr(firstMatch(block, "class=[\"'][^\"']*(?:date|time)[^\"']*[\"'][^>]*>(.*?)<"), "");
            post.body = cleanText(body);
            posts.add(post);
            fallbackNumber++;
        }
    }

    private void parseClassicPosts(String html, List<Post> posts) {
        Pattern pattern = Pattern.compile("<dt[^>]*>(.*?)</dt>\\s*<dd[^>]*>(.*?)</dd>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String meta = matcher.group(1);
            String body = matcher.group(2);
            Post post = new Post();
            post.number = valueOr(firstMatch(meta, "^(\\s*\\d+)"), String.valueOf(posts.size() + 1)).trim();
            post.name = valueOr(firstMatch(meta, "<b[^>]*>(.*?)</b>"), "anonymous");
            post.date = cleanText(stripTags(meta)).replace(post.number, "").replace(post.name, "").trim();
            post.body = cleanText(body);
            posts.add(post);
        }
    }

    private String firstMatch(String text, String regex) {
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String cleanText(String html) {
        String normalized = html.replace("<br>", "\n")
                .replace("<br/>", "\n")
                .replace("<br />", "\n");
        Spanned spanned;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(normalized, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(normalized);
        }
        return spanned.toString().replace('\u00a0', ' ').trim();
    }

    private String stripTags(String html) {
        return cleanText(html.replaceAll("<script[\\s\\S]*?</script>", "")
                .replaceAll("<style[\\s\\S]*?</style>", "")
                .replaceAll("<[^>]+>", " "));
    }

    private String valueOr(String value, String fallback) {
        if (value == null || cleanText(value).isEmpty()) {
            return fallback;
        }
        return cleanText(value);
    }

    private boolean isThreadUrl(String url) {
        String lower = url.toLowerCase(Locale.ROOT);
        return lower.contains(".5ch.net/test/read.cgi/")
                || lower.contains(".2ch.sc/test/read.cgi/")
                || lower.contains("/test/read.cgi/");
    }

    private boolean looksLikeUrl(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://")
                || lower.startsWith("https://")
                || lower.contains(".5ch.net/")
                || lower.contains(".io/")
                || lower.matches("^[a-z0-9.-]+\\.[a-z]{2,}(/.*)?$");
    }

    private String normalizeUrl(String input) {
        if (input == null || input.trim().isEmpty()) {
            return HOME_URL;
        }
        String value = input.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        return "https://" + value;
    }

    private String searchUrl(String query) {
        try {
            return "https://find.5ch.net/search?STR="
                    + URLEncoder.encode(query, "UTF-8")
                    + "&TYPE=TITLE&BBS=ALL";
        } catch (Exception error) {
            return HOME_URL;
        }
    }

    private String hostTitle(String url) {
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            return host == null ? "Tab" : host.replace("www.", "");
        } catch (Exception error) {
            return "Tab";
        }
    }

    private String cleanTitle(String title, String url) {
        if (title == null || title.trim().isEmpty()) {
            return hostTitle(url);
        }
        return title.trim();
    }

    private String shorten(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 1) + "...";
    }

    private void hideKeyboard() {
        try {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(addressBar.getWindowToken(), 0);
        } catch (Exception ignored) {
            Toast.makeText(this, "Opening...", Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class CuspTab {
        String title;
        String url;
        WebView webView;
        View readerView;
        boolean readerMode;
    }

    private static class ThreadPage {
        String url;
        String title;
        String error;
        List<Post> posts = new ArrayList<>();

        static ThreadPage error(String url, String message) {
            ThreadPage page = new ThreadPage();
            page.url = url;
            page.title = "Load failed";
            page.error = message == null ? "Unknown error" : message;
            return page;
        }
    }

    private static class Post {
        String number;
        String name;
        String date;
        String body;
    }
}
