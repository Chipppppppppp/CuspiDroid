package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

public class FavoritePostsActivity extends Activity {
    private SharedPreferences preferences;
    private LinearLayout list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        buildLayout();
        renderFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (list != null) renderFavorites();
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

        TextView title = new TextView(this);
        title.setText(MainActivity.text("お気に入りのレス", "Favorite Posts"));
        title.setTextColor(Theme.text(this));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        titleParams.setMargins(dp(8), 0, 0, 0);
        topBar.addView(title, titleParams);

        ImageButton add = iconButton(R.drawable.ic_add,
                MainActivity.text("カテゴリを作成", "Create category"));
        add.setOnClickListener(v -> showCategoryDialog(null));
        topBar.addView(add, new LinearLayout.LayoutParams(dp(46), dp(44)));
        root.addView(topBar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56), Gravity.TOP));
    }

    private void renderFavorites() {
        list.removeAllViews();
        FavoritePostsStore.Snapshot snapshot = FavoritePostsStore.read(preferences);
        if (snapshot.categories.isEmpty()) {
            TextView empty = helperText(MainActivity.text(
                    "カテゴリを作成すると、レスの長押しメニューからお気に入りに追加できます。",
                    "Create a category, then add posts from their long-press menu."));
            empty.setPadding(0, dp(12), 0, dp(12));
            list.addView(empty);
            list.addView(actionRow(R.drawable.ic_add,
                    MainActivity.text("カテゴリを作成", "Create category"),
                    v -> showCategoryDialog(null)), rowParams());
            return;
        }
        Map<String, List<FavoritePostsStore.FavoritePost>> grouped = snapshot.postsByCategory();
        for (FavoritePostsStore.Category category : snapshot.categories) {
            list.addView(categoryHeader(category), rowParams());
            List<FavoritePostsStore.FavoritePost> posts = grouped.get(category.id);
            if (posts == null || posts.isEmpty()) {
                TextView empty = helperText(MainActivity.text("このカテゴリにレスはありません。",
                        "No posts in this category."));
                empty.setPadding(dp(14), dp(3), 0, dp(12));
                list.addView(empty);
                continue;
            }
            for (FavoritePostsStore.FavoritePost post : posts) {
                list.addView(postRow(category, post), rowParams());
            }
        }
    }

    private View categoryHeader(FavoritePostsStore.Category category) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(6), dp(4), dp(6));
        View color = new View(this);
        color.setBackground(ThemeColorPicker.colorPreviewBackground(this, category.color));
        row.addView(color, new LinearLayout.LayoutParams(dp(18), dp(34)));
        TextView name = new TextView(this);
        name.setText(category.displayName());
        name.setTextColor(Theme.text(this));
        name.setTextSize(18);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        nameParams.setMargins(dp(10), 0, 0, 0);
        row.addView(name, nameParams);
        ImageButton edit = iconButton(R.drawable.ic_edit,
                MainActivity.text("カテゴリを編集", "Edit category"));
        edit.setOnClickListener(v -> showCategoryDialog(category));
        row.addView(edit, new LinearLayout.LayoutParams(dp(40), dp(40)));
        ImageButton delete = iconButton(R.drawable.ic_delete,
                MainActivity.text("カテゴリを削除", "Delete category"));
        delete.setColorFilter(Theme.muted(this));
        delete.setOnClickListener(v -> confirmDeleteCategory(category));
        row.addView(delete, new LinearLayout.LayoutParams(dp(40), dp(40)));
        return row;
    }

    private View postRow(FavoritePostsStore.Category category,
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
        TextView title = new TextView(this);
        title.setText(post.title.trim().isEmpty() ? post.url : post.title);
        title.setTextColor(Theme.text(this));
        title.setTextSize(15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        content.addView(title);
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
            renderFavorites();
        });
        row.addView(delete, new LinearLayout.LayoutParams(dp(40), dp(40)));
        return shell;
    }

    private void showCategoryDialog(FavoritePostsStore.Category category) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(18), dp(8), dp(18), dp(8));
        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setHint(MainActivity.text("名前", "Name"));
        name.setText(category == null ? "" : category.name);
        name.setTextColor(Theme.text(this));
        name.setHintTextColor(Theme.subtle(this));
        name.setBackgroundColor(Theme.field(this));
        name.setPadding(dp(10), 0, dp(10), 0);
        form.addView(name, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));

        int[] selectedColor = {category == null ? Theme.accent(this) : category.color};
        TextView color = new TextView(this);
        color.setText(MainActivity.text("色を選択", "Choose color"));
        color.setTextSize(16);
        color.setGravity(Gravity.CENTER);
        color.setTextColor(Theme.contrastingText(selectedColor[0]));
        color.setBackground(ThemeColorPicker.colorPreviewBackground(this, selectedColor[0]));
        LinearLayout.LayoutParams colorParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        colorParams.setMargins(0, dp(12), 0, 0);
        form.addView(color, colorParams);
        color.setOnClickListener(v -> ThemeColorPicker.show(this, selectedColor[0], picked -> {
            selectedColor[0] = picked;
            color.setTextColor(Theme.contrastingText(picked));
            color.setBackground(ThemeColorPicker.colorPreviewBackground(this, picked));
        }));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(category == null
                        ? MainActivity.text("カテゴリを作成", "Create category")
                        : MainActivity.text("カテゴリを編集", "Edit category"))
                .setView(form)
                .setNegativeButton(MainActivity.text("キャンセル", "Cancel"), null)
                .setPositiveButton(MainActivity.text("保存", "Save"), null)
                .create();
        dialog.show();
        Theme.styleDialog(dialog, this);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String categoryName = name.getText().toString().trim();
            if (categoryName.isEmpty()) {
                name.setError(MainActivity.text("名前を入力してください。", "Enter a name."));
                name.requestFocus();
                return;
            }
            if (category == null) {
                FavoritePostsStore.addCategory(preferences, categoryName, selectedColor[0]);
            } else {
                FavoritePostsStore.updateCategory(preferences, category.id,
                        categoryName, selectedColor[0]);
            }
            dialog.dismiss();
            renderFavorites();
        });
    }

    private void confirmDeleteCategory(FavoritePostsStore.Category category) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.text("カテゴリを削除", "Delete category"))
                .setMessage(MainActivity.text(
                        "カテゴリ内のお気に入りもすべて削除されます。削除しますか？",
                        "All favorites in this category will also be deleted. Continue?"))
                .setNegativeButton(MainActivity.text("キャンセル", "Cancel"), null)
                .setPositiveButton(MainActivity.text("削除", "Delete"), (d, which) -> {
                    FavoritePostsStore.deleteCategory(preferences, category.id);
                    renderFavorites();
                    Toast.makeText(this, MainActivity.text("カテゴリを削除しました。",
                            "Category deleted."), Toast.LENGTH_SHORT).show();
                })
                .create();
        dialog.show();
        Theme.styleDialog(dialog, this);
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

    private View actionRow(int iconRes, String label, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), 0, dp(12), 0);
        row.setBackground(rowBackground());
        ImageButton icon = iconButton(iconRes, label);
        row.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));
        TextView text = new TextView(this);
        text.setText(label);
        text.setTextColor(Theme.text(this));
        text.setTextSize(16);
        row.addView(text, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.setOnClickListener(listener);
        return row;
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
