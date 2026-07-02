package io.github.cuspidroid;

import android.app.Activity;
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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ButtonLayoutSettingsActivity extends Activity {
    static final String EXTRA_MODE = "mode";
    static final String MODE_ADDRESS = "address";
    static final String MODE_TITLE = "title";

    private static final String LIST_ADDRESS_MENU = "address_menu";
    private static final String LIST_ADDRESS_BAR = "address_bar";
    private static final String LIST_ADDRESS_NAV = "address_nav";
    private static final String LIST_TITLE_BAR = "title_bar";
    private static final String LIST_TITLE_MENU = "title_menu";
    private static final String LIST_HIDDEN = "hidden";

    private SharedPreferences preferences;
    private String mode;
    private LinearLayout firstList;
    private LinearLayout secondList;
    private LinearLayout thirdList;
    private LinearLayout hiddenList;
    private ScrollView scrollView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable dragAutoScrollTask;
    private int dragAutoScrollDelta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (!MODE_TITLE.equals(mode)) {
            mode = MODE_ADDRESS;
        }
        buildLayout();
    }

    @Override
    protected void onDestroy() {
        stopDragAutoScroll();
        super.onDestroy();
    }

    private void buildLayout() {
        Theme.applySystemBars(this);
        ScrollView scroll = new ScrollView(this);
        scrollView = scroll;
        scroll.setBackgroundColor(bgColor());
        scroll.setOnDragListener((v, event) -> handleAutoScrollDrag(v, event));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        root.setOnDragListener((v, event) -> handleAutoScrollDrag(v, event));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText(MODE_TITLE.equals(mode)
                ? MainActivity.text("\u30bf\u30a4\u30c8\u30eb\u30d0\u30fc\u30e1\u30cb\u30e5\u30fc\u914d\u7f6e", "Title bar menu layout")
                : MainActivity.text("\u691c\u7d22\u30d0\u30fc\u30e1\u30cb\u30e5\u30fc\u914d\u7f6e", "Search bar menu layout"));
        title.setTextColor(textColor());
        title.setTextSize(24);
        title.setPadding(0, 0, 0, dp(10));
        title.setOnDragListener((v, event) -> handleAutoScrollDrag(v, event));
        root.addView(title);
        root.addView(helper(MainActivity.text(
                "\u9577\u62bc\u3057\u3057\u3066\u304b\u3089\u30c9\u30e9\u30c3\u30b0\u3057\u307e\u3059\u3002\u30c1\u30a7\u30c3\u30af\u3092\u5916\u3059\u3068\u975e\u8868\u793a\u306b\u79fb\u52d5\u3057\u307e\u3059\u3002",
                "Long-press, then drag. Unchecking an item moves it to Hidden.")));

        if (MODE_TITLE.equals(mode)) {
            root.addView(sectionTitle(MainActivity.text("\u30bf\u30a4\u30c8\u30eb\u30d0\u30fc\u306b\u5e38\u99d0", "Pinned on title bar")));
            firstList = listBox(LIST_TITLE_BAR);
            root.addView(firstList);
            root.addView(sectionTitle(MainActivity.text("\u30bf\u30a4\u30c8\u30eb\u30e1\u30cb\u30e5\u30fc", "Title menu")));
            secondList = listBox(LIST_TITLE_MENU);
            root.addView(secondList);
        } else {
            root.addView(sectionTitle(MainActivity.text("\u691c\u7d22\u30d0\u30fc\u306b\u5e38\u99d0", "Pinned on search bar")));
            firstList = listBox(LIST_ADDRESS_BAR);
            root.addView(firstList);
            root.addView(sectionTitle(MainActivity.text("\u691c\u7d22\u30d0\u30fc\u30e1\u30cb\u30e5\u30fc", "Search bar menu")));
            secondList = listBox(LIST_ADDRESS_MENU);
            root.addView(secondList);
            root.addView(sectionTitle(MainActivity.text("\u30ca\u30d3\u30b2\u30fc\u30b7\u30e7\u30f3\u884c", "Navigation row")));
            thirdList = listBox(LIST_ADDRESS_NAV);
            root.addView(thirdList);
        }
        root.addView(sectionTitle(MainActivity.text("\u975e\u8868\u793a", "Hidden")));
        hiddenList = listBox(LIST_HIDDEN);
        root.addView(hiddenList);

        rebuildLists();
    }

    private void rebuildLists() {
        firstList.removeAllViews();
        secondList.removeAllViews();
        if (thirdList != null) thirdList.removeAllViews();
        hiddenList.removeAllViews();
        if (MODE_TITLE.equals(mode)) {
            List<String> bar = read(MainActivity.PREF_THREAD_TITLE_BAR_BUTTONS,
                    MainActivity.DEFAULT_THREAD_TITLE_BAR_BUTTONS, MainActivity.THREAD_TITLE_BUTTON_IDS);
            List<String> menu = read(MainActivity.PREF_THREAD_TITLE_MENU_BUTTONS,
                    MainActivity.DEFAULT_THREAD_TITLE_MENU_BUTTONS, MainActivity.THREAD_TITLE_BUTTON_IDS);
            menu.remove(MainActivity.THREAD_BUTTON_MENU);
            for (String id : bar) firstList.addView(row(id, true, LIST_TITLE_BAR));
            for (String id : menu) secondList.addView(row(id, true, LIST_TITLE_MENU));
            for (String id : MainActivity.THREAD_TITLE_BUTTON_IDS) {
                if (!bar.contains(id) && !menu.contains(id)) hiddenList.addView(row(id, false, LIST_HIDDEN));
            }
        } else {
            List<String> bar = read(MainActivity.PREF_ADDRESS_BAR_BUTTONS,
                    MainActivity.DEFAULT_ADDRESS_BAR_BUTTONS, MainActivity.ADDRESS_BUTTON_IDS);
            List<String> menu = read(MainActivity.PREF_ADDRESS_MENU_BUTTONS,
                    MainActivity.DEFAULT_ADDRESS_MENU_BUTTONS, MainActivity.ADDRESS_BUTTON_IDS);
            List<String> nav = read(MainActivity.PREF_ADDRESS_NAV_BUTTONS,
                    MainActivity.DEFAULT_ADDRESS_NAV_BUTTONS, MainActivity.ADDRESS_BUTTON_IDS);
            removeDuplicates(menu, bar);
            removeDuplicates(nav, bar);
            removeDuplicates(nav, menu);
            menu.remove(MainActivity.ADDRESS_BAR_MENU);
            nav.remove(MainActivity.ADDRESS_BAR_MENU);
            if (!bar.contains(MainActivity.ADDRESS_BAR_MENU)) {
                bar.add(MainActivity.ADDRESS_BAR_MENU);
            }
            if (!bar.contains(MainActivity.ADDRESS_MENU_SETTINGS)
                    && !menu.contains(MainActivity.ADDRESS_MENU_SETTINGS)
                    && !nav.contains(MainActivity.ADDRESS_MENU_SETTINGS)) {
                menu.add(MainActivity.ADDRESS_MENU_SETTINGS);
            }
            for (String id : bar) firstList.addView(row(id, true, LIST_ADDRESS_BAR));
            for (String id : menu) secondList.addView(row(id, true, LIST_ADDRESS_MENU));
            for (String id : nav) thirdList.addView(row(id, true, LIST_ADDRESS_NAV));
            for (String id : MainActivity.ADDRESS_BUTTON_IDS) {
                if (!bar.contains(id) && !menu.contains(id) && !nav.contains(id)) {
                    hiddenList.addView(row(id, false, LIST_HIDDEN));
                }
            }
        }
    }

    private View row(String id, boolean checked, String listKey) {
        LinearLayout row = new LinearLayout(this);
        row.setTag(id);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(8), dp(8), dp(8));
        row.setBackground(rowBackground());

        CheckBox box = new CheckBox(this);
        box.setChecked(checked);
        if (isRequiredAddressButton(id)) {
            box.setChecked(true);
            box.setEnabled(false);
            box.setAlpha(0.45f);
        }
        Theme.tintCompoundButton(this, box);
        box.setOnCheckedChangeListener((buttonView, isChecked) -> setVisible(id, isChecked, listKey));
        row.addView(box, new LinearLayout.LayoutParams(dp(42), dp(42)));

        row.addView(rowIcon(id), new LinearLayout.LayoutParams(dp(22), dp(22)));

        TextView label = new TextView(this);
        label.setText(labelFor(id));
        label.setTextColor(textColor());
        label.setTextSize(15);
        label.setPadding(dp(10), 0, dp(8), 0);
        row.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(58));
        params.setMargins(0, 0, 0, dp(6));
        row.setLayoutParams(params);
        row.setOnLongClickListener(v -> {
            DragPayload payload = new DragPayload(id, listKey);
            v.startDragAndDrop(ClipData.newPlainText("menu-item", id),
                    new View.DragShadowBuilder(v), payload, 0);
            return true;
        });
        row.setOnDragListener((v, event) -> handleDropOnRow(listKey, row, event));
        return row;
    }

    private LinearLayout listBox(String listKey) {
        LinearLayout list = new LinearLayout(this);
        list.setTag(listKey);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(8), dp(8), dp(8), dp(2));
        list.setBackground(boxBackground());
        list.setOnDragListener((v, event) -> handleDropOnList(listKey, event));
        return list;
    }

    private boolean handleDropOnRow(String targetList, View row, DragEvent event) {
        if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) return event.getLocalState() instanceof DragPayload;
        autoScrollDuringDrag(row, event);
        if (event.getAction() != DragEvent.ACTION_DROP) return true;
        DragPayload payload = (DragPayload) event.getLocalState();
        move(payload.id, payload.listKey, targetList, indexOfRow(targetList, row));
        return true;
    }

    private boolean handleDropOnList(String targetList, DragEvent event) {
        if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) return event.getLocalState() instanceof DragPayload;
        autoScrollDuringDrag(listFor(targetList), event);
        if (event.getAction() != DragEvent.ACTION_DROP) return true;
        DragPayload payload = (DragPayload) event.getLocalState();
        move(payload.id, payload.listKey, targetList, childCount(targetList));
        return true;
    }

    private boolean handleAutoScrollDrag(View anchor, DragEvent event) {
        if (!(event.getLocalState() instanceof DragPayload)) {
            return false;
        }
        autoScrollDuringDrag(anchor, event);
        return true;
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
        int nextDelta = 0;
        if (screenY <= frame.top + edge) {
            float ratio = Math.min(1f, Math.max(0f, (frame.top + edge - screenY) / Math.max(1f, edge)));
            nextDelta = -Math.max(minStep, Math.round(maxStep * ratio));
        } else if (screenY >= frame.bottom - edge) {
            float ratio = Math.min(1f, Math.max(0f, (screenY - (frame.bottom - edge)) / Math.max(1f, edge)));
            nextDelta = Math.max(minStep, Math.round(maxStep * ratio));
        }
        if (nextDelta == 0) {
            stopDragAutoScroll();
            return;
        }
        dragAutoScrollDelta = nextDelta;
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

    private int indexOfRow(String listKey, View row) {
        LinearLayout list = listFor(listKey);
        return list == null ? 0 : Math.max(0, list.indexOfChild(row));
    }

    private int childCount(String listKey) {
        LinearLayout list = listFor(listKey);
        return list == null ? 0 : list.getChildCount();
    }

    private LinearLayout listFor(String listKey) {
        if (LIST_HIDDEN.equals(listKey)) return hiddenList;
        if (MODE_TITLE.equals(mode)) {
            if (LIST_TITLE_BAR.equals(listKey)) return firstList;
            if (LIST_TITLE_MENU.equals(listKey)) return secondList;
        } else {
            if (LIST_ADDRESS_BAR.equals(listKey)) return firstList;
            if (LIST_ADDRESS_MENU.equals(listKey)) return secondList;
            if (LIST_ADDRESS_NAV.equals(listKey)) return thirdList;
        }
        return null;
    }

    private void move(String id, String from, String to, int index) {
        String target = normalizedTarget(id, to);
        if (target == null) return;
        if (target.equals(from) && LIST_HIDDEN.equals(target)) return;
        List<String> first = idsFrom(firstList);
        List<String> second = idsFrom(secondList);
        List<String> third = idsFrom(thirdList);
        first.remove(id);
        second.remove(id);
        third.remove(id);
        if (!LIST_HIDDEN.equals(target)) {
            List<String> destination = firstListKey().equals(target) ? first
                    : secondListKey().equals(target) ? second : third;
            destination.add(Math.max(0, Math.min(index, destination.size())), id);
        }
        save(first, second, third);
        rebuildLists();
    }

    private String normalizedTarget(String id, String requested) {
        if (MODE_TITLE.equals(mode)) {
            if (MainActivity.THREAD_BUTTON_MENU.equals(id)
                    && !LIST_TITLE_BAR.equals(requested) && !LIST_HIDDEN.equals(requested)) {
                return null;
            }
            if (LIST_TITLE_BAR.equals(requested) || LIST_TITLE_MENU.equals(requested) || LIST_HIDDEN.equals(requested)) {
                return requested;
            }
            return null;
        }
        if (isRequiredAddressButton(id) && LIST_HIDDEN.equals(requested)) {
            return null;
        }
        if (LIST_HIDDEN.equals(requested)) return requested;
        if (MainActivity.ADDRESS_BAR_MENU.equals(id) && !LIST_ADDRESS_BAR.equals(requested)) {
            return null;
        }
        if (LIST_ADDRESS_BAR.equals(requested) || LIST_ADDRESS_MENU.equals(requested) || LIST_ADDRESS_NAV.equals(requested)) {
            return requested;
        }
        return null;
    }

    private String firstListKey() {
        return MODE_TITLE.equals(mode) ? LIST_TITLE_BAR : LIST_ADDRESS_BAR;
    }

    private String secondListKey() {
        return MODE_TITLE.equals(mode) ? LIST_TITLE_MENU : LIST_ADDRESS_MENU;
    }

    private void setVisible(String id, boolean visible, String currentList) {
        if (!visible && isRequiredAddressButton(id)) {
            rebuildLists();
            return;
        }
        String target;
        if (!visible) {
            target = LIST_HIDDEN;
        } else if (MODE_TITLE.equals(mode)) {
            target = MainActivity.THREAD_BUTTON_MENU.equals(id) ? LIST_TITLE_BAR
                    : LIST_HIDDEN.equals(currentList) ? LIST_TITLE_MENU : currentList;
        } else {
            target = MainActivity.ADDRESS_BAR_MENU.equals(id) ? LIST_ADDRESS_BAR
                    : LIST_HIDDEN.equals(currentList) ? LIST_ADDRESS_MENU : currentList;
        }
        move(id, currentList, target, childCount(target));
    }

    private void save(List<String> first, List<String> second, List<String> third) {
        SharedPreferences.Editor editor = preferences.edit();
        if (MODE_TITLE.equals(mode)) {
            editor.putString(MainActivity.PREF_THREAD_TITLE_BAR_BUTTONS, MainActivity.joinButtonIds(first))
                    .putString(MainActivity.PREF_THREAD_TITLE_MENU_BUTTONS, MainActivity.joinButtonIds(second));
        } else {
            editor.putString(MainActivity.PREF_ADDRESS_BAR_BUTTONS, MainActivity.joinButtonIds(first))
                    .putString(MainActivity.PREF_ADDRESS_MENU_BUTTONS, MainActivity.joinButtonIds(second))
                    .putString(MainActivity.PREF_ADDRESS_NAV_BUTTONS, MainActivity.joinButtonIds(third));
        }
        editor.apply();
    }

    private List<String> read(String key, String fallback, String[] allowed) {
        return MainActivity.orderedButtonIds(preferences.getString(key, fallback), fallback, allowed);
    }

    private void removeDuplicates(List<String> target, List<String> existing) {
        target.removeAll(existing);
    }

    private boolean isRequiredAddressButton(String id) {
        return MODE_ADDRESS.equals(mode)
                && (MainActivity.ADDRESS_BAR_MENU.equals(id) || MainActivity.ADDRESS_MENU_SETTINGS.equals(id));
    }

    private List<String> idsFrom(LinearLayout list) {
        List<String> ids = new ArrayList<>();
        if (list == null) return ids;
        for (int i = 0; i < list.getChildCount(); i++) {
            Object tag = list.getChildAt(i).getTag();
            if (tag instanceof String) ids.add((String) tag);
        }
        return ids;
    }

    private CharSequence labelFor(String id) {
        if (MainActivity.ADDRESS_MENU_WEBVIEW.equals(id)) return MainActivity.text("WebView\u3067\u958b\u304f", "Open in WebView");
        if (MainActivity.ADDRESS_MENU_BOOKMARK.equals(id)) return MainActivity.text("\u30d6\u30c3\u30af\u30de\u30fc\u30af", "Bookmark");
        if (MainActivity.ADDRESS_MENU_FIND.equals(id)) return MainActivity.text("\u30da\u30fc\u30b8\u5185\u691c\u7d22", "Find in page");
        if (MainActivity.ADDRESS_MENU_SYNC.equals(id)) return MainActivity.text("Sync2ch\u3067\u540c\u671f", "Sync with Sync2ch");
        if (MainActivity.ADDRESS_MENU_SETTINGS.equals(id)) return MainActivity.text("\u8a2d\u5b9a", "Settings");
        if (MainActivity.ADDRESS_MENU_NAV.equals(id)) return MainActivity.text("\u30ca\u30d3\u30b2\u30fc\u30b7\u30e7\u30f3", "Navigation");
        if (MainActivity.ADDRESS_BAR_NEW_TAB.equals(id)) return MainActivity.text("\u65b0\u898f\u30bf\u30d6", "New tab");
        if (MainActivity.ADDRESS_BAR_TABS.equals(id)) return MainActivity.text("\u30bf\u30d6\u4e00\u89a7", "Tabs");
        if (MainActivity.ADDRESS_BAR_MENU.equals(id)) return MainActivity.text("\u30e1\u30cb\u30e5\u30fc", "Menu");
        if (MainActivity.ADDRESS_NAV_BACK.equals(id)) return MainActivity.text("\u623b\u308b", "Back");
        if (MainActivity.ADDRESS_NAV_FORWARD.equals(id)) return MainActivity.text("\u9032\u3080", "Forward");
        if (MainActivity.ADDRESS_NAV_SHARE.equals(id)) return MainActivity.text("\u5171\u6709", "Share");
        if (MainActivity.ADDRESS_NAV_RELOAD.equals(id)) return MainActivity.text("\u66f4\u65b0", "Reload");
        if (MainActivity.THREAD_BUTTON_WRITE.equals(id)) return MainActivity.text("\u66f8\u304d\u8fbc\u307f", "Write");
        if (MainActivity.THREAD_BUTTON_JUMP.equals(id)) return MainActivity.text("\u30b9\u30ec\u79fb\u52d5", "Thread navigation");
        if (MainActivity.THREAD_BUTTON_MENU.equals(id)) return MainActivity.text("\u30b9\u30ec\u30e1\u30cb\u30e5\u30fc", "Thread menu");
        if (MainActivity.THREAD_BUTTON_BOARD.equals(id)) return MainActivity.text("\u677f\u3078", "Go to board");
        if (MainActivity.THREAD_BUTTON_NEXT.equals(id)) return MainActivity.text("\u6b21\u30b9\u30ec\u691c\u7d22", "Search next thread");
        if (MainActivity.THREAD_BUTTON_NG.equals(id)) return MainActivity.text("NGThread\u306b\u8ffd\u52a0", "Add to NGThread");
        if (MainActivity.THREAD_BUTTON_MEDIA.equals(id)) return MainActivity.text("\u30e1\u30c7\u30a3\u30a2", "Media");
        if (MainActivity.THREAD_BUTTON_LINKS.equals(id)) return MainActivity.text("\u30ea\u30f3\u30af", "Links");
        if (MainActivity.THREAD_BUTTON_COPY.equals(id)) return MainActivity.text("\u30b3\u30d4\u30fc", "Copy");
        return id;
    }

    private int iconFor(String id) {
        if (MainActivity.ADDRESS_MENU_WEBVIEW.equals(id)) return R.drawable.ic_arrow_forward;
        if (MainActivity.ADDRESS_MENU_BOOKMARK.equals(id)) return R.drawable.ic_star_border;
        if (MainActivity.ADDRESS_MENU_FIND.equals(id)) return R.drawable.ic_search;
        if (MainActivity.ADDRESS_MENU_SYNC.equals(id)) return R.drawable.ic_refresh;
        if (MainActivity.ADDRESS_MENU_SETTINGS.equals(id)) return R.drawable.ic_settings;
        if (MainActivity.ADDRESS_MENU_NAV.equals(id)) return R.drawable.ic_arrow_back;
        if (MainActivity.ADDRESS_BAR_NEW_TAB.equals(id)) return R.drawable.ic_add;
        if (MainActivity.ADDRESS_BAR_TABS.equals(id)) return R.drawable.ic_folder;
        if (MainActivity.ADDRESS_BAR_MENU.equals(id)) return R.drawable.ic_more_vert;
        if (MainActivity.ADDRESS_NAV_BACK.equals(id)) return R.drawable.ic_arrow_back;
        if (MainActivity.ADDRESS_NAV_FORWARD.equals(id)) return R.drawable.ic_arrow_forward;
        if (MainActivity.ADDRESS_NAV_SHARE.equals(id)) return R.drawable.ic_share;
        if (MainActivity.ADDRESS_NAV_RELOAD.equals(id)) return R.drawable.ic_refresh;
        if (MainActivity.THREAD_BUTTON_WRITE.equals(id)) return R.drawable.ic_edit;
        if (MainActivity.THREAD_BUTTON_JUMP.equals(id)) return R.drawable.ic_jump_arrow;
        if (MainActivity.THREAD_BUTTON_MENU.equals(id)) return R.drawable.ic_more_vert;
        if (MainActivity.THREAD_BUTTON_BOARD.equals(id)) return R.drawable.ic_arrow_up;
        if (MainActivity.THREAD_BUTTON_NEXT.equals(id)) return R.drawable.ic_search_next;
        if (MainActivity.THREAD_BUTTON_NG.equals(id)) return R.drawable.ic_close;
        if (MainActivity.THREAD_BUTTON_MEDIA.equals(id)) return R.drawable.ic_image;
        if (MainActivity.THREAD_BUTTON_LINKS.equals(id)) return R.drawable.ic_link;
        if (MainActivity.THREAD_BUTTON_COPY.equals(id)) return R.drawable.ic_copy;
        return R.drawable.ic_more_vert;
    }

    private View rowIcon(String id) {
        if (MainActivity.ADDRESS_BAR_TABS.equals(id)) {
            TextView icon = new TextView(this);
            icon.setText("1");
            icon.setTextColor(textColor());
            icon.setTextSize(11);
            icon.setGravity(Gravity.CENTER);
            icon.setBackground(tabIconBackground(false));
            return icon;
        }
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconFor(id));
        icon.setColorFilter(textColor());
        return icon;
    }

    private GradientDrawable tabIconBackground(boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(selected ? Theme.active(this) : Color.TRANSPARENT);
        drawable.setStroke(dp(2), textColor());
        drawable.setCornerRadius(dp(4));
        return drawable;
    }

    private TextView sectionTitle(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(textColor());
        view.setTextSize(17);
        view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        view.setPadding(0, dp(18), 0, dp(8));
        view.setOnDragListener((v, event) -> handleAutoScrollDrag(v, event));
        return view;
    }

    private TextView helper(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(mutedColor());
        view.setTextSize(13);
        view.setPadding(0, 0, 0, dp(6));
        view.setOnDragListener((v, event) -> handleAutoScrollDrag(v, event));
        return view;
    }

    private GradientDrawable boxBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(surfaceColor());
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private GradientDrawable rowBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(bgColor());
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private int bgColor() { return Theme.background(this); }
    private int surfaceColor() { return Theme.surface(this); }
    private int textColor() { return Theme.text(this); }
    private int mutedColor() { return Theme.muted(this); }
    private int borderColor() { return Theme.border(this); }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }

    private static class DragPayload {
        final String id;
        final String listKey;
        DragPayload(String id, String listKey) {
            this.id = id;
            this.listKey = listKey;
        }
    }
}
