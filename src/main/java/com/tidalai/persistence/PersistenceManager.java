package com.tidalai.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.tidalai.agents.Agent;
import com.tidalai.agents.AgentManager;
import com.tidalai.agents.AgentRole;
import com.tidalai.chat.ChatMessage;
import com.tidalai.chat.ChatSession;
import com.tidalai.chat.ChatManager;
import com.tidalai.projects.Project;
import com.tidalai.projects.ProjectManager;
import com.tidalai.tasks.Task;
import com.tidalai.tasks.TaskManager;
import com.tidalai.tasks.TaskStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PersistenceManager {

    private static final Path ROOT =
        Path.of(
            System.getProperty("user.home"),
            ".tidalai"
        );

    private static final Path DATA =
        ROOT.resolve("data");

    private static final Path PROJECTS_FILE =
        DATA.resolve("projects.json");

    private static final Path TASKS_FILE =
        DATA.resolve("tasks.json");

    private static final Path AGENTS_FILE =
        DATA.resolve("agents.json");

    private static final Path CHATS_FILE =
        DATA.resolve("chats.json");

    private static final ObjectMapper MAPPER =
        new ObjectMapper()
            .enable(
                SerializationFeature.INDENT_OUTPUT
            );

    private PersistenceManager() {
    }

    // =========================================================
    // LOAD ALL
    // =========================================================

    public static synchronized void loadAll(
        ProjectManager projectManager,
        TaskManager taskManager,
        AgentManager agentManager,
        ChatManager chatManager
    ) {

        if (
            projectManager == null ||
            taskManager == null ||
            agentManager == null ||
            chatManager == null
        ) {

            return;
        }

        try {

            Files.createDirectories(
                DATA
            );

        } catch (IOException e) {

            System.err.println(
                "Could not create TidalAI data directory: " +
                e.getMessage()
            );

            return;
        }

        loadProjects(
            projectManager
        );

        loadTasks(
            taskManager
        );

        loadAgents(
            agentManager
        );

        loadChats(
            chatManager
        );
    }

    // Compatibility overload.
    public static synchronized void loadAll(
        ProjectManager projectManager,
        TaskManager taskManager,
        AgentManager agentManager
    ) {

        loadAll(
            projectManager,
            taskManager,
            agentManager,
            null
        );
    }

    // =========================================================
    // SAVE ALL
    // =========================================================

    public static synchronized void saveAll(
        ProjectManager projectManager,
        TaskManager taskManager,
        AgentManager agentManager,
        ChatManager chatManager
    ) {

        if (
            projectManager == null ||
            taskManager == null ||
            agentManager == null ||
            chatManager == null
        ) {

            return;
        }

        try {

            Files.createDirectories(
                DATA
            );

        } catch (IOException e) {

            System.err.println(
                "Could not create TidalAI data directory: " +
                e.getMessage()
            );

            return;
        }

        saveProjects(
            projectManager
        );

        saveTasks(
            taskManager
        );

        saveAgents(
            agentManager
        );

        saveChats(
            chatManager
        );
    }

    // Compatibility overload.
    public static synchronized void saveAll(
        ProjectManager projectManager,
        TaskManager taskManager,
        AgentManager agentManager
    ) {

        saveProjects(
            projectManager
        );

        saveTasks(
            taskManager
        );

        saveAgents(
            agentManager
        );
    }

    // =========================================================
    // PROJECTS
    // =========================================================

    private static void saveProjects(
        ProjectManager manager
    ) {

        if (
            manager == null
        ) {

            return;
        }

        ArrayNode root =
            MAPPER.createArrayNode();

        for (
            Project project :
            manager.getProjects()
        ) {

            ObjectNode node =
                root.addObject();

            node.put(
                "id",
                project.getId()
            );

            node.put(
                "name",
                project.getName()
            );

            node.put(
                "description",
                project.getDescription()
            );

            node.put(
                "createdAt",
                project.getCreatedAt().toString()
            );
        }

        writeJson(
            PROJECTS_FILE,
            root
        );
    }

    private static void loadProjects(
        ProjectManager manager
    ) {

        if (
            manager == null ||
            !Files.exists(
                PROJECTS_FILE
            )
        ) {

            return;
        }

        try {

            JsonNode root =
                MAPPER.readTree(
                    PROJECTS_FILE.toFile()
                );

            if (
                !root.isArray()
            ) {

                return;
            }

            List<Project> projects =
                new ArrayList<>();

            for (
                JsonNode node :
                root
            ) {

                String id =
                    node.path("id")
                        .asText();

                String name =
                    node.path("name")
                        .asText();

                String description =
                    node.path("description")
                        .asText("");

                String createdAtText =
                    node.path("createdAt")
                        .asText();

                if (
                    id.isBlank() ||
                    name.isBlank() ||
                    createdAtText.isBlank()
                ) {

                    continue;
                }

                LocalDateTime createdAt =
                    LocalDateTime.parse(
                        createdAtText
                    );

                projects.add(
                    new Project(
                        id,
                        name,
                        description,
                        createdAt
                    )
                );
            }

            manager.restore(
                projects
            );

        } catch (Exception e) {

            System.err.println(
                "Could not load projects: " +
                e.getMessage()
            );
        }
    }

    // =========================================================
    // TASKS
    // =========================================================

    private static void saveTasks(
        TaskManager manager
    ) {

        if (
            manager == null
        ) {

            return;
        }

        ArrayNode root =
            MAPPER.createArrayNode();

        for (
            Task task :
            manager.getTasks()
        ) {

            ObjectNode node =
                root.addObject();

            node.put(
                "id",
                task.getId()
            );

            node.put(
                "description",
                task.getDescription()
            );

            node.put(
                "createdAt",
                task.getCreatedAt().toString()
            );

            node.put(
                "status",
                task.getStatus().name()
            );

            node.put(
                "stopRequested",
                task.isStopRequested()
            );

            if (
                task.getProjectId() != null
            ) {

                node.put(
                    "projectId",
                    task.getProjectId()
                );
            }

            ArrayNode messages =
                node.putArray(
                    "conversation"
                );

            for (
                ChatMessage message :
                task
                    .getConversation()
                    .getMessages()
            ) {

                ObjectNode messageNode =
                    messages.addObject();

                messageNode.put(
                    "role",
                    message.role()
                );

                messageNode.put(
                    "content",
                    message.content()
                );
            }
        }

        writeJson(
            TASKS_FILE,
            root
        );
    }

    private static void loadTasks(
        TaskManager manager
    ) {

        if (
            manager == null ||
            !Files.exists(
                TASKS_FILE
            )
        ) {

            return;
        }

        try {

            JsonNode root =
                MAPPER.readTree(
                    TASKS_FILE.toFile()
                );

            if (
                !root.isArray()
            ) {

                return;
            }

            List<Task> tasks =
                new ArrayList<>();

            for (
                JsonNode node :
                root
            ) {

                String id =
                    node.path("id")
                        .asText();

                String description =
                    node.path("description")
                        .asText();

                String createdAtText =
                    node.path("createdAt")
                        .asText();

                if (
                    id.isBlank() ||
                    description.isBlank() ||
                    createdAtText.isBlank()
                ) {

                    continue;
                }

                LocalDateTime createdAt =
                    LocalDateTime.parse(
                        createdAtText
                    );

                TaskStatus status;

                try {

                    status =
                        TaskStatus.valueOf(
                            node
                                .path("status")
                                .asText(
                                    "QUEUED"
                                )
                        );

                } catch (Exception e) {

                    status =
                        TaskStatus.QUEUED;
                }

                if (
                    status == TaskStatus.RUNNING
                ) {

                    status =
                        TaskStatus.STOPPED;
                }

                boolean stopRequested =
                    node
                        .path("stopRequested")
                        .asBoolean(false);

                String projectId =
                    node.hasNonNull(
                        "projectId"
                    )
                        ? node
                            .path("projectId")
                            .asText()
                        : null;

                Task task =
                    new Task(
                        id,
                        description,
                        createdAt,
                        status,
                        stopRequested,
                        projectId
                    );

                JsonNode conversation =
                    node.path(
                        "conversation"
                    );

                loadConversation(
                    conversation,
                    task
                        .getConversation()
                );

                tasks.add(
                    task
                );
            }

            manager.restore(
                tasks
            );

        } catch (Exception e) {

            System.err.println(
                "Could not load tasks: " +
                e.getMessage()
            );
        }
    }

    // =========================================================
    // AGENTS
    // =========================================================

    private static void saveAgents(
        AgentManager manager
    ) {

        if (
            manager == null
        ) {

            return;
        }

        ArrayNode root =
            MAPPER.createArrayNode();

        for (
            Agent agent :
            manager.getAgents()
        ) {

            ObjectNode node =
                root.addObject();

            node.put(
                "id",
                agent.getId()
            );

            node.put(
                "name",
                agent.getName()
            );

            node.put(
                "role",
                agent.getRole().name()
            );
        }

        writeJson(
            AGENTS_FILE,
            root
        );
    }

    private static void loadAgents(
        AgentManager manager
    ) {

        if (
            manager == null ||
            !Files.exists(
                AGENTS_FILE
            )
        ) {

            return;
        }

        try {

            JsonNode root =
                MAPPER.readTree(
                    AGENTS_FILE.toFile()
                );

            if (
                !root.isArray()
            ) {

                return;
            }

            List<Agent> agents =
                new ArrayList<>();

            for (
                JsonNode node :
                root
            ) {

                String id =
                    node.path("id")
                        .asText();

                String name =
                    node.path("name")
                        .asText();

                String roleText =
                    node.path("role")
                        .asText();

                if (
                    id.isBlank() ||
                    name.isBlank() ||
                    roleText.isBlank()
                ) {

                    continue;
                }

                AgentRole role;

                try {

                    role =
                        AgentRole.valueOf(
                            roleText
                        );

                } catch (Exception e) {

                    continue;
                }

                agents.add(
                    new Agent(
                        id,
                        name,
                        role
                    )
                );
            }

            manager.restore(
                agents
            );

        } catch (Exception e) {

            System.err.println(
                "Could not load agents: " +
                e.getMessage()
            );
        }
    }

    // =========================================================
    // CHATS
    // =========================================================

    private static void saveChats(
        ChatManager manager
    ) {

        if (
            manager == null
        ) {

            return;
        }

        ArrayNode root =
            MAPPER.createArrayNode();

        for (
            ChatSession chat :
            manager.getChats()
        ) {

            ObjectNode node =
                root.addObject();

            node.put(
                "id",
                chat.getId()
            );

            node.put(
                "title",
                chat.getTitle()
            );

            node.put(
                "createdAt",
                chat.getCreatedAt().toString()
            );

            if (
                chat.getModelId() != null
            ) {

                node.put(
                    "modelId",
                    chat.getModelId()
                );
            }

            ArrayNode messages =
                node.putArray(
                    "conversation"
                );

            for (
                ChatMessage message :
                chat
                    .getConversation()
                    .getMessages()
            ) {

                ObjectNode messageNode =
                    messages.addObject();

                messageNode.put(
                    "role",
                    message.role()
                );

                messageNode.put(
                    "content",
                    message.content()
                );
            }
        }

        writeJson(
            CHATS_FILE,
            root
        );
    }

    private static void loadChats(
        ChatManager manager
    ) {

        if (
            manager == null ||
            !Files.exists(
                CHATS_FILE
            )
        ) {

            return;
        }

        try {

            JsonNode root =
                MAPPER.readTree(
                    CHATS_FILE.toFile()
                );

            if (
                !root.isArray()
            ) {

                return;
            }

            List<ChatSession> chats =
                new ArrayList<>();

            for (
                JsonNode node :
                root
            ) {

                String id =
                    node.path("id")
                        .asText();

                String title =
                    node.path("title")
                        .asText(
                            "New Chat"
                        );

                String modelId =
                    node.hasNonNull(
                        "modelId"
                    )
                        ? node
                            .path("modelId")
                            .asText()
                        : null;

                String createdAtText =
                    node.path("createdAt")
                        .asText();

                LocalDateTime createdAt;

                try {

                    createdAt =
                        createdAtText.isBlank()
                            ? LocalDateTime.now()
                            : LocalDateTime.parse(
                                createdAtText
                            );

                } catch (Exception e) {

                    createdAt =
                        LocalDateTime.now();
                }

                ChatSession chat =
                    new ChatSession(
                        id,
                        title,
                        modelId,
                        createdAt
                    );

                loadConversation(
                    node.path(
                        "conversation"
                    ),
                    chat
                        .getConversation()
                );

                chats.add(
                    chat
                );
            }

            manager.restore(
                chats
            );

        } catch (Exception e) {

            System.err.println(
                "Could not load chats: " +
                e.getMessage()
            );
        }
    }

    // =========================================================
    // CONVERSATION HELPER
    // =========================================================

    private static void loadConversation(
        JsonNode conversationNode,
        com.tidalai.chat.Conversation conversation
    ) {

        if (
            conversationNode == null ||
            !conversationNode.isArray() ||
            conversation == null
        ) {

            return;
        }

        for (
            JsonNode messageNode :
            conversationNode
        ) {

            String role =
                messageNode
                    .path("role")
                    .asText(
                        "assistant"
                    );

            String content =
                messageNode
                    .path("content")
                    .asText();

            if (
                content.isBlank()
            ) {

                continue;
            }

            switch (role) {

                case "user" ->
                    conversation.addUserMessage(
                        content
                    );

                case "system" ->
                    conversation.addSystemMessage(
                        content
                    );

                default ->
                    conversation.addAssistantMessage(
                        content
                    );
            }
        }
    }

    // =========================================================
    // JSON
    // =========================================================

    private static void writeJson(
        Path file,
        JsonNode node
    ) {

        Path temporary =
            file.resolveSibling(
                file.getFileName() +
                ".tmp"
            );

        try {

            MAPPER.writeValue(
                temporary.toFile(),
                node
            );

            try {

                Files.move(
                    temporary,
                    file,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                );

            } catch (java.nio.file.AtomicMoveNotSupportedException e) {

                Files.move(
                    temporary,
                    file,
                    StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (Exception e) {

            try {

                Files.deleteIfExists(
                    temporary
                );

            } catch (Exception ignored) {
            }

            System.err.println(
                "Could not save " +
                file +
                ": " +
                e.getMessage()
            );
        }
    }
}
