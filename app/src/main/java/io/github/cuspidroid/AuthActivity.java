package io.github.cuspidroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class AuthActivity extends Activity {
    public static final String EXTRA_URL = "auth_url";

    private WebView webView;
    private ImageButton nativeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Theme.applySystemBars(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Theme.background(this));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        bar.setPadding(dp(8), dp(6), dp(8), dp(6));
        bar.setBackgroundColor(Theme.surface(this));
        nativeButton = iconButton(R.drawable.ic_arrow_forward,
                MainActivity.text("\u30cd\u30a4\u30c6\u30a3\u30d6\u3067\u958b\u304f", "Open natively"));
        nativeButton.setOnClickListener(v -> openCurrentUrlNatively());
        ImageButton close = iconButton(R.drawable.ic_close,
                MainActivity.text("WebView\u3092\u9589\u3058\u308b", "Close WebView"));
        close.setOnClickListener(v -> finishWithResult());
        bar.addView(nativeButton, new LinearLayout.LayoutParams(dp(44), dp(44)));
        bar.addView(close, new LinearLayout.LayoutParams(dp(44), dp(44)));

        webView = new WebView(this);
        webView.setBackgroundColor(Theme.background(this));
        boolean barAtTop = addressBarOnTop();
        if (barAtTop) {
            root.addView(bar, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        root.addView(webView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        if (!barAtTop) {
            root.addView(bar, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        setContentView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager.getInstance().flush();
                updateNativeButton(url);
            }
        });

        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, MainActivity.text("\u958b\u304fURL\u304c\u3042\u308a\u307e\u305b\u3093", "No URL to open."), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String normalized = normalize(url);
        updateNativeButton(normalized);
        webView.loadUrl(normalized);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        finishWithResult();
    }

    private void finishWithResult() {
        CookieManager.getInstance().flush();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onPause() {
        CookieManager.getInstance().flush();
        super.onPause();
    }

    private String normalize(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return "https://" + trimmed;
    }

    private boolean addressBarOnTop() {
        return getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
                .getBoolean(MainActivity.PREF_ADDRESS_BAR_TOP, false);
    }

    private ImageButton iconButton(int iconRes, String description) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(iconRes);
        button.setColorFilter(Theme.text(this));
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setScaleType(ImageButton.ScaleType.CENTER);
        button.setContentDescription(description);
        button.setPadding(dp(10), dp(10), dp(10), dp(10));
        return button;
    }

    private void updateNativeButton(String url) {
        if (nativeButton == null) {
            return;
        }
        boolean supported = isNativeSupportedUrl(url);
        nativeButton.setEnabled(supported);
        nativeButton.setAlpha(supported ? 1f : 0.38f);
    }

    private void openCurrentUrlNatively() {
        String url = webView == null ? "" : webView.getUrl();
        if (!isNativeSupportedUrl(url)) {
            Toast.makeText(this,
                    MainActivity.text("\u30cd\u30a4\u30c6\u30a3\u30d6\u3067\u958b\u3051\u307e\u305b\u3093", "Cannot open natively."),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        CookieManager.getInstance().flush();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isNativeSupportedUrl(String url) {
        return isThreadUrl(url)
                || isBoardUrl(url)
                || isBbsDirectoryUrl(url)
                || isFindUrl(url)
                || isFullTextSearchUrl(url);
    }

    private boolean isThreadUrl(String url) {
        if (url == null) {
            return false;
        }
        String lower = url.toLowerCase(Locale.ROOT);
        return lower.contains("/test/read.cgi/")
                || lower.contains("/bbs/read.cgi/")
                || isFutabaThreadUrl(url)
                || datLikeThreadUrl(url);
    }

    private boolean isBoardUrl(String url) {
        try {
            Uri uri = Uri.parse(normalize(url));
            String host = uri.getHost();
            return host != null && isSupportedBbsHost(host) && boardNameFromUrl(url) != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isBbsDirectoryUrl(String url) {
        try {
            Uri uri = Uri.parse(normalize(url));
            String host = uri.getHost();
            if (host == null || !isSupportedBbsHost(host)) {
                return false;
            }
            return isBbsMenuUrl(url) || boardNameFromUrl(url) == null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isFindUrl(String url) {
        try {
            Uri uri = Uri.parse(normalize(url));
            String host = lowerHost(uri);
            String path = uri.getPath();
            return ("find.5ch.io".equals(host) || "find.5ch.net".equals(host))
                    && (path == null || path.isEmpty() || "/".equals(path) || "/search".equals(path));
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isFullTextSearchUrl(String url) {
        try {
            String host = lowerHost(Uri.parse(normalize(url)));
            return "search2ch.info".equals(host) || host.endsWith(".search2ch.info");
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isBbsMenuUrl(String url) {
        try {
            String path = Uri.parse(normalize(url)).getPath();
            if (path == null) {
                return false;
            }
            String lower = path.toLowerCase(Locale.ROOT);
            return lower.endsWith("/bbsmenu.html")
                    || lower.endsWith("/bbsmenu.htm")
                    || lower.endsWith("/bbsmenu.json")
                    || lower.endsWith("/menu.html")
                    || lower.endsWith("/menu.htm");
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isSupportedBbsHost(String host) {
        if (host == null) {
            return false;
        }
        String lower = host.toLowerCase(Locale.ROOT);
        return lower.equals("5ch.net") || lower.endsWith(".5ch.net")
                || lower.equals("5ch.io") || lower.endsWith(".5ch.io")
                || lower.equals("bbspink.com") || lower.endsWith(".bbspink.com")
                || lower.equals("bbspink.org") || lower.endsWith(".bbspink.org")
                || lower.equals("2ch.sc") || lower.endsWith(".2ch.sc")
                || lower.equals("open2ch.net") || lower.endsWith(".open2ch.net")
                || lower.equals("machi.to") || lower.endsWith(".machi.to")
                || lower.equals("2chan.net") || lower.endsWith(".2chan.net")
                || lower.equals("jbbs.shitaraba.net")
                || lower.equals("bbs-menu.pages.dev")
                || lower.equals("bbs.eddibb.cc")
                || lower.equals("afternoontea.st") || lower.endsWith(".afternoontea.st");
    }

    private boolean isFutabaThreadUrl(String url) {
        try {
            Uri uri = Uri.parse(normalize(url));
            if (!isFutabaHost(uri.getHost())) {
                return false;
            }
            String path = uri.getPath();
            return path != null && path.toLowerCase(Locale.ROOT).matches(".*/res/\\d+\\.html?$");
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean datLikeThreadUrl(String url) {
        try {
            Uri uri = Uri.parse(normalize(url));
            if (!isSupportedBbsHost(uri.getHost())) {
                return false;
            }
            List<String> parts = uri.getPathSegments();
            if (parts == null || parts.size() < 2) {
                return false;
            }
            String last = parts.get(parts.size() - 1);
            return last.matches("\\d{9,13}") || last.matches("\\d{9,13}\\.html?");
        } catch (Exception ignored) {
            return false;
        }
    }

    private String boardNameFromUrl(String url) {
        try {
            Uri uri = Uri.parse(normalize(url));
            String host = lowerHost(uri);
            List<String> parts = uri.getPathSegments();
            if (parts == null || parts.isEmpty() || isBbsMenuUrl(url)) {
                return null;
            }
            if ("jbbs.shitaraba.net".equals(host)) {
                if (parts.size() >= 3 && "bbs".equals(parts.get(0)) && "subject.cgi".equals(parts.get(1))) {
                    return parts.get(2);
                }
                if (parts.size() >= 2) {
                    return parts.get(0) + "/" + parts.get(1);
                }
                return null;
            }
            if (isFutabaHost(host)) {
                String first = parts.get(0);
                return "res".equalsIgnoreCase(first) ? null : first;
            }
            if (parts.size() >= 3 && "bbs".equals(parts.get(0)) && "subject.cgi".equals(parts.get(1))) {
                return parts.get(2);
            }
            String first = parts.get(0);
            String lowerFirst = first.toLowerCase(Locale.ROOT);
            return "test".equals(lowerFirst) || "bbs".equals(lowerFirst) || "dat".equals(lowerFirst) ? null : first;
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isFutabaHost(String host) {
        if (host == null) {
            return false;
        }
        String lower = host.toLowerCase(Locale.ROOT);
        return lower.equals("2chan.net") || lower.endsWith(".2chan.net");
    }

    private String lowerHost(Uri uri) {
        String host = uri == null ? null : uri.getHost();
        return host == null ? "" : host.toLowerCase(Locale.ROOT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
