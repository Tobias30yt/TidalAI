package com.tidalai.agents;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public final class TerminalWindowRunner {

    private TerminalWindowRunner() {
    }

    public static ToolResult run(
        Path root,
        String command,
        BooleanSupplier cancelled
    ) {

        if (
            root == null ||
            command == null ||
            command.isBlank()
        ) {

            return ToolResult.failure(
                "Invalid terminal configuration."
            );
        }

        BooleanSupplier cancelCheck =
            cancelled != null
                ? cancelled
                : () -> false;

        Path tempDirectory;

        try {

            tempDirectory =
                Files.createTempDirectory(
                    "tidalai-terminal-"
                );

        } catch (IOException e) {

            return ToolResult.failure(
                "Could not create terminal workspace: " +
                e.getMessage()
            );
        }

        Path script =
            tempDirectory.resolve(
                "run.ps1"
            );

        Path output =
            tempDirectory.resolve(
                "output.log"
            );

        Path exitFile =
            tempDirectory.resolve(
                "exit.txt"
            );

        Path pidFile =
            tempDirectory.resolve(
                "pid.txt"
            );

        String scriptContent = """
            $ErrorActionPreference = "Continue"

            $root = $env:TIDALAI_ROOT
            $command = $env:TIDALAI_COMMAND
            $output = $env:TIDALAI_OUTPUT
            $exitFile = $env:TIDALAI_EXIT
            $pidFile = $env:TIDALAI_PID

            $PID | Set-Content `
                -LiteralPath $pidFile `
                -Encoding utf8

            Set-Location `
                -LiteralPath $root

            Write-Host ""
            Write-Host "=========================================="
            Write-Host "        TIDALAI AGENT TERMINAL"
            Write-Host "=========================================="
            Write-Host ""
            Write-Host ("Workspace: " + $root)
            Write-Host ("Command:   " + $command)
            Write-Host ""
            Write-Host "------------------------------------------"

            "Workspace: $root" |
                Tee-Object `
                    -FilePath $output

            ("Command: " + $command) |
                Tee-Object `
                    -FilePath $output `
                    -Append

            & cmd.exe /d /s /c $command 2>&1 |
                Tee-Object `
                    -FilePath $output `
                    -Append

            $exitCode = $LASTEXITCODE

            Write-Host ""
            Write-Host "------------------------------------------"
            Write-Host ("Exit code: " + $exitCode)

            Set-Content `
                -LiteralPath $exitFile `
                -Value $exitCode `
                -Encoding utf8

            Start-Sleep `
                -Milliseconds 1200

            exit $exitCode
            """;

        try {

            Files.writeString(
                script,
                scriptContent,
                StandardCharsets.UTF_8
            );

            ProcessBuilder launcher =
                createLauncher(
                    root,
                    command,
                    output,
                    exitFile,
                    pidFile,
                    script
                );

            launcher.start();

        } catch (Exception e) {

            deleteQuietly(
                tempDirectory
            );

            return ToolResult.failure(
                "Could not open PowerShell terminal: " +
                e.getMessage()
            );
        }

        long start =
            System.nanoTime();

        long timeoutNanos =
            TimeUnit.SECONDS.toNanos(
                120
            );

        while (true) {

            if (
                cancelCheck.getAsBoolean()
            ) {

                killTerminal(
                    pidFile
                );

                String log =
                    readQuietly(
                        output
                    );

                deleteQuietly(
                    tempDirectory
                );

                return ToolResult.failure(
                    "Command cancelled by user.\n\n" +
                    log
                );
            }

            if (
                Files.exists(exitFile)
            ) {

                String exitText =
                    readQuietly(
                        exitFile
                    ).trim();

                String log =
                    readQuietly(
                        output
                    );

                int exitCode;

                try {

                    exitCode =
                        Integer.parseInt(
                            exitText
                        );

                } catch (NumberFormatException e) {

                    exitCode = -1;
                }

                deleteQuietly(
                    tempDirectory
                );

                String result =
                    "Exit code: " +
                    exitCode +
                    "\n\n" +
                    log;

                return exitCode == 0
                    ? ToolResult.success(result)
                    : ToolResult.failure(result);
            }

            if (
                System.nanoTime() - start >
                timeoutNanos
            ) {

                killTerminal(
                    pidFile
                );

                String log =
                    readQuietly(
                        output
                    );

                deleteQuietly(
                    tempDirectory
                );

                return ToolResult.failure(
                    "Command timed out after 120 seconds.\n\n" +
                    log
                );
            }

            try {

                Thread.sleep(150);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                killTerminal(
                    pidFile
                );

                return ToolResult.failure(
                    "Terminal execution interrupted."
                );
            }
        }
    }

    private static ProcessBuilder createLauncher(
        Path root,
        String command,
        Path output,
        Path exitFile,
        Path pidFile,
        Path script
    ) {

        String launchCommand =
            "start \"TidalAI Agent Terminal\" " +
            "powershell.exe " +
            "-NoLogo " +
            "-NoProfile " +
            "-ExecutionPolicy Bypass " +
            "-File \"" +
            script.toAbsolutePath() +
            "\"";

        ProcessBuilder builder =
            new ProcessBuilder(
                "cmd.exe",
                "/c",
                launchCommand
            );

        builder.environment().put(
            "TIDALAI_ROOT",
            root.toAbsolutePath().toString()
        );

        builder.environment().put(
            "TIDALAI_COMMAND",
            command
        );

        builder.environment().put(
            "TIDALAI_OUTPUT",
            output.toAbsolutePath().toString()
        );

        builder.environment().put(
            "TIDALAI_EXIT",
            exitFile.toAbsolutePath().toString()
        );

        builder.environment().put(
            "TIDALAI_PID",
            pidFile.toAbsolutePath().toString()
        );

        return builder;
    }

    private static void killTerminal(
        Path pidFile
    ) {

        String pid =
            readQuietly(
                pidFile
            ).trim();

        if (
            pid.isEmpty() ||
            !pid.matches("\\d+")
        ) {

            return;
        }

        try {

            new ProcessBuilder(
                "taskkill",
                "/PID",
                pid,
                "/T",
                "/F"
            )
            .start()
            .waitFor(
                5,
                TimeUnit.SECONDS
            );

        } catch (Exception ignored) {
        }
    }

    private static String readQuietly(
        Path file
    ) {

        try {

            if (
                Files.exists(file)
            ) {

                return Files.readString(
                    file,
                    StandardCharsets.UTF_8
                );
            }

        } catch (Exception ignored) {
        }

        return "";
    }

    private static void deleteQuietly(
        Path directory
    ) {

        try {

            if (
                !Files.exists(directory)
            ) {

                return;
            }

            try (
                var stream =
                    Files.walk(directory)
            ) {

                stream
                    .sorted(
                        Comparator.reverseOrder()
                    )
                    .forEach(
                        path -> {

                            try {

                                Files.deleteIfExists(
                                    path
                                );

                            } catch (Exception ignored) {
                            }
                        }
                    );
            }

        } catch (Exception ignored) {
        }
    }
}
