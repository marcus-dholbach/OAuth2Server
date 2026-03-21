package com.oauth.domain.exception;

/**
 * Excepción de dominio para errores de validación de contraseña
 */
public class UserPasswordException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UserPasswordException() {
        super("Las contraseñas no coinciden");
    }

    public UserPasswordException(String message) {
        super(message);
    }
}
