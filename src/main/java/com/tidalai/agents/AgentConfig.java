package com.tidalai.agents;

public enum AgentConfig {

    BEEFY(
        "Beefy",
        5,
        3
    ),

    STANDARD(
        "Standard",
        3,
        2
    ),

    BARE_MINIMUM(
        "Bare Minimum",
        2,
        1
    );

    private final String displayName;
    private final int maxAgents;
    private final int maxParallelAgents;

    AgentConfig(
        String displayName,
        int maxAgents,
        int maxParallelAgents
    ) {

        this.displayName = displayName;
        this.maxAgents = maxAgents;
        this.maxParallelAgents = maxParallelAgents;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxAgents() {
        return maxAgents;
    }

    public int getMaxParallelAgents() {
        return maxParallelAgents;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
