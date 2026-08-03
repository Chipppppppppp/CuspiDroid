package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
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
        title.setText(MainActivity.text("お気に入りカテゴリ", "Favorite Categories"));
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
            List<FavoritePostsStore.FavoritePost> posts = grouped.get(category.id);
            list.addView(categoryHeader(category, posts == null ? 0 : posts.size()), rowParams());
        }
    }

    private View categoryHeader(FavoritePostsStore.Category category, int postCount) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(6), dp(4), dp(6));
        row.setBackground(rowBackground());
        row.setOnClickListener(v -> openCategory(category));
        View color = new View(this);
        color.setBackground(ThemeColorPicker.colorPreviewBackground(this, category.color));
        row.addView(color, new LinearLayout.LayoutParams(dp(18), dp(34)));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        TextView name = new TextView(this);
        name.setText(category.displayName());
        name.setTextColor(Theme.text(this));
        name.setTextSize(18);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        labels.addView(name);
        TextView count = helperText(MainActivity.text("レス ", "Posts ") + postCount);
        labels.addView(count);
        LinearLayout.LayoutParams labelsParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        labelsParams.setMargins(dp(10), 0, 0, 0);
        row.addView(labels, labelsParams);
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

    private void openCategory(FavoritePostsStore.Category category) {
        startActivity(new Intent(this, FavoritePostListActivity.class)
                .putExtra(FavoritePostListActivity.EXTRA_CATEGORY_ID, category.id));
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

        int[] selectedColor = {category == null ? ThemeColorPicker.PRESET_RED : category.color};
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
                .setPositiveButton(category == null
                        ? MainActivity.text("追加", "Add")
                        : MainActivity.text("保存", "Save"), null)
                .create();
        dialog.show();
        Theme.styleDialog(dialog, this);
        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Theme.styleDialogButtonEnabled(this, saveButton,
                category != null && !category.name.trim().isEmpty());
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Theme.styleDialogButtonEnabled(FavoritePostsActivity.this, saveButton,
                        s != null && !s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        saveButton.setOnClickListener(v -> {
            String categoryName = name.getText().toString().trim();
            if (categoryName.isEmpty()) return;
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
