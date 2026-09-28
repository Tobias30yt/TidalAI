package com.tidalai.views;

import com.tidalai.AppState;
import com.tidalai.Theme;
import com.tidalai.chat.ChatManager;
import com.tidalai.chat.ChatMessage;
import com.tidalai.chat.ChatSession;
import com.tidalai.models.AIModel;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatView extends BorderPane {

    private final ListView<ChatSession> chatList =
        new ListView<>();

    private final VBox messages =
        new VBox(14);

    private final TextArea input =
        new TextArea();

    private final ComboBox<String> modelSelector =
        new ComboBox<>();

    private final Label titleLabel =
        new Label();

    private final Label statusLabel =
        new Label();

    private final Button sendButton =
        new Button(
            "Send"
        );

    private final Button deleteButton =
        new Button(
            "Delete"
        );

    private ChatSession selectedSession;

    public ChatView() {

        createSidebar();

        createMainArea();

        loadModels();

        AppState.CHATS
            .getChats()
            .addListener(
                (ListChangeListener<ChatSession>)
                    change -> {

                        chatList.refresh();
                    }
            );

        if (
            !AppState.CHATS
                .getChats()
                .isEmpty()
        ) {

            chatList
                .getSelectionModel()
                .selectFirst();
        }
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    private void createSidebar() {

        VBox sidebar =
            new VBox(
                12
            );

        sidebar.setPrefWidth(
            280
        );

        sidebar.setPadding(
            new Insets(18)
        );

        sidebar.setStyle(
            Theme.globalStyle() +
            "-fx-background-color: " +
            Theme.sidebar() +
            ";"
        );

        Label title =
            new Label(
                "Chats"
            );

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;"
        );

        Button newChat =
            new Button(
                "+ New Chat"
            );

        newChat.setMaxWidth(
            Double.MAX_VALUE
        );

        newChat.setStyle(
            "-fx-background-color: " +
            Theme.accent() +
            ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 11 16;" +
            "-fx-font-weight: bold;"
        );

        newChat.setOnAction(
            event ->
                createNewChat()
        );

        chatList.setItems(
            AppState.CHATS
                .getChats()
        );

        chatList.setCellFactory(
            listView ->
                new ChatListCell()
        );

        chatList.setStyle(
            Theme.globalStyle() +
            "-fx-background-color: " +
            Theme.sidebar() +
            ";" +
            "-fx-control-inner-background: " +
            Theme.sidebar() +
            ";" +
            "-fx-border-color: transparent;"
        );

        chatList
            .getSelectionModel()
            .selectedItemProperty()
            .addListener(
                (observable, oldValue, newValue) -> {

                    if (
                        newValue != null
                    ) {

                        selectedSession =
                            newValue;

                        renderSelectedChat();
                    }
                }
            );

        VBox.setVgrow(
            chatList,
            Priority.ALWAYS
        );

        sidebar.getChildren().addAll(
            title,
            newChat,
            chatList
        );

        setLeft(
            sidebar
        );
    }

    // =========================================================
    // MAIN
    // =========================================================

    private void createMainArea() {

        BorderPane main =
            new BorderPane();

        main.setStyle(
            Theme.globalStyle() +
            "-fx-background-color: " +
            Theme.background() +
            ";"
        );

        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------

        HBox header =
            new HBox(
                12
            );

        header.setAlignment(
            Pos.CENTER_LEFT
        );

        titleLabel.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;"
        );

        HBox.setHgrow(
            titleLabel,
            Priority.ALWAYS
        );

        modelSelector.setPrefWidth(
            260
        );

        styleModelSelector();

        deleteButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " +
            Theme.error() +
            ";" +
            "-fx-border-color: " +
            Theme.border() +
            ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 9 14;"
        );

        deleteButton.setOnAction(
            event ->
                deleteSelectedChat()
        );

        header.getChildren().addAll(
            titleLabel,
            modelSelector,
            deleteButton
        );

        // -----------------------------------------------------
        // MESSAGES
        // -----------------------------------------------------

        messages.setPadding(
            new Insets(4)
        );

        ScrollPane messageScroll =
            new ScrollPane(
                messages
            );

        messageScroll.setFitToWidth(
            true
        );

        messageScroll.setStyle(
            Theme.globalStyle() +
            "-fx-background-color: transparent;" +
            "-fx-background: transparent;"
        );

        // -----------------------------------------------------
        // INPUT
        // -----------------------------------------------------

        input.setPromptText(
            "Message the local AI..."
        );

        input.setWrapText(
            true
        );

        input.setPrefRowCount(
            3
        );

        styleTextArea();

        sendButton.setStyle(
            "-fx-background-color: " +
            Theme.accent() +
            ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 12 22;" +
            "-fx-font-weight: bold;"
        );

        sendButton.setOnAction(
            event ->
                sendMessage()
        );

        statusLabel.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 11px;"
        );

        VBox inputArea =
            new VBox(
                8
            );

        HBox inputRow =
            new HBox(
                10,
                input,
                sendButton
            );

        inputRow.setAlignment(
            Pos.BOTTOM_CENTER
        );

        HBox.setHgrow(
            input,
            Priority.ALWAYS
        );

        inputArea.getChildren().addAll(
            statusLabel,
            inputRow
        );

        VBox.setVgrow(
            messageScroll,
            Priority.ALWAYS
        );

        main.setTop(
            header
        );

        main.setCenter(
            messageScroll
        );

        main.setBottom(
            inputArea
        );

        BorderPane.setMargin(
            header,
            new Insets(
                24,
                24,
                12,
                24
            )
        );

        BorderPane.setMargin(
            messageScroll,
            new Insets(
                0,
                24,
                12,
                24
            )
        );

        BorderPane.setMargin(
            inputArea,
            new Insets(
                0,
                24,
                24,
                24
            )
        );

        setCenter(
            main
        );

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
    // CHAT MANAGEMENT
    // =========================================================

    private void createNewChat() {

        ChatSession chat =
            AppState.CHATS.createChat(
                "New Chat",
                AppState.CONFIG
                    .getDefaultChatModel()
            );

        chatList
            .getSelectionModel()
            .select(
                chat
            );

        input.clear();
    }

    private void deleteSelectedChat() {

        if (
            selectedSession == null
        ) {

            return;
        }

        int index =
            chatList
                .getSelectionModel()
                .getSelectedIndex();

        AppState.CHATS.deleteChat(
            selectedSession
        );

        selectedSession =
            null;

        if (
            !AppState.CHATS
                .getChats()
                .isEmpty()
        ) {

            int newIndex =
                Math.max(
                    0,
                    Math.min(
                        index,
                        AppState.CHATS
                            .getChats()
                            .size() - 1
                    )
                );

            chatList
                .getSelectionModel()
                .select(
                    newIndex
                );
        }
    }

    private void renderSelectedChat() {

        if (
            selectedSession == null
        ) {

            return;
        }

        titleLabel.setText(
            selectedSession.getTitle()
        );

        String model =
            selectedSession.getModelId();

        if (
            model != null &&
            modelSelector
                .getItems()
                .contains(
                    model
                )
        ) {

            modelSelector
                .getSelectionModel()
                .select(
                    model
                );

        } else if (
            !modelSelector
                .getItems()
                .isEmpty()
        ) {

            String defaultModel =
                AppState.CONFIG
                    .getDefaultChatModel();

            if (
                defaultModel != null &&
                modelSelector
                    .getItems()
                    .contains(
                        defaultModel
                    )
            ) {

                selectedSession.setModelId(
                    defaultModel
                );

                modelSelector
                    .getSelectionModel()
                    .select(
                        defaultModel
                    );

            } else {

                String first =
                    modelSelector
                        .getItems()
                        .get(0);

                selectedSession.setModelId(
                    first
                );

                modelSelector
                    .getSelectionModel()
                    .select(
                        first
                    );
            }
        }

        statusLabel.setText(
            selectedSession.getModelId() == null
                ? "Select a model"
                : "Model: " +
                  selectedSession.getModelId()
        );

        renderConversation();
    }

    // =========================================================
    // MODELS
    // =========================================================

    private void loadModels() {

        statusLabel.setText(
            "Loading LM Studio models..."
        );

        javafx.concurrent.Task<List<AIModel>> task =
            new javafx.concurrent.Task<>() {

                @Override
                protected List<AIModel> call() {

                    return AppState.PROVIDER
                        .getModels();
                }
            };

        task.setOnSucceeded(
            event -> {

                modelSelector
                    .getItems()
                    .clear();

                for (
                    AIModel model :
                    task.getValue()
                ) {

                    modelSelector
                        .getItems()
                        .add(
                            model.getId()
                        );
                }

                if (
                    modelSelector
                        .getItems()
                        .isEmpty()
                ) {

                    statusLabel.setText(
                        "No models available in LM Studio."
                    );

                    return;
                }

                if (
                    selectedSession != null
                ) {

                    renderSelectedChat();

                } else {

                    statusLabel.setText(
                        modelSelector
                            .getItems()
                            .size() +
                        " local model(s) available"
                    );
                }
            }
        );

        task.setOnFailed(
            event -> {

                statusLabel.setText(
                    "Could not connect to LM Studio."
                );
            }
        );

        Thread thread =
            new Thread(
                task,
                "tidalai-chat-model-loader"
            );

        thread.setDaemon(
            true
        );

        thread.start();
    }

    private void styleModelSelector() {

        modelSelector.setStyle(
            Theme.globalStyle() +
            "-fx-background-color: " +
            Theme.cardColor() +
            ";" +
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-border-color: " +
            Theme.border() +
            ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;"
        );

        modelSelector.setOnAction(
            event -> {

                if (
                    selectedSession == null
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

                    return;
                }

                selectedSession.setModelId(
                    model
                );

                statusLabel.setText(
                    "Model: " +
                    model
                );

                AppState.saveAll();
            }
        );
    }

    // =========================================================
    // SEND MESSAGE
    // =========================================================

    private void sendMessage() {

        if (
            selectedSession == null
        ) {

            return;
        }

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
            model == null ||
            model.isBlank()
        ) {

            statusLabel.setText(
                "Please select a model first."
            );

            return;
        }

        if (
            selectedSession.isUntitled()
        ) {

            selectedSession.setTitle(
                createTitle(
                    prompt
                )
            );

            chatList.refresh();
        }

        selectedSession.setModelId(
            model
        );

        selectedSession
            .getConversation()
            .addUserMessage(
                prompt
            );

        AppState.saveAll();

        addMessageBubble(
            "You",
            prompt
        );

        input.clear();

        sendButton.setDisable(
            true
        );

        modelSelector.setDisable(
            true
        );

        deleteButton.setDisable(
            true
        );

        statusLabel.setText(
            "TidalAI is thinking..."
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
            selectedSession
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

                if (
                    response == null ||
                    response.isBlank()
                ) {

                    response =
                        "No response received.";
                }

                if (
                    selectedSession != null
                ) {

                    selectedSession
                        .getConversation()
                        .addAssistantMessage(
                            response
                        );

                    AppState.saveAll();
                }

                updateBubble(
                    thinkingBubble,
                    "TidalAI",
                    response
                );

                sendButton.setDisable(
                    false
                );

                modelSelector.setDisable(
                    false
                );

                deleteButton.setDisable(
                    false
                );

                statusLabel.setText(
                    "Model: " +
                    model
                );
            }
        );

        requestTask.setOnFailed(
            event -> {

                Throwable exception =
                    requestTask.getException();

                String message =
                    exception == null
                        ? "Unknown error."
                        : exception.getMessage();

                updateBubble(
                    thinkingBubble,
                    "TidalAI",
                    "Error: " +
                    message
                );

                sendButton.setDisable(
                    false
                );

                modelSelector.setDisable(
                    false
                );

                deleteButton.setDisable(
                    false
                );

                statusLabel.setText(
                    "Request failed."
                );
            }
        );

        Thread thread =
            new Thread(
                requestTask,
                "tidalai-chat-request"
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

        if (
            selectedSession == null
        ) {

            return;
        }

        List<ChatMessage> conversation =
            selectedSession
                .getConversation()
                .getMessages();

        if (
            conversation.isEmpty()
        ) {

            VBox welcome =
                new VBox(
                    8
                );

            welcome.setAlignment(
                Pos.CENTER
            );

            Label title =
                new Label(
                    "Start a conversation"
                );

            title.setStyle(
                "-fx-text-fill: " +
                Theme.text() +
                ";" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
            );

            Label description =
                new Label(
                    "Choose a local model and send your first message."
                );

            description.setStyle(
                "-fx-text-fill: " +
                Theme.muted() +
                ";" +
                "-fx-font-size: 12px;"
            );

            welcome
                .getChildren()
                .addAll(
                    title,
                    description
                );

            messages
                .getChildren()
                .add(
                    welcome
                );

            return;
        }

        for (
            ChatMessage message :
            conversation
        ) {

            String sender =
                switch (
                    message.role()
                ) {

                    case "user" ->
                        "You";

                    case "assistant" ->
                        "TidalAI";

                    default ->
                        "System";
                };

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

    private void addMessageBubble(
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
    }

    private VBox createBubble(
        String sender,
        String text
    ) {

        VBox bubble =
            new VBox(
                8
            );

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
            (
                "You".equals(
                    sender
                )
                    ? Theme.text()
                    : Theme.accent()
            ) +
            ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );

        bubble
            .getChildren()
            .addAll(
                senderLabel,
                renderMarkdown(
                    text
                )
            );

        return bubble;
    }

    private void updateBubble(
        VBox bubble,
        String sender,
        String text
    ) {

        Platform.runLater(
            () -> {

                bubble
                    .getChildren()
                    .clear();

                Label senderLabel =
                    new Label(
                        sender
                    );

                senderLabel.setStyle(
                    "-fx-text-fill: " +
                    Theme.accent() +
                    ";" +
                    "-fx-font-size: 12px;" +
                    "-fx-font-weight: bold;"
                );

                bubble
                    .getChildren()
                    .addAll(
                        senderLabel,
                        renderMarkdown(
                            text
                        )
                    );
            }
        );
    }

    // =========================================================
    // MARKDOWN
    // =========================================================

    private VBox renderMarkdown(
        String text
    ) {

        VBox container =
            new VBox(
                8
            );

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
                Theme.text() +
                ";" +
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
            "-fx-background-color: " +
            (
                Theme.isDarkMode()
                    ? "#0A0C0F"
                    : "#EEF0F4"
            ) +
            ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " +
            Theme.border() +
            ";" +
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
            Theme.muted() +
            ";" +
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
            Theme.muted() +
            ";" +
            "-fx-font-size: 11px;"
        );

        copyButton.setOnAction(
            event -> {

                Clipboard clipboard =
                    Clipboard
                        .getSystemClipboard();

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

        header
            .getChildren()
            .addAll(
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
            "-fx-text-fill: " +
            (
                Theme.isDarkMode()
                    ? "#E6E8EC"
                    : "#20242A"
            ) +
            ";" +
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
            Theme.globalStyle() +
            "-fx-background-color: transparent;"
        );

        codeBox
            .getChildren()
            .addAll(
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
    // INPUT
    // =========================================================

    private void styleTextArea() {

        input.setStyle(
            Theme.globalStyle() +
            "-fx-control-inner-background: " +
            Theme.cardColor() +
            ";" +
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-prompt-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-background-color: " +
            Theme.cardColor() +
            ";" +
            "-fx-border-color: " +
            Theme.border() +
            ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;"
        );
    }

    // =========================================================
    // TITLE
    // =========================================================

    private String createTitle(
        String message
    ) {

        String clean =
            message
                .replaceAll(
                    "\\s+",
                    " "
                )
                .trim();

        if (
            clean.length() <= 32
        ) {

            return clean;
        }

        return
            clean.substring(
                0,
                32
            )
            .trim() +
            "...";
    }

    // =========================================================
    // LIST CELL
    // =========================================================

    private static class ChatListCell
        extends ListCell<ChatSession> {

        @Override
        protected void updateItem(
            ChatSession item,
            boolean empty
        ) {

            super.updateItem(
                item,
                empty
            );

            if (
                empty ||
                item == null
            ) {

                setText(
                    null
                );

                setStyle(
                    "-fx-background-color: transparent;"
                );

                return;
            }

            setText(
                item.getTitle()
            );

            applyStyle();
        }

        private void applyStyle() {

            if (
                isSelected()
            ) {

                setStyle(
                    "-fx-background-color: " +
                    Theme.accent() +
                    ";" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 10 12;"
                );

            } else {

                setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: " +
                    Theme.text() +
                    ";" +
                    "-fx-padding: 10 12;"
                );
            }
        }
    }
}
