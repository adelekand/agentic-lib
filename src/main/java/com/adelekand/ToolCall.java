package com.adelekand;

import tools.jackson.databind.JsonNode;

public record ToolCall(String id, String name, JsonNode arguments) {}