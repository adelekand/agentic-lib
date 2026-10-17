package com.adelekand.models;

import com.adelekand.dto.Message;
import com.adelekand.dto.ModelResponse;
import com.adelekand.dto.StreamChunk;
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
    }
}
