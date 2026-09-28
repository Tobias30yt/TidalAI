package com.tidalai.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tidalai.AppState;
import com.tidalai.models.AIModel;
import com.tidalai.providers.LMStudioProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;

public class CoderAgent {

    private static final int MAX_ACTIONS = 12;

    private final AgentBus bus;
    private final LMStudioProvider provider;
    private final AgentToolExecutor tools;
    private final AgentConfig config;

    private final ObjectMapper objectMapper =
        new ObjectMapper();

    private final ExecutorService executor =
        Executors.newSingleThreadExecutor(
            runnable -> {

                Thread thread =
                    new Thread(
                        runnable,
                        "tidalai-coder-agent"
                    );

                thread.setDaemon(true);

                return thread;
            }
        );

    public CoderAgent(
        AgentBus bus,
        LMStudioProvider provider,
        AgentToolExecutor tools,
        AgentConfig config
    ) {

        this.bus =
            bus;

        this.provider =
            provider;

        this.tools =
            tools;

        this.config =
            config;

        /*
         * The Coder can still receive direct agent messages.
         *
         * The orchestrator normally calls execute(...) directly.
         * Do not send the same orchestration step through the bus
         * at the same time, otherwise it could be executed twice.
         */
        bus.subscribe(
            this::handleMessage
        );
    }

    public String getName() {
        return "Coder Agent";
    }

    public AgentRole getRole() {
        return AgentRole.CODER;
    }

    public AgentConfig getConfig() {
        return config;
    }

    // =========================================================
    // BUS
    // =========================================================

    private void handleMessage(
        AgentMessage message
    ) {

        if (
            message == null ||
            !"Coder Agent".equals(
                message.getReceiver()
            )
        ) {

            return;
        }

        executor.submit(
            () -> {

                String result =
                    processInternal(
                        message.getContent(),
                        () -> false
                    );

                bus.send(
                    getName(),
                    MainAgent.NAME,
                    result
                );
            }
        );
    }

    // =========================================================
    // EXECUTION API
    // =========================================================

    public CompletableFuture<String> execute(
        String task
    ) {

        return execute(
            task,
            () -> false
        );
    }

    public CompletableFuture<String> execute(
        String task,
        BooleanSupplier cancelled
    ) {

        BooleanSupplier cancelCheck =
            cancelled != null
                ? cancelled
                : () -> false;

        return CompletableFuture.supplyAsync(
            () ->
                processInternal(
                    task,
                    cancelCheck
                ),
            executor
        );
    }

    // =========================================================
    // MAIN PROCESS
    // =========================================================

    private String processInternal(
        String task,
        BooleanSupplier cancelled
    ) {

        if (
            task == null ||
            task.isBlank()
        ) {

            return
                "ERROR: Empty coding task.";
        }

        if (
            cancelled.getAsBoolean()
        ) {

            return
                "Coder Agent stopped by user.";
        }

        if (
            !AppState.SANDBOX.isConfigured()
        ) {

            return
                "ERROR: No sandbox workspace configured.";
        }

        List<AIModel> models =
            provider.getModels();

        if (
            models.isEmpty()
        ) {

            return
                "ERROR: No AI model available in LM Studio.";
        }

        /*
         * IMPORTANT:
         *
         * The Coder does not simply use models.get(0).
         *
         * It uses the model assigned to CODER in
         * AppState.AGENTS. If none is assigned or the
         * configured model is unavailable, AgentManager
         * falls back to the first available model.
         */
        String model =
            AppState.AGENTS.resolveModelForRole(
                AgentRole.CODER,
                models
            );

        if (
            model == null ||
            model.isBlank()
        ) {

            return
                "ERROR: Could not resolve a model for Coder Agent.";
        }

        StringBuilder history =
            new StringBuilder();

        history
            .append(
                "TASK:\n"
            )
            .append(
                task
            )
            .append(
                "\n\n"
            );

        history
            .append(
                "WORKSPACE:\n"
            )
            .append(
                AppState.SANDBOX
                    .getRoot()
                    .toAbsolutePath()
            )
            .append(
                "\n\n"
            );

        history
            .append(
                "ASSIGNED MODEL:\n"
            )
            .append(
                model
            )
            .append(
                "\n\n"
            );

        for (
            int actionNumber = 1;
            actionNumber <= MAX_ACTIONS;
            actionNumber++
        ) {

            if (
                cancelled.getAsBoolean()
            ) {

                return
                    "Coder Agent stopped by user.";
            }

            String prompt =
                buildPrompt(
                    history.toString(),
                    actionNumber
                );

            String response;

            try {

                response =
                    provider.chatStructured(
                        model,
                        prompt,
                        buildSchema()
                    );

            } catch (Exception e) {

                return
                    "ERROR: Coder Agent AI request failed.\n\n" +
                    e.getMessage();
            }

            /*
             * Structured output may not be available for every
             * local model. Fall back to normal chat in that case.
             */
            if (
                response == null ||
                response.isBlank() ||
                response.startsWith(
                    "LM Studio structured-output error:"
                )
            ) {

                response =
                    provider.chat(
                        model,
                        prompt
                    );
            }

            if (
                response == null ||
                response.isBlank()
            ) {

                return
                    "ERROR: Coder Agent received an empty model response.";
            }

            ToolAction action;

            try {

                String cleaned =
                    cleanJson(
                        response
                    );

                action =
                    objectMapper.readValue(
                        cleaned,
                        ToolAction.class
                    );

            } catch (Exception e) {

                return
                    "ERROR: Coder Agent returned invalid tool JSON.\n\n" +
                    "RAW RESPONSE:\n" +
                    response;
            }

            String actionName =
                action.getAction();

            if (
                actionName == null ||
                actionName.isBlank()
            ) {

                return
                    "ERROR: Coder Agent returned no action.";
            }

            actionName =
                actionName
                    .trim()
                    .toLowerCase();

            if (
                "done".equals(
                    actionName
                )
            ) {

                String reason =
                    action.getReason();

                return
                    reason == null ||
                    reason.isBlank()
                        ? "Coder Agent finished."
                        : reason;
            }

            if (
                !isAllowedAction(
                    actionName
                )
            ) {

                ToolResult denied =
                    ToolResult.failure(
                        "Action '" +
                        actionName +
                        "' is not allowed for Coder Agent."
                    );

                appendHistory(
                    history,
                    actionName,
                    action,
                    denied
                );

                continue;
            }

            action.setAction(
                actionName
            );

            ToolResult result;

            try {

                result =
                    executeAction(
                        action,
                        cancelled
                    );

            } catch (Exception e) {

                result =
                    ToolResult.failure(
                        "Tool execution error: " +
                        e.getMessage()
                    );
            }

            appendHistory(
                history,
                actionName,
                action,
                result
            );

            /*
             * Stop immediately when the workspace operation
             * reports cancellation.
             */
            if (
                cancelled.getAsBoolean()
            ) {

                return
                    "Coder Agent stopped by user.";
            }
        }

        return
            "Coder Agent stopped after " +
            MAX_ACTIONS +
            " actions.";
    }

