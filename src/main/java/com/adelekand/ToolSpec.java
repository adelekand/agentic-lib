package com.adelekand;

import tools.jackson.databind.JsonNode;

public record ToolSpec(String name, String description, JsonNode parameters) {}
