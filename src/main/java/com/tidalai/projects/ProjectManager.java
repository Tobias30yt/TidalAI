package com.tidalai.projects;

import com.tidalai.AppState;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ProjectManager {

    private final ObservableList<Project> projects =
        FXCollections.observableArrayList();

    public ObservableList<Project> getProjects() {
        return projects;
    }

    public Project createProject(
        String name,
        String description
    ) {

        if (
            name == null ||
            name.isBlank()
        ) {

            throw new IllegalArgumentException(
                "Project name cannot be empty."
            );
        }

        Project project =
            new Project(
                name.trim(),
                description == null
                    ? ""
                    : description.trim()
            );

        projects.add(
            project
        );

        AppState.saveAll();

        return project;
    }

    public void deleteProject(
        Project project
    ) {

        if (
            project == null
        ) {
            return;
        }

        projects.remove(
            project
        );

        AppState.saveAll();
    }

    public Project findById(
        String id
    ) {

        if (
            id == null ||
            id.isBlank()
        ) {

            return null;
        }

        for (
            Project project :
            projects
        ) {

            if (
                id.equals(
                    project.getId()
                )
            ) {

                return project;
            }
        }

        return null;
    }

    public void restore(
        List<Project> restored
    ) {

        projects.clear();

        if (
            restored != null
        ) {

            projects.addAll(
                restored
            );
        }
    }
}
