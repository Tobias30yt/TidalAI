package com.tidalai.agents;

import java.util.function.BooleanSupplier;

public class AgentToolExecutor {

    private final SandboxManager sandbox;

    public AgentToolExecutor(
        SandboxManager sandbox
    ) {

        this.sandbox = sandbox;
    }

    public ToolResult readFile(
        String path
    ) {

        return sandbox.readFile(
            path
        );
    }

    public ToolResult writeFile(
        String path,
        String content,
        String reason
    ) {

        return sandbox.writeFile(
            path,
            content,
            reason
        );
    }

    public ToolResult replaceInFile(
        String path,
        String search,
        String replacement,
        String reason
    ) {

        return sandbox.replaceInFile(
            path,
            search,
            replacement,
            reason
        );
    }

    public ToolResult listFiles(
        String directory
    ) {

        return sandbox.listFiles(
            directory
        );
    }

    public ToolResult runCommand(
        String command,
        String reason
    ) {

        return sandbox.runCommand(
            command,
            reason
        );
    }

    public ToolResult runCommand(
        String command,
        String reason,
        BooleanSupplier cancelled
    ) {

        return sandbox.runCommand(
            command,
            reason,
            cancelled
        );
    }
}
