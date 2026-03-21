package com.oauth.domain.ports.out.persistence;

import java.util.Optional;

import com.oauth.domain.model.UserEntity;

/**
 * Puerto de salida para operaciones de repositorio de usuarios
 */
public interface UserRepositoryPort {

    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    UserEntity save(UserEntity user);

    void deleteById(Long id);

    void delete(UserEntity user);
}
