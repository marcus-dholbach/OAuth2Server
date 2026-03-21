package com.oauth.domain.ports.in.user;

import java.util.Optional;

import com.oauth.domain.model.UserEntity;

/**
 * Puerto de entrada para servicios de Usuario
 * Define las operaciones de negocio relacionadas con usuarios
 */
public interface UserServicePort {

    /**
     * Busca un usuario por su nombre de usuario
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Busca un usuario por su correo electrónico
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Busca un usuario por su ID
     */
    Optional<UserEntity> findById(Long id);

    /**
     * Guarda un usuario
     */
    UserEntity save(UserEntity user);
}
