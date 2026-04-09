package com.playrole.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String mensaje) {
        super(mensaje);
    }
}
