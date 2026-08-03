package io.github.cuspidroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class FavoritePostListActivity extends Activity {
    static final String EXTRA_CATEGORY_ID = "favorite_category_id";

    private SharedPreferences preferences;
    private String categoryId;
    private LinearLayout list;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        buildLayout();
        renderPosts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (list != null) renderPosts();
    }

    private void buildLayout() {
        Theme.applySystemBars(this);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Theme.background(this));
        setContentView(root);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(18), dp(72), dp(18), dp(24));
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(10), 0, dp(10), 0);
        topBar.setBackground(topBarBackground());
        ImageButton back = iconButton(R.drawable.ic_arrow_back, MainActivity.text("戻る", "Back"));
        back.setOnClickListener(v -> finish());
        topBar.addView(back, new LinearLayout.LayoutParams(dp(44), dp(44)));
        title = new TextView(this);
        title.setTextColor(Theme.text(this));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        titleParams.setMargins(dp(8), 0, 0, 0);
        topBar.addView(title, titleParams);
        root.addView(topBar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56), Gravity.TOP));
    }

    private void renderPosts() {
        FavoritePostsStore.Snapshot snapshot = FavoritePostsStore.read(preferences);
        FavoritePostsStore.Category category = FavoritePostsStore.category(snapshot, categoryId);
        if (category == null) {
            finish();
            return;
        }
        title.setText(category.displayName());
        list.removeAllViews();
        int count = 0;
        for (FavoritePostsStore.FavoritePost post : snapshot.posts) {
            if (!category.id.equals(post.categoryId)) continue;
            list.addView(postRow(category, post), rowParams());
            count++;
        }
        if (count == 0) {
            TextView empty = helperText(MainActivity.text(
                    "このカテゴリにレスはありません。", "No posts in this category."));
            empty.setPadding(0, dp(12), 0, dp(12));
            list.addView(empty);
        }
    }

    private FrameLayout postRow(FavoritePostsStore.Category category,
                                FavoritePostsStore.FavoritePost post) {
        FrameLayout shell = new FrameLayout(this);
        shell.setBackground(rowBackground());
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(9), dp(7), dp(9));
        shell.addView(row, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setOnClickListener(v -> openPost(post));
        row.addView(content, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView threadTitle = new TextView(this);
        threadTitle.setText(post.title.trim().isEmpty() ? post.url : post.title);
        threadTitle.setTextColor(Theme.text(this));
        threadTitle.setTextSize(15);
        threadTitle.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(threadTitle);
        TextView meta = helperText(">>" + post.number + formatMeta(post));
        meta.setTextColor(category.color);
        content.addView(meta);
        TextView body = helperText(compact(post.body, 180));
        body.setTextColor(Theme.text(this));
        content.addView(body);

        ImageButton jump = iconButton(R.drawable.ic_arrow_forward,
                MainActivity.text("レスに移動", "Jump to post"));
        jump.setOnClickListener(v -> openPost(post));
        row.addView(jump, new LinearLayout.LayoutParams(dp(40), dp(40)));
        ImageButton delete = iconButton(R.drawable.ic_close,
                MainActivity.text("お気に入りから削除", "Remove favorite"));
        delete.setColorFilter(Theme.muted(this));
        delete.setOnClickListener(v -> {
            FavoritePostsStore.removePost(preferences, post.url, post.number);
            renderPosts();
        });
        row.addView(delete, new LinearLayout.LayoutParams(dp(40), dp(40)));
        return shell;
    }

    private void openPost(FavoritePostsStore.FavoritePost post) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(post.url));
        intent.putExtra(MainActivity.EXTRA_JUMP_POST_NUMBER, post.number);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private String formatMeta(FavoritePostsStore.FavoritePost post) {
        String value = "";
        if (!post.name.trim().isEmpty()) value += "  " + post.name;
        if (!post.date.trim().isEmpty()) value += "  " + post.date;
        return value;
    }

    private String compact(String value, int max) {
        String text = value == null ? "" : value.replace('\r', '\n').trim();
        text = text.replaceAll("\\n{3,}", "\n\n");
        if (text.length() <= max) return text;
        return text.substring(0, Math.max(0, max - 1)).trim() + "…";
    }

    private TextView helperText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(Theme.muted(this));
        view.setTextSize(13);
        view.setPadding(0, dp(2), 0, dp(2));
        return view;
    }

    private ImageButton iconButton(int iconRes, String description) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(iconRes);
        button.setContentDescription(description);
        button.setColorFilter(Theme.accent(this));
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setPadding(dp(10), dp(10), dp(10), dp(10));
        return button;
    }

    private LinearLayout.LayoutParams rowParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(8));
        return params;
    }

    private GradientDrawable topBarBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.topBar(this));
        drawable.setStroke(dp(1), Theme.border(this));
        return drawable;
    }

    private GradientDrawable rowBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.surface(this));
        drawable.setStroke(dp(1), Theme.border(this));
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
