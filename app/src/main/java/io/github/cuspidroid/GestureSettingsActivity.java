package io.github.cuspidroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Map;

public class GestureSettingsActivity extends Activity {
    private SharedPreferences preferences;
    private CheckBox enabled;
    private SeekBar sensitivity;
    private TextView sensitivityValue;
    private final Map<String, TextView> gestureValues = new LinkedHashMap<>();

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
        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        scroll.setBackgroundColor(bgColor());
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        scroll.addView(root, new android.widget.ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);

        TextView title = title(MainActivity.text("\u30b8\u30a7\u30b9\u30c1\u30e3\u30fc", "Gestures"), 24);
        title.setPadding(0, 0, 0, dp(16));
        root.addView(title);

        enabled = new CheckBox(this);
        enabled.setText(MainActivity.text("\u30b8\u30a7\u30b9\u30c1\u30e3\u30fc\u3092\u4f7f\u7528", "Use gestures"));
        enabled.setTextColor(textColor());
        enabled.setTextSize(16);
        Theme.tintCompoundButton(this, enabled);
        enabled.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.edit().putBoolean(MainActivity.PREF_GESTURES_ENABLED, isChecked).apply());
        root.addView(enabled);

        root.addView(sectionTitle(MainActivity.text("\u611f\u5ea6", "Sensitivity")));
        LinearLayout sensitivityRow = new LinearLayout(this);
        sensitivityRow.setOrientation(LinearLayout.HORIZONTAL);
        sensitivityRow.setGravity(Gravity.CENTER_VERTICAL);
        sensitivity = new SeekBar(this);
        sensitivity.setMax(4);
        tintSensitivityBar();
        sensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSensitivityValue(progress);
                if (fromUser) {
                    preferences.edit().putInt(MainActivity.PREF_GESTURE_SENSITIVITY, progress).apply();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferences.edit().putInt(MainActivity.PREF_GESTURE_SENSITIVITY, seekBar.getProgress()).apply();
            }
        });
        sensitivityRow.addView(sensitivity, new LinearLayout.LayoutParams(0, dp(44), 1));
        sensitivityValue = valueText("");
        sensitivityRow.addView(sensitivityValue, new LinearLayout.LayoutParams(dp(72), dp(44)));
        root.addView(sensitivityRow);

        root.addView(sectionTitle(MainActivity.text("\u30a2\u30af\u30b7\u30e7\u30f3", "Actions")));
        root.addView(helperText(MainActivity.text(
                "\u30b8\u30a7\u30b9\u30c1\u30e3\u30fc\u306f\u5de6\u307e\u305f\u306f\u53f3\u30b9\u30ef\u30a4\u30d7\u304b\u3089\u958b\u59cb\u3057\u307e\u3059\u3002\u4e0a\u30fb\u4e0b\u304b\u3089\u59cb\u307e\u308b\u64cd\u4f5c\u306f\u30b9\u30af\u30ed\u30fc\u30eb\u3068\u3057\u3066\u6271\u3044\u3001\u5272\u308a\u5f53\u3066\u3067\u304d\u307e\u305b\u3093\u3002",
                "Gestures must start with a left or right swipe. Up/down starts are treated as scrolling and cannot be assigned.")));
        for (String action : MainActivity.GESTURE_ACTIONS) {
            root.addView(actionRow(action));
        }
    }

    private void loadSettings() {
        enabled.setChecked(preferences.getBoolean(MainActivity.PREF_GESTURES_ENABLED, false));
        int progress = preferences.getInt(MainActivity.PREF_GESTURE_SENSITIVITY, 2);
        sensitivity.setProgress(Math.max(0, Math.min(4, progress)));
        updateSensitivityValue(sensitivity.getProgress());
        refreshGestureValues();
    }

    private View actionRow(String action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(8), dp(12), dp(8));
        row.setBackground(rowBackground());
        row.setOnClickListener(v -> showCaptureDialog(action));

        TextView label = title(MainActivity.gestureActionLabel(action), 16);
        row.addView(label, new LinearLayout.LayoutParams(0, dp(46), 1));
        TextView value = valueText("");
        gestureValues.put(action, value);
        row.addView(value, new LinearLayout.LayoutParams(dp(112), dp(46)));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(62));
        params.setMargins(0, dp(4), 0, dp(6));
        row.setLayoutParams(params);
        return row;
    }

    private void showCaptureDialog(String action) {
        GestureCaptureView capture = new GestureCaptureView(this);
        final String[] pendingGesture = {MainActivity.gestureForAction(preferences, action)};
        TextView result = valueText(MainActivity.gestureArrows(pendingGesture[0]));
        result.setTextSize(22);
        result.setTextColor(textColor());
        TextView hint = valueText(MainActivity.text(
                "\u5de6\u307e\u305f\u306f\u53f3\u304b\u3089\u59cb\u3081\u3066\u3001\u4fdd\u5b58\u3067\u78ba\u5b9a",
                "Start left or right, then save to apply"));
        hint.setTextColor(mutedColor());
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(8), dp(12), dp(12));
        content.addView(result, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(42)));
        content.addView(hint, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(28)));
        LinearLayout captureFrame = new LinearLayout(this);
        captureFrame.setPadding(dp(1), dp(1), dp(1), dp(1));
        captureFrame.setBackground(captureBackground());
        captureFrame.addView(capture, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams captureParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(220));
        captureParams.setMargins(0, dp(8), 0, 0);
        content.addView(captureFrame, captureParams);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(MainActivity.gestureActionLabel(action))
                .setView(content)
                .setNegativeButton(MainActivity.text("\u30ad\u30e3\u30f3\u30bb\u30eb", "Cancel"), null)
                .setNeutralButton(MainActivity.text("\u30af\u30ea\u30a2", "Clear"), null)
                .setPositiveButton(MainActivity.text("\u4fdd\u5b58", "Save"), null)
                .create();
        capture.setListener(gesture -> {
            if ("__INVALID_START__".equals(gesture)) {
                result.setText(MainActivity.text("\u5de6\u307e\u305f\u306f\u53f3\u304b\u3089\u958b\u59cb", "Start left or right"));
                return;
            }
            pendingGesture[0] = gesture;
            result.setText(MainActivity.gestureArrows(gesture));
        });
        dialog.setOnShowListener(d -> {
            Theme.styleDialog(dialog, this);
            hint.setTextColor(mutedColor());
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (!pendingGesture[0].isEmpty() && !MainActivity.validGesture(pendingGesture[0])) {
                    result.setText(MainActivity.text("\u5de6\u307e\u305f\u306f\u53f3\u304b\u3089\u958b\u59cb", "Start left or right"));
                    return;
                }
                String duplicate = duplicateGestureAction(action, pendingGesture[0]);
                if (duplicate != null) {
                    result.setText(MainActivity.text("\u767b\u9332\u6e08\u307f: ", "Already used: ")
                            + MainActivity.gestureActionLabel(duplicate));
                    return;
                }
                MainActivity.saveGestureForAction(preferences, action, pendingGesture[0]);
                refreshGestureValues();
                dialog.dismiss();
            });
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                pendingGesture[0] = "";
                result.setText(MainActivity.gestureArrows(""));
                capture.clear();
            });
        });
        dialog.show();
    }

    private String duplicateGestureAction(String currentAction, String gesture) {
        String normalized = MainActivity.normalizeGesture(gesture);
        if (normalized.isEmpty()) {
            return null;
        }
        for (String action : MainActivity.GESTURE_ACTIONS) {
            if (action.equals(currentAction)) {
                continue;
            }
            if (normalized.equals(MainActivity.gestureForAction(preferences, action))) {
                return action;
            }
        }
        return null;
    }

    private void tintSensitivityBar() {
        int active = Theme.accent(this);
        int inactive = Theme.border(this);
        int thumb = active;
        sensitivity.setProgressTintList(ColorStateList.valueOf(active));
        sensitivity.setProgressBackgroundTintList(ColorStateList.valueOf(inactive));
        sensitivity.setThumbTintList(ColorStateList.valueOf(thumb));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            sensitivity.setTickMarkTintList(ColorStateList.valueOf(
                    Theme.subtle(this)));
        }
    }

    private void refreshGestureValues() {
        for (Map.Entry<String, TextView> entry : gestureValues.entrySet()) {
            entry.getValue().setText(MainActivity.gestureArrows(
                    MainActivity.gestureForAction(preferences, entry.getKey())));
        }
    }

    private void updateSensitivityValue(int progress) {
        String[] labels = {
                MainActivity.text("\u4f4e", "Low"),
                MainActivity.text("\u3084\u3084\u4f4e", "M-Low"),
                MainActivity.text("\u4e2d", "Mid"),
                MainActivity.text("\u3084\u3084\u9ad8", "M-High"),
                MainActivity.text("\u9ad8", "High")
        };
        sensitivityValue.setText(labels[Math.max(0, Math.min(progress, labels.length - 1))]);
    }

    private TextView sectionTitle(String value) {
        TextView view = title(value, 18);
        view.setPadding(0, dp(16), 0, dp(8));
        return view;
    }

    private TextView title(String value, int sp) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(textColor());
        view.setTextSize(sp);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    private TextView valueText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(mutedColor());
        view.setTextSize(15);
        view.setGravity(Gravity.CENTER);
        view.setSingleLine(true);
        return view;
    }

    private TextView helperText(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(mutedColor());
        view.setTextSize(13);
        view.setPadding(0, dp(2), 0, dp(8));
        return view;
    }

    private GradientDrawable rowBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(surfaceColor());
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(dp(10));
        return drawable;
    }

    private GradientDrawable captureBackground() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Theme.surface(this));
        drawable.setStroke(dp(1), borderColor());
        drawable.setCornerRadius(dp(10));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class GestureCaptureView extends View {
        interface Listener {
            void onGesture(String gesture);
        }

        private final StringBuilder gesture = new StringBuilder();
        private Listener listener;
        private float lastX;
        private float lastY;
        private int threshold;
        private boolean rejected;

        GestureCaptureView(Activity activity) {
            super(activity);
            threshold = (int) (48 * activity.getResources().getDisplayMetrics().density + 0.5f);
        }

        void setListener(Listener listener) {
            this.listener = listener;
        }

        void clear() {
            gesture.setLength(0);
            rejected = false;
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                clear();
                lastX = event.getX();
                lastY = event.getY();
                return true;
            }
            if (action == MotionEvent.ACTION_MOVE) {
                float x = event.getX();
                float y = event.getY();
                addDirection(x, y);
                return true;
            }
            if (action == MotionEvent.ACTION_UP) {
                addDirection(event.getX(), event.getY());
                notifyGesture();
                return true;
            }
            if (action == MotionEvent.ACTION_CANCEL) {
                clear();
                return true;
            }
            return true;
        }

        private void addDirection(float x, float y) {
            if (rejected) {
                return;
            }
            float dx = x - lastX;
            float dy = y - lastY;
            if (Math.abs(dx) < threshold && Math.abs(dy) < threshold) {
                return;
            }
            char direction = Math.abs(dx) >= Math.abs(dy)
                    ? (dx > 0 ? 'R' : 'L')
                    : (dy > 0 ? 'D' : 'U');
            if (gesture.length() == 0 && (direction == 'U' || direction == 'D')) {
                rejected = true;
                gesture.setLength(0);
                notifyGesture();
                return;
            }
            int length = gesture.length();
            if (length == 0 || gesture.charAt(length - 1) != direction) {
                gesture.append(direction);
                notifyGesture();
            }
            lastX = x;
            lastY = y;
        }

        private void notifyGesture() {
            if (listener != null) {
                listener.onGesture(rejected ? "__INVALID_START__" : gesture.toString());
            }
        }
    }
}
