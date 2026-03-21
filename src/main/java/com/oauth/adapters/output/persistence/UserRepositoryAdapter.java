package com.oauth.adapters.output.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.oauth.domain.ports.out.persistence.UserRepositoryPort;
import com.oauth.domain.model.UserEntity;
import com.oauth.adapters.output.persistence.UserEntityRepository;

/**
 * Adaptador de salida que implementa el puerto de repositorio de usuarios
 */
@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserEntityRepository userEntityRepository;

    public UserRepositoryAdapter(UserEntityRepository userEntityRepository) {
        this.userEntityRepository = userEntityRepository;
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        return userEntityRepository.findById(id);
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return userEntityRepository.findByUsername(username);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userEntityRepository.findByEmail(email);
    }

    @Override
    public UserEntity save(UserEntity user) {
        return userEntityRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userEntityRepository.deleteById(id);
    }

    @Override
    public void delete(UserEntity user) {
        userEntityRepository.delete(user);
    }
}
