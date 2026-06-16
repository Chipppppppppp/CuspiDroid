package io.github.cuspidroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class DebugSettingsActivity extends Activity {
    private SharedPreferences preferences;
    private CheckBox aaDebug;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        buildLayout();
        loadSettings();
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
        title.setText(MainActivity.text("\u30c7\u30d0\u30c3\u30b0\u8a2d\u5b9a", "Debug settings"));
        title.setTextColor(textColor());
        title.setTextSize(24);
        title.setGravity(Gravity.START);
        title.setPadding(0, 0, 0, dp(16));
        root.addView(title);

        TextView note = helperText(MainActivity.text(
                "\u3053\u306e\u753b\u9762\u306e\u9805\u76ee\u306f\u8abf\u67fb\u7528\u3067\u3059\u3002\u901a\u5e38\u306f\u30aa\u30d5\u306e\u307e\u307e\u4f7f\u7528\u3057\u3066\u304f\u3060\u3055\u3044\u3002",
                "These options are for diagnostics. Leave them off during normal use."));
        root.addView(note);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.setBackground(roundedCard());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, dp(12), 0, 0);
        root.addView(card, cardParams);

        aaDebug = new CheckBox(this);
        aaDebug.setText(MainActivity.text("AA\u5224\u5b9a\u306e\u30c7\u30d0\u30c3\u30b0\u8868\u793a", "Show AA detection debug info"));
        aaDebug.setTextColor(textColor());
        aaDebug.setTextSize(16);
        Theme.tintCompoundButton(this, aaDebug);
        aaDebug.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.edit().putBoolean(MainActivity.PREF_AA_DEBUG, isChecked).apply());
        card.addView(aaDebug);

        card.addView(helperText(MainActivity.text(
                "\u30b9\u30ec\u306e\u66f8\u304d\u8fbc\u307f\u4e0b\u306b\u3001AA\u5224\u5b9a\u306b\u4f7f\u3063\u305f\u884c\u6570\u3068\u5272\u5408\u3092\u8868\u793a\u3057\u307e\u3059\u3002",
                "Shows the line counts and ratio used for AA detection under each post.")));
    }

    private void loadSettings() {
        aaDebug.setChecked(preferences.getBoolean(MainActivity.PREF_AA_DEBUG, false));
    }

    private TextView helperText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(mutedColor());
        view.setTextSize(13);
        view.setPadding(0, dp(4), 0, dp(4));
        return view;
    }

    private GradientDrawable roundedCard() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(surfaceColor());
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(dp(14));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
