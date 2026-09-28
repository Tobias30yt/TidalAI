package com.tidalai.views;

import com.tidalai.Theme;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class WelcomeView extends VBox {

    public WelcomeView(
        Runnable openDashboard,
        Runnable openChat,
        Runnable openModels,
        Runnable openAgents,
        Runnable openTasks,
        Runnable openProjects,
        Runnable openSettings
    ) {

        setSpacing(22);
        setPadding(new Insets(10));
        setFillWidth(true);

        Label badge =
            new Label("WELCOME TO TIDALAI");

        badge.setStyle(
            "-fx-text-fill: " +
            Theme.accent() + ";" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );

        Label title =
            new Label(
                "Your local AI workspace."
            );

        title.setWrapText(true);

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 34px;" +
            "-fx-font-weight: bold;"
        );

        Label subtitle =
            new Label(
                "TidalAI connects your local models, chat, agents, " +
                "tasks and projects in one workspace."
            );

        subtitle.setWrapText(true);

        subtitle.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 15px;"
        );

        VBox hero =
            new VBox(
                8,
                badge,
                title,
                subtitle
            );

        VBox quickStart =
            createCard(
                "Quick Start",
                "Start here when you are using TidalAI for the first time.",
                createButton(
                    "Open Dashboard",
                    openDashboard,
                    true
                )
            );

        VBox chat =
            createCard(
                "Chat",
                "Talk directly to a model loaded in LM Studio. " +
                "The conversation keeps its context.",
                createButton(
                    "Open Chat",
                    openChat,
                    false
                )
            );

        VBox models =
            createCard(
                "Models",
                "See which local models LM Studio exposes and choose " +
                "the model you want to use.",
                createButton(
                    "Open Models",
                    openModels,
                    false
                )
            );

        VBox agents =
            createCard(
                "Agents",
                "Use the multi-agent system with Main, Reasoning, " +
                "Coder, Researcher, Tester and Reviewer agents.",
                createButton(
                    "Open Agents",
                    openAgents,
                    false
                )
            );

        VBox tasks =
            createCard(
                "Tasks",
                "Turn a larger goal into an executable task. " +
                "Tasks can run through the agent orchestration pipeline.",
                createButton(
                    "Open Tasks",
                    openTasks,
                    false
                )
            );

        VBox projects =
            createCard(
                "Projects",
                "Group related tasks into projects so your work stays organized.",
                createButton(
                    "Open Projects",
                    openProjects,
                    false
                )
            );

        VBox settings =
            createCard(
                "Settings",
                "Choose the workspace, agent configuration, approval mode, " +
                "terminal behavior and appearance.",
                createButton(
                    "Open Settings",
                    openSettings,
                    false
                )
            );

        GridPane grid =
            new GridPane();

        grid.setHgap(14);
        grid.setVgap(14);

        grid.add(
            quickStart,
            0,
            0
        );

        grid.add(
            chat,
            1,
            0
        );

        grid.add(
            models,
            0,
            1
        );

        grid.add(
            agents,
            1,
            1
        );

        grid.add(
            tasks,
            0,
            2
        );

        grid.add(
            projects,
            1,
            2
        );

        grid.add(
            settings,
            0,
            3
        );

        GridPane.setHgrow(
            quickStart,
            Priority.ALWAYS
        );

        GridPane.setHgrow(
            chat,
            Priority.ALWAYS
        );

        GridPane.setHgrow(
            models,
            Priority.ALWAYS
        );

        GridPane.setHgrow(
            agents,
            Priority.ALWAYS
        );

        GridPane.setHgrow(
            tasks,
            Priority.ALWAYS
        );

        GridPane.setHgrow(
            projects,
            Priority.ALWAYS
        );

        GridPane.setHgrow(
            settings,
            Priority.ALWAYS
        );

        HBox footer =
            new HBox();

        footer.setAlignment(
            Pos.CENTER_RIGHT
        );

        Label tip =
            new Label(
                "Tip: Configure your workspace in Settings before letting coding agents modify files."
            );

        tip.setWrapText(true);

        tip.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 12px;"
        );

        footer.getChildren().add(
            tip
        );

        getChildren().addAll(
            hero,
            grid,
            footer
        );
    }

    private VBox createCard(
        String title,
        String description,
        Button button
    ) {

        VBox card =
            new VBox(10);

        card.setPadding(
            new Insets(18)
        );

        card.setMinHeight(
            170
        );

        card.setStyle(
            Theme.cardStyle()
        );

        Label titleLabel =
            new Label(title);

        titleLabel.setStyle(
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;"
        );

        Label descriptionLabel =
            new Label(description);

        descriptionLabel.setWrapText(true);

        descriptionLabel.setStyle(
            "-fx-text-fill: " +
            Theme.muted() + ";" +
            "-fx-font-size: 13px;"
        );

        VBox.setVgrow(
            descriptionLabel,
            Priority.ALWAYS
        );

        card.getChildren().addAll(
            titleLabel,
            descriptionLabel,
            button
        );

        return card;
    }

    private Button createButton(
        String text,
        Runnable action,
        boolean primary
    ) {

        Button button =
            new Button(text);

        button.setMaxWidth(
            Double.MAX_VALUE
        );

        button.setStyle(
            primary
                ? primaryButtonStyle()
                : secondaryButtonStyle()
        );

        button.setOnAction(
            event -> action.run()
        );

        return button;
    }

    private String primaryButtonStyle() {

        return
            "-fx-background-color: " +
            Theme.accent() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 16;" +
            "-fx-font-weight: bold;";
    }

    private String secondaryButtonStyle() {

        return
            "-fx-background-color: " +
            Theme.cardColor() + ";" +
            "-fx-text-fill: " +
            Theme.text() + ";" +
            "-fx-border-color: " +
            Theme.border() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 10 16;";
    }
}
