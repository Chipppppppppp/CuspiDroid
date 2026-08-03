package io.github.cuspidroid;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
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
    static final String ID_TEAL_LIGHT = "teal_light";
    static final String ID_TEAL_DARK = "teal_dark";
    static final String ID_PRIVATE = "private";
    static final String ID_BLUE = "blue";
    static final String ID_GREEN_LIGHT = "green_light";
    static final String ID_BLUE_DARK = "blue_dark";
    static final String ID_LIGHT_BLUE_LIGHT = "light_blue_light";
    static final String ID_LIGHT_BLUE_DARK = "light_blue_dark";
    static final String ID_PURPLE_LIGHT = "purple_light";
    static final String ID_PURPLE_DARK = "purple_dark";
    static final String ID_CRIMSON_LIGHT = "crimson_light";
    static final String ID_CRIMSON_DARK = "crimson_dark";
    static final String ID_PINK_LIGHT = "pink_light";
    static final String ID_PINK_DARK = "pink_dark";
    static final String ID_PURE_WHITE = "pure_white";
    static final String ID_PURE_DARK = "pure_dark";
    static final String CUSTOM_PREFIX = "custom:";

    static final String PREF_NORMAL_THEME = "normal_theme_id";
    static final String PREF_SYSTEM_LIGHT_THEME = "system_light_theme_id";
    static final String PREF_SYSTEM_DARK_THEME = "system_dark_theme_id";
    static final String PREF_CUSTOM_THEMES = "custom_themes_json";

    static final String EXPORT_FORMAT = "cuspidroid-theme";
    static final int EXPORT_VERSION = 2;

    static final String[] COLOR_KEYS = {
            "background", "topBar", "surface", "post", "unread", "field",
            "text", "muted", "subtle", "border", "strongBorder", "active", "accent",
            "sidebarUnread", "replyPostMarker", "metricLow", "metricHigh"
    };

    static final String[] EDITABLE_COLOR_KEYS = COLOR_KEYS;

    private static final Palette LIGHT = new Palette(MODE_LIGHT, "Indigo (Light)", false,
            Color.rgb(247, 248, 252), Color.rgb(238, 240, 247), Color.WHITE,
            Color.rgb(251, 252, 255), Color.rgb(219, 226, 255), Color.rgb(243, 244, 248),
            Color.WHITE, Color.rgb(32, 37, 52), Color.rgb(89, 98, 117),
            Color.rgb(123, 132, 151), Color.rgb(217, 221, 231), Color.rgb(174, 182, 199),
            Color.rgb(224, 231, 255), Color.rgb(79, 70, 229))
            .withDetails(Color.rgb(102, 112, 133), Color.rgb(79, 70, 229), Color.rgb(99, 102, 241),
                    Color.rgb(79, 70, 229), Color.rgb(124, 58, 237), Color.rgb(99, 102, 241),
                    Color.rgb(123, 132, 151), Color.rgb(67, 56, 202));

    private static final Palette DARK = new Palette(MODE_DARK, "Indigo (Dark)", true,
            Color.rgb(15, 17, 23), Color.rgb(21, 24, 33), Color.rgb(26, 30, 40),
            Color.rgb(29, 34, 45), Color.rgb(49, 57, 96), Color.rgb(32, 37, 49),
            Color.rgb(26, 30, 40), Color.rgb(244, 246, 250), Color.rgb(178, 184, 197),
            Color.rgb(137, 146, 164), Color.rgb(51, 58, 73), Color.rgb(85, 96, 119),
            Color.rgb(41, 46, 82), Color.rgb(129, 140, 248))
            .withDetails(Color.rgb(85, 96, 119), Color.rgb(129, 140, 248), Color.rgb(99, 102, 241),
                    Color.rgb(129, 140, 248), Color.rgb(167, 139, 250), Color.rgb(129, 140, 248),
                    Color.rgb(137, 146, 164), Color.rgb(129, 140, 248));

    private static final Palette TEAL_LIGHT = new Palette(ID_TEAL_LIGHT, "Teal (Light)", false,
            Color.rgb(243, 250, 250), Color.rgb(229, 245, 243), Color.rgb(251, 254, 253),
            Color.rgb(248, 252, 251), Color.rgb(196, 235, 228), Color.rgb(234, 246, 244),
            Color.WHITE, Color.rgb(21, 58, 58), Color.rgb(82, 111, 112),
            Color.rgb(113, 140, 140), Color.rgb(184, 221, 216), Color.rgb(111, 185, 176),
            Color.rgb(208, 240, 236), Color.rgb(15, 118, 110))
            .withDetails(Color.rgb(184, 221, 216), Color.rgb(15, 118, 110), Color.rgb(20, 184, 166),
                    Color.rgb(15, 118, 110), Color.rgb(20, 184, 166), Color.rgb(15, 118, 110),
                    Color.rgb(113, 140, 140), Color.rgb(13, 148, 136));

    private static final Palette TEAL_DARK = new Palette(ID_TEAL_DARK, "Teal (Dark)", true,
            Color.rgb(7, 28, 27), Color.rgb(10, 37, 35), Color.rgb(13, 48, 45),
            Color.rgb(16, 54, 50), Color.rgb(24, 88, 81), Color.rgb(18, 59, 56),
            Color.rgb(13, 48, 45), Color.rgb(236, 253, 251), Color.rgb(170, 203, 199),
            Color.rgb(130, 168, 164), Color.rgb(40, 94, 89), Color.rgb(47, 139, 129),
            Color.rgb(18, 75, 69), Color.rgb(45, 212, 191))
            .withDetails(Color.rgb(40, 94, 89), Color.rgb(45, 212, 191), Color.rgb(94, 234, 212),
                    Color.rgb(45, 212, 191), Color.rgb(94, 234, 212), Color.rgb(45, 212, 191),
                    Color.rgb(130, 168, 164), Color.rgb(45, 212, 191));

    private static final Palette GREEN_LIGHT = new Palette(ID_GREEN_LIGHT, "Green (Light)", false,
            Color.rgb(244, 251, 247), Color.rgb(231, 247, 238), Color.rgb(250, 253, 251),
            Color.rgb(247, 252, 249), Color.rgb(196, 238, 211), Color.rgb(234, 247, 239),
            Color.WHITE, Color.rgb(18, 53, 36), Color.rgb(70, 100, 81),
            Color.rgb(102, 130, 115), Color.rgb(183, 222, 197), Color.rgb(103, 185, 133),
            Color.rgb(209, 250, 229), Color.rgb(21, 128, 61))
            .withDetails(Color.rgb(183, 222, 197), Color.rgb(21, 128, 61), Color.rgb(34, 197, 94),
                    Color.rgb(21, 128, 61), Color.rgb(34, 197, 94), Color.rgb(21, 128, 61),
                    Color.rgb(102, 130, 115), Color.rgb(22, 163, 74));

    private static final Palette PRIVATE = new Palette(ID_PRIVATE, "Green (Dark)", true,
            Color.rgb(0, 24, 17), Color.rgb(0, 24, 17), Color.rgb(1, 40, 29),
            Color.rgb(1, 40, 29), Color.rgb(8, 72, 57), Color.rgb(1, 40, 29),
            Color.rgb(1, 40, 29), Color.rgb(245, 247, 250), Color.rgb(168, 176, 186),
            Color.rgb(168, 176, 186), Color.rgb(8, 72, 52), Color.rgb(16, 104, 76),
            Color.rgb(2, 48, 48), Color.rgb(52, 211, 153))
            .withDetails(Color.rgb(8, 72, 52), Color.rgb(52, 211, 153), Color.rgb(110, 231, 183),
                    Color.rgb(52, 211, 153), Color.rgb(110, 231, 183), Color.rgb(52, 211, 153),
                    Color.rgb(168, 176, 186), Color.rgb(16, 185, 129));

    private static final Palette BLUE = new Palette(ID_BLUE, "Blue (Light)", false,
            Color.rgb(246, 249, 255), Color.rgb(234, 242, 255), Color.WHITE,
            Color.rgb(248, 250, 255), Color.rgb(207, 231, 253), Color.rgb(237, 244, 255),
            Color.WHITE, Color.rgb(23, 37, 84), Color.rgb(71, 85, 105),
            Color.rgb(100, 116, 139), Color.rgb(191, 219, 254), Color.rgb(96, 165, 250),
            Color.rgb(219, 234, 254), Color.rgb(37, 99, 235))
            .withDetails(Color.rgb(191, 219, 254), Color.rgb(37, 99, 235), Color.rgb(59, 130, 246),
                    Color.rgb(37, 99, 235), Color.rgb(96, 165, 250), Color.rgb(37, 99, 235),
                    Color.rgb(100, 116, 139), Color.rgb(29, 78, 216));

    private static final Palette BLUE_DARK = new Palette(ID_BLUE_DARK, "Blue (Dark)", true,
            Color.rgb(7, 20, 38), Color.rgb(10, 25, 47), Color.rgb(13, 31, 56),
            Color.rgb(16, 36, 63), Color.rgb(24, 66, 112), Color.rgb(17, 40, 68),
            Color.rgb(13, 31, 56), Color.rgb(239, 246, 255), Color.rgb(169, 188, 211),
            Color.rgb(127, 152, 181), Color.rgb(41, 74, 109), Color.rgb(59, 130, 246),
            Color.rgb(18, 59, 104), Color.rgb(96, 165, 250))
            .withDetails(Color.rgb(41, 74, 109), Color.rgb(96, 165, 250), Color.rgb(147, 197, 253),
                    Color.rgb(96, 165, 250), Color.rgb(147, 197, 253), Color.rgb(96, 165, 250),
                    Color.rgb(127, 152, 181), Color.rgb(59, 130, 246));

    private static final Palette LIGHT_BLUE_LIGHT = new Palette(ID_LIGHT_BLUE_LIGHT, "Sky Blue (Light)", false,
            Color.rgb(243, 250, 255), Color.rgb(229, 244, 255), Color.rgb(251, 253, 255),
            Color.rgb(248, 252, 255), Color.rgb(199, 230, 249), Color.rgb(234, 246, 253),
            Color.WHITE, Color.rgb(20, 59, 87), Color.rgb(83, 116, 139),
            Color.rgb(116, 148, 169), Color.rgb(185, 221, 239), Color.rgb(112, 184, 220),
            Color.rgb(215, 238, 251), Color.rgb(2, 132, 199))
            .withDetails(Color.rgb(185, 221, 239), Color.rgb(2, 132, 199), Color.rgb(14, 165, 233),
                    Color.rgb(2, 132, 199), Color.rgb(56, 189, 248), Color.rgb(2, 132, 199),
                    Color.rgb(116, 148, 169), Color.rgb(3, 105, 161));

    private static final Palette LIGHT_BLUE_DARK = new Palette(ID_LIGHT_BLUE_DARK, "Sky Blue (Dark)", true,
            Color.rgb(7, 27, 39), Color.rgb(9, 37, 53), Color.rgb(12, 45, 62),
            Color.rgb(16, 52, 71), Color.rgb(23, 82, 113), Color.rgb(18, 57, 76),
            Color.rgb(12, 45, 62), Color.rgb(240, 249, 255), Color.rgb(170, 200, 217),
            Color.rgb(130, 165, 184), Color.rgb(40, 90, 115), Color.rgb(45, 141, 184),
            Color.rgb(16, 74, 101), Color.rgb(56, 189, 248))
            .withDetails(Color.rgb(40, 90, 115), Color.rgb(56, 189, 248), Color.rgb(125, 211, 252),
                    Color.rgb(56, 189, 248), Color.rgb(125, 211, 252), Color.rgb(56, 189, 248),
                    Color.rgb(130, 165, 184), Color.rgb(14, 165, 233));

    private static final Palette PURPLE_LIGHT = new Palette(ID_PURPLE_LIGHT, "Purple (Light)", false,
            Color.rgb(251, 248, 255), Color.rgb(243, 234, 255), Color.rgb(254, 252, 255),
            Color.rgb(252, 249, 255), Color.rgb(226, 205, 250), Color.rgb(246, 238, 255),
            Color.WHITE, Color.rgb(59, 29, 90), Color.rgb(107, 84, 125),
            Color.rgb(133, 109, 150), Color.rgb(222, 200, 242), Color.rgb(183, 131, 227),
            Color.rgb(240, 225, 255), Color.rgb(126, 34, 206))
            .withDetails(Color.rgb(222, 200, 242), Color.rgb(126, 34, 206), Color.rgb(168, 85, 247),
                    Color.rgb(126, 34, 206), Color.rgb(168, 85, 247), Color.rgb(126, 34, 206),
                    Color.rgb(133, 109, 150), Color.rgb(126, 34, 206));

    private static final Palette PURPLE_DARK = new Palette(ID_PURPLE_DARK, "Purple (Dark)", true,
            Color.rgb(23, 13, 35), Color.rgb(26, 15, 40), Color.rgb(33, 19, 47),
            Color.rgb(39, 22, 56), Color.rgb(74, 43, 104), Color.rgb(43, 24, 61),
            Color.rgb(33, 19, 47), Color.rgb(250, 245, 255), Color.rgb(196, 181, 208),
            Color.rgb(157, 135, 174), Color.rgb(83, 53, 106), Color.rgb(139, 92, 246),
            Color.rgb(64, 33, 93), Color.rgb(192, 132, 252))
            .withDetails(Color.rgb(83, 53, 106), Color.rgb(192, 132, 252), Color.rgb(216, 180, 254),
                    Color.rgb(192, 132, 252), Color.rgb(216, 180, 254), Color.rgb(192, 132, 252),
                    Color.rgb(157, 135, 174), Color.rgb(168, 85, 247));

    private static final Palette CRIMSON_LIGHT = new Palette(ID_CRIMSON_LIGHT, "Crimson (Light)", false,
            Color.rgb(255, 248, 249), Color.rgb(255, 236, 239), Color.rgb(255, 252, 252),
            Color.rgb(255, 249, 250), Color.rgb(249, 204, 212), Color.rgb(255, 240, 242),
            Color.WHITE, Color.rgb(74, 22, 32), Color.rgb(123, 75, 84),
            Color.rgb(149, 104, 115), Color.rgb(244, 194, 203), Color.rgb(232, 121, 141),
            Color.rgb(255, 224, 229), Color.rgb(190, 18, 60))
            .withDetails(Color.rgb(244, 194, 203), Color.rgb(190, 18, 60), Color.rgb(225, 29, 72),
                    Color.rgb(190, 18, 60), Color.rgb(225, 29, 72), Color.rgb(190, 18, 60),
                    Color.rgb(149, 104, 115), Color.rgb(190, 18, 60));

    private static final Palette CRIMSON_DARK = new Palette(ID_CRIMSON_DARK, "Crimson (Dark)", true,
            Color.rgb(33, 8, 14), Color.rgb(41, 11, 19), Color.rgb(49, 15, 24),
            Color.rgb(56, 17, 28), Color.rgb(100, 30, 48), Color.rgb(64, 20, 33),
            Color.rgb(49, 15, 24), Color.rgb(255, 241, 242), Color.rgb(216, 173, 181),
            Color.rgb(184, 135, 145), Color.rgb(113, 48, 66), Color.rgb(190, 52, 85),
            Color.rgb(88, 23, 42), Color.rgb(251, 113, 133))
            .withDetails(Color.rgb(113, 48, 66), Color.rgb(251, 113, 133), Color.rgb(253, 164, 175),
                    Color.rgb(251, 113, 133), Color.rgb(253, 164, 175), Color.rgb(251, 113, 133),
                    Color.rgb(184, 135, 145), Color.rgb(225, 29, 72));

    private static final Palette PINK_LIGHT = new Palette(ID_PINK_LIGHT, "Pink (Light)", false,
            Color.rgb(255, 247, 251), Color.rgb(252, 231, 243), Color.rgb(255, 251, 253),
            Color.rgb(255, 249, 252), Color.rgb(247, 204, 228), Color.rgb(253, 240, 247),
            Color.WHITE, Color.rgb(85, 27, 61), Color.rgb(135, 92, 117),
            Color.rgb(164, 122, 146), Color.rgb(243, 195, 218), Color.rgb(232, 121, 176),
            Color.rgb(252, 224, 239), Color.rgb(219, 39, 119))
            .withDetails(Color.rgb(243, 195, 218), Color.rgb(219, 39, 119), Color.rgb(236, 72, 153),
                    Color.rgb(219, 39, 119), Color.rgb(244, 114, 182), Color.rgb(219, 39, 119),
                    Color.rgb(164, 122, 146), Color.rgb(190, 24, 93));

    private static final Palette PINK_DARK = new Palette(ID_PINK_DARK, "Pink (Dark)", true,
            Color.rgb(42, 13, 29), Color.rgb(50, 17, 36), Color.rgb(58, 21, 42),
            Color.rgb(66, 24, 47), Color.rgb(113, 43, 82), Color.rgb(72, 27, 52),
            Color.rgb(58, 21, 42), Color.rgb(253, 242, 248), Color.rgb(215, 168, 194),
            Color.rgb(184, 132, 160), Color.rgb(113, 51, 82), Color.rgb(190, 73, 127),
            Color.rgb(99, 34, 72), Color.rgb(244, 114, 182))
            .withDetails(Color.rgb(113, 51, 82), Color.rgb(244, 114, 182), Color.rgb(249, 168, 212),
                    Color.rgb(244, 114, 182), Color.rgb(249, 168, 212), Color.rgb(244, 114, 182),
                    Color.rgb(184, 132, 160), Color.rgb(236, 72, 153));

    private static final Palette PURE_WHITE = new Palette(ID_PURE_WHITE, "Pure White", false,
            Color.WHITE, Color.rgb(250, 250, 250), Color.WHITE,
            Color.WHITE, Color.rgb(222, 222, 222), Color.rgb(245, 245, 245),
            Color.WHITE, Color.BLACK, Color.rgb(70, 70, 70),
            Color.rgb(115, 115, 115), Color.rgb(205, 205, 205), Color.rgb(128, 128, 128),
            Color.rgb(224, 224, 224), Color.BLACK)
            .withDetails(Color.rgb(205, 205, 205), Color.BLACK, Color.rgb(80, 80, 80),
                    Color.BLACK, Color.rgb(96, 96, 96), Color.rgb(128, 128, 128),
                    Color.rgb(160, 160, 160), Color.BLACK);

    private static final Palette PURE_DARK = new Palette(ID_PURE_DARK, "Pure Dark", true,
            Color.BLACK, Color.BLACK, Color.rgb(6, 6, 6),
            Color.rgb(12, 12, 12), Color.rgb(56, 56, 56), Color.rgb(20, 20, 20),
            Color.rgb(6, 6, 6), Color.WHITE, Color.rgb(190, 190, 190),
            Color.rgb(140, 140, 140), Color.rgb(54, 54, 54), Color.rgb(112, 112, 112),
            Color.rgb(64, 64, 64), Color.WHITE)
            .withDetails(Color.rgb(54, 54, 54), Color.WHITE, Color.rgb(176, 176, 176),
                    Color.WHITE, Color.rgb(208, 208, 208), Color.rgb(112, 112, 112),
                    Color.rgb(140, 140, 140), Color.WHITE);

    private static String cachedJson;
    private static String cachedNormalId;
    private static int cachedNight = -1;
    private static Palette cachedNormal;

    private Theme() {
    }

    static boolean dark(Context context) { return palette(context).dark; }
    static int background(Context context) { return palette(context).background; }
    static int topBar(Context context) { return palette(context).topBar; }
    static int surface(Context context) { return palette(context).surface; }
    static int post(Context context) { return palette(context).post; }
    static int unread(Context context) { return palette(context).unread; }
    static int field(Context context) { return palette(context).field; }
    static int menu(Context context) { return surface(context); }
    static int text(Context context) { return palette(context).text; }
    static int muted(Context context) { return palette(context).muted; }
    static int subtle(Context context) { return palette(context).subtle; }
    static int border(Context context) { return palette(context).border; }
    static int strongBorder(Context context) { return palette(context).strongBorder; }
    static int active(Context context) { return palette(context).active; }
    static int searchHighlight(Context context) { return active(context); }
    static int accent(Context context) { return palette(context).accent; }
    static int sidebar(Context context) { return border(context); }
    static int sidebarThumb(Context context) { return accent(context); }
    static int sidebarUnread(Context context) { return palette(context).sidebarUnread; }
    static int myPostMarker(Context context) { return accent(context); }
    static int replyPostMarker(Context context) { return palette(context).replyPostMarker; }
    static int treeConnector(Context context) { return strongBorder(context); }
    static int metricLow(Context context) { return palette(context).metricLow; }
    static int metricHigh(Context context) { return palette(context).metricHigh; }

    static int contrastingText(int background) {
        double luminance = 0.2126d * linearColorChannel(Color.red(background))
                + 0.7152d * linearColorChannel(Color.green(background))
                + 0.0722d * linearColorChannel(Color.blue(background));
        double blackContrast = (luminance + 0.05d) / 0.05d;
        double whiteContrast = 1.05d / (luminance + 0.05d);
        return blackContrast >= whiteContrast ? Color.BLACK : Color.WHITE;
    }

    private static double linearColorChannel(int channel) {
        double value = channel / 255d;
        return value <= 0.04045d ? value / 12.92d : Math.pow((value + 0.055d) / 1.055d, 2.4d);
    }

    static String normalSelection(Context context) {
        SharedPreferences prefs = preferences(context);
        if (prefs.contains(PREF_NORMAL_THEME)) {
            return normalizeThemeId(prefs.getString(PREF_NORMAL_THEME, MODE_SYSTEM));
        }
        return normalizeThemeId(prefs.getString(MainActivity.PREF_THEME_MODE, MODE_SYSTEM));
    }

    static String systemLightSelection(Context context) {
        return validSystemSelection(context, false,
                preferences(context).getString(PREF_SYSTEM_LIGHT_THEME, ID_TEAL_LIGHT));
    }

    static String systemDarkSelection(Context context) {
        return validSystemSelection(context, true,
                preferences(context).getString(PREF_SYSTEM_DARK_THEME, ID_TEAL_DARK));
    }

    private static String validSystemSelection(Context context, boolean dark, String id) {
        String normalizedId = normalizeThemeId(id);
        Palette palette = paletteById(context, normalizedId);
        if (palette != null && palette.dark == dark) return normalizedId;
        return dark ? ID_TEAL_DARK : ID_TEAL_LIGHT;
    }

    private static String normalizeThemeId(String id) {
        if ("monet_white".equals(id)) return ID_PURE_WHITE;
        if ("monet_black".equals(id)) return ID_PURE_DARK;
        return id;
    }

    static String signature(Context context) {
        SharedPreferences prefs = preferences(context);
        int night = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return normalSelection(context) + "|" + systemLightSelection(context) + "|"
                + systemDarkSelection(context) + "|" + night + "|"
                + prefs.getString(PREF_CUSTOM_THEMES, "[]");
    }

    static List<Palette> selectablePalettes(Context context) {
        List<Palette> result = builtInPalettes();
        result.addAll(customPalettes(context));
        return result;
    }

    static List<Palette> builtInPalettes() {
        List<Palette> result = new ArrayList<>();
        result.add(TEAL_LIGHT.copy());
        result.add(TEAL_DARK.copy());
        result.add(GREEN_LIGHT.copy());
        result.add(PRIVATE.copy());
        result.add(BLUE.copy());
        result.add(BLUE_DARK.copy());
        result.add(LIGHT_BLUE_LIGHT.copy());
        result.add(LIGHT_BLUE_DARK.copy());
        result.add(LIGHT.copy());
        result.add(DARK.copy());
        result.add(PURPLE_LIGHT.copy());
        result.add(PURPLE_DARK.copy());
        result.add(CRIMSON_LIGHT.copy());
        result.add(CRIMSON_DARK.copy());
        result.add(PINK_LIGHT.copy());
        result.add(PINK_DARK.copy());
        result.add(PURE_WHITE.copy());
        result.add(PURE_DARK.copy());
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
        id = normalizeThemeId(id);
        if (MODE_LIGHT.equals(id)) return LIGHT.copy();
        if (MODE_DARK.equals(id)) return DARK.copy();
        if (ID_TEAL_LIGHT.equals(id)) return TEAL_LIGHT.copy();
        if (ID_TEAL_DARK.equals(id)) return TEAL_DARK.copy();
        if (ID_GREEN_LIGHT.equals(id)) return GREEN_LIGHT.copy();
        if (ID_PRIVATE.equals(id)) return PRIVATE.copy();
        if (ID_BLUE.equals(id)) return BLUE.copy();
        if (ID_BLUE_DARK.equals(id)) return BLUE_DARK.copy();
        if (ID_LIGHT_BLUE_LIGHT.equals(id)) return LIGHT_BLUE_LIGHT.copy();
        if (ID_LIGHT_BLUE_DARK.equals(id)) return LIGHT_BLUE_DARK.copy();
        if (ID_PURPLE_LIGHT.equals(id)) return PURPLE_LIGHT.copy();
        if (ID_PURPLE_DARK.equals(id)) return PURPLE_DARK.copy();
        if (ID_CRIMSON_LIGHT.equals(id)) return CRIMSON_LIGHT.copy();
        if (ID_CRIMSON_DARK.equals(id)) return CRIMSON_DARK.copy();
        if (ID_PINK_LIGHT.equals(id)) return PINK_LIGHT.copy();
        if (ID_PINK_DARK.equals(id)) return PINK_DARK.copy();
        if (ID_PURE_WHITE.equals(id)) return PURE_WHITE.copy();
        if (ID_PURE_DARK.equals(id)) return PURE_DARK.copy();
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
        if (id.equals(systemLightSelection(context))) {
            editor.putString(PREF_SYSTEM_LIGHT_THEME, ID_TEAL_LIGHT);
        }
        if (id.equals(systemDarkSelection(context))) {
            editor.putString(PREF_SYSTEM_DARK_THEME, ID_TEAL_DARK);
        }
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
        id = normalizeThemeId(id);
        if (MODE_SYSTEM.equals(id)) return MainActivity.text("端末のテーマに従う", "Follow device theme");
        Palette palette = paletteById(context, id);
        if (palette == null) return MainActivity.text("不明なテーマ", "Unknown theme");
        if (MODE_LIGHT.equals(id)) return MainActivity.text("インディゴ (ライト)", "Indigo (Light)");
        if (MODE_DARK.equals(id)) return MainActivity.text("インディゴ (ダーク)", "Indigo (Dark)");
        if (ID_TEAL_LIGHT.equals(id)) return MainActivity.text("ティール (ライト)", "Teal (Light)");
        if (ID_TEAL_DARK.equals(id)) return MainActivity.text("ティール (ダーク)", "Teal (Dark)");
        if (ID_GREEN_LIGHT.equals(id)) return MainActivity.text("グリーン (ライト)", "Green (Light)");
        if (ID_PRIVATE.equals(id)) return MainActivity.text("グリーン (ダーク)", "Green (Dark)");
        if (ID_BLUE.equals(id)) return MainActivity.text("ブルー (ライト)", "Blue (Light)");
        if (ID_BLUE_DARK.equals(id)) return MainActivity.text("ブルー (ダーク)", "Blue (Dark)");
        if (ID_LIGHT_BLUE_LIGHT.equals(id)) return MainActivity.text("スカイブルー (ライト)", "Sky Blue (Light)");
        if (ID_LIGHT_BLUE_DARK.equals(id)) return MainActivity.text("スカイブルー (ダーク)", "Sky Blue (Dark)");
        if (ID_PURPLE_LIGHT.equals(id)) return MainActivity.text("パープル (ライト)", "Purple (Light)");
        if (ID_PURPLE_DARK.equals(id)) return MainActivity.text("パープル (ダーク)", "Purple (Dark)");
        if (ID_CRIMSON_LIGHT.equals(id)) return MainActivity.text("クリムゾン (ライト)", "Crimson (Light)");
        if (ID_CRIMSON_DARK.equals(id)) return MainActivity.text("クリムゾン (ダーク)", "Crimson (Dark)");
        if (ID_PINK_LIGHT.equals(id)) return MainActivity.text("ピンク (ライト)", "Pink (Light)");
        if (ID_PINK_DARK.equals(id)) return MainActivity.text("ピンク (ダーク)", "Pink (Dark)");
        if (ID_PURE_WHITE.equals(id)) return MainActivity.text("ピュアホワイト", "Pure White");
        if (ID_PURE_DARK.equals(id)) return MainActivity.text("ピュアダーク", "Pure Dark");
        return palette.name;
    }

    static Palette previewPalette(Context context, String id) {
        if (MODE_SYSTEM.equals(id)) {
            int night = context.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            String selected = night == Configuration.UI_MODE_NIGHT_YES
                    ? systemDarkSelection(context) : systemLightSelection(context);
            Palette palette = paletteById(context, selected);
            return (palette == null ? (night == Configuration.UI_MODE_NIGHT_YES ? TEAL_DARK : TEAL_LIGHT)
                    : palette).copy();
        }
        Palette palette = paletteById(context, id);
        return palette == null ? LIGHT.copy() : palette;
    }

    static void tintCompoundButton(Context context, CompoundButton button) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || button == null) return;
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = new int[]{accent(context), subtle(context)};
        button.setButtonTintList(new ColorStateList(states, colors));
    }

    static void tintProgressBar(Context context, ProgressBar progressBar) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || progressBar == null) return;
        ColorStateList accentTint = ColorStateList.valueOf(accent(context));
        progressBar.setIndeterminateTintList(accentTint);
        progressBar.setProgressTintList(accentTint);
        progressBar.setSecondaryProgressTintList(ColorStateList.valueOf(strongBorder(context)));
        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(border(context)));
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
        tintDialogText(decor, context, textColor);
        if (dialog instanceof android.app.AlertDialog) {
            android.app.AlertDialog alert = (android.app.AlertDialog) dialog;
            if (alert.getButton(android.app.AlertDialog.BUTTON_POSITIVE) != null) alert.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(accentColor);
            if (alert.getButton(android.app.AlertDialog.BUTTON_NEGATIVE) != null) alert.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(accentColor);
            if (alert.getButton(android.app.AlertDialog.BUTTON_NEUTRAL) != null) alert.getButton(android.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(accentColor);
        }
    }

    static void styleDialogButtonEnabled(Context context, android.widget.Button button,
                                         boolean enabled) {
        if (button == null) return;
        button.setEnabled(enabled);
        button.setTextColor(enabled ? accent(context) : muted(context));
        button.setAlpha(enabled ? 1f : 0.55f);
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

    private static void tintDialogText(View view, Context context, int color) {
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            text.setTextColor(color);
            applyTextSelectionToText(context, text);
        }
        if (!(view instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            tintDialogText(group.getChildAt(i), context, color);
        }
    }

    static void applySystemBars(Activity activity) {
        activity.getWindow().setStatusBarColor(background(activity));
        activity.getWindow().setNavigationBarColor(topBar(activity));
        installTextSelectionTheme(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility();
            if (dark(activity)) flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            else flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (dark(activity)) flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                else flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private static void installTextSelectionTheme(Activity activity) {
        View decor = activity.getWindow().getDecorView();
        if (!Boolean.TRUE.equals(decor.getTag(R.id.tag_theme_text_selection_listener))) {
            decor.getViewTreeObserver().addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
                if (newFocus instanceof TextView) {
                    applyTextSelectionToText(activity, (TextView) newFocus);
                }
            });
            decor.setTag(R.id.tag_theme_text_selection_listener, true);
        }
        View focused = decor.findFocus();
        if (focused instanceof TextView) {
            applyTextSelectionToText(activity, (TextView) focused);
        }
    }

    static void applyTextSelection(Context context, View view) {
        if (view instanceof TextView) {
            applyTextSelectionToText(context, (TextView) view);
        }
        if (!(view instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            applyTextSelection(context, group.getChildAt(i));
        }
    }

    static void makeTextSelectable(Context context, TextView view) {
        view.setTextIsSelectable(true);
        applyTextSelectionToText(context, view);
    }

    private static void applyTextSelectionToText(Context context, TextView view) {
        view.setHighlightColor(active(context));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;
        int color = accent(context);
        Drawable cursor = tintedCopy(context, view.getTextCursorDrawable(), color);
        Drawable handle = tintedCopy(context, view.getTextSelectHandle(), color);
        Drawable handleLeft = tintedCopy(context, view.getTextSelectHandleLeft(), color);
        Drawable handleRight = tintedCopy(context, view.getTextSelectHandleRight(), color);
        if (cursor != null) view.setTextCursorDrawable(cursor);
        if (handle != null) view.setTextSelectHandle(handle);
        if (handleLeft != null) view.setTextSelectHandleLeft(handleLeft);
        if (handleRight != null) view.setTextSelectHandleRight(handleRight);
    }

    private static Drawable tintedCopy(Context context, Drawable source, int color) {
        if (source == null) return null;
        Drawable.ConstantState state = source.getConstantState();
        Drawable drawable = state == null
                ? source.mutate()
                : state.newDrawable(context.getResources()).mutate();
        drawable.setTint(color);
        return drawable;
    }

    private static synchronized Palette palette(Context context) {
        SharedPreferences prefs = preferences(context);
        String json = prefs.getString(PREF_CUSTOM_THEMES, "[]");
        String normalId = normalSelection(context);
        int night = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (!json.equals(cachedJson) || !normalId.equals(cachedNormalId) || night != cachedNight) {
            cachedJson = json;
            cachedNormalId = normalId;
            cachedNight = night;
            cachedNormal = resolve(context, normalId, night);
        }
        return cachedNormal;
    }

    private static Palette resolve(Context context, String id, int night) {
        if (MODE_SYSTEM.equals(id)) {
            String selected = night == Configuration.UI_MODE_NIGHT_YES
                    ? systemDarkSelection(context) : systemLightSelection(context);
            Palette palette = paletteById(context, selected);
            return palette == null
                    ? (night == Configuration.UI_MODE_NIGHT_YES ? TEAL_DARK : TEAL_LIGHT)
                    : palette;
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
        cachedNight = -1;
        cachedNormal = null;
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
        int accent;
        int sidebar;
        int sidebarThumb;
        int sidebarUnread;
        int myPostMarker;
        int replyPostMarker;
        int treeConnector;
        int metricLow;
        int metricHigh;

        Palette(String id, String name, boolean dark, int background, int topBar, int surface,
                int post, int unread, int field, int menu, int text, int muted, int subtle,
                int border, int strongBorder, int active, int accent) {
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
            this.accent = accent;
            this.sidebar = border;
            this.sidebarThumb = accent;
            this.sidebarUnread = accent;
            this.myPostMarker = accent;
            this.replyPostMarker = strongBorder;
            this.treeConnector = accent;
            this.metricLow = subtle;
            this.metricHigh = accent;
        }

        Palette withDetails(int sidebar, int sidebarThumb, int sidebarUnread, int myPostMarker,
                            int replyPostMarker, int treeConnector, int metricLow, int metricHigh) {
            this.sidebar = sidebar;
            this.sidebarThumb = sidebarThumb;
            this.sidebarUnread = sidebarUnread;
            this.myPostMarker = myPostMarker;
            this.replyPostMarker = replyPostMarker;
            this.treeConnector = treeConnector;
            this.metricLow = metricLow;
            this.metricHigh = metricHigh;
            return synchronizeLinkedColors();
        }

        private Palette synchronizeLinkedColors() {
            menu = surface;
            sidebar = border;
            sidebarThumb = accent;
            myPostMarker = accent;
            treeConnector = strongBorder;
            return this;
        }

        Palette copy() {
            return new Palette(id, name, dark, background, topBar, surface, post, unread, field,
                    menu, text, muted, subtle, border, strongBorder, active, accent)
                    .withDetails(sidebar, sidebarThumb, sidebarUnread, myPostMarker, replyPostMarker,
                            treeConnector, metricLow, metricHigh);
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
                case "accent": return accent;
                case "sidebar": return sidebar;
                case "sidebarThumb": return sidebarThumb;
                case "sidebarUnread": return sidebarUnread;
                case "myPostMarker": return myPostMarker;
                case "replyPostMarker": return replyPostMarker;
                case "treeConnector": return treeConnector;
                case "metricLow": return metricLow;
                case "metricHigh": return metricHigh;
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
                case "accent": accent = color; break;
                case "sidebar": sidebar = color; break;
                case "sidebarThumb": sidebarThumb = color; break;
                case "sidebarUnread": sidebarUnread = color; break;
                case "myPostMarker": myPostMarker = color; break;
                case "replyPostMarker": replyPostMarker = color; break;
                case "treeConnector": treeConnector = color; break;
                case "metricLow": metricLow = color; break;
                case "metricHigh": metricHigh = color; break;
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
            for (String key : COLOR_KEYS) {
                if (!colors.has(key) || colors.isNull(key)) {
                    throw new JSONException("Missing color: " + key);
                }
                palette.setColor(key, parseColor(colors.getString(key)));
            }
            return palette.synchronizeLinkedColors();
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
