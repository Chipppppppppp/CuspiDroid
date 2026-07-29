package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class AuthActivity extends Activity {
    public static final String EXTRA_URL = "auth_url";

    private WebView webView;
    private EditText addressBar;
    private LinearLayout toolbar;
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

        toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(6), dp(5), dp(6), dp(5));
        toolbar.setBackground(toolbarBackground());
        addressBar = buildAddressBar();
        toolbar.addView(addressBar, new LinearLayout.LayoutParams(0, dp(40), 1));
        addConfiguredToolbarButtons();

        webView = new WebView(this);
        webView.setBackgroundColor(Theme.background(this));
        boolean barAtTop = addressBarOnTop();
        if (barAtTop) {
            root.addView(toolbar, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        root.addView(webView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        if (!barAtTop) {
            root.addView(toolbar, new LinearLayout.LayoutParams(
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
        input.setMaxLines(1);
        input.setMinLines(1);
        input.setLines(1);
        input.setHorizontallyScrolling(true);
        input.setHorizontalScrollBarEnabled(false);
        input.setEllipsize(TextUtils.TruncateAt.END);
        input.setTextSize(15);
        input.setIncludeFontPadding(false);
        input.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
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
        background.setCornerRadius(dp(20));
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

    private void addConfiguredToolbarButtons() {
        for (String id : webViewToolbarButtonIds()) {
            if (MainActivity.ADDRESS_MENU_SYNC.equals(id) && !sync2chEnabled()) {
                continue;
            }
            ImageButton button = iconButton(buttonIcon(id), buttonLabel(id));
            button.setOnClickListener(v -> performToolbarAction(id, button));
            toolbar.addView(button, new LinearLayout.LayoutParams(dp(42), dp(40)));
        }
    }

    private List<String> webViewToolbarButtonIds() {
        SharedPreferences preferences = preferences();
        List<String> ids = MainActivity.orderedButtonIds(
                preferences.getString(MainActivity.PREF_ADDRESS_BAR_BUTTONS,
                        MainActivity.DEFAULT_ADDRESS_BAR_BUTTONS),
                MainActivity.DEFAULT_ADDRESS_BAR_BUTTONS,
                MainActivity.ADDRESS_BUTTON_IDS);
        removeWebViewExcludedButtons(ids);
        if (!ids.contains(MainActivity.ADDRESS_BAR_MENU)) {
            ids.add(MainActivity.ADDRESS_BAR_MENU);
        }
        return ids;
    }

    private List<String> webViewMenuButtonIds() {
        SharedPreferences preferences = preferences();
        List<String> ids = MainActivity.orderedButtonIds(
                preferences.getString(MainActivity.PREF_ADDRESS_MENU_BUTTONS,
                        MainActivity.DEFAULT_ADDRESS_MENU_BUTTONS),
                MainActivity.DEFAULT_ADDRESS_MENU_BUTTONS,
                MainActivity.ADDRESS_BUTTON_IDS);
        removeWebViewExcludedButtons(ids);
        ids.remove(MainActivity.ADDRESS_BAR_MENU);
        if (!buttonPlaced(MainActivity.ADDRESS_MENU_WEBVIEW)
                && !ids.contains(MainActivity.ADDRESS_MENU_WEBVIEW)) {
            ids.add(0, MainActivity.ADDRESS_MENU_WEBVIEW);
        }
        if (!buttonPlaced(MainActivity.ADDRESS_MENU_SETTINGS)
                && !ids.contains(MainActivity.ADDRESS_MENU_SETTINGS)) {
            ids.add(MainActivity.ADDRESS_MENU_SETTINGS);
        }
        return ids;
    }

    private List<String> webViewNavButtonIds() {
        List<String> ids = MainActivity.orderedButtonIds(
                preferences().getString(MainActivity.PREF_ADDRESS_NAV_BUTTONS,
                        MainActivity.DEFAULT_ADDRESS_NAV_BUTTONS),
                MainActivity.DEFAULT_ADDRESS_NAV_BUTTONS,
                MainActivity.ADDRESS_BUTTON_IDS);
        removeWebViewExcludedButtons(ids);
        ids.remove(MainActivity.ADDRESS_BAR_MENU);
        return ids;
    }

    private void removeWebViewExcludedButtons(List<String> ids) {
        ids.remove(MainActivity.ADDRESS_BAR_NEW_TAB);
        ids.remove(MainActivity.ADDRESS_BAR_TABS);
        ids.remove(MainActivity.ADDRESS_MENU_BOOKMARK);
    }

    private boolean buttonPlaced(String id) {
        return webViewButtonListContains(MainActivity.PREF_ADDRESS_BAR_BUTTONS,
                MainActivity.DEFAULT_ADDRESS_BAR_BUTTONS, id)
                || webViewButtonListContains(MainActivity.PREF_ADDRESS_MENU_BUTTONS,
                MainActivity.DEFAULT_ADDRESS_MENU_BUTTONS, id)
                || webViewButtonListContains(MainActivity.PREF_ADDRESS_NAV_BUTTONS,
                MainActivity.DEFAULT_ADDRESS_NAV_BUTTONS, id);
    }

    private boolean webViewButtonListContains(String key, String fallback, String id) {
        List<String> ids = MainActivity.orderedButtonIds(
                preferences().getString(key, fallback), fallback, MainActivity.ADDRESS_BUTTON_IDS);
        removeWebViewExcludedButtons(ids);
        return ids.contains(id);
    }

    private SharedPreferences preferences() {
        return getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
    }

    private boolean sync2chEnabled() {
        return preferences().getBoolean(MainActivity.PREF_SYNC2CH_ENABLED, false);
    }

    private void performToolbarAction(String id, View anchor) {
        if (MainActivity.ADDRESS_BAR_MENU.equals(id)) {
            showAddressMenu(anchor);
            return;
        }
        performAddressAction(id);
    }

    private void performAddressAction(String id) {
        if (MainActivity.ADDRESS_MENU_WEBVIEW.equals(id)) {
            finishWithResult();
        } else if (MainActivity.ADDRESS_MENU_FIND.equals(id)) {
            showFindInPageDialog();
        } else if (MainActivity.ADDRESS_MENU_SYNC.equals(id)) {
            runSync2chNow();
        } else if (MainActivity.ADDRESS_MENU_SETTINGS.equals(id)) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (MainActivity.ADDRESS_NAV_BACK.equals(id)) {
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
            } else {
                finishWithResult();
            }
        } else if (MainActivity.ADDRESS_NAV_FORWARD.equals(id)) {
            if (webView != null && webView.canGoForward()) {
                webView.goForward();
            }
        } else if (MainActivity.ADDRESS_NAV_SHARE.equals(id)) {
            shareCurrentPage();
        } else if (MainActivity.ADDRESS_NAV_RELOAD.equals(id) && webView != null) {
            webView.reload();
        }
    }

    private void showAddressMenu(View anchor) {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setPadding(dp(4), dp(4), dp(4), dp(4));
        menu.setBackground(menuBackground());
        PopupWindow popup = new PopupWindow(menu, dp(220),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        boolean added = false;
        for (String id : webViewMenuButtonIds()) {
            if (MainActivity.ADDRESS_MENU_SYNC.equals(id) && !sync2chEnabled()) {
                continue;
            }
            if (added) {
                menu.addView(horizontalDivider());
            }
            menu.addView(menuItem(id, popup), new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            added = true;
        }
        List<String> navIds = webViewNavButtonIds();
        if (!navIds.isEmpty()) {
            if (added) {
                menu.addView(horizontalDivider());
            }
            menu.addView(navigationRow(navIds, popup), new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        }
        int xOffset = anchor == null ? 0 : anchor.getWidth() - dp(220);
        popup.showAsDropDown(anchor == null ? toolbar : anchor, xOffset, 0);
    }

    private View menuItem(String id, PopupWindow popup) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(10), dp(10), dp(10));
        ImageView icon = new ImageView(this);
        icon.setImageResource(buttonIcon(id));
        icon.setColorFilter(Theme.text(this));
        row.addView(icon, new LinearLayout.LayoutParams(dp(21), dp(21)));
        TextView label = new TextView(this);
        label.setText(buttonLabel(id));
        label.setTextColor(Theme.text(this));
        label.setTextSize(14);
        label.setPadding(dp(10), 0, 0, 0);
        row.addView(label, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.setOnClickListener(v -> {
            popup.dismiss();
            performAddressAction(id);
        });
        return row;
    }

    private LinearLayout navigationRow(List<String> ids, PopupWindow popup) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        for (String id : ids) {
            ImageButton button = iconButton(buttonIcon(id), buttonLabel(id));
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            button.setOnClickListener(v -> {
                popup.dismiss();
                performAddressAction(id);
            });
            if (MainActivity.ADDRESS_NAV_FORWARD.equals(id)
                    && (webView == null || !webView.canGoForward())) {
                button.setEnabled(false);
                button.setAlpha(0.32f);
            }
            row.addView(button);
        }
        return row;
    }

    private void showFindInPageDialog() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextColor(Theme.text(this));
        input.setHintTextColor(Theme.muted(this));
        input.setHint(MainActivity.text("\u30da\u30fc\u30b8\u5185\u691c\u7d22", "Find in page"));
        input.setPadding(dp(12), 0, dp(12), 0);
        input.setBackground(addressBarBackground());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("\u30da\u30fc\u30b8\u5185\u691c\u7d22", "Find in page"))
                .setView(input)
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u691c\u7d22", "Find"), (d, which) -> {
                    if (webView != null) {
                        webView.findAllAsync(input.getText().toString());
                    }
                })
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private void runSync2chNow() {
        Toast.makeText(this, MainActivity.text("Sync2ch\u3067\u540c\u671f\u4e2d",
                "Syncing with Sync2ch..."), Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                Sync2chClient.sync(getApplicationContext(), preferences());
                runOnUiThread(() -> Toast.makeText(this,
                        MainActivity.text("Sync2ch\u540c\u671f\u5b8c\u4e86",
                                "Sync2ch sync complete"), Toast.LENGTH_SHORT).show());
            } catch (Exception error) {
                runOnUiThread(() -> Toast.makeText(this,
                        MainActivity.text("Sync2ch\u540c\u671f\u5931\u6557: ",
                                "Sync2ch sync failed: ") + error.getMessage(),
                        Toast.LENGTH_LONG).show());
            }
        }, "webview-sync2ch").start();
    }

    private void shareCurrentPage() {
        String url = webView == null ? "" : webView.getUrl();
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(share,
                MainActivity.text("\u5171\u6709", "Share")));
    }

    private String buttonLabel(String id) {
        if (MainActivity.ADDRESS_MENU_WEBVIEW.equals(id)) {
            return MainActivity.text("\u30a2\u30d7\u30ea\u306b\u623b\u308b", "Return to app");
        }
        if (MainActivity.ADDRESS_MENU_FIND.equals(id)) {
            return MainActivity.text("\u30da\u30fc\u30b8\u5185\u691c\u7d22", "Find in page");
        }
        if (MainActivity.ADDRESS_MENU_SYNC.equals(id)) {
            return MainActivity.text("Sync2ch\u3067\u540c\u671f", "Sync with Sync2ch");
        }
        if (MainActivity.ADDRESS_MENU_SETTINGS.equals(id)) {
            return MainActivity.text("\u8a2d\u5b9a", "Settings");
        }
        if (MainActivity.ADDRESS_BAR_MENU.equals(id)) {
            return MainActivity.text("\u30e1\u30cb\u30e5\u30fc", "Menu");
        }
        if (MainActivity.ADDRESS_NAV_BACK.equals(id)) {
            return MainActivity.text("\u623b\u308b", "Back");
        }
        if (MainActivity.ADDRESS_NAV_FORWARD.equals(id)) {
            return MainActivity.text("\u9032\u3080", "Forward");
        }
        if (MainActivity.ADDRESS_NAV_SHARE.equals(id)) {
            return MainActivity.text("\u5171\u6709", "Share");
        }
        if (MainActivity.ADDRESS_NAV_RELOAD.equals(id)) {
            return MainActivity.text("\u66f4\u65b0", "Reload");
        }
        return id;
    }

    private int buttonIcon(String id) {
        if (MainActivity.ADDRESS_MENU_WEBVIEW.equals(id)
                || MainActivity.ADDRESS_NAV_BACK.equals(id)) {
            return R.drawable.ic_arrow_back;
        }
        if (MainActivity.ADDRESS_MENU_FIND.equals(id)) return R.drawable.ic_search;
        if (MainActivity.ADDRESS_MENU_SYNC.equals(id)) return R.drawable.ic_refresh;
        if (MainActivity.ADDRESS_MENU_SETTINGS.equals(id)) return R.drawable.ic_settings;
        if (MainActivity.ADDRESS_BAR_MENU.equals(id)) return R.drawable.ic_more_vert;
        if (MainActivity.ADDRESS_NAV_FORWARD.equals(id)) return R.drawable.ic_arrow_forward;
        if (MainActivity.ADDRESS_NAV_SHARE.equals(id)) return R.drawable.ic_share;
        if (MainActivity.ADDRESS_NAV_RELOAD.equals(id)) return R.drawable.ic_refresh;
        return R.drawable.ic_more_vert;
    }

    private View horizontalDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(Theme.border(this));
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        return divider;
    }

    private GradientDrawable addressBarBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Theme.field(this));
        background.setStroke(dp(1), Theme.border(this));
        background.setCornerRadius(dp(20));
        return background;
    }

    private GradientDrawable toolbarBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Theme.surface(this));
        background.setStroke(dp(1), Theme.border(this));
        return background;
    }

    private GradientDrawable menuBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Theme.menu(this));
        background.setStroke(dp(2), Theme.border(this));
        background.setCornerRadius(dp(10));
        return background;
    }

    private ImageButton iconButton(int iconRes, String description) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(iconRes);
        button.setColorFilter(Theme.text(this));
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.TRANSPARENT);
        background.setCornerRadius(dp(8));
        button.setBackground(background);
        button.setScaleType(ImageButton.ScaleType.CENTER);
        button.setContentDescription(description);
        button.setPadding(dp(9), dp(9), dp(9), dp(9));
        return button;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
