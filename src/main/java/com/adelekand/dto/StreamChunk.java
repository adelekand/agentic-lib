package com.adelekand.dto;

public sealed interface StreamChunk {
    record Text(String text) implements StreamChunk {}
    record ToolCallDelta(int index, String id, String name, String arguments) implements StreamChunk {}
    record UsageChunk(Usage usage) implements StreamChunk {}
}
