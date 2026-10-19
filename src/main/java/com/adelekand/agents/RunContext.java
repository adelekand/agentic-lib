package com.adelekand.agents;

import com.adelekand.messages.Message;
import com.adelekand.messages.RunInput;

import java.util.ArrayList;
import java.util.List;

public class RunContext {
    final RunInput req;
    final List<Message> messages = new ArrayList<>();
    int runStart;

    RunContext(RunInput req) { this.req = req; }
}
