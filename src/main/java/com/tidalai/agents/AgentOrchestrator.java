package com.tidalai.agents;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;

public class AgentOrchestrator {

    private final AgentBus bus;
    private final MainAgent mainAgent;

    private final CoderAgent coderAgent;
    private final WorkerAgent researcherAgent;
    private final WorkerAgent testerAgent;
    private final WorkerAgent reviewerAgent;

    private final ObjectMapper objectMapper =
        new ObjectMapper();

    private final ExecutorService executor =
        Executors.newSingleThreadExecutor();

    public AgentOrchestrator(
        AgentBus bus,
        MainAgent mainAgent,
        CoderAgent coderAgent,
        WorkerAgent researcherAgent,
        WorkerAgent testerAgent,
        WorkerAgent reviewerAgent
    ) {

        this.bus = bus;
        this.mainAgent = mainAgent;
        this.coderAgent = coderAgent;
        this.researcherAgent = researcherAgent;
        this.testerAgent = testerAgent;
        this.reviewerAgent = reviewerAgent;
    }

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

        if (
            task == null ||
            task.isBlank()
        ) {

            return CompletableFuture.failedFuture(
                new IllegalArgumentException(
                    "Task cannot be empty."
                )
            );
        }

        BooleanSupplier cancelCheck =
            cancelled != null
                ? cancelled
                : () -> false;

