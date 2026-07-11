package com.orbitamarket.orders.exception;

import lombok.Getter;

@Getter
public class InvalidOrderException extends RuntimeException {

    private final String errorCode;

    public InvalidOrderException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public InvalidOrderException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}