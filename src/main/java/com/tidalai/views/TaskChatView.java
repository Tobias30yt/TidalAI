package com.tidalai.views;

import com.tidalai.AppState;
import com.tidalai.Theme;
import com.tidalai.chat.ChatMessage;
import com.tidalai.tasks.Task;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskChatView extends VBox {

    private final Task task;

    private final VBox messages =
        new VBox(14);

    private final TextArea input =
        new TextArea();

    private final ComboBox<String> modelSelector =
        new ComboBox<>();

    private final Button sendButton =
        new Button("Send");

    public TaskChatView(
        Task task
    ) {

        this.task =
            task;

        setSpacing(
            18
        );

        Label title =
            new Label(
                "Task Chat"
            );

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;"
        );

        Label taskLabel =
            new Label(
                task.getDescription()
            );

        taskLabel.setWrapText(
            true
        );

        taskLabel.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 13px;"
        );

        Label status =
            new Label(
                "● " +
                task.getStatus()
            );

        status.setStyle(
            "-fx-text-fill: " +
            Theme.accent() + ";" +
            "-fx-font-size: 12px;"
        );

        VBox header =
            new VBox(
                5,
                title,
                taskLabel,
                status
            );

        modelSelector.setPromptText(
            "Select model"
        );

        modelSelector.setMaxWidth(
            Double.MAX_VALUE
        );

        modelSelector.setStyle(
            "-fx-background-color: " +
            Theme.cardColor() + ";" +
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-border-color: " +
            Theme.border() + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;"
        );

        ScrollPane scrollPane =
            new ScrollPane(
                messages
            );

        scrollPane.setFitToWidth(
            true
        );

        scrollPane.setFitToHeight(
            true
        );

        scrollPane.setStyle(
            "-fx-background: " +
            Theme.background() + ";" +
            "-fx-background-color: " +
            Theme.background() + ";"
        );

        VBox.setVgrow(
            scrollPane,
            Priority.ALWAYS
        );

        messages.setPadding(
            new Insets(10)
        );

        input.setPromptText(
            "Continue working on this task..."
        );

        input.setWrapText(
            true
        );

        input.setPrefRowCount(
            3
        );

        input.setStyle(
            "-fx-control-inner-background: " +
            Theme.cardColor() + ";" +
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-prompt-text-fill: " +
            Theme.muted() + ";" +
            "-fx-background-color: " +
            Theme.cardColor() + ";" +
            "-fx-border-color: " +
            Theme.border() + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;"
        );

        sendButton.setStyle(
            "-fx-background-color: " +
            Theme.accent() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 12 22;" +
            "-fx-font-weight: bold;"
        );

        sendButton.setOnAction(
            event -> sendMessage()
        );

        HBox inputBar =
            new HBox(
                10,
                input,
                sendButton
            );

        inputBar.setAlignment(
            Pos.BOTTOM_CENTER
        );

        HBox.setHgrow(
            input,
            Priority.ALWAYS
        );

        getChildren().addAll(
            header,
            modelSelector,
            scrollPane,
            inputBar
        );

        renderConversation();
        loadModels();

        input.setOnKeyPressed(
            event -> {

                if (
                    event.isControlDown() &&
                    event.getCode() == KeyCode.ENTER
                ) {

                    sendMessage();

                    event.consume();
                }
            }
        );
    }

    // =========================================================
    // MODEL LOADING
    // =========================================================

    private void loadModels() {

        javafx.concurrent.Task<
            List<com.tidalai.models.AIModel>
        > loadTask =
            new javafx.concurrent.Task<>() {

                @Override
                protected List<com.tidalai.models.AIModel> call() {

                    return AppState.PROVIDER
                        .getModels();
                }
            };

        loadTask.setOnSucceeded(
            event -> {

                modelSelector
                    .getItems()
                    .clear();

                for (
                    var model :
                    loadTask.getValue()
                ) {

                    modelSelector
                        .getItems()
                        .add(
                            model.getId()
                        );
                }

                if (
                    !modelSelector
                        .getItems()
                        .isEmpty()
                ) {

                    modelSelector
                        .getSelectionModel()
                        .selectFirst();
                }
            }
        );

        loadTask.setOnFailed(
            event -> {

                addSimpleMessage(
                    "System",
                    "Could not load models from LM Studio."
                );
            }
        );

        Thread thread =
            new Thread(
                loadTask
            );

        thread.setDaemon(
            true
        );

        thread.start();
    }

    // =========================================================
    // SEND MESSAGE
    // =========================================================

    private void sendMessage() {

        String prompt =
            input
                .getText()
                .trim();

        if (
            prompt.isEmpty()
        ) {

            return;
        }

        String model =
            modelSelector
                .getSelectionModel()
                .getSelectedItem();

        if (
            model == null
        ) {

            addSimpleMessage(
                "System",
                "Please select a model first."
            );

            return;
        }

        task
            .getConversation()
            .addUserMessage(
                prompt
            );

        addSimpleMessage(
            "You",
            prompt
        );

        input.clear();

        sendButton.setDisable(
            true
        );

        VBox thinkingBubble =
            createBubble(
                "TidalAI",
                "Thinking..."
            );

        messages
            .getChildren()
            .add(
                thinkingBubble
            );

        List<ChatMessage> context =
            task
                .getConversation()
                .getMessages();

        javafx.concurrent.Task<String> requestTask =
            new javafx.concurrent.Task<>() {

                @Override
                protected String call() {

                    return AppState.PROVIDER.chat(
                        model,
                        context
                    );
                }
            };

        requestTask.setOnSucceeded(
            event -> {

                String response =
                    requestTask.getValue();

                task
                    .getConversation()
                    .addAssistantMessage(
                        response
                    );

                updateBubble(
                    thinkingBubble,
                    "TidalAI",
                    response
                );

                sendButton.setDisable(
                    false
                );

                scrollToBottom();
            }
        );

        requestTask.setOnFailed(
            event -> {

                String error =
                    requestTask.getException() == null
                        ? "Unknown error"
                        : requestTask
                            .getException()
                            .getMessage();

                updateBubble(
                    thinkingBubble,
                    "TidalAI",
                    "Error: " + error
                );

                sendButton.setDisable(
                    false
                );

                scrollToBottom();
            }
        );

        Thread thread =
            new Thread(
                requestTask
            );

        thread.setDaemon(
            true
        );

        thread.start();
    }

    // =========================================================
    // CONVERSATION
    // =========================================================

    private void renderConversation() {

        messages
            .getChildren()
            .clear();

        for (
            ChatMessage message :
            task
                .getConversation()
                .getMessages()
        ) {

            String sender;

            if (
                "user".equals(
                    message.role()
                )
            ) {

                sender =
                    "You";

            } else if (
                "assistant".equals(
                    message.role()
                )
            ) {

                sender =
                    "TidalAI";

            } else {

                sender =
                    "System";
            }

            messages
                .getChildren()
                .add(
                    createBubble(
                        sender,
                        message.content()
                    )
                );
        }
    }

    // =========================================================
    // MESSAGE UI
    // =========================================================

    private VBox createBubble(
        String sender,
        String text
    ) {

        VBox bubble =
            new VBox(8);

        bubble.setPadding(
            new Insets(14)
        );

        bubble.setMaxWidth(
            Double.MAX_VALUE
        );

        bubble.setStyle(
            Theme.cardStyle()
        );

        Label senderLabel =
            new Label(
                sender
            );

        senderLabel.setStyle(
            "-fx-text-fill: " +
            Theme.accent() + ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );

        bubble.getChildren().addAll(
            senderLabel,
            renderMarkdown(text)
        );

        return bubble;
    }

    private void updateBubble(
        VBox bubble,
        String sender,
        String text
    ) {

        bubble
            .getChildren()
            .clear();

        Label senderLabel =
            new Label(
                sender
            );

        senderLabel.setStyle(
            "-fx-text-fill: " +
            Theme.accent() + ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );

        bubble.getChildren().addAll(
            senderLabel,
            renderMarkdown(text)
        );
    }

    // =========================================================
    // MARKDOWN
    // =========================================================

    private VBox renderMarkdown(
        String text
    ) {

        VBox container =
            new VBox(8);

        String safeText =
            text == null
                ? ""
                : text;

        Pattern pattern =
            Pattern.compile(
                "```(\\w*)\\s*\\n?(.*?)```",
                Pattern.DOTALL
            );

        Matcher matcher =
            pattern.matcher(
                safeText
            );

        int lastEnd =
            0;

        while (
            matcher.find()
        ) {

            addNormalText(
                container,
                safeText.substring(
                    lastEnd,
                    matcher.start()
                )
            );

            addCodeBlock(
                container,
                matcher.group(1),
                matcher.group(2)
            );

            lastEnd =
                matcher.end();
        }

        addNormalText(
            container,
            safeText.substring(
                lastEnd
            )
        );

        return container;
    }

    private void addNormalText(
        VBox container,
        String text
    ) {

        if (
            text == null ||
            text.trim().isEmpty()
        ) {

            return;
        }

        for (
            String line :
            text.split("\\R")
        ) {

            if (
                line.trim().isEmpty()
            ) {

                continue;
            }

            Label label =
                new Label(
                    formatInlineMarkdown(
                        line.trim()
                    )
                );

            label.setWrapText(
                true
            );

            label.setMaxWidth(
                Double.MAX_VALUE
            );

            label.setStyle(
                "-fx-text-fill: " +
                Theme.text() + ";" +
                "-fx-font-size: 13px;"
            );

            container
                .getChildren()
                .add(
                    label
                );
        }
    }

    private String formatInlineMarkdown(
        String text
    ) {

        return text
            .replaceAll(
                "\\*\\*(.*?)\\*\\*",
                "$1"
            )
            .replaceAll(
                "`([^`]+)`",
                "$1"
            );
    }

    // =========================================================
    // CODE BLOCK
    // =========================================================

    private void addCodeBlock(
        VBox container,
        String language,
        String code
    ) {

        VBox codeBox =
            new VBox();

        codeBox.setSpacing(
            0
        );

        codeBox.setPadding(
            new Insets(12)
        );

        codeBox.setStyle(
            "-fx-background-color: #0A0C0F;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " +
            Theme.border() + ";" +
            "-fx-border-radius: 10;"
        );

        HBox header =
            new HBox();

        header.setAlignment(
            Pos.CENTER_LEFT
        );

        Label languageLabel =
            new Label(
                language == null ||
                language.isBlank()
                    ? "code"
                    : language
            );

        languageLabel.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );

        Button copyButton =
            new Button(
                "Copy"
            );

        copyButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 11px;"
        );

        copyButton.setOnAction(
            event -> {

                Clipboard clipboard =
                    Clipboard.getSystemClipboard();

                ClipboardContent content =
                    new ClipboardContent();

                content.putString(
                    code.trim()
                );

                clipboard.setContent(
                    content
                );

                copyButton.setText(
                    "Copied"
                );
            }
        );

        HBox.setHgrow(
            languageLabel,
            Priority.ALWAYS
        );

        header.getChildren().addAll(
            languageLabel,
            copyButton
        );

        Label codeLabel =
            new Label(
                code.trim()
            );

        codeLabel.setWrapText(
            false
        );

        codeLabel.setStyle(
            "-fx-text-fill: #E6E8EC;" +
            "-fx-font-family: 'Consolas';" +
            "-fx-font-size: 12px;"
        );

        ScrollPane codeScroll =
            new ScrollPane(
                codeLabel
            );

        codeScroll.setFitToHeight(
            true
        );

        codeScroll.setFitToWidth(
            false
        );

        codeScroll.setStyle(
            "-fx-background-color: transparent;"
        );

        codeBox.getChildren().addAll(
            header,
            codeScroll
        );

        container
            .getChildren()
            .add(
                codeBox
            );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void addSimpleMessage(
        String sender,
        String text
    ) {

        messages
            .getChildren()
            .add(
                createBubble(
                    sender,
                    text
                )
            );

        scrollToBottom();
    }

    private void scrollToBottom() {

        Platform.runLater(() -> {

            if (
                messages.getParent()
                    instanceof ScrollPane scrollPane
            ) {

                scrollPane.setVvalue(
                    1.0
                );
            }
        });
    }
}