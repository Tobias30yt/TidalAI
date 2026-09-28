package com.tidalai.views;

import com.tidalai.AppState;
import com.tidalai.Theme;
import com.tidalai.models.AIModel;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class ModelsView extends VBox {

    private final VBox modelList =
        new VBox(12);

    private final Label connectionStatus =
        new Label();

    private final Label modelCount =
        new Label();

    private final ProgressIndicator loading =
        new ProgressIndicator();

    public ModelsView() {

        setSpacing(20);
        setPadding(new Insets(10));
        setFillWidth(true);

        createHeader();
        createModelArea();

        refreshModels();
    }

    // =========================================================
    // HEADER
    // =========================================================

    private void createHeader() {

        Label title =
            new Label(
                "Models"
            );

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;"
        );

        Label subtitle =
            new Label(
                "Models available from your local LM Studio backend."
            );

        subtitle.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 13px;"
        );

        subtitle.setWrapText(true);

        HBox statusRow =
            new HBox(12);

        statusRow.setAlignment(
            Pos.CENTER_LEFT
        );

        connectionStatus.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );

        modelCount.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 12px;"
        );

        Button refreshButton =
            new Button(
                "Refresh Models"
            );

        refreshButton.setStyle(
            "-fx-background-color: " +
            Theme.accent() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 16;" +
            "-fx-font-weight: bold;"
        );

        refreshButton.setOnAction(
            event -> refreshModels()
        );

        HBox.setHgrow(
            connectionStatus,
            Priority.ALWAYS
        );

        statusRow.getChildren().addAll(
            connectionStatus,
            modelCount,
            refreshButton
        );

        getChildren().addAll(
            title,
            subtitle,
            statusRow
        );
    }

    // =========================================================
    // MODEL AREA
    // =========================================================

    private void createModelArea() {

        VBox card =
            new VBox(14);

        card.setPadding(
            new Insets(18)
        );

        card.setStyle(
            Theme.cardStyle()
        );

        Label title =
            new Label(
                "Available Models"
            );

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;"
        );

        loading.setVisible(false);
        loading.setPrefSize(24, 24);

        HBox loadingRow =
            new HBox(
                10,
                loading,
                new Label("Loading models...")
            );

        loadingRow.setAlignment(
            Pos.CENTER_LEFT
        );

        loadingRow.getChildren()
            .get(1)
            .setStyle(
                "-fx-text-fill: " +
                Theme.muted() + ";" +
                "-fx-font-size: 12px;"
            );

        ScrollPane scrollPane =
            new ScrollPane(
                modelList
            );

        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);

        scrollPane.setPrefHeight(450);

        scrollPane.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background: transparent;"
        );

        modelList.setFillWidth(true);

        VBox.setVgrow(
            scrollPane,
            Priority.ALWAYS
        );

        card.getChildren().addAll(
            title,
            loadingRow,
            scrollPane
        );

        VBox.setVgrow(
            card,
            Priority.ALWAYS
        );

        getChildren().add(
            card
        );
    }

    // =========================================================
    // REFRESH
    // =========================================================

    private void refreshModels() {

        loading.setVisible(true);

        modelList
            .getChildren()
            .clear();

        connectionStatus.setText(
            "Checking LM Studio..."
        );

        connectionStatus.setStyle(
            "-fx-text-fill: " +
            Theme.warning() + ";" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );

        Task<List<AIModel>> task =
            new Task<>() {

                @Override
                protected List<AIModel> call() {

                    return AppState.PROVIDER
                        .getModels();
                }
            };

        task.setOnSucceeded(
            event -> {

                loading.setVisible(false);

                List<AIModel> models =
                    task.getValue();

                boolean connected =
                    AppState.PROVIDER.isConnected();

                if (connected) {

                    connectionStatus.setText(
                        "LM Studio connected"
                    );

                    connectionStatus.setStyle(
                        "-fx-text-fill: " +
                        Theme.success() + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
                    );

                } else {

                    connectionStatus.setText(
                        "LM Studio not connected"
                    );

                    connectionStatus.setStyle(
                        "-fx-text-fill: " +
                        Theme.error() + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
                    );
                }

                modelCount.setText(
                    models.size() +
                    (
                        models.size() == 1
                            ? " model"
                            : " models"
                    )
                );

                if (models.isEmpty()) {

                    showEmptyState(
                        connected
                            ? "LM Studio is connected, but no chat models were found."
                            : "Could not connect to LM Studio."
                    );

                    return;
                }

                for (
                    AIModel model :
                    models
                ) {

                    modelList
                        .getChildren()
                        .add(
                            createModelCard(model)
                        );
                }
            }
        );

        task.setOnFailed(
            event -> {

                loading.setVisible(false);

                connectionStatus.setText(
                    "LM Studio error"
                );

                connectionStatus.setStyle(
                    "-fx-text-fill: " +
                    Theme.error() + ";" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;"
                );

                modelCount.setText(
                    "0 models"
                );

                String message =
                    task.getException() == null
                        ? "Unknown error."
                        : task
                            .getException()
                            .getMessage();

                showEmptyState(
                    "Could not load models:\n" +
                    message
                );
            }
        );

        Thread thread =
            new Thread(
                task,
                "tidalai-model-loader"
            );

        thread.setDaemon(true);
        thread.start();
    }

    // =========================================================
    // MODEL CARD
    // =========================================================

    private VBox createModelCard(
        AIModel model
    ) {

        VBox card =
            new VBox(8);

        card.setPadding(
            new Insets(16)
        );

        card.setStyle(
            Theme.cardStyle()
        );

        Label name =
            new Label(
                model.getName()
            );

        name.setWrapText(true);

        name.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );

        Label id =
            new Label(
                model.getId()
            );

        id.setWrapText(true);

        id.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 12px;"
        );

        HBox infoRow =
            new HBox(10);

        infoRow.setAlignment(
            Pos.CENTER_LEFT
        );

        Label type =
            new Label(
                "CHAT MODEL"
            );

        type.setStyle(
            "-fx-text-fill: " +
            Theme.accent() + ";" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;"
        );

        Label status =
            new Label(
                model.isLoaded()
                    ? "Loaded"
                    : "Available"
            );

        status.setStyle(
            "-fx-text-fill: " +
            (
                model.isLoaded()
                    ? Theme.success()
                    : Theme.muted()
            ) +
            ";" +
            "-fx-font-size: 11px;"
        );

        infoRow.getChildren().addAll(
            type,
            status
        );

        card.getChildren().addAll(
            name,
            id,
            infoRow
        );

        return card;
    }

    // =========================================================
    // EMPTY
    // =========================================================

    private void showEmptyState(
        String message
    ) {

        VBox empty =
            new VBox(10);

        empty.setAlignment(
            Pos.CENTER
        );

        empty.setPadding(
            new Insets(40)
        );

        Label title =
            new Label(
                "No models available"
            );

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );

        Label description =
            new Label(
                message
            );

        description.setWrapText(true);

        description.setMaxWidth(500);

        description.setAlignment(
            Pos.CENTER
        );

        description.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 12px;"
        );

        empty.getChildren().addAll(
            title,
            description
        );

        modelList
            .getChildren()
            .add(
                empty
            );
    }
}
