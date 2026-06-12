package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Rect;
import android.text.Html;
import android.text.TextUtils;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.CookieManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    static final String HOME_URL = "https://find.5ch.io/";
    static final String PREFS_NAME = "cuspidroid_settings";
    static final String PREF_5CH_NEW_TAB = "open_5ch_links_in_new_tab";
    static final String PREF_SEARCH_TEMPLATE = "search_template";
    static final String PREF_BLUR_IMGUR = "blur_imgur_images";
    static final String PREF_ADDRESS_BAR_TOP = "address_bar_top";
    static final String PREF_BBS_LINKS = "bbs_links";
    static final String PREF_NG_WORDS = "ng_words";
    static final String PREF_READ_POSTS = "read_posts";
    private static final String PREF_TABS = "saved_tabs";
    static final String PREF_HISTORY = "thread_history";
    static final String DEFAULT_SEARCH_TEMPLATE = "https://find.5ch.io/search?q=%s";
    static final String LEGACY_FIND_IO_TEMPLATE = "https://find.5ch.io/search?STR=%s&TYPE=TITLE&BBS=ALL";
    static final String FIND_NET_TEMPLATE = "https://find.5ch.net/search?STR=%s&TYPE=TITLE&BBS=ALL";
    private static final String NATIVE_THREAD = "thread";
    private static final String NATIVE_SEARCH = "search";
    private static final String NATIVE_SEARCH_HOME = "search_home";
    private static final String NATIVE_BOARD = "board";
    private static final int TEAL = Color.rgb(15, 118, 110);
    private static final int SURFACE = Color.rgb(247, 248, 250);
    private static final int BORDER = Color.rgb(215, 221, 226);
    private static final int TEXT = Color.rgb(31, 41, 55);
    private static final Pattern URL_TEXT_PATTERN = Pattern.compile("(?:h?ttps?://|ttps?://|ttp://)\\S+", Pattern.CASE_INSENSITIVE);
    private static final Pattern POST_ID_PATTERN = Pattern.compile("\\bID:([A-Za-z0-9+/._-]+)");

    private final List<CuspTab> tabs = new ArrayList<>();
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private LinearLayout suggestionsPanel;
    private FrameLayout overlayFrame;
    private EditText addressBar;
    private FrameLayout contentFrame;
    private ProgressBar progressBar;
    private LinearLayout bottomThreadBar;
    private LinearLayout threadSearchBar;
    private EditText threadSearchInput;
    private TextView threadSearchCount;
    private LinearLayout bottomToolbar;
    private TextView bottomThreadTitle;
    private ImageButton bottomWriteButton;
    private TextView tabCountButton;
    private View centerSpinnerOverlay;
    private SharedPreferences preferences;
    private final List<View> toolbarButtons = new ArrayList<>();
    private ThreadPage visibleThreadPage;
    private ScrollView visibleThreadScroll;
    private final Map<Integer, View> visiblePostViews = new LinkedHashMap<>();
    private final List<PopupWindow> replyPopups = new ArrayList<>();
    private int currentIndex = -1;
    private boolean pendingNewTab;
    private boolean pendingHistoryAll;
    private boolean tabOverviewVisible;
    private boolean addressBarTop;
    private boolean updatingThreadSearchInput;
    private Runnable threadSearchHighlightTask;
    private View imageOverlay;
    private View highlightedPostView;

    static String text(String ja, String en) {
        return Locale.JAPANESE.getLanguage().equals(Locale.getDefault().getLanguage()) ? ja : en;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        buildLayout();

        String launchUrl = null;
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            launchUrl = intent.getData().toString();
        }
        if (launchUrl == null) {
            if (!restoreTabs()) {
                createBlankTab();
            }
        } else {
            createTab(launchUrl, true);
        }
    }

    @Override
    protected void onPause() {
        saveTabs();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomToolbar != null && addressBarTop != addressBarOnTop()) {
            CuspTab tab = currentTab();
            if (tab != null) {
                rememberThreadScroll(tab);
            }
            buildLayout();
            if (pendingNewTab) {
                showPendingNewTab(pendingHistoryAll);
            } else if (currentIndex >= 0 && currentIndex < tabs.size()) {
                switchToTab(currentIndex);
            }
        }
    }

    @Override
    protected void onDestroy() {
        ioExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (imageOverlay != null) {
            closeImageViewer();
            return;
        }
        if (tabOverviewVisible) {
            tabOverviewVisible = false;
            switchToTab(currentIndex);
            return;
        }
        if (pendingNewTab) {
            if (pendingHistoryAll) {
                showPendingNewTab(false);
                return;
            }
            cancelPendingNewTab();
            return;
        }
        if (addressBar != null && addressBar.hasFocus()) {
            clearAddressFocus();
            return;
        }
        if (!replyPopups.isEmpty()) {
            dismissTopReplyPopup();
            return;
        }
        CuspTab tab = currentTab();
        if (tab != null && tab.backToNewTab && tab.navigationIndex <= 0) {
            closeCurrentTab();
            showPendingNewTab();
            return;
        }
        if (tabs.size() > 1) {
            closeCurrentTab();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (highlightedPostView != null) {
                clearJumpHighlight();
            }
            if (!replyPopups.isEmpty() && !isTouchInsideReplyPopup(event)) {
                dismissTopReplyPopup();
                return true;
            }
            if (addressBar != null && addressBar.hasFocus()
                    && !isTouchInsideView(event, addressBar)
                    && (suggestionsPanel == null || suggestionsPanel.getVisibility() != View.VISIBLE
                    || !isTouchInsideView(event, suggestionsPanel))) {
                clearAddressFocus();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void buildLayout() {
        addressBarTop = addressBarOnTop();
        toolbarButtons.clear();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        setContentView(root);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        root.addView(progressBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(3)));

        contentFrame = new FrameLayout(this);
        contentFrame.setFocusableInTouchMode(true);
        overlayFrame = new FrameLayout(this);
        overlayFrame.addView(contentFrame, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        suggestionsPanel = new LinearLayout(this);
        suggestionsPanel.setOrientation(LinearLayout.VERTICAL);
        suggestionsPanel.setBackgroundColor(Color.WHITE);
        suggestionsPanel.setPadding(dp(12), dp(12), dp(12), dp(12));
        suggestionsPanel.setElevation(dp(12));
        suggestionsPanel.setVisibility(View.GONE);
        FrameLayout.LayoutParams suggestionsParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        suggestionsParams.gravity = Gravity.TOP;
        overlayFrame.addView(suggestionsPanel, suggestionsParams);

        threadSearchBar = new LinearLayout(this);
        threadSearchBar.setOrientation(LinearLayout.HORIZONTAL);
        threadSearchBar.setGravity(Gravity.CENTER_VERTICAL);
        threadSearchBar.setPadding(dp(8), dp(5), dp(6), dp(5));
        threadSearchBar.setBackground(bottomBarBackground());
        threadSearchBar.setVisibility(View.GONE);

        threadSearchInput = new EditText(this);
        threadSearchInput.setSingleLine(true);
        threadSearchInput.setTextSize(14);
        threadSearchInput.setTextColor(TEXT);
        threadSearchInput.setHint(text("\u30b9\u30ec\u5185\u691c\u7d22", "Find in thread"));
        threadSearchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        threadSearchInput.setBackground(addressBarBackground());
        threadSearchInput.setPadding(dp(12), 0, dp(12), 0);
        threadSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!updatingThreadSearchInput) {
                    updateThreadSearch(s.toString(), true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        threadSearchBar.addView(threadSearchInput, new LinearLayout.LayoutParams(0, dp(40), 1));
        threadSearchCount = new TextView(this);
        threadSearchCount.setTextColor(Color.rgb(79, 91, 103));
        threadSearchCount.setTextSize(12);
        threadSearchCount.setGravity(Gravity.CENTER);
        threadSearchBar.addView(threadSearchCount, new LinearLayout.LayoutParams(dp(42), dp(40)));
        threadSearchBar.addView(threadSearchButton(R.drawable.ic_arrow_up, text("\u524d\u3078", "Previous"), v -> moveThreadSearch(-1)));
        threadSearchBar.addView(threadSearchButton(R.drawable.ic_arrow_down, text("\u6b21\u3078", "Next"), v -> moveThreadSearch(1)));
        threadSearchBar.addView(threadSearchButton(R.drawable.ic_close, text("\u9589\u3058\u308b", "Close"), v -> closeThreadSearch()));

        bottomThreadBar = new LinearLayout(this);
        bottomThreadBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomThreadBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomThreadBar.setPadding(dp(10), dp(4), dp(6), dp(4));
        bottomThreadBar.setBackground(bottomBarBackground());

        bottomThreadTitle = new TextView(this);
        bottomThreadTitle.setTextColor(TEXT);
        bottomThreadTitle.setTextSize(12);
        bottomThreadTitle.setSingleLine(false);
        bottomThreadTitle.setMaxLines(2);
        bottomThreadTitle.setEllipsize(TextUtils.TruncateAt.END);
        bottomThreadTitle.setGravity(Gravity.CENTER_VERTICAL);
        bottomThreadTitle.setOnClickListener(v -> scrollCurrentThreadToBottom());
        bottomThreadBar.addView(bottomThreadTitle, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        bottomWriteButton = iconButton(R.drawable.ic_edit, text("\u66f8\u304d\u8fbc\u307f", "Write"), v -> showWriteDialog());
        bottomThreadBar.addView(bottomWriteButton, new LinearLayout.LayoutParams(dp(42), dp(40)));

        bottomToolbar = new LinearLayout(this);
        bottomToolbar.setOrientation(LinearLayout.HORIZONTAL);
        bottomToolbar.setGravity(Gravity.CENTER_VERTICAL);
        bottomToolbar.setPadding(dp(6), dp(5), dp(6), dp(5));
        bottomToolbar.setBackgroundColor(Color.rgb(242, 246, 249));

        addressBar = new EditText(this);
        addressBar.setSingleLine(true);
        addressBar.setMaxLines(1);
        addressBar.setMinLines(1);
        addressBar.setHorizontallyScrolling(true);
        addressBar.setEllipsize(TextUtils.TruncateAt.END);
        addressBar.setTextSize(15);
        addressBar.setTextColor(TEXT);
        addressBar.setHint(text("\u691c\u7d22\u307e\u305f\u306fURL", "Search or URL"));
        addressBar.setSelectAllOnFocus(true);
        addressBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        addressBar.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        addressBar.setSingleLine(true);
        addressBar.setMaxLines(1);
        addressBar.setMinLines(1);
        addressBar.setHorizontallyScrolling(false);
        addressBar.setEllipsize(TextUtils.TruncateAt.END);
        addressBar.setBackground(addressBarBackground());
        addressBar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0);
        addressBar.setCompoundDrawablePadding(dp(8));
        addressBar.setPadding(dp(12), 0, dp(12), 0);
        addressBar.setOnEditorActionListener((v, actionId, event) -> {
            boolean enter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_UP;
            boolean enterDown = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || enter) {
                openFromAddressBar();
                return true;
            }
            return enterDown;
        });
        addressBar.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                openFromAddressBar();
                return true;
            }
            return false;
        });
        addressBar.setOnClickListener(v -> addressBar.selectAll());
        addressBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                addressBar.selectAll();
            }
            updateAddressFocusUi(hasFocus);
        });
        addressBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (addressBar.hasFocus()) {
                    updateSuggestions();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        addressBar.setOnLongClickListener(v -> {
            if (addressBar.hasFocus()) {
                return true;
            }
            showAddressEditMenu();
            return true;
        });
        bottomToolbar.addView(addressBar, new LinearLayout.LayoutParams(0, dp(40), 1));

        tabCountButton = tabCountButton();
        toolbarButtons.add(tabCountButton);
        bottomToolbar.addView(tabCountButton, new LinearLayout.LayoutParams(dp(32), dp(32)));
        addToolbarButton(bottomToolbar, R.drawable.ic_add, text("\u65b0\u898f\u30bf\u30d6", "New tab"), v -> createBlankTab());
        addToolbarButton(bottomToolbar, R.drawable.ic_more_vert, text("\u30e1\u30cb\u30e5\u30fc", "Menu"), v -> showThreadMenu(v));

        if (addressBarTop) {
            root.addView(bottomToolbar, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
            root.addView(overlayFrame, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
            root.addView(threadSearchBar, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(50)));
            root.addView(bottomThreadBar, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(50)));
        } else {
            root.addView(overlayFrame, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
            root.addView(threadSearchBar, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(50)));
            root.addView(bottomThreadBar, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(50)));
            root.addView(bottomToolbar, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
        }
    }

    private ImageButton iconButton(int iconRes, String description, View.OnClickListener listener) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(iconRes);
        button.setContentDescription(description);
        button.setColorFilter(TEXT);
        button.setBackground(iconButtonBackground());
        button.setPadding(dp(9), dp(9), dp(9), dp(9));
        button.setScaleType(ImageButton.ScaleType.CENTER);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(34), dp(36));
        params.setMargins(dp(2), 0, dp(2), 0);
        button.setLayoutParams(params);
        return button;
    }

    private TextView tabCountButton() {
        TextView view = new TextView(this);
        view.setTextColor(TEXT);
        view.setTextSize(13);
        view.setGravity(Gravity.CENTER);
        view.setSingleLine(true);
        view.setContentDescription(text("\u30bf\u30d6", "Tabs"));
        view.setBackground(tabCountBackground(false));
        view.setOnClickListener(v -> showTabOverview());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(30), dp(32));
        params.setMargins(dp(2), 0, dp(2), 0);
        view.setLayoutParams(params);
        return view;
    }

    private GradientDrawable tabCountBackground(boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(selected ? Color.rgb(231, 247, 244) : Color.TRANSPARENT);
        drawable.setStroke(dp(2), selected ? TEAL : TEXT);
        drawable.setCornerRadius(dp(5));
        return drawable;
    }

    private void addToolbarButton(LinearLayout toolbar, int iconRes, String description, View.OnClickListener listener) {
        ImageButton button = iconButton(iconRes, description, listener);
        toolbarButtons.add(button);
        toolbar.addView(button);
    }

    private void updateAddressFocusUi(boolean focused) {
        for (View button : toolbarButtons) {
            button.setVisibility(focused ? View.GONE : View.VISIBLE);
        }
        if (bottomThreadBar != null) {
            if (focused) {
                bottomThreadBar.setVisibility(View.GONE);
            } else {
                updateBottomThreadBar(currentTab());
            }
        }
        if (focused) {
            updateSuggestions();
        } else if (suggestionsPanel != null) {
            suggestionsPanel.setVisibility(View.GONE);
        }
    }

    private void updateSuggestions() {
        if (suggestionsPanel == null || !addressBar.hasFocus()) {
            return;
        }
        suggestionsPanel.removeAllViews();

        boolean allSelected = addressBar.hasSelection()
                && addressBar.getSelectionStart() == 0
                && addressBar.getSelectionEnd() == addressBar.getText().length();
        String query = allSelected ? "" : addressBar.getText().toString().trim().toLowerCase(Locale.ROOT);
        String clipboardLink = query.isEmpty() ? clipboardLink() : null;
        if (!query.isEmpty()) {
            int count = 0;
            for (ThreadHistoryItem history : threadHistory()) {
                if (history.title.toLowerCase(Locale.ROOT).contains(query)) {
                    TextView item = suggestionItem(text("\u30b9\u30ec\u5c65\u6b74", "Thread history"), history.title);
                    item.setOnClickListener(v -> {
                        addressBar.setText(history.url);
                        addressBar.setSelection(addressBar.getText().length());
                        openFromAddressBar();
                    });
                    if (suggestionsPanel.getChildCount() > 0) {
                        suggestionsPanel.addView(suggestionDivider());
                    }
                    suggestionsPanel.addView(item);
                    count++;
                    if (count >= 6) {
                        break;
                    }
                }
            }
        }
        if (clipboardLink != null) {
            if (suggestionsPanel.getChildCount() > 0) {
                suggestionsPanel.addView(suggestionDivider());
            }
            TextView item = suggestionItem(text("\u30af\u30ea\u30c3\u30d7\u30dc\u30fc\u30c9\u306e\u30ea\u30f3\u30af\u3092\u5165\u529b", "Enter link from clipboard"), clipboardLink);
            item.setOnClickListener(v -> {
                addressBar.setText(clipboardLink);
                addressBar.selectAll();
                updateSuggestions();
            });
            suggestionsPanel.addView(item);
        }
        if (suggestionsPanel.getChildCount() == 0) {
            TextView empty = suggestionItem(text("\u5019\u88dc\u306a\u3057", "No suggestions"), text("\u691c\u7d22\u8a9e\u307e\u305f\u306fURL\u3092\u5165\u529b", "Enter a search term or URL"));
            suggestionsPanel.addView(empty);
        }
        suggestionsPanel.setVisibility(View.VISIBLE);
    }

    private TextView suggestionItem(String label, String value) {
        TextView view = new TextView(this);
        view.setText(label + "\n" + value);
        view.setTextColor(TEXT);
        view.setTextSize(14);
        view.setBackgroundColor(Color.WHITE);
        view.setPadding(dp(12), dp(10), dp(12), dp(10));
        view.setMinHeight(dp(58));
        return view;
    }

    private View suggestionDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(BORDER);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        return divider;
    }

    private String clipboardLink() {
        try {
            ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (manager == null || !manager.hasPrimaryClip()) {
                return null;
            }
            ClipData data = manager.getPrimaryClip();
            if (data == null || data.getItemCount() == 0) {
                return null;
            }
            CharSequence text = data.getItemAt(0).coerceToText(this);
            if (text == null) {
                return null;
            }
            String value = text.toString().trim();
            if (!looksLikeUrl(value)) {
                return null;
            }
            String url = normalizeUrl(value);
            return is5chUrl(url) ? url : null;
        } catch (Exception error) {
            return null;
        }
    }

    private void showAddressEditMenu() {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.HORIZONTAL);
        menu.setBackground(menuBackground());
        menu.setPadding(dp(4), dp(4), dp(4), dp(4));
        PopupWindow popup = new PopupWindow(menu, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        popup.setElevation(dp(12));

        menu.addView(menuItem(text("\u30b3\u30d4\u30fc", "Copy"), v -> {
            copyAddressText();
            popup.dismiss();
        }));
        menu.addView(verticalDivider());
        menu.addView(menuItem(text("\u8cbc\u308a\u4ed8\u3051", "Paste"), v -> {
            pasteIntoAddressBar(false);
            popup.dismiss();
        }));
        menu.addView(verticalDivider());
        menu.addView(menuItem(text("\u8cbc\u308a\u4ed8\u3051\u3066\u79fb\u52d5", "Paste and go"), v -> {
            pasteIntoAddressBar(true);
            popup.dismiss();
        }));
        int yOffset = addressBarTop ? dp(2) : -addressBar.getHeight() - dp(54);
        popup.showAsDropDown(addressBar, dp(8), yOffset);
    }

    private TextView menuItem(String text, View.OnClickListener listener) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(TEXT);
        view.setTextSize(14);
        view.setGravity(Gravity.CENTER);
        view.setMinWidth(dp(82));
        view.setPadding(dp(12), dp(10), dp(12), dp(10));
        view.setOnClickListener(listener);
        return view;
    }

    private void showThreadMenu(View anchor) {
        CuspTab tab = currentTab();
        boolean hasUrl = tab != null && tab.url != null && !tab.url.trim().isEmpty();
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setBackground(menuBackground());
        menu.setPadding(dp(4), dp(4), dp(4), dp(4));
        PopupWindow popup = new PopupWindow(menu, dp(220), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        popup.setElevation(dp(12));

        menu.addView(menuIconItem(R.drawable.ic_arrow_forward, text("WebView\u3067\u958b\u304f", "Open in WebView"), v -> {
            popup.dismiss();
            openCurrentThreadInWebView();
        }), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        menu.addView(horizontalDivider());
        menu.addView(menuIconItem(R.drawable.ic_share, text("\u5171\u6709", "Share"), v -> {
            popup.dismiss();
            shareCurrentThread();
        }), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (!hasUrl) {
            menu.getChildAt(0).setEnabled(false);
            menu.getChildAt(0).setAlpha(0.45f);
            menu.getChildAt(2).setEnabled(false);
            menu.getChildAt(2).setAlpha(0.45f);
        }
        menu.addView(horizontalDivider());
        menu.addView(menuIconItem(R.drawable.ic_search, text("\u30b9\u30ec\u5185\u691c\u7d22", "Find in thread"), v -> {
            popup.dismiss();
            showThreadSearchDialog();
        }), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        menu.addView(horizontalDivider());
        menu.addView(menuIconItem(R.drawable.ic_search, text("\u6b21\u30b9\u30ec\u691c\u7d22", "Search next thread"), v -> {
            popup.dismiss();
            searchNextThread();
        }), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        menu.addView(horizontalDivider());
        menu.addView(menuIconItem(R.drawable.ic_settings, text("\u8a2d\u5b9a", "Settings"), v -> {
            popup.dismiss();
            openSettings();
        }), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        menu.addView(horizontalDivider());
        menu.addView(menuNavigationRow(popup), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        showMenuWithinScreen(popup, menu, anchor);
    }

    private LinearLayout menuNavigationRow(PopupWindow popup) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.addView(menuIconButton(R.drawable.ic_arrow_back, text("\u623b\u308b", "Back"), v -> {
            popup.dismiss();
            goBack();
        }));
        row.addView(menuIconButton(R.drawable.ic_arrow_forward, text("\u9032\u3080", "Forward"), v -> {
            popup.dismiss();
            goForward();
        }));
        row.addView(menuIconButton(R.drawable.ic_refresh, text("\u66f4\u65b0", "Reload"), v -> {
            popup.dismiss();
            reloadFromMenu();
        }));
        return row;
    }

    private void showMenuWithinScreen(PopupWindow popup, View menu, View anchor) {
        int width = dp(220);
        menu.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int height = menu.getMeasuredHeight();
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int margin = dp(8);
        int[] anchorLocation = new int[2];
        anchor.getLocationOnScreen(anchorLocation);

        int x = anchorLocation[0] + anchor.getWidth() - width;
        x = Math.max(frame.left + margin, Math.min(x, frame.right - width - margin));

        int below = anchorLocation[1] + anchor.getHeight() + dp(2);
        int above = anchorLocation[1] - height - dp(2);
        int y = addressBarTop ? below : above;
        if (y + height > frame.bottom - margin) {
            y = above;
        }
        if (y < frame.top + margin) {
            y = frame.top + margin;
        }
        if (height > frame.height() - margin * 2) {
            y = frame.top + margin;
        }
        popup.setClippingEnabled(true);
        popup.showAtLocation(getWindow().getDecorView(), Gravity.NO_GRAVITY, x, y);
    }

    private void showCenterSpinner() {
        if (overlayFrame == null) {
            return;
        }
        hideCenterSpinner();
        FrameLayout shade = new FrameLayout(this);
        shade.setClickable(false);
        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER);
        shade.addView(spinner, spinnerParams);
        centerSpinnerOverlay = shade;
        overlayFrame.addView(shade, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void hideCenterSpinner() {
        if (centerSpinnerOverlay != null && overlayFrame != null) {
            overlayFrame.removeView(centerSpinnerOverlay);
        }
        centerSpinnerOverlay = null;
    }

    private ImageButton menuIconButton(int iconRes, String description, View.OnClickListener listener) {
        ImageButton button = iconButton(iconRes, description, listener);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return button;
    }

    private ImageButton threadSearchButton(int iconRes, String description, View.OnClickListener listener) {
        ImageButton button = iconButton(iconRes, description, listener);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setPadding(dp(8), dp(8), dp(8), dp(8));
        button.setLayoutParams(new LinearLayout.LayoutParams(dp(34), dp(40)));
        return button;
    }

    private LinearLayout menuIconItem(int iconRes, String text, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setOnClickListener(listener);
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(TEXT);
        row.addView(icon, new LinearLayout.LayoutParams(dp(22), dp(22)));
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(TEXT);
        label.setTextSize(14);
        label.setPadding(dp(12), 0, 0, 0);
        row.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return row;
    }

    private View verticalDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(BORDER);
        divider.setLayoutParams(new LinearLayout.LayoutParams(dp(1), ViewGroup.LayoutParams.MATCH_PARENT));
        return divider;
    }

    private View horizontalDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(BORDER);
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        return divider;
    }

    private void copyAddressText() {
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            String selected = addressBar.getText().toString();
            manager.setPrimaryClip(ClipData.newPlainText("CuspiDroid address", selected));
        }
    }

    private void pasteIntoAddressBar(boolean go) {
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null || !manager.hasPrimaryClip()) {
            return;
        }
        ClipData data = manager.getPrimaryClip();
        if (data == null || data.getItemCount() == 0) {
            return;
        }
        CharSequence text = data.getItemAt(0).coerceToText(this);
        if (text == null) {
            return;
        }
        addressBar.setText(text.toString());
        addressBar.setSelection(addressBar.getText().length());
        if (go) {
            openFromAddressBar();
        } else {
            addressBar.requestFocus();
            addressBar.post(() -> {
                addressBar.requestFocus();
                showKeyboard();
                updateSuggestions();
                addressBar.postDelayed(() -> {
                    addressBar.requestFocus();
                    showKeyboard();
                }, 180);
            });
        }
    }

    private GradientDrawable iconButtonBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.TRANSPARENT);
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private GradientDrawable roundedDrawable(int fill, int stroke, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setStroke(dp(1), stroke);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private GradientDrawable addressBarBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.rgb(241, 245, 249));
        drawable.setStroke(dp(1), BORDER);
        drawable.setCornerRadius(dp(20));
        return drawable;
    }

    private GradientDrawable menuBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setStroke(dp(2), Color.rgb(148, 163, 184));
        drawable.setCornerRadius(dp(10));
        return drawable;
    }

    private GradientDrawable bottomBarBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.rgb(242, 246, 249));
        drawable.setStroke(dp(1), Color.rgb(176, 188, 199));
        return drawable;
    }

    private void createTab(String url, boolean select) {
        createTab(url, select, -1, false);
    }

    private void createTab(String url, boolean select, int returnToIndex) {
        createTab(url, select, returnToIndex, false);
    }

    private void createTab(String url, boolean select, int returnToIndex, boolean backToNewTab) {
        CuspTab tab = new CuspTab();
        tab.title = text("\u65b0\u898f\u30bf\u30d6", "New tab");
        tab.url = "";
        tab.returnToIndex = returnToIndex;
        tab.backToNewTab = backToNewTab;
        tabs.add(tab);
        if (select) {
            switchToTab(tabs.size() - 1);
            openInCurrentTab(normalizeUrl(url));
        }
        renderTabs();
    }

    private void createBlankTab() {
        showPendingNewTab();
    }

    private void showPendingNewTab() {
        CuspTab previous = currentTab();
        if (previous != null) {
            rememberThreadScroll(previous);
        }
        if (!replyPopups.isEmpty()) {
            dismissThreadPopups();
        }
        pendingNewTab = true;
        pendingHistoryAll = false;
        tabOverviewVisible = false;
        contentFrame.removeAllViews();
        visibleThreadPage = null;
        visibleThreadScroll = null;
        visiblePostViews.clear();
        contentFrame.addView(buildSearchHomeView(false));
        addressBar.setText("");
        updateBottomThreadBar(null);
        clearAddressFocus();
        renderTabs();
    }

    private void showPendingNewTab(boolean fullHistory) {
        if (!pendingNewTab) {
            showPendingNewTab();
            return;
        }
        pendingHistoryAll = fullHistory;
        tabOverviewVisible = false;
        contentFrame.removeAllViews();
        contentFrame.addView(fullHistory ? buildHistoryView() : buildSearchHomeView(false));
        addressBar.setText("");
        clearAddressFocus();
        renderTabs();
    }

    private void cancelPendingNewTab() {
        pendingNewTab = false;
        if (tabs.isEmpty()) {
            showPendingNewTab();
            return;
        }
        pendingHistoryAll = false;
        switchToTab(Math.max(0, Math.min(currentIndex, tabs.size() - 1)));
    }

    private boolean restoreTabs() {
        String saved = preferences.getString(PREF_TABS, "");
        if (saved == null || saved.isEmpty()) {
            return false;
        }
        try {
            JSONObject root = new JSONObject(saved);
            JSONArray array = root.optJSONArray("tabs");
            if (array == null || array.length() == 0) {
                return false;
            }
            int selected = Math.max(0, Math.min(root.optInt("current", 0), array.length() - 1));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                String url = item.optString("url", "");
                CuspTab tab = new CuspTab();
                tab.title = item.optString("title", text("\u65b0\u898f\u30bf\u30d6", "New tab"));
                tab.url = url;
                String nativeKind = item.optString("nativeKind", "");
                tab.nativeKind = nativeKind.isEmpty() || "null".equals(nativeKind) ? null : nativeKind;
                tab.threadScrollRatio = (float) item.optDouble("threadScrollRatio", 0);
                tab.threadBottomOffset = item.optInt("threadBottomOffset", 0);
                restoreNavigationHistory(tab, item);
                if (NATIVE_THREAD.equals(tab.nativeKind)) {
                    tab.threadPage = threadPageFromJson(item.optJSONObject("threadPage"));
                    if (tab.threadPage != null && !tab.threadPage.posts.isEmpty()) {
                        tab.readerMode = true;
                        tab.readPostNumber = readPostNumber(preferences, tab.threadPage.url);
                        tab.postViews = new LinkedHashMap<>();
                        tab.readerView = buildThreadView(tab.threadPage, tab);
                    }
                } else if (NATIVE_SEARCH.equals(tab.nativeKind) || NATIVE_BOARD.equals(tab.nativeKind)) {
                    tab.searchPage = searchPageFromJson(item.optJSONObject("searchPage"));
                    if (tab.searchPage != null) {
                        tab.readerMode = true;
                        tab.readerView = buildSearchView(tab.searchPage);
                    }
                } else if (NATIVE_SEARCH_HOME.equals(tab.nativeKind) || url.isEmpty()) {
                    tab.readerMode = true;
                    tab.readerView = buildSearchHomeView(true);
                }
                if (tab.navigationHistory.isEmpty() && url != null && !url.isEmpty()) {
                    tab.navigationHistory.add(url);
                    tab.navigationIndex = 0;
                }
                tab.returnToIndex = -1;
                tabs.add(tab);
            }
            switchToTab(selected);
            CuspTab tab = currentTab();
            if (tab != null) {
                if (NATIVE_THREAD.equals(tab.nativeKind) && tab.threadPage != null && !tab.threadPage.posts.isEmpty()) {
                    restoreThreadScroll(tab);
                } else if ((NATIVE_SEARCH.equals(tab.nativeKind) || NATIVE_BOARD.equals(tab.nativeKind)) && tab.searchPage != null) {
                    tab.readerMode = true;
                    tab.readerView = buildSearchView(tab.searchPage);
                    switchToTab(selected);
                } else if (tab.url == null || tab.url.isEmpty()) {
                    tab.readerMode = true;
                    tab.nativeKind = NATIVE_SEARCH_HOME;
                    tab.readerView = buildSearchHomeView(true);
                    switchToTab(selected);
                    clearAddressFocus();
                } else {
                    openInCurrentTab(tab.url);
                }
            }
            renderTabs();
            return true;
        } catch (Exception error) {
            return false;
        }
    }

    private void saveTabs() {
        try {
            CuspTab current = currentTab();
            if (current != null) {
                rememberThreadScroll(current);
            }
            JSONArray array = new JSONArray();
            for (CuspTab tab : tabs) {
                JSONObject item = new JSONObject();
                item.put("url", tab.url == null ? "" : tab.url);
                item.put("title", tab.title == null ? "Tab" : tab.title);
                item.put("nativeKind", tab.nativeKind == null ? JSONObject.NULL : tab.nativeKind);
                item.put("threadScrollRatio", tab.threadScrollRatio);
                item.put("threadBottomOffset", tab.threadBottomOffset);
                item.put("navigationIndex", tab.navigationIndex);
                JSONArray history = new JSONArray();
                for (String historyUrl : tab.navigationHistory) {
                    history.put(historyUrl);
                }
                item.put("navigationHistory", history);
                if (NATIVE_THREAD.equals(tab.nativeKind) && tab.threadPage != null && tab.threadPage.error == null) {
                    item.put("threadPage", threadPageToJson(tab.threadPage));
                } else if ((NATIVE_SEARCH.equals(tab.nativeKind) || NATIVE_BOARD.equals(tab.nativeKind))
                        && tab.searchPage != null && tab.searchPage.error == null) {
                    item.put("searchPage", searchPageToJson(tab.searchPage));
                }
                array.put(item);
            }
            JSONObject root = new JSONObject();
            root.put("current", Math.max(0, currentIndex));
            root.put("tabs", array);
            preferences.edit().putString(PREF_TABS, root.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private JSONObject threadPageToJson(ThreadPage page) throws Exception {
        JSONObject object = new JSONObject();
        object.put("url", page.url);
        object.put("title", page.title);
        JSONArray posts = new JSONArray();
        for (Post post : page.posts) {
            JSONObject item = new JSONObject();
            item.put("number", post.number);
            item.put("name", post.name);
            item.put("date", post.date);
            item.put("body", post.body);
            posts.put(item);
        }
        object.put("posts", posts);
        return object;
    }

    private ThreadPage threadPageFromJson(JSONObject object) {
        if (object == null) {
            return null;
        }
        ThreadPage page = new ThreadPage();
        page.url = object.optString("url", "");
        page.title = object.optString("title", hostTitle(page.url));
        JSONArray posts = object.optJSONArray("posts");
        if (posts != null) {
            for (int i = 0; i < posts.length(); i++) {
                JSONObject item = posts.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                Post post = new Post();
                post.number = item.optInt("number", i + 1);
                post.name = item.optString("name", "");
                post.date = item.optString("date", "");
                post.body = item.optString("body", "");
                page.posts.add(post);
                page.postsByNumber.put(post.number, post);
            }
        }
        return page;
    }

    private JSONObject searchPageToJson(SearchPage page) throws Exception {
        JSONObject object = new JSONObject();
        object.put("url", page.url);
        object.put("title", page.title);
        JSONArray results = new JSONArray();
        for (SearchResult result : page.results) {
            JSONObject item = new JSONObject();
            item.put("title", result.title);
            item.put("url", result.url);
            item.put("meta", result.meta);
            results.put(item);
        }
        object.put("results", results);
        return object;
    }

    private SearchPage searchPageFromJson(JSONObject object) {
        if (object == null) {
            return null;
        }
        SearchPage page = new SearchPage();
        page.url = object.optString("url", "");
        page.title = object.optString("title", searchTitle(page.url));
        JSONArray results = object.optJSONArray("results");
        if (results != null) {
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                SearchResult result = new SearchResult();
                result.title = item.optString("title", "");
                result.url = item.optString("url", "");
                result.meta = item.optString("meta", "");
                page.results.add(result);
            }
        }
        return page;
    }

    private void restoreNavigationHistory(CuspTab tab, JSONObject item) {
        JSONArray history = item.optJSONArray("navigationHistory");
        if (history != null) {
            for (int j = 0; j < history.length(); j++) {
                String url = history.optString(j, "");
                if (!url.isEmpty()) {
                    tab.navigationHistory.add(url);
                }
            }
        }
        if (tab.navigationHistory.isEmpty()) {
            tab.navigationIndex = -1;
        } else {
            tab.navigationIndex = Math.max(0, Math.min(item.optInt("navigationIndex", tab.navigationHistory.size() - 1),
                    tab.navigationHistory.size() - 1));
        }
    }

    private void switchToTab(int index) {
        if (index < 0 || index >= tabs.size()) {
            return;
        }
        tabOverviewVisible = false;
        pendingNewTab = false;
        pendingHistoryAll = false;
        CuspTab previous = currentTab();
        if (previous != null) {
            rememberThreadScroll(previous);
        }
        if (highlightedPostView != null) {
            clearJumpHighlight();
        }
        clearAddressFocus();
        if (index != currentIndex && !replyPopups.isEmpty()) {
            dismissThreadPopups();
        }
        currentIndex = index;
        CuspTab tab = tabs.get(index);
        contentFrame.removeAllViews();
        visibleThreadPage = null;
        visibleThreadScroll = null;
        visiblePostViews.clear();
        if (tab.readerView != null) {
            ViewGroup oldParent = (ViewGroup) tab.readerView.getParent();
            if (oldParent != null) {
                oldParent.removeView(tab.readerView);
            }
            contentFrame.addView(tab.readerView);
            if (NATIVE_THREAD.equals(tab.nativeKind)) {
                visibleThreadPage = tab.threadPage;
                visibleThreadScroll = tab.threadScroll;
            }
            if (NATIVE_THREAD.equals(tab.nativeKind) && tab.postViews != null) {
                visiblePostViews.putAll(tab.postViews);
            }
        }
        addressBar.setText(tab.url == null ? "" : tab.url);
        updateBottomThreadBar(tab);
        updateThreadSearchBar(tab);
        renderTabs();
    }

    private void updateBottomThreadBar(CuspTab tab) {
        if (bottomThreadBar == null || bottomThreadTitle == null || bottomWriteButton == null) {
            return;
        }
        if (tabOverviewVisible) {
            bottomThreadBar.setVisibility(View.GONE);
        } else if (pendingNewTab) {
            bottomThreadTitle.setText(text("\u65b0\u898f\u30bf\u30d6", "New tab"));
            bottomWriteButton.setVisibility(View.GONE);
            bottomThreadBar.setVisibility(View.VISIBLE);
        } else if (tab != null) {
            String title = tab.threadPage != null && tab.threadPage.title != null ? tab.threadPage.title : tab.title;
            bottomThreadTitle.setText(title == null || title.trim().isEmpty() ? text("\u30bf\u30d6", "Tab") : title);
            boolean canWrite = NATIVE_THREAD.equals(tab.nativeKind) && tab.threadPage != null && tab.threadPage.error == null;
            bottomWriteButton.setVisibility(canWrite ? View.VISIBLE : View.GONE);
            bottomThreadBar.setVisibility(View.VISIBLE);
        } else {
            bottomThreadBar.setVisibility(View.GONE);
        }
    }

    private void renderTabs() {
        if (bottomToolbar != null) {
            bottomToolbar.setVisibility(tabOverviewVisible ? View.GONE : View.VISIBLE);
        }
        if (tabCountButton != null) {
            tabCountButton.setText(tabs.size() > 99 ? "\u221e" : String.valueOf(tabs.size()));
            tabCountButton.setBackground(tabCountBackground(tabOverviewVisible));
        }
        updateBottomThreadBar(pendingNewTab ? null : currentTab());
    }

    private void openFromAddressBar() {
        String input = addressBar.getText().toString().trim();
        if (input.isEmpty()) {
            return;
        }
        clearAddressFocus();
        boolean urlLike = looksLikeUrl(input);
        String url = urlLike ? normalizeUrl(input) : searchUrl(input);
        if (pendingNewTab) {
            pendingNewTab = false;
            createTab(url, true, -1, true);
            return;
        }
        openInCurrentTab(url);
    }

    private void openInCurrentTab(String url) {
        openInCurrentTab(url, true);
    }

    private void openInCurrentTab(String url, boolean addHistory) {
        CuspTab tab = currentTab();
        if (tab == null) {
            createTab(url, true);
            return;
        }
        url = normalizeUrl(url);
        if (isThreadUrl(url)) {
            if (addHistory) {
                recordNavigation(tab, url);
            }
            loadThread(tab, url);
            return;
        }
        if (isFindSearchUrl(url)) {
            if (addHistory) {
                recordNavigation(tab, url);
            }
            loadSearchResults(tab, url);
            return;
        }
        if (isFindHomeUrl(url)) {
            if (addHistory) {
                recordNavigation(tab, url);
            }
            loadSearchHome(tab, url);
            return;
        }
        if (isBoardUrl(url)) {
            if (addHistory) {
                recordNavigation(tab, url);
            }
            loadBoard(tab, url);
            return;
        }
        openExternal(url);
    }

    private void recordNavigation(CuspTab tab, String url) {
        if (url == null || url.isEmpty()) {
            return;
        }
        if (tab.navigationIndex >= 0
                && tab.navigationIndex < tab.navigationHistory.size()
                && url.equals(tab.navigationHistory.get(tab.navigationIndex))) {
            return;
        }
        while (tab.navigationHistory.size() > tab.navigationIndex + 1) {
            tab.navigationHistory.remove(tab.navigationHistory.size() - 1);
        }
        tab.navigationHistory.add(url);
        tab.navigationIndex = tab.navigationHistory.size() - 1;
    }

    private void loadThread(CuspTab tab, String url) {
        loadThread(tab, url, true);
    }

    private void loadThread(CuspTab tab, String url, boolean showFullLoading) {
        rememberThreadScroll(tab);
        tab.readerMode = true;
        tab.nativeKind = NATIVE_THREAD;
        tab.url = url;
        tab.title = hostTitle(url);
        tab.searchPage = null;
        if (showFullLoading || tab.readerView == null) {
            tab.readerView = loadingView("");
            switchToTab(tabs.indexOf(tab));
        }
        progressBar.setVisibility(View.VISIBLE);

        ioExecutor.execute(() -> {
            ThreadPage page;
            try {
                page = downloadDatThread(url);
                if (page == null) {
                    String html = download(url);
                    page = parseThread(url, html);
                }
            } catch (Exception error) {
                page = ThreadPage.error(url, error.getMessage());
            }
                ThreadPage result = page;
            runOnUiThread(() -> {
                tab.title = result.title;
                tab.threadPage = result;
                tab.readPostNumber = readPostNumber(preferences, result.url);
                tab.postViews = new LinkedHashMap<>();
                tab.readerView = buildThreadView(result, tab);
                if (result.error == null && !result.posts.isEmpty()) {
                    addThreadHistory(result.url, result.title);
                }
                progressBar.setVisibility(View.GONE);
                if (tab == currentTab()) {
                    switchToTab(currentIndex);
                    if (tab.threadSearchOpen && tab.threadSearchQuery != null && !tab.threadSearchQuery.trim().isEmpty()) {
                        updateThreadSearch(tab.threadSearchQuery, false);
                    }
                    restoreThreadScroll(tab);
                }
                renderTabs();
            });
        });
    }

    private void refreshThreadFromBottom(CuspTab tab) {
        refreshThreadFromBottom(tab, false, false);
    }

    private void refreshThreadFromBottom(CuspTab tab, boolean forceScrollToBottom) {
        refreshThreadFromBottom(tab, forceScrollToBottom, false);
    }

    private void refreshThreadFromBottom(CuspTab tab, boolean forceScrollToBottom, boolean centerSpinner) {
        if (tab == null || tab.url == null || tab.url.isEmpty()) {
            if (centerSpinner) {
                hideCenterSpinner();
            }
            return;
        }
        if (centerSpinner) {
            showCenterSpinner();
        }
        ioExecutor.execute(() -> {
            ThreadPage page;
            try {
                page = downloadDatThread(tab.url);
                if (page == null) {
                    String html = download(tab.url);
                    page = parseThread(tab.url, html);
                }
            } catch (Exception error) {
                page = ThreadPage.error(tab.url, error.getMessage());
            }
            ThreadPage result = page;
            runOnUiThread(() -> {
                if (centerSpinner) {
                    hideCenterSpinner();
                }
                if (tab.threadBottomLoader != null) {
                    setBottomRefreshSpinning(tab.threadBottomLoader, true);
                    tab.threadBottomLoader.clearAnimation();
                    tab.threadBottomLoader.setTranslationY(dp(58));
                    tab.threadBottomLoader.setRotation(0f);
                    setBottomRefreshSpinning(tab.threadBottomLoader, false);
                    tab.threadBottomLoader.setVisibility(View.GONE);
                }
                if (result.error != null) {
                    Toast.makeText(this, result.error, Toast.LENGTH_SHORT).show();
                    return;
                }
                int oldCount = tab.threadPage == null ? 0 : tab.threadPage.posts.size();
                if (oldCount <= 0 || tab.threadList == null || tab.postViews == null) {
                    tab.threadPage = result;
                    tab.readPostNumber = readPostNumber(preferences, result.url);
                    tab.postViews = new LinkedHashMap<>();
                    tab.readerView = buildThreadView(result, tab);
                    if (tab == currentTab()) {
                        switchToTab(currentIndex);
                        if (tab.threadSearchOpen && tab.threadSearchQuery != null && !tab.threadSearchQuery.trim().isEmpty()) {
                            updateThreadSearch(tab.threadSearchQuery, false);
                        }
                        if (forceScrollToBottom) {
                            scrollCurrentThreadToBottom();
                        }
                    }
                    return;
                }
                if (result.posts.size() <= oldCount) {
                    tab.threadPage = result;
                    tab.readPostNumber = Math.max(tab.readPostNumber, readPostNumber(preferences, result.url));
                    if (!centerSpinner && !forceScrollToBottom) {
                        markReadTo(tab, maxPostNumber(result), false);
                        renderTabs();
                    }
                    if (tab == currentTab() && tab.threadSearchOpen
                            && tab.threadSearchQuery != null && !tab.threadSearchQuery.trim().isEmpty()) {
                        updateThreadSearch(tab.threadSearchQuery, false);
                    }
                    if (forceScrollToBottom) {
                        scrollCurrentThreadToBottom();
                    }
                    return;
                }
                int insertIndex = tab.threadList.getChildCount();
                tab.threadPage = result;
                tab.readPostNumber = Math.max(tab.readPostNumber, readPostNumber(preferences, result.url));
                if (!centerSpinner && !forceScrollToBottom) {
                    markReadTo(tab, maxPostNumber(result), false);
                }
                for (int i = oldCount; i < result.posts.size(); i++) {
                    addPostCard(tab.threadList, result, tab, result.posts.get(i), insertIndex++);
                }
                tab.title = result.title;
                if (result.error == null && !result.posts.isEmpty()) {
                    addThreadHistory(result.url, result.title);
                }
                if (tab == currentTab() && tab.threadSearchOpen
                        && tab.threadSearchQuery != null && !tab.threadSearchQuery.trim().isEmpty()) {
                    updateThreadSearch(tab.threadSearchQuery, false);
                }
                if (tab == currentTab() && forceScrollToBottom) {
                    scrollCurrentThreadToBottom();
                }
                renderTabs();
            });
        });
    }

    private void loadSearchResults(CuspTab tab, String url) {
        loadSearchResults(tab, url, true);
    }

    private void loadSearchResults(CuspTab tab, String url, boolean foreground) {
        tab.readerMode = true;
        tab.nativeKind = NATIVE_SEARCH;
        tab.url = url;
        tab.title = searchTitle(url);
        if (foreground || tab.readerView == null) {
            tab.readerView = loadingView("");
        }
        tab.threadPage = null;
        tab.searchPage = null;
        tab.threadScroll = null;
        tab.postViews = null;
        if (foreground) {
            switchToTab(tabs.indexOf(tab));
            progressBar.setVisibility(View.VISIBLE);
        }

        ioExecutor.execute(() -> {
            SearchPage page;
            try {
                String html = download(url);
                page = parseSearchPage(url, html);
            } catch (Exception error) {
                page = SearchPage.error(url, error.getMessage());
            }
            SearchPage result = page;
            runOnUiThread(() -> {
                tab.title = result.title;
                tab.searchPage = result;
                tab.readerView = buildSearchView(result);
                if (foreground) {
                    progressBar.setVisibility(View.GONE);
                }
                if (tab == currentTab() && !tabOverviewVisible) {
                    switchToTab(currentIndex);
                }
                renderTabs();
            });
        });
    }

    private void loadSearchHome(CuspTab tab, String url) {
        loadSearchHome(tab, url, true);
    }

    private void loadSearchHome(CuspTab tab, String url, boolean foreground) {
        tab.readerMode = true;
        tab.nativeKind = NATIVE_SEARCH_HOME;
        tab.url = url;
        tab.title = text("5ch\u691c\u7d22", "5ch Search");
        tab.threadPage = null;
        tab.searchPage = null;
        tab.threadScroll = null;
        tab.postViews = null;
        tab.readerView = buildSearchHomeView(true);
        if (foreground) {
            switchToTab(tabs.indexOf(tab));
        }
        renderTabs();
    }

    private void loadBoard(CuspTab tab, String url) {
        loadBoard(tab, url, true);
    }

    private void loadBoard(CuspTab tab, String url, boolean foreground) {
        tab.readerMode = true;
        tab.nativeKind = NATIVE_BOARD;
        tab.url = url;
        tab.title = boardTitle(url);
        if (foreground || tab.readerView == null) {
            tab.readerView = loadingView("");
        }
        tab.threadPage = null;
        tab.searchPage = null;
        tab.threadScroll = null;
        tab.postViews = null;
        if (foreground) {
            switchToTab(tabs.indexOf(tab));
            progressBar.setVisibility(View.VISIBLE);
        }

        ioExecutor.execute(() -> {
            SearchPage page;
            try {
                page = downloadBoard(url);
            } catch (Exception error) {
                page = SearchPage.error(url, error.getMessage());
            }
            SearchPage result = page;
            runOnUiThread(() -> {
                tab.title = result.title;
                tab.searchPage = result;
                tab.readerView = buildSearchView(result);
                if (foreground) {
                    progressBar.setVisibility(View.GONE);
                }
                if (tab == currentTab() && !tabOverviewVisible) {
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
        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        box.addView(spinner, new LinearLayout.LayoutParams(dp(44), dp(44)));
        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(TEXT);
        text.setTextSize(16);
        text.setPadding(0, dp(10), 0, 0);
        if (message != null && !message.isEmpty()) {
            box.addView(text);
        }
        return box;
    }

    private View buildThreadView(ThreadPage page, CuspTab tab) {
        ScrollView scroll = new ScrollView(this);
        tab.threadScroll = scroll;
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOnClickListener(v -> dismissThreadPopups());
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(24));
        tab.threadList = list;
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
            TextView error = postText(page.error, page);
            error.setTextColor(Color.rgb(185, 28, 28));
            list.addView(error);
            return scroll;
        }

        for (Post post : page.posts) {
            if (matchesNgWord(post)) {
                continue;
            }
            addPostCard(list, page, tab, post, list.getChildCount());
        }

        if (page.posts.isEmpty()) {
            list.addView(postText(text("\u66f8\u304d\u8fbc\u307f\u3092\u89e3\u6790\u3067\u304d\u307e\u305b\u3093", "No posts were parsed. Use reload or open another URL."), page));
        }
        FrameLayout bottomLoader = bottomRefreshLoader();
        bottomLoader.setVisibility(View.GONE);
        bottomLoader.setTranslationY(dp(58));
        tab.threadBottomLoader = bottomLoader;

        enableBottomPullRefresh(scroll, bottomLoader, () -> {
            refreshThreadFromBottom(tab);
        });
        FrameLayout frame = new FrameLayout(this);
        frame.addView(withScrollScrubber(scroll), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams loaderParams = new FrameLayout.LayoutParams(dp(66), dp(66),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        loaderParams.setMargins(0, 0, 0, dp(2));
        frame.addView(bottomLoader, loaderParams);
        return frame;
    }

    private void addPostCard(LinearLayout list, ThreadPage page, CuspTab tab, Post post, int index) {
        if (matchesNgWord(post)) {
            return;
        }
        FrameLayout shell = new FrameLayout(this);
        shell.setClipChildren(false);
        shell.setBackgroundColor(Color.rgb(238, 244, 247));
        ImageView readAction = swipeActionIcon(R.drawable.ic_check, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        ImageView replyAction = swipeActionIcon(R.drawable.ic_reply, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        shell.addView(readAction);
        shell.addView(replyAction);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(8), dp(10), dp(10));
        card.setBackgroundColor(post.number > tab.readPostNumber ? Color.rgb(232, 247, 244) : Color.rgb(250, 251, 252));
        card.setOnLongClickListener(v -> {
            showPostActionMenu(card, tab, post);
            return true;
        });
        attachPostSwipe(card, card, readAction, replyAction, tab, post);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dp(8));

        View metaView = postMetaText(post, page, () -> showPostActionMenu(card, tab, post));
        card.addView(metaView);

        View bodyView = postBodyView(card, page, tab, post);
        card.addView(bodyView);
        attachPostSwipeDeep(metaView, card, readAction, replyAction, tab, post);
        attachPostSwipeDeep(bodyView, card, readAction, replyAction, tab, post);
        shell.addView(card, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        list.addView(shell, Math.max(0, Math.min(index, list.getChildCount())), cardParams);
        tab.postViews.put(post.number, card);
    }

    private ImageView swipeActionIcon(int iconRes, int gravity) {
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(TEAL);
        icon.setAlpha(0f);
        icon.setPadding(dp(12), dp(12), dp(12), dp(12));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(56), dp(56), gravity);
        icon.setLayoutParams(params);
        return icon;
    }

    private void attachPostSwipeDeep(View trigger, View card, View readAction, View replyAction, CuspTab tab, Post post) {
        attachPostSwipe(trigger, card, readAction, replyAction, tab, post);
        if (trigger instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) trigger;
            for (int i = 0; i < group.getChildCount(); i++) {
                attachPostSwipeDeep(group.getChildAt(i), card, readAction, replyAction, tab, post);
            }
        }
    }

    private void attachPostSwipe(View trigger, View card, View readAction, View replyAction, CuspTab tab, Post post) {
        final float[] downX = new float[1];
        final float[] downY = new float[1];
        final boolean[] dragging = new boolean[1];
        trigger.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downX[0] = event.getRawX();
                downY[0] = event.getRawY();
                dragging[0] = false;
                card.clearAnimation();
                return false;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = event.getRawX() - downX[0];
                float dy = event.getRawY() - downY[0];
                if (!dragging[0] && Math.abs(dx) > dp(12) && Math.abs(dx) > Math.abs(dy) * 1.4f) {
                    dragging[0] = true;
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (dragging[0]) {
                    float translation = Math.max(-dp(92), Math.min(dp(92), dx * 0.55f));
                    card.setTranslationX(translation);
                    readAction.setAlpha(Math.max(0f, Math.min(1f, translation / dp(64))));
                    replyAction.setAlpha(Math.max(0f, Math.min(1f, -translation / dp(64))));
                    return true;
                }
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (dragging[0]) {
                    float tx = card.getTranslationX();
                    card.animate().translationX(0).setDuration(130).start();
                    readAction.animate().alpha(0f).setDuration(130).start();
                    replyAction.animate().alpha(0f).setDuration(130).start();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (tx <= -dp(54)) {
                            showWriteDialog(">>" + post.number + "\n");
                        } else if (tx >= dp(54)) {
                            markReadTo(tab, post.number);
                        }
                    }
                    return true;
                }
            }
            return false;
        });
    }

    private FrameLayout bottomRefreshLoader() {
        FrameLayout loader = new FrameLayout(this);
        ImageView arrow = new ImageView(this);
        arrow.setImageResource(R.drawable.ic_refresh);
        arrow.setColorFilter(TEAL);
        arrow.setScaleType(ImageView.ScaleType.FIT_CENTER);
        loader.addView(arrow, new FrameLayout.LayoutParams(dp(58), dp(58), Gravity.CENTER));

        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        spinner.setVisibility(View.GONE);
        loader.addView(spinner, new FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER));
        return loader;
    }

    private TextView postMetaText(Post post, ThreadPage page, Runnable longClickAction) {
        TextView meta = new TextView(this);
        String value = post.number + "  " + post.name + "  " + post.date;
        SpannableString text = new SpannableString(value);
        Matcher matcher = POST_ID_PATTERN.matcher(value);
        while (matcher.find()) {
            String id = matcher.group(1);
            text.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    showIdPopup(widget, page, id);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(TEAL);
                    ds.setUnderlineText(false);
                }
            }, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        meta.setText(text);
        meta.setTextColor(Color.rgb(79, 91, 103));
        meta.setLinkTextColor(TEAL);
        meta.setTextSize(12);
        meta.setPadding(0, 0, 0, dp(5));
        meta.setMovementMethod(LinkMovementMethod.getInstance());
        meta.setOnLongClickListener(v -> {
            if (longClickAction != null) {
                longClickAction.run();
                return true;
            }
            return false;
        });
        return meta;
    }

    private void showPostActionMenu(View anchor, CuspTab tab, Post post) {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setPadding(dp(18), dp(8), dp(18), 0);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(">>" + post.number)
                .setView(menu)
                .create();

        menu.addView(dialogAction(R.drawable.ic_reply, text("\u8fd4\u4fe1", "Reply"), () -> {
            dialog.dismiss();
            showWriteDialog(">>" + post.number + "\n");
        }));
        menu.addView(dialogAction(R.drawable.ic_check, text("\u3053\u3053\u307e\u3067\u3092\u65e2\u8aad\u306b\u3059\u308b", "Mark read to here"), () -> {
            dialog.dismiss();
            markReadTo(tab, post.number);
        }));
        menu.addView(dialogAction(R.drawable.ic_text_fields, post.aaMode ? text("\u901a\u5e38\u8868\u793a", "Normal view") : text("AA\u8868\u793a", "AA view"), () -> {
            dialog.dismiss();
            toggleAaMode(tab, post);
        }));
        menu.addView(dialogAction(R.drawable.ic_copy, text("\u30b3\u30d4\u30fc", "Copy"), () -> {
            dialog.dismiss();
            showPostCopyDialog(post);
        }));
        dialog.show();
    }

    private View dialogAction(int iconRes, String label, Runnable action) {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.HORIZONTAL);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(dp(12), 0, dp(12), 0);
        view.setBackground(roundedDrawable(Color.rgb(250, 251, 252), BORDER, dp(8)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        params.setMargins(0, 0, 0, dp(8));
        view.setLayoutParams(params);
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(TEAL);
        view.addView(icon, new LinearLayout.LayoutParams(dp(26), dp(26)));
        TextView textView = new TextView(this);
        textView.setText(label);
        textView.setTextColor(TEXT);
        textView.setTextSize(16);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        textParams.setMargins(dp(12), 0, 0, 0);
        view.addView(textView, textParams);
        view.setOnClickListener(v -> action.run());
        return view;
    }

    private void showPostCopyDialog(Post post) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(8), dp(18), 0);
        TextView body = new TextView(this);
        body.setText(post.body);
        body.setTextColor(TEXT);
        body.setTextSize(15);
        body.setTextIsSelectable(true);
        body.setPadding(dp(10), dp(10), dp(10), dp(10));
        ScrollView scroll = new ScrollView(this);
        scroll.addView(body, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(320)));
        Button copyAll = new Button(this);
        copyAll.setText(text("\u5168\u4f53\u3092\u30b3\u30d4\u30fc", "Copy all"));
        copyAll.setAllCaps(false);
        root.addView(copyAll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(text("\u66f8\u304d\u8fbc\u307f\u3092\u30b3\u30d4\u30fc", "Copy post") + " >>" + post.number)
                .setView(root)
                .setPositiveButton("OK", null)
                .create();
        copyAll.setOnClickListener(v -> {
            ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (manager != null) {
                manager.setPrimaryClip(ClipData.newPlainText("CuspiDroid post", post.body));
                Toast.makeText(this, text("\u30b3\u30d4\u30fc\u3057\u307e\u3057\u305f", "Copied."), Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private boolean matchesNgWord(Post post) {
        if (post == null) {
            return false;
        }
        String haystack = (post.name + "\n" + post.date + "\n" + post.body).toLowerCase(Locale.ROOT);
        for (String word : ngWords()) {
            if (!word.isEmpty() && haystack.contains(word.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private List<String> ngWords() {
        List<String> words = new ArrayList<>();
        String saved = preferences.getString(PREF_NG_WORDS, "");
        for (String line : saved.split("\\r?\\n")) {
            String word = line.trim();
            if (!word.isEmpty()) {
                words.add(word);
            }
        }
        return words;
    }

    private View buildSearchView(SearchPage page) {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText(page.title);
        title.setTextColor(TEXT);
        title.setTextSize(20);
        title.setPadding(0, 0, 0, dp(10));
        list.addView(title);

        if (page.error != null) {
            TextView error = postText(page.error, null);
            error.setTextColor(Color.rgb(185, 28, 28));
            list.addView(error);
            return scroll;
        }

        for (SearchResult result : page.results) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(10), dp(9), dp(10), dp(9));
            row.setBackgroundColor(Color.rgb(250, 251, 252));
            row.setOnClickListener(v -> routeLink(result.url, currentTab()));
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, dp(8));

            TextView resultTitle = new TextView(this);
            resultTitle.setText(result.title);
            resultTitle.setTextColor(TEXT);
            resultTitle.setTextSize(16);
            resultTitle.setPadding(0, 0, 0, dp(4));
            row.addView(resultTitle);

            TextView meta = new TextView(this);
            meta.setText(result.meta);
            meta.setTextColor(Color.rgb(79, 91, 103));
            meta.setTextSize(12);
            row.addView(meta);
            list.addView(row, rowParams);
        }

        if (page.results.isEmpty()) {
            list.addView(postText(text("\u691c\u7d22\u7d50\u679c\u306a\u3057", "No search results."), null));
        }
        return withScrollScrubber(scroll);
    }

    private View withScrollScrubber(ScrollView scroll) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(scroll, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        FrameLayout scrubber = new FrameLayout(this);
        root.addView(scrubber, new LinearLayout.LayoutParams(
                dp(34), ViewGroup.LayoutParams.MATCH_PARENT));

        View rail = new View(this);
        rail.setBackgroundColor(Color.argb(28, 31, 41, 55));
        FrameLayout.LayoutParams railParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        scrubber.addView(rail, railParams);

        View thumb = new View(this);
        GradientDrawable thumbBackground = new GradientDrawable();
        thumbBackground.setColor(Color.argb(170, 15, 118, 110));
        thumbBackground.setCornerRadius(dp(8));
        thumb.setBackground(thumbBackground);
        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(dp(16), dp(56));
        thumbParams.gravity = Gravity.CENTER_HORIZONTAL;
        scrubber.addView(thumb, thumbParams);

        Runnable updateThumb = () -> {
            int range = scroll.getChildCount() == 0 ? 0 : scroll.getChildAt(0).getHeight() - scroll.getHeight();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) thumb.getLayoutParams();
            if (range <= 0) {
                thumb.setVisibility(View.GONE);
                return;
            }
            thumb.setVisibility(View.VISIBLE);
            int frameHeight = Math.max(1, scrubber.getHeight());
            int thumbHeight = Math.max(dp(42), frameHeight * scroll.getHeight() / Math.max(scroll.getChildAt(0).getHeight(), 1));
            int maxTop = Math.max(0, frameHeight - thumbHeight);
            params.height = thumbHeight;
            params.topMargin = maxTop * scroll.getScrollY() / range;
            thumb.setLayoutParams(params);
        };

        scroll.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> updateThumb.run());
        scrubber.post(updateThumb);
        rail.setOnTouchListener(scrubberTouchListener(scroll, scrubber, thumb));
        thumb.setOnTouchListener(scrubberTouchListener(scroll, scrubber, thumb));
        return root;
    }

    private View.OnTouchListener scrubberTouchListener(ScrollView scroll, View frame, View thumb) {
        return (view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE
                    || event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                scroll.getParent().requestDisallowInterceptTouchEvent(true);
                return handleScrubberDrag(event, scroll, frame, thumb);
            }
            return false;
        };
    }

    private boolean handleScrubberDrag(MotionEvent event, ScrollView scroll, View frame, View thumb) {
        if (event.getAction() != MotionEvent.ACTION_DOWN
                && event.getAction() != MotionEvent.ACTION_MOVE
                && event.getAction() != MotionEvent.ACTION_UP
                && event.getAction() != MotionEvent.ACTION_CANCEL) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            scroll.fling(0);
            scroll.requestFocus();
        }
        int range = scroll.getChildCount() == 0 ? 0 : scroll.getChildAt(0).getHeight() - scroll.getHeight();
        if (range <= 0) {
            return true;
        }
        int thumbHeight = Math.max(thumb.getHeight(), dp(42));
        int usable = Math.max(1, frame.getHeight() - thumbHeight);
        float y = event.getRawY();
        int[] frameLocation = new int[2];
        frame.getLocationOnScreen(frameLocation);
        float localY = y - frameLocation[1] - thumbHeight / 2f;
        float ratio = Math.max(0f, Math.min(1f, localY / usable));
        scroll.scrollTo(0, (int) (range * ratio));
        return true;
    }

    private void enableBottomPullRefresh(ScrollView scroll, View loader, Runnable refresh) {
        final float[] downY = new float[1];
        final float[] pullDistance = new float[1];
        final boolean[] startedAtBottom = new boolean[1];
        final boolean[] dragging = new boolean[1];
        final boolean[] refreshing = new boolean[1];
        scroll.setOnTouchListener((v, event) -> {
            int hiddenOffset = dp(58);
            int maxOffset = -dp(86);
            int maxPull = dp(164);
            int triggerPull = maxPull / 2;
            int triggerOffset = hiddenOffset + (maxOffset - hiddenOffset) / 2;
            if (refreshing[0]) {
                if (loader.getVisibility() == View.GONE) {
                    refreshing[0] = false;
                } else {
                    return false;
                }
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downY[0] = event.getY();
                pullDistance[0] = 0;
                startedAtBottom[0] = !scroll.canScrollVertically(1);
                dragging[0] = false;
                if (!refreshing[0]) {
                    setBottomRefreshSpinning(loader, false);
                    loader.clearAnimation();
                    loader.setVisibility(View.GONE);
                    loader.setTranslationY(hiddenOffset);
                    loader.setRotation(0f);
                }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (startedAtBottom[0] && !refreshing[0]) {
                    float pull = Math.max(0, downY[0] - event.getY());
                    pullDistance[0] = pull;
                    if (pull > dp(4)) {
                        dragging[0] = true;
                        loader.clearAnimation();
                        loader.setVisibility(View.VISIBLE);
                        float clampedPull = Math.min(pull, maxPull);
                        float progress = clampedPull / maxPull;
                        setBottomRefreshSpinning(loader, false);
                        loader.setTranslationY(hiddenOffset + (maxOffset - hiddenOffset) * progress);
                        loader.setRotation(progress * 270f);
                        return true;
                    }
                    if (dragging[0]) {
                        loader.setTranslationY(hiddenOffset);
                        loader.setRotation(0f);
                        return true;
                    }
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (dragging[0] && event.getAction() == MotionEvent.ACTION_UP && pullDistance[0] >= triggerPull) {
                    refreshing[0] = true;
                    loader.setVisibility(View.VISIBLE);
                    loader.setRotation(0f);
                    setBottomRefreshSpinning(loader, true);
                    loader.animate().translationY(triggerOffset).setDuration(110).withEndAction(() -> {
                        refresh.run();
                    }).start();
                    return true;
                }
                if (dragging[0] || loader.getVisibility() == View.VISIBLE) {
                    loader.animate().translationY(hiddenOffset).setDuration(140)
                            .withEndAction(() -> {
                                setBottomRefreshSpinning(loader, false);
                                loader.setVisibility(View.GONE);
                                loader.setRotation(0f);
                            }).start();
                    return dragging[0];
                }
            }
            return false;
        });
    }

    private void setBottomRefreshSpinning(View loader, boolean spinning) {
        if (!(loader instanceof ViewGroup)) {
            return;
        }
        ViewGroup group = (ViewGroup) loader;
        if (group.getChildCount() < 2) {
            return;
        }
        group.getChildAt(0).setVisibility(spinning ? View.GONE : View.VISIBLE);
        group.getChildAt(1).setVisibility(spinning ? View.VISIBLE : View.GONE);
    }

    private View buildSearchHomeView(boolean fullHistory) {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        list.addView(sectionTitleView("5ch"));
        addBoardFolder(list, text("\u30cb\u30e5\u30fc\u30b9", "News"), new String[][]{
                {text("\u30cb\u30e5\u30fc\u30b9\u901f\u5831+", "News+"), "https://asahi.5ch.net/newsplus/"},
                {text("\u82b8\u30b9\u30dd\u901f\u5831+", "Entertainment News+"), "https://hayabusa9.5ch.net/mnewsplus/"},
                {text("\u30cb\u30e5\u30fc\u30b9\u901f\u5831", "Breaking News"), "https://hayabusa9.5ch.net/news/"}
        });
        addBoardFolder(list, text("\u6587\u5316", "Culture"), new String[][]{
                {text("\u6620\u753b\u4e00\u822c", "Movies"), "https://lavender.5ch.net/movie/"},
                {text("\u97f3\u697d\u4e00\u822c", "Music"), "https://lavender.5ch.net/music/"},
                {text("\u8aad\u66f8", "Books"), "https://mevius.5ch.net/books/"}
        });
        addBoardFolder(list, text("\u6280\u8853", "Technology"), new String[][]{
                {text("\u30d7\u30ed\u30b0\u30e9\u30de\u30fc", "Programming"), "https://medaka.5ch.net/prog/"},
                {"Linux", "https://mao.5ch.net/linux/"},
                {text("\u81ea\u4f5cPC", "Custom PC"), "https://egg.5ch.net/jisaku/"}
        });
        List<BbsLink> customLinks = readBbsLinks(preferences);
        if (!customLinks.isEmpty()) {
            list.addView(sectionTitleView(text("\u30ab\u30b9\u30bf\u30e0BBS", "Custom BBS")));
            for (BbsLink link : customLinks) {
                TextView row = actionRow(link.name);
                row.setOnClickListener(v -> openBoardUrl(link.url));
                list.addView(row);
            }
        }
        addHistorySection(list, fullHistory);
        return withScrollScrubber(scroll);
    }

    private void addHistorySection(LinearLayout list, boolean fullHistory) {
        list.addView(sectionTitleView(text("\u5c65\u6b74", "History")));
        List<ThreadHistoryItem> history = threadHistory();
        int limit = fullHistory ? history.size() : Math.min(history.size(), 8);
        if (history.isEmpty()) {
            list.addView(helperLine(text("\u30b9\u30ec\u5c65\u6b74\u306a\u3057", "No thread history.")));
            return;
        }
        for (int i = 0; i < limit; i++) {
            list.addView(historyRow(history.get(i)));
        }
        if (!fullHistory && history.size() > limit) {
            TextView more = actionRow(text("\u5c65\u6b74\u3092\u3082\u3063\u3068\u898b\u308b", "More history"));
            more.setOnClickListener(v -> {
                if (pendingNewTab) {
                    showPendingNewTab(true);
                } else {
                    CuspTab tab = currentTab();
                    if (tab != null) {
                        tab.readerView = buildHistoryView();
                        contentFrame.removeAllViews();
                        contentFrame.addView(tab.readerView);
                    }
                }
            });
            list.addView(more);
        }
    }

    private View buildHistoryView() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        list.addView(sectionTitleView(text("\u5c65\u6b74", "History")));
        List<ThreadHistoryItem> history = threadHistory();
        if (history.isEmpty()) {
            list.addView(helperLine(text("\u30b9\u30ec\u5c65\u6b74\u306a\u3057", "No thread history.")));
        } else {
            for (ThreadHistoryItem item : history) {
                list.addView(historyRow(item));
            }
        }
        return withScrollScrubber(scroll);
    }

    private void showTabOverview() {
        CuspTab current = currentTab();
        if (current != null) {
            rememberThreadScroll(current);
        }
        clearAddressFocus();
        if (!replyPopups.isEmpty()) {
            dismissThreadPopups();
        }
        pendingNewTab = false;
        pendingHistoryAll = false;
        tabOverviewVisible = true;
        contentFrame.removeAllViews();
        visibleThreadPage = null;
        visibleThreadScroll = null;
        visiblePostViews.clear();
        contentFrame.addView(buildTabOverviewView());
        updateBottomThreadBar(currentTab());
        renderTabs();
    }

    private View buildTabOverviewView() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(84));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(withScrollScrubber(scroll), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        list.addView(sectionTitleView(text("\u30bf\u30d6", "Tabs")));
        if (tabs.isEmpty()) {
            list.addView(helperLine(text("\u30bf\u30d6\u306a\u3057", "No tabs.")));
        } else {
            for (int i = 0; i < tabs.size(); i++) {
                list.addView(tabOverviewRow(tabs.get(i), i));
            }
        }

        ImageButton reloadAll = iconButton(R.drawable.ic_refresh, text("\u3059\u3079\u3066\u66f4\u65b0", "Reload all"), v -> reloadAllTabs());
        reloadAll.setBackground(roundedDrawable(Color.WHITE, BORDER, dp(22)));
        FrameLayout.LayoutParams reloadParams = new FrameLayout.LayoutParams(dp(54), dp(54), Gravity.BOTTOM | Gravity.RIGHT);
        reloadParams.setMargins(0, 0, dp(84), dp(18));
        root.addView(reloadAll, reloadParams);

        ImageButton add = iconButton(R.drawable.ic_add, text("\u65b0\u898f\u30bf\u30d6", "New tab"), v -> createBlankTab());
        add.setBackground(roundedDrawable(TEAL, TEAL, dp(22)));
        add.setColorFilter(Color.WHITE);
        FrameLayout.LayoutParams addParams = new FrameLayout.LayoutParams(dp(54), dp(54), Gravity.BOTTOM | Gravity.RIGHT);
        addParams.setMargins(0, 0, dp(18), dp(18));
        root.addView(add, addParams);
        return root;
    }

    private View tabOverviewRow(CuspTab tab, int index) {
        boolean selected = !pendingNewTab && index == currentIndex;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(9), dp(8), dp(9));
        row.setBackground(roundedDrawable(Color.rgb(250, 251, 252), selected ? TEAL : Color.rgb(226, 232, 240), dp(8)));
        row.setOnClickListener(v -> switchToTab(index));

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText(tab.title == null || tab.title.trim().isEmpty() ? text("\u30bf\u30d6", "Tab") : tab.title);
        title.setTextColor(TEXT);
        title.setTextSize(15);
        title.setSingleLine(true);
        TextView url = new TextView(this);
        url.setText(tab.url == null || tab.url.trim().isEmpty() ? text("\u65b0\u898f\u30bf\u30d6", "New tab") : tab.url);
        url.setTextColor(Color.rgb(79, 91, 103));
        url.setTextSize(12);
        url.setSingleLine(true);
        textBox.addView(title);
        textBox.addView(url);
        row.addView(textBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        int unread = unreadCount(tab);
        if (unread > 0) {
            TextView unreadBadge = new TextView(this);
            unreadBadge.setText(String.valueOf(unread));
            unreadBadge.setTextColor(Color.WHITE);
            unreadBadge.setTextSize(12);
            unreadBadge.setGravity(Gravity.CENTER);
            unreadBadge.setBackground(roundedDrawable(Color.rgb(15, 118, 110), Color.rgb(15, 118, 110), dp(12)));
            LinearLayout.LayoutParams unreadParams = new LinearLayout.LayoutParams(dp(34), dp(24));
            unreadParams.setMargins(dp(8), 0, 0, 0);
            row.addView(unreadBadge, unreadParams);
        }

        ImageButton close = iconButton(R.drawable.ic_close, text("\u30bf\u30d6\u3092\u9589\u3058\u308b", "Close tab"), v -> closeTabFromOverview(index));
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(dp(42), dp(40));
        closeParams.setMargins(dp(8), 0, 0, 0);
        row.addView(close, closeParams);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(rowParams);
        return row;
    }

    private void closeTabFromOverview(int index) {
        closeTab(index);
        if (!tabs.isEmpty()) {
            tabOverviewVisible = true;
            contentFrame.removeAllViews();
            contentFrame.addView(buildTabOverviewView());
            updateBottomThreadBar(currentTab());
            renderTabs();
        }
    }

    private TextView sectionTitleView(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(TEXT);
        view.setTextSize(18);
        view.setPadding(0, dp(10), 0, dp(8));
        return view;
    }

    private TextView helperLine(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(Color.rgb(79, 91, 103));
        view.setTextSize(14);
        view.setPadding(dp(10), dp(8), dp(10), dp(10));
        return view;
    }

    private TextView actionRow(String value) {
        TextView view = helperLine(value);
        view.setTextColor(TEAL);
        view.setBackgroundColor(Color.rgb(250, 251, 252));
        return view;
    }

    private View historyRow(ThreadHistoryItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(10), dp(9), dp(10), dp(9));
        row.setBackgroundColor(Color.rgb(250, 251, 252));
        row.setOnClickListener(v -> routeLink(item.url, currentTab()));
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(rowParams);

        TextView title = new TextView(this);
        title.setText(item.title);
        title.setTextColor(TEXT);
        title.setTextSize(16);
        row.addView(title);

        TextView url = new TextView(this);
        url.setText(item.url);
        url.setTextColor(Color.rgb(79, 91, 103));
        url.setTextSize(12);
        row.addView(url);
        if (item.lastViewedAt > 0) {
            TextView viewedAt = new TextView(this);
            viewedAt.setText(text("\u6700\u7d42\u95b2\u89a7: ", "Last viewed: ") + formatHistoryTime(item.lastViewedAt));
            viewedAt.setTextColor(Color.rgb(100, 116, 139));
            viewedAt.setTextSize(12);
            row.addView(viewedAt);
        }
        return row;
    }

    static String formatHistoryTime(long time) {
        if (time <= 0) {
            return "";
        }
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        return format.format(new Date(time));
    }

    private void addBoardFolder(LinearLayout list, String folder, String[][] boards) {
        TextView header = helperLine(folder);
        header.setTextColor(TEXT);
        header.setBackgroundColor(Color.rgb(229, 233, 238));
        list.addView(header);
        for (String[] board : boards) {
            TextView row = actionRow("  " + board[0]);
            row.setOnClickListener(v -> openBoardUrl(board[1]));
            list.addView(row);
        }
    }

    private void openBoardUrl(String url) {
        if (pendingNewTab) {
            pendingNewTab = false;
            createTab(url, true, -1, true);
        } else {
            openInCurrentTab(url);
        }
    }

    private TextView postText(String value, ThreadPage page) {
        return postText(value, page, null);
    }

    private TextView postText(String value, ThreadPage page, String highlight) {
        TextView text = new TextView(this);
        SpannableString linkedText = new SpannableString(value);
        applySearchHighlights(linkedText, highlight);
        Linkify.addLinks(linkedText, Linkify.WEB_URLS);
        addLooseUrlSpans(linkedText);
        replaceUrlSpans(linkedText);
        replaceReplySpans(linkedText, page);
        text.setText(linkedText);
        text.setTextColor(TEXT);
        text.setLinkTextColor(TEAL);
        text.setTextSize(15);
        text.setLineSpacing(0, 1.15f);
        text.setTextIsSelectable(false);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        return text;
    }

    private View postContent(String value, ThreadPage page) {
        return postContent(value, page, null);
    }

    private View postContent(String value, ThreadPage page, String highlight) {
        return postContent(value, page, highlight, null);
    }

    private View postContent(String value, ThreadPage page, String highlight, Runnable longClickAction) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        TextView bodyText = postText(value, page, highlight);
        if (longClickAction != null) {
            bodyText.setOnLongClickListener(v -> {
                longClickAction.run();
                return true;
            });
            box.setOnLongClickListener(v -> {
                longClickAction.run();
                return true;
            });
        }
        box.addView(bodyText);

        Matcher matcher = URL_TEXT_PATTERN.matcher(value);
        List<String> added = new ArrayList<>();
        while (matcher.find()) {
            String rawUrl = matcher.group();
            String cleanUrl = stripTrailingUrlPunctuation(rawUrl);
            String imageUrl = imgurImageUrl(cleanUrl);
            if (imageUrl == null || added.contains(imageUrl)) {
                continue;
            }
            box.addView(imgurPreview(cleanUrl, imageUrl));
            added.add(imageUrl);
        }
        return box;
    }

    private View postBodyView(LinearLayout card, ThreadPage page, CuspTab tab, Post post) {
        Runnable longClick = () -> showPostActionMenu(card, tab, post);
        if (!post.aaMode) {
            return postContent(post.body, page, tab.threadSearchQuery, longClick);
        }
        TextView body = new TextView(this);
        body.setText(post.body);
        body.setTextColor(TEXT);
        body.setTextSize(13);
        body.setTypeface(Typeface.MONOSPACE);
        body.setIncludeFontPadding(false);
        body.setLineSpacing(0, 1.0f);
        body.setSingleLine(false);
        body.setHorizontallyScrolling(true);
        body.setPadding(0, dp(4), 0, dp(6));
        body.setOnLongClickListener(v -> {
            longClick.run();
            return true;
        });
        body.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> fitAaTextSize(body));
        body.post(() -> fitAaTextSize(body));
        return body;
    }

    private void fitAaTextSize(TextView body) {
        int available = body.getWidth() - body.getPaddingLeft() - body.getPaddingRight();
        if (available <= 0) {
            return;
        }
        String[] lines = body.getText().toString().split("\\n", -1);
        float longest = 0f;
        for (String line : lines) {
            longest = Math.max(longest, body.getPaint().measureText(line));
        }
        if (longest <= 0f) {
            return;
        }
        float size = body.getTextSize();
        float min = dp(7);
        while (longest > available && size > min) {
            size = Math.max(min, size * 0.92f);
            body.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            longest = 0f;
            for (String line : lines) {
                longest = Math.max(longest, body.getPaint().measureText(line));
            }
        }
    }

    private void toggleAaMode(CuspTab tab, Post post) {
        if (tab == null || post == null || tab.postViews == null) {
            return;
        }
        View cardView = tab.postViews.get(post.number);
        if (!(cardView instanceof LinearLayout)) {
            return;
        }
        post.aaMode = !post.aaMode;
        LinearLayout card = (LinearLayout) cardView;
        if (card.getChildCount() >= 2) {
            card.removeViewAt(1);
            card.addView(postBodyView(card, tab.threadPage, tab, post), 1);
        }
    }

    private void applySearchHighlights(SpannableString text, String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }
        String haystack = text.toString().toLowerCase(Locale.ROOT);
        String needle = query.trim().toLowerCase(Locale.ROOT);
        int index = haystack.indexOf(needle);
        while (index >= 0) {
            text.setSpan(new BackgroundColorSpan(Color.rgb(187, 247, 208)),
                    index, index + needle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            index = haystack.indexOf(needle, index + needle.length());
        }
    }

    private View imgurPreview(String originalUrl, String imageUrl) {
        FrameLayout frame = new FrameLayout(this);
        frame.setClickable(true);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(176));
        frameParams.setMargins(0, dp(6), 0, dp(6));
        frame.setLayoutParams(frameParams);

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_START);
        image.setVisibility(View.GONE);
        frame.addView(image, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(dp(36), dp(36));
        spinnerParams.gravity = Gravity.CENTER;
        frame.addView(spinner, spinnerParams);

        TextView error = new TextView(this);
        error.setText(text("imgur\u753b\u50cf\u3092\u958b\u304f", "Open imgur image"));
        error.setTextColor(TEAL);
        error.setTextSize(14);
        error.setGravity(Gravity.CENTER);
        error.setVisibility(View.GONE);
        error.setOnClickListener(v -> openExternal(originalUrl));
        frame.addView(error, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Button reveal = new Button(this);
        reveal.setText("Show");
        reveal.setAllCaps(false);
        reveal.setTextColor(TEXT);
        reveal.setVisibility(View.GONE);
        reveal.setBackground(roundedDrawable(Color.WHITE, BORDER, dp(8)));
        FrameLayout.LayoutParams revealParams = new FrameLayout.LayoutParams(dp(112), dp(44));
        revealParams.gravity = Gravity.CENTER;
        frame.addView(reveal, revealParams);

        boolean blur = blurImgurImages();
        final boolean[] started = new boolean[1];
        Runnable load = () -> ioExecutor.execute(() -> {
            ImageLoadResult loaded = downloadBitmap(imageUrl, getResources().getDisplayMetrics().widthPixels, dp(176));
            runOnUiThread(() -> {
                if (!frame.isAttachedToWindow()) {
                    return;
                }
                spinner.setVisibility(View.GONE);
                Bitmap bitmap = loaded == null ? null : loaded.bitmap;
                if (bitmap == null) {
                    error.setVisibility(View.VISIBLE);
                    return;
                }
                boolean shouldBlur = blur && !loaded.missing;
                image.setImageBitmap(shouldBlur ? blurredBitmap(bitmap) : bitmap);
                image.setVisibility(View.VISIBLE);
                if (shouldBlur) {
                    positionRevealButton(frame, reveal, bitmap);
                    reveal.setVisibility(View.VISIBLE);
                    reveal.setOnClickListener(v -> {
                        image.setImageBitmap(bitmap);
                        image.setOnClickListener(click -> showImageViewer(originalUrl, imageUrl));
                        reveal.setVisibility(View.GONE);
                    });
                } else {
                    image.setOnClickListener(v -> showImageViewer(originalUrl, imageUrl));
                }
            });
        });
        frame.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                if (!started[0]) {
                    started[0] = true;
                    load.run();
                }
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
            }
        });
        if (frame.isAttachedToWindow()) {
            started[0] = true;
            load.run();
        }
        return frame;
    }

    private void showImageViewer(String originalUrl, String imageUrl) {
        clearAddressFocus();
        FrameLayout overlay = new FrameLayout(this);
        imageOverlay = overlay;
        overlay.setBackgroundColor(Color.BLACK);
        overlay.setClickable(true);

        ZoomImageView image = new ZoomImageView(this);
        overlay.addView(image, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(dp(44), dp(44));
        spinnerParams.gravity = Gravity.CENTER;
        overlay.addView(spinner, spinnerParams);

        ImageButton close = iconButton(R.drawable.ic_close, text("\u753b\u50cf\u3092\u9589\u3058\u308b", "Close image"), v -> {
            closeImageViewer();
        });
        close.setColorFilter(Color.WHITE);
        close.setBackgroundColor(Color.argb(80, 0, 0, 0));
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(dp(48), dp(48));
        closeParams.gravity = Gravity.TOP | Gravity.RIGHT;
        closeParams.setMargins(0, dp(18), dp(14), 0);
        overlay.addView(close, closeParams);

        ImageButton open = iconButton(R.drawable.ic_arrow_forward, text("\u753b\u50cf\u30ea\u30f3\u30af\u3092\u958b\u304f", "Open image link"), v -> openExternal(originalUrl));
        open.setColorFilter(Color.WHITE);
        open.setBackgroundColor(Color.argb(80, 0, 0, 0));
        FrameLayout.LayoutParams openParams = new FrameLayout.LayoutParams(dp(48), dp(48));
        openParams.gravity = Gravity.TOP | Gravity.RIGHT;
        openParams.setMargins(0, dp(18), dp(68), 0);
        overlay.addView(open, openParams);

        addContentView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ioExecutor.execute(() -> {
            ImageLoadResult loaded = downloadBitmap(imageUrl,
                    getResources().getDisplayMetrics().widthPixels * 3,
                    getResources().getDisplayMetrics().heightPixels * 3);
            runOnUiThread(() -> {
                spinner.setVisibility(View.GONE);
                Bitmap bitmap = loaded == null ? null : loaded.bitmap;
                if (bitmap == null) {
                    Toast.makeText(this, "Image failed to load.", Toast.LENGTH_SHORT).show();
                    closeImageViewer();
                    return;
                }
                image.setImageBitmap(bitmap);
            });
        });
    }

    private void closeImageViewer() {
        if (imageOverlay == null) {
            return;
        }
        ViewGroup parent = (ViewGroup) imageOverlay.getParent();
        if (parent != null) {
            parent.removeView(imageOverlay);
        }
        imageOverlay = null;
    }

    private void positionRevealButton(FrameLayout frame, Button reveal, Bitmap bitmap) {
        frame.post(() -> {
            int frameWidth = Math.max(1, frame.getWidth());
            int frameHeight = Math.max(1, frame.getHeight());
            float scale = Math.min(frameWidth / (float) bitmap.getWidth(), frameHeight / (float) bitmap.getHeight());
            int imageWidth = Math.max(1, (int) (bitmap.getWidth() * scale));
            int imageHeight = Math.max(1, (int) (bitmap.getHeight() * scale));
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) reveal.getLayoutParams();
            params.gravity = Gravity.NO_GRAVITY;
            params.leftMargin = Math.max(0, (imageWidth - params.width) / 2);
            params.topMargin = Math.max(0, (imageHeight - params.height) / 2);
            reveal.setLayoutParams(params);
        });
    }

    private ImageLoadResult downloadBitmap(String url, int maxWidth, int maxHeight) {
        ImageLoadResult result = downloadBitmapOnce(url, maxWidth, maxHeight);
        if (result != null) {
            return result;
        }
        if (url.startsWith("https://i.imgur.com/") && url.endsWith(".jpg")) {
            String base = url.substring(0, url.length() - 4);
            result = downloadBitmapOnce(base + ".png", maxWidth, maxHeight);
            if (result != null) {
                return result;
            }
            return downloadBitmapOnce(base + ".webp", maxWidth, maxHeight);
        }
        return null;
    }

    private ImageLoadResult downloadBitmapOnce(String url, int maxWidth, int maxHeight) {
        ImageLoadResult cached = cachedBitmap(url, maxWidth, maxHeight);
        if (cached != null) {
            return cached;
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "CuspiDroid/0.1");
            int code = connection.getResponseCode();
            String contentType = connection.getContentType();
            if (code == HttpURLConnection.HTTP_NOT_FOUND || isImgurMissingResponse(connection, contentType)) {
                Bitmap missing = missingImgurBitmap(maxWidth, maxHeight);
                return missing == null ? null : new ImageLoadResult(missing, true);
            }
            try (InputStream stream = connection.getInputStream()) {
                byte[] bytes = readBytes(stream);
                if (looksLikeImgurMissing(bytes, contentType)) {
                    Bitmap missing = missingImgurBitmap(maxWidth, maxHeight);
                    return missing == null ? null : new ImageLoadResult(missing, true);
                }
                cacheImageBytes(url, bytes);
                Bitmap bitmap = decodeBitmap(bytes, maxWidth, maxHeight);
                return bitmap == null ? null : new ImageLoadResult(bitmap, false);
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private ImageLoadResult cachedBitmap(String url, int maxWidth, int maxHeight) {
        try {
            File file = imageCacheFile(url);
            if (file.exists() && file.length() > 0) {
                Bitmap bitmap = decodeBitmap(readFileBytes(file), maxWidth, maxHeight);
                return bitmap == null ? null : new ImageLoadResult(bitmap, false);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Bitmap decodeBitmap(byte[] bytes, int maxWidth, int maxHeight) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxWidth, maxHeight);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    private boolean isImgurMissingResponse(HttpURLConnection connection, String contentType) {
        try {
            String url = connection.getURL().toString().toLowerCase(Locale.ROOT);
            return url.contains("imgur.com/removed") || url.contains("i.imgur.com/removed");
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean looksLikeImgurMissing(byte[] bytes, String contentType) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        String type = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (!type.contains("text") && !type.contains("html")) {
            return false;
        }
        try {
            String text = new String(bytes, Charset.forName("UTF-8")).toLowerCase(Locale.ROOT);
            return text.contains("the image you are requesting does not exist or is no longer available")
                    || text.contains("no longer available");
        } catch (Exception ignored) {
            return false;
        }
    }

    private Bitmap missingImgurBitmap(int maxWidth, int maxHeight) {
        int width = Math.max(dp(220), Math.min(Math.max(1, maxWidth), dp(520)));
        int height = Math.max(dp(120), Math.min(Math.max(1, maxHeight), dp(176)));
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(245, 247, 250));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setStyle(android.graphics.Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(Color.rgb(203, 213, 225));
        canvas.drawRect(dp(1), dp(1), width - dp(1), height - dp(1), paint);
        paint.setStyle(android.graphics.Paint.Style.FILL);
        paint.setTextAlign(android.graphics.Paint.Align.CENTER);
        paint.setColor(Color.rgb(79, 91, 103));
        paint.setTextSize(dp(14));
        canvas.drawText(text("\u753b\u50cf\u306f\u5229\u7528\u3067\u304d\u307e\u305b\u3093", "Image not available"), width / 2f, height / 2f, paint);
        return bitmap;
    }

    private int sampleSize(int width, int height, int maxWidth, int maxHeight) {
        int sample = 1;
        if (width <= 0 || height <= 0) {
            return sample;
        }
        while (width / (sample * 2) >= maxWidth || height / (sample * 2) >= maxHeight) {
            sample *= 2;
        }
        return sample;
    }

    private byte[] readFileBytes(File file) throws Exception {
        try (InputStream stream = new java.io.FileInputStream(file)) {
            return readBytes(stream);
        }
    }

    private void cacheImageBytes(String url, byte[] bytes) {
        try {
            File dir = imageCacheDir();
            if (!dir.exists() && !dir.mkdirs()) {
                return;
            }
            try (FileOutputStream out = new FileOutputStream(imageCacheFile(url))) {
                out.write(bytes);
            }
        } catch (Exception ignored) {
        }
    }

    private File imageCacheDir() {
        return new File(getCacheDir(), "imgur");
    }

    private File imageCacheFile(String url) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(url.getBytes(Charset.forName("UTF-8")));
        StringBuilder name = new StringBuilder();
        for (byte value : hash) {
            name.append(String.format(Locale.ROOT, "%02x", value));
        }
        return new File(imageCacheDir(), name.toString() + ".img");
    }

    private Bitmap blurredBitmap(Bitmap bitmap) {
        int smallWidth = Math.max(1, bitmap.getWidth() / 22);
        int smallHeight = Math.max(1, bitmap.getHeight() / 22);
        Bitmap small = Bitmap.createScaledBitmap(bitmap, smallWidth, smallHeight, true);
        small = boxBlur(small, 2);
        return Bitmap.createScaledBitmap(small, bitmap.getWidth(), bitmap.getHeight(), true);
    }

    private Bitmap boxBlur(Bitmap source, int iterations) {
        Bitmap current = source.copy(Bitmap.Config.ARGB_8888, true);
        int width = current.getWidth();
        int height = current.getHeight();
        if (width < 3 || height < 3) {
            return current;
        }
        int[] pixels = new int[width * height];
        int[] blurred = new int[width * height];
        for (int pass = 0; pass < iterations; pass++) {
            current.getPixels(pixels, 0, width, 0, 0, width, height);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int a = 0;
                    int r = 0;
                    int g = 0;
                    int b = 0;
                    int count = 0;
                    for (int dy = -1; dy <= 1; dy++) {
                        int yy = y + dy;
                        if (yy < 0 || yy >= height) {
                            continue;
                        }
                        for (int dx = -1; dx <= 1; dx++) {
                            int xx = x + dx;
                            if (xx < 0 || xx >= width) {
                                continue;
                            }
                            int color = pixels[yy * width + xx];
                            a += Color.alpha(color);
                            r += Color.red(color);
                            g += Color.green(color);
                            b += Color.blue(color);
                            count++;
                        }
                    }
                    blurred[y * width + x] = Color.argb(a / count, r / count, g / count, b / count);
                }
            }
            current.setPixels(blurred, 0, width, 0, 0, width, height);
        }
        return current;
    }

    private String stripTrailingUrlPunctuation(String url) {
        int end = url.length();
        while (end > 0) {
            char c = url.charAt(end - 1);
            if (c == '.' || c == ',' || c == ')' || c == ']' || c == '}' || c == '>' || c == '"' || c == '\'') {
                end--;
            } else {
                break;
            }
        }
        return url.substring(0, end);
    }

    private String imgurImageUrl(String rawUrl) {
        try {
            Uri uri = Uri.parse(normalizeUrl(rawUrl));
            String host = uri.getHost();
            String path = uri.getPath();
            if (host == null || path == null) {
                return null;
            }
            host = host.toLowerCase(Locale.ROOT);
            if (!host.equals("i.imgur.com") && !host.equals("imgur.com")
                    && !host.equals("www.imgur.com") && !host.equals("m.imgur.com")) {
                return null;
            }
            if (path.contains("/a/") || path.contains("/gallery/")) {
                return null;
            }
            String file = path.substring(path.lastIndexOf('/') + 1);
            if (file.isEmpty()) {
                return null;
            }
            int dot = file.lastIndexOf('.');
            String id = dot > 0 ? file.substring(0, dot) : file;
            if (!id.matches("[A-Za-z0-9]+")) {
                return null;
            }
            if (dot > 0) {
                String ext = file.substring(dot + 1).toLowerCase(Locale.ROOT);
                if (!ext.matches("jpe?g|png|webp|gif")) {
                    return null;
                }
                return "https://i.imgur.com/" + file;
            }
            return "https://i.imgur.com/" + id + ".jpg";
        } catch (Exception ignored) {
            return null;
        }
    }

    private void replaceUrlSpans(SpannableString text) {
        URLSpan[] spans = text.getSpans(0, text.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = text.getSpanStart(span);
            int end = text.getSpanEnd(span);
            int flags = text.getSpanFlags(span);
            String url = span.getURL();
            text.removeSpan(span);
            text.setSpan(new URLSpan(url) {
                @Override
                public void onClick(View widget) {
                    routeLink(getURL(), currentTab());
                }
            }, start, end, flags);
        }
    }

    private void replaceReplySpans(SpannableString text, ThreadPage page) {
        Matcher matcher = Pattern.compile(">>\\s*(\\d{1,5})(?:\\s*[-\u2010-\u2015]\\s*(\\d{1,5}))?").matcher(text);
        while (matcher.find()) {
            int from = parsePositiveInt(matcher.group(1), -1);
            int to = matcher.group(2) == null ? from : parsePositiveInt(matcher.group(2), from);
            if (from <= 0) {
                continue;
            }
            int start = matcher.start();
            int end = matcher.end();
            text.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    showReplyPopup(widget, page, from, to);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(TEAL);
                    ds.setUnderlineText(true);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void showReplyPopup(View anchor, ThreadPage page, int from, int to) {
        if (page == null || page.postsByNumber.isEmpty()) {
            return;
        }
        int first = Math.min(from, to);
        int last = Math.max(from, to);
        List<Post> targets = new ArrayList<>();
        for (int number = first; number <= last && targets.size() < 20; number++) {
            Post post = page.postsByNumber.get(number);
            if (post != null) {
                targets.add(post);
            }
        }
        if (targets.isEmpty()) {
            Toast.makeText(this, "Referenced post not found.", Toast.LENGTH_SHORT).show();
            return;
        }
        showPostsPopup(anchor, page, targets, false);
    }

    private void showIdPopup(View anchor, ThreadPage page, String id) {
        if (page == null || id == null || id.isEmpty()) {
            return;
        }
        List<Post> targets = new ArrayList<>();
        for (Post post : page.posts) {
            if (id.equals(post.id())) {
                targets.add(post);
            }
        }
        if (targets.isEmpty()) {
            return;
        }
        showPostsPopup(anchor, page, targets, true);
    }

    private void showPostsPopup(View anchor, ThreadPage page, List<Post> targets, boolean jumpEachPost) {
        FrameLayout popupRoot = new FrameLayout(this);
        popupRoot.setBackgroundColor(Color.WHITE);
        popupRoot.setFocusable(true);
        popupRoot.setClickable(true);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), dp(8), dp(10), dp(10));
        box.setBackgroundColor(Color.WHITE);
        popupRoot.addView(box, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ScrollView popupScroll = new ScrollView(this);
        LinearLayout popupPosts = new LinearLayout(this);
        popupPosts.setOrientation(LinearLayout.VERTICAL);
        popupPosts.setPadding(0, 0, 0, 0);
        popupScroll.addView(popupPosts, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        box.addView(popupScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        ImageButton jump = iconButton(R.drawable.ic_arrow_up, targets.size() == 1
                ? "Jump to >>" + targets.get(0).number : "Jump to first", null);
        FrameLayout.LayoutParams jumpParams = new FrameLayout.LayoutParams(dp(38), dp(38));
        jumpParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        jumpParams.setMargins(0, 0, dp(6), dp(6));
        if (!jumpEachPost) {
            popupRoot.addView(jump, jumpParams);
        }

        for (Post post : targets) {
            LinearLayout metaRow = new LinearLayout(this);
            metaRow.setOrientation(LinearLayout.HORIZONTAL);
            metaRow.setGravity(Gravity.CENTER_VERTICAL);
            TextView meta = new TextView(this);
            meta.setText(">>" + post.number + "  " + post.name + "  " + post.date);
            meta.setTextColor(Color.rgb(79, 91, 103));
            meta.setTextSize(12);
            metaRow.addView(meta, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            if (jumpEachPost) {
                ImageButton postJump = iconButton(R.drawable.ic_arrow_up, "Jump to >>" + post.number, null);
                postJump.setBackgroundColor(Color.TRANSPARENT);
                postJump.setPadding(dp(8), dp(8), dp(8), dp(8));
                postJump.setOnClickListener(v -> {
                    dismissThreadPopups();
                    jumpToPost(post.number);
                });
                metaRow.addView(postJump, new LinearLayout.LayoutParams(dp(34), dp(34)));
            }
            popupPosts.addView(metaRow);

            View body = postContent(post.body, page);
            body.setPadding(0, dp(4), 0, dp(6));
            popupPosts.addView(body);
        }

        int width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(32), dp(420));
        int[] anchorLocation = new int[2];
        anchor.getLocationOnScreen(anchorLocation);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int x = Math.max(dp(8), Math.min(anchorLocation[0] + dp(8), screenWidth - width - dp(8)));
        int availableAbove = Math.max(dp(140), anchorLocation[1] - dp(16));
        int maxHeight = Math.min(getResources().getDisplayMetrics().heightPixels - dp(64), availableAbove);
        popupPosts.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int desiredHeight = popupPosts.getMeasuredHeight() + dp(18);
        int popupHeight = Math.max(dp(120), Math.min(desiredHeight, maxHeight));
        int y = Math.max(dp(8), anchorLocation[1] - popupHeight - dp(8));
        PopupWindow popup = new PopupWindow(popupRoot, width, popupHeight, false);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        popup.setElevation(dp(8));
        popup.setOnDismissListener(() -> replyPopups.remove(popup));
        if (!jumpEachPost) {
            jump.setOnClickListener(v -> {
                dismissThreadPopups();
                jumpToPost(targets.get(0).number);
            });
        }
        replyPopups.add(popup);
        popup.showAtLocation(contentFrame, Gravity.NO_GRAVITY, x, y);
    }

    private void dismissThreadPopups() {
        List<PopupWindow> popups = new ArrayList<>(replyPopups);
        replyPopups.clear();
        for (PopupWindow popup : popups) {
            popup.dismiss();
        }
    }

    private void dismissTopReplyPopup() {
        if (replyPopups.isEmpty()) {
            return;
        }
        PopupWindow popup = replyPopups.get(replyPopups.size() - 1);
        popup.dismiss();
    }

    private boolean isTouchInsideReplyPopup(MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        Rect bounds = new Rect();
        for (PopupWindow popup : replyPopups) {
            View content = popup.getContentView();
            if (content == null || !popup.isShowing()) {
                continue;
            }
            int[] location = new int[2];
            content.getLocationOnScreen(location);
            bounds.set(location[0], location[1], location[0] + content.getWidth(), location[1] + content.getHeight());
            if (bounds.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTouchInsideView(MotionEvent event, View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        Rect bounds = new Rect(
                location[0],
                location[1],
                location[0] + view.getWidth(),
                location[1] + view.getHeight());
        return bounds.contains((int) event.getRawX(), (int) event.getRawY());
    }

    private void jumpToPost(int number) {
        View target = visiblePostViews.get(number);
        if (target == null || visibleThreadScroll == null) {
            Toast.makeText(this, text("\u53c2\u7167\u5148\u304c\u8868\u793a\u3055\u308c\u3066\u3044\u307e\u305b\u3093", "Referenced post is not visible."), Toast.LENGTH_SHORT).show();
            return;
        }
        visibleThreadScroll.post(() -> {
            visibleThreadScroll.smoothScrollTo(0, Math.max(0, target.getTop() - dp(8)));
            highlightPost(target);
        });
    }

    private void highlightPost(View target) {
        clearJumpHighlight();
        highlightedPostView = target;
        target.setBackgroundColor(Color.rgb(187, 247, 208));
    }

    private void clearJumpHighlight() {
        if (highlightedPostView == null) {
            return;
        }
        CuspTab tab = currentTab();
        refreshUnreadColors(tab);
        highlightedPostView = null;
    }

    private void scrollCurrentThreadToBottom() {
        CuspTab tab = currentTab();
        ScrollView scroll = tab == null ? visibleThreadScroll : tab.threadScroll;
        if (scroll == null && tab != null) {
            scroll = findScrollView(tab.readerView);
        }
        if (scroll == null) {
            scroll = findScrollView(contentFrame);
        }
        if (scroll == null || scroll.getChildCount() == 0) {
            return;
        }
        clearAddressFocus();
        final ScrollView targetScroll = scroll;
        targetScroll.post(() -> {
            int range = Math.max(0, targetScroll.getChildAt(0).getHeight() - targetScroll.getHeight());
            targetScroll.smoothScrollTo(0, range);
        });
    }

    private ScrollView findScrollView(View view) {
        if (view instanceof ScrollView) {
            return (ScrollView) view;
        }
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            ScrollView found = findScrollView(group.getChildAt(i));
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private void showWriteDialog() {
        showWriteDialog("");
    }

    private void addLooseUrlSpans(SpannableString text) {
        Matcher matcher = URL_TEXT_PATTERN.matcher(text);
        while (matcher.find()) {
            if (text.getSpans(matcher.start(), matcher.end(), URLSpan.class).length > 0) {
                continue;
            }
            String raw = stripTrailingUrlPunctuation(matcher.group());
            int end = matcher.start() + raw.length();
            text.setSpan(new URLSpan(normalizeUrl(raw)) {
                @Override
                public void onClick(View widget) {
                    routeLink(getURL(), currentTab());
                }
            }, matcher.start(), end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void showWriteDialog(String initialMessage) {
        CuspTab tab = currentTab();
        if (tab == null || !NATIVE_THREAD.equals(tab.nativeKind) || datAddress(tab.url) == null) {
            Toast.makeText(this, text("\u3053\u3053\u304b\u3089\u306f\u66f8\u304d\u8fbc\u3081\u307e\u305b\u3093", "This thread cannot be written from here."), Toast.LENGTH_SHORT).show();
            return;
        }
        clearAddressFocus();

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(18), dp(8), dp(18), 0);

        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setHint("Name");
        form.addView(name, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));

        EditText mail = new EditText(this);
        mail.setSingleLine(true);
        mail.setHint("Mail");
        form.addView(mail, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));

        EditText message = new EditText(this);
        message.setMinLines(5);
        message.setGravity(Gravity.TOP | Gravity.START);
        message.setHint("Message");
        message.setText(initialMessage == null ? "" : initialMessage);
        message.setSelection(message.getText().length());
        form.addView(message, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(150)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(tab.threadPage == null ? text("\u66f8\u304d\u8fbc\u307f", "Write") : tab.threadPage.title)
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Post", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String body = message.getText().toString();
            if (body.trim().isEmpty()) {
                Toast.makeText(this, "Enter a message.", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            submitPost(tab, name.getText().toString(), mail.getText().toString(), body);
        }));
        dialog.show();
        message.requestFocus();
        message.postDelayed(() -> {
            try {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.showSoftInput(message, InputMethodManager.SHOW_IMPLICIT);
            } catch (Exception ignored) {
            }
        }, 120);
    }

    private void submitPost(CuspTab tab, String name, String mail, String message) {
        DatAddress address = datAddress(tab.url);
        if (address == null) {
            Toast.makeText(this, text("\u66f8\u304d\u8fbc\u307f\u5148\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093", "Cannot find thread write target."), Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, text("\u66f8\u304d\u8fbc\u307f\u4e2d", "Posting..."), Toast.LENGTH_SHORT).show();
        ioExecutor.execute(() -> {
            String result;
            boolean success = false;
            try {
                result = postToThreadWithCookieConfirm(tab.url, address, name, mail, message);
                String plain = cleanText(result);
                success = postSucceeded(plain);
                if (!success) {
                    result = shorten(plain.replace('\n', ' '), 220);
                }
            } catch (Exception error) {
                result = error.getMessage() == null ? text("\u66f8\u304d\u8fbc\u307f\u5931\u6557", "Post failed.") : error.getMessage();
            }
            String messageText = result;
            boolean posted = success;
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (posted) {
                    Toast.makeText(this, text("\u66f8\u304d\u8fbc\u307f\u5b8c\u4e86", "Posted."), Toast.LENGTH_SHORT).show();
                    refreshThreadFromBottom(tab, true);
                } else {
                    showCopyablePostFailure(messageText);
                }
            });
        });
    }

    private void showCopyablePostFailure(String messageText) {
        TextView message = new TextView(this);
        message.setText(messageText);
        message.setTextColor(TEXT);
        message.setTextSize(14);
        message.setTextIsSelectable(true);
        message.setPadding(dp(20), dp(12), dp(20), 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(text("\u66f8\u304d\u8fbc\u307f\u5931\u6557", "Post failed"))
                .setView(message)
                .setNegativeButton(text("\u30b3\u30d4\u30fc", "Copy"), (dialog, which) -> {
                    ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (manager != null) {
                        manager.setPrimaryClip(ClipData.newPlainText(text("\u66f8\u304d\u8fbc\u307f\u5931\u6557", "Post failed"), messageText));
                    }
                })
                .setPositiveButton("OK", null);
        builder.show();
    }

    private String postToThread(String threadUrl, DatAddress address, String name, String mail, String message) throws Exception {
        String endpoint = "https://" + address.server + ".5ch.net/test/bbs.cgi";
        String payload = formField("bbs", address.board)
                + "&" + formField("key", address.key)
                + "&" + formField("time", String.valueOf(System.currentTimeMillis() / 1000L))
                + "&" + formField("FROM", name)
                + "&" + formField("mail", mail)
                + "&" + formField("MESSAGE", message)
                + "&" + formField("submit", "\u66f8\u304d\u8fbc\u3080");
        byte[] body = payload.getBytes(Charset.forName("MS932"));
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(18000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", "Monazilla/1.00 CuspiDroid/0.1");
        connection.setRequestProperty("Referer", threadUrl);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", String.valueOf(body.length));
        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(body);
        }
        int code = connection.getResponseCode();
        InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String response = stream == null ? "" : readText(stream, Charset.forName("MS932"));
        connection.disconnect();
        if (code >= 400) {
            throw new IllegalStateException("HTTP " + code + "\n" + cleanText(response));
        }
        return response;
    }

    private String postToThreadWithCookieConfirm(String threadUrl, DatAddress address, String name, String mail, String message) throws Exception {
        String endpoint = postEndpoint(address);
        Map<String, String> fields = postFields(address, name, mail, message);
        String payload = postPayload(fields, "\u66f8\u304d\u8fbc\u3080");

        PostResult first = sendPostWithCookie(endpoint, threadUrl, payload, null);
        String firstPlain = cleanText(first.body);
        if (!requiresCookieConfirm(firstPlain)) {
            return postSucceeded(firstPlain) ? "write done" : first.body;
        }

        String cookie = cookieHeader(first.cookies);
        if (cookie.isEmpty()) {
            cookie = "yuki=akari";
        } else if (!cookie.contains("MonaTicket=") && !cookie.contains("yuki=")) {
            cookie = cookie + "; yuki=akari";
        }
        String confirmPayload = confirmPostPayload(first.body, fields);
        PostResult second = sendPostWithCookie(endpoint, threadUrl, confirmPayload, cookie);
        String secondPlain = cleanText(second.body);
        return postSucceeded(secondPlain) ? "write done" : second.body;
    }

    private Map<String, String> postFields(DatAddress address, String name, String mail, String message) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("bbs", address.board);
        fields.put("key", address.key);
        fields.put("time", String.valueOf(System.currentTimeMillis() / 1000L));
        fields.put("FROM", name);
        fields.put("mail", mail);
        fields.put("MESSAGE", message);
        return fields;
    }

    private String postEndpoint(DatAddress address) {
        return (address.scheme == null ? "https" : address.scheme) + "://" + address.host + "/test/bbs.cgi";
    }

    private String confirmPostPayload(String html, Map<String, String> originalFields) throws Exception {
        Map<String, String> fields = hiddenFormFields(html);
        fields.putAll(originalFields);
        return postPayload(fields, "\u4e0a\u8a18\u5168\u3066\u3092\u627f\u8afe\u3057\u3066\u66f8\u304d\u8fbc\u3080");
    }

    private Map<String, String> hiddenFormFields(String html) {
        Map<String, String> fields = new LinkedHashMap<>();
        Matcher inputMatcher = Pattern.compile("<input\\b[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(html);
        while (inputMatcher.find()) {
            String input = inputMatcher.group();
            String name = htmlAttribute(input, "name");
            if (name == null || name.isEmpty()) {
                continue;
            }
            String value = htmlAttribute(input, "value");
            fields.put(name, value == null ? "" : value);
        }
        return fields;
    }

    private String htmlAttribute(String tag, String attribute) {
        Matcher quoted = Pattern.compile(attribute + "\\s*=\\s*(['\"])(.*?)\\1", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(tag);
        if (quoted.find()) {
            return cleanText(quoted.group(2));
        }
        Matcher bare = Pattern.compile(attribute + "\\s*=\\s*([^\\s>]+)", Pattern.CASE_INSENSITIVE).matcher(tag);
        return bare.find() ? cleanText(bare.group(1)) : null;
    }

    private String postPayload(Map<String, String> fields, String submit) throws Exception {
        StringBuilder payload = new StringBuilder();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if ("submit".equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            if (payload.length() > 0) {
                payload.append('&');
            }
            payload.append(formField(entry.getKey(), entry.getValue()));
        }
        if (payload.length() > 0) {
            payload.append('&');
        }
        payload.append(formField("submit", submit));
        return payload.toString();
    }

    private PostResult sendPostWithCookie(String endpoint, String referer, String payload, String cookie) throws Exception {
        byte[] body = payload.getBytes(Charset.forName("MS932"));
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(18000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", "Monazilla/1.00 CuspiDroid/0.1");
        connection.setRequestProperty("Referer", referer);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", String.valueOf(body.length));
        applyCookies(connection, endpoint, cookie);
        try (OutputStream stream = connection.getOutputStream()) {
            stream.write(body);
        }
        int code = connection.getResponseCode();
        InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String response = stream == null ? "" : readText(stream, responseCharset(connection));
        List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
        storeCookies(endpoint, cookies);
        connection.disconnect();
        if (code >= 400) {
            throw new IllegalStateException("HTTP " + code + "\n" + cleanText(response));
        }
        PostResult result = new PostResult();
        result.body = response;
        result.cookies = cookies == null ? new ArrayList<>() : cookies;
        return result;
    }

    private String cookieHeader(List<String> cookies) {
        List<String> values = new ArrayList<>();
        for (String cookie : cookies) {
            if (cookie == null || cookie.trim().isEmpty()) {
                continue;
            }
            String value = cookie.split(";", 2)[0].trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return String.join("; ", values);
    }

    private void applyCookies(HttpURLConnection connection, String url, String extraCookie) {
        List<String> values = new ArrayList<>();
        try {
            String stored = CookieManager.getInstance().getCookie(url);
            if (stored != null && !stored.trim().isEmpty()) {
                values.add(stored);
            }
        } catch (Exception ignored) {
        }
        if (extraCookie != null && !extraCookie.trim().isEmpty()) {
            values.add(extraCookie);
        }
        if (!values.isEmpty()) {
            connection.setRequestProperty("Cookie", String.join("; ", values));
        }
    }

    private void storeCookies(String url, List<String> cookies) {
        if (cookies == null || cookies.isEmpty()) {
            return;
        }
        try {
            CookieManager manager = CookieManager.getInstance();
            manager.setAcceptCookie(true);
            for (String cookie : cookies) {
                if (cookie != null && !cookie.trim().isEmpty()) {
                    manager.setCookie(url, cookie);
                }
            }
            manager.flush();
        } catch (Exception ignored) {
        }
    }

    private Charset responseCharset(HttpURLConnection connection) {
        String contentType = connection.getContentType();
        if (contentType != null) {
            Matcher matcher = Pattern.compile("charset=([^;]+)", Pattern.CASE_INSENSITIVE).matcher(contentType);
            if (matcher.find()) {
                try {
                    return Charset.forName(matcher.group(1).trim());
                } catch (Exception ignored) {
                }
            }
        }
        return Charset.forName("MS932");
    }

    private boolean requiresCookieConfirm(String text) {
        return text.contains("\u66f8\u304d\u8fbc\u307f\u78ba\u8a8d")
                || text.contains("\u30af\u30c3\u30ad\u30fc\u78ba\u8a8d")
                || text.toLowerCase(Locale.ROOT).contains("cookie");
    }

    private boolean postSucceeded(String text) {
        return text.contains("\u66f8\u304d\u3053\u307f\u307e\u3057\u305f")
                || text.contains("\u66f8\u304d\u8fbc\u307f\u307e\u3057\u305f")
                || text.toLowerCase(Locale.ROOT).contains("write done");
    }

    private String formField(String name, String value) throws Exception {
        return URLEncoder.encode(name, "MS932") + "=" + URLEncoder.encode(value == null ? "" : value, "MS932");
    }

    private void rememberThreadScroll(CuspTab tab) {
        if (tab == null || tab.threadScroll == null || tab.threadScroll.getChildCount() == 0) {
            return;
        }
        int range = tab.threadScroll.getChildAt(0).getHeight() - tab.threadScroll.getHeight();
        tab.threadScrollRatio = range <= 0 ? 0f : Math.max(0f, Math.min(1f, tab.threadScroll.getScrollY() / (float) range));
        tab.threadBottomOffset = range <= 0 ? 0 : Math.max(0, range - tab.threadScroll.getScrollY());
    }

    private void restoreThreadScroll(CuspTab tab) {
        if (tab == null || tab.threadScroll == null) {
            return;
        }
        tab.threadScroll.post(() -> {
            if (tab.threadScroll == null || tab.threadScroll.getChildCount() == 0) {
                return;
            }
            int range = tab.threadScroll.getChildAt(0).getHeight() - tab.threadScroll.getHeight();
            if (range > 0) {
                if (tab.restoreFromBottom) {
                    tab.threadScroll.scrollTo(0, Math.max(0, range - tab.threadBottomOffset));
                    tab.restoreFromBottom = false;
                } else {
                    tab.threadScroll.scrollTo(0, (int) (range * tab.threadScrollRatio));
                }
            }
        });
    }

    private boolean routeLink(String rawUrl, CuspTab sourceTab) {
        String url = normalizeUrl(rawUrl);
        if (isThreadUrl(url)) {
            if (open5chLinksInNewTab()) {
                createTab(url, true, tabs.indexOf(sourceTab == null ? currentTab() : sourceTab));
            } else {
                CuspTab tab = sourceTab == null ? currentTab() : sourceTab;
                if (tab == null) {
                    createTab(url, true);
                } else {
                    int index = tabs.indexOf(tab);
                    if (index >= 0) {
                        switchToTab(index);
                    }
                    openInCurrentTab(url);
                }
            }
            return true;
        }

        if (is5chUrl(url)) {
            if (isFindSearchUrl(url) || isFindHomeUrl(url)) {
                CuspTab tab = sourceTab == null ? currentTab() : sourceTab;
                if (tab == null) {
                    createTab(url, true);
                } else {
                    int index = tabs.indexOf(tab);
                    if (index >= 0) {
                        switchToTab(index);
                    }
                    openInCurrentTab(url);
                }
                return true;
            }
            if (open5chLinksInNewTab() && isThreadUrl(url)) {
                createTab(url, true, tabs.indexOf(sourceTab == null ? currentTab() : sourceTab));
            } else if (isThreadUrl(url)) {
                CuspTab tab = sourceTab == null ? currentTab() : sourceTab;
                if (tab == null) {
                    createTab(url, true);
                } else {
                    int index = tabs.indexOf(tab);
                    if (index >= 0) {
                        switchToTab(index);
                    }
                    openInCurrentTab(url);
                }
            } else {
                openExternal(url);
            }
            return true;
        }

        openExternal(url);
        return true;
    }

    private boolean open5chLinksInNewTab() {
        return preferences.getBoolean(PREF_5CH_NEW_TAB, true);
    }

    private boolean blurImgurImages() {
        return preferences.getBoolean(PREF_BLUR_IMGUR, true);
    }

    private boolean addressBarOnTop() {
        return preferences.getBoolean(PREF_ADDRESS_BAR_TOP, false);
    }

    private void openExternal(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(intent);
        } catch (ActivityNotFoundException error) {
            Toast.makeText(this, text("\u958b\u3051\u308b\u30a2\u30d7\u30ea\u304c\u3042\u308a\u307e\u305b\u3093", "No app can open this link."), Toast.LENGTH_SHORT).show();
        }
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void openCurrentThreadInWebView() {
        CuspTab tab = currentTab();
        if (tab == null || tab.url == null || tab.url.trim().isEmpty()) {
            Toast.makeText(this, text("\u958b\u304fURL\u304c\u3042\u308a\u307e\u305b\u3093", "No thread URL to open."), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, AuthActivity.class);
        intent.putExtra(AuthActivity.EXTRA_URL, tab.url);
        startActivity(intent);
    }

    private void showThreadSearchDialog() {
        CuspTab tab = currentTab();
        if (tab == null || tab.threadPage == null || tab.threadPage.posts.isEmpty()) {
            Toast.makeText(this, text("\u691c\u7d22\u3067\u304d\u308b\u30b9\u30ec\u304c\u3042\u308a\u307e\u305b\u3093", "No searchable thread."), Toast.LENGTH_SHORT).show();
            return;
        }
        tab.threadSearchOpen = true;
        updateThreadSearchBar(tab);
        threadSearchInput.requestFocus();
        threadSearchInput.selectAll();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(threadSearchInput, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void updateThreadSearch(String query, boolean resetIndex) {
        CuspTab tab = currentTab();
        if (tab == null || tab.threadPage == null) {
            return;
        }
        tab.threadSearchQuery = query == null ? "" : query;
        tab.threadSearchMatches.clear();
        String needle = tab.threadSearchQuery.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            tab.threadSearchIndex = -1;
            tab.threadSearchLastQuery = "";
            tab.threadSearchLastCandidates = new ArrayList<>();
            scheduleThreadHighlightRender(tab);
            updateThreadSearchCount(tab);
            return;
        }
        List<Post> candidates = tab.threadSearchLastQuery != null
                && !tab.threadSearchLastQuery.isEmpty()
                && needle.startsWith(tab.threadSearchLastQuery)
                ? tab.threadSearchLastCandidates : tab.threadPage.posts;
        tab.threadSearchLastCandidates = new ArrayList<>();
        for (Post post : candidates) {
            String haystack = post.searchBody();
            if (haystack.contains(needle)) {
                tab.threadSearchMatches.add(post.number);
                tab.threadSearchLastCandidates.add(post);
            }
        }
        tab.threadSearchLastQuery = needle;
        if (tab.threadSearchMatches.isEmpty()) {
            tab.threadSearchIndex = -1;
        } else if (resetIndex || tab.threadSearchIndex < 0 || tab.threadSearchIndex >= tab.threadSearchMatches.size()) {
            tab.threadSearchIndex = 0;
        }
        scheduleThreadHighlightRender(tab);
        updateThreadSearchCount(tab);
        if (resetIndex && tab.threadSearchIndex >= 0) {
            jumpToPost(tab.threadSearchMatches.get(tab.threadSearchIndex));
        }
    }

    private void moveThreadSearch(int direction) {
        CuspTab tab = currentTab();
        if (tab == null || tab.threadSearchMatches.isEmpty()) {
            return;
        }
        int size = tab.threadSearchMatches.size();
        tab.threadSearchIndex = (tab.threadSearchIndex + direction + size) % size;
        updateThreadSearchCount(tab);
        jumpToPost(tab.threadSearchMatches.get(tab.threadSearchIndex));
    }

    private void closeThreadSearch() {
        CuspTab tab = currentTab();
        if (tab != null) {
            tab.threadSearchOpen = false;
            tab.threadSearchQuery = "";
            tab.threadSearchMatches.clear();
            tab.threadSearchLastQuery = "";
            tab.threadSearchLastCandidates = new ArrayList<>();
            tab.threadSearchGeneration++;
            tab.threadSearchIndex = -1;
            if (threadSearchHighlightTask != null) {
                mainHandler.removeCallbacks(threadSearchHighlightTask);
                threadSearchHighlightTask = null;
            }
            rerenderThreadHighlights(tab);
        }
        if (threadSearchInput != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(threadSearchInput.getWindowToken(), 0);
            }
            threadSearchInput.clearFocus();
        }
        if (threadSearchBar != null) {
            threadSearchBar.setVisibility(View.GONE);
        }
    }

    private void updateThreadSearchBar(CuspTab tab) {
        if (threadSearchBar == null || threadSearchInput == null || threadSearchCount == null) {
            return;
        }
        boolean show = tab != null && tab.threadSearchOpen && NATIVE_THREAD.equals(tab.nativeKind);
        threadSearchBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) {
            return;
        }
        String query = tab.threadSearchQuery == null ? "" : tab.threadSearchQuery;
        if (!query.contentEquals(threadSearchInput.getText())) {
            updatingThreadSearchInput = true;
            threadSearchInput.setText(query);
            threadSearchInput.setSelection(threadSearchInput.getText().length());
            updatingThreadSearchInput = false;
        }
        updateThreadSearchCount(tab);
    }

    private void updateThreadSearchCount(CuspTab tab) {
        if (threadSearchCount == null || tab == null) {
            return;
        }
        int total = tab.threadSearchMatches.size();
        if (tab.threadSearchQuery == null || tab.threadSearchQuery.trim().isEmpty()) {
            threadSearchCount.setText("");
        } else if (total == 0) {
            threadSearchCount.setText("0/0");
        } else {
            threadSearchCount.setText((tab.threadSearchIndex + 1) + "/" + total);
        }
    }

    private void rerenderThreadHighlights(CuspTab tab) {
        if (tab == null || tab.threadPage == null || tab.postViews == null) {
            return;
        }
        for (Post post : tab.threadPage.posts) {
            View cardView = tab.postViews.get(post.number);
            if (!(cardView instanceof LinearLayout)) {
                continue;
            }
            LinearLayout card = (LinearLayout) cardView;
            if (card.getChildCount() < 2) {
                continue;
            }
            card.removeViewAt(1);
            View bodyView = postBodyView(card, tab.threadPage, tab, post);
            card.addView(bodyView, 1);
            View parent = (View) card.getParent();
            if (parent instanceof ViewGroup && ((ViewGroup) parent).getChildCount() >= 3) {
                ViewGroup shell = (ViewGroup) parent;
                attachPostSwipeDeep(bodyView, card, shell.getChildAt(0), shell.getChildAt(1), tab, post);
            }
        }
    }

    private void scheduleThreadHighlightRender(CuspTab tab) {
        if (threadSearchHighlightTask != null) {
            mainHandler.removeCallbacks(threadSearchHighlightTask);
        }
        int generation = ++tab.threadSearchGeneration;
        threadSearchHighlightTask = () -> {
            if (tab == currentTab() && tab.threadSearchGeneration == generation) {
                rerenderThreadHighlights(tab);
            }
        };
        mainHandler.postDelayed(threadSearchHighlightTask, 180);
    }

    private void searchNextThread() {
        CuspTab tab = currentTab();
        if (tab == null || tab.threadPage == null || tab.threadPage.title == null) {
            Toast.makeText(this, text("\u691c\u7d22\u3067\u304d\u308b\u30b9\u30ec\u540d\u304c\u3042\u308a\u307e\u305b\u3093", "No thread title to search."), Toast.LENGTH_SHORT).show();
            return;
        }
        openInCurrentTab(searchUrl(nextThreadQuery(tab.threadPage.title)));
    }

    private String nextThreadQuery(String title) {
        return title.replaceAll("(?i)\\s*(part|vol\\.?|#)?\\s*[0-9\uff10-\uff19]+\\s*$", "").trim();
    }

    private void shareCurrentThread() {
        CuspTab tab = currentTab();
        if (tab == null || tab.url == null || tab.url.trim().isEmpty()) {
            Toast.makeText(this, text("\u5171\u6709\u3059\u308bURL\u304c\u3042\u308a\u307e\u305b\u3093", "No thread URL to share."), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, tab.url);
        startActivity(Intent.createChooser(intent, text("\u30b9\u30ec\u3092\u5171\u6709", "Share thread")));
    }

    private void goBack() {
        CuspTab tab = currentTab();
        if (tab == null || tab.navigationIndex <= 0 || tab.navigationIndex > tab.navigationHistory.size() - 1) {
            clearAddressFocus();
            if (tab != null && tab.backToNewTab) {
                closeCurrentTab();
                showPendingNewTab();
            }
            return;
        }
        clearAddressFocus();
        tab.navigationIndex--;
        openInCurrentTab(tab.navigationHistory.get(tab.navigationIndex), false);
    }

    private void goForward() {
        CuspTab tab = currentTab();
        if (tab == null || tab.navigationIndex < 0 || tab.navigationIndex >= tab.navigationHistory.size() - 1) {
            clearAddressFocus();
            return;
        }
        clearAddressFocus();
        tab.navigationIndex++;
        openInCurrentTab(tab.navigationHistory.get(tab.navigationIndex), false);
    }

    private void reload() {
        CuspTab tab = currentTab();
        if (tab == null) {
            return;
        }
        if (tab.readerMode && NATIVE_THREAD.equals(tab.nativeKind)) {
            clearAddressFocus();
            loadThread(tab, tab.url);
        } else if (tab.readerMode && NATIVE_SEARCH.equals(tab.nativeKind)) {
            clearAddressFocus();
            loadSearchResults(tab, tab.url);
        } else if (tab.readerMode && NATIVE_BOARD.equals(tab.nativeKind)) {
            clearAddressFocus();
            loadBoard(tab, tab.url);
        } else if (tab.readerMode && NATIVE_SEARCH_HOME.equals(tab.nativeKind)) {
            clearAddressFocus();
            loadSearchHome(tab, tab.url);
        }
    }

    private void reloadFromMenu() {
        CuspTab tab = currentTab();
        if (tab == null) {
            return;
        }
        if (tab.readerMode && NATIVE_THREAD.equals(tab.nativeKind)) {
            clearAddressFocus();
            refreshThreadFromBottom(tab, false, true);
        } else {
            reload();
        }
    }

    private void reloadAllTabs() {
        boolean wasOverview = tabOverviewVisible;
        for (CuspTab tab : new ArrayList<>(tabs)) {
            if (tab == null || tab.url == null || tab.url.isEmpty()) {
                continue;
            }
            if (tab.readerMode && NATIVE_THREAD.equals(tab.nativeKind)) {
                refreshThreadFromBottom(tab, false, true);
            } else if (tab.readerMode && NATIVE_SEARCH.equals(tab.nativeKind)) {
                loadSearchResults(tab, tab.url, false);
            } else if (tab.readerMode && NATIVE_BOARD.equals(tab.nativeKind)) {
                loadBoard(tab, tab.url, false);
            } else if (tab.readerMode && NATIVE_SEARCH_HOME.equals(tab.nativeKind)) {
                loadSearchHome(tab, tab.url, false);
            }
        }
        if (wasOverview) {
            tabOverviewVisible = true;
            mainHandler.postDelayed(() -> {
                if (tabOverviewVisible) {
                    contentFrame.removeAllViews();
                    contentFrame.addView(buildTabOverviewView());
                    renderTabs();
                }
            }, 500);
        }
    }

    private void closeCurrentTab() {
        if (tabs.isEmpty()) {
            return;
        }
        closeTab(currentIndex);
    }

    private void closeTab(int index) {
        if (tabs.isEmpty() || index < 0 || index >= tabs.size()) {
            return;
        }
        CuspTab closing = tabs.get(index);
        int returnToIndex = closing.returnToIndex;
        tabs.remove(index);
        for (CuspTab tab : tabs) {
            if (tab.returnToIndex == index) {
                tab.returnToIndex = -1;
            } else if (tab.returnToIndex > index) {
                tab.returnToIndex--;
            }
        }
        if (tabs.isEmpty()) {
            createBlankTab();
            return;
        }
        if (index == currentIndex && returnToIndex >= 0) {
            if (returnToIndex > index) {
                returnToIndex--;
            }
            currentIndex = Math.max(0, Math.min(returnToIndex, tabs.size() - 1));
        } else if (index < currentIndex) {
            currentIndex--;
        } else if (index == currentIndex) {
            currentIndex = Math.max(0, Math.min(index, tabs.size() - 1));
        } else {
            currentIndex = Math.max(0, Math.min(currentIndex, tabs.size() - 1));
        }
        switchToTab(currentIndex);
    }

    private CuspTab currentTab() {
        if (pendingNewTab) {
            return null;
        }
        if (currentIndex < 0 || currentIndex >= tabs.size()) {
            return null;
        }
        return tabs.get(currentIndex);
    }

    private String download(String urlText) throws Exception {
        HttpURLConnection connection = openConnectionFollowingRedirects(urlText, "Mozilla/5.0 (Linux; Android) CuspiDroid/0.1");
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
        String body = readText(stream, charset);
        if (code >= 400) {
            throw new IllegalStateException("HTTP " + code + "\n" + stripTags(body));
        }
        return body;
    }

    private HttpURLConnection openConnectionFollowingRedirects(String urlText, String userAgent) throws Exception {
        String current = urlText;
        for (int i = 0; i < 8; i++) {
            HttpURLConnection connection = (HttpURLConnection) new URL(current).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(16000);
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setRequestProperty("Accept", "*/*");
            applyCookies(connection, current, null);
            int code = connection.getResponseCode();
            storeCookies(current, connection.getHeaderFields().get("Set-Cookie"));
            if (code == HttpURLConnection.HTTP_MOVED_PERM
                    || code == HttpURLConnection.HTTP_MOVED_TEMP
                    || code == HttpURLConnection.HTTP_SEE_OTHER
                    || code == 307
                    || code == 308) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();
                if (location == null || location.trim().isEmpty()) {
                    throw new IllegalStateException("Redirect without Location");
                }
                current = new URL(new URL(current), location).toString();
                continue;
            }
            return connection;
        }
        throw new IllegalStateException("Too many redirects");
    }

    private String readText(InputStream stream, Charset charset) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append('\n');
        }
        reader.close();
        return builder.toString();
    }

    private byte[] readBytes(InputStream stream) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        stream.close();
        return out.toByteArray();
    }

    private ThreadPage downloadDatThread(String threadUrl) throws Exception {
        DatAddress address = datAddress(threadUrl);
        if (address == null) {
            HttpURLConnection canonical = openConnectionFollowingRedirects(
                    threadUrl,
                    "Mozilla/5.0 (Linux; Android) CuspiDroid/0.1");
            String canonicalUrl = canonical.getURL().toString();
            canonical.disconnect();
            address = datAddress(canonicalUrl);
        }
        if (address == null) {
            return null;
        }
        List<String> candidates = datCandidates(address);
        for (String candidate : candidates) {
            try {
                HttpURLConnection connection = openConnectionFollowingRedirects(
                        candidate,
                        "Monazilla/1.00 CuspiDroid/0.1");
                int code = connection.getResponseCode();
                InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
                if (stream == null) {
                    throw new IllegalStateException("HTTP " + code);
                }
                byte[] bytes = readBytes(stream);
                String body = new String(bytes, Charset.forName("MS932"));
                if (code >= 400) {
                    throw new IllegalStateException("DAT HTTP " + code + "\n" + body.trim());
                }
                return parseDatThread(threadUrl, body);
            } catch (Exception error) {
            }
        }
        return null;
    }

    private List<String> datCandidates(DatAddress address) {
        List<String> candidates = new ArrayList<>();
        if (address.host != null && !address.host.isEmpty()) {
            String base = (address.scheme == null ? "https" : address.scheme) + "://" + address.host + "/" + address.board;
            candidates.add(base + "/dat/" + address.key + ".dat");
            if (address.key.length() >= 4) {
                String bucket = address.key.substring(0, 4);
                candidates.add(base + "/kako/" + bucket + "/" + address.key + ".dat");
            }
            if (address.key.length() >= 5) {
                String bucket4 = address.key.substring(0, 4);
                String bucket5 = address.key.substring(0, 5);
                candidates.add(base + "/kako/" + bucket4 + "/" + bucket5 + "/" + address.key + ".dat");
            }
        }
        candidates.add("https://" + address.server + ".5ch.io/" + address.board + "/dat/" + address.key + ".dat");
        candidates.add("https://" + address.server + ".5ch.net/" + address.board + "/dat/" + address.key + ".dat");
        if (address.key.length() >= 4) {
            String bucket = address.key.substring(0, 4);
            candidates.add("https://" + address.server + ".5ch.io/" + address.board + "/oyster/" + bucket + "/" + address.key + ".dat");
            candidates.add("https://" + address.server + ".5ch.net/" + address.board + "/oyster/" + bucket + "/" + address.key + ".dat");
            candidates.add("https://" + address.server + ".5ch.io/" + address.board + "/kako/" + bucket + "/" + address.key + ".dat");
            candidates.add("https://" + address.server + ".5ch.net/" + address.board + "/kako/" + bucket + "/" + address.key + ".dat");
        }
        if (address.key.length() >= 5) {
            String bucket4 = address.key.substring(0, 4);
            String bucket5 = address.key.substring(0, 5);
            candidates.add("https://" + address.server + ".5ch.io/" + address.board + "/kako/" + bucket4 + "/" + bucket5 + "/" + address.key + ".dat");
            candidates.add("https://" + address.server + ".5ch.net/" + address.board + "/kako/" + bucket4 + "/" + bucket5 + "/" + address.key + ".dat");
        }
        return candidates;
    }

    private DatAddress datAddress(String threadUrl) {
        Uri uri = Uri.parse(threadUrl);
        String host = uri.getHost();
        if (host == null) {
            return null;
        }
        String[] segments = uri.getPath() == null ? new String[0] : uri.getPath().split("/");
        List<String> parts = new ArrayList<>();
        for (String segment : segments) {
            if (!segment.isEmpty()) {
                parts.add(segment);
            }
        }

        int testIndex = parts.indexOf("test");
        if (testIndex < 0 || testIndex + 3 >= parts.size() || !"read.cgi".equals(parts.get(testIndex + 1))) {
            return null;
        }

        String board = parts.get(testIndex + 2);
        String key = parts.get(testIndex + 3);
        String server = host.split("\\.")[0];
        if ("itest".equals(server) && testIndex > 0) {
            server = parts.get(testIndex - 1);
        }
        if ("itest".equals(server) || server.trim().isEmpty()) {
            return null;
        }

        DatAddress address = new DatAddress();
        address.scheme = uri.getScheme() == null ? "https" : uri.getScheme();
        address.host = host;
        address.server = server;
        address.board = board;
        address.key = key;
        return address;
    }

    private ThreadPage parseDatThread(String threadUrl, String dat) {
        ThreadPage page = new ThreadPage();
        page.url = threadUrl;
        page.title = hostTitle(threadUrl);
        String[] lines = dat.split("\\r?\\n");
        int number = 1;
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] fields = line.split("<>", -1);
            if (fields.length < 4) {
                continue;
            }
            Post post = new Post();
            post.number = number;
            post.name = cleanText(fields[0]);
            post.date = cleanText(fields[2]);
            post.body = cleanText(fields[3]);
            page.posts.add(post);
            page.postsByNumber.put(post.number, post);
            if (number == 1 && fields.length >= 5 && !cleanText(fields[4]).isEmpty()) {
                page.title = cleanText(fields[4]);
            }
            number++;
        }
        return page;
    }

    private SearchPage downloadBoard(String boardUrl) throws Exception {
        Uri uri = Uri.parse(boardUrl);
        String host = uri.getHost();
        String board = boardNameFromUrl(boardUrl);
        if (host == null || board == null) {
            throw new IllegalStateException("Unsupported board URL.");
        }
        String subjectUrl = "https://" + host + "/" + board + "/subject.txt";
        HttpURLConnection connection = openConnectionFollowingRedirects(
                subjectUrl,
                "Monazilla/1.00 CuspiDroid/0.1");
        int code = connection.getResponseCode();
        InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (stream == null) {
            throw new IllegalStateException("HTTP " + code);
        }
        String body = readText(stream, Charset.forName("MS932"));
        connection.disconnect();
        if (code >= 400) {
            throw new IllegalStateException("HTTP " + code + "\n" + cleanText(body));
        }

        SearchPage page = new SearchPage();
        page.url = boardUrl;
        page.title = boardTitle(boardUrl);
        for (String line : body.split("\\r?\\n")) {
            int sep = line.indexOf("<>");
            if (sep <= 0) {
                continue;
            }
            String dat = line.substring(0, sep);
            String title = cleanText(line.substring(sep + 2));
            if (!dat.endsWith(".dat")) {
                continue;
            }
            String key = dat.substring(0, dat.length() - 4);
            SearchResult result = new SearchResult();
            result.title = title;
            result.url = "https://" + host + "/test/read.cgi/" + board + "/" + key + "/";
            result.meta = board;
            page.results.add(result);
        }
        return page;
    }

    private ThreadPage parseThread(String url, String html) {
        ThreadPage page = new ThreadPage();
        page.url = url;
        page.title = firstMatch(html, "<title[^>]*>(.*?)</title>");
        if (page.title == null || page.title.trim().isEmpty()) {
            page.title = hostTitle(url);
        }
        page.title = cleanText(page.title);

        parseModernPosts(html, page.posts);
        if (page.posts.isEmpty()) {
            parseClassicPosts(html, page.posts);
        }
        indexPosts(page);
        return page;
    }

    private void indexPosts(ThreadPage page) {
        page.postsByNumber.clear();
        for (Post post : page.posts) {
            page.postsByNumber.put(post.number, post);
        }
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
            post.number = parsePositiveInt(valueOr(firstMatch(block, "class=[\"'][^\"']*(?:number|no)[^\"']*[\"'][^>]*>(.*?)<"), String.valueOf(fallbackNumber)), fallbackNumber);
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
            post.number = parsePositiveInt(valueOr(firstMatch(meta, "^(\\s*\\d+)"), String.valueOf(posts.size() + 1)).trim(), posts.size() + 1);
            post.name = valueOr(firstMatch(meta, "<b[^>]*>(.*?)</b>"), "anonymous");
            post.date = cleanText(stripTags(meta)).replace(String.valueOf(post.number), "").replace(post.name, "").trim();
            post.body = cleanText(body);
            posts.add(post);
        }
    }

    private SearchPage parseSearchPage(String url, String html) {
        SearchPage page = new SearchPage();
        page.url = url;
        page.title = searchTitle(url);
        Pattern rowPattern = Pattern.compile(
                "<div[^>]+class=[\"'][^\"']*(?<![A-Za-z0-9_-])list_line(?![A-Za-z0-9_-])[^\"']*[\"'][^>]*>(.*?)(?=<div[^>]+class=[\"'][^\"']*(?<![A-Za-z0-9_-])list_line(?![A-Za-z0-9_-])[^\"']*[\"']|</div>\\s*</div>\\s*<div[^>]+class=[\"'][^\"']*col-lg-5|<script|</body>)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher rowMatcher = rowPattern.matcher(html);
        while (rowMatcher.find()) {
            String block = rowMatcher.group(1);
            String href = firstMatch(block, "<a[^>]+class=[\"'][^\"']*list_line_link[^\"']*[\"'][^>]+href=[\"']([^\"']+)[\"']");
            String title = firstMatch(block, "<div[^>]+class=[\"'][^\"']*list_line_link_title[^\"']*[\"'][^>]*>(.*?)</div>");
            if (href == null || title == null) {
                continue;
            }
            SearchResult result = new SearchResult();
            result.url = absolutizeFindUrl(href);
            result.title = cleanText(title);
            result.meta = cleanSearchMeta(block);
            page.results.add(result);
        }
        return page;
    }

    private String cleanSearchMeta(String block) {
        StringBuilder meta = new StringBuilder();
        Pattern infoPattern = Pattern.compile(
                "<div[^>]+class=[\"'][^\"']*list_line_info_container[^\"']*[\"'][^>]*>(.*?)</div>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = infoPattern.matcher(block);
        while (matcher.find()) {
            String value = cleanText(matcher.group(1));
            if (!value.isEmpty()) {
                if (meta.length() > 0) {
                    meta.append("  ");
                }
                meta.append(value);
            }
        }
        return meta.toString();
    }

    private String absolutizeFindUrl(String href) {
        if (href.startsWith("//")) {
            return "https:" + href;
        }
        if (href.startsWith("http://") || href.startsWith("https://")) {
            return href;
        }
        if (href.startsWith("/")) {
            return "https://find.5ch.io" + href;
        }
        return "https://find.5ch.io/" + href;
    }

    private int parsePositiveInt(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        Matcher matcher = Pattern.compile("\\d+").matcher(value);
        if (!matcher.find()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(matcher.group());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException error) {
            return fallback;
        }
    }

    private String firstMatch(String text, String regex) {
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String cleanText(String html) {
        String marker = "\uE000";
        String normalized = html
                .replaceAll("(?i)<br\\s*/?>", marker)
                .replaceAll("(?i)</p\\s*>", marker)
                .replaceAll("(?i)</div\\s*>", marker);
        Spanned spanned;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(normalized, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(normalized);
        }
        return spanned.toString()
                .replace(marker, "\n")
                .replace('\u00a0', ' ')
                .replaceAll("[ \\t]+\\n", "\n")
                .replaceAll("\\n[ \\t]+", "\n")
                .trim();
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

    private void addThreadHistory(String url, String title) {
        if (url == null || url.trim().isEmpty() || title == null || title.trim().isEmpty()) {
            return;
        }
        List<ThreadHistoryItem> history = threadHistory();
        for (int i = history.size() - 1; i >= 0; i--) {
            if (url.equals(history.get(i).url)) {
                history.remove(i);
            }
        }
        history.add(0, new ThreadHistoryItem(title, url, System.currentTimeMillis()));
        while (history.size() > 100) {
            history.remove(history.size() - 1);
        }
        JSONArray array = new JSONArray();
        try {
            for (ThreadHistoryItem item : history) {
                JSONObject object = new JSONObject();
                object.put("title", item.title);
                object.put("url", item.url);
                object.put("lastViewedAt", item.lastViewedAt);
                array.put(object);
            }
        } catch (Exception ignored) {
        }
        preferences.edit().putString(PREF_HISTORY, array.toString()).apply();
    }

    static List<ThreadHistoryItem> readThreadHistory(SharedPreferences preferences) {
        List<ThreadHistoryItem> history = readThreadItems(preferences, PREF_HISTORY);
        Collections.sort(history, (left, right) -> Long.compare(right.lastViewedAt, left.lastViewedAt));
        return history;
    }

    private static List<ThreadHistoryItem> readThreadItems(SharedPreferences preferences, String key) {
        List<ThreadHistoryItem> history = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString(key, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                String title = object.optString("title", "").trim();
                String url = object.optString("url", "").trim();
                long fallbackViewedAt = System.currentTimeMillis() - i;
                long lastViewedAt = object.optLong("lastViewedAt", fallbackViewedAt);
                if (!title.isEmpty() && !url.isEmpty()) {
                    history.add(new ThreadHistoryItem(title, url, lastViewedAt));
                }
            }
        } catch (Exception ignored) {
        }
        return history;
    }

    private List<ThreadHistoryItem> threadHistory() {
        return readThreadHistory(preferences);
    }

    private static int readPostNumber(SharedPreferences preferences, String url) {
        if (url == null || url.isEmpty()) {
            return 0;
        }
        try {
            JSONObject object = new JSONObject(preferences.getString(PREF_READ_POSTS, "{}"));
            return object.optInt(url, 0);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static void saveReadPostNumber(SharedPreferences preferences, String url, int number) {
        if (url == null || url.isEmpty()) {
            return;
        }
        try {
            JSONObject object = new JSONObject(preferences.getString(PREF_READ_POSTS, "{}"));
            object.put(url, Math.max(0, number));
            preferences.edit().putString(PREF_READ_POSTS, object.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private void markReadTo(CuspTab tab, int number) {
        markReadTo(tab, number, true);
    }

    private void markReadTo(CuspTab tab, int number, boolean refreshOverview) {
        if (tab == null || tab.threadPage == null || tab.threadPage.url == null) {
            return;
        }
        tab.readPostNumber = Math.max(tab.readPostNumber, number);
        saveReadPostNumber(preferences, tab.threadPage.url, tab.readPostNumber);
        refreshUnreadColors(tab);
        if (refreshOverview) {
            renderTabs();
            if (tabOverviewVisible) {
                contentFrame.removeAllViews();
                contentFrame.addView(buildTabOverviewView());
            }
        }
    }

    private int unreadCount(CuspTab tab) {
        if (tab == null || tab.threadPage == null) {
            return 0;
        }
        int count = 0;
        for (Post post : tab.threadPage.posts) {
            if (post.number > tab.readPostNumber) {
                count++;
            }
        }
        return count;
    }

    private int maxPostNumber(ThreadPage page) {
        int max = 0;
        if (page != null) {
            for (Post post : page.posts) {
                max = Math.max(max, post.number);
            }
        }
        return max;
    }

    private void refreshUnreadColors(CuspTab tab) {
        if (tab == null || tab.threadPage == null || tab.postViews == null) {
            return;
        }
        for (Post post : tab.threadPage.posts) {
            View card = tab.postViews.get(post.number);
            if (card != null) {
                card.setBackgroundColor(post.number > tab.readPostNumber
                        ? Color.rgb(232, 247, 244) : Color.rgb(250, 251, 252));
            }
        }
    }

    static void clearThreadHistory(SharedPreferences preferences) {
        preferences.edit().remove(PREF_HISTORY).apply();
    }

    static void removeThreadHistory(SharedPreferences preferences, String url) {
        List<ThreadHistoryItem> history = readThreadHistory(preferences);
        JSONArray array = new JSONArray();
        try {
            for (ThreadHistoryItem item : history) {
                if (item.url.equals(url)) {
                    continue;
                }
                JSONObject object = new JSONObject();
                object.put("title", item.title);
                object.put("url", item.url);
                object.put("lastViewedAt", item.lastViewedAt);
                array.put(object);
            }
        } catch (Exception ignored) {
        }
        preferences.edit().putString(PREF_HISTORY, array.toString()).apply();
    }

    static List<BbsLink> readBbsLinks(SharedPreferences preferences) {
        List<BbsLink> links = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString(PREF_BBS_LINKS, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                String name = item.optString("name", "").trim();
                String url = item.optString("url", "").trim();
                if (!name.isEmpty() && !url.isEmpty()) {
                    links.add(new BbsLink(name, url));
                }
            }
        } catch (Exception ignored) {
        }
        return links;
    }

    static void addBbsLink(SharedPreferences preferences, String name, String url) {
        List<BbsLink> links = readBbsLinks(preferences);
        JSONArray array = new JSONArray();
        try {
            String normalized = normalizeUrlStatic(url);
            for (BbsLink link : links) {
                if (!normalized.equals(link.url)) {
                    JSONObject item = new JSONObject();
                    item.put("name", link.name);
                    item.put("url", link.url);
                    array.put(item);
                }
            }
            JSONObject added = new JSONObject();
            added.put("name", name.trim());
            added.put("url", normalized);
            array.put(added);
        } catch (Exception ignored) {
        }
        preferences.edit().putString(PREF_BBS_LINKS, array.toString()).apply();
    }

    static void removeBbsLink(SharedPreferences preferences, String url) {
        List<BbsLink> links = readBbsLinks(preferences);
        JSONArray array = new JSONArray();
        try {
            for (BbsLink link : links) {
                if (!url.equals(link.url)) {
                    JSONObject item = new JSONObject();
                    item.put("name", link.name);
                    item.put("url", link.url);
                    array.put(item);
                }
            }
        } catch (Exception ignored) {
        }
        preferences.edit().putString(PREF_BBS_LINKS, array.toString()).apply();
    }

    private boolean isThreadUrl(String url) {
        String lower = url.toLowerCase(Locale.ROOT);
        return lower.contains(".5ch.net/test/read.cgi/")
                || lower.contains(".5ch.io/test/read.cgi/")
                || lower.contains(".5ch.io/") && lower.contains("/test/read.cgi/")
                || lower.contains(".2ch.sc/test/read.cgi/")
                || lower.contains("/test/read.cgi/");
    }

    private boolean isFindSearchUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            String path = uri.getPath();
            if (host == null || path == null) {
                return false;
            }
            String lowerHost = host.toLowerCase(Locale.ROOT);
            return (lowerHost.equals("find.5ch.io") || lowerHost.equals("find.5ch.net"))
                    && path.equals("/search");
        } catch (Exception error) {
            return false;
        }
    }

    private boolean isFindHomeUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            String path = uri.getPath();
            if (host == null) {
                return false;
            }
            String lowerHost = host.toLowerCase(Locale.ROOT);
            boolean findHost = lowerHost.equals("find.5ch.io") || lowerHost.equals("find.5ch.net");
            return findHost && (path == null || path.isEmpty() || path.equals("/"));
        } catch (Exception error) {
            return false;
        }
    }

    private boolean isBoardUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            return host != null && (is5chUrl(url) || isRegisteredBbsUrl(url)) && boardNameFromUrl(url) != null;
        } catch (Exception error) {
            return false;
        }
    }

    private boolean isRegisteredBbsUrl(String url) {
        try {
            String normalized = normalizeUrl(url);
            Uri target = Uri.parse(normalized);
            String targetHost = target.getHost();
            if (targetHost == null) {
                return false;
            }
            for (BbsLink link : readBbsLinks(preferences)) {
                Uri base = Uri.parse(link.url);
                String baseHost = base.getHost();
                if (baseHost != null && targetHost.equalsIgnoreCase(baseHost)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private String boardNameFromUrl(String url) {
        Uri uri = Uri.parse(url);
        String path = uri.getPath();
        if (path == null) {
            return null;
        }
        String[] parts = path.split("/");
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if ("test".equals(part) || "read.cgi".equals(part) || "dat".equals(part)) {
                return null;
            }
            return part;
        }
        return null;
    }

    private String boardTitle(String url) {
        String board = boardNameFromUrl(url);
        return board == null ? hostTitle(url) : board;
    }

    private boolean is5chUrl(String url) {
        try {
            String host = Uri.parse(url).getHost();
            if (host == null) {
                return false;
            }
            String lower = host.toLowerCase(Locale.ROOT);
            return lower.equals("5ch.net")
                    || lower.equals("5ch.io")
                    || lower.endsWith(".5ch.net")
                    || lower.endsWith(".5ch.io")
                    || lower.equals("bbspink.com")
                    || lower.endsWith(".bbspink.com");
        } catch (Exception error) {
            return false;
        }
    }

    private boolean looksLikeUrl(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://")
                || lower.startsWith("https://")
                || lower.startsWith("ttp://")
                || lower.startsWith("ttps://")
                || lower.contains(".5ch.net/")
                || lower.contains(".io/")
                || lower.matches("^[a-z0-9.-]+\\.[a-z]{2,}(/.*)?$");
    }

    private String normalizeUrl(String input) {
        return normalizeUrlStatic(input);
    }

    private static String normalizeUrlStatic(String input) {
        if (input == null || input.trim().isEmpty()) {
            return HOME_URL;
        }
        String value = input.trim();
        if (value.startsWith("//")) {
            return "https:" + value;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.startsWith("ttps://")) {
            return "h" + value;
        }
        if (lower.startsWith("ttp://")) {
            return "h" + value;
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        return "https://" + value;
    }

    private String searchUrl(String query) {
        try {
            String encoded = URLEncoder.encode(query, "UTF-8");
            String template = preferences.getString(PREF_SEARCH_TEMPLATE, DEFAULT_SEARCH_TEMPLATE);
            if (template == null || template.trim().isEmpty()) {
                template = DEFAULT_SEARCH_TEMPLATE;
            }
            if (LEGACY_FIND_IO_TEMPLATE.equals(template)) {
                template = DEFAULT_SEARCH_TEMPLATE;
            }
            if (template.contains("%s")) {
                return template.replace("%s", encoded);
            }
            String separator = template.contains("?") ? "&" : "?";
            return template + separator + "q=" + encoded;
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

    private String searchTitle(String url) {
        String query = "";
        try {
            Uri uri = Uri.parse(url);
            query = uri.getQueryParameter("q");
            if (query == null) {
                query = uri.getQueryParameter("STR");
            }
            if (query == null) {
                Matcher matcher = Pattern.compile("[?&](?:q|STR)=([^&]+)").matcher(url);
                if (matcher.find()) {
                    query = URLDecoder.decode(matcher.group(1), "UTF-8");
                }
            }
        } catch (Exception ignored) {
            query = "";
        }
        if (query == null || query.trim().isEmpty()) {
            return text("5ch\u691c\u7d22", "5ch Search");
        }
        return text("\u691c\u7d22: ", "Search: ") + query.trim();
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
            Toast.makeText(this, text("\u958b\u3044\u3066\u3044\u307e\u3059", "Opening..."), Toast.LENGTH_SHORT).show();
        }
    }

    private void showKeyboard() {
        try {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.showSoftInput(addressBar, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception ignored) {
            Toast.makeText(this, text("\u691c\u7d22\u3067\u304d\u307e\u3059", "Ready to search."), Toast.LENGTH_SHORT).show();
        }
    }

    private void startAddressEntry() {
        addressBar.setText("");
        addressBar.requestFocus();
        addressBar.post(() -> {
            addressBar.requestFocus();
            showKeyboard();
        });
    }

    private void clearAddressFocus() {
        addressBar.clearFocus();
        addressBar.setSelection(addressBar.getText().length());
        hideKeyboard();
        View current = getCurrentFocus();
        if (current == null || current == addressBar) {
            contentFrame.requestFocus();
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class ZoomImageView extends ImageView {
        private final Matrix matrix = new Matrix();
        private final ScaleGestureDetector scaleDetector;
        private float scale = 1f;
        private float minScale = 1f;
        private float lastX;
        private float lastY;
        private boolean dragging;

        ZoomImageView(Context context) {
            super(context);
            setScaleType(ScaleType.MATRIX);
            setBackgroundColor(Color.BLACK);
            scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    float next = Math.max(minScale, Math.min(5f, scale * detector.getScaleFactor()));
                    float factor = next / scale;
                    scale = next;
                    matrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
                    constrain();
                    setImageMatrix(matrix);
                    return true;
                }
            });
        }

        @Override
        public void setImageBitmap(Bitmap bitmap) {
            super.setImageBitmap(bitmap);
            post(this::fitImage);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            scaleDetector.onTouchEvent(event);
            if (event.getPointerCount() > 1) {
                dragging = false;
                return true;
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = event.getX();
                    lastY = event.getY();
                    dragging = true;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (dragging && scale > minScale) {
                        float dx = event.getX() - lastX;
                        float dy = event.getY() - lastY;
                        matrix.postTranslate(dx, dy);
                        constrain();
                        setImageMatrix(matrix);
                    }
                    lastX = event.getX();
                    lastY = event.getY();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dragging = false;
                    return true;
                default:
                    return true;
            }
        }

        private void fitImage() {
            if (getDrawable() == null || getWidth() <= 0 || getHeight() <= 0) {
                return;
            }
            int imageWidth = getDrawable().getIntrinsicWidth();
            int imageHeight = getDrawable().getIntrinsicHeight();
            if (imageWidth <= 0 || imageHeight <= 0) {
                return;
            }
            float fit = Math.min((float) getWidth() / imageWidth, (float) getHeight() / imageHeight);
            minScale = fit;
            scale = fit;
            matrix.reset();
            matrix.postScale(fit, fit);
            matrix.postTranslate((getWidth() - imageWidth * fit) / 2f, (getHeight() - imageHeight * fit) / 2f);
            setImageMatrix(matrix);
        }

        private void constrain() {
            if (getDrawable() == null) {
                return;
            }
            android.graphics.RectF rect = new android.graphics.RectF(
                    0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
            matrix.mapRect(rect);
            float dx = 0;
            float dy = 0;
            if (rect.width() <= getWidth()) {
                dx = (getWidth() - rect.width()) / 2f - rect.left;
            } else if (rect.left > 0) {
                dx = -rect.left;
            } else if (rect.right < getWidth()) {
                dx = getWidth() - rect.right;
            }
            if (rect.height() <= getHeight()) {
                dy = (getHeight() - rect.height()) / 2f - rect.top;
            } else if (rect.top > 0) {
                dy = -rect.top;
            } else if (rect.bottom < getHeight()) {
                dy = getHeight() - rect.bottom;
            }
            matrix.postTranslate(dx, dy);
        }
    }

    private static class CuspTab {
        String title;
        String url;
        View readerView;
        ThreadPage threadPage;
        SearchPage searchPage;
        ScrollView threadScroll;
        LinearLayout threadList;
        View threadBottomLoader;
        Map<Integer, View> postViews;
        String nativeKind;
        float threadScrollRatio;
        int threadBottomOffset;
        int readPostNumber;
        boolean restoreFromBottom;
        boolean threadSearchOpen;
        String threadSearchQuery = "";
        List<Integer> threadSearchMatches = new ArrayList<>();
        String threadSearchLastQuery = "";
        List<Post> threadSearchLastCandidates = new ArrayList<>();
        int threadSearchGeneration;
        int threadSearchIndex = -1;
        int returnToIndex = -1;
        boolean backToNewTab;
        boolean readerMode;
        List<String> navigationHistory = new ArrayList<>();
        int navigationIndex = -1;
    }

    static class ThreadHistoryItem {
        final String title;
        final String url;
        final long lastViewedAt;

        ThreadHistoryItem(String title, String url) {
            this(title, url, 0);
        }

        ThreadHistoryItem(String title, String url, long lastViewedAt) {
            this.title = title;
            this.url = url;
            this.lastViewedAt = lastViewedAt;
        }
    }

    static class BbsLink {
        final String name;
        final String url;

        BbsLink(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    private static class ThreadPage {
        String url;
        String title;
        String error;
        List<Post> posts = new ArrayList<>();
        Map<Integer, Post> postsByNumber = new LinkedHashMap<>();

        static ThreadPage error(String url, String message) {
            ThreadPage page = new ThreadPage();
            page.url = url;
            page.title = text("\u8aad\u307f\u8fbc\u307f\u5931\u6557", "Load failed");
            page.error = message == null ? "Unknown error" : message;
            return page;
        }
    }

    private static class SearchPage {
        String url;
        String title;
        String error;
        List<SearchResult> results = new ArrayList<>();

        static SearchPage error(String url, String message) {
            SearchPage page = new SearchPage();
            page.url = url;
            page.title = text("\u691c\u7d22\u5931\u6557", "Search failed");
            page.error = message == null ? "Unknown error" : message;
            return page;
        }
    }

    private static class SearchResult {
        String title;
        String url;
        String meta;
    }

    private static class PostResult {
        String body;
        List<String> cookies = new ArrayList<>();
    }

    private static class ImageLoadResult {
        final Bitmap bitmap;
        final boolean missing;

        ImageLoadResult(Bitmap bitmap, boolean missing) {
            this.bitmap = bitmap;
            this.missing = missing;
        }
    }

    private static class DatAddress {
        String scheme;
        String host;
        String server;
        String board;
        String key;
    }

    private static class Post {
        int number;
        String name;
        String date;
        String body;
        String cachedSearchBody;
        boolean aaMode;

        String searchBody() {
            if (cachedSearchBody == null) {
                cachedSearchBody = body == null ? "" : body.toLowerCase(Locale.ROOT);
            }
            return cachedSearchBody;
        }

        String id() {
            Matcher matcher = POST_ID_PATTERN.matcher(date == null ? "" : date);
            return matcher.find() ? matcher.group(1) : "";
        }
    }
}
