package com.tidalai.tasks;

import java.util.concurrent.CompletableFuture;

public record TaskExecution(
    Task task,
    CompletableFuture<String> future
) {
}