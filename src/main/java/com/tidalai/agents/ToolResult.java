package com.tidalai.agents;

public class ToolResult {

    private final boolean success;
    private final String output;

    public ToolResult(
        boolean success,
        String output
    ) {

        this.success = success;
        this.output = output;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getOutput() {
        return output;
    }

    public static ToolResult success(
        String output
    ) {

        return new ToolResult(
            true,
            output
        );
    }

    public static ToolResult failure(
        String output
    ) {

        return new ToolResult(
            false,
            output
        );
    }

    @Override
    public String toString() {

        return output;
    }
}