    // =========================================================
    // TOOL EXECUTION
    // =========================================================

    private ToolResult executeAction(
        ToolAction action,
        BooleanSupplier cancelled
    ) {

        String actionName =
            action.getAction()
                .trim()
                .toLowerCase();

        return switch (actionName) {

            case "read_file" -> {

                String path =
                    requireValue(
                        action.getPath(),
                        "path"
                    );

                yield tools.readFile(
                    path
                );
            }

            case "write_file" -> {

                String path =
                    requireValue(
                        action.getPath(),
                        "path"
                    );

                String content =
                    action.getContent();

                if (
                    content == null
                ) {

                    content = "";
                }

                yield tools.writeFile(
                    path,
                    content,
                    action.getReason()
                );
            }

            case "replace_file" -> {

                String path =
                    requireValue(
                        action.getPath(),
                        "path"
                    );

                String search =
                    requireValue(
                        action.getSearch(),
                        "search"
                    );

                String replacement =
                    action.getReplacement();

                if (
                    replacement == null
                ) {

                    replacement = "";
                }

                yield tools.replaceInFile(
                    path,
                    search,
                    replacement,
                    action.getReason()
                );
            }

            case "list_files" -> {

                String path =
                    action.getPath();

                if (
                    path == null ||
                    path.isBlank()
                ) {

                    path = ".";
                }

                yield tools.listFiles(
                    path
                );
            }

            case "run_command" -> {

                String command =
                    requireValue(
                        action.getCommand(),
                        "command"
                    );

                yield tools.runCommand(
                    command,
                    action.getReason(),
                    cancelled
                );
            }

            default ->
                ToolResult.failure(
                    "Unknown action: " +
                    actionName
                );
        };
    }

    private boolean isAllowedAction(
        String action
    ) {

        return switch (action) {

            case "read_file",
                 "write_file",
                 "replace_file",
                 "list_files",
                 "run_command" ->
                true;

            default ->
                false;
        };
    }

    // =========================================================
    // PROMPT
    // =========================================================

    private String buildPrompt(
        String history,
        int actionNumber
    ) {

        return """
            You are the TidalAI Coder Agent.

            ROLE:
            You are responsible for implementing the requested coding task
            inside the configured workspace.

            RULES:
            - Work only inside the configured TidalAI workspace.
            - Inspect existing files before modifying them.
            - Preserve working code unless the requested task requires changes.
            - Prefer precise edits over unnecessary rewrites.
            - Use read_file before replacing unfamiliar code.
            - Use list_files when you need to discover the project structure.
            - Use run_command for appropriate build/test commands.
            - After changes, verify the implementation when practical.
            - Never invent file contents you have not inspected when inspection
              is needed.
            - Return EXACTLY ONE JSON object.
            - Do not use Markdown code fences.
            - Do not return explanations outside the JSON object.

            AVAILABLE ACTIONS:

            1. read_file
               {
                 "action": "read_file",
                 "path": "relative/or/absolute/path",
                 "reason": "why"
               }

            2. write_file
               {
                 "action": "write_file",
                 "path": "relative/path",
                 "content": "complete file content",
                 "reason": "why"
               }

            3. replace_file
               {
                 "action": "replace_file",
                 "path": "relative/path",
                 "search": "exact existing text",
                 "replacement": "new text",
                 "reason": "why"
               }

            4. list_files
               {
                 "action": "list_files",
                 "path": ".",
                 "reason": "why"
               }

            5. run_command
               {
                 "action": "run_command",
                 "command": "mvn test",
                 "reason": "why"
               }

            6. done
               {
                 "action": "done",
                 "reason": "summary of completed work"
               }

            IMPORTANT:
            You can perform only ONE tool action per response.
            After receiving the tool result, decide on the next action.

            ACTION NUMBER:
            %d

            CURRENT WORK HISTORY:
            %s
            """.formatted(
                actionNumber,
                history
            );
    }

