package io.github.cuspidroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class ListDisplaySettingsActivity extends Activity {
    static final String EXTRA_MODE = "mode";
    static final String MODE_BOARD = "board";
    static final String MODE_TAB = "tab";

    private SharedPreferences preferences;
    private boolean tabMode;
    private CheckBox showBoardName;
    private CheckBox showCreated;
    private CheckBox showUnread;
    private CheckBox showOrder;
    private CheckBox showVelocity;
    private CheckBox showResponses;
    private CheckBox tabSortEnabled;
    private CheckBox bookmarkSortEnabled;
    private RadioGroup sortGroup;
    private RadioButton sortBoardName;
    private RadioButton sortCreated;
    private RadioButton sortUnread;
    private RadioButton sortOrder;
    private RadioButton sortVelocity;
    private RadioButton sortResponses;
    private RadioGroup sortDirectionGroup;
    private RadioButton sortAsc;
    private RadioButton sortDesc;
    private RadioGroup nonThreadPositionGroup;
    private RadioButton nonThreadTop;
    private RadioButton nonThreadBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        tabMode = MODE_TAB.equals(getIntent().getStringExtra(EXTRA_MODE));
        buildLayout();
        loadSettings();
        setupAutoSave();
    }

    @Override
    protected void onPause() {
        saveSettings();
        super.onPause();
    }

    private void buildLayout() {
        Theme.applySystemBars(this);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Theme.background(this));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText(tabMode
                ? MainActivity.text("タブ一覧設定", "Tab List Settings")
                : MainActivity.text("スレ一覧設定", "Thread List Settings"));
        title.setTextColor(Theme.text(this));
        title.setTextSize(24);
        title.setGravity(Gravity.START);
        title.setPadding(0, 0, 0, dp(16));
        root.addView(title);

        root.addView(sectionTitle(MainActivity.text("表示する項目", "Displayed Fields")));
        showBoardName = settingCheckBox(MainActivity.text("板名", "Board name"));
        showCreated = settingCheckBox(MainActivity.text("作成日時", "Created time"));
        showUnread = settingCheckBox(MainActivity.text("未読数", "Unread count"));
        showOrder = settingCheckBox(MainActivity.text("順位", "Rank"));
        showVelocity = settingCheckBox(MainActivity.text("勢い", "Speed"));
        showResponses = settingCheckBox(MainActivity.text("レス数", "Post count"));
        root.addView(showBoardName);
        root.addView(showCreated);
        root.addView(showUnread);
        root.addView(showOrder);
        root.addView(showVelocity);
        root.addView(showResponses);

        if (tabMode) {
            tabSortEnabled = settingCheckBox(MainActivity.text("タブを自動で並べ替え", "Automatically sort tabs"));
            root.addView(tabSortEnabled);
            bookmarkSortEnabled = settingCheckBox(MainActivity.text("\u30d6\u30c3\u30af\u30de\u30fc\u30af\u3092\u81ea\u52d5\u3067\u4e26\u3079\u66ff\u3048", "Automatically sort bookmarks"));
            root.addView(bookmarkSortEnabled);
        }

        root.addView(sectionTitle(MainActivity.text("並べ替え", "Sort")));
        if (tabMode) {
            root.removeView(tabSortEnabled);
            root.addView(tabSortEnabled);
            root.removeView(bookmarkSortEnabled);
            root.addView(bookmarkSortEnabled);
        }
        sortGroup = new RadioGroup(this);
        sortGroup.setOrientation(RadioGroup.VERTICAL);
        sortBoardName = radio(MainActivity.text("板名", "Board name"));
        sortCreated = radio(MainActivity.text("作成日時", "Created time"));
        sortUnread = radio(MainActivity.text("未読数", "Unread count"));
        sortOrder = radio(MainActivity.text("順位", "Rank"));
        sortVelocity = radio(MainActivity.text("勢い", "Speed"));
        sortResponses = radio(MainActivity.text("レス数", "Post count"));
        sortBoardName.setId(View.generateViewId());
        sortCreated.setId(View.generateViewId());
        sortUnread.setId(View.generateViewId());
        sortOrder.setId(View.generateViewId());
        sortVelocity.setId(View.generateViewId());
        sortResponses.setId(View.generateViewId());
        sortGroup.addView(sortBoardName);
        sortGroup.addView(sortCreated);
        sortGroup.addView(sortUnread);
        sortGroup.addView(sortOrder);
        sortGroup.addView(sortVelocity);
        sortGroup.addView(sortResponses);
        root.addView(sortGroup);

        sortDirectionGroup = new RadioGroup(this);
        sortDirectionGroup.setOrientation(RadioGroup.HORIZONTAL);
        sortAsc = radio(MainActivity.text("昇順", "Ascending"));
        sortDesc = radio(MainActivity.text("降順", "Descending"));
        sortAsc.setId(View.generateViewId());
        sortDesc.setId(View.generateViewId());
        sortDirectionGroup.addView(sortAsc, new RadioGroup.LayoutParams(0, dp(44), 1));
        sortDirectionGroup.addView(sortDesc, new RadioGroup.LayoutParams(0, dp(44), 1));
        root.addView(sortDirectionGroup);

        if (tabMode) {
            root.addView(sectionTitle(MainActivity.text("スレ以外のタブ", "Non-thread Tabs")));
            nonThreadPositionGroup = new RadioGroup(this);
            nonThreadPositionGroup.setOrientation(RadioGroup.HORIZONTAL);
            nonThreadTop = radio(MainActivity.text("上に置く", "Place above"));
            nonThreadBottom = radio(MainActivity.text("下に置く", "Place below"));
            nonThreadTop.setId(View.generateViewId());
            nonThreadBottom.setId(View.generateViewId());
            nonThreadPositionGroup.addView(nonThreadTop, new RadioGroup.LayoutParams(0, dp(44), 1));
            nonThreadPositionGroup.addView(nonThreadBottom, new RadioGroup.LayoutParams(0, dp(44), 1));
            root.addView(nonThreadPositionGroup);
        } else {
            root.addView(sectionTitle(MainActivity.text("優先表示", "Priority")));
            root.addView(managementRow(R.drawable.ic_text_fields,
                    MainActivity.text("優先ワードを管理", "Manage priority words"),
                    MainActivity.text("スレ一覧で優先するワードを追加・編集", "Add and edit words prioritized in thread lists"),
                    v -> startActivity(new Intent(this, BoardPriorityRulesActivity.class))));
        }
    }

    private void loadSettings() {
        showBoardName.setChecked(preferences.getBoolean(showBoardNameKey(), tabMode));
        showCreated.setChecked(preferences.getBoolean(showCreatedKey(), !tabMode));
        showUnread.setChecked(preferences.getBoolean(showUnreadKey(), true));
        showOrder.setChecked(preferences.getBoolean(showOrderKey(), !tabMode));
        showVelocity.setChecked(preferences.getBoolean(showVelocityKey(), true));
        showResponses.setChecked(preferences.getBoolean(showResponsesKey(), true));
        setSortRadio(preferences.getString(sortKeyPref(), MainActivity.BOARD_SORT_VELOCITY));
        if (preferences.getBoolean(sortDescPref(), true)) {
            sortDesc.setChecked(true);
        } else {
            sortAsc.setChecked(true);
        }
        if (tabMode) {
            tabSortEnabled.setChecked(preferences.getBoolean(MainActivity.PREF_TAB_SORT_ENABLED, false));
            bookmarkSortEnabled.setChecked(preferences.getBoolean(MainActivity.PREF_BOOKMARK_SORT_ENABLED, false));
            if (preferences.getBoolean(MainActivity.PREF_TAB_NON_THREAD_TOP, true)) {
                nonThreadTop.setChecked(true);
            } else {
                nonThreadBottom.setChecked(true);
            }
            updateTabSortDependentSettings();
        }
    }

    private void setupAutoSave() {
        showBoardName.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());
        showCreated.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());
        showUnread.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());
        showOrder.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());
        showVelocity.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());
        showResponses.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());
        sortGroup.setOnCheckedChangeListener((group, checkedId) -> saveSettings());
        sortDirectionGroup.setOnCheckedChangeListener((group, checkedId) -> saveSettings());
        if (tabMode) {
            tabSortEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateTabSortDependentSettings();
                saveSettings();
            });
            bookmarkSortEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateTabSortDependentSettings();
                saveSettings();
            });
            nonThreadPositionGroup.setOnCheckedChangeListener((group, checkedId) -> saveSettings());
        }
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit()
                .putBoolean(showBoardNameKey(), showBoardName.isChecked())
                .putBoolean(showCreatedKey(), showCreated.isChecked())
                .putBoolean(showUnreadKey(), showUnread.isChecked())
                .putBoolean(showOrderKey(), showOrder.isChecked())
                .putBoolean(showVelocityKey(), showVelocity.isChecked())
                .putBoolean(showResponsesKey(), showResponses.isChecked())
                .putString(sortKeyPref(), selectedSortKey())
                .putBoolean(sortDescPref(), sortDesc.isChecked());
        if (tabMode) {
            editor.putBoolean(MainActivity.PREF_TAB_SORT_ENABLED, tabSortEnabled.isChecked())
                    .putBoolean(MainActivity.PREF_BOOKMARK_SORT_ENABLED, bookmarkSortEnabled.isChecked())
                    .putBoolean(MainActivity.PREF_TAB_NON_THREAD_TOP, nonThreadTop.isChecked());
        } else {
            editor.putBoolean(MainActivity.PREF_BOARD_SORT_BY_SPEED,
                    MainActivity.BOARD_SORT_VELOCITY.equals(selectedSortKey()) && sortDesc.isChecked());
        }
        editor.apply();
    }

    private String showBoardNameKey() {
        return tabMode ? MainActivity.PREF_TAB_SHOW_BOARD_NAME : MainActivity.PREF_BOARD_SHOW_BOARD_NAME;
    }

    private String showCreatedKey() {
        return tabMode ? MainActivity.PREF_TAB_SHOW_CREATED : MainActivity.PREF_BOARD_SHOW_CREATED;
    }

    private String showUnreadKey() {
        return tabMode ? MainActivity.PREF_TAB_SHOW_UNREAD : MainActivity.PREF_BOARD_SHOW_UNREAD;
    }

    private String showOrderKey() {
        return tabMode ? MainActivity.PREF_TAB_SHOW_ORDER : MainActivity.PREF_BOARD_SHOW_ORDER;
    }

    private String showVelocityKey() {
        return tabMode ? MainActivity.PREF_TAB_SHOW_VELOCITY : MainActivity.PREF_BOARD_SHOW_VELOCITY;
    }

    private String showResponsesKey() {
        return tabMode ? MainActivity.PREF_TAB_SHOW_RESPONSES : MainActivity.PREF_BOARD_SHOW_RESPONSES;
    }

    private String sortKeyPref() {
        return tabMode ? MainActivity.PREF_TAB_SORT_KEY : MainActivity.PREF_BOARD_THREAD_SORT_KEY;
    }

    private String sortDescPref() {
        return tabMode ? MainActivity.PREF_TAB_SORT_DESC : MainActivity.PREF_BOARD_THREAD_SORT_DESC;
    }

    private String selectedSortKey() {
        int checkedId = sortGroup.getCheckedRadioButtonId();
        if (checkedId == sortBoardName.getId()) {
            return MainActivity.BOARD_SORT_BOARD_NAME;
        }
        if (checkedId == sortResponses.getId()) {
            return MainActivity.BOARD_SORT_RESPONSES;
        }
        if (checkedId == sortOrder.getId()) {
            return MainActivity.BOARD_SORT_ORDER;
        }
        if (checkedId == sortCreated.getId()) {
            return MainActivity.BOARD_SORT_CREATED;
        }
        if (checkedId == sortUnread.getId()) {
            return MainActivity.BOARD_SORT_UNREAD;
        }
        return MainActivity.BOARD_SORT_VELOCITY;
    }

    private void setSortRadio(String key) {
        if (MainActivity.BOARD_SORT_BOARD_NAME.equals(key)) {
            sortBoardName.setChecked(true);
        } else if (MainActivity.BOARD_SORT_RESPONSES.equals(key)) {
            sortResponses.setChecked(true);
        } else if (MainActivity.BOARD_SORT_ORDER.equals(key)) {
            sortOrder.setChecked(true);
        } else if (MainActivity.BOARD_SORT_CREATED.equals(key)) {
            sortCreated.setChecked(true);
        } else if (MainActivity.BOARD_SORT_UNREAD.equals(key)) {
            sortUnread.setChecked(true);
        } else {
            sortVelocity.setChecked(true);
        }
    }

    private void updateTabSortDependentSettings() {
        boolean enabled = tabSortEnabled != null && tabSortEnabled.isChecked()
                || bookmarkSortEnabled != null && bookmarkSortEnabled.isChecked();
        setGroupEnabled(sortGroup, enabled);
        setGroupEnabled(sortDirectionGroup, enabled);
        setGroupEnabled(nonThreadPositionGroup, tabSortEnabled != null && tabSortEnabled.isChecked());
    }

    private void setGroupEnabled(ViewGroup group, boolean enabled) {
        if (group == null) {
            return;
        }
        group.setEnabled(enabled);
        group.setAlpha(enabled ? 1f : 0.45f);
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setEnabled(enabled);
        }
    }

    private CheckBox settingCheckBox(String label) {
        CheckBox box = new CheckBox(this);
        box.setText(label);
        box.setTextColor(Theme.text(this));
        box.setTextSize(16);
        Theme.tintCompoundButton(this, box);
        return box;
    }

    private RadioButton radio(String label) {
        RadioButton radio = new RadioButton(this);
        radio.setText(label);
        radio.setTextColor(Theme.text(this));
        radio.setTextSize(15);
        Theme.tintCompoundButton(this, radio);
        return radio;
    }

    private TextView sectionTitle(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(Theme.text(this));
        view.setTextSize(16);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setPadding(0, dp(18), 0, dp(8));
        return view;
    }

    private TextView helperText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(Theme.muted(this));
        view.setTextSize(13);
        view.setPadding(0, dp(4), 0, dp(6));
        return view;
    }

    private LinearLayout managementRow(int iconRes, String title, String subtitle, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(10), dp(10));
        row.setBackground(managementCardBackground());
        row.setOnClickListener(listener);
        row.setClickable(true);
        row.setFocusable(true);
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Theme.accent(this));
        icon.setPadding(dp(8), dp(8), dp(8), dp(8));
        icon.setBackground(managementIconBackground());
        row.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setGravity(Gravity.CENTER_VERTICAL);
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(Theme.text(this));
        titleView.setTextSize(16);
        titleView.setSingleLine(true);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        texts.addView(titleView);
        TextView subtitleView = new TextView(this);
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(Theme.muted(this));
        subtitleView.setTextSize(12);
        subtitleView.setSingleLine(true);
        subtitleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        texts.addView(subtitleView);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        textParams.setMargins(dp(12), 0, dp(8), 0);
        row.addView(texts, textParams);

        ImageView arrow = new ImageView(this);
        arrow.setImageResource(R.drawable.ic_arrow_forward);
        arrow.setColorFilter(Theme.muted(this));
        row.addView(arrow, new LinearLayout.LayoutParams(dp(24), dp(24)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(68));
        params.setMargins(0, dp(4), 0, dp(8));
        row.setLayoutParams(params);
        return row;
    }

    private GradientDrawable rowBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.surface(this));
        drawable.setStroke(dp(1), Theme.border(this));
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private GradientDrawable managementCardBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.surface(this));
        drawable.setStroke(dp(1), Theme.border(this));
        drawable.setCornerRadius(dp(14));
        return drawable;
    }

    private GradientDrawable managementIconBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.active(this));
        drawable.setCornerRadius(dp(13));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
