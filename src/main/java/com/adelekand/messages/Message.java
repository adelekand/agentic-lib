package com.adelekand.messages;

import com.adelekand.tools.ToolCall;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
// Message.java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "role")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Message.SystemMessage.class, name = "system"),
        @JsonSubTypes.Type(value = Message.UserMessage.class, name = "user"),
        @JsonSubTypes.Type(value = Message.AssistantMessage.class, name = "assistant"),
        @JsonSubTypes.Type(value = Message.ToolMessage.class, name = "tool"),
})
public sealed interface Message {
    record SystemMessage(String content) implements Message {}
    record UserMessage(String content) implements Message {}
    record AssistantMessage(String content, List<ToolCall> toolCalls) implements Message {
        public AssistantMessage {
            toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
        }
    }
    record ToolMessage(String toolCallId, String content) implements Message {}
}