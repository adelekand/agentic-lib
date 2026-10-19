package com.adelekand.models;

import com.adelekand.messages.Message;
import com.adelekand.messages.Usage;

public record ModelResponse(Message.AssistantMessage message, Usage usage) {}
