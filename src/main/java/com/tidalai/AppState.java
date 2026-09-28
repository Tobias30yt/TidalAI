package com.tidalai;

import com.tidalai.agents.AgentBus;
import com.tidalai.agents.AgentConfig;
import com.tidalai.agents.AgentManager;
import com.tidalai.agents.AgentOrchestrator;
import com.tidalai.agents.AgentRole;
import com.tidalai.agents.AgentToolExecutor;
import com.tidalai.agents.ApprovalMode;
import com.tidalai.agents.CoderAgent;
import com.tidalai.agents.MainAgent;
import com.tidalai.agents.ReasoningAgent;
import com.tidalai.agents.SandboxManager;
import com.tidalai.agents.WorkerAgent;
import com.tidalai.chat.ChatManager;
import com.tidalai.config.AppConfig;
import com.tidalai.config.ConfigManager;
import com.tidalai.persistence.PersistenceManager;
import com.tidalai.projects.ProjectManager;
import com.tidalai.providers.LMStudioProvider;
import com.tidalai.tasks.TaskManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public final class AppState {

    private AppState() {
    }

    // =========================================================
    // CONFIG
    // =========================================================

    public static final AppConfig CONFIG =
        ConfigManager.load();

    // =========================================================
    // CORE DATA
    // =========================================================

    public static final TaskManager TASKS =
        new TaskManager();

    public static final ProjectManager PROJECTS =
        new ProjectManager();

    public static final ChatManager CHATS =
        new ChatManager();

    public static final AgentManager AGENTS =
        new AgentManager();

    // =========================================================
    // AI
    // =========================================================

    public static final AgentBus AGENT_BUS =
        new AgentBus();

    public static final LMStudioProvider PROVIDER =
        new LMStudioProvider();

    public static final SandboxManager SANDBOX =
        new SandboxManager();

    public static final AgentToolExecutor TOOLS =
        new AgentToolExecutor(
            SANDBOX
        );

    public static AgentConfig AGENT_CONFIG =
        readAgentConfig();

    // =========================================================
    // AGENTS
    // =========================================================

    public static final MainAgent MAIN_AGENT =
        new MainAgent(
            AGENT_BUS,
            AGENT_CONFIG
        );

    public static final ReasoningAgent REASONING_AGENT =
        new ReasoningAgent(
            AGENT_BUS,
            PROVIDER,
            AGENT_CONFIG
        );

    public static final CoderAgent CODER_AGENT =
        new CoderAgent(
            AGENT_BUS,
            PROVIDER,
            TOOLS,
            AGENT_CONFIG
        );

    public static final WorkerAgent RESEARCHER_AGENT =
        new WorkerAgent(
            "Researcher Agent",
            "Inspect the local project, read relevant files, understand existing implementations and collect technical information needed by later agents.",
            AgentRole.RESEARCHER,
            AGENT_BUS,
            PROVIDER,
            TOOLS,
            Set.of(
                "read_file",
                "list_files"
            )
        );

    public static final WorkerAgent TESTER_AGENT =
        new WorkerAgent(
            "Tester Agent",
            "Test the implementation and report concrete problems. Run appropriate build or test commands.",
            AgentRole.TESTER,
            AGENT_BUS,
            PROVIDER,
            TOOLS,
            Set.of(
                "read_file",
                "list_files",
                "run_command"
            )
        );

    public static final WorkerAgent REVIEWER_AGENT =
        new WorkerAgent(
            "Reviewer Agent",
            "Review the implementation for correctness, structure and obvious bugs. Do not modify files.",
            AgentRole.REVIEWER,
            AGENT_BUS,
            PROVIDER,
            TOOLS,
            Set.of(
                "read_file",
                "list_files",
                "run_command"
            )
        );

    public static final AgentOrchestrator ORCHESTRATOR =
        new AgentOrchestrator(
            AGENT_BUS,
            MAIN_AGENT,
            CODER_AGENT,
            RESEARCHER_AGENT,
            TESTER_AGENT,
            REVIEWER_AGENT
        );

    // =========================================================
    // STARTUP
    // =========================================================

    static {

        applyStoredConfiguration();

        PersistenceManager.loadAll(
            PROJECTS,
            TASKS,
            AGENTS,
            CHATS
        );
    }

    private static AgentConfig readAgentConfig() {

        try {

            return AgentConfig.valueOf(
                CONFIG.getAgentConfig()
            );

        } catch (Exception e) {

            return AgentConfig.STANDARD;
        }
    }

    private static void applyStoredConfiguration() {

        Theme.setDarkMode(
            CONFIG.isDarkMode()
        );

        try {

            SANDBOX.setApprovalMode(
                ApprovalMode.valueOf(
                    CONFIG.getApprovalMode()
                )
            );

        } catch (Exception e) {

            SANDBOX.setApprovalMode(
                ApprovalMode.MANUAL
            );
        }

        SANDBOX.setShowApprovalDialogsInAuto(
            CONFIG.isShowApprovalDialogsInAuto()
        );

        SANDBOX.setExternalTerminalEnabled(
            CONFIG.isExternalTerminalEnabled()
        );

        String workspace =
            CONFIG.getWorkspacePath();

        if (
            workspace != null &&
            !workspace.isBlank()
        ) {

            try {

                Path path =
                    Path.of(
                        workspace
                    );

                if (
                    Files.exists(path) &&
                    Files.isDirectory(path)
                ) {

                    SANDBOX.setRoot(
                        path
                    );
                }

            } catch (Exception e) {

                System.err.println(
                    "Stored workspace could not be restored: " +
                    e.getMessage()
                );
            }
        }

        AGENTS.setModelAssignments(
            CONFIG.getAgentModels()
        );
    }

    // =========================================================
    // SAVE
    // =========================================================

    public static synchronized void saveConfig() {

        CONFIG.setDarkMode(
            Theme.isDarkMode()
        );

        CONFIG.setAgentConfig(
            AGENT_CONFIG.name()
        );

        CONFIG.setApprovalMode(
            SANDBOX
                .getApprovalMode()
                .name()
        );

        CONFIG.setShowApprovalDialogsInAuto(
            SANDBOX
                .isShowApprovalDialogsInAuto()
        );

        CONFIG.setExternalTerminalEnabled(
            SANDBOX
                .isExternalTerminalEnabled()
        );

        if (
            SANDBOX.isConfigured()
        ) {

            CONFIG.setWorkspacePath(
                SANDBOX
                    .getRoot()
                    .toAbsolutePath()
                    .toString()
            );

        } else {

            CONFIG.setWorkspacePath(
                ""
            );
        }

        CONFIG.setAgentModels(
            AGENTS.getModelAssignments()
        );

        ConfigManager.save(
            CONFIG
        );
    }

    public static synchronized void saveAll() {

        saveConfig();

        PersistenceManager.saveAll(
            PROJECTS,
            TASKS,
            AGENTS,
            CHATS
        );
    }
}
