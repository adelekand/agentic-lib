package com.adelekand;

import com.adelekand.dto.RunInput;
import com.adelekand.dto.RunOutput;

public interface RunnableEntity {
    String name();
    String description();
    RunOutput run(RunInput req);
}
