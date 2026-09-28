package com.tidalai.views;

import com.tidalai.AppState;
import com.tidalai.Theme;
import com.tidalai.agents.AgentConfig;
import com.tidalai.agents.ApprovalMode;
import com.tidalai.models.AIModel;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class SettingsView extends VBox {

    private boolean loadingSettings =
        false;

    private final ComboBox<AgentConfig> agentConfigSelector =
        new ComboBox<>();

    private final ComboBox<String> defaultChatModelSelector =
        new ComboBox<>();

    private final ComboBox<ApprovalMode> approvalModeSelector =
        new ComboBox<>();

    private final ComboBox<String> qualitySelector =
        new ComboBox<>();

    private final ToggleButton approvalDialogsToggle =
        new ToggleButton();

    private final ToggleButton externalTerminalToggle =
        new ToggleButton();

    private final ToggleButton appearanceToggle =
        new ToggleButton();

    private final Label workspaceValue =
        new Label();

    private final Label backendStatus =
        new Label();

    private final Label themeValue =
        new Label();

    public SettingsView() {

        setSpacing(
            20
        );

        setPadding(
            new Insets(10)
        );

        setFillWidth(
            true
        );

        createView();

        loadingSettings =
            true;

        loadCurrentSettings();

        loadingSettings =
            false;
    }

    // =========================================================
    // VIEW
    // =========================================================

    private void createView() {

        Label title =
            new Label(
                "Settings"
            );

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;"
        );

        Label subtitle =
            new Label(
                "Configure TidalAI's local AI, agents, workspace, permissions and appearance."
            );

        subtitle.setWrapText(
            true
        );

        subtitle.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 13px;"
        );

        getChildren().addAll(
            title,
            subtitle,
            createWorkspaceCard(),
            createAgentCard(),
            createChatModelCard(),
            createSecurityCard(),
            createAppearanceCard(),
            createBackendCard()
        );
    }

    // =========================================================
    // WORKSPACE
    // =========================================================

    private VBox createWorkspaceCard() {

        VBox card =
            createCard();

        Label title =
            createSectionTitle(
                "Workspace"
            );

        Label description =
            createDescription(
                "Choose the directory in which TidalAI agents are allowed to work."
            );

        HBox row =
            new HBox(
                12
            );

        row.setAlignment(
            Pos.CENTER_LEFT
        );

        workspaceValue.setWrapText(
            true
        );

        workspaceValue.setMaxWidth(
            Double.MAX_VALUE
        );

        workspaceValue.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 13px;"
        );

        HBox.setHgrow(
            workspaceValue,
            Priority.ALWAYS
        );

        Button chooseButton =
            createSecondaryButton(
                "Choose Folder"
            );

        chooseButton.setOnAction(
            event ->
                chooseWorkspace()
        );

        row.getChildren().addAll(
            workspaceValue,
            chooseButton
        );

        card.getChildren().addAll(
            title,
            description,
            row
        );

        return card;
    }

    private void chooseWorkspace() {

        DirectoryChooser chooser =
            new DirectoryChooser();

        chooser.setTitle(
            "Choose TidalAI Workspace"
        );

        Window window =
            getScene() == null
                ? null
                : getScene().getWindow();

        File selected =
            chooser.showDialog(
                window
            );

        if (
            selected == null
        ) {
            return;
        }

        Path root =
            selected.toPath();

        AppState.SANDBOX.setRoot(
            root
        );

        updateWorkspaceLabel();

        AppState.saveConfig();
    }

    private void updateWorkspaceLabel() {

        if (
            AppState.SANDBOX.isConfigured()
        ) {

            workspaceValue.setText(
                AppState.SANDBOX
                    .getRoot()
                    .toAbsolutePath()
                    .toString()
            );

        } else {

            workspaceValue.setText(
                "No workspace configured"
            );
        }
    }

    // =========================================================
    // AGENT CONFIGURATION
    // =========================================================

    private VBox createAgentCard() {

        VBox card =
            createCard();

        Label title =
            createSectionTitle(
                "Agent Configuration"
            );

        Label description =
            createDescription(
                "Choose the overall agent configuration and limits."
            );

        agentConfigSelector
            .getItems()
            .setAll(
                AgentConfig.values()
            );

        agentConfigSelector.setMaxWidth(
            Double.MAX_VALUE
        );

        styleComboBox(
            agentConfigSelector
        );

        agentConfigSelector.setOnAction(
            event -> {

                if (
                    loadingSettings
                ) {
                    return;
                }

                applyAgentConfig();
            }
        );

        card.getChildren().addAll(
            title,
            description,
            agentConfigSelector
        );

        return card;
    }

    private void applyAgentConfig() {

        AgentConfig config =
            agentConfigSelector
                .getSelectionModel()
                .getSelectedItem();

        if (
            config == null
        ) {
            return;
        }

        AppState.AGENT_CONFIG =
            config;

        AppState.CONFIG.setAgentConfig(
            config.name()
        );

        AppState.saveConfig();
    }

    // =========================================================
    // CHAT MODEL
    // =========================================================

    private VBox createChatModelCard() {

        VBox card =
            createCard();

        Label title =
            createSectionTitle(
                "Chat Model"
            );

        Label description =
            createDescription(
                "Choose the default model used when a new normal Chat conversation is created."
            );

        HBox row =
            new HBox(
                10
            );

        defaultChatModelSelector.setMaxWidth(
            Double.MAX_VALUE
        );

        styleComboBox(
            defaultChatModelSelector
        );

        HBox.setHgrow(
            defaultChatModelSelector,
            Priority.ALWAYS
        );

        defaultChatModelSelector.setOnAction(
            event -> {

                if (
                    loadingSettings
                ) {
                    return;
                }

                applyDefaultChatModel();
            }
        );

        Button refreshButton =
            createSecondaryButton(
                "Refresh Models"
            );

        refreshButton.setOnAction(
            event ->
                loadDefaultChatModels()
        );

        row.getChildren().addAll(
            defaultChatModelSelector,
            refreshButton
        );

        card.getChildren().addAll(
            title,
            description,
            row
        );

        return card;
    }

    private void applyDefaultChatModel() {

        String model =
            defaultChatModelSelector
                .getSelectionModel()
                .getSelectedItem();

        if (
            model == null ||
            model.isBlank()
        ) {
            return;
        }

        AppState.CONFIG.setDefaultChatModel(
            model
        );

        AppState.saveConfig();
    }

    private void loadDefaultChatModels() {

        defaultChatModelSelector
            .setDisable(
                true
            );

        defaultChatModelSelector
            .setPromptText(
                "Loading models..."
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

                defaultChatModelSelector
                    .setDisable(
                        false
                    );

                defaultChatModelSelector
                    .getItems()
                    .clear();

                List<AIModel> models =
                    task.getValue();

                for (
                    AIModel model :
                    models
                ) {

                    defaultChatModelSelector
                        .getItems()
                        .add(
                            model.getId()
                        );
                }

                String saved =
                    AppState.CONFIG
                        .getDefaultChatModel();

                if (
                    saved != null &&
                    !saved.isBlank() &&
                    defaultChatModelSelector
                        .getItems()
                        .contains(
                            saved
                        )
                ) {

                    defaultChatModelSelector
                        .getSelectionModel()
                        .select(
                            saved
                        );

                } else if (
                    !defaultChatModelSelector
                        .getItems()
                        .isEmpty()
                ) {

                    String first =
                        defaultChatModelSelector
                            .getItems()
                            .get(0);

                    defaultChatModelSelector
                        .getSelectionModel()
                        .select(
                            first
                        );

                    AppState.CONFIG
                        .setDefaultChatModel(
                            first
                        );

                    AppState.saveConfig();

                } else {

                    defaultChatModelSelector
                        .setPromptText(
                            "No models available"
                        );
                }
            }
        );

        task.setOnFailed(
            event -> {

                defaultChatModelSelector
                    .setDisable(
                        false
                    );

                defaultChatModelSelector
                    .setPromptText(
                        "Could not load models"
                    );
            }
        );

        Thread thread =
            new Thread(
                task,
                "tidalai-default-chat-model-loader"
            );

        thread.setDaemon(
            true
        );

        thread.start();
    }

    // =========================================================
    // SECURITY
    // =========================================================

    private VBox createSecurityCard() {

        VBox card =
            createCard();

        Label title =
            createSectionTitle(
                "Agent Permissions"
            );

        Label description =
            createDescription(
                "Control approvals and terminal behavior for agent tool usage."
            );

        HBox approvalModeRow =
            createSettingRow(
                "Approval mode",
                "Manual asks before protected operations. Auto uses the configured command policy."
            );

        approvalModeSelector
            .getItems()
            .setAll(
                ApprovalMode.values()
            );

        approvalModeSelector.setPrefWidth(
            150
        );

        styleComboBox(
            approvalModeSelector
        );

        approvalModeSelector.setOnAction(
            event -> {

                if (
                    loadingSettings
                ) {
                    return;
                }

                applyApprovalMode();
            }
        );

        approvalModeRow
            .getChildren()
            .add(
                approvalModeSelector
            );

        HBox dialogsRow =
            createSettingRow(
                "Approval dialogs",
                "Show confirmation dialogs even when Auto mode is active."
            );

        configureToggle(
            approvalDialogsToggle,
            "Show",
            "Hidden"
        );

        approvalDialogsToggle.setOnAction(
            event -> {

                if (
                    loadingSettings
                ) {
                    return;
                }

                applyApprovalDialogs();
            }
        );

        dialogsRow
            .getChildren()
            .add(
                approvalDialogsToggle
            );

        HBox terminalRow =
            createSettingRow(
                "External terminal",
                "Open allowed agent commands in a visible PowerShell terminal."
            );

        configureToggle(
            externalTerminalToggle,
            "Enabled",
            "Disabled"
        );

        externalTerminalToggle.setOnAction(
            event -> {

                if (
                    loadingSettings
                ) {
                    return;
                }

                applyExternalTerminal();
            }
        );

        terminalRow
            .getChildren()
            .add(
                externalTerminalToggle
            );

        card.getChildren().addAll(
            title,
            description,
            new Separator(),
            approvalModeRow,
            dialogsRow,
            terminalRow
        );

        return card;
    }

    private void applyApprovalMode() {

        ApprovalMode mode =
            approvalModeSelector
                .getSelectionModel()
                .getSelectedItem();

        if (
            mode == null
        ) {
            return;
        }

        AppState.SANDBOX.setApprovalMode(
            mode
        );

        AppState.saveConfig();
    }

    private void applyApprovalDialogs() {

        AppState.SANDBOX
            .setShowApprovalDialogsInAuto(
                approvalDialogsToggle
                    .isSelected()
            );

        AppState.saveConfig();
    }

    private void applyExternalTerminal() {

        AppState.SANDBOX
            .setExternalTerminalEnabled(
                externalTerminalToggle
                    .isSelected()
            );

        AppState.saveConfig();
    }

    // =========================================================
    // APPEARANCE
    // =========================================================

    private VBox createAppearanceCard() {

        VBox card =
            createCard();

        Label title =
            createSectionTitle(
                "Appearance"
            );

        Label description =
            createDescription(
                "Switch the entire TidalAI interface between dark and light mode."
            );

        HBox themeRow =
            createSettingRow(
                "Theme",
                "The complete interface is rebuilt after switching."
            );

        configureToggle(
            appearanceToggle,
            "Dark",
            "Light"
        );

        appearanceToggle.setOnAction(
            event -> {

                if (
                    loadingSettings
                ) {
                    return;
                }

                applyTheme();
            }
        );

        themeValue.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 12px;"
        );

        themeRow
            .getChildren()
            .addAll(
                appearanceToggle,
                themeValue
            );

        card.getChildren().addAll(
            title,
            description,
            themeRow
        );

        return card;
    }

    private void applyTheme() {

        boolean dark =
            appearanceToggle.isSelected();

        Theme.setDarkMode(
            dark
        );

        AppState.saveConfig();

        if (
            getScene() != null &&
            getScene().getRoot()
                instanceof com.tidalai.Dashboard dashboard
        ) {

            dashboard.refreshTheme();
        }
    }

    // =========================================================
    // BACKEND
    // =========================================================

    private VBox createBackendCard() {

        VBox card =
            createCard();

        Label title =
            createSectionTitle(
                "AI Backend"
            );

        Label description =
            createDescription(
                "Check the connection to your local LM Studio server."
            );

        HBox statusRow =
            new HBox(
                12
            );

        statusRow.setAlignment(
            Pos.CENTER_LEFT
        );

        Label statusTitle =
            new Label(
                "LM Studio"
            );

        statusTitle.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );

        backendStatus.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );

        Button refreshButton =
            createSecondaryButton(
                "Refresh"
            );

        refreshButton.setOnAction(
            event ->
                refreshBackendStatus()
        );

        HBox.setHgrow(
            backendStatus,
            Priority.ALWAYS
        );

        statusRow.getChildren().addAll(
            statusTitle,
            backendStatus,
            refreshButton
        );

        VBox qualityBox =
            new VBox(
                8
            );

        Label qualityLabel =
            new Label(
                "Response quality"
            );

        qualityLabel.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );

        qualitySelector
            .getItems()
            .setAll(
                "Fast",
                "Balanced",
                "High Quality"
            );

        qualitySelector.setMaxWidth(
            Double.MAX_VALUE
        );

        styleComboBox(
            qualitySelector
        );

        qualitySelector.setOnAction(
            event -> {

                if (
                    loadingSettings
                ) {
                    return;
                }

                String quality =
                    qualitySelector
                        .getSelectionModel()
                        .getSelectedItem();

                if (
                    quality == null
                ) {
                    return;
                }

                AppState.CONFIG
                    .setResponseQuality(
                        quality
                    );

                AppState.saveConfig();
            }
        );

        qualityBox
            .getChildren()
            .addAll(
                qualityLabel,
                qualitySelector
            );

        card.getChildren().addAll(
            title,
            description,
            statusRow,
            qualityBox
        );

        return card;
    }

    private void refreshBackendStatus() {

        boolean connected =
            AppState.PROVIDER
                .isConnected();

        if (
            connected
        ) {

            backendStatus.setText(
                "Connected"
            );

            backendStatus.setStyle(
                "-fx-text-fill: " +
                Theme.success() +
                ";" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
            );

        } else {

            backendStatus.setText(
                "Not connected"
            );

            backendStatus.setStyle(
                "-fx-text-fill: " +
                Theme.error() +
                ";" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;"
            );
        }
    }

    // =========================================================
    // LOAD SETTINGS
    // =========================================================

    private void loadCurrentSettings() {

        agentConfigSelector
            .getSelectionModel()
            .select(
                AppState.AGENT_CONFIG
            );

        try {

            approvalModeSelector
                .getSelectionModel()
                .select(
                    AppState.SANDBOX
                        .getApprovalMode()
                );

        } catch (
            Exception e
        ) {

            approvalModeSelector
                .getSelectionModel()
                .select(
                    ApprovalMode.MANUAL
                );
        }

        approvalDialogsToggle.setSelected(
            AppState.SANDBOX
                .isShowApprovalDialogsInAuto()
        );

        externalTerminalToggle.setSelected(
            AppState.SANDBOX
                .isExternalTerminalEnabled()
        );

        appearanceToggle.setSelected(
            Theme.isDarkMode()
        );

        String quality =
            AppState.CONFIG
                .getResponseQuality();

        if (
            qualitySelector
                .getItems()
                .contains(
                    quality
                )
        ) {

            qualitySelector
                .getSelectionModel()
                .select(
                    quality
                );

        } else {

            qualitySelector
                .getSelectionModel()
                .select(
                    "Balanced"
                );
        }

        updateWorkspaceLabel();

        updateToggleTexts();

        themeValue.setText(
            Theme.isDarkMode()
                ? "Dark mode"
                : "Light mode"
        );

        refreshBackendStatus();

        loadDefaultChatModels();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private VBox createCard() {

        VBox card =
            new VBox(
                12
            );

        card.setPadding(
            new Insets(18)
        );

        card.setMaxWidth(
            Double.MAX_VALUE
        );

        card.setStyle(
            Theme.cardStyle()
        );

        return card;
    }

    private Label createSectionTitle(
        String text
    ) {

        Label label =
            new Label(
                text
            );

        label.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;"
        );

        return label;
    }

    private Label createDescription(
        String text
    ) {

        Label label =
            new Label(
                text
            );

        label.setWrapText(
            true
        );

        label.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 12px;"
        );

        return label;
    }

    private HBox createSettingRow(
        String title,
        String description
    ) {

        VBox textBox =
            new VBox(
                4
            );

        Label titleLabel =
            new Label(
                title
            );

        titleLabel.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );

        Label descriptionLabel =
            new Label(
                description
            );

        descriptionLabel.setWrapText(
            true
        );

        descriptionLabel.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 11px;"
        );

        textBox
            .getChildren()
            .addAll(
                titleLabel,
                descriptionLabel
            );

        HBox row =
            new HBox(
                15
            );

        row.setAlignment(
            Pos.CENTER_LEFT
        );

        HBox.setHgrow(
            textBox,
            Priority.ALWAYS
        );

        row.getChildren()
            .add(
                textBox
            );

        return row;
    }

    private Button createSecondaryButton(
        String text
    ) {

        Button button =
            new Button(
                text
            );

        button.setStyle(
            "-fx-background-color: " +
            Theme.cardColor() +
            ";" +
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-border-color: " +
            Theme.border() +
            ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 16;"
        );

        return button;
    }

    private void styleComboBox(
        ComboBox<?> comboBox
    ) {

        comboBox.setStyle(
            "-fx-background-color: " +
            Theme.cardColor() +
            ";" +
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-border-color: " +
            Theme.border() +
            ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;"
        );
    }

    private void configureToggle(
        ToggleButton toggle,
        String selectedText,
        String unselectedText
    ) {

        toggle.setPrefWidth(
            130
        );

        toggle.selectedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {

                    toggle.setText(
                        newValue
                            ? selectedText
                            : unselectedText
                    );

                    toggle.setStyle(
                        getToggleStyle(
                            toggle
                        )
                    );
                }
            );

        toggle.setText(
            toggle.isSelected()
                ? selectedText
                : unselectedText
        );

        toggle.setStyle(
            getToggleStyle(
                toggle
            )
        );
    }

    private String getToggleStyle(
        ToggleButton toggle
    ) {

        if (
            toggle.isSelected()
        ) {

            return
                "-fx-background-color: " +
                Theme.accent() +
                ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-padding: 10 16;" +
                "-fx-font-weight: bold;";
        }

        return
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
            "-fx-border-radius: 10;" +
            "-fx-padding: 10 16;";
    }

    private void updateToggleTexts() {

        approvalDialogsToggle.setText(
            approvalDialogsToggle
                .isSelected()
                ? "Show"
                : "Hidden"
        );

        externalTerminalToggle.setText(
            externalTerminalToggle
                .isSelected()
                ? "Enabled"
                : "Disabled"
        );

        appearanceToggle.setText(
            appearanceToggle
                .isSelected()
                ? "Dark"
                : "Light"
        );

        approvalDialogsToggle.setStyle(
            getToggleStyle(
                approvalDialogsToggle
            )
        );

        externalTerminalToggle.setStyle(
            getToggleStyle(
                externalTerminalToggle
            )
        );

        appearanceToggle.setStyle(
            getToggleStyle(
                appearanceToggle
            )
        );
    }
}