package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

final class ThemeColorPicker {
    interface Callback {
        void onColorSelected(int color);
    }

    private ThemeColorPicker() {
    }

    static void show(Activity activity, int initialColor, Callback callback) {
        float[] initialHsv = new float[3];
        Color.colorToHSV(initialColor, initialHsv);
        int[] selectedColor = {initialColor};

        LinearLayout form = new LinearLayout(activity);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(activity, 18), dp(activity, 8), dp(activity, 18), dp(activity, 10));
        form.setBackgroundColor(Theme.surface(activity));

        View preview = new View(activity);
        preview.setBackground(colorPreviewBackground(activity, initialColor));
        form.addView(preview, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 72)));

        TextView hex = text(activity, Theme.Palette.colorHex(initialColor), 17, Theme.text(activity));
        hex.setGravity(Gravity.CENTER);
        hex.setTypeface(Typeface.DEFAULT_BOLD);
        hex.setPadding(0, dp(activity, 6), 0, dp(activity, 4));
        form.addView(hex);

        form.addView(fieldLabel(activity, MainActivity.text("色相プリセット", "Hue presets")));
        LinearLayout presets = new LinearLayout(activity);
        presets.setOrientation(LinearLayout.HORIZONTAL);
        int[] presetColors = {
                Color.rgb(239, 68, 68), Color.rgb(249, 115, 22), Color.rgb(234, 179, 8),
                Color.rgb(34, 197, 94), Color.rgb(20, 184, 166), Color.rgb(14, 165, 233),
                Color.rgb(37, 99, 235), Color.rgb(126, 34, 206), Color.rgb(219, 39, 119)
        };

        TextView hueValue = text(activity, "", 13, Theme.muted(activity));
        TextView saturationValue = text(activity, "", 13, Theme.muted(activity));
        TextView brightnessValue = text(activity, "", 13, Theme.muted(activity));
        SeekBar hue = pickerSeekBar(activity, 359, Math.round(initialHsv[0]));
        SeekBar saturation = pickerSeekBar(activity, 100, Math.round(initialHsv[1] * 100));
        SeekBar brightness = pickerSeekBar(activity, 100, Math.round(initialHsv[2] * 100));

        Runnable update = () -> {
            float[] hsv = {hue.getProgress(), saturation.getProgress() / 100f,
                    brightness.getProgress() / 100f};
            int color = Color.HSVToColor(hsv);
            selectedColor[0] = color;
            preview.setBackground(colorPreviewBackground(activity, color));
            hex.setText(Theme.Palette.colorHex(color));
            hueValue.setText(String.format(Locale.getDefault(),
                    MainActivity.text("色相: %d°", "Hue: %d°"), hue.getProgress()));
            saturationValue.setText(String.format(Locale.getDefault(),
                    MainActivity.text("彩度: %d%%", "Saturation: %d%%"), saturation.getProgress()));
            brightnessValue.setText(String.format(Locale.getDefault(),
                    MainActivity.text("明度: %d%%", "Brightness: %d%%"), brightness.getProgress()));
            ColorStateList tint = ColorStateList.valueOf(color);
            hue.setThumbTintList(tint);
            saturation.setProgressTintList(tint);
            saturation.setThumbTintList(tint);
            brightness.setProgressTintList(tint);
            brightness.setThumbTintList(tint);
        };

        for (int presetColor : presetColors) {
            View swatch = new View(activity);
            swatch.setBackground(colorPreviewBackground(activity, presetColor));
            swatch.setContentDescription(MainActivity.text("色相プリセット", "Hue preset"));
            swatch.setFocusable(true);
            swatch.setOnClickListener(v -> {
                float[] hsv = new float[3];
                Color.colorToHSV(presetColor, hsv);
                hue.setProgress(Math.round(hsv[0]));
                saturation.setProgress(Math.round(hsv[1] * 100));
                brightness.setProgress(Math.round(hsv[2] * 100));
                update.run();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(activity, 32), 1);
            params.setMargins(0, 0, dp(activity, 4), 0);
            presets.addView(swatch, params);
        }
        form.addView(presets, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 32)));

        form.addView(hueValue);
        form.addView(hue);
        form.addView(saturationValue);
        form.addView(saturation);
        form.addView(brightnessValue);
        form.addView(brightness);
        bindPickerSeekBar(hue, update);
        bindPickerSeekBar(saturation, update);
        bindPickerSeekBar(brightness, update);
        update.run();

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(MainActivity.text("色を選択", "Choose color"))
                .setView(form)
                .setNegativeButton(MainActivity.text("キャンセル", "Cancel"), null)
                .setPositiveButton(MainActivity.text("適用", "Apply"), (d, which) -> {
                    if (callback != null) {
                        callback.onColorSelected(selectedColor[0]);
                    }
                })
                .create();
        dialog.setOnShowListener(d -> Theme.styleDialog(dialog, activity));
        dialog.show();
    }

    static GradientDrawable colorPreviewBackground(Activity activity, int color) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setStroke(dp(activity, 1), Theme.strongBorder(activity));
        background.setCornerRadius(dp(activity, 8));
        return background;
    }

    private static TextView fieldLabel(Activity activity, String value) {
        TextView label = text(activity, value, 13, Theme.muted(activity));
        label.setPadding(0, dp(activity, 6), 0, dp(activity, 5));
        return label;
    }

    private static TextView text(Activity activity, String value, float size, int color) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private static SeekBar pickerSeekBar(Activity activity, int max, int progress) {
        SeekBar seekBar = new SeekBar(activity);
        seekBar.setMax(max);
        seekBar.setProgress(progress);
        return seekBar;
    }

    private static void bindPickerSeekBar(SeekBar seekBar, Runnable update) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                update.run();
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
            }
        });
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }
}
