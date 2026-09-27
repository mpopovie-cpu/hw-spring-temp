package com.smartshop.exception;

public class InvalidOrderStateException extends BusinessException {

    public InvalidOrderStateException(String message) {
        super(message);
    }
}