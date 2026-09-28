package com.tidalai.agents;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class SandboxManager {

    private Path root;

    private ApprovalMode approvalMode =
        ApprovalMode.MANUAL;

    private ToolApprovalHandler approvalHandler =
        request -> false;

    private boolean showApprovalDialogsInAuto =
        false;

    private boolean externalTerminalEnabled =
        true;

    public SandboxManager() {
    }

    // =========================================================
    // CONFIGURATION
    // =========================================================

    public synchronized void setRoot(
        Path root
    ) {

        if (root == null) {

            this.root = null;
            return;
        }

        Path normalized =
            root.toAbsolutePath()
                .normalize();

        if (!Files.exists(normalized)) {

            throw new IllegalArgumentException(
                "Sandbox directory does not exist: " +
                normalized
            );
        }

        if (!Files.isDirectory(normalized)) {

            throw new IllegalArgumentException(
                "Sandbox path is not a directory: " +
                normalized
            );
        }

        this.root = normalized;
    }

    public synchronized Path getRoot() {
        return root;
    }

    public synchronized boolean isConfigured() {
        return root != null;
    }

    public synchronized void setApprovalMode(
        ApprovalMode approvalMode
    ) {

        if (approvalMode == null) {

            throw new IllegalArgumentException(
                "Approval mode cannot be null."
            );
        }

        this.approvalMode =
            approvalMode;
    }

    public synchronized ApprovalMode getApprovalMode() {
        return approvalMode;
    }

    public synchronized void setApprovalHandler(
        ToolApprovalHandler approvalHandler
    ) {

        this.approvalHandler =
            approvalHandler != null
                ? approvalHandler
                : request -> false;
    }

    public synchronized boolean isShowApprovalDialogsInAuto() {
        return showApprovalDialogsInAuto;
    }

    public synchronized void setShowApprovalDialogsInAuto(
        boolean enabled
    ) {

        showApprovalDialogsInAuto =
            enabled;
    }

    public synchronized boolean isExternalTerminalEnabled() {
        return externalTerminalEnabled;
    }

    public synchronized void setExternalTerminalEnabled(
        boolean enabled
    ) {

        externalTerminalEnabled =
            enabled;
    }

    // =========================================================
    // FILE OPERATIONS
    // =========================================================

    public ToolResult readFile(
        String relativePath
    ) {

        try {

            Path path =
                resolveInsideSandbox(
                    relativePath
                );

            if (!Files.exists(path)) {

                return ToolResult.failure(
                    "File does not exist: " +
                    relativePath
                );
            }

            if (!Files.isRegularFile(path)) {

                return ToolResult.failure(
                    "Path is not a file: " +
                    relativePath
                );
            }

            String content =
                Files.readString(
                    path,
                    StandardCharsets.UTF_8
                );

            return ToolResult.success(
                content
            );

        } catch (Exception e) {

            return ToolResult.failure(
                "Could not read file: " +
                e.getMessage()
            );
        }
    }

    public ToolResult writeFile(
        String relativePath,
        String content,
        String reason
    ) {

        try {

            Path path =
                resolveInsideSandbox(
                    relativePath
                );

            ToolRequest request =
                new ToolRequest(
                    ToolType.WRITE_FILE,
                    relativePath,
                    content,
                    reason
                );

            if (!isApproved(request)) {

                return ToolResult.failure(
                    "File write denied by approval policy."
                );
            }

            if (path.getParent() != null) {

                Files.createDirectories(
                    path.getParent()
                );
            }

            Files.writeString(
                path,
                content == null
                    ? ""
                    : content,
                StandardCharsets.UTF_8
            );

            return ToolResult.success(
                "File written: " +
                relativePath
            );

        } catch (Exception e) {

            return ToolResult.failure(
                "Could not write file: " +
                e.getMessage()
            );
        }
    }

    public ToolResult replaceInFile(
        String relativePath,
        String search,
        String replacement,
        String reason
    ) {

        try {

            Path path =
                resolveInsideSandbox(
                    relativePath
                );

            if (!Files.isRegularFile(path)) {

                return ToolResult.failure(
                    "File does not exist: " +
                    relativePath
                );
            }

            if (
                search == null ||
                search.isEmpty()
            ) {

                return ToolResult.failure(
                    "Search text cannot be empty."
                );
            }

            String content =
                Files.readString(
                    path,
                    StandardCharsets.UTF_8
                );

            int occurrences =
                countOccurrences(
                    content,
                    search
                );

            if (occurrences == 0) {

                return ToolResult.failure(
                    "Search text was not found."
                );
            }

            ToolRequest request =
                new ToolRequest(
                    ToolType.REPLACE_FILE,
                    relativePath,
                    search,
                    reason
                );

            if (!isApproved(request)) {

                return ToolResult.failure(
                    "File modification denied by approval policy."
                );
            }

            String updated =
                content.replace(
                    search,
                    replacement == null
                        ? ""
                        : replacement
                );

            Files.writeString(
                path,
                updated,
                StandardCharsets.UTF_8
            );

            return ToolResult.success(
                "File updated: " +
                relativePath +
                " (" +
                occurrences +
                " replacement(s))"
            );

        } catch (Exception e) {

            return ToolResult.failure(
                "Could not modify file: " +
                e.getMessage()
            );
        }
    }

    public ToolResult listFiles(
        String relativeDirectory
    ) {

        try {

            Path directory =
                resolveInsideSandbox(
                    relativeDirectory
                );

            if (!Files.isDirectory(directory)) {

                return ToolResult.failure(
                    "Directory does not exist: " +
                    relativeDirectory
                );
            }

            String output;

            try (
                var stream =
                    Files.list(directory)
            ) {

                output =
                    stream
                        .sorted(
                            Comparator.comparing(
                                path ->
                                    path.getFileName()
                                        .toString()
                                        .toLowerCase()
                            )
                        )
                        .map(path -> {

                            String prefix =
                                Files.isDirectory(path)
                                    ? "[DIR] "
                                    : "[FILE] ";

                            return prefix +
                                path.getFileName();

                        })
                        .collect(
                            Collectors.joining(
                                System.lineSeparator()
                            )
                        );
            }

            return ToolResult.success(
                output.isBlank()
                    ? "Directory is empty."
                    : output
            );

        } catch (Exception e) {

            return ToolResult.failure(
                "Could not list directory: " +
                e.getMessage()
            );
        }
    }

    // =========================================================
    // TERMINAL
    // =========================================================

    public ToolResult runCommand(
        String command,
        String reason
    ) {

        return runCommand(
            command,
            reason,
            () -> false
        );
    }

    public ToolResult runCommand(
        String command,
        String reason,
        BooleanSupplier cancelled
    ) {

        if (
            command == null ||
            command.isBlank()
        ) {

            return ToolResult.failure(
                "Command cannot be empty."
            );
        }

        if (!isConfigured()) {

            return ToolResult.failure(
                "No sandbox workspace configured."
            );
        }

        BooleanSupplier cancelCheck =
            cancelled != null
                ? cancelled
                : () -> false;

        ToolRequest request =
            new ToolRequest(
                ToolType.RUN_COMMAND,
                ".",
                command,
                reason
            );

        if (!isApproved(request)) {

            return ToolResult.failure(
                "Terminal command denied by approval policy."
            );
        }

        if (
            cancelCheck.getAsBoolean()
        ) {

            return ToolResult.failure(
                "Command cancelled by user."
            );
        }

        if (
            externalTerminalEnabled
        ) {

            return TerminalWindowRunner.run(
                root,
                command,
                cancelCheck
            );
        }

        return runCommandHidden(
            command,
            cancelCheck
        );
    }

    private ToolResult runCommandHidden(
        String command,
        BooleanSupplier cancelled
    ) {

        try {

            ProcessBuilder processBuilder =
                new ProcessBuilder(
                    "cmd.exe",
                    "/d",
                    "/c",
                    command
                );

            processBuilder.directory(
                root.toFile()
            );

            processBuilder.redirectErrorStream(
                true
            );

            Process process =
                processBuilder.start();

            StringBuilder output =
                new StringBuilder();

            while (
                process.isAlive()
            ) {

                if (
                    cancelled.getAsBoolean()
                ) {

                    process.destroyForcibly();

                    return ToolResult.failure(
                        "Command cancelled by user.\n\n" +
                        output
                    );
                }

                if (
                    process.getInputStream()
                        .available() > 0
                ) {

                    int available =
                        process.getInputStream()
                            .available();

                    output.append(
                        new String(
                            process.getInputStream()
                                .readNBytes(
                                    available
                                ),
                            StandardCharsets.UTF_8
                        )
                    );
                }

                Thread.sleep(100);
            }

            output.append(
                new String(
                    process.getInputStream()
                        .readAllBytes(),
                    StandardCharsets.UTF_8
                )
            );

            int exitCode =
                process.exitValue();

            String result =
                "Exit code: " +
                exitCode +
                "\n\n" +
                output;

            return exitCode == 0
                ? ToolResult.success(
                    result
                )
                : ToolResult.failure(
                    result
                );

        } catch (
            InterruptedException e
        ) {

            Thread.currentThread().interrupt();

            return ToolResult.failure(
                "Command execution interrupted."
            );

        } catch (Exception e) {

            return ToolResult.failure(
                "Could not run command: " +
                e.getMessage()
            );
        }
    }

    // =========================================================
    // SECURITY / APPROVAL
    // =========================================================

    private Path resolveInsideSandbox(
        String relativePath
    ) {

        if (!isConfigured()) {

            throw new IllegalStateException(
                "No sandbox workspace configured."
            );
        }

        if (
            relativePath == null ||
            relativePath.isBlank()
        ) {

            throw new IllegalArgumentException(
                "Path cannot be empty."
            );
        }

        Path requested =
            Path.of(
                relativePath
            );

        Path resolved =
            root.resolve(
                requested
            )
            .normalize();

        if (!resolved.startsWith(root)) {

            throw new SecurityException(
                "Path escapes sandbox: " +
                relativePath
            );
        }

        return resolved;
    }

    private boolean isApproved(
        ToolRequest request
    ) {

        if (
            request.getType() ==
                ToolType.READ_FILE ||
            request.getType() ==
                ToolType.LIST_FILES
        ) {

            return true;
        }

        if (
            approvalMode ==
            ApprovalMode.AUTO
        ) {

            if (
                request.getType() ==
                    ToolType.RUN_COMMAND
            ) {

                if (
                    !CommandPolicy
                        .isAllowedAutomatically(
                            request.getArgument()
                        )
                ) {

                    return false;
                }
            }

            if (
                !showApprovalDialogsInAuto
            ) {

                return true;
            }

            return approvalHandler.approve(
                request
            );
        }

        return approvalHandler.approve(
            request
        );
    }

    private int countOccurrences(
        String text,
        String search
    ) {

        int count = 0;
        int index = 0;

        while (
            (index =
                text.indexOf(
                    search,
                    index
                )) >= 0
        ) {

            count++;

            index +=
                search.length();
        }

        return count;
    }
}
