package io.github.cuspidroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
    private CheckBox blurImgurImages;
    private CheckBox addressBarTop;
    private CheckBox treeView;
    private CheckBox treeSkipFirstReply;
    private CheckBox boardSortBySpeed;
    private RadioButton themeSystem;
    private RadioButton themeLight;
    private RadioButton themeDark;
    private RadioGroup themeGroup;
    private RadioButton searchFind5chIo;
    private RadioButton searchCustom;
    private EditText customTemplate;
    private EditText bbsName;
    private EditText bbsUrl;
    private Button addBbsButton;
    private LinearLayout bbsList;
    private String editingBbsUrl;
    private EditText boardPriorityWord;
    private Button addBoardPriorityWordButton;
    private LinearLayout boardPriorityWordList;
    private String editingBoardPriorityWord;

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

        root.addView(sectionTitle(MainActivity.text("\u30ea\u30f3\u30af", "Links")));
        open5chInNewTab = new CheckBox(this);
        open5chInNewTab.setText(MainActivity.text("5ch\u30ea\u30f3\u30af\u3092\u65b0\u898f\u30bf\u30d6\u3067\u958b\u304f", "Open 5ch links in a new tab"));
        open5chInNewTab.setTextColor(textColor());
        open5chInNewTab.setTextSize(16);
        root.addView(open5chInNewTab);

        externalLinkInApp = new CheckBox(this);
        externalLinkInApp.setText(MainActivity.text("\u5916\u90e8\u30ea\u30f3\u30af\u3092\u30a2\u30d7\u30ea\u5185\u30d6\u30e9\u30a6\u30b6\u3067\u958b\u304f", "Open external links in the in-app browser"));
        externalLinkInApp.setTextColor(textColor());
        externalLinkInApp.setTextSize(16);
        root.addView(externalLinkInApp);

        blurImgurImages = new CheckBox(this);
        blurImgurImages.setText(MainActivity.text("imgur\u306e\u30b0\u30ed\u753b\u50cf\u3092\u307c\u304b\u3059", "Blur graphic imgur images"));
        blurImgurImages.setTextColor(textColor());
        blurImgurImages.setTextSize(16);
        root.addView(blurImgurImages);

        addressBarTop = new CheckBox(this);
        addressBarTop.setText(MainActivity.text("\u691c\u7d22\u30d0\u30fc\u3092\u4e0a\u306b\u8868\u793a", "Show address bar at top"));
        addressBarTop.setTextColor(textColor());
        addressBarTop.setTextSize(16);
        root.addView(addressBarTop);

        treeView = new CheckBox(this);
        treeView.setText(MainActivity.text("\u30c4\u30ea\u30fc\u8868\u793a", "Tree view"));
        treeView.setTextColor(textColor());
        treeView.setTextSize(16);
        root.addView(treeView);

        treeSkipFirstReply = new CheckBox(this);
        treeSkipFirstReply.setText(MainActivity.text(">>1\u3092\u30c4\u30ea\u30fc\u8868\u793a\u3057\u306a\u3044", "Do not tree replies to >>1"));
        treeSkipFirstReply.setTextColor(textColor());
        treeSkipFirstReply.setTextSize(16);
        root.addView(treeSkipFirstReply);

        root.addView(sectionTitle(MainActivity.text("\u677f\u30b9\u30ec\u4e00\u89a7", "Board Thread List")));
        boardSortBySpeed = new CheckBox(this);
        boardSortBySpeed.setText(MainActivity.text("\u677f\u306e\u30b9\u30ec\u3092\u52e2\u3044\u9806\u306b\u4e26\u3079\u308b", "Sort board threads by speed"));
        boardSortBySpeed.setTextColor(textColor());
        boardSortBySpeed.setTextSize(16);
        root.addView(boardSortBySpeed);

        boardPriorityWord = new EditText(this);
        boardPriorityWord.setSingleLine(true);
        boardPriorityWord.setTextSize(14);
        boardPriorityWord.setTextColor(textColor());
        boardPriorityWord.setHintTextColor(hintColor());
        boardPriorityWord.setHint(MainActivity.text("\u4e0a\u4f4d\u306b\u7f6e\u304f\u30ef\u30fc\u30c9", "Priority word"));
        boardPriorityWord.setImeOptions(EditorInfo.IME_ACTION_DONE);
        boardPriorityWord.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        boardPriorityWord.setBackground(roundedField());
        boardPriorityWord.setPadding(dp(12), 0, dp(12), 0);
        root.addView(boardPriorityWord, fieldParams());

        addBoardPriorityWordButton = new Button(this);
        addBoardPriorityWordButton.setText(MainActivity.text("\u512a\u5148\u30ef\u30fc\u30c9\u3092\u8ffd\u52a0", "Add priority word"));
        addBoardPriorityWordButton.setAllCaps(false);
        addBoardPriorityWordButton.setOnClickListener(v -> addBoardPriorityWord());
        root.addView(addBoardPriorityWordButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        boardPriorityWordList = new LinearLayout(this);
        boardPriorityWordList.setOrientation(LinearLayout.VERTICAL);
        root.addView(boardPriorityWordList);
        renderBoardPriorityWords();

        root.addView(sectionTitle(MainActivity.text("\u30c6\u30fc\u30de", "Theme")));
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

        root.addView(sectionTitle(MainActivity.text("\u6a19\u6e96\u691c\u7d22\u30a8\u30f3\u30b8\u30f3", "Default Search Engine")));
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
        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        fieldParams.setMargins(0, dp(4), 0, dp(8));
        root.addView(customTemplate, fieldParams);

        TextView hint = helperText(MainActivity.text("\u691c\u7d22\u8a9e\u3092\u5165\u308c\u308b\u5834\u6240\u306b %s \u3092\u4f7f\u3046", "Use %s where the encoded query should be inserted."));
        root.addView(hint);

        root.addView(sectionTitle(MainActivity.text("NG\u8a2d\u5b9a", "NG Rules")));
        Button openNgRules = new Button(this);
        openNgRules.setText(MainActivity.text("NG\u8a2d\u5b9a\u3092\u7ba1\u7406", "Manage NG rules"));
        openNgRules.setAllCaps(false);
        openNgRules.setOnClickListener(v -> startActivity(new Intent(this, NgRulesActivity.class)));
        root.addView(openNgRules, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        root.addView(sectionTitle(MainActivity.text("BBS\u30ea\u30f3\u30af", "BBS Links")));
        root.addView(helperText(MainActivity.text(
                "\u8a8d\u8a3c\u304c\u5fc5\u8981\u306aBBS\u306f\u3001\u30b9\u30ec\u3092WebView\u3067\u958b\u3044\u3066\u8a8d\u8a3c\u3059\u308b\u3068\u3001\u305d\u306e\u30af\u30c3\u30ad\u30fc\u3092\u4f7f\u3063\u3066\u95b2\u89a7\u30fb\u66f8\u304d\u8fbc\u307f\u3067\u304d\u307e\u3059\u3002",
                "If a BBS requires authentication, open the thread in WebView and authenticate there. CuspiDroid will use those cookies for reading and posting.")));
        bbsName = new EditText(this);
        bbsName.setSingleLine(true);
        bbsName.setTextSize(14);
        bbsName.setTextColor(textColor());
        bbsName.setHintTextColor(hintColor());
        bbsName.setHint(MainActivity.text("\u540d\u524d", "Name"));
        bbsName.setBackground(roundedField());
        bbsName.setPadding(dp(12), 0, dp(12), 0);
        root.addView(bbsName, fieldParams());

        bbsUrl = new EditText(this);
        bbsUrl.setSingleLine(true);
        bbsUrl.setTextSize(14);
        bbsUrl.setTextColor(textColor());
        bbsUrl.setHintTextColor(hintColor());
        bbsUrl.setHint(MainActivity.text("\u677fURL", "Board URL"));
        bbsUrl.setImeOptions(EditorInfo.IME_ACTION_DONE);
        bbsUrl.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        bbsUrl.setBackground(roundedField());
        bbsUrl.setPadding(dp(12), 0, dp(12), 0);
        root.addView(bbsUrl, fieldParams());

        addBbsButton = new Button(this);
        addBbsButton.setText(MainActivity.text("BBS\u30ea\u30f3\u30af\u3092\u8ffd\u52a0", "Add BBS link"));
        addBbsButton.setAllCaps(false);
        addBbsButton.setOnClickListener(v -> addBbsLink());
        root.addView(addBbsButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        bbsList = new LinearLayout(this);
        bbsList.setOrientation(LinearLayout.VERTICAL);
        root.addView(bbsList);
        renderBbsLinks();

        root.addView(sectionTitle(MainActivity.text("\u30b9\u30ec\u5c65\u6b74", "Thread History")));
        Button openHistory = new Button(this);
        openHistory.setText(MainActivity.text("\u30b9\u30ec\u5c65\u6b74\u3092\u7ba1\u7406", "Manage thread history"));
        openHistory.setAllCaps(false);
        openHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        root.addView(openHistory, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        root.addView(sectionTitle(MainActivity.text("\u65e2\u8aad", "Read Positions")));
        Button openReadPosts = new Button(this);
        openReadPosts.setText(MainActivity.text("\u65e2\u8aad\u3092\u7ba1\u7406", "Manage read positions"));
        openReadPosts.setAllCaps(false);
        openReadPosts.setOnClickListener(v -> startActivity(new Intent(this, ReadPostsActivity.class)));
        root.addView(openReadPosts, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

    }

    private void loadSettings() {
        open5chInNewTab.setChecked(preferences.getBoolean(MainActivity.PREF_5CH_NEW_TAB, true));
        externalLinkInApp.setChecked(preferences.getBoolean(MainActivity.PREF_EXTERNAL_LINK_IN_APP, false));
        blurImgurImages.setChecked(preferences.getBoolean(MainActivity.PREF_BLUR_IMGUR, true));
        addressBarTop.setChecked(preferences.getBoolean(MainActivity.PREF_ADDRESS_BAR_TOP, false));
        treeView.setChecked(preferences.getBoolean(MainActivity.PREF_TREE_VIEW, false));
        treeSkipFirstReply.setChecked(preferences.getBoolean(MainActivity.PREF_TREE_SKIP_FIRST_REPLY, false));
        boardSortBySpeed.setChecked(preferences.getBoolean(MainActivity.PREF_BOARD_SORT_BY_SPEED, false));
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
        blurImgurImages.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        addressBarTop.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        treeView.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        treeSkipFirstReply.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        boardSortBySpeed.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
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
                .putBoolean(MainActivity.PREF_BLUR_IMGUR, blurImgurImages.isChecked())
                .putBoolean(MainActivity.PREF_ADDRESS_BAR_TOP, addressBarTop.isChecked())
                .putBoolean(MainActivity.PREF_TREE_VIEW, treeView.isChecked())
                .putBoolean(MainActivity.PREF_TREE_SKIP_FIRST_REPLY, treeSkipFirstReply.isChecked())
                .putBoolean(MainActivity.PREF_BOARD_SORT_BY_SPEED, boardSortBySpeed.isChecked())
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

    private RadioButton radio(String value) {
        RadioButton button = new RadioButton(this);
        button.setText(value);
        button.setTextColor(textColor());
        button.setTextSize(16);
        return button;
    }

    private LinearLayout.LayoutParams fieldParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        params.setMargins(0, dp(4), 0, dp(8));
        return params;
    }

    private void addBoardPriorityWord() {
        String word = boardPriorityWord.getText().toString().trim();
        if (word.isEmpty()) {
            Toast.makeText(this, MainActivity.text("\u30ef\u30fc\u30c9\u3092\u5165\u529b", "Enter a word."), Toast.LENGTH_SHORT).show();
            return;
        }
        if (editingBoardPriorityWord != null) {
            MainActivity.removeBoardPriorityWord(preferences, editingBoardPriorityWord);
        }
        MainActivity.addBoardPriorityWord(preferences, word);
        editingBoardPriorityWord = null;
        boardPriorityWord.setText("");
        addBoardPriorityWordButton.setText(MainActivity.text("\u512a\u5148\u30ef\u30fc\u30c9\u3092\u8ffd\u52a0", "Add priority word"));
        renderBoardPriorityWords();
    }

    private void renderBoardPriorityWords() {
        if (boardPriorityWordList == null) {
            return;
        }
        boardPriorityWordList.removeAllViews();
        java.util.List<String> words = MainActivity.readBoardPriorityWords(preferences);
        if (words.isEmpty()) {
            boardPriorityWordList.addView(helperText(MainActivity.text("\u512a\u5148\u30ef\u30fc\u30c9\u306a\u3057", "No priority words.")));
            return;
        }
        for (String word : words) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView text = helperText(word);
            text.setTextColor(textColor());
            row.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            ImageButton edit = new ImageButton(this);
            edit.setImageResource(R.drawable.ic_edit);
            edit.setContentDescription(MainActivity.text("\u512a\u5148\u30ef\u30fc\u30c9\u3092\u7de8\u96c6", "Edit priority word"));
            edit.setColorFilter(textColor());
            edit.setBackground(roundedField());
            edit.setPadding(dp(10), dp(10), dp(10), dp(10));
            edit.setScaleType(ImageButton.ScaleType.CENTER);
            edit.setOnClickListener(v -> {
                editingBoardPriorityWord = word;
                boardPriorityWord.setText(word);
                boardPriorityWord.setSelection(boardPriorityWord.getText().length());
                addBoardPriorityWordButton.setText(MainActivity.text("\u512a\u5148\u30ef\u30fc\u30c9\u3092\u66f4\u65b0", "Update priority word"));
            });
            LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(dp(46), dp(44));
            editParams.setMargins(dp(8), 0, 0, 0);
            row.addView(edit, editParams);

            ImageButton delete = new ImageButton(this);
            delete.setImageResource(R.drawable.ic_delete);
            delete.setContentDescription(MainActivity.text("\u512a\u5148\u30ef\u30fc\u30c9\u3092\u524a\u9664", "Delete priority word"));
            delete.setColorFilter(textColor());
            delete.setBackground(roundedField());
            delete.setPadding(dp(10), dp(10), dp(10), dp(10));
            delete.setScaleType(ImageButton.ScaleType.CENTER);
            delete.setOnClickListener(v -> {
                MainActivity.removeBoardPriorityWord(preferences, word);
                if (word.equals(editingBoardPriorityWord)) {
                    editingBoardPriorityWord = null;
                    boardPriorityWord.setText("");
                    addBoardPriorityWordButton.setText(MainActivity.text("\u512a\u5148\u30ef\u30fc\u30c9\u3092\u8ffd\u52a0", "Add priority word"));
                }
                renderBoardPriorityWords();
            });
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(dp(46), dp(44));
            deleteParams.setMargins(dp(8), 0, 0, 0);
            row.addView(delete, deleteParams);
            boardPriorityWordList.addView(row);
        }
    }

    private void addBbsLink() {
        String name = bbsName.getText().toString().trim();
        String url = bbsUrl.getText().toString().trim();
        if (name.isEmpty() || url.isEmpty()) {
            Toast.makeText(this, MainActivity.text("BBS\u540d\u3068\u677fURL\u3092\u5165\u529b", "Enter a BBS name and board URL."), Toast.LENGTH_SHORT).show();
            return;
        }
        if (editingBbsUrl != null) {
            MainActivity.removeBbsLink(preferences, editingBbsUrl);
        }
        MainActivity.addBbsLink(preferences, name, url);
        editingBbsUrl = null;
        bbsName.setText("");
        bbsUrl.setText("");
        addBbsButton.setText(MainActivity.text("BBS\u30ea\u30f3\u30af\u3092\u8ffd\u52a0", "Add BBS link"));
        renderBbsLinks();
    }

    private void renderBbsLinks() {
        if (bbsList == null) {
            return;
        }
        bbsList.removeAllViews();
        java.util.List<MainActivity.BbsLink> links = MainActivity.readBbsLinks(preferences);
        if (links.isEmpty()) {
            bbsList.addView(helperText(MainActivity.text("BBS\u30ea\u30f3\u30af\u306a\u3057", "No BBS links.")));
            return;
        }
        for (MainActivity.BbsLink link : links) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView text = helperText(link.name + "\n" + link.url);
            text.setTextColor(textColor());
            row.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            ImageButton edit = new ImageButton(this);
            edit.setImageResource(R.drawable.ic_edit);
            edit.setContentDescription(MainActivity.text("BBS\u30ea\u30f3\u30af\u3092\u7de8\u96c6", "Edit BBS link"));
            edit.setColorFilter(textColor());
            edit.setBackground(roundedField());
            edit.setPadding(dp(10), dp(10), dp(10), dp(10));
            edit.setScaleType(ImageButton.ScaleType.CENTER);
            edit.setOnClickListener(v -> {
                editingBbsUrl = link.url;
                bbsName.setText(link.name);
                bbsUrl.setText(link.url);
                addBbsButton.setText(MainActivity.text("BBS\u30ea\u30f3\u30af\u3092\u66f4\u65b0", "Update BBS link"));
            });
            LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(dp(46), dp(44));
            editParams.setMargins(dp(8), 0, 0, 0);
            row.addView(edit, editParams);
            ImageButton delete = new ImageButton(this);
            delete.setImageResource(R.drawable.ic_delete);
            delete.setContentDescription(MainActivity.text("BBS\u30ea\u30f3\u30af\u3092\u524a\u9664", "Delete BBS link"));
            delete.setColorFilter(textColor());
            delete.setBackground(roundedField());
            delete.setPadding(dp(10), dp(10), dp(10), dp(10));
            delete.setScaleType(ImageButton.ScaleType.CENTER);
            delete.setOnClickListener(v -> {
                MainActivity.removeBbsLink(preferences, link.url);
                if (link.url.equals(editingBbsUrl)) {
                    editingBbsUrl = null;
                    bbsName.setText("");
                    bbsUrl.setText("");
                    addBbsButton.setText(MainActivity.text("BBS\u30ea\u30f3\u30af\u3092\u8ffd\u52a0", "Add BBS link"));
                }
                renderBbsLinks();
            });
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(dp(46), dp(44));
            deleteParams.setMargins(dp(8), 0, 0, 0);
            row.addView(delete, deleteParams);
            bbsList.addView(row);
        }
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
