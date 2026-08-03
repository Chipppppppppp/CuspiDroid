package io.github.cuspidroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

        ScrollView scrollView = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(18), dp(72), dp(18), dp(24));
        scrollView.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scrollView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(10), 0, dp(10), 0);
        topBar.setBackground(topBarBackground());
        ImageButton back = iconButton(R.drawable.ic_arrow_back,
                MainActivity.text("\u623b\u308b", "Back"));
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
            list.addView(postRow(post), rowParams());
            count++;
        }
        if (count == 0) {
            TextView empty = helperText(MainActivity.text(
                    "\u3053\u306e\u30ab\u30c6\u30b4\u30ea\u306b\u30ec\u30b9\u306f\u3042\u308a\u307e\u305b\u3093\u3002",
                    "No posts in this category."));
            empty.setPadding(0, dp(12), 0, dp(12));
            list.addView(empty);
        }
    }

    private FrameLayout postRow(FavoritePostsStore.FavoritePost post) {
        return PostListItemView.create(this, post.title, post.url, post.number,
                PostListItemView.formatMeta(post.name, post.date), post.body, Theme.accent(this),
                () -> openThread(post), () -> openPost(post), () -> {
                    FavoritePostsStore.removePost(preferences, post.url, post.number);
                    renderPosts();
                });
    }

    private void openPost(FavoritePostsStore.FavoritePost post) {
        openTarget(post, true);
    }

    private void openThread(FavoritePostsStore.FavoritePost post) {
        openTarget(post, false);
    }

    private void openTarget(FavoritePostsStore.FavoritePost post, boolean jumpToPost) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(post.url));
        if (jumpToPost && post.number > 0) {
            intent.putExtra(MainActivity.EXTRA_JUMP_POST_NUMBER, post.number);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
