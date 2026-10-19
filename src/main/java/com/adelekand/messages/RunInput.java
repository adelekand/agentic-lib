package com.adelekand.messages;

public record RunInput(String input, String sessionId, String userId) {
    public static RunInput of(String input) { return new RunInput(input, null, null); }
}
