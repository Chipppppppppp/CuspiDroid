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
    private CheckBox blurImgurImages;
    private CheckBox addressBarTop;
    private RadioButton searchFind5chIo;
    private RadioButton searchCustom;
    private EditText customTemplate;
    private EditText ngWords;
    private EditText bbsName;
    private EditText bbsUrl;
    private Button addBbsButton;
    private LinearLayout bbsList;
    private String editingBbsUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        buildLayout();
        loadSettings();
        setupAutoSave();
    }

    private void buildLayout() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.WHITE);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText(MainActivity.text("\u8a2d\u5b9a", "Settings"));
        title.setTextColor(TEXT);
        title.setTextSize(24);
        title.setGravity(Gravity.START);
        title.setPadding(0, 0, 0, dp(16));
        root.addView(title);

        root.addView(sectionTitle(MainActivity.text("\u30ea\u30f3\u30af", "Links")));
        open5chInNewTab = new CheckBox(this);
        open5chInNewTab.setText(MainActivity.text("5ch\u30ea\u30f3\u30af\u3092\u65b0\u898f\u30bf\u30d6\u3067\u958b\u304f", "Open 5ch links in a new tab"));
        open5chInNewTab.setTextColor(TEXT);
        open5chInNewTab.setTextSize(16);
        root.addView(open5chInNewTab);

        blurImgurImages = new CheckBox(this);
        blurImgurImages.setText(MainActivity.text("imgur\u306e\u30b0\u30ed\u753b\u50cf\u3092\u307c\u304b\u3059", "Blur graphic imgur images"));
        blurImgurImages.setTextColor(TEXT);
        blurImgurImages.setTextSize(16);
        root.addView(blurImgurImages);

        addressBarTop = new CheckBox(this);
        addressBarTop.setText(MainActivity.text("\u691c\u7d22\u30d0\u30fc\u3092\u4e0a\u306b\u8868\u793a", "Show address bar at top"));
        addressBarTop.setTextColor(TEXT);
        addressBarTop.setTextSize(16);
        root.addView(addressBarTop);

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
        customTemplate.setTextColor(TEXT);
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

        root.addView(sectionTitle(MainActivity.text("NG\u30ef\u30fc\u30c9", "NG Words")));
        ngWords = new EditText(this);
        ngWords.setMinLines(3);
        ngWords.setGravity(Gravity.TOP | Gravity.START);
        ngWords.setTextSize(14);
        ngWords.setTextColor(TEXT);
        ngWords.setHint(MainActivity.text("1\u884c\u306b1\u8a9e", "One word per line"));
        ngWords.setBackground(roundedField());
        ngWords.setPadding(dp(12), dp(8), dp(12), dp(8));
        root.addView(ngWords, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(104)));
        root.addView(helperText(MainActivity.text(
                "\u540d\u524d\u30fb\u65e5\u4ed8\u30fb\u672c\u6587\u306b\u542b\u307e\u308c\u308b\u30ec\u30b9\u3092\u975e\u8868\u793a",
                "Hides posts containing these words in name, date, or body.")));

        root.addView(sectionTitle(MainActivity.text("BBS\u30ea\u30f3\u30af", "BBS Links")));
        bbsName = new EditText(this);
        bbsName.setSingleLine(true);
        bbsName.setTextSize(14);
        bbsName.setTextColor(TEXT);
        bbsName.setHint(MainActivity.text("\u540d\u524d \u4f8b: Edge", "Name, e.g. Edge"));
        bbsName.setBackground(roundedField());
        bbsName.setPadding(dp(12), 0, dp(12), 0);
        root.addView(bbsName, fieldParams());

        bbsUrl = new EditText(this);
        bbsUrl.setSingleLine(true);
        bbsUrl.setTextSize(14);
        bbsUrl.setTextColor(TEXT);
        bbsUrl.setHint(MainActivity.text("\u677fURL \u4f8b: https://example.net/live/", "Board URL, e.g. https://example.net/live/"));
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

    }

    private void loadSettings() {
        open5chInNewTab.setChecked(preferences.getBoolean(MainActivity.PREF_5CH_NEW_TAB, true));
        blurImgurImages.setChecked(preferences.getBoolean(MainActivity.PREF_BLUR_IMGUR, true));
        addressBarTop.setChecked(preferences.getBoolean(MainActivity.PREF_ADDRESS_BAR_TOP, false));

        String template = preferences.getString(MainActivity.PREF_SEARCH_TEMPLATE, MainActivity.DEFAULT_SEARCH_TEMPLATE);
        customTemplate.setText(template);
        ngWords.setText(preferences.getString(MainActivity.PREF_NG_WORDS, ""));
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
        blurImgurImages.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
        addressBarTop.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
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
        ngWords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveSettings(false);
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

        preferences.edit()
                .putBoolean(MainActivity.PREF_5CH_NEW_TAB, open5chInNewTab.isChecked())
                .putBoolean(MainActivity.PREF_BLUR_IMGUR, blurImgurImages.isChecked())
                .putBoolean(MainActivity.PREF_ADDRESS_BAR_TOP, addressBarTop.isChecked())
                .putString(MainActivity.PREF_SEARCH_TEMPLATE, template)
                .putString(MainActivity.PREF_NG_WORDS, ngWords.getText().toString())
                .apply();
    }

    private TextView sectionTitle(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(TEXT);
        view.setTextSize(18);
        view.setPadding(0, dp(16), 0, dp(8));
        return view;
    }

    private TextView helperText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(MUTED);
        view.setTextSize(13);
        view.setPadding(0, dp(4), 0, dp(4));
        return view;
    }

    private RadioButton radio(String value) {
        RadioButton button = new RadioButton(this);
        button.setText(value);
        button.setTextColor(TEXT);
        button.setTextSize(16);
        return button;
    }

    private LinearLayout.LayoutParams fieldParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        params.setMargins(0, dp(4), 0, dp(8));
        return params;
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
            text.setTextColor(TEXT);
            row.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            ImageButton edit = new ImageButton(this);
            edit.setImageResource(R.drawable.ic_edit);
            edit.setContentDescription(MainActivity.text("BBS\u30ea\u30f3\u30af\u3092\u7de8\u96c6", "Edit BBS link"));
            edit.setColorFilter(TEXT);
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
            delete.setColorFilter(TEXT);
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
        drawable.setColor(SURFACE);
        drawable.setStroke(dp(1), BORDER);
        drawable.setCornerRadius(dp(10));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
