package com.tidalai.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Conversation {

    private static final int MAX_MESSAGES =
        40;

    private final List<ChatMessage> messages =
        new ArrayList<>();

    public Conversation() {
    }

    public synchronized void addUserMessage(
        String content
    ) {

        if (
            content == null ||
            content.isBlank()
        ) {
            return;
        }

        messages.add(
            ChatMessage.user(
                content
            )
        );

        trim();
    }

    public synchronized void addAssistantMessage(
        String content
    ) {

        if (
            content == null ||
            content.isBlank()
        ) {
            return;
        }

        messages.add(
            ChatMessage.assistant(
                content
            )
        );

        trim();
    }

    public synchronized void addSystemMessage(
        String content
    ) {

        if (
            content == null ||
            content.isBlank()
        ) {
            return;
        }

        messages.add(
            ChatMessage.system(
                content
            )
        );

        trim();
    }

    public synchronized List<ChatMessage> getMessages() {

        return Collections.unmodifiableList(
            new ArrayList<>(
                messages
            )
        );
    }

    public synchronized void clear() {

        messages.clear();
    }

    public synchronized int size() {

        return messages.size();
    }

    private void trim() {

        while (
            messages.size() >
            MAX_MESSAGES
        ) {

            messages.remove(0);
        }
    }
}
