package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.graphics.Rect;
import android.text.Html;
import android.text.Layout;
import android.text.TextUtils;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.webkit.CookieManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    static final String HOME_URL = "https://find.5ch.io/";
    static final String PREFS_NAME = "cuspidroid_settings";
    static final String PREF_5CH_NEW_TAB = "open_5ch_links_in_new_tab";
    static final String PREF_SEARCH_TEMPLATE = "search_template";
    static final String PREF_SHOW_MEDIA = "show_media";
    static final String PREF_BLUR_IMGUR = "blur_imgur_images";
    static final String PREF_BLUR_VIDEO_THUMBNAILS = "blur_video_thumbnails";
    static final String PREF_BLUR_GIF_THUMBNAILS = "blur_gif_thumbnails";
    static final String PREF_AUTOPLAY_GIFS = "autoplay_gifs";
    static final String PREF_ADDRESS_BAR_TOP = "address_bar_top";
    static final String PREF_TREE_VIEW = "tree_view";
    static final String PREF_TREE_SKIP_FIRST_REPLY = "tree_skip_first_reply";
    static final String PREF_AUTO_SCROLL_UNREAD = "auto_scroll_unread_boundary";
    static final String PREF_OMIT_COPYPASTE = "omit_copypaste_posts";
    static final String PREF_EXTERNAL_LINK_IN_APP = "external_link_in_app";
    static final String PREF_THEME_MODE = "theme_mode";
    static final String PREF_BBS_LINKS = "bbs_links";
    static final String PREF_NG_WORDS = "ng_words";
    static final String PREF_NG_RULES = "ng_rules";
    static final String PREF_READ_POSTS = "read_posts";
    static final String PREF_AA_POSTS = "aa_posts";
    static final String PREF_AUTO_AA = "auto_aa";
    static final String PREF_AA_DEBUG = "aa_debug";
    static final String PREF_MY_POSTS = "my_posts";
    static final String PREF_IMGUR_META = "imgur_meta";
    static final String PREF_IMGBB_API_KEY = "imgbb_api_key";
    static final String PREF_IMGBB_UPLOADS = "imgbb_uploads";
    static final String PREF_CACHE_ENABLED = "cache_enabled";
    static final String PREF_CACHE_MAX_MB = "cache_max_mb";
    static final String PREF_BOARD_FAVORITES = "board_favorites";
    static final String PREF_THREAD_BOOKMARKS = "thread_bookmarks";
    static final String PREF_SHOW_BOOKMARKS_IN_TAB_OVERVIEW = "show_bookmarks_in_tab_overview";
    static final String PREF_SHOW_HISTORY_ON_HOME = "show_history_on_home";
    static final String PREF_BOARD_SORT_BY_SPEED = "board_sort_by_speed";
    static final String PREF_BOARD_PRIORITY_WORDS = "board_priority_words";
    static final String PREF_DISABLE_HISTORY = "disable_history";
    static final String PREF_GESTURES_ENABLED = "gestures_enabled";
    static final String PREF_GESTURE_SENSITIVITY = "gesture_sensitivity";
    static final String PREF_GESTURE_PREFIX = "gesture_";
    static final String GESTURE_TAB_OVERVIEW = "tab_overview";
    static final String GESTURE_BACK = "back";
    static final String GESTURE_FORWARD = "forward";
    static final String GESTURE_TOP = "top";
    static final String GESTURE_BOTTOM = "bottom";
    static final String GESTURE_RELOAD = "reload";
    static final String GESTURE_CLOSE_TAB = "close_tab";
    static final String GESTURE_NEW_TAB = "new_tab";
    static final String GESTURE_RIGHT_TAB = "right_tab";
    static final String GESTURE_LEFT_TAB = "left_tab";
    static final String GESTURE_SETTINGS = "settings";
    static final String GESTURE_NEXT_THREAD = "next_thread";
    static final String GESTURE_FIND = "find";
    static final String GESTURE_BOARD = "board";
    static final String[] GESTURE_ACTIONS = {
            GESTURE_TAB_OVERVIEW, GESTURE_BACK, GESTURE_FORWARD, GESTURE_TOP,
            GESTURE_BOTTOM, GESTURE_RELOAD, GESTURE_CLOSE_TAB, GESTURE_NEW_TAB,
            GESTURE_RIGHT_TAB, GESTURE_LEFT_TAB, GESTURE_SETTINGS, GESTURE_NEXT_THREAD,
            GESTURE_FIND, GESTURE_BOARD
    };
    private static final String PREF_TABS = "saved_tabs";
    private static final String PREF_BOOKMARK_OVERVIEW_EXPANDED = "bookmark_overview_expanded";
    private static final String PREF_BOOKMARK_OVERVIEW_STATUS = "bookmark_overview_status";
    private static final String PREF_BOOKMARK_ORDER = "bookmark_order";
    static final String PREF_HISTORY = "thread_history";
    static final String DEFAULT_SEARCH_TEMPLATE = "https://find.5ch.io/search?q=%s";
    static final String LEGACY_FIND_IO_TEMPLATE = "https://find.5ch.io/search?STR=%s&TYPE=TITLE&BBS=ALL";
    static final String FIND_NET_TEMPLATE = "https://find.5ch.net/search?STR=%s&TYPE=TITLE&BBS=ALL";
    private static final String FIVE_CH_BBSMENU_URL = "https://menu.5ch.io/bbsmenu.html";
    private static final String NATIVE_THREAD = "thread";
    private static final String NATIVE_SEARCH = "search";
    private static final String NATIVE_SEARCH_HOME = "search_home";
    private static final String NATIVE_BOARD = "board";
    private static final String NATIVE_SAVED = "saved";
    private static final String NATIVE_HISTORY = "history";
    private static final String INTERNAL_URL_PREFIX = "cuspidroid://";
    private static final Charset POST_CHARSET = Charset.forName("UTF-8");
    private static final int TEAL = Color.rgb(15, 118, 110);
    private static final int SURFACE = Color.rgb(247, 248, 250);
    private static final int BORDER = Color.rgb(215, 221, 226);
    private static final int TEXT = Color.rgb(31, 41, 55);
    private static final Pattern URL_TEXT_PATTERN = Pattern.compile("(?:h?ttps?[;:]//|ttps?[;:]//|ttp[;:]//)\\S+", Pattern.CASE_INSENSITIVE);
    private static final Pattern POST_ID_PATTERN = Pattern.compile("\\bID:([A-Za-z0-9+/._-]+)");
    private static final Pattern REPLY_PATTERN = Pattern.compile(">>\\s*(\\d{1,5})(?:\\s*[-\\u2010\\u2011\\u2012\\u2013\\u2014\\u2015\\u2212\\uff0d~\\uff5e]\\s*(\\d{1,5}))?");
    private static final Pattern BE_PATTERN = Pattern.compile("\\bBE:?\\s*([A-Za-z0-9+/._-]+)", Pattern.CASE_INSENSITIVE);
    private static final int REQUEST_IMGBB_IMAGE = 42;
    private static final int MEDIA_GRID_CELL_DP = 108;
    private static final long THREAD_SCROLL_SAVE_INTERVAL_MS = 350;
    private static final long THREAD_POST_VISIBILITY_INTERVAL_MS = 16;
    private static final int THREAD_VISIBLE_RENDER_BUDGET = 6;
    private static final int THREAD_IDLE_RENDER_BUDGET = 12;
    private static final int THREAD_SCROLL_RENDER_BUDGET = 6;
    private static final int THREAD_AA_RENDER_COST = 4;
    private static final int THREAD_MEDIA_RENDER_COST = 2;
    private static final int POPUP_INITIAL_RENDER_COUNT = 1;
    private static final int POPUP_RENDER_CHUNK_SIZE = 1;
    private static final int DEFERRED_TEXT_DECORATION_BUDGET = 4;
    private static final int SEARCH_VISIBLE_RENDER_BUDGET = 24;
    private static final int SEARCH_SCROLL_RENDER_BUDGET = 48;
    private static final int SEARCH_IDLE_RENDER_BUDGET = 96;
    private static final long TAB_UNLOAD_INTERVAL_MS = 15_000L;
    private static final long TAB_UNLOAD_AFTER_MS = 60_000L;
    private static final long TAB_UNLOAD_AFTER_MANY_TABS_MS = 15_000L;
    private static final int MAX_BACKGROUND_TAB_VIEWS = 2;
    private static final int MAX_BACKGROUND_PAGE_DATA = 3;
    private static final int TAB_RELOAD_PARALLELISM = 4;
    private static final String AA_FONT_FAMILY = "Textar";
    private static final float POST_TEXT_SIZE_SP = 15f;
    private static final float AA_LINE_SPACING_MULTIPLIER = 1.0f;
    private static final float AA_SPECIAL_CHAR_RATIO_THRESHOLD = 0.35f;
    private static final int POST_OUTER_GAP_DP = 4;

    private final List<CuspTab> tabs = new ArrayList<>();
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService tabReloadExecutor = Executors.newFixedThreadPool(TAB_RELOAD_PARALLELISM);
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
    private ImageButton bottomBookmarkButton;
    private TextView tabCountButton;
    private View centerSpinnerOverlay;
    private SharedPreferences preferences;
    private EditText pendingImgbbUploadMessage;
    private int pendingImgbbExpirationSeconds;
    private AlertDialog pendingImgbbUploadDialog;
    private LinearLayout pendingImgbbMediaBox;
    private EditText pendingImgbbExpirationInput;
    private List<Uri> pendingImgbbUploadUris = new ArrayList<>();
    private final List<View> toolbarButtons = new ArrayList<>();
    private ThreadPage visibleThreadPage;
    private ScrollView visibleThreadScroll;
    private final Map<Integer, View> visiblePostViews = new LinkedHashMap<>();
    private final Set<View> suppressNextLinkClick = new LinkedHashSet<>();
    private final List<PopupWindow> replyPopups = new ArrayList<>();
    private final List<PopupWindow> animatedPopups = new ArrayList<>();
    private boolean postPopupOpening;
    private int postPopupGeneration;
    private int currentIndex = -1;
    private boolean pendingNewTab;
    private boolean pendingPrivateNewTab;
    private boolean pendingHistoryAll;
    private boolean tabOverviewVisible;
    private boolean tabOverviewPrivateMode;
    private ClosedTab recentlyClosedTab;
    private Runnable clearClosedTabUndoTask;
    private boolean addressBarTop;
    private boolean updatingThreadSearchInput;
    private Runnable threadSearchTask;
    private Runnable threadSearchHighlightTask;
    private boolean pageSearchOpen;
    private String pageSearchQuery = "";
    private final List<TextView> pageSearchMatches = new ArrayList<>();
    private final Map<TextView, CharSequence> pageSearchOriginalText = new LinkedHashMap<>();
    private int pageSearchIndex = -1;
    private int pageSearchGeneration;
    private Runnable saveTabsTask;
    private Runnable unloadTabsTask;
    private int tabOverviewScrollY;
    private boolean suppressNextAddressClick;
    private boolean addressFocusedOnDown;
    private boolean addressTouchInProgress;
    private boolean addressKeyboardVisible;
    private View imageOverlay;
    private View highlightedPostView;
    private final List<String> newTabNavigationHistory = new ArrayList<>();
    private int newTabNavigationIndex = -1;
    private Interpreter graphicViolenceInterpreter;
    private boolean graphicViolenceModelLoadAttempted;
    private final List<LazyImgurPreview> lazyImgurPreviews = new ArrayList<>();
    private final List<DeferredMediaPreview> deferredMediaPreviews = new ArrayList<>();
    private final List<DeferredTextDecoration> deferredTextDecorations = new ArrayList<>();
    private boolean imgurLoadInFlight;
    private Runnable lazyImgurTask;
    private Runnable deferredMediaTask;
    private Runnable deferredTextTask;
    private CuspTab pendingScrollToBottomTab;
    private String appliedThemeMode;
    private String cachedNgRulesKey;
    private NgRules cachedNgRules;
    private Typeface aaTypeface;
    private float gestureDownX;
    private float gestureDownY;
    private float gestureLastX;
    private float gestureLastY;
    private boolean gestureTracking;
    private boolean gestureMoved;
    private boolean gestureIntercepting;
    private final StringBuilder gestureSequence = new StringBuilder();
    private TextView gestureOverlay;

    static String gestureActionLabel(String action) {
        if (GESTURE_TAB_OVERVIEW.equals(action)) {
            return text("\u30bf\u30d6\u4e00\u89a7", "Tab list");
        }
        if (GESTURE_BACK.equals(action)) {
            return text("\u623b\u308b", "Back");
        }
        if (GESTURE_FORWARD.equals(action)) {
            return text("\u9032\u3080", "Forward");
        }
        if (GESTURE_TOP.equals(action)) {
            return text("\u5148\u982d\u3078", "Top");
        }
        if (GESTURE_BOTTOM.equals(action)) {
            return text("\u672b\u5c3e\u3078", "Bottom");
        }
        if (GESTURE_RELOAD.equals(action)) {
            return text("\u66f4\u65b0", "Reload");
        }
        if (GESTURE_CLOSE_TAB.equals(action)) {
            return text("\u30bf\u30d6\u3092\u9589\u3058\u308b", "Close tab");
        }
        if (GESTURE_NEW_TAB.equals(action)) {
            return text("\u65b0\u898f\u30bf\u30d6", "New tab");
        }
        if (GESTURE_RIGHT_TAB.equals(action)) {
            return text("\u53f3\u306e\u30bf\u30d6", "Right tab");
        }
        if (GESTURE_LEFT_TAB.equals(action)) {
            return text("\u5de6\u306e\u30bf\u30d6", "Left tab");
        }
        if (GESTURE_SETTINGS.equals(action)) {
            return text("\u8a2d\u5b9a", "Settings");
        }
        if (GESTURE_NEXT_THREAD.equals(action)) {
            return text("\u6b21\u30b9\u30ec\u691c\u7d22", "Search next thread");
        }
        if (GESTURE_FIND.equals(action)) {
            return text("\u30da\u30fc\u30b8\u5185\u691c\u7d22", "Find in page");
        }
        if (GESTURE_BOARD.equals(action)) {
            return text("\u677f\u3078", "Go to board");
        }
        return action;
    }

    static String defaultGestureForAction(String action) {
        if (GESTURE_TAB_OVERVIEW.equals(action)) {
            return "RUL";
        }
        if (GESTURE_BACK.equals(action)) {
            return "R";
        }
        if (GESTURE_FORWARD.equals(action)) {
            return "L";
        }
        if (GESTURE_TOP.equals(action)) {
            return "RD";
        }
        if (GESTURE_BOTTOM.equals(action)) {
            return "RU";
        }
        if (GESTURE_RELOAD.equals(action)) {
            return "RDL";
        }
        if (GESTURE_CLOSE_TAB.equals(action)) {
            return "LDR";
        }
        if (GESTURE_NEW_TAB.equals(action)) {
            return "LUR";
        }
        return "";
    }

    static String gestureForAction(SharedPreferences preferences, String action) {
        if (preferences == null || action == null) {
            return "";
        }
        return preferences.getString(PREF_GESTURE_PREFIX + action, defaultGestureForAction(action));
    }

    static void saveGestureForAction(SharedPreferences preferences, String action, String gesture) {
        if (preferences == null || action == null) {
            return;
        }
        String normalized = normalizeGesture(gesture);
        preferences.edit().putString(PREF_GESTURE_PREFIX + action,
                validGesture(normalized) ? normalized : "").apply();
    }

    static String normalizeGesture(String gesture) {
        if (gesture == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < gesture.length(); i++) {
            char value = Character.toUpperCase(gesture.charAt(i));
            if (value != 'L' && value != 'R' && value != 'U' && value != 'D') {
                continue;
            }
            int length = builder.length();
            if (length == 0 || builder.charAt(length - 1) != value) {
                builder.append(value);
            }
        }
        return builder.toString();
    }

    static boolean validGesture(String gesture) {
        String normalized = normalizeGesture(gesture);
        return !normalized.isEmpty()
                && (normalized.charAt(0) == 'L' || normalized.charAt(0) == 'R');
    }

    static String gestureArrows(String gesture) {
        String normalized = normalizeGesture(gesture);
        if (normalized.isEmpty()) {
            return text("\u672a\u8a2d\u5b9a", "Unassigned");
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            if (i > 0) {
                builder.append(' ');
            }
            char value = normalized.charAt(i);
            if (value == 'L') {
                builder.append("\u2190");
            } else if (value == 'R') {
                builder.append("\u2192");
            } else if (value == 'U') {
                builder.append("\u2191");
            } else if (value == 'D') {
                builder.append("\u2193");
            }
        }
        return builder.toString();
    }

    private int bgColor() {
        if (privateUiActive()) {
            return Theme.dark(this) ? Color.rgb(1, 12, 31) : Color.rgb(219, 234, 254);
        }
        return Theme.background(this);
    }

    private int surfaceColor() {
        return Theme.surface(this);
    }

    private int postColor() {
        return Theme.post(this);
    }

    private int textColor() {
        return Theme.text(this);
    }

    private int mutedColor() {
        return Theme.muted(this);
    }

    private int borderColor() {
        return Theme.border(this);
    }

    private int menuColor() {
        return Theme.menu(this);
    }

    private int barColor() {
        if (privateUiActive()) {
            return Theme.dark(this) ? Color.rgb(2, 23, 53) : Color.rgb(199, 220, 252);
        }
        return Theme.topBar(this);
    }

    private int hintTextColor() {
        return Theme.dark(this) ? Color.rgb(168, 176, 186) : Color.rgb(100, 116, 139);
    }

    private int privateBlue() {
        return Theme.dark(this) ? Color.rgb(59, 130, 246) : Color.rgb(29, 78, 216);
    }

    private int privateButtonFill(boolean active) {
        if (!active) {
            return Theme.dark(this) ? Color.rgb(17, 24, 39) : menuColor();
        }
        return Theme.dark(this) ? Color.rgb(8, 47, 99) : Color.rgb(191, 219, 254);
    }

    private int privateButtonStroke(boolean active) {
        if (!active) {
            return Theme.dark(this) ? Color.rgb(51, 65, 85) : borderColor();
        }
        return Theme.dark(this) ? Color.rgb(37, 99, 235) : Color.rgb(29, 78, 216);
    }

    private int privateButtonIcon(boolean active) {
        if (!active && Theme.dark(this)) {
            return Color.rgb(147, 197, 253);
        }
        return privateBlue();
    }

    static String text(String ja, String en) {
        return Locale.JAPANESE.getLanguage().equals(Locale.getDefault().getLanguage()) ? ja : en;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        migrateFavoriteBoardsToBookmarks();
        appliedThemeMode = themeMode();
        buildLayout();
        contentFrame.addView(loadingView(""));
        scheduleTabUnload();

        contentFrame.postDelayed(this::openInitialContent, 32);
    }

    private void openInitialContent() {
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
        if (saveTabsTask != null) {
            mainHandler.removeCallbacks(saveTabsTask);
            saveTabsTask = null;
        }
        if (unloadTabsTask != null) {
            mainHandler.removeCallbacks(unloadTabsTask);
            unloadTabsTask = null;
        }
        saveTabs(true);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentThemeMode = themeMode();
        boolean themeChanged = appliedThemeMode != null && !appliedThemeMode.equals(currentThemeMode);
        if (bottomToolbar != null && (addressBarTop != addressBarOnTop() || themeChanged)) {
            CuspTab tab = currentTab();
            if (tab != null) {
                rememberThreadScroll(tab);
            }
            appliedThemeMode = currentThemeMode;
            buildLayout();
            if (pendingNewTab) {
                showPendingNewTabHistory(pendingHistoryAll);
            } else if (currentIndex >= 0 && currentIndex < tabs.size()) {
                switchToTab(currentIndex);
            }
        }
        scheduleTabUnload();
    }

    @Override
    protected void onDestroy() {
        if (saveTabsTask != null) {
            mainHandler.removeCallbacks(saveTabsTask);
            saveTabsTask = null;
        }
        if (unloadTabsTask != null) {
            mainHandler.removeCallbacks(unloadTabsTask);
            unloadTabsTask = null;
        }
        saveTabs(true);
        closeImageClassifiers();
        ioExecutor.shutdownNow();
        tabReloadExecutor.shutdownNow();
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
            if (newTabNavigationIndex > 0) {
                navigateNewTabHistory(-1);
                return;
            }
            cancelPendingNewTab();
            return;
        }
        if (addressBar != null && addressBar.hasFocus()) {
            clearAddressFocus();
            return;
        }
        if (isPageSearchOpen()) {
            closeThreadSearch();
            return;
        }
        if (!replyPopups.isEmpty()) {
            dismissTopReplyPopup();
            return;
        }
        CuspTab tab = currentTab();
        if (canGoBackInCurrentTab(tab)) {
            goBack();
            return;
        }
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
            if (!animatedPopups.isEmpty() && !isTouchInsideAnimatedPopup(event)) {
                dismissTopAnimatedPopup();
                return true;
            }
            if (highlightedPostView != null) {
                clearJumpHighlight();
            }
            if (!replyPopups.isEmpty() && !isTouchInsideTopReplyPopup(event)) {
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
        boolean wasGestureIntercepting = gestureIntercepting;
        boolean consumeGesture = trackGestureEvent(event);
        if (consumeGesture) {
            if (!wasGestureIntercepting && gestureIntercepting) {
                cancelChildTouch(event);
            }
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    private void cancelChildTouch(MotionEvent event) {
        MotionEvent cancel = MotionEvent.obtain(event);
        cancel.setAction(MotionEvent.ACTION_CANCEL);
        try {
            super.dispatchTouchEvent(cancel);
        } finally {
            cancel.recycle();
        }
    }

    private void buildLayout() {
        addressBarTop = addressBarOnTop();
        applySystemBarTheme();
        toolbarButtons.clear();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bgColor());
        root.setFocusableInTouchMode(true);
        root.requestFocus();
        setContentView(root);
        installKeyboardFocusWatcher(root);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
        root.addView(progressBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(3)));

        contentFrame = new FrameLayout(this);
        contentFrame.setBackgroundColor(bgColor());
        contentFrame.setFocusableInTouchMode(true);
        overlayFrame = new FrameLayout(this);
        overlayFrame.addView(contentFrame, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        suggestionsPanel = new LinearLayout(this);
        suggestionsPanel.setOrientation(LinearLayout.VERTICAL);
        suggestionsPanel.setBackgroundColor(Color.TRANSPARENT);
        suggestionsPanel.setPadding(dp(12), dp(12), dp(12), dp(12));
        suggestionsPanel.setVisibility(View.GONE);
        FrameLayout.LayoutParams suggestionsParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        suggestionsParams.gravity = addressBarTop ? Gravity.TOP : Gravity.BOTTOM;
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
        threadSearchInput.setTextColor(textColor());
        threadSearchInput.setHintTextColor(hintTextColor());
        threadSearchInput.setHint(text("\u30da\u30fc\u30b8\u5185\u691c\u7d22", "Find in page"));
        threadSearchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        threadSearchInput.setBackground(addressBarBackground());
        threadSearchInput.setPadding(dp(12), 0, dp(12), 0);
        threadSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean enter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_UP;
            boolean enterDown = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || enter) {
                if (isThreadPageSearchActive()) {
                    CuspTab tab = currentTab();
                    if (tab != null && !tab.threadSearchMatches.isEmpty()) {
                        moveThreadSearch(1);
                    } else {
                        scheduleThreadSearch(threadSearchInput.getText().toString(), true);
                    }
                } else if (!pageSearchMatches.isEmpty()) {
                    movePageSearch(1);
                } else {
                    updatePageSearch(threadSearchInput.getText().toString(), true);
                }
                threadSearchInput.requestFocus();
                threadSearchInput.setSelection(threadSearchInput.getText().length());
                return true;
            }
            return enterDown;
        });
        threadSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!updatingThreadSearchInput) {
                    if (isThreadPageSearchActive()) {
                        scheduleThreadSearch(s.toString(), true);
                    } else {
                        updatePageSearch(s.toString(), true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        threadSearchBar.addView(threadSearchInput, new LinearLayout.LayoutParams(0, dp(40), 1));
        threadSearchCount = new TextView(this);
        threadSearchCount.setTextColor(mutedColor());
        threadSearchCount.setTextSize(12);
        threadSearchCount.setGravity(Gravity.CENTER);
        threadSearchBar.addView(threadSearchCount, new LinearLayout.LayoutParams(dp(42), dp(40)));
        threadSearchBar.addView(threadSearchButton(R.drawable.ic_arrow_up, text("\u524d\u3078", "Previous"), v -> moveActivePageSearch(-1)));
        threadSearchBar.addView(threadSearchButton(R.drawable.ic_arrow_down, text("\u6b21\u3078", "Next"), v -> moveActivePageSearch(1)));
        threadSearchBar.addView(threadSearchButton(R.drawable.ic_close, text("\u9589\u3058\u308b", "Close"), v -> closeThreadSearch()));

        bottomThreadBar = new LinearLayout(this);
        bottomThreadBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomThreadBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomThreadBar.setPadding(dp(10), dp(4), dp(6), dp(4));
        bottomThreadBar.setBackground(bottomBarBackground());

        bottomThreadTitle = new TextView(this);
        bottomThreadTitle.setTextColor(textColor());
        bottomThreadTitle.setTextSize(14);
        bottomThreadTitle.setSingleLine(false);
        bottomThreadTitle.setMaxLines(2);
        bottomThreadTitle.setEllipsize(TextUtils.TruncateAt.END);
        bottomThreadTitle.setIncludeFontPadding(false);
        bottomThreadTitle.setGravity(Gravity.CENTER_VERTICAL);
        bottomThreadBar.addView(bottomThreadTitle, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        bottomWriteButton = iconButton(R.drawable.ic_edit, text("\u66f8\u304d\u8fbc\u307f", "Write"), v -> showWriteDialog());
        bottomThreadBar.addView(bottomWriteButton, new LinearLayout.LayoutParams(dp(42), dp(40)));

        bottomBookmarkButton = iconButton(R.drawable.ic_star_border, text("\u30d6\u30c3\u30af\u30de\u30fc\u30af", "Bookmark"), null);

        bottomToolbar = new LinearLayout(this);
        bottomToolbar.setOrientation(LinearLayout.HORIZONTAL);
        bottomToolbar.setGravity(Gravity.CENTER_VERTICAL);
        bottomToolbar.setPadding(dp(6), dp(5), dp(6), dp(5));
        bottomToolbar.setBackgroundColor(barColor());

        addressBar = new EditText(this);
        addressBar.setSingleLine(true);
        addressBar.setMaxLines(1);
        addressBar.setMinLines(1);
        addressBar.setLines(1);
        addressBar.setHorizontallyScrolling(true);
        addressBar.setHorizontalScrollBarEnabled(false);
        addressBar.setEllipsize(TextUtils.TruncateAt.END);
        addressBar.setTextSize(15);
        addressBar.setIncludeFontPadding(false);
        addressBar.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        addressBar.setTextColor(textColor());
        addressBar.setHintTextColor(hintTextColor());
        addressBar.setHint(text("\u691c\u7d22\u307e\u305f\u306fURL", "Search or URL"));
        addressBar.setSelectAllOnFocus(true);
        addressBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        addressBar.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        addressBar.setSingleLine(true);
        addressBar.setMaxLines(1);
        addressBar.setMinLines(1);
        addressBar.setLines(1);
        addressBar.setHorizontallyScrolling(true);
        addressBar.setHorizontalScrollBarEnabled(false);
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
        addressBar.setOnClickListener(v -> {
            if (suppressNextAddressClick) {
                suppressNextAddressClick = false;
                return;
            }
            addressBar.requestFocus();
            addressBar.selectAll();
            showKeyboardSoon();
        });
        addressBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                addressBar.selectAll();
                if (!addressTouchInProgress) {
                    showKeyboardSoon();
                }
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
            suppressNextAddressClick = true;
            mainHandler.postDelayed(() -> suppressNextAddressClick = false, 900);
            addressTouchInProgress = false;
            clearAddressFocus();
            showAddressEditMenu();
            return true;
        });
        addressBar.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                addressFocusedOnDown = addressBar.hasFocus();
                addressTouchInProgress = true;
            }
            if (suppressNextAddressClick
                    && (event.getActionMasked() == MotionEvent.ACTION_UP
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL)) {
                suppressNextAddressClick = false;
                addressTouchInProgress = false;
                if (!addressFocusedOnDown) {
                    clearAddressFocus();
                }
                return true;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_UP
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                addressTouchInProgress = false;
            }
            return false;
        });
        addressBar.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollX != 0 || scrollY != 0) {
                addressBar.scrollTo(0, 0);
            }
        });
        LinearLayout.LayoutParams addressParams = new LinearLayout.LayoutParams(0, dp(40), 1);
        bottomToolbar.addView(addressBar, addressParams);

        addToolbarButton(bottomToolbar, R.drawable.ic_add, text("\u65b0\u898f\u30bf\u30d6", "New tab"), v -> createBlankTab());
        tabCountButton = tabCountButton();
        toolbarButtons.add(tabCountButton);
        bottomToolbar.addView(tabCountButton, new LinearLayout.LayoutParams(dp(32), dp(32)));
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
        button.setColorFilter(textColor());
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
        view.setTextColor(textColor());
        view.setTextSize(13);
        view.setGravity(Gravity.CENTER);
        view.setSingleLine(true);
        view.setContentDescription(text("\u30bf\u30d6", "Tabs"));
        view.setBackground(tabCountBackground(false));
        view.setOnClickListener(v -> showTabOverview());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(30), dp(32));
        params.setMargins(dp(7), 0, dp(7), 0);
        view.setLayoutParams(params);
        return view;
    }

    private GradientDrawable tabCountBackground(boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(selected ? Theme.active(this) : Color.TRANSPARENT);
        drawable.setStroke(dp(2), selected ? TEAL : textColor());
        drawable.setCornerRadius(dp(5));
        return drawable;
    }

    private void addToolbarButton(LinearLayout toolbar, int iconRes, String description, View.OnClickListener listener) {
        ImageButton button = iconButton(iconRes, description, listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(34), dp(36));
        params.setMargins(dp(7), 0, dp(7), 0);
        button.setLayoutParams(params);
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

    private void prepareChromeForLoading() {
        if (addressBar != null && addressBar.hasFocus()) {
            clearAddressFocus();
        }
        if (suggestionsPanel != null) {
            suggestionsPanel.setVisibility(View.GONE);
        }
        for (View button : toolbarButtons) {
            button.setVisibility(View.VISIBLE);
            button.setEnabled(true);
        }
        if (bottomToolbar != null && !tabOverviewVisible) {
            bottomToolbar.setVisibility(View.VISIBLE);
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
        configureSuggestionsPanel(!query.isEmpty());
        if (!query.isEmpty()) {
            int bookmarkCount = 0;
            for (SavedItem bookmark : readSavedItems(PREF_THREAD_BOOKMARKS)) {
                String title = bookmark.title == null ? "" : bookmark.title;
                String url = bookmark.url == null ? "" : bookmark.url;
                if (!title.toLowerCase(Locale.ROOT).contains(query)
                        && !url.toLowerCase(Locale.ROOT).contains(query)) {
                    continue;
                }
                TextView item = suggestionItem(text("\u30d6\u30c3\u30af\u30de\u30fc\u30af", "Bookmark"), title);
                item.setOnClickListener(v -> {
                    addressBar.setText(url);
                    addressBar.setSelection(addressBar.getText().length());
                    openFromAddressBar();
                });
                if (suggestionsPanel.getChildCount() > 0) {
                    suggestionsPanel.addView(suggestionDivider());
                }
                suggestionsPanel.addView(item);
                bookmarkCount++;
                if (bookmarkCount >= 6) {
                    break;
                }
            }
            int tabCount = 0;
            for (int i = 0; i < tabs.size(); i++) {
                CuspTab tab = tabs.get(i);
                String title = tabSuggestionTitle(tab);
                String url = tab.url == null ? "" : tab.url;
                if (!title.toLowerCase(Locale.ROOT).contains(query)
                        && !url.toLowerCase(Locale.ROOT).contains(query)) {
                    continue;
                }
                int index = i;
                TextView item = suggestionItem(text("\u30bf\u30d6", "Tab"), title);
                item.setOnClickListener(v -> switchToTab(index));
                if (suggestionsPanel.getChildCount() > 0) {
                    suggestionsPanel.addView(suggestionDivider());
                }
                suggestionsPanel.addView(item);
                tabCount++;
                if (tabCount >= 6) {
                    break;
                }
            }
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
            boolean hasRows = suggestionsPanel.getChildCount() > 0;
            if (!query.isEmpty() && !addressBarTop) {
                suggestionsPanel.addView(new View(this), new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
            }
            if (hasRows) {
                suggestionsPanel.addView(suggestionDivider());
            }
            TextView item = suggestionItem(
                    text("\u30af\u30ea\u30c3\u30d7\u30dc\u30fc\u30c9\u304b\u3089\u30ea\u30f3\u30af\u3092\u5165\u529b", "Enter link from clipboard"),
                    clipboardLink,
                    R.drawable.ic_paste);
            item.setOnClickListener(v -> {
                addressBar.setText(clipboardLink);
                addressBar.setSelection(addressBar.getText().length());
                openFromAddressBar();
            });
            suggestionsPanel.addView(item);
        }
        if (suggestionsPanel.getChildCount() == 0) {
            if (query.isEmpty()) {
                suggestionsPanel.setVisibility(View.GONE);
                return;
            }
            TextView empty = suggestionItem(text("\u5019\u88dc\u306a\u3057", "No suggestions"), text("\u691c\u7d22\u8a9e\u307e\u305f\u306fURL\u3092\u5165\u529b", "Enter a search term or URL"));
            suggestionsPanel.addView(empty);
        }
        suggestionsPanel.setVisibility(View.VISIBLE);
    }

    private String tabSuggestionTitle(CuspTab tab) {
        if (tab == null) {
            return text("\u65b0\u898f\u30bf\u30d6", "New tab");
        }
        if (tab.title != null && !tab.title.trim().isEmpty()) {
            return tab.title.trim();
        }
        if (tab.url != null && !tab.url.trim().isEmpty()) {
            return tab.url.trim();
        }
        return text("\u65b0\u898f\u30bf\u30d6", "New tab");
    }

    private void configureSuggestionsPanel(boolean fullScreen) {
        if (suggestionsPanel == null) {
            return;
        }
        ViewGroup.LayoutParams currentParams = suggestionsPanel.getLayoutParams();
        FrameLayout.LayoutParams params = currentParams instanceof FrameLayout.LayoutParams
                ? (FrameLayout.LayoutParams) currentParams
                : new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = fullScreen ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = addressBarTop ? Gravity.TOP : Gravity.BOTTOM;
        suggestionsPanel.setLayoutParams(params);
        suggestionsPanel.setPadding(fullScreen ? dp(12) : 0, fullScreen ? dp(12) : 0,
                fullScreen ? dp(12) : 0, fullScreen ? dp(12) : 0);
        suggestionsPanel.setBackground(fullScreen
                ? new ColorDrawable(menuColor())
                : compactSuggestionPanelBackground());
    }

    private GradientDrawable compactSuggestionPanelBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(menuColor());
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(0);
        return drawable;
    }

    private TextView suggestionItem(String label, String value) {
        return suggestionItem(label, value, 0);
    }

    private TextView suggestionItem(String label, String value, int iconRes) {
        TextView view = new TextView(this);
        view.setText(label + "\n" + value);
        view.setTextColor(textColor());
        view.setTextSize(14);
        view.setBackgroundColor(menuColor());
        view.setPadding(dp(12), dp(10), dp(12), dp(10));
        view.setMinHeight(dp(58));
        if (iconRes != 0) {
            view.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
            view.setCompoundDrawablePadding(dp(10));
        }
        return view;
    }

    private View suggestionDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(borderColor());
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
            return (is5chUrl(url) || isRegisteredBbsUrl(url)) ? url : null;
        } catch (Exception error) {
            return null;
        }
    }

    private void showAddressEditMenu() {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.HORIZONTAL);
        menu.setBackground(menuBackground());
        menu.setPadding(dp(4), dp(4), dp(4), dp(4));
        PopupWindow popup = new PopupWindow(menu, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        prepareAnimatedPopupDismiss(popup, menu);

        menu.addView(menuItem(text("\u30b3\u30d4\u30fc", "Copy"), v -> {
            copyAddressText();
            dismissPopupAnimated(popup);
        }));
        menu.addView(verticalDivider());
        menu.addView(menuItem(text("\u8cbc\u308a\u4ed8\u3051", "Paste"), v -> {
            pasteIntoAddressBar(false);
            dismissPopupAnimated(popup);
        }));
        menu.addView(verticalDivider());
        menu.addView(menuItem(text("\u8cbc\u308a\u4ed8\u3051\u3066\u79fb\u52d5", "Paste and go"), v -> {
            pasteIntoAddressBar(true);
            dismissPopupAnimated(popup);
        }));
        showAddressMenuAtToolbarEdge(popup, menu, true);
    }

    private void cancelThreadChunkRender(CuspTab tab) {
        if (tab != null) {
            tab.threadRenderGeneration++;
            tab.threadRendering = false;
            if (tab.threadPostVisibilityTask != null) {
                mainHandler.removeCallbacks(tab.threadPostVisibilityTask);
                tab.threadPostVisibilityTask = null;
            }
            if (tab.threadScrollChromeTask != null) {
                mainHandler.removeCallbacks(tab.threadScrollChromeTask);
                tab.threadScrollChromeTask = null;
            }
        }
    }

    private void applySystemBarTheme() {
        Theme.applySystemBars(this);
    }

    private void showAddressMenuAtToolbarEdge(PopupWindow popup, View menu, boolean alignLeft) {
        int[] toolbarLocation = new int[2];
        bottomToolbar.getLocationOnScreen(toolbarLocation);
        menu.measure(View.MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int width = menu.getMeasuredWidth();
        int height = menu.getMeasuredHeight();
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int x = alignLeft ? frame.left : frame.right - width;
        int y = addressBarTop
                ? toolbarLocation[1] + bottomToolbar.getHeight()
                : toolbarLocation[1] - height;
        popup.setClippingEnabled(true);
        popup.showAtLocation(getWindow().getDecorView(), Gravity.NO_GRAVITY, x, y);
        animatePopupIn(popup, !addressBarTop);
    }

    private TextView menuItem(String text, View.OnClickListener listener) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(textColor());
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
        PopupWindow popup = new PopupWindow(menu, dp(220), ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        prepareAnimatedPopupDismiss(popup, menu);

        menu.addView(menuIconItem(R.drawable.ic_arrow_forward, text("WebView\u3067\u958b\u304f", "Open in WebView"), v -> {
            dismissPopupAnimated(popup);
            openCurrentThreadInWebView();
        }), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (!hasUrl) {
            menu.getChildAt(0).setEnabled(false);
            menu.getChildAt(0).setAlpha(0.45f);
        }
        menu.addView(horizontalDivider());
        String boardUrl = currentThreadBoardUrl(tab);
        View openBoard = menuIconItem(R.drawable.ic_arrow_up, text("\u677f\u3078", "Go to board"), v -> {
            dismissPopupAnimated(popup);
            openInCurrentTab(boardUrl);
        });
        if (boardUrl == null) {
            openBoard.setEnabled(false);
            openBoard.setAlpha(0.45f);
        }
        menu.addView(openBoard, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        menu.addView(horizontalDivider());
        boolean bookmarked = tab != null && savedItemExists(PREF_THREAD_BOOKMARKS, tab.url);
        View bookmark = menuIconItem(savedIcon(PREF_THREAD_BOOKMARKS, tab == null ? "" : tab.url),
                bookmarked ? text("\u30d6\u30c3\u30af\u30de\u30fc\u30af\u3092\u5916\u3059", "Remove bookmark")
                        : text("\u30d6\u30c3\u30af\u30de\u30fc\u30af", "Bookmark"), v -> {
                    dismissPopupAnimated(popup);
                    toggleCurrentBookmark();
                });
        if (!canBookmarkTab(tab)) {
            bookmark.setEnabled(false);
            bookmark.setAlpha(0.45f);
        }
        menu.addView(bookmark, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        menu.addView(horizontalDivider());
        menu.addView(menuIconItem(R.drawable.ic_search, text("\u30da\u30fc\u30b8\u5185\u691c\u7d22", "Find in page"), v -> {
            dismissPopupAnimated(popup);
            showThreadSearchDialog();
        }), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        menu.addView(horizontalDivider());
        menu.addView(menuIconItem(R.drawable.ic_search_next, text("\u6b21\u30b9\u30ec\u691c\u7d22", "Search next thread"), v -> {
            dismissPopupAnimated(popup);
            searchNextThread();
        }), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        menu.addView(horizontalDivider());
        menu.addView(menuIconItem(R.drawable.ic_settings, text("\u8a2d\u5b9a", "Settings"), v -> {
            dismissPopupAnimated(popup);
            openSettings();
        }), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        menu.addView(horizontalDivider());
        menu.addView(menuNavigationRow(popup), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        showMenuWithinScreen(popup, menu, anchor);
    }

    private String currentThreadBoardUrl(CuspTab tab) {
        if (tab == null || tab.url == null || !isThreadUrl(tab.url)) {
            return null;
        }
        DatAddress address = datAddress(tab.url);
        if (address == null || address.host == null || address.board == null
                || address.host.isEmpty() || address.board.isEmpty()) {
            return null;
        }
        String scheme = address.scheme == null || address.scheme.isEmpty() ? "https" : address.scheme;
        return scheme + "://" + address.host + "/" + address.board + "/";
    }

    private void openCurrentThreadBoard() {
        String boardUrl = currentThreadBoardUrl(currentTab());
        if (boardUrl == null) {
            Toast.makeText(this, text("\u677fURL\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093", "No board URL found."), Toast.LENGTH_SHORT).show();
            return;
        }
        openInCurrentTab(boardUrl);
    }

    private LinearLayout menuNavigationRow(PopupWindow popup) {
        CuspTab tab = currentTab();
        boolean canBack = canGoBackInCurrentTab(tab) || tabs.size() > 1 || pendingNewTab;
        boolean canForward = pendingNewTab ? canGoForwardInNewTab() : canGoForwardInCurrentTab(tab);
        boolean canShareOrReload = tab != null
                && tab.url != null
                && !tab.url.trim().isEmpty()
                && !NATIVE_SEARCH_HOME.equals(tab.nativeKind);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        ImageButton back = menuIconButton(R.drawable.ic_arrow_back, text("\u623b\u308b", "Back"), v -> {
            dismissPopupAnimated(popup);
            onBackPressed();
        });
        setMenuButtonEnabled(back, canBack);
        row.addView(back);
        ImageButton forward = menuIconButton(R.drawable.ic_arrow_forward, text("\u9032\u3080", "Forward"), v -> {
            dismissPopupAnimated(popup);
            goForward();
        });
        setMenuButtonEnabled(forward, canForward);
        row.addView(forward);
        ImageButton share = menuIconButton(R.drawable.ic_share, text("\u5171\u6709", "Share"), v -> {
            dismissPopupAnimated(popup);
            shareCurrentThread();
        });
        setMenuButtonEnabled(share, canShareOrReload);
        row.addView(share);
        ImageButton reload = menuIconButton(R.drawable.ic_refresh, text("\u66f4\u65b0", "Reload"), v -> {
            dismissPopupAnimated(popup);
            reloadFromMenu();
        });
        setMenuButtonEnabled(reload, canShareOrReload);
        row.addView(reload);
        return row;
    }

    private void setMenuButtonEnabled(ImageButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.32f);
    }

    private void showMenuWithinScreen(PopupWindow popup, View menu, View anchor) {
        int width = dp(220);
        menu.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int height = menu.getMeasuredHeight();
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int[] toolbarLocation = new int[2];
        bottomToolbar.getLocationOnScreen(toolbarLocation);
        int x = frame.right - width;
        int y = addressBarTop
                ? toolbarLocation[1] + bottomToolbar.getHeight()
                : toolbarLocation[1] - height;
        popup.setClippingEnabled(true);
        popup.showAtLocation(getWindow().getDecorView(), Gravity.NO_GRAVITY, x, y);
        animatePopupIn(popup, !addressBarTop);
    }

    private void showPopupAttachedToAnchor(PopupWindow popup, View menu, View anchor) {
        int width = dp(220);
        menu.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int height = menu.getMeasuredHeight();
        Rect frame = new Rect();
        anchor.getWindowVisibleDisplayFrame(frame);
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int xoff = anchor.getWidth() - width;
        boolean showAbove = location[1] + anchor.getHeight() + height > frame.bottom;
        int yoff = showAbove ? -height - anchor.getHeight() : 0;
        popup.setClippingEnabled(true);
        popup.showAsDropDown(anchor, xoff, yoff);
        animatePopupIn(popup, showAbove);
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
        icon.setColorFilter(textColor());
        row.addView(icon, new LinearLayout.LayoutParams(dp(22), dp(22)));
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(textColor());
        label.setTextSize(14);
        label.setPadding(dp(12), 0, 0, 0);
        row.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return row;
    }

    private View verticalDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(borderColor());
        divider.setLayoutParams(new LinearLayout.LayoutParams(dp(1), ViewGroup.LayoutParams.MATCH_PARENT));
        return divider;
    }

    private View horizontalDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(borderColor());
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
        return roundedDrawable(fill, stroke, radius, dp(1));
    }

    private GradientDrawable roundedDrawable(int fill, int stroke, int radius, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setStroke(strokeWidth, stroke);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private GradientDrawable roundedFill(int fill, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private Drawable postBackground(boolean unread) {
        return postBackground(unread, false);
    }

    private Drawable postBackground(boolean unread, boolean myPost) {
        int fill = unread ? Theme.unread(this) : postColor();
        if (!myPost) {
            return roundedFill(fill, dp(12));
        }
        return new MyPostBackgroundDrawable(fill, Color.rgb(37, 99, 235), dp(12), dp(5));
    }

    private Drawable framedPostBackground(Drawable base) {
        GradientDrawable frame = new GradientDrawable();
        frame.setColor(Color.TRANSPARENT);
        frame.setStroke(dp(2), TEAL);
        frame.setCornerRadius(dp(12));
        return new LayerDrawable(new Drawable[]{base, frame});
    }

    private GradientDrawable addressBarBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.field(this));
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(dp(20));
        return drawable;
    }

    private GradientDrawable menuBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(menuColor());
        drawable.setStroke(dp(2), Theme.strongBorder(this));
        drawable.setCornerRadius(dp(10));
        return drawable;
    }

    private GradientDrawable bottomBarBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(barColor());
        drawable.setStroke(dp(1), privateUiActive() ? privateBlue() : borderColor());
        return drawable;
    }

    private void updatePrivateChrome() {
        if (contentFrame != null) {
            contentFrame.setBackgroundColor(bgColor());
        }
        if (bottomToolbar != null) {
            bottomToolbar.setBackgroundColor(barColor());
        }
        if (bottomThreadBar != null) {
            bottomThreadBar.setBackground(bottomBarBackground());
        }
        if (threadSearchBar != null) {
            threadSearchBar.setBackground(bottomBarBackground());
        }
        if (addressBar != null) {
            addressBar.setHintTextColor(hintTextColor());
            addressBar.setBackground(addressBarBackground());
        }
        if (threadSearchInput != null) {
            threadSearchInput.setBackground(addressBarBackground());
            threadSearchInput.setHintTextColor(hintTextColor());
        }
    }

    private void createTab(String url, boolean select) {
        createTab(url, select, -1, false, currentTabIsPrivate());
    }

    private void createTab(String url, boolean select, int returnToIndex) {
        createTab(url, select, returnToIndex, false, currentTabIsPrivate());
    }

    private void createTab(String url, boolean select, int returnToIndex, boolean backToNewTab) {
        createTab(url, select, returnToIndex, backToNewTab, currentTabIsPrivate());
    }

    private void createTab(String url, boolean select, int returnToIndex, boolean backToNewTab, boolean privateBrowsing) {
        CuspTab tab = new CuspTab();
        tab.title = text("\u65b0\u898f\u30bf\u30d6", "New tab");
        tab.url = "";
        tab.returnToIndex = returnToIndex;
        tab.backToNewTab = backToNewTab;
        tab.privateBrowsing = privateBrowsing;
        tab.lastActivatedAt = android.os.SystemClock.uptimeMillis();
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

    private void createPrivateBlankTab() {
        showPendingNewTab(true);
    }

    private void showPendingNewTab() {
        showPendingNewTab(false);
    }

    private void showPendingNewTab(boolean privateBrowsing) {
        CuspTab previous = currentTab();
        if (previous != null) {
            rememberThreadScroll(previous);
            requestSaveTabsSoon();
        }
        if (!replyPopups.isEmpty()) {
            dismissThreadPopups();
        }
        pendingNewTab = true;
        pendingPrivateNewTab = privateBrowsing;
        pendingHistoryAll = false;
        resetNewTabHistory();
        tabOverviewVisible = false;
        contentFrame.setBackgroundColor(bgColor());
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

    private void showPendingNewTabHistory(boolean fullHistory) {
        if (!pendingNewTab) {
            showPendingNewTab();
            return;
        }
        pendingHistoryAll = fullHistory;
        recordNewTabPage(fullHistory ? "history" : "home");
        tabOverviewVisible = false;
        contentFrame.setBackgroundColor(bgColor());
        contentFrame.removeAllViews();
        contentFrame.addView(fullHistory ? buildHistoryView() : buildSearchHomeView(false));
        addressBar.setText("");
        clearAddressFocus();
        renderTabs();
    }

    private void cancelPendingNewTab() {
        pendingNewTab = false;
        pendingPrivateNewTab = false;
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
                tab.privateBrowsing = item.optBoolean("privateBrowsing", false);
                tab.bookmarkOverviewTab = item.optBoolean("bookmarkOverviewTab", false);
                String nativeKind = item.optString("nativeKind", "");
                tab.nativeKind = nativeKind.isEmpty() || "null".equals(nativeKind) ? null : nativeKind;
                tab.threadScrollRatio = (float) item.optDouble("threadScrollRatio", 0);
                tab.threadBottomOffset = item.optInt("threadBottomOffset", 0);
                tab.threadScrollUrl = item.optString("threadScrollUrl", url);
                tab.hasSavedThreadScroll = item.optBoolean("hasSavedThreadScroll", false);
                tab.knownMaxPostNumber = item.optInt("knownMaxPostNumber", 0);
                tab.knownPostCount = item.optInt("knownPostCount", 0);
                tab.cachedUnreadCount = item.optInt("cachedUnreadCount", 0);
                tab.hasThreadStats = item.optBoolean("hasThreadStats", tab.knownMaxPostNumber > 0);
                tab.knownThreadArchived = item.optBoolean("knownThreadArchived", false);
                restoreNavigationHistory(tab, item);
                tab.readerMode = tab.nativeKind != null || url.isEmpty();
                tab.readerView = loadingView("");
                if (NATIVE_THREAD.equals(tab.nativeKind)) {
                    tab.savedThreadPageJson = item.optJSONObject("threadPage");
                } else if (NATIVE_SEARCH.equals(tab.nativeKind) || NATIVE_BOARD.equals(tab.nativeKind)) {
                    tab.savedSearchPageJson = item.optJSONObject("searchPage");
                } else if (NATIVE_SAVED.equals(tab.nativeKind) && url.startsWith(INTERNAL_URL_PREFIX + "saved/")) {
                    SavedPage savedPage = savedPageFromToken(decodeNewTabToken(url.substring((INTERNAL_URL_PREFIX + "saved/").length())));
                    tab.readerView = buildSavedItemsView(savedPage.key, savedPage.folder);
                } else if (NATIVE_HISTORY.equals(tab.nativeKind)) {
                    tab.readerView = buildHistoryView();
                } else if (NATIVE_SEARCH_HOME.equals(tab.nativeKind) || url.isEmpty()) {
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
            renderTabs();
            mainHandler.post(() -> hydrateRestoredTabs(selected));
            return true;
        } catch (Exception error) {
            return false;
        }
    }

    private String themeMode() {
        return preferences.getString(PREF_THEME_MODE, Theme.MODE_SYSTEM);
    }

    private void hydrateRestoredTabs(int selected) {
        List<Integer> order = new ArrayList<>();
        if (selected >= 0 && selected < tabs.size()) {
            order.add(selected);
        }
        for (int i = 0; i < tabs.size(); i++) {
            if (i != selected) {
                order.add(i);
            }
        }
        hydrateRestoredTabs(order, 0);
    }

    private void hydrateRestoredTabs(List<Integer> order, int position) {
        if (position >= order.size()) {
            renderTabs();
            return;
        }
        int i = order.get(position);
        if (i >= 0 && i < tabs.size()) {
            CuspTab tab = tabs.get(i);
            if (NATIVE_THREAD.equals(tab.nativeKind)) {
                if (tab.savedThreadPageJson != null) {
                    tab.threadPage = threadPageFromJson(tab.savedThreadPageJson);
                    tab.savedThreadPageJson = null;
                }
                if (tab.threadPage != null && !tab.threadPage.posts.isEmpty()) {
                    tab.readerMode = true;
                    tab.readPostNumber = readPostNumberForTab(tab, tab.threadPage.url);
                    updateTabThreadStats(tab, tab.threadPage);
                    if (i == currentIndex) {
                        tab.postViews = new LinkedHashMap<>();
                        tab.readerView = buildThreadView(tab.threadPage, tab);
                    } else {
                        unloadTabView(tab);
                    }
                } else if (i == currentIndex && tab.url != null && !tab.url.isEmpty()) {
                    openInTab(tab, tab.url, false);
                } else {
                    unloadTabView(tab);
                }
            } else if (NATIVE_SEARCH.equals(tab.nativeKind) || NATIVE_BOARD.equals(tab.nativeKind)) {
                if (tab.savedSearchPageJson != null) {
                    tab.searchPage = searchPageFromJson(tab.savedSearchPageJson);
                    tab.savedSearchPageJson = null;
                }
                if (tab.searchPage != null) {
                    tab.readerMode = true;
                    if (i == currentIndex) {
                        tab.readerView = buildSearchView(tab.searchPage);
                    } else {
                        unloadTabView(tab);
                    }
                } else if (i == currentIndex && tab.url != null && !tab.url.isEmpty()) {
                    openInTab(tab, tab.url, false);
                } else {
                    unloadTabView(tab);
                }
            } else if (tab.url == null || tab.url.isEmpty()) {
                tab.readerMode = true;
                tab.nativeKind = NATIVE_SEARCH_HOME;
                if (i == currentIndex) {
                    tab.readerView = buildSearchHomeView(true);
                } else {
                    unloadTabView(tab);
                }
            } else if (i == currentIndex && tab.readerView == null) {
                openInTab(tab, tab.url, false);
            } else if (i != currentIndex) {
                unloadTabView(tab);
            }
            if (i == currentIndex && !tabOverviewVisible && !pendingNewTab) {
                switchToTab(i);
                if (NATIVE_THREAD.equals(tab.nativeKind) && tab.threadPage != null && !tab.threadPage.posts.isEmpty()) {
                    restoreThreadScroll(tab);
                }
            }
        }
        renderTabs();
        mainHandler.post(() -> hydrateRestoredTabs(order, position + 1));
    }

    private void openInTab(CuspTab tab, String url, boolean addHistory) {
        int oldIndex = currentIndex;
        int index = tabs.indexOf(tab);
        if (index < 0) {
            return;
        }
        currentIndex = index;
        openInCurrentTab(url, addHistory);
        currentIndex = Math.max(0, Math.min(oldIndex, tabs.size() - 1));
        if (currentIndex != index && contentFrame.getChildCount() > 0 && currentTab() != null && currentTab().readerView != null) {
            switchToTab(currentIndex);
        }
    }

    private void saveTabs() {
        saveTabs(false);
    }

    private void saveTabs(boolean synchronous) {
        try {
            CuspTab current = currentTab();
            if (current != null) {
                rememberThreadScroll(current);
            }
            JSONArray array = new JSONArray();
            int savedCurrent = 0;
            int savedIndex = 0;
            for (CuspTab tab : tabs) {
                if (tab.privateBrowsing) {
                    continue;
                }
                JSONObject item = new JSONObject();
                item.put("url", tab.url == null ? "" : tab.url);
                item.put("title", tab.title == null ? "Tab" : tab.title);
                item.put("privateBrowsing", false);
                item.put("bookmarkOverviewTab", tab.bookmarkOverviewTab);
                item.put("nativeKind", tab.nativeKind == null ? JSONObject.NULL : tab.nativeKind);
                item.put("threadScrollRatio", tab.threadScrollRatio);
                item.put("threadBottomOffset", tab.threadBottomOffset);
                item.put("threadScrollUrl", tab.threadScrollUrl == null ? threadUrl(tab) : tab.threadScrollUrl);
                item.put("hasSavedThreadScroll", tab.hasSavedThreadScroll);
                item.put("knownMaxPostNumber", tab.knownMaxPostNumber);
                item.put("knownPostCount", tab.knownPostCount);
                item.put("cachedUnreadCount", tab.cachedUnreadCount);
                item.put("hasThreadStats", tab.hasThreadStats);
                item.put("knownThreadArchived", tab.knownThreadArchived);
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
                if (tab == current) {
                    savedCurrent = savedIndex;
                }
                savedIndex++;
            }
            JSONObject root = new JSONObject();
            root.put("current", Math.max(0, savedCurrent));
            root.put("tabs", array);
            SharedPreferences.Editor editor = preferences.edit().putString(PREF_TABS, root.toString());
            if (synchronous) {
                editor.commit();
            } else {
                editor.apply();
            }
        } catch (Exception ignored) {
        }
    }

    private void requestSaveTabsSoon() {
        if (saveTabsTask != null) {
            return;
        }
        saveTabsTask = () -> {
            saveTabsTask = null;
            saveTabs(false);
        };
        mainHandler.postDelayed(saveTabsTask, 500);
    }

    private void scheduleTabUnload() {
        if (unloadTabsTask != null) {
            return;
        }
        unloadTabsTask = () -> {
            unloadTabsTask = null;
            unloadIdleTabs();
            scheduleTabUnload();
        };
        mainHandler.postDelayed(unloadTabsTask, TAB_UNLOAD_INTERVAL_MS);
    }

    private void unloadIdleTabs() {
        if (tabs.isEmpty() || pendingNewTab) {
            return;
        }
        long now = android.os.SystemClock.uptimeMillis();
        long unloadAfter = tabs.size() > 8 ? TAB_UNLOAD_AFTER_MANY_TABS_MS : TAB_UNLOAD_AFTER_MS;
        for (int i = 0; i < tabs.size(); i++) {
            if (!tabOverviewVisible && i == currentIndex) {
                continue;
            }
            CuspTab tab = tabs.get(i);
            if (tab.readerView == null || now - tab.lastActivatedAt < unloadAfter) {
                continue;
            }
            unloadTabView(tab);
        }
        trimBackgroundTabViews();
        trimBackgroundPageData();
    }

    private void trimBackgroundTabViews() {
        while (backgroundTabViewCount() > MAX_BACKGROUND_TAB_VIEWS) {
            CuspTab oldest = null;
            for (int i = 0; i < tabs.size(); i++) {
                if (!tabOverviewVisible && i == currentIndex) {
                    continue;
                }
                CuspTab tab = tabs.get(i);
                if (tab.readerView == null) {
                    continue;
                }
                if (oldest == null || tab.lastActivatedAt < oldest.lastActivatedAt) {
                    oldest = tab;
                }
            }
            if (oldest == null) {
                return;
            }
            unloadTabView(oldest);
        }
    }

    private int backgroundTabViewCount() {
        int count = 0;
        for (int i = 0; i < tabs.size(); i++) {
            if ((tabOverviewVisible || i != currentIndex) && tabs.get(i).readerView != null) {
                count++;
            }
        }
        return count;
    }

    private void trimBackgroundPageData() {
        while (backgroundPageDataCount() > MAX_BACKGROUND_PAGE_DATA) {
            CuspTab oldest = null;
            for (int i = 0; i < tabs.size(); i++) {
                if (!tabOverviewVisible && i == currentIndex) {
                    continue;
                }
                CuspTab tab = tabs.get(i);
                if (!hasUnloadablePageData(tab)) {
                    continue;
                }
                if (oldest == null || tab.lastActivatedAt < oldest.lastActivatedAt) {
                    oldest = tab;
                }
            }
            if (oldest == null) {
                return;
            }
            unloadTabPageData(oldest);
        }
    }

    private int backgroundPageDataCount() {
        int count = 0;
        for (int i = 0; i < tabs.size(); i++) {
            if ((tabOverviewVisible || i != currentIndex) && hasUnloadablePageData(tabs.get(i))) {
                count++;
            }
        }
        return count;
    }

    private boolean hasUnloadablePageData(CuspTab tab) {
        return tab != null && (tab.threadPage != null || tab.searchPage != null);
    }

    private void unloadTabView(CuspTab tab) {
        if (tab == null || tab.readerView == null) {
            return;
        }
        rememberThreadScroll(tab);
        ViewGroup parent = (ViewGroup) tab.readerView.getParent();
        if (parent != null) {
            parent.removeView(tab.readerView);
        }
        if (tab.threadScrollChromeTask != null) {
            mainHandler.removeCallbacks(tab.threadScrollChromeTask);
            tab.threadScrollChromeTask = null;
        }
        tab.threadScrollChromeFrames = 0;
        tab.readerView = null;
        tab.threadScroll = null;
        tab.threadList = null;
        tab.threadBottomLoader = null;
        tab.scrollScrubber = null;
        tab.unreadMarkerLayer = null;
        tab.postViews = null;
        tab.postSlots = null;
        tab.renderedPostSlots = null;
    }

    private void unloadTabPageData(CuspTab tab) {
        if (tab == null) {
            return;
        }
        if (tab.threadPage != null) {
            updateTabThreadStats(tab, tab.threadPage);
            if (!isPrivateTab(tab)) {
                cacheThreadPage(tab.threadPage);
            }
            tab.threadPage = null;
        }
        tab.searchPage = null;
        tab.savedThreadPageJson = null;
        tab.savedSearchPageJson = null;
    }

    private JSONObject threadPageToJson(ThreadPage page) throws Exception {
        JSONObject object = new JSONObject();
        object.put("url", page.url);
        object.put("title", page.title);
        object.put("archived", page.archived);
        object.put("datUrl", page.datUrl == null ? "" : page.datUrl);
        object.put("datByteLength", page.datByteLength);
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
        page.archived = object.optBoolean("archived", false);
        page.datUrl = object.optString("datUrl", "");
        page.datByteLength = object.optLong("datByteLength", 0);
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

    private void cacheThreadPage(ThreadPage page) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ioExecutor.execute(() -> writeThreadPageCache(page));
        } else {
            writeThreadPageCache(page);
        }
    }

    private void writeThreadPageCache(ThreadPage page) {
        if (page == null || page.url == null || page.url.isEmpty() || page.error != null || page.posts.isEmpty()) {
            return;
        }
        if (!AppCache.enabled(preferences)) {
            return;
        }
        try {
            File dir = threadCacheDir();
            if (!dir.exists() && !dir.mkdirs()) {
                return;
            }
            byte[] bytes = threadPageToJson(page).toString().getBytes(Charset.forName("UTF-8"));
            if (!AppCache.canWrite(this, preferences, bytes.length)) {
                return;
            }
            try (FileOutputStream out = new FileOutputStream(threadCacheFile(page.url))) {
                out.write(bytes);
            }
        } catch (Exception ignored) {
        }
    }

    private ThreadPage readCachedThreadPage(String url) {
        if (url == null || url.isEmpty() || !AppCache.enabled(preferences)) {
            return null;
        }
        try {
            File file = threadCacheFile(url);
            if (!file.exists() || file.length() <= 0) {
                return null;
            }
            file.setLastModified(System.currentTimeMillis());
            return threadPageFromJson(new JSONObject(new String(readFileBytes(file), Charset.forName("UTF-8"))));
        } catch (Exception ignored) {
            return null;
        }
    }

    private File threadCacheDir() {
        return new File(getCacheDir(), "threads");
    }

    private File threadCacheFile(String url) throws Exception {
        return new File(threadCacheDir(), cacheKey(url) + ".json");
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
            item.put("category", result.category == null ? "" : result.category);
            item.put("responses", result.responses);
            item.put("velocity", result.velocity);
            item.put("boardOrder", result.boardOrder);
            if (result.priorityMatch != null) {
                JSONObject priority = new JSONObject();
                priority.put("value", result.priorityMatch.value);
                priority.put("regex", result.priorityMatch.regex);
                item.put("priorityMatch", priority);
            }
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
                result.category = item.optString("category", "");
                result.responses = item.optInt("responses", 0);
                result.velocity = item.optDouble("velocity", 0d);
                result.boardOrder = item.optInt("boardOrder", i);
                Object priority = item.opt("priorityMatch");
                if (priority instanceof JSONObject) {
                    JSONObject priorityObject = (JSONObject) priority;
                    String value = priorityObject.optString("value", "").trim();
                    if (!value.isEmpty()) {
                        result.priorityMatch = new BoardPriorityMatch(value, priorityObject.optBoolean("regex", false));
                    }
                } else if (priority instanceof String) {
                    String value = ((String) priority).trim();
                    if (!value.isEmpty()) {
                        result.priorityMatch = new BoardPriorityMatch(value, false);
                    }
                }
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
        CuspTab target = tabs.get(index);
        if (previous != null && previous != target) {
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
        CuspTab tab = target;
        tab.lastActivatedAt = android.os.SystemClock.uptimeMillis();
        ensureTabViewLoaded(tab);
        contentFrame.setBackgroundColor(bgColor());
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
            if (NATIVE_THREAD.equals(tab.nativeKind) && tab.threadPage != null && !tab.threadPage.posts.isEmpty()) {
                if (shouldRestoreThreadScroll(tab)) {
                    tab.readerView.setVisibility(View.INVISIBLE);
                }
                restoreThreadScroll(tab);
            }
        }
        addressBar.setText(tab.url == null ? "" : tab.url);
        updateBottomThreadBar(tab);
        updateThreadSearchBar(tab);
        renderTabs();
        scheduleThreadScrollChromeRefresh(tab, 5);
        scheduleTabUnload();
        trimBackgroundTabViews();
    }

    private void ensureTabViewLoaded(CuspTab tab) {
        if (tab == null || tab.readerView != null) {
            return;
        }
        if (NATIVE_THREAD.equals(tab.nativeKind)) {
            if (tab.savedThreadPageJson != null) {
                tab.threadPage = threadPageFromJson(tab.savedThreadPageJson);
                tab.savedThreadPageJson = null;
            }
            if ((tab.threadPage == null || tab.threadPage.posts.isEmpty())
                    && tab.url != null && !tab.url.isEmpty()) {
                ThreadPage cached = readCachedThreadPage(tab.url);
                if (cached != null && cached.error == null && !cached.posts.isEmpty()) {
                    tab.threadPage = cached;
                }
            }
            if (tab.threadPage != null && !tab.threadPage.posts.isEmpty()) {
                tab.readerMode = true;
                tab.readPostNumber = readPostNumberForTab(tab, tab.threadPage.url);
                updateTabThreadStats(tab, tab.threadPage);
                tab.postViews = new LinkedHashMap<>();
                tab.readerView = buildThreadView(tab.threadPage, tab);
                return;
            }
        } else if (NATIVE_SEARCH.equals(tab.nativeKind) || NATIVE_BOARD.equals(tab.nativeKind)) {
            if (tab.savedSearchPageJson != null) {
                tab.searchPage = searchPageFromJson(tab.savedSearchPageJson);
                tab.savedSearchPageJson = null;
            }
            if (tab.searchPage != null) {
                tab.readerMode = true;
                tab.readerView = buildSearchView(tab.searchPage);
                return;
            }
        } else if (NATIVE_SEARCH_HOME.equals(tab.nativeKind) || tab.url == null || tab.url.isEmpty()) {
            tab.readerMode = true;
            tab.nativeKind = NATIVE_SEARCH_HOME;
            tab.readerView = buildSearchHomeView(true);
            return;
        }
        tab.readerView = loadingView("");
        if (tab.url != null && !tab.url.isEmpty()) {
            mainHandler.post(() -> openInTab(tab, tab.url, false));
        }
    }

    private void updateBottomThreadBar(CuspTab tab) {
        if (bottomThreadBar == null || bottomThreadTitle == null || bottomWriteButton == null || bottomBookmarkButton == null) {
            return;
        }
        if (tabOverviewVisible) {
            bottomThreadBar.setVisibility(View.GONE);
        } else if (pendingNewTab) {
            bottomThreadTitle.setText(pendingNewTabTitle());
            bottomThreadTitle.setOnClickListener(null);
            bottomThreadTitle.setClickable(false);
            bottomWriteButton.setVisibility(View.GONE);
            bottomBookmarkButton.setVisibility(View.GONE);
            bottomThreadBar.setVisibility(View.VISIBLE);
        } else if (tab != null) {
            String title = tab.threadPage != null && tab.threadPage.title != null ? tab.threadPage.title : tab.title;
            if (tab.threadPage != null) {
                setThreadTitleText(bottomThreadTitle, tab.threadPage, title);
            } else {
                bottomThreadTitle.setText(title == null || title.trim().isEmpty() ? text("\u30bf\u30d6", "Tab") : title);
            }
            boolean canWrite = NATIVE_THREAD.equals(tab.nativeKind) && tab.threadPage != null && tab.threadPage.error == null;
            bottomThreadTitle.setOnClickListener(canWrite ? v -> scrollCurrentThreadToBottom() : null);
            bottomThreadTitle.setClickable(canWrite);
            bottomWriteButton.setVisibility(canWrite ? View.VISIBLE : View.GONE);
            bottomBookmarkButton.setVisibility(View.GONE);
            bottomBookmarkButton.setOnClickListener(null);
            bottomThreadBar.setVisibility(View.VISIBLE);
        } else {
            bottomThreadBar.setVisibility(View.GONE);
        }
    }

    private String pendingNewTabTitle() {
        String page = newTabNavigationIndex >= 0 && newTabNavigationIndex < newTabNavigationHistory.size()
                ? newTabNavigationHistory.get(newTabNavigationIndex) : "";
        return pendingNewTabPageTitle(page);
    }

    private String pendingNewTabPageTitle(String page) {
        if ("5ch".equals(page)) {
            return text("5ch\u677f\u4e00\u89a7", "5ch boards");
        }
        if ("history".equals(page)) {
            return text("\u5c65\u6b74", "History");
        }
        if (page != null && page.startsWith("saved:")) {
            SavedPage savedPage = savedPageFromToken(page.substring("saved:".length()));
            return savedListTitle(savedPage.key, savedPage.folder);
        }
        if (page != null && page.startsWith("bbs-category:")) {
            BbsCategoryRequest request = decodeBbsCategoryToken(page.substring("bbs-category:".length()));
            if (request.category != null && !request.category.trim().isEmpty()) {
                return request.category;
            }
            return text("5ch\u677f\u4e00\u89a7", "5ch boards");
        }
        if (page != null && page.startsWith("5ch-category:")) {
            String category = decodeNewTabToken(page.substring("5ch-category:".length()));
            if (!category.trim().isEmpty()) {
                return category;
            }
        }
        return text("\u65b0\u898f\u30bf\u30d6", "New tab");
    }

    private void setThreadTitleText(TextView view, ThreadPage page, String fallback) {
        String title = page != null && page.title != null && !page.title.trim().isEmpty()
                ? page.title : (fallback == null || fallback.trim().isEmpty() ? text("\u30bf\u30d6", "Tab") : fallback);
        setThreadTitleText(view, title, page != null && page.archived);
    }

    private void setThreadTitleText(TextView view, String title, boolean archived) {
        title = title == null || title.trim().isEmpty() ? text("\u30bf\u30d6", "Tab") : title.trim();
        if (!archived) {
            view.setText(title);
            return;
        }
        String badge = "  " + text("DAT\u843d\u3061", "Archived");
        SpannableStringBuilder builder = new SpannableStringBuilder(title).append(badge);
        int start = builder.length() - badge.trim().length();
        int end = builder.length();
        builder.setSpan(new BackgroundColorSpan(Theme.linkHighlight(this)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(Color.rgb(29, 78, 216)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(builder);
    }

    private void renderTabs() {
        updatePrivateChrome();
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
            openPendingNewTabUrl(url);
            return;
        }
        openInCurrentTab(url);
    }

    private void openInCurrentTab(String url) {
        openInCurrentTab(url, true);
    }

    private void openInCurrentTab(String url, boolean addHistory) {
        openInCurrentTab(url, addHistory, false);
    }

    private void openInCurrentTab(String url, boolean addHistory, boolean bookmarkOverviewTab) {
        if (pendingNewTab) {
            openPendingNewTabUrl(url);
            return;
        }
        CuspTab tab = currentTab();
        if (tab == null) {
            createTab(url, true);
            return;
        }
        tab.bookmarkOverviewTab = bookmarkOverviewTab;
        if (isInternalPageUrl(url)) {
            if (addHistory) {
                recordNavigation(tab, url);
            }
            loadInternalPage(tab, url);
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
        if (isBbsDirectoryUrl(url)) {
            if (addHistory) {
                recordNavigation(tab, url);
            }
            loadBbsDirectory(tab, url);
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

    private boolean isInternalPageUrl(String url) {
        return url != null && url.startsWith(INTERNAL_URL_PREFIX);
    }

    private String savedPageUrl(String key) {
        return savedPageUrl(key, "");
    }

    private String savedPageUrl(String key, String folder) {
        return INTERNAL_URL_PREFIX + "saved/" + encodeNewTabToken(savedPageToken(key, folder));
    }

    private String historyPageUrl() {
        return INTERNAL_URL_PREFIX + "history";
    }

    private String newTabReturnPageUrl() {
        if (!pendingNewTab || newTabNavigationIndex < 0 || newTabNavigationIndex >= newTabNavigationHistory.size()) {
            return null;
        }
        if (newTabNavigationIndex > 0) {
            return INTERNAL_URL_PREFIX + "newtab-history/" + encodeNewTabToken(newTabHistorySnapshot());
        }
        return null;
    }

    private String newTabHistorySnapshot() {
        try {
            JSONObject root = new JSONObject();
            JSONArray pages = new JSONArray();
            for (String page : newTabNavigationHistory) {
                pages.put(page == null ? "" : page);
            }
            root.put("pages", pages);
            root.put("index", newTabNavigationIndex);
            return root.toString();
        } catch (Exception error) {
            return "";
        }
    }

    private String internalPageTitle(String url) {
        if (url == null) {
            return text("\u30bf\u30d6", "Tab");
        }
        if (url.startsWith(INTERNAL_URL_PREFIX + "saved/")) {
            SavedPage savedPage = savedPageFromToken(decodeNewTabToken(url.substring((INTERNAL_URL_PREFIX + "saved/").length())));
            return savedListTitle(savedPage.key, savedPage.folder);
        }
        if (url.equals(historyPageUrl())) {
            return text("\u5c65\u6b74", "History");
        }
        if (url.startsWith(INTERNAL_URL_PREFIX + "newtab-history/")) {
            List<String> pages = decodeNewTabHistoryPages(url);
            int index = decodeNewTabHistoryIndex(url, pages);
            return pendingNewTabPageTitle(pages.isEmpty() ? "home" : pages.get(Math.max(0, Math.min(index, pages.size() - 1))));
        }
        return text("\u30bf\u30d6", "Tab");
    }

    private void loadInternalPage(CuspTab tab, String url) {
        if (tab == null || url == null) {
            return;
        }
        tab.readerMode = true;
        tab.url = url;
        tab.title = internalPageTitle(url);
        tab.threadPage = null;
        tab.searchPage = null;
        tab.threadScroll = null;
        tab.postViews = null;
        if (url.startsWith(INTERNAL_URL_PREFIX + "saved/")) {
            tab.nativeKind = NATIVE_SAVED;
            SavedPage savedPage = savedPageFromToken(decodeNewTabToken(url.substring((INTERNAL_URL_PREFIX + "saved/").length())));
            tab.readerView = buildSavedItemsView(savedPage.key, savedPage.folder);
        } else if (url.equals(historyPageUrl())) {
            tab.nativeKind = NATIVE_HISTORY;
            tab.readerView = buildHistoryView();
        } else {
            tab.nativeKind = NATIVE_SEARCH_HOME;
            tab.readerView = buildSearchHomeView(true);
        }
        if (tab == currentTab() && !tabOverviewVisible) {
            contentFrame.removeAllViews();
            contentFrame.addView(tab.readerView);
        }
        renderTabs();
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
        requestSaveTabsSoon();
    }

    private void openPendingNewTabUrl(String url) {
        boolean privateBrowsing = pendingPrivateNewTab;
        String returnUrl = newTabReturnPageUrl();
        pendingNewTab = false;
        pendingPrivateNewTab = false;
        if (returnUrl == null) {
            createTab(url, true, -1, true, privateBrowsing);
            return;
        }
        CuspTab tab = new CuspTab();
        tab.title = text("\u65b0\u898f\u30bf\u30d6", "New tab");
        tab.url = "";
        tab.privateBrowsing = privateBrowsing;
        tab.backToNewTab = false;
        tab.lastActivatedAt = android.os.SystemClock.uptimeMillis();
        tab.navigationHistory.add(returnUrl);
        tab.navigationIndex = 0;
        tabs.add(tab);
        switchToTab(tabs.size() - 1);
        openInCurrentTab(normalizeUrl(url));
        renderTabs();
    }

    private void loadThread(CuspTab tab, String url) {
        loadThread(tab, url, true);
    }

    private void loadThread(CuspTab tab, String url, boolean showFullLoading) {
        final String loadUrl = url;
        rememberThreadScroll(tab);
        if (showFullLoading) {
            prepareChromeForLoading();
        }
        boolean keepExistingScroll = tab.hasSavedThreadScroll && loadUrl.equals(tab.threadScrollUrl);
        tab.readerMode = true;
        tab.nativeKind = NATIVE_THREAD;
        tab.url = loadUrl;
        tab.title = hostTitle(loadUrl);
        tab.searchPage = null;
        if (showFullLoading || tab.readerView == null) {
            tab.readerView = loadingView("");
            switchToTab(tabs.indexOf(tab));
        }
        progressBar.setVisibility(View.VISIBLE);
        mainHandler.post(() -> loadThreadAfterLoading(tab, loadUrl, keepExistingScroll, showFullLoading));
    }

    private void loadThreadAfterLoading(CuspTab tab, String loadUrl, boolean keepExistingScroll,
                                        boolean showFullLoading) {
        if (tab == null || !loadUrl.equals(tab.url)) {
            return;
        }
        ThreadPage cached = readCachedThreadPage(loadUrl);
        if (cached != null && cached.error == null && !cached.posts.isEmpty()) {
            tab.title = cached.title;
            tab.threadPage = cached;
            tab.readPostNumber = readPostNumberForTab(tab, cached.url);
            updateTabThreadStats(tab, cached);
            tab.postViews = new LinkedHashMap<>();
            tab.readerView = buildThreadView(cached, tab);
            if (!tabOverviewVisible && (showFullLoading || tab == currentTab())) {
                switchToTab(tabs.indexOf(tab));
                restoreThreadScroll(tab);
            }
        }

        ioExecutor.execute(() -> {
            ThreadPage page;
            try {
                page = downloadThreadPage(loadUrl);
            } catch (Exception error) {
                page = ThreadPage.error(loadUrl, error.getMessage());
            }
                ThreadPage result = page;
            runOnUiThread(() -> {
                if (!loadUrl.equals(tab.url)) {
                    if (tab == currentTab()) {
                        progressBar.setVisibility(View.GONE);
                    }
                    return;
                }
                if (cached != null && cached.error == null && result.error == null
                        && sameRenderedThread(cached, result)
                        && tab.readerView != null && tab.threadPage == cached) {
                    tab.title = result.title;
                    applyThreadPageMetadata(cached, result);
                    tab.threadPage = cached;
                    tab.readPostNumber = readPostNumberForTab(tab, result.url);
                    updateTabThreadStats(tab, cached);
                    cacheThreadPage(result);
                    addThreadHistory(tab, result.url, result.title);
                    if (!tab.threadRendering) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (tab == currentTab()) {
                        restoreThreadScroll(tab);
                    }
                    renderTabs();
                    return;
                }
                if (result.error != null) {
                    Toast.makeText(this, friendlyThreadLoadError(result.error), Toast.LENGTH_SHORT).show();
                    if (cached != null && cached.error == null && !cached.posts.isEmpty()) {
                        tab.title = cached.title;
                        tab.threadPage = cached;
                        updateTabThreadStats(tab, cached);
                        if (!tab.threadRendering) {
                            progressBar.setVisibility(View.GONE);
                        }
                        renderTabs();
                        return;
                    }
                    if (tab.threadPage != null && tab.threadPage.error == null && !tab.threadPage.posts.isEmpty()) {
                        if (!tab.threadRendering) {
                            progressBar.setVisibility(View.GONE);
                        }
                        renderTabs();
                        return;
                    }
                    result.error = text("\u30ad\u30e3\u30c3\u30b7\u30e5\u304c\u306a\u304f\u3001\u30b9\u30ec\u3092\u8aad\u307f\u8fbc\u3081\u307e\u305b\u3093\u3067\u3057\u305f\u3002", "Could not load the thread and no cache is available.");
                }
                tab.title = result.title;
                tab.threadPage = result;
                tab.readPostNumber = readPostNumberForTab(tab, result.url);
                updateTabThreadStats(tab, result);
                tab.postViews = new LinkedHashMap<>();
                tab.readerView = buildThreadView(result, tab);
                tab.hasSavedThreadScroll = keepExistingScroll;
                if (result.error == null && !result.posts.isEmpty()) {
                    cacheThreadPage(result);
                    addThreadHistory(tab, result.url, result.title);
                }
                if (result.error != null || result.posts.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                }
                if (tab == currentTab() && !tabOverviewVisible) {
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

    private boolean sameRenderedThread(ThreadPage a, ThreadPage b) {
        if (a == null || b == null || a.posts.size() != b.posts.size()) {
            return false;
        }
        if (!TextUtils.equals(a.title, b.title)) {
            return false;
        }
        if (a.archived != b.archived) {
            return false;
        }
        if (a.posts.isEmpty()) {
            return true;
        }
        Post lastA = a.posts.get(a.posts.size() - 1);
        Post lastB = b.posts.get(b.posts.size() - 1);
        return lastA.number == lastB.number
                && TextUtils.equals(lastA.body, lastB.body)
                && TextUtils.equals(lastA.date, lastB.date)
                && TextUtils.equals(lastA.id(), lastB.id());
    }

    private void updateThreadTitleHeader(CuspTab tab, ThreadPage page) {
        if (tab == null || page == null || tab.threadList == null || tab.threadList.getChildCount() == 0) {
            return;
        }
        View title = tab.threadList.getChildAt(0);
        if (title instanceof TextView) {
            setThreadTitleText((TextView) title, page, page.title);
        }
    }

    private void refreshThreadFromBottom(CuspTab tab) {
        refreshThreadFromBottom(tab, false, false);
    }

    private void refreshThreadFromBottom(CuspTab tab, boolean forceScrollToBottom) {
        refreshThreadFromBottom(tab, forceScrollToBottom, false);
    }

    private void refreshThreadFromBottom(CuspTab tab, boolean forceScrollToBottom, boolean centerSpinner) {
        refreshThreadFromBottom(tab, forceScrollToBottom, centerSpinner, true, null);
    }

    private void refreshThreadFromBottom(CuspTab tab, boolean forceScrollToBottom, boolean centerSpinner,
                                         boolean markReadWhenNoNewPosts, Runnable onComplete) {
        if (tab == null || tab.url == null || tab.url.isEmpty()) {
            if (centerSpinner) {
                hideCenterSpinner();
            }
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        if (centerSpinner) {
            showCenterSpinner();
        }
        ioExecutor.execute(() -> {
            ThreadPage page;
            boolean partialUpdate = false;
            try {
                ThreadPage partialPage = null;
                try {
                    partialPage = downloadNewDatPosts(tab);
                } catch (Exception ignored) {
                }
                if (partialPage != null) {
                    page = partialPage;
                    partialUpdate = true;
                } else {
                    page = downloadDatThread(tab.url);
                    if (page == null) {
                        String html = download(tab.url);
                        page = parseThread(tab.url, html);
                    }
                }
            } catch (Exception error) {
                page = ThreadPage.error(tab.url, error.getMessage());
            }
            ThreadPage result = page;
            boolean wasPartialUpdate = partialUpdate;
            runOnUiThread(() -> {
                if (centerSpinner) {
                    hideCenterSpinner();
                }
                if ((tab.threadPage != null && tab.threadPage.archived) || tab.knownThreadArchived) {
                    result.archived = true;
                }
                if (tab.threadBottomLoader != null) {
                    resetBottomRefreshLoader(tab.threadBottomLoader);
                }
                if (result.error != null) {
                    Toast.makeText(this, friendlyThreadLoadError(result.error), Toast.LENGTH_SHORT).show();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    return;
                }
                int oldCount = wasPartialUpdate
                        ? Math.max(0, result.posts.size() - result.newPostCount)
                        : (tab.threadPage == null ? 0 : tab.threadPage.posts.size());
                if (oldCount <= 0 || tab.threadList == null || tab.postViews == null) {
                    tab.title = result.title;
                    tab.threadPage = result;
                    updateThreadTitleHeader(tab, result);
                    tab.readPostNumber = readPostNumberForTab(tab, result.url);
                    updateTabThreadStats(tab, result);
                    tab.postViews = new LinkedHashMap<>();
                    tab.readerView = buildThreadView(result, tab);
                    cacheThreadPage(result);
                    if (tab == currentTab()) {
                        switchToTab(currentIndex);
                        if (tab.threadSearchOpen && tab.threadSearchQuery != null && !tab.threadSearchQuery.trim().isEmpty()) {
                            updateThreadSearch(tab.threadSearchQuery, false);
                        }
                        if (forceScrollToBottom) {
                            scrollCurrentThreadToBottom();
                        }
                    }
                    renderTabs();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    return;
                }
                if (result.posts.size() <= oldCount) {
                    tab.title = result.title;
                    tab.threadPage = result;
                    updateThreadTitleHeader(tab, result);
                    cacheThreadPage(result);
                    tab.readPostNumber = Math.max(tab.readPostNumber, readPostNumberForTab(tab, result.url));
                    updateTabThreadStats(tab, result);
                    if (markReadWhenNoNewPosts && !centerSpinner && !forceScrollToBottom) {
                        markReadTo(tab, maxPostNumber(result), false);
                    }
                    renderTabs();
                    if (tab == currentTab()) {
                        updateBottomThreadBar(tab);
                    }
                    if (tab == currentTab() && tab.threadSearchOpen
                            && tab.threadSearchQuery != null && !tab.threadSearchQuery.trim().isEmpty()) {
                        updateThreadSearch(tab.threadSearchQuery, false);
                    }
                    if (forceScrollToBottom) {
                        scrollCurrentThreadToBottom();
                    }
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    return;
                }
                tab.threadPage = result;
                updateThreadTitleHeader(tab, result);
                tab.readPostNumber = Math.max(tab.readPostNumber, readPostNumberForTab(tab, result.url));
                updateTabThreadStats(tab, result);
                markReadTo(tab, lastExistingPostNumber(result, oldCount), false);
                tab.title = result.title;
                if (result.error == null && !result.posts.isEmpty()) {
                    cacheThreadPage(result);
                    addThreadHistory(tab, result.url, result.title);
                }
                renderAdditionalPostCardsIncrementally(tab.threadList, result, tab, oldCount, () -> {
                    if (tab == currentTab() && tab.threadSearchOpen
                            && tab.threadSearchQuery != null && !tab.threadSearchQuery.trim().isEmpty()) {
                        updateThreadSearch(tab.threadSearchQuery, false);
                    }
                    if (tab == currentTab() && forceScrollToBottom) {
                        scrollCurrentThreadToBottom();
                    }
                    renderTabs();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
            });
        });
    }

    private void loadSearchResults(CuspTab tab, String url) {
        loadSearchResults(tab, url, true);
    }

    private void loadSearchResults(CuspTab tab, String url, boolean foreground) {
        final String loadUrl = url;
        if (foreground) {
            prepareChromeForLoading();
        }
        tab.readerMode = true;
        tab.nativeKind = NATIVE_SEARCH;
        tab.url = loadUrl;
        tab.title = searchTitle(loadUrl);
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
                String html = download(loadUrl);
                page = parseSearchPage(loadUrl, html);
            } catch (Exception error) {
                page = SearchPage.error(loadUrl, error.getMessage());
            }
            SearchPage result = page;
            runOnUiThread(() -> {
                if (!loadUrl.equals(tab.url)) {
                    if (foreground && tab == currentTab()) {
                        progressBar.setVisibility(View.GONE);
                    }
                    return;
                }
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

    private String friendlyThreadLoadError(String detail) {
        String raw = detail == null ? "" : detail.trim();
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.contains("unable to resolve host")
                || lower.contains("no address associated with hostname")
                || lower.contains("failed to connect")
                || lower.contains("timeout")
                || lower.contains("timed out")
                || lower.contains("network is unreachable")
                || lower.contains("connection reset")) {
            return text("\u901a\u4fe1\u3067\u304d\u306a\u3044\u305f\u3081\u3001\u6700\u65b0\u306e\u30b9\u30ec\u3092\u8aad\u307f\u8fbc\u3081\u307e\u305b\u3093\u3067\u3057\u305f\u3002",
                    "Could not load the latest thread because the network is unavailable.");
        }
        return raw.isEmpty()
                ? text("\u30b9\u30ec\u3092\u8aad\u307f\u8fbc\u3081\u307e\u305b\u3093\u3067\u3057\u305f\u3002", "Could not load the thread.")
                : text("\u30b9\u30ec\u3092\u8aad\u307f\u8fbc\u3081\u307e\u305b\u3093\u3067\u3057\u305f\u3002", "Could not load the thread.");
    }

    private ThreadPage downloadThreadPage(String url) throws Exception {
        ThreadPage datPage = downloadDatThread(url);
        if (datPage != null && !datPage.posts.isEmpty()) {
            return datPage;
        }
        ThreadPage htmlPage = null;
        Exception htmlError = null;
        try {
            String html = download(url);
            htmlPage = parseThread(url, html);
            if (!htmlPage.posts.isEmpty()) {
                return htmlPage;
            }
        } catch (Exception error) {
            htmlError = error;
        }
        if (htmlPage != null) {
            return htmlPage;
        }
        throw htmlError == null ? new IllegalStateException("No posts were parsed.") : htmlError;
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
        final String loadUrl = url;
        if (foreground) {
            prepareChromeForLoading();
        }
        tab.readerMode = true;
        tab.nativeKind = NATIVE_BOARD;
        tab.url = loadUrl;
        tab.title = boardTitle(loadUrl);
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
                page = downloadBoard(loadUrl);
            } catch (Exception error) {
                page = SearchPage.error(loadUrl, error.getMessage());
            }
            SearchPage result = page;
            runOnUiThread(() -> {
                if (!loadUrl.equals(tab.url)) {
                    if (foreground && tab == currentTab()) {
                        progressBar.setVisibility(View.GONE);
                    }
                    return;
                }
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

    private void loadBbsDirectory(CuspTab tab, String url) {
        loadBbsDirectory(tab, url, true);
    }

    private void loadBbsDirectory(CuspTab tab, String url, boolean foreground) {
        final String loadUrl = url;
        if (foreground) {
            prepareChromeForLoading();
        }
        tab.readerMode = true;
        tab.nativeKind = NATIVE_BOARD;
        tab.url = loadUrl;
        tab.title = hostTitle(loadUrl);
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
                page = downloadBbsDirectory(loadUrl);
            } catch (Exception error) {
                page = SearchPage.error(loadUrl, error.getMessage());
            }
            SearchPage result = page;
            runOnUiThread(() -> {
                if (!loadUrl.equals(tab.url)) {
                    if (foreground && tab == currentTab()) {
                        progressBar.setVisibility(View.GONE);
                    }
                    return;
                }
                tab.title = result.title;
                tab.searchPage = result;
                tab.readerView = isBbsMenuUrl(loadUrl) ? buildBbsCategoryIndexView(result) : buildSearchView(result);
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
        text.setTextColor(textColor());
        text.setTextSize(16);
        text.setPadding(0, dp(10), 0, 0);
        if (message != null && !message.isEmpty()) {
            box.addView(text);
        }
        return box;
    }

    private View buildThreadView(ThreadPage page, CuspTab tab) {
        cancelThreadChunkRender(tab);
        ScrollView scroll = new ScrollView(this);
        tab.threadScroll = scroll;
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.getViewTreeObserver().addOnScrollChangedListener(this::scheduleThreadMediaLoads);
        scroll.setOnClickListener(v -> dismissTopReplyPopup());
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(POST_OUTER_GAP_DP), dp(POST_OUTER_GAP_DP),
                dp(POST_OUTER_GAP_DP), dp(POST_OUTER_GAP_DP));
        tab.threadList = list;
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        setThreadTitleText(title, page, page.title);
        title.setTextColor(textColor());
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

        if (matchesNgThread(page.title)) {
            list.addView(postText(text("NGThread\u306b\u3088\u308a\u975e\u8868\u793a", "Hidden by NGThread."), page));
            return withScrollScrubber(scroll, tab);
        }

        renderPostCardsIncrementally(list, page, tab);

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
        frame.addView(withScrollScrubber(scroll, tab), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams loaderParams = new FrameLayout.LayoutParams(dp(66), dp(66),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        loaderParams.setMargins(0, 0, 0, dp(2));
        frame.addView(bottomLoader, loaderParams);
        return frame;
    }

    private void addPostCard(LinearLayout list, ThreadPage page, CuspTab tab, Post post, int index) {
        addPostCard(list, page, tab, post, index, 0);
    }

    private void addPostCard(LinearLayout list, ThreadPage page, CuspTab tab, Post post, int index, int depth) {
        addPostCard(list, page, tab, new PostRenderItem(post, depth, new LinkedHashSet<>(), false), index);
    }

    private void addPostCard(LinearLayout list, ThreadPage page, CuspTab tab, PostRenderItem item, int index) {
        PostCardShell postCard = createPostCardShell(page, tab, item);
        if (postCard == null) {
            return;
        }
        list.addView(postCard.shell, Math.max(0, Math.min(index, list.getChildCount())),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        tab.postViews.put(item.post.number, postCard.card);
    }

    private PostCardShell createPostCardShell(ThreadPage page, CuspTab tab, PostRenderItem item) {
        Post post = item.post;
        int depth = item.depth;
        if (matchesNgPost(post)) {
            return null;
        }
        post.aaMode = aaModeForPost(page, post);
        FrameLayout shell = new FrameLayout(this);
        shell.setClipChildren(false);
        shell.setBackgroundColor(Color.TRANSPARENT);
        ImageView readAction = swipeActionIcon(R.drawable.ic_check, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        ImageView replyAction = swipeActionIcon(R.drawable.ic_reply, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        shell.addView(readAction);
        shell.addView(replyAction);
        boolean showTreeConnector = treeViewEnabled() && (depth > 0 || item.hasReplies);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setTag(R.id.tag_post_card, true);
        boolean copyPasteOmitted = copyPasteSourcePost(page, post) != null;
        card.setPadding(dp(10), dp(8), dp(10), dp(10));
        card.setBackground(postBackground(post.number > tab.readPostNumber, isMyPost(page, post)));
        card.setOnLongClickListener(v -> {
            if (isPostSwipeBlocked(post)) {
                return true;
            }
            showPostActionMenu(card, tab, post);
            return true;
        });
        attachPostSwipe(card, card, readAction, replyAction, tab, post);
        int indentLeft = dp(Math.min(depth, 8) * 18);
        if (indentLeft > 0 && readAction.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams readActionParams = (FrameLayout.LayoutParams) readAction.getLayoutParams();
            readActionParams.leftMargin = indentLeft;
            readAction.setLayoutParams(readActionParams);
        }
        FrameLayout.LayoutParams cardFrameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardFrameParams.setMargins(indentLeft, 0, 0, dp(POST_OUTER_GAP_DP));

        if (copyPasteOmitted) {
            TextView omitted = copyPasteOmittedView(page, post, card, tab, readAction, replyAction);
            card.addView(omitted, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(36)));
            attachPostSwipeDeep(omitted, card, readAction, replyAction, tab, post);
            if (showTreeConnector) {
                shell.addView(new TreeConnectorView(this, item, dp(18), TEAL),
                        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
            }
            shell.addView(card, cardFrameParams);
            return new PostCardShell(shell, card);
        }

        View metaView = postMetaText(post, page, () -> {
            if (!isPostSwipeBlocked(post)) {
                showPostActionMenu(card, tab, post);
            }
        });
        card.addView(metaView);

        View bodyView = postBodyView(card, page, tab, post, depth);
        card.addView(bodyView);
        attachPostSwipeDeep(metaView, card, readAction, replyAction, tab, post);
        attachPostSwipeDeep(bodyView, card, readAction, replyAction, tab, post);
        if (showTreeConnector) {
            shell.addView(new TreeConnectorView(this, item, dp(18), TEAL),
                    new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
        }
        shell.addView(card, cardFrameParams);
        return new PostCardShell(shell, card);
    }

    private TextView copyPasteOmittedView(ThreadPage page, Post post, View card, CuspTab tab,
                                         View readAction, View replyAction) {
        TextView view = new TextView(this);
        view.setText(text("\u30b3\u30d4\u30da", "Copy-paste"));
        view.setTextColor(TEAL);
        view.setTextSize(15);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        view.setIncludeFontPadding(false);
        view.setPadding(0, 0, 0, 0);
        view.setOnClickListener(v -> {
            if (consumePostPopupTap(view)) {
                return;
            }
            showPostsPopup(view, page, Collections.singletonList(post), false, true);
        });
        view.setOnLongClickListener(v -> {
            if (!isPostSwipeBlocked(post)) {
                showPostActionMenu(card, tab, post);
            }
            return true;
        });
        return view;
    }

    private void renderPostCardsIncrementally(LinearLayout list, ThreadPage page, CuspTab tab) {
        if (tab.postViews == null) {
            tab.postViews = new LinkedHashMap<>();
        }
        if (tab.postSlots == null) {
            tab.postSlots = new LinkedHashMap<>();
        }
        if (tab.renderedPostSlots == null) {
            tab.renderedPostSlots = new LinkedHashSet<>();
        }
        tab.postViews.clear();
        tab.postSlots.clear();
        tab.renderedPostSlots.clear();
        List<PostRenderItem> items = treeViewEnabled()
                ? treePostRenderItemsForReadBoundary(page, tab.readPostNumber)
                : flatPostRenderItems(page);
        int generation = ++tab.threadRenderGeneration;
        tab.threadRendering = true;
        renderPostSlots(list, page, tab, items, list.getChildCount(), generation, null);
    }

    private void renderAdditionalPostCardsIncrementally(LinearLayout list, ThreadPage page, CuspTab tab,
                                                        int fromPostIndex, Runnable onComplete) {
        if (list == null || tab == null || tab.postViews == null || tab.postSlots == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        List<PostRenderItem> items = treeViewEnabled()
                ? treePostRenderItems(page, Math.max(0, fromPostIndex), tab.readPostNumber)
                : flatPostRenderItems(page, Math.max(0, fromPostIndex));
        int generation = ++tab.threadRenderGeneration;
        tab.threadRendering = true;
        renderPostSlots(list, page, tab, items, list.getChildCount(), generation, onComplete);
    }

    private void renderPostSlots(LinearLayout list, ThreadPage page, CuspTab tab, List<PostRenderItem> items,
                                 int insertIndex, int generation, Runnable onComplete) {
        if (tab.threadRenderGeneration != generation || tab.threadList != list) {
            return;
        }
        if (copyPasteOmitEnabled()) {
            ensureCopyPasteIndex(page);
        }
        for (PostRenderItem item : items) {
            VirtualPostSlot slot = new VirtualPostSlot(page, tab, item, estimatePostSlotHeight(item));
            FrameLayout holder = new FrameLayout(this);
            holder.setTag(slot);
            holder.addView(postSlotSpacer(slot.height), new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, slot.height));
            int index = Math.max(0, Math.min(insertIndex++, list.getChildCount()));
            list.addView(holder, index, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tab.postSlots.put(item.post.number, holder);
        }
        completeThreadRender(tab, onComplete);
    }

    private void completeThreadRender(CuspTab tab, Runnable onComplete) {
        tab.threadRendering = false;
        finishThreadRender(tab);
        scheduleThreadPostVisibilityRefresh(tab);
        scheduleLazyImgurLoads();
        if (tab == currentTab()) {
            visiblePostViews.clear();
            if (tab.postViews != null) {
                visiblePostViews.putAll(tab.postViews);
            }
            if (tab.threadSearchOpen && tab.threadSearchQuery != null
                    && !tab.threadSearchQuery.trim().isEmpty()) {
                updateThreadSearch(tab.threadSearchQuery, false);
            }
            restoreThreadScroll(tab);
            runPendingScrollToBottom(tab);
        }
        if (pendingScrollToBottomTab != tab) {
            tab.fastRenderToBottom = false;
        }
        if (onComplete != null) {
            onComplete.run();
        }
    }

    private void finishThreadRender(CuspTab tab) {
        refreshThreadScrollChrome(tab);
        mainHandler.postDelayed(() -> refreshThreadScrollChrome(tab), 16);
        mainHandler.postDelayed(() -> refreshThreadScrollChrome(tab), 64);
        mainHandler.postDelayed(() -> refreshThreadScrollChrome(tab), 144);
        if (tab == currentTab()) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private List<PostRenderItem> flatPostRenderItems(ThreadPage page) {
        return flatPostRenderItems(page, 0);
    }

    private List<PostRenderItem> flatPostRenderItems(ThreadPage page, int fromPostIndex) {
        List<PostRenderItem> items = new ArrayList<>();
        for (int i = Math.max(0, fromPostIndex); i < page.posts.size(); i++) {
            Post post = page.posts.get(i);
            if (!matchesNgPost(post)) {
                items.add(new PostRenderItem(post, 0, new LinkedHashSet<>(), false));
            }
        }
        return items;
    }

    private List<PostRenderItem> treePostRenderItems(ThreadPage page) {
        return treePostRenderItems(page, 0, 0);
    }

    private List<PostRenderItem> treePostRenderItemsForReadBoundary(ThreadPage page, int readPostNumber) {
        return treePostRenderItems(page, 0, readPostNumber);
    }

    private List<PostRenderItem> treePostRenderItems(ThreadPage page, int fromPostIndex) {
        return treePostRenderItems(page, fromPostIndex, 0);
    }

    private List<PostRenderItem> treePostRenderItems(ThreadPage page, int fromPostIndex, int readPostNumber) {
        List<Post> visible = new ArrayList<>();
        Set<Integer> visibleNumbers = new LinkedHashSet<>();
        for (int i = Math.max(0, fromPostIndex); i < page.posts.size(); i++) {
            Post post = page.posts.get(i);
            if (!matchesNgPost(post)) {
                visible.add(post);
                visibleNumbers.add(post.number);
            }
        }
        Map<Integer, List<Post>> children = new LinkedHashMap<>();
        List<Post> roots = new ArrayList<>();
        for (Post post : visible) {
            int parent = firstQuotedParent(post, visibleNumbers, readPostNumber);
            if (parent > 0) {
                List<Post> childList = children.get(parent);
                if (childList == null) {
                    childList = new ArrayList<>();
                    children.put(parent, childList);
                }
                childList.add(post);
            } else {
                roots.add(post);
            }
        }
        List<PostRenderItem> items = new ArrayList<>();
        Set<Integer> rendered = new LinkedHashSet<>();
        for (Post post : roots) {
            collectTreePostRenderItems(post, children, rendered, 0, new LinkedHashSet<>(), items);
        }
        for (Post post : visible) {
            if (!rendered.contains(post.number)) {
                collectTreePostRenderItems(post, children, rendered, 0, new LinkedHashSet<>(), items);
            }
        }
        return items;
    }

    private void collectTreePostRenderItems(Post post, Map<Integer, List<Post>> children,
                                            Set<Integer> rendered, int depth, Set<Integer> continuationDepths,
                                            List<PostRenderItem> items) {
        if (!rendered.add(post.number)) {
            return;
        }
        List<Post> replies = children.get(post.number);
        items.add(new PostRenderItem(post, depth, continuationDepths, replies != null && !replies.isEmpty()));
        if (replies == null) {
            return;
        }
        for (int i = 0; i < replies.size(); i++) {
            Set<Integer> childContinuations = new LinkedHashSet<>(continuationDepths);
            if (i < replies.size() - 1) {
                childContinuations.add(depth + 1);
            } else {
                childContinuations.remove(depth + 1);
            }
            collectTreePostRenderItems(replies.get(i), children, rendered, depth + 1,
                    childContinuations, items);
        }
    }

    private void addTreePostCards(LinearLayout list, ThreadPage page, CuspTab tab) {
        List<Post> visible = new ArrayList<>();
        Set<Integer> visibleNumbers = new LinkedHashSet<>();
        for (Post post : page.posts) {
            if (!matchesNgPost(post)) {
                visible.add(post);
                visibleNumbers.add(post.number);
            }
        }
        Map<Integer, List<Post>> children = new LinkedHashMap<>();
        List<Post> roots = new ArrayList<>();
        for (Post post : visible) {
            int parent = firstQuotedParent(post, visibleNumbers, tab == null ? 0 : tab.readPostNumber);
            if (parent > 0) {
                List<Post> items = children.get(parent);
                if (items == null) {
                    items = new ArrayList<>();
                    children.put(parent, items);
                }
                items.add(post);
            } else {
                roots.add(post);
            }
        }
        Set<Integer> rendered = new LinkedHashSet<>();
        for (Post post : roots) {
            addTreePostCard(list, page, tab, post, children, rendered, 0);
        }
        for (Post post : visible) {
            if (!rendered.contains(post.number)) {
                addTreePostCard(list, page, tab, post, children, rendered, 0);
            }
        }
    }

    private void addTreePostCard(LinearLayout list, ThreadPage page, CuspTab tab, Post post,
                                 Map<Integer, List<Post>> children, Set<Integer> rendered, int depth) {
        if (!rendered.add(post.number)) {
            return;
        }
        addPostCard(list, page, tab, post, list.getChildCount(), depth);
        List<Post> replies = children.get(post.number);
        if (replies == null) {
            return;
        }
        for (Post child : replies) {
            addTreePostCard(list, page, tab, child, children, rendered, depth + 1);
        }
    }

    private int firstQuotedParent(Post post, Set<Integer> visibleNumbers) {
        return firstQuotedParent(post, visibleNumbers, 0);
    }

    private int firstQuotedParent(Post post, Set<Integer> visibleNumbers, int readPostNumber) {
        if (post == null || post.body == null) {
            return 0;
        }
        boolean unreadPost = post.number > readPostNumber;
        Matcher matcher = REPLY_PATTERN.matcher(post.body);
        while (matcher.find()) {
            int from = parsePositiveInt(matcher.group(1), -1);
            int to = matcher.group(2) == null ? from : parsePositiveInt(matcher.group(2), from);
            int first = Math.min(from, to);
            int last = Math.max(from, to);
            for (int number = first; number <= last; number++) {
                if (number == 1 && skipFirstReplyInTree()) {
                    continue;
                }
                if (unreadPost && number <= readPostNumber) {
                    continue;
                }
                if (number > 0 && number < post.number && visibleNumbers.contains(number)) {
                    return number;
                }
            }
        }
        return 0;
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
        final boolean[] horizontalIntent = new boolean[1];
        final Map<View, Boolean> longClickStates = new LinkedHashMap<>();
        trigger.setOnTouchListener((v, event) -> {
            if (gesturesEnabled()) {
                return false;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (v instanceof TextView) {
                    String url = touchedUrl((TextView) v, event);
                    v.setTag(url == null ? null : new TouchedLink(url, (int) event.getRawX(), (int) event.getRawY()));
                }
                downX[0] = event.getRawX();
                downY[0] = event.getRawY();
                dragging[0] = false;
                horizontalIntent[0] = false;
                card.clearAnimation();
                return false;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = event.getRawX() - downX[0];
                float dy = event.getRawY() - downY[0];
                if (!horizontalIntent[0] && Math.abs(dx) > dp(3) && Math.abs(dx) > Math.abs(dy) * 1.05f) {
                    horizontalIntent[0] = true;
                    post.swiping = true;
                    post.lastSwipeAt = System.currentTimeMillis();
                    v.cancelLongPress();
                    card.cancelLongPress();
                    setLongClickableDeep(card, false, longClickStates);
                    requestDisallowInterceptDeep(v, true);
                    if (v instanceof TextView) {
                        v.setTag(null);
                    }
                }
                if (!dragging[0] && horizontalIntent[0] && Math.abs(dx) > dp(6)) {
                    dragging[0] = true;
                }
                if (dragging[0]) {
                    float translation = Math.max(-dp(92), Math.min(dp(92), dx * 0.55f));
                    card.setTranslationX(translation);
                    readAction.setAlpha(Math.max(0f, Math.min(1f, translation / dp(64))));
                    replyAction.setAlpha(Math.max(0f, Math.min(1f, -translation / dp(64))));
                    return true;
                }
                if (horizontalIntent[0]) {
                    return true;
                }
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (event.getAction() == MotionEvent.ACTION_CANCEL && v instanceof TextView) {
                    v.setTag(null);
                }
                if (dragging[0]) {
                    float tx = card.getTranslationX();
                    post.lastSwipeAt = System.currentTimeMillis();
                    card.animate().translationX(0).setDuration(130).start();
                    readAction.animate().alpha(0f).setDuration(130).start();
                    replyAction.animate().alpha(0f).setDuration(130).start();
                    mainHandler.postDelayed(() -> post.swiping = false, 220);
                    requestDisallowInterceptDeep(v, false);
                    restoreLongClickableStates(longClickStates);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (tx <= -dp(54)) {
                            showWriteDialog(">>" + post.number + "\n");
                        } else if (tx >= dp(54)) {
                            setReadThroughPost(tab, post);
                            card.setBackground(postBackground(false, isMyPost(tab == null ? null : tab.threadPage, post)));
                        }
                    }
                    return true;
                }
                if (horizontalIntent[0]) {
                    post.swiping = false;
                    post.lastSwipeAt = System.currentTimeMillis();
                    requestDisallowInterceptDeep(v, false);
                    restoreLongClickableStates(longClickStates);
                    return true;
                }
                post.swiping = false;
                post.lastSwipeAt = System.currentTimeMillis();
                requestDisallowInterceptDeep(v, false);
                restoreLongClickableStates(longClickStates);
            }
            return false;
        });
    }

    private void setLongClickableDeep(View view, boolean enabled, Map<View, Boolean> oldStates) {
        if (view == null || oldStates == null) {
            return;
        }
        if (!oldStates.containsKey(view)) {
            oldStates.put(view, view.isLongClickable());
        }
        view.setLongClickable(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                setLongClickableDeep(group.getChildAt(i), enabled, oldStates);
            }
        }
    }

    private void restoreLongClickableStates(Map<View, Boolean> oldStates) {
        if (oldStates == null || oldStates.isEmpty()) {
            return;
        }
        for (Map.Entry<View, Boolean> entry : oldStates.entrySet()) {
            entry.getKey().setLongClickable(entry.getValue());
        }
        oldStates.clear();
    }

    private void requestDisallowInterceptDeep(View view, boolean disallow) {
        ViewParent parent = view == null ? null : view.getParent();
        while (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallow);
            parent = parent instanceof View ? ((View) parent).getParent() : null;
        }
    }

    private boolean isPostSwipeBlocked(Post post) {
        return post != null && (post.swiping || System.currentTimeMillis() - post.lastSwipeAt < 650);
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
        int numberEnd = String.valueOf(post.number).length();
        text.setSpan(new StyleSpan(Typeface.BOLD), 0, numberEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Matcher matcher = POST_ID_PATTERN.matcher(value);
        while (matcher.find()) {
            String id = matcher.group(1);
            text.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (suppressNextLinkClick.remove(widget)) {
                        return;
                    }
                    if (consumePostPopupTap(widget)) {
                        return;
                    }
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
        meta.setTextColor(mutedColor());
        meta.setLinkTextColor(TEAL);
        meta.setTextSize(12);
        meta.setPadding(0, 0, 0, dp(5));
        meta.setMovementMethod(LinkMovementMethod.getInstance());
        meta.setOnLongClickListener(v -> {
            suppressNextLinkClick.add(v);
            mainHandler.postDelayed(() -> suppressNextLinkClick.remove(v), 1200);
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
        menu.setBackgroundColor(surfaceColor());
        menu.addView(postActionPreview(tab, post));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(menu)
                .create();

        menu.addView(dialogAction(R.drawable.ic_reply, text("\u8fd4\u4fe1", "Reply"), () -> {
            dialog.dismiss();
            showWriteDialog(">>" + post.number + "\n");
        }));
        menu.addView(dialogAction(R.drawable.ic_check, text("\u3053\u3053\u307e\u3067\u8aad\u3093\u3060", "Read to here"), () -> {
            dialog.dismiss();
            setReadThroughPost(tab, post);
        }));
        menu.addView(dialogAction(R.drawable.ic_text_fields, post.aaMode ? text("\u901a\u5e38\u8868\u793a", "Normal view") : text("AA\u8868\u793a", "AA view"), () -> {
            dialog.dismiss();
            toggleAaMode(tab, post, anchor);
        }));
        dialog.show();
        Theme.styleDialog(dialog, this);
    }

    private View postActionPreview(CuspTab tab, Post post) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(8), dp(10), dp(10));
        card.setBackground(postBackground(tab != null && post.number > tab.readPostNumber,
                isMyPost(tab == null ? null : tab.threadPage, post)));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(cardParams);

        LinearLayout metaRow = new LinearLayout(this);
        metaRow.setOrientation(LinearLayout.HORIZONTAL);
        metaRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView meta = new TextView(this);
        meta.setText(postHeaderText(post));
        meta.setTextColor(mutedColor());
        meta.setTextSize(12);
        meta.setTextIsSelectable(true);
        metaRow.addView(meta, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        ImageButton copy = iconButton(R.drawable.ic_copy, text("\u672c\u6587\u3092\u30b3\u30d4\u30fc", "Copy body"), v -> copyPost(post));
        copy.setColorFilter(TEAL);
        copy.setBackgroundColor(Color.TRANSPARENT);
        metaRow.addView(copy, new LinearLayout.LayoutParams(dp(36), dp(34)));
        card.addView(metaRow);

        boolean aa = tab != null && tab.threadPage != null && aaModeForPost(tab.threadPage, post);
        TextView body = postActionPreviewBody(tab, post, aa);
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setScrollbarFadingEnabled(true);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.addView(body, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        card.addView(scroll, scrollParams);
        if (aa) {
            int[] lastAaWidth = new int[]{0};
            body.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                int width = Math.max(0, right - left);
                if (width > 0 && width != lastAaWidth[0]) {
                    lastAaWidth[0] = width;
                    fitAaTextSize(body, post);
                }
                adjustPostActionPreviewScroll(scroll, body);
            });
            body.post(() -> {
                lastAaWidth[0] = body.getWidth();
                fitAaTextSize(body, post);
                adjustPostActionPreviewScroll(scroll, body);
            });
            body.postDelayed(() -> adjustPostActionPreviewScroll(scroll, body), 80);
        } else {
            scroll.post(() -> adjustPostActionPreviewScroll(scroll, body));
        }
        return card;
    }

    private void adjustPostActionPreviewScroll(ScrollView scroll, TextView body) {
        int maxHeight = dp(380);
        int contentHeight = body.getHeight();
        if (contentHeight <= 0) {
            body.measure(
                    View.MeasureSpec.makeMeasureSpec(Math.max(1, scroll.getWidth()), View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            contentHeight = body.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = scroll.getLayoutParams();
        if (contentHeight > maxHeight) {
            params.height = maxHeight;
            scroll.setVerticalScrollBarEnabled(true);
            scroll.setOnTouchListener(null);
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            scroll.setVerticalScrollBarEnabled(false);
            scroll.setOnTouchListener((v, event) -> event.getActionMasked() == MotionEvent.ACTION_MOVE);
        }
        scroll.setLayoutParams(params);
    }

    private TextView postActionPreviewBody(CuspTab tab, Post post, boolean aa) {
        TextView body = new TextView(this);
        body.setText(aa ? aaDisplayBody(post) : post.body);
        body.setTextColor(textColor());
        body.setTextSize(POST_TEXT_SIZE_SP);
        if (aa) {
            applyAaTypeface(body);
        } else {
            body.setTypeface(Typeface.DEFAULT);
            body.setIncludeFontPadding(true);
        }
        body.setLineSpacing(0, aa ? AA_LINE_SPACING_MULTIPLIER : 1.15f);
        body.setTextIsSelectable(true);
        body.setPadding(0, aa ? 0 : dp(4), 0, 0);
        body.setMinHeight(0);
        body.setMinimumHeight(0);
        if (aa) {
            body.setSingleLine(false);
            body.setHorizontallyScrolling(true);
        }
        return body;
    }

    private int previewBodyHeight(Post post) {
        int lines = Math.max(1, (post.body == null ? "" : post.body).split("\\n", -1).length);
        return dp(20 + Math.min(lines, 12) * 22);
    }

    private String postHeaderText(Post post) {
        return post.number + "  " + post.name + "  " + post.date;
    }

    private String postCopyText(Post post) {
        return post == null || post.body == null ? "" : post.body;
    }

    private void copyPost(Post post) {
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            manager.setPrimaryClip(ClipData.newPlainText("CuspiDroid post", postCopyText(post)));
            Toast.makeText(this, text("\u30b3\u30d4\u30fc\u3057\u307e\u3057\u305f", "Copied."), Toast.LENGTH_SHORT).show();
        }
    }

    private View dialogAction(int iconRes, String label, Runnable action) {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.HORIZONTAL);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(dp(12), 0, dp(12), 0);
        view.setBackground(roundedDrawable(postColor(), borderColor(), dp(8)));
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
        textView.setTextColor(textColor());
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
        body.setText(postCopyText(post));
        body.setTextColor(textColor());
        body.setTextSize(15);
        body.setTextIsSelectable(true);
        body.setPadding(dp(10), dp(10), dp(10), dp(10));
        ScrollView scroll = new ScrollView(this);
        scroll.addView(body, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(320)));
        Button copyAll = new Button(this);
        copyAll.setText(text("\u672c\u6587\u3092\u30b3\u30d4\u30fc", "Copy body"));
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
                manager.setPrimaryClip(ClipData.newPlainText("CuspiDroid post", postCopyText(post)));
                Toast.makeText(this, text("\u30b3\u30d4\u30fc\u3057\u307e\u3057\u305f", "Copied."), Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private boolean matchesNgPost(Post post) {
        if (post == null) {
            return false;
        }
        NgRules rules = ngRules();
        if (post.cachedNgRulesKey != null && post.cachedNgRulesKey.equals(cachedNgRulesKey)) {
            return post.cachedNgMatch;
        }
        boolean match = rules.matches("NGWord", post.body)
                || rules.matches("NGName", post.name)
                || rules.matches("NGID", post.id())
                || rules.matches("NGBe", post.be());
        post.cachedNgRulesKey = cachedNgRulesKey;
        post.cachedNgMatch = match;
        return match;
    }

    private boolean matchesNgThread(String title) {
        return ngRules().matches("NGThread", title);
    }

    private NgRules ngRules() {
        String rulesJson = preferences.getString(PREF_NG_RULES, "{}");
        String legacyWords = preferences.getString(PREF_NG_WORDS, "");
        String key = rulesJson + "\n" + legacyWords;
        if (cachedNgRules != null && key.equals(cachedNgRulesKey)) {
            return cachedNgRules;
        }
        NgRules rules = new NgRules();
        try {
            JSONObject root = new JSONObject(rulesJson);
            for (String category : NgRules.CATEGORIES) {
                JSONObject item = root.optJSONObject(category);
                if (item != null) {
                    rules.addText(category, item.optString("text", ""));
                    rules.addRegex(category, item.optString("regex", ""));
                }
            }
        } catch (Exception ignored) {
        }
        rules.addText("NGWord", legacyWords);
        cachedNgRulesKey = key;
        cachedNgRules = rules;
        return rules;
    }

    private static List<String> ruleLines(String saved) {
        List<String> lines = new ArrayList<>();
        if (saved == null) {
            return lines;
        }
        for (String line : saved.split("\\r?\\n")) {
            String value = line.trim();
            if (!value.isEmpty()) {
                lines.add(value);
            }
        }
        return lines;
    }

    private static class NgRules {
        static final String[] CATEGORIES = {"NGWord", "NGName", "NGID", "NGBe", "NGThread"};
        final Map<String, List<String>> texts = new LinkedHashMap<>();
        final Map<String, List<Pattern>> regexes = new LinkedHashMap<>();

        void addText(String category, String saved) {
            List<String> values = texts.get(category);
            if (values == null) {
                values = new ArrayList<>();
                texts.put(category, values);
            }
            for (String line : ruleLines(saved)) {
                values.add(line.toLowerCase(Locale.ROOT));
            }
        }

        void addRegex(String category, String saved) {
            List<Pattern> values = regexes.get(category);
            if (values == null) {
                values = new ArrayList<>();
                regexes.put(category, values);
            }
            for (String line : ruleLines(saved)) {
                try {
                    values.add(Pattern.compile(line, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
                } catch (Exception ignored) {
                }
            }
        }

        boolean matches(String category, String value) {
            if (value == null || value.isEmpty()) {
                return false;
            }
            String lower = value.toLowerCase(Locale.ROOT);
            List<String> textRules = texts.get(category);
            if (textRules != null) {
                for (String rule : textRules) {
                    if (!rule.isEmpty() && lower.contains(rule)) {
                        return true;
                    }
                }
            }
            List<Pattern> regexRules = regexes.get(category);
            if (regexRules != null) {
                for (Pattern rule : regexRules) {
                    if (rule.matcher(value).find()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private View buildSearchView(SearchPage page) {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText query = new EditText(this);
        query.setSingleLine(true);
        query.setText(searchQueryFromUrl(page.url));
        query.setHint(text("\u691c\u7d22\u8a9e", "Search query"));
        query.setTextColor(textColor());
        query.setHintTextColor(mutedColor());
        query.setTextSize(18);
        query.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        query.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        query.setBackground(addressBarBackground());
        query.setPadding(dp(12), 0, dp(12), 0);
        query.setOnEditorActionListener((v, actionId, event) -> {
            boolean enter = event != null && event.getAction() == KeyEvent.ACTION_UP
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER;
            if (actionId == EditorInfo.IME_ACTION_SEARCH || enter) {
                String value = query.getText().toString().trim();
                if (!value.isEmpty()) {
                    try {
                        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        manager.hideSoftInputFromWindow(query.getWindowToken(), 0);
                    } catch (Exception ignored) {
                    }
                    openInCurrentTab(searchUrl(value));
                }
                return true;
            }
            return false;
        });
        LinearLayout.LayoutParams queryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        queryParams.setMargins(0, 0, 0, dp(10));
        list.addView(query, queryParams);

        if (page.error != null) {
            TextView error = postText(page.error, null);
            error.setTextColor(Color.rgb(185, 28, 28));
            list.addView(error);
            return scroll;
        }

        renderSearchSlots(scroll, list, page);

        if (page.results.isEmpty()) {
            list.addView(postText(text("\u691c\u7d22\u7d50\u679c\u306a\u3057", "No search results."), null));
        }
        return withScrollScrubber(scroll);
    }

    private void renderSearchSlots(ScrollView scroll, LinearLayout list, SearchPage page) {
        VirtualSearchState state = new VirtualSearchState();
        list.setTag(state);
        String renderedCategory = null;
        int count = 0;
        for (SearchResult result : page.results) {
            if (matchesNgThread(result.title)) {
                continue;
            }
            if (result.category != null && !result.category.trim().isEmpty()
                    && !result.category.equals(renderedCategory)) {
                renderedCategory = result.category;
                addVirtualSearchSlot(list, new VirtualSearchSlot(renderedCategory, null, true));
            }
            addVirtualSearchSlot(list, new VirtualSearchSlot(null, result, false));
            count++;
        }
        if (count > 0) {
            state.refreshTask = () -> {
                state.refreshPending = false;
                refreshSearchSlots(scroll, list);
            };
            scroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
                if (list.getTag() instanceof VirtualSearchState) {
                    ((VirtualSearchState) list.getTag()).lastScrollAt = android.os.SystemClock.uptimeMillis();
                }
                scheduleSearchSlotRefresh(list);
            });
            scheduleSearchSlotRefresh(list);
        }
    }

    private void scheduleSearchSlotRefresh(LinearLayout list) {
        if (list == null || !(list.getTag() instanceof VirtualSearchState)) {
            return;
        }
        VirtualSearchState state = (VirtualSearchState) list.getTag();
        if (state.refreshTask == null || state.refreshPending) {
            return;
        }
        state.refreshPending = true;
        list.post(state.refreshTask);
    }

    private void addVirtualSearchSlot(LinearLayout list, VirtualSearchSlot slot) {
        FrameLayout holder = new FrameLayout(this);
        holder.setTag(slot);
        holder.addView(searchSlotSpacer(slot.height), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, slot.height));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (slot.categoryHeader) {
            params.setMargins(0, dp(8), 0, dp(8));
        } else {
            params.setMargins(0, 0, 0, dp(8));
        }
        list.addView(holder, params);
    }

    private void refreshSearchSlots(ScrollView scroll, LinearLayout list) {
        if (scroll == null || list == null) {
            return;
        }
        int scrollY = scroll.getScrollY();
        int height = scroll.getHeight();
        if (height <= 0) {
            return;
        }
        VirtualSearchState state = list.getTag() instanceof VirtualSearchState
                ? (VirtualSearchState) list.getTag() : null;
        boolean scrolling = state != null
                && android.os.SystemClock.uptimeMillis() - state.lastScrollAt < 180;
        int keepTop = Math.max(0, scrollY - (scrolling ? height * 3 : height * 2));
        int keepBottom = scrollY + (scrolling ? height * 7 : height * 5);
        int top = Math.max(0, scrollY - (scrolling ? height : height * 2));
        int bottom = scrollY + (scrolling ? height * 5 : height * 4);
        int visibleStart = firstChildWithBottomAtLeast(list, scrollY);
        int visibleEnd = lastChildWithTopAtMost(list, scrollY + height);
        int start = firstChildWithBottomAtLeast(list, top);
        int end = lastChildWithTopAtMost(list, bottom);
        int keepStart = firstChildWithBottomAtLeast(list, keepTop);
        int keepEnd = lastChildWithTopAtMost(list, keepBottom);
        Set<FrameLayout> keep = new LinkedHashSet<>();
        collectSearchSlotsInRange(list, keepStart, keepEnd, keep);
        int[] rendered = {0};
        boolean[] budgetReached = {false};
        renderSearchSlotsInRange(scroll, list, visibleStart, visibleEnd, SEARCH_VISIBLE_RENDER_BUDGET,
                rendered, budgetReached, keep);
        int budget = scrolling ? SEARCH_SCROLL_RENDER_BUDGET : SEARCH_IDLE_RENDER_BUDGET;
        renderSearchSlotsInRange(scroll, list, start, end, budget, rendered, budgetReached, keep);
        if (budgetReached[0]) {
            scheduleSearchSlotRefresh(list);
        }
        if (!scrolling && state != null && !state.renderedSlots.isEmpty()) {
            for (FrameLayout holder : new ArrayList<>(state.renderedSlots)) {
                if (keep.contains(holder)) {
                    continue;
                }
                Object tag = holder.getTag();
                if (tag instanceof VirtualSearchSlot) {
                    recycleSearchSlot(holder, (VirtualSearchSlot) tag);
                }
            }
        }
    }

    private void collectSearchSlotsInRange(ViewGroup list, int start, int end, Set<FrameLayout> keep) {
        if (list == null || keep == null || start > end || end < 0) {
            return;
        }
        int childCount = list.getChildCount();
        for (int i = Math.max(0, start); i <= end && i < childCount; i++) {
            View child = list.getChildAt(i);
            Object tag = child.getTag();
            if (child instanceof FrameLayout && tag instanceof VirtualSearchSlot) {
                keep.add((FrameLayout) child);
            }
        }
    }

    private void renderSearchSlotsInRange(ScrollView scroll, ViewGroup list, int start, int end, int budget,
                                          int[] rendered, boolean[] budgetReached, Set<FrameLayout> keep) {
        if (list == null || start > end || end < 0) {
            return;
        }
        int childCount = list.getChildCount();
        for (int i = Math.max(0, start); i <= end && i < childCount; i++) {
            View child = list.getChildAt(i);
            Object tag = child.getTag();
            if (!(child instanceof FrameLayout) || !(tag instanceof VirtualSearchSlot)) {
                continue;
            }
            FrameLayout holder = (FrameLayout) child;
            if (keep != null) {
                keep.add(holder);
            }
            VirtualSearchSlot slot = (VirtualSearchSlot) tag;
            if (slot.rendered) {
                continue;
            }
            if (scroll != null && holder.getBottom() <= scroll.getScrollY()) {
                continue;
            }
            if (rendered[0] >= budget) {
                budgetReached[0] = true;
                return;
            }
            renderSearchSlot(holder, slot);
            rendered[0]++;
        }
    }

    private void renderSearchSlot(FrameLayout holder, VirtualSearchSlot slot) {
        if (holder == null || slot == null || slot.rendered) {
            return;
        }
        View view = slot.categoryHeader ? categoryHeader(slot.category) : searchResultRow(slot.result);
        holder.removeAllViews();
        holder.addView(view, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        slot.rendered = true;
        if (holder.getParent() instanceof View) {
            View parent = (View) holder.getParent();
            if (parent instanceof LinearLayout && parent.getTag() instanceof VirtualSearchState) {
                ((VirtualSearchState) parent.getTag()).renderedSlots.add(holder);
            }
        }
    }

    private void recycleSearchSlot(FrameLayout holder, VirtualSearchSlot slot) {
        if (holder == null || slot == null || !slot.rendered) {
            return;
        }
        int measured = holder.getHeight();
        if (measured > 0) {
            slot.height = Math.max(slot.height, measured);
        }
        holder.removeAllViews();
        holder.addView(searchSlotSpacer(slot.height), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, slot.height));
        slot.rendered = false;
        if (holder.getParent() instanceof View) {
            View parent = (View) holder.getParent();
            if (parent instanceof LinearLayout && parent.getTag() instanceof VirtualSearchState) {
                ((VirtualSearchState) parent.getTag()).renderedSlots.remove(holder);
            }
        }
    }

    private View searchSlotSpacer(int height) {
        View spacer = new View(this);
        spacer.setMinimumHeight(Math.max(dp(44), height));
        return spacer;
    }

    private int estimateSearchResultHeight(SearchResult result) {
        String title = result == null || result.title == null ? "" : result.title;
        String meta = result == null || result.meta == null ? "" : result.meta;
        int titleLines = Math.max(1, title.length() / 24 + 1);
        int metaLines = meta.isEmpty() ? 0 : Math.max(1, meta.length() / 42 + 1);
        return dp(26 + Math.min(3, titleLines) * 20 + Math.min(2, metaLines) * 16);
    }

    private int firstChildWithBottomAtLeast(ViewGroup group, int y) {
        int low = 0;
        int high = group == null ? -1 : group.getChildCount() - 1;
        int answer = high + 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            View child = group.getChildAt(mid);
            if (child.getBottom() >= y) {
                answer = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return Math.max(0, answer);
    }

    private int lastChildWithTopAtMost(ViewGroup group, int y) {
        int low = 0;
        int high = group == null ? -1 : group.getChildCount() - 1;
        int answer = -1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            View child = group.getChildAt(mid);
            if (child.getTop() <= y) {
                answer = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return answer;
    }

    private View searchResultRow(SearchResult result) {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.HORIZONTAL);
        shell.setGravity(Gravity.CENTER_VERTICAL);
        shell.setBackgroundColor(postColor());
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(10), dp(9), dp(10), dp(9));
        row.setOnClickListener(v -> routeLink(result.url, currentTab()));

        TextView resultTitle = new TextView(this);
        resultTitle.setText(styledResultTitle(result));
        resultTitle.setTextColor(textColor());
        resultTitle.setTextSize(16);
        resultTitle.setPadding(0, 0, 0, dp(4));
        row.addView(resultTitle);

        TextView meta = new TextView(this);
        meta.setText(styledResultMeta(result.meta));
        meta.setTextColor(mutedColor());
        meta.setTextSize(12);
        row.addView(meta);
        shell.addView(row, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        ImageButton save = saveToggleButtonForResult(result);
        if (save != null) {
            shell.addView(save, new LinearLayout.LayoutParams(dp(44), dp(44)));
        }
        return shell;
    }

    private View buildBbsCategoryIndexView(SearchPage page) {
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText(page.title);
        title.setTextColor(textColor());
        title.setTextSize(20);
        title.setPadding(0, 0, 0, dp(10));
        list.addView(title);

        if (page.error != null) {
            TextView error = postText(page.error, null);
            error.setTextColor(Color.rgb(185, 28, 28));
            list.addView(error);
            return scroll;
        }

        Map<String, Integer> counts = new LinkedHashMap<>();
        for (SearchResult result : page.results) {
            String category = result.category == null ? "" : result.category.trim();
            counts.put(category, counts.containsKey(category) ? counts.get(category) + 1 : 1);
        }
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            String category = entry.getKey();
            String label = category.isEmpty() ? text("\u305d\u306e\u4ed6", "Other") : category;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(12), dp(12), dp(12));
            row.setBackground(roundedDrawable(postColor(), borderColor(), dp(8)));
            TextView name = new TextView(this);
            name.setText(label);
            name.setTextColor(textColor());
            name.setTextSize(16);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            row.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            TextView count = new TextView(this);
            count.setText(String.valueOf(entry.getValue()));
            count.setTextColor(mutedColor());
            count.setTextSize(13);
            row.addView(count);
            row.setOnClickListener(v -> openBbsCategory(page.url, category));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, dp(8));
            list.addView(row, params);
        }
        return scroll;
    }

    private void openBbsCategory(String menuUrl, String category) {
        String token = bbsCategoryToken(menuUrl, category);
        if (pendingNewTab) {
            showBbsCategoryView(token, true);
            return;
        }
        showBbsCategoryView(token, false);
    }

    private TextView categoryHeader(String value) {
        TextView header = new TextView(this);
        header.setText(value == null ? "" : value);
        header.setTextColor(textColor());
        header.setTextSize(15);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setPadding(dp(12), dp(10), dp(12), dp(8));
        header.setBackground(roundedDrawable(surfaceColor(), borderColor(), dp(8)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(8), 0, dp(8));
        header.setLayoutParams(params);
        return header;
    }

    private CharSequence styledResultMeta(String value) {
        if (value == null) {
            return "";
        }
        SpannableString text = new SpannableString(value);
        applyMetaNumberStyle(text, value, "(?:\u30ec\u30b9|Posts):\\s*(\\d+)", false);
        applyMetaNumberStyle(text, value, "(?:\u52e2\u3044|Speed):\\s*(\\d+(?:\\.\\d+)?)", true);
        return text;
    }

    private CharSequence styledResultTitle(SearchResult result) {
        String title = result == null || result.title == null ? "" : result.title;
        BoardPriorityMatch match = result == null ? null : result.priorityMatch;
        if (match == null || match.value == null || match.value.isEmpty()) {
            return title;
        }
        SpannableString highlighted = new SpannableString(title);
        if (match.regex) {
            try {
                Matcher matcher = Pattern.compile(match.value, Pattern.CASE_INSENSITIVE).matcher(title);
                while (matcher.find()) {
                    applyPriorityTitleHighlight(highlighted, matcher.start(), matcher.end());
                }
            } catch (Exception ignored) {
            }
        } else {
            String lowerTitle = title.toLowerCase(Locale.ROOT);
            String lowerMatch = match.value.toLowerCase(Locale.ROOT);
            int index = lowerTitle.indexOf(lowerMatch);
            while (index >= 0) {
                int end = index + match.value.length();
                applyPriorityTitleHighlight(highlighted, index, end);
                index = lowerTitle.indexOf(lowerMatch, end);
            }
        }
        return highlighted;
    }

    private void applyPriorityTitleHighlight(SpannableString text, int start, int end) {
        if (start < 0 || end <= start || end > text.length()) {
            return;
        }
        text.setSpan(new BackgroundColorSpan(Theme.linkHighlight(this)),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new StyleSpan(Typeface.BOLD),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private ImageButton saveToggleButtonForResult(SearchResult result) {
        return null;
    }

    private int savedIcon(String key, String url) {
        boolean saved = isSavedItem(key, url);
        return saved ? R.drawable.ic_star : R.drawable.ic_star_border;
    }

    private void applyMetaNumberStyle(SpannableString text, String value, String pattern, boolean velocity) {
        Matcher matcher = Pattern.compile(pattern).matcher(value);
        while (matcher.find()) {
            int start = matcher.start(1);
            int end = matcher.end(1);
            double number;
            try {
                number = Double.parseDouble(matcher.group(1));
            } catch (Exception ignored) {
                continue;
            }
            text.setSpan(new ForegroundColorSpan(metaBlue(number, velocity)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private int metaBlue(double value, boolean velocity) {
        double max = velocity ? 10000d : 1000d;
        double ratio = Math.max(0d, Math.min(1d, value / max));
        int start = Color.rgb(100, 116, 139);
        int end = Color.rgb(29, 78, 216);
        return Color.rgb(
                interpolate(Color.red(start), Color.red(end), ratio),
                interpolate(Color.green(start), Color.green(end), ratio),
                interpolate(Color.blue(start), Color.blue(end), ratio));
    }

    private int interpolate(int start, int end, double ratio) {
        return (int) Math.round(start + (end - start) * ratio);
    }

    private View withScrollScrubber(ScrollView scroll) {
        return withScrollScrubber(scroll, null);
    }

    private View withScrollScrubber(ScrollView scroll, CuspTab tab) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(bgColor());
        scroll.setBackgroundColor(bgColor());
        root.addView(scroll, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        FrameLayout scrubber = new FrameLayout(this);
        if (tab != null) {
            tab.scrollScrubber = scrubber;
        }
        root.addView(scrubber, new LinearLayout.LayoutParams(
                dp(24), ViewGroup.LayoutParams.MATCH_PARENT));

        View rail = new View(this);
        rail.setBackgroundColor(Color.argb(28, 31, 41, 55));
        FrameLayout.LayoutParams railParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        scrubber.addView(rail, railParams);

        FrameLayout unreadMarkers = new FrameLayout(this);
        if (tab != null) {
            tab.unreadMarkerLayer = unreadMarkers;
        }
        scrubber.addView(unreadMarkers, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View thumb = new View(this);
        GradientDrawable thumbBackground = new GradientDrawable();
        thumbBackground.setColor(Color.argb(170, 15, 118, 110));
        thumbBackground.setCornerRadius(dp(8));
        thumb.setBackground(thumbBackground);
        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(dp(10), dp(56));
        thumbParams.gravity = Gravity.CENTER_HORIZONTAL;
        scrubber.addView(thumb, thumbParams);

        scroll.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (tab != null && tab.threadRendering) {
                updateScrollThumb(scroll, scrubber, thumb);
                if (scrollY != oldScrollY) {
                    tab.lastScrollAt = android.os.SystemClock.uptimeMillis();
                }
                scheduleLazyImgurLoads();
                scheduleThreadPostVisibilityRefresh(tab);
                return;
            }
            updateScrollThumb(scroll, scrubber, thumb);
            if (tab != null && scrollY != oldScrollY) {
                long now = android.os.SystemClock.uptimeMillis();
                tab.lastScrollAt = now;
                if (isBottomJumpActive(tab)) {
                    pinThreadScrollToBottom(tab);
                } else if (now - tab.lastThreadScrollSaveAt >= THREAD_SCROLL_SAVE_INTERVAL_MS) {
                    tab.lastThreadScrollSaveAt = now;
                    rememberThreadScroll(tab);
                    requestSaveTabsSoon();
                }
                scheduleThreadPostVisibilityRefresh(tab);
            }
            scheduleLazyImgurLoads();
        });
        scrubber.post(() -> {
            updateScrollThumb(scroll, scrubber, thumb);
            scheduleLazyImgurLoads();
            scheduleThreadPostVisibilityRefresh(tab);
        });
        if (tab != null) {
            scrubber.post(() -> updateUnreadScrollMarkers(tab));
            scroll.getViewTreeObserver().addOnGlobalLayoutListener(() -> scheduleThreadScrollChromeRefresh(tab, 2));
        }
        rail.setOnTouchListener(scrubberTouchListener(scroll, scrubber, thumb));
        thumb.setOnTouchListener(scrubberTouchListener(scroll, scrubber, thumb));
        return root;
    }

    private void updateUnreadScrollMarkers(CuspTab tab) {
        if (tab == null || tab.threadPage == null || tab.postViews == null
                || tab.unreadMarkerLayer == null || tab.scrollScrubber == null
                || tab.threadScroll == null || tab.threadScroll.getChildCount() == 0) {
            return;
        }
        ViewGroup markers = tab.unreadMarkerLayer;
        markers.removeAllViews();
        if (tab.threadRendering) {
            return;
        }
        View content = tab.threadScroll.getChildAt(0);
        int contentHeight = Math.max(1, content.getHeight());
        int frameHeight = Math.max(1, tab.scrollScrubber.getHeight());
        int firstUnreadIndex = firstPostIndexAfter(tab.threadPage.posts, tab.readPostNumber);
        if (firstUnreadIndex < 0) {
            return;
        }
        View markerSource = postAnchorView(tab, tab.threadPage.posts.get(firstUnreadIndex).number);
        if (markerSource == null) {
            return;
        }
        int firstUnreadTop = Math.max(0, descendantTopWithin(markerSource, content));
        if (firstUnreadTop == 0 && firstUnreadIndex > 0 && !markerSource.isLaidOut()) {
            scheduleThreadScrollChromeRefresh(tab, 2);
            return;
        }
        int markerTop = Math.max(0, Math.min(frameHeight - dp(2), firstUnreadTop * frameHeight / contentHeight));
        View marker = new View(this);
        marker.setBackgroundColor(Color.argb(95, 20, 184, 166));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(dp(2), frameHeight - markerTop));
        params.topMargin = markerTop;
        markers.addView(marker, params);
    }

    private int firstPostIndexAfter(List<Post> posts, int number) {
        if (posts == null || posts.isEmpty()) {
            return -1;
        }
        int low = 0;
        int high = posts.size() - 1;
        int answer = -1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            Post post = posts.get(mid);
            int postNumber = post == null ? 0 : post.number;
            if (postNumber > number) {
                answer = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return answer;
    }

    private void scheduleThreadPostVisibilityRefresh(CuspTab tab) {
        if (tab == null || tab.threadList == null || tab.threadScroll == null) {
            return;
        }
        if (tab.threadPostVisibilityTask != null) {
            return;
        }
        tab.threadPostVisibilityTask = () -> {
            tab.threadPostVisibilityTask = null;
            refreshThreadPostVisibility(tab);
        };
        mainHandler.postDelayed(tab.threadPostVisibilityTask, THREAD_POST_VISIBILITY_INTERVAL_MS);
    }

    private void refreshThreadPostVisibility(CuspTab tab) {
        if (tab == null || tab.threadList == null || tab.threadScroll == null) {
            return;
        }
        int scrollY = tab.threadScroll.getScrollY();
        int height = tab.threadScroll.getHeight();
        if (height <= 0) {
            return;
        }
        boolean scrolling = recentlyScrolled(tab);
        int top = Math.max(0, scrollY - (scrolling ? height / 3 : height));
        int bottom = scrollY + height + (scrolling ? height / 2 : height * 2);
        int unloadTop = Math.max(0, scrollY - (scrolling ? height : height * 2));
        int unloadBottom = scrollY + height + (scrolling ? height * 2 : height * 3);
        ViewGroup list = tab.threadList;
        int start = firstChildWithBottomAtLeast(list, top);
        int end = lastChildWithTopAtMost(list, bottom);
        int unloadStart = firstChildWithBottomAtLeast(list, unloadTop);
        int unloadEnd = lastChildWithTopAtMost(list, unloadBottom);
        int visibleStart = firstChildWithBottomAtLeast(list, scrollY);
        int visibleEnd = lastChildWithTopAtMost(list, scrollY + height);
        int[] renderCost = {0};
        boolean[] budgetReached = {false};
        Set<FrameLayout> keep = new LinkedHashSet<>();
        collectVirtualPostSlotsInRange(list, unloadStart, unloadEnd, keep);
        renderVirtualPostSlotsInRange(list, visibleStart, visibleEnd, THREAD_VISIBLE_RENDER_BUDGET,
                renderCost, budgetReached, keep);
        int budget = scrolling ? THREAD_SCROLL_RENDER_BUDGET : THREAD_IDLE_RENDER_BUDGET;
        renderVirtualPostSlotsInRange(list, start, end, budget, renderCost, budgetReached, keep);
        if (budgetReached[0]) {
            scheduleThreadPostVisibilityRefresh(tab);
        }
        if (tab.renderedPostSlots != null && !tab.renderedPostSlots.isEmpty()) {
            for (FrameLayout holder : new ArrayList<>(tab.renderedPostSlots)) {
                if (keep.contains(holder)) {
                    continue;
                }
                Object tag = holder.getTag();
                if (tag instanceof VirtualPostSlot) {
                    recycleVirtualPostSlot(holder, (VirtualPostSlot) tag);
                }
            }
        }
        if (isBottomJumpActive(tab)) {
            pinThreadScrollToBottom(tab);
        }
    }

    private void renderVirtualPostSlotsInRange(ViewGroup list, int start, int end, int budget,
                                               int[] renderCost, boolean[] budgetReached,
                                               Set<FrameLayout> keep) {
        if (list == null || start > end || end < 0) {
            return;
        }
        int childCount = list.getChildCount();
        for (int i = Math.max(0, start); i <= end && i < childCount; i++) {
            View child = list.getChildAt(i);
            Object tag = child.getTag();
            if (!(child instanceof FrameLayout) || !(tag instanceof VirtualPostSlot)) {
                continue;
            }
            FrameLayout holder = (FrameLayout) child;
            VirtualPostSlot slot = (VirtualPostSlot) tag;
            if (keep != null) {
                keep.add(holder);
            }
            if (slot.rendered) {
                continue;
            }
            if (isSlotFullyAboveViewport(holder, slot.tab)) {
                continue;
            }
            int cost = renderCostForSlot(slot);
            if (renderCost[0] > 0 && renderCost[0] + cost > budget) {
                budgetReached[0] = true;
                return;
            }
            renderVirtualPostSlot(holder, slot);
            renderCost[0] += cost;
        }
    }

    private int renderCostForSlot(VirtualPostSlot slot) {
        if (slot == null || slot.item == null || slot.item.post == null) {
            return 1;
        }
        int cost = 1;
        Post post = slot.item.post;
        if (Boolean.TRUE.equals(post.cachedLikelyAa) || post.aaMode
                || (post.cachedLikelyAa == null && maybeHeavyAaBody(post.body))) {
            cost += THREAD_AA_RENDER_COST;
        }
        List<ImgurLink> cachedMedia = post.cachedImgurLinks;
        if (cachedMedia != null && !cachedMedia.isEmpty()) {
            cost += THREAD_MEDIA_RENDER_COST;
        }
        return cost;
    }

    private static boolean maybeHeavyAaBody(String body) {
        return body != null && body.length() > 120 && body.indexOf('\n') >= 0;
    }

    private void renderVirtualPostSlot(FrameLayout holder, VirtualPostSlot slot) {
        if (holder == null || slot == null || slot.rendered) {
            return;
        }
        PostCardShell postCard = createPostCardShell(slot.page, slot.tab, slot.item);
        if (postCard == null) {
            return;
        }
        holder.removeAllViews();
        ViewGroup.LayoutParams holderParams = holder.getLayoutParams();
        if (holderParams != null && holderParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
            holderParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.setLayoutParams(holderParams);
        }
        holder.addView(postCard.shell, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        slot.rendered = true;
        slot.card = postCard.card;
        slot.shell = postCard.shell;
        slot.tab.postViews.put(slot.item.post.number, postCard.card);
        if (slot.tab.renderedPostSlots != null) {
            slot.tab.renderedPostSlots.add(holder);
        }
        if (slot.tab == currentTab()) {
            visiblePostViews.put(slot.item.post.number, postCard.card);
        }
        holder.post(() -> {
            int measured = renderedSlotContentHeight(holder);
            if (measured > 0 && !isSlotFullyAboveViewport(holder, slot.tab)) {
                slot.height = measured;
            }
            scheduleThreadScrollChromeRefresh(slot.tab, 3);
        });
    }

    private void recycleVirtualPostSlot(FrameLayout holder, VirtualPostSlot slot) {
        if (holder == null || slot == null || !slot.rendered || highlightedPostView == slot.card) {
            return;
        }
        int measured = holder.getHeight();
        if (measured > 0) {
            slot.height = measured;
        }
        holder.removeAllViews();
        ViewGroup.LayoutParams holderParams = holder.getLayoutParams();
        if (holderParams != null && holderParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
            holderParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.setLayoutParams(holderParams);
        }
        holder.addView(postSlotSpacer(slot.height), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, slot.height));
        slot.rendered = false;
        if (slot.tab.renderedPostSlots != null) {
            slot.tab.renderedPostSlots.remove(holder);
        }
        slot.tab.postViews.remove(slot.item.post.number);
        if (slot.tab == currentTab()) {
            visiblePostViews.remove(slot.item.post.number);
        }
        slot.card = null;
        slot.shell = null;
    }

    private View postSlotSpacer(int height) {
        View spacer = new View(this);
        spacer.setMinimumHeight(Math.max(dp(56), height));
        return spacer;
    }

    private boolean isSlotFullyAboveViewport(View holder, CuspTab tab) {
        return holder != null
                && tab != null
                && tab.threadScroll != null
                && holder.getBottom() <= tab.threadScroll.getScrollY();
    }

    private int estimatePostSlotHeight(PostRenderItem item) {
        Post post = item == null ? null : item.post;
        if (post != null && copyPasteOmitEnabled() && post.copyPasteSourceNumber > 0) {
            return dp(52 + Math.min(item.depth, 8) * 2);
        }
        String body = post == null || post.body == null ? "" : post.body;
        int lines = post == null ? bodyLineCount(body) : bodyLineCount(post);
        boolean aaLike = post != null && (Boolean.TRUE.equals(post.cachedLikelyAa)
                || post.aaMode || maybeHeavyAaBody(body));
        int wrapped = aaLike ? 0 : Math.max(0, body.length() / 38);
        int textLines = aaLike ? lines : Math.min(42, lines + wrapped);
        int mediaRows = post == null ? 0 : (int) Math.ceil(imgurLinks(post).size() / 3.0);
        int depthPad = item == null ? 0 : Math.min(item.depth, 8) * 2;
        int lineHeight = aaLike ? estimateAaLineHeight(post, item) : dp(18);
        return dp(58) + Math.max(1, textLines) * lineHeight
                + dp(mediaRows * (MEDIA_GRID_CELL_DP + 10) + depthPad);
    }

    private int estimateAaLineHeight(Post post, PostRenderItem item) {
        int depth = item == null ? 0 : item.depth;
        int available = estimatePostTextWidth(depth);
        available = Math.max(dp(80), available);
        float baseSize = aaBaseTextSizePx();
        float longest = post != null && post.cachedAaLongestLineWidthPx > 0f
                ? post.cachedAaLongestLineWidthPx
                : longestLineWidth(aaMeasurePaint(baseSize), aaDisplayBody(post));
        if (post != null && longest > 0f) {
            post.cachedAaLongestLineWidthPx = longest;
        }
        float targetSize = longest > available
                ? Math.max(1f, baseSize * Math.max(1, available - 1) / longest)
                : baseSize;
        TextPaint paint = aaMeasurePaint(targetSize);
        Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
        return Math.max(dp(8), metrics.descent - metrics.ascent);
    }

    private int estimatePostTextWidth(int depth) {
        int safeDepth = Math.min(Math.max(0, depth), 8);
        int reservedDp = 24 + POST_OUTER_GAP_DP * 2 + 20 + safeDepth * 18;
        return Math.max(dp(80), getResources().getDisplayMetrics().widthPixels - dp(reservedDp));
    }

    private Post copyPasteSourcePost(ThreadPage page, Post post) {
        if (!copyPasteOmitEnabled() || page == null || post == null || post.body == null || post.body.isEmpty()) {
            return null;
        }
        ensureCopyPasteIndex(page);
        Post source = post.copyPasteSourceNumber > 0
                ? page.postsByNumber.get(post.copyPasteSourceNumber)
                : page.firstPostByBody.get(post.body);
        return source != null && source.number < post.number ? source : null;
    }

    private void ensureCopyPasteIndex(ThreadPage page) {
        if (page == null || page.copyPasteIndexBuilt) {
            return;
        }
        page.firstPostByBody.clear();
        for (Post post : page.posts) {
            if (post == null || post.body == null || post.body.isEmpty()) {
                continue;
            }
            Post source = page.firstPostByBody.get(post.body);
            if (source == null) {
                page.firstPostByBody.put(post.body, post);
                post.copyPasteSourceNumber = 0;
            } else {
                post.copyPasteSourceNumber = source.number;
            }
        }
        page.copyPasteIndexBuilt = true;
    }

    private int bodyLineCount(Post post) {
        if (post == null) {
            return 1;
        }
        if (post.cachedBodyLineCount <= 0) {
            post.cachedBodyLineCount = bodyLineCount(post.body);
        }
        return post.cachedBodyLineCount;
    }

    private static int bodyLineCount(String body) {
        if (body == null || body.isEmpty()) {
            return 1;
        }
        int lines = 1;
        for (int i = 0; i < body.length(); i++) {
            if (body.charAt(i) == '\n') {
                lines++;
            }
        }
        return lines;
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
        CuspTab tab = currentTab();
        if (ratio < 0.995f && isBottomJumpActive(tab)) {
            cancelBottomJump(tab);
        }
        scrollToScrubberRatio(scroll, ratio);
        return true;
    }

    private void scrollToScrubberRatio(ScrollView scroll, float ratio) {
        int range = scroll.getChildCount() == 0 ? 0 : scroll.getChildAt(0).getHeight() - scroll.getHeight();
        if (range <= 0) {
            return;
        }
        int target = (int) (range * Math.max(0f, Math.min(1f, ratio)));
        scroll.scrollTo(0, target);
        scroll.postDelayed(() -> scroll.scrollTo(0, target), 16);
        scroll.postDelayed(() -> scroll.scrollTo(0, target), 48);
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
                    resetBottomRefreshLoader(loader);
                }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                CuspTab tab = currentTab();
                if (isBottomJumpActive(tab) && event.getY() > downY[0] + dp(4)) {
                    cancelBottomJump(tab);
                    return false;
                }
                if (!startedAtBottom[0] && !dragging[0] && !refreshing[0]
                        && !scroll.canScrollVertically(1)) {
                    startedAtBottom[0] = true;
                    downY[0] = event.getY();
                    pullDistance[0] = 0;
                }
                if (startedAtBottom[0] && !refreshing[0]) {
                    if (scroll.canScrollVertically(1)) {
                        startedAtBottom[0] = false;
                        dragging[0] = false;
                        pullDistance[0] = 0;
                        resetBottomRefreshLoader(loader);
                        return false;
                    }
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
                    startedAtBottom[0] = false;
                    dragging[0] = false;
                    pullDistance[0] = 0;
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
                                resetBottomRefreshLoader(loader);
                            }).start();
                    return dragging[0];
                }
            }
            return false;
        });
    }

    private void resetBottomRefreshLoader(View loader) {
        loader.clearAnimation();
        loader.animate().cancel();
        setBottomRefreshSpinning(loader, false);
        loader.setTranslationY(dp(58));
        loader.setRotation(0f);
        loader.setVisibility(View.GONE);
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
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(bgColor());
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bgColor());
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView fiveChTitle = sectionTitleView("5ch");
        topRow.addView(fiveChTitle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        topRow.addView(new View(this), new LinearLayout.LayoutParams(dp(38), dp(38)));
        list.addView(topRow);
        TextView fiveCh = actionRow(text("5ch\u677f\u4e00\u89a7", "5ch boards"));
        fiveCh.setOnClickListener(v -> showFiveChBoardsView(true));
        list.addView(fiveCh);

        List<BbsLink> customLinks = readBbsLinks(preferences);
        if (!customLinks.isEmpty()) {
            list.addView(sectionTitleView(text("\u30ab\u30b9\u30bf\u30e0BBS", "Custom BBS")));
            for (BbsLink link : customLinks) {
                TextView row = actionRow(link.name);
                row.setOnClickListener(v -> openBoardUrl(link.url));
                row.setOnLongClickListener(v -> {
                    showValueCopyPopup(row, link.url);
                    return true;
                });
                list.addView(row);
            }
        }
        list.addView(sectionTitleView(text("\u30d6\u30c3\u30af\u30de\u30fc\u30af", "Bookmarks")));
        addHomeBookmarkSection(list);
        if (showHistoryOnHome()) {
            addHistorySection(list, fullHistory);
        }
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addPrivateModeOverlay(root, currentTabIsPrivate(), v -> togglePendingPrivateNewTab());
        return root;
    }

    private void updateScrollThumb(ScrollView scroll, View scrubber, View thumb) {
        if (scroll == null || scrubber == null || thumb == null) {
            return;
        }
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
    }

    private void refreshThreadScrollChrome(CuspTab tab) {
        if (tab == null || tab.threadScroll == null || tab.scrollScrubber == null) {
            return;
        }
        View thumb = tab.scrollScrubber.getChildCount() >= 3 ? tab.scrollScrubber.getChildAt(2) : null;
        tab.scrollScrubber.post(() -> {
            tab.scrollScrubber.setAlpha(1f);
            updateScrollThumb(tab.threadScroll, tab.scrollScrubber, thumb);
            if (tab.threadRendering) {
                if (tab.unreadMarkerLayer != null) {
                    tab.unreadMarkerLayer.removeAllViews();
                }
                return;
            }
            updateUnreadScrollMarkers(tab);
        });
    }

    private void scheduleThreadScrollChromeRefresh(CuspTab tab, int frames) {
        if (tab == null || tab.threadScroll == null || tab.scrollScrubber == null) {
            return;
        }
        tab.threadScrollChromeFrames = Math.max(tab.threadScrollChromeFrames, Math.max(1, frames));
        if (tab.threadScrollChromeTask != null) {
            return;
        }
        tab.threadScrollChromeTask = () -> {
            tab.threadScrollChromeTask = null;
            tab.threadScrollChromeFrames = Math.max(0, tab.threadScrollChromeFrames - 1);
            refreshThreadScrollChrome(tab);
            if (tab.threadScrollChromeFrames > 0 && tab.threadScroll != null && tab.scrollScrubber != null) {
                scheduleThreadScrollChromeRefresh(tab, tab.threadScrollChromeFrames);
            }
        };
        long delay = tab.threadRendering ? 160L : 16L;
        mainHandler.postDelayed(tab.threadScrollChromeTask, delay);
    }

    private void showFiveChBoardsView() {
        showFiveChBoardsView(true);
    }

    private void showFiveChBoardsView(boolean recordHistory) {
        if (recordHistory) {
            recordNewTabPage("5ch");
        }
        prepareChromeForLoading();
        View view = loadingView("");
        if (pendingNewTab) {
            contentFrame.removeAllViews();
            contentFrame.addView(view);
        } else {
            CuspTab tab = currentTab();
            if (tab != null) {
                tab.readerView = view;
                contentFrame.removeAllViews();
                contentFrame.addView(view);
            }
        }
        progressBar.setVisibility(View.VISIBLE);
        ioExecutor.execute(() -> {
            SearchPage page;
            try {
                page = downloadBbsDirectory(FIVE_CH_BBSMENU_URL);
                page.title = text("5ch\u677f\u4e00\u89a7", "5ch boards");
            } catch (Exception error) {
                page = SearchPage.error(FIVE_CH_BBSMENU_URL, error.getMessage());
            }
            SearchPage result = page;
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                View resultView = buildBbsCategoryIndexView(result);
                if (pendingNewTab) {
                    contentFrame.removeAllViews();
                    contentFrame.addView(resultView);
                } else {
                    CuspTab tab = currentTab();
                    if (tab != null) {
                        tab.readerMode = true;
                        tab.nativeKind = NATIVE_BOARD;
                        tab.url = FIVE_CH_BBSMENU_URL;
                        tab.title = result.title;
                        tab.searchPage = result;
                        tab.readerView = resultView;
                        tab.threadPage = null;
                        tab.threadScroll = null;
                        tab.postViews = null;
                    }
                    if (!tabOverviewVisible) {
                        contentFrame.removeAllViews();
                        contentFrame.addView(resultView);
                    }
                }
                updateBottomThreadBar(pendingNewTab ? null : currentTab());
                renderTabs();
            });
        });
    }

    private void showFiveChCategoryView(String encodedCategory, boolean record) {
        showBbsCategoryView(bbsCategoryToken(FIVE_CH_BBSMENU_URL, decodeNewTabToken(encodedCategory)), record);
    }

    private void showBbsCategoryView(String token, boolean record) {
        BbsCategoryRequest request = decodeBbsCategoryToken(token);
        String menuUrl = request.menuUrl == null || request.menuUrl.isEmpty() ? FIVE_CH_BBSMENU_URL : request.menuUrl;
        String category = request.category;
        if (record) {
            recordNewTabPage("bbs-category:" + bbsCategoryToken(menuUrl, category));
        }
        prepareChromeForLoading();
        View view = loadingView("");
        contentFrame.removeAllViews();
        contentFrame.addView(view);
        progressBar.setVisibility(View.VISIBLE);
        ioExecutor.execute(() -> {
            SearchPage page;
            try {
                SearchPage all = downloadBbsDirectory(menuUrl);
                page = filterBbsCategory(all, category);
                page.title = category == null || category.isEmpty()
                        ? all.title
                        : category;
            } catch (Exception error) {
                page = SearchPage.error(menuUrl, error.getMessage());
            }
            SearchPage result = page;
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                View resultView = buildSearchView(result);
                if (pendingNewTab) {
                    contentFrame.removeAllViews();
                    contentFrame.addView(resultView);
                } else {
                    CuspTab tab = currentTab();
                    if (tab != null) {
                        tab.readerMode = true;
                        tab.nativeKind = NATIVE_BOARD;
                        tab.url = result.url;
                        tab.title = result.title;
                        tab.searchPage = result;
                        tab.readerView = resultView;
                        tab.threadPage = null;
                        tab.threadScroll = null;
                        tab.postViews = null;
                    }
                    if (!tabOverviewVisible) {
                        contentFrame.removeAllViews();
                        contentFrame.addView(resultView);
                    }
                }
                updateBottomThreadBar(pendingNewTab ? null : currentTab());
                renderTabs();
            });
        });
    }

    private String bbsCategoryToken(String menuUrl, String category) {
        return encodeNewTabToken(menuUrl) + "::" + encodeNewTabToken(category);
    }

    private BbsCategoryRequest decodeBbsCategoryToken(String token) {
        String value = token == null ? "" : token;
        int split = value.indexOf("::");
        if (split < 0) {
            return new BbsCategoryRequest(FIVE_CH_BBSMENU_URL, decodeNewTabToken(value));
        }
        return new BbsCategoryRequest(
                decodeNewTabToken(value.substring(0, split)),
                decodeNewTabToken(value.substring(split + 2)));
    }

    private SearchPage filterBbsCategory(SearchPage source, String category) {
        SearchPage page = new SearchPage();
        page.url = source.url;
        page.title = category;
        page.error = source.error;
        for (SearchResult result : source.results) {
            if (TextUtils.equals(category, result.category)) {
                SearchResult copy = new SearchResult();
                copy.title = result.title;
                copy.url = result.url;
                copy.meta = result.meta;
                copy.responses = result.responses;
                copy.velocity = result.velocity;
                copy.boardOrder = result.boardOrder;
                copy.priorityMatch = result.priorityMatch;
                copy.category = "";
                page.results.add(copy);
            }
        }
        return page;
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
            list.addView(historyRow(history.get(i), fullHistory));
        }
        if (!fullHistory && history.size() > limit) {
            TextView more = actionRow(text("\u5c65\u6b74\u3092\u3082\u3063\u3068\u898b\u308b", "More history"));
            more.setOnClickListener(v -> {
                if (pendingNewTab) {
                    renderNewTabPage("history", true);
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
                list.addView(historyRow(item, true));
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
        closeThreadSearch();
        if (!replyPopups.isEmpty()) {
            dismissThreadPopups();
        }
        tabOverviewPrivateMode = pendingNewTab ? pendingPrivateNewTab : currentTabIsPrivate();
        pendingNewTab = false;
        pendingHistoryAll = false;
        tabOverviewVisible = true;
        tabOverviewScrollY = 0;
        contentFrame.removeAllViews();
        visibleThreadPage = null;
        visibleThreadScroll = null;
        visiblePostViews.clear();
        trimBackgroundTabViews();
        trimBackgroundPageData();
        contentFrame.addView(buildTabOverviewView());
        updateBottomThreadBar(currentTab());
        renderTabs();
    }

    private View buildTabOverviewView() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(bgColor());

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bgColor());
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            tabOverviewScrollY = scrollY;
        });
        scroll.setOnDragListener((v, event) -> {
            autoScrollDuringDrag(scroll, event);
            return true;
        });
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(12), dp(12), dp(12), dp(84));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        populateTabOverviewList(list);

        addPrivateModeOverlay(root, tabOverviewPrivateMode, v -> toggleTabOverviewPrivateMode());
        ImageButton reloadAll = iconButton(R.drawable.ic_refresh, text("\u3059\u3079\u3066\u66f4\u65b0", "Reload all"), v -> reloadAllTabs(true));
        reloadAll.setBackground(roundedDrawable(menuColor(), borderColor(), dp(22)));
        FrameLayout.LayoutParams reloadParams = new FrameLayout.LayoutParams(dp(54), dp(54), Gravity.BOTTOM | Gravity.RIGHT);
        reloadParams.setMargins(0, 0, dp(84), dp(18));
        root.addView(reloadAll, reloadParams);

        ImageButton add = iconButton(R.drawable.ic_add, text("\u65b0\u898f\u30bf\u30d6", "New tab"), v -> showPendingNewTab(tabOverviewPrivateMode));
        add.setBackground(roundedDrawable(TEAL, TEAL, dp(22)));
        add.setColorFilter(Color.WHITE);
        FrameLayout.LayoutParams addParams = new FrameLayout.LayoutParams(dp(54), dp(54), Gravity.BOTTOM | Gravity.RIGHT);
        addParams.setMargins(0, 0, dp(18), dp(18));
        root.addView(add, addParams);
        if (recentlyClosedTab != null) {
            root.addView(closedTabUndoBar(), closedTabUndoParams());
        }
        if (tabOverviewScrollY > 0) {
            scroll.post(() -> scroll.scrollTo(0, tabOverviewScrollY));
        }
        return root;
    }

    private void populateTabOverviewList(LinearLayout list) {
        list.removeAllViews();
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = sectionTitleView(tabOverviewPrivateMode
                ? text("\u30d7\u30e9\u30a4\u30d9\u30fc\u30c8\u30bf\u30d6", "Private tabs")
                : text("\u901a\u5e38\u30bf\u30d6", "Normal tabs"));
        header.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        header.addView(new View(this), new LinearLayout.LayoutParams(dp(38), dp(38)));
        list.addView(header);
        if (!tabOverviewPrivateMode && showBookmarksInTabOverview()) {
            addBookmarkOverviewSection(list);
        }
        addTabOverviewSection(list, tabOverviewPrivateMode);
    }

    private ImageButton privateModeButton(boolean active, View.OnClickListener listener) {
        ImageButton button = iconButton(R.drawable.ic_private_glasses,
                text("\u30d7\u30e9\u30a4\u30d9\u30fc\u30c8", "Private"), listener);
        button.setBackground(roundedDrawable(privateButtonFill(active),
                privateButtonStroke(active), dp(20)));
        button.setColorFilter(privateButtonIcon(active));
        button.setPadding(dp(7), dp(7), dp(7), dp(7));
        button.setTranslationY(-dp(2));
        return button;
    }

    private void addPrivateModeOverlay(FrameLayout root, boolean active, View.OnClickListener listener) {
        ImageButton button = privateModeButton(active, listener);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(38), dp(38), Gravity.TOP | Gravity.RIGHT);
        params.setMargins(0, dp(10), dp(12), 0);
        root.addView(button, params);
    }

    private void togglePendingPrivateNewTab() {
        if (!pendingNewTab) {
            return;
        }
        pendingPrivateNewTab = !pendingPrivateNewTab;
        contentFrame.setBackgroundColor(bgColor());
        contentFrame.removeAllViews();
        contentFrame.addView(pendingHistoryAll ? buildHistoryView() : buildSearchHomeView(false));
        renderTabs();
    }

    private void toggleTabOverviewPrivateMode() {
        tabOverviewPrivateMode = !tabOverviewPrivateMode;
        if (tabOverviewVisible && contentFrame != null) {
            contentFrame.setBackgroundColor(bgColor());
            refreshTabOverviewListOnly();
            renderTabs();
        }
    }

    private void addTabOverviewSection(LinearLayout list, boolean privateSection) {
        boolean any = false;
        for (int i = 0; i < tabs.size(); i++) {
            CuspTab tab = tabs.get(i);
            if (tab.privateBrowsing == privateSection && !tab.bookmarkOverviewTab) {
                list.addView(tabOverviewRow(tab, i));
                any = true;
            }
        }
        if (!any) {
            list.addView(helperLine(privateSection
                    ? text("\u30d7\u30e9\u30a4\u30d9\u30fc\u30c8\u30bf\u30d6\u306a\u3057", "No private tabs.")
                    : text("\u901a\u5e38\u30bf\u30d6\u306a\u3057", "No normal tabs.")));
        }
    }

    private View closedTabUndoBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(14), 0, dp(6), 0);
        bar.setBackground(roundedDrawable(menuColor(), borderColor(), dp(8)));
        TextView message = new TextView(this);
        message.setText(text("\u30bf\u30d6\u3092\u9589\u3058\u307e\u3057\u305f", "Tab closed"));
        message.setTextColor(textColor());
        message.setTextSize(14);
        bar.addView(message, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView undo = new TextView(this);
        undo.setText(text("\u5143\u306b\u623b\u3059", "Undo"));
        undo.setTextColor(TEAL);
        undo.setTextSize(14);
        undo.setGravity(Gravity.CENTER);
        undo.setPadding(dp(12), 0, dp(12), 0);
        undo.setOnClickListener(v -> undoClosedTab());
        bar.addView(undo, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        attachDismissSwipe(bar, () -> {
            recentlyClosedTab = null;
            if (clearClosedTabUndoTask != null) {
                mainHandler.removeCallbacks(clearClosedTabUndoTask);
                clearClosedTabUndoTask = null;
            }
            if (tabOverviewVisible) {
                refreshTabOverviewListOnly();
            }
        });
        return bar;
    }

    private FrameLayout.LayoutParams closedTabUndoParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48), Gravity.BOTTOM);
        params.setMargins(dp(12), 0, dp(12), dp(88));
        return params;
    }

    private void attachDismissSwipe(View view, Runnable dismiss) {
        final float[] downX = new float[1];
        final boolean[] dragging = new boolean[1];
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downX[0] = event.getRawX();
                dragging[0] = false;
                v.clearAnimation();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = event.getRawX() - downX[0];
                if (!dragging[0] && Math.abs(dx) > dp(10)) {
                    dragging[0] = true;
                }
                if (dragging[0]) {
                    v.setTranslationX(dx);
                    v.setAlpha(Math.max(0.25f, 1f - Math.abs(dx) / Math.max(1f, v.getWidth())));
                    return true;
                }
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (dragging[0] && Math.abs(v.getTranslationX()) > dp(90)) {
                    float target = v.getTranslationX() < 0 ? -v.getWidth() : v.getWidth();
                    v.animate().translationX(target).alpha(0f).setDuration(140).withEndAction(dismiss).start();
                    return true;
                }
                v.animate().translationX(0).alpha(1f).setDuration(120).start();
                return true;
            }
            return true;
        });
    }

    private View tabOverviewRow(CuspTab tab, int index) {
        boolean selected = !pendingNewTab && index == currentIndex;
        return tabOverviewRowShell(tab, selected,
                (v, event) -> {
                    if (event.getAction() == android.view.DragEvent.ACTION_DROP) {
                        Object local = event.getLocalState();
                        if (local instanceof DragPayload) {
                            DragPayload payload = (DragPayload) local;
                            if ("tabs".equals(payload.key)) {
                                moveTabInOverview(payload, index);
                                return true;
                            }
                            if (PREF_THREAD_BOOKMARKS.equals(payload.key)) {
                                moveBookmarkToTabsFromOverview(payload.index, index);
                                return true;
                            }
                        }
                    }
                    return true;
                },
                v -> selectTabFromOverview(index),
                (row, shell) -> {
                    row.startDragAndDrop(ClipData.newPlainText("tab", String.valueOf(index)),
                            new View.DragShadowBuilder(shell), new DragPayload("tabs", index), 0);
                    return true;
                },
                text("\u30bf\u30d6\u3092\u9589\u3058\u308b", "Close tab"),
                shell -> closeTabFromOverview(tabs.indexOf(tab), shell),
                (row, deleteLeft, deleteRight, shell) ->
                        attachTabOverviewSwipe(row, deleteLeft, deleteRight, tab, index, shell));
    }

    private FrameLayout tabOverviewRowShell(CuspTab tab, boolean selected, View.OnDragListener dragListener,
                                            View.OnClickListener clickListener,
                                            TabOverviewLongClick longClickListener,
                                            String closeLabel,
                                            TabOverviewClose closeListener,
                                            TabOverviewSwipeSetup swipeSetup) {
        FrameLayout shell = new FrameLayout(this);
        shell.setClipChildren(false);
        shell.setBackgroundColor(Color.TRANSPARENT);
        shell.setOnDragListener(dragListener);
        ImageView deleteLeft = swipeActionIcon(R.drawable.ic_delete, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        deleteLeft.setColorFilter(TEAL);
        ImageView deleteRight = swipeActionIcon(R.drawable.ic_delete, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        deleteRight.setColorFilter(TEAL);
        shell.addView(deleteLeft);
        shell.addView(deleteRight);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(7), dp(8), dp(7));
        row.setMinimumHeight(dp(78));
        row.setBackground(roundedDrawable(postColor(), selected ? TEAL : borderColor(), dp(8)));
        row.setOnClickListener(clickListener);
        if (longClickListener != null) {
            row.setOnLongClickListener(v -> longClickListener.onLongClick(row, shell));
        }

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        String rowTitle = tab.title == null || tab.title.trim().isEmpty() ? text("\u30bf\u30d6", "Tab") : tab.title;
        if (tab.threadPage != null) {
            setThreadTitleText(title, tab.threadPage, rowTitle);
        } else if (tab.knownThreadArchived) {
            setThreadTitleText(title, rowTitle, true);
        } else {
            title.setText(rowTitle);
        }
        title.setTextColor(textColor());
        title.setTextSize(14);
        title.setSingleLine(false);
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        title.setIncludeFontPadding(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            title.setAutoSizeTextTypeUniformWithConfiguration(11, 14, 1, TypedValue.COMPLEX_UNIT_SP);
        }
        TextView url = new TextView(this);
        url.setText(tab.url == null || tab.url.trim().isEmpty() ? text("\u65b0\u898f\u30bf\u30d6", "New tab") : tab.url);
        url.setTextColor(mutedColor());
        url.setTextSize(12);
        url.setSingleLine(true);
        url.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        url.setIncludeFontPadding(false);
        textBox.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(38)));
        textBox.addView(url, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(18)));
        row.addView(textBox, new LinearLayout.LayoutParams(0, dp(56), 1));

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

        if (tab.privateBrowsing) {
            ImageView privateIcon = new ImageView(this);
            privateIcon.setImageResource(R.drawable.ic_private_glasses);
            privateIcon.setColorFilter(privateButtonIcon(true));
            LinearLayout.LayoutParams privateIconParams = new LinearLayout.LayoutParams(dp(26), dp(26));
            privateIconParams.setMargins(dp(8), 0, 0, 0);
            row.addView(privateIcon, privateIconParams);
        }

        ImageButton close = iconButton(R.drawable.ic_close, closeLabel, v -> closeListener.close(shell));
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(dp(42), dp(40));
        closeParams.setMargins(dp(8), 0, 0, 0);
        row.addView(close, closeParams);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(78));
        rowParams.setMargins(0, 0, 0, dp(8));
        shell.setLayoutParams(rowParams);
        shell.addView(row, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(78)));
        swipeSetup.attach(row, deleteLeft, deleteRight, shell);
        return shell;
    }

    private interface TabOverviewLongClick {
        boolean onLongClick(View row, FrameLayout shell);
    }

    private interface TabOverviewClose {
        void close(FrameLayout shell);
    }

    private interface TabOverviewSwipeSetup {
        void attach(View row, View deleteLeft, View deleteRight, FrameLayout shell);
    }

    private void attachTabOverviewSwipe(View row, View deleteLeft, View deleteRight, CuspTab tab, int index, View shell) {
        final float[] downX = new float[1];
        final float[] downY = new float[1];
        final boolean[] dragging = new boolean[1];
        row.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downX[0] = event.getRawX();
                downY[0] = event.getRawY();
                dragging[0] = false;
                row.clearAnimation();
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
                    float translation = Math.max(-dp(108), Math.min(dp(108), dx * 0.58f));
                    row.setTranslationX(translation);
                    deleteLeft.setAlpha(Math.max(0f, Math.min(1f, translation / dp(64))));
                    deleteRight.setAlpha(Math.max(0f, Math.min(1f, -translation / dp(64))));
                    return true;
                }
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (dragging[0]) {
                    float tx = row.getTranslationX();
                    row.animate().translationX(0).setDuration(130).start();
                    deleteLeft.animate().alpha(0f).setDuration(130).start();
                    deleteRight.animate().alpha(0f).setDuration(130).start();
                    if (event.getAction() == MotionEvent.ACTION_UP && Math.abs(tx) >= dp(54)) {
                        int closeIndex = tabs.indexOf(tab);
                        closeTabFromOverview(closeIndex, shell);
                    }
                    return true;
                }
            }
            return false;
        });
    }

    private void selectTabFromOverview(int index) {
        if (index < 0 || index >= tabs.size()) {
            return;
        }
        tabOverviewVisible = false;
        tabOverviewScrollY = 0;
        pendingNewTab = false;
        pendingHistoryAll = false;
        contentFrame.removeAllViews();
        contentFrame.addView(loadingView(""));
        updateBottomThreadBar(tabs.get(index));
        renderTabs();
        mainHandler.post(() -> switchToTab(index));
    }

    private void moveTabInOverview(DragPayload payload, int to) {
        int from = payload.index;
        if (from < 0 || from >= tabs.size() || to < 0 || to >= tabs.size() || from == to) {
            return;
        }
        CuspTab selected = currentTab();
        CuspTab moved = tabs.remove(from);
        tabs.add(to, moved);
        currentIndex = selected == null ? Math.min(to, tabs.size() - 1) : tabs.indexOf(selected);
        payload.index = to;
        renderTabs();
        saveTabs(false);
        if (tabOverviewVisible) {
            refreshTabOverviewListOnly();
        }
    }

    private void autoScrollDuringDrag(View anchor, android.view.DragEvent event) {
        if (event == null || anchor == null) {
            return;
        }
        int action = event.getAction();
        if (action != android.view.DragEvent.ACTION_DRAG_LOCATION
                && action != android.view.DragEvent.ACTION_DRAG_ENTERED) {
            return;
        }
        ScrollView scroll = anchor instanceof ScrollView ? (ScrollView) anchor : findParentScrollView(anchor);
        if (scroll == null || scroll.getHeight() <= 0) {
            return;
        }
        int[] scrollLocation = new int[2];
        int[] anchorLocation = new int[2];
        scroll.getLocationOnScreen(scrollLocation);
        anchor.getLocationOnScreen(anchorLocation);
        float y = anchorLocation[1] + event.getY() - scrollLocation[1];
        int edge = dp(72);
        int step = dp(22);
        if (y < edge) {
            scroll.smoothScrollBy(0, -step);
        } else if (y > scroll.getHeight() - edge) {
            scroll.smoothScrollBy(0, step);
        }
    }

    private ScrollView findParentScrollView(View view) {
        View current = view;
        while (current != null) {
            if (current instanceof ScrollView) {
                return (ScrollView) current;
            }
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }
        return findScrollView(view);
    }

    private void closeTabFromOverview(int index) {
        closeTabFromOverview(index, null);
    }

    private void closeTabFromOverview(int index, View rowView) {
        ClosedTab closed = removeTabForOverview(index);
        if (closed == null) {
            return;
        }
        showClosedTabUndo(closed);
        tabOverviewVisible = true;
        pendingNewTab = tabs.isEmpty();
        updateBottomThreadBar(currentTab());
        renderTabs();
        if (rowView == null || rowView.getParent() == null) {
            refreshTabOverviewListOnly();
            return;
        }
        animateTabOverviewRowRemoval(rowView);
    }

    private void animateTabOverviewRowRemoval(View rowView) {
        int startHeight = rowView.getHeight() > 0 ? rowView.getHeight() : dp(86);
        rowView.animate()
                .translationX(rowView.getTranslationX() < 0 ? -rowView.getWidth() : rowView.getWidth())
                .alpha(0f)
                .setDuration(120)
                .start();
        ValueAnimator heightAnimator = ValueAnimator.ofInt(startHeight, 0);
        heightAnimator.setDuration(180);
        heightAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = rowView.getLayoutParams();
            if (params != null) {
                params.height = (Integer) animation.getAnimatedValue();
                rowView.setLayoutParams(params);
            }
        });
        heightAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (contentFrame != null && tabOverviewVisible) {
                    refreshTabOverviewListOnly();
                }
            }
        });
        heightAnimator.start();
    }

    private void resetNewTabHistory() {
        newTabNavigationHistory.clear();
        newTabNavigationHistory.add("home");
        newTabNavigationIndex = 0;
    }

    private void recordNewTabPage(String page) {
        if (!pendingNewTab || page == null || page.isEmpty()) {
            return;
        }
        if (newTabNavigationIndex >= 0
                && newTabNavigationIndex < newTabNavigationHistory.size()
                && page.equals(newTabNavigationHistory.get(newTabNavigationIndex))) {
            return;
        }
        while (newTabNavigationHistory.size() > newTabNavigationIndex + 1) {
            newTabNavigationHistory.remove(newTabNavigationHistory.size() - 1);
        }
        newTabNavigationHistory.add(page);
        newTabNavigationIndex = newTabNavigationHistory.size() - 1;
    }

    private String encodeNewTabToken(String value) {
        try {
            return URLEncoder.encode(value == null ? "" : value, "UTF-8");
        } catch (Exception error) {
            return value == null ? "" : value;
        }
    }

    private String decodeNewTabToken(String value) {
        try {
            return URLDecoder.decode(value == null ? "" : value, "UTF-8");
        } catch (Exception error) {
            return value == null ? "" : value;
        }
    }

    private void navigateNewTabHistory(int direction) {
        int next = newTabNavigationIndex + direction;
        if (next < 0 || next >= newTabNavigationHistory.size()) {
            return;
        }
        newTabNavigationIndex = next;
        renderNewTabPage(newTabNavigationHistory.get(newTabNavigationIndex), false);
    }

    private void renderNewTabPage(String page, boolean record) {
        if (record) {
            recordNewTabPage(page);
        }
        if ("history".equals(page)) {
            pendingHistoryAll = true;
            contentFrame.removeAllViews();
            contentFrame.addView(buildHistoryView());
        } else if ("5ch".equals(page)) {
            showFiveChBoardsView(record);
        } else if (page != null && page.startsWith("5ch-category:")) {
            showFiveChCategoryView(page.substring("5ch-category:".length()), record);
        } else if (page != null && page.startsWith("bbs-category:")) {
            showBbsCategoryView(page.substring("bbs-category:".length()), record);
        } else if (page != null && page.startsWith("saved:")) {
            SavedPage savedPage = savedPageFromToken(page.substring("saved:".length()));
            showSavedItemsView(savedPage.key, savedPage.folder, record);
        } else {
            pendingHistoryAll = false;
            contentFrame.removeAllViews();
            contentFrame.addView(buildSearchHomeView(false));
        }
        addressBar.setText("");
        clearAddressFocus();
        renderTabs();
    }

    private ClosedTab removeTabForOverview(int index) {
        if (tabs.isEmpty() || index < 0 || index >= tabs.size()) {
            return null;
        }
        CuspTab closing = tabs.remove(index);
        int oldCurrent = currentIndex;
        for (CuspTab tab : tabs) {
            if (tab.returnToIndex == index) {
                tab.returnToIndex = -1;
            } else if (tab.returnToIndex > index) {
                tab.returnToIndex--;
            }
        }
        if (tabs.isEmpty()) {
            currentIndex = -1;
        } else if (index < currentIndex) {
            currentIndex--;
        } else if (index == currentIndex) {
            currentIndex = Math.max(0, Math.min(index, tabs.size() - 1));
        } else {
            currentIndex = Math.max(0, Math.min(currentIndex, tabs.size() - 1));
        }
        requestSaveTabsSoon();
        return new ClosedTab(closing, index, oldCurrent);
    }

    private void showClosedTabUndo(ClosedTab closed) {
        recentlyClosedTab = closed;
        if (clearClosedTabUndoTask != null) {
            mainHandler.removeCallbacks(clearClosedTabUndoTask);
        }
        clearClosedTabUndoTask = () -> {
            recentlyClosedTab = null;
            clearClosedTabUndoTask = null;
            if (tabOverviewVisible && contentFrame != null) {
                refreshTabOverviewListOnly();
            }
        };
        mainHandler.postDelayed(clearClosedTabUndoTask, 4500);
    }

    private void undoClosedTab() {
        ClosedTab closed = recentlyClosedTab;
        if (closed == null) {
            return;
        }
        if (clearClosedTabUndoTask != null) {
            mainHandler.removeCallbacks(clearClosedTabUndoTask);
            clearClosedTabUndoTask = null;
        }
        if (closed.savedItem != null) {
            restoreDeletedBookmark(closed.savedItem, closed.savedItemIndex);
            recentlyClosedTab = null;
            refreshTabOverview();
            return;
        }
        int insertIndex = Math.max(0, Math.min(closed.index, tabs.size()));
        tabs.add(insertIndex, closed.tab);
        for (CuspTab tab : tabs) {
            if (tab != closed.tab && tab.returnToIndex >= insertIndex) {
                tab.returnToIndex++;
            }
        }
        if (closed.oldCurrentIndex < 0 || tabs.size() == 1) {
            currentIndex = insertIndex;
        } else if (closed.oldCurrentIndex >= insertIndex) {
            currentIndex = Math.min(tabs.size() - 1, closed.oldCurrentIndex);
        } else {
            currentIndex = Math.min(tabs.size() - 1, closed.oldCurrentIndex);
        }
        pendingNewTab = false;
        recentlyClosedTab = null;
        requestSaveTabsSoon();
        if (tabOverviewVisible && contentFrame != null) {
            refreshTabOverviewListOnly();
            updateBottomThreadBar(currentTab());
            renderTabs();
        }
    }

    private View savedListButton(String label, String key) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(8), dp(10), dp(8));
        row.setBackgroundColor(postColor());
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(rowParams);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_star);
        icon.setColorFilter(TEAL);
        row.addView(icon, new LinearLayout.LayoutParams(dp(26), dp(26)));

        TextView textView = new TextView(this);
        textView.setText(label + "  " + readSavedItems(key).size());
        textView.setTextColor(TEAL);
        textView.setTextSize(14);
        textView.setPadding(dp(10), 0, 0, 0);
        row.addView(textView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.setOnClickListener(v -> showSavedItemsView(key));
        return row;
    }

    private void showSavedItemsView(String key) {
        showSavedItemsView(key, "", true);
    }

    private void showSavedItemsView(String key, boolean recordHistory) {
        showSavedItemsView(key, "", recordHistory);
    }

    private void showSavedItemsView(String key, String folder) {
        showSavedItemsView(key, folder, true);
    }

    private void showSavedItemsView(String key, String folder, boolean recordHistory) {
        folder = normalizeSavedFolder(folder);
        View view = buildSavedItemsView(key, folder);
        if (pendingNewTab) {
            contentFrame.removeAllViews();
            contentFrame.addView(view);
            renderTabs();
        } else {
            CuspTab tab = currentTab();
            if (tab != null) {
                String url = savedPageUrl(key, folder);
                tab.readerMode = true;
                tab.nativeKind = NATIVE_SAVED;
                tab.url = url;
                tab.title = savedListTitle(key, folder);
                tab.threadPage = null;
                tab.searchPage = null;
                tab.threadScroll = null;
                tab.postViews = null;
                tab.readerView = view;
                contentFrame.removeAllViews();
                contentFrame.addView(view);
                renderTabs();
            }
        }
    }

    private void moveBookmarkToTabsFromOverview(int bookmarkIndex, int to) {
        List<SavedItem> items = readSavedItems(PREF_THREAD_BOOKMARKS);
        if (bookmarkIndex < 0 || bookmarkIndex >= items.size()) {
            return;
        }
        SavedItem item = items.get(bookmarkIndex);
        CuspTab tab = bookmarkOverviewTab(item);
        tab.bookmarkOverviewTab = false;
        tab.readerView = loadingView("");
        int insert = Math.max(0, Math.min(to, tabs.size()));
        tabs.add(insert, tab);
        removeSavedItem(PREF_THREAD_BOOKMARKS, item.url);
        if (currentIndex >= insert) {
            currentIndex++;
        }
        requestSaveTabsSoon();
        renderTabs();
        if (tabOverviewVisible && contentFrame != null) {
            refreshTabOverviewListOnly();
        }
    }

    private void addBookmarkOverviewSection(LinearLayout list) {
        List<SavedItem> bookmarks = readSavedItems(PREF_THREAD_BOOKMARKS);
        String selectedFolder = selectedBookmarkOverviewFolder(bookmarks);
        boolean hasSelectedBookmark = selectedFolder != null;
        String rootKey = bookmarkOverviewExpandedKey("");
        boolean rootExpanded = bookmarkOverviewExpanded(rootKey, true);
        list.addView(bookmarkOverviewFolderRow(
                text("\u30d6\u30c3\u30af\u30de\u30fc\u30af", "Bookmarks"),
                "",
                rootExpanded ? 0 : bookmarkOverviewUnreadSum(bookmarks, null),
                rootExpanded,
                0,
                hasSelectedBookmark && !rootExpanded,
                -1,
                v -> toggleBookmarkOverviewExpanded(rootKey, true)));
        if (!rootExpanded) {
            return;
        }
        List<String> folders = readSavedFolders(PREF_THREAD_BOOKMARKS);
        for (BookmarkNode node : bookmarkChildren("")) {
            if (node.folderNode) {
                addBookmarkOverviewFolderWithItems(list, bookmarks, folders, node.folder, selectedFolder, 1);
            } else {
                list.addView(bookmarkOverviewItemRow(node.item, 1));
            }
        }
    }

    private void addHomeBookmarkSection(LinearLayout list) {
        List<SavedItem> bookmarks = readSavedItems(PREF_THREAD_BOOKMARKS);
        List<String> folders = readSavedFolders(PREF_THREAD_BOOKMARKS);
        String rootKey = bookmarkOverviewExpandedKey("home:__root__");
        boolean rootExpanded = bookmarkOverviewExpanded(rootKey, true);
        list.addView(homeBookmarkRootRow(rootExpanded, v -> {
            toggleBookmarkOverviewExpanded(rootKey, false);
        }));
        if (rootExpanded) {
            for (BookmarkNode node : bookmarkChildren("")) {
                if (node.folderNode) {
                    addHomeBookmarkFolder(list, bookmarks, folders, node.folder, 1);
                } else {
                    list.addView(homeBookmarkItemRow(node.item, 1));
                }
            }
        }
        if (bookmarks.isEmpty() && folders.isEmpty()) {
            list.addView(helperLine(text("\u307e\u3060\u3042\u308a\u307e\u305b\u3093", "Nothing saved yet.")));
        }
    }

    private void addHomeBookmarkFolder(LinearLayout list, List<SavedItem> bookmarks, List<String> folders,
                                       String folder, int indentLevel) {
        String key = bookmarkOverviewExpandedKey("home:" + folder);
        boolean expanded = bookmarkOverviewExpanded(key, false);
        list.addView(homeBookmarkFolderRow(folder, expanded, indentLevel,
                v -> {
                    toggleBookmarkOverviewExpanded(key, false);
                }));
        if (!expanded) {
            return;
        }
        for (BookmarkNode node : bookmarkChildren(folder)) {
            if (node.folderNode) {
                addHomeBookmarkFolder(list, bookmarks, folders, node.folder, indentLevel + 1);
            } else {
                list.addView(homeBookmarkItemRow(node.item, indentLevel + 1));
            }
        }
    }

    private View homeBookmarkRootRow(boolean expanded, View.OnClickListener listener) {
        View row = homeBookmarkFolderRow("", expanded, 0, listener);
        return row;
    }

    private View homeBookmarkFolderRow(String folder, boolean expanded, int indentLevel, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10 + indentLevel * 18), dp(7), dp(8), dp(7));
        row.setMinimumHeight(dp(48));
        row.setBackgroundColor(postColor());
        row.setOnClickListener(listener);
        row.setOnLongClickListener(v -> {
            if (folder.isEmpty()) {
                return false;
            }
            v.startDragAndDrop(ClipData.newPlainText("folder", folder),
                    new View.DragShadowBuilder(row), new DragPayload(bookmarkNodeDragKey(BookmarkNode.folder(folder)), 0), 0);
            return true;
        });
        row.setOnDragListener((v, event) -> handleHomeBookmarkNodeDrop(event, parentSavedFolder(folder),
                folder, folder.isEmpty() ? "" : BookmarkNode.folder(folder).orderKey(), true, v));
        ImageView arrow = new ImageView(this);
        arrow.setImageResource(expanded ? R.drawable.ic_arrow_down : R.drawable.ic_chevron_right);
        arrow.setColorFilter(mutedColor());
        row.addView(arrow, new LinearLayout.LayoutParams(dp(24), dp(24)));
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_folder);
        icon.setColorFilter(TEAL);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(24), dp(24));
        iconParams.setMargins(dp(4), 0, 0, 0);
        row.addView(icon, iconParams);
        TextView label = new TextView(this);
        label.setText(folder.isEmpty() ? text("\u30d6\u30c3\u30af\u30de\u30fc\u30af", "Bookmarks") : savedFolderDisplayName(folder));
        label.setTextColor(textColor());
        label.setTextSize(15);
        label.setSingleLine(true);
        label.setEllipsize(TextUtils.TruncateAt.END);
        label.setPadding(dp(10), 0, 0, 0);
        row.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        ImageButton add = iconButton(R.drawable.ic_add, text("\u30d5\u30a9\u30eb\u30c0\u3092\u4f5c\u6210", "Create folder"),
                v -> showCreateHomeBookmarkFolderDialog(folder));
        add.setColorFilter(mutedColor());
        add.setBackgroundColor(Color.TRANSPARENT);
        row.addView(add, new LinearLayout.LayoutParams(dp(36), dp(36)));
        if (!folder.isEmpty()) {
            ImageButton delete = iconButton(R.drawable.ic_close, text("\u30d5\u30a9\u30eb\u30c0\u3092\u524a\u9664", "Delete folder"),
                    v -> confirmDeleteHomeBookmarkFolder(folder));
            delete.setColorFilter(mutedColor());
            delete.setBackgroundColor(Color.TRANSPARENT);
            row.addView(delete, new LinearLayout.LayoutParams(dp(36), dp(36)));
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(6));
        row.setLayoutParams(params);
        return row;
    }

    private View homeBookmarkItemRow(SavedItem item, int indentLevel) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10 + indentLevel * 18), dp(8), dp(10), dp(8));
        row.setBackgroundColor(postColor());
        row.setOnClickListener(v -> createTab(item.url, true));
        row.setOnLongClickListener(v -> {
            v.startDragAndDrop(ClipData.newPlainText("bookmark", item.url),
                    new View.DragShadowBuilder(row),
                    new DragPayload(bookmarkNodeDragKey(BookmarkNode.item(item)), 0), 0);
            return true;
        });
        row.setOnDragListener((v, event) -> handleHomeBookmarkNodeDrop(event,
                normalizeSavedFolder(item.folder), normalizeSavedFolder(item.folder),
                BookmarkNode.item(item).orderKey(), false, v));
        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText(item.title);
        title.setTextColor(textColor());
        title.setTextSize(15);
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        textBox.addView(title);
        TextView url = new TextView(this);
        url.setText(item.url);
        url.setTextColor(mutedColor());
        url.setTextSize(12);
        url.setSingleLine(true);
        url.setEllipsize(TextUtils.TruncateAt.END);
        textBox.addView(url);
        row.addView(textBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        ImageButton delete = iconButton(R.drawable.ic_close, text("\u524a\u9664", "Delete"),
                v -> confirmDeleteHomeBookmarkItem(item));
        delete.setColorFilter(mutedColor());
        delete.setBackgroundColor(Color.TRANSPARENT);
        row.addView(delete, new LinearLayout.LayoutParams(dp(36), dp(36)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(6));
        row.setLayoutParams(params);
        return row;
    }

    private boolean handleHomeBookmarkNodeDrop(android.view.DragEvent event, String parent, String targetFolder,
                                               String targetKey, boolean targetIsFolder, View anchor) {
        if (event.getAction() != android.view.DragEvent.ACTION_DROP) {
            return true;
        }
        Object local = event.getLocalState();
        if (local instanceof DragPayload) {
            DragPayload payload = (DragPayload) local;
            String movingKey = bookmarkNodeDragValue(payload.key);
            if (!movingKey.isEmpty()) {
                int zone = bookmarkDropZone(event, anchor, targetIsFolder);
                if (targetIsFolder) {
                    if (zone < 0 && !targetKey.isEmpty()) {
                        moveBookmarkNodeToParentNear(movingKey, parentSavedFolder(targetFolder), targetKey, false);
                    } else if (zone > 0 && !targetKey.isEmpty()) {
                        moveBookmarkNodeToParentNear(movingKey, parentSavedFolder(targetFolder), targetKey, true);
                    } else {
                        moveBookmarkNodeIntoFolder(movingKey, targetFolder);
                    }
                } else if (!targetKey.isEmpty()) {
                    moveBookmarkNodeToParentNear(movingKey, parent, targetKey, zone > 0);
                }
                refreshCurrentHomeOrHistoryView();
                return true;
            }
        }
        return true;
    }

    private void showCreateHomeBookmarkFolderDialog(String parent) {
        showSavedFolderNameDialog(
                text("\u30d5\u30a9\u30eb\u30c0\u3092\u4f5c\u6210", "Create folder"),
                "",
                folder -> {
                    createSavedFolder(PREF_THREAD_BOOKMARKS, parent, folder);
                    refreshCurrentHomeOrHistoryView();
                });
    }

    private void confirmDeleteHomeBookmarkFolder(String folder) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(text("\u30d5\u30a9\u30eb\u30c0\u3092\u524a\u9664", "Delete folder"))
                .setMessage(text("\u30d5\u30a9\u30eb\u30c0\u3060\u3051\u524a\u9664\u3057\u3001\u4e2d\u306e\u9805\u76ee\u306f\u4e00\u89a7\u306b\u623b\u3057\u307e\u3059\u304b\uff1f",
                        "Delete only the folder and move its items back to the main list?"))
                .setNegativeButton(text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(text("\u524a\u9664", "Delete"), (d, which) -> {
                    deleteSavedFolder(PREF_THREAD_BOOKMARKS, folder);
                    refreshCurrentHomeOrHistoryView();
                })
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private void confirmDeleteHomeBookmarkItem(SavedItem item) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(text("\u524a\u9664", "Delete"))
                .setMessage(text("\u3053\u306e\u9805\u76ee\u3092\u30d6\u30c3\u30af\u30de\u30fc\u30af\u304b\u3089\u524a\u9664\u3057\u307e\u3059\u304b\uff1f",
                        "Delete this item from bookmarks?"))
                .setNegativeButton(text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(text("\u524a\u9664", "Delete"), (d, which) -> {
                    removeSavedItem(PREF_THREAD_BOOKMARKS, item.url);
                    refreshCurrentHomeOrHistoryView();
                })
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private void addBookmarkOverviewFolderWithItems(LinearLayout list, List<SavedItem> bookmarks,
                                                    List<String> folders, String folder, String selectedFolder,
                                                    int indentLevel) {
        String key = bookmarkOverviewExpandedKey(folder);
        boolean expanded = bookmarkOverviewExpanded(key, false);
        list.addView(bookmarkOverviewFolderRow(savedFolderDisplayName(folder),
                folder,
                expanded ? 0 : bookmarkOverviewUnreadSum(bookmarks, folder),
                expanded,
                indentLevel,
                savedFolderDescendantOrSelf(folder, selectedFolder) && !expanded,
                folders.indexOf(folder),
                v -> toggleBookmarkOverviewExpanded(key)));
        if (expanded) {
            for (BookmarkNode node : bookmarkChildren(folder)) {
                if (node.folderNode) {
                    addBookmarkOverviewFolderWithItems(list, bookmarks, folders, node.folder, selectedFolder, indentLevel + 1);
                } else {
                    list.addView(bookmarkOverviewItemRow(node.item, indentLevel + 1));
                }
            }
        }
    }

    private View bookmarkOverviewFolderRow(String label, String folder, int unread, boolean expanded, int indentLevel,
                                           boolean selected, int folderIndex, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(7), dp(8), dp(7));
        row.setMinimumHeight(dp(56));
        row.setBackground(roundedDrawable(postColor(), selected ? TEAL : borderColor(), dp(8)));
        row.setOnClickListener(listener);
        row.setOnDragListener((v, event) -> {
            if (event.getAction() == android.view.DragEvent.ACTION_DROP) {
                Object local = event.getLocalState();
                if (local instanceof DragPayload) {
                    DragPayload payload = (DragPayload) local;
                    if ("tabs".equals(payload.key)) {
                        moveTabToBookmarksFromOverview(payload.index, folder, -1);
                        return true;
                    }
                    if (PREF_THREAD_BOOKMARKS.equals(payload.key)) {
                        List<SavedItem> items = readSavedItems(PREF_THREAD_BOOKMARKS);
                        if (payload.index >= 0 && payload.index < items.size()) {
                            SavedItem moved = items.get(payload.index);
                            String movingKey = BookmarkNode.item(moved).orderKey();
                            int zone = bookmarkDropZone(event, v, true);
                            if (zone < 0) {
                                moveBookmarkNodeToParentNear(movingKey, parentSavedFolder(folder),
                                        BookmarkNode.folder(folder).orderKey(), false);
                            } else if (zone > 0) {
                                moveBookmarkNodeToParentNear(movingKey, parentSavedFolder(folder),
                                        BookmarkNode.folder(folder).orderKey(), true);
                            } else {
                                moveBookmarkNodeIntoFolder(movingKey, folder);
                            }
                            refreshTabOverview();
                            return true;
                        }
                    }
                    String movingKey = bookmarkNodeDragValue(payload.key);
                    if (!movingKey.isEmpty()) {
                        int zone = bookmarkDropZone(event, v, true);
                        if (zone < 0) {
                            moveBookmarkNodeToParentNear(movingKey, parentSavedFolder(folder),
                                    BookmarkNode.folder(folder).orderKey(), false);
                        } else if (zone > 0) {
                            moveBookmarkNodeToParentNear(movingKey, parentSavedFolder(folder),
                                    BookmarkNode.folder(folder).orderKey(), true);
                        } else if (movingKey.startsWith("I:")) {
                            moveBookmarkNodeIntoFolder(movingKey, folder);
                        } else {
                            String movingFolder = movingKey.substring(2);
                            String movingParent = parentSavedFolder(movingFolder);
                            if (movingParent.equals(parentSavedFolder(folder))) {
                                moveBookmarkNodeIntoFolder(movingKey, folder);
                            } else {
                                moveBookmarkNodeIntoFolder(movingKey, folder);
                            }
                        }
                        refreshTabOverview();
                        return true;
                    }
                    if ((PREF_THREAD_BOOKMARKS + ":folder").equals(payload.key) && folderIndex >= 0) {
                        moveBookmarkOverviewFolder(payload.index, folderIndex);
                        payload.index = folderIndex;
                        refreshTabOverview();
                        return true;
                    }
                }
            }
            return true;
        });
        if (folderIndex >= 0) {
            row.setOnLongClickListener(v -> {
                v.startDragAndDrop(ClipData.newPlainText("bookmark-folder", label),
                        new View.DragShadowBuilder(row),
                        new DragPayload(bookmarkNodeDragKey(BookmarkNode.folder(folder)), folderIndex), 0);
                return true;
            });
        }

        int iconColor = mutedColor();
        ImageView arrow = new ImageView(this);
        arrow.setImageResource(expanded ? R.drawable.ic_arrow_down : R.drawable.ic_chevron_right);
        arrow.setColorFilter(iconColor);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(24), dp(24)));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_folder);
        icon.setColorFilter(TEAL);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(26), dp(26));
        iconParams.setMargins(dp(4), 0, 0, 0);
        row.addView(icon, iconParams);

        TextView textView = new TextView(this);
        textView.setText(label);
        textView.setTextColor(textColor());
        textView.setTextSize(15);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setPadding(dp(10), 0, 0, 0);
        row.addView(textView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        ImageButton add = iconButton(R.drawable.ic_add, text("\u30d5\u30a9\u30eb\u30c0\u3092\u4f5c\u6210", "Create folder"),
                v -> showCreateBookmarkOverviewFolderDialog(folder));
        add.setColorFilter(mutedColor());
        add.setBackgroundColor(Color.TRANSPARENT);
        row.addView(add, new LinearLayout.LayoutParams(dp(38), dp(38)));
        if (folderIndex >= 0) {
            ImageButton delete = iconButton(R.drawable.ic_close, text("\u30d5\u30a9\u30eb\u30c0\u3092\u524a\u9664", "Delete folder"),
                    v -> confirmDeleteBookmarkOverviewFolder(folder));
            delete.setColorFilter(mutedColor());
            delete.setBackgroundColor(Color.TRANSPARENT);
            row.addView(delete, new LinearLayout.LayoutParams(dp(38), dp(38)));
        }
        addUnreadBadgeIfNeeded(row, unread);
        return bookmarkOverviewShell(row, indentLevel, dp(56));
    }

    private void showCreateBookmarkOverviewFolderDialog(String parent) {
        showSavedFolderNameDialog(
                text("\u30d5\u30a9\u30eb\u30c0\u3092\u4f5c\u6210", "Create folder"),
                "",
                folder -> {
                    createSavedFolder(PREF_THREAD_BOOKMARKS, parent, folder);
                    refreshTabOverview();
                });
    }

    private void confirmDeleteBookmarkOverviewFolder(String folder) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(text("\u30d5\u30a9\u30eb\u30c0\u3092\u524a\u9664", "Delete folder"))
                .setMessage(text("\u30d5\u30a9\u30eb\u30c0\u3060\u3051\u524a\u9664\u3057\u3001\u4e2d\u306e\u9805\u76ee\u306f\u4e00\u89a7\u306b\u623b\u3057\u307e\u3059\u304b\uff1f",
                        "Delete only the folder and move its items back to the main list?"))
                .setNegativeButton(text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(text("\u524a\u9664", "Delete"), (d, which) -> {
                    deleteSavedFolder(PREF_THREAD_BOOKMARKS, folder);
                    refreshTabOverview();
                })
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private View bookmarkOverviewItemRow(SavedItem item, int indentLevel) {
        int itemIndex = savedItemIndex(PREF_THREAD_BOOKMARKS, item.url);
        CuspTab tab = bookmarkOverviewTab(item);
        FrameLayout shell = tabOverviewRowShell(tab, bookmarkOverviewItemSelected(item),
                (v, event) -> {
                    if (event.getAction() == android.view.DragEvent.ACTION_DROP) {
                        Object local = event.getLocalState();
                        if (local instanceof DragPayload) {
                            DragPayload payload = (DragPayload) local;
                            if ("tabs".equals(payload.key) && itemIndex >= 0) {
                                moveTabToBookmarksFromOverview(payload.index, normalizeSavedFolder(item.folder), itemIndex);
                                return true;
                            }
                            if (PREF_THREAD_BOOKMARKS.equals(payload.key) && itemIndex >= 0) {
                                List<SavedItem> items = readSavedItems(PREF_THREAD_BOOKMARKS);
                                if (payload.index >= 0 && payload.index < items.size()) {
                                    SavedItem moved = items.get(payload.index);
                                    moveBookmarkNodeToParentNear(BookmarkNode.item(moved).orderKey(),
                                            normalizeSavedFolder(item.folder), BookmarkNode.item(item).orderKey(),
                                            bookmarkDropZone(event, v, false) > 0);
                                }
                                payload.index = itemIndex;
                                refreshTabOverview();
                                return true;
                            }
                            String movingKey = bookmarkNodeDragValue(payload.key);
                            if (!movingKey.isEmpty()) {
                                moveBookmarkNodeToParentNear(movingKey, normalizeSavedFolder(item.folder),
                                        BookmarkNode.item(item).orderKey(), bookmarkDropZone(event, v, false) > 0);
                                refreshTabOverview();
                                return true;
                            }
                            if ((PREF_THREAD_BOOKMARKS + ":folder").equals(payload.key) && itemIndex >= 0) {
                                moveBookmarkOverviewFolderBeforeItem(payload.index, itemIndex);
                                refreshTabOverview();
                                return true;
                            }
                        }
                    }
                    return true;
                },
                v -> openBookmarkOverviewItem(item),
                (row, rowShell) -> {
                    if (itemIndex < 0) {
                        return false;
                    }
                    row.startDragAndDrop(ClipData.newPlainText("bookmark", item.url),
                            new View.DragShadowBuilder(rowShell),
                            new DragPayload(bookmarkNodeDragKey(BookmarkNode.item(item)), itemIndex), 0);
                    return true;
                },
                text("\u30d6\u30c3\u30af\u30de\u30fc\u30af\u3092\u524a\u9664", "Delete bookmark"),
                rowShell -> deleteBookmarkFromOverview(item),
                (row, deleteLeft, deleteRight, rowShell) ->
                        attachBookmarkOverviewSwipe(row, deleteLeft, deleteRight, item));
        return bookmarkOverviewShell(shell, indentLevel, dp(78));
    }

    private void openBookmarkOverviewItem(SavedItem item) {
        if (item == null || item.url == null || item.url.trim().isEmpty()) {
            return;
        }
        tabOverviewVisible = false;
        tabOverviewScrollY = 0;
        pendingHistoryAll = false;
        openInCurrentTab(item.url, true, true);
        renderTabs();
    }

    private View bookmarkOverviewShell(View row, int indentLevel, int height) {
        FrameLayout shell = new FrameLayout(this);
        int indent = dp(18 * Math.max(0, indentLevel));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height);
        params.setMargins(indent, 0, 0, dp(8));
        shell.setLayoutParams(params);
        shell.addView(row, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height));
        return shell;
    }

    private String selectedBookmarkOverviewFolder(List<SavedItem> bookmarks) {
        for (SavedItem bookmark : bookmarks) {
            if (bookmarkOverviewItemSelected(bookmark)) {
                return normalizeSavedFolder(bookmark.folder);
            }
        }
        return null;
    }

    private boolean bookmarkOverviewItemSelected(SavedItem item) {
        CuspTab tab = currentTab();
        return tab != null && tab.bookmarkOverviewTab
                && item != null && item.url != null
                && sameSavedUrl(threadUrl(tab), item.url);
    }

    private CuspTab bookmarkOverviewTab(SavedItem item) {
        CuspTab tab = new CuspTab();
        tab.url = item == null ? "" : item.url;
        tab.title = item == null ? "" : item.title;
        tab.readerMode = true;
        tab.nativeKind = isThreadUrl(tab.url) ? NATIVE_THREAD
                : isBoardUrl(tab.url) || isBbsDirectoryUrl(tab.url) ? NATIVE_BOARD : null;
        BookmarkOverviewStatus status = item == null ? null : bookmarkOverviewStatus(item.url);
        if (status != null) {
            if (status.title != null && !status.title.trim().isEmpty()) {
                tab.title = status.title.trim();
            }
            tab.knownThreadArchived = status.archived;
            if (status.responseCount > 0) {
                tab.knownMaxPostNumber = status.responseCount;
                tab.knownPostCount = status.responseCount;
                tab.readPostNumber = readPostNumber(preferences, item.url);
                tab.cachedUnreadCount = Math.max(0, status.responseCount - tab.readPostNumber);
                tab.hasThreadStats = true;
            }
        }
        CuspTab openTab = matchingThreadTab(item == null ? "" : item.url);
        if (openTab != null) {
            if ((tab.title == null || tab.title.trim().isEmpty()) && openTab.title != null) {
                tab.title = openTab.title;
            }
            tab.knownThreadArchived = tab.knownThreadArchived || openTab.knownThreadArchived
                    || (openTab.threadPage != null && openTab.threadPage.archived);
            if (!tab.hasThreadStats && openTab.hasThreadStats) {
                tab.knownMaxPostNumber = openTab.knownMaxPostNumber;
                tab.knownPostCount = openTab.knownPostCount;
                tab.readPostNumber = readPostNumber(preferences, tab.url);
                tab.cachedUnreadCount = Math.max(0, tab.knownMaxPostNumber - tab.readPostNumber);
                tab.hasThreadStats = true;
            }
        }
        return tab;
    }

    private CuspTab matchingThreadTab(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        for (CuspTab tab : tabs) {
            if (tab == null || !NATIVE_THREAD.equals(tab.nativeKind)) {
                continue;
            }
            String tabUrl = threadUrl(tab);
            if (sameSavedUrl(tabUrl, url)) {
                return tab;
            }
        }
        return null;
    }

    private int savedItemIndex(String key, String url) {
        String normalized = trimSlash(normalizeUrl(url));
        List<SavedItem> items = readSavedItems(key);
        for (int i = 0; i < items.size(); i++) {
            if (trimSlash(normalizeUrl(items.get(i).url)).equals(normalized)) {
                return i;
            }
        }
        return -1;
    }

    private void attachBookmarkOverviewSwipe(View row, View deleteLeft, View deleteRight, SavedItem item) {
        final float[] downX = new float[1];
        final float[] downY = new float[1];
        final boolean[] dragging = new boolean[1];
        row.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downX[0] = event.getRawX();
                downY[0] = event.getRawY();
                dragging[0] = false;
                row.clearAnimation();
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
                    float translation = Math.max(-dp(108), Math.min(dp(108), dx * 0.58f));
                    row.setTranslationX(translation);
                    deleteLeft.setAlpha(Math.max(0f, Math.min(1f, translation / dp(64))));
                    deleteRight.setAlpha(Math.max(0f, Math.min(1f, -translation / dp(64))));
                    return true;
                }
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (dragging[0]) {
                    float tx = row.getTranslationX();
                    row.animate().translationX(0).setDuration(130).start();
                    deleteLeft.animate().alpha(0f).setDuration(130).start();
                    deleteRight.animate().alpha(0f).setDuration(130).start();
                    if (event.getAction() == MotionEvent.ACTION_UP && Math.abs(tx) >= dp(54)) {
                        deleteBookmarkFromOverview(item);
                    }
                    return true;
                }
            }
            return false;
        });
    }

    private void deleteBookmarkFromOverview(SavedItem item) {
        if (item == null) {
            return;
        }
        showBookmarkClosedUndo(item);
        removeSavedItem(PREF_THREAD_BOOKMARKS, item.url);
        refreshTabOverview();
    }

    private void showBookmarkClosedUndo(SavedItem item) {
        ClosedTab closed = new ClosedTab(item, savedItemIndex(PREF_THREAD_BOOKMARKS, item.url));
        showClosedTabUndo(closed);
    }

    private void restoreDeletedBookmark(SavedItem item, int index) {
        List<SavedItem> items = readSavedItems(PREF_THREAD_BOOKMARKS);
        for (SavedItem existing : items) {
            if (sameSavedUrl(existing.url, item.url)) {
                return;
            }
        }
        int insert = Math.max(0, Math.min(index, items.size()));
        items.add(insert, item);
        writeSavedItems(PREF_THREAD_BOOKMARKS, items);
        String folder = normalizeSavedFolder(item.folder);
        if (!folder.isEmpty()) {
            createSavedFolder(PREF_THREAD_BOOKMARKS, folder);
        }
    }

    private void moveTabToBookmarksFromOverview(int tabIndex, String folder, int beforeItemIndex) {
        if (tabIndex < 0 || tabIndex >= tabs.size()) {
            return;
        }
        CuspTab tab = tabs.get(tabIndex);
        if (tab == null || tab.url == null || tab.url.trim().isEmpty()) {
            return;
        }
        addBookmarkFromTab(tab, folder, beforeItemIndex);
        tab.bookmarkOverviewTab = true;
        currentIndex = tabIndex;
        pendingNewTab = false;
        requestSaveTabsSoon();
        renderTabs();
        refreshTabOverview();
    }

    private void addBookmarkFromTab(CuspTab tab, String folder, int beforeItemIndex) {
        List<SavedItem> items = readSavedItems(PREF_THREAD_BOOKMARKS);
        String url = tab.url;
        for (int i = items.size() - 1; i >= 0; i--) {
            if (sameSavedUrl(items.get(i).url, url)) {
                items.remove(i);
            }
        }
        folder = normalizeSavedFolder(folder);
        String title = tab.title == null || tab.title.trim().isEmpty() ? hostTitle(url) : tab.title;
        SavedItem added = new SavedItem(cleanTitle(title, url), url, folder);
        int insert = 0;
        if (beforeItemIndex >= 0) {
            String beforeUrl = "";
            List<SavedItem> current = readSavedItems(PREF_THREAD_BOOKMARKS);
            if (beforeItemIndex < current.size()) {
                beforeUrl = current.get(beforeItemIndex).url;
            }
            insert = items.size();
            for (int i = 0; i < items.size(); i++) {
                if (sameSavedUrl(items.get(i).url, beforeUrl)) {
                    insert = i;
                    break;
                }
            }
        } else if (!folder.isEmpty()) {
            insert = items.size();
            for (int i = 0; i < items.size(); i++) {
                if (folder.equals(normalizeSavedFolder(items.get(i).folder))) {
                    insert = i;
                    break;
                }
            }
        }
        items.add(Math.max(0, Math.min(insert, items.size())), added);
        writeSavedItems(PREF_THREAD_BOOKMARKS, items);
        if (!folder.isEmpty()) {
            List<String> folders = readSavedFolders(PREF_THREAD_BOOKMARKS);
            if (!folders.contains(folder)) {
                folders.add(folder);
                writeSavedFolders(PREF_THREAD_BOOKMARKS, folders);
            }
        }
        if (tab.hasThreadStats || tab.knownThreadArchived) {
            ThreadOverviewStatus status = new ThreadOverviewStatus();
            status.url = url;
            status.title = title;
            status.responseCount = Math.max(tab.knownMaxPostNumber, tab.knownPostCount);
            status.archived = tab.knownThreadArchived || (tab.threadPage != null && tab.threadPage.archived);
            saveBookmarkOverviewStatus(url, status);
        }
    }

    private void moveBookmarkOverviewItem(int from, int to) {
        List<SavedItem> items = readSavedItems(PREF_THREAD_BOOKMARKS);
        if (from < 0 || from >= items.size() || to < 0 || to >= items.size() || from == to) {
            return;
        }
        String targetFolder = normalizeSavedFolder(items.get(to).folder);
        SavedItem moved = items.remove(from);
        int insert = to;
        if (from < insert) {
            insert--;
        }
        items.add(insert, new SavedItem(moved.title, moved.url, targetFolder));
        writeSavedItems(PREF_THREAD_BOOKMARKS, items);
    }

    private void moveSavedFolder(String key, int from, int to) {
        List<String> folders = readSavedFolders(key);
        if (from < 0 || from >= folders.size() || to < 0 || to >= folders.size() || from == to) {
            return;
        }
        String folder = folders.remove(from);
        int insert = to;
        if (from < insert) {
            insert--;
        }
        folders.add(insert, folder);
        writeSavedFolders(key, folders);
    }

    private void moveBookmarkOverviewFolder(int from, int to) {
        List<String> folders = readSavedFolders(PREF_THREAD_BOOKMARKS);
        if (from < 0 || from >= folders.size() || to < 0 || to >= folders.size() || from == to) {
            return;
        }
        String targetFolder = folders.get(to);
        moveSavedFolder(PREF_THREAD_BOOKMARKS, from, to);
        moveBookmarkFolderItemsBeforeTarget(folders.get(from), targetFolder, -1);
    }

    private void moveBookmarkOverviewFolderBeforeItem(int folderIndex, int targetItemIndex) {
        List<String> folders = readSavedFolders(PREF_THREAD_BOOKMARKS);
        if (folderIndex < 0 || folderIndex >= folders.size()) {
            return;
        }
        String folder = folders.get(folderIndex);
        moveBookmarkFolderItemsBeforeTarget(folder, null, targetItemIndex);
    }

    private void moveBookmarkFolderItemsBeforeTarget(String folder, String targetFolder, int targetItemIndex) {
        folder = normalizeSavedFolder(folder);
        targetFolder = targetFolder == null ? null : normalizeSavedFolder(targetFolder);
        if (folder.isEmpty() || folder.equals(targetFolder)) {
            return;
        }
        List<SavedItem> items = readSavedItems(PREF_THREAD_BOOKMARKS);
        List<SavedItem> moved = new ArrayList<>();
        List<SavedItem> rest = new ArrayList<>();
        for (SavedItem item : items) {
            if (folder.equals(normalizeSavedFolder(item.folder))) {
                moved.add(item);
            } else {
                rest.add(item);
            }
        }
        if (moved.isEmpty()) {
            return;
        }
        int insert = rest.size();
        if (targetFolder != null) {
            for (int i = 0; i < rest.size(); i++) {
                if (targetFolder.equals(normalizeSavedFolder(rest.get(i).folder))) {
                    insert = i;
                    break;
                }
            }
        } else if (targetItemIndex >= 0 && targetItemIndex < items.size()) {
            String targetUrl = items.get(targetItemIndex).url;
            for (int i = 0; i < rest.size(); i++) {
                if (sameSavedUrl(rest.get(i).url, targetUrl)) {
                    insert = i;
                    break;
                }
            }
        }
        rest.addAll(insert, moved);
        writeSavedItems(PREF_THREAD_BOOKMARKS, rest);
    }

    private boolean sameSavedUrl(String left, String right) {
        return trimSlash(normalizeUrl(left)).equals(trimSlash(normalizeUrl(right)));
    }

    private void refreshTabOverview() {
        if (tabOverviewVisible && contentFrame != null) {
            refreshTabOverviewListOnly();
            renderTabs();
        }
    }

    private void addUnreadBadgeIfNeeded(LinearLayout row, int unread) {
        if (unread <= 0) {
            return;
        }
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

    private String bookmarkOverviewExpandedKey(String folder) {
        return normalizeSavedFolder(folder).isEmpty()
                ? "__root__"
                : normalizeSavedFolder(folder);
    }

    private boolean bookmarkOverviewExpanded(String key, boolean defaultValue) {
        try {
            JSONObject object = new JSONObject(preferences.getString(PREF_BOOKMARK_OVERVIEW_EXPANDED, "{}"));
            return object.has(key) ? object.optBoolean(key, defaultValue) : defaultValue;
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private void toggleBookmarkOverviewExpanded(String key) {
        toggleBookmarkOverviewExpanded(key, true);
    }

    private void toggleBookmarkOverviewExpanded(String key, boolean refreshTabOverviewAfterToggle) {
        try {
            JSONObject object = new JSONObject(preferences.getString(PREF_BOOKMARK_OVERVIEW_EXPANDED, "{}"));
            boolean next = !object.optBoolean(key, "__root__".equals(key));
            object.put(key, next);
            preferences.edit().putString(PREF_BOOKMARK_OVERVIEW_EXPANDED, object.toString()).apply();
        } catch (Exception ignored) {
        }
        if (refreshTabOverviewAfterToggle && tabOverviewVisible && contentFrame != null) {
            refreshTabOverviewListOnly();
            renderTabs();
        } else if (!refreshTabOverviewAfterToggle) {
            refreshCurrentHomeOrHistoryView();
        }
    }

    private void refreshTabOverviewListOnly() {
        ScrollView scroll = findScrollView(contentFrame);
        if (scroll == null || scroll.getChildCount() == 0 || !(scroll.getChildAt(0) instanceof LinearLayout)) {
            if (contentFrame != null) {
                contentFrame.removeAllViews();
                contentFrame.addView(buildTabOverviewView());
            }
            return;
        }
        LinearLayout list = (LinearLayout) scroll.getChildAt(0);
        populateTabOverviewList(list);
    }

    private View buildSavedItemsView(String key) {
        return buildSavedItemsView(key, "");
    }

    private View buildSavedItemsView(String key, String folder) {
        folder = normalizeSavedFolder(folder);
        final String currentFolder = folder;
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setLayoutTransition(new LayoutTransition());
        list.setPadding(dp(12), dp(12), dp(12), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        list.addView(sectionTitleView(savedListTitle(key, folder)));
        boolean hasFolders = false;
        if (folder.isEmpty()) {
            list.addView(rootSavedFolderRow(key));
            for (String childFolder : childSavedFolders(key, folder)) {
                list.addView(savedFolderRow(key, childFolder));
                hasFolders = true;
            }
        } else {
            list.addView(actionButtonRow(R.drawable.ic_arrow_back,
                    text("\u4e00\u89a7\u306b\u623b\u308b", "Back to list"),
                    v -> showSavedItemsView(key, parentSavedFolder(currentFolder))));
            for (String childFolder : childSavedFolders(key, folder)) {
                list.addView(savedFolderRow(key, childFolder));
                hasFolders = true;
            }
        }
        List<SavedItem> items = readSavedItems(key);
        boolean added = false;
        for (int i = 0; i < items.size(); i++) {
            SavedItem item = items.get(i);
            if (!folder.equals(normalizeSavedFolder(item.folder))) {
                continue;
            }
            list.addView(savedItemRow(key, item, i, folder));
            added = true;
        }
        if (!added && !hasFolders) {
            list.addView(helperLine(text("\u307e\u3060\u3042\u308a\u307e\u305b\u3093", "Nothing saved yet.")));
        }
        return scroll;
    }

    private String savedListTitle(String key) {
        return savedListTitle(key, "");
    }

    private String savedListTitle(String key, String folder) {
        folder = normalizeSavedFolder(folder);
        if (!folder.isEmpty()) {
            return savedFolderDisplayName(folder);
        }
        return text("\u30d6\u30c3\u30af\u30de\u30fc\u30af", "Bookmarks");
    }

    private View savedItemRow(String key, SavedItem item, int index) {
        return savedItemRow(key, item, index, normalizeSavedFolder(item.folder));
    }

    private View savedItemRow(String key, SavedItem item, int index, String folder) {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.HORIZONTAL);
        shell.setGravity(Gravity.CENTER_VERTICAL);
        shell.setBackgroundColor(postColor());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(8));
        shell.setLayoutParams(params);
        shell.setOnDragListener((v, event) -> {
            if (event.getAction() == android.view.DragEvent.ACTION_DROP) {
                Object local = event.getLocalState();
                if (local instanceof DragPayload) {
                    DragPayload payload = (DragPayload) local;
                    if (key.equals(payload.key)) {
                        moveSavedItem(key, payload.index, index);
                        payload.index = index;
                        showSavedItemsView(key, folder);
                        return true;
                    }
                }
            }
            return true;
        });

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        textBox.setPadding(dp(10), dp(9), dp(10), dp(9));
        textBox.setOnClickListener(v -> openSavedItemInNewTab(item));
        textBox.setOnLongClickListener(v -> {
            v.startDragAndDrop(ClipData.newPlainText("saved", item.url),
                    new View.DragShadowBuilder(shell), new DragPayload(key, index), 0);
            return true;
        });
        TextView title = new TextView(this);
        title.setText(item.title);
        title.setTextColor(textColor());
        title.setTextSize(16);
        textBox.addView(title);
        TextView url = new TextView(this);
        url.setText(item.url);
        url.setTextColor(mutedColor());
        url.setTextSize(12);
        textBox.addView(url);
        shell.addView(textBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        ImageButton delete = iconButton(R.drawable.ic_close, text("\u524a\u9664", "Delete"),
                v -> confirmDeleteSavedItem(key, item, folder));
        delete.setColorFilter(mutedColor());
        delete.setBackgroundColor(Color.TRANSPARENT);
        shell.addView(delete, new LinearLayout.LayoutParams(dp(42), dp(42)));
        return shell;
    }

    private void openSavedItemInNewTab(SavedItem item) {
        if (item == null || item.url == null || item.url.trim().isEmpty()) {
            return;
        }
        createTab(item.url, true);
    }

    private View savedFolderRow(String key, String folder) {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.HORIZONTAL);
        shell.setGravity(Gravity.CENTER_VERTICAL);
        shell.setBackgroundColor(postColor());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(8));
        shell.setLayoutParams(params);
        shell.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.DragEvent.ACTION_DRAG_ENTERED:
                    shell.setBackground(roundedDrawable(postColor(), TEAL, dp(8), dp(2)));
                    return true;
                case android.view.DragEvent.ACTION_DRAG_EXITED:
                case android.view.DragEvent.ACTION_DRAG_ENDED:
                    shell.setBackgroundColor(postColor());
                    return true;
                case android.view.DragEvent.ACTION_DROP:
                    shell.setBackgroundColor(postColor());
                    Object local = event.getLocalState();
                    if (local instanceof DragPayload) {
                        DragPayload payload = (DragPayload) local;
                        if (key.equals(payload.key)) {
                            List<SavedItem> items = readSavedItems(key);
                            if (payload.index >= 0 && payload.index < items.size()) {
                                moveSavedItemToFolder(key, items.get(payload.index).url, folder);
                                showSavedItemsView(key);
                                return true;
                            }
                        }
                    }
                    return true;
                default:
                    return true;
            }
        });

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_folder);
        icon.setColorFilter(TEAL);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(26), dp(26));
        iconParams.setMargins(dp(10), 0, 0, 0);
        shell.addView(icon, iconParams);

        TextView label = new TextView(this);
        label.setText(savedFolderDisplayName(folder) + "  " + savedFolderItemCount(key, folder));
        label.setTextColor(textColor());
        label.setTextSize(16);
        label.setPadding(dp(10), dp(12), dp(10), dp(12));
        label.setOnClickListener(v -> showSavedItemsView(key, folder));
        shell.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        ImageButton add = iconButton(R.drawable.ic_add, text("\u30d5\u30a9\u30eb\u30c0\u3092\u4f5c\u6210", "Create folder"),
                v -> showCreateSavedFolderDialog(key, folder));
        add.setColorFilter(mutedColor());
        add.setBackgroundColor(Color.TRANSPARENT);
        shell.addView(add, new LinearLayout.LayoutParams(dp(42), dp(42)));

        ImageButton rename = iconButton(R.drawable.ic_edit, text("\u540d\u524d\u3092\u5909\u66f4", "Rename"),
                v -> showRenameSavedFolderDialog(key, folder));
        rename.setColorFilter(mutedColor());
        rename.setBackgroundColor(Color.TRANSPARENT);
        shell.addView(rename, new LinearLayout.LayoutParams(dp(42), dp(42)));

        ImageButton delete = iconButton(R.drawable.ic_close, text("\u30d5\u30a9\u30eb\u30c0\u3092\u524a\u9664", "Delete folder"),
                v -> confirmDeleteSavedFolder(key, folder));
        delete.setColorFilter(mutedColor());
        delete.setBackgroundColor(Color.TRANSPARENT);
        shell.addView(delete, new LinearLayout.LayoutParams(dp(42), dp(42)));
        return shell;
    }

    private View rootSavedFolderRow(String key) {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.HORIZONTAL);
        shell.setGravity(Gravity.CENTER_VERTICAL);
        shell.setBackgroundColor(postColor());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(8));
        shell.setLayoutParams(params);
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_folder);
        icon.setColorFilter(TEAL);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(26), dp(26));
        iconParams.setMargins(dp(10), 0, 0, 0);
        shell.addView(icon, iconParams);
        TextView label = new TextView(this);
        label.setText(savedListTitle(key));
        label.setTextColor(textColor());
        label.setTextSize(16);
        label.setPadding(dp(10), dp(12), dp(10), dp(12));
        shell.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        ImageButton add = iconButton(R.drawable.ic_add, text("\u30d5\u30a9\u30eb\u30c0\u3092\u4f5c\u6210", "Create folder"),
                v -> showCreateSavedFolderDialog(key, ""));
        add.setColorFilter(mutedColor());
        add.setBackgroundColor(Color.TRANSPARENT);
        shell.addView(add, new LinearLayout.LayoutParams(dp(42), dp(42)));
        return shell;
    }

    private View actionButtonRow(int iconRes, String label, View.OnClickListener listener) {
        LinearLayout row = menuIconItem(iconRes, label, listener);
        row.setBackgroundColor(postColor());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(params);
        return row;
    }

    private TextView sectionTitleView(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(textColor());
        view.setTextSize(18);
        view.setPadding(0, dp(10), 0, dp(8));
        return view;
    }

    private TextView helperLine(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(mutedColor());
        view.setTextSize(14);
        view.setPadding(dp(10), dp(8), dp(10), dp(10));
        return view;
    }

    private TextView actionRow(String value) {
        TextView view = helperLine(value);
        view.setTextColor(TEAL);
        view.setBackgroundColor(postColor());
        return view;
    }

    private View historyRow(ThreadHistoryItem item) {
        return historyRow(item, false);
    }

    private View historyRow(ThreadHistoryItem item, boolean fullHistory) {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.HORIZONTAL);
        shell.setGravity(Gravity.CENTER_VERTICAL);
        shell.setBackgroundColor(postColor());
        LinearLayout.LayoutParams shellParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        shellParams.setMargins(0, 0, 0, dp(8));
        shell.setLayoutParams(shellParams);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(10), dp(9), dp(10), dp(9));
        row.setOnClickListener(v -> routeLink(item.url, currentTab()));
        row.setOnLongClickListener(v -> {
            showValueCopyPopup(row, item.url);
            return true;
        });

        TextView title = new TextView(this);
        title.setText(item.title);
        title.setTextColor(textColor());
        title.setTextSize(16);
        row.addView(title);

        TextView url = new TextView(this);
        url.setText(item.url);
        url.setTextColor(mutedColor());
        url.setTextSize(12);
        row.addView(url);
        if (item.lastViewedAt > 0) {
            TextView viewedAt = new TextView(this);
            viewedAt.setText(text("\u6700\u7d42\u95b2\u89a7: ", "Last viewed: ") + formatHistoryTime(item.lastViewedAt));
            viewedAt.setTextColor(Color.rgb(100, 116, 139));
            viewedAt.setTextSize(12);
            row.addView(viewedAt);
        }
        shell.addView(row, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        ImageButton delete = iconButton(R.drawable.ic_close, text("\u5c65\u6b74\u3092\u524a\u9664", "Delete history"), v -> {
            removeThreadHistory(preferences, item.url);
            refreshCurrentHomeOrHistoryView(fullHistory);
        });
        delete.setColorFilter(mutedColor());
        delete.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(dp(42), dp(42));
        deleteParams.setMargins(dp(6), 0, dp(4), 0);
        shell.addView(delete, deleteParams);
        return shell;
    }

    private void refreshCurrentHomeOrHistoryView() {
        refreshCurrentHomeOrHistoryView(false);
    }

    private void refreshCurrentHomeOrHistoryView(boolean fullHistory) {
        if (contentFrame == null) {
            return;
        }
        ScrollView previousScroll = findScrollView(contentFrame);
        int scrollY = previousScroll == null ? 0 : previousScroll.getScrollY();
        contentFrame.removeAllViews();
        View nextView;
        if (pendingNewTab) {
            pendingHistoryAll = fullHistory || pendingHistoryAll;
            nextView = pendingHistoryAll ? buildHistoryView() : buildSearchHomeView(false);
        } else {
            CuspTab tab = currentTab();
            if (tab == null) {
                return;
            }
            nextView = fullHistory ? buildHistoryView() : buildSearchHomeView(false);
            tab.readerView = nextView;
        }
        contentFrame.addView(nextView);
        ScrollView nextScroll = findScrollView(nextView);
        if (nextScroll != null) {
            nextScroll.post(() -> nextScroll.scrollTo(0, Math.max(0, Math.min(scrollY,
                    nextScroll.getChildAt(0).getHeight() - nextScroll.getHeight()))));
        }
    }

    static String formatHistoryTime(long time) {
        if (time <= 0) {
            return "";
        }
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
        return format.format(new Date(time));
    }

    private void openBoardUrl(String url) {
        if (pendingNewTab) {
            openPendingNewTabUrl(url);
        } else {
            openInCurrentTab(url);
        }
    }

    private TextView postText(String value, ThreadPage page) {
        return postText(value, page, null);
    }

    private TextView postText(String value, ThreadPage page, String highlight) {
        TextView text = new TextView(this);
        SpannableString linkedText = decoratedPostText(value, page, highlight);
        text.setText(linkedText);
        text.setTextColor(textColor());
        text.setLinkTextColor(TEAL);
        text.setTextSize(15);
        text.setLineSpacing(0, 1.15f);
        text.setTextIsSelectable(false);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        installLinkTouchTracking(text);
        text.setOnLongClickListener(v -> showLinkCopyPopupIfAny(text));
        return text;
    }

    private SpannableString decoratedPostText(String value, ThreadPage page, String highlight) {
        SpannableString linkedText = new SpannableString(value == null ? "" : value);
        applySearchHighlights(linkedText, highlight);
        addLooseUrlSpans(linkedText);
        replaceReplySpans(linkedText, page);
        return linkedText;
    }

    private View postContent(String value, ThreadPage page) {
        return postContent(value, page, null);
    }

    private View postContent(String value, ThreadPage page, String highlight) {
        return postContent(value, page, highlight, null);
    }

    private View postContent(String value, ThreadPage page, String highlight, Runnable longClickAction) {
        return postContent(value, page, highlight, longClickAction, imgurLinks(value));
    }

    private View postContent(String value, ThreadPage page, String highlight, Runnable longClickAction,
                             List<ImgurLink> mediaLinks) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        TextView bodyText = postBodyText(value, page, highlight);
        bodyText.setTag(R.id.tag_post_swipe_text, true);
        if (longClickAction != null) {
            bodyText.setOnLongClickListener(v -> {
                if (showLinkCopyPopupIfAny(bodyText)) {
                    return true;
                }
                suppressNextLinkClick.add(bodyText);
                mainHandler.postDelayed(() -> suppressNextLinkClick.remove(bodyText), 1200);
                longClickAction.run();
                return true;
            });
            box.setOnLongClickListener(v -> {
                longClickAction.run();
                return true;
            });
        }
        box.addView(bodyText);

        if (!mediaLinks.isEmpty()) {
            box.addView(mediaGrid(mediaLinks, longClickAction));
        }
        if (aaDebugEnabled()) {
            box.addView(aaDebugView(value));
        }
        return box;
    }

    private View mediaGrid(List<ImgurLink> mediaLinks, Runnable longClickAction) {
        GridLayout grid = new GridLayout(this);
        int count = mediaLinks.size();
        int available = Math.max(dp(96), getResources().getDisplayMetrics().widthPixels - dp(56));
        int gap = dp(6);
        int cellSize = dp(MEDIA_GRID_CELL_DP);
        int columns = Math.max(1, Math.min(count, Math.max(1, (available + gap) / (cellSize + gap))));
        grid.setColumnCount(columns);
        grid.setPadding(0, dp(6), 0, dp(2));
        for (int i = 0; i < count; i++) {
            ImgurLink link = mediaLinks.get(i);
            View cell = deferredMediaPreview(link, longClickAction, cellSize);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cellSize;
            params.height = cellSize;
            params.setMargins(0, 0, (i % columns == columns - 1) ? 0 : gap, gap);
            grid.addView(cell, params);
        }
        return grid;
    }

    private TextView postBodyText(String value, ThreadPage page, String highlight) {
        TextView text = plainPostText(value);
        boolean immediate = highlight != null && !highlight.trim().isEmpty();
        if (immediate) {
            decoratePostTextNow(text, value, page, highlight);
        } else {
            decoratePostUrlsNow(text, value);
            deferPostTextDecoration(text, value, page, highlight);
        }
        return text;
    }

    private TextView plainPostText(String value) {
        TextView text = new TextView(this);
        text.setText(value == null ? "" : value);
        text.setTextColor(textColor());
        text.setLinkTextColor(TEAL);
        text.setTextSize(15);
        text.setLineSpacing(0, 1.15f);
        text.setTextIsSelectable(false);
        return text;
    }

    private void decoratePostTextNow(TextView text, String value, ThreadPage page, String highlight) {
        text.setText(decoratedPostText(value, page, highlight));
        text.setMovementMethod(LinkMovementMethod.getInstance());
        installLinkTouchTracking(text);
    }

    private void decoratePostUrlsNow(TextView text, String value) {
        SpannableString linkedText = new SpannableString(value == null ? "" : value);
        addLooseUrlSpans(linkedText);
        text.setText(linkedText);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        installLinkTouchTracking(text);
    }

    private void deferPostTextDecoration(TextView text, String value, ThreadPage page, String highlight) {
        DeferredTextDecoration decoration = new DeferredTextDecoration(text, value, page, highlight);
        text.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                if (!deferredTextDecorations.contains(decoration)) {
                    deferredTextDecorations.add(decoration);
                }
                scheduleDeferredTextDecorations();
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                deferredTextDecorations.remove(decoration);
            }
        });
        if (text.isAttachedToWindow()) {
            deferredTextDecorations.add(decoration);
            scheduleDeferredTextDecorations();
        }
    }

    private List<ImgurLink> imgurLinks(String value) {
        List<ImgurLink> links = new ArrayList<>();
        if (value == null || value.isEmpty() || !showMediaPreviews()) {
            return links;
        }
        Matcher matcher = URL_TEXT_PATTERN.matcher(value);
        Set<String> added = new LinkedHashSet<>();
        while (matcher.find()) {
            String cleanUrl = stripTrailingUrlPunctuation(matcher.group());
            ImgurLink media = previewMediaLink(cleanUrl);
            if (media != null && added.add(media.imageUrl)) {
                links.add(media);
            }
        }
        return links;
    }

    private List<ImgurLink> imgurLinks(Post post) {
        if (!showMediaPreviews()) {
            return new ArrayList<>();
        }
        if (post == null) {
            return new ArrayList<>();
        }
        if (post.cachedImgurLinks == null) {
            post.cachedImgurLinks = imgurLinks(post.body);
        }
        return post.cachedImgurLinks;
    }

    private View postBodyView(LinearLayout card, ThreadPage page, CuspTab tab, Post post) {
        return postBodyView(card, page, tab, post, 0);
    }

    private View postBodyView(LinearLayout card, ThreadPage page, CuspTab tab, Post post, int depth) {
        Runnable longClick = () -> {
            if (!isPostSwipeBlocked(post)) {
                showPostActionMenu(card, tab, post);
            }
        };
        if (!post.aaMode) {
            return postContent(post.body, page, tab.threadSearchQuery, longClick, imgurLinks(post));
        }
        TextView body = new TextView(this);
        String aaBody = aaDisplayBody(post);
        body.setText(aaBody);
        body.setTextColor(textColor());
        body.setTextSize(POST_TEXT_SIZE_SP);
        applyAaTypeface(body);
        body.setLineSpacing(0, AA_LINE_SPACING_MULTIPLIER);
        body.setSingleLine(false);
        body.setHorizontallyScrolling(true);
        body.setPadding(0, 0, 0, 0);
        body.setMinHeight(0);
        body.setMinimumHeight(0);
        body.setOnLongClickListener(v -> {
            if (showLinkCopyPopupIfAny(body)) {
                return true;
            }
            suppressNextLinkClick.add(body);
            mainHandler.postDelayed(() -> suppressNextLinkClick.remove(body), 1200);
            longClick.run();
            return true;
        });
        if (tab.threadSearchQuery != null && !tab.threadSearchQuery.trim().isEmpty()) {
            decoratePostTextNow(body, aaBody, page, tab.threadSearchQuery);
        } else {
            deferPostTextDecoration(body, aaBody, page, null);
        }
        int[] lastAaWidth = new int[]{0};
        int estimatedWidth = estimatePostTextWidth(depth);
        if (estimatedWidth > 0) {
            lastAaWidth[0] = estimatedWidth;
            fitAaTextSize(body, post, estimatedWidth);
        }
        body.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int width = Math.max(0, right - left);
            if (width != lastAaWidth[0]) {
                lastAaWidth[0] = width;
                fitAaTextSize(body, post);
            }
        });
        body.post(() -> {
            lastAaWidth[0] = body.getWidth();
            fitAaTextSize(body, post);
        });
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.addView(body, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        List<ImgurLink> mediaLinks = imgurLinks(post);
        if (!mediaLinks.isEmpty()) {
            box.addView(mediaGrid(mediaLinks, longClick));
        }
        if (aaDebugEnabled()) {
            box.addView(aaDebugView(post.body));
        }
        return box;
    }

    private void applyAaTypeface(TextView body) {
        body.setTypeface(aaTypeface());
        body.setIncludeFontPadding(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            body.setLetterSpacing(0f);
        }
    }

    private static String trimTrailingBlankLines(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        int end = value.length();
        while (end > 0) {
            int lineEnd = end;
            while (lineEnd > 0) {
                char ch = value.charAt(lineEnd - 1);
                if (ch != '\n' && ch != '\r') {
                    break;
                }
                lineEnd--;
            }
            int lineStart = lineEnd;
            while (lineStart > 0) {
                char ch = value.charAt(lineStart - 1);
                if (ch == '\n' || ch == '\r') {
                    break;
                }
                lineStart--;
            }
            boolean blank = true;
            for (int i = lineStart; i < lineEnd; i++) {
                char ch = value.charAt(i);
                if (ch != ' ' && ch != '\t' && ch != '\u3000') {
                    blank = false;
                    break;
                }
            }
            if (!blank) {
                return value.substring(0, lineEnd);
            }
            end = lineStart;
            while (end > 0) {
                char ch = value.charAt(end - 1);
                if (ch != '\n' && ch != '\r') {
                    break;
                }
                end--;
            }
        }
        return "";
    }

    private Typeface aaTypeface() {
        if (aaTypeface != null) {
            return aaTypeface;
        }
        try {
            aaTypeface = Typeface.createFromAsset(getAssets(), "fonts/textar.ttf");
        } catch (Exception ignored) {
            aaTypeface = Typeface.create(AA_FONT_FAMILY, Typeface.NORMAL);
        }
        return aaTypeface;
    }

    private void fitAaTextSize(TextView body) {
        fitAaTextSize(body, null);
    }

    private void fitAaTextSize(TextView body, Post post) {
        int available = body.getWidth() - body.getPaddingLeft() - body.getPaddingRight();
        fitAaTextSize(body, post, available);
    }

    private void fitAaTextSize(TextView body, Post post, int available) {
        if (available <= 0) {
            return;
        }
        float baseSize = aaBaseTextSizePx();
        if (post != null && post.cachedAaFitWidth == available && post.cachedAaFitTextSizePx > 0f) {
            boolean changed = applyAaTextSizeIfNeeded(body, post.cachedAaFitTextSizePx);
            body.setLineSpacing(0, AA_LINE_SPACING_MULTIPLIER);
            body.setMinHeight(0);
            body.setMinimumHeight(0);
            if (changed) {
                body.requestLayout();
            }
            return;
        }
        body.setTextScaleX(1f);
        float longest = post != null && post.cachedAaLongestLineWidthPx > 0f
                ? post.cachedAaLongestLineWidthPx
                : longestLineWidth(body, post == null ? body.getText().toString() : aaDisplayBody(post), baseSize);
        if (post != null) {
            post.cachedAaLongestLineWidthPx = longest;
        }
        if (longest <= 0f) {
            return;
        }
        float targetSize = baseSize;
        if (longest > available) {
            targetSize = Math.max(1f, baseSize * Math.max(1, available - 1) / longest);
        }
        if (post != null) {
            post.cachedAaFitWidth = available;
            post.cachedAaFitTextSizePx = targetSize;
        }
        boolean changed = applyAaTextSizeIfNeeded(body, targetSize);
        body.setLineSpacing(0, AA_LINE_SPACING_MULTIPLIER);
        body.setMinHeight(0);
        body.setMinimumHeight(0);
        if (changed) {
            body.requestLayout();
        }
    }

    private int renderedSlotContentHeight(FrameLayout holder) {
        if (holder == null) {
            return 0;
        }
        if (holder.getChildCount() == 0) {
            return holder.getHeight();
        }
        View child = holder.getChildAt(0);
        int childHeight = child.getHeight();
        if (childHeight > 0) {
            return childHeight;
        }
        int width = holder.getWidth();
        if (width > 0) {
            child.measure(
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int measured = child.getMeasuredHeight();
            if (measured > 0) {
                return measured;
            }
        }
        return childHeight > 0 ? childHeight : holder.getHeight();
    }

    private boolean applyAaTextSizeIfNeeded(TextView body, float textSizePx) {
        if (Math.abs(body.getTextSize() - textSizePx) > 0.5f) {
            body.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
            return true;
        }
        return false;
    }

    private float aaBaseTextSizePx() {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, POST_TEXT_SIZE_SP, getResources().getDisplayMetrics());
    }

    private void collectVirtualPostSlotsInRange(ViewGroup list, int start, int end, Set<FrameLayout> keep) {
        if (list == null || keep == null || start > end || end < 0) {
            return;
        }
        int childCount = list.getChildCount();
        for (int i = Math.max(0, start); i <= end && i < childCount; i++) {
            View child = list.getChildAt(i);
            if (child instanceof FrameLayout && child.getTag() instanceof VirtualPostSlot) {
                keep.add((FrameLayout) child);
            }
        }
    }

    private String aaDisplayBody(Post post) {
        if (post == null) {
            return "";
        }
        if (post.cachedAaBody == null) {
            post.cachedAaBody = trimTrailingBlankLines(post.body);
        }
        return post.cachedAaBody;
    }

    private TextPaint aaMeasurePaint(float textSizePx) {
        TextPaint paint = new TextPaint();
        paint.setTypeface(aaTypeface());
        paint.setTextSize(textSizePx);
        return paint;
    }

    private float longestLineWidth(TextView body, String text, float textSizePx) {
        TextPaint paint = new TextPaint(body.getPaint());
        paint.setTextSize(textSizePx);
        return longestLineWidth(paint, text);
    }

    private float longestLineWidth(TextPaint paint, String text) {
        float longest = 0f;
        String value = text == null ? "" : text;
        int start = 0;
        int length = value.length();
        for (int i = 0; i <= length; i++) {
            if (i == length || value.charAt(i) == '\n') {
                int end = i;
                if (end > start && value.charAt(end - 1) == '\r') {
                    end--;
                }
                longest = Math.max(longest, paint.measureText(value, start, end));
                start = i + 1;
            }
        }
        return longest;
    }

    private void toggleAaMode(CuspTab tab, Post post) {
        toggleAaMode(tab, post, null);
    }

    private void toggleAaMode(CuspTab tab, Post post, View preferredCard) {
        if (tab == null || post == null) {
            return;
        }
        View cardView = preferredCard instanceof LinearLayout ? preferredCard
                : (tab.postViews == null ? null : tab.postViews.get(post.number));
        if (!(cardView instanceof LinearLayout)) {
            return;
        }
        post.aaMode = !post.aaMode;
        saveAaPost(preferences, tab.threadPage.url, post.number, post.aaMode);
        LinearLayout card = (LinearLayout) cardView;
        if (card.getChildCount() >= 2) {
            card.removeViewAt(1);
            card.addView(postBodyView(card, tab.threadPage, tab, post), 1);
            View holder = (View) card.getParent();
            if (holder != null && holder.getParent() instanceof FrameLayout
                    && ((View) holder.getParent()).getTag() instanceof VirtualPostSlot) {
                View slotHolder = (View) holder.getParent();
                ViewGroup.LayoutParams params = slotHolder.getLayoutParams();
                if (params != null) {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    slotHolder.setLayoutParams(params);
                }
            }
        }
    }

    private static boolean likelyAaPost(String body) {
        return aaDebugMetrics(body).aa;
    }

    private View aaDebugView(String body) {
        TextView view = new TextView(this);
        view.setText(aaDebugMetrics(body).debugText());
        view.setTextColor(mutedColor());
        view.setTextSize(11);
        view.setLineSpacing(0, 1.05f);
        view.setPadding(0, dp(6), 0, dp(2));
        return view;
    }

    private static AaDebugMetrics aaDebugMetrics(String body) {
        if (body == null) {
            return new AaDebugMetrics(false, "null", 0, 0, 0, 0f);
        }
        String value = body.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = value.split("\\n", -1);
        int candidateLines = 0;
        int targetChars = 0;
        int specialChars = 0;
        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            if (!isAaCandidateLine(line)) {
                continue;
            }
            candidateLines++;
            for (int i = 0; i < line.length(); ) {
                int codePoint = line.codePointAt(i);
                targetChars++;
                if (isAaSpecialChar(codePoint)) {
                    specialChars++;
                }
                i += Character.charCount(codePoint);
            }
        }
        if (candidateLines < 3) {
            return new AaDebugMetrics(false, "candidate-lines<3", candidateLines,
                    targetChars, specialChars, 0f);
        }
        if (targetChars <= 0) {
            return new AaDebugMetrics(false, "no-candidate-chars", candidateLines,
                    targetChars, specialChars, 0f);
        }
        float ratio = specialChars / (float) targetChars;
        boolean aa = ratio > AA_SPECIAL_CHAR_RATIO_THRESHOLD;
        return new AaDebugMetrics(aa, aa ? "special-char-ratio" : "below",
                candidateLines, targetChars, specialChars, ratio);
    }

    private static boolean isAaCandidateLine(String line) {
        int first = line.codePointAt(0);
        if (isAaSpaceChar(first) || first == '.' || first == '\uff0e') {
            return true;
        }
        boolean previousSpace = false;
        for (int i = 0; i < line.length(); ) {
            int codePoint = line.codePointAt(i);
            boolean currentSpace = isAaSpaceChar(codePoint);
            if (previousSpace && currentSpace) {
                return true;
            }
            previousSpace = currentSpace;
            i += Character.charCount(codePoint);
        }
        return false;
    }

    private static boolean isAaSpaceChar(int codePoint) {
        if (Character.isWhitespace(codePoint)) {
            return true;
        }
        switch (Character.getType(codePoint)) {
            case Character.SPACE_SEPARATOR:
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
                return true;
            default:
                return false;
        }
    }

    private static boolean isAaSpecialChar(int codePoint) {
        if (isAaSpaceChar(codePoint)) {
            return true;
        }
        switch (Character.getType(codePoint)) {
            case Character.CONNECTOR_PUNCTUATION:
            case Character.DASH_PUNCTUATION:
            case Character.START_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.INITIAL_QUOTE_PUNCTUATION:
            case Character.FINAL_QUOTE_PUNCTUATION:
            case Character.OTHER_PUNCTUATION:
            case Character.MATH_SYMBOL:
            case Character.CURRENCY_SYMBOL:
            case Character.MODIFIER_SYMBOL:
            case Character.OTHER_SYMBOL:
                return true;
            default:
                return false;
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
            text.setSpan(new BackgroundColorSpan(Theme.searchHighlight(this)),
                    index, index + needle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            index = haystack.indexOf(needle, index + needle.length());
        }
    }

    private View imgurPreview(String originalUrl, String imageUrl, Runnable longClickAction, int cellSize) {
        return MediaPreviewHelper.create(this, preferences, ioExecutor, mainHandler,
                originalUrl, imageUrl, false, cellSize, longClickAction, mediaPreviewCallbacks());
    }

    private TextView mediaPlayOverlay() {
        TextView play = new TextView(this);
        play.setText("\u25b6");
        play.setTextColor(Color.WHITE);
        play.setTextSize(34);
        play.setGravity(Gravity.CENTER);
        play.setBackgroundColor(Color.argb(82, 0, 0, 0));
        return play;
    }

    private View videoPreview(String originalUrl, String videoUrl, Runnable longClickAction, int cellSize) {
        return MediaPreviewHelper.create(this, preferences, ioExecutor, mainHandler,
                originalUrl, videoUrl, true, cellSize, longClickAction, mediaPreviewCallbacks());
    }

    private MediaPreviewHelper.Callback mediaPreviewCallbacks() {
        return new MediaPreviewHelper.Callback() {
            @Override
            public void openImage(String originalUrl, String mediaUrl) {
                showImageViewer(originalUrl, mediaUrl);
            }

            @Override
            public void openVideo(String originalUrl, String mediaUrl) {
                showVideoViewer(originalUrl, mediaUrl);
            }

            @Override
            public void openExternal(String url) {
                MainActivity.this.openExternal(url);
            }
        };
    }

    private View deferredMediaPreview(ImgurLink link, Runnable longClickAction, int cellSize) {
        FrameLayout placeholder = new FrameLayout(this);
        placeholder.setClickable(true);
        placeholder.setClipToOutline(true);
        placeholder.setBackgroundColor(Theme.dark(this) ? Color.rgb(15, 23, 42) : Color.rgb(241, 245, 249));
        if (longClickAction != null) {
            placeholder.setOnLongClickListener(v -> {
                longClickAction.run();
                return true;
            });
        }
        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        spinner.setAlpha(0.55f);
        FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(dp(28), dp(28));
        spinnerParams.gravity = Gravity.CENTER;
        placeholder.addView(spinner, spinnerParams);
        DeferredMediaPreview preview = new DeferredMediaPreview(link, placeholder, longClickAction, cellSize);
        placeholder.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                if (!deferredMediaPreviews.contains(preview)) {
                    deferredMediaPreviews.add(preview);
                }
                scheduleDeferredMediaLoads();
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                deferredMediaPreviews.remove(preview);
            }
        });
        if (placeholder.isAttachedToWindow()) {
            deferredMediaPreviews.add(preview);
            scheduleDeferredMediaLoads();
        }
        return placeholder;
    }

    private void scheduleThreadMediaLoads() {
        scheduleDeferredTextDecorations();
        scheduleDeferredMediaLoads();
        scheduleLazyImgurLoads();
    }

    private void scheduleDeferredTextDecorations() {
        if (deferredTextTask != null) {
            return;
        }
        deferredTextTask = () -> {
            deferredTextTask = null;
            runDeferredTextDecorations();
        };
        mainHandler.postDelayed(deferredTextTask, 80);
    }

    private void runDeferredTextDecorations() {
        CuspTab current = currentTab();
        if (recentlyScrolled(current)) {
            scheduleDeferredTextDecorations();
            return;
        }
        for (int i = deferredTextDecorations.size() - 1; i >= 0; i--) {
            DeferredTextDecoration decoration = deferredTextDecorations.get(i);
            if (!decoration.text.isAttachedToWindow()) {
                deferredTextDecorations.remove(i);
            }
        }
        int usedBudget = 0;
        for (DeferredTextDecoration decoration : new ArrayList<>(deferredTextDecorations)) {
            if (decoration.decorated || !isNearViewport(decoration.text)) {
                continue;
            }
            int cost = deferredTextDecorationCost(decoration.value);
            if (usedBudget > 0 && usedBudget + cost > DEFERRED_TEXT_DECORATION_BUDGET) {
                break;
            }
            decoration.decorated = true;
            decoratePostTextNow(decoration.text, decoration.value, decoration.page, decoration.highlight);
            deferredTextDecorations.remove(decoration);
            usedBudget += cost;
            if (usedBudget >= DEFERRED_TEXT_DECORATION_BUDGET) {
                break;
            }
        }
        if (usedBudget > 0 && !deferredTextDecorations.isEmpty()) {
            scheduleDeferredTextDecorations();
        }
    }

    private int deferredTextDecorationCost(String value) {
        if (value == null || value.isEmpty()) {
            return 1;
        }
        int cost = 1;
        int lines = bodyLineCount(value);
        if (maybeHeavyAaBody(value) || value.length() > 1000 || lines > 16) {
            cost += 2;
        }
        if (value.length() > 3000 || lines > 45) {
            cost += 2;
        }
        return Math.min(DEFERRED_TEXT_DECORATION_BUDGET, cost);
    }

    private void scheduleDeferredMediaLoads() {
        if (deferredMediaTask != null) {
            return;
        }
        deferredMediaTask = () -> {
            deferredMediaTask = null;
            runDeferredMediaLoads();
        };
        mainHandler.postDelayed(deferredMediaTask, 120);
    }

    private void runDeferredMediaLoads() {
        CuspTab current = currentTab();
        if (recentlyScrolled(current)) {
            scheduleDeferredMediaLoads();
            return;
        }
        for (int i = deferredMediaPreviews.size() - 1; i >= 0; i--) {
            DeferredMediaPreview preview = deferredMediaPreviews.get(i);
            if (!preview.placeholder.isAttachedToWindow()) {
                deferredMediaPreviews.remove(i);
            }
        }
        int created = 0;
        for (DeferredMediaPreview preview : new ArrayList<>(deferredMediaPreviews)) {
            if (preview.created || !isInViewport(preview.placeholder)) {
                continue;
            }
            preview.created = true;
            ViewParent parent = preview.placeholder.getParent();
            if (!(parent instanceof ViewGroup)) {
                deferredMediaPreviews.remove(preview);
                continue;
            }
            ViewGroup group = (ViewGroup) parent;
            int index = group.indexOfChild(preview.placeholder);
            ViewGroup.LayoutParams params = preview.placeholder.getLayoutParams();
            View media = preview.link.video
                    ? videoPreview(preview.link.originalUrl, preview.link.imageUrl, preview.longClickAction, preview.cellSize)
                    : imgurPreview(preview.link.originalUrl, preview.link.imageUrl, preview.longClickAction, preview.cellSize);
            group.removeView(preview.placeholder);
            group.addView(media, Math.max(0, index), params);
            deferredMediaPreviews.remove(preview);
            created++;
            if (created >= 1) {
                break;
            }
        }
        if (created > 0 && !deferredMediaPreviews.isEmpty()) {
            scheduleDeferredMediaLoads();
        }
    }

    private boolean recentlyScrolled(CuspTab tab) {
        return tab != null && android.os.SystemClock.uptimeMillis() - tab.lastScrollAt < 220;
    }

    private TextView unavailableMediaLabel(String message) {
        TextView label = new TextView(this);
        label.setText(message);
        label.setTextColor(mutedColor());
        label.setGravity(Gravity.CENTER);
        label.setTextSize(16);
        label.setPadding(dp(8), dp(8), dp(8), dp(8));
        label.setBackgroundColor(unavailableMediaColor());
        return label;
    }

    private int unavailableMediaColor() {
        return Theme.dark(this) ? Color.rgb(24, 24, 27) : Color.rgb(226, 232, 240);
    }

    private void scheduleLazyImgurLoads() {
        if (imgurLoadInFlight || lazyImgurTask != null) {
            return;
        }
        lazyImgurTask = () -> {
            lazyImgurTask = null;
            runLazyImgurLoads();
        };
        mainHandler.postDelayed(lazyImgurTask, 90);
    }

    private void runLazyImgurLoads() {
        if (imgurLoadInFlight) {
            return;
        }
        CuspTab current = currentTab();
        if (recentlyScrolled(current)) {
            scheduleLazyImgurLoads();
            return;
        }
        for (int i = lazyImgurPreviews.size() - 1; i >= 0; i--) {
            LazyImgurPreview preview = lazyImgurPreviews.get(i);
            if (!preview.frame.isAttachedToWindow()) {
                lazyImgurPreviews.remove(i);
            }
        }
        for (LazyImgurPreview preview : new ArrayList<>(lazyImgurPreviews)) {
            if (!preview.started && isInViewport(preview.frame)) {
                startLazyImgurLoad(preview);
                return;
            }
        }
    }

    private boolean isNearViewport(View view) {
        if (view == null || !view.isShown()) {
            return false;
        }
        Rect visible = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(visible);
        int buffer = Math.max(dp(240), visible.height() / 2);
        visible.top -= buffer;
        visible.bottom += buffer;
        Rect bounds = new Rect();
        return view.getGlobalVisibleRect(bounds) && Rect.intersects(visible, bounds);
    }

    private boolean isInViewport(View view) {
        if (view == null || !view.isShown()) {
            return false;
        }
        Rect visible = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(visible);
        Rect bounds = new Rect();
        return view.getGlobalVisibleRect(bounds) && Rect.intersects(visible, bounds);
    }

    private void startLazyImgurLoad(LazyImgurPreview preview) {
        preview.started = true;
        imgurLoadInFlight = true;
        ioExecutor.execute(() -> {
            ImageLoadResult loaded = downloadBitmap(preview.imageUrl, getResources().getDisplayMetrics().widthPixels, dp(176));
            Bitmap bitmap = loaded == null ? null : loaded.bitmap;
            Drawable drawable = loaded == null ? null : loaded.drawable;
            boolean sensitive = false;
            Bitmap displayBitmap = bitmap;
            boolean gif = isGifUrl(preview.imageUrl);
            if (bitmap != null) {
                Boolean cachedSensitive = readCachedImageSensitive(preview.imageUrl);
                if (loaded.missing) {
                    sensitive = false;
                } else if (cachedSensitive != null) {
                    sensitive = cachedSensitive;
                } else {
                    sensitive = isGraphicViolenceImage(bitmap);
                    saveImageSensitive(preview.imageUrl, sensitive);
                }
                if ((gif ? blurGifThumbnails() : blurImgurImages()) && sensitive) {
                    displayBitmap = blurredBitmap(bitmap);
                }
            }
            Bitmap finalBitmap = bitmap;
            Drawable finalDrawable = drawable;
            Bitmap finalDisplayBitmap = displayBitmap;
            boolean finalSensitive = sensitive;
            boolean finalGif = gif;
            runOnUiThread(() -> {
                applyLazyImgurLoadResultWhenIdle(preview, finalBitmap, finalDrawable, finalDisplayBitmap, finalSensitive, finalGif);
            });
        });
    }

    private void applyLazyImgurLoadResultWhenIdle(LazyImgurPreview preview, Bitmap bitmap, Drawable drawable,
                                                  Bitmap displayBitmap, boolean sensitive, boolean gif) {
        if (recentlyScrolled(currentTab())) {
            mainHandler.postDelayed(() -> applyLazyImgurLoadResultWhenIdle(preview, bitmap, drawable, displayBitmap, sensitive, gif), 180);
            return;
        }
        imgurLoadInFlight = false;
        if (!preview.frame.isAttachedToWindow()) {
            lazyImgurPreviews.remove(preview);
            scheduleLazyImgurLoads();
            return;
        }
        preview.spinner.setVisibility(View.GONE);
        if (bitmap == null && drawable == null) {
            preview.error.setVisibility(View.VISIBLE);
            scheduleLazyImgurLoads();
            return;
        }
        boolean shouldBlur = (gif ? blurGifThumbnails() : blurImgurImages()) && sensitive;
        if (drawable != null && gif && shouldBlur && bitmap != null) {
            preview.image.setImageBitmap(displayBitmap);
        } else if (drawable != null && (!gif || autoplayGifs())) {
            preview.image.setImageDrawable(drawable);
            startAnimatedDrawable(drawable);
        } else {
            preview.image.setImageBitmap(displayBitmap);
        }
        preview.image.setVisibility(View.VISIBLE);
        if (drawable != null && gif && shouldBlur && bitmap != null) {
            positionRevealButton(preview.frame, preview.reveal, bitmap);
            preview.reveal.setVisibility(View.VISIBLE);
            preview.reveal.setOnClickListener(v -> {
                if (autoplayGifs()) {
                    preview.image.setImageDrawable(drawable);
                    startAnimatedDrawable(drawable);
                } else {
                    preview.image.setImageBitmap(bitmap);
                    showGifPlayButton(preview, drawable);
                }
                preview.image.setOnClickListener(click -> showImageViewer(preview.originalUrl, preview.imageUrl));
                preview.reveal.setVisibility(View.GONE);
            });
        } else if (drawable != null) {
            if (gif && !autoplayGifs()) {
                showGifPlayButton(preview, drawable);
            }
            preview.image.setOnClickListener(v -> showImageViewer(preview.originalUrl, preview.imageUrl));
        } else if (shouldBlur && sensitive) {
            positionRevealButton(preview.frame, preview.reveal, bitmap);
            preview.reveal.setVisibility(View.VISIBLE);
            preview.reveal.setOnClickListener(v -> {
                preview.image.setImageBitmap(bitmap);
                preview.image.setOnClickListener(click -> showImageViewer(preview.originalUrl, preview.imageUrl));
                preview.reveal.setVisibility(View.GONE);
            });
        } else if (!shouldBlur) {
            preview.image.setOnClickListener(v -> showImageViewer(preview.originalUrl, preview.imageUrl));
        }
        scheduleLazyImgurLoads();
    }

    private void showGifPlayButton(LazyImgurPreview preview, Drawable drawable) {
        preview.play.setVisibility(View.VISIBLE);
        preview.play.setOnClickListener(v -> {
            preview.image.setImageDrawable(drawable);
            startAnimatedDrawable(drawable, true);
            preview.play.setVisibility(View.GONE);
            preview.image.setOnClickListener(click -> showImageViewer(preview.originalUrl, preview.imageUrl));
        });
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

        TextView play = mediaPlayOverlay();
        play.setVisibility(View.GONE);
        overlay.addView(play, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

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

        ImageButton download = iconButton(R.drawable.ic_download, text("\u753b\u50cf\u3092\u4fdd\u5b58", "Download image"),
                v -> downloadImgurImage(imageUrl));
        download.setColorFilter(Color.WHITE);
        download.setBackgroundColor(Color.argb(80, 0, 0, 0));
        FrameLayout.LayoutParams downloadParams = new FrameLayout.LayoutParams(dp(48), dp(48));
        downloadParams.gravity = Gravity.TOP | Gravity.RIGHT;
        downloadParams.setMargins(0, dp(18), dp(122), 0);
        overlay.addView(download, downloadParams);

        addContentView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ioExecutor.execute(() -> {
            ImageLoadResult loaded = downloadBitmap(imageUrl,
                    getResources().getDisplayMetrics().widthPixels * 3,
                    getResources().getDisplayMetrics().heightPixels * 3);
            runOnUiThread(() -> {
                spinner.setVisibility(View.GONE);
                Bitmap bitmap = loaded == null ? null : loaded.bitmap;
                Drawable drawable = loaded == null ? null : loaded.drawable;
                if (bitmap == null && drawable == null) {
                    Toast.makeText(this, "Image failed to load.", Toast.LENGTH_SHORT).show();
                    closeImageViewer();
                    return;
                }
                boolean gif = isGifUrl(imageUrl);
                if (drawable != null && gif && !autoplayGifs()) {
                    if (bitmap != null) {
                        image.setImageBitmap(bitmap);
                    } else {
                        image.setImageDrawable(drawable);
                    }
                    play.setVisibility(View.VISIBLE);
                    play.setOnClickListener(v -> {
                        image.setImageDrawable(drawable);
                        startAnimatedDrawable(drawable, true);
                        play.setVisibility(View.GONE);
                    });
                } else if (drawable != null) {
                    image.setImageDrawable(drawable);
                    startAnimatedDrawable(drawable);
                } else {
                    image.setImageBitmap(bitmap);
                }
            });
        });
    }

    private void showVideoViewer(String originalUrl, String videoUrl) {
        clearAddressFocus();
        FrameLayout overlay = new FrameLayout(this);
        imageOverlay = overlay;
        overlay.setBackgroundColor(Color.BLACK);
        overlay.setClickable(true);

        VideoView video = new VideoView(this);
        video.setVideoURI(Uri.parse(videoUrl));
        overlay.addView(video, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        MediaController controller = new MediaController(this);
        controller.setAnchorView(video);
        video.setMediaController(controller);

        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        FrameLayout.LayoutParams spinnerParams = new FrameLayout.LayoutParams(dp(44), dp(44));
        spinnerParams.gravity = Gravity.CENTER;
        overlay.addView(spinner, spinnerParams);

        ImageButton close = iconButton(R.drawable.ic_close, text("\u52d5\u753b\u3092\u9589\u3058\u308b", "Close video"), v -> closeImageViewer());
        close.setColorFilter(Color.WHITE);
        close.setBackgroundColor(Color.argb(80, 0, 0, 0));
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(dp(48), dp(48));
        closeParams.gravity = Gravity.TOP | Gravity.RIGHT;
        closeParams.setMargins(0, dp(18), dp(14), 0);
        overlay.addView(close, closeParams);

        ImageButton open = iconButton(R.drawable.ic_arrow_forward, text("\u52d5\u753b\u30ea\u30f3\u30af\u3092\u958b\u304f", "Open video link"),
                v -> openExternal(originalUrl));
        open.setColorFilter(Color.WHITE);
        open.setBackgroundColor(Color.argb(80, 0, 0, 0));
        FrameLayout.LayoutParams openParams = new FrameLayout.LayoutParams(dp(48), dp(48));
        openParams.gravity = Gravity.TOP | Gravity.RIGHT;
        openParams.setMargins(0, dp(18), dp(68), 0);
        overlay.addView(open, openParams);

        ImageButton download = iconButton(R.drawable.ic_download, text("\u52d5\u753b\u3092\u4fdd\u5b58", "Download video"),
                v -> downloadImgurImage(videoUrl));
        download.setColorFilter(Color.WHITE);
        download.setBackgroundColor(Color.argb(80, 0, 0, 0));
        FrameLayout.LayoutParams downloadParams = new FrameLayout.LayoutParams(dp(48), dp(48));
        downloadParams.gravity = Gravity.TOP | Gravity.RIGHT;
        downloadParams.setMargins(0, dp(18), dp(122), 0);
        overlay.addView(download, downloadParams);

        video.setOnPreparedListener(player -> {
            spinner.setVisibility(View.GONE);
            centerVideoView(video, player.getVideoWidth(), player.getVideoHeight());
            video.start();
            controller.show();
        });
        video.setOnErrorListener((player, what, extra) -> {
            spinner.setVisibility(View.GONE);
            Toast.makeText(this, "Video failed to load.", Toast.LENGTH_SHORT).show();
            return true;
        });

        addContentView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void centerVideoView(VideoView video, int videoWidth, int videoHeight) {
        if (video == null || videoWidth <= 0 || videoHeight <= 0) {
            return;
        }
        View parent = (View) video.getParent();
        int availableWidth = parent == null || parent.getWidth() <= 0
                ? getResources().getDisplayMetrics().widthPixels
                : parent.getWidth();
        int availableHeight = parent == null || parent.getHeight() <= 0
                ? getResources().getDisplayMetrics().heightPixels
                : parent.getHeight();
        float scale = Math.min(availableWidth / (float) videoWidth, availableHeight / (float) videoHeight);
        int width = Math.max(1, Math.round(videoWidth * scale));
        int height = Math.max(1, Math.round(videoHeight * scale));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height, Gravity.CENTER);
        video.setLayoutParams(params);
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

    private void downloadImgurImage(String imageUrl) {
        Toast.makeText(this, text("\u753b\u50cf\u3092\u4fdd\u5b58\u4e2d", "Saving image..."), Toast.LENGTH_SHORT).show();
        ioExecutor.execute(() -> {
            String error = null;
            String savedName = null;
            try {
                DownloadedImageBytes image = downloadOriginalImageBytes(imageUrl);
                if (image == null || image.bytes == null || image.bytes.length == 0) {
                    throw new IllegalStateException(text("\u753b\u50cf\u3092\u53d6\u5f97\u3067\u304d\u307e\u305b\u3093", "Image could not be downloaded."));
                }
                String mime = imageMimeType(image.url, image.bytes);
                savedName = imgurFileName(image.url, mime);
                saveImageBytesToPictures(savedName, mime, image.bytes);
            } catch (Exception exception) {
                error = exception.getMessage() == null
                        ? text("\u4fdd\u5b58\u306b\u5931\u6557\u3057\u307e\u3057\u305f", "Save failed.")
                        : exception.getMessage();
            }
            String finalError = error;
            String finalSavedName = savedName;
            runOnUiThread(() -> {
                if (finalError == null) {
                    Toast.makeText(this, text("\u753b\u50cf\u3092\u4fdd\u5b58\u3057\u307e\u3057\u305f", "Image saved.") + "\n" + finalSavedName,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, finalError, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private DownloadedImageBytes downloadOriginalImageBytes(String imageUrl) {
        List<String> candidates = new ArrayList<>();
        candidates.add(imageUrl);
        for (String candidate : candidates) {
            if (AppCache.enabled(preferences)) {
                try {
                    File cached = imageCacheFile(candidate);
                    if (cached.exists() && cached.length() > 0 && !isCachedImageMissing(candidate)) {
                        cached.setLastModified(System.currentTimeMillis());
                        return new DownloadedImageBytes(candidate, readFileBytes(cached));
                    }
                } catch (Exception ignored) {
                }
            }
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(candidate).openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(20000);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestProperty("User-Agent", "CuspiDroid/0.1");
                int code = connection.getResponseCode();
                String contentType = connection.getContentType();
                if (code >= 400 || isImgurMissingResponse(connection, contentType)) {
                    continue;
                }
                try (InputStream stream = connection.getInputStream()) {
                    byte[] bytes = readBytes(stream);
                    if (looksLikeImgurMissing(bytes, contentType)) {
                        continue;
                    }
                    cacheImageBytes(candidate, bytes);
                    return new DownloadedImageBytes(candidate, bytes);
                }
            } catch (Exception ignored) {
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return null;
    }

    private void saveImageBytesToPictures(String fileName, String mime, byte[] bytes) throws Exception {
        boolean video = mime != null && mime.startsWith("video/");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(video ? MediaStore.Video.Media.MIME_TYPE : MediaStore.Images.Media.MIME_TYPE, mime);
            values.put(video ? MediaStore.Video.Media.RELATIVE_PATH : MediaStore.Images.Media.RELATIVE_PATH,
                    (video ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES) + "/CuspiDroid");
            values.put(video ? MediaStore.Video.Media.IS_PENDING : MediaStore.Images.Media.IS_PENDING, 1);
            Uri uri = getContentResolver().insert(video
                    ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    : MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                throw new IllegalStateException(text("\u4fdd\u5b58\u5148\u3092\u4f5c\u6210\u3067\u304d\u307e\u305b\u3093", "Could not create destination."));
            }
            try (OutputStream output = getContentResolver().openOutputStream(uri)) {
                if (output == null) {
                    throw new IllegalStateException(text("\u4fdd\u5b58\u5148\u3092\u958b\u3051\u307e\u305b\u3093", "Could not open destination."));
                }
                output.write(bytes);
            }
            values.clear();
            values.put(video ? MediaStore.Video.Media.IS_PENDING : MediaStore.Images.Media.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);
            return;
        }
        File baseDir = getExternalFilesDir(video ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES);
        if (baseDir == null) {
            throw new IllegalStateException(text("\u4fdd\u5b58\u5148\u3092\u958b\u3051\u307e\u305b\u3093", "Could not open destination."));
        }
        File dir = new File(baseDir, "CuspiDroid");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException(text("\u4fdd\u5b58\u5148\u3092\u4f5c\u6210\u3067\u304d\u307e\u305b\u3093", "Could not create destination."));
        }
        File file = new File(dir, fileName);
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(bytes);
        }
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }

    private String imageMimeType(String url, byte[] bytes) {
        String lower = url == null ? "" : url.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".mp4") || lower.endsWith(".m4v")) {
            return "video/mp4";
        }
        if (lower.endsWith(".webm")) {
            return "video/webm";
        }
        if (lower.endsWith(".mov")) {
            return "video/quicktime";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (bytes != null && bytes.length >= 12
                && bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47) {
            return "image/png";
        }
        if (bytes != null && bytes.length >= 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private String imgurFileName(String url, String mime) {
        String ext = ".jpg";
        if ("video/mp4".equals(mime)) {
            ext = ".mp4";
        } else if ("video/webm".equals(mime)) {
            ext = ".webm";
        } else if ("video/quicktime".equals(mime)) {
            ext = ".mov";
        } else if ("image/png".equals(mime)) {
            ext = ".png";
        } else if ("image/webp".equals(mime)) {
            ext = ".webp";
        } else if ("image/gif".equals(mime)) {
            ext = ".gif";
        }
        String id = "imgur";
        try {
            String path = Uri.parse(url).getLastPathSegment();
            if (path != null && !path.trim().isEmpty()) {
                int dot = path.lastIndexOf('.');
                id = dot > 0 ? path.substring(0, dot) : path;
            }
        } catch (Exception ignored) {
        }
        return "CuspiDroid-" + id + "-" + System.currentTimeMillis() + ext;
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
        if (url.startsWith("https://i.imgur.com/") && url.endsWith(".gifv")) {
            return downloadBitmapOnce(url.substring(0, url.length() - 5) + ".mp4", maxWidth, maxHeight);
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
                saveImageMissing(url);
                Bitmap missing = missingImgurBitmap(maxWidth, maxHeight);
                return missing == null ? null : new ImageLoadResult(missing, true);
            }
            try (InputStream stream = connection.getInputStream()) {
                byte[] bytes = readBytes(stream);
                if (looksLikeImgurMissing(bytes, contentType)) {
                    saveImageMissing(url);
                    Bitmap missing = missingImgurBitmap(maxWidth, maxHeight);
                    return missing == null ? null : new ImageLoadResult(missing, true);
                }
                cacheImageBytes(url, bytes);
                Drawable drawable = decodeAnimatedDrawableIfPossible(url, bytes);
                Bitmap bitmap = decodeBitmap(bytes, maxWidth, maxHeight);
                if (drawable != null) {
                    return new ImageLoadResult(bitmap, drawable, false);
                }
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
        if (!AppCache.enabled(preferences)) {
            return null;
        }
        try {
            if (isCachedImageMissing(url)) {
                Bitmap missing = missingImgurBitmap(maxWidth, maxHeight);
                return missing == null ? null : new ImageLoadResult(missing, true);
            }
            File file = imageCacheFile(url);
            if (file.exists() && file.length() > 0) {
                file.setLastModified(System.currentTimeMillis());
                byte[] bytes = readFileBytes(file);
                Drawable drawable = decodeAnimatedDrawableIfPossible(url, bytes);
                Bitmap bitmap = decodeBitmap(bytes, maxWidth, maxHeight);
                if (drawable != null) {
                    return new ImageLoadResult(bitmap, drawable, false);
                }
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

    private Drawable decodeAnimatedDrawableIfPossible(String url, byte[] bytes) {
        if (!isGifUrl(url) || bytes == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return null;
        }
        try {
            Drawable drawable = ImageDecoder.decodeDrawable(ImageDecoder.createSource(ByteBuffer.wrap(bytes)));
            if (drawable instanceof AnimatedImageDrawable) {
                ((AnimatedImageDrawable) drawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
            }
            return drawable;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void startAnimatedDrawable(Drawable drawable) {
        startAnimatedDrawable(drawable, false);
    }

    private void startAnimatedDrawable(Drawable drawable, boolean force) {
        if ((force || autoplayGifs()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable instanceof AnimatedImageDrawable) {
            ((AnimatedImageDrawable) drawable).start();
        }
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
        int size = Math.max(dp(108), Math.min(Math.max(1, Math.min(maxWidth, maxHeight)), dp(MEDIA_GRID_CELL_DP)));
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paint.setColor(unavailableMediaColor());
        canvas.drawRect(0, 0, size, size, paint);
        paint.setStyle(android.graphics.Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(borderColor());
        canvas.drawRect(dp(1), dp(1), size - dp(1), size - dp(1), paint);
        paint.setStyle(android.graphics.Paint.Style.FILL);
        paint.setTextAlign(android.graphics.Paint.Align.CENTER);
        paint.setColor(mutedColor());
        paint.setTextSize(dp(16));
        android.graphics.Paint.FontMetrics metrics = paint.getFontMetrics();
        String[] lines = text("\u753b\u50cf\u3092\u8868\u793a\n\u3067\u304d\u307e\u305b\u3093", "Image\nunavailable").split("\\n", -1);
        float lineHeight = metrics.descent - metrics.ascent;
        float y = size / 2f - lineHeight * (lines.length - 1) / 2f - (metrics.ascent + metrics.descent) / 2f;
        for (String line : lines) {
            canvas.drawText(line, size / 2f, y, paint);
            y += lineHeight;
        }
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
        if (!AppCache.canWrite(this, preferences, bytes == null ? 0 : bytes.length)) {
            return;
        }
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

    private boolean isCachedImageMissing(String url) {
        JSONObject item = imageMeta(url);
        return item != null && item.optBoolean("missing", false);
    }

    private void saveImageMissing(String url) {
        saveImageMeta(url, true, null);
    }

    private Boolean readCachedImageSensitive(String url) {
        JSONObject item = imageMeta(url);
        if (item == null || !item.has("sensitive")) {
            return null;
        }
        return item.optBoolean("sensitive", false);
    }

    private void saveImageSensitive(String url, boolean sensitive) {
        saveImageMeta(url, false, sensitive);
    }

    private JSONObject imageMeta(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            JSONObject root = new JSONObject(preferences.getString(PREF_IMGUR_META, "{}"));
            return root.optJSONObject(url);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void saveImageMeta(String url, boolean missing, Boolean sensitive) {
        if (url == null || url.isEmpty()) {
            return;
        }
        try {
            JSONObject root = new JSONObject(preferences.getString(PREF_IMGUR_META, "{}"));
            JSONObject item = root.optJSONObject(url);
            if (item == null) {
                item = new JSONObject();
            }
            item.put("missing", missing);
            if (sensitive != null) {
                item.put("sensitive", sensitive);
            }
            item.put("savedAt", System.currentTimeMillis());
            root.put(url, item);
            preferences.edit().putString(PREF_IMGUR_META, root.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private File imageCacheDir() {
        return new File(getCacheDir(), "imgur");
    }

    private File imageCacheFile(String url) throws Exception {
        return new File(imageCacheDir(), cacheKey(url) + ".img");
    }

    private String cacheKey(String url) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(url.getBytes(Charset.forName("UTF-8")));
        StringBuilder name = new StringBuilder();
        for (byte value : hash) {
            name.append(String.format(Locale.ROOT, "%02x", value));
        }
        return name.toString();
    }

    private Bitmap blurredBitmap(Bitmap bitmap) {
        int smallWidth = Math.max(1, bitmap.getWidth() / 12);
        int smallHeight = Math.max(1, bitmap.getHeight() / 12);
        Bitmap small = Bitmap.createScaledBitmap(bitmap, smallWidth, smallHeight, true);
        small = boxBlur(small, 1);
        return Bitmap.createScaledBitmap(small, bitmap.getWidth(), bitmap.getHeight(), true);
    }

    private boolean isGraphicViolenceImage(Bitmap bitmap) {
        Interpreter interpreter = graphicViolenceInterpreter();
        if (interpreter == null || bitmap == null) {
            return false;
        }
        try {
            float[][] output = new float[1][3];
            interpreter.run(graphicViolenceInput(bitmap), output);
            int winner = 0;
            for (int i = 1; i < output[0].length; i++) {
                if (output[0][i] > output[0][winner]) {
                    winner = i;
                }
            }
            return winner != 2 && output[0][winner] >= 0.995f;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private Interpreter graphicViolenceInterpreter() {
        if (graphicViolenceInterpreter != null) {
            return graphicViolenceInterpreter;
        }
        if (graphicViolenceModelLoadAttempted) {
            return null;
        }
        graphicViolenceModelLoadAttempted = true;
        try {
            graphicViolenceInterpreter = new Interpreter(loadMappedAsset("graphic_violence.tflite"));
            return graphicViolenceInterpreter;
        } catch (Throwable ignored) {
            graphicViolenceInterpreter = null;
            return null;
        }
    }

    private MappedByteBuffer loadMappedAsset(String name) throws Exception {
        try (AssetFileDescriptor descriptor = getAssets().openFd(name);
             FileInputStream input = new FileInputStream(descriptor.getFileDescriptor());
             FileChannel channel = input.getChannel()) {
            return channel.map(FileChannel.MapMode.READ_ONLY, descriptor.getStartOffset(), descriptor.getDeclaredLength());
        }
    }

    private ByteBuffer graphicViolenceInput(Bitmap bitmap) {
        int cropSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap cropped = Bitmap.createBitmap(bitmap,
                Math.max(0, (bitmap.getWidth() - cropSize) / 2),
                Math.max(0, (bitmap.getHeight() - cropSize) / 2),
                cropSize,
                cropSize);
        Bitmap scaled = Bitmap.createScaledBitmap(cropped, 320, 320, true);
        if (cropped != bitmap) {
            cropped.recycle();
        }
        ByteBuffer data = ByteBuffer.allocateDirect(1 * 320 * 320 * 3 * 4);
        data.order(ByteOrder.LITTLE_ENDIAN);
        int[] pixels = new int[320 * 320];
        scaled.getPixels(pixels, 0, 320, 0, 0, 320, 320);
        for (int color : pixels) {
            data.putFloat(Color.red(color));
            data.putFloat(Color.green(color));
            data.putFloat(Color.blue(color));
        }
        data.rewind();
        scaled.recycle();
        return data;
    }

    private void closeImageClassifiers() {
        if (graphicViolenceInterpreter != null) {
            graphicViolenceInterpreter.close();
            graphicViolenceInterpreter = null;
        }
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

    private static String stripTrailingUrlPunctuation(String url) {
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

    private ImgurLink previewMediaLink(String rawUrl) {
        String direct = directMediaUrl(rawUrl);
        return direct == null ? null : new ImgurLink(rawUrl, direct, isVideoUrl(direct));
    }

    private String directMediaUrl(String rawUrl) {
        try {
            String normalized = normalizeUrl(rawUrl);
            Uri uri = Uri.parse(normalized);
            String path = uri.getPath();
            if (path == null) {
                return null;
            }
            String lower = path.toLowerCase(Locale.ROOT);
            return lower.matches(".+\\.(jpe?g|png|webp|gif|bmp|avif|mp4|webm|mov|m4v)$")
                    ? normalized
                    : null;
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
            text.setSpan(new URLSpan(normalizeUrl(url)) {
                @Override
                public void onClick(View widget) {
                    if (suppressNextLinkClick.remove(widget)) {
                        return;
                    }
                    if (consumePostPopupTap(widget)) {
                        return;
                    }
                    routeLink(getURL(), currentTab());
                }
            }, start, end, flags);
        }
    }

    private boolean isVideoUrl(String url) {
        String lower = mediaPath(url);
        return lower.endsWith(".mp4") || lower.endsWith(".webm") || lower.endsWith(".mov") || lower.endsWith(".m4v");
    }

    private boolean isGifUrl(String url) {
        String lower = mediaPath(url);
        return lower.endsWith(".gif");
    }

    private String mediaPath(String url) {
        try {
            String path = Uri.parse(normalizeUrl(url)).getPath();
            return path == null ? "" : path.toLowerCase(Locale.ROOT);
        } catch (Exception ignored) {
            return url == null ? "" : url.toLowerCase(Locale.ROOT);
        }
    }

    private void installLinkTouchTracking(TextView text) {
        if (Boolean.TRUE.equals(text.getTag(R.id.tag_post_swipe_text))) {
            return;
        }
        text.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                String url = touchedUrl(text, event);
                text.setTag(url == null ? null : new TouchedLink(url, (int) event.getRawX(), (int) event.getRawY()));
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                text.setTag(null);
            }
            return false;
        });
    }

    private String touchedUrl(TextView text, MotionEvent event) {
        CharSequence value = text.getText();
        if (!(value instanceof Spanned)) {
            return null;
        }
        Layout layout = text.getLayout();
        if (layout == null) {
            return null;
        }
        int x = (int) event.getX() - text.getTotalPaddingLeft() + text.getScrollX();
        int y = (int) event.getY() - text.getTotalPaddingTop() + text.getScrollY();
        if (y < 0 || y > layout.getHeight()) {
            return null;
        }
        int line = layout.getLineForVertical(y);
        int offset = layout.getOffsetForHorizontal(line, x);
        Spanned spanned = (Spanned) value;
        URLSpan[] spans = spanned.getSpans(offset, offset, URLSpan.class);
        for (URLSpan span : spans) {
            if (spanned.getSpanStart(span) <= offset && spanned.getSpanEnd(span) > offset) {
                return normalizeUrl(span.getURL());
            }
        }
        return null;
    }

    private boolean showLinkCopyPopupIfAny(TextView anchor) {
        Object tag = anchor.getTag();
        if (!(tag instanceof TouchedLink) || ((TouchedLink) tag).url.isEmpty()) {
            return false;
        }
        suppressNextLinkClick.add(anchor);
        mainHandler.postDelayed(() -> suppressNextLinkClick.remove(anchor), 1400);
        showLinkCopyPopup(anchor, (TouchedLink) tag);
        return true;
    }

    private void showLinkCopyPopup(View anchor, TouchedLink link) {
        showValueCopyPopupAt(link.url, link.rawX, link.rawY + dp(18), true);
    }

    private void showValueCopyPopup(View anchor, String value) {
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        showValueCopyPopupAt(value, location[0] + dp(16), location[1] + anchor.getHeight() + dp(4), false);
    }

    private void showValueCopyPopupAt(String value, int rawX, int rawY, boolean linkMenu) {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setBackground(menuBackground());
        menu.setPadding(dp(4), dp(4), dp(4), dp(4));

        String normalized = normalizeUrl(value);
        boolean threadLink = linkMenu && isThreadUrl(normalized);
        if (threadLink) {
            boolean newTabByDefault = open5chLinksInNewTab();
            int icon = newTabByDefault ? R.drawable.ic_arrow_forward : R.drawable.ic_add;
            TextView open = menuItem(newTabByDefault
                    ? text("\u73fe\u5728\u306e\u30bf\u30d6\u3067\u958b\u304f", "Open in current tab")
                    : text("\u65b0\u3057\u3044\u30bf\u30d6\u3067\u958b\u304f", "Open in new tab"), v -> {
            });
            open.setGravity(Gravity.CENTER_VERTICAL);
            open.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
            open.setCompoundDrawablePadding(dp(6));
            menu.addView(open, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        TextView copy = menuItem(text("\u30b3\u30d4\u30fc", "Copy"), v -> {
        });
        copy.setGravity(Gravity.CENTER_VERTICAL);
        copy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_copy, 0, 0, 0);
        copy.setCompoundDrawablePadding(dp(6));
        menu.addView(copy, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView share = null;
        if (linkMenu) {
            share = menuItem(text("\u5171\u6709", "Share"), v -> {
            });
            share.setGravity(Gravity.CENTER_VERTICAL);
            share.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_share, 0, 0, 0);
            share.setCompoundDrawablePadding(dp(6));
            menu.addView(share, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        PopupWindow popup = new PopupWindow(menu, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        prepareAnimatedPopupDismiss(popup, menu);
        if (threadLink) {
            View open = menu.getChildAt(0);
            open.setOnClickListener(v -> {
                if (open5chLinksInNewTab()) {
                    openLinkInCurrentTab(normalized);
                } else {
                    CuspTab current = currentTab();
                    createTab(normalized, true, tabs.indexOf(current), false, isPrivateTab(current));
                }
                dismissPopupAnimated(popup);
            });
        }
        copy.setOnClickListener(v -> {
            ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (manager != null) {
                manager.setPrimaryClip(ClipData.newPlainText("CuspiDroid link", value));
                Toast.makeText(this, text("\u30ea\u30f3\u30af\u3092\u30b3\u30d4\u30fc", "Link copied."), Toast.LENGTH_SHORT).show();
            }
            dismissPopupAnimated(popup);
        });
        TextView shareButton = share;
        if (shareButton != null) {
            shareButton.setOnClickListener(v -> {
                shareUrl(value);
                dismissPopupAnimated(popup);
            });
        }
        menu.measure(View.MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int x = Math.max(0, Math.min(rawX - dp(18),
                getResources().getDisplayMetrics().widthPixels - menu.getMeasuredWidth()));
        int y = rawY;
        popup.showAtLocation(getWindow().getDecorView(), Gravity.NO_GRAVITY, x, y);
        animatePopupIn(popup, false);
    }

    private void openLinkInCurrentTab(String url) {
        CuspTab tab = currentTab();
        if (tab == null) {
            createTab(url, true);
            return;
        }
        openInCurrentTab(url);
    }

    private void replaceReplySpans(SpannableString text, ThreadPage page) {
        Matcher matcher = REPLY_PATTERN.matcher(text);
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
                    if (suppressNextLinkClick.remove(widget)) {
                        return;
                    }
                    if (consumePostPopupTap(widget)) {
                        return;
                    }
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
        showPostsPopup(anchor, page, targets, jumpEachPost, false);
    }

    private void showPostsPopup(View anchor, ThreadPage page, List<Post> targets, boolean jumpEachPost,
                                boolean placeNearAnchor) {
        if (consumePostPopupTap(anchor)) {
            return;
        }
        postPopupOpening = true;
        int generation = ++postPopupGeneration;
        showPostsPopupNow(anchor, page, targets, jumpEachPost, placeNearAnchor, generation);
    }

    private boolean consumePostPopupTap() {
        return consumePostPopupTap(null);
    }

    private boolean consumePostPopupTap(View source) {
        if (!postPopupOpening && replyPopups.isEmpty()) {
            return false;
        }
        boolean insideReplyPopup = isViewInsideReplyPopup(source);
        if (insideReplyPopup) {
            return false;
        }
        dismissThreadPopups();
        return true;
    }

    private void showPostsPopupNow(View anchor, ThreadPage page, List<Post> targets, boolean jumpEachPost,
                                   boolean placeNearAnchor, int generation) {
        if (!postPopupOpening || generation != postPopupGeneration) {
            return;
        }
        FrameLayout popupRoot = new FrameLayout(this);
        int popupRootGap = jumpEachPost ? 0 : dp(POST_OUTER_GAP_DP);
        int popupFrameInset = jumpEachPost ? dp(POST_OUTER_GAP_DP) : 0;
        int popupCardInset = 0;
        boolean framePopupViewport = jumpEachPost || (!jumpEachPost && targets != null && targets.size() == 1);
        popupRoot.setPadding(popupRootGap, popupRootGap, popupRootGap, popupRootGap);
        popupRoot.setBackgroundColor(Color.TRANSPARENT);
        popupRoot.setFocusable(true);
        popupRoot.setClickable(true);
        if (framePopupViewport) {
            popupRoot.setForeground(roundedDrawable(Color.TRANSPARENT, TEAL, dp(12), dp(2)));
        }

        ScrollView popupScroll = new ScrollView(this);
        popupScroll.setVerticalScrollBarEnabled(false);
        popupScroll.setScrollbarFadingEnabled(true);
        if (jumpEachPost) {
            popupScroll.setPadding(popupFrameInset, popupFrameInset, popupFrameInset, popupFrameInset);
            popupScroll.setBackground(roundedFill(menuColor(), dp(12)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupScroll.setClipToOutline(true);
            }
        }
        LinearLayout popupPosts = new LinearLayout(this);
        popupPosts.setOrientation(LinearLayout.VERTICAL);
        popupPosts.setPadding(0, 0, 0, 0);
        popupScroll.addView(popupPosts, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        FrameLayout.LayoutParams scrollParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (jumpEachPost) {
            scrollParams.setMargins(0, 0, 0, 0);
        }
        popupRoot.addView(popupScroll, scrollParams);

        int[] anchorLocation = new int[2];
        anchor.getLocationOnScreen(anchorLocation);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        View sourcePostCard = findSourcePostCard(anchor);
        int[] sourceLocation = new int[2];
        if (sourcePostCard != null) {
            sourcePostCard.getLocationOnScreen(sourceLocation);
        }
        int edgeGap = dp(POST_OUTER_GAP_DP);
        int postCardWidth = sourcePostCard != null && sourcePostCard.getWidth() > edgeGap * 2
                ? sourcePostCard.getWidth()
                : Math.min(screenWidth - edgeGap * 2, dp(420));
        int width = postCardWidth + popupFrameInset * 2 + popupRootGap * 2 + popupCardInset * 2;
        int x = sourcePostCard != null
                ? sourceLocation[0] - popupFrameInset - popupRootGap - popupCardInset
                : Math.max(edgeGap, Math.min(anchorLocation[0] - edgeGap, screenWidth - width - edgeGap));
        x = Math.max(0, Math.min(x, screenWidth - width));
        int popupOverlap = jumpEachPost ? dp(36) : dp(12);
        int minPopupY = jumpEachPost ? 0 : dp(8);
        int availableAbove = Math.max(dp(140), anchorLocation[1] + popupOverlap - minPopupY);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int popupHeightLimit = Math.max(dp(40), screenHeight / 2);
        int fullHeightFromTop = Math.max(dp(40), Math.min(screenHeight - minPopupY, popupHeightLimit));
        int measuredContentWidth = jumpEachPost
                ? postCardWidth
                : postCardWidth + popupCardInset * 2;
        boolean incremental = shouldRenderPopupIncrementally(targets);
        int initialCount = incremental
                ? Math.min(POPUP_INITIAL_RENDER_COUNT, targets.size())
                : targets.size();
        boolean showPostFrame = !framePopupViewport && !jumpEachPost;
        addPopupPosts(popupPosts, page, targets, jumpEachPost, showPostFrame, 0, initialCount);
        popupPosts.measure(
                View.MeasureSpec.makeMeasureSpec(Math.max(dp(120), measuredContentWidth), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int desiredHeight = popupPosts.getMeasuredHeight()
                + (jumpEachPost ? popupFrameInset * 2 : popupRootGap * 2);
        boolean hasDeferredPopupPosts = incremental && initialCount < targets.size();
        int maxHeight = hasDeferredPopupPosts
                ? fullHeightFromTop
                : Math.min(fullHeightFromTop, Math.max(availableAbove, desiredHeight));
        if (hasDeferredPopupPosts) {
            desiredHeight = Math.max(desiredHeight, maxHeight);
        }
        boolean popupScrollable = hasDeferredPopupPosts || desiredHeight > maxHeight;
        int popupHeight = Math.max(dp(40), Math.min(desiredHeight, maxHeight));
        popupScroll.setVerticalScrollBarEnabled(popupScrollable);
        popupScroll.setOverScrollMode(popupScrollable ? View.OVER_SCROLL_IF_CONTENT_SCROLLS : View.OVER_SCROLL_NEVER);
        popupScroll.setOnTouchListener((v, event) -> !popupScrollable
                && event.getActionMasked() == MotionEvent.ACTION_MOVE);
        int preferredY = placeNearAnchor
                ? anchorLocation[1] - popupHeight
                : anchorLocation[1] - popupHeight + popupOverlap;
        int y = preferredY < minPopupY ? minPopupY : preferredY;
        PopupWindow popup = new PopupWindow(popupRoot, width, popupHeight, false);
        popup.setOutsideTouchable(false);
        popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        popup.setOnDismissListener(() -> replyPopups.remove(popup));
        replyPopups.add(popup);
        popup.showAtLocation(contentFrame, Gravity.NO_GRAVITY, x, y);
        animatePopupIn(popup, true);
        if (initialCount < targets.size()) {
            mainHandler.postDelayed(() -> appendPopupPostsChunk(popup, popupPosts, page, targets,
                    jumpEachPost, showPostFrame, initialCount, generation), 16);
        }
        mainHandler.postDelayed(() -> {
            if (generation == postPopupGeneration) {
                postPopupOpening = false;
            }
        }, 300);
    }

    private boolean shouldRenderPopupIncrementally(List<Post> targets) {
        if (targets == null || targets.size() <= POPUP_INITIAL_RENDER_COUNT) {
            return false;
        }
        if (targets.size() > 3) {
            return true;
        }
        for (Post post : targets) {
            if (isHeavyTextPost(post)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHeavyTextPost(Post post) {
        if (post == null || post.body == null) {
            return false;
        }
        if (Boolean.TRUE.equals(post.cachedLikelyAa) || post.aaMode || maybeHeavyAaBody(post.body)) {
            return true;
        }
        return post.body.length() > 1200 || bodyLineCount(post) > 24;
    }

    private void addPopupPosts(LinearLayout popupPosts, ThreadPage page, List<Post> targets,
                               boolean jumpEachPost, boolean showPostFrame, int start, int end) {
        int safeStart = Math.max(0, start);
        int safeEnd = Math.min(targets == null ? 0 : targets.size(), end);
        for (int i = safeStart; i < safeEnd; i++) {
            Post post = targets.get(i);
            popupPosts.addView(popupPostCard(page, post, showPostFrame),
                    popupPostParams(jumpEachPost, i == targets.size() - 1));
        }
    }

    private void appendPopupPostsChunk(PopupWindow popup, LinearLayout popupPosts, ThreadPage page,
                                       List<Post> targets, boolean jumpEachPost, boolean showPostFrame,
                                       int start, int generation) {
        if (popup == null || !popup.isShowing() || popupPosts == null
                || targets == null || generation != postPopupGeneration) {
            return;
        }
        int end = Math.min(targets.size(), start + POPUP_RENDER_CHUNK_SIZE);
        addPopupPosts(popupPosts, page, targets, jumpEachPost, showPostFrame, start, end);
        if (end < targets.size()) {
            mainHandler.postDelayed(() -> appendPopupPostsChunk(popup, popupPosts, page, targets,
                    jumpEachPost, showPostFrame, end, generation), 16);
        }
    }

    private View popupPostCard(ThreadPage page, Post post, boolean showFrame) {
        CuspTab tab = currentTab();
        if (tab != null) {
            post.aaMode = aaModeForPost(page, post);
        }
        FrameLayout shell = new FrameLayout(this);
        shell.setClipChildren(false);
        shell.setClipToPadding(false);
        shell.setBackgroundColor(Color.TRANSPARENT);
        int cardInset = 0;
        View swipeBackground = new View(this);
        swipeBackground.setBackground(roundedFill(menuColor(), dp(12)));
        FrameLayout.LayoutParams swipeBackgroundParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        swipeBackgroundParams.setMargins(cardInset, cardInset, cardInset, cardInset);
        shell.addView(swipeBackground, swipeBackgroundParams);
        ImageView readAction = swipeActionIcon(R.drawable.ic_check, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        ImageView replyAction = swipeActionIcon(R.drawable.ic_reply, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        shell.addView(readAction);
        shell.addView(replyAction);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setTag(R.id.tag_post_card, true);
        card.setPadding(dp(10), dp(8), dp(10), dp(10));
        Drawable background = postBackground(tab != null && post.number > tab.readPostNumber, isMyPost(page, post));
        card.setBackground(showFrame ? framedPostBackground(background) : background);
        card.setOnLongClickListener(v -> {
            if (isPostSwipeBlocked(post)) {
                return true;
            }
            showPostActionMenu(card, tab, post);
            return true;
        });
        if (tab != null) {
            attachPostSwipe(card, card, readAction, replyAction, tab, post);
        }

        LinearLayout metaRow = new LinearLayout(this);
        metaRow.setOrientation(LinearLayout.HORIZONTAL);
        metaRow.setGravity(Gravity.CENTER_VERTICAL);
        View meta = postMetaText(post, page, () -> {
            if (!isPostSwipeBlocked(post)) {
                showPostActionMenu(card, tab, post);
            }
        });
        metaRow.addView(meta, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        ImageButton postJump = iconButton(R.drawable.ic_arrow_forward, "Jump to >>" + post.number, null);
        postJump.setColorFilter(TEAL);
        postJump.setBackgroundColor(Color.TRANSPARENT);
        postJump.setPadding(dp(8), dp(8), dp(8), dp(8));
        postJump.setOnClickListener(v -> {
            dismissThreadPopups();
            jumpToPost(post.number);
        });
        metaRow.addView(postJump, new LinearLayout.LayoutParams(dp(34), dp(34)));
        card.addView(metaRow);

        View body = tab == null ? postContent(post.body, page, null, () -> showPostActionMenu(card, null, post))
                : postBodyView(card, page, tab, post);
        body.setPadding(0, dp(4), 0, 0);
        card.addView(body);
        if (tab != null) {
            attachPostSwipeDeep(metaRow, card, readAction, replyAction, tab, post);
            attachPostSwipeDeep(body, card, readAction, replyAction, tab, post);
        }
        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(cardInset, cardInset, cardInset, cardInset);
        shell.addView(card, cardParams);
        return shell;
    }

    private View findSourcePostCard(View anchor) {
        View current = anchor;
        while (current != null) {
            if (Boolean.TRUE.equals(current.getTag(R.id.tag_post_card))) {
                return current;
            }
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }
        return null;
    }

    private LinearLayout.LayoutParams popupPostParams(boolean compact, boolean last) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, last ? 0 : dp(POST_OUTER_GAP_DP));
        return params;
    }

    private void dismissThreadPopups() {
        postPopupOpening = false;
        postPopupGeneration++;
        dismissThreadPopupsOnly();
    }

    private void dismissThreadPopupsOnly() {
        List<PopupWindow> popups = new ArrayList<>(replyPopups);
        replyPopups.clear();
        for (PopupWindow popup : popups) {
            dismissPopupAnimated(popup);
        }
    }

    private void dismissTopReplyPopup() {
        if (replyPopups.isEmpty()) {
            return;
        }
        PopupWindow popup = replyPopups.get(replyPopups.size() - 1);
        replyPopups.remove(popup);
        dismissPopupAnimated(popup);
    }

    private void dismissTopAnimatedPopup() {
        if (animatedPopups.isEmpty()) {
            return;
        }
        PopupWindow popup = animatedPopups.get(animatedPopups.size() - 1);
        dismissPopupAnimated(popup);
    }

    private void dismissPopupAnimated(PopupWindow popup) {
        if (popup == null || !popup.isShowing()) {
            return;
        }
        View content = popup.getContentView();
        if (content == null) {
            popup.dismiss();
            return;
        }
        content.animate().cancel();
        content.setPivotX(content.getWidth() / 2f);
        content.setPivotY(content.getHeight());
        content.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .alpha(0f)
                .setDuration(120)
                .withEndAction(popup::dismiss)
                .start();
    }

    private void prepareAnimatedPopupDismiss(PopupWindow popup, View content) {
        if (popup == null || content == null) {
            return;
        }
        animatedPopups.remove(popup);
        animatedPopups.add(popup);
        popup.setOnDismissListener(() -> animatedPopups.remove(popup));
        popup.setTouchInterceptor((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                dismissPopupAnimated(popup);
                return true;
            }
            return false;
        });
        content.setFocusableInTouchMode(true);
        content.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                dismissPopupAnimated(popup);
                return true;
            }
            return false;
        });
    }

    private boolean isTouchInsideAnimatedPopup(MotionEvent event) {
        for (int i = animatedPopups.size() - 1; i >= 0; i--) {
            PopupWindow popup = animatedPopups.get(i);
            if (popup == null || !popup.isShowing()) {
                animatedPopups.remove(i);
                continue;
            }
            View content = popup.getContentView();
            if (content == null) {
                continue;
            }
            int[] location = new int[2];
            content.getLocationOnScreen(location);
            float x = event.getRawX();
            float y = event.getRawY();
            if (x >= location[0] && x <= location[0] + content.getWidth()
                    && y >= location[1] && y <= location[1] + content.getHeight()) {
                return true;
            }
        }
        return false;
    }

    private void animatePopupIn(PopupWindow popup, boolean pivotBottom) {
        if (popup == null || !popup.isShowing()) {
            return;
        }
        View content = popup.getContentView();
        if (content == null) {
            return;
        }
        content.animate().cancel();
        content.setAlpha(0f);
        content.setScaleX(0.94f);
        content.setScaleY(0.94f);
        content.post(() -> {
            content.setPivotX(content.getWidth() / 2f);
            content.setPivotY(pivotBottom ? content.getHeight() : 0f);
            content.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(120)
                    .start();
        });
    }

    private boolean isTouchInsideTopReplyPopup(MotionEvent event) {
        if (replyPopups.isEmpty()) {
            return false;
        }
        PopupWindow popup = replyPopups.get(replyPopups.size() - 1);
        return isTouchInsidePopup(event, popup);
    }

    private boolean isTouchInsidePopup(MotionEvent event, PopupWindow popup) {
        if (event == null || popup == null || !popup.isShowing()) {
            return false;
        }
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        View content = popup.getContentView();
        if (content == null) {
            return false;
        }
        int[] location = new int[2];
        content.getLocationOnScreen(location);
        Rect bounds = new Rect(location[0], location[1],
                location[0] + content.getWidth(), location[1] + content.getHeight());
        return bounds.contains(x, y);
    }

    private boolean isViewInsideReplyPopup(View view) {
        if (view == null) {
            return false;
        }
        for (PopupWindow popup : replyPopups) {
            View content = popup.getContentView();
            if (content != null && popup.isShowing() && isDescendantOf(view, content)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDescendantOf(View child, View ancestor) {
        View current = child;
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }
        return false;
    }

    private boolean isTouchInsideView(MotionEvent event, View view) {
        if (event == null || view == null || view.getVisibility() != View.VISIBLE) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        Rect bounds = new Rect(
                location[0],
                location[1],
                location[0] + view.getWidth(),
                location[1] + view.getHeight());
        return bounds.contains((int) event.getRawX(), (int) event.getRawY());
    }

    private boolean trackGestureEvent(MotionEvent event) {
        if (!gesturesEnabled() || shouldIgnoreGestureEvent(event)) {
            resetGestureTracking();
            return false;
        }
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            gestureTracking = true;
            gestureMoved = false;
            gestureIntercepting = false;
            gestureSequence.setLength(0);
            gestureDownX = gestureLastX = event.getRawX();
            gestureDownY = gestureLastY = event.getRawY();
            hideGestureOverlay();
            return false;
        }
        if (!gestureTracking) {
            return gestureIntercepting;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            addGestureMove(event.getRawX(), event.getRawY());
            if (gestureMoved) {
                gestureIntercepting = true;
            }
            String matched = matchedGestureAction(gestureSequence.toString());
            if (matched != null) {
                showGestureOverlay(gestureActionLabel(matched));
            } else if (gestureMoved) {
                showGestureOverlay(gestureArrows(gestureSequence.toString()));
            }
            return gestureIntercepting;
        }
        if (action == MotionEvent.ACTION_UP) {
            boolean consumed = gestureIntercepting;
            String matched = matchedGestureAction(gestureSequence.toString());
            hideGestureOverlay();
            resetGestureTracking();
            if (matched != null) {
                performGestureAction(matched);
            }
            return consumed;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            boolean consumed = gestureIntercepting;
            hideGestureOverlay();
            resetGestureTracking();
            return consumed;
        }
        return gestureIntercepting;
    }

    private boolean shouldIgnoreGestureEvent(MotionEvent event) {
        if (event == null || event.getPointerCount() > 1 || imageOverlay != null || !animatedPopups.isEmpty()
                || !replyPopups.isEmpty()) {
            return true;
        }
        return isTouchInsideView(event, addressBar)
                || isTouchInsideView(event, threadSearchInput)
                || isTouchInsideView(event, suggestionsPanel)
                || isTouchInsideView(event, bottomToolbar)
                || isTouchInsideView(event, bottomThreadBar)
                || isTouchInsideView(event, threadSearchBar);
    }

    private void addGestureMove(float x, float y) {
        float dx = x - gestureLastX;
        float dy = y - gestureLastY;
        int threshold = gestureThresholdPx();
        if (Math.abs(dx) < threshold && Math.abs(dy) < threshold) {
            return;
        }
        char direction = Math.abs(dx) >= Math.abs(dy)
                ? (dx > 0 ? 'R' : 'L')
                : (dy > 0 ? 'D' : 'U');
        if (gestureSequence.length() == 0 && (direction == 'U' || direction == 'D')) {
            resetGestureTracking();
            hideGestureOverlay();
            return;
        }
        int length = gestureSequence.length();
        if (length == 0 || gestureSequence.charAt(length - 1) != direction) {
            gestureSequence.append(direction);
            gestureMoved = true;
        }
        gestureLastX = x;
        gestureLastY = y;
    }

    private void resetGestureTracking() {
        gestureTracking = false;
        gestureMoved = false;
        gestureIntercepting = false;
        gestureSequence.setLength(0);
    }

    private boolean gesturesEnabled() {
        return preferences != null && preferences.getBoolean(PREF_GESTURES_ENABLED, false);
    }

    private int gestureThresholdPx() {
        int level = preferences == null ? 2 : preferences.getInt(PREF_GESTURE_SENSITIVITY, 2);
        int[] dpValues = {96, 72, 56, 42, 30};
        int index = Math.max(0, Math.min(level, dpValues.length - 1));
        return dp(dpValues[index]);
    }

    private String matchedGestureAction(String sequence) {
        if (sequence == null || sequence.isEmpty()) {
            return null;
        }
        for (String action : GESTURE_ACTIONS) {
            String gesture = gestureForAction(preferences, action);
            if (validGesture(gesture) && sequence.equals(gesture)) {
                return action;
            }
        }
        return null;
    }

    private void showGestureOverlay(String value) {
        if (value == null || value.isEmpty()) {
            hideGestureOverlay();
            return;
        }
        if (gestureOverlay == null) {
            gestureOverlay = new TextView(this);
            gestureOverlay.setTextColor(Color.WHITE);
            gestureOverlay.setTextSize(22);
            gestureOverlay.setGravity(Gravity.CENTER);
            gestureOverlay.setTypeface(Typeface.DEFAULT_BOLD);
            gestureOverlay.setPadding(dp(22), dp(14), dp(22), dp(14));
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.argb(205, 31, 41, 55));
            background.setCornerRadius(dp(18));
            gestureOverlay.setBackground(background);
        }
        gestureOverlay.setText(value);
        if (gestureOverlay.getParent() == null) {
            ViewGroup content = findViewById(android.R.id.content);
            content.addView(gestureOverlay, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER));
        }
        gestureOverlay.bringToFront();
        gestureOverlay.setVisibility(View.VISIBLE);
    }

    private void hideGestureOverlay() {
        if (gestureOverlay != null) {
            gestureOverlay.setVisibility(View.GONE);
        }
    }

    private void performGestureAction(String action) {
        if (GESTURE_TAB_OVERVIEW.equals(action)) {
            showTabOverview();
        } else if (GESTURE_BACK.equals(action)) {
            onBackPressed();
        } else if (GESTURE_FORWARD.equals(action)) {
            goForward();
        } else if (GESTURE_TOP.equals(action)) {
            scrollCurrentPageToTop();
        } else if (GESTURE_BOTTOM.equals(action)) {
            scrollCurrentPageToBottom();
        } else if (GESTURE_RELOAD.equals(action)) {
            reloadFromMenu();
        } else if (GESTURE_CLOSE_TAB.equals(action)) {
            closeCurrentTab();
        } else if (GESTURE_NEW_TAB.equals(action)) {
            createBlankTab();
        } else if (GESTURE_RIGHT_TAB.equals(action)) {
            switchRelativeTab(1);
        } else if (GESTURE_LEFT_TAB.equals(action)) {
            switchRelativeTab(-1);
        } else if (GESTURE_SETTINGS.equals(action)) {
            openSettings();
        } else if (GESTURE_NEXT_THREAD.equals(action)) {
            searchNextThread();
        } else if (GESTURE_FIND.equals(action)) {
            showThreadSearchDialog();
        } else if (GESTURE_BOARD.equals(action)) {
            openCurrentThreadBoard();
        }
    }

    private void jumpToPost(int number) {
        View target = visiblePostViews.get(number);
        if (target == null) {
            target = renderPostForJump(number);
        }
        if (target == null || visibleThreadScroll == null || visibleThreadScroll.getChildCount() == 0) {
            Toast.makeText(this, text("\u53c2\u7167\u5148\u304c\u8868\u793a\u3055\u308c\u3066\u3044\u307e\u305b\u3093", "Referenced post is not visible."), Toast.LENGTH_SHORT).show();
            return;
        }
        final View jumpTarget = target;
        visibleThreadScroll.post(() -> {
            int top = descendantTopWithin(jumpTarget, visibleThreadScroll.getChildAt(0));
            visibleThreadScroll.smoothScrollTo(0, Math.max(0, top - dp(8)));
            ensurePostShellVisible(jumpTarget);
            highlightPost(jumpTarget);
            scheduleThreadPostVisibilityRefresh(currentTab());
        });
    }

    private View renderPostForJump(int number) {
        return renderPostForJump(currentTab(), number);
    }

    private View renderPostForJump(CuspTab tab, int number) {
        if (tab == null) {
            return null;
        }
        if (tab.postSlots == null) {
            return tab.postViews == null ? null : tab.postViews.get(number);
        }
        FrameLayout holder = tab.postSlots.get(number);
        if (holder == null || !(holder.getTag() instanceof VirtualPostSlot)) {
            return tab.postViews == null ? null : tab.postViews.get(number);
        }
        VirtualPostSlot slot = (VirtualPostSlot) holder.getTag();
        renderVirtualPostSlot(holder, slot);
        return slot.card;
    }

    private int descendantTopWithin(View target, View ancestor) {
        int top = 0;
        View current = target;
        while (current != null && current != ancestor) {
            top += current.getTop();
            if (!(current.getParent() instanceof View)) {
                break;
            }
            current = (View) current.getParent();
        }
        return top;
    }

    private void ensurePostShellVisible(View postCard) {
        if (postCard == null || !(postCard.getParent() instanceof View)) {
            return;
        }
        ((View) postCard.getParent()).setVisibility(View.VISIBLE);
    }

    private void highlightPost(View target) {
        clearJumpHighlight();
        highlightedPostView = target;
        CuspTab tab = currentTab();
        boolean unread = false;
        if (tab != null && tab.threadPage != null) {
            for (Post post : tab.threadPage.posts) {
                if (tab.postViews != null && tab.postViews.get(post.number) == target) {
                    unread = post.number > tab.readPostNumber;
                    break;
                }
            }
        }
        int fill = unread ? Theme.unread(this) : postColor();
        target.setBackground(roundedDrawable(fill, TEAL, dp(8)));
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
        if (tab == null) {
            return;
        }
        pendingScrollToBottomTab = tab;
        tab.fastRenderToBottom = true;
        tab.bottomScrollLockUntil = android.os.SystemClock.uptimeMillis() + 1800;
        scrollThreadToBottomWhenReady(tab, 0);
    }

    private void scrollCurrentPageToTop() {
        ScrollView scroll = visibleThreadScroll;
        CuspTab tab = currentTab();
        if (scroll == null && tab != null) {
            scroll = tab.threadScroll == null ? findScrollView(tab.readerView) : tab.threadScroll;
        }
        if (scroll == null) {
            scroll = findScrollView(contentFrame);
        }
        if (scroll != null) {
            scroll.smoothScrollTo(0, 0);
        }
    }

    private void scrollCurrentPageToBottom() {
        CuspTab tab = currentTab();
        if (tab != null && NATIVE_THREAD.equals(tab.nativeKind)) {
            scrollCurrentThreadToBottom();
            return;
        }
        ScrollView scroll = findScrollView(contentFrame);
        if (scroll == null || scroll.getChildCount() == 0) {
            return;
        }
        int range = Math.max(0, scroll.getChildAt(0).getHeight() - scroll.getHeight());
        scroll.smoothScrollTo(0, range);
    }

    private void scrollToLastRenderedPost(CuspTab tab) {
        ScrollView scroll = tab.threadScroll == null ? findScrollView(tab.readerView) : tab.threadScroll;
        if (scroll == null) {
            scroll = visibleThreadScroll == null ? findScrollView(contentFrame) : visibleThreadScroll;
        }
        View target = lastRenderedPostView(tab);
        if (scroll == null || scroll.getChildCount() == 0 || target == null) {
            return;
        }
        clearAddressFocus();
        final ScrollView targetScroll = scroll;
        targetScroll.fling(0);
        targetScroll.clearAnimation();
        targetScroll.post(() -> {
            if (targetScroll.getChildCount() == 0) {
                return;
            }
            targetScroll.fling(0);
            targetScroll.clearAnimation();
            int top = descendantTopWithin(target, targetScroll.getChildAt(0));
            int maxY = Math.max(0, targetScroll.getChildAt(0).getHeight() - targetScroll.getHeight());
            targetScroll.scrollTo(0, Math.max(0, Math.min(top - dp(8), maxY)));
            ensurePostShellVisible(target);
            scheduleThreadPostVisibilityRefresh(tab);
        });
    }

    private View lastRenderedPostView(CuspTab tab) {
        if (tab == null || tab.threadPage == null) {
            return null;
        }
        for (int i = tab.threadPage.posts.size() - 1; i >= 0; i--) {
            int number = tab.threadPage.posts.get(i).number;
            View view = tab.postViews == null ? null : tab.postViews.get(number);
            if (view != null && view.getParent() != null) {
                return view;
            }
            view = tab.postSlots == null ? null : tab.postSlots.get(number);
            if (view != null && view.getParent() != null) {
                return view;
            }
        }
        return null;
    }

    private void runPendingScrollToBottom(CuspTab tab) {
        if (pendingScrollToBottomTab == null || pendingScrollToBottomTab != tab || tab != currentTab()) {
            return;
        }
        scrollThreadToBottomWhenReady(tab, 0);
    }

    private void scrollThreadToRenderedBottom(CuspTab tab) {
        scrollThreadToRenderedBottom(tab, 0);
    }

    private void scrollThreadToRenderedBottom(CuspTab tab, int attempt) {
        ScrollView scroll = tab == null ? visibleThreadScroll : tab.threadScroll;
        if (scroll == null && tab != null) {
            scroll = findScrollView(tab.readerView);
        }
        if (scroll == null || scroll.getChildCount() == 0) {
            return;
        }
        clearAddressFocus();
        final ScrollView targetScroll = scroll;
        targetScroll.fling(0);
        targetScroll.clearAnimation();
        targetScroll.post(() -> {
            if (targetScroll.getChildCount() == 0) {
                return;
            }
            targetScroll.fling(0);
            targetScroll.clearAnimation();
            int range = Math.max(0, targetScroll.getChildAt(0).getHeight() - targetScroll.getHeight());
            targetScroll.scrollTo(0, range);
            scheduleThreadPostVisibilityRefresh(tab);
            if (attempt < 12) {
                targetScroll.postDelayed(() -> scrollThreadToRenderedBottom(tab, attempt + 1), 40);
            }
        });
    }

    private void scrollThreadToBottomWhenReady(CuspTab tab, int attempt) {
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
        targetScroll.fling(0);
        targetScroll.clearAnimation();
        if (!lastPostViewReady(tab) && attempt < 160) {
            targetScroll.postDelayed(() -> scrollThreadToBottomWhenReady(tab, attempt + 1), 25);
            return;
        }
        targetScroll.post(() -> {
            targetScroll.fling(0);
            targetScroll.clearAnimation();
            int range = Math.max(0, targetScroll.getChildAt(0).getHeight() - targetScroll.getHeight());
            targetScroll.scrollTo(0, range);
            rememberThreadBottom(tab, targetScroll);
            scheduleThreadPostVisibilityRefresh(tab);
            boolean keepLocked = shouldKeepBottomLocked(tab);
            if (attempt < 160 && (!isThreadAtBottom(targetScroll) || keepLocked)) {
                targetScroll.postDelayed(() -> scrollThreadToBottomWhenReady(tab, attempt + 1), 25);
            } else if (pendingScrollToBottomTab == tab) {
                pendingScrollToBottomTab = null;
                if (tab != null) {
                    tab.fastRenderToBottom = false;
                    tab.bottomScrollLockUntil = 0;
                }
            }
        });
    }

    private void pinThreadScrollToBottom(CuspTab tab) {
        if (tab == null || tab.threadScroll == null || tab.threadScroll.getChildCount() == 0) {
            return;
        }
        tab.threadScroll.post(() -> {
            if (tab.threadScroll == null || tab.threadScroll.getChildCount() == 0 || !isBottomJumpActive(tab)) {
                return;
            }
            int range = Math.max(0, tab.threadScroll.getChildAt(0).getHeight() - tab.threadScroll.getHeight());
            tab.threadScroll.scrollTo(0, range);
            rememberThreadBottom(tab, tab.threadScroll);
        });
    }

    private void cancelBottomJump(CuspTab tab) {
        if (tab == null) {
            return;
        }
        if (pendingScrollToBottomTab == tab) {
            pendingScrollToBottomTab = null;
        }
        tab.fastRenderToBottom = false;
        tab.bottomScrollLockUntil = 0;
    }

    private void rememberThreadBottom(CuspTab tab, ScrollView scroll) {
        if (tab == null || scroll == null || scroll.getChildCount() == 0) {
            return;
        }
        int range = Math.max(0, scroll.getChildAt(0).getHeight() - scroll.getHeight());
        tab.threadScrollRatio = range <= 0 ? 0f : 1f;
        tab.threadBottomOffset = 0;
        tab.threadScrollUrl = threadUrl(tab);
        tab.hasSavedThreadScroll = true;
    }

    private boolean shouldKeepBottomLocked(CuspTab tab) {
        return isBottomJumpActive(tab)
                && (tab.threadRendering || android.os.SystemClock.uptimeMillis() < tab.bottomScrollLockUntil);
    }

    private boolean isBottomJumpActive(CuspTab tab) {
        return tab != null && (tab.fastRenderToBottom || android.os.SystemClock.uptimeMillis() < tab.bottomScrollLockUntil);
    }

    private boolean lastPostViewReady(CuspTab tab) {
        if (tab == null || tab.threadPage == null || tab.threadPage.posts.isEmpty()) {
            return true;
        }
        Post last = tab.threadPage.posts.get(tab.threadPage.posts.size() - 1);
        View view = tab.postViews == null ? null : tab.postViews.get(last.number);
        if (view == null && tab.postSlots != null) {
            view = tab.postSlots.get(last.number);
        }
        return view != null && view.getHeight() > 0 && view.isShown();
    }

    private boolean isThreadAtBottom(ScrollView scroll) {
        if (scroll == null || scroll.getChildCount() == 0) {
            return true;
        }
        int range = Math.max(0, scroll.getChildAt(0).getHeight() - scroll.getHeight());
        return scroll.getScrollY() >= range - dp(2);
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
            String raw = stripTrailingUrlPunctuation(matcher.group());
            int start = matcher.start();
            int end = matcher.start() + raw.length();
            URLSpan[] overlapping = text.getSpans(start, end, URLSpan.class);
            for (URLSpan span : overlapping) {
                int spanStart = text.getSpanStart(span);
                int spanEnd = text.getSpanEnd(span);
                if (spanStart < end && spanEnd > start) {
                    text.removeSpan(span);
                }
            }
            text.setSpan(new URLSpan(normalizeUrl(raw)) {
                @Override
                public void onClick(View widget) {
                    if (suppressNextLinkClick.remove(widget)) {
                        return;
                    }
                    routeLink(getURL(), currentTab());
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void applyWriteMessageAaMode(EditText message, boolean aaMode) {
        if (aaMode) {
            message.setTextSize(POST_TEXT_SIZE_SP);
            applyAaTypeface(message);
            message.setLineSpacing(0, AA_LINE_SPACING_MULTIPLIER);
            message.setSingleLine(false);
            message.setHorizontallyScrolling(true);
            message.post(() -> fitAaTextSize(message));
        } else {
            message.setTextSize(POST_TEXT_SIZE_SP);
            message.setTypeface(Typeface.DEFAULT);
            message.setIncludeFontPadding(true);
            message.setLineSpacing(0, 1.15f);
            message.setSingleLine(false);
            message.setHorizontallyScrolling(false);
            message.setTextScaleX(1f);
        }
    }

    private void updateWriteAaToggleLabel(TextView label, boolean aaMode) {
        if (label != null) {
            label.setText(aaMode ? text("\u901a\u5e38\u8868\u793a", "Normal view") : text("AA\u8868\u793a", "AA view"));
        }
    }

    private void showWriteActionMenu(View anchor, CuspTab tab, EditText message,
                                     boolean[] writeAaMode, TextView[] aaToggleLabel) {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setBackground(menuBackground());
        menu.setPadding(dp(4), dp(4), dp(4), dp(4));
        PopupWindow popup = new PopupWindow(menu, dp(220), ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        prepareAnimatedPopupDismiss(popup, menu);

        menu.addView(menuIconItem(R.drawable.ic_text_fields,
                writeAaMode[0] ? text("\u901a\u5e38\u8868\u793a", "Normal view") : text("AA\u8868\u793a", "AA view"), v -> {
                    dismissPopupAnimated(popup);
                    writeAaMode[0] = !writeAaMode[0];
                    applyWriteMessageAaMode(message, writeAaMode[0]);
                    updateWriteAaToggleLabel(aaToggleLabel[0], writeAaMode[0]);
                    message.requestFocus();
                    message.setSelection(message.getText().length());
                }), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        menu.addView(horizontalDivider());
        menu.addView(menuIconItem(R.drawable.ic_add, text("ImgBB\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9", "Upload to ImgBB"), v -> {
            dismissPopupAnimated(popup);
            chooseImgbbUploadImage(message);
        }), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        menu.addView(horizontalDivider());
        menu.addView(menuIconItem(R.drawable.ic_copy, text("\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u5c65\u6b74", "Upload history"), v -> {
            dismissPopupAnimated(popup);
            startActivity(new Intent(this, UploadHistoryActivity.class));
        }), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        menu.addView(horizontalDivider());
        menu.addView(menuIconItem(R.drawable.ic_delete, text("\u30af\u30c3\u30ad\u30fc\u3092\u524a\u9664", "Delete cookies"), v -> {
            dismissPopupAnimated(popup);
            confirmDeleteWriteSiteCookies(tab);
        }), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        showPopupAttachedToAnchor(popup, menu, anchor);
    }

    private LinearLayout writeDialogTitleRow(String title, ImageButton menuButton) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(20), dp(14), dp(12), dp(4));
        row.setBackgroundColor(surfaceColor());

        TextView titleView = new TextView(this);
        titleView.setText(title == null || title.trim().isEmpty()
                ? text("\u66f8\u304d\u8fbc\u307f", "Write")
                : title.trim());
        titleView.setTextColor(textColor());
        titleView.setTextSize(18);
        titleView.setMaxLines(2);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        row.addView(titleView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(dp(36), dp(36));
        menuParams.setMargins(dp(8), 0, 0, 0);
        row.addView(menuButton, menuParams);
        return row;
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
        form.setPadding(dp(18), dp(10), dp(18), dp(6));
        form.setBackgroundColor(surfaceColor());

        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setHint(text("\u540d\u524d", "Name"));
        name.setTextColor(textColor());
        name.setHintTextColor(mutedColor());
        name.setBackground(addressBarBackground());
        name.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        nameParams.setMargins(0, 0, 0, dp(8));
        form.addView(name, nameParams);

        EditText mail = new EditText(this);
        mail.setSingleLine(true);
        mail.setHint(text("\u30e1\u30fc\u30eb", "Mail"));
        mail.setTextColor(textColor());
        mail.setHintTextColor(mutedColor());
        mail.setBackground(addressBarBackground());
        mail.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams mailParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        mailParams.setMargins(0, 0, 0, dp(10));
        form.addView(mail, mailParams);

        EditText message = new EditText(this);
        message.setMinLines(9);
        message.setGravity(Gravity.TOP | Gravity.START);
        message.setHint(text("\u672c\u6587", "Message"));
        message.setTextColor(textColor());
        message.setHintTextColor(mutedColor());
        message.setBackground(addressBarBackground());
        message.setPadding(dp(12), dp(10), dp(12), dp(10));
        message.setText(initialMessage == null ? "" : initialMessage);
        message.setSelection(message.getText().length());
        applyWriteMessageAaMode(message, false);

        boolean[] writeAaMode = new boolean[]{false};
        TextView[] aaToggleLabel = new TextView[1];
        ImageButton writeMenu = iconButton(R.drawable.ic_more_vert, text("\u66f8\u304d\u8fbc\u307f\u30e1\u30cb\u30e5\u30fc", "Write menu"),
                v -> showWriteActionMenu(v, tab, message, writeAaMode, aaToggleLabel));
        writeMenu.setColorFilter(textColor());

        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (writeAaMode[0]) {
                    message.post(() -> fitAaTextSize(message));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        form.addView(message, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(280)));

        LinearLayout titleRow = writeDialogTitleRow(
                tab.threadPage == null ? text("\u66f8\u304d\u8fbc\u307f", "Write") : tab.threadPage.title,
                writeMenu);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCustomTitle(titleRow)
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Post", null)
                .create();
        dialog.setOnShowListener(d -> {
            Theme.styleDialog(dialog, this);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String body = message.getText().toString();
            if (body.trim().isEmpty()) {
                Toast.makeText(this, text("\u672c\u6587\u3092\u5165\u529b", "Enter a message."), Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            submitPost(tab, name.getText().toString(), mail.getText().toString(), body);
            });
        });
        dialog.show();
        Theme.styleDialog(dialog, this);
        message.requestFocus();
        message.postDelayed(() -> {
            try {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.showSoftInput(message, InputMethodManager.SHOW_IMPLICIT);
            } catch (Exception ignored) {
            }
        }, 120);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMGBB_IMAGE && resultCode == RESULT_OK && data != null) {
            List<Uri> uris = selectedMediaUris(data);
            if (uris.isEmpty()) {
                Toast.makeText(this, text("\u753b\u50cf\u304c\u9078\u629e\u3055\u308c\u3066\u3044\u307e\u305b\u3093", "No image selected."), Toast.LENGTH_SHORT).show();
                return;
            }
            addPendingImgbbUploadUris(uris);
            renderPendingImgbbMedia();
        }
    }

    private void chooseImgbbUploadImage(EditText message) {
        pendingImgbbUploadMessage = message;
        if (imgbbApiKey().isEmpty()) {
            showImgbbApiKeyDialog(() -> showImgbbUploadDialog(message));
            return;
        }
        showImgbbUploadDialog(message);
    }

    private void showImgbbUploadDialog(EditText message) {
        pendingImgbbUploadMessage = message;
        pendingImgbbUploadUris = new ArrayList<>();
        pendingImgbbExpirationSeconds = 0;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(8), dp(18), 0);

        TextView expirationLabel = helperLine(text("\u524a\u9664\u307e\u3067\u306e\u671f\u9650", "Expiration"));
        root.addView(expirationLabel);
        LinearLayout presets = new LinearLayout(this);
        presets.setOrientation(LinearLayout.HORIZONTAL);
        int[] presetSeconds = {0, 300, 3600, 86400};
        String[] presetLabels = {text("\u306a\u3057", "None"), text("5\u5206", "5m"), text("1\u6642\u9593", "1h"), text("1\u65e5", "1d")};
        for (int i = 0; i < presetSeconds.length; i++) {
            int seconds = presetSeconds[i];
            TextView chip = actionRow(presetLabels[i]);
            chip.setGravity(Gravity.CENTER);
            chip.setOnClickListener(v -> {
                pendingImgbbExpirationSeconds = seconds;
                pendingImgbbExpirationInput.setText(seconds <= 0 ? "" : String.valueOf(seconds));
            });
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(0, dp(40), 1);
            chipParams.setMargins(0, 0, dp(6), dp(6));
            presets.addView(chip, chipParams);
        }
        root.addView(presets);

        pendingImgbbExpirationInput = new EditText(this);
        pendingImgbbExpirationInput.setSingleLine(true);
        pendingImgbbExpirationInput.setHint(text("\u79d2\u6570\u3001\u307e\u305f\u306f 10m / 2h / 7d", "Seconds, or 10m / 2h / 7d"));
        pendingImgbbExpirationInput.setTextColor(textColor());
        pendingImgbbExpirationInput.setHintTextColor(mutedColor());
        pendingImgbbExpirationInput.setBackground(addressBarBackground());
        pendingImgbbExpirationInput.setPadding(dp(12), 0, dp(12), 0);
        root.addView(pendingImgbbExpirationInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));

        pendingImgbbMediaBox = new LinearLayout(this);
        pendingImgbbMediaBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams mediaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mediaParams.setMargins(0, dp(10), 0, 0);
        root.addView(pendingImgbbMediaBox, mediaParams);
        renderPendingImgbbMedia();

        pendingImgbbUploadDialog = new AlertDialog.Builder(this)
                .setTitle(text("ImgBB\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9", "ImgBB upload"))
                .setView(root)
                .setNegativeButton(text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton("OK", null)
                .create();
        pendingImgbbUploadDialog.setOnShowListener(d -> {
            Theme.styleDialog(pendingImgbbUploadDialog, this);
            pendingImgbbUploadDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (pendingImgbbUploadUris.isEmpty()) {
                    Toast.makeText(this, text("\u753b\u50cf\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044", "Choose images."), Toast.LENGTH_SHORT).show();
                    return;
                }
                pendingImgbbExpirationSeconds = parseImgbbExpiration(pendingImgbbExpirationInput.getText().toString());
                uploadSelectedImagesToImgbb(new ArrayList<>(pendingImgbbUploadUris), pendingImgbbUploadMessage);
                pendingImgbbUploadDialog.dismiss();
            });
        });
        pendingImgbbUploadDialog.show();
    }

    private void renderPendingImgbbMedia() {
        if (pendingImgbbMediaBox == null) {
            return;
        }
        pendingImgbbMediaBox.removeAllViews();
        if (pendingImgbbUploadUris.isEmpty()) {
            TextView choose = actionRow(text("\u753b\u50cf\u3092\u9078\u629e", "Choose images"));
            choose.setGravity(Gravity.CENTER);
            choose.setOnClickListener(v -> openImgbbImagePicker(pendingImgbbUploadMessage));
            pendingImgbbMediaBox.addView(choose, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(92)));
            return;
        }
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(true);
        scroll.setFillViewport(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        int cellSize = dp(MEDIA_GRID_CELL_DP);
        int gap = dp(6);
        for (Uri uri : pendingImgbbUploadUris) {
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setBackgroundColor(Theme.dark(this) ? Color.rgb(15, 23, 42) : Color.rgb(241, 245, 249));
            image.setImageURI(uri);
            image.setOnClickListener(v -> openImgbbImagePicker(pendingImgbbUploadMessage));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cellSize, cellSize);
            params.setMargins(0, 0, gap, gap);
            row.addView(image, params);
        }
        TextView add = actionRow(text("+", "+"));
        add.setGravity(Gravity.CENTER);
        add.setTextSize(24);
        add.setOnClickListener(v -> openImgbbImagePicker(pendingImgbbUploadMessage));
        row.addView(add, new LinearLayout.LayoutParams(cellSize, cellSize));
        scroll.addView(row, new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        pendingImgbbMediaBox.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, cellSize + gap));
    }

    private void addPendingImgbbUploadUris(List<Uri> uris) {
        Set<String> seen = new LinkedHashSet<>();
        for (Uri current : pendingImgbbUploadUris) {
            if (current != null) {
                seen.add(current.toString());
            }
        }
        for (Uri uri : uris) {
            if (uri == null || !seen.add(uri.toString())) {
                continue;
            }
            pendingImgbbUploadUris.add(uri);
        }
    }

    private int parseImgbbExpiration(String value) {
        String raw = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (raw.isEmpty()) {
            return pendingImgbbExpirationSeconds;
        }
        try {
            int multiplier = 1;
            if (raw.endsWith("m")) {
                multiplier = 60;
                raw = raw.substring(0, raw.length() - 1).trim();
            } else if (raw.endsWith("h")) {
                multiplier = 3600;
                raw = raw.substring(0, raw.length() - 1).trim();
            } else if (raw.endsWith("d")) {
                multiplier = 86400;
                raw = raw.substring(0, raw.length() - 1).trim();
            }
            int seconds = Math.max(0, Integer.parseInt(raw) * multiplier);
            if (seconds > 0 && seconds < 60) {
                return 60;
            }
            return Math.min(seconds, 15552000);
        } catch (Exception ignored) {
            return pendingImgbbExpirationSeconds;
        }
    }

    private void openImgbbImagePicker(EditText message) {
        pendingImgbbUploadMessage = message;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        try {
            startActivityForResult(Intent.createChooser(intent,
                    text("\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u3059\u308b\u753b\u50cf", "Select images to upload")), REQUEST_IMGBB_IMAGE);
        } catch (Exception exception) {
            Toast.makeText(this, text("\u753b\u50cf\u9078\u629e\u3092\u958b\u3051\u307e\u305b\u3093", "Cannot open image picker."), Toast.LENGTH_LONG).show();
        }
    }

    private List<Uri> selectedMediaUris(Intent data) {
        List<Uri> uris = new ArrayList<>();
        if (data.getClipData() != null) {
            ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                if (uri != null) {
                    uris.add(uri);
                }
            }
        } else if (data.getData() != null) {
            uris.add(data.getData());
        }
        return uris;
    }

    private void showImgbbApiKeyDialog(Runnable afterSave) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(imgbbApiKey());
        input.setHint("API key");
        input.setTextColor(textColor());
        input.setHintTextColor(mutedColor());
        input.setBackground(addressBarBackground());
        input.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(8), dp(18), 0);
        root.addView(helperLine(text("ImgBB API key\u3092\u5165\u529b\u3057\u3066\u304f\u3060\u3055\u3044\u3002\u753b\u50cf\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u306b\u5fc5\u8981\u3067\u3059\u3002",
                "Enter an ImgBB API key. It is required for image uploads.")));
        TextView help = actionRow(text("API key\u306e\u53d6\u5f97\u65b9\u6cd5", "How to get an API key"));
        help.setOnClickListener(v -> showImgbbApiKeyHelp());
        root.addView(help);
        root.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(text("ImgBB API key", "ImgBB API key"))
                .setView(root)
                .setNegativeButton(text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(text("\u4fdd\u5b58", "Save"), null)
                .create();
        dialog.setOnShowListener(d -> {
            Theme.styleDialog(dialog, this);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String value = input.getText().toString().trim();
                if (value.isEmpty()) {
                    Toast.makeText(this, text("API key\u3092\u5165\u529b", "Enter an API key."), Toast.LENGTH_SHORT).show();
                    return;
                }
                preferences.edit().putString(PREF_IMGBB_API_KEY, value).apply();
                dialog.dismiss();
                if (afterSave != null) {
                    afterSave.run();
                }
            });
        });
        dialog.show();
    }

    private void showImgbbApiKeyHelp() {
        String message = text(
                "1. ImgBB\u306b\u30ed\u30b0\u30a4\u30f3\u3057\u307e\u3059\u3002\n"
                        + "2. https://api.imgbb.com/ \u3092\u958b\u304d\u307e\u3059\u3002\n"
                        + "3. \u8868\u793a\u3055\u308c\u305f API key \u3092\u30b3\u30d4\u30fc\u3057\u3001\u3053\u3053\u306b\u5165\u529b\u3057\u307e\u3059\u3002",
                "1. Sign in to ImgBB.\n"
                        + "2. Open https://api.imgbb.com/.\n"
                        + "3. Copy the displayed API key and enter it here.");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(text("ImgBB API key\u306e\u53d6\u5f97\u65b9\u6cd5", "How to get an ImgBB API key"))
                .setMessage(message)
                .setNegativeButton(text("\u9589\u3058\u308b", "Close"), null)
                .setPositiveButton(text("ImgBB API\u3092\u958b\u304f", "Open ImgBB API"),
                        (d, which) -> openExternal("https://api.imgbb.com/"))
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private void uploadSelectedImagesToImgbb(List<Uri> uris, EditText message) {
        String apiKey = imgbbApiKey();
        if (apiKey.isEmpty()) {
            showImgbbApiKeyDialog(() -> uploadSelectedImagesToImgbb(uris, message));
            return;
        }
        Toast.makeText(this, text("ImgBB\u306b\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u4e2d", "Uploading to ImgBB..."), Toast.LENGTH_SHORT).show();
        int parallelism = Math.max(1, Math.min(3, uris.size()));
        ExecutorService uploadExecutor = Executors.newFixedThreadPool(parallelism);
        AtomicInteger remaining = new AtomicInteger(uris.size());
        for (Uri uri : uris) {
            uploadExecutor.execute(() -> {
                try {
                    ImgbbUploadResult result = uploadImageToImgbb(uri, apiKey, pendingImgbbExpirationSeconds);
                    saveImgbbUpload(result);
                    runOnUiThread(() -> {
                        appendUploadUrl(message, result.link);
                        Toast.makeText(this, text("ImgBB\u306b\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u3057\u307e\u3057\u305f", "Uploaded to ImgBB.") + "\n" + result.link,
                                Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception exception) {
                    String error = exception.getMessage() == null
                            ? text("\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u5931\u6557", "Upload failed.")
                            : exception.getMessage();
                    runOnUiThread(() -> Toast.makeText(this, error, Toast.LENGTH_LONG).show());
                } finally {
                    if (remaining.decrementAndGet() == 0) {
                        uploadExecutor.shutdown();
                    }
                }
            });
        }
    }

    private ImgbbUploadResult uploadImageToImgbb(Uri uri, String apiKey, int expirationSeconds) throws Exception {
        String mime = getContentResolver().getType(uri);
        if (mime == null || !mime.startsWith("image/")) {
            throw new IllegalStateException(text("\u753b\u50cf\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044", "Choose an image."));
        }
        String name = displayNameForUri(uri);
        String endpoint = "https://api.imgbb.com/1/upload?key="
                + URLEncoder.encode(apiKey, POST_CHARSET.name());
        if (expirationSeconds >= 60 && expirationSeconds <= 15552000) {
            endpoint += "&expiration=" + expirationSeconds;
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        String boundary = "----CuspiDroidImgBB" + System.currentTimeMillis();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(60000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(true);
        connection.setChunkedStreamingMode(0);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        try (OutputStream output = connection.getOutputStream()) {
            writeMultipartFile(output, boundary, "image", name, mime, uri);
            output.write(("--" + boundary + "--\r\n").getBytes(POST_CHARSET));
        }
        int code = connection.getResponseCode();
        InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String response = stream == null ? "" : readText(stream, Charset.forName("UTF-8"));
        connection.disconnect();
        JSONObject root = new JSONObject(response);
        if (code >= 400 || !root.optBoolean("success", false)) {
            JSONObject error = root.optJSONObject("error");
            String message = error == null ? root.optString("error", response) : error.optString("message", response);
            throw new IllegalStateException("ImgBB: " + message);
        }
        JSONObject data = root.getJSONObject("data");
        String link = data.optString("url", "");
        if (link.isEmpty()) {
            throw new IllegalStateException(text("ImgBB URL\u3092\u53d6\u5f97\u3067\u304d\u307e\u305b\u3093", "Could not read ImgBB URL."));
        }
        return new ImgbbUploadResult(name, mime, link, data.optString("delete_url", ""),
                expirationSeconds >= 60 && expirationSeconds <= 15552000 ? expirationSeconds : 0,
                System.currentTimeMillis());
    }

    private void writeMultipartField(OutputStream output, String boundary, String name, String value) throws Exception {
        output.write(("--" + boundary + "\r\n").getBytes(POST_CHARSET));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(POST_CHARSET));
        output.write(value.getBytes(POST_CHARSET));
        output.write("\r\n".getBytes(POST_CHARSET));
    }

    private void writeMultipartFile(OutputStream output, String boundary, String fieldName,
                                    String fileName, String mime, Uri uri) throws Exception {
        output.write(("--" + boundary + "\r\n").getBytes(POST_CHARSET));
        output.write(("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + sanitizeMultipartName(fileName) + "\"\r\n").getBytes(POST_CHARSET));
        output.write(("Content-Type: " + mime + "\r\n\r\n").getBytes(POST_CHARSET));
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) {
                throw new IllegalStateException(text("\u30e1\u30c7\u30a3\u30a2\u3092\u8aad\u307f\u8fbc\u3081\u307e\u305b\u3093", "Cannot read media."));
            }
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }
        output.write("\r\n".getBytes(POST_CHARSET));
    }

    private String sanitizeMultipartName(String value) {
        return (value == null || value.trim().isEmpty() ? "media" : value).replace("\"", "'");
    }

    private String displayNameForUri(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = cursor.getString(index);
                    if (name != null && !name.trim().isEmpty()) {
                        return name;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        String path = uri == null ? "" : uri.getLastPathSegment();
        return path == null || path.trim().isEmpty() ? "media" : path;
    }

    private void appendUploadUrl(EditText message, String url) {
        if (message == null || url == null || url.isEmpty()) {
            return;
        }
        String current = message.getText().toString();
        String insert = (current.isEmpty() || current.endsWith("\n") ? "" : "\n") + url + "\n";
        int start = Math.max(0, message.getSelectionStart());
        int end = Math.max(0, message.getSelectionEnd());
        int from = Math.min(start, end);
        int to = Math.max(start, end);
        message.getText().replace(from, to, insert);
        message.requestFocus();
    }

    private String imgbbApiKey() {
        return preferences.getString(PREF_IMGBB_API_KEY, "").trim();
    }

    private synchronized void saveImgbbUpload(ImgbbUploadResult result) {
        try {
            JSONArray array = new JSONArray(preferences.getString(PREF_IMGBB_UPLOADS, "[]"));
            JSONArray next = new JSONArray();
            JSONObject item = new JSONObject();
            item.put("name", result.name);
            item.put("mime", result.mime);
            item.put("url", result.link);
            item.put("delete_url", result.deleteUrl);
            item.put("expiration", result.expirationSeconds);
            item.put("time", result.time);
            next.put(item);
            for (int i = 0; i < array.length() && i < 199; i++) {
                next.put(array.getJSONObject(i));
            }
            preferences.edit().putString(PREF_IMGBB_UPLOADS, next.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private void submitPost(CuspTab tab, String name, String mail, String message) {
        DatAddress address = datAddress(tab.url);
        if (address == null) {
            Toast.makeText(this, text("\u66f8\u304d\u8fbc\u307f\u5148\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093", "Cannot find thread write target."), Toast.LENGTH_SHORT).show();
            return;
        }
        int readNumberBeforePost = tab.readPostNumber;
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
                    saveMyPost(tab, message);
                    refreshThreadFromBottom(tab, true, false, true,
                            () -> markPostedOwnPostReadIfNextUnread(tab, message, readNumberBeforePost));
                } else {
                    showCopyablePostFailure(messageText);
                }
            });
        });
    }

    private void markPostedOwnPostReadIfNextUnread(CuspTab tab, String body, int readNumberBeforePost) {
        if (tab == null || tab.threadPage == null || body == null) {
            return;
        }
        String submittedHash = postBodyHash(body);
        if (submittedHash.isEmpty()) {
            return;
        }
        Post firstUnread = null;
        for (Post post : tab.threadPage.posts) {
            if (post.number > readNumberBeforePost) {
                firstUnread = post;
                break;
            }
        }
        if (firstUnread == null || !submittedHash.equals(postBodyHash(firstUnread.body))) {
            return;
        }
        markReadTo(tab, firstUnread.number, false);
        renderTabs();
    }

    private void showCopyablePostFailure(String messageText) {
        TextView message = new TextView(this);
        message.setText(messageText);
        message.setTextColor(textColor());
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
        AlertDialog dialog = builder.show();
        Theme.styleDialog(dialog, this);
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
        byte[] body = payload.getBytes(POST_CHARSET);
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(18000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", "Monazilla/1.00 CuspiDroid/0.1");
        connection.setRequestProperty("Referer", threadUrl);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
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
        byte[] body = payload.getBytes(POST_CHARSET);
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(12000);
        connection.setReadTimeout(18000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", "Monazilla/1.00 CuspiDroid/0.1");
        connection.setRequestProperty("Referer", referer);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
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

    private void confirmDeleteWriteSiteCookies(CuspTab tab) {
        String siteUrl = cookieSiteUrl(tab);
        if (siteUrl == null) {
            Toast.makeText(this, text("\u30b5\u30a4\u30c8URL\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093", "No site URL found."), Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(text("\u30af\u30c3\u30ad\u30fc\u3092\u524a\u9664", "Delete cookies"))
                .setMessage(text("\u3053\u306e\u63b2\u793a\u677f\u306e\u30af\u30c3\u30ad\u30fc\u3092\u524a\u9664\u3057\u307e\u3059\u304b\uff1f\n", "Delete this board's cookies?\n") + siteUrl)
                .setNegativeButton(text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(text("\u524a\u9664", "Delete"), (d, which) -> deleteCookiesForSite(siteUrl))
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private String cookieSiteUrl(CuspTab tab) {
        if (tab == null || tab.url == null) {
            return null;
        }
        try {
            Uri uri = Uri.parse(normalizeUrl(tab.url));
            String scheme = uri.getScheme() == null ? "https" : uri.getScheme();
            String host = uri.getHost();
            if (host == null || host.trim().isEmpty()) {
                return null;
            }
            return scheme + "://" + host + "/";
        } catch (Exception ignored) {
            return null;
        }
    }

    private void deleteCookiesForSite(String siteUrl) {
        try {
            CookieManager manager = CookieManager.getInstance();
            String cookies = manager.getCookie(siteUrl);
            if (cookies == null || cookies.trim().isEmpty()) {
                Toast.makeText(this, text("\u524a\u9664\u3059\u308b\u30af\u30c3\u30ad\u30fc\u306f\u3042\u308a\u307e\u305b\u3093", "No cookies to delete."), Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = Uri.parse(siteUrl);
            String host = uri.getHost();
            String[] parts = cookies.split(";");
            for (String part : parts) {
                String name = part.split("=", 2)[0].trim();
                if (name.isEmpty()) {
                    continue;
                }
                String expired = name + "=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Max-Age=0; Path=/";
                manager.setCookie(siteUrl, expired);
                if (host != null && !host.isEmpty()) {
                    manager.setCookie(siteUrl, expired + "; Domain=" + host);
                    manager.setCookie(siteUrl, expired + "; Domain=." + host);
                }
            }
            manager.flush();
            Toast.makeText(this, text("\u30af\u30c3\u30ad\u30fc\u3092\u524a\u9664\u3057\u307e\u3057\u305f", "Cookies deleted."), Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Toast.makeText(this, exception.getMessage() == null
                    ? text("\u30af\u30c3\u30ad\u30fc\u3092\u524a\u9664\u3067\u304d\u307e\u305b\u3093", "Could not delete cookies.")
                    : exception.getMessage(), Toast.LENGTH_LONG).show();
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
        return URLEncoder.encode(name, POST_CHARSET.name()) + "="
                + URLEncoder.encode(value == null ? "" : value, POST_CHARSET.name());
    }

    private void rememberThreadScroll(CuspTab tab) {
        if (tab == null || tab.threadScroll == null || tab.threadScroll.getChildCount() == 0) {
            return;
        }
        if (tab.threadScroll.getHeight() <= 0 || !tab.threadScroll.isAttachedToWindow()) {
            return;
        }
        int range = tab.threadScroll.getChildAt(0).getHeight() - tab.threadScroll.getHeight();
        if (range <= 0) {
            return;
        }
        tab.threadScrollRatio = range <= 0 ? 0f : Math.max(0f, Math.min(1f, tab.threadScroll.getScrollY() / (float) range));
        tab.threadBottomOffset = range <= 0 ? 0 : Math.max(0, range - tab.threadScroll.getScrollY());
        tab.threadScrollUrl = threadUrl(tab);
        tab.hasSavedThreadScroll = true;
    }

    private void restoreThreadScroll(CuspTab tab) {
        restoreThreadScroll(tab, 0);
    }

    private void restoreThreadScroll(CuspTab tab, int attempt) {
        if (tab == null || tab.threadScroll == null) {
            return;
        }
        if (isBottomJumpActive(tab)) {
            return;
        }
        tab.threadScroll.post(() -> {
            if (isBottomJumpActive(tab)) {
                return;
            }
            if (tab.threadScroll == null || tab.threadScroll.getChildCount() == 0) {
                revealThreadAfterScrollRestore(tab, attempt);
                return;
            }
            int range = tab.threadScroll.getChildAt(0).getHeight() - tab.threadScroll.getHeight();
            if (range <= 0) {
                if (attempt < 10) {
                    tab.threadScroll.postDelayed(() -> restoreThreadScroll(tab, attempt + 1), 50);
                } else {
                    revealThreadAfterScrollRestore(tab, attempt);
                }
                return;
            }
            if (tab.restoreFromBottom) {
                tab.threadScroll.scrollTo(0, Math.max(0, range - tab.threadBottomOffset));
                tab.restoreFromBottom = false;
            } else if (tab.hasSavedThreadScroll && threadUrl(tab).equals(tab.threadScrollUrl)) {
                int target = (int) (range * tab.threadScrollRatio);
                tab.threadScroll.scrollTo(0, Math.max(0, Math.min(target, range)));
            } else if (autoScrollUnreadBoundary()) {
                scrollToUnreadBoundaryWhenReady(tab, 0);
            }
            revealThreadAfterScrollRestore(tab, attempt);
            scheduleThreadScrollChromeRefresh(tab, 6);
        });
    }

    private boolean shouldRestoreThreadScroll(CuspTab tab) {
        return tab != null
                && tab.hasSavedThreadScroll
                && threadUrl(tab).equals(tab.threadScrollUrl);
    }

    private void revealThreadAfterScrollRestore(CuspTab tab, int attempt) {
        if (tab == null || tab.readerView == null) {
            return;
        }
        tab.readerView.setVisibility(View.VISIBLE);
    }

    private String threadUrl(CuspTab tab) {
        if (tab == null) {
            return "";
        }
        if (tab.threadPage != null && tab.threadPage.url != null && !tab.threadPage.url.isEmpty()) {
            return tab.threadPage.url;
        }
        return tab.url == null ? "" : tab.url;
    }

    private void scrollToUnreadBoundaryWhenReady(CuspTab tab, int attempt) {
        if (scrollToUnreadBoundary(tab)) {
            scheduleThreadPostVisibilityRefresh(tab);
            if (tab != null && tab.threadScroll != null && attempt < 3) {
                tab.threadScroll.postDelayed(() -> scrollToUnreadBoundaryWhenReady(tab, attempt + 1), 48);
            }
            return;
        }
        if (tab != null && tab.threadScroll != null && attempt < 12) {
            tab.threadScroll.postDelayed(() -> scrollToUnreadBoundaryWhenReady(tab, attempt + 1), 50);
        }
    }

    private boolean scrollToUnreadBoundary(CuspTab tab) {
        if (tab == null || tab.threadScroll == null || tab.threadPage == null) {
            return false;
        }
        View target = null;
        for (Post post : tab.threadPage.posts) {
            if (post.number > tab.readPostNumber) {
                target = renderPostForJump(tab, post.number);
                if (target != null) {
                    break;
                }
            }
        }
        if (target == null && !tab.threadPage.posts.isEmpty()) {
            for (int i = tab.threadPage.posts.size() - 1; i >= 0; i--) {
                target = renderPostForJump(tab, tab.threadPage.posts.get(i).number);
                if (target != null) {
                    break;
                }
            }
        }
        if (target == null) {
            return false;
        }
        View scrollChild = tab.threadScroll.getChildAt(0);
        if (scrollChild == null) {
            return false;
        }
        int y = 0;
        View current = target;
        while (current != null && current != scrollChild) {
            y += current.getTop();
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }
        int maxY = Math.max(0, scrollChild.getHeight() - tab.threadScroll.getHeight());
        tab.threadScroll.scrollTo(0, Math.max(0, Math.min(y - dp(8), maxY)));
        return true;
    }

    private View postAnchorView(CuspTab tab, int postNumber) {
        if (tab == null) {
            return null;
        }
        View view = tab.postViews == null ? null : tab.postViews.get(postNumber);
        if (view != null) {
            return view;
        }
        return tab.postSlots == null ? null : tab.postSlots.get(postNumber);
    }

    private boolean routeLink(String rawUrl, CuspTab sourceTab) {
        String url = normalizeUrl(rawUrl);
        if (pendingNewTab) {
            openPendingNewTabUrl(url);
            return true;
        }
        if (isThreadUrl(url)) {
            if (open5chLinksInNewTab()) {
                CuspTab source = sourceTab == null ? currentTab() : sourceTab;
                createTab(url, true, tabs.indexOf(source), false, isPrivateTab(source));
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
            if (isBoardUrl(url) || isBbsDirectoryUrl(url)) {
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
                CuspTab source = sourceTab == null ? currentTab() : sourceTab;
                createTab(url, true, tabs.indexOf(source), false, isPrivateTab(source));
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

        if (isRegisteredBbsUrl(url) && (isBbsDirectoryUrl(url) || isBoardUrl(url) || isThreadUrl(url))) {
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

        openExternal(url);
        return true;
    }

    private boolean open5chLinksInNewTab() {
        return preferences.getBoolean(PREF_5CH_NEW_TAB, true);
    }

    private boolean showMediaPreviews() {
        return preferences.getBoolean(PREF_SHOW_MEDIA, true);
    }

    private boolean blurImgurImages() {
        return showMediaPreviews() && preferences.getBoolean(PREF_BLUR_IMGUR, true);
    }

    private boolean blurVideoThumbnails() {
        return blurImgurImages() && preferences.getBoolean(PREF_BLUR_VIDEO_THUMBNAILS, true);
    }

    private boolean blurGifThumbnails() {
        return blurImgurImages() && preferences.getBoolean(PREF_BLUR_GIF_THUMBNAILS, true);
    }

    private boolean autoplayGifs() {
        return showMediaPreviews() && preferences.getBoolean(PREF_AUTOPLAY_GIFS, false);
    }

    private boolean autoAaEnabled() {
        return preferences.getBoolean(PREF_AUTO_AA, true);
    }

    private boolean aaDebugEnabled() {
        return preferences.getBoolean(PREF_AA_DEBUG, false);
    }

    private boolean addressBarOnTop() {
        return preferences.getBoolean(PREF_ADDRESS_BAR_TOP, false);
    }

    private boolean treeViewEnabled() {
        return preferences.getBoolean(PREF_TREE_VIEW, true);
    }

    private boolean skipFirstReplyInTree() {
        return preferences.getBoolean(PREF_TREE_SKIP_FIRST_REPLY, false);
    }

    private boolean autoScrollUnreadBoundary() {
        return preferences.getBoolean(PREF_AUTO_SCROLL_UNREAD, true);
    }

    private boolean copyPasteOmitEnabled() {
        return preferences.getBoolean(PREF_OMIT_COPYPASTE, false);
    }

    private boolean externalLinksInApp() {
        return preferences.getBoolean(PREF_EXTERNAL_LINK_IN_APP, false);
    }

    private void openExternal(String url) {
        if (externalLinksInApp() && openInDefaultBrowserCustomTab(url)) {
            return;
        }
        openInDefaultBrowser(url);
    }

    private boolean openInDefaultBrowserCustomTab(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.putExtra("android.support.customtabs.extra.SESSION", (android.os.Bundle) null);
            intent.putExtra("android.support.customtabs.extra.TITLE_VISIBILITY", 1);
            String browserPackage = defaultBrowserPackage(url);
            if (browserPackage != null && !browserPackage.equals(getPackageName())) {
                intent.setPackage(browserPackage);
            }
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException error) {
            return false;
        }
    }

    private void openInDefaultBrowser(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            String browserPackage = defaultBrowserPackage(url);
            if (browserPackage != null && !browserPackage.equals(getPackageName())) {
                intent.setPackage(browserPackage);
            }
            startActivity(intent);
        } catch (ActivityNotFoundException error) {
            Toast.makeText(this, text("\u958b\u3051\u308b\u30a2\u30d7\u30ea\u304c\u3042\u308a\u307e\u305b\u3093", "No app can open this link."), Toast.LENGTH_SHORT).show();
        }
    }

    private String defaultBrowserPackage(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            ResolveInfo info = getPackageManager().resolveActivity(intent, 0);
            if (info == null || info.activityInfo == null) {
                return null;
            }
            String packageName = info.activityInfo.packageName;
            if (packageName == null || packageName.equals("android")) {
                return null;
            }
            return packageName;
        } catch (Exception ignored) {
            return null;
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
        if (tab != null && tab.threadPage != null && !tab.threadPage.posts.isEmpty()) {
            pageSearchOpen = false;
            tab.threadSearchOpen = true;
            updateThreadSearchBar(tab);
        } else {
            pageSearchOpen = true;
            pageSearchQuery = "";
            pageSearchMatches.clear();
            updateThreadSearchBar(tab);
        }
        focusThreadSearchInput();
    }

    private boolean isThreadPageSearchActive() {
        CuspTab tab = currentTab();
        return tab != null && tab.threadSearchOpen && tab.threadPage != null && NATIVE_THREAD.equals(tab.nativeKind);
    }

    private boolean isPageSearchOpen() {
        CuspTab tab = currentTab();
        return pageSearchOpen || (tab != null && tab.threadSearchOpen);
    }

    private void focusThreadSearchInput() {
        if (threadSearchInput == null) {
            return;
        }
        threadSearchInput.requestFocus();
        threadSearchInput.selectAll();
        mainHandler.postDelayed(() -> {
            threadSearchInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(threadSearchInput, InputMethodManager.SHOW_FORCED);
            }
        }, 80);
    }

    private void scheduleThreadSearch(String query, boolean resetIndex) {
        if (threadSearchTask != null) {
            mainHandler.removeCallbacks(threadSearchTask);
        }
        CuspTab tab = currentTab();
        if (tab == null) {
            return;
        }
        int generation = ++tab.threadSearchGeneration;
        threadSearchTask = () -> {
            if (tab == currentTab() && tab.threadSearchGeneration == generation) {
                updateThreadSearch(query, resetIndex);
            }
        };
        mainHandler.postDelayed(threadSearchTask, 90);
    }

    private void updateThreadSearch(String query, boolean resetIndex) {
        CuspTab tab = currentTab();
        if (tab == null || tab.threadPage == null) {
            return;
        }
        tab.threadSearchQuery = query == null ? "" : query;
        Set<Integer> previousHighlights = new LinkedHashSet<>(tab.threadSearchHighlightedPosts);
        tab.threadSearchMatches.clear();
        String needle = tab.threadSearchQuery.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            tab.threadSearchIndex = -1;
            tab.threadSearchLastQuery = "";
            tab.threadSearchLastCandidates = new ArrayList<>();
            tab.threadSearchHighlightedPosts.clear();
            scheduleThreadHighlightRender(tab, previousHighlights);
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
            if (fastContains(haystack, needle)) {
                tab.threadSearchMatches.add(post.number);
                tab.threadSearchLastCandidates.add(post);
            }
        }
        tab.threadSearchLastQuery = needle;
        tab.threadSearchHighlightedPosts = new LinkedHashSet<>(tab.threadSearchMatches);
        if (tab.threadSearchMatches.isEmpty()) {
            tab.threadSearchIndex = -1;
        } else if (resetIndex || tab.threadSearchIndex < 0 || tab.threadSearchIndex >= tab.threadSearchMatches.size()) {
            tab.threadSearchIndex = 0;
        }
        Set<Integer> rerenderTargets = new LinkedHashSet<>(previousHighlights);
        rerenderTargets.addAll(tab.threadSearchHighlightedPosts);
        scheduleThreadHighlightRender(tab, rerenderTargets);
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

    private void moveActivePageSearch(int direction) {
        if (isThreadPageSearchActive()) {
            moveThreadSearch(direction);
        } else {
            movePageSearch(direction);
        }
    }

    private void closeThreadSearch() {
        CuspTab tab = currentTab();
        if (tab != null) {
            Set<Integer> previousHighlights = new LinkedHashSet<>(tab.threadSearchHighlightedPosts);
            tab.threadSearchOpen = false;
            tab.threadSearchQuery = "";
            tab.threadSearchMatches.clear();
            tab.threadSearchLastQuery = "";
            tab.threadSearchLastCandidates = new ArrayList<>();
            tab.threadSearchHighlightedPosts.clear();
            tab.threadSearchGeneration++;
            if (threadSearchTask != null) {
                mainHandler.removeCallbacks(threadSearchTask);
                threadSearchTask = null;
            }
            tab.threadSearchIndex = -1;
            if (threadSearchHighlightTask != null) {
                mainHandler.removeCallbacks(threadSearchHighlightTask);
                threadSearchHighlightTask = null;
            }
            rerenderThreadHighlightsChunked(tab, previousHighlights, tab.threadSearchGeneration);
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
        int pageGeneration = ++pageSearchGeneration;
        clearPageSearchHighlightsChunked(pageGeneration);
        pageSearchOpen = false;
        pageSearchQuery = "";
        pageSearchMatches.clear();
        pageSearchIndex = -1;
    }

    private void updateThreadSearchBar(CuspTab tab) {
        if (threadSearchBar == null || threadSearchInput == null || threadSearchCount == null) {
            return;
        }
        boolean threadShow = tab != null && tab.threadSearchOpen && NATIVE_THREAD.equals(tab.nativeKind);
        boolean show = threadShow || pageSearchOpen;
        threadSearchBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) {
            return;
        }
        String query = threadShow ? (tab.threadSearchQuery == null ? "" : tab.threadSearchQuery) : pageSearchQuery;
        if (!query.contentEquals(threadSearchInput.getText())) {
            updatingThreadSearchInput = true;
            threadSearchInput.setText(query);
            threadSearchInput.setSelection(threadSearchInput.getText().length());
            updatingThreadSearchInput = false;
        }
        if (threadShow) {
            updateThreadSearchCount(tab);
        } else {
            updatePageSearchCount();
        }
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

    private void updatePageSearch(String query, boolean resetIndex) {
        pageSearchGeneration++;
        pageSearchQuery = query == null ? "" : query;
        clearPageSearchHighlights();
        pageSearchMatches.clear();
        String needle = pageSearchQuery.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            pageSearchIndex = -1;
            updatePageSearchCount();
            return;
        }
        List<TextView> textViews = new ArrayList<>();
        collectTextViews(contentFrame, textViews);
        for (TextView view : textViews) {
            CharSequence original = view.getText();
            if (original == null) {
                continue;
            }
            String haystack = original.toString().toLowerCase(Locale.ROOT);
            if (!fastContains(haystack, needle)) {
                continue;
            }
            pageSearchOriginalText.put(view, original);
            SpannableString highlighted = new SpannableString(original);
            int start = haystack.indexOf(needle);
            while (start >= 0) {
                int end = start + needle.length();
                highlighted.setSpan(new BackgroundColorSpan(Theme.linkHighlight(this)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                highlighted.setSpan(new ForegroundColorSpan(Color.rgb(15, 118, 110)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = haystack.indexOf(needle, end);
            }
            view.setText(highlighted);
            pageSearchMatches.add(view);
        }
        if (pageSearchMatches.isEmpty()) {
            pageSearchIndex = -1;
        } else if (resetIndex || pageSearchIndex < 0 || pageSearchIndex >= pageSearchMatches.size()) {
            pageSearchIndex = 0;
            jumpToPageSearchMatch();
        }
        updatePageSearchCount();
    }

    private void collectTextViews(View view, List<TextView> out) {
        if (view instanceof TextView && view != threadSearchInput && view != threadSearchCount) {
            out.add((TextView) view);
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                collectTextViews(group.getChildAt(i), out);
            }
        }
    }

    private void clearPageSearchHighlights() {
        for (Map.Entry<TextView, CharSequence> entry : pageSearchOriginalText.entrySet()) {
            entry.getKey().setText(entry.getValue());
        }
        pageSearchOriginalText.clear();
    }

    private void clearPageSearchHighlightsChunked(int generation) {
        if (pageSearchOriginalText.isEmpty()) {
            return;
        }
        List<Map.Entry<TextView, CharSequence>> entries = new ArrayList<>(pageSearchOriginalText.entrySet());
        pageSearchOriginalText.clear();
        restorePageSearchHighlightsChunk(entries, 0, generation);
    }

    private void restorePageSearchHighlightsChunk(List<Map.Entry<TextView, CharSequence>> entries, int start, int generation) {
        if (pageSearchGeneration != generation) {
            return;
        }
        int end = Math.min(entries.size(), start + 12);
        for (int i = start; i < end; i++) {
            Map.Entry<TextView, CharSequence> entry = entries.get(i);
            TextView view = entry.getKey();
            if (view != null && view.isAttachedToWindow()) {
                view.setText(entry.getValue());
            }
        }
        if (end < entries.size()) {
            mainHandler.post(() -> restorePageSearchHighlightsChunk(entries, end, generation));
        }
    }

    private void movePageSearch(int direction) {
        if (pageSearchMatches.isEmpty()) {
            return;
        }
        int size = pageSearchMatches.size();
        pageSearchIndex = (pageSearchIndex + direction + size) % size;
        updatePageSearchCount();
        jumpToPageSearchMatch();
    }

    private void updatePageSearchCount() {
        if (threadSearchCount == null) {
            return;
        }
        if (pageSearchQuery == null || pageSearchQuery.trim().isEmpty()) {
            threadSearchCount.setText("");
        } else if (pageSearchMatches.isEmpty()) {
            threadSearchCount.setText("0/0");
        } else {
            threadSearchCount.setText((pageSearchIndex + 1) + "/" + pageSearchMatches.size());
        }
    }

    private void jumpToPageSearchMatch() {
        if (pageSearchIndex < 0 || pageSearchIndex >= pageSearchMatches.size()) {
            return;
        }
        TextView target = pageSearchMatches.get(pageSearchIndex);
        ScrollView scroll = findScrollView(contentFrame);
        if (scroll == null || scroll.getChildCount() == 0) {
            return;
        }
        View current = target;
        int y = 0;
        View scrollChild = scroll.getChildAt(0);
        while (current != null && current != scrollChild) {
            y += current.getTop();
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }
        int maxY = Math.max(0, scrollChild.getHeight() - scroll.getHeight());
        scroll.smoothScrollTo(0, Math.max(0, Math.min(y - dp(8), maxY)));
    }

    private void rerenderThreadHighlights(CuspTab tab) {
        if (tab == null) {
            return;
        }
        rerenderThreadHighlights(tab, new LinkedHashSet<>(tab.threadSearchHighlightedPosts));
    }

    private void rerenderThreadHighlights(CuspTab tab, Set<Integer> targets) {
        if (tab == null || tab.threadPage == null || tab.postViews == null) {
            return;
        }
        if (targets != null && !targets.isEmpty()) {
            for (Integer number : targets) {
                rerenderThreadHighlight(tab, number == null ? -1 : number);
            }
            return;
        }
        for (Post post : tab.threadPage.posts) {
            rerenderThreadHighlight(tab, post.number);
        }
    }

    private void rerenderThreadHighlightsChunked(CuspTab tab, Set<Integer> targets, int generation) {
        if (tab == null || tab.threadPage == null || tab.postViews == null || targets == null || targets.isEmpty()) {
            return;
        }
        List<Integer> numbers = new ArrayList<>(targets);
        rerenderThreadHighlightChunk(tab, numbers, 0, generation);
    }

    private void rerenderThreadHighlightChunk(CuspTab tab, List<Integer> numbers, int start, int generation) {
        if (tab == null || tab != currentTab() || tab.threadSearchGeneration != generation) {
            return;
        }
        int end = Math.min(numbers.size(), start + 8);
        for (int i = start; i < end; i++) {
            Integer number = numbers.get(i);
            rerenderThreadHighlight(tab, number == null ? -1 : number);
        }
        if (end < numbers.size()) {
            mainHandler.post(() -> rerenderThreadHighlightChunk(tab, numbers, end, generation));
        }
    }

    private void rerenderThreadHighlight(CuspTab tab, int postNumber) {
        if (tab == null || tab.threadPage == null || tab.postViews == null) {
            return;
        }
        Post post = tab.threadPage.postsByNumber.get(postNumber);
        if (post == null) {
            return;
        }
        View cardView = tab.postViews.get(post.number);
        if (!(cardView instanceof LinearLayout)) {
            return;
        }
        LinearLayout card = (LinearLayout) cardView;
        if (card.getChildCount() < 2) {
            return;
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

    private void scheduleThreadHighlightRender(CuspTab tab, Set<Integer> targets) {
        if (threadSearchHighlightTask != null) {
            mainHandler.removeCallbacks(threadSearchHighlightTask);
        }
        int generation = ++tab.threadSearchGeneration;
        Set<Integer> renderTargets = targets == null ? new LinkedHashSet<>() : new LinkedHashSet<>(targets);
        threadSearchHighlightTask = () -> {
            if (tab == currentTab() && tab.threadSearchGeneration == generation) {
                rerenderThreadHighlights(tab, renderTargets);
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
        shareUrl(tab.url, text("\u30b9\u30ec\u3092\u5171\u6709", "Share thread"));
    }

    private void shareUrl(String url) {
        shareUrl(url, text("\u30ea\u30f3\u30af\u3092\u5171\u6709", "Share link"));
    }

    private void shareUrl(String url, String title) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(intent, title));
    }

    private void goBack() {
        CuspTab tab = currentTab();
        if (tab == null || tab.navigationIndex <= 0 || tab.navigationIndex > tab.navigationHistory.size() - 1) {
            clearAddressFocus();
            if (tab != null && tab.backToNewTab) {
                closeCurrentTab();
                showPendingNewTab();
            } else if (tab != null && tab.returnToIndex >= 0) {
                int returnIndex = Math.max(0, Math.min(tab.returnToIndex, tabs.size() - 1));
                closeCurrentTab();
                if (!pendingNewTab && !tabs.isEmpty()) {
                    switchToTab(Math.max(0, Math.min(returnIndex, tabs.size() - 1)));
                }
            }
            return;
        }
        clearAddressFocus();
        tab.navigationIndex--;
        requestSaveTabsSoon();
        navigateHistory(tab);
    }

    private void goForward() {
        if (pendingNewTab) {
            if (canGoForwardInNewTab()) {
                navigateNewTabHistory(1);
            }
            clearAddressFocus();
            return;
        }
        CuspTab tab = currentTab();
        if (!canGoForwardInCurrentTab(tab)) {
            clearAddressFocus();
            return;
        }
        clearAddressFocus();
        int nextIndex = tab.navigationIndex + 1;
        tab.navigationIndex = nextIndex;
        requestSaveTabsSoon();
        navigateHistory(tab);
    }

    private void navigateHistory(CuspTab tab) {
        if (tab == null || tab.navigationIndex < 0 || tab.navigationIndex >= tab.navigationHistory.size()) {
            return;
        }
        int tabIndex = tabs.indexOf(tab);
        if (tabIndex >= 0 && tabIndex != currentIndex) {
            switchToTab(tabIndex);
        }
        String url = tab.navigationHistory.get(tab.navigationIndex);
        if (restorePendingNewTabFromInternalUrl(tab, url)) {
            return;
        }
        openInCurrentTab(url, false);
    }

    private boolean restorePendingNewTabFromInternalUrl(CuspTab tab, String url) {
        if (url == null || !url.startsWith(INTERNAL_URL_PREFIX + "newtab-history/")) {
            return false;
        }
        List<String> pages = decodeNewTabHistoryPages(url);
        int restoredIndex = decodeNewTabHistoryIndex(url, pages);
        int removeIndex = tabs.indexOf(tab);
        if (removeIndex >= 0) {
            tabs.remove(removeIndex);
            if (currentIndex > removeIndex) {
                currentIndex--;
            } else if (currentIndex >= tabs.size()) {
                currentIndex = tabs.size() - 1;
            }
        }
        pendingNewTab = true;
        pendingPrivateNewTab = isPrivateTab(tab);
        tabOverviewVisible = false;
        newTabNavigationHistory.clear();
        newTabNavigationHistory.addAll(pages);
        newTabNavigationIndex = Math.max(0, Math.min(restoredIndex, newTabNavigationHistory.size() - 1));
        String currentPage = newTabNavigationHistory.get(newTabNavigationIndex);
        pendingHistoryAll = "history".equals(currentPage);
        contentFrame.removeAllViews();
        renderNewTabPage(currentPage, false);
        requestSaveTabsSoon();
        return true;
    }

    private List<String> decodeNewTabHistoryPages(String url) {
        List<String> pages = new ArrayList<>();
        try {
            String json = decodeNewTabToken(url.substring((INTERNAL_URL_PREFIX + "newtab-history/").length()));
            JSONObject root = new JSONObject(json);
            JSONArray array = root.optJSONArray("pages");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    String page = array.optString(i, "");
                    if (!page.isEmpty()) {
                        pages.add(page);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        if (pages.isEmpty()) {
            pages.add("home");
        } else if (!"home".equals(pages.get(0))) {
            pages.add(0, "home");
        }
        return pages;
    }

    private int decodeNewTabHistoryIndex(String url, List<String> pages) {
        try {
            String json = decodeNewTabToken(url.substring((INTERNAL_URL_PREFIX + "newtab-history/").length()));
            JSONObject root = new JSONObject(json);
            return Math.max(0, Math.min(root.optInt("index", pages.size() - 1), pages.size() - 1));
        } catch (Exception ignored) {
            return Math.max(0, pages.size() - 1);
        }
    }

    private boolean canGoBackInCurrentTab(CuspTab tab) {
        return tab != null
                && (tab.navigationIndex > 0 || tab.backToNewTab || tab.returnToIndex >= 0);
    }

    private boolean canGoForwardInCurrentTab(CuspTab tab) {
        return tab != null
                && tab.navigationIndex >= 0
                && tab.navigationIndex < tab.navigationHistory.size() - 1;
    }

    private boolean canGoForwardInNewTab() {
        return pendingNewTab
                && newTabNavigationIndex >= 0
                && newTabNavigationIndex < newTabNavigationHistory.size() - 1;
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
        reloadAllTabs(false);
    }

    private void reloadAllTabs(boolean centerSpinner) {
        boolean wasOverview = tabOverviewVisible;
        List<CuspTab> targets = new ArrayList<>();
        for (CuspTab tab : new ArrayList<>(tabs)) {
            if (tab == null || tab.url == null || tab.url.isEmpty()
                    || !tab.readerMode || !NATIVE_THREAD.equals(tab.nativeKind)
                    || (wasOverview && tab.privateBrowsing != tabOverviewPrivateMode)) {
                continue;
            }
            targets.add(tab);
        }
        List<SavedItem> bookmarkTargets = wasOverview && !tabOverviewPrivateMode && showBookmarksInTabOverview()
                ? readSavedItems(PREF_THREAD_BOOKMARKS)
                : new ArrayList<>();
        int totalTargets = targets.size() + bookmarkTargets.size();
        if (wasOverview && centerSpinner && totalTargets > 0) {
            showCenterSpinner();
        }
        AtomicInteger remaining = new AtomicInteger(totalTargets);
        Runnable done = () -> {
            if (remaining.decrementAndGet() <= 0) {
                if (wasOverview && centerSpinner) {
                    hideCenterSpinner();
                }
                trimBackgroundTabViews();
                trimBackgroundPageData();
                requestSaveTabsSoon();
                if (wasOverview) {
                    if (tabOverviewVisible) {
                        refreshTabOverviewListOnly();
                        renderTabs();
                    }
                }
            }
        };
        for (CuspTab tab : targets) {
            reloadThreadTabForOverview(tab, done);
        }
        for (SavedItem bookmark : bookmarkTargets) {
            reloadBookmarkForOverview(bookmark, done);
        }
        if (wasOverview && totalTargets == 0) {
            tabOverviewVisible = true;
        }
    }

    private void reloadThreadTabForOverview(CuspTab tab, Runnable onComplete) {
        if (tab == null || tab.url == null || tab.url.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        final String url = tab.url;
        tabReloadExecutor.execute(() -> {
            ThreadOverviewStatus status = null;
            try {
                status = downloadThreadOverviewStatus(url);
            } catch (Exception ignored) {
            }
            ThreadOverviewStatus result = status;
            mainHandler.post(() -> {
                if (result != null && url.equals(tab.url)) {
                    applyOverviewReloadStatus(tab, result);
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }

    private void reloadBookmarkForOverview(SavedItem bookmark, Runnable onComplete) {
        if (bookmark == null || bookmark.url == null || bookmark.url.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        final String url = bookmark.url;
        tabReloadExecutor.execute(() -> {
            ThreadOverviewStatus status = null;
            try {
                status = downloadThreadOverviewStatus(url);
            } catch (Exception ignored) {
            }
            ThreadOverviewStatus result = status;
            mainHandler.post(() -> {
                if (result != null) {
                    saveBookmarkOverviewStatus(url, result);
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }

    private ThreadOverviewStatus downloadThreadOverviewStatus(String url) throws Exception {
        DatAddress address = datAddress(url);
        if (address == null) {
            HttpURLConnection canonical = openConnectionFollowingRedirects(
                    url,
                    "Mozilla/5.0 (Linux; Android) CuspiDroid/0.1");
            try {
                address = datAddress(canonical.getURL().toString());
            } finally {
                canonical.disconnect();
            }
        }
        if (address == null) {
            throw new IllegalStateException("Unsupported thread URL.");
        }
        Exception lastError = null;
        for (String host : threadSubjectHosts(address)) {
            try {
                String scheme = address.scheme == null || address.scheme.isEmpty() ? "https" : address.scheme;
                String boardUrl = scheme + "://" + host + "/" + address.board + "/";
                BoardSubject subject = downloadBoardSubject(boardUrl, host, address.board);
                return threadOverviewStatusFromSubject(url, address.key, subject.body);
            } catch (Exception error) {
                lastError = error;
            }
        }
        throw lastError == null ? new IllegalStateException("subject.txt not found.") : lastError;
    }

    private List<String> threadSubjectHosts(DatAddress address) {
        List<String> hosts = new ArrayList<>();
        if (address == null) {
            return hosts;
        }
        addUnique(hosts, address.host);
        if (address.server != null && !address.server.trim().isEmpty()) {
            addUnique(hosts, address.server + ".5ch.net");
            addUnique(hosts, address.server + ".5ch.io");
        }
        return hosts;
    }

    private ThreadOverviewStatus threadOverviewStatusFromSubject(String url, String key, String body) {
        ThreadOverviewStatus status = new ThreadOverviewStatus();
        status.url = url;
        for (String line : (body == null ? "" : body).split("\\r?\\n")) {
            int sep = line.indexOf("<>");
            int sepLength = 2;
            if (sep <= 0) {
                sep = line.indexOf(",");
                sepLength = 1;
            }
            if (sep <= 0) {
                continue;
            }
            String dat = line.substring(0, sep);
            if (!dat.endsWith(".dat") && !dat.endsWith(".cgi")) {
                continue;
            }
            String lineKey = dat.substring(0, dat.length() - 4);
            if (!key.equals(lineKey)) {
                continue;
            }
            String subjectTitle = cleanText(line.substring(sep + sepLength));
            status.title = stripThreadResponseCount(subjectTitle);
            status.responseCount = threadResponseCount(subjectTitle);
            status.archived = false;
            return status;
        }
        status.archived = true;
        return status;
    }

    private void applyOverviewReloadStatus(CuspTab tab, ThreadOverviewStatus status) {
        if (tab == null || status == null) {
            return;
        }
        boolean archived = status.archived || tab.knownThreadArchived
                || (tab.threadPage != null && tab.threadPage.archived);
        String title = status.title == null || status.title.trim().isEmpty()
                ? (tab.threadPage != null && tab.threadPage.title != null && !tab.threadPage.title.trim().isEmpty()
                ? tab.threadPage.title : tab.title)
                : status.title.trim();
        if (title == null || title.trim().isEmpty()) {
            title = hostTitle(status.url);
        }
        tab.title = title;
        tab.knownThreadArchived = archived;
        tab.readPostNumber = Math.max(tab.readPostNumber, readPostNumberForTab(tab, status.url));
        if (tab.threadPage != null) {
            tab.threadPage.title = title;
            tab.threadPage.archived = archived;
            updateThreadTitleHeader(tab, tab.threadPage);
        }
        if (status.responseCount > 0) {
            tab.knownMaxPostNumber = status.responseCount;
            tab.knownPostCount = status.responseCount;
            tab.cachedUnreadCount = Math.max(0, status.responseCount - tab.readPostNumber);
            tab.hasThreadStats = true;
        } else if (tab.threadPage != null) {
            updateTabThreadStats(tab, tab.threadPage);
        } else if (tab.hasThreadStats) {
            tab.cachedUnreadCount = Math.max(0, tab.knownMaxPostNumber - tab.readPostNumber);
        }
    }

    private void saveBookmarkOverviewStatus(String url, ThreadOverviewStatus status) {
        if (url == null || status == null) {
            return;
        }
        try {
            JSONObject root = new JSONObject(preferences.getString(PREF_BOOKMARK_OVERVIEW_STATUS, "{}"));
            JSONObject item = new JSONObject();
            item.put("title", status.title == null ? "" : status.title);
            item.put("responseCount", status.responseCount);
            item.put("archived", status.archived);
            item.put("updatedAt", System.currentTimeMillis());
            root.put(bookmarkOverviewStatusKey(url), item);
            preferences.edit().putString(PREF_BOOKMARK_OVERVIEW_STATUS, root.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private BookmarkOverviewStatus bookmarkOverviewStatus(String url) {
        try {
            JSONObject root = new JSONObject(preferences.getString(PREF_BOOKMARK_OVERVIEW_STATUS, "{}"));
            JSONObject item = root.optJSONObject(bookmarkOverviewStatusKey(url));
            if (item == null) {
                item = root.optJSONObject(url);
            }
            if (item == null) {
                return null;
            }
            BookmarkOverviewStatus status = new BookmarkOverviewStatus();
            status.title = item.optString("title", "");
            status.responseCount = item.optInt("responseCount", 0);
            status.archived = item.optBoolean("archived", false);
            return status;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String bookmarkOverviewStatusKey(String url) {
        return trimSlash(normalizeUrl(url));
    }

    private int bookmarkOverviewUnread(SavedItem item) {
        if (item == null || item.url == null) {
            return 0;
        }
        BookmarkOverviewStatus status = bookmarkOverviewStatus(item.url);
        if (status == null || status.responseCount <= 0) {
            return 0;
        }
        return Math.max(0, status.responseCount - readPostNumber(preferences, item.url));
    }

    private int bookmarkOverviewUnreadSum(List<SavedItem> bookmarks, String folder) {
        int total = 0;
        String normalizedFolder = folder == null ? null : normalizeSavedFolder(folder);
        for (SavedItem bookmark : bookmarks) {
            if (normalizedFolder != null && !normalizedFolder.equals(normalizeSavedFolder(bookmark.folder))) {
                continue;
            }
            total += bookmarkOverviewUnread(bookmark);
        }
        return total;
    }

    private void closeCurrentTab() {
        if (tabs.isEmpty()) {
            return;
        }
        closeTab(currentIndex);
    }

    private void switchRelativeTab(int delta) {
        if (tabs.isEmpty() || pendingNewTab) {
            return;
        }
        int target = currentIndex + delta;
        if (target < 0 || target >= tabs.size()) {
            return;
        }
        switchToTab(target);
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

    private boolean currentTabIsPrivate() {
        return pendingNewTab ? pendingPrivateNewTab : isPrivateTab(currentTab());
    }

    private boolean privateUiActive() {
        return tabOverviewVisible ? tabOverviewPrivateMode : currentTabIsPrivate();
    }

    private boolean isPrivateTab(CuspTab tab) {
        return tab != null && tab.privateBrowsing;
    }

    private String download(String urlText) throws Exception {
        HttpURLConnection connection = openConnectionFollowingRedirects(urlText, "Mozilla/5.0 (Linux; Android) CuspiDroid/0.1");
        int code = connection.getResponseCode();
        InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (stream == null) {
            throw new IllegalStateException("HTTP " + code);
        }
        byte[] bytes = readBytes(stream);
        Charset charset = responseCharset(connection, Charset.forName("UTF-8"));
        String body = new String(bytes, charset);
        Charset metaCharset = htmlMetaCharset(body);
        if (metaCharset != null && !metaCharset.equals(charset)) {
            body = new String(bytes, metaCharset);
        }
        if (code >= 400) {
            throw new IllegalStateException("HTTP " + code + "\n" + stripTags(body));
        }
        return body;
    }

    private Charset responseCharset(HttpURLConnection connection, Charset fallback) {
        Charset charset = fallback;
        String contentType = connection.getContentType();
        if (contentType != null) {
            Matcher matcher = Pattern.compile("charset=([^;]+)", Pattern.CASE_INSENSITIVE).matcher(contentType);
            if (matcher.find()) {
                try {
                    charset = Charset.forName(matcher.group(1).trim());
                } catch (Exception ignored) {
                }
            }
        }
        return charset;
    }

    private Charset htmlMetaCharset(String body) {
        if (body == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("<meta[^>]+charset=[\"']?([^\"'\\s>/;]+)", Pattern.CASE_INSENSITIVE)
                .matcher(body);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Charset.forName(matcher.group(1).trim());
        } catch (Exception error) {
            return null;
        }
    }

    private HttpURLConnection openConnectionFollowingRedirects(String urlText, String userAgent) throws Exception {
        return openConnectionFollowingRedirects(urlText, userAgent, null);
    }

    private HttpURLConnection openConnectionFollowingRedirects(String urlText, String userAgent, Map<String, String> headers) throws Exception {
        String current = urlText;
        for (int i = 0; i < 8; i++) {
            HttpURLConnection connection = (HttpURLConnection) new URL(current).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(16000);
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setRequestProperty("Accept", "*/*");
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
            }
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
                DatDownload download = downloadDatBytes(candidate, 0);
                ThreadPage page = parseDatThread(threadUrl, download.body);
                page.datUrl = download.url;
                page.datByteLength = download.totalByteLength;
                page.archived = isArchiveDatUrl(download.url);
                return page;
            } catch (Exception error) {
            }
        }
        return null;
    }

    private ThreadPage downloadNewDatPosts(CuspTab tab) throws Exception {
        if (tab == null || tab.threadPage == null || tab.threadPage.posts.isEmpty()
                || tab.threadPage.datUrl == null || tab.threadPage.datUrl.isEmpty()
                || tab.threadPage.datByteLength <= 0) {
            return null;
        }
        return downloadNewDatPosts(tab.url, tab.threadPage);
    }

    private ThreadPage downloadNewDatPosts(String threadUrl, ThreadPage existing) throws Exception {
        if (existing == null || existing.posts == null || existing.posts.isEmpty()
                || existing.datUrl == null || existing.datUrl.isEmpty()
                || existing.datByteLength <= 0) {
            return null;
        }
        DatDownload download = downloadDatBytes(existing.datUrl, existing.datByteLength);
        if (!download.partial || download.body.trim().isEmpty()) {
            return null;
        }
        ThreadPage additional = parseDatThread(threadUrl, download.body,
                existing.posts.get(existing.posts.size() - 1).number + 1);
        if (additional.posts.isEmpty()) {
            return null;
        }
        ThreadPage merged = cloneThreadPage(existing);
        merged.datUrl = download.url;
        merged.datByteLength = download.totalByteLength;
        merged.archived = existing.archived || isArchiveDatUrl(download.url);
        for (Post post : additional.posts) {
            merged.posts.add(post);
            merged.postsByNumber.put(post.number, post);
        }
        merged.newPostCount = additional.posts.size();
        return merged;
    }

    private DatDownload downloadDatBytes(String url, long rangeStart) throws Exception {
        Map<String, String> headers = new LinkedHashMap<>();
        if (rangeStart > 0) {
            headers.put("Range", "bytes=" + rangeStart + "-");
        }
        HttpURLConnection connection = openConnectionFollowingRedirects(
                url,
                "Monazilla/1.00 CuspiDroid/0.1",
                headers);
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
        boolean partial = rangeStart > 0 && code == HttpURLConnection.HTTP_PARTIAL;
        long totalLength = partial ? totalLengthFromContentRange(connection.getHeaderField("Content-Range")) : bytes.length;
        if (totalLength <= 0) {
            totalLength = rangeStart + bytes.length;
        }
        return new DatDownload(connection.getURL().toString(), body, totalLength, partial);
    }

    private long totalLengthFromContentRange(String value) {
        if (value == null) {
            return 0;
        }
        Matcher matcher = Pattern.compile("/(\\d+)\\s*$").matcher(value);
        if (!matcher.find()) {
            return 0;
        }
        try {
            return Long.parseLong(matcher.group(1));
        } catch (Exception error) {
            return 0;
        }
    }

    private boolean isArchiveDatUrl(String url) {
        String lower = url == null ? "" : url.toLowerCase(Locale.ROOT);
        return lower.contains("/kako/") || lower.contains("/oyster/");
    }

    private ThreadPage cloneThreadPage(ThreadPage source) {
        ThreadPage copy = new ThreadPage();
        copy.url = source.url;
        copy.title = source.title;
        copy.error = source.error;
        copy.archived = source.archived;
        copy.datUrl = source.datUrl;
        copy.datByteLength = source.datByteLength;
        for (Post post : source.posts) {
            copy.posts.add(post);
            copy.postsByNumber.put(post.number, post);
        }
        return copy;
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
        if (testIndex < 0) {
            testIndex = parts.indexOf("bbs");
        }
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
        return parseDatThread(threadUrl, dat, 1);
    }

    private ThreadPage parseDatThread(String threadUrl, String dat, int firstNumber) {
        ThreadPage page = new ThreadPage();
        page.url = threadUrl;
        page.title = hostTitle(threadUrl);
        String[] lines = dat.split("\\r?\\n");
        int number = Math.max(1, firstNumber);
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
        BoardSubject subject = downloadBoardSubject(boardUrl, host, board);
        String body = subject.body;

        SearchPage page = new SearchPage();
        page.url = boardUrl;
        page.title = boardTitle(boardUrl);
        for (String line : body.split("\\r?\\n")) {
            int sep = line.indexOf("<>");
            int sepLength = 2;
            if (sep <= 0) {
                sep = line.indexOf(",");
                sepLength = 1;
            }
            if (sep <= 0) {
                continue;
            }
            String dat = line.substring(0, sep);
            String title = cleanText(line.substring(sep + sepLength));
            if (!dat.endsWith(".dat") && !dat.endsWith(".cgi")) {
                continue;
            }
            String key = dat.substring(0, dat.length() - 4);
            int responses = threadResponseCount(title);
            SearchResult result = new SearchResult();
            result.title = title;
            result.url = subject.threadBase + key + "/";
            result.responses = responses;
            result.velocity = threadVelocity(key, responses);
            result.priorityMatch = matchingBoardPriorityWord(title);
            result.meta = boardThreadMeta(board, responses, result.velocity);
            page.results.add(result);
        }
        sortBoardResults(page.results);
        return page;
    }

    private SearchPage downloadBbsDirectory(String directoryUrl) throws Exception {
        String html = download(directoryUrl);
        Uri base = Uri.parse(normalizeUrl(directoryUrl));
        String baseHost = base.getHost();
        SearchPage page = new SearchPage();
        page.url = directoryUrl;
        page.title = hostTitle(directoryUrl);
        Set<String> seen = new LinkedHashSet<>();
        Pattern anchorPattern = Pattern.compile(
                "<a\\s+[^>]*href\\s*=\\s*(?:\"([^\"]+)\"|'([^']+)'|([^\\s>]+))[^>]*>(.*?)</a>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = anchorPattern.matcher(html);
        int lastEnd = 0;
        String currentCategory = "";
        while (matcher.find()) {
            currentCategory = lastBbsMenuCategory(html.substring(lastEnd, matcher.start()), currentCategory);
            lastEnd = matcher.end();
            String href = firstNonEmpty(matcher.group(1), matcher.group(2), matcher.group(3));
            String label = cleanText(matcher.group(4));
            if (href == null || href.trim().isEmpty()) {
                continue;
            }
            String absolute = absoluteUrl(directoryUrl, href);
            Uri target = Uri.parse(absolute);
            String host = target.getHost();
            if (!isSameDirectoryFamily(directoryUrl, absolute, baseHost, host)) {
                continue;
            }
            if (!isDirectoryBoardLink(absolute)) {
                continue;
            }
            String board = boardNameFromUrl(absolute);
            if (board == null || board.trim().isEmpty()) {
                continue;
            }
            String boardUrl = boardUrlFromDirectoryLink(absolute, board);
            if (!seen.add(boardUrl)) {
                continue;
            }
            SearchResult result = new SearchResult();
            result.title = label == null || label.isEmpty() ? board : label;
            result.url = boardUrl;
            result.meta = host;
            result.category = currentCategory;
            page.results.add(result);
        }
        if (page.results.isEmpty()) {
            throw new IllegalStateException(text("\u677f\u30ea\u30f3\u30af\u304c\u898b\u3064\u304b\u308a\u307e\u305b\u3093", "No board links found."));
        }
        return page;
    }

    private String lastBbsMenuCategory(String htmlFragment, String fallback) {
        if (htmlFragment == null || htmlFragment.isEmpty()) {
            return fallback == null ? "" : fallback;
        }
        String category = fallback == null ? "" : fallback;
        Pattern pattern = Pattern.compile(
                "<(?:b|strong)\\b[^>]*>(.*?)</(?:b|strong)>|<h[1-6]\\b[^>]*>(.*?)</h[1-6]>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(htmlFragment);
        while (matcher.find()) {
            String value = cleanText(firstNonEmpty(matcher.group(1), matcher.group(2)));
            if (looksLikeBbsCategory(value)) {
                category = value;
            }
        }
        return category;
    }

    private boolean looksLikeBbsCategory(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return !trimmed.isEmpty() && trimmed.length() <= 40 && !trimmed.contains("http");
    }

    private boolean isSameDirectoryFamily(String directoryUrl, String targetUrl, String baseHost, String targetHost) {
        if (baseHost == null || targetHost == null) {
            return false;
        }
        if (targetHost.equalsIgnoreCase(baseHost)) {
            return true;
        }
        if (isBbsMenuUrl(directoryUrl) && is5chUrl(directoryUrl) && is5chUrl(targetUrl)) {
            return true;
        }
        if (isBbsMenuUrl(directoryUrl) && isSameBbsHostFamily(baseHost, targetHost)) {
            return true;
        }
        return false;
    }

    private boolean isDirectoryBoardLink(String url) {
        try {
            Uri uri = Uri.parse(normalizeUrl(url));
            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                return false;
            }
            String[] parts = path.split("/");
            List<String> nonEmpty = new ArrayList<>();
            for (String part : parts) {
                if (part != null && !part.isEmpty()) {
                    nonEmpty.add(part);
                }
            }
            if (nonEmpty.isEmpty()) {
                return false;
            }
            String first = nonEmpty.get(0).toLowerCase(Locale.ROOT);
            if ("cdn-cgi".equals(first) || "image".equals(first) || "sp".equals(first)) {
                return false;
            }
            if ("bbs".equals(first) && nonEmpty.size() >= 3
                    && "read.cgi".equalsIgnoreCase(nonEmpty.get(1))) {
                return true;
            }
            if (isRegisteredBbsMenuChild(url, nonEmpty)) {
                return true;
            }
            if (nonEmpty.size() == 1) {
                return !nonEmpty.get(0).contains(".");
            }
            return false;
        } catch (Exception error) {
            return false;
        }
    }

    private boolean isRegisteredBbsMenuChild(String url, List<String> pathParts) {
        if (url == null || pathParts == null || pathParts.size() < 2) {
            return false;
        }
        try {
            Uri target = Uri.parse(normalizeUrl(url));
            String targetHost = target.getHost();
            if (targetHost == null) {
                return false;
            }
            for (BbsLink link : readBbsLinks(preferences)) {
                Uri base = Uri.parse(normalizeUrl(link.url));
                String baseHost = base.getHost();
                if (baseHost == null || !isSameBbsHostFamily(baseHost, targetHost) || !isBbsMenuUrl(link.url)) {
                    continue;
                }
                List<String> baseParts = pathParts(base.getPath());
                if (baseParts.isEmpty()) {
                    continue;
                }
                String menuFile = baseParts.get(baseParts.size() - 1).toLowerCase(Locale.ROOT);
                if (!menuFile.endsWith(".html") && !menuFile.endsWith(".htm")) {
                    continue;
                }
                List<String> prefix = baseParts.subList(0, baseParts.size() - 1);
                if (pathParts.size() == prefix.size() + 1 && pathStartsWith(pathParts, prefix)
                        && !pathParts.get(pathParts.size() - 1).contains(".")) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private String boardUrlFromDirectoryLink(String url, String board) {
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme() == null ? "https" : uri.getScheme();
        String host = uri.getHost();
        if (host == null) {
            return url;
        }
        return scheme + "://" + host + "/" + board + "/";
    }

    private String absoluteUrl(String baseUrl, String href) {
        try {
            return new URL(new URL(normalizeUrl(baseUrl)), href).toString();
        } catch (Exception error) {
            return normalizeUrl(href);
        }
    }

    private BoardSubject downloadBoardSubject(String boardUrl, String host, String board) throws Exception {
        Exception lastError = null;
        for (String subjectUrl : boardSubjectCandidates(boardUrl, host, board)) {
            HttpURLConnection connection = null;
            try {
                connection = openConnectionFollowingRedirects(
                        subjectUrl,
                        "Monazilla/1.00 CuspiDroid/0.1");
                int code = connection.getResponseCode();
                InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
                if (stream == null) {
                    throw new IllegalStateException("HTTP " + code);
                }
                String body = readText(stream, responseCharset(connection, Charset.forName("MS932")));
                if (code >= 400) {
                    throw new IllegalStateException("HTTP " + code + "\n" + cleanText(body));
                }
                return new BoardSubject(body, threadBaseFromSubjectUrl(subjectUrl, board));
            } catch (Exception error) {
                lastError = error;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        throw lastError == null ? new IllegalStateException("subject.txt not found.") : lastError;
    }

    private List<String> boardSubjectCandidates(String boardUrl, String host, String board) {
        List<String> urls = new ArrayList<>();
        String normalized = normalizeUrl(boardUrl);
        addUnique(urls, trimSlash(normalized) + "/subject.txt");
        String scheme = Uri.parse(normalized).getScheme();
        if (scheme == null || scheme.isEmpty()) {
            scheme = "https";
        }
        if (host.toLowerCase(Locale.ROOT).endsWith("machi.to")) {
            addUnique(urls, scheme + "://" + host + "/bbs/" + board + "/subject.txt");
            addUnique(urls, "https://" + host + "/bbs/" + board + "/subject.txt");
            addUnique(urls, "http://" + host + "/bbs/" + board + "/subject.txt");
        }
        addUnique(urls, scheme + "://" + host + "/" + board + "/subject.txt");
        addUnique(urls, "https://" + host + "/" + board + "/subject.txt");
        addUnique(urls, "http://" + host + "/" + board + "/subject.txt");
        for (BbsLink link : readBbsLinks(preferences)) {
            try {
                Uri base = Uri.parse(normalizeUrl(link.url));
                if (base.getHost() != null && base.getHost().equalsIgnoreCase(host)) {
                    addUnique(urls, trimSlash(normalizeUrl(link.url)) + "/subject.txt");
                }
            } catch (Exception ignored) {
            }
        }
        return urls;
    }

    private void addUnique(List<String> values, String value) {
        if (value != null && !values.contains(value)) {
            values.add(value);
        }
    }

    private String trimSlash(String value) {
        String result = value == null ? "" : value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private String threadBaseFromSubjectUrl(String subjectUrl, String board) {
        Uri uri = Uri.parse(subjectUrl);
        String scheme = uri.getScheme() == null ? "https" : uri.getScheme();
        String host = uri.getHost();
        if (host == null) {
            return "";
        }
        String reader = host.toLowerCase(Locale.ROOT).endsWith("machi.to")
                ? "/bbs/read.cgi/"
                : "/test/read.cgi/";
        return scheme + "://" + host + reader + board + "/";
    }

    private int threadResponseCount(String title) {
        Matcher matcher = Pattern.compile("\\((\\d{1,6})\\)\\s*$").matcher(title == null ? "" : title);
        return matcher.find() ? parsePositiveInt(matcher.group(1), 0) : 0;
    }

    private String stripThreadResponseCount(String title) {
        return Pattern.compile("\\s*\\(\\d{1,6}\\)\\s*$")
                .matcher(title == null ? "" : title)
                .replaceFirst("")
                .trim();
    }

    private double threadVelocity(String key, int responses) {
        int created = parsePositiveInt(key, 0);
        if (created <= 0 || responses <= 0) {
            return 0d;
        }
        double days = Math.max(1d / 24d, (System.currentTimeMillis() / 1000d - created) / 86400d);
        return responses / days;
    }

    private void sortBoardResults(List<SearchResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }
        final boolean sortBySpeed = preferences.getBoolean(PREF_BOARD_SORT_BY_SPEED, true);
        for (int i = 0; i < results.size(); i++) {
            results.get(i).boardOrder = i;
        }
        Collections.sort(results, (left, right) -> {
            int leftPriority = left.priorityMatch == null ? 0 : 1;
            int rightPriority = right.priorityMatch == null ? 0 : 1;
            if (leftPriority != rightPriority) {
                return Integer.compare(rightPriority, leftPriority);
            }
            if (sortBySpeed) {
                int velocity = Double.compare(right.velocity, left.velocity);
                if (velocity != 0) {
                    return velocity;
                }
            }
            return Integer.compare(left.boardOrder, right.boardOrder);
        });
    }

    private BoardPriorityMatch matchingBoardPriorityWord(String title) {
        if (title == null || title.isEmpty()) {
            return null;
        }
        String lowerTitle = title.toLowerCase(Locale.ROOT);
        for (BoardPriorityRule rule : readBoardPriorityRules(preferences)) {
            String value = rule.value == null ? "" : rule.value.trim();
            if (value.isEmpty()) {
                continue;
            }
            if (rule.regex) {
                try {
                    Matcher matcher = Pattern.compile(value, Pattern.CASE_INSENSITIVE).matcher(title);
                    if (matcher.find()) {
                        return new BoardPriorityMatch(value, true);
                    }
                } catch (Exception ignored) {
                }
            } else if (lowerTitle.contains(value.toLowerCase(Locale.ROOT))) {
                return new BoardPriorityMatch(value, false);
            }
        }
        return null;
    }

    private String boardThreadMeta(String board, int responses, double velocity) {
        String count = responses > 0
                ? text("\u30ec\u30b9: ", "Posts: ") + responses
                : text("\u30ec\u30b9: -", "Posts: -");
        String speed = velocity > 0
                ? text("\u52e2\u3044: ", "Speed: ") + String.format(Locale.ROOT, "%.1f", velocity)
                : text("\u52e2\u3044: -", "Speed: -");
        return board + "  " + count + "  " + speed;
    }

    private ThreadPage parseThread(String url, String html) {
        ThreadPage page = new ThreadPage();
        page.url = url;
        page.title = firstMatch(html, "<title[^>]*>(.*?)</title>");
        if (page.title == null || page.title.trim().isEmpty()) {
            page.title = hostTitle(url);
        }
        page.title = cleanText(page.title);

        parseMachiPosts(html, page.posts);
        if (page.posts.isEmpty()) {
            parseKakoPosts(html, page.posts);
        }
        if (page.posts.isEmpty()) {
            parseModernPosts(html, page.posts);
        }
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

    private void parseKakoPosts(String html, List<Post> posts) {
        Pattern pattern = Pattern.compile(
                "<div\\s+id=[\"'](\\d+)[\"'][^>]+class=[\"'][^\"']*(?<![A-Za-z0-9_-])post(?![A-Za-z0-9_-])[^\"']*[\"'][^>]*>(.*?)(?=<div\\s+id=[\"']\\d+[\"'][^>]+class=[\"'][^\"']*(?<![A-Za-z0-9_-])post(?![A-Za-z0-9_-])|<div[^>]+class=[\"'][^\"']*ads_container|<div[^>]+class=[\"'][^\"']*navmenu|</div>\\s*<footer|</body>)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String block = matcher.group(2);
            String body = firstMatch(block,
                    "<section[^>]+class=[\"'][^\"']*post-content[^\"']*[\"'][^>]*>(.*?)</section>");
            if (body == null) {
                continue;
            }
            Post post = new Post();
            post.number = parsePositiveInt(matcher.group(1), posts.size() + 1);
            post.name = valueOr(firstMatch(block,
                    "<span[^>]+class=[\"'][^\"']*postusername[^\"']*[\"'][^>]*>\\s*<b[^>]*>(.*?)</b>"), "anonymous");
            post.date = valueOr(firstMatch(block,
                    "<span[^>]+class=[\"'][^\"']*date[^\"']*[\"'][^>]*>(.*?)</span>"), "");
            String id = firstMatch(block,
                    "<span[^>]+class=[\"'][^\"']*uid[^\"']*[\"'][^>]*>(.*?)</span>");
            if (id != null && !cleanText(id).isEmpty()) {
                String plainId = cleanText(id);
                post.date = post.date == null || post.date.trim().isEmpty()
                        ? plainId
                        : post.date + " " + plainId;
            }
            post.body = cleanText(body);
            if (post.body.isEmpty()) {
                continue;
            }
            posts.add(post);
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

    private void parseMachiPosts(String html, List<Post> posts) {
        Pattern pattern = Pattern.compile(
                "<div[^>]+class=[\"'][^\"']*(?<![A-Za-z0-9_-])res(?![A-Za-z0-9_-])[^\"']*[\"'][^>]*>(.*?)</div>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String block = matcher.group(1);
            Matcher separator = Pattern.compile("<hr[^>]+class=[\"'][^\"']*reshr[^\"']*[\"'][^>]*>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(block);
            if (!separator.find()) {
                continue;
            }
            String meta = block.substring(0, separator.start());
            String body = block.substring(separator.end());
            Post post = new Post();
            post.number = parsePositiveInt(valueOr(firstMatch(meta, "^\\s*(\\d+)\\s*:"), String.valueOf(posts.size() + 1)),
                    posts.size() + 1);
            post.name = valueOr(firstMatch(meta, "<b[^>]*>(.*?)</b>"), "anonymous");
            post.date = valueOr(firstMatch(meta, "<font[^>]+size=[\"']?1[\"']?[^>]*>(.*?)</font>"), "");
            post.body = cleanText(body);
            if (post.body.isEmpty()) {
                continue;
            }
            posts.add(post);
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

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
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

    private void addThreadHistory(CuspTab tab, String url, String title) {
        if (isPrivateTab(tab) || historyDisabled()) {
            return;
        }
        addThreadHistory(url, title);
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

    private List<SavedItem> readSavedItems(String key) {
        List<SavedItem> items = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString(key, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                String title = object.optString("title", "").trim();
                String url = object.optString("url", "").trim();
                String folder = normalizeSavedFolder(object.optString("folder", ""));
                if (!title.isEmpty() && !url.isEmpty()) {
                    items.add(new SavedItem(title, url, folder));
                }
            }
        } catch (Exception ignored) {
        }
        return items;
    }

    private void migrateFavoriteBoardsToBookmarks() {
        if (preferences == null || preferences.getBoolean("favorite_boards_migrated_to_bookmarks", false)) {
            return;
        }
        List<SavedItem> bookmarks = readSavedItems(PREF_THREAD_BOOKMARKS);
        boolean changed = false;
        for (SavedItem favorite : readSavedItems(PREF_BOARD_FAVORITES)) {
            boolean exists = false;
            for (SavedItem bookmark : bookmarks) {
                if (sameSavedUrl(bookmark.url, favorite.url)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                bookmarks.add(new SavedItem(favorite.title, favorite.url, favorite.folder));
                changed = true;
            }
        }
        SharedPreferences.Editor editor = preferences.edit()
                .putBoolean("favorite_boards_migrated_to_bookmarks", true)
                .putString(PREF_BOARD_FAVORITES, "[]");
        if (changed) {
            JSONArray array = new JSONArray();
            try {
                for (SavedItem item : bookmarks) {
                    JSONObject object = new JSONObject();
                    object.put("title", item.title);
                    object.put("url", item.url);
                    object.put("folder", normalizeSavedFolder(item.folder));
                    array.put(object);
                }
            } catch (Exception ignored) {
            }
            editor.putString(PREF_THREAD_BOOKMARKS, array.toString());
        }
        editor.apply();
    }

    private void writeSavedItems(String key, List<SavedItem> items) {
        JSONArray array = new JSONArray();
        try {
            for (SavedItem item : items) {
                JSONObject object = new JSONObject();
                object.put("title", item.title);
                object.put("url", item.url);
                object.put("folder", normalizeSavedFolder(item.folder));
                array.put(object);
            }
        } catch (Exception ignored) {
        }
        preferences.edit().putString(key, array.toString()).apply();
    }

    private List<String> readSavedFolders(String key) {
        List<String> folders = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString(savedFoldersKey(key), "[]"));
            for (int i = 0; i < array.length(); i++) {
                String folder = normalizeSavedFolder(array.optString(i, ""));
                if (!folder.isEmpty() && !folders.contains(folder)) {
                    folders.add(folder);
                }
            }
        } catch (Exception ignored) {
        }
        for (SavedItem item : readSavedItems(key)) {
            String folder = normalizeSavedFolder(item.folder);
            if (!folder.isEmpty() && !folders.contains(folder)) {
                folders.add(folder);
            }
        }
        return folders;
    }

    private void writeSavedFolders(String key, List<String> folders) {
        JSONArray array = new JSONArray();
        try {
            for (String folder : folders) {
                folder = normalizeSavedFolder(folder);
                if (!folder.isEmpty() && !containsString(array, folder)) {
                    array.put(folder);
                }
            }
        } catch (Exception ignored) {
        }
        preferences.edit().putString(savedFoldersKey(key), array.toString()).apply();
    }

    private boolean containsString(JSONArray array, String value) {
        for (int i = 0; i < array.length(); i++) {
            if (value.equals(array.optString(i, ""))) {
                return true;
            }
        }
        return false;
    }

    private String savedFoldersKey(String key) {
        return key + "_folders";
    }

    private String normalizeSavedFolder(String folder) {
        if (folder == null) {
            return "";
        }
        String value = folder.trim().replace('\\', '/');
        while (value.contains("//")) {
            value = value.replace("//", "/");
        }
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value.trim();
    }

    private String parentSavedFolder(String folder) {
        folder = normalizeSavedFolder(folder);
        int sep = folder.lastIndexOf('/');
        return sep < 0 ? "" : folder.substring(0, sep);
    }

    private String savedFolderDisplayName(String folder) {
        folder = normalizeSavedFolder(folder);
        int sep = folder.lastIndexOf('/');
        return sep < 0 ? folder : folder.substring(sep + 1);
    }

    private String topSavedFolder(String folder) {
        folder = normalizeSavedFolder(folder);
        int sep = folder.indexOf('/');
        return sep < 0 ? folder : folder.substring(0, sep);
    }

    private String childSavedFolderPath(String parent, String childName) {
        parent = normalizeSavedFolder(parent);
        childName = normalizeSavedFolder(childName);
        if (childName.isEmpty()) {
            return parent;
        }
        return parent.isEmpty() ? childName : parent + "/" + childName;
    }

    private boolean savedFolderDescendantOrSelf(String folder, String target) {
        folder = normalizeSavedFolder(folder);
        target = normalizeSavedFolder(target);
        return !folder.isEmpty() && (target.equals(folder) || target.startsWith(folder + "/"));
    }

    private List<String> childSavedFolders(String key, String parent) {
        parent = normalizeSavedFolder(parent);
        List<String> children = new ArrayList<>();
        for (String folder : readSavedFolders(key)) {
            if (parent.equals(parentSavedFolder(folder)) && !children.contains(folder)) {
                children.add(folder);
            }
        }
        return children;
    }

    private List<BookmarkNode> bookmarkChildren(String parent) {
        parent = normalizeSavedFolder(parent);
        List<BookmarkNode> nodes = new ArrayList<>();
        for (String folder : childSavedFolders(PREF_THREAD_BOOKMARKS, parent)) {
            nodes.add(BookmarkNode.folder(folder));
        }
        for (SavedItem item : readSavedItems(PREF_THREAD_BOOKMARKS)) {
            if (parent.equals(normalizeSavedFolder(item.folder))) {
                nodes.add(BookmarkNode.item(item));
            }
        }
        List<String> order = bookmarkOrder(parent);
        Collections.sort(nodes, (left, right) -> {
            int li = order.indexOf(left.orderKey());
            int ri = order.indexOf(right.orderKey());
            if (li < 0) {
                li = Integer.MAX_VALUE;
            }
            if (ri < 0) {
                ri = Integer.MAX_VALUE;
            }
            if (li != ri) {
                return Integer.compare(li, ri);
            }
            return left.label().compareToIgnoreCase(right.label());
        });
        return nodes;
    }

    private List<String> bookmarkOrder(String parent) {
        parent = normalizeSavedFolder(parent);
        List<String> order = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(preferences.getString(PREF_BOOKMARK_ORDER, "{}"));
            JSONArray array = root.optJSONArray(parent);
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    String value = array.optString(i, "");
                    if (!value.isEmpty() && !order.contains(value)) {
                        order.add(value);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return order;
    }

    private void writeBookmarkOrder(String parent, List<BookmarkNode> nodes) {
        parent = normalizeSavedFolder(parent);
        try {
            JSONObject root = new JSONObject(preferences.getString(PREF_BOOKMARK_ORDER, "{}"));
            JSONArray array = new JSONArray();
            for (BookmarkNode node : nodes) {
                array.put(node.orderKey());
            }
            root.put(parent, array);
            preferences.edit().putString(PREF_BOOKMARK_ORDER, root.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private void moveBookmarkNodeIntoFolder(String movingKey, String targetFolder) {
        targetFolder = normalizeSavedFolder(targetFolder);
        if (movingKey == null || movingKey.length() < 3) {
            return;
        }
        if (movingKey.startsWith("I:")) {
            String url = movingKey.substring(2);
            SavedItem item = savedItemByUrl(PREF_THREAD_BOOKMARKS, url);
            if (item == null) {
                return;
            }
            String oldParent = normalizeSavedFolder(item.folder);
            if (oldParent.equals(targetFolder)) {
                return;
            }
            moveSavedItemToFolder(PREF_THREAD_BOOKMARKS, url, targetFolder);
            appendBookmarkOrder(targetFolder, "I:" + url);
            removeBookmarkOrderEntry(oldParent, "I:" + url);
            return;
        }
        if (movingKey.startsWith("F:")) {
            String folder = normalizeSavedFolder(movingKey.substring(2));
            if (folder.isEmpty() || folder.equals(targetFolder) || savedFolderDescendantOrSelf(folder, targetFolder)) {
                return;
            }
            String oldParent = parentSavedFolder(folder);
            if (oldParent.equals(targetFolder)) {
                return;
            }
            String newFolder = childSavedFolderPath(targetFolder, savedFolderDisplayName(folder));
            renameSavedFolder(PREF_THREAD_BOOKMARKS, folder, newFolder);
            appendBookmarkOrder(targetFolder, "F:" + newFolder);
            removeBookmarkOrderEntry(oldParent, "F:" + folder);
        }
    }

    private void moveBookmarkNodeToParentNear(String movingKey, String targetParent, String targetKey, boolean after) {
        targetParent = normalizeSavedFolder(targetParent);
        if (movingKey == null || movingKey.isEmpty() || targetKey == null || targetKey.isEmpty()) {
            return;
        }
        String oldParent = bookmarkNodeParent(movingKey);
        if (!oldParent.equals(targetParent)) {
            moveBookmarkNodeIntoFolder(movingKey, targetParent);
            movingKey = movedBookmarkNodeKey(movingKey, targetParent);
            if (movingKey.isEmpty()) {
                return;
            }
        }
        List<BookmarkNode> nodes = bookmarkChildren(targetParent);
        BookmarkNode moved = null;
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (nodes.get(i).orderKey().equals(movingKey)) {
                moved = nodes.remove(i);
                break;
            }
        }
        if (moved == null) {
            return;
        }
        int target = -1;
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).orderKey().equals(targetKey)) {
                target = i;
                break;
            }
        }
        if (target < 0) {
            nodes.add(moved);
        } else {
            nodes.add(Math.max(0, Math.min(after ? target + 1 : target, nodes.size())), moved);
        }
        writeBookmarkOrder(targetParent, nodes);
    }

    private int bookmarkDropZone(android.view.DragEvent event, View target, boolean hasCenter) {
        if (event == null || target == null || target.getHeight() <= 0) {
            return 0;
        }
        float ratio = event.getY() / Math.max(1f, target.getHeight());
        if (hasCenter) {
            if (ratio < 0.32f) {
                return -1;
            }
            if (ratio > 0.68f) {
                return 1;
            }
            return 0;
        }
        return ratio < 0.5f ? -1 : 1;
    }

    private String bookmarkNodeParent(String key) {
        if (key == null || key.length() < 3) {
            return "";
        }
        if (key.startsWith("I:")) {
            SavedItem item = savedItemByUrl(PREF_THREAD_BOOKMARKS, key.substring(2));
            return item == null ? "" : normalizeSavedFolder(item.folder);
        }
        if (key.startsWith("F:")) {
            return parentSavedFolder(key.substring(2));
        }
        return "";
    }

    private String movedBookmarkNodeKey(String originalKey, String targetParent) {
        if (originalKey == null || originalKey.length() < 3) {
            return "";
        }
        if (originalKey.startsWith("I:")) {
            return originalKey;
        }
        if (originalKey.startsWith("F:")) {
            return "F:" + childSavedFolderPath(targetParent, savedFolderDisplayName(originalKey.substring(2)));
        }
        return "";
    }

    private SavedItem savedItemByUrl(String key, String url) {
        for (SavedItem item : readSavedItems(key)) {
            if (sameSavedUrl(item.url, url)) {
                return item;
            }
        }
        return null;
    }

    private boolean savedItemExists(String key, String url) {
        return savedItemByUrl(key, url) != null;
    }

    private void appendBookmarkOrder(String parent, String key) {
        parent = normalizeSavedFolder(parent);
        List<BookmarkNode> nodes = bookmarkChildren(parent);
        List<String> keys = new ArrayList<>();
        for (BookmarkNode node : nodes) {
            String nodeKey = node.orderKey();
            if (!nodeKey.equals(key)) {
                keys.add(nodeKey);
            }
        }
        keys.add(key);
        writeBookmarkOrderKeys(parent, keys);
    }

    private void removeBookmarkOrderEntry(String parent, String key) {
        parent = normalizeSavedFolder(parent);
        List<String> keys = bookmarkOrder(parent);
        if (keys.remove(key)) {
            writeBookmarkOrderKeys(parent, keys);
        }
    }

    private void writeBookmarkOrderKeys(String parent, List<String> keys) {
        parent = normalizeSavedFolder(parent);
        try {
            JSONObject root = new JSONObject(preferences.getString(PREF_BOOKMARK_ORDER, "{}"));
            JSONArray array = new JSONArray();
            for (String key : keys) {
                if (key != null && !key.isEmpty() && !containsString(array, key)) {
                    array.put(key);
                }
            }
            root.put(parent, array);
            preferences.edit().putString(PREF_BOOKMARK_ORDER, root.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private String bookmarkNodeDragKey(BookmarkNode node) {
        return "bookmark-node:" + node.orderKey();
    }

    private String bookmarkNodeDragValue(String dragKey) {
        return dragKey != null && dragKey.startsWith("bookmark-node:")
                ? dragKey.substring("bookmark-node:".length()) : "";
    }

    private String savedPageToken(String key, String folder) {
        folder = normalizeSavedFolder(folder);
        return folder.isEmpty() ? key : key + "|" + folder;
    }

    private SavedPage savedPageFromToken(String token) {
        String value = token == null ? "" : token;
        int sep = value.indexOf('|');
        if (sep < 0) {
            return new SavedPage(value, "");
        }
        return new SavedPage(value.substring(0, sep), value.substring(sep + 1));
    }

    private int savedFolderItemCount(String key, String folder) {
        int count = 0;
        folder = normalizeSavedFolder(folder);
        for (SavedItem item : readSavedItems(key)) {
            if (savedFolderDescendantOrSelf(folder, normalizeSavedFolder(item.folder))) {
                count++;
            }
        }
        return count;
    }

    private boolean isSavedItem(String key, String url) {
        if (url == null) {
            return false;
        }
        for (SavedItem item : readSavedItems(key)) {
            if (url.equals(item.url)) {
                return true;
            }
        }
        return false;
    }

    private void toggleSavedItem(String key, String title, String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        List<SavedItem> items = readSavedItems(key);
        for (int i = 0; i < items.size(); i++) {
            if (url.equals(items.get(i).url)) {
                items.remove(i);
                writeSavedItems(key, items);
                return;
            }
        }
        items.add(0, new SavedItem(cleanTitle(title, url), url, ""));
        writeSavedItems(key, items);
    }

    private void removeSavedItem(String key, String url) {
        List<SavedItem> items = readSavedItems(key);
        for (int i = items.size() - 1; i >= 0; i--) {
            if (url.equals(items.get(i).url)) {
                items.remove(i);
            }
        }
        writeSavedItems(key, items);
    }

    private void moveSavedItemToFolder(String key, String url, String folder) {
        List<SavedItem> items = readSavedItems(key);
        folder = normalizeSavedFolder(folder);
        for (int i = 0; i < items.size(); i++) {
            SavedItem item = items.get(i);
            if (url.equals(item.url)) {
                items.set(i, new SavedItem(item.title, item.url, folder));
                break;
            }
        }
        if (!folder.isEmpty()) {
            List<String> folders = readSavedFolders(key);
            if (!folders.contains(folder)) {
                folders.add(folder);
                writeSavedFolders(key, folders);
            }
        }
        writeSavedItems(key, items);
    }

    private void createSavedFolder(String key, String folder) {
        createSavedFolder(key, "", folder);
    }

    private void createSavedFolder(String key, String parent, String folder) {
        folder = childSavedFolderPath(parent, folder);
        if (folder.isEmpty()) {
            return;
        }
        List<String> folders = readSavedFolders(key);
        if (!folders.contains(folder)) {
            folders.add(folder);
            writeSavedFolders(key, folders);
        }
    }

    private void renameSavedFolder(String key, String oldFolder, String newFolder) {
        oldFolder = normalizeSavedFolder(oldFolder);
        newFolder = childSavedFolderPath(parentSavedFolder(oldFolder), newFolder);
        if (oldFolder.isEmpty() || newFolder.isEmpty() || oldFolder.equals(newFolder)) {
            return;
        }
        List<String> folders = readSavedFolders(key);
        for (int i = 0; i < folders.size(); i++) {
            String folder = folders.get(i);
            if (savedFolderDescendantOrSelf(oldFolder, folder)) {
                folders.set(i, newFolder + folder.substring(oldFolder.length()));
            }
        }
        if (!folders.contains(newFolder)) {
            folders.add(newFolder);
        }
        List<SavedItem> items = readSavedItems(key);
        for (int i = 0; i < items.size(); i++) {
            SavedItem item = items.get(i);
            String folder = normalizeSavedFolder(item.folder);
            if (savedFolderDescendantOrSelf(oldFolder, folder)) {
                items.set(i, new SavedItem(item.title, item.url, newFolder + folder.substring(oldFolder.length())));
            }
        }
        writeSavedFolders(key, folders);
        writeSavedItems(key, items);
    }

    private void deleteSavedFolder(String key, String folder) {
        folder = normalizeSavedFolder(folder);
        List<String> folders = readSavedFolders(key);
        for (int i = folders.size() - 1; i >= 0; i--) {
            if (savedFolderDescendantOrSelf(folder, folders.get(i))) {
                folders.remove(i);
            }
        }
        List<SavedItem> items = readSavedItems(key);
        for (int i = 0; i < items.size(); i++) {
            SavedItem item = items.get(i);
            if (savedFolderDescendantOrSelf(folder, normalizeSavedFolder(item.folder))) {
                items.set(i, new SavedItem(item.title, item.url, ""));
            }
        }
        writeSavedFolders(key, folders);
        writeSavedItems(key, items);
    }

    private void moveSavedItem(String key, int from, int to) {
        List<SavedItem> items = readSavedItems(key);
        if (from < 0 || from >= items.size() || to < 0 || to >= items.size() || from == to) {
            return;
        }
        SavedItem item = items.remove(from);
        items.add(to, item);
        writeSavedItems(key, items);
    }

    private void showCreateSavedFolderDialog(String key) {
        showCreateSavedFolderDialog(key, "");
    }

    private void showCreateSavedFolderDialog(String key, String parent) {
        showSavedFolderNameDialog(
                text("\u30d5\u30a9\u30eb\u30c0\u3092\u4f5c\u6210", "Create folder"),
                "",
                folder -> {
                    createSavedFolder(key, parent, folder);
                    showSavedItemsView(key, parent);
                });
    }

    private void showRenameSavedFolderDialog(String key, String oldFolder) {
        showSavedFolderNameDialog(
                text("\u30d5\u30a9\u30eb\u30c0\u540d\u3092\u5909\u66f4", "Rename folder"),
                oldFolder,
                folder -> {
                    renameSavedFolder(key, oldFolder, folder);
                    showSavedItemsView(key);
                });
    }

    private interface SavedFolderNameCallback {
        void onFolderName(String folder);
    }

    private void showSavedFolderNameDialog(String title, String initialValue, SavedFolderNameCallback callback) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(10), dp(20), 0);
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(initialValue == null ? "" : initialValue);
        input.setSelectAllOnFocus(true);
        input.setTextColor(textColor());
        input.setHintTextColor(mutedColor());
        input.setTextSize(16);
        input.setPadding(dp(12), 0, dp(12), 0);
        input.setBackground(roundedDrawable(postColor(), borderColor(), dp(8), dp(1)));
        root.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(root)
                .setNegativeButton(text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(text("OK", "OK"), null)
                .create();
        dialog.setOnShowListener(d -> {
            Theme.styleDialog(dialog, this);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String folder = normalizeSavedFolder(input.getText().toString());
                if (folder.isEmpty()) {
                    Toast.makeText(this, text("\u30d5\u30a9\u30eb\u30c0\u540d\u3092\u5165\u529b", "Enter a folder name."), Toast.LENGTH_SHORT).show();
                    return;
                }
                callback.onFolderName(folder);
                dialog.dismiss();
            });
        });
        dialog.show();
        input.requestFocus();
    }

    private void confirmDeleteSavedItem(String key, SavedItem item, String folder) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(text("\u524a\u9664", "Delete"))
                .setMessage(text("\u3053\u306e\u9805\u76ee\u3092\u4e00\u89a7\u304b\u3089\u524a\u9664\u3057\u307e\u3059\u304b\uff1f",
                        "Delete this item from the list?"))
                .setNegativeButton(text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(text("\u524a\u9664", "Delete"), (d, which) -> {
                    removeSavedItem(key, item.url);
                    showSavedItemsView(key, folder);
                })
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private void confirmDeleteSavedFolder(String key, String folder) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(text("\u30d5\u30a9\u30eb\u30c0\u3092\u524a\u9664", "Delete folder"))
                .setMessage(text("\u30d5\u30a9\u30eb\u30c0\u3060\u3051\u524a\u9664\u3057\u3001\u4e2d\u306e\u9805\u76ee\u306f\u4e00\u89a7\u306b\u623b\u3057\u307e\u3059\u304b\uff1f",
                        "Delete only the folder and move its items back to the main list?"))
                .setNegativeButton(text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(text("\u524a\u9664", "Delete"), (d, which) -> {
                    deleteSavedFolder(key, folder);
                    showSavedItemsView(key);
                })
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private void toggleCurrentBookmark() {
        CuspTab tab = currentTab();
        if (!canBookmarkTab(tab)) {
            return;
        }
        toggleSavedItem(PREF_THREAD_BOOKMARKS, bookmarkTitleForTab(tab), tab.url);
        updateBottomThreadBar(tab);
    }

    private boolean canBookmarkTab(CuspTab tab) {
        return tab != null && tab.url != null && !tab.url.trim().isEmpty() && !isInternalPageUrl(tab.url);
    }

    private String bookmarkTitleForTab(CuspTab tab) {
        if (tab == null) {
            return "";
        }
        String title = tab.threadPage != null && tab.threadPage.title != null
                ? tab.threadPage.title
                : tab.searchPage != null && tab.searchPage.title != null ? tab.searchPage.title : tab.title;
        if (title == null || title.trim().isEmpty()) {
            return hostTitle(tab.url);
        }
        return title;
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

    private int readPostNumberForTab(CuspTab tab, String url) {
        return isPrivateTab(tab) || historyDisabled() ? 0 : readPostNumber(preferences, url);
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

    private void saveReadPostNumber(CuspTab tab, String url, int number) {
        if (isPrivateTab(tab) || historyDisabled()) {
            return;
        }
        saveReadPostNumber(preferences, url, number);
    }

    private boolean historyDisabled() {
        return preferences != null && preferences.getBoolean(PREF_DISABLE_HISTORY, false);
    }

    private boolean showBookmarksInTabOverview() {
        return preferences == null || preferences.getBoolean(PREF_SHOW_BOOKMARKS_IN_TAB_OVERVIEW, true);
    }

    private boolean showHistoryOnHome() {
        return preferences == null || preferences.getBoolean(PREF_SHOW_HISTORY_ON_HOME, true);
    }

    private boolean aaModeForPost(ThreadPage page, Post post) {
        if (page == null || post == null) {
            return false;
        }
        Boolean manual = aaPostOverride(preferences, page.url, post.number);
        if (manual != null) {
            return manual;
        }
        if (!autoAaEnabled()) {
            return false;
        }
        if (post.cachedLikelyAa == null) {
            post.cachedLikelyAa = likelyAaPost(post.body);
        }
        return post.cachedLikelyAa;
    }

    private static Boolean aaPostOverride(SharedPreferences preferences, String url, int number) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            JSONObject object = new JSONObject(preferences.getString(PREF_AA_POSTS, "{}"));
            String key = url + "#" + number;
            return object.has(key) ? object.optBoolean(key, false) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void saveAaPost(SharedPreferences preferences, String url, int number, boolean enabled) {
        if (url == null || url.isEmpty()) {
            return;
        }
        try {
            JSONObject object = new JSONObject(preferences.getString(PREF_AA_POSTS, "{}"));
            String key = url + "#" + number;
            object.put(key, enabled);
            preferences.edit().putString(PREF_AA_POSTS, object.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private boolean isMyPost(ThreadPage page, Post post) {
        if (page == null || post == null || page.url == null || page.url.isEmpty()) {
            return false;
        }
        String hash = postBodyHash(post.body);
        if (hash.isEmpty()) {
            return false;
        }
        try {
            JSONObject root = new JSONObject(preferences.getString(PREF_MY_POSTS, "{}"));
            JSONArray items = root.optJSONArray(page.url);
            if (items == null) {
                return false;
            }
            for (int i = 0; i < items.length(); i++) {
                if (hash.equals(items.optString(i))) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private void saveMyPost(CuspTab tab, String body) {
        if (isPrivateTab(tab) || tab == null || tab.url == null || tab.url.isEmpty()) {
            return;
        }
        String hash = postBodyHash(body);
        if (hash.isEmpty()) {
            return;
        }
        try {
            JSONObject root = new JSONObject(preferences.getString(PREF_MY_POSTS, "{}"));
            JSONArray old = root.optJSONArray(tab.url);
            JSONArray next = new JSONArray();
            next.put(hash);
            if (old != null) {
                for (int i = 0; i < old.length() && next.length() < 80; i++) {
                    String value = old.optString(i);
                    if (!hash.equals(value) && !value.isEmpty()) {
                        next.put(value);
                    }
                }
            }
            root.put(tab.url, next);
            preferences.edit().putString(PREF_MY_POSTS, root.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private String postBodyHash(String body) {
        String normalized = normalizeOwnPostBody(body);
        if (normalized.isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(normalized.getBytes(Charset.forName("UTF-8")));
            StringBuilder builder = new StringBuilder();
            for (byte value : bytes) {
                builder.append(String.format(Locale.ROOT, "%02x", value & 0xff));
            }
            return builder.toString();
        } catch (Exception ignored) {
            return normalized;
        }
    }

    private String normalizeOwnPostBody(String body) {
        if (body == null) {
            return "";
        }
        return cleanText(body).replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private void markReadTo(CuspTab tab, int number) {
        markReadTo(tab, number, true);
    }

    private void markReadTo(CuspTab tab, int number, boolean refreshOverview) {
        if (tab == null || tab.threadPage == null || tab.threadPage.url == null) {
            return;
        }
        tab.readPostNumber = Math.max(tab.readPostNumber, number);
        saveReadPostNumber(tab, tab.threadPage.url, tab.readPostNumber);
        updateTabThreadStats(tab, tab.threadPage);
        refreshUnreadColors(tab);
        if (refreshOverview) {
            renderTabs();
            if (tabOverviewVisible) {
                refreshTabOverviewListOnly();
            }
        }
    }

    private void setReadThrough(CuspTab tab, int number) {
        if (tab == null || tab.threadPage == null || tab.threadPage.url == null) {
            return;
        }
        tab.readPostNumber = Math.max(0, number);
        saveReadPostNumber(tab, tab.threadPage.url, tab.readPostNumber);
        updateTabThreadStats(tab, tab.threadPage);
        refreshUnreadColors(tab);
        renderTabs();
        if (tabOverviewVisible) {
            refreshTabOverviewListOnly();
        }
    }

    private void setReadThroughPost(CuspTab tab, Post post) {
        if (tab == null || post == null) {
            return;
        }
        setReadThrough(tab, post.number);
    }

    private int lastExistingPostNumber(ThreadPage page, int oldCount) {
        if (page == null || page.posts == null || page.posts.isEmpty() || oldCount <= 0) {
            return 0;
        }
        int index = Math.min(oldCount, page.posts.size()) - 1;
        Post post = page.posts.get(index);
        return post == null ? 0 : post.number;
    }

    private int unreadCount(CuspTab tab) {
        if (tab == null) {
            return 0;
        }
        if (tab.threadPage != null) {
            int pageMax = maxPostNumber(tab.threadPage);
            if (pageMax >= tab.knownMaxPostNumber) {
                updateTabThreadStats(tab, tab.threadPage);
            }
        }
        if (tab.hasThreadStats) {
            return Math.max(0, tab.cachedUnreadCount);
        }
        return 0;
    }

    private void updateTabThreadStats(CuspTab tab, ThreadPage page) {
        if (tab == null || page == null) {
            return;
        }
        tab.knownMaxPostNumber = maxPostNumber(page);
        tab.knownPostCount = page.posts == null ? 0 : page.posts.size();
        tab.cachedUnreadCount = countUnreadPosts(page, tab.readPostNumber);
        tab.knownThreadArchived = page.archived;
        tab.hasThreadStats = true;
    }

    private void applyThreadPageMetadata(ThreadPage target, ThreadPage source) {
        if (target == null || source == null) {
            return;
        }
        target.title = source.title;
        target.datUrl = source.datUrl;
        target.datByteLength = source.datByteLength;
        target.archived = source.archived;
    }

    private int countUnreadPosts(ThreadPage page, int readPostNumber) {
        if (page == null || page.posts == null) {
            return 0;
        }
        int count = 0;
        for (Post post : page.posts) {
            if (post != null && post.number > readPostNumber) {
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
                card.setBackground(postBackground(post.number > tab.readPostNumber, isMyPost(tab.threadPage, post)));
            }
        }
        updateUnreadScrollMarkers(tab);
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

    static List<BoardPriorityRule> readBoardPriorityRules(SharedPreferences preferences) {
        List<BoardPriorityRule> rules = new ArrayList<>();
        if (preferences == null) {
            return rules;
        }
        try {
            JSONArray array = new JSONArray(preferences.getString(PREF_BOARD_PRIORITY_WORDS, "[]"));
            for (int i = 0; i < array.length(); i++) {
                Object item = array.opt(i);
                if (item instanceof JSONObject) {
                    JSONObject object = (JSONObject) item;
                    String value = object.optString("value", "").trim();
                    if (!value.isEmpty()) {
                        rules.add(new BoardPriorityRule(value, object.optBoolean("regex", false)));
                    }
                } else {
                    String value = array.optString(i, "").trim();
                    if (!value.isEmpty()) {
                        rules.add(new BoardPriorityRule(value, false));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return rules;
    }

    static void saveBoardPriorityRules(SharedPreferences preferences, List<BoardPriorityRule> rules) {
        JSONArray array = new JSONArray();
        try {
            if (rules != null) {
                for (BoardPriorityRule rule : rules) {
                    String value = rule == null || rule.value == null ? "" : rule.value.trim();
                    if (value.isEmpty()) {
                        continue;
                    }
                    JSONObject object = new JSONObject();
                    object.put("value", value);
                    object.put("regex", rule.regex);
                    array.put(object);
                }
            }
        } catch (Exception ignored) {
        }
        preferences.edit().putString(PREF_BOARD_PRIORITY_WORDS, array.toString()).apply();
    }

    static void addBoardPriorityRule(SharedPreferences preferences, String word, boolean regex) {
        String normalized = word == null ? "" : word.trim();
        if (normalized.isEmpty()) {
            return;
        }
        List<BoardPriorityRule> rules = readBoardPriorityRules(preferences);
        for (BoardPriorityRule existing : rules) {
            if (existing.regex == regex && existing.value.equalsIgnoreCase(normalized)) {
                return;
            }
        }
        rules.add(new BoardPriorityRule(normalized, regex));
        saveBoardPriorityRules(preferences, rules);
    }

    static void removeBoardPriorityRule(SharedPreferences preferences, String word, boolean regex) {
        String normalized = word == null ? "" : word.trim();
        List<BoardPriorityRule> rules = readBoardPriorityRules(preferences);
        for (int i = rules.size() - 1; i >= 0; i--) {
            BoardPriorityRule rule = rules.get(i);
            if (rule.regex == regex && rule.value.equalsIgnoreCase(normalized)) {
                rules.remove(i);
            }
        }
        saveBoardPriorityRules(preferences, rules);
    }

    private boolean isThreadUrl(String url) {
        String lower = url.toLowerCase(Locale.ROOT);
        return lower.contains(".5ch.net/test/read.cgi/")
                || lower.contains(".5ch.io/test/read.cgi/")
                || lower.contains(".5ch.io/") && lower.contains("/test/read.cgi/")
                || lower.contains(".2ch.sc/test/read.cgi/")
                || lower.contains("/test/read.cgi/")
                || lower.contains("/bbs/read.cgi/");
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
            return host != null
                    && (is5chUrl(url) || isRegisteredBbsUrl(url)) && boardNameFromUrl(url) != null;
        } catch (Exception error) {
            return false;
        }
    }

    private boolean isBbsDirectoryUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host == null || !isRegisteredBbsUrl(url)) {
                return false;
            }
            return isBbsMenuUrl(url) || boardNameFromUrl(url) == null;
        } catch (Exception error) {
            return false;
        }
    }

    private boolean isBbsMenuUrl(String url) {
        try {
            Uri uri = Uri.parse(normalizeUrl(url));
            String path = uri.getPath();
            if (path == null) {
                return false;
            }
            String lower = path.toLowerCase(Locale.ROOT);
            return lower.endsWith("/bbsmenu.html") || lower.endsWith("/bbsmenu.htm")
                    || lower.endsWith("/menu.html") || lower.endsWith("/menu.htm");
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
                if (baseHost != null && isBbsMenuUrl(link.url) && isSameBbsHostFamily(baseHost, targetHost)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isSameBbsHostFamily(String leftHost, String rightHost) {
        String left = leftHost == null ? "" : leftHost.toLowerCase(Locale.ROOT);
        String right = rightHost == null ? "" : rightHost.toLowerCase(Locale.ROOT);
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        if (left.equals(right)) {
            return true;
        }
        if (left.endsWith(".open2ch.net") || left.equals("open2ch.net")) {
            return right.endsWith(".open2ch.net") || right.equals("open2ch.net");
        }
        if (left.endsWith(".machi.to") || left.equals("machi.to")) {
            return right.endsWith(".machi.to") || right.equals("machi.to");
        }
        if (left.endsWith(".bbspink.org") || left.equals("bbspink.org")) {
            return right.endsWith(".bbspink.org") || right.equals("bbspink.org");
        }
        return false;
    }

    private String boardNameFromUrl(String url) {
        String registeredBoard = registeredMenuBoardName(url);
        if (registeredBoard != null) {
            return registeredBoard;
        }
        Uri uri = Uri.parse(url);
        String path = uri.getPath();
        if (path == null) {
            return null;
        }
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            if ("bbs".equals(part) && i + 2 < parts.length
                    && "read.cgi".equals(parts[i + 1]) && !parts[i + 2].isEmpty()) {
                return parts[i + 2];
            }
            if ("test".equals(part) || "read.cgi".equals(part) || "dat".equals(part)) {
                if ("read.cgi".equals(part) && i + 1 < parts.length && !parts[i + 1].isEmpty()) {
                    return parts[i + 1];
                }
                return null;
            }
            return part;
        }
        return null;
    }

    private String registeredMenuBoardName(String url) {
        try {
            Uri target = Uri.parse(normalizeUrl(url));
            String targetHost = target.getHost();
            if (targetHost == null || isBbsMenuUrl(url)) {
                return null;
            }
            List<String> targetParts = pathParts(target.getPath());
            if (targetParts.isEmpty()) {
                return null;
            }
            for (BbsLink link : readBbsLinks(preferences)) {
                Uri base = Uri.parse(normalizeUrl(link.url));
                String baseHost = base.getHost();
                if (baseHost == null || !isSameBbsHostFamily(baseHost, targetHost) || !isBbsMenuUrl(link.url)) {
                    continue;
                }
                List<String> baseParts = pathParts(base.getPath());
                if (baseParts.isEmpty()) {
                    continue;
                }
                List<String> prefix = baseParts.subList(0, baseParts.size() - 1);
                if (targetParts.size() == prefix.size() + 1 && pathStartsWith(targetParts, prefix)) {
                    String board = targetParts.get(targetParts.size() - 1);
                    return board.contains(".") ? null : board;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<String> pathParts(String path) {
        List<String> parts = new ArrayList<>();
        if (path == null) {
            return parts;
        }
        for (String part : path.split("/")) {
            if (part != null && !part.isEmpty()) {
                parts.add(part);
            }
        }
        return parts;
    }

    private boolean pathStartsWith(List<String> path, List<String> prefix) {
        if (path == null || prefix == null || path.size() < prefix.size()) {
            return false;
        }
        for (int i = 0; i < prefix.size(); i++) {
            if (!path.get(i).equalsIgnoreCase(prefix.get(i))) {
                return false;
            }
        }
        return true;
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
        String value = input.trim().replaceFirst("(?i)^(h?ttps?|ttps?);//", "$1://");
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
        String query = searchQueryFromUrl(url);
        if (query == null || query.trim().isEmpty()) {
            return text("5ch\u691c\u7d22", "5ch Search");
        }
        return text("\u691c\u7d22: ", "Search: ") + query.trim();
    }

    private String searchQueryFromUrl(String url) {
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
        return query == null ? "" : query.trim();
    }

    private boolean fastContains(String haystack, String needle) {
        if (haystack == null || needle == null) {
            return false;
        }
        int n = haystack.length();
        int m = needle.length();
        if (m == 0) {
            return true;
        }
        if (m < 4 || n < 96) {
            return haystack.contains(needle);
        }
        int[] skip = new int[256];
        for (int i = 0; i < skip.length; i++) {
            skip[i] = m;
        }
        for (int i = 0; i < m - 1; i++) {
            skip[needle.charAt(i) & 0xff] = m - 1 - i;
        }
        int index = 0;
        while (index <= n - m) {
            int j = m - 1;
            while (j >= 0 && haystack.charAt(index + j) == needle.charAt(j)) {
                j--;
            }
            if (j < 0) {
                return true;
            }
            index += Math.max(1, skip[haystack.charAt(index + m - 1) & 0xff]);
        }
        return false;
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

    private void installKeyboardFocusWatcher(View root) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect visibleFrame = new Rect();
            root.getWindowVisibleDisplayFrame(visibleFrame);
            int rootHeight = root.getRootView().getHeight();
            if (rootHeight <= 0) {
                return;
            }
            boolean keyboardVisible = rootHeight - visibleFrame.height() > rootHeight * 0.15f;
            if (addressKeyboardVisible && !keyboardVisible && addressBar != null && addressBar.hasFocus()) {
                addressBar.clearFocus();
                addressBar.setSelection(addressBar.getText().length());
                if (suggestionsPanel != null) {
                    suggestionsPanel.setVisibility(View.GONE);
                }
                updateAddressFocusUi(false);
                if (contentFrame != null) {
                    contentFrame.requestFocus();
                }
            }
            addressKeyboardVisible = keyboardVisible;
        });
    }

    private void showKeyboard() {
        try {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.showSoftInput(addressBar, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception ignored) {
            Toast.makeText(this, text("\u691c\u7d22\u3067\u304d\u307e\u3059", "Ready to search."), Toast.LENGTH_SHORT).show();
        }
    }

    private void showKeyboardSoon() {
        if (addressBar == null) {
            return;
        }
        addressBar.post(() -> {
            if (addressBar != null && addressBar.hasFocus()) {
                showKeyboard();
            }
        });
        addressBar.postDelayed(() -> {
            if (addressBar != null && addressBar.hasFocus()) {
                showKeyboard();
            }
        }, 120);
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

    private static class LazyImgurPreview {
        final String originalUrl;
        final String imageUrl;
        final FrameLayout frame;
        final ImageView image;
        final ProgressBar spinner;
        final TextView error;
        final Button reveal;
        final TextView play;
        boolean started;

        LazyImgurPreview(String originalUrl, String imageUrl, FrameLayout frame,
                         ImageView image, ProgressBar spinner, TextView error, Button reveal, TextView play) {
            this.originalUrl = originalUrl;
            this.imageUrl = imageUrl;
            this.frame = frame;
            this.image = image;
            this.spinner = spinner;
            this.error = error;
            this.reveal = reveal;
            this.play = play;
        }
    }

    private static class DeferredMediaPreview {
        final ImgurLink link;
        final FrameLayout placeholder;
        final Runnable longClickAction;
        final int cellSize;
        boolean created;

        DeferredMediaPreview(ImgurLink link, FrameLayout placeholder, Runnable longClickAction, int cellSize) {
            this.link = link;
            this.placeholder = placeholder;
            this.longClickAction = longClickAction;
            this.cellSize = cellSize;
        }
    }

    private static class DeferredTextDecoration {
        final TextView text;
        final String value;
        final ThreadPage page;
        final String highlight;
        boolean decorated;

        DeferredTextDecoration(TextView text, String value, ThreadPage page, String highlight) {
            this.text = text;
            this.value = value;
            this.page = page;
            this.highlight = highlight;
        }
    }

    private static class AaDebugMetrics {
        final boolean aa;
        final String reason;
        final int candidateLines;
        final int targetChars;
        final int specialChars;
        final float ratio;

        AaDebugMetrics(boolean aa, String reason, int candidateLines,
                       int targetChars, int specialChars, float ratio) {
            this.aa = aa;
            this.reason = reason;
            this.candidateLines = candidateLines;
            this.targetChars = targetChars;
            this.specialChars = specialChars;
            this.ratio = ratio;
        }

        String debugText() {
            return String.format(Locale.ROOT,
                    "AA debug: %s (%s) candidate-lines=%d special-chars=%d target-chars=%d %.1f%% | threshold: candidate-lines>=3 & special-chars>%.0f%%",
                    aa ? "YES" : "NO", reason, candidateLines,
                    specialChars, targetChars, ratio * 100f,
                    AA_SPECIAL_CHAR_RATIO_THRESHOLD * 100f);
        }
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
        public void setImageDrawable(Drawable drawable) {
            super.setImageDrawable(drawable);
            post(this::fitImage);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
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
        JSONObject savedThreadPageJson;
        JSONObject savedSearchPageJson;
        ScrollView threadScroll;
        LinearLayout threadList;
        View threadBottomLoader;
        FrameLayout scrollScrubber;
        ViewGroup unreadMarkerLayer;
        Map<Integer, View> postViews;
        Map<Integer, FrameLayout> postSlots;
        Set<FrameLayout> renderedPostSlots;
        String nativeKind;
        float threadScrollRatio;
        int threadBottomOffset;
        String threadScrollUrl = "";
        int readPostNumber;
        int knownMaxPostNumber;
        int knownPostCount;
        int cachedUnreadCount;
        boolean hasThreadStats;
        boolean knownThreadArchived;
        long bottomScrollLockUntil;
        long lastActivatedAt = android.os.SystemClock.uptimeMillis();
        long lastScrollAt;
        long lastThreadScrollSaveAt;
        int threadScrollChromeFrames;
        boolean hasSavedThreadScroll;
        boolean restoreFromBottom;
        boolean threadSearchOpen;
        String threadSearchQuery = "";
        List<Integer> threadSearchMatches = new ArrayList<>();
        String threadSearchLastQuery = "";
        List<Post> threadSearchLastCandidates = new ArrayList<>();
        Set<Integer> threadSearchHighlightedPosts = new LinkedHashSet<>();
        int threadSearchGeneration;
        int threadSearchIndex = -1;
        int returnToIndex = -1;
        boolean backToNewTab;
        boolean privateBrowsing;
        boolean bookmarkOverviewTab;
        boolean readerMode;
        List<String> navigationHistory = new ArrayList<>();
        int navigationIndex = -1;
        int threadRenderGeneration;
        boolean fastRenderToBottom;
        boolean threadRendering;
        Runnable threadScrollChromeTask;
        Runnable threadPostVisibilityTask;
    }

    private static class PostCardShell {
        final FrameLayout shell;
        final LinearLayout card;

        PostCardShell(FrameLayout shell, LinearLayout card) {
            this.shell = shell;
            this.card = card;
        }
    }

    private static class MyPostBackgroundDrawable extends Drawable {
        private final int fillColor;
        private final int markerColor;
        private final int radius;
        private final int markerWidth;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF bounds = new RectF();
        private final Path clip = new Path();
        private ColorFilter colorFilter;
        private int alpha = 255;

        MyPostBackgroundDrawable(int fillColor, int markerColor, int radius, int markerWidth) {
            this.fillColor = fillColor;
            this.markerColor = markerColor;
            this.radius = radius;
            this.markerWidth = markerWidth;
        }

        @Override
        public void draw(Canvas canvas) {
            android.graphics.Rect rawBounds = getBounds();
            bounds.set(rawBounds.left, rawBounds.top, rawBounds.right, rawBounds.bottom);
            paint.setColor(fillColor);
            paint.setAlpha(alpha);
            paint.setColorFilter(colorFilter);
            canvas.drawRoundRect(bounds, radius, radius, paint);

            int save = canvas.save();
            clip.reset();
            clip.addRoundRect(bounds, radius, radius, Path.Direction.CW);
            canvas.clipPath(clip);
            paint.setColor(markerColor);
            paint.setAlpha(alpha);
            canvas.drawRect(rawBounds.left, rawBounds.top,
                    Math.min(rawBounds.right, rawBounds.left + markerWidth), rawBounds.bottom, paint);
            canvas.restoreToCount(save);
        }

        @Override
        public void setAlpha(int alpha) {
            this.alpha = alpha;
            invalidateSelf();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            this.colorFilter = colorFilter;
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return android.graphics.PixelFormat.TRANSLUCENT;
        }
    }

    private static class VirtualPostSlot {
        final ThreadPage page;
        final CuspTab tab;
        final PostRenderItem item;
        int height;
        boolean rendered;
        FrameLayout shell;
        LinearLayout card;

        VirtualPostSlot(ThreadPage page, CuspTab tab, PostRenderItem item, int height) {
            this.page = page;
            this.tab = tab;
            this.item = item;
            this.height = height;
        }
    }

    private class VirtualSearchSlot {
        final String category;
        final SearchResult result;
        final boolean categoryHeader;
        int height;
        boolean rendered;

        VirtualSearchSlot(String category, SearchResult result, boolean categoryHeader) {
            this.category = category;
            this.result = result;
            this.categoryHeader = categoryHeader;
            this.height = categoryHeader ? dp(50) : estimateSearchResultHeight(result);
        }
    }

    private static class VirtualSearchState {
        final Set<FrameLayout> renderedSlots = new LinkedHashSet<>();
        Runnable refreshTask;
        boolean refreshPending;
        long lastScrollAt;
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

    private static class SavedItem {
        final String title;
        final String url;
        final String folder;

        SavedItem(String title, String url) {
            this(title, url, "");
        }

        SavedItem(String title, String url, String folder) {
            this.title = title;
            this.url = url;
            this.folder = folder == null ? "" : folder;
        }
    }

    private static class BookmarkNode {
        final boolean folderNode;
        final String folder;
        final SavedItem item;

        private BookmarkNode(boolean folderNode, String folder, SavedItem item) {
            this.folderNode = folderNode;
            this.folder = folder == null ? "" : folder;
            this.item = item;
        }

        static BookmarkNode folder(String folder) {
            return new BookmarkNode(true, folder, null);
        }

        static BookmarkNode item(SavedItem item) {
            return new BookmarkNode(false, item == null ? "" : item.folder, item);
        }

        String orderKey() {
            return folderNode ? "F:" + folder : "I:" + (item == null ? "" : item.url);
        }

        String label() {
            return folderNode ? folder : item == null ? "" : item.title;
        }
    }

    private static class SavedPage {
        final String key;
        final String folder;

        SavedPage(String key, String folder) {
            this.key = key == null ? "" : key;
            this.folder = folder == null ? "" : folder;
        }
    }

    private static class DragPayload {
        final String key;
        int index;

        DragPayload(String key, int index) {
            this.key = key;
            this.index = index;
        }
    }

    private static class ThreadPage {
        String url;
        String title;
        String error;
        String datUrl;
        long datByteLength;
        int newPostCount;
        boolean archived;
        List<Post> posts = new ArrayList<>();
        Map<Integer, Post> postsByNumber = new LinkedHashMap<>();
        Map<String, Post> firstPostByBody = new LinkedHashMap<>();
        boolean copyPasteIndexBuilt;

        static ThreadPage error(String url, String message) {
            ThreadPage page = new ThreadPage();
            page.url = url;
            page.title = text("\u8aad\u307f\u8fbc\u307f\u5931\u6557", "Load failed");
            page.error = message == null ? "Unknown error" : message;
            return page;
        }
    }

    private static class ThreadOverviewStatus {
        String url;
        String title;
        int responseCount;
        boolean archived;
    }

    private static class BookmarkOverviewStatus {
        String title;
        int responseCount;
        boolean archived;
    }

    private static class DatDownload {
        final String url;
        final String body;
        final long totalByteLength;
        final boolean partial;

        DatDownload(String url, String body, long totalByteLength, boolean partial) {
            this.url = url;
            this.body = body;
            this.totalByteLength = totalByteLength;
            this.partial = partial;
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
        String category;
        int responses;
        double velocity;
        int boardOrder;
        BoardPriorityMatch priorityMatch;
    }

    private static class BbsCategoryRequest {
        final String menuUrl;
        final String category;

        BbsCategoryRequest(String menuUrl, String category) {
            this.menuUrl = menuUrl;
            this.category = category;
        }
    }

    static class BoardPriorityRule {
        final String value;
        final boolean regex;

        BoardPriorityRule(String value, boolean regex) {
            this.value = value;
            this.regex = regex;
        }
    }

    private static class BoardPriorityMatch {
        final String value;
        final boolean regex;

        BoardPriorityMatch(String value, boolean regex) {
            this.value = value;
            this.regex = regex;
        }
    }

    private static class BoardSubject {
        final String body;
        final String threadBase;

        BoardSubject(String body, String threadBase) {
            this.body = body;
            this.threadBase = threadBase;
        }
    }

    private static class PostRenderItem {
        final Post post;
        final int depth;
        final Set<Integer> continuationDepths;
        final boolean hasReplies;

        PostRenderItem(Post post, int depth, Set<Integer> continuationDepths, boolean hasReplies) {
            this.post = post;
            this.depth = depth;
            this.continuationDepths = continuationDepths == null
                    ? new LinkedHashSet<>()
                    : new LinkedHashSet<>(continuationDepths);
            this.hasReplies = hasReplies;
        }
    }

    private static class TreeConnectorView extends View {
        private final int depth;
        private final Set<Integer> continuationDepths;
        private final boolean hasReplies;
        private final int indent;
        private final int cardBottomGap;
        private final float connectorOffset;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TreeConnectorView(Context context, PostRenderItem item, int indent, int color) {
            super(context);
            this.depth = item.depth;
            this.continuationDepths = item.continuationDepths;
            this.hasReplies = item.hasReplies;
            this.indent = indent;
            float density = context.getResources().getDisplayMetrics().density;
            this.cardBottomGap = Math.round(density * POST_OUTER_GAP_DP);
            this.connectorOffset = density * 3f;
            int nightMode = context.getResources().getConfiguration().uiMode
                    & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            paint.setColor(nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
                    ? Color.rgb(43, 95, 91)
                    : Color.rgb(169, 216, 210));
            paint.setStrokeWidth(Math.max(2f, context.getResources().getDisplayMetrics().density * 2f));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            setWillNotDraw(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (depth <= 0 && !hasReplies) {
                return;
            }
            float branchY = Math.min(getHeight() - 1f, Math.max(indent * 0.75f, indent * 0.9f));
            for (Integer level : continuationDepths) {
                if (level == null || level <= 0 || level >= depth) {
                    continue;
                }
                float x = connectorX(level);
                canvas.drawLine(x, 0, x, getHeight(), paint);
            }
            if (depth > 0) {
                float parentX = connectorX(depth);
                float childEdgeX = depth * indent;
                float currentEndY = continuationDepths.contains(depth) ? getHeight() : branchY;
                float radius = Math.min(indent * 0.35f, Math.abs(childEdgeX - parentX) * 0.5f);
                Path branch = new Path();
                if (continuationDepths.contains(depth)) {
                    canvas.drawLine(parentX, 0, parentX, currentEndY, paint);
                    branch.moveTo(parentX, Math.max(0f, branchY - radius));
                } else {
                    branch.moveTo(parentX, 0);
                    branch.lineTo(parentX, Math.max(0f, branchY - radius));
                }
                branch.quadTo(parentX, branchY, parentX + radius, branchY);
                branch.lineTo(childEdgeX, branchY);
                canvas.drawPath(branch, paint);
            }
            if (hasReplies) {
                float childTrunkX = connectorX(depth + 1);
                float cardBottomY = Math.max(branchY, getHeight() - cardBottomGap);
                float startY = Math.max(branchY, Math.min(getHeight(), cardBottomY - paint.getStrokeWidth() * 0.75f));
                canvas.drawLine(childTrunkX, startY, childTrunkX, getHeight(), paint);
            }
        }

        private float connectorX(int level) {
            return (level - 1) * indent + indent / 2f + connectorOffset;
        }
    }

    private static class PostResult {
        String body;
        List<String> cookies = new ArrayList<>();
    }

    private static class ImageLoadResult {
        final Bitmap bitmap;
        final Drawable drawable;
        final boolean missing;

        ImageLoadResult(Bitmap bitmap, boolean missing) {
            this.bitmap = bitmap;
            this.drawable = null;
            this.missing = missing;
        }

        ImageLoadResult(Drawable drawable, boolean missing) {
            this.bitmap = null;
            this.drawable = drawable;
            this.missing = missing;
        }

        ImageLoadResult(Bitmap bitmap, Drawable drawable, boolean missing) {
            this.bitmap = bitmap;
            this.drawable = drawable;
            this.missing = missing;
        }
    }

    private static class ImgurLink {
        final String originalUrl;
        final String imageUrl;
        final boolean video;

        ImgurLink(String originalUrl, String imageUrl, boolean video) {
            this.originalUrl = originalUrl;
            this.imageUrl = imageUrl;
            this.video = video;
        }
    }

    private static class ImgbbUploadResult {
        final String name;
        final String mime;
        final String link;
        final String deleteUrl;
        final int expirationSeconds;
        final long time;

        ImgbbUploadResult(String name, String mime, String link, String deleteUrl, int expirationSeconds, long time) {
            this.name = name;
            this.mime = mime;
            this.link = link;
            this.deleteUrl = deleteUrl;
            this.expirationSeconds = expirationSeconds;
            this.time = time;
        }
    }

    private static class DownloadedImageBytes {
        final String url;
        final byte[] bytes;

        DownloadedImageBytes(String url, byte[] bytes) {
            this.url = url;
            this.bytes = bytes;
        }
    }

    private static class DatAddress {
        String scheme;
        String host;
        String server;
        String board;
        String key;
    }

    private static class TouchedLink {
        final String url;
        final int rawX;
        final int rawY;

        TouchedLink(String url, int rawX, int rawY) {
            this.url = url;
            this.rawX = rawX;
            this.rawY = rawY;
        }
    }

    private static class ClosedTab {
        final CuspTab tab;
        final int index;
        final int oldCurrentIndex;
        final SavedItem savedItem;
        final int savedItemIndex;

        ClosedTab(CuspTab tab, int index, int oldCurrentIndex) {
            this.tab = tab;
            this.index = index;
            this.oldCurrentIndex = oldCurrentIndex;
            this.savedItem = null;
            this.savedItemIndex = -1;
        }

        ClosedTab(SavedItem savedItem, int savedItemIndex) {
            this.tab = null;
            this.index = -1;
            this.oldCurrentIndex = -1;
            this.savedItem = savedItem;
            this.savedItemIndex = savedItemIndex;
        }
    }

    private static class Post {
        int number;
        String name;
        String date;
        String body;
        String cachedSearchBody;
        List<ImgurLink> cachedImgurLinks;
        String cachedId;
        String cachedBe;
        String cachedNgRulesKey;
        boolean cachedNgMatch;
        Boolean cachedLikelyAa;
        String cachedAaBody;
        int cachedBodyLineCount;
        float cachedAaLongestLineWidthPx;
        int cachedAaFitWidth;
        float cachedAaFitTextSizePx;
        boolean aaMode;
        boolean swiping;
        long lastSwipeAt;
        int copyPasteSourceNumber;

        String searchBody() {
            if (cachedSearchBody == null) {
                cachedSearchBody = body == null ? "" : body.toLowerCase(Locale.ROOT);
            }
            return cachedSearchBody;
        }

        String id() {
            if (cachedId == null) {
                Matcher matcher = POST_ID_PATTERN.matcher(date == null ? "" : date);
                cachedId = matcher.find() ? matcher.group(1) : "";
            }
            return cachedId;
        }

        String be() {
            if (cachedBe == null) {
                Matcher matcher = BE_PATTERN.matcher(date == null ? "" : date);
                cachedBe = matcher.find() ? matcher.group(1) : "";
            }
            return cachedBe;
        }
    }
}
