package com.tidalai.agents;

import java.util.ArrayList;
import java.util.List;

public class AgentPlan {

    private String summary;

    private List<AgentPlanStep> steps =
        new ArrayList<>();

    public AgentPlan() {
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(
        String summary
    ) {

        this.summary = summary;
    }

    public List<AgentPlanStep> getSteps() {
        return steps;
    }

    public void setSteps(
        List<AgentPlanStep> steps
    ) {

        this.steps = steps;
    }
}
