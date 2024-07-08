package com.ageinghippy.truexercise.exception;

public class InternalServerException extends RuntimeException{

    public InternalServerException(String message, Throwable ex) {
        super(message, ex);
    }
}
