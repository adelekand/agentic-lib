package com.adelekand.tools;

import tools.jackson.databind.JsonNode;

public record ToolSpec(String name, String description, JsonNode parameters) {}
