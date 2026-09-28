package com.tidalai;

public final class Theme {

    private Theme() {
    }

    private static boolean darkMode = true;

    // =========================================================
    // DARK
    // =========================================================

    private static final String DARK_BACKGROUND =
        "#0B0D10";

    private static final String DARK_SIDEBAR =
        "#11141A";

    private static final String DARK_CARD =
        "#151922";

    private static final String DARK_CARD_HOVER =
        "#1B202B";

    private static final String DARK_ACCENT =
        "#7C5CFC";

    private static final String DARK_ACCENT_HOVER =
        "#9278FF";

    private static final String DARK_TEXT =
        "#F2F3F5";

    private static final String DARK_MUTED =
        "#8B919E";

    private static final String DARK_SUCCESS =
        "#45D483";

    private static final String DARK_WARNING =
        "#F2C94C";

    private static final String DARK_ERROR =
        "#F05D5E";

    private static final String DARK_BORDER =
        "#242936";

    // =========================================================
    // LIGHT
    // =========================================================

    private static final String LIGHT_BACKGROUND =
        "#F5F6F8";

    private static final String LIGHT_SIDEBAR =
        "#FFFFFF";

    private static final String LIGHT_CARD =
        "#FFFFFF";

    private static final String LIGHT_CARD_HOVER =
        "#EEF0F4";

    private static final String LIGHT_ACCENT =
        "#6D4AFF";

    private static final String LIGHT_ACCENT_HOVER =
        "#805FFF";

    private static final String LIGHT_TEXT =
        "#17191D";

    private static final String LIGHT_MUTED =
        "#6B7280";

    private static final String LIGHT_SUCCESS =
        "#20A464";

    private static final String LIGHT_WARNING =
        "#C58A00";

    private static final String LIGHT_ERROR =
        "#D9363E";

    private static final String LIGHT_BORDER =
        "#DDE1E7";

    // =========================================================
    // COMPATIBILITY FIELDS
    // =========================================================

    public static String BACKGROUND =
        DARK_BACKGROUND;

    public static String SIDEBAR =
        DARK_SIDEBAR;

    public static String CARD =
        DARK_CARD;

    public static String CARD_HOVER =
        DARK_CARD_HOVER;

    public static String ACCENT =
        DARK_ACCENT;

    public static String ACCENT_HOVER =
        DARK_ACCENT_HOVER;

    public static String TEXT =
        DARK_TEXT;

    public static String MUTED =
        DARK_MUTED;

    public static String SUCCESS =
        DARK_SUCCESS;

    public static String WARNING =
        DARK_WARNING;

    public static String ERROR =
        DARK_ERROR;

    public static String BORDER =
        DARK_BORDER;

    public static String CARD_STYLE =
        buildCardStyle();

    public static String BUTTON_STYLE =
        buildButtonStyle();

    // =========================================================
    // COLORS
    // =========================================================

    public static String background() {
        return darkMode
            ? DARK_BACKGROUND
            : LIGHT_BACKGROUND;
    }

    public static String sidebar() {
        return darkMode
            ? DARK_SIDEBAR
            : LIGHT_SIDEBAR;
    }

    public static String cardColor() {
        return darkMode
            ? DARK_CARD
            : LIGHT_CARD;
    }

    public static String cardHover() {
        return darkMode
            ? DARK_CARD_HOVER
            : LIGHT_CARD_HOVER;
    }

    public static String accent() {
        return darkMode
            ? DARK_ACCENT
            : LIGHT_ACCENT;
    }

    public static String accentHover() {
        return darkMode
            ? DARK_ACCENT_HOVER
            : LIGHT_ACCENT_HOVER;
    }

    public static String text() {
        return darkMode
            ? DARK_TEXT
            : LIGHT_TEXT;
    }

    public static String muted() {
        return darkMode
            ? DARK_MUTED
            : LIGHT_MUTED;
    }

    public static String success() {
        return darkMode
            ? DARK_SUCCESS
            : LIGHT_SUCCESS;
    }

    public static String warning() {
        return darkMode
            ? DARK_WARNING
            : LIGHT_WARNING;
    }

