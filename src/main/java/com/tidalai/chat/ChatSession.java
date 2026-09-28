package com.tidalai.chat;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChatSession {

    private final String id;
    private String title;
    private String modelId;
    private final LocalDateTime createdAt;

    private final Conversation conversation =
        new Conversation();

    public ChatSession(
        String title
    ) {

        this(
            UUID.randomUUID().toString(),
            title,
            null,
            LocalDateTime.now()
        );
    }

    public ChatSession(
        String id,
        String title,
        String modelId,
        LocalDateTime createdAt
    ) {

        this.id =
            id == null || id.isBlank()
                ? UUID.randomUUID().toString()
                : id;

        this.title =
            title == null || title.isBlank()
                ? "New Chat"
                : title;

        this.modelId =
            modelId == null || modelId.isBlank()
                ? null
                : modelId;

        this.createdAt =
            createdAt == null
                ? LocalDateTime.now()
                : createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
        String title
    ) {

        if (
            title != null &&
            !title.isBlank()
        ) {

            this.title =
                title.trim();
        }
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(
        String modelId
    ) {

        this.modelId =
            modelId == null || modelId.isBlank()
                ? null
                : modelId.trim();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public boolean isUntitled() {

        return
            title == null ||
            title.isBlank() ||
            title.equalsIgnoreCase(
                "New Chat"
            );
    }

    @Override
    public String toString() {
        return title;
    }
}
