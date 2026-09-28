package com.tidalai.monitor;

public record SystemStats(

    double cpuUsage,

    long ramUsedBytes,
    long ramTotalBytes,

    boolean gpuAvailable,
    String gpuName,

    double gpuUtilization,

    long vramUsedBytes,
    long vramTotalBytes,

    double temperatureCelsius,

    double powerDrawWatts,
    double powerLimitWatts

) {

    public double ramUsagePercent() {

        if (
            ramTotalBytes <= 0
        ) {

            return 0;
        }

        return
            (ramUsedBytes * 100.0)
            / ramTotalBytes;
    }

    public double vramUsagePercent() {

        if (
            vramTotalBytes <= 0
        ) {

            return 0;
        }

        return
            (vramUsedBytes * 100.0)
            / vramTotalBytes;
    }

    public static SystemStats unavailable(
        double cpuUsage,
        long ramUsedBytes,
        long ramTotalBytes
    ) {

        return new SystemStats(
            cpuUsage,
            ramUsedBytes,
            ramTotalBytes,
            false,
            "GPU unavailable",
            0,
            0,
            0,
            -1,
            -1,
            -1
        );
    }
}
