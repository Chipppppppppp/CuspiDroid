package io.github.cuspidroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
    private RadioButton linksInBrowser;
    private RadioButton linksInApp;
    private RadioButton searchFind5chIo;
    private RadioButton searchFind5chNet;
    private RadioButton searchCustom;
    private EditText customTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        buildLayout();
        loadSettings();
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
        title.setText("Settings");
        title.setTextColor(TEXT);
        title.setTextSize(24);
        title.setGravity(Gravity.START);
        title.setPadding(0, 0, 0, dp(16));
        root.addView(title);

        root.addView(sectionTitle("Links"));
        open5chInNewTab = new CheckBox(this);
        open5chInNewTab.setText("Open 5ch links in a new tab");
        open5chInNewTab.setTextColor(TEXT);
        open5chInNewTab.setTextSize(16);
        root.addView(open5chInNewTab);

        TextView otherLinks = helperText("Other links");
        root.addView(otherLinks);
        RadioGroup linkGroup = new RadioGroup(this);
        linkGroup.setOrientation(RadioGroup.VERTICAL);
        linksInBrowser = radio("Browser app");
        linksInApp = radio("This app");
        linkGroup.addView(linksInBrowser);
        linkGroup.addView(linksInApp);
        root.addView(linkGroup);

        root.addView(sectionTitle("Default Search Engine"));
        RadioGroup searchGroup = new RadioGroup(this);
        searchGroup.setOrientation(RadioGroup.VERTICAL);
        searchFind5chIo = radio("find.5ch.io");
        searchFind5chNet = radio("find.5ch.net");
        searchCustom = radio("Custom URL template");
        searchGroup.addView(searchFind5chIo);
        searchGroup.addView(searchFind5chNet);
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

        TextView hint = helperText("Use %s where the encoded query should be inserted.");
        root.addView(hint);

        Button save = new Button(this);
        save.setText("Save");
        save.setAllCaps(false);
        save.setTextSize(16);
        save.setOnClickListener(v -> saveSettings());
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        saveParams.setMargins(0, dp(18), 0, 0);
        root.addView(save, saveParams);
    }

    private void loadSettings() {
        open5chInNewTab.setChecked(preferences.getBoolean(MainActivity.PREF_5CH_NEW_TAB, true));
        if (preferences.getBoolean(MainActivity.PREF_LINKS_IN_APP, false)) {
            linksInApp.setChecked(true);
        } else {
            linksInBrowser.setChecked(true);
        }

        String template = preferences.getString(MainActivity.PREF_SEARCH_TEMPLATE, MainActivity.DEFAULT_SEARCH_TEMPLATE);
        customTemplate.setText(template);
        if (MainActivity.DEFAULT_SEARCH_TEMPLATE.equals(template)) {
            searchFind5chIo.setChecked(true);
        } else if ("https://find.5ch.net/search?STR=%s&TYPE=TITLE&BBS=ALL".equals(template)) {
            searchFind5chNet.setChecked(true);
        } else {
            searchCustom.setChecked(true);
        }
    }

    private void saveSettings() {
        String template;
        if (searchFind5chIo.isChecked()) {
            template = MainActivity.DEFAULT_SEARCH_TEMPLATE;
        } else if (searchFind5chNet.isChecked()) {
            template = "https://find.5ch.net/search?STR=%s&TYPE=TITLE&BBS=ALL";
        } else {
            template = customTemplate.getText().toString().trim();
            if (template.isEmpty()) {
                Toast.makeText(this, "Enter a custom search URL template.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        preferences.edit()
                .putBoolean(MainActivity.PREF_5CH_NEW_TAB, open5chInNewTab.isChecked())
                .putBoolean(MainActivity.PREF_LINKS_IN_APP, linksInApp.isChecked())
                .putString(MainActivity.PREF_SEARCH_TEMPLATE, template)
                .apply();
        Toast.makeText(this, "Settings saved.", Toast.LENGTH_SHORT).show();
        finish();
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
