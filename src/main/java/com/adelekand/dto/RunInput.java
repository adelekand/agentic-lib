package com.adelekand.dto;

public record RunInput(String input, String sessionId, String userId) {
    public static RunInput of(String input) { return new RunInput(input, null, null); }
}
