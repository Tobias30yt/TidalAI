package com.tidalai.chat;

import com.tidalai.AppState;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ChatManager {

    private final ObservableList<ChatSession> chats =
        FXCollections.observableArrayList();

    public ObservableList<ChatSession> getChats() {
        return chats;
    }

    public ChatSession createChat() {

        ChatSession chat =
            new ChatSession(
                "New Chat"
            );

        chats.add(
            0,
            chat
        );

        AppState.saveAll();

        return chat;
    }

    public ChatSession createChat(
        String title,
        String modelId
    ) {

        ChatSession chat =
            new ChatSession(
                title
            );

        chat.setModelId(
            modelId
        );

        chats.add(
            0,
            chat
        );

        AppState.saveAll();

        return chat;
    }

    public void deleteChat(
        ChatSession chat
    ) {

        if (
            chat == null
        ) {
            return;
        }

        chats.remove(
            chat
        );

        if (
            chats.isEmpty()
        ) {

            chats.add(
                new ChatSession(
                    "New Chat"
                )
            );
        }

        AppState.saveAll();
    }

    public ChatSession findById(
        String id
    ) {

        if (
            id == null ||
            id.isBlank()
        ) {

            return null;
        }

        for (
            ChatSession chat :
            chats
        ) {

            if (
                id.equals(
                    chat.getId()
                )
            ) {

                return chat;
            }
        }

        return null;
    }

    public void restore(
        List<ChatSession> restored
    ) {

        chats.clear();

        if (
            restored != null
        ) {

            chats.addAll(
                restored
            );
        }

        if (
            chats.isEmpty()
        ) {

            chats.add(
                new ChatSession(
                    "New Chat"
                )
            );
        }
    }

    public void save() {
        AppState.saveAll();
    }
}