        return CompletableFuture.supplyAsync(
            () -> {

                if (
                    cancelCheck.getAsBoolean()
                ) {

                    return "Task stopped by user.";
                }

                CompletableFuture<String> reasoning =
                    awaitReasoning(
                        task,
                        cancelCheck
                    );

                try {

                    String reasoningResult =
                        reasoning.get();

                    if (
                        cancelCheck.getAsBoolean()
                    ) {

                        return "Task stopped by user.";
                    }

                    return executeReasoningPlan(
                        reasoningResult,
                        cancelCheck
                    );

                } catch (Exception e) {

                    Throwable cause =
                        e.getCause() != null
                            ? e.getCause()
                            : e;

                    throw new RuntimeException(
                        cause
                    );
                }
            },
            executor
        );
    }

    private CompletableFuture<String> awaitReasoning(
        String task,
        BooleanSupplier cancelled
    ) {

        CompletableFuture<String> result =
            new CompletableFuture<>();

        var listener =
            new java.util.function.Consumer<AgentMessage>() {

                @Override
                public void accept(
                    AgentMessage message
                ) {

                    if (
                        !ReasoningAgent.NAME.equals(
                            message.getSender()
                        )
                    ) {
                        return;
                    }

                    if (
                        !MainAgent.NAME.equals(
                            message.getReceiver()
                        )
                    ) {
                        return;
                    }

                    bus.unsubscribe(
                        this
                    );

                    if (
                        cancelled.getAsBoolean()
                    ) {

                        result.complete(
                            "Task stopped by user."
                        );

                    } else {

                        result.complete(
                            message.getContent()
                        );
                    }
                }
            };

        bus.subscribe(
            listener
        );

        if (
            cancelled.getAsBoolean()
        ) {

            bus.unsubscribe(
                listener
            );

            result.complete(
                "Task stopped by user."
            );

            return result;
        }

        try {

            mainAgent.submitTask(
                task
            );

        } catch (Exception e) {

            bus.unsubscribe(
                listener
            );

            result.completeExceptionally(
                e
            );
        }

        return result;
    }

    private String executeReasoningPlan(
        String content,
        BooleanSupplier cancelled
    ) {

        if (
            content == null ||
            content.isBlank()
        ) {

            return
                "Reasoning Agent returned no result.";
        }

        if (
            content.startsWith("Task stopped")
        ) {

            return content;
        }

        if (
            content.startsWith("ERROR:")
        ) {

            throw new IllegalStateException(
                content
            );
        }

        AgentPlan plan;

        try {

            plan =
                objectMapper.readValue(
                    cleanJson(content),
                    AgentPlan.class
                );

        } catch (Exception e) {

            throw new IllegalStateException(
                "Could not parse AgentPlan: " +
                e.getMessage() +
                "\n\nResponse:\n" +
                content,
                e
            );
        }

        StringBuilder history =
            new StringBuilder();

        if (
            plan.getSummary() != null &&
            !plan.getSummary().isBlank()
        ) {

            history
                .append(
                    "PLAN SUMMARY\n"
                )
                .append(
                    plan.getSummary()
                )
                .append(
                    "\n"
                );
        }

        if (
            plan.getSteps() == null
        ) {

            return history.toString();
        }

        for (
            AgentPlanStep step :
            plan.getSteps()
        ) {

            if (
                cancelled.getAsBoolean()
            ) {

                history.append(
                    "\nTask stopped by user.\n"
                );

                return history.toString();
            }

            String result =
                executeStep(
                    step,
                    history.toString(),
                    cancelled
                );

            history
                .append(
                    "\n--- STEP "
                )
                .append(
                    step.getStep()
                )
                .append(
                    " / "
                )
                .append(
                    step.getResponsibleAgent()
                )
                .append(
                    " ---\n"
                )
                .append(
                    result
                )
                .append(
                    "\n"
                );
        }

        return history.toString();
    }

    private String executeStep(
        AgentPlanStep step,
        String previousWork,
        BooleanSupplier cancelled
    ) {

        String instruction =
            buildInstruction(
                step,
                previousWork
            );

        String agent =
            normalizeAgentName(
                step.getResponsibleAgent()
            );

        try {

            return switch (agent) {

                case "coder" ->
                    coderAgent
                        .execute(
                            instruction,
                            cancelled
                        )
                        .get();

                case "researcher" ->
                    researcherAgent
                        .execute(
                            instruction,
                            cancelled
                        )
                        .get();

                case "tester" ->
                    testerAgent
                        .execute(
                            instruction,
                            cancelled
                        )
                        .get();

                case "reviewer" ->
                    reviewerAgent
                        .execute(
                            instruction,
                            cancelled
                        )
                        .get();

                default ->
                    "Unknown agent: " +
                    step.getResponsibleAgent();
            };

        } catch (Exception e) {

            Throwable cause =
                e.getCause() != null
                    ? e.getCause()
                    : e;

            return
                "Agent execution failed: " +
                cause.getMessage();
        }
    }

    private String buildInstruction(
        AgentPlanStep step,
        String previousWork
    ) {

        return """
            Execute this TidalAI plan step.

            OBJECTIVE:
            %s

            REQUIRED INPUT:
            %s

            EXPECTED OUTPUT:
            %s

            DEPENDENCIES:
            %s

            PREVIOUS WORK:
            %s
            """.formatted(
                safe(step.getObjective()),
                safe(step.getRequiredInput()),
                safe(step.getExpectedOutput()),
                step.getDependencies() == null ||
                step.getDependencies().isEmpty()
                    ? "None"
                    : String.join(
                        ", ",
                        step.getDependencies()
                    ),
                previousWork == null ||
                previousWork.isBlank()
                    ? "None"
                    : previousWork
            );
    }

    private String normalizeAgentName(
        String name
    ) {

        if (name == null) {
            return "";
        }

        String value =
            name
                .trim()
                .toLowerCase();

        if (
            value.equals("coder") ||
            value.equals("coder agent")
        ) {

            return "coder";
        }

        if (
            value.equals("researcher") ||
            value.equals("research") ||
            value.equals("researcher agent")
        ) {

            return "researcher";
        }

        if (
            value.equals("tester") ||
            value.equals("tester agent")
        ) {

            return "tester";
        }

        if (
            value.equals("reviewer") ||
            value.equals("reviewer agent")
        ) {

            return "reviewer";
        }

        return value;
    }

    private String cleanJson(
        String response
    ) {

        if (response == null) {
            return "";
        }

        String cleaned =
            response.trim();

        if (
            cleaned.startsWith("```json")
        ) {

            cleaned =
                cleaned.substring(7);

        } else if (
            cleaned.startsWith("```")
        ) {

            cleaned =
                cleaned.substring(3);
        }

        if (
            cleaned.endsWith("```")
        ) {

            cleaned =
                cleaned.substring(
                    0,
                    cleaned.length() - 3
                );
        }

        int start =
            cleaned.indexOf('{');

        int end =
            cleaned.lastIndexOf('}');

        if (
            start >= 0 &&
            end > start
        ) {

            cleaned =
                cleaned.substring(
                    start,
                    end + 1
                );
        }

        return cleaned.trim();
    }

    private String safe(
        String value
    ) {

        return value == null ||
            value.isBlank()
                ? "None"
                : value;
    }

    public void shutdown() {

        executor.shutdownNow();

        coderAgent.shutdown();
        researcherAgent.shutdown();
        testerAgent.shutdown();
        reviewerAgent.shutdown();
    }
}