package com.playrole.exeption;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String mensaje) {
        super(mensaje);
    }
}
