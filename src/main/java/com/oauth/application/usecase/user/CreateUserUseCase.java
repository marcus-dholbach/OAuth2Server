package com.oauth.application.usecase.user;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.oauth.domain.exception.UserPasswordException;
import com.oauth.domain.model.Role;
import com.oauth.domain.model.UserEntity;
import com.oauth.domain.ports.in.role.RoleServicePort;
import com.oauth.domain.ports.in.usecase.user.CreateUserUseCasePort;
import com.oauth.domain.ports.in.user.UserServicePort;
import com.oauth.domain.ports.out.security.PasswordEncoderPort;

/**
 * Caso de uso para crear un nuevo usuario
 * Implementa la lógica de negocio para el registro de usuarios
 * Implementa el puerto CreateUserUseCasePort
 */
@Service
public class CreateUserUseCase implements CreateUserUseCasePort {

    private final UserServicePort userService;
    private final RoleServicePort roleService;
    private final PasswordEncoderPort passwordEncoder;

    public CreateUserUseCase(UserServicePort userService, 
                             RoleServicePort roleService,
                             PasswordEncoderPort passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Ejecuta el caso de uso para crear un nuevo usuario
     */
    @Override
    public CompletableFuture<UserEntity> execute(String username, String email, 
                                                  String password, String password2, 
                                                  String fullName) {
        return CompletableFuture
                .supplyAsync(() -> validatePasswords(password, password2))
                .thenApply(dto -> createUserEntity(username, email, password, fullName))
                .thenCompose(this::assignDefaultRole)
                .thenApply(userService::save)
                .exceptionally(this::handleException);
    }

    /**
     * Valida que las contraseñas coincidan
     */
    private Void validatePasswords(String password, String password2) {
        if (!password.equals(password2)) {
            throw new UserPasswordException();
        }
        return null;
    }

    /**
     * Crea la entidad de usuario con los datos básicos
     */
    private UserEntity createUserEntity(String username, String email, 
                                         String password, String fullName) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        return user;
    }

    /**
     * Asigna el rol por defecto al usuario
     */
    private CompletableFuture<UserEntity> assignDefaultRole(UserEntity user) {
        return CompletableFuture
                .supplyAsync(() -> roleService.findOrCreateRole("ROLE_USER", "Usuario estándar"))
                .thenApply(role -> {
                    Set<Role> roles = new HashSet<>();
                    roles.add(role);
                    user.setRoles(roles);
                    return user;
                });
    }

    /**
     * Maneja las excepciones ocurridas durante la creación del usuario
     */
    private UserEntity handleException(Throwable ex) {
        var cause = ex.getCause() != null ? ex.getCause() : ex;

        if (cause instanceof UserPasswordException) {
            throw (UserPasswordException) cause;
        }
        if (cause instanceof ResponseStatusException) {
            throw (ResponseStatusException) cause;
        }
        if (cause instanceof DataIntegrityViolationException) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre de usuario o email ya existe");
        }
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }

        throw new RuntimeException(cause);
    }
}