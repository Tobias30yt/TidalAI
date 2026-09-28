package com.tidalai.agents;

import java.util.ArrayList;
import java.util.List;

public class AgentPlanStep {

    private int step;

    private String responsibleAgent;

    private String objective;

    private String requiredInput;

    private String expectedOutput;

    private List<String> dependencies =
        new ArrayList<>();

    public AgentPlanStep() {
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getResponsibleAgent() {
        return responsibleAgent;
    }

    public void setResponsibleAgent(
        String responsibleAgent
    ) {

        this.responsibleAgent =
            responsibleAgent;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(
        String objective
    ) {

        this.objective = objective;
    }

    public String getRequiredInput() {
        return requiredInput;
    }

    public void setRequiredInput(
        String requiredInput
    ) {

        this.requiredInput =
            requiredInput;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(
        String expectedOutput
    ) {

        this.expectedOutput =
            expectedOutput;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(
        List<String> dependencies
    ) {

        this.dependencies =
            dependencies;
    }
}
