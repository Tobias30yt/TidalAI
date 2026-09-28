package com.tidalai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {

    private static final Path CONFIG_DIRECTORY =
        Path.of(
            System.getProperty("user.home"),
            ".tidalai"
        );

    private static final Path CONFIG_FILE =
        CONFIG_DIRECTORY.resolve(
            "config.json"
        );

    private static final ObjectMapper OBJECT_MAPPER =
        new ObjectMapper()
            .enable(
                SerializationFeature.INDENT_OUTPUT
            );

    private ConfigManager() {
    }

    public static synchronized AppConfig load() {

        try {

            Files.createDirectories(
                CONFIG_DIRECTORY
            );

        } catch (IOException e) {

            System.err.println(
                "Could not create TidalAI config directory: " +
                e.getMessage()
            );

            return new AppConfig();
        }

        if (!Files.exists(CONFIG_FILE)) {

            AppConfig config =
                new AppConfig();

            save(config);

            return config;
        }

        try {

            return OBJECT_MAPPER.readValue(
                CONFIG_FILE.toFile(),
                AppConfig.class
            );

        } catch (Exception e) {

            System.err.println(
                "Could not read TidalAI config: " +
                e.getMessage()
            );

            /*
             * Keep the broken file for debugging.
             */
            try {

                Path backup =
                    CONFIG_DIRECTORY.resolve(
                        "config.broken.json"
                    );

                Files.copy(
                    CONFIG_FILE,
                    backup,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );

            } catch (Exception ignored) {
            }

            AppConfig config =
                new AppConfig();

            save(config);

            return config;
        }
    }

    public static synchronized boolean save(
        AppConfig config
    ) {

        if (config == null) {
            return false;
        }

        try {

            Files.createDirectories(
                CONFIG_DIRECTORY
            );

            OBJECT_MAPPER.writeValue(
                CONFIG_FILE.toFile(),
                config
            );

            return true;

        } catch (Exception e) {

            System.err.println(
                "Could not save TidalAI config: " +
                e.getMessage()
            );

            return false;
        }
    }

    public static Path getConfigFile() {
        return CONFIG_FILE;
    }
}