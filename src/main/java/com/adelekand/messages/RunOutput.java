package com.adelekand.messages;

import java.util.List;

public record RunOutput(String content, List<Message> messages, Usage usage) {}
