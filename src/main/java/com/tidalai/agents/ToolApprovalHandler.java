package com.tidalai.agents;

@FunctionalInterface
public interface ToolApprovalHandler {

    boolean approve(
        ToolRequest request
    );
}
