package com.tidalai.agents;

import java.util.UUID;

public class Agent {

    private final String id;
    private final String name;
    private final AgentRole role;

    public Agent(
        String name,
        AgentRole role
    ) {

        this(
            UUID.randomUUID().toString(),
            name,
            role
        );
    }

    public Agent(
        String id,
        String name,
        AgentRole role
    ) {

        this.id =
            id == null || id.isBlank()
                ? UUID.randomUUID().toString()
                : id;

        this.name =
            name == null || name.isBlank()
                ? "Unnamed Agent"
                : name;

        this.role =
            role == null
                ? AgentRole.CODER
                : role;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AgentRole getRole() {
        return role;
    }

    @Override
    public String toString() {
        return name;
    }
}
