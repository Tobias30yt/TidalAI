package com.tidalai.projects;

import java.time.LocalDateTime;
import java.util.UUID;

public class Project {

    private final String id;
    private String name;
    private String description;
    private final LocalDateTime createdAt;

    public Project(
        String name,
        String description
    ) {

        this(
            UUID.randomUUID().toString(),
            name,
            description,
            LocalDateTime.now()
        );
    }

    public Project(
        String id,
        String name,
        String description,
        LocalDateTime createdAt
    ) {

        this.id =
            id == null || id.isBlank()
                ? UUID.randomUUID().toString()
                : id;

        this.name =
            name == null || name.isBlank()
                ? "Unnamed Project"
                : name;

        this.description =
            description == null
                ? ""
                : description;

        this.createdAt =
            createdAt == null
                ? LocalDateTime.now()
                : createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(
        String name
    ) {

        if (
            name != null &&
            !name.isBlank()
        ) {

            this.name =
                name.trim();
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
        String description
    ) {

        this.description =
            description == null
                ? ""
                : description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return name;
    }
}
