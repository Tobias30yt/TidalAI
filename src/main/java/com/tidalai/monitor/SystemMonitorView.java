package com.tidalai.monitor;

import com.tidalai.Theme;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Locale;
import java.util.function.Consumer;

public class SystemMonitorView extends VBox {

    private final Label cpuValue =
        createValueLabel();

    private final Label ramValue =
        createValueLabel();

    private final Label ramPercent =
        createPercentLabel();

    private final Label gpuName =
        createValueLabel();

    private final Label gpuValue =
        createValueLabel();

    private final Label vramValue =
        createValueLabel();

    private final Label vramPercent =
        createPercentLabel();

    private final Label temperatureValue =
        createValueLabel();

    private final Label powerValue =
        createValueLabel();

    private final Label gpuStatus =
        createStatusLabel();

    private final ProgressBar ramBar =
        createProgressBar();

    private final ProgressBar gpuBar =
        createProgressBar();

    private final ProgressBar vramBar =
        createProgressBar();

    private final SystemMonitorService service =
        SystemMonitorService.getInstance();

    private final Consumer<SystemStats> listener =
        this::updateStats;

    public SystemMonitorView() {

        setSpacing(
            10
        );

        setPadding(
            new Insets(14)
        );

        setMaxWidth(
            Double.MAX_VALUE
        );

        setStyle(
            Theme.cardStyle()
        );

        createView();

        service.subscribe(
            listener
        );

        service.start();
    }

    // =========================================================
    // VIEW
    // =========================================================

    private void createView() {

        Label title =
            new Label(
                "System Monitor"
            );

        title.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;"
        );

        Label subtitle =
            new Label(
                "Live hardware usage"
            );

