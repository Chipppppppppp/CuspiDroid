package io.github.cuspidroid;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.URLEncoder;

public class AuthActivity extends Activity {
    public static final String EXTRA_URL = "auth_url";

    private WebView webView;
    private EditText addressBar;
    private String retryUrl = "";
    private int badGatewayRetries;
    private boolean loadingSearchFallback;

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
        addressBar = buildAddressBar();
        ImageButton returnToApp = iconButton(R.drawable.ic_arrow_back,
                MainActivity.text("\u30a2\u30d7\u30ea\u306b\u623b\u308b", "Return to app"));
        returnToApp.setOnClickListener(v -> finishWithResult());
        bar.addView(addressBar, new LinearLayout.LayoutParams(0, dp(40), 1));
        bar.addView(returnToApp, new LinearLayout.LayoutParams(dp(44), dp(44)));

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
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url != null && !url.equals(retryUrl)) {
                    retryUrl = url;
                    badGatewayRetries = 0;
                    loadingSearchFallback = false;
                }
                updateAddressBar(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager.getInstance().flush();
                updateAddressBar(url);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                            WebResourceResponse errorResponse) {
                if (request == null || errorResponse == null || !request.isForMainFrame()
                        || errorResponse.getStatusCode() != 502
                        || !isFindSearchUrl(request.getUrl() == null ? "" : request.getUrl().toString())) {
                    return;
                }
                if (badGatewayRetries < 4) {
                    badGatewayRetries++;
                    view.postDelayed(view::reload, 180);
                    return;
                }
                if (!loadingSearchFallback) {
                    loadingSearchFallback = true;
                    view.loadUrl(fullTextSearchUrl(searchQuery(request.getUrl())));
                }
            }
        });

        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null || url.trim().isEmpty()) {
            Toast.makeText(this, MainActivity.text("\u958b\u304fURL\u304c\u3042\u308a\u307e\u305b\u3093", "No URL to open."), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String normalized = normalize(url);
        updateAddressBar(normalized);
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

    private EditText buildAddressBar() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(15);
        input.setTextColor(Theme.text(this));
        input.setHintTextColor(Theme.muted(this));
        input.setHint(MainActivity.text("\u691c\u7d22\u307e\u305f\u306fURL", "Search or URL"));
        input.setSelectAllOnFocus(true);
        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        input.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0);
        input.setCompoundDrawablePadding(dp(8));
        input.setPadding(dp(12), 0, dp(12), 0);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Theme.field(this));
        background.setStroke(dp(1), Theme.border(this));
        background.setCornerRadius(dp(8));
        input.setBackground(background);
        input.setOnEditorActionListener((v, actionId, event) -> {
            boolean enter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_UP;
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || enter) {
                openFromAddressBar();
                return true;
            }
            return event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
        });
        return input;
    }

    private void openFromAddressBar() {
        if (addressBar == null || webView == null) {
            return;
        }
        String input = addressBar.getText().toString().trim();
        if (input.isEmpty()) {
            return;
        }
        String target = looksLikeUrl(input) ? normalize(input) : searchUrl(input);
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(addressBar.getWindowToken(), 0);
        }
        addressBar.clearFocus();
        webView.loadUrl(target);
    }

    private void updateAddressBar(String url) {
        if (addressBar != null && !addressBar.hasFocus()) {
            addressBar.setText(url == null ? "" : url);
            addressBar.setSelection(0);
        }
    }

    private boolean looksLikeUrl(String value) {
        String lower = value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("//") || lower.contains(".") && !lower.contains(" ");
    }

    private String searchUrl(String query) {
        try {
            String encoded = URLEncoder.encode(query, "UTF-8");
            String template = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
                    .getString(MainActivity.PREF_SEARCH_TEMPLATE, MainActivity.DEFAULT_SEARCH_TEMPLATE);
            if (template == null || template.trim().isEmpty()
                    || MainActivity.LEGACY_FIND_IO_TEMPLATE.equals(template)) {
                template = MainActivity.DEFAULT_SEARCH_TEMPLATE;
            }
            if (template.contains("%s")) {
                return template.replace("%s", encoded);
            }
            return template + (template.contains("?") ? "&" : "?") + "q=" + encoded;
        } catch (Exception ignored) {
            return MainActivity.HOME_URL;
        }
    }

    private boolean isFindSearchUrl(String url) {
        try {
            Uri uri = Uri.parse(normalize(url));
            String host = uri.getHost();
            return host != null
                    && ("find.5ch.io".equalsIgnoreCase(host) || "find.5ch.net".equalsIgnoreCase(host))
                    && "/search".equals(uri.getPath());
        } catch (Exception ignored) {
            return false;
        }
    }

    private String searchQuery(Uri uri) {
        if (uri == null) {
            return "";
        }
        String query = uri.getQueryParameter("q");
        return query == null ? "" : query;
    }

    private String fullTextSearchUrl(String query) {
        try {
            return "https://search2ch.info/?q=" + URLEncoder.encode(query == null ? "" : query, "UTF-8");
        } catch (Exception ignored) {
            return "https://search2ch.info/";
        }
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
