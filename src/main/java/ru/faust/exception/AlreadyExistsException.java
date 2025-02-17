package ru.faust.exception;

import lombok.Getter;

@Getter
public class AlreadyExistsException extends RuntimeException {

    private final String methodName;

    public AlreadyExistsException(String message, String methodName) {
        super(message);
        this.methodName = methodName;
    }
}
