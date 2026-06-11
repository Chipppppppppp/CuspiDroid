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
    private RadioButton searchFind5chIo;
    private RadioButton searchCustom;
    private EditText customTemplate;
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
        title.setText("設定 / Settings");
        title.setTextColor(TEXT);
        title.setTextSize(24);
        title.setGravity(Gravity.START);
        title.setPadding(0, 0, 0, dp(16));
        root.addView(title);

        root.addView(sectionTitle("リンク / Links"));
        open5chInNewTab = new CheckBox(this);
        open5chInNewTab.setText("5chリンクを新規タブで開く / Open 5ch links in a new tab");
        open5chInNewTab.setTextColor(TEXT);
        open5chInNewTab.setTextSize(16);
        root.addView(open5chInNewTab);

        blurImgurImages = new CheckBox(this);
        blurImgurImages.setText("imgur画像を表示までぼかす / Blur imgur previews until shown");
        blurImgurImages.setTextColor(TEXT);
        blurImgurImages.setTextSize(16);
        root.addView(blurImgurImages);

        root.addView(sectionTitle("標準検索エンジン / Default Search Engine"));
        RadioGroup searchGroup = new RadioGroup(this);
        searchGroup.setOrientation(RadioGroup.VERTICAL);
        searchFind5chIo = radio("find.5ch.io");
        searchCustom = radio("カスタムURLテンプレート / Custom URL template");
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

        TextView hint = helperText("検索語を入れる場所に %s を使う / Use %s where the encoded query should be inserted.");
        root.addView(hint);

        root.addView(sectionTitle("BBSリンク / BBS Links"));
        bbsName = new EditText(this);
        bbsName.setSingleLine(true);
        bbsName.setTextSize(14);
        bbsName.setTextColor(TEXT);
        bbsName.setHint("名前 例: Edge / Name, e.g. Edge");
        bbsName.setBackground(roundedField());
        bbsName.setPadding(dp(12), 0, dp(12), 0);
        root.addView(bbsName, fieldParams());

        bbsUrl = new EditText(this);
        bbsUrl.setSingleLine(true);
        bbsUrl.setTextSize(14);
        bbsUrl.setTextColor(TEXT);
        bbsUrl.setHint("板URL 例: https://example.net/live/ / Board URL");
        bbsUrl.setImeOptions(EditorInfo.IME_ACTION_DONE);
        bbsUrl.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        bbsUrl.setBackground(roundedField());
        bbsUrl.setPadding(dp(12), 0, dp(12), 0);
        root.addView(bbsUrl, fieldParams());

        addBbsButton = new Button(this);
        addBbsButton.setText("BBSリンクを追加 / Add BBS link");
        addBbsButton.setAllCaps(false);
        addBbsButton.setOnClickListener(v -> addBbsLink());
        root.addView(addBbsButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        bbsList = new LinearLayout(this);
        bbsList.setOrientation(LinearLayout.VERTICAL);
        root.addView(bbsList);
        renderBbsLinks();

        root.addView(sectionTitle("スレ履歴 / Thread History"));
        Button openHistory = new Button(this);
        openHistory.setText("スレ履歴を管理 / Manage thread history");
        openHistory.setAllCaps(false);
        openHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        root.addView(openHistory, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

    }

    private void loadSettings() {
        open5chInNewTab.setChecked(preferences.getBoolean(MainActivity.PREF_5CH_NEW_TAB, true));
        blurImgurImages.setChecked(preferences.getBoolean(MainActivity.PREF_BLUR_IMGUR, true));

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
        blurImgurImages.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings(false));
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
            Toast.makeText(this, "%s を含む検索URLテンプレートを入力 / Enter a search URL template containing %s.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }

        preferences.edit()
                .putBoolean(MainActivity.PREF_5CH_NEW_TAB, open5chInNewTab.isChecked())
                .putBoolean(MainActivity.PREF_BLUR_IMGUR, blurImgurImages.isChecked())
                .putString(MainActivity.PREF_SEARCH_TEMPLATE, template)
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
            Toast.makeText(this, "BBS名と板URLを入力 / Enter a BBS name and board URL.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (editingBbsUrl != null) {
            MainActivity.removeBbsLink(preferences, editingBbsUrl);
        }
        MainActivity.addBbsLink(preferences, name, url);
        editingBbsUrl = null;
        bbsName.setText("");
        bbsUrl.setText("");
        addBbsButton.setText("BBSリンクを追加 / Add BBS link");
        renderBbsLinks();
    }

    private void renderBbsLinks() {
        if (bbsList == null) {
            return;
        }
        bbsList.removeAllViews();
        java.util.List<MainActivity.BbsLink> links = MainActivity.readBbsLinks(preferences);
        if (links.isEmpty()) {
            bbsList.addView(helperText("BBSリンクなし / No BBS links."));
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
            edit.setContentDescription("BBSリンクを編集 / Edit BBS link");
            edit.setColorFilter(TEXT);
            edit.setBackground(roundedField());
            edit.setPadding(dp(10), dp(10), dp(10), dp(10));
            edit.setScaleType(ImageButton.ScaleType.CENTER);
            edit.setOnClickListener(v -> {
                editingBbsUrl = link.url;
                bbsName.setText(link.name);
                bbsUrl.setText(link.url);
                addBbsButton.setText("BBSリンクを更新 / Update BBS link");
            });
            LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(dp(46), dp(44));
            editParams.setMargins(dp(8), 0, 0, 0);
            row.addView(edit, editParams);
            ImageButton delete = new ImageButton(this);
            delete.setImageResource(R.drawable.ic_delete);
            delete.setContentDescription("BBSリンクを削除 / Delete BBS link");
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
                    addBbsButton.setText("BBSリンクを追加 / Add BBS link");
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
