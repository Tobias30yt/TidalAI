package com.tidalai.views;

import com.tidalai.AppState;
import com.tidalai.Theme;
import com.tidalai.agents.Agent;
import com.tidalai.agents.AgentConfig;
import com.tidalai.agents.AgentManager;
import com.tidalai.agents.AgentRole;
import com.tidalai.models.AIModel;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class AgentsView extends VBox {

    private final VBox agentList =
        new VBox(12);

    private final TextField agentNameField =
        new TextField();

    private final ComboBox<AgentRole> roleSelector =
        new ComboBox<>();

    private final ComboBox<AgentConfig> configSelector =
        new ComboBox<>();

    private final Label countLabel =
        new Label();

    private final Label modelStatus =
        new Label();

    private final List<AIModel> availableModels =
        new ArrayList<>();

    public AgentsView() {

        setSpacing(20);
        setPadding(new Insets(10));
        setFillWidth(true);

        createHeader();
        createConfigurationCard();
        createAgentArea();

        loadModels();

        refreshAgents();
    }

    // =========================================================
    // HEADER
    // =========================================================

    private void createHeader() {

        Label title =
            new Label("Agents");

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;"
        );

        Label subtitle =
            new Label(
                "Configure your agent team and assign a specific LM Studio model to each agent."
            );

        subtitle.setWrapText(true);

        subtitle.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 13px;"
        );

        HBox statusRow =
            new HBox(12);

        statusRow.setAlignment(
            Pos.CENTER_LEFT
        );

        countLabel.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 12px;"
        );

        modelStatus.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 12px;"
        );

        configSelector
            .getItems()
            .setAll(
                AgentConfig.values()
            );

        configSelector
            .getSelectionModel()
            .select(
                AppState.AGENT_CONFIG
            );

        styleComboBox(
            configSelector
        );

        configSelector.setPrefWidth(
            150
        );

        configSelector.setOnAction(
            event -> {

                AgentConfig config =
                    configSelector
                        .getSelectionModel()
                        .getSelectedItem();

                if (config != null) {

                    AppState.AGENT_CONFIG =
                        config;

                    AppState.saveConfig();
                }
            }
        );

        HBox.setHgrow(
            countLabel,
            Priority.ALWAYS
        );

        statusRow.getChildren().addAll(
            countLabel,
            modelStatus,
            configSelector
        );

        getChildren().addAll(
            title,
            subtitle,
            statusRow
        );
    }

    // =========================================================
    // CREATE AGENT
    // =========================================================

    private void createConfigurationCard() {

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
                "Create Agent"
            );

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;"
        );

        Label description =
            new Label(
                "Create an additional registered agent."
            );

        description.setWrapText(true);

        description.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 12px;"
        );

        HBox row =
            new HBox(10);

        agentNameField.setPromptText(
            "Agent name"
        );

        styleTextField(
            agentNameField
        );

        roleSelector
            .getItems()
            .setAll(
                AgentRole.values()
            );

        roleSelector
            .getSelectionModel()
            .select(
                AgentRole.CODER
            );

        styleComboBox(
            roleSelector
        );

        roleSelector.setPrefWidth(
            170
        );

        Button createButton =
            new Button(
                "Create Agent"
            );

        createButton.setStyle(
            "-fx-background-color: " +
            Theme.accent() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 18;" +
            "-fx-font-weight: bold;"
        );

        createButton.setOnAction(
            event -> createAgent()
        );

        HBox.setHgrow(
            agentNameField,
            Priority.ALWAYS
        );

        row.getChildren().addAll(
            agentNameField,
            roleSelector,
            createButton
        );

        card.getChildren().addAll(
            title,
            description,
            row
        );

        getChildren().add(
            card
        );
    }

    private void createAgent() {

        String name =
            agentNameField
                .getText()
                .trim();

        AgentRole role =
            roleSelector
                .getSelectionModel()
                .getSelectedItem();

        if (
            name.isEmpty() ||
            role == null
        ) {

            return;
        }

        AgentConfig config =
            AppState.AGENT_CONFIG;

        if (
            AppState.AGENTS
                .getAgents()
                .size()
                >= config.getMaxAgents()
        ) {

            showLimitMessage();

            return;
        }

        AppState.AGENTS.createAgent(
            name,
            role
        );

        agentNameField.clear();

        AppState.saveConfig();

        refreshAgents();
    }

    // =========================================================
    // MODELS
    // =========================================================

    private void loadModels() {

        modelStatus.setText(
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

                availableModels.clear();

                availableModels.addAll(
                    task.getValue()
                );

                modelStatus.setText(
                    availableModels.size() +
                    (
                        availableModels.size() == 1
                            ? " LM Studio model"
                            : " LM Studio models"
                    )
                );

                refreshAgents();
            }
        );

        task.setOnFailed(
            event -> {

                availableModels.clear();

                modelStatus.setText(
                    "Could not load models"
                );

                refreshAgents();
            }
        );

        Thread thread =
            new Thread(
                task,
                "tidalai-agent-model-loader"
            );

        thread.setDaemon(true);
        thread.start();
    }

    // =========================================================
    // AGENT LIST
    // =========================================================

    private void createAgentArea() {

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
                "Agent Models"
            );

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;"
        );

        ScrollPane scrollPane =
            new ScrollPane(
                agentList
            );

        scrollPane.setFitToWidth(true);

        scrollPane.setPrefHeight(
            520
        );

        scrollPane.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background: transparent;"
        );

        agentList.setFillWidth(true);

        card.getChildren().addAll(
            title,
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

    private void refreshAgents() {

        agentList
            .getChildren()
            .clear();

        List<AgentRole> coreRoles =
            List.of(
                AgentRole.MAIN,
                AgentRole.REASONING,
                AgentRole.CODER,
                AgentRole.RESEARCHER,
                AgentRole.TESTER,
                AgentRole.REVIEWER
            );

        countLabel.setText(
            coreRoles.size() +
            " core agents + " +
            AppState.AGENTS
                .getAgents()
                .size() +
            " registered agents"
        );

        for (
            AgentRole role :
            coreRoles
        ) {

            agentList
                .getChildren()
                .add(
                    createCoreAgentCard(
                        role
                    )
                );
        }

        /*
         * Additional custom agents.
         */
        for (
            Agent agent :
            AppState.AGENTS
                .getAgents()
        ) {

            if (
                agent.getRole() == AgentRole.MAIN ||
                agent.getRole() == AgentRole.REASONING
            ) {
                continue;
            }

            agentList
                .getChildren()
                .add(
                    createCustomAgentCard(
                        agent
                    )
                );
        }
    }

    // =========================================================
    // CORE AGENT CARD
    // =========================================================

    private VBox createCoreAgentCard(
        AgentRole role
    ) {

        VBox card =
            new VBox(10);

        card.setPadding(
            new Insets(16)
        );

        card.setStyle(
            Theme.cardStyle()
        );

        HBox top =
            new HBox(12);

        top.setAlignment(
            Pos.CENTER_LEFT
        );

        Label name =
            new Label(
                AgentManager.getAgentDisplayName(
                    role
                )
            );

        name.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );

        Label roleLabel =
            new Label(
                role.name()
            );

        roleLabel.setStyle(
            "-fx-text-fill: " +
            Theme.accent() + ";" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;"
        );

        HBox.setHgrow(
            name,
            Priority.ALWAYS
        );

        top.getChildren().addAll(
            name,
            roleLabel
        );

        Label info =
            new Label(
                getRoleDescription(
                    role
                )
            );

        info.setWrapText(true);

        info.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 12px;"
        );

        ComboBox<String> modelSelector =
            createModelSelector();

        String assigned =
            AppState.AGENTS
                .getModelForRole(
                    role
                );

        applyModelsToSelector(
            modelSelector,
            assigned
        );

        modelSelector.setOnAction(
            event -> {

                String value =
                    modelSelector
                        .getSelectionModel()
                        .getSelectedItem();

                AppState.AGENTS
                    .setModelForRole(
                        role,
                        value
                    );

                AppState.saveConfig();
            }
        );

        HBox modelRow =
            new HBox(10);

        modelRow.setAlignment(
            Pos.CENTER_LEFT
        );

        Label modelLabel =
            new Label(
                "Model"
            );

        modelLabel.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );

        HBox.setHgrow(
            modelSelector,
            Priority.ALWAYS
        );

        modelRow.getChildren().addAll(
            modelLabel,
            modelSelector
        );

        card.getChildren().addAll(
            top,
            info,
            modelRow
        );

        return card;
    }

    // =========================================================
    // CUSTOM AGENT CARD
    // =========================================================

    private VBox createCustomAgentCard(
        Agent agent
    ) {

        VBox card =
            new VBox(10);

        card.setPadding(
            new Insets(16)
        );

        card.setStyle(
            Theme.cardStyle()
        );

        HBox top =
            new HBox(12);

        top.setAlignment(
            Pos.CENTER_LEFT
        );

        Label name =
            new Label(
                agent.getName()
            );

        name.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;"
        );

        Label role =
            new Label(
                agent.getRole().name()
            );

        role.setStyle(
            "-fx-text-fill: " +
            Theme.accent() + ";" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;"
        );

        HBox.setHgrow(
            name,
            Priority.ALWAYS
        );

        top.getChildren().addAll(
            name,
            role
        );

        ComboBox<String> selector =
            createModelSelector();

        String assigned =
            AppState.AGENTS
                .getModelForAgent(
                    agent.getId()
                );

        applyModelsToSelector(
            selector,
            assigned
        );

        selector.setOnAction(
            event -> {

                AppState.AGENTS
                    .setModelForAgent(
                        agent.getId(),
                        selector
                            .getSelectionModel()
                            .getSelectedItem()
                    );

                AppState.saveConfig();
            }
        );

        Label id =
            new Label(
                "ID: " +
                agent.getId()
            );

        id.setWrapText(true);

        id.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 10px;"
        );

        card.getChildren().addAll(
            top,
            selector,
            id
        );

        return card;
    }

    private ComboBox<String>
    createModelSelector() {

        ComboBox<String> selector =
            new ComboBox<>();

        selector.setMaxWidth(
            Double.MAX_VALUE
        );

        styleComboBox(
            selector
        );

        return selector;
    }

    private void applyModelsToSelector(
        ComboBox<String> selector,
        String assigned
    ) {

        selector.getItems().clear();

        selector.getItems().add(
            "Auto (first available)"
        );

        for (
            AIModel model :
            availableModels
        ) {

            selector
                .getItems()
                .add(
                    model.getId()
                );
        }

        if (
            assigned != null &&
            selector
                .getItems()
                .contains(
                    assigned
                )
        ) {

            selector
                .getSelectionModel()
                .select(
                    assigned
                );

        } else {

            selector
                .getSelectionModel()
                .selectFirst();
        }
    }

    // =========================================================
    // DESCRIPTIONS
    // =========================================================

    private String getRoleDescription(
        AgentRole role
    ) {

        return switch (role) {

            case MAIN ->
                "Receives the user's task and starts the agent workflow.";

            case REASONING ->
                "Breaks the task into a structured execution plan.";

            case CODER ->
                "Reads, creates and modifies code inside the workspace.";

            case RESEARCHER ->
                "Inspects the project and gathers technical information.";

            case TESTER ->
                "Runs tests/builds and reports concrete problems.";

            case REVIEWER ->
                "Reviews the implementation for correctness and obvious issues.";
        };
    }

    // =========================================================
    // LIMIT
    // =========================================================

    private void showLimitMessage() {

        Label message =
            new Label(
                "Maximum of " +
                AppState.AGENT_CONFIG
                    .getMaxAgents() +
                " additional registered agents reached."
            );

        message.setWrapText(true);

        message.setStyle(
            "-fx-text-fill: " +
            Theme.warning() + ";" +
            "-fx-font-size: 12px;"
        );

        agentList
            .getChildren()
            .add(
                0,
                message
            );
    }

    // =========================================================
    // STYLE HELPERS
    // =========================================================

    private void styleComboBox(
        ComboBox<?> comboBox
    ) {

        comboBox.setStyle(
            "-fx-background-color: " +
            Theme.cardColor() + ";" +
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-border-color: " +
            Theme.border() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;"
        );
    }

    private void styleTextField(
        TextField field
    ) {

        field.setStyle(
            "-fx-control-inner-background: " +
            Theme.cardColor() + ";" +
            "-fx-background-color: " +
            Theme.cardColor() + ";" +
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-prompt-text-fill: " +
            Theme.muted() + ";" +
            "-fx-border-color: " +
            Theme.border() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 10;"
        );
    }
}