    public static String error() {
        return darkMode
            ? DARK_ERROR
            : LIGHT_ERROR;
    }

    public static String border() {
        return darkMode
            ? DARK_BORDER
            : LIGHT_BORDER;
    }

    // =========================================================
    // GLOBAL JAVAFX CONTROL STYLE
    // =========================================================

    /*
     * JavaFX controls such as ComboBox, ListView, TextField,
     * ScrollBar, CheckBox and their popup cells inherit many
     * colors from Modena.
     *
     * This style ensures those default colors follow TidalAI's
     * current theme instead of remaining inverted.
     */
    public static String globalStyle() {

        String base =
            darkMode
                ? DARK_CARD
                : LIGHT_CARD;

        String baseAlt =
            darkMode
                ? DARK_CARD_HOVER
                : LIGHT_CARD_HOVER;

        String text =
            darkMode
                ? DARK_TEXT
                : LIGHT_TEXT;

        String muted =
            darkMode
                ? DARK_MUTED
                : LIGHT_MUTED;

        String accent =
            darkMode
                ? DARK_ACCENT
                : LIGHT_ACCENT;

        String border =
            darkMode
                ? DARK_BORDER
                : LIGHT_BORDER;

        String background =
            darkMode
                ? DARK_BACKGROUND
                : LIGHT_BACKGROUND;

        return
            "-fx-base: " +
            base +
            ";" +

            "-fx-background: " +
            background +
            ";" +

            "-fx-control-inner-background: " +
            base +
            ";" +

            "-fx-control-inner-background-alt: " +
            baseAlt +
            ";" +

            "-fx-text-base-color: " +
            text +
            ";" +

            "-fx-text-background-color: " +
            text +
            ";" +

            "-fx-text-inner-color: " +
            text +
            ";" +

            "-fx-prompt-text-fill: " +
            muted +
            ";" +

            "-fx-accent: " +
            accent +
            ";" +

            "-fx-focus-color: " +
            accent +
            ";" +

            "-fx-faint-focus-color: transparent;" +

            "-fx-border-color: " +
            border +
            ";" +

            "-fx-selection-bar: " +
            accent +
            ";" +

            "-fx-selection-bar-non-focused: " +
            accent +
            ";" +

            "-fx-text-fill: " +
            text +
            ";";
    }

    // =========================================================
    // STYLES
    // =========================================================

    public static String cardStyle() {

        return
            "-fx-background-color: " +
            cardColor() +
            ";" +

            "-fx-background-radius: 14;" +

            "-fx-border-color: " +
            border() +
            ";" +

            "-fx-border-radius: 14;" +

            "-fx-border-width: 1;";
    }

    public static String buttonStyle() {

        return
            "-fx-background-color: " +
            cardColor() +
            ";" +

            "-fx-text-fill: " +
            text() +
            ";" +

            "-fx-background-radius: 10;" +

            "-fx-border-radius: 10;" +

            "-fx-border-color: " +
            border() +
            ";" +

            "-fx-font-size: 13px;" +

            "-fx-padding: 10 16;";
    }

    public static String card() {
        return cardStyle();
    }

    private static String buildCardStyle() {
        return cardStyle();
    }

    private static String buildButtonStyle() {
        return buttonStyle();
    }

    // =========================================================
    // MODE
    // =========================================================

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(
        boolean dark
    ) {

        darkMode =
            dark;

        refreshConstants();
    }

    public static void toggle() {

        darkMode =
            !darkMode;

        refreshConstants();
    }

    private static void refreshConstants() {

        BACKGROUND =
            background();

        SIDEBAR =
            sidebar();

        CARD =
            cardColor();

        CARD_HOVER =
            cardHover();

        ACCENT =
            accent();

        ACCENT_HOVER =
            accentHover();

        TEXT =
            text();

        MUTED =
            muted();

        SUCCESS =
            success();

        WARNING =
            warning();

        ERROR =
            error();

        BORDER =
            border();

        CARD_STYLE =
            buildCardStyle();

        BUTTON_STYLE =
            buildButtonStyle();
    }
}
