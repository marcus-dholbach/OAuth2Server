package com.oauth.domain.ports.out.security;

/**
 * Puerto de salida para codificación de contraseñas
 */
public interface PasswordEncoderPort {

    /**
     * Codifica una contraseña plana
     * @param rawPassword contraseña sin codificar
     * @return contraseña codificada
     */
    String encode(CharSequence rawPassword);

    /**
     * Compara una contraseña plana con una codificada
     * @param rawPassword contraseña sin codificar
     * @param encodedPassword contraseña codificada
     * @return true si coinciden
     */
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
