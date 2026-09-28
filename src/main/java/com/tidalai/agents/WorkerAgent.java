package com.tidalai.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tidalai.AppState;
import com.tidalai.models.AIModel;
import com.tidalai.providers.LMStudioProvider;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;

public class WorkerAgent {

    private static final int MAX_ACTIONS = 8;

    private final String name;
    private final String roleDescription;
    private final AgentRole role;

    private final AgentBus bus;
    private final LMStudioProvider provider;
    private final AgentToolExecutor tools;
    private final Set<String> allowedActions;

    private final ObjectMapper objectMapper =
        new ObjectMapper();

    private final ExecutorService executor =
        Executors.newSingleThreadExecutor(
            runnable -> {

                Thread thread =
                    new Thread(
                        runnable,
                        "tidalai-worker-agent"
                    );

                thread.setDaemon(true);

                return thread;
            }
        );

    public WorkerAgent(
        String name,
        String roleDescription,
        AgentRole role,
        AgentBus bus,
        LMStudioProvider provider,
        AgentToolExecutor tools,
        Set<String> allowedActions
    ) {

        this.name =
            name;

        this.roleDescription =
            roleDescription;

        this.role =
            role;

        this.bus =
            bus;

        this.provider =
            provider;

        this.tools =
            tools;

        this.allowedActions =
            allowedActions;

        bus.subscribe(
            this::handleMessage
        );
    }

    public String getName() {
        return name;
    }

    public AgentRole getRole() {
        return role;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    // =========================================================
    // BUS
    // =========================================================

    private void handleMessage(
        AgentMessage message
    ) {

        if (
            message == null ||
            !name.equals(
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
                    name,
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
                "ERROR: Empty task.";
        }

        if (
            cancelled.getAsBoolean()
        ) {

            return
                name +
                " stopped by user.";
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
         * Each worker gets its own model assignment:
         *
         * RESEARCHER
         * TESTER
         * REVIEWER
         *
         * If no model is assigned, AgentManager falls back
         * to the first available model.
         */
        String model =
            AppState.AGENTS.resolveModelForRole(
                role,
                models
            );

        if (
            model == null ||
            model.isBlank()
        ) {

            return
                "ERROR: Could not resolve a model for " +
                name +
                ".";
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
                "ROLE:\n"
            )
            .append(
                role.name()
            )
            .append(
                "\n\n"
            );

        history
            .append(
                "ROLE DESCRIPTION:\n"
            )
            .append(
                roleDescription
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
                    name +
                    " stopped by user.";
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
                    "ERROR: " +
                    name +
                    " AI request failed.\n\n" +
                    e.getMessage();
            }

            /*
             * Fallback for models that do not handle
             * structured output correctly.
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
                    "ERROR: " +
                    name +
                    " received an empty model response.";
            }

            ToolAction action;

            try {

                action =
                    objectMapper.readValue(
                        cleanJson(response),
                        ToolAction.class
                    );

            } catch (Exception e) {

                return
                    "ERROR: " +
                    name +
                    " returned invalid tool JSON.\n\n" +
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
                    "ERROR: " +
                    name +
                    " returned no action.";
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
                        ? name + " finished."
                        : reason;
            }

            if (
                !allowedActions.contains(
                    actionName
                )
            ) {

                ToolResult denied =
                    ToolResult.failure(
                        "Action '" +
                        actionName +
                        "' is not allowed for " +
                        name +
                        "."
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

            if (
                cancelled.getAsBoolean()
            ) {

                return
                    name +
                    " stopped by user.";
            }
        }

        return
            name +
            " stopped after " +
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

            /*
             * The worker agent should normally not have
             * write permissions. The allowedActions set
             * controls this, so these cases are still
             * supported by the executor when explicitly
             * enabled.
             */
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

            default ->
                ToolResult.failure(
                    "Unknown action: " +
                    actionName
                );
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
            You are the TidalAI %s.

            YOUR ROLE:
            %s

            IMPORTANT:
            You are a worker agent inside a larger multi-agent
            orchestration pipeline.

            Your job is to perform your assigned role and provide
            concrete information for the rest of the system.

            RULES:
            - Work only with the tools available to your role.
            - Stay inside the configured TidalAI workspace.
            - Inspect files before making conclusions about them.
            - Never invent file contents.
            - Do not modify files unless your allowed tool set explicitly
              contains a write operation.
            - Use run_command only when your role permits it.
            - Return EXACTLY ONE JSON object.
            - Do not use Markdown code fences.
            - Do not return explanations outside the JSON object.

            AVAILABLE ACTIONS:
            %s

            GENERAL ACTION FORMAT:

            {
              "action": "read_file",
              "path": "relative/or/absolute/path",
              "reason": "why"
            }

            OR:

            {
              "action": "list_files",
              "path": ".",
              "reason": "why"
            }

            OR:

            {
              "action": "run_command",
              "command": "mvn test",
              "reason": "why"
            }

            OR:

            {
              "action": "done",
              "reason": "summary of your findings"
            }

            IMPORTANT:
            You can perform only ONE tool action per response.
            After receiving the result, decide on the next action.

            ACTION NUMBER:
            %d

            CURRENT WORK HISTORY:
            %s
            """.formatted(
                AgentManager.getAgentDisplayName(role),
                roleDescription,
                buildAllowedActionText(),
                actionNumber,
                history
            );
    }

    private String buildAllowedActionText() {

        if (
            allowedActions == null ||
            allowedActions.isEmpty()
        ) {

            return
                "- done";
        }

        StringBuilder builder =
            new StringBuilder();

        for (
            String action :
            allowedActions
        ) {

            builder
                .append(
                    "- "
                )
                .append(
                    action
                )
                .append(
                    "\n"
                );
        }

        builder
            .append(
                "- done"
            );

        return builder.toString();
    }

    // =========================================================
    // STRUCTURED OUTPUT
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
            .putObject("action")
            .put("type", "string");

        properties
            .putObject("path")
            .put("type", "string");

        properties
            .putObject("content")
            .put("type", "string");

        properties
            .putObject("search")
            .put("type", "string");

        properties
            .putObject("replacement")
            .put("type", "string");

        properties
            .putObject("command")
            .put("type", "string");

        properties
            .putObject("reason")
            .put("type", "string");

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

        int maxLength =
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