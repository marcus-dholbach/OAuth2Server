package com.oauth.domain.ports.in.usecase.user;

import java.util.Optional;

import com.oauth.domain.model.UserEntity;

/**
 * Puerto de entrada para el caso de uso de obtención de usuario
 * Define la interfaz que implementa el caso de uso
 */
public interface GetUserUseCasePort {

    /**
     * Busca un usuario por su nombre de usuario
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Busca un usuario por su ID
     */
    Optional<UserEntity> findById(Long id);

    /**
     * Busca un usuario por su correo electrónico
     */
    Optional<UserEntity> findByEmail(String email);
}
