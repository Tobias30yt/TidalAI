package com.tidalai.providers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tidalai.chat.ChatMessage;
import com.tidalai.models.AIModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class LMStudioProvider implements AIProvider {

    private static final String BASE_URL =
        "http://localhost:1234";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LMStudioProvider() {

        httpClient =
            HttpClient.newHttpClient();

        objectMapper =
            new ObjectMapper();
    }

    @Override
    public String getName() {

        return "LM Studio";
    }

    @Override
    public boolean isConnected() {

        try {

            HttpRequest request =
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            BASE_URL +
                            "/v1/models"
                        )
                    )
                    .GET()
                    .build();

            HttpResponse<String> response =
                httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

            return response.statusCode() == 200;

        } catch (Exception e) {

            return false;
        }
    }

    @Override
    public List<AIModel> getModels() {

        List<AIModel> models =
            new ArrayList<>();

        try {

            HttpRequest request =
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            BASE_URL +
                            "/v1/models"
                        )
                    )
                    .GET()
                    .build();

            HttpResponse<String> response =
                httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

            if (
                response.statusCode() != 200
            ) {

                return models;
            }

            JsonNode root =
                objectMapper.readTree(
                    response.body()
                );

            JsonNode data =
                root.get("data");

            if (
                data == null ||
                !data.isArray()
            ) {

                return models;
            }

            for (
                JsonNode modelNode :
                data
            ) {

                String id =
                    modelNode
                        .path("id")
                        .asText();

                if (
                    id.isEmpty() ||
                    id.toLowerCase()
                        .contains("embedding")
                ) {

                    continue;
                }

                models.add(
                    new AIModel(
                        id,
                        id
                    )
                );
            }

        } catch (
            IOException |
            InterruptedException e
        ) {

            e.printStackTrace();
        }

        return models;
    }

    @Override
    public String chat(
        String model,
        String prompt
    ) {

        return chat(
            model,
            List.of(
                ChatMessage.user(
                    prompt
                )
            )
        );
    }

    public String chat(
        String model,
        List<ChatMessage> conversation
    ) {

        try {

            var root =
                objectMapper.createObjectNode();

            root.put(
                "model",
                model
            );

            var messages =
                root.putArray(
                    "messages"
                );

            for (
                ChatMessage chatMessage :
                conversation
            ) {

                var message =
                    messages.addObject();

                message.put(
                    "role",
                    chatMessage.role()
                );

                message.put(
                    "content",
                    chatMessage.content()
                );
            }

            root.put(
                "temperature",
                0.7
            );

            root.put(
                "max_tokens",
                1024
            );

            String json =
                objectMapper.writeValueAsString(
                    root
                );

            HttpRequest request =
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            BASE_URL +
                            "/v1/chat/completions"
                        )
                    )
                    .header(
                        "Content-Type",
                        "application/json"
                    )
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            json
                        )
                    )
                    .build();

            HttpResponse<String> response =
                httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

            if (
                response.statusCode() != 200
            ) {

                return
                    "LM Studio error: HTTP " +
                    response.statusCode() +
                    "\n" +
                    response.body();
            }

            return extractContent(
                response.body()
            );

        } catch (Exception e) {

            return
                "Could not connect to LM Studio: " +
                e.getMessage();
        }
    }

    public String chatStructured(
        String model,
        String prompt,
        JsonNode schema
    ) {

        try {

            var root =
                objectMapper.createObjectNode();

            root.put(
                "model",
                model
            );

            var messages =
                root.putArray(
                    "messages"
                );

            var message =
                messages.addObject();

            message.put(
                "role",
                "user"
            );

            message.put(
                "content",
                prompt
            );

            root.put(
                "temperature",
                0.2
            );

            root.put(
                "max_tokens",
                1200
            );

            var responseFormat =
                root.putObject(
                    "response_format"
                );

            responseFormat.put(
                "type",
                "json_schema"
            );

            var jsonSchema =
                responseFormat.putObject(
                    "json_schema"
                );

            jsonSchema.put(
                "name",
                "agent_response"
            );

            jsonSchema.put(
                "strict",
                true
            );

            jsonSchema.set(
                "schema",
                schema
            );

            String json =
                objectMapper.writeValueAsString(
                    root
                );

            HttpRequest request =
                HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                            BASE_URL +
                            "/v1/chat/completions"
                        )
                    )
                    .header(
                        "Content-Type",
                        "application/json"
                    )
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            json
                        )
                    )
                    .build();

            HttpResponse<String> response =
                httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

            if (
                response.statusCode() != 200
            ) {

                return
                    "LM Studio structured-output error: HTTP " +
                    response.statusCode() +
                    "\n" +
                    response.body();
            }

            return extractContent(
                response.body()
            );

        } catch (Exception e) {

            return
                "LM Studio structured-output error: " +
                e.getMessage();
        }
    }

    private String extractContent(
        String json
    ) {

        try {

            JsonNode root =
                objectMapper.readTree(
                    json
                );

            JsonNode content =
                root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content");

            if (
                content.isTextual()
            ) {

                return content.asText();
            }

            return json;

        } catch (Exception e) {

            return json;
        }
    }
}
