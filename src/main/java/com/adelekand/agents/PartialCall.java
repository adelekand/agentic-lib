package com.adelekand.agents;

import com.adelekand.messages.StreamChunk;
import com.adelekand.messages.Json;
import com.adelekand.tools.ToolCall;

public final class PartialCall {
    private String id = "";
    private final StringBuilder name = new StringBuilder();
    private final StringBuilder args = new StringBuilder();

    void append(StreamChunk.ToolCallDelta d) {
        if (d.id() != null) id = d.id();
        if (d.name() != null) name.append(d.name());
        if (d.arguments() != null) args.append(d.arguments());
    }

    ToolCall build() {
        String a = args.toString().isBlank() ? "{}" : args.toString();
        return new ToolCall(id, name.toString(), Json.parse(a));
    }
}
