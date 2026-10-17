package com.adelekand.models;

import com.adelekand.dto.Message;
import com.adelekand.exceptions.ModelException;
import com.adelekand.dto.ModelResponse;
import com.adelekand.tools.ToolCall;
import com.adelekand.tools.ToolSpec;
import com.adelekand.dto.Usage;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class AzureOpenAI implements Model {
    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();
    private final String endpoint, deployment, apiKey, apiVersion;

    public AzureOpenAI(String endpoint, String deployment, String apiKey, String apiVersion) {
        this.endpoint = endpoint.replaceAll("/+$", "");
        this.deployment = deployment;
        this.apiKey = apiKey;
        this.apiVersion = apiVersion;
    }

    @Override
    public String id() {
        return deployment;
    }

    private String url() {
        return "%s/openai/deployments/%s/chat/completions?api-version=%s".formatted(endpoint, deployment, apiVersion);
    }

    @Override
    public ModelResponse chat(List<Message> messages, List<ToolSpec> tools) {
        try {
            ObjectNode body = JSON.createObjectNode();
            ArrayNode msgs = body.putArray("messages");
            messages.forEach(m -> msgs.add(toWire(m)));
            if (!tools.isEmpty()) {
                ArrayNode t = body.putArray("tools");
                tools.forEach(s -> t.add(toolToWire(s)));
            }

            body.put("model", deployment);
            body.put("max_completion_tokens", 1000);

            HttpRequest req = HttpRequest.newBuilder(URI.create(url()))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMinutes(2))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                throw new ModelException(resp.statusCode() + ": " + resp.body());
            }

            JsonNode root = JSON.readTree(resp.body());
            JsonNode msg = root.path("choices").path(0).path("message");
            if (msg.isMissingNode()) throw new ModelException("no choices returned");

            List<ToolCall> calls = new ArrayList<>();
            for (JsonNode c : msg.path("tool_calls")) {
                String rawArgs = c.path("function").path("arguments").asString("{}");
                calls.add(new ToolCall(
                        c.path("id").asString(),
                        c.path("function").path("name").asString(),
                        JSON.readTree(rawArgs.isBlank() ? "{}" : rawArgs)));
            }

            String content = msg.path("content").isString() ? msg.path("content").asString() : null;
            JsonNode u = root.path("usage");
            Usage usage = new Usage(u.path("prompt_tokens").asInt(), u.path("completion_tokens").asInt());

            return new ModelResponse(new Message.AssistantMessage(content, calls), usage);
        } catch (IOException e) {
            throw new ModelException("request failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ModelException("request interrupted: " + e.getMessage(), e);
        }
    }

    private static ObjectNode toWire(Message m) {
        ObjectNode n = JSON.createObjectNode();
        return switch (m) {
            case Message.SystemMessage s -> n.put("role", "system").put("content", s.content());
            case Message.UserMessage u -> n.put("role", "user").put("content", u.content());
            case Message.AssistantMessage a -> {
                n.put("role", "assistant").put("content", a.content());
                if (!a.toolCalls().isEmpty()) {
                    ArrayNode arr = n.putArray("tool_calls");
                    for (ToolCall c : a.toolCalls()) {
                        ObjectNode tc = arr.addObject().put("id", c.id()).put("type", "function");
                        tc.putObject("function")
                                .put("name", c.name())
                                .put("arguments", c.arguments().toString());
                    }
                }
                yield n;
            }
            case Message.ToolMessage t -> n.put("role", "tool")
                    .put("tool_call_id", t.toolCallId()).put("content", t.content());
        };
    }

    private static ObjectNode toolToWire(ToolSpec t) {
        ObjectNode n = JSON.createObjectNode().put("type", "function");
        n.putObject("function")
                .put("name", t.name())
                .put("description", t.description())
                .set("parameters", t.parameters());
        return n;
    }
}
