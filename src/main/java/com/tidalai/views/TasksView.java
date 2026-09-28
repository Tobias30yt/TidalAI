package com.tidalai.views;

import com.tidalai.AppState;
import com.tidalai.Theme;
import com.tidalai.projects.Project;
import com.tidalai.tasks.Task;
import com.tidalai.tasks.TaskStatus;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TasksView extends BorderPane {

    private final VBox taskList =
        new VBox(10);

    private final VBox chatContainer =
        new VBox();

    private final TextArea taskInput =
        new TextArea();

    private final ComboBox<Project> projectSelector =
        new ComboBox<>();

    private final Button runButton =
        new Button("Run Task");

    private Task selectedTask;

    public TasksView() {

        setPadding(
            new Insets(24)
        );

        createTaskSidebar();
        createMainArea();

        AppState.TASKS
            .getTasks()
            .addListener(
                (ListChangeListener<Task>)
                    change -> refreshTasks()
            );

        AppState.PROJECTS
            .getProjects()
            .addListener(
                (ListChangeListener<Project>)
                    change -> refreshProjects()
            );

        refreshProjects();
        refreshTasks();
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    private void createTaskSidebar() {

        VBox sidebar =
            new VBox(14);

        sidebar.setPrefWidth(
            280
        );

        sidebar.setPadding(
            new Insets(16)
        );

        sidebar.setStyle(
            Theme.cardStyle()
        );

        Label title =
            new Label(
                "Tasks"
            );

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;"
        );

        ScrollPane taskScroll =
            new ScrollPane(
                taskList
            );

        taskScroll.setFitToWidth(
            true
        );

        taskScroll.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;"
        );

        VBox.setVgrow(
            taskScroll,
            Priority.ALWAYS
        );

        sidebar.getChildren().addAll(
            title,
            taskScroll
        );

        setLeft(
            sidebar
        );
    }

    // =========================================================
    // MAIN AREA
    // =========================================================

    private void createMainArea() {

        VBox main =
            new VBox(18);

        main.setPadding(
            new Insets(
                0,
                0,
                0,
                20
            )
        );

        Label title =
            new Label(
                "Task Workspace"
            );

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;"
        );

        Label description =
            new Label(
                "Create a task or open an existing task to continue its conversation."
            );

        description.setWrapText(
            true
        );

        description.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 13px;"
        );

        VBox createCard =
            new VBox(12);

        createCard.setPadding(
            new Insets(18)
        );

        createCard.setStyle(
            Theme.cardStyle()
        );

        Label newTaskTitle =
            new Label(
                "New Task"
            );

        newTaskTitle.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );

        taskInput.setPromptText(
            "What should TidalAI work on?"
        );

        taskInput.setWrapText(
            true
        );

        taskInput.setPrefRowCount(
            3
        );

        taskInput.setStyle(
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

        projectSelector.setPromptText(
            "Assign to project"
        );

        projectSelector.setMaxWidth(
            Double.MAX_VALUE
        );

        projectSelector.setStyle(
            Theme.buttonStyle()
        );

        runButton.setStyle(
            "-fx-background-color: " +
            Theme.accent() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 18;" +
            "-fx-font-weight: bold;"
        );

        runButton.setOnAction(
            event -> createTask()
        );

        HBox createRow =
            new HBox(
                10,
                projectSelector,
                runButton
            );

        HBox.setHgrow(
            projectSelector,
            Priority.ALWAYS
        );

        createCard.getChildren().addAll(
            newTaskTitle,
            taskInput,
            createRow
        );

        chatContainer.setMaxWidth(
            Double.MAX_VALUE
        );

        VBox placeholder =
            new VBox(10);

        placeholder.setAlignment(
            Pos.CENTER
        );

        Label placeholderLabel =
            new Label(
                "Select a task to open its chat."
            );

        placeholderLabel.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 14px;"
        );

        placeholder
            .getChildren()
            .add(
                placeholderLabel
            );

        chatContainer
            .getChildren()
            .add(
                placeholder
            );

        VBox.setVgrow(
            chatContainer,
            Priority.ALWAYS
        );

        main.getChildren().addAll(
            title,
            description,
            createCard,
            chatContainer
        );

        VBox.setVgrow(
            main,
            Priority.ALWAYS
        );

        setCenter(
            main
        );
    }

    // =========================================================
    // CREATE TASK
    // =========================================================

    private void createTask() {

        String description =
            taskInput
                .getText()
                .trim();

        if (
            description.isEmpty()
        ) {

            return;
        }

        Project project =
            projectSelector
                .getSelectionModel()
                .getSelectedItem();

        Task task =
            AppState.TASKS.createTask(
                description
            );

        AppState.TASKS.assignToProject(
            task,
            project
        );

        taskInput.clear();

        selectTask(
            task
        );

        AppState.TASKS.startTask(
            task
        );

        refreshTasks();

        AppState.ORCHESTRATOR
            .execute(
                task.getDescription(),
                task::isStopRequested
            )
            .whenComplete(
                (result, error) -> {

                    Platform.runLater(() -> {

                        if (
                            task.isStopRequested()
                        ) {

                            task.setStatus(
                                TaskStatus.STOPPED
                            );

                        } else if (
                            error != null
                        ) {

                            AppState.TASKS.failTask(
                                task
                            );

                        } else {

                            AppState.TASKS.completeTask(
                                task
                            );
                        }

                        refreshTasks();
                    });
                }
            );
    }

    // =========================================================
    // PROJECTS
    // =========================================================

    private void refreshProjects() {

        projectSelector
            .getItems()
            .setAll(
                AppState.PROJECTS
                    .getProjects()
            );
    }

    // =========================================================
    // TASK LIST
    // =========================================================

    private void refreshTasks() {

        taskList
            .getChildren()
            .clear();

        if (
            AppState.TASKS
                .getTasks()
                .isEmpty()
        ) {

            taskList
                .getChildren()
                .add(
                    createMutedLabel(
                        "No tasks yet."
                    )
                );

            return;
        }

        for (
            Task task :
            AppState.TASKS
                .getTasks()
        ) {

            Button button =
                new Button();

            button.setMaxWidth(
                Double.MAX_VALUE
            );

            button.setAlignment(
                Pos.CENTER_LEFT
            );

            String status =
                switch (
                    task.getStatus()
                ) {

                    case QUEUED ->
                        "Queued";

                    case RUNNING ->
                        "Running";

                    case COMPLETED ->
                        "Completed";

                    case FAILED ->
                        "Failed";

                    case STOPPED ->
                        "Stopped";
                };

            button.setText(
                status +
                "  •  " +
                task.getDescription()
            );

            button.setWrapText(
                true
            );

            button.setStyle(
                "-fx-background-color: " +
                (
                    task == selectedTask
                        ? Theme.accent()
                        : Theme.cardColor()
                ) + ";" +
                "-fx-text-fill: " +
                (
                    task == selectedTask
                        ? "white"
                        : Theme.text()
                ) + ";" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: " +
                Theme.border() + ";" +
                "-fx-border-radius: 8;" +
                "-fx-padding: 10;"
            );

            button.setOnAction(
                event ->
                    selectTask(task)
            );

            taskList
                .getChildren()
                .add(
                    button
                );
        }
    }

    // =========================================================
    // OPEN TASK CHAT
    // =========================================================

    private void selectTask(
        Task task
    ) {

        selectedTask =
            task;

        chatContainer
            .getChildren()
            .clear();

        chatContainer
            .getChildren()
            .add(
                new TaskChatView(
                    task
                )
            );

        refreshTasks();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Label createMutedLabel(
        String text
    ) {

        Label label =
            new Label(
                text
            );

        label.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 12px;"
        );

        return label;
    }
}