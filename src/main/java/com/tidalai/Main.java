package com.tidalai;

import java.io.InputStream;

import com.tidalai.providers.LMStudioProvider;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        LMStudioProvider provider =
            new LMStudioProvider();

        System.out.println(
            "LM Studio connected: " +
            provider.isConnected()
        );

        Dashboard dashboard =
            new Dashboard();

        Scene scene =
            new Scene(
                dashboard,
                1280,
                800
            );

        stage.setTitle("TidalAI");

        InputStream icon =
            getClass()
                .getResourceAsStream("/iconig.png");

        if (icon != null) {
            stage.getIcons().add(
                new Image(icon)
            );
        } else {
            System.out.println(
                "WARNING: iconig.png not found!"
            );
        }

        stage.setMinWidth(950);
        stage.setMinHeight(600);
        stage.setResizable(true);

        scene.setOnKeyPressed(event -> {

            if (event.getCode() == KeyCode.F11) {

                stage.setFullScreen(
                    !stage.isFullScreen()
                );

                event.consume();
            }
        });

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}