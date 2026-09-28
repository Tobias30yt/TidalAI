package com.tidalai.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class AppConfig {

    private int version = 2;

    private boolean darkMode = true;

    private String workspacePath = "";

    private String agentConfig =
        "STANDARD";

    private String approvalMode =
        "MANUAL";

    private boolean showApprovalDialogsInAuto =
        false;

    private boolean externalTerminalEnabled =
        true;

    private String responseQuality =
        "Balanced";

    /*
     * Default model for normal Chat sessions.
     *
     * Individual chats can still select their own model.
     */
    private String defaultChatModel = "";

    /*
     * Model assignment per agent.
     *
     * Core agents:
     * MAIN
     * REASONING
     * CODER
     * RESEARCHER
     * TESTER
     * REVIEWER
     *
     * Custom agents use their UUID as the key.
     */
    private Map<String, String> agentModels =
        new LinkedHashMap<>();

    public AppConfig() {
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(
        int version
    ) {

        this.version =
            version;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(
        boolean darkMode
    ) {

        this.darkMode =
            darkMode;
    }

    public String getWorkspacePath() {
        return workspacePath;
    }

    public void setWorkspacePath(
        String workspacePath
    ) {

        this.workspacePath =
            workspacePath == null
                ? ""
                : workspacePath;
    }

    public String getAgentConfig() {
        return agentConfig;
    }

    public void setAgentConfig(
        String agentConfig
    ) {

        this.agentConfig =
            agentConfig == null ||
            agentConfig.isBlank()
                ? "STANDARD"
                : agentConfig;
    }

    public String getApprovalMode() {
        return approvalMode;
    }

    public void setApprovalMode(
        String approvalMode
    ) {

        this.approvalMode =
            approvalMode == null ||
            approvalMode.isBlank()
                ? "MANUAL"
                : approvalMode;
    }

    public boolean isShowApprovalDialogsInAuto() {
        return showApprovalDialogsInAuto;
    }

    public void setShowApprovalDialogsInAuto(
        boolean showApprovalDialogsInAuto
    ) {

        this.showApprovalDialogsInAuto =
            showApprovalDialogsInAuto;
    }

    public boolean isExternalTerminalEnabled() {
        return externalTerminalEnabled;
    }

    public void setExternalTerminalEnabled(
        boolean externalTerminalEnabled
    ) {

        this.externalTerminalEnabled =
            externalTerminalEnabled;
    }

    public String getResponseQuality() {
        return responseQuality;
    }

    public void setResponseQuality(
        String responseQuality
    ) {

        this.responseQuality =
            responseQuality == null ||
            responseQuality.isBlank()
                ? "Balanced"
                : responseQuality;
    }

    // =========================================================
    // DEFAULT CHAT MODEL
    // =========================================================

    public String getDefaultChatModel() {
        return defaultChatModel;
    }

    public void setDefaultChatModel(
        String defaultChatModel
    ) {

        this.defaultChatModel =
            defaultChatModel == null
                ? ""
                : defaultChatModel.trim();
    }

    // =========================================================
    // AGENT MODELS
    // =========================================================

    public Map<String, String> getAgentModels() {
        return agentModels;
    }

    public void setAgentModels(
        Map<String, String> agentModels
    ) {

        this.agentModels =
            agentModels == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(
                    agentModels
                );
    }
}