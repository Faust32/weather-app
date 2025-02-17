package ru.faust.exception;

import lombok.Getter;

@Getter
public class IncorrectInputDataException extends RuntimeException {

    private final String methodName;

    public IncorrectInputDataException(String message, String methodName) {
        super(message);
        this.methodName = methodName;
    }

}
