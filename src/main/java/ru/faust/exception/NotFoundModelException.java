package ru.faust.exception;

import lombok.Getter;

@Getter
public class NotFoundModelException extends RuntimeException {

    private final String methodName;

    public NotFoundModelException(String message, String methodName) {
        super(message);
        this.methodName = methodName;
    }

}
