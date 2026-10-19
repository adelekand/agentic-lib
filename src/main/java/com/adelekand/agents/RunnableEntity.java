package com.adelekand.agents;

import com.adelekand.messages.RunInput;
import com.adelekand.messages.RunOutput;

public interface RunnableEntity {
    String name();
    String description();
    RunOutput run(RunInput req);
}
