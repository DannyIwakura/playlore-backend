package com.playrole.exception;

public class AccessDeniedException extends org.springframework.security.access.AccessDeniedException {
    public AccessDeniedException(String mensaje) {
        super(mensaje);
    }
}
