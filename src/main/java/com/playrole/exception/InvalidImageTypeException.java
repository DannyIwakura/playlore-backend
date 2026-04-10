package com.playrole.exception;

public class InvalidImageTypeException extends RuntimeException {

	private final String field;

    public InvalidImageTypeException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
