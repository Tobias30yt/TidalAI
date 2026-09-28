package com.tidalai.tasks;

import com.tidalai.AppState;

import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;

public class AgentTaskService {

    public TaskExecution createAndRun(
        String description
    ) {

        if (
            description == null ||
            description.isBlank()
        ) {

            throw new IllegalArgumentException(
                "Task description cannot be empty."
            );
        }

        Task task =
            AppState.TASKS.createTask(
                description.trim()
            );

        runOnJavaFxThread(() ->
            AppState.TASKS.startTask(task)
        );

        CompletableFuture<String> future =
            AppState.ORCHESTRATOR.execute(
                task.getDescription(),
                task::isStopRequested
            );

        future.whenComplete(
            (result, error) -> {

                runOnJavaFxThread(() -> {

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
                });
            }
        );

        return new TaskExecution(
            task,
            future
        );
    }

    public void stopTask(
        Task task
    ) {

        if (task == null) {
            return;
        }

        AppState.TASKS.stopTask(
            task
        );
    }

    private void runOnJavaFxThread(
        Runnable action
    ) {

        if (
            Platform.isFxApplicationThread()
        ) {

            action.run();

        } else {

            Platform.runLater(action);
        }
    }
}