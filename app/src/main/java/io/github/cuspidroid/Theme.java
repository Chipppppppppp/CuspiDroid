package io.github.cuspidroid;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Central source for every semantic UI color used by the app. */
final class Theme {
    static final String MODE_SYSTEM = "system";
    static final String MODE_LIGHT = "light";
    static final String MODE_DARK = "dark";
    static final String ID_PRIVATE = "private";
    static final String ID_BLUE = "blue";
    static final String CUSTOM_PREFIX = "custom:";

    static final String PREF_NORMAL_THEME = "normal_theme_id";
    static final String PREF_PRIVATE_THEME = "private_theme_id";
    static final String PREF_CUSTOM_THEMES = "custom_themes_json";

    static final String EXPORT_FORMAT = "cuspidroid-theme";
    static final int EXPORT_VERSION = 1;

    static final String[] COLOR_KEYS = {
            "background", "topBar", "surface", "post", "unread", "field", "menu",
            "text", "muted", "subtle", "border", "strongBorder", "active",
            "linkHighlight", "accent"
    };

    private static final Palette LIGHT = new Palette(MODE_LIGHT, "Light", false,
            Color.WHITE, Color.rgb(242, 246, 249), Color.rgb(247, 248, 250),
            Color.rgb(250, 251, 252), Color.rgb(232, 247, 244), Color.rgb(241, 245, 249),
            Color.WHITE, Color.rgb(31, 41, 55), Color.rgb(79, 91, 103),
            Color.rgb(100, 116, 139), Color.rgb(215, 221, 226), Color.rgb(148, 163, 184),
            Color.rgb(224, 242, 241), Color.rgb(219, 234, 254), Color.rgb(15, 118, 110));

    private static final Palette DARK = new Palette(MODE_DARK, "Dark", true,
            Color.BLACK, Color.BLACK, Color.rgb(12, 12, 12), Color.rgb(16, 16, 16),
            Color.rgb(4, 44, 43), Color.rgb(18, 18, 18), Color.rgb(12, 12, 12),
            Color.rgb(245, 247, 250), Color.rgb(168, 176, 186), Color.rgb(116, 128, 141),
            Color.rgb(54, 62, 72), Color.rgb(86, 98, 112), Color.rgb(2, 48, 48),
            Color.rgb(23, 37, 84), Color.rgb(20, 184, 166));

    private static final Palette PRIVATE = new Palette(ID_PRIVATE, "Private browsing", true,
            Color.rgb(0, 24, 17), Color.rgb(0, 24, 17), Color.rgb(1, 40, 29),
            Color.rgb(1, 40, 29), Color.rgb(4, 44, 43), Color.rgb(1, 40, 29),
            Color.rgb(1, 40, 29), Color.rgb(245, 247, 250), Color.rgb(168, 176, 186),
            Color.rgb(168, 176, 186), Color.rgb(8, 72, 52), Color.rgb(16, 104, 76),
            Color.rgb(2, 48, 48), Color.rgb(23, 37, 84), Color.rgb(52, 211, 153));

    private static final Palette BLUE = new Palette(ID_BLUE, "Blue", false,
            Color.rgb(246, 249, 255), Color.rgb(234, 242, 255), Color.WHITE,
            Color.rgb(248, 250, 255), Color.rgb(224, 242, 254), Color.rgb(237, 244, 255),
            Color.WHITE, Color.rgb(23, 37, 84), Color.rgb(71, 85, 105),
            Color.rgb(100, 116, 139), Color.rgb(191, 219, 254), Color.rgb(96, 165, 250),
            Color.rgb(219, 234, 254), Color.rgb(191, 219, 254), Color.rgb(37, 99, 235));

    private static String cachedJson;
    private static String cachedNormalId;
    private static String cachedPrivateId;
    private static int cachedNight = -1;
    private static Palette cachedNormal;
    private static Palette cachedPrivate;

    private Theme() {
    }

    static boolean dark(Context context) {
        return palette(context, false).dark;
    }

