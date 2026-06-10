package io.github.cuspidroid;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends Activity {
    public static final String EXTRA_URL = "auth_url";
    public static final String EXTRA_USER_AGENT = "auth_user_agent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = new WebView(this);
        setContentView(webView, new ViewGroup.LayoutParams(
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
        String userAgent = getIntent().getStringExtra(EXTRA_USER_AGENT);
        if (userAgent != null && !userAgent.trim().isEmpty()) {
            settings.setUserAgentString(userAgent);
        }
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager.getInstance().flush();
            }
        });

        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, "Enter an auth URL in Settings.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String normalized = normalize(url);
        Map<String, String> headers = new HashMap<>();
        String cookie = cookieManager.getCookie(normalized);
        if (cookie != null && !cookie.trim().isEmpty()) {
            headers.put("Cookie", cookie);
        }
        webView.loadUrl(normalized, headers);
    }

    private String normalize(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return "https://" + trimmed;
    }
}
