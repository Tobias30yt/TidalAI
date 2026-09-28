package com.tidalai.providers;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LMStudioManager {


public static final String DEFAULT_HOST = "127.0.0.1";
public static final int DEFAULT_PORT = 1234;

private static final String DOWNLOAD_URL = "https://lmstudio.ai/download";

private final HttpClient httpClient;

public LMStudioManager() {
    httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
}

public boolean isWindows() {
    return System.getProperty("os.name", "")
            .toLowerCase(Locale.ROOT)
            .contains("win");
}

public boolean isMac() {
    return System.getProperty("os.name", "")
            .toLowerCase(Locale.ROOT)
            .contains("mac");
}

public boolean isLinux() {
    return System.getProperty("os.name", "")
            .toLowerCase(Locale.ROOT)
            .contains("linux");
}

public String getPlatformName() {
    if (isWindows()) {
        return "Windows";
    }

    if (isMac()) {
        return "macOS";
    }

    if (isLinux()) {
        return "Linux";
    }

    return System.getProperty("os.name", "Unknown");
}

public Path findInstallation() {
    for (Path candidate : installationCandidates()) {
        if (candidate != null && Files.isRegularFile(candidate)) {
            return candidate;
        }
    }

    return null;
}

public Path findLmsExecutable() {
    List<Path> candidates = new ArrayList<>();

    String userHome = System.getProperty("user.home", "");
    String localAppData = System.getenv("LOCALAPPDATA");
    String appData = System.getenv("APPDATA");
    String programFiles = System.getenv("ProgramFiles");
    String programFilesX86 = System.getenv("ProgramFiles(x86)");

    if (isWindows()) {
        if (localAppData != null && !localAppData.isBlank()) {
            candidates.add(Path.of(
                    localAppData,
                    "Programs",
                    "LM Studio",
                    "resources",
                    "app",
                    ".webpack",
                    "x64",
                    "lms.exe"
            ));

            candidates.add(Path.of(
                    localAppData,
                    "Programs",
                    "LM Studio",
                    "lms.exe"
            ));
        }

        candidates.add(Path.of(
                userHome,
                ".lmstudio",
                "bin",
                "lms.exe"
        ));

        if (appData != null && !appData.isBlank()) {
            candidates.add(Path.of(
                    appData,
                    "LM Studio",
                    "lms.exe"
            ));
        }

        if (programFiles != null && !programFiles.isBlank()) {
            candidates.add(Path.of(
                    programFiles,
                    "LM Studio",
                    "lms.exe"
            ));
        }

        if (programFilesX86 != null && !programFilesX86.isBlank()) {
            candidates.add(Path.of(
                    programFilesX86,
                    "LM Studio",
                    "lms.exe"
            ));
        }

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        Path fromPath = locateCommand("where.exe", "lms.exe");
        if (fromPath != null) {
            return fromPath;
        }

        return null;
    }

    candidates.add(Path.of(userHome, ".lmstudio", "bin", "lms"));

    if (isLinux()) {
        candidates.add(Path.of("/usr/bin/lms"));
        candidates.add(Path.of("/usr/local/bin/lms"));
    }

    if (isMac()) {
        candidates.add(Path.of(
                userHome,
                ".lmstudio",
                "bin",
                "lms"
        ));
    }

    for (Path candidate : candidates) {
        if (Files.isRegularFile(candidate)) {
            return candidate;
        }
    }

    Path fromPath = locateCommand(
            isWindows() ? "where.exe" : "which",
            "lms"
    );

    return fromPath;
}

public boolean isInstalled() {
    return findInstallation() != null || findLmsExecutable() != null;
}

public boolean isServerRunning() {
    try {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getServerUrl() + "/v1/models"))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();

        HttpResponse<Void> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.discarding()
        );

        return response.statusCode() >= 200
                && response.statusCode() < 300;

    } catch (Exception ignored) {
        return false;
    }
}

public String getServerUrl() {
    String configuredHost = System.getenv("TIDALAI_LM_HOST");

    if (configuredHost != null && !configuredHost.isBlank()) {
        return "http://" + configuredHost.trim() + ":" + DEFAULT_PORT;
    }

    return "http://" + DEFAULT_HOST + ":" + DEFAULT_PORT;
}

