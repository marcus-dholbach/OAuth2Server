package com.oauth.adapters.input;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oauth.domain.ports.in.user.UserServicePort;
import com.oauth.domain.ports.out.persistence.UserRepositoryPort;
import com.oauth.domain.model.UserEntity;

/**
 * Adaptador de entrada que implementa el puerto de servicio de usuarios
 */
@Service
public class UserServiceAdapter implements UserServicePort {

    private final UserRepositoryPort userRepositoryPort;

    public UserServiceAdapter(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return userRepositoryPort.findByUsername(username);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userRepositoryPort.findByEmail(email);
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        return userRepositoryPort.findById(id);
    }

    @Override
    public UserEntity save(UserEntity user) {
        return userRepositoryPort.save(user);
    }
}
