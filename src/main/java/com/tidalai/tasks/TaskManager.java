package com.tidalai.tasks;

import com.tidalai.AppState;
import com.tidalai.projects.Project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class TaskManager {

    private final ObservableList<Task> tasks =
        FXCollections.observableArrayList();

    public ObservableList<Task> getTasks() {
        return tasks;
    }

    public Task createTask(
        String description
    ) {

        Task task =
            new Task(
                description
            );

        tasks.add(
            task
        );

        AppState.saveAll();

        return task;
    }

    public Task createTask(
        String description,
        Project project
    ) {

        Task task =
            createTask(
                description
            );

        assignToProject(
            task,
            project
        );

        return task;
    }

    public void assignToProject(
        Task task,
        Project project
    ) {

        if (
            task == null
        ) {
            return;
        }

        task.setProjectId(
            project == null
                ? null
                : project.getId()
        );

        AppState.saveAll();
    }

    public void removeFromProject(
        Task task
    ) {

        if (
            task == null
        ) {
            return;
        }

        task.setProjectId(
            null
        );

        AppState.saveAll();
    }

    public void startTask(
        Task task
    ) {

        if (
            task == null ||
            task.isStopRequested()
        ) {
            return;
        }

        task.setStatus(
            TaskStatus.RUNNING
        );

        AppState.saveAll();
    }

    public void completeTask(
        Task task
    ) {

        if (
            task != null &&
            !task.isStopRequested()
        ) {

            task.setStatus(
                TaskStatus.COMPLETED
            );

            AppState.saveAll();
        }
    }

    public void failTask(
        Task task
    ) {

        if (
            task != null &&
            !task.isStopRequested()
        ) {

            task.setStatus(
                TaskStatus.FAILED
            );

            AppState.saveAll();
        }
    }

    public void stopTask(
        Task task
    ) {

        if (
            task == null
        ) {
            return;
        }

        task.requestStop();

        task.setStatus(
            TaskStatus.STOPPED
        );

        AppState.saveAll();
    }

    public void restore(
        List<Task> restored
    ) {

        tasks.clear();

        if (
            restored != null
        ) {

            tasks.addAll(
                restored
            );
        }
    }
}
