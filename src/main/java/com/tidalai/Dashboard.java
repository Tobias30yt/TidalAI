package com.tidalai;

import com.tidalai.monitor.SystemMonitorView;
import com.tidalai.tasks.Task;
import com.tidalai.views.AgentsView;
import com.tidalai.views.ChatView;
import com.tidalai.views.DashboardView;
import com.tidalai.views.ModelsView;
import com.tidalai.views.ProjectsView;
import com.tidalai.views.SettingsView;
import com.tidalai.views.TaskChatView;
import com.tidalai.views.TasksView;
import com.tidalai.views.WelcomeView;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class Dashboard extends BorderPane {

    private enum Page {
        START,
        DASHBOARD,
        CHAT,
        MODELS,
        AGENTS,
        TASKS,
        PROJECTS,
        SETTINGS,
        TASK_CHAT
    }

    private final VBox content =
        new VBox();

    private final List<NavigationButton> navigationButtons =
        new ArrayList<>();

    private SystemMonitorView systemMonitorView;

    private Page currentPage =
        Page.START;

    private Task currentTask;

    public Dashboard() {

        applyTheme();

        createSidebar();

        showStart();
    }

    // =========================================================
    // THEME
    // =========================================================

    public void refreshTheme() {

        /*
         * The old SystemMonitorView owns a listener.
         * Remove it before creating the new themed version.
         */
        if (
            systemMonitorView != null
        ) {

            systemMonitorView.dispose();

            systemMonitorView =
                null;
        }

        applyTheme();

        createSidebar();

        renderCurrentPage();
    }

    private void applyTheme() {

        setStyle(
            Theme.globalStyle() +
            "-fx-background-color: " +
            Theme.background() +
            ";"
        );
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    private void createSidebar() {

        navigationButtons.clear();

        VBox sidebar =
            new VBox(12);

        sidebar.setPadding(
            new Insets(20)
        );

        sidebar.setPrefWidth(
            270
        );

        sidebar.setStyle(
            Theme.globalStyle() +
            "-fx-background-color: " +
            Theme.sidebar() +
            ";"
        );

        // -----------------------------------------------------
        // BRAND
        // -----------------------------------------------------

        Label logo =
            new Label(
                "TidalAI"
            );

        logo.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;"
        );

        Label subtitle =
            new Label(
                "LOCAL AI WORKSPACE"
            );

        subtitle.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 10px;"
        );

        VBox brand =
            new VBox(
                4,
                logo,
                subtitle
            );

        sidebar.getChildren().add(
            brand
        );

        sidebar.getChildren().add(
            new Label("")
        );

        // -----------------------------------------------------
        // NAVIGATION
        // -----------------------------------------------------

        addNavigationButton(
            sidebar,
            "Start",
            this::showStart
        );

        addNavigationButton(
            sidebar,
            "Dashboard",
            this::showDashboard
        );

        addNavigationButton(
            sidebar,
            "Chat",
            this::showChat
        );

        addNavigationButton(
            sidebar,
            "Models",
            this::showModels
        );

        addNavigationButton(
            sidebar,
            "Agents",
            this::showAgents
        );

        addNavigationButton(
            sidebar,
            "Tasks",
            this::showTasks
        );

        addNavigationButton(
            sidebar,
            "Projects",
            this::showProjects
        );

        addNavigationButton(
            sidebar,
            "Settings",
            this::showSettings
        );

        VBox spacer =
            new VBox();

        VBox.setVgrow(
            spacer,
            Priority.ALWAYS
        );

        sidebar
            .getChildren()
            .add(
                spacer
            );

        // -----------------------------------------------------
        // SYSTEM MONITOR
        // -----------------------------------------------------

        systemMonitorView =
            new SystemMonitorView();

        sidebar
            .getChildren()
            .add(
                systemMonitorView
            );

        // -----------------------------------------------------
        // ACTIVE NAVIGATION
        // -----------------------------------------------------

        setActiveButtonForPage();

        setLeft(
            sidebar
        );
    }

    private NavigationButton addNavigationButton(
        VBox sidebar,
        String text,
        Runnable action
    ) {

        NavigationButton button =
            new NavigationButton(
                text,
                action
            );

        navigationButtons.add(
            button
        );

        sidebar
            .getChildren()
            .add(
                button
            );

        return button;
    }

    private void setActiveButtonForPage() {

        String wanted;

        switch (
            currentPage
        ) {

            case START ->
                wanted = "Start";

            case DASHBOARD ->
                wanted = "Dashboard";

            case CHAT ->
                wanted = "Chat";

            case MODELS ->
                wanted = "Models";

            case AGENTS ->
                wanted = "Agents";

            case TASKS,
                 TASK_CHAT ->
                wanted = "Tasks";

            case PROJECTS ->
                wanted = "Projects";

            case SETTINGS ->
                wanted = "Settings";

            default ->
                wanted = "Start";
        }

        for (
            NavigationButton button :
            navigationButtons
        ) {

            button.setActive(
                wanted.equals(
                    button.getText()
                )
            );

            button.refreshTheme();
        }
    }

    // =========================================================
    // CONTENT
    // =========================================================

    private void setupContent(
        Node node
    ) {

        content
            .getChildren()
            .clear();

        content.setPadding(
            new Insets(30)
        );

        content.setSpacing(
            20
        );

        content.setFillWidth(
            true
        );

        content.getChildren().add(
            node
        );

        ScrollPane scrollPane =
            new ScrollPane(
                content
            );

        scrollPane.setFitToWidth(
            true
        );

        scrollPane.setFitToHeight(
            false
        );

        scrollPane.setStyle(
            Theme.globalStyle() +
            "-fx-background: " +
            Theme.background() +
            ";" +
            "-fx-background-color: " +
            Theme.background() +
            ";"
        );

        setCenter(
            scrollPane
        );
    }

    // =========================================================
    // CURRENT PAGE
    // =========================================================

    private void renderCurrentPage() {

        switch (
            currentPage
        ) {

            case START ->
                renderStart();

            case DASHBOARD ->
                renderDashboard();

            case CHAT ->
                renderChat();

            case MODELS ->
                renderModels();

            case AGENTS ->
                renderAgents();

            case TASKS ->
                renderTasks();

            case PROJECTS ->
                renderProjects();

            case SETTINGS ->
                renderSettings();

            case TASK_CHAT -> {

                if (
                    currentTask != null
                ) {

                    renderTaskChat();

                } else {

                    showTasks();
                }
            }
        }

        setActiveButtonForPage();
    }

    // =========================================================
    // START
    // =========================================================

    private void showStart() {

        currentPage =
            Page.START;

        currentTask =
            null;

        renderStart();

        setActiveButtonForPage();
    }

    private void renderStart() {

        setupContent(
            new WelcomeView(
                this::showDashboard,
                this::showChat,
                this::showModels,
                this::showAgents,
                this::showTasks,
                this::showProjects,
                this::showSettings
            )
        );
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private void showDashboard() {

        currentPage =
            Page.DASHBOARD;

        currentTask =
            null;

        renderDashboard();

        setActiveButtonForPage();
    }

    private void renderDashboard() {

        setupContent(
            new DashboardView()
        );
    }

    // =========================================================
    // CHAT
    // =========================================================

    private void showChat() {

        currentPage =
            Page.CHAT;

        currentTask =
            null;

        renderChat();

        setActiveButtonForPage();
    }

    private void renderChat() {

        setupContent(
            new ChatView()
        );
    }

    // =========================================================
    // MODELS
    // =========================================================

    private void showModels() {

        currentPage =
            Page.MODELS;

        currentTask =
            null;

        renderModels();

        setActiveButtonForPage();
    }

    private void renderModels() {

        setupContent(
            new ModelsView()
        );
    }

    // =========================================================
    // AGENTS
    // =========================================================

    private void showAgents() {

        currentPage =
            Page.AGENTS;

        currentTask =
            null;

        renderAgents();

        setActiveButtonForPage();
    }

    private void renderAgents() {

        setupContent(
            new AgentsView()
        );
    }

    // =========================================================
    // TASKS
    // =========================================================

    private void showTasks() {

        currentPage =
            Page.TASKS;

        currentTask =
            null;

        renderTasks();

        setActiveButtonForPage();
    }

    private void renderTasks() {

        setupContent(
            new TasksView()
        );
    }

    // =========================================================
    // PROJECTS
    // =========================================================

    private void showProjects() {

        currentPage =
            Page.PROJECTS;

        currentTask =
            null;

        renderProjects();

        setActiveButtonForPage();
    }

    private void renderProjects() {

        setupContent(
            new ProjectsView(
                this::showTaskChat
            )
        );
    }

    // =========================================================
    // TASK CHAT
    // =========================================================

    private void showTaskChat(
        Task task
    ) {

        if (
            task == null
        ) {

            showTasks();

            return;
        }

        currentPage =
            Page.TASK_CHAT;

        currentTask =
            task;

        renderTaskChat();

        setActiveButtonForPage();
    }

    private void renderTaskChat() {

        if (
            currentTask == null
        ) {

            showTasks();

            return;
        }

        setupContent(
            new TaskChatView(
                currentTask
            )
        );
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private void showSettings() {

        currentPage =
            Page.SETTINGS;

        currentTask =
            null;

        renderSettings();

        setActiveButtonForPage();
    }

    private void renderSettings() {

        setupContent(
            new SettingsView()
        );
    }
}
