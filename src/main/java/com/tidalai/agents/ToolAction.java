package com.tidalai.agents;

import java.util.HashMap;
import java.util.Map;

public class ToolAction {

    private String action;

    private String path;

    private String content;

    private String search;

    private String replacement;

    private String command;

    private String reason;

    public ToolAction() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(
        String action
    ) {

        this.action = action;
    }

    public String getPath() {
        return path;
    }

    public void setPath(
        String path
    ) {

        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(
        String content
    ) {

        this.content = content;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(
        String search
    ) {

        this.search = search;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(
        String replacement
    ) {

        this.replacement = replacement;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(
        String command
    ) {

        this.command = command;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(
        String reason
    ) {

        this.reason = reason;
    }

    public Map<String, Object> toMap() {

        Map<String, Object> map =
            new HashMap<>();

        if (action != null) {
            map.put("action", action);
        }

        if (path != null) {
            map.put("path", path);
        }

        if (content != null) {
            map.put("content", content);
        }

        if (search != null) {
            map.put("search", search);
        }

        if (replacement != null) {
            map.put(
                "replacement",
                replacement
            );
        }

        if (command != null) {
            map.put(
                "command",
                command
            );
        }

        if (reason != null) {
            map.put("reason", reason);
        }

        return map;
    }
}