public boolean startServer() throws Exception {
    Path lms = findLmsExecutable();

    if (lms == null) {
        Path installation = findInstallation();

        if (installation != null) {
            launchInstallation();
            return false;
        }

        throw new IllegalStateException(
                "LM Studio / lms executable was not found."
        );
    }

    Process process = new ProcessBuilder(
            lms.toString(),
            "server",
            "start"
    )
            .redirectErrorStream(true)
            .start();

    StringBuilder output = new StringBuilder();

    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream())
    )) {
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line).append(System.lineSeparator());
        }
    }

    int exitCode = process.waitFor();

    if (exitCode == 0) {
        return true;
    }

    return isServerRunning();
}

public void launchInstallation() throws Exception {
    Path installation = findInstallation();

    if (installation == null) {
        throw new IllegalStateException(
                "LM Studio installation could not be located."
        );
    }

    new ProcessBuilder(
            installation.toString()
    ).start();
}

public int installWithWinget() throws Exception {
    if (!isWindows()) {
        throw new UnsupportedOperationException(
                "WinGet installation is only available on Windows."
        );
    }

    Process process = new ProcessBuilder(
            "winget.exe",
            "install",
            "-e",
            "--id",
            "ElementLabs.LMStudio",
            "--accept-source-agreements",
            "--accept-package-agreements"
    )
            .redirectErrorStream(true)
            .start();

    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream())
    )) {
        while (reader.readLine() != null) {
            // Consume output so the process cannot block on a full buffer.
        }
    }

    return process.waitFor();
}

public void openDownloadPage() throws Exception {
    openUrl(DOWNLOAD_URL);
}

public void openDocumentation() throws Exception {
    openUrl("https://lmstudio.ai/docs");
}

private void openUrl(String url) throws Exception {
    if (!Desktop.isDesktopSupported()) {
        throw new UnsupportedOperationException(
                "Desktop browsing is not supported on this system."
        );
    }

    Desktop desktop = Desktop.getDesktop();

    if (!desktop.isSupported(Desktop.Action.BROWSE)) {
        throw new UnsupportedOperationException(
                "Opening a browser is not supported on this system."
        );
    }

    desktop.browse(URI.create(url));
}

private List<Path> installationCandidates() {
    String userHome = System.getProperty("user.home", "");

    String localAppData = System.getenv("LOCALAPPDATA");
    String appData = System.getenv("APPDATA");
    String programFiles = System.getenv("ProgramFiles");
    String programFilesX86 = System.getenv("ProgramFiles(x86)");

    List<Path> candidates = new ArrayList<>();

    if (isWindows()) {
        if (localAppData != null && !localAppData.isBlank()) {
            candidates.add(Path.of(
                    localAppData,
                    "Programs",
                    "LM Studio",
                    "LM Studio.exe"
            ));
        }

        if (appData != null && !appData.isBlank()) {
            candidates.add(Path.of(
                    appData,
                    "LM Studio",
                    "LM Studio.exe"
            ));
        }

        if (programFiles != null && !programFiles.isBlank()) {
            candidates.add(Path.of(
                    programFiles,
                    "LM Studio",
                    "LM Studio.exe"
            ));
        }

        if (programFilesX86 != null && !programFilesX86.isBlank()) {
            candidates.add(Path.of(
                    programFilesX86,
                    "LM Studio",
                    "LM Studio.exe"
            ));
        }

        return candidates;
    }

    if (isMac()) {
        candidates.add(Path.of(
                userHome,
                "Applications",
                "LM Studio.app",
                "Contents",
                "MacOS",
                "LM Studio"
        ));

        candidates.add(Path.of(
                "/Applications",
                "LM Studio.app",
                "Contents",
                "MacOS",
                "LM Studio"
        ));

        return candidates;
    }

    if (isLinux()) {
        candidates.add(Path.of(
                userHome,
                ".local",
                "bin",
                "lm-studio"
        ));

        candidates.add(Path.of(
                "/usr/bin/lm-studio"
        ));

        candidates.add(Path.of(
                "/usr/local/bin/lm-studio"
        ));
    }

    return candidates;
}

private Path locateCommand(String command, String executable)
        {
    try {
        Process process = new ProcessBuilder(
                command,
                executable
        )
                .redirectErrorStream(true)
                .start();

        String firstLine = null;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        )) {
            firstLine = reader.readLine();
        }

        process.waitFor();

        if (firstLine == null || firstLine.isBlank()) {
            return null;
        }

        Path result = Path.of(firstLine.trim());

        if (Files.isRegularFile(result)) {
            return result;
        }

    } catch (Exception ignored) {
        // Command lookup failed.
    }

    return null;
}


}
