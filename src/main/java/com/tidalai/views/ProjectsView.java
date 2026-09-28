package com.tidalai.views;

import com.tidalai.AppState;
import com.tidalai.Theme;
import com.tidalai.projects.Project;
import com.tidalai.tasks.Task;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class ProjectsView extends VBox {

    private final Consumer<Task> openTask;

    private final VBox projectList =
        new VBox(12);

    public ProjectsView(
        Consumer<Task> openTask
    ) {

        this.openTask =
            openTask;

        setSpacing(20);

        Label title =
            new Label(
                "Projects"
            );

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;"
        );

        Label subtitle =
            new Label(
                "Organize your tasks into separate workspaces."
            );

        subtitle.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 13px;"
        );

        VBox createCard =
            new VBox(12);

        createCard.setPadding(
            new Insets(20)
        );

        createCard.setStyle(
            Theme.cardStyle()
        );

        TextField name =
            new TextField();

        name.setPromptText(
            "Project name"
        );

        name.setStyle(
            "-fx-control-inner-background: " +
            Theme.cardColor() + ";" +
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-prompt-text-fill: " +
            Theme.muted() + ";" +
            "-fx-border-color: " +
            Theme.border() + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );

        TextArea description =
            new TextArea();

        description.setPromptText(
            "Project description"
        );

        description.setPrefRowCount(
            3
        );

        description.setWrapText(
            true
        );

        description.setStyle(
            "-fx-control-inner-background: " +
            Theme.cardColor() + ";" +
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-prompt-text-fill: " +
            Theme.muted() + ";" +
            "-fx-border-color: " +
            Theme.border() + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );

        Button create =
            new Button(
                "Create Project"
            );

        create.setStyle(
            "-fx-background-color: " +
            Theme.accent() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 16;" +
            "-fx-font-weight: bold;"
        );

        create.setOnAction(
            event -> {

                String projectName =
                    name
                        .getText()
                        .trim();

                if (
                    projectName.isEmpty()
                ) {

                    return;
                }

                AppState.PROJECTS
                    .createProject(
                        projectName,
                        description
                            .getText()
                    );

                name.clear();
                description.clear();

                refreshProjects();
            }
        );

        createCard.getChildren().addAll(
            new Label("New Project"),
            name,
            description,
            create
        );

        ScrollPane scroll =
            new ScrollPane(
                projectList
            );

        scroll.setFitToWidth(
            true
        );

        scroll.setStyle(
            "-fx-background: " +
            Theme.background() + ";" +
            "-fx-background-color: " +
            Theme.background() + ";"
        );

        VBox.setVgrow(
            scroll,
            Priority.ALWAYS
        );

        getChildren().addAll(
            title,
            subtitle,
            createCard,
            scroll
        );

        AppState.PROJECTS
            .getProjects()
            .addListener(
                (ListChangeListener<Project>)
                    change -> refreshProjects()
            );

        refreshProjects();
    }

    private void refreshProjects() {

        projectList
            .getChildren()
            .clear();

        if (
            AppState.PROJECTS
                .getProjects()
                .isEmpty()
        ) {

            projectList.getChildren().add(
                createDescription(
                    "No projects yet."
                )
            );

            return;
        }

        for (
            Project project :
            AppState.PROJECTS
                .getProjects()
        ) {

            projectList
                .getChildren()
                .add(
                    createProjectCard(
                        project
                    )
                );
        }
    }

    private VBox createProjectCard(
        Project project
    ) {

        VBox card =
            new VBox(10);

        card.setPadding(
            new Insets(18)
        );

        card.setStyle(
            Theme.cardStyle()
        );

        Label name =
            new Label(
                project.getName()
            );

        name.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;"
        );

        Label description =
            createDescription(
                project.getDescription()
            );

        VBox taskList =
            new VBox(6);

        for (
            Task task :
            AppState.TASKS
                .getTasks()
        ) {

            if (
                project.getId().equals(
                    task.getProjectId()
                )
            ) {

                Button taskButton =
                    new Button(
                        "• " +
                        task.getDescription()
                    );

                taskButton.setMaxWidth(
                    Double.MAX_VALUE
                );

                taskButton.setAlignment(
                    Pos.CENTER_LEFT
                );

                taskButton.setWrapText(
                    true
                );

                taskButton.setStyle(
                    Theme.buttonStyle()
                );

                taskButton.setOnAction(
                    event ->
                        openTask.accept(task)
                );

                taskList
                    .getChildren()
                    .add(
                        taskButton
                    );
            }
        }

        if (
            taskList
                .getChildren()
                .isEmpty()
        ) {

            taskList
                .getChildren()
                .add(
                    createDescription(
                        "No tasks assigned."
                    )
                );
        }

        Button delete =
            new Button(
                "Delete Project"
            );

        delete.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " +
            Theme.error() + ";" +
            "-fx-border-color: " +
            Theme.border() + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 12;"
        );

        delete.setOnAction(
            event -> {

                for (
                    Task task :
                    AppState.TASKS
                        .getTasks()
                ) {

                    if (
                        project.getId().equals(
                            task.getProjectId()
                        )
                    ) {

                        task.setProjectId(
                            null
                        );
                    }
                }

                AppState.PROJECTS
                    .deleteProject(
                        project
                    );

                refreshProjects();
            }
        );

        card.getChildren().addAll(
            name,
            description,
            taskList,
            delete
        );

        return card;
    }

    private Label createDescription(
        String text
    ) {

        Label label =
            new Label(
                text == null ||
                text.isBlank()
                    ? "No description."
                    : text
            );

        label.setWrapText(
            true
        );

        label.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 12px;"
        );

        return label;
    }
}