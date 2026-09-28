package com.tidalai.agents;

import java.time.LocalDateTime;
import java.util.UUID;

public class AgentMessage {

    private final String id;
    private final String sender;
    private final String receiver;
    private final String content;
    private final LocalDateTime timestamp;

    public AgentMessage(
        String sender,
        String receiver,
        String content
    ) {

        this.id =
            UUID.randomUUID().toString();

        this.sender = sender;
        this.receiver = receiver;
        this.content = content;

        this.timestamp =
            LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {

        return "[" +
            timestamp +
            "] " +
            sender +
            " -> " +
            receiver +
            ": " +
            content;
    }
}
