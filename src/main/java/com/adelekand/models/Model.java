package com.adelekand.models;

import com.adelekand.messages.Message;
import com.adelekand.messages.ModelResponse;
import com.adelekand.messages.StreamChunk;
import com.adelekand.tools.ToolCall;
import com.adelekand.tools.ToolSpec;

import java.util.List;
import java.util.function.Consumer;

public interface Model {
    String id();
    ModelResponse chat(List<Message> messages, List<ToolSpec> tools);

    default void chatStream(List<Message> messages, List<ToolSpec> tools, Consumer<StreamChunk> onChunk) {
        ModelResponse r = chat(messages, tools);
        if (r.message().content() != null) onChunk.accept(new StreamChunk.Text(r.message().content()));
        List<ToolCall> calls = r.message().toolCalls();
        for (int i = 0; i < calls.size(); i++) {
            ToolCall c = calls.get(i);
            onChunk.accept(new StreamChunk.ToolCallDelta(i, c.id(), c.name(), c.arguments().toString()));
        }
        if (r.usage() != null) onChunk.accept(new StreamChunk.UsageChunk(r.usage()));
    }
}
