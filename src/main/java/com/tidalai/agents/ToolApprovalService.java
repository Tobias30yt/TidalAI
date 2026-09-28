package com.tidalai.agents;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ToolApprovalService {

    private ToolApprovalService() {
    }

    public static boolean requestApproval(
        ToolRequest request
    ) {

        CountDownLatch latch =
            new CountDownLatch(1);

        AtomicBoolean approved =
            new AtomicBoolean(false);

        Platform.runLater(() -> {

            Alert alert =
                new Alert(
                    Alert.AlertType.CONFIRMATION
                );

            alert.setTitle(
                "TidalAI Tool Approval"
            );

            alert.setHeaderText(
                "Agent wants to use a tool"
            );

            alert.setContentText(
                buildMessage(request)
            );

            var result =
                alert.showAndWait();

            approved.set(
                result.isPresent() &&
                result.get() == ButtonType.OK
            );

            latch.countDown();
        });

        try {

            latch.await();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            return false;
        }

        return approved.get();
    }

    private static String buildMessage(
        ToolRequest request
    ) {

        return switch (
            request.getType()
        ) {

            case READ_FILE ->
                "Read file:\n" +
                request.getTarget();

            case LIST_FILES ->
                "List directory:\n" +
                request.getTarget();

            case WRITE_FILE ->
                "Write file:\n" +
                request.getTarget() +
                "\n\nReason:\n" +
                safeReason(
                    request.getReason()
                );

            case REPLACE_FILE ->
                "Modify file:\n" +
                request.getTarget() +
                "\n\nReason:\n" +
                safeReason(
                    request.getReason()
                );

            case RUN_COMMAND ->
                "Run terminal command:\n\n" +
                request.getArgument() +
                "\n\nReason:\n" +
                safeReason(
                    request.getReason()
                );
        };
    }

    private static String safeReason(
        String reason
    ) {

        if (
            reason == null ||
            reason.isBlank()
        ) {

            return "No reason provided.";
        }

        return reason;
    }
}
