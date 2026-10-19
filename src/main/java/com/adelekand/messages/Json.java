package com.adelekand.messages;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.datatype.jsr310.JavaTimeModule;

public final class Json {
    public static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    private Json() {}

    public static ObjectNode object() {
        return MAPPER.createObjectNode();
    }

    public static JsonNode parse(String s) {
        try { return MAPPER.readTree(s); }
        catch (Exception e) { throw new IllegalArgumentException("bad JSON: " + e.getMessage(), e); }
    }

    public static String write(Object o) {
        try { return MAPPER.writeValueAsString(o); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }

    public static <T> T read(String s, Class<T> type) {
        try { return MAPPER.readValue(s, type); }
        catch (Exception e) { throw new IllegalArgumentException("bad JSON: " + e.getMessage(), e); }
    }
}
