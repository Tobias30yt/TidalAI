package com.tidalai.agents;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class AgentBus {

    private final List<AgentMessage> messages =
        new CopyOnWriteArrayList<>();

    private final List<Consumer<AgentMessage>> listeners =
        new CopyOnWriteArrayList<>();

    public void send(
        AgentMessage message
    ) {

        messages.add(message);

        for (
            Consumer<AgentMessage> listener :
            listeners
        ) {

            listener.accept(message);
        }
    }

    public void send(
        String sender,
        String receiver,
        String content
    ) {

        send(
            new AgentMessage(
                sender,
                receiver,
                content
            )
        );
    }

    public void subscribe(
        Consumer<AgentMessage> listener
    ) {

        listeners.add(listener);
    }

    public void unsubscribe(
        Consumer<AgentMessage> listener
    ) {
        listeners.remove(listener);
    }

    public List<AgentMessage> getMessages() {

        return List.copyOf(messages);
    }

    public void clear() {

        messages.clear();
    }
}
