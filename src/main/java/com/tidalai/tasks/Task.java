package com.tidalai.tasks;

import com.tidalai.chat.Conversation;

import java.time.LocalDateTime;
import java.util.UUID;

public class Task {

    private final String id;
    private final String description;
    private final LocalDateTime createdAt;

    private volatile TaskStatus status;
    private volatile boolean stopRequested;

    private volatile String projectId;

    private final Conversation conversation =
        new Conversation();

    public Task(
        String description
    ) {

        this(
            UUID.randomUUID().toString(),
            description,
            LocalDateTime.now(),
            TaskStatus.QUEUED,
            false,
            null
        );
    }

    public Task(
        String id,
        String description,
        LocalDateTime createdAt,
        TaskStatus status,
        boolean stopRequested,
        String projectId
    ) {

        this.id =
            id == null || id.isBlank()
                ? UUID.randomUUID().toString()
                : id;

        this.description =
            description == null
                ? ""
                : description;

        this.createdAt =
            createdAt == null
                ? LocalDateTime.now()
                : createdAt;

        this.status =
            status == null
                ? TaskStatus.QUEUED
                : status;

        this.stopRequested =
            stopRequested;

        this.projectId =
            projectId;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(
        TaskStatus status
    ) {

        if (
            status != null
        ) {

            this.status =
                status;
        }
    }

    public boolean isStopRequested() {
        return stopRequested;
    }

    public void requestStop() {
        stopRequested = true;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(
        String projectId
    ) {

        this.projectId =
            projectId;
    }

    public Conversation getConversation() {
        return conversation;
    }
}
