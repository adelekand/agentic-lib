package com.adelekand.exceptions;

public class AgentException extends RuntimeException {
    public AgentException(String msg) {
        super(msg);
    }

    public AgentException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
