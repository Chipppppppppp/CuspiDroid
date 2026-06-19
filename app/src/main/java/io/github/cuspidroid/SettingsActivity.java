package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
    private CheckBox treeView;
    private CheckBox treeSkipFirstReply;
    private CheckBox autoScrollUnread;
    private CheckBox omitCopyPaste;
    private CheckBox autoAa;
    private CheckBox boardSortBySpeed;
    private CheckBox cacheEnabled;
    private CheckBox disableHistory;
    private EditText cacheMaxMb;
    private TextView cacheApply;
    private ProgressBar cacheUsage;
    private TextView cacheUsageText;
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
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bgColor());
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText(MainActivity.text("\u8a2d\u5b9a", "Settings"));
        title.setTextColor(textColor());
        title.setTextSize(24);
        title.setGravity(Gravity.START);
        title.setPadding(0, 0, 0, dp(16));
        root.addView(title);

        root.addView(sectionTitle(MainActivity.text("\u8868\u793a\u3068\u64cd\u4f5c", "Display & Controls")));
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

        root.addView(managementRow(R.drawable.ic_jump_arrow,
                MainActivity.text("\u30b8\u30a7\u30b9\u30c1\u30e3\u30fc", "Gestures"),
                MainActivity.text("\u30b9\u30ef\u30a4\u30d7\u64cd\u4f5c\u3068\u5272\u308a\u5f53\u3066\u3092\u8a2d\u5b9a", "Set swipe gestures and actions"),
                v -> startActivity(new Intent(this, GestureSettingsActivity.class))));

        root.addView(sectionTitle(MainActivity.text("\u30b9\u30ec\u8868\u793a", "Thread View")));
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

        root.addView(sectionTitle(MainActivity.text("\u677f\u30b9\u30ec\u4e00\u89a7", "Board Thread List")));
        boardSortBySpeed = new CheckBox(this);
        boardSortBySpeed.setText(MainActivity.text("\u677f\u306e\u30b9\u30ec\u3092\u52e2\u3044\u9806\u306b\u4e26\u3079\u308b", "Sort board threads by speed"));
        boardSortBySpeed.setTextColor(textColor());
        boardSortBySpeed.setTextSize(16);
        Theme.tintCompoundButton(this, boardSortBySpeed);
        root.addView(boardSortBySpeed);

        root.addView(managementRow(R.drawable.ic_text_fields,
                MainActivity.text("\u512a\u5148\u30ef\u30fc\u30c9\u3092\u7ba1\u7406", "Manage priority words"),
                MainActivity.text("\u30b9\u30ec\u4e00\u89a7\u3067\u512a\u5148\u3059\u308b\u30ef\u30fc\u30c9\u3092\u8ffd\u52a0\u30fb\u7de8\u96c6", "Add and edit words prioritized in board thread lists"),
                v -> startActivity(new Intent(this, BoardPriorityRulesActivity.class))));

        root.addView(sectionTitle(MainActivity.text("\u30ea\u30f3\u30af\u3068\u691c\u7d22", "Links & Search")));
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

        root.addView(sectionTitle(MainActivity.text("BBS\u30ea\u30f3\u30af", "BBS Links")));
        root.addView(helperText(MainActivity.text(
                "\u8a8d\u8a3c\u304c\u5fc5\u8981\u306aBBS\u306f\u3001\u30b9\u30ec\u3092WebView\u3067\u958b\u3044\u3066\u8a8d\u8a3c\u3059\u308b\u3068\u3001\u305d\u306e\u30af\u30c3\u30ad\u30fc\u3092\u4f7f\u3063\u3066\u95b2\u89a7\u30fb\u66f8\u304d\u8fbc\u307f\u3067\u304d\u307e\u3059\u3002",
                "If a BBS requires authentication, open the thread in WebView and authenticate there. CuspiDroid will use those cookies for reading and posting.")));
        root.addView(managementRow(R.drawable.ic_settings,
                MainActivity.text("BBS\u30ea\u30f3\u30af\u3092\u7ba1\u7406", "Manage BBS links"),
                MainActivity.text("\u30ab\u30b9\u30bf\u30e0BBS\u306e\u540d\u524d\u3068\u677fURL\u3092\u8ffd\u52a0\u30fb\u7de8\u96c6", "Add and edit custom BBS names and board URLs"),
                v -> startActivity(new Intent(this, BbsLinksActivity.class))));

        root.addView(sectionTitle(MainActivity.text("\u753b\u50cf\u3068\u30d5\u30a3\u30eb\u30bf", "Images & Filters")));
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
                MainActivity.text("NG\u8a2d\u5b9a\u3092\u7ba1\u7406", "Manage NG rules"),
                MainActivity.text("NGWord\u3001NGName\u3001NGID\u306a\u3069\u3092\u8ffd\u52a0\u30fb\u7de8\u96c6", "Add and edit NGWord, NGName, NGID, and related rules"),
                v -> startActivity(new Intent(this, NgRulesActivity.class))));

        root.addView(sectionTitle(MainActivity.text("\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9", "Uploads")));
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

        root.addView(managementRow(R.drawable.ic_copy,
                MainActivity.text("\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u5c65\u6b74\u3092\u7ba1\u7406", "Manage upload history"),
                MainActivity.text("ImgBB\u306b\u30a2\u30c3\u30d7\u30ed\u30fc\u30c9\u3057\u305f\u753b\u50cf\u3068URL\u3092\u8868\u793a\u30fb\u524a\u9664", "View and delete images and URLs uploaded to ImgBB"),
                v -> startActivity(new Intent(this, UploadHistoryActivity.class))));

        root.addView(sectionTitle(MainActivity.text("\u30ad\u30e3\u30c3\u30b7\u30e5", "Cache")));
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
        root.addView(cacheUsage, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(18)));

        root.addView(managementRow(android.R.drawable.ic_menu_delete,
                MainActivity.text("\u30ad\u30e3\u30c3\u30b7\u30e5\u3092\u524a\u9664", "Clear cache"),
                MainActivity.text("\u4fdd\u5b58\u6e08\u307f\u306e\u30b9\u30ec\u3068\u30e1\u30c7\u30a3\u30a2\u3092\u524a\u9664", "Delete cached threads and media"),
                v -> confirmClearCache()));

        root.addView(sectionTitle(MainActivity.text("\u5c65\u6b74\u3068\u4fdd\u5b58\u30c7\u30fc\u30bf", "History & Stored Data")));
        disableHistory = new CheckBox(this);
        disableHistory.setText(MainActivity.text("\u5c65\u6b74\u30fb\u65e2\u8aad\u5c65\u6b74\u3092\u8a18\u9332\u3057\u306a\u3044", "Do not record history or read positions"));
        disableHistory.setTextColor(textColor());
        disableHistory.setTextSize(16);
        Theme.tintCompoundButton(this, disableHistory);
        root.addView(disableHistory);

        root.addView(managementRow(android.R.drawable.ic_menu_recent_history,
                MainActivity.text("\u30b9\u30ec\u5c65\u6b74\u3092\u7ba1\u7406", "Manage thread history"),
                MainActivity.text("\u4fdd\u5b58\u3055\u308c\u305f\u30b9\u30ec\u5c65\u6b74\u3092\u8868\u793a\u30fb\u524a\u9664", "View and delete saved thread history"),
                v -> startActivity(new Intent(this, HistoryActivity.class))));

        root.addView(managementRow(R.drawable.ic_check,
                MainActivity.text("\u65e2\u8aad\u3092\u7ba1\u7406", "Manage read positions"),
                MainActivity.text("\u30b9\u30ec\u3054\u3068\u306e\u65e2\u8aad\u4f4d\u7f6e\u3092\u78ba\u8a8d\u30fb\u524a\u9664", "Review and delete saved read positions by thread"),
                v -> startActivity(new Intent(this, ReadPostsActivity.class))));

        root.addView(sectionTitle(MainActivity.text("\u30e1\u30f3\u30c6\u30ca\u30f3\u30b9", "Maintenance")));
        root.addView(managementRow(android.R.drawable.ic_dialog_info,
                MainActivity.text("\u30c7\u30d0\u30c3\u30b0\u8a2d\u5b9a", "Debug settings"),
                MainActivity.text("\u8abf\u67fb\u7528\u306e\u8868\u793a\u3092\u5207\u308a\u66ff\u3048", "Toggle diagnostic displays"),
                v -> startActivity(new Intent(this, DebugSettingsActivity.class))));

        root.addView(managementRow(android.R.drawable.ic_menu_revert,
                MainActivity.text("\u8a2d\u5b9a\u3092\u30c7\u30d5\u30a9\u30eb\u30c8\u306b\u623b\u3059", "Reset all settings"),
                MainActivity.text("\u8868\u793a\u3001\u691c\u7d22\u3001\u30b8\u30a7\u30b9\u30c1\u30e3\u30fc\u306a\u3069\u306e\u8a2d\u5b9a\u3092\u521d\u671f\u5024\u306b\u623b\u3059", "Restore display, search, gesture, and related settings"),
                v -> confirmResetDefaults()));

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
        updateMediaDependentSettings();
        if (preferences.getBoolean(MainActivity.PREF_ADDRESS_BAR_TOP, false)) {
            addressBarTop.setChecked(true);
        } else {
            addressBarBottom.setChecked(true);
        }
        treeView.setChecked(preferences.getBoolean(MainActivity.PREF_TREE_VIEW, true));
        treeSkipFirstReply.setChecked(preferences.getBoolean(MainActivity.PREF_TREE_SKIP_FIRST_REPLY, false));
        autoScrollUnread.setChecked(preferences.getBoolean(MainActivity.PREF_AUTO_SCROLL_UNREAD, true));
        omitCopyPaste.setChecked(preferences.getBoolean(MainActivity.PREF_OMIT_COPYPASTE, false));
        autoAa.setChecked(preferences.getBoolean(MainActivity.PREF_AUTO_AA, true));
        updateTreeDependentSettings();
        boardSortBySpeed.setChecked(preferences.getBoolean(MainActivity.PREF_BOARD_SORT_BY_SPEED, true));
        cacheEnabled.setChecked(preferences.getBoolean(MainActivity.PREF_CACHE_ENABLED, true));
        disableHistory.setChecked(preferences.getBoolean(MainActivity.PREF_DISABLE_HISTORY, false));
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
        addressBarTop.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        addressBarBottom.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        treeView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTreeDependentSettings();
            saveSettings(false);
        });
        treeSkipFirstReply.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        autoScrollUnread.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        omitCopyPaste.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        autoAa.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        boardSortBySpeed.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        disableHistory.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
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
                .putBoolean(MainActivity.PREF_TREE_VIEW, treeView.isChecked())
                .putBoolean(MainActivity.PREF_TREE_SKIP_FIRST_REPLY,
                        treeView.isChecked() && treeSkipFirstReply.isChecked())
                .putBoolean(MainActivity.PREF_AUTO_SCROLL_UNREAD, autoScrollUnread.isChecked())
                .putBoolean(MainActivity.PREF_OMIT_COPYPASTE, omitCopyPaste.isChecked())
                .putBoolean(MainActivity.PREF_AUTO_AA, autoAa.isChecked())
                .putBoolean(MainActivity.PREF_BOARD_SORT_BY_SPEED, boardSortBySpeed.isChecked())
                .putBoolean(MainActivity.PREF_DISABLE_HISTORY, disableHistory.isChecked())
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
                        "\u8868\u793a\u3001\u64cd\u4f5c\u3001\u691c\u7d22\u3001\u30b8\u30a7\u30b9\u30c1\u30e3\u30fc\u306a\u3069\u306e\u8a2d\u5b9a\u3092\u521d\u671f\u5024\u306b\u623b\u3057\u307e\u3059\u3002BBS\u30ea\u30f3\u30af\u3001NG\u8a2d\u5b9a\u3001\u5c65\u6b74\u3001\u65e2\u8aad\u4f4d\u7f6e\u3001\u30d6\u30c3\u30af\u30de\u30fc\u30af\u3001\u81ea\u5206\u306e\u66f8\u304d\u8fbc\u307f\u60c5\u5831\u306f\u6b8b\u308a\u307e\u3059\u3002",
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
                .putBoolean(MainActivity.PREF_TREE_VIEW, true)
                .putBoolean(MainActivity.PREF_TREE_SKIP_FIRST_REPLY, false)
                .putBoolean(MainActivity.PREF_AUTO_SCROLL_UNREAD, true)
                .putBoolean(MainActivity.PREF_OMIT_COPYPASTE, false)
                .putBoolean(MainActivity.PREF_AUTO_AA, true)
                .putBoolean(MainActivity.PREF_AA_DEBUG, false)
                .putBoolean(MainActivity.PREF_EXTERNAL_LINK_IN_APP, false)
                .putString(MainActivity.PREF_THEME_MODE, Theme.MODE_SYSTEM)
                .putBoolean(MainActivity.PREF_BOARD_SORT_BY_SPEED, true)
                .putBoolean(MainActivity.PREF_DISABLE_HISTORY, false)
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
    }

    private void confirmClearCache() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("\u30ad\u30e3\u30c3\u30b7\u30e5\u3092\u524a\u9664", "Clear cache"))
                .setMessage(MainActivity.text(
                        "\u4fdd\u5b58\u6e08\u307f\u306e\u30b9\u30ec\u3068\u30e1\u30c7\u30a3\u30a2\u30ad\u30e3\u30c3\u30b7\u30e5\u3092\u524a\u9664\u3057\u307e\u3059\u304b\uff1f",
                        "Delete cached threads and media?"))
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(MainActivity.text("\u524a\u9664", "Delete"), (d, which) -> {
                    AppCache.clear(this);
                    updateCacheUsage();
                    Toast.makeText(this, MainActivity.text("\u30ad\u30e3\u30c3\u30b7\u30e5\u3092\u524a\u9664\u3057\u307e\u3057\u305f", "Cache cleared."),
                            Toast.LENGTH_SHORT).show();
                })
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private TextView sectionTitle(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(textColor());
        view.setTextSize(18);
        view.setPadding(0, dp(16), 0, dp(8));
        return view;
    }

    private TextView helperText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(mutedColor());
        view.setTextSize(13);
        view.setPadding(0, dp(4), 0, dp(4));
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
