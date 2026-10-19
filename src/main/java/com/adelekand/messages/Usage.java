package com.adelekand.messages;

public record Usage(int inputTokens, int outputTokens) {
    public static final Usage ZERO = new Usage(0, 0);
    public Usage plus(Usage o) {
        return o == null ? this : new Usage(inputTokens + o.inputTokens, outputTokens + o.outputTokens);
    }
}