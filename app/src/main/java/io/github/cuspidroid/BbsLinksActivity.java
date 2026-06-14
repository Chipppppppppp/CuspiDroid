package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class BbsLinksActivity extends Activity {
    private SharedPreferences preferences;
    private LinearLayout list;

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
        renderLinks();
    }

    private void buildLayout() {
        Theme.applySystemBars(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bgColor());
        setContentView(root);

        TextView title = new TextView(this);
        title.setText(MainActivity.text("BBS\u30ea\u30f3\u30af", "BBS Links"));
        title.setTextColor(textColor());
        title.setTextSize(24);
        title.setPadding(dp(18), dp(18), dp(18), dp(10));
        root.addView(title);

        ViewGroup add = addRow(MainActivity.text("BBS\u30ea\u30f3\u30af\u3092\u8ffd\u52a0", "Add BBS link"),
                MainActivity.text("\u540d\u524d\u3068\u677fURL\u3092\u5165\u529b", "Enter a name and board URL"));
        add.setOnClickListener(v -> showLinkDialog(null));
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(64));
        addParams.setMargins(dp(18), 0, dp(18), dp(8));
        root.addView(add, addParams);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(18), 0, dp(18), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private void renderLinks() {
        list.removeAllViews();
        List<MainActivity.BbsLink> links = MainActivity.readBbsLinks(preferences);
        if (links.isEmpty()) {
            list.addView(helperText(MainActivity.text("BBS\u30ea\u30f3\u30af\u306a\u3057", "No BBS links.")));
            return;
        }
        for (MainActivity.BbsLink link : links) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(8), dp(8), dp(8));
            row.setBackground(rowBackground());

            TextView text = helperText(link.name + "\n" + link.url);
            text.setTextColor(textColor());
            row.addView(text, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            ImageButton edit = iconButton(R.drawable.ic_edit, MainActivity.text("\u7de8\u96c6", "Edit"));
            edit.setOnClickListener(v -> showLinkDialog(link));
            row.addView(edit, iconParams());

            ImageButton delete = iconButton(R.drawable.ic_close, MainActivity.text("\u524a\u9664", "Delete"));
            delete.setOnClickListener(v -> {
                MainActivity.removeBbsLink(preferences, link.url);
                renderLinks();
            });
            row.addView(delete, iconParams());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, dp(8));
            list.addView(row, params);
        }
    }

    private void showLinkDialog(MainActivity.BbsLink existing) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(4), dp(12), 0);

        EditText name = field(MainActivity.text("\u540d\u524d", "Name"));
        content.addView(name, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));

        EditText url = field(MainActivity.text("\u677fURL", "Board URL"));
        url.setImeOptions(EditorInfo.IME_ACTION_DONE);
        url.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        LinearLayout.LayoutParams urlParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46));
        urlParams.setMargins(0, dp(10), 0, 0);
        content.addView(url, urlParams);

        if (existing != null) {
            name.setText(existing.name);
            url.setText(existing.url);
            url.setSelection(url.length());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existing == null
                        ? MainActivity.text("BBS\u30ea\u30f3\u30af\u3092\u8ffd\u52a0", "Add BBS link")
                        : MainActivity.text("BBS\u30ea\u30f3\u30af\u3092\u7de8\u96c6", "Edit BBS link"))
                .setView(content)
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setPositiveButton(existing == null
                        ? MainActivity.text("\u8ffd\u52a0", "Add")
                        : MainActivity.text("\u66f4\u65b0", "Update"), null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nameValue = name.getText().toString().trim();
            String urlValue = url.getText().toString().trim();
            if (nameValue.isEmpty() || urlValue.isEmpty()) {
                Toast.makeText(this, MainActivity.text("BBS\u540d\u3068\u677fURL\u3092\u5165\u529b", "Enter a BBS name and board URL."), Toast.LENGTH_SHORT).show();
                return;
            }
            if (existing != null) {
                MainActivity.removeBbsLink(preferences, existing.url);
            }
            MainActivity.addBbsLink(preferences, nameValue, urlValue);
            renderLinks();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private EditText field(String hint) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(15);
        input.setTextColor(textColor());
        input.setHintTextColor(hintColor());
        input.setHint(hint);
        input.setBackground(fieldBackground());
        input.setPadding(dp(12), 0, dp(12), 0);
        return input;
    }

    private ViewGroup addRow(String title, String subtitle) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(8), dp(12), dp(8));
        row.setBackground(addBackground());
        row.setClickable(true);
        row.setFocusable(true);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_add);
        icon.setColorFilter(Color.WHITE);
        icon.setPadding(dp(8), dp(8), dp(8), dp(8));
        icon.setBackground(addIconBackground());
        row.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setGravity(Gravity.CENTER_VERTICAL);
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(textColor());
        titleView.setTextSize(16);
        texts.addView(titleView);
        TextView subtitleView = new TextView(this);
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(mutedColor());
        subtitleView.setTextSize(12);
        texts.addView(subtitleView);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        textParams.setMargins(dp(12), 0, 0, 0);
        row.addView(texts, textParams);
        return row;
    }

    private TextView helperText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(mutedColor());
        view.setTextSize(14);
        view.setPadding(0, dp(4), 0, dp(4));
        return view;
    }

    private ImageButton iconButton(int iconRes, String description) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(iconRes);
        button.setContentDescription(description);
        button.setColorFilter(textColor());
        button.setBackground(iconButtonBackground());
        button.setPadding(dp(10), dp(10), dp(10), dp(10));
        button.setScaleType(ImageButton.ScaleType.CENTER);
        return button;
    }

    private LinearLayout.LayoutParams iconParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(42), dp(40));
        params.setMargins(dp(8), 0, 0, 0);
        return params;
    }

    private GradientDrawable rowBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(surfaceColor());
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private GradientDrawable fieldBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(surfaceColor());
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private GradientDrawable iconButtonBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.TRANSPARENT);
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private GradientDrawable addBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(surfaceColor());
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(dp(14));
        return drawable;
    }

    private GradientDrawable addIconBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.rgb(15, 118, 110));
        drawable.setCornerRadius(dp(13));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
