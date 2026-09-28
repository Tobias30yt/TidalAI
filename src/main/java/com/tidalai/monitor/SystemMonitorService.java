package com.tidalai.monitor;

import com.sun.management.OperatingSystemMXBean;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class SystemMonitorService {

    private static final long UPDATE_INTERVAL_SECONDS = 1;

    private static final SystemMonitorService INSTANCE =
        new SystemMonitorService();

    private final OperatingSystemMXBean operatingSystem;

    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor(
            runnable -> {

                Thread thread =
                    new Thread(
                        runnable,
                        "tidalai-system-monitor"
                    );

                thread.setDaemon(true);

                return thread;
            }
        );

    private final List<Consumer<SystemStats>> listeners =
        new CopyOnWriteArrayList<>();

    private volatile SystemStats latestStats;

    private volatile boolean started =
        false;

    private SystemMonitorService() {

        operatingSystem =
            ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class
            );

        latestStats =
            collectStats();
    }

    public static SystemMonitorService getInstance() {
        return INSTANCE;
    }

    // =========================================================
    // START
    // =========================================================

    public synchronized void start() {

        if (started) {
            return;
        }

        started =
            true;

        scheduler.scheduleAtFixedRate(
            this::update,
            0,
            UPDATE_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );
    }

    // =========================================================
    // STOP
    // =========================================================

    public synchronized void stop() {

        if (!started) {
            return;
        }

        started =
            false;

        scheduler.shutdownNow();
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    public void subscribe(
        Consumer<SystemStats> listener
    ) {

        if (listener == null) {
            return;
        }

        listeners.add(
            listener
        );

        SystemStats stats =
            latestStats;

        if (stats != null) {
            listener.accept(stats);
        }
    }

    public void unsubscribe(
        Consumer<SystemStats> listener
    ) {

        if (listener == null) {
            return;
        }

        listeners.remove(
            listener
        );
    }

    public SystemStats getLatestStats() {
        return latestStats;
    }

    // =========================================================
    // UPDATE
    // =========================================================

    private void update() {

        SystemStats stats =
            collectStats();

        latestStats =
            stats;

        for (
            Consumer<SystemStats> listener :
            listeners
        ) {

            try {

                listener.accept(
                    stats
                );

            } catch (Exception e) {

                System.err.println(
                    "System monitor listener error: " +
                    e.getMessage()
                );
            }
        }
    }

    // =========================================================
    // COLLECT
    // =========================================================

    private SystemStats collectStats() {

        double cpu =
            operatingSystem.getCpuLoad();

        if (
            cpu < 0
        ) {

            cpu = 0;
        }

        long totalRam =
            operatingSystem.getTotalMemorySize();

        long freeRam =
            operatingSystem.getFreeMemorySize();

        long usedRam =
            Math.max(
                0,
                totalRam - freeRam
            );

        NvidiaStats gpu =
            readNvidiaStats();

        if (
            gpu == null
        ) {

            return SystemStats.unavailable(
                cpu,
                usedRam,
                totalRam
            );
        }

        return new SystemStats(
            cpu,
            usedRam,
            totalRam,
            true,
            gpu.name,
            gpu.gpuUtilization,
            gpu.memoryUsedBytes,
            gpu.memoryTotalBytes,
            gpu.temperature,
            gpu.powerDraw,
            gpu.powerLimit
        );
    }

    // =========================================================
    // NVIDIA EXECUTABLE DISCOVERY
    // =========================================================

    private NvidiaStats readNvidiaStats() {

        for (
            String executable :
            findNvidiaSmiExecutables()
        ) {

            NvidiaStats result =
                runNvidiaSmi(
                    executable
                );

            if (
                result != null
            ) {

                return result;
            }
        }

        return null;
    }

    private List<String>
    findNvidiaSmiExecutables() {

        List<String> paths =
            new ArrayList<>();

        /*
         * First try the PATH.
         */
        paths.add(
            "nvidia-smi.exe"
        );

        paths.add(
            "nvidia-smi"
        );

        /*
         * Standard NVIDIA Windows locations.
         */
        String programFiles =
            System.getenv(
                "ProgramW6432"
            );

        if (
            programFiles == null ||
            programFiles.isBlank()
        ) {

            programFiles =
                System.getenv(
                    "ProgramFiles"
                );
        }

        if (
            programFiles != null &&
            !programFiles.isBlank()
        ) {

            paths.add(
                Path.of(
                    programFiles,
                    "NVIDIA Corporation",
                    "NVSMI",
                    "nvidia-smi.exe"
                ).toString()
            );
        }

        String systemRoot =
            System.getenv(
                "SystemRoot"
            );

        if (
            systemRoot != null &&
            !systemRoot.isBlank()
        ) {

            paths.add(
                Path.of(
                    systemRoot,
                    "System32",
                    "nvidia-smi.exe"
                ).toString()
            );
        }

        /*
         * Search with where.exe as a final Windows fallback.
         */
        String located =
            findUsingWhere();

        if (
            located != null &&
            !located.isBlank()
        ) {

            paths.add(
                located
            );
        }

        /*
         * Remove duplicate paths.
         */
        List<String> unique =
            new ArrayList<>();

        for (
            String path :
            paths
        ) {

            if (
                path == null ||
                path.isBlank()
            ) {

                continue;
            }

            boolean alreadyPresent =
                unique
                    .stream()
                    .anyMatch(
                        existing ->
                            existing.equalsIgnoreCase(
                                path
                            )
                    );

            if (
                !alreadyPresent
            ) {

                unique.add(
                    path
                );
            }
        }

        return unique;
    }

    private String findUsingWhere() {

        Process process =
            null;

        try {

            process =
                new ProcessBuilder(
                    "where.exe",
                    "nvidia-smi"
                )
                .redirectErrorStream(true)
                .start();

            boolean finished =
                process.waitFor(
                    1500,
                    TimeUnit.MILLISECONDS
                );

            if (
                !finished
            ) {

                process.destroyForcibly();

                return null;
            }

            if (
                process.exitValue() != 0
            ) {

                return null;
            }

            String output;

            try (
                BufferedReader reader =
                    new BufferedReader(
                        new InputStreamReader(
                            process.getInputStream(),
                            StandardCharsets.UTF_8
                        )
                    )
            ) {

                output =
                    reader.lines()
                        .collect(
                            Collectors.joining(
                                "\n"
                            )
                        );
            }

            return output
                .lines()
                .map(String::trim)
                .filter(
                    line ->
                        !line.isBlank()
                )
                .findFirst()
                .orElse(null);

        } catch (Exception ignored) {

            if (process != null) {

                try {
                    process.destroyForcibly();
                } catch (Exception ignoredAgain) {
                }
            }

            return null;
        }
    }

    // =========================================================
    // NVIDIA QUERY
    // =========================================================

    private NvidiaStats runNvidiaSmi(
        String executable
    ) {

        if (
            executable == null ||
            executable.isBlank()
        ) {

            return null;
        }

        /*
         * If the path explicitly exists, use it.
         * For PATH commands this check is skipped.
         */
        if (
            (executable.contains("\\") ||
             executable.contains("/")) &&
            !Files.exists(
                Path.of(executable)
            )
        ) {

            return null;
        }

        Process process =
            null;

        try {

            ProcessBuilder builder =
                new ProcessBuilder(
                    executable,

                    "--query-gpu=" +
                    "name," +
                    "utilization.gpu," +
                    "memory.used," +
                    "memory.total," +
                    "temperature.gpu," +
                    "power.draw," +
                    "power.limit",

                    "--format=csv,noheader,nounits"
                );

            builder.redirectErrorStream(
                true
            );

            process =
                builder.start();

            boolean finished =
                process.waitFor(
                    2000,
                    TimeUnit.MILLISECONDS
                );

            if (
                !finished
            ) {

                process.destroyForcibly();

                return null;
            }

            if (
                process.exitValue() != 0
            ) {

                return null;
            }

            String output;

            try (
                BufferedReader reader =
                    new BufferedReader(
                        new InputStreamReader(
                            process.getInputStream(),
                            StandardCharsets.UTF_8
                        )
                    )
            ) {

                output =
                    reader.lines()
                        .collect(
                            Collectors.joining(
                                "\n"
                            )
                        );
            }

            if (
                output.isBlank()
            ) {

                return null;
            }

            /*
             * nvidia-smi can return multiple GPUs.
             * We intentionally display GPU 0 for now.
             */
            String firstLine =
                output
                    .lines()
                    .map(String::trim)
                    .filter(
                        line ->
                            !line.isBlank()
                    )
                    .findFirst()
                    .orElse("");

            return parseNvidiaLine(
                firstLine
            );

        } catch (Exception e) {

            if (process != null) {

                try {
                    process.destroyForcibly();
                } catch (Exception ignored) {
                }
            }

            return null;
        }
    }

    // =========================================================
    // PARSE
    // =========================================================

    private NvidiaStats parseNvidiaLine(
        String line
    ) {

        String[] parts =
            line.split(
                "\\s*,\\s*",
                -1
            );

        if (
            parts.length < 7
        ) {

            return null;
        }

        String name =
            parts[0].trim();

        double gpuUtilization =
            parseDouble(
                parts[1]
            );

        long memoryUsedMiB =
            parseLong(
                parts[2]
            );

        long memoryTotalMiB =
            parseLong(
                parts[3]
            );

        double temperature =
            parseDouble(
                parts[4]
            );

        double powerDraw =
            parseDouble(
                parts[5]
            );

        double powerLimit =
            parseDouble(
                parts[6]
            );

        if (
            name.isBlank()
        ) {

            return null;
        }

        /*
         * nvidia-smi reports memory values in MiB here.
         * Convert them to bytes so the rest of TidalAI
         * uses one consistent unit.
         */
        long memoryUsedBytes =
            Math.max(
                0,
                memoryUsedMiB
            )
            * 1024L
            * 1024L;

        long memoryTotalBytes =
            Math.max(
                0,
                memoryTotalMiB
            )
            * 1024L
            * 1024L;

        return new NvidiaStats(
            name,
            clamp(
                gpuUtilization,
                0,
                100
            ),
            memoryUsedBytes,
            memoryTotalBytes,
            temperature,
            powerDraw,
            powerLimit
        );
    }

    private double parseDouble(
        String value
    ) {

        if (
            value == null
        ) {

            return -1;
        }

        String clean =
            value
                .trim()
                .replace(
                    ",",
                    "."
                );

        if (
            clean.equalsIgnoreCase("N/A") ||
            clean.equalsIgnoreCase("[N/A]") ||
            clean.isBlank()
        ) {

            return -1;
        }

        try {

            return Double.parseDouble(
                clean
            );

        } catch (
            NumberFormatException e
        ) {

            return -1;
        }
    }

    private long parseLong(
        String value
    ) {

        if (
            value == null
        ) {

            return 0;
        }

        String clean =
            value
                .trim()
                .replace(
                    ",",
                    "."
                );

        if (
            clean.equalsIgnoreCase("N/A") ||
            clean.equalsIgnoreCase("[N/A]") ||
            clean.isBlank()
        ) {

            return 0;
        }

        try {

            return Long.parseLong(
                clean
            );

        } catch (
            NumberFormatException e
        ) {

            try {

                return (long)
                    Double.parseDouble(
                        clean
                    );

            } catch (
                NumberFormatException ignored
            ) {

                return 0;
            }
        }
    }

    private double clamp(
        double value,
        double min,
        double max
    ) {

        return Math.max(
            min,
            Math.min(
                max,
                value
            )
        );
    }

    // =========================================================
    // INTERNAL GPU DATA
    // =========================================================

    private static final class NvidiaStats {

        private final String name;

        private final double gpuUtilization;

        private final long memoryUsedBytes;
        private final long memoryTotalBytes;

        private final double temperature;

        private final double powerDraw;
        private final double powerLimit;

        private NvidiaStats(
            String name,
            double gpuUtilization,
            long memoryUsedBytes,
            long memoryTotalBytes,
            double temperature,
            double powerDraw,
            double powerLimit
        ) {

            this.name =
                name;

            this.gpuUtilization =
                gpuUtilization;

            this.memoryUsedBytes =
                memoryUsedBytes;

            this.memoryTotalBytes =
                memoryTotalBytes;

            this.temperature =
                temperature;

            this.powerDraw =
                powerDraw;

            this.powerLimit =
                powerLimit;
        }
    }
}
