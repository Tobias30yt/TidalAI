package com.tidalai.agents;

public class ToolRequest {

    private final ToolType type;
    private final String target;
    private final String argument;
    private final String reason;

    public ToolRequest(
        ToolType type,
        String target,
        String argument,
        String reason
    ) {

        this.type = type;
        this.target = target;
        this.argument = argument;
        this.reason = reason;
    }

    public ToolType getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public String getArgument() {
        return argument;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {

        return type +
            " | target=" +
            target +
            " | argument=" +
            argument +
            " | reason=" +
            reason;
    }
}
