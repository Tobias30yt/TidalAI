package com.tidalai.agents;

public class MainAgent {

    public static final String NAME =
        "Main Agent";

    private final AgentBus bus;
    private final AgentConfig config;

    public MainAgent(
        AgentBus bus,
        AgentConfig config
    ) {

        this.bus = bus;
        this.config = config;
    }

    public void submitTask(
        String task
    ) {

        if (
            task == null ||
            task.isBlank()
        ) {
            return;
        }

        bus.send(
            NAME,
            ReasoningAgent.NAME,
            task
        );
    }

    public AgentConfig getConfig() {
        return config;
    }
}