    static boolean dark(Context context, boolean privateBrowsing) {
        return palette(context, privateBrowsing).dark;
    }

    static int background(Context context) { return background(context, false); }
    static int background(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).background; }
    static int topBar(Context context) { return topBar(context, false); }
    static int topBar(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).topBar; }
    static int surface(Context context) { return surface(context, false); }
    static int surface(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).surface; }
    static int post(Context context) { return post(context, false); }
    static int post(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).post; }
    static int unread(Context context) { return unread(context, false); }
    static int unread(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).unread; }
    static int field(Context context) { return field(context, false); }
    static int field(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).field; }
    static int menu(Context context) { return menu(context, false); }
    static int menu(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).menu; }
    static int text(Context context) { return text(context, false); }
    static int text(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).text; }
    static int muted(Context context) { return muted(context, false); }
    static int muted(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).muted; }
    static int subtle(Context context) { return subtle(context, false); }
    static int subtle(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).subtle; }
    static int border(Context context) { return border(context, false); }
    static int border(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).border; }
    static int strongBorder(Context context) { return strongBorder(context, false); }
    static int strongBorder(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).strongBorder; }
    static int active(Context context) { return active(context, false); }
    static int active(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).active; }
    static int searchHighlight(Context context) { return searchHighlight(context, false); }
    static int searchHighlight(Context context, boolean privateBrowsing) { return active(context, privateBrowsing); }
    static int linkHighlight(Context context) { return linkHighlight(context, false); }
    static int linkHighlight(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).linkHighlight; }
    static int accent(Context context) { return accent(context, false); }
    static int accent(Context context, boolean privateBrowsing) { return palette(context, privateBrowsing).accent; }

    static int contrastingText(int background) {
        int brightness = (Color.red(background) * 299 + Color.green(background) * 587
                + Color.blue(background) * 114) / 1000;
        return brightness >= 150 ? Color.BLACK : Color.WHITE;
    }

    static String normalSelection(Context context) {
        SharedPreferences prefs = preferences(context);
        if (prefs.contains(PREF_NORMAL_THEME)) {
            return prefs.getString(PREF_NORMAL_THEME, MODE_SYSTEM);
        }
        return prefs.getString(MainActivity.PREF_THEME_MODE, MODE_SYSTEM);
    }

    static String privateSelection(Context context) {
        return preferences(context).getString(PREF_PRIVATE_THEME, ID_PRIVATE);
    }

    static String signature(Context context) {
        SharedPreferences prefs = preferences(context);
        int night = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return normalSelection(context) + "|" + privateSelection(context) + "|" + night + "|"
                + prefs.getString(PREF_CUSTOM_THEMES, "[]");
    }

    static List<Palette> selectablePalettes(Context context) {
        List<Palette> result = new ArrayList<>();
        result.add(LIGHT.copy());
        result.add(DARK.copy());
        result.add(PRIVATE.copy());
        result.add(BLUE.copy());
        result.addAll(customPalettes(context));
        return result;
    }

    static List<Palette> customPalettes(Context context) {
        List<Palette> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences(context).getString(PREF_CUSTOM_THEMES, "[]"));
            for (int i = 0; i < array.length(); i++) {
                try {
                    Palette palette = Palette.fromJson(array.getJSONObject(i));
                    if (palette.id.startsWith(CUSTOM_PREFIX)) {
                        result.add(palette);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    static Palette paletteById(Context context, String id) {
        if (MODE_LIGHT.equals(id)) return LIGHT.copy();
        if (MODE_DARK.equals(id)) return DARK.copy();
        if (ID_PRIVATE.equals(id)) return PRIVATE.copy();
        if (ID_BLUE.equals(id)) return BLUE.copy();
        for (Palette palette : customPalettes(context)) {
            if (palette.id.equals(id)) return palette;
        }
        return null;
    }

    static Palette newCustomPalette(Palette source, String name) {
        Palette copy = (source == null ? LIGHT : source).copy();
        copy.id = CUSTOM_PREFIX + UUID.randomUUID();
        copy.name = name == null ? "Custom theme" : name.trim();
        if (copy.name.isEmpty()) copy.name = "Custom theme";
        return copy;
    }

    static void saveCustomPalette(Context context, Palette palette) throws JSONException {
        List<Palette> palettes = customPalettes(context);
        boolean replaced = false;
        for (int i = 0; i < palettes.size(); i++) {
            if (palettes.get(i).id.equals(palette.id)) {
                palettes.set(i, palette.copy());
                replaced = true;
                break;
            }
        }
        if (!replaced) palettes.add(palette.copy());
        saveCustomPalettes(context, palettes);
    }

    static void deleteCustomPalette(Context context, String id) throws JSONException {
        List<Palette> palettes = customPalettes(context);
        for (int i = palettes.size() - 1; i >= 0; i--) {
            if (palettes.get(i).id.equals(id)) palettes.remove(i);
        }
        SharedPreferences prefs = preferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        if (id.equals(normalSelection(context))) editor.putString(PREF_NORMAL_THEME, MODE_SYSTEM);
        if (id.equals(privateSelection(context))) editor.putString(PREF_PRIVATE_THEME, ID_PRIVATE);
        JSONArray array = paletteArray(palettes);
        editor.putString(PREF_CUSTOM_THEMES, array.toString()).apply();
        invalidateCache();
    }

    static JSONObject exportJson(Palette palette) throws JSONException {
        JSONObject root = new JSONObject();
        root.put("format", EXPORT_FORMAT);
        root.put("version", EXPORT_VERSION);
        root.put("theme", palette.toJson());
        return root;
    }

    static Palette importJson(JSONObject root) throws JSONException {
        if (!EXPORT_FORMAT.equals(root.optString("format")) || root.optInt("version") != EXPORT_VERSION) {
            throw new JSONException("Unsupported theme file");
        }
        Palette imported = Palette.fromJson(root.getJSONObject("theme"));
        imported.id = CUSTOM_PREFIX + UUID.randomUUID();
        return imported;
    }

    static String displayName(Context context, String id) {
        if (MODE_SYSTEM.equals(id)) return MainActivity.text("端末のテーマに従う", "Follow device theme");
        Palette palette = paletteById(context, id);
        if (palette == null) return MainActivity.text("不明なテーマ", "Unknown theme");
        if (MODE_LIGHT.equals(id)) return MainActivity.text("ライト", "Light");
        if (MODE_DARK.equals(id)) return MainActivity.text("ダーク", "Dark");
        if (ID_PRIVATE.equals(id)) return MainActivity.text("プライベートブラウジング", "Private browsing");
        if (ID_BLUE.equals(id)) return MainActivity.text("ブルー（サンプル）", "Blue (sample)");
        return palette.name;
    }

    static Palette previewPalette(Context context, String id) {
        if (MODE_SYSTEM.equals(id)) {
            int night = context.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            return (night == Configuration.UI_MODE_NIGHT_YES ? DARK : LIGHT).copy();
        }
        Palette palette = paletteById(context, id);
        return palette == null ? LIGHT.copy() : palette;
    }

    static void tintCompoundButton(Context context, CompoundButton button) {
        tintCompoundButton(context, button, false);
    }

    static void tintCompoundButton(Context context, CompoundButton button, boolean privateBrowsing) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || button == null) return;
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{accent(context, privateBrowsing), subtle(context, privateBrowsing)};
        button.setButtonTintList(new ColorStateList(states, colors));
    }

    static void styleDialog(Dialog dialog, Context context) {
        styleDialog(dialog, context, border(context));
    }

    static void styleDialog(Dialog dialog, Context context, int borderColor) {
        styleDialog(dialog, context, surface(context), text(context), accent(context), borderColor);
    }

    static void styleDialog(Dialog dialog, Context context, int backgroundColor, int textColor,
                            int accentColor, int borderColor) {
        if (dialog == null) return;
        stylePopupDialog(dialog, context, backgroundColor, borderColor);
        View decor = dialog.getWindow() == null ? null : dialog.getWindow().getDecorView();
        tintDialogText(decor, textColor);
        if (dialog instanceof android.app.AlertDialog) {
            android.app.AlertDialog alert = (android.app.AlertDialog) dialog;
            if (alert.getButton(android.app.AlertDialog.BUTTON_POSITIVE) != null) alert.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(accentColor);
            if (alert.getButton(android.app.AlertDialog.BUTTON_NEGATIVE) != null) alert.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(accentColor);
            if (alert.getButton(android.app.AlertDialog.BUTTON_NEUTRAL) != null) alert.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(accentColor);
        }
    }

    static void stylePopupDialog(Dialog dialog, Context context) {
        stylePopupDialog(dialog, context, border(context));
    }

    static void stylePopupDialog(Dialog dialog, Context context, int borderColor) {
        stylePopupDialog(dialog, context, surface(context), borderColor);
    }

    static void stylePopupDialog(Dialog dialog, Context context, int backgroundColor, int borderColor) {
        if (dialog == null) return;
        if (dialog.getWindow() != null) {
            GradientDrawable background = new GradientDrawable();
            background.setColor(backgroundColor);
            background.setCornerRadius(dp(context, 10));
            dialog.getWindow().setBackgroundDrawable(background);
            GradientDrawable frame = new GradientDrawable();
            frame.setColor(Color.TRANSPARENT);
            frame.setStroke(dp(context, 2), borderColor);
            frame.setCornerRadius(dp(context, 10));
            dialog.getWindow().getDecorView().setForeground(frame);
        }
    }

    private static void tintDialogText(View view, int color) {
        if (view instanceof TextView) ((TextView) view).setTextColor(color);
        if (!(view instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) tintDialogText(group.getChildAt(i), color);
    }

    static void applySystemBars(Activity activity) {
        applySystemBars(activity, false);
    }

    static void applySystemBars(Activity activity, boolean privateBrowsing) {
        activity.getWindow().setStatusBarColor(background(activity, privateBrowsing));
        activity.getWindow().setNavigationBarColor(topBar(activity, privateBrowsing));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            if (dark(activity, privateBrowsing)) flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            else flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (dark(activity, privateBrowsing)) flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                else flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private static synchronized Palette palette(Context context, boolean privateBrowsing) {
        SharedPreferences prefs = preferences(context);
        String json = prefs.getString(PREF_CUSTOM_THEMES, "[]");
        String normalId = normalSelection(context);
        String privateId = privateSelection(context);
        int night = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (!json.equals(cachedJson) || !normalId.equals(cachedNormalId)
                || !privateId.equals(cachedPrivateId) || night != cachedNight) {
            cachedJson = json;
            cachedNormalId = normalId;
            cachedPrivateId = privateId;
            cachedNight = night;
            cachedNormal = resolve(context, normalId, night);
            cachedPrivate = resolve(context, privateId, night);
        }
        return privateBrowsing ? cachedPrivate : cachedNormal;
    }

    private static Palette resolve(Context context, String id, int night) {
        if (MODE_SYSTEM.equals(id)) {
            return night == Configuration.UI_MODE_NIGHT_YES ? DARK : LIGHT;
        }
        Palette palette = paletteById(context, id);
        return palette == null ? LIGHT : palette;
    }

    private static void saveCustomPalettes(Context context, List<Palette> palettes) throws JSONException {
        preferences(context).edit().putString(PREF_CUSTOM_THEMES, paletteArray(palettes).toString()).apply();
        invalidateCache();
    }

    private static JSONArray paletteArray(List<Palette> palettes) throws JSONException {
        JSONArray array = new JSONArray();
        for (Palette palette : palettes) array.put(palette.toJson());
        return array;
    }

    static synchronized void invalidateCache() {
        cachedJson = null;
        cachedNormalId = null;
        cachedPrivateId = null;
        cachedNight = -1;
        cachedNormal = null;
        cachedPrivate = null;
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static final class Palette {
        String id;
        String name;
        boolean dark;
        int background;
        int topBar;
        int surface;
        int post;
        int unread;
        int field;
        int menu;
        int text;
        int muted;
        int subtle;
        int border;
        int strongBorder;
        int active;
        int linkHighlight;
        int accent;

        Palette(String id, String name, boolean dark, int background, int topBar, int surface,
                int post, int unread, int field, int menu, int text, int muted, int subtle,
                int border, int strongBorder, int active, int linkHighlight, int accent) {
            this.id = id;
            this.name = name;
            this.dark = dark;
            this.background = background;
            this.topBar = topBar;
            this.surface = surface;
            this.post = post;
            this.unread = unread;
            this.field = field;
            this.menu = menu;
            this.text = text;
            this.muted = muted;
            this.subtle = subtle;
            this.border = border;
            this.strongBorder = strongBorder;
            this.active = active;
            this.linkHighlight = linkHighlight;
            this.accent = accent;
        }

        Palette copy() {
            return new Palette(id, name, dark, background, topBar, surface, post, unread, field,
                    menu, text, muted, subtle, border, strongBorder, active, linkHighlight, accent);
        }

        int color(String key) {
            switch (key) {
                case "background": return background;
                case "topBar": return topBar;
                case "surface": return surface;
                case "post": return post;
                case "unread": return unread;
                case "field": return field;
                case "menu": return menu;
                case "text": return text;
                case "muted": return muted;
                case "subtle": return subtle;
                case "border": return border;
                case "strongBorder": return strongBorder;
                case "active": return active;
                case "linkHighlight": return linkHighlight;
                case "accent": return accent;
                default: throw new IllegalArgumentException(key);
            }
        }

        void setColor(String key, int color) {
            switch (key) {
                case "background": background = color; break;
                case "topBar": topBar = color; break;
                case "surface": surface = color; break;
                case "post": post = color; break;
                case "unread": unread = color; break;
                case "field": field = color; break;
                case "menu": menu = color; break;
                case "text": text = color; break;
                case "muted": muted = color; break;
                case "subtle": subtle = color; break;
                case "border": border = color; break;
                case "strongBorder": strongBorder = color; break;
                case "active": active = color; break;
                case "linkHighlight": linkHighlight = color; break;
                case "accent": accent = color; break;
                default: throw new IllegalArgumentException(key);
            }
        }

        JSONObject toJson() throws JSONException {
            JSONObject object = new JSONObject();
            object.put("id", id);
            object.put("name", name);
            object.put("dark", dark);
            JSONObject colors = new JSONObject();
            for (String key : COLOR_KEYS) colors.put(key, colorHex(color(key)));
            object.put("colors", colors);
            return object;
        }

        static Palette fromJson(JSONObject object) throws JSONException {
            String id = object.getString("id");
            String name = object.getString("name").trim();
            if (name.isEmpty()) throw new JSONException("Theme name is empty");
            if (name.length() > 80) throw new JSONException("Theme name is too long");
            JSONObject colors = object.getJSONObject("colors");
            Palette palette = LIGHT.copy();
            palette.id = id;
            palette.name = name;
            palette.dark = object.getBoolean("dark");
            for (String key : COLOR_KEYS) palette.setColor(key, parseColor(colors.getString(key)));
            return palette;
        }

        private static int parseColor(String value) throws JSONException {
            if (value == null || !value.matches("#[0-9a-fA-F]{6}")) {
                throw new JSONException("Invalid color: " + value);
            }
            try {
                return Color.parseColor(value);
            } catch (IllegalArgumentException error) {
                throw new JSONException("Invalid color: " + value);
            }
        }

        static String colorHex(int color) {
            return String.format(Locale.US, "#%06X", color & 0xFFFFFF);
        }
    }
}
