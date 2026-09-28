package com.tidalai.agents;

import java.util.Set;

public final class CommandPolicy {

    private CommandPolicy() {
    }

    private static final Set<String> AUTO_ALLOWED_COMMANDS =
        Set.of(
            "mvn",
            "mvn.cmd",
            "mvnw",
            "mvnw.cmd",
            "gradle",
            "gradle.bat",
            "gradlew",
            "gradlew.bat"
        );

    public static boolean isAllowedAutomatically(
        String command
    ) {

        if (
            command == null ||
            command.isBlank()
        ) {

            return false;
        }

        String trimmed =
            command.trim();

        // Prevent obvious command chaining
        if (
            trimmed.contains("&&") ||
            trimmed.contains("||") ||
            trimmed.contains("|") ||
            trimmed.contains(">") ||
            trimmed.contains("<") ||
            trimmed.contains(";") ||
            trimmed.contains("\n") ||
            trimmed.contains("\r")
        ) {

            return false;
        }

        String executable =
            trimmed.split("\\s+", 2)[0];

        int slash =
            Math.max(
                executable.lastIndexOf('\\'),
                executable.lastIndexOf('/')
            );

        if (slash >= 0) {

            executable =
                executable.substring(
                    slash + 1
                );
        }

        return AUTO_ALLOWED_COMMANDS.contains(
            executable.toLowerCase()
        );
    }
}