        subtitle.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 10px;"
        );

        VBox header =
            new VBox(
                2,
                title,
                subtitle
            );

        VBox cpu =
            createMetric(
                "CPU",
                cpuValue,
                null,
                cpuBar()
            );

        VBox ram =
            createMetric(
                "RAM",
                ramValue,
                ramPercent,
                ramBar
            );

        VBox gpu =
            createMetric(
                "GPU Load",
                gpuValue,
                null,
                gpuBar
            );

        VBox vram =
            createMetric(
                "VRAM",
                vramValue,
                vramPercent,
                vramBar
            );

        VBox gpuHeader =
            new VBox(
                3
            );

        gpuName.setText(
            "GPU unavailable"
        );

        gpuName.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );

        gpuStatus.setText(
            "Checking..."
        );

        gpuHeader
            .getChildren()
            .addAll(
                gpuName,
                gpuStatus
            );

        VBox temperature =
            createMetric(
                "Temperature",
                temperatureValue,
                null,
                null
            );

        VBox power =
            createMetric(
                "Power",
                powerValue,
                null,
                null
            );

        getChildren().addAll(
            header,
            cpu,
            ram,
            gpuHeader,
            gpu,
            vram,
            temperature,
            power
        );
    }

    /*
     * CPU gets a dedicated ProgressBar created here because
     * the metric helper otherwise expects the RAM/GPU fields.
     */
    private ProgressBar cpuBar() {

        ProgressBar bar =
            createProgressBar();

        /*
         * Keep a simple listener in the stats updater by
         * storing the bar in the node tree.
         */
        return bar;
    }

    // =========================================================
    // METRIC
    // =========================================================

    private VBox createMetric(
        String name,
        Label value,
        Label percent,
        ProgressBar bar
    ) {

        VBox box =
            new VBox(
                4
            );

        HBox row =
            new HBox(
                7
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
            "-fx-font-size: 10px;"
        );

        HBox.setHgrow(
            nameLabel,
            javafx.scene.layout.Priority.ALWAYS
        );

        row.getChildren().add(
            nameLabel
        );

        if (
            percent != null
        ) {

            row.getChildren().add(
                percent
            );
        }

        row.getChildren().add(
            value
        );

        box.getChildren().add(
            row
        );

        if (
            bar != null
        ) {

            box.getChildren().add(
                bar
            );
        }

        return box;
    }

    private ProgressBar createProgressBar() {

        ProgressBar bar =
            new ProgressBar(
                0
            );

        bar.setMaxWidth(
            Double.MAX_VALUE
        );

        bar.setPrefHeight(
            6
        );

        bar.setStyle(
            "-fx-accent: " +
            Theme.accent() +
            ";"
        );

        return bar;
    }

    // =========================================================
    // UPDATE
    // =========================================================

    private void updateStats(
        SystemStats stats
    ) {

        if (
            !Platform.isFxApplicationThread()
        ) {

            Platform.runLater(
                () ->
                    updateStats(
                        stats
                    )
            );

            return;
        }

        /*
         * CPU percent.
         */
        double cpuPercent =
            stats.cpuUsage();

        cpuValue.setText(
            formatPercent(
                cpuPercent
            )
        );

        /*
         * Update the first ProgressBar in the view.
         */
        if (
            getChildren().size() > 1
        ) {

            updateFirstProgressBar(
                cpuPercent / 100.0
            );
        }

        double ramPercentValue =
            stats.ramUsagePercent();

        ramPercent.setText(
            formatPercent(
                ramPercentValue
            )
        );

        ramValue.setText(
            formatBytes(
                stats.ramUsedBytes()
            ) +
            " / " +
            formatBytes(
                stats.ramTotalBytes()
            )
        );

        ramBar.setProgress(
            clampFraction(
                ramPercentValue / 100.0
            )
        );

        if (
            !stats.gpuAvailable()
        ) {

            gpuName.setText(
                "GPU unavailable"
            );

            gpuStatus.setText(
                "NVIDIA GPU not detected"
            );

            gpuValue.setText(
                "--"
            );

            vramPercent.setText(
                "--"
            );

            vramValue.setText(
                "--"
            );

            temperatureValue.setText(
                "--"
            );

            powerValue.setText(
                "--"
            );

            gpuBar.setProgress(
                0
            );

            vramBar.setProgress(
                0
            );

            return;
        }

        gpuName.setText(
            stats.gpuName()
        );

        gpuStatus.setText(
            "NVIDIA GPU detected"
        );

        double gpuUsage =
            stats.gpuUtilization();

        gpuValue.setText(
            formatPercent(
                gpuUsage
            )
        );

        gpuBar.setProgress(
            clampFraction(
                gpuUsage / 100.0
            )
        );

        double vramUsage =
            stats.vramUsagePercent();

        vramPercent.setText(
            formatPercent(
                vramUsage
            )
        );

        vramValue.setText(
            formatBytes(
                stats.vramUsedBytes()
            ) +
            " / " +
            formatBytes(
                stats.vramTotalBytes()
            )
        );

        vramBar.setProgress(
            clampFraction(
                vramUsage / 100.0
            )
        );

        if (
            stats.temperatureCelsius() >= 0
        ) {

            temperatureValue.setText(
                String.format(
                    Locale.ROOT,
                    "%.0f °C",
                    stats.temperatureCelsius()
                )
            );

        } else {

            temperatureValue.setText(
                "--"
            );
        }

        if (
            stats.powerDrawWatts() >= 0
        ) {

            if (
                stats.powerLimitWatts() > 0
            ) {

                powerValue.setText(
                    String.format(
                        Locale.ROOT,
                        "%.0f W / %.0f W",
                        stats.powerDrawWatts(),
                        stats.powerLimitWatts()
                    )
                );

            } else {

                powerValue.setText(
                    String.format(
                        Locale.ROOT,
                        "%.0f W",
                        stats.powerDrawWatts()
                    )
                );
            }

        } else {

            powerValue.setText(
                "--"
            );
        }
    }

    private void updateFirstProgressBar(
        double progress
    ) {

        /*
         * Layout:
         *
         * 0 header
         * 1 CPU metric
         * 2 RAM metric
         * 3 GPU header
         * 4 GPU metric
         * ...
         *
         * The CPU ProgressBar is the first child of the
         * CPU metric VBox. We locate it safely.
         */
        if (
            getChildren().size() <= 1
        ) {
            return;
        }

        if (
            getChildren().get(1) instanceof VBox cpuBox
        ) {

            for (
                javafx.scene.Node child :
                cpuBox.getChildren()
            ) {

                if (
                    child instanceof ProgressBar bar
                ) {

                    bar.setProgress(
                        clampFraction(
                            progress
                        )
                    );

                    return;
                }
            }
        }
    }

    // =========================================================
    // LABELS
    // =========================================================

    private Label createValueLabel() {

        Label label =
            new Label(
                "--"
            );

        label.setStyle(
            "-fx-text-fill: " +
            Theme.text() +
            ";" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;"
        );

        return label;
    }

    private Label createPercentLabel() {

        Label label =
            new Label(
                "--"
            );

        label.setStyle(
            "-fx-text-fill: " +
            Theme.accent() +
            ";" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;"
        );

        return label;
    }

    private Label createStatusLabel() {

        Label label =
            new Label(
                "Checking..."
            );

        label.setStyle(
            "-fx-text-fill: " +
            Theme.muted() +
            ";" +
            "-fx-font-size: 9px;"
        );

        return label;
    }

    // =========================================================
    // FORMAT
    // =========================================================

    private String formatPercent(
        double percent
    ) {

        if (
            percent < 0
        ) {

            return "--";
        }

        return String.format(
            Locale.ROOT,
            "%.0f%%",
            percent
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

        double gigabytes =
            bytes /
            1024.0 /
            1024.0 /
            1024.0;

        return String.format(
            Locale.ROOT,
            "%.1f GB",
            gigabytes
        );
    }

    private double clampFraction(
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

        service.unsubscribe(
            listener
        );
    }
}
