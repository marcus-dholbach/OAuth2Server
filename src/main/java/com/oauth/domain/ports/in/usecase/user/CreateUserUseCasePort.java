package com.oauth.domain.ports.in.usecase.user;

import java.util.concurrent.CompletableFuture;

import com.oauth.domain.model.UserEntity;

/**
 * Puerto de entrada para el caso de uso de creación de usuario
 * Define la interfaz que implementa el caso de uso
 */
public interface CreateUserUseCasePort {

    /**
     * Ejecuta el caso de uso para crear un nuevo usuario
     * 
     * @param username  nombre de usuario
     * @param email     correo electrónico
     * @param password  contraseña
     * @param password2 confirmación de contraseña
     * @param fullName  nombre completo
     * @return CompletableFuture con el usuario creado
     */
    CompletableFuture<UserEntity> execute(String username, String email, String password,
            String password2, String fullName);
}
