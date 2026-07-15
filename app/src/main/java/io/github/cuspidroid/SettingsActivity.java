package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    static final String EXTRA_CATEGORY = "settings_category";
    static final String EXTRA_CATEGORY_TITLE = "settings_category_title";
    static final String EXTRA_CATEGORY_SUBTITLE = "settings_category_subtitle";
    private static final int CATEGORY_GESTURES = 3;
    private static final int CATEGORY_BBS_LINKS = 5;
    private static final int REQUEST_CHMATE_DATABASE = 4201;
    private static final int REQUEST_CUSPIDROID_BACKUP_CREATE = 4202;
    private static final int REQUEST_CUSPIDROID_BACKUP_RESTORE = 4203;
    private static final int TEXT = Color.rgb(31, 41, 55);
    private static final int MUTED = Color.rgb(79, 91, 103);
    private static final int SURFACE = Color.rgb(247, 248, 250);
    private static final int BORDER = Color.rgb(215, 221, 226);

    private SharedPreferences preferences;
    private CheckBox open5chInNewTab;
    private CheckBox externalLinkInApp;
    private CheckBox showMediaPreviews;
    private CheckBox blurImgurImages;
    private CheckBox blurVideoThumbnails;
    private CheckBox blurGifThumbnails;
    private CheckBox autoplayGifs;
    private EditText imgbbApiKey;
    private RadioButton addressBarTop;
    private RadioButton addressBarBottom;
    private CheckBox hideBarsOnScroll;
    private CheckBox titleBarTabSwipe;
    private CheckBox treeView;
    private CheckBox treeSkipFirstReply;
    private CheckBox autoScrollUnread;
    private CheckBox markExistingReadOnThreadUpdate;
    private CheckBox colorUnreadPosts;
    private CheckBox omitCopyPaste;
    private CheckBox autoAa;
    private EditText popularReplyThreshold;
    private CheckBox cacheEnabled;
    private CheckBox saveBrowsingHistory;
    private CheckBox saveReadHistory;
    private CheckBox saveWritePostHistory;
    private CheckBox saveWriteIdentityHistory;
    private CheckBox saveUploadHistory;
    private CheckBox showBookmarksInTabOverview;
    private CheckBox showHistoryOnHome;
    private CheckBox showHomeBookmarkUnreadBadges;
    private CheckBox sync2chEnabled;
    private EditText sync2chId;
    private EditText sync2chApiPassword;
    private EditText cacheMaxMb;
    private TextView cacheApply;
    private ProgressBar cacheUsage;
    private TextView cacheUsageText;
    private Button clearCacheButton;
    private RadioButton themeSystem;
    private RadioButton themeLight;
    private RadioButton themeDark;
    private RadioGroup themeGroup;
    private RadioButton searchFind5chIo;
    private RadioButton searchCustom;
    private EditText customTemplate;

    private int bgColor() {
        return Theme.background(this);
    }

    private int surfaceColor() {
        return Theme.surface(this);
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

    private int hintColor() {
        return Theme.subtle(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        buildLayout();
        loadSettings();
        setupAutoSave();
    }

    @Override
    protected void onPause() {
        saveSettings(false);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCacheUsage();
    }

    private void buildLayout() {
        Theme.applySystemBars(this);
        boolean categoryScreen = this instanceof SettingsCategoryActivity;
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bgColor());
        SectionedSettingsLayout root = new SectionedSettingsLayout(categoryScreen,
                getIntent().getIntExtra(EXTRA_CATEGORY, -1));
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText(categoryScreen
                ? getIntent().getStringExtra(EXTRA_CATEGORY_TITLE)
                : MainActivity.text("\u8a2d\u5b9a", "Settings"));
        title.setTextColor(textColor());
        title.setTextSize(28);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.START);
        title.setPadding(0, 0, 0, dp(2));
        root.addView(title);

        TextView introduction = helperText(categoryScreen
                ? getIntent().getStringExtra(EXTRA_CATEGORY_SUBTITLE)
                : MainActivity.text(
                "\u4f7f\u3044\u65b9\u306b\u5408\u308f\u305b\u3066\u3001\u8868\u793a\u30fb\u64cd\u4f5c\u30fb\u30c7\u30fc\u30bf\u7ba1\u7406\u3092\u8abf\u6574\u3067\u304d\u307e\u3059\u3002",
                "Tune appearance, controls, and data management to match how you use the app."));
        introduction.setTextSize(14);
        introduction.setPadding(0, 0, 0, dp(10));
        root.addView(introduction);

        root.addView(sectionTitle(R.drawable.ic_settings,
                MainActivity.text("\u5916\u89b3\u3068\u30db\u30fc\u30e0", "Appearance & Home"),
                MainActivity.text("\u30c6\u30fc\u30de\u3001\u30d0\u30fc\u306e\u4f4d\u7f6e\u3001\u30db\u30fc\u30e0\u3068\u30e1\u30cb\u30e5\u30fc", "Theme, bar position, home screen, and menus")));
        themeGroup = new RadioGroup(this);
        themeGroup.setOrientation(RadioGroup.VERTICAL);
        themeSystem = radio(MainActivity.text("\u7aef\u672b\u306e\u30c6\u30fc\u30de\u306b\u5f93\u3046", "Follow device theme"));
        themeLight = radio(MainActivity.text("\u30e9\u30a4\u30c8", "Light"));
        themeDark = radio(MainActivity.text("\u30c0\u30fc\u30af", "Dark"));
        themeSystem.setId(View.generateViewId());
        themeLight.setId(View.generateViewId());
        themeDark.setId(View.generateViewId());
        themeGroup.addView(themeSystem);
        themeGroup.addView(themeLight);
        themeGroup.addView(themeDark);
        root.addView(themeGroup);

        RadioGroup addressBarPosition = new RadioGroup(this);
        addressBarPosition.setOrientation(RadioGroup.HORIZONTAL);
        addressBarBottom = radio(MainActivity.text("\u691c\u7d22\u30d0\u30fc\u3092\u4e0b\u306b\u8868\u793a", "Address bar at bottom"));
        addressBarTop = radio(MainActivity.text("\u691c\u7d22\u30d0\u30fc\u3092\u4e0a\u306b\u8868\u793a", "Address bar at top"));
        addressBarBottom.setId(View.generateViewId());
        addressBarTop.setId(View.generateViewId());
        addressBarPosition.addView(addressBarBottom, new RadioGroup.LayoutParams(0, dp(44), 1));
        addressBarPosition.addView(addressBarTop, new RadioGroup.LayoutParams(0, dp(44), 1));
        root.addView(addressBarPosition);

        hideBarsOnScroll = new CheckBox(this);
        hideBarsOnScroll.setText(MainActivity.text("\u30b9\u30af\u30ed\u30fc\u30eb\u6642\u306b\u30d0\u30fc\u3092\u81ea\u52d5\u3067\u96a0\u3059", "Hide bars while scrolling"));
        hideBarsOnScroll.setTextColor(textColor());
        hideBarsOnScroll.setTextSize(16);
        Theme.tintCompoundButton(this, hideBarsOnScroll);
        root.addView(hideBarsOnScroll);

        titleBarTabSwipe = new CheckBox(this);
        titleBarTabSwipe.setText(MainActivity.text("\u30bf\u30a4\u30c8\u30eb\u30d0\u30fc\u306e\u30b9\u30ef\u30a4\u30d7\u3067\u30bf\u30d6\u3092\u79fb\u52d5", "Swipe the title bar to switch tabs"));
        titleBarTabSwipe.setTextColor(textColor());
        titleBarTabSwipe.setTextSize(16);
        Theme.tintCompoundButton(this, titleBarTabSwipe);
        root.addView(titleBarTabSwipe);

        showBookmarksInTabOverview = new CheckBox(this);
        showBookmarksInTabOverview.setText(MainActivity.text("\u30bf\u30d6\u4e00\u89a7\u306b\u30d6\u30c3\u30af\u30de\u30fc\u30af\u3092\u8868\u793a", "Show bookmarks in the tab overview"));
        showBookmarksInTabOverview.setTextColor(textColor());
        showBookmarksInTabOverview.setTextSize(16);
        Theme.tintCompoundButton(this, showBookmarksInTabOverview);
        root.addView(showBookmarksInTabOverview);

        showHistoryOnHome = new CheckBox(this);
        showHistoryOnHome.setText(MainActivity.text("\u65b0\u898f\u30bf\u30d6\u306b\u5c65\u6b74\u3092\u8868\u793a", "Show history on the new tab page"));
        showHistoryOnHome.setTextColor(textColor());
        showHistoryOnHome.setTextSize(16);
        Theme.tintCompoundButton(this, showHistoryOnHome);
        root.addView(showHistoryOnHome);

        showHomeBookmarkUnreadBadges = new CheckBox(this);
        showHomeBookmarkUnreadBadges.setText(MainActivity.text("\u65b0\u898f\u30bf\u30d6\u306e\u30d6\u30c3\u30af\u30de\u30fc\u30af\u306b\u672a\u8aad\u6570\u3092\u8868\u793a", "Show unread counts on new tab bookmarks"));
        showHomeBookmarkUnreadBadges.setTextColor(textColor());
        showHomeBookmarkUnreadBadges.setTextSize(16);
        Theme.tintCompoundButton(this, showHomeBookmarkUnreadBadges);
        root.addView(showHomeBookmarkUnreadBadges);

        root.addView(managementRow(R.drawable.ic_more_vert,
                MainActivity.text("\u691c\u7d22\u30d0\u30fc\u30e1\u30cb\u30e5\u30fc\u914d\u7f6e", "Search bar menu layout"),
                MainActivity.text("\u691c\u7d22\u30d0\u30fc\u30e1\u30cb\u30e5\u30fc\u306e\u8868\u793a\u3068\u9806\u756a\u3092\u8a2d\u5b9a", "Configure visibility and order for the search bar menu"),
                v -> startActivity(new Intent(this, ButtonLayoutSettingsActivity.class)
                        .putExtra(ButtonLayoutSettingsActivity.EXTRA_MODE, ButtonLayoutSettingsActivity.MODE_ADDRESS))));
        root.addView(managementRow(R.drawable.ic_more_vert,
                MainActivity.text("\u30bf\u30a4\u30c8\u30eb\u30d0\u30fc\u30e1\u30cb\u30e5\u30fc\u914d\u7f6e", "Title bar menu layout"),
                MainActivity.text("\u30bf\u30a4\u30c8\u30eb\u30d0\u30fc\u5e38\u99d0\u30fb\u30e1\u30cb\u30e5\u30fc\u5185\u30fb\u975e\u8868\u793a\u3092\u8a2d\u5b9a", "Configure pinned, menu, and hidden title actions"),
                v -> startActivity(new Intent(this, ButtonLayoutSettingsActivity.class)
                        .putExtra(ButtonLayoutSettingsActivity.EXTRA_MODE, ButtonLayoutSettingsActivity.MODE_TITLE))));

        root.addView(sectionTitle(R.drawable.ic_text_fields,
                MainActivity.text("\u30b9\u30ec\u306e\u95b2\u89a7", "Reading Threads"),
                MainActivity.text("\u672a\u8aad\u3001\u30c4\u30ea\u30fc\u8868\u793a\u3001AA\u306e\u8aad\u307f\u65b9", "Unread posts, tree view, and AA rendering")));
        treeView = new CheckBox(this);
        treeView.setText(MainActivity.text("\u30c4\u30ea\u30fc\u8868\u793a", "Tree view"));
        treeView.setTextColor(textColor());
        treeView.setTextSize(16);
        Theme.tintCompoundButton(this, treeView);
        root.addView(treeView);

        treeSkipFirstReply = new CheckBox(this);
        treeSkipFirstReply.setText(MainActivity.text(">>1\u3092\u30c4\u30ea\u30fc\u8868\u793a\u3057\u306a\u3044", "Do not tree replies to >>1"));
        treeSkipFirstReply.setTextColor(textColor());
        treeSkipFirstReply.setTextSize(16);
        Theme.tintCompoundButton(this, treeSkipFirstReply);
        root.addView(treeSkipFirstReply);

        autoScrollUnread = new CheckBox(this);
        autoScrollUnread.setText(MainActivity.text("\u30b9\u30ec\u8aad\u307f\u8fbc\u307f\u6642\u306b\u672a\u8aad\u306e\u5148\u982d\u3078\u79fb\u52d5", "Jump to the first unread post when opening a thread"));
        autoScrollUnread.setTextColor(textColor());
        autoScrollUnread.setTextSize(16);
        Theme.tintCompoundButton(this, autoScrollUnread);
        root.addView(autoScrollUnread);

        markExistingReadOnThreadUpdate = new CheckBox(this);
        markExistingReadOnThreadUpdate.setText(MainActivity.text("\u30b9\u30ec\u66f4\u65b0\u6642\u306b\u4eca\u307e\u3067\u306e\u66f8\u304d\u8fbc\u307f\u3092\u65e2\u8aad\u306b\u3059\u308b", "Mark existing posts read when refreshing a thread"));
        markExistingReadOnThreadUpdate.setTextColor(textColor());
        markExistingReadOnThreadUpdate.setTextSize(16);
        Theme.tintCompoundButton(this, markExistingReadOnThreadUpdate);
        root.addView(markExistingReadOnThreadUpdate);

        colorUnreadPosts = new CheckBox(this);
        colorUnreadPosts.setText(MainActivity.text("\u672a\u8aad\u306e\u66f8\u304d\u8fbc\u307f\u3092\u5225\u306e\u8272\u306b\u3059\u308b", "Use a different color for unread posts"));
        colorUnreadPosts.setTextColor(textColor());
        colorUnreadPosts.setTextSize(16);
        Theme.tintCompoundButton(this, colorUnreadPosts);
        root.addView(colorUnreadPosts);

        omitCopyPaste = new CheckBox(this);
        omitCopyPaste.setText(MainActivity.text("\u30b3\u30d4\u30da\u3092\u7701\u7565\u8868\u793a", "Omit repeated copy-paste posts"));
        omitCopyPaste.setTextColor(textColor());
        omitCopyPaste.setTextSize(16);
        Theme.tintCompoundButton(this, omitCopyPaste);
        root.addView(omitCopyPaste);

        autoAa = new CheckBox(this);
        autoAa.setText(MainActivity.text("AA\u3092\u81ea\u52d5\u5224\u5b9a\u3057\u3066\u8868\u793a", "Automatically detect and show AA"));
        autoAa.setTextColor(textColor());
        autoAa.setTextSize(16);
        Theme.tintCompoundButton(this, autoAa);
        root.addView(autoAa);

        root.addView(sectionTitle(android.R.drawable.ic_menu_sort_by_size,
                MainActivity.text("\u4e00\u89a7\u8868\u793a", "List Display"),
                MainActivity.text("\u30b9\u30ec\u4e00\u89a7\u3068\u30bf\u30d6\u4e00\u89a7\u306e\u898b\u3048\u65b9", "Thread-list and tab-list presentation")));
        root.addView(managementRow(android.R.drawable.ic_menu_sort_by_size,
                MainActivity.text("\u30b9\u30ec\u4e00\u89a7\u8a2d\u5b9a", "Thread list settings"),
                MainActivity.text("\u30b9\u30ec\u4e00\u89a7\u306e\u8868\u793a\u9805\u76ee\u3001\u4e26\u3079\u66ff\u3048\u3001\u512a\u5148\u30ef\u30fc\u30c9", "Displayed fields, sorting, and priority words for thread lists"),
                v -> startActivity(new Intent(this, ListDisplaySettingsActivity.class)
                        .putExtra(ListDisplaySettingsActivity.EXTRA_MODE, ListDisplaySettingsActivity.MODE_BOARD))));
        root.addView(managementRow(android.R.drawable.ic_menu_sort_by_size,
                MainActivity.text("\u30bf\u30d6\u4e00\u89a7\u8a2d\u5b9a", "Tab list settings"),
                MainActivity.text("\u30bf\u30d6\u4e00\u89a7\u306e\u8868\u793a\u9805\u76ee\u3001\u81ea\u52d5\u4e26\u3079\u66ff\u3048\u3001\u8868\u793a\u4f4d\u7f6e", "Displayed fields, automatic sorting, and placement for the tab list"),
                v -> startActivity(new Intent(this, ListDisplaySettingsActivity.class)
                        .putExtra(ListDisplaySettingsActivity.EXTRA_MODE, ListDisplaySettingsActivity.MODE_TAB))));

        root.addView(sectionTitle(android.R.drawable.ic_menu_compass,
                MainActivity.text("\u30b8\u30a7\u30b9\u30c1\u30e3\u30fc", "Gestures"),
                MainActivity.text("\u30b9\u30ef\u30a4\u30d7\u64cd\u4f5c\u3068\u5272\u308a\u5f53\u3066", "Swipes and action assignments")));
        root.addView(managementRow(android.R.drawable.ic_menu_compass,
                MainActivity.text("\u30b8\u30a7\u30b9\u30c1\u30e3\u30fc", "Gestures"),
                MainActivity.text("\u30b9\u30ef\u30a4\u30d7\u64cd\u4f5c\u3068\u5272\u308a\u5f53\u3066\u3092\u8a2d\u5b9a", "Set swipe gestures and actions"),
                v -> startActivity(new Intent(this, GestureSettingsActivity.class))));

        root.addView(sectionTitle(R.drawable.ic_search,
                MainActivity.text("\u30ea\u30f3\u30af\u3068\u691c\u7d22", "Links & Search"),
                MainActivity.text("\u30ea\u30f3\u30af\u306e\u958b\u304d\u65b9\u3068\u30b9\u30ec\u691c\u7d22", "How links open and thread search")));
        open5chInNewTab = new CheckBox(this);
        open5chInNewTab.setText(MainActivity.text("5ch\u30ea\u30f3\u30af\u3092\u65b0\u898f\u30bf\u30d6\u3067\u958b\u304f", "Open 5ch links in a new tab"));
        open5chInNewTab.setTextColor(textColor());
        open5chInNewTab.setTextSize(16);
        Theme.tintCompoundButton(this, open5chInNewTab);
        root.addView(open5chInNewTab);

        externalLinkInApp = new CheckBox(this);
        externalLinkInApp.setText(MainActivity.text("\u5916\u90e8\u30ea\u30f3\u30af\u3092\u30a2\u30d7\u30ea\u5185\u30d6\u30e9\u30a6\u30b6\u3067\u958b\u304f", "Open external links in the in-app browser"));
        externalLinkInApp.setTextColor(textColor());
        externalLinkInApp.setTextSize(16);
        Theme.tintCompoundButton(this, externalLinkInApp);
        root.addView(externalLinkInApp);

        RadioGroup searchGroup = new RadioGroup(this);
        searchGroup.setOrientation(RadioGroup.VERTICAL);
        searchFind5chIo = radio("find.5ch.io");
        searchCustom = radio(MainActivity.text("\u30ab\u30b9\u30bf\u30e0URL\u30c6\u30f3\u30d7\u30ec\u30fc\u30c8", "Custom URL template"));
        searchGroup.addView(searchFind5chIo);
        searchGroup.addView(searchCustom);
        root.addView(searchGroup);

        customTemplate = new EditText(this);
        customTemplate.setSingleLine(true);
        customTemplate.setTextSize(14);
        customTemplate.setTextColor(textColor());
        customTemplate.setHintTextColor(hintColor());
        customTemplate.setHint("https://example.com/search?q=%s");
        customTemplate.setImeOptions(EditorInfo.IME_ACTION_DONE);
        customTemplate.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        customTemplate.setBackground(roundedField());
        customTemplate.setPadding(dp(12), 0, dp(12), 0);
        root.addView(customTemplate, fieldParams());

        TextView hint = helperText(MainActivity.text("\u691c\u7d22\u8a9e\u3092\u5165\u308c\u308b\u5834\u6240\u306b %s \u3092\u4f7f\u3046", "Use %s where the encoded query should be inserted."));
        root.addView(hint);

        root.addView(sectionTitle(R.drawable.ic_link,
                MainActivity.text("BBS\u30ea\u30f3\u30af\u3092\u7ba1\u7406", "Manage BBS Links"),
                MainActivity.text("\u30ab\u30b9\u30bf\u30e0BBS\u3068\u8a8d\u8a3c", "Custom BBS sites and authentication")));
        root.addView(helperText(MainActivity.text(
                "\u8a8d\u8a3c\u304c\u5fc5\u8981\u306aBBS\u306f\u3001\u30b9\u30ec\u3092WebView\u3067\u958b\u3044\u3066\u8a8d\u8a3c\u3059\u308b\u3068\u3001\u305d\u306e\u30af\u30c3\u30ad\u30fc\u3092\u4f7f\u3063\u3066\u95b2\u89a7\u30fb\u66f8\u304d\u8fbc\u307f\u3067\u304d\u307e\u3059\u3002",
                "If a BBS requires authentication, open the thread in WebView and authenticate there. CuspiDroid will use those cookies for reading and posting.")));
        root.addView(managementRow(R.drawable.ic_settings,
                MainActivity.text("BBS\u30ea\u30f3\u30af\u3092\u7ba1\u7406", "Manage BBS links"),
                MainActivity.text("\u30ab\u30b9\u30bf\u30e0BBS\u306e\u540d\u524d\u3068\u677fURL\u3092\u8ffd\u52a0\u30fb\u7de8\u96c6", "Add and edit custom BBS names and board URLs"),
                v -> startActivity(new Intent(this, BbsLinksActivity.class))));

        root.addView(sectionTitle(R.drawable.ic_image,
                MainActivity.text("\u30e1\u30c7\u30a3\u30a2\u3068\u30d5\u30a3\u30eb\u30bf", "Media & Filters"),
                MainActivity.text("\u753b\u50cf\u30fb\u52d5\u753b\u306e\u8868\u793a\u3068\u30b3\u30f3\u30c6\u30f3\u30c4\u30d5\u30a3\u30eb\u30bf", "Image and video display, plus content filters")));
        root.addView(fieldLabel(MainActivity.text("\u4eba\u6c17\u30ec\u30b9\u306e\u65e2\u5b9a\u95be\u5024", "Default popular post threshold")));
        root.addView(helperText(MainActivity.text(
                "\u4eba\u6c17\u30ec\u30b9\u30d5\u30a3\u30eb\u30bf\u3092\u958b\u3044\u305f\u3068\u304d\u306b\u4f7f\u3046\u300cn\u4ef6\u4ee5\u4e0a\u306e\u8fd4\u4fe1\u300d\u306e\u521d\u671f\u5024",
                "Initial reply-count threshold used when opening the popular posts filter.")));
        popularReplyThreshold = new EditText(this);
        popularReplyThreshold.setSingleLine(true);
        popularReplyThreshold.setTextSize(14);
        popularReplyThreshold.setTextColor(textColor());
        popularReplyThreshold.setHintTextColor(hintColor());
        popularReplyThreshold.setHint("3");
        popularReplyThreshold.setImeOptions(EditorInfo.IME_ACTION_DONE);
        popularReplyThreshold.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        popularReplyThreshold.setBackground(roundedField());
        popularReplyThreshold.setPadding(dp(12), 0, dp(12), 0);
        root.addView(popularReplyThreshold, fieldParams());

        showMediaPreviews = new CheckBox(this);
        showMediaPreviews.setText(MainActivity.text("\u66f8\u304d\u8fbc\u307f\u5185\u306e\u30e1\u30c7\u30a3\u30a2\u3092\u8868\u793a", "Show media in posts"));
        showMediaPreviews.setTextColor(textColor());
        showMediaPreviews.setTextSize(16);
        Theme.tintCompoundButton(this, showMediaPreviews);
        root.addView(showMediaPreviews);

        blurImgurImages = new CheckBox(this);
        blurImgurImages.setText(MainActivity.text("\u30b0\u30ed\u753b\u50cf\u3092\u307c\u304b\u3059", "Blur graphic images"));
        blurImgurImages.setTextColor(textColor());
        blurImgurImages.setTextSize(16);
        Theme.tintCompoundButton(this, blurImgurImages);
        root.addView(blurImgurImages);

        blurVideoThumbnails = new CheckBox(this);
        blurVideoThumbnails.setText(MainActivity.text("\u52d5\u753b\u30b5\u30e0\u30cd\u30a4\u30eb\u3082\u5224\u5b9a\u3057\u3066\u307c\u304b\u3059", "Also check and blur video thumbnails"));
        blurVideoThumbnails.setTextColor(textColor());
        blurVideoThumbnails.setTextSize(16);
        Theme.tintCompoundButton(this, blurVideoThumbnails);
        root.addView(blurVideoThumbnails);

        blurGifThumbnails = new CheckBox(this);
        blurGifThumbnails.setText(MainActivity.text("GIF\u30b5\u30e0\u30cd\u30a4\u30eb\u3082\u5224\u5b9a\u3057\u3066\u307c\u304b\u3059", "Also check and blur GIF thumbnails"));
        blurGifThumbnails.setTextColor(textColor());
        blurGifThumbnails.setTextSize(16);
        Theme.tintCompoundButton(this, blurGifThumbnails);
        root.addView(blurGifThumbnails);

        autoplayGifs = new CheckBox(this);
        autoplayGifs.setText(MainActivity.text("GIF\u3092\u81ea\u52d5\u518d\u751f", "Autoplay GIFs"));
        autoplayGifs.setTextColor(textColor());
        autoplayGifs.setTextSize(16);
        Theme.tintCompoundButton(this, autoplayGifs);
        root.addView(autoplayGifs);

        root.addView(managementRow(R.drawable.ic_close,
                MainActivity.text("NG\u7ba1\u7406", "NG management"),
                MainActivity.text("NGWord\u3001NGName\u3001NGID\u306a\u3069\u3092\u7ba1\u7406", "Manage NGWord, NGName, NGID, and related rules"),
                v -> startActivity(new Intent(this, NgRulesActivity.class))));

        root.addView(sectionTitle(R.drawable.ic_image,
                MainActivity.text("\u753b\u50cf\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9", "Image Uploads"),
                MainActivity.text("ImgBB\u306e\u63a5\u7d9a\u3068\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u5c65\u6b74", "ImgBB connection and upload history")));
        imgbbApiKey = new EditText(this);
        imgbbApiKey.setSingleLine(true);
        imgbbApiKey.setTextSize(14);
        imgbbApiKey.setTextColor(textColor());
        imgbbApiKey.setHintTextColor(hintColor());
        imgbbApiKey.setHint("ImgBB API key");
        imgbbApiKey.setImeOptions(EditorInfo.IME_ACTION_DONE);
        imgbbApiKey.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        imgbbApiKey.setBackground(roundedField());
        imgbbApiKey.setPadding(dp(12), 0, dp(12), 0);
        root.addView(helperText(MainActivity.text("ImgBB\u306b\u753b\u50cf\u3092\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u3059\u308b\u5834\u5408\u306b\u5fc5\u8981", "Required for uploading images to ImgBB.")));
        root.addView(imgbbApiKey, fieldParams());
        root.addView(managementRow(R.drawable.ic_search,
                MainActivity.text("ImgBB API key\u306e\u53d6\u5f97\u65b9\u6cd5", "How to get an ImgBB API key"),
                MainActivity.text("\u753b\u50cf\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u7528\u306eAPI key\u3092\u53d6\u5f97\u3059\u308b\u624b\u9806", "Steps for getting an API key for image uploads"),
                v -> showImgbbApiKeyHelp()));

        saveUploadHistory = new CheckBox(this);
        saveUploadHistory.setText(MainActivity.text("\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u5c65\u6b74\u3092\u4fdd\u5b58", "Save upload history"));
        saveUploadHistory.setTextColor(textColor());
        saveUploadHistory.setTextSize(16);
        Theme.tintCompoundButton(this, saveUploadHistory);
        root.addView(saveUploadHistory);

        root.addView(managementRow(R.drawable.ic_image,
                MainActivity.text("\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u5c65\u6b74\u3092\u7ba1\u7406", "Manage upload history"),
                MainActivity.text("ImgBB\u306b\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u3057\u305f\u753b\u50cf\u3068URL\u3092\u8868\u793a\u30fb\u524a\u9664", "View and delete images and URLs uploaded to ImgBB"),
                v -> startActivity(new Intent(this, UploadHistoryActivity.class))));

        root.addView(sectionTitle(R.drawable.ic_folder,
                MainActivity.text("\u30b9\u30c8\u30ec\u30fc\u30b8", "Storage"),
                MainActivity.text("\u30ad\u30e3\u30c3\u30b7\u30e5\u306e\u4f7f\u7528\u91cf\u3068\u4e0a\u9650", "Cache usage and storage limit")));
        cacheEnabled = new CheckBox(this);
        cacheEnabled.setText(MainActivity.text("\u30ad\u30e3\u30c3\u30b7\u30e5\u3092\u4f7f\u7528", "Use cache"));
        cacheEnabled.setTextColor(textColor());
        cacheEnabled.setTextSize(16);
        Theme.tintCompoundButton(this, cacheEnabled);
        root.addView(cacheEnabled);

        root.addView(helperText(MainActivity.text(
                "\u4e00\u5ea6\u8aad\u307f\u8fbc\u3093\u3060\u30b9\u30ec\u3068\u30e1\u30c7\u30a3\u30a2\u3092\u4fdd\u5b58\u3057\u3001\u518d\u8868\u793a\u3092\u9ad8\u901f\u5316\u3057\u307e\u3059\u3002",
                "Store loaded threads and media for faster reopening.")));

        cacheMaxMb = new EditText(this);
        cacheMaxMb.setSingleLine(true);
        cacheMaxMb.setTextSize(14);
        cacheMaxMb.setTextColor(textColor());
        cacheMaxMb.setHintTextColor(hintColor());
        cacheMaxMb.setHint(MainActivity.text("\u6700\u5927\u30ad\u30e3\u30c3\u30b7\u30e5\u30b5\u30a4\u30ba (MB)", "Maximum cache size (MB)"));
        cacheMaxMb.setImeOptions(EditorInfo.IME_ACTION_DONE);
        cacheMaxMb.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        cacheMaxMb.setBackground(roundedField());
        cacheMaxMb.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout cacheLimitRow = new LinearLayout(this);
        cacheLimitRow.setOrientation(LinearLayout.HORIZONTAL);
        cacheLimitRow.setGravity(Gravity.CENTER_VERTICAL);
        cacheLimitRow.addView(cacheMaxMb, new LinearLayout.LayoutParams(0, dp(44), 1));
        cacheApply = smallActionButton(MainActivity.text("\u9069\u7528", "Apply"));
        cacheApply.setOnClickListener(v -> applyCacheMaxMb());
        LinearLayout.LayoutParams applyParams = new LinearLayout.LayoutParams(dp(86), dp(44));
        applyParams.setMargins(dp(8), 0, 0, 0);
        cacheLimitRow.addView(cacheApply, applyParams);
        root.addView(cacheLimitRow, fieldParams());

        cacheUsageText = helperText("");
        root.addView(cacheUsageText);
        cacheUsage = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        cacheUsage.setMax(1000);
        cacheUsage.setProgress(0);
        cacheUsage.setProgressDrawable(cacheUsageDrawable());
        root.addView(cacheUsage, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(18)));

        LinearLayout clearCacheRow = new LinearLayout(this);
        clearCacheRow.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        clearCacheButton = cacheActionButton();
        clearCacheButton.setOnClickListener(v -> confirmClearCache());
        clearCacheRow.addView(clearCacheButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(44)));
        LinearLayout.LayoutParams clearCacheParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56));
        clearCacheParams.setMargins(0, dp(4), 0, dp(4));
        root.addView(clearCacheRow, clearCacheParams);

        root.addView(sectionTitle(android.R.drawable.ic_menu_recent_history,
                MainActivity.text("\u30d7\u30e9\u30a4\u30d0\u30b7\u30fc\u3068\u5c65\u6b74", "Privacy & History"),
                MainActivity.text("\u4fdd\u5b58\u3059\u308b\u884c\u52d5\u5c65\u6b74\u3068\u305d\u306e\u7ba1\u7406", "Choose and manage saved activity")));

        saveBrowsingHistory = new CheckBox(this);
        saveBrowsingHistory.setText(MainActivity.text("\u95b2\u89a7\u5c65\u6b74\u3092\u4fdd\u5b58", "Save browsing history"));
        saveBrowsingHistory.setTextColor(textColor());
        saveBrowsingHistory.setTextSize(16);
        Theme.tintCompoundButton(this, saveBrowsingHistory);
        root.addView(saveBrowsingHistory);

        root.addView(managementRow(android.R.drawable.ic_menu_recent_history,
                MainActivity.text("\u95b2\u89a7\u5c65\u6b74\u3092\u7ba1\u7406", "Manage browsing history"),
                MainActivity.text("\u4fdd\u5b58\u3055\u308c\u305f\u95b2\u89a7\u5c65\u6b74\u3092\u8868\u793a\u30fb\u524a\u9664", "View and delete saved browsing history"),
                v -> startActivity(new Intent(this, HistoryActivity.class))));

        saveReadHistory = new CheckBox(this);
        saveReadHistory.setText(MainActivity.text("\u65e2\u8aad\u5c65\u6b74\u3092\u4fdd\u5b58", "Save read history"));
        saveReadHistory.setTextColor(textColor());
        saveReadHistory.setTextSize(16);
        Theme.tintCompoundButton(this, saveReadHistory);
        root.addView(saveReadHistory);

        root.addView(managementRow(R.drawable.ic_check,
                MainActivity.text("\u65e2\u8aad\u5c65\u6b74\u3092\u7ba1\u7406", "Manage read history"),
                MainActivity.text("\u30b9\u30ec\u3054\u3068\u306e\u65e2\u8aad\u5c65\u6b74\u3092\u78ba\u8a8d\u30fb\u524a\u9664", "Review and delete saved read history by thread"),
                v -> startActivity(new Intent(this, ReadPostsActivity.class))));

        saveWritePostHistory = new CheckBox(this);
        saveWritePostHistory.setText(MainActivity.text("\u66f8\u304d\u8fbc\u307f\u5c65\u6b74\u3092\u4fdd\u5b58", "Save post history"));
        saveWritePostHistory.setTextColor(textColor());
        saveWritePostHistory.setTextSize(16);
        Theme.tintCompoundButton(this, saveWritePostHistory);
        root.addView(saveWritePostHistory);

        root.addView(managementRow(R.drawable.ic_reply,
                MainActivity.text("\u66f8\u304d\u8fbc\u307f\u5c65\u6b74\u3092\u7ba1\u7406", "Manage post history"),
                MainActivity.text("\u81ea\u5206\u306e\u66f8\u304d\u8fbc\u307f\u306b\u79fb\u52d5\u30fb\u5c65\u6b74\u304b\u3089\u524a\u9664", "Jump to your posts and delete saved post history"),
                v -> startActivity(new Intent(this, WritePostHistoryActivity.class))));

        saveWriteIdentityHistory = new CheckBox(this);
        saveWriteIdentityHistory.setText(MainActivity.text("\u540d\u524d\u30fb\u30e1\u30fc\u30eb\u5c65\u6b74\u3092\u4fdd\u5b58", "Save name/mail history"));
        saveWriteIdentityHistory.setTextColor(textColor());
        saveWriteIdentityHistory.setTextSize(16);
        Theme.tintCompoundButton(this, saveWriteIdentityHistory);
        root.addView(saveWriteIdentityHistory);

        root.addView(managementRow(android.R.drawable.ic_menu_recent_history,
                MainActivity.text("\u540d\u524d\u30fb\u30e1\u30fc\u30eb\u5c65\u6b74\u3092\u7ba1\u7406", "Manage name/mail history"),
                MainActivity.text("\u66f8\u304d\u8fbc\u307f\u306b\u4f7f\u3063\u305f\u540d\u524d\u3068\u30e1\u30fc\u30eb\u306e\u7d44\u307f\u5408\u308f\u305b\u3092\u8868\u793a\u30fb\u524a\u9664", "View and delete saved name/mail pairs"),
                v -> startActivity(new Intent(this, WriteIdentityHistoryActivity.class))));

        root.addView(sectionTitle(android.R.drawable.ic_popup_sync, "Sync2ch",
                MainActivity.text("\u30d6\u30c3\u30af\u30de\u30fc\u30af\u3001\u30bf\u30d6\u3001\u65e2\u8aad\u4f4d\u7f6e\u306e\u540c\u671f", "Sync bookmarks, tabs, and read positions")));
        sync2chEnabled = new CheckBox(this);
        sync2chEnabled.setText(MainActivity.text("Sync2ch\u3092\u4f7f\u7528", "Use Sync2ch"));
        sync2chEnabled.setTextColor(textColor());
        sync2chEnabled.setTextSize(16);
        Theme.tintCompoundButton(this, sync2chEnabled);
        root.addView(sync2chEnabled);

        sync2chId = new EditText(this);
        sync2chId.setSingleLine(true);
        sync2chId.setTextSize(14);
        sync2chId.setTextColor(textColor());
        sync2chId.setHintTextColor(hintColor());
        sync2chId.setHint(MainActivity.text("Sync2ch ID", "Sync2ch ID"));
        sync2chId.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        sync2chId.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        sync2chId.setBackground(roundedField());
        sync2chId.setPadding(dp(12), 0, dp(12), 0);
        root.addView(sync2chId, fieldParams());

        sync2chApiPassword = new EditText(this);
        sync2chApiPassword.setSingleLine(true);
        sync2chApiPassword.setTextSize(14);
        sync2chApiPassword.setTextColor(textColor());
        sync2chApiPassword.setHintTextColor(hintColor());
        sync2chApiPassword.setHint(MainActivity.text("API\u63a5\u7d9a\u7528\u30d1\u30b9\u30ef\u30fc\u30c9", "API connection password"));
        sync2chApiPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
        sync2chApiPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        sync2chApiPassword.setBackground(roundedField());
        sync2chApiPassword.setPadding(dp(12), 0, dp(12), 0);
        root.addView(sync2chApiPassword, fieldParams());

        root.addView(helperText(MainActivity.text(
                "Sync2ch\u306e\u30a2\u30ab\u30a6\u30f3\u30c8\u60c5\u5831\u306b\u8868\u793a\u3055\u308c\u308b\u300cAPI\u63a5\u7d9a\u7528\u30d1\u30b9\u30ef\u30fc\u30c9\u300d\u3092\u4f7f\u3044\u307e\u3059\u3002\u30ed\u30b0\u30a4\u30f3\u7528\u30d1\u30b9\u30ef\u30fc\u30c9\u3068\u306f\u5225\u3067\u3059\u3002\u30d6\u30c3\u30af\u30de\u30fc\u30af\u3001\u901a\u5e38\u30bf\u30d6\u3001\u65e2\u8aad\u4f4d\u7f6e\u3092\u540c\u671f\u3057\u307e\u3059\u3002",
                "Use the API connection password shown on your Sync2ch account page. It is different from your login password. Bookmarks, normal tabs, and read positions are synced.")));

        root.addView(managementRow(android.R.drawable.ic_menu_help,
                MainActivity.text("Sync2ch\u30a2\u30ab\u30a6\u30f3\u30c8\u60c5\u5831", "Sync2ch account information"),
                MainActivity.text("API\u63a5\u7d9a\u7528\u30d1\u30b9\u30ef\u30fc\u30c9\u306e\u78ba\u8a8d\u5148\u3092\u958b\u304f", "Open the page where the API connection password is shown"),
                v -> openUrl("https://sync2ch.com/user")));

        root.addView(managementRow(android.R.drawable.ic_popup_sync,
                MainActivity.text("Sync2ch\u3067\u540c\u671f", "Sync with Sync2ch"),
                MainActivity.text("\u30d6\u30c3\u30af\u30de\u30fc\u30af\u3001\u901a\u5e38\u30bf\u30d6\u3001\u65e2\u8aad\u4f4d\u7f6e\u3092\u30de\u30fc\u30b8", "Merge bookmarks, normal tabs, and read positions"),
                v -> runSync2chNow()));

        root.addView(sectionTitle(R.drawable.ic_download,
                MainActivity.text("\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u3068\u79fb\u884c", "Backup & Migration"),
                MainActivity.text("\u30c7\u30fc\u30bf\u306e\u4fdd\u5b58\u3001\u5fa9\u5143\u3001ChMate\u304b\u3089\u306e\u79fb\u884c", "Save, restore, or migrate data from ChMate")));
        root.addView(managementRow(android.R.drawable.ic_menu_save,
                MainActivity.text("CuspiDroid\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u3092\u4f5c\u6210", "Create CuspiDroid backup"),
                MainActivity.text("\u8a2d\u5b9a\u3001\u30bf\u30d6\u3001\u30d6\u30c3\u30af\u30de\u30fc\u30af\u3001\u5c65\u6b74\u3001\u65e2\u8aad\u4f4d\u7f6e\u3001\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u5c65\u6b74\u3092zip\u306b\u4fdd\u5b58", "Save settings, tabs, bookmarks, history, read positions, and upload history to a zip"),
                v -> createCuspiDroidBackup()));

        root.addView(managementRow(android.R.drawable.ic_menu_upload,
                MainActivity.text("CuspiDroid\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u304b\u3089\u5fa9\u5143", "Restore CuspiDroid backup"),
                MainActivity.text("CuspiDroid\u306e\u30d0\u30c3\u30af\u30a2\u30c3\u30d7zip\u304b\u3089\u8a2d\u5b9a\u3068\u30c7\u30fc\u30bf\u3092\u5fa9\u5143", "Restore settings and data from a CuspiDroid backup zip"),
                v -> confirmChooseCuspiDroidBackup()));

        root.addView(managementRow(android.R.drawable.ic_menu_upload,
                MainActivity.text("ChMate\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u304b\u3089\u5fa9\u5143", "Restore from ChMate backup"),
                MainActivity.text("ChMate\u306e\u30d0\u30c3\u30af\u30a2\u30c3\u30d7zip\u304b\u3089\u30b9\u30ec\u60c5\u5831\u3092\u30de\u30fc\u30b8", "Merge thread data from a ChMate backup zip"),
                v -> showChMateRestoreHelp()));

        root.addView(sectionTitle(R.drawable.ic_settings,
                MainActivity.text("\u8a73\u7d30\u8a2d\u5b9a", "Advanced"),
                MainActivity.text("\u30c7\u30d0\u30c3\u30b0\u3068\u8a2d\u5b9a\u306e\u521d\u671f\u5316", "Diagnostics and resetting preferences")));
        root.addView(managementRow(android.R.drawable.ic_dialog_info,
                MainActivity.text("\u30c7\u30d0\u30c3\u30b0\u8a2d\u5b9a", "Debug settings"),
                MainActivity.text("\u8abf\u67fb\u7528\u306e\u8868\u793a\u3092\u5207\u308a\u66ff\u3048", "Toggle diagnostic displays"),
                v -> startActivity(new Intent(this, DebugSettingsActivity.class))));

        root.addView(managementRow(android.R.drawable.ic_menu_revert,
                MainActivity.text("\u8a2d\u5b9a\u3092\u30c7\u30d5\u30a9\u30eb\u30c8\u306b\u623b\u3059", "Reset all settings"),
                MainActivity.text("\u8868\u793a\u3001\u691c\u7d22\u3001\u30b8\u30a7\u30b9\u30c1\u30e3\u30fc\u306a\u3069\u306e\u8a2d\u5b9a\u3092\u521d\u671f\u5024\u306b\u623b\u3059", "Restore display, search, gesture, and related settings"),
                v -> confirmResetDefaults()));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHMATE_DATABASE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            restoreChMateBackup(data.getData());
        } else if (requestCode == REQUEST_CUSPIDROID_BACKUP_CREATE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            exportCuspiDroidBackup(data.getData());
        } else if (requestCode == REQUEST_CUSPIDROID_BACKUP_RESTORE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            confirmRestoreCuspiDroidBackup(data.getData());
        }
    }

    private void loadSettings() {
        open5chInNewTab.setChecked(preferences.getBoolean(MainActivity.PREF_5CH_NEW_TAB, true));
        externalLinkInApp.setChecked(preferences.getBoolean(MainActivity.PREF_EXTERNAL_LINK_IN_APP, false));
        showMediaPreviews.setChecked(preferences.getBoolean(MainActivity.PREF_SHOW_MEDIA, true));
        blurImgurImages.setChecked(preferences.getBoolean(MainActivity.PREF_BLUR_IMGUR, true));
        blurVideoThumbnails.setChecked(preferences.getBoolean(MainActivity.PREF_BLUR_VIDEO_THUMBNAILS, true));
        blurGifThumbnails.setChecked(preferences.getBoolean(MainActivity.PREF_BLUR_GIF_THUMBNAILS, true));
        autoplayGifs.setChecked(preferences.getBoolean(MainActivity.PREF_AUTOPLAY_GIFS, false));
        imgbbApiKey.setText(preferences.getString(MainActivity.PREF_IMGBB_API_KEY, ""));
        saveUploadHistory.setChecked(preferences.getBoolean(MainActivity.PREF_SAVE_UPLOAD_HISTORY, true));
        updateMediaDependentSettings();
        if (preferences.getBoolean(MainActivity.PREF_ADDRESS_BAR_TOP, false)) {
            addressBarTop.setChecked(true);
        } else {
            addressBarBottom.setChecked(true);
        }
        hideBarsOnScroll.setChecked(preferences.getBoolean(MainActivity.PREF_HIDE_BARS_ON_SCROLL, false));
        titleBarTabSwipe.setChecked(preferences.getBoolean(MainActivity.PREF_TITLE_BAR_TAB_SWIPE, true));
        treeView.setChecked(preferences.getBoolean(MainActivity.PREF_TREE_VIEW, true));
        treeSkipFirstReply.setChecked(preferences.getBoolean(MainActivity.PREF_TREE_SKIP_FIRST_REPLY, false));
        autoScrollUnread.setChecked(preferences.getBoolean(MainActivity.PREF_AUTO_SCROLL_UNREAD, true));
        markExistingReadOnThreadUpdate.setChecked(preferences.getBoolean(MainActivity.PREF_MARK_EXISTING_READ_ON_THREAD_UPDATE, true));
        colorUnreadPosts.setChecked(preferences.getBoolean(MainActivity.PREF_COLOR_UNREAD_POSTS, true));
        omitCopyPaste.setChecked(preferences.getBoolean(MainActivity.PREF_OMIT_COPYPASTE, false));
        autoAa.setChecked(preferences.getBoolean(MainActivity.PREF_AUTO_AA, true));
        popularReplyThreshold.setText(String.valueOf(preferences.getInt(MainActivity.PREF_POPULAR_REPLY_THRESHOLD, 3)));
        updateTreeDependentSettings();
        cacheEnabled.setChecked(preferences.getBoolean(MainActivity.PREF_CACHE_ENABLED, true));
        showBookmarksInTabOverview.setChecked(preferences.getBoolean(MainActivity.PREF_SHOW_BOOKMARKS_IN_TAB_OVERVIEW, true));
        showHistoryOnHome.setChecked(preferences.getBoolean(MainActivity.PREF_SHOW_HISTORY_ON_HOME, true));
        showHomeBookmarkUnreadBadges.setChecked(preferences.getBoolean(MainActivity.PREF_HOME_BOOKMARK_UNREAD_BADGES, true));
        boolean legacyDisabled = preferences.getBoolean(MainActivity.PREF_DISABLE_HISTORY, false);
        saveBrowsingHistory.setChecked(!legacyDisabled
                && preferences.getBoolean(MainActivity.PREF_SAVE_BROWSING_HISTORY, true));
        saveReadHistory.setChecked(!legacyDisabled
                && preferences.getBoolean(MainActivity.PREF_SAVE_READ_HISTORY, true));
        saveWritePostHistory.setChecked(!legacyDisabled
                && preferences.getBoolean(MainActivity.PREF_SAVE_WRITE_POST_HISTORY, true));
        saveWriteIdentityHistory.setChecked(preferences.getBoolean(MainActivity.PREF_SAVE_WRITE_IDENTITY_HISTORY, true));
        sync2chEnabled.setChecked(preferences.getBoolean(MainActivity.PREF_SYNC2CH_ENABLED, false));
        sync2chId.setText(preferences.getString(MainActivity.PREF_SYNC2CH_ID, ""));
        sync2chApiPassword.setText(preferences.getString(MainActivity.PREF_SYNC2CH_API_PASSWORD, ""));
        cacheMaxMb.setText(String.valueOf(preferences.getInt(MainActivity.PREF_CACHE_MAX_MB, AppCache.DEFAULT_MAX_MB)));
        updateCacheDependentSettings();
        updateCacheUsage();
        String themeMode = preferences.getString(MainActivity.PREF_THEME_MODE, Theme.MODE_SYSTEM);
        if (Theme.MODE_DARK.equals(themeMode)) {
            themeDark.setChecked(true);
        } else if (Theme.MODE_LIGHT.equals(themeMode)) {
            themeLight.setChecked(true);
        } else {
            themeSystem.setChecked(true);
        }

        String template = preferences.getString(MainActivity.PREF_SEARCH_TEMPLATE, MainActivity.DEFAULT_SEARCH_TEMPLATE);
        customTemplate.setText(template);
        if (MainActivity.DEFAULT_SEARCH_TEMPLATE.equals(template)
                || MainActivity.LEGACY_FIND_IO_TEMPLATE.equals(template)
                || MainActivity.FIND_NET_TEMPLATE.equals(template)) {
            searchFind5chIo.setChecked(true);
        } else {
            searchCustom.setChecked(true);
        }
    }

    private void setupAutoSave() {
        open5chInNewTab.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        externalLinkInApp.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        showMediaPreviews.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateMediaDependentSettings();
            saveSettings(false);
        });
        blurImgurImages.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateMediaDependentSettings();
            saveSettings(false);
        });
        blurVideoThumbnails.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        blurGifThumbnails.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        autoplayGifs.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        imgbbApiKey.setOnEditorActionListener((v, actionId, event) -> {
            saveSettings(false);
            return false;
        });
        imgbbApiKey.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveSettings(false);
            }
        });
        sync2chId.setOnEditorActionListener((v, actionId, event) -> {
            saveSettings(false);
            return false;
        });
        sync2chId.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveSettings(false);
            }
        });
        sync2chApiPassword.setOnEditorActionListener((v, actionId, event) -> {
            saveSettings(false);
            return false;
        });
        sync2chApiPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveSettings(false);
            }
        });
        addressBarTop.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        addressBarBottom.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        hideBarsOnScroll.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        titleBarTabSwipe.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        treeView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTreeDependentSettings();
            saveSettings(false);
        });
        treeSkipFirstReply.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        autoScrollUnread.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        markExistingReadOnThreadUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        colorUnreadPosts.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        omitCopyPaste.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        autoAa.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        popularReplyThreshold.setOnEditorActionListener((v, actionId, event) -> {
            applyPopularReplyThreshold();
            return false;
        });
        popularReplyThreshold.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                applyPopularReplyThreshold();
            }
        });
        showBookmarksInTabOverview.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        showHistoryOnHome.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        showHomeBookmarkUnreadBadges.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        saveBrowsingHistory.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        saveReadHistory.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        saveWritePostHistory.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        saveWriteIdentityHistory.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        saveUploadHistory.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        sync2chEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        cacheEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateCacheDependentSettings();
            saveSettings(false);
        });
        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            saveThemeMode();
            group.post(this::recreate);
        });
        searchFind5chIo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                saveSettings(false);
            }
        });
        searchCustom.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                saveSettings(false);
            }
        });
        customTemplate.setOnEditorActionListener((v, actionId, event) -> {
            saveSettings(true);
            return false;
        });
        customTemplate.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveSettings(true);
            }
        });
        customTemplate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchCustom.isChecked() && s.toString().trim().contains("%s")) {
                    saveSettings(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private int popularReplyThresholdValue() {
        try {
            int value = Integer.parseInt(popularReplyThreshold.getText().toString().trim());
            return Math.max(1, value);
        } catch (Exception ignored) {
            return 3;
        }
    }

    private void applyPopularReplyThreshold() {
        int value = popularReplyThresholdValue();
        preferences.edit().putInt(MainActivity.PREF_POPULAR_REPLY_THRESHOLD, value).apply();
        popularReplyThreshold.setText(String.valueOf(value));
    }

    private void saveSettings(boolean showError) {
        String template;
        if (searchFind5chIo.isChecked()) {
            template = MainActivity.DEFAULT_SEARCH_TEMPLATE;
        } else {
            template = customTemplate.getText().toString().trim();
            if (template.isEmpty() || !template.contains("%s")) {
                if (showError) {
                    Toast.makeText(this, MainActivity.text("%s \u3092\u542b\u3080\u691c\u7d22URL\u30c6\u30f3\u30d7\u30ec\u30fc\u30c8\u3092\u5165\u529b", "Enter a search URL template containing %s."), Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }

        String themeMode = Theme.MODE_SYSTEM;
        if (themeLight.isChecked()) {
            themeMode = Theme.MODE_LIGHT;
        } else if (themeDark.isChecked()) {
            themeMode = Theme.MODE_DARK;
        }

        preferences.edit()
                .putBoolean(MainActivity.PREF_5CH_NEW_TAB, open5chInNewTab.isChecked())
                .putBoolean(MainActivity.PREF_EXTERNAL_LINK_IN_APP, externalLinkInApp.isChecked())
                .putBoolean(MainActivity.PREF_SHOW_MEDIA, showMediaPreviews.isChecked())
                .putBoolean(MainActivity.PREF_BLUR_IMGUR, blurImgurImages.isChecked())
                .putBoolean(MainActivity.PREF_BLUR_VIDEO_THUMBNAILS,
                        blurImgurImages.isChecked() && blurVideoThumbnails.isChecked())
                .putBoolean(MainActivity.PREF_BLUR_GIF_THUMBNAILS,
                        blurImgurImages.isChecked() && blurGifThumbnails.isChecked())
                .putBoolean(MainActivity.PREF_AUTOPLAY_GIFS, autoplayGifs.isChecked())
                .putString(MainActivity.PREF_IMGBB_API_KEY, imgbbApiKey.getText().toString().trim())
                .putBoolean(MainActivity.PREF_ADDRESS_BAR_TOP, addressBarTop.isChecked())
                .putBoolean(MainActivity.PREF_HIDE_BARS_ON_SCROLL, hideBarsOnScroll.isChecked())
                .putBoolean(MainActivity.PREF_TITLE_BAR_TAB_SWIPE, titleBarTabSwipe.isChecked())
                .putBoolean(MainActivity.PREF_TREE_VIEW, treeView.isChecked())
                .putBoolean(MainActivity.PREF_TREE_SKIP_FIRST_REPLY,
                        treeView.isChecked() && treeSkipFirstReply.isChecked())
                .putBoolean(MainActivity.PREF_AUTO_SCROLL_UNREAD, autoScrollUnread.isChecked())
                .putBoolean(MainActivity.PREF_MARK_EXISTING_READ_ON_THREAD_UPDATE, markExistingReadOnThreadUpdate.isChecked())
                .putBoolean(MainActivity.PREF_COLOR_UNREAD_POSTS, colorUnreadPosts.isChecked())
                .putBoolean(MainActivity.PREF_OMIT_COPYPASTE, omitCopyPaste.isChecked())
                .putBoolean(MainActivity.PREF_AUTO_AA, autoAa.isChecked())
                .putInt(MainActivity.PREF_POPULAR_REPLY_THRESHOLD, popularReplyThresholdValue())
                .putBoolean(MainActivity.PREF_SHOW_BOOKMARKS_IN_TAB_OVERVIEW, showBookmarksInTabOverview.isChecked())
                .putBoolean(MainActivity.PREF_SHOW_HISTORY_ON_HOME, showHistoryOnHome.isChecked())
                .putBoolean(MainActivity.PREF_HOME_BOOKMARK_UNREAD_BADGES, showHomeBookmarkUnreadBadges.isChecked())
                .putBoolean(MainActivity.PREF_DISABLE_HISTORY, false)
                .putBoolean(MainActivity.PREF_SAVE_BROWSING_HISTORY, saveBrowsingHistory.isChecked())
                .putBoolean(MainActivity.PREF_SAVE_READ_HISTORY, saveReadHistory.isChecked())
                .putBoolean(MainActivity.PREF_SAVE_WRITE_POST_HISTORY, saveWritePostHistory.isChecked())
                .putBoolean(MainActivity.PREF_SAVE_WRITE_IDENTITY_HISTORY, saveWriteIdentityHistory.isChecked())
                .putBoolean(MainActivity.PREF_SAVE_UPLOAD_HISTORY, saveUploadHistory.isChecked())
                .putBoolean(MainActivity.PREF_SYNC2CH_ENABLED, sync2chEnabled.isChecked())
                .putString(MainActivity.PREF_SYNC2CH_ID, sync2chId.getText().toString().trim())
                .putString(MainActivity.PREF_SYNC2CH_API_PASSWORD, sync2chApiPassword.getText().toString().trim())
                .putBoolean(MainActivity.PREF_CACHE_ENABLED, cacheEnabled.isChecked())
                .putString(MainActivity.PREF_THEME_MODE, themeMode)
                .putString(MainActivity.PREF_SEARCH_TEMPLATE, template)
                .apply();
    }

    private void saveThemeMode() {
        String themeMode = Theme.MODE_SYSTEM;
        if (themeLight.isChecked()) {
            themeMode = Theme.MODE_LIGHT;
        } else if (themeDark.isChecked()) {
            themeMode = Theme.MODE_DARK;
        }
        preferences.edit().putString(MainActivity.PREF_THEME_MODE, themeMode).apply();
    }

    private void confirmResetDefaults() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("\u8a2d\u5b9a\u3092\u30c7\u30d5\u30a9\u30eb\u30c8\u306b\u623b\u3059", "Reset all settings"))
                .setMessage(MainActivity.text(
                        "\u8868\u793a\u3001\u64cd\u4f5c\u3001\u691c\u7d22\u3001\u30b8\u30a7\u30b9\u30c1\u30e3\u30fc\u306a\u3069\u306e\u8a2d\u5b9a\u3092\u521d\u671f\u5024\u306b\u623b\u3057\u307e\u3059\u3002BBS\u30ea\u30f3\u30af\u3001NG\u7ba1\u7406\u3001\u5c65\u6b74\u3001\u65e2\u8aad\u4f4d\u7f6e\u3001\u30d6\u30c3\u30af\u30de\u30fc\u30af\u3001\u81ea\u5206\u306e\u66f8\u304d\u8fbc\u307f\u60c5\u5831\u306f\u6b8b\u308a\u307e\u3059\u3002",
                        "Restore display, controls, search, gesture, and related settings. BBS links, NG rules, history, read positions, bookmarks, and your post markers are kept."))
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u623b\u3059", "Reset"), (d, which) -> resetSettingsDefaults())
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private void showImgbbApiKeyHelp() {
        String message = MainActivity.text(
                "1. ImgBB\u306b\u30ed\u30b0\u30a4\u30f3\u3057\u307e\u3059\u3002\n"
                        + "2. https://api.imgbb.com/ \u3092\u958b\u304d\u307e\u3059\u3002\n"
                        + "3. \u8868\u793a\u3055\u308c\u305f API key \u3092\u30b3\u30d4\u30fc\u3057\u3001\u3053\u3053\u306b\u5165\u529b\u3057\u307e\u3059\u3002",
                "1. Sign in to ImgBB.\n"
                        + "2. Open https://api.imgbb.com/.\n"
                        + "3. Copy the displayed API key and enter it here.");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("ImgBB API key\u306e\u53d6\u5f97\u65b9\u6cd5", "How to get an ImgBB API key"))
                .setMessage(message)
                .setNegativeButton(MainActivity.text("\u9589\u3058\u308b", "Close"), null)
                .setPositiveButton(MainActivity.text("ImgBB API\u3092\u958b\u304f", "Open ImgBB API"),
                        (d, which) -> openUrl("https://api.imgbb.com/"))
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void runSync2chNow() {
        saveSettings(false);
        Toast.makeText(this, MainActivity.text("Sync2ch\u3067\u540c\u671f\u4e2d", "Syncing with Sync2ch..."), Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                Sync2chClient.Result result = Sync2chClient.sync(getApplicationContext(), preferences);
                runOnUiThread(() -> Toast.makeText(this,
                        MainActivity.text("Sync2ch\u540c\u671f\u5b8c\u4e86", "Sync2ch sync complete")
                                + "\n" + MainActivity.text("\u8ffd\u52a0\u30bf\u30d6: ", "Added tabs: ")
                                + result.addedOpenThreads
                                + "  " + MainActivity.text("\u8ffd\u52a0\u30d6\u30c3\u30af\u30de\u30fc\u30af: ", "Added bookmarks: ")
                                + result.addedBookmarks,
                        Toast.LENGTH_LONG).show());
            } catch (Exception error) {
                runOnUiThread(() -> Toast.makeText(this,
                        MainActivity.text("Sync2ch\u540c\u671f\u5931\u6557: ", "Sync2ch sync failed: ")
                                + error.getMessage(), Toast.LENGTH_LONG).show());
            }
        }, "CuspiDroid-Sync2ch").start();
    }

    private void createCuspiDroidBackup() {
        saveSettings(false);
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_TITLE, "cuspidroid-backup-" + backupTimestamp() + ".zip");
        startActivityForResult(intent, REQUEST_CUSPIDROID_BACKUP_CREATE);
    }

    private void exportCuspiDroidBackup(Uri uri) {
        Toast.makeText(this, MainActivity.text("CuspiDroid\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u3092\u4f5c\u6210\u4e2d", "Creating CuspiDroid backup..."), Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                CuspiDroidBackup.exportBackup(getApplicationContext(), preferences, uri);
                runOnUiThread(() -> Toast.makeText(this,
                        MainActivity.text("CuspiDroid\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u3092\u4f5c\u6210\u3057\u307e\u3057\u305f", "CuspiDroid backup created"),
                        Toast.LENGTH_LONG).show());
            } catch (Exception error) {
                runOnUiThread(() -> Toast.makeText(this,
                        MainActivity.text("CuspiDroid\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u4f5c\u6210\u5931\u6557: ", "CuspiDroid backup failed: ")
                                + error.getMessage(), Toast.LENGTH_LONG).show());
            }
        }, "CuspiDroid-BackupExport").start();
    }

    private void confirmChooseCuspiDroidBackup() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("CuspiDroid\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u304b\u3089\u5fa9\u5143", "Restore CuspiDroid backup"))
                .setMessage(MainActivity.text(
                        "CuspiDroid\u306e\u30d0\u30c3\u30af\u30a2\u30c3\u30d7zip\u3092\u9078\u629e\u3057\u307e\u3059\u3002\u5fa9\u5143\u6642\u306b\u73fe\u5728\u306e\u8a2d\u5b9a\u3001\u30bf\u30d6\u3001\u30d6\u30c3\u30af\u30de\u30fc\u30af\u3001\u5c65\u6b74\u3001\u65e2\u8aad\u4f4d\u7f6e\u306f\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u5185\u5bb9\u3067\u7f6e\u304d\u63db\u308f\u308a\u307e\u3059\u3002",
                        "Choose a CuspiDroid backup zip. Restoring replaces the current settings, tabs, bookmarks, history, and read positions with the backup contents."))
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u9078\u629e", "Choose"), (d, which) -> openCuspiDroidBackupPicker())
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private void openCuspiDroidBackupPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CUSPIDROID_BACKUP_RESTORE);
    }

    private void confirmRestoreCuspiDroidBackup(Uri uri) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("\u5fa9\u5143\u3057\u307e\u3059\u304b\uff1f", "Restore backup?"))
                .setMessage(MainActivity.text(
                        "\u73fe\u5728\u306eCuspiDroid\u306e\u30c7\u30fc\u30bf\u306f\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u306e\u5185\u5bb9\u3067\u7f6e\u304d\u63db\u308f\u308a\u307e\u3059\u3002",
                        "Current CuspiDroid data will be replaced with the backup contents."))
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u5fa9\u5143", "Restore"), (d, which) -> restoreCuspiDroidBackup(uri))
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private void restoreCuspiDroidBackup(Uri uri) {
        Toast.makeText(this, MainActivity.text("CuspiDroid\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u3092\u5fa9\u5143\u4e2d", "Restoring CuspiDroid backup..."), Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                CuspiDroidBackup.Result result = CuspiDroidBackup.importBackup(getApplicationContext(), preferences, uri);
                preferences.edit().putLong(MainActivity.PREF_LOCAL_BACKUP_RESTORED_AT, System.currentTimeMillis()).apply();
                runOnUiThread(() -> {
                    loadSettings();
                    updateMediaDependentSettings();
                    updateTreeDependentSettings();
                    updateCacheDependentSettings();
                    updateCacheUsage();
                    Toast.makeText(this,
                            MainActivity.text("CuspiDroid\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u3092\u5fa9\u5143\u3057\u307e\u3057\u305f", "CuspiDroid backup restored")
                                    + "\n" + MainActivity.text("\u5fa9\u5143\u9805\u76ee: ", "Restored entries: ")
                                    + result.restoredPreferences,
                            Toast.LENGTH_LONG).show();
                });
            } catch (Exception error) {
                runOnUiThread(() -> Toast.makeText(this,
                        MainActivity.text("CuspiDroid\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u5fa9\u5143\u5931\u6557: ", "CuspiDroid restore failed: ")
                                + error.getMessage(), Toast.LENGTH_LONG).show());
            }
        }, "CuspiDroid-BackupRestore").start();
    }

    private String backupTimestamp() {
        return new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
                .format(new java.util.Date());
    }

    private void showChMateRestoreHelp() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("ChMate\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u304b\u3089\u5fa9\u5143", "Restore from ChMate backup"))
                .setMessage(MainActivity.text(
                        "ChMate\u306e\u30d0\u30c3\u30af\u30a2\u30c3\u30d7zip\u30d5\u30a1\u30a4\u30eb\u3092\u9078\u629e\u3057\u3066\u304f\u3060\u3055\u3044\u3002zip\u5185\u306e databases/roidon.sqlite\u3001dat\u30d5\u30a1\u30a4\u30eb\u3001ng/*.json \u3092\u8aad\u307f\u53d6\u308a\u307e\u3059\u3002\n\n"
                                + "\u8a2d\u5b9a\u306f\u5fa9\u5143\u305b\u305a\u3001\u30d6\u30c3\u30af\u30de\u30fc\u30af\u3001\u5c65\u6b74\u3001\u65e2\u8aad\u4f4d\u7f6e\u3001NG\u3001files/postDataList.json \u306e\u66f8\u304d\u8fbc\u307f\u5c65\u6b74\u3092\u73fe\u5728\u306e\u30c7\u30fc\u30bf\u306b\u8ffd\u52a0\u30fb\u30de\u30fc\u30b8\u3057\u307e\u3059\u3002",
                        "Select the ChMate backup zip file. databases/roidon.sqlite, dat files, and ng/*.json inside the zip are read.\n\n"
                                + "Settings are not restored. Bookmarks, history, read positions, NG rules, and post history from files/postDataList.json are added and merged into the current data."))
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u9078\u629e", "Choose"), (d, which) -> openChMateDatabasePicker())
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private void openChMateDatabasePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CHMATE_DATABASE);
    }

    private void restoreChMateBackup(Uri uri) {
        Toast.makeText(this, MainActivity.text("ChMate\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u3092\u5fa9\u5143\u4e2d", "Restoring ChMate backup..."), Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                ChMateBackupImporter.Result result = ChMateBackupImporter.importBackup(getApplicationContext(), preferences, uri);
                runOnUiThread(() -> Toast.makeText(this,
                        MainActivity.text("ChMate\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u3092\u5fa9\u5143\u3057\u307e\u3057\u305f", "ChMate backup restored")
                                + "\n" + MainActivity.text("\u30d6\u30c3\u30af\u30de\u30fc\u30af: ", "Bookmarks: ") + result.addedBookmarks
                                + "  " + MainActivity.text("\u5c65\u6b74: ", "History: ") + result.addedHistory
                                + "\n" + MainActivity.text("\u65e2\u8aad\u66f4\u65b0: ", "Read positions: ") + result.updatedReadPositions
                                + "  " + MainActivity.text("\u30bf\u30d6: ", "Tabs: ") + result.addedTabs
                                + "\n" + MainActivity.text("\u66f8\u304d\u8fbc\u307f\u5c65\u6b74: ", "Post history: ") + result.addedPostHistory
                                + "  " + MainActivity.text("NG: ", "NG: ") + result.addedNgRules
                                + "\n" + MainActivity.text("\u5fa9\u5143\u3067\u304d\u306a\u304b\u3063\u305f\u30b9\u30ec: ", "Skipped threads: ") + result.skippedThreads,
                        Toast.LENGTH_LONG).show());
            } catch (Exception error) {
                runOnUiThread(() -> Toast.makeText(this,
                        MainActivity.text("ChMate\u30d0\u30c3\u30af\u30a2\u30c3\u30d7\u5fa9\u5143\u5931\u6557: ", "ChMate restore failed: ")
                                + error.getMessage(), Toast.LENGTH_LONG).show());
            }
        }, "CuspiDroid-ChMateRestore").start();
    }

    private void resetSettingsDefaults() {
        SharedPreferences.Editor editor = preferences.edit()
                .putBoolean(MainActivity.PREF_5CH_NEW_TAB, true)
                .putString(MainActivity.PREF_SEARCH_TEMPLATE, MainActivity.DEFAULT_SEARCH_TEMPLATE)
                .putBoolean(MainActivity.PREF_SHOW_MEDIA, true)
                .putBoolean(MainActivity.PREF_BLUR_IMGUR, true)
                .putBoolean(MainActivity.PREF_BLUR_VIDEO_THUMBNAILS, true)
                .putBoolean(MainActivity.PREF_BLUR_GIF_THUMBNAILS, true)
                .putBoolean(MainActivity.PREF_AUTOPLAY_GIFS, false)
                .putString(MainActivity.PREF_IMGBB_API_KEY, "")
                .putBoolean(MainActivity.PREF_ADDRESS_BAR_TOP, false)
                .putBoolean(MainActivity.PREF_HIDE_BARS_ON_SCROLL, false)
                .putBoolean(MainActivity.PREF_TITLE_BAR_TAB_SWIPE, true)
                .putString(MainActivity.PREF_ADDRESS_BAR_BUTTONS, MainActivity.DEFAULT_ADDRESS_BAR_BUTTONS)
                .putString(MainActivity.PREF_ADDRESS_MENU_BUTTONS, MainActivity.DEFAULT_ADDRESS_MENU_BUTTONS)
                .putString(MainActivity.PREF_ADDRESS_NAV_BUTTONS, MainActivity.DEFAULT_ADDRESS_NAV_BUTTONS)
                .putString(MainActivity.PREF_THREAD_TITLE_BAR_BUTTONS, MainActivity.DEFAULT_THREAD_TITLE_BAR_BUTTONS)
                .putString(MainActivity.PREF_THREAD_TITLE_MENU_BUTTONS, MainActivity.DEFAULT_THREAD_TITLE_MENU_BUTTONS)
                .putBoolean(MainActivity.PREF_POPULAR_BUTTON_MIGRATED, true)
                .putBoolean(MainActivity.PREF_TREE_VIEW, true)
                .putBoolean(MainActivity.PREF_TREE_SKIP_FIRST_REPLY, false)
                .putBoolean(MainActivity.PREF_AUTO_SCROLL_UNREAD, true)
                .putBoolean(MainActivity.PREF_MARK_EXISTING_READ_ON_THREAD_UPDATE, true)
                .putBoolean(MainActivity.PREF_COLOR_UNREAD_POSTS, true)
                .putBoolean(MainActivity.PREF_OMIT_COPYPASTE, false)
                .putBoolean(MainActivity.PREF_AUTO_AA, true)
                .putInt(MainActivity.PREF_POPULAR_REPLY_THRESHOLD, 3)
                .putBoolean(MainActivity.PREF_AA_DEBUG, false)
                .putBoolean(MainActivity.PREF_EXTERNAL_LINK_IN_APP, false)
                .putString(MainActivity.PREF_THEME_MODE, Theme.MODE_SYSTEM)
                .putBoolean(MainActivity.PREF_BOARD_SORT_BY_SPEED, true)
                .putBoolean(MainActivity.PREF_BOARD_SHOW_BOARD_NAME, false)
                .putBoolean(MainActivity.PREF_BOARD_SHOW_RESPONSES, true)
                .putBoolean(MainActivity.PREF_BOARD_SHOW_VELOCITY, true)
                .putBoolean(MainActivity.PREF_BOARD_SHOW_ORDER, true)
                .putBoolean(MainActivity.PREF_BOARD_SHOW_CREATED, true)
                .putBoolean(MainActivity.PREF_BOARD_SHOW_UNREAD, true)
                .putString(MainActivity.PREF_BOARD_THREAD_SORT_KEY, MainActivity.BOARD_SORT_VELOCITY)
                .putBoolean(MainActivity.PREF_BOARD_THREAD_SORT_DESC, true)
                .putBoolean(MainActivity.PREF_TAB_SHOW_BOARD_NAME, true)
                .putBoolean(MainActivity.PREF_TAB_SHOW_RESPONSES, true)
                .putBoolean(MainActivity.PREF_TAB_SHOW_VELOCITY, true)
                .putBoolean(MainActivity.PREF_TAB_SHOW_ORDER, false)
                .putBoolean(MainActivity.PREF_TAB_SHOW_CREATED, false)
                .putBoolean(MainActivity.PREF_TAB_SHOW_UNREAD, true)
                .putBoolean(MainActivity.PREF_TAB_SORT_ENABLED, false)
                .putBoolean(MainActivity.PREF_BOOKMARK_SORT_ENABLED, false)
                .putString(MainActivity.PREF_TAB_SORT_KEY, MainActivity.BOARD_SORT_VELOCITY)
                .putBoolean(MainActivity.PREF_TAB_SORT_DESC, true)
                .putBoolean(MainActivity.PREF_TAB_NON_THREAD_TOP, true)
                .putBoolean(MainActivity.PREF_SHOW_BOOKMARKS_IN_TAB_OVERVIEW, true)
                .putBoolean(MainActivity.PREF_SHOW_HISTORY_ON_HOME, true)
                .putBoolean(MainActivity.PREF_HOME_BOOKMARK_UNREAD_BADGES, true)
                .putBoolean(MainActivity.PREF_DISABLE_HISTORY, false)
                .putBoolean(MainActivity.PREF_SAVE_BROWSING_HISTORY, true)
                .putBoolean(MainActivity.PREF_SAVE_READ_HISTORY, true)
                .putBoolean(MainActivity.PREF_SAVE_WRITE_POST_HISTORY, true)
                .putBoolean(MainActivity.PREF_SAVE_WRITE_IDENTITY_HISTORY, true)
                .putBoolean(MainActivity.PREF_SAVE_UPLOAD_HISTORY, true)
                .putBoolean(MainActivity.PREF_SYNC2CH_ENABLED, false)
                .putBoolean(MainActivity.PREF_CACHE_ENABLED, true)
                .putInt(MainActivity.PREF_CACHE_MAX_MB, AppCache.DEFAULT_MAX_MB)
                .putString(MainActivity.PREF_BOARD_PRIORITY_WORDS, "[]")
                .putBoolean(MainActivity.PREF_GESTURES_ENABLED, false)
                .putInt(MainActivity.PREF_GESTURE_SENSITIVITY, 2);
        for (String action : MainActivity.GESTURE_ACTIONS) {
            editor.putString(MainActivity.PREF_GESTURE_PREFIX + action,
                    MainActivity.defaultGestureForAction(action));
        }
        editor.apply();
        loadSettings();
        Toast.makeText(this, MainActivity.text("\u8a2d\u5b9a\u3092\u521d\u671f\u5024\u306b\u623b\u3057\u307e\u3057\u305f", "Settings restored to defaults"), Toast.LENGTH_SHORT).show();
        recreate();
    }

    private void updateTreeDependentSettings() {
        boolean enabled = treeView.isChecked();
        treeSkipFirstReply.setEnabled(enabled);
        treeSkipFirstReply.setAlpha(enabled ? 1f : 0.45f);
    }

    private void updateMediaDependentSettings() {
        boolean mediaEnabled = showMediaPreviews.isChecked();
        blurImgurImages.setEnabled(mediaEnabled);
        blurImgurImages.setAlpha(mediaEnabled ? 1f : 0.45f);
        boolean videoBlurEnabled = mediaEnabled && blurImgurImages.isChecked();
        blurVideoThumbnails.setEnabled(videoBlurEnabled);
        blurVideoThumbnails.setAlpha(videoBlurEnabled ? 1f : 0.45f);
        blurGifThumbnails.setEnabled(videoBlurEnabled);
        blurGifThumbnails.setAlpha(videoBlurEnabled ? 1f : 0.45f);
        autoplayGifs.setEnabled(mediaEnabled);
        autoplayGifs.setAlpha(mediaEnabled ? 1f : 0.45f);
    }

    private void updateCacheDependentSettings() {
        boolean enabled = cacheEnabled.isChecked();
        cacheMaxMb.setEnabled(enabled);
        cacheMaxMb.setAlpha(enabled ? 1f : 0.45f);
        cacheApply.setEnabled(enabled);
        cacheApply.setAlpha(enabled ? 1f : 0.45f);
        cacheUsage.setAlpha(enabled ? 1f : 0.45f);
        cacheUsageText.setAlpha(enabled ? 1f : 0.45f);
    }

    private Integer parseCacheMaxInput() {
        try {
            return Integer.parseInt(cacheMaxMb.getText().toString().trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private void applyCacheMaxMb() {
        Integer value = parseCacheMaxInput();
        if (value == null || value < AppCache.MIN_MAX_MB || value > AppCache.MAX_MAX_MB) {
            Toast.makeText(this, MainActivity.text("32〜2048 MB\u306e\u6570\u5024\u3092\u5165\u529b", "Enter a value from 32 to 2048 MB."), Toast.LENGTH_SHORT).show();
            cacheMaxMb.setText(String.valueOf(preferences.getInt(MainActivity.PREF_CACHE_MAX_MB, AppCache.DEFAULT_MAX_MB)));
            return;
        }
        long current = AppCache.size(this);
        long requested = value * 1024L * 1024L;
        if (requested < current) {
            Toast.makeText(this, MainActivity.text(
                    "\u73fe\u5728\u306e\u30ad\u30e3\u30c3\u30b7\u30e5\u4f7f\u7528\u91cf\u3088\u308a\u5c0f\u3055\u3044\u5024\u306f\u8a2d\u5b9a\u3067\u304d\u307e\u305b\u3093",
                    "The cache limit cannot be smaller than the current cache size."), Toast.LENGTH_LONG).show();
            cacheMaxMb.setText(String.valueOf(preferences.getInt(MainActivity.PREF_CACHE_MAX_MB, AppCache.DEFAULT_MAX_MB)));
            return;
        }
        preferences.edit().putInt(MainActivity.PREF_CACHE_MAX_MB, value).apply();
        cacheMaxMb.setText(String.valueOf(value));
        updateCacheUsage();
        Toast.makeText(this, MainActivity.text("\u30ad\u30e3\u30c3\u30b7\u30e5\u30b5\u30a4\u30ba\u3092\u9069\u7528\u3057\u307e\u3057\u305f", "Cache size applied."), Toast.LENGTH_SHORT).show();
    }

    private void updateCacheUsage() {
        if (cacheUsage == null || cacheUsageText == null) {
            return;
        }
        long current = AppCache.size(this);
        long max = Math.max(1L, preferences.getInt(MainActivity.PREF_CACHE_MAX_MB, AppCache.DEFAULT_MAX_MB) * 1024L * 1024L);
        int progress = (int) Math.max(0L, Math.min(1000L, current * 1000L / max));
        cacheUsage.setProgress(progress);
        cacheUsageText.setText(MainActivity.text("\u30ad\u30e3\u30c3\u30b7\u30e5\u4f7f\u7528\u91cf: ",
                "Cache used: ") + AppCache.formatBytes(current) + " / " + AppCache.formatBytes(max));
        if (clearCacheButton != null) {
            clearCacheButton.setEnabled(current > 0L);
            clearCacheButton.setAlpha(current > 0L ? 1f : 0.5f);
        }
    }

    private void confirmClearCache() {
        long current = AppCache.size(this);
        if (current <= 0L) {
            Toast.makeText(this, MainActivity.text("\u30ad\u30e3\u30c3\u30b7\u30e5\u306f\u7a7a\u3067\u3059", "The cache is empty."),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("\u30ad\u30e3\u30c3\u30b7\u30e5\u3092\u524a\u9664", "Clear cache"))
                .setMessage(MainActivity.text(
                        "\u30ad\u30e3\u30c3\u30b7\u30e5 " + AppCache.formatBytes(current)
                                + " \u3092\u524a\u9664\u3057\u307e\u3059\u3002\n\n\u30d6\u30c3\u30af\u30de\u30fc\u30af\u3001\u5c65\u6b74\u3001\u8a2d\u5b9a\u306f\u524a\u9664\u3055\u308c\u307e\u305b\u3093\u3002",
                        "Delete " + AppCache.formatBytes(current)
                                + " of cached data?\n\nBookmarks, history, and settings will not be deleted."))
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u30ad\u30e3\u30c3\u30b7\u30e5\u3092\u524a\u9664", "Delete cache"), (d, which) -> {
                    AppCache.clear(this);
                    updateCacheUsage();
                    Toast.makeText(this, MainActivity.text("\u30ad\u30e3\u30c3\u30b7\u30e5\u3092\u524a\u9664\u3057\u307e\u3057\u305f", "Cache cleared."),
                            Toast.LENGTH_SHORT).show();
                })
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private final class SectionedSettingsLayout extends LinearLayout {
        private final boolean categoryScreen;
        private final int targetCategory;
        private int categoryIndex = -1;
        private boolean includeCurrentCategory;

        SectionedSettingsLayout(boolean categoryScreen, int targetCategory) {
            super(SettingsActivity.this);
            this.categoryScreen = categoryScreen;
            this.targetCategory = targetCategory;
        }

        @Override
        public void addView(View child, int index, ViewGroup.LayoutParams params) {
            Object tag = child.getTag();
            if (tag instanceof SectionHeaderTag) {
                SectionHeaderTag header = (SectionHeaderTag) tag;
                categoryIndex++;
                includeCurrentCategory = categoryScreen && categoryIndex == targetCategory;
                if (!categoryScreen) {
                    int selectedCategory = categoryIndex;
                    child.setOnClickListener(v -> openCategory(selectedCategory, header));
                    super.addView(child, index, params);
                }
                return;
            }
            if (categoryIndex < 0 || includeCurrentCategory) {
                super.addView(child, index, params);
            }
        }
    }

    private static final class SectionHeaderTag {
        final String title;
        final String subtitle;

        SectionHeaderTag(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    private View sectionTitle(int iconRes, String title, String subtitle) {
        View row = managementRow(iconRes, title, subtitle, null);
        row.setTag(new SectionHeaderTag(title, subtitle));
        return row;
    }

    private void openCategory(int category, SectionHeaderTag header) {
        if (category == CATEGORY_GESTURES) {
            startActivity(new Intent(this, GestureSettingsActivity.class));
            return;
        }
        if (category == CATEGORY_BBS_LINKS) {
            startActivity(new Intent(this, BbsLinksActivity.class));
            return;
        }
        startActivity(new Intent(this, SettingsCategoryActivity.class)
                .putExtra(EXTRA_CATEGORY, category)
                .putExtra(EXTRA_CATEGORY_TITLE, header.title)
                .putExtra(EXTRA_CATEGORY_SUBTITLE, header.subtitle));
    }

    private TextView helperText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(mutedColor());
        view.setTextSize(13);
        view.setPadding(0, dp(4), 0, dp(4));
        return view;
    }

    private TextView fieldLabel(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(textColor());
        view.setTextSize(15);
        view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        view.setPadding(0, dp(8), 0, dp(2));
        return view;
    }

    private TextView smallActionButton(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(Color.WHITE);
        view.setTextSize(14);
        view.setGravity(Gravity.CENTER);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(15, 118, 110));
        background.setCornerRadius(dp(10));
        view.setBackground(background);
        return view;
    }

    private LinearLayout.LayoutParams fieldParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        params.setMargins(0, dp(4), 0, dp(8));
        return params;
    }

    private RadioButton radio(String value) {
        RadioButton button = new RadioButton(this);
        button.setText(value);
        button.setTextColor(textColor());
        button.setTextSize(16);
        Theme.tintCompoundButton(this, button);
        return button;
    }

    private CheckBox settingCheckBox(String value) {
        CheckBox box = new CheckBox(this);
        box.setText(value);
        box.setTextColor(textColor());
        box.setTextSize(16);
        Theme.tintCompoundButton(this, box);
        return box;
    }

    private View managementRow(int iconRes, String title, String subtitle, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(10), dp(10));
        row.setBackground(roundedManagementCard());
        row.setOnClickListener(listener);
        row.setClickable(true);
        row.setFocusable(true);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Color.rgb(15, 118, 110));
        icon.setPadding(dp(8), dp(8), dp(8), dp(8));
        icon.setBackground(roundedIconBubble());
        row.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setGravity(Gravity.CENTER_VERTICAL);
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(textColor());
        titleView.setTextSize(16);
        titleView.setSingleLine(true);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        texts.addView(titleView);

        TextView subtitleView = new TextView(this);
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(mutedColor());
        subtitleView.setTextSize(12);
        subtitleView.setSingleLine(true);
        subtitleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        texts.addView(subtitleView);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        textParams.setMargins(dp(12), 0, dp(8), 0);
        row.addView(texts, textParams);

        ImageView arrow = new ImageView(this);
        arrow.setImageResource(R.drawable.ic_arrow_forward);
        arrow.setColorFilter(mutedColor());
        row.addView(arrow, new LinearLayout.LayoutParams(dp(24), dp(24)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(68));
        params.setMargins(0, dp(4), 0, dp(8));
        row.setLayoutParams(params);
        return row;
    }

    private Button cacheActionButton() {
        Button button = new Button(this);
        button.setText(MainActivity.text("\u30ad\u30e3\u30c3\u30b7\u30e5\u3092\u524a\u9664", "Delete cache"));
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(14), 0, dp(14), 0);
        Drawable icon = getDrawable(R.drawable.ic_delete).mutate();
        icon.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        button.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
        button.setCompoundDrawablePadding(dp(8));
        GradientDrawable background = new GradientDrawable();
        background.setColor(Theme.dark(this) ? Color.rgb(75, 85, 99) : Color.rgb(100, 116, 139));
        background.setCornerRadius(dp(10));
        button.setBackground(background);
        return button;
    }

    private GradientDrawable roundedManagementCard() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(surfaceColor());
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(dp(14));
        return drawable;
    }

    private GradientDrawable roundedIconBubble() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.dark(this) ? Color.rgb(17, 55, 58) : Color.rgb(220, 252, 247));
        drawable.setCornerRadius(dp(13));
        return drawable;
    }

    private Drawable cacheUsageDrawable() {
        boolean dark = Theme.dark(this);
        GradientDrawable track = new GradientDrawable();
        track.setColor(dark ? Color.rgb(34, 45, 56) : Color.rgb(226, 232, 240));
        track.setStroke(dp(1), dark ? Color.rgb(86, 98, 112) : borderColor());
        track.setCornerRadius(dp(9));

        GradientDrawable progress = new GradientDrawable();
        progress.setColor(dark ? Color.rgb(45, 212, 191) : Theme.accent(this));
        progress.setCornerRadius(dp(9));

        ClipDrawable clippedProgress = new ClipDrawable(progress, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        LayerDrawable layers = new LayerDrawable(new Drawable[]{track, clippedProgress});
        layers.setId(0, android.R.id.background);
        layers.setId(1, android.R.id.progress);
        return layers;
    }

    private GradientDrawable roundedField() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(surfaceColor());
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(dp(10));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