    // =========================================================
    // STRUCTURED OUTPUT SCHEMA
    // =========================================================

    private JsonNode buildSchema() {

        ObjectNode root =
            objectMapper.createObjectNode();

        root.put(
            "type",
            "object"
        );

        root.put(
            "additionalProperties",
            false
        );

        ObjectNode properties =
            root.putObject(
                "properties"
            );

        properties
            .putObject(
                "action"
            )
            .put(
                "type",
                "string"
            );

        properties
            .putObject(
                "path"
            )
            .put(
                "type",
                "string"
            );

        properties
            .putObject(
                "content"
            )
            .put(
                "type",
                "string"
            );

        properties
            .putObject(
                "search"
            )
            .put(
                "type",
                "string"
            );

        properties
            .putObject(
                "replacement"
            )
            .put(
                "type",
                "string"
            );

        properties
            .putObject(
                "command"
            )
            .put(
                "type",
                "string"
            );

        properties
            .putObject(
                "reason"
            )
            .put(
                "type",
                "string"
            );

        ArrayNode required =
            root.putArray(
                "required"
            );

        required.add(
            "action"
        );

        required.add(
            "reason"
        );

        return root;
    }

    // =========================================================
    // HISTORY
    // =========================================================

    private void appendHistory(
        StringBuilder history,
        String actionName,
        ToolAction action,
        ToolResult result
    ) {

        history
            .append(
                "\n\nACTION:\n"
            )
            .append(
                actionName
            )
            .append(
                "\n"
            );

        if (
            action.getPath() != null
        ) {

            history
                .append(
                    "PATH:\n"
                )
                .append(
                    action.getPath()
                )
                .append(
                    "\n"
                );
        }

        if (
            action.getCommand() != null
        ) {

            history
                .append(
                    "COMMAND:\n"
                )
                .append(
                    action.getCommand()
                )
                .append(
                    "\n"
                );
        }

        if (
            action.getReason() != null
        ) {

            history
                .append(
                    "REASON:\n"
                )
                .append(
                    action.getReason()
                )
                .append(
                    "\n"
                );
        }

        history
            .append(
                "TOOL SUCCESS:\n"
            )
            .append(
                result.isSuccess()
            )
            .append(
                "\n"
            );

        history
            .append(
                "TOOL RESULT:\n"
            )
            .append(
                limitHistory(
                    result.getOutput()
                )
            )
            .append(
                "\n"
            );
    }

    private String limitHistory(
        String text
    ) {

        if (
            text == null
        ) {

            return "";
        }

        final int maxLength =
            12000;

        if (
            text.length() <= maxLength
        ) {

            return text;
        }

        return
            text.substring(
                0,
                maxLength
            ) +
            "\n\n[Tool output truncated]";
    }

    // =========================================================
    // JSON CLEANUP
    // =========================================================

    private String cleanJson(
        String response
    ) {

        String cleaned =
            response.trim();

        if (
            cleaned.startsWith(
                "```"
            )
        ) {

            int firstNewline =
                cleaned.indexOf(
                    '\n'
                );

            if (
                firstNewline >= 0
            ) {

                cleaned =
                    cleaned.substring(
                        firstNewline + 1
                    );
            }

            int lastFence =
                cleaned.lastIndexOf(
                    "```"
                );

            if (
                lastFence >= 0
            ) {

                cleaned =
                    cleaned.substring(
                        0,
                        lastFence
                    );
            }
        }

        /*
         * Some models occasionally return additional text
         * around the JSON object. Try to isolate the first
         * complete-looking object.
         */
        int firstBrace =
            cleaned.indexOf(
                '{'
            );

        int lastBrace =
            cleaned.lastIndexOf(
                '}'
            );

        if (
            firstBrace >= 0 &&
            lastBrace > firstBrace
        ) {

            cleaned =
                cleaned.substring(
                    firstBrace,
                    lastBrace + 1
                );
        }

        return cleaned.trim();
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private String requireValue(
        String value,
        String field
    ) {

        if (
            value == null ||
            value.isBlank()
        ) {

            throw new IllegalArgumentException(
                "Missing required field: " +
                field
            );
        }

        return value;
    }

    // =========================================================
    // SHUTDOWN
    // =========================================================

    public void shutdown() {

        executor.shutdownNow();
    }
}