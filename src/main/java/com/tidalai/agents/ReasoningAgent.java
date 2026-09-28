package com.tidalai.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tidalai.models.AIModel;
import com.tidalai.providers.LMStudioProvider;

import java.util.List;

public class ReasoningAgent {

    public static final String NAME =
        "Reasoning Agent";

    private final AgentBus bus;
    private final LMStudioProvider provider;
    private final AgentConfig config;

    private final ObjectMapper objectMapper =
        new ObjectMapper();

    public ReasoningAgent(
        AgentBus bus,
        LMStudioProvider provider,
        AgentConfig config
    ) {

        this.bus = bus;
        this.provider = provider;
        this.config = config;

        bus.subscribe(
            this::handleMessage
        );
    }

    private void handleMessage(
        AgentMessage message
    ) {

        if (
            !NAME.equals(
                message.getReceiver()
            )
        ) {
            return;
        }

        if (
            !MainAgent.NAME.equals(
                message.getSender()
            )
        ) {
            return;
        }

        processTask(
            message.getContent()
        );
    }

    private void processTask(
        String task
    ) {

        List<AIModel> models =
            provider.getModels();

        if (
            models.isEmpty()
        ) {

            bus.send(
                NAME,
                MainAgent.NAME,
                "ERROR: No AI model available in LM Studio."
            );

            return;
        }

        String model =
            models.get(0).getId();

        String prompt =
            buildReasoningPrompt(
                task
            );

        String response =
            provider.chatStructured(
                model,
                prompt,
                buildAgentPlanSchema()
            );

        // If structured output fails completely,
        // fall back to normal chat.
        if (
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

        String cleaned =
            cleanJsonResponse(
                response
            );

        try {

            AgentPlan plan =
                objectMapper.readValue(
                    cleaned,
                    AgentPlan.class
                );

            String json =
                objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(
                        plan
                    );

            bus.send(
                NAME,
                MainAgent.NAME,
                json
            );

        } catch (Exception e) {

            bus.send(
                NAME,
                MainAgent.NAME,
                "ERROR: Could not parse reasoning plan.\n\n" +
                e.getMessage() +
                "\n\nRAW MODEL RESPONSE:\n" +
                response
            );
        }
    }

    private String buildReasoningPrompt(
        String task
    ) {

        return """
            You are the Reasoning Agent of TidalAI.

            Analyze the user's task and create a precise
            execution plan for other AI agents.

            Available agents:
            - Researcher
            - Coder
            - Tester
            - Reviewer

            Agent configuration:
            %s

            Maximum agents:
            %d

            Maximum parallel agents:
            %d

            The response must be a JSON object matching
            the provided JSON schema.

            Do not use Markdown.
            Do not use code fences.
            Do not add explanations outside the JSON.

            User task:
            %s
            """.formatted(
                config.getDisplayName(),
                config.getMaxAgents(),
                config.getMaxParallelAgents(),
                task
            );
    }

    // =========================================================
    // JSON SCHEMA
    // =========================================================

    private JsonNode buildAgentPlanSchema() {

        var root =
            objectMapper.createObjectNode();

        root.put(
            "type",
            "object"
        );

        var properties =
            root.putObject(
                "properties"
            );

        properties.putObject(
            "summary"
        )
        .put(
            "type",
            "string"
        );

        var steps =
            properties.putObject(
                "steps"
            );

        steps.put(
            "type",
            "array"
        );

        steps.put(
            "minItems",
            1
        );

        var stepItem =
            steps.putObject(
                "items"
            );

        stepItem.put(
            "type",
            "object"
        );

        var stepProperties =
            stepItem.putObject(
                "properties"
            );

        stepProperties.putObject(
            "step"
        )
        .put(
            "type",
            "integer"
        );

        stepProperties.putObject(
            "responsibleAgent"
        )
        .put(
            "type",
            "string"
        );

        stepProperties.putObject(
            "objective"
        )
        .put(
            "type",
            "string"
        );

        stepProperties.putObject(
            "requiredInput"
        )
        .put(
            "type",
            "string"
        );

        stepProperties.putObject(
            "expectedOutput"
        )
        .put(
            "type",
            "string"
        );

        var dependencies =
            stepProperties.putObject(
                "dependencies"
            );

        dependencies.put(
            "type",
            "array"
        );

        dependencies.putObject(
            "items"
        )
        .put(
            "type",
            "string"
        );

        stepItem.putArray(
            "required"
        )
        .add("step")
        .add("responsibleAgent")
        .add("objective")
        .add("requiredInput")
        .add("expectedOutput")
        .add("dependencies");

        stepItem.put(
            "additionalProperties",
            false
        );

        root.putArray(
            "required"
        )
        .add("summary")
        .add("steps");

        root.put(
            "additionalProperties",
            false
        );

        return root;
    }

    // =========================================================
    // JSON CLEANUP
    // =========================================================

    private String cleanJsonResponse(
        String response
    ) {

        if (
            response == null
        ) {

            return "";
        }

        String cleaned =
            response.trim();

        if (
            cleaned.startsWith(
                "```json"
            )
        ) {

            cleaned =
                cleaned.substring(7);

        } else if (
            cleaned.startsWith(
                "```"
            )
        ) {

            cleaned =
                cleaned.substring(3);
        }

        if (
            cleaned.endsWith(
                "```"
            )
        ) {

            cleaned =
                cleaned.substring(
                    0,
                    cleaned.length() - 3
                );
        }

        cleaned =
            cleaned.trim();

        // Robustly extract the JSON object even when
        // the model adds text before or after it.
        int firstBrace =
            cleaned.indexOf('{');

        int lastBrace =
            cleaned.lastIndexOf('}');

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
}
