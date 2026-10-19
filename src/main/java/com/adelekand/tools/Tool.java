package com.adelekand.tools;

import tools.jackson.databind.JsonNode;

public interface Tool {
    ToolSpec spec();
    String call(JsonNode args) throws Exception;
    default boolean stopsRun() { return false; }
}
