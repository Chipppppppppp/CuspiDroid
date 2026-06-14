package io.github.cuspidroid;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

final class Theme {
    static final String MODE_SYSTEM = "system";
    static final String MODE_LIGHT = "light";
    static final String MODE_DARK = "dark";

    private Theme() {
    }

    static boolean dark(Context context) {
        String mode = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
                .getString(MainActivity.PREF_THEME_MODE, MODE_SYSTEM);
        if (MODE_DARK.equals(mode)) {
            return true;
        }
        if (MODE_LIGHT.equals(mode)) {
            return false;
        }
        int night = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return night == Configuration.UI_MODE_NIGHT_YES;
    }

    static int background(Context context) {
        return dark(context) ? Color.BLACK : Color.WHITE;
    }

    static int topBar(Context context) {
        return dark(context) ? Color.BLACK : Color.rgb(242, 246, 249);
    }

    static int surface(Context context) {
        return dark(context) ? Color.rgb(12, 12, 12) : Color.rgb(247, 248, 250);
    }

    static int post(Context context) {
        return dark(context) ? Color.rgb(16, 16, 16) : Color.rgb(250, 251, 252);
    }

    static int unread(Context context) {
        return dark(context) ? Color.rgb(4, 44, 43) : Color.rgb(232, 247, 244);
    }

    static int field(Context context) {
        return dark(context) ? Color.rgb(18, 18, 18) : Color.rgb(241, 245, 249);
    }

    static int menu(Context context) {
        return dark(context) ? Color.rgb(12, 12, 12) : Color.WHITE;
    }

    static int text(Context context) {
        return dark(context) ? Color.rgb(245, 247, 250) : Color.rgb(31, 41, 55);
    }

    static int muted(Context context) {
        return dark(context) ? Color.rgb(168, 176, 186) : Color.rgb(79, 91, 103);
    }

    static int subtle(Context context) {
        return dark(context) ? Color.rgb(116, 128, 141) : Color.rgb(100, 116, 139);
    }

    static int border(Context context) {
        return dark(context) ? Color.rgb(54, 62, 72) : Color.rgb(215, 221, 226);
    }

    static int strongBorder(Context context) {
        return dark(context) ? Color.rgb(86, 98, 112) : Color.rgb(148, 163, 184);
    }

    static int active(Context context) {
        return dark(context) ? Color.rgb(2, 48, 48) : Color.rgb(224, 242, 241);
    }

    static int searchHighlight(Context context) {
        return dark(context) ? Color.rgb(10, 70, 82) : Color.rgb(187, 247, 208);
    }

    static int linkHighlight(Context context) {
        return dark(context) ? Color.rgb(23, 37, 84) : Color.rgb(219, 234, 254);
    }

    static int accent(Context context) {
        return dark(context) ? Color.rgb(20, 184, 166) : Color.rgb(15, 118, 110);
    }

    static void tintCompoundButton(Context context, CompoundButton button) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || button == null) {
            return;
        }
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{
                accent(context),
                dark(context) ? Color.rgb(148, 163, 184) : Color.rgb(100, 116, 139)
        };
        button.setButtonTintList(new ColorStateList(states, colors));
    }

    static void styleDialog(Dialog dialog, Context context) {
        if (dialog == null) {
            return;
        }
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(surface(context)));
        }
        int accent = accent(context);
        View decor = dialog.getWindow() == null ? null : dialog.getWindow().getDecorView();
        tintDialogText(decor, text(context));
        if (dialog instanceof android.app.AlertDialog) {
            android.app.AlertDialog alert = (android.app.AlertDialog) dialog;
            if (alert.getButton(android.app.AlertDialog.BUTTON_POSITIVE) != null) {
                alert.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(accent);
            }
            if (alert.getButton(android.app.AlertDialog.BUTTON_NEGATIVE) != null) {
                alert.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(accent);
            }
            if (alert.getButton(android.app.AlertDialog.BUTTON_NEUTRAL) != null) {
                alert.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(accent);
            }
        }
    }

    private static void tintDialogText(View view, int color) {
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        }
        if (!(view instanceof ViewGroup)) {
            return;
        }
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            tintDialogText(group.getChildAt(i), color);
        }
    }

    static void applySystemBars(Activity activity) {
        activity.getWindow().setStatusBarColor(background(activity));
        activity.getWindow().setNavigationBarColor(topBar(activity));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            if (dark(activity)) {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (dark(activity)) {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                } else {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
            }
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }
}
