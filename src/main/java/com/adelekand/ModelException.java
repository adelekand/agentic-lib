package com.adelekand;

public class ModelException extends RuntimeException {
    public ModelException(String msg) { super(msg); }
    public ModelException(String msg, Throwable cause) { super(msg, cause); }
}
