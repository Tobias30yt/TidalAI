package com.tidalai.views;

import com.tidalai.AppState;
import com.tidalai.Theme;
import com.tidalai.monitor.SystemMonitorService;
import com.tidalai.monitor.SystemStats;
import com.tidalai.projects.Project;
import com.tidalai.tasks.Task;
import com.tidalai.tasks.TaskStatus;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class DashboardView extends VBox {

    private final Label modelsValue =
        createBigValue();

    private final Label agentsValue =
        createBigValue();

    private final Label tasksValue =
        createBigValue();

    private final Label projectsValue =
        createBigValue();

    private final Label cpuValue =
        createMetricValue();

    private final Label ramValue =
        createMetricValue();

    private final Label gpuValue =
        createMetricValue();

    private final Label vramValue =
        createMetricValue();

    private final Label temperatureValue =
        createMetricValue();

    private final Label gpuName =
        new Label(
            "GPU unavailable"
        );

    private final ProgressBar cpuBar =
        createBar();

    private final ProgressBar ramBar =
        createBar();

    private final ProgressBar gpuBar =
        createBar();

    private final ProgressBar vramBar =
        createBar();

    private final SystemMonitorService monitor =
        SystemMonitorService.getInstance();

    private final Consumer<SystemStats> monitorListener =
        this::updateSystemStats;

    public DashboardView() {

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

        refreshCounts();

        monitor.subscribe(
            monitorListener
        );

        monitor.start();
    }

    // =========================================================
    // VIEW
    // =========================================================

    private void createView() {

        Label title =
            new Label(
                "Dashboard"
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
                "Overview of your local AI workspace."
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

        GridPane statsGrid =
            new GridPane();

        statsGrid.setHgap(
            14
        );

        statsGrid.setVgap(
            14
        );

        statsGrid.add(
            createStatCard(
                "Models",
                modelsValue,
                "Available LM Studio models"
            ),
            0,
            0
        );

        statsGrid.add(
            createStatCard(
                "Agents",
                agentsValue,
                "Registered agents"
            ),
            1,
            0
        );

        statsGrid.add(
            createStatCard(
                "Tasks",
                tasksValue,
                "Total tasks"
            ),
            2,
            0
        );

        statsGrid.add(
            createStatCard(
                "Projects",
                projectsValue,
                "Projects"
            ),
            3,
            0
        );

        for (
            int i = 0;
            i < 4;
            i++
        ) {

            if (
                statsGrid
                    .getChildren()
                    .size() > i
            ) {

                GridPane.setHgrow(
                    statsGrid
                        .getChildren()
                        .get(i),
                    Priority.ALWAYS
                );
            }
        }

        VBox systemCard =
            createSystemCard();

        VBox activityCard =
            createActivityCard();

        GridPane lower =
            new GridPane();

        lower.setHgap(
            14
        );

        lower.setVgap(
            14
        );

        lower.add(
            systemCard,
            0,
            0
        );

        lower.add(
            activityCard,
            1,
            0
        );

        GridPane.setHgrow(
            systemCard,
            Priority.ALWAYS
        );

        GridPane.setHgrow(
            activityCard,
            Priority.ALWAYS
        );

        ScrollPane scroll =
            new ScrollPane();

        VBox pageContent =
            new VBox(
                20,
                statsGrid,
                lower
            );

        pageContent.setPadding(
            new Insets(2)
        );

        scroll.setContent(
            pageContent
        );

        scroll.setFitToWidth(
            true
        );

        scroll.setStyle(
            Theme.globalStyle() +
            "-fx-background-color: transparent;" +
            "-fx-background: transparent;"
        );

        VBox.setVgrow(
            scroll,
            Priority.ALWAYS
        );

        getChildren().addAll(
            title,
            subtitle,
            scroll
        );
    }

    // =========================================================
    // STAT CARD
    // =========================================================

    private VBox createStatCard(
        String title,
        Label value,
        String description
    ) {

        VBox card =
            new VBox(
                6
            );

        card.setPadding(
            new Insets(18)
        );

        card.setMinHeight(
            120
        );

        card.setStyle(
            Theme.cardStyle()
        );

        Label titleLabel =
            new Label(
                title
            );

        titleLabel.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 11px;" +
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
            "-fx-font-size: 10px;"
        );

        card.getChildren().addAll(
            titleLabel,
            value,
            descriptionLabel
        );

        return card;
    }

    // =========================================================
    // SYSTEM CARD
    // =========================================================

    private VBox createSystemCard() {

        VBox card =
            new VBox(
                14
            );

        card.setPadding(
            new Insets(18)
        );

        card.setStyle(
            Theme.cardStyle()
        );

        Label title =
            new Label(
                "System Resources"
            );

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;"
        );

        Label subtitle =
            new Label(
                "Live CPU, RAM, GPU and VRAM usage."
            );

        subtitle.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 11px;"
        );

        VBox cpu =
            createResourceRow(
                "CPU",
                cpuValue,
                cpuBar
            );

        VBox ram =
            createResourceRow(
                "RAM",
                ramValue,
                ramBar
            );

        gpuName.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );

        VBox gpu =
            createResourceRow(
                "GPU Load",
                gpuValue,
                gpuBar
            );

        VBox vram =
            createResourceRow(
                "VRAM",
                vramValue,
                vramBar
            );

        HBox gpuHeader =
            new HBox(
                gpuName
            );

        gpuHeader.setAlignment(
            Pos.CENTER_LEFT
        );

        HBox.setHgrow(
            gpuName,
            Priority.ALWAYS
        );

        HBox temperatureRow =
            createTextMetric(
                "Temperature",
                temperatureValue
            );

        card.getChildren().addAll(
            title,
            subtitle,
            cpu,
            ram,
            gpuHeader,
            gpu,
            vram,
            temperatureRow
        );

        return card;
    }

    private VBox createResourceRow(
        String title,
        Label value,
        ProgressBar bar
    ) {

        VBox box =
            new VBox(
                6
            );

        HBox row =
            new HBox(
                8
            );

        row.setAlignment(
            Pos.CENTER_LEFT
        );

        Label titleLabel =
            new Label(
                title
            );

        titleLabel.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 11px;"
        );

        HBox.setHgrow(
            titleLabel,
            Priority.ALWAYS
        );

        row.getChildren().addAll(
            titleLabel,
            value
        );

        box.getChildren().addAll(
            row,
            bar
        );

        return box;
    }

    private HBox createTextMetric(
        String title,
        Label value
    ) {

        HBox row =
            new HBox(
                8
            );

        row.setAlignment(
            Pos.CENTER_LEFT
        );

        Label label =
            new Label(
                title
            );

        label.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 11px;"
        );

        HBox.setHgrow(
            label,
            Priority.ALWAYS
        );

        row.getChildren().addAll(
            label,
            value
        );

        return row;
    }

    // =========================================================
    // ACTIVITY
    // =========================================================

    private VBox createActivityCard() {

        VBox card =
            new VBox(
                12
            );

        card.setPadding(
            new Insets(18)
        );

        card.setStyle(
            Theme.cardStyle()
        );

        Label title =
            new Label(
                "Workspace Activity"
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
                "Current task state."
            );

        description.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 11px;"
        );

        Label queued =
            createActivityLabel();

        Label running =
            createActivityLabel();

        Label completed =
            createActivityLabel();

        Label failed =
            createActivityLabel();

        Label stopped =
            createActivityLabel();

        card.getChildren().addAll(
            title,
            description,
            createActivityRow(
                "Queued",
                queued
            ),
            createActivityRow(
                "Running",
                running
            ),
            createActivityRow(
                "Completed",
                completed
            ),
            createActivityRow(
                "Failed",
                failed
            ),
            createActivityRow(
                "Stopped",
                stopped
            )
        );

        /*
         * Store the labels as userData so refreshCounts()
         * can update them without recreating the entire card.
         */
        card.setUserData(
            new ActivityLabels(
                queued,
                running,
                completed,
                failed,
                stopped
            )
        );

        return card;
    }

    private HBox createActivityRow(
        String name,
        Label value
    ) {

        HBox row =
            new HBox(
                8
            );

        row.setAlignment(
            Pos.CENTER_LEFT
        );

        Label nameLabel =
            new Label(
                name
            );

        nameLabel.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 11px;"
        );

        HBox.setHgrow(
            nameLabel,
            Priority.ALWAYS
        );

        row.getChildren().addAll(
            nameLabel,
            value
        );

        return row;
    }

    // =========================================================
    // COUNTS
    // =========================================================

    private void refreshCounts() {

        Platform.runLater(
            () -> {

                modelsValue.setText(
                    String.valueOf(
                        AppState.PROVIDER
                            .getModels()
                            .size()
                    )
                );

                agentsValue.setText(
                    String.valueOf(
                        AppState.AGENTS
                            .getAgents()
                            .size()
                    )
                );

                tasksValue.setText(
                    String.valueOf(
                        AppState.TASKS
                            .getTasks()
                            .size()
                    )
                );

                projectsValue.setText(
                    String.valueOf(
                        AppState.PROJECTS
                            .getProjects()
                            .size()
                    )
                );

                /*
                 * Refresh listeners are attached below to the
                 * observable lists.
                 */
            }
        );
    }

    // =========================================================
    // MONITOR
    // =========================================================

    private void updateSystemStats(
        SystemStats stats
    ) {

        if (
            !Platform.isFxApplicationThread()
        ) {

            Platform.runLater(
                () ->
                    updateSystemStats(
                        stats
                    )
            );

            return;
        }

        cpuValue.setText(
            formatPercent(
                stats.cpuUsage()
            )
        );

        cpuBar.setProgress(
            clamp(
                stats.cpuUsage()
            )
        );

        ramValue.setText(
            formatPercent(
                stats.ramUsagePercent() /
                100.0
            ) +
            "  " +
            formatBytes(
                stats.ramUsedBytes()
            ) +
            " / " +
            formatBytes(
                stats.ramTotalBytes()
            )
        );

        ramBar.setProgress(
            clamp(
                stats.ramUsagePercent() /
                100.0
            )
        );

        if (
            !stats.gpuAvailable()
        ) {

            gpuName.setText(
                "NVIDIA GPU not detected"
            );

            gpuValue.setText(
                "--"
            );

            vramValue.setText(
                "--"
            );

            vramBar.setProgress(
                0
            );

            temperatureValue.setText(
                "--"
            );

            gpuBar.setProgress(
                0
            );

            return;
        }

        gpuName.setText(
            stats.gpuName()
        );

        gpuValue.setText(
            formatPercent(
                stats.gpuUtilization()
                /
                100.0
            )
        );

        gpuBar.setProgress(
            clamp(
                stats.gpuUtilization()
                /
                100.0
            )
        );

        vramValue.setText(
            formatPercent(
                stats.vramUsagePercent()
                /
                100.0
            ) +
            "  " +
            formatBytes(
                stats.vramUsedBytes()
            ) +
            " / " +
            formatBytes(
                stats.vramTotalBytes()
            )
        );

        vramBar.setProgress(
            clamp(
                stats.vramUsagePercent()
                /
                100.0
            )
        );

        if (
            stats.temperatureCelsius() >= 0
        ) {

            temperatureValue.setText(
                String.format(
                    java.util.Locale.ROOT,
                    "%.0f °C",
                    stats.temperatureCelsius()
                )
            );

        } else {

            temperatureValue.setText(
                "--"
            );
        }
    }

    // =========================================================
    // LABEL HELPERS
    // =========================================================

    private Label createBigValue() {

        Label label =
            new Label(
                "0"
            );

        label.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 30px;" +
            "-fx-font-weight: bold;"
        );

        return label;
    }

    private Label createMetricValue() {

        Label label =
            new Label(
                "--"
            );

        label.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );

        return label;
    }

    private Label createActivityLabel() {

        Label label =
            new Label(
                "0"
            );

        label.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );

        return label;
    }

    private ProgressBar createBar() {

        ProgressBar bar =
            new ProgressBar(
                0
            );

        bar.setMaxWidth(
            Double.MAX_VALUE
        );

        bar.setPrefHeight(
            7
        );

        bar.setStyle(
            "-fx-accent: " +
            Theme.accent() +
            ";"
        );

        return bar;
    }

    // =========================================================
    // FORMAT
    // =========================================================

    private String formatPercent(
        double fraction
    ) {

        if (
            fraction < 0
        ) {

            return "--";
        }

        return String.format(
            java.util.Locale.ROOT,
            "%.0f%%",
            fraction * 100.0
        );
    }

    private String formatBytes(
        long bytes
    ) {

        if (
            bytes < 0
        ) {

            return "--";
        }

        double gb =
            bytes /
            1024.0 /
            1024.0 /
            1024.0;

        return String.format(
            java.util.Locale.ROOT,
            "%.1f GB",
            gb
        );
    }

    private double clamp(
        double value
    ) {

        return Math.max(
            0,
            Math.min(
                1,
                value
            )
        );
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    public void dispose() {

        monitor.unsubscribe(
            monitorListener
        );
    }

    // =========================================================
    // ACTIVITY DATA
    // =========================================================

    private record ActivityLabels(
        Label queued,
        Label running,
        Label completed,
        Label failed,
        Label stopped
    ) {
    }
}
