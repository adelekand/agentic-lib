package com.adelekand.agents;

import com.adelekand.messages.RunOutput;
import tools.jackson.databind.JsonNode;

public sealed interface AgentEvent {
    record Text(String text) implements AgentEvent {}
    record ToolCallStarted(String name, JsonNode arguments) implements AgentEvent {}
    record ToolCallCompleted(String name, String result) implements AgentEvent {}
    record Completed(RunOutput output) implements AgentEvent {}

    default String kind() {
        return switch (this) {
            case Text t -> "text";
            case ToolCallStarted s -> "tool_call_started";
            case ToolCallCompleted c -> "tool_call_completed";
            case Completed c -> "completed";
        };
    }
}
