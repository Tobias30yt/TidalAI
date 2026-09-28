package com.tidalai;

import javafx.geometry.Pos;
import javafx.scene.control.Button;

public class NavigationButton extends Button {

    private boolean active = false;

    public NavigationButton(
        String text,
        Runnable action
    ) {

        super(text);

        setMaxWidth(
            Double.MAX_VALUE
        );

        setAlignment(
            Pos.CENTER_LEFT
        );

        setOnAction(
            event -> action.run()
        );

        applyNormalStyle();

        setOnMouseEntered(
            event -> {

                if (!active) {
                    applyHoverStyle();
                }
            }
        );

        setOnMouseExited(
            event -> {

                if (active) {
                    applyActiveStyle();
                } else {
                    applyNormalStyle();
                }
            }
        );
    }

    public void setActive(
        boolean active
    ) {

        this.active =
            active;

        if (active) {
            applyActiveStyle();
        } else {
            applyNormalStyle();
        }
    }

    public void refreshTheme() {

        if (active) {
            applyActiveStyle();
        } else {
            applyNormalStyle();
        }
    }

    private void applyNormalStyle() {

        setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 11 14;" +
            "-fx-font-size: 13px;" +
            "-fx-alignment: CENTER-LEFT;"
        );
    }

    private void applyHoverStyle() {

        setStyle(
            "-fx-background-color: " +
            Theme.cardHover() + ";" +
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 11 14;" +
            "-fx-font-size: 13px;" +
            "-fx-alignment: CENTER-LEFT;"
        );
    }

    private void applyActiveStyle() {

        setStyle(
            "-fx-background-color: " +
            Theme.accent() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 11 14;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-alignment: CENTER-LEFT;"
        );
    }
}
