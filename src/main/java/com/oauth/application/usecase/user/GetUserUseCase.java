package com.oauth.application.usecase.user;

import java.util.Optional;

import com.oauth.domain.ports.in.usecase.user.GetUserUseCasePort;
import com.oauth.domain.ports.in.user.UserServicePort;
import com.oauth.domain.model.UserEntity;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para obtener información de un usuario
 * Implementa el puerto GetUserUseCasePort
 */
@Service
public class GetUserUseCase implements GetUserUseCasePort {

    private final UserServicePort userService;

    public GetUserUseCase(UserServicePort userService) {
        this.userService = userService;
    }

    /**
     * Busca un usuario por su nombre de usuario
     */
    public Optional<UserEntity> findByUsername(String username) {
        return userService.findByUsername(username);
    }

    /**
     * Busca un usuario por su ID
     */
    public Optional<UserEntity> findById(Long id) {
        return userService.findById(id);
    }

    /**
     * Busca un usuario por su correo electrónico
     */
    public Optional<UserEntity> findByEmail(String email) {
        return userService.findByEmail(email);
    }
}
