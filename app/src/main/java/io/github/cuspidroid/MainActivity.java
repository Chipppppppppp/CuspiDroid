package io.github.cuspidroid;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Rect;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
    private static final String PREF_TABS = "saved_tabs";
    static final String PREF_HISTORY = "thread_history";
    static final String DEFAULT_SEARCH_TEMPLATE = "https://find.5ch.io/search?q=%s";
    static final String LEGACY_FIND_IO_TEMPLATE = "https://find.5ch.io/search?STR=%s&TYPE=TITLE&BBS=ALL";
    static final String FIND_NET_TEMPLATE = "https://find.5ch.net/search?STR=%s&TYPE=TITLE&BBS=ALL";
    private static final String NATIVE_THREAD = "thread";
    private static final String NATIVE_SEARCH = "search";
    private static final String NATIVE_SEARCH_HOME = "search_home";
    private static final int TEAL = Color.rgb(15, 118, 110);
    private static final int SURFACE = Color.rgb(247, 248, 250);
    private static final int BORDER = Color.rgb(215, 221, 226);
    private static final int TEXT = Color.rgb(31, 41, 55);

    private final List<CuspTab> tabs = new ArrayList<>();
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private LinearLayout tabStrip;
    private LinearLayout suggestionsPanel;
    private FrameLayout overlayFrame;
    private EditText addressBar;
    private FrameLayout contentFrame;
    private ProgressBar progressBar;
    private SharedPreferences preferences;
    private final List<View> toolbarButtons = new ArrayList<>();
    private ThreadPage visibleThreadPage;
    private ScrollView visibleThreadScroll;
    private final Map<Integer, View> visiblePostViews = new LinkedHashMap<>();
    private final List<PopupWindow> replyPopups = new ArrayList<>();
    private int currentIndex = -1;

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
    protected void onDestroy() {
        ioExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!replyPopups.isEmpty()) {
            dismissThreadPopups();
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
            if (!replyPopups.isEmpty() && !isTouchInsideReplyPopup(event)) {
                dismissThreadPopups();
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
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setFocusableInTouchMode(true);
        root.requestFocus();
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

        addToolbarButton(toolbar, R.drawable.ic_arrow_back, "Back", v -> goBack());
        addToolbarButton(toolbar, R.drawable.ic_arrow_forward, "Forward", v -> goForward());
        addToolbarButton(toolbar, R.drawable.ic_refresh, "Reload", v -> reload());

        addressBar = new EditText(this);
        addressBar.setSingleLine(true);
        addressBar.setTextSize(15);
        addressBar.setTextColor(TEXT);
        addressBar.setHint("Search 5ch or enter URL");
        addressBar.setSelectAllOnFocus(true);
        addressBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        addressBar.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI);
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
            showAddressEditMenu();
            return true;
        });
        toolbar.addView(addressBar, new LinearLayout.LayoutParams(0, dp(40), 1));

        addToolbarButton(toolbar, R.drawable.ic_add, "New tab", v -> createBlankTab());
        addToolbarButton(toolbar, R.drawable.ic_settings, "Settings", v -> openSettings());

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
        suggestionsPanel.setBackground(suggestionsBackground());
        suggestionsPanel.setVisibility(View.GONE);
        FrameLayout.LayoutParams suggestionsParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        suggestionsParams.gravity = Gravity.TOP;
        suggestionsParams.leftMargin = dp(8);
        suggestionsParams.rightMargin = dp(8);
        overlayFrame.addView(suggestionsPanel, suggestionsParams);
        root.addView(overlayFrame, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(38), dp(40));
        params.setMargins(dp(2), 0, dp(2), 0);
        button.setLayoutParams(params);
        return button;
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

        String query = addressBar.getText().toString().trim().toLowerCase(Locale.ROOT);
        String clipboardLink = query.isEmpty() ? clipboardLink() : null;
        if (clipboardLink != null) {
            TextView item = suggestionItem("Paste link from clipboard", clipboardLink);
            item.setOnClickListener(v -> {
                addressBar.setText(clipboardLink);
                addressBar.selectAll();
                updateSuggestions();
            });
            if (suggestionsPanel.getChildCount() > 0) {
                suggestionsPanel.addView(suggestionDivider());
            }
            suggestionsPanel.addView(item);
        }

        if (!query.isEmpty()) {
            int count = 0;
            for (ThreadHistoryItem history : threadHistory()) {
                if (history.title.toLowerCase(Locale.ROOT).contains(query)) {
                    TextView item = suggestionItem("Thread history", history.title);
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
        suggestionsPanel.setVisibility(suggestionsPanel.getChildCount() == 0 ? View.GONE : View.VISIBLE);
    }

    private TextView suggestionItem(String label, String value) {
        TextView view = new TextView(this);
        view.setText(label + "\n" + value);
        view.setTextColor(TEXT);
        view.setTextSize(14);
        view.setBackgroundColor(Color.WHITE);
        view.setPadding(dp(14), dp(10), dp(14), dp(10));
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

        menu.addView(menuItem("Copy", v -> {
            copyAddressText();
            popup.dismiss();
        }));
        menu.addView(verticalDivider());
        menu.addView(menuItem("Paste", v -> {
            pasteIntoAddressBar(false);
            popup.dismiss();
        }));
        menu.addView(verticalDivider());
        menu.addView(menuItem("Paste and go", v -> {
            pasteIntoAddressBar(true);
            popup.dismiss();
        }));
        popup.showAsDropDown(addressBar, dp(8), dp(2));
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

    private View verticalDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(BORDER);
        divider.setLayoutParams(new LinearLayout.LayoutParams(dp(1), ViewGroup.LayoutParams.MATCH_PARENT));
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
            });
        }
    }

    private GradientDrawable iconButtonBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.TRANSPARENT);
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private GradientDrawable addressBarBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.rgb(241, 245, 249));
        drawable.setStroke(dp(1), BORDER);
        drawable.setCornerRadius(dp(20));
        return drawable;
    }

    private GradientDrawable suggestionsBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.rgb(255, 255, 255));
        drawable.setStroke(dp(2), Color.rgb(148, 163, 184));
        drawable.setCornerRadius(dp(12));
        return drawable;
    }

    private GradientDrawable menuBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setStroke(dp(2), Color.rgb(148, 163, 184));
        drawable.setCornerRadius(dp(10));
        return drawable;
    }

    private void createTab(String url, boolean select) {
        createTab(url, select, -1);
    }

    private void createTab(String url, boolean select, int returnToIndex) {
        CuspTab tab = new CuspTab();
        tab.title = "New tab";
        tab.url = normalizeUrl(url);
        tab.returnToIndex = returnToIndex;
        tabs.add(tab);
        if (select) {
            switchToTab(tabs.size() - 1);
            openInCurrentTab(tab.url);
        }
        renderTabs();
    }

    private void createBlankTab() {
        CuspTab tab = new CuspTab();
        tab.title = "New tab";
        tab.url = "";
        tab.returnToIndex = -1;
        tab.readerMode = true;
        tab.nativeKind = NATIVE_SEARCH_HOME;
        tab.readerView = buildSearchHomeView();
        tabs.add(tab);
        switchToTab(tabs.size() - 1);
        startAddressEntry();
        renderTabs();
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
                tab.title = item.optString("title", "New tab");
                tab.url = url;
                String nativeKind = item.optString("nativeKind", "");
                tab.nativeKind = nativeKind.isEmpty() || "null".equals(nativeKind) ? null : nativeKind;
                tab.threadScrollRatio = (float) item.optDouble("threadScrollRatio", 0);
                tab.threadBottomOffset = item.optInt("threadBottomOffset", 0);
                if (NATIVE_THREAD.equals(tab.nativeKind)) {
                    tab.threadPage = threadPageFromJson(item.optJSONObject("threadPage"));
                    if (tab.threadPage != null && !tab.threadPage.posts.isEmpty()) {
                        tab.readerMode = true;
                        tab.postViews = new LinkedHashMap<>();
                        tab.readerView = buildThreadView(tab.threadPage, tab);
                    }
                } else if (NATIVE_SEARCH_HOME.equals(tab.nativeKind) || url.isEmpty()) {
                    tab.readerMode = true;
                    tab.readerView = buildSearchHomeView();
                }
                tab.returnToIndex = -1;
                tabs.add(tab);
            }
            switchToTab(selected);
            CuspTab tab = currentTab();
            if (tab != null) {
                if (NATIVE_THREAD.equals(tab.nativeKind) && tab.threadPage != null && !tab.threadPage.posts.isEmpty()) {
                    restoreThreadScroll(tab);
                } else if (tab.url == null || tab.url.isEmpty()) {
                    tab.readerMode = true;
                    tab.nativeKind = NATIVE_SEARCH_HOME;
                    tab.readerView = buildSearchHomeView();
                    switchToTab(selected);
                    startAddressEntry();
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
                if (NATIVE_THREAD.equals(tab.nativeKind) && tab.threadPage != null && tab.threadPage.error == null) {
                    item.put("threadPage", threadPageToJson(tab.threadPage));
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

    private void switchToTab(int index) {
        if (index < 0 || index >= tabs.size()) {
            return;
        }
        CuspTab previous = currentTab();
        if (previous != null) {
            rememberThreadScroll(previous);
        }
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
        renderTabs();
    }

    private void renderTabs() {
        if (tabStrip == null) {
            return;
        }
        tabStrip.removeAllViews();
        for (int i = 0; i < tabs.size(); i++) {
            CuspTab tab = tabs.get(i);
            LinearLayout tabBox = new LinearLayout(this);
            tabBox.setOrientation(LinearLayout.HORIZONTAL);
            tabBox.setGravity(Gravity.CENTER_VERTICAL);
            tabBox.setBackgroundColor(i == currentIndex ? TEAL : Color.rgb(229, 233, 238));
            tabBox.setPadding(dp(8), 0, dp(4), 0);
            int target = i;
            tabBox.setOnClickListener(v -> switchToTab(target));

            TextView tabView = new TextView(this);
            tabView.setText(shorten(tab.title == null ? "Tab" : tab.title, 22));
            tabView.setGravity(Gravity.CENTER_VERTICAL);
            tabView.setTextSize(13);
            tabView.setSingleLine(true);
            tabView.setTextColor(i == currentIndex ? Color.WHITE : TEXT);
            tabView.setOnClickListener(v -> switchToTab(target));
            tabBox.addView(tabView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

            ImageButton close = new ImageButton(this);
            close.setImageResource(R.drawable.ic_close);
            close.setContentDescription("Close tab");
            close.setColorFilter(i == currentIndex ? Color.WHITE : TEXT);
            close.setBackgroundColor(Color.TRANSPARENT);
            close.setPadding(dp(7), dp(7), dp(7), dp(7));
            close.setOnClickListener(v -> closeTab(target));
            tabBox.addView(close, new LinearLayout.LayoutParams(dp(30), dp(34)));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(166), dp(36));
            params.setMargins(dp(3), 0, dp(3), 0);
            tabStrip.addView(tabBox, params);
        }

        ImageButton add = new ImageButton(this);
        add.setImageResource(R.drawable.ic_add);
        add.setContentDescription("New tab");
        add.setColorFilter(TEXT);
        add.setBackgroundColor(Color.TRANSPARENT);
        add.setPadding(dp(8), dp(8), dp(8), dp(8));
        add.setOnClickListener(v -> createBlankTab());
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(dp(40), dp(36));
        addParams.setMargins(dp(3), 0, dp(8), 0);
        tabStrip.addView(add, addParams);
    }

    private void openFromAddressBar() {
        String input = addressBar.getText().toString().trim();
        if (input.isEmpty()) {
            return;
        }
        clearAddressFocus();
        boolean urlLike = looksLikeUrl(input);
        String url = urlLike ? normalizeUrl(input) : searchUrl(input);
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
        if (isFindSearchUrl(url)) {
            loadSearchResults(tab, url);
            return;
        }
        if (isFindHomeUrl(url)) {
            loadSearchHome(tab, url);
            return;
        }
        openExternal(url);
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
                tab.postViews = new LinkedHashMap<>();
                tab.readerView = buildThreadView(result, tab);
                if (result.error == null && !result.posts.isEmpty()) {
                    addThreadHistory(result.url, result.title);
                }
                progressBar.setVisibility(View.GONE);
                if (tab == currentTab()) {
                    switchToTab(currentIndex);
                    restoreThreadScroll(tab);
                }
                renderTabs();
            });
        });
    }

    private void loadSearchResults(CuspTab tab, String url) {
        tab.readerMode = true;
        tab.nativeKind = NATIVE_SEARCH;
        tab.url = url;
        tab.title = searchTitle(url);
        tab.readerView = loadingView("Searching...");
        tab.threadPage = null;
        tab.threadScroll = null;
        tab.postViews = null;
        switchToTab(tabs.indexOf(tab));
        progressBar.setVisibility(View.VISIBLE);

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
                tab.readerView = buildSearchView(result);
                progressBar.setVisibility(View.GONE);
                if (tab == currentTab()) {
                    switchToTab(currentIndex);
                }
                renderTabs();
            });
        });
    }

    private void loadSearchHome(CuspTab tab, String url) {
        tab.readerMode = true;
        tab.nativeKind = NATIVE_SEARCH_HOME;
        tab.url = url;
        tab.title = "5ch Search";
        tab.threadPage = null;
        tab.threadScroll = null;
        tab.postViews = null;
        tab.readerView = buildSearchHomeView();
        switchToTab(tabs.indexOf(tab));
        renderTabs();
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

            TextView body = postText(post.body, page);
            card.addView(body);
            list.addView(card, cardParams);
            tab.postViews.put(post.number, card);
        }

        if (page.posts.isEmpty()) {
            list.addView(postText("No posts were parsed. Use R to reload, or open another URL.", page));
        }
        ProgressBar bottomLoader = new ProgressBar(this);
        bottomLoader.setIndeterminate(true);
        bottomLoader.setVisibility(View.GONE);
        LinearLayout.LayoutParams loaderParams = new LinearLayout.LayoutParams(dp(42), dp(42));
        loaderParams.gravity = Gravity.CENTER_HORIZONTAL;
        loaderParams.setMargins(0, dp(4), 0, dp(8));
        list.addView(bottomLoader, loaderParams);

        enableBottomPullRefresh(scroll, list, bottomLoader, () -> {
            rememberThreadScroll(tab);
            tab.restoreFromBottom = true;
            loadThread(tab, tab.url, false);
        });
        return withScrollScrubber(scroll);
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
            list.addView(postText("No search results.", null));
        }
        return withScrollScrubber(scroll);
    }

    private View withScrollScrubber(ScrollView scroll) {
        FrameLayout frame = new FrameLayout(this);
        frame.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View rail = new View(this);
        rail.setBackgroundColor(Color.argb(28, 31, 41, 55));
        FrameLayout.LayoutParams railParams = new FrameLayout.LayoutParams(dp(34), ViewGroup.LayoutParams.MATCH_PARENT);
        railParams.gravity = Gravity.RIGHT;
        frame.addView(rail, railParams);

        View thumb = new View(this);
        GradientDrawable thumbBackground = new GradientDrawable();
        thumbBackground.setColor(Color.argb(170, 15, 118, 110));
        thumbBackground.setCornerRadius(dp(8));
        thumb.setBackground(thumbBackground);
        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(dp(16), dp(56));
        thumbParams.gravity = Gravity.RIGHT;
        thumbParams.rightMargin = dp(9);
        frame.addView(thumb, thumbParams);

        Runnable updateThumb = () -> {
            int range = scroll.getChildCount() == 0 ? 0 : scroll.getChildAt(0).getHeight() - scroll.getHeight();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) thumb.getLayoutParams();
            if (range <= 0) {
                thumb.setVisibility(View.GONE);
                return;
            }
            thumb.setVisibility(View.VISIBLE);
            int frameHeight = Math.max(1, frame.getHeight());
            int thumbHeight = Math.max(dp(42), frameHeight * scroll.getHeight() / Math.max(scroll.getChildAt(0).getHeight(), 1));
            int maxTop = Math.max(0, frameHeight - thumbHeight);
            params.height = thumbHeight;
            params.topMargin = maxTop * scroll.getScrollY() / range;
            thumb.setLayoutParams(params);
        };

        scroll.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> updateThumb.run());
        frame.post(updateThumb);
        rail.setOnTouchListener(scrubberTouchListener(scroll, frame, thumb));
        thumb.setOnTouchListener(scrubberTouchListener(scroll, frame, thumb));
        return frame;
    }

    private View.OnTouchListener scrubberTouchListener(ScrollView scroll, View frame, View thumb) {
        return (view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN
                    || event.getAction() == MotionEvent.ACTION_MOVE
                    || event.getAction() == MotionEvent.ACTION_UP) {
                return handleScrubberDrag(event, scroll, frame, thumb);
            }
            return false;
        };
    }

    private boolean handleScrubberDrag(MotionEvent event, ScrollView scroll, View frame, View thumb) {
        if (event.getAction() != MotionEvent.ACTION_DOWN
                && event.getAction() != MotionEvent.ACTION_MOVE
                && event.getAction() != MotionEvent.ACTION_UP) {
            return false;
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

    private void enableBottomPullRefresh(ScrollView scroll, View content, View loader, Runnable refresh) {
        final float[] downY = new float[1];
        final boolean[] startedAtBottom = new boolean[1];
        final boolean[] triggered = new boolean[1];
        scroll.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downY[0] = event.getY();
                startedAtBottom[0] = !scroll.canScrollVertically(1);
                triggered[0] = false;
                loader.setVisibility(View.GONE);
                content.setTranslationY(0);
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (startedAtBottom[0]) {
                    float pull = Math.max(0, downY[0] - event.getY());
                    if (pull > dp(8)) {
                        loader.setVisibility(View.VISIBLE);
                        content.setTranslationY(-Math.min(dp(72), pull * 0.45f));
                    }
                    if (!triggered[0] && pull > dp(116)) {
                        triggered[0] = true;
                        loader.setVisibility(View.VISIBLE);
                        content.setTranslationY(-dp(72));
                        refresh.run();
                        return true;
                    }
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (!triggered[0]) {
                    content.animate().translationY(0).setDuration(140).start();
                    loader.setVisibility(View.GONE);
                }
            }
            return false;
        });
    }

    private View buildSearchHomeView() {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        for (ThreadHistoryItem item : threadHistory()) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(dp(10), dp(9), dp(10), dp(9));
            row.setBackgroundColor(Color.rgb(250, 251, 252));
            row.setOnClickListener(v -> routeLink(item.url, currentTab()));
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, dp(8));

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
            list.addView(row, rowParams);
        }
        return withScrollScrubber(scroll);
    }

    private TextView postText(String value, ThreadPage page) {
        TextView text = new TextView(this);
        SpannableString linkedText = new SpannableString(value);
        Linkify.addLinks(linkedText, Linkify.WEB_URLS);
        replaceUrlSpans(linkedText);
        replaceReplySpans(linkedText, page);
        text.setText(linkedText);
        text.setTextColor(TEXT);
        text.setLinkTextColor(TEAL);
        text.setTextSize(15);
        text.setLineSpacing(0, 1.15f);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        return text;
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

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), dp(8), dp(10), dp(10));
        box.setBackgroundColor(Color.WHITE);
        box.setFocusable(true);
        box.setClickable(true);

        Button jump = new Button(this);
        jump.setText(targets.size() == 1 ? "Jump to >>" + targets.get(0).number : "Jump to first");
        jump.setAllCaps(false);
        box.addView(jump, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(40)));

        ScrollView popupScroll = new ScrollView(this);
        LinearLayout popupPosts = new LinearLayout(this);
        popupPosts.setOrientation(LinearLayout.VERTICAL);
        popupScroll.addView(popupPosts, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        box.addView(popupScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        for (Post post : targets) {
            TextView meta = new TextView(this);
            meta.setText(">>" + post.number + "  " + post.name + "  " + post.date);
            meta.setTextColor(Color.rgb(79, 91, 103));
            meta.setTextSize(12);
            popupPosts.addView(meta);

            TextView body = postText(post.body, page);
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
        int desiredHeight = popupPosts.getMeasuredHeight() + dp(40) + dp(18);
        int popupHeight = Math.max(dp(120), Math.min(desiredHeight, maxHeight));
        int y = Math.max(dp(8), anchorLocation[1] - popupHeight - dp(8));
        PopupWindow popup = new PopupWindow(box, width, popupHeight, false);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        popup.setElevation(dp(8));
        popup.setOnDismissListener(() -> replyPopups.remove(popup));
        jump.setOnClickListener(v -> {
            dismissThreadPopups();
            jumpToPost(targets.get(0).number);
        });
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
            Toast.makeText(this, "Referenced post is not visible.", Toast.LENGTH_SHORT).show();
            return;
        }
        visibleThreadScroll.post(() -> visibleThreadScroll.smoothScrollTo(0, Math.max(0, target.getTop() - dp(8))));
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
                    loadThread(tab, url);
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

    private void openExternal(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(intent);
        } catch (ActivityNotFoundException error) {
            Toast.makeText(this, "No app can open this link.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void goBack() {
    }

    private void goForward() {
    }

    private void reload() {
        CuspTab tab = currentTab();
        if (tab == null) {
            return;
        }
        if (tab.readerMode && NATIVE_THREAD.equals(tab.nativeKind)) {
            loadThread(tab, tab.url);
        } else if (tab.readerMode && NATIVE_SEARCH.equals(tab.nativeKind)) {
            loadSearchResults(tab, tab.url);
        } else if (tab.readerMode && NATIVE_SEARCH_HOME.equals(tab.nativeKind)) {
            loadSearchHome(tab, tab.url);
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
            int code = connection.getResponseCode();
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
        Exception lastError = null;
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
                lastError = error;
            }
        }
        if (lastError != null) {
            throw lastError;
        }
        return null;
    }

    private List<String> datCandidates(DatAddress address) {
        List<String> candidates = new ArrayList<>();
        candidates.add("https://" + address.server + ".5ch.io/" + address.board + "/dat/" + address.key + ".dat");
        candidates.add("https://" + address.server + ".5ch.net/" + address.board + "/dat/" + address.key + ".dat");
        if (address.key.length() >= 4) {
            String bucket = address.key.substring(0, 4);
            candidates.add("https://" + address.server + ".5ch.io/" + address.board + "/oyster/" + bucket + "/" + address.key + ".dat");
            candidates.add("https://" + address.server + ".5ch.net/" + address.board + "/oyster/" + bucket + "/" + address.key + ".dat");
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
        history.add(0, new ThreadHistoryItem(title, url));
        while (history.size() > 100) {
            history.remove(history.size() - 1);
        }
        JSONArray array = new JSONArray();
        try {
            for (ThreadHistoryItem item : history) {
                JSONObject object = new JSONObject();
                object.put("title", item.title);
                object.put("url", item.url);
                array.put(object);
            }
        } catch (Exception ignored) {
        }
        preferences.edit().putString(PREF_HISTORY, array.toString()).apply();
    }

    static List<ThreadHistoryItem> readThreadHistory(SharedPreferences preferences) {
        List<ThreadHistoryItem> history = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString(PREF_HISTORY, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                String title = object.optString("title", "").trim();
                String url = object.optString("url", "").trim();
                if (!title.isEmpty() && !url.isEmpty()) {
                    history.add(new ThreadHistoryItem(title, url));
                }
            }
        } catch (Exception ignored) {
        }
        return history;
    }

    private List<ThreadHistoryItem> threadHistory() {
        return readThreadHistory(preferences);
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
                array.put(object);
            }
        } catch (Exception ignored) {
        }
        preferences.edit().putString(PREF_HISTORY, array.toString()).apply();
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
                || lower.contains(".5ch.net/")
                || lower.contains(".io/")
                || lower.matches("^[a-z0-9.-]+\\.[a-z]{2,}(/.*)?$");
    }

    private String normalizeUrl(String input) {
        if (input == null || input.trim().isEmpty()) {
            return HOME_URL;
        }
        String value = input.trim();
        if (value.startsWith("//")) {
            return "https:" + value;
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
            return "5ch Search";
        }
        return "Search: " + query.trim();
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

    private void showKeyboard() {
        try {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.showSoftInput(addressBar, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception ignored) {
            Toast.makeText(this, "Ready to search.", Toast.LENGTH_SHORT).show();
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

    private static class CuspTab {
        String title;
        String url;
        View readerView;
        ThreadPage threadPage;
        ScrollView threadScroll;
        Map<Integer, View> postViews;
        String nativeKind;
        float threadScrollRatio;
        int threadBottomOffset;
        boolean restoreFromBottom;
        int returnToIndex = -1;
        boolean readerMode;
    }

    static class ThreadHistoryItem {
        final String title;
        final String url;

        ThreadHistoryItem(String title, String url) {
            this.title = title;
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
            page.title = "Load failed";
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
            page.title = "Search failed";
            page.error = message == null ? "Unknown error" : message;
            return page;
        }
    }

    private static class SearchResult {
        String title;
        String url;
        String meta;
    }

    private static class DatAddress {
        String server;
        String board;
        String key;
    }

    private static class Post {
        int number;
        String name;
        String date;
        String body;
    }
}
