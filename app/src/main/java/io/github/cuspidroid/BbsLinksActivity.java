package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class BbsLinksActivity extends Activity {
    private static final String HISSI_TEMPLATE_EXAMPLE =
            "https://www.kyodemo.net/sdemo/b/e_e_liveedge/?hi={$id}&key={$key}&date={$date[yyyyMMdd]}";
    private static final String[][] RECOMMENDED_CUSTOM_BBS_LINKS = {
            {"まちBBS", "Machi BBS", "https://machi.to/bbsmenu.html"},
            {"したらば掲示板", "Shitaraba", "https://bbs-menu.pages.dev/shitaraba_bbsmenu/bbsmenu.json"},
            {"ふたばちゃんねる", "Futaba Channel", "https://www.2chan.net/bbsmenu.html"},
            {"BBSPINK", "BBSPINK", "https://bbspink.org/ex0ch/bbsmenu.html"},
            {"おーぷん2ちゃんねる", "Open2ch", "https://menu.open2ch.net/bbsmenu.html"},
            {"エッヂ", "Edge", "https://bbs.eddibb.cc/liveedge/"},
            {"AfternoonTea", "AfternoonTea", "https://afternoontea.st/boards/bbsmenu.html"}
    };

    private SharedPreferences preferences;
    private LinearLayout list;
    private ScrollView scrollView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable dragAutoScrollTask;
    private int dragAutoScrollDelta;

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

    @Override
    protected void onDestroy() {
        stopDragAutoScroll();
        super.onDestroy();
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

        ViewGroup addRecommended = addRow(
                MainActivity.text("対応BBSを一括追加", "Add all supported BBS links"),
                MainActivity.text("未追加のカスタムBBSをまとめて追加", "Add all missing recommended custom BBS links"));
        addRecommended.setOnClickListener(v -> confirmAddRecommendedBbsLinks());
        LinearLayout.LayoutParams recommendedParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(64));
        recommendedParams.setMargins(dp(18), 0, dp(18), dp(8));
        root.addView(addRecommended, recommendedParams);

        ScrollView scroll = new ScrollView(this);
        scrollView = scroll;
        scroll.setOnDragListener((v, event) -> handleAutoScrollDrag(v, event));
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(18), 0, dp(18), dp(24));
        list.setOnDragListener((v, event) -> handleDropOnList(event));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    }

    private void confirmAddRecommendedBbsLinks() {
        int missing = missingRecommendedBbsLinks();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("対応BBSを一括追加", "Add all supported BBS links"))
                .setMessage(MainActivity.text(
                        "READMEに掲載している対応BBSのうち、未追加の" + missing + "件を追加します。既存の項目は変更しません。",
                        "Add " + missing + " missing supported BBS links listed in the README. Existing entries will not be changed."))
                .setNegativeButton(MainActivity.text("キャンセル", "Cancel"), null)
                .setPositiveButton(MainActivity.text("追加", "Add"), (d, which) -> addRecommendedBbsLinks())
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private int missingRecommendedBbsLinks() {
        List<MainActivity.BbsLink> existing = MainActivity.readBbsLinks(preferences);
        int missing = 0;
        for (String[] item : RECOMMENDED_CUSTOM_BBS_LINKS) {
            boolean found = false;
            for (MainActivity.BbsLink link : existing) {
                if (item[2].equals(link.url)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missing++;
            }
        }
        return missing;
    }

    private void addRecommendedBbsLinks() {
        List<MainActivity.BbsLink> existing = MainActivity.readBbsLinks(preferences);
        int added = 0;
        for (String[] item : RECOMMENDED_CUSTOM_BBS_LINKS) {
            String url = item[2];
            boolean found = false;
            for (MainActivity.BbsLink link : existing) {
                if (url.equals(link.url)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }
            MainActivity.addBbsLink(preferences, MainActivity.text(item[0], item[1]), url);
            existing.add(new MainActivity.BbsLink(MainActivity.text(item[0], item[1]), url));
            added++;
        }
        renderLinks();
        Toast.makeText(this, added > 0
                        ? MainActivity.text(added + "件のBBSを追加しました", "Added " + added + " BBS links")
                        : MainActivity.text("掲載BBSはすべて追加済みです", "All listed BBS links are already added"),
                Toast.LENGTH_SHORT).show();
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
            row.setPadding(dp(12), dp(10), dp(8), dp(10));
            row.setBackground(rowBackground());
            row.setOnLongClickListener(v -> {
                v.startDragAndDrop(ClipData.newPlainText("bbs-link", link.url),
                        new View.DragShadowBuilder(v), new DragPayload(link.url), 0);
                return true;
            });
            row.setOnDragListener((v, event) -> handleDropOnRow(row, event));

            LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.setGravity(Gravity.CENTER_VERTICAL);
            TextView name = new TextView(this);
            name.setText(link.name);
            name.setTextColor(textColor());
            name.setTextSize(16);
            name.setSingleLine(true);
            name.setEllipsize(TextUtils.TruncateAt.END);
            texts.addView(name);

            TextView url = helperText(link.url);
            url.setTextColor(mutedColor());
            url.setSingleLine(true);
            url.setEllipsize(TextUtils.TruncateAt.END);
            texts.addView(url);

            row.addView(texts, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            ImageButton edit = iconButton(R.drawable.ic_edit, MainActivity.text("\u7de8\u96c6", "Edit"));
            edit.setOnClickListener(v -> showLinkDialog(link));
            row.addView(edit, iconParams());

            ImageButton delete = iconButton(R.drawable.ic_close, MainActivity.text("\u524a\u9664", "Delete"));
            delete.setOnClickListener(v -> confirmDeleteBbsLink(link));
            row.addView(delete, iconParams());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(72));
            params.setMargins(0, 0, 0, dp(8));
            list.addView(row, params);
        }
    }

    private void confirmDeleteBbsLink(MainActivity.BbsLink link) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("BBSリンクを削除", "Delete BBS link"))
                .setMessage(MainActivity.text("「" + link.name + "」を削除しますか？",
                        "Delete \"" + link.name + "\"?"))
                .setNegativeButton(MainActivity.text("キャンセル", "Cancel"), null)
                .setPositiveButton(MainActivity.text("削除", "Delete"), (d, which) -> {
                    MainActivity.removeBbsLink(preferences, link.url);
                    renderLinks();
                })
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, this));
        dialog.show();
    }

    private boolean handleDropOnRow(View row, DragEvent event) {
        if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
            return event.getLocalState() instanceof DragPayload;
        }
        autoScrollDuringDrag(row, event);
        if (event.getAction() != DragEvent.ACTION_DROP) {
            return true;
        }
        DragPayload payload = (DragPayload) event.getLocalState();
        int targetIndex = Math.max(0, list.indexOfChild(row));
        if (event.getY() > row.getHeight() / 2f) {
            targetIndex++;
        }
        moveBbsLink(payload.url, targetIndex);
        return true;
    }

    private boolean handleDropOnList(DragEvent event) {
        if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
            return event.getLocalState() instanceof DragPayload;
        }
        autoScrollDuringDrag(list, event);
        if (event.getAction() != DragEvent.ACTION_DROP) {
            return true;
        }
        DragPayload payload = (DragPayload) event.getLocalState();
        moveBbsLink(payload.url, list.getChildCount());
        return true;
    }

    private boolean handleAutoScrollDrag(View anchor, DragEvent event) {
        if (!(event.getLocalState() instanceof DragPayload)) {
            return false;
        }
        autoScrollDuringDrag(anchor, event);
        return true;
    }

    private void moveBbsLink(String url, int targetIndex) {
        List<MainActivity.BbsLink> links = MainActivity.readBbsLinks(preferences);
        int sourceIndex = -1;
        for (int i = 0; i < links.size(); i++) {
            if (url.equals(links.get(i).url)) {
                sourceIndex = i;
                break;
            }
        }
        if (sourceIndex < 0) {
            return;
        }
        MainActivity.BbsLink link = links.remove(sourceIndex);
        if (sourceIndex < targetIndex) {
            targetIndex--;
        }
        links.add(Math.max(0, Math.min(targetIndex, links.size())), link);
        MainActivity.saveBbsLinks(preferences, links);
        renderLinks();
    }

    private void autoScrollDuringDrag(View anchor, DragEvent event) {
        if (event == null || anchor == null || scrollView == null) {
            return;
        }
        int action = event.getAction();
        if (action == DragEvent.ACTION_DRAG_ENDED || action == DragEvent.ACTION_DROP
                || action == DragEvent.ACTION_DRAG_EXITED) {
            stopDragAutoScroll();
            return;
        }
        if (action != DragEvent.ACTION_DRAG_LOCATION) {
            return;
        }
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        float screenY = location[1] + event.getY();
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int edge = dp(48);
        int maxStep = dp(34);
        int minStep = dp(5);
        int delta = 0;
        if (screenY <= frame.top + edge) {
            float ratio = Math.min(1f, Math.max(0f, (frame.top + edge - screenY) / Math.max(1f, edge)));
            delta = -Math.max(minStep, Math.round(maxStep * ratio));
        } else if (screenY >= frame.bottom - edge) {
            float ratio = Math.min(1f, Math.max(0f, (screenY - (frame.bottom - edge)) / Math.max(1f, edge)));
            delta = Math.max(minStep, Math.round(maxStep * ratio));
        }
        if (delta == 0) {
            stopDragAutoScroll();
            return;
        }
        dragAutoScrollDelta = delta;
        startDragAutoScroll();
    }

    private void startDragAutoScroll() {
        if (dragAutoScrollTask != null) {
            return;
        }
        dragAutoScrollTask = new Runnable() {
            @Override
            public void run() {
                if (scrollView == null || dragAutoScrollDelta == 0) {
                    dragAutoScrollTask = null;
                    return;
                }
                scrollView.scrollBy(0, dragAutoScrollDelta);
                handler.postDelayed(this, 16);
            }
        };
        handler.post(dragAutoScrollTask);
    }

    private void stopDragAutoScroll() {
        dragAutoScrollDelta = 0;
        if (dragAutoScrollTask != null) {
            handler.removeCallbacks(dragAutoScrollTask);
            dragAutoScrollTask = null;
        }
    }

    private void showLinkDialog(MainActivity.BbsLink existing) {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(4), dp(12), 0);
        content.setBackgroundColor(surfaceColor());

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

        TextView hissiLabel = helperText(MainActivity.text(
                "\u5fc5\u6b7b\u30c1\u30a7\u30c3\u30ab\u30fcURL\uff08\u4efb\u610f\uff09",
                "Hissi Checker URL (optional)"));
        hissiLabel.setTextColor(textColor());
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.setMargins(0, dp(14), 0, dp(4));
        content.addView(hissiLabel, labelParams);

        EditText hissiUrl = field(MainActivity.text("URL\u30c6\u30f3\u30d7\u30ec\u30fc\u30c8", "URL template"));
        hissiUrl.setSingleLine(false);
        hissiUrl.setMinLines(2);
        hissiUrl.setMaxLines(4);
        hissiUrl.setGravity(Gravity.TOP | Gravity.START);
        hissiUrl.setImeOptions(EditorInfo.IME_ACTION_DONE);
        hissiUrl.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_URI
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        hissiUrl.setPadding(dp(12), dp(9), dp(12), dp(9));
        LinearLayout.LayoutParams hissiParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(96));
        content.addView(hissiUrl, hissiParams);

        TextView exampleRow = selectableExampleText(MainActivity.text("\u4f8b: ", "Example: ") + HISSI_TEMPLATE_EXAMPLE);
        LinearLayout.LayoutParams exampleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        exampleParams.setMargins(0, dp(8), 0, 0);
        content.addView(exampleRow, exampleParams);

        if (existing != null) {
            name.setText(existing.name);
            url.setText(existing.url);
            url.setSelection(url.length());
            hissiUrl.setText(existing.hissiUrl);
            hissiUrl.setSelection(hissiUrl.length());
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
        dialog.setOnShowListener(d -> {
            Theme.styleDialog(dialog, this);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nameValue = name.getText().toString().trim();
            String urlValue = url.getText().toString().trim();
            String hissiUrlValue = hissiUrl.getText().toString().trim();
            if (nameValue.isEmpty() || urlValue.isEmpty()) {
                Toast.makeText(this, MainActivity.text("BBS\u540d\u3068\u677fURL\u3092\u5165\u529b", "Enter a BBS name and board URL."), Toast.LENGTH_SHORT).show();
                return;
            }
            if (existing != null) {
                MainActivity.removeBbsLink(preferences, existing.url);
            }
            MainActivity.addBbsLink(preferences, nameValue, urlValue, hissiUrlValue);
            renderLinks();
            dialog.dismiss();
            });
        });
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

    private TextView selectableExampleText(String value) {
        TextView view = helperText(value);
        Theme.makeTextSelectable(this, view);
        view.setPadding(dp(10), dp(8), dp(10), dp(8));
        view.setBackground(subtleBoxBackground());
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

    private GradientDrawable subtleBoxBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(bgColor());
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(dp(7));
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
        drawable.setColor(Theme.accent(this));
        drawable.setCornerRadius(dp(13));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class DragPayload {
        final String url;

        DragPayload(String url) {
            this.url = url;
        }
    }
}
