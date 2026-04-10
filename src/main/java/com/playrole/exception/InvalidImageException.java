package com.playrole.exception;

public class InvalidImageException extends RuntimeException {

	private final String field;

    public InvalidImageException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}