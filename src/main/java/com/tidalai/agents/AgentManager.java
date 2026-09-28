package com.tidalai.agents;

import com.tidalai.AppState;
import com.tidalai.models.AIModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AgentManager {

    private final ObservableList<Agent> agents =
        FXCollections.observableArrayList();

    private final Map<String, String> modelAssignments =
        new LinkedHashMap<>();

    public AgentManager() {

        agents.add(
            new Agent(
                "Main Agent",
                AgentRole.MAIN
            )
        );

        agents.add(
            new Agent(
                "Reasoning Agent",
                AgentRole.REASONING
            )
        );
    }

    public ObservableList<Agent> getAgents() {
        return agents;
    }

    public Agent createAgent(
        String name,
        AgentRole role
    ) {

        Agent agent =
            new Agent(
                name,
                role
            );

        agents.add(
            agent
        );

        AppState.saveAll();

        return agent;
    }

    public Agent getAgentByRole(
        AgentRole role
    ) {

        if (
            role == null
        ) {
            return null;
        }

        for (
            Agent agent :
            agents
        ) {

            if (
                agent.getRole() == role
            ) {

                return agent;
            }
        }

        return null;
    }

    // =========================================================
    // MODEL ASSIGNMENTS
    // =========================================================

    public void setModelForAgent(
        String agentKey,
        String modelId
    ) {

        if (
            agentKey == null ||
            agentKey.isBlank()
        ) {

            return;
        }

        String cleanModel =
            modelId == null
                ? null
                : modelId.trim();

        if (
            cleanModel == null ||
            cleanModel.isEmpty() ||
            cleanModel.equalsIgnoreCase(
                "Auto (first available)"
            )
        ) {

            modelAssignments.remove(
                agentKey
            );

        } else {

            modelAssignments.put(
                agentKey,
                cleanModel
            );
        }

        AppState.saveConfig();
    }

    public String getModelForAgent(
        String agentKey
    ) {

        if (
            agentKey == null ||
            agentKey.isBlank()
        ) {

            return null;
        }

        return modelAssignments.get(
            agentKey
        );
    }

    public void setModelForRole(
        AgentRole role,
        String modelId
    ) {

        if (
            role == null
        ) {
            return;
        }

        setModelForAgent(
            role.name(),
            modelId
        );
    }

    public String getModelForRole(
        AgentRole role
    ) {

        if (
            role == null
        ) {

            return null;
        }

        return getModelForAgent(
            role.name()
        );
    }

    public String resolveModelForRole(
        AgentRole role,
        List<AIModel> availableModels
    ) {

        if (
            availableModels == null ||
            availableModels.isEmpty()
        ) {

            return null;
        }

        String assigned =
            getModelForRole(
                role
            );

        if (
            assigned != null &&
            !assigned.isBlank()
        ) {

            for (
                AIModel model :
                availableModels
            ) {

                if (
                    assigned.equals(
                        model.getId()
                    )
                ) {

                    return assigned;
                }
            }
        }

        return availableModels
            .get(0)
            .getId();
    }

    public Map<String, String>
    getModelAssignments() {

        return new LinkedHashMap<>(
            modelAssignments
        );
    }

    public void setModelAssignments(
        Map<String, String> assignments
    ) {

        modelAssignments.clear();

        if (
            assignments == null
        ) {

            return;
        }

        for (
            Map.Entry<String, String> entry :
            assignments.entrySet()
        ) {

            String value =
                entry.getValue();

            if (
                value != null &&
                !value.isBlank()
            ) {

                modelAssignments.put(
                    entry.getKey(),
                    value
                );
            }
        }
    }

    // =========================================================
    // RESTORE
    // =========================================================

    public void restore(
        List<Agent> restored
    ) {

        agents.clear();

        if (
            restored != null
        ) {

            agents.addAll(
                restored
            );
        }

        ensureCoreAgent(
            AgentRole.MAIN,
            "Main Agent"
        );

        ensureCoreAgent(
            AgentRole.REASONING,
            "Reasoning Agent"
        );
    }

    private void ensureCoreAgent(
        AgentRole role,
        String name
    ) {

        if (
            getAgentByRole(
                role
            ) != null
        ) {

            return;
        }

        agents.add(
            new Agent(
                name,
                role
            )
        );
    }

    // =========================================================
    // DISPLAY
    // =========================================================

    public static String getAgentDisplayName(
        AgentRole role
    ) {

        if (
            role == null
        ) {

            return "Unknown Agent";
        }

        return switch (role) {

            case MAIN ->
                "Main Agent";

            case REASONING ->
                "Reasoning Agent";

            case CODER ->
                "Coder Agent";

            case RESEARCHER ->
                "Researcher Agent";

            case TESTER ->
                "Tester Agent";

            case REVIEWER ->
                "Reviewer Agent";
        };
    }
}
