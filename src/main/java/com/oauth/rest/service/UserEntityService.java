package com.oauth.rest.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.oauth.rest.dto.CreateUserDto;
import com.oauth.rest.exception.UserPasswordException;
import com.oauth.rest.model.Role;
import com.oauth.rest.model.UserEntity;
import com.oauth.rest.repository.UserEntityRepository;

@Service
public class UserEntityService extends BaseService<UserEntity, Long, UserEntityRepository> {

    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserEntityService(UserEntityRepository repository, PasswordEncoder passwordEncoder, RoleService roleService) {
        super(repository);
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    public Optional<UserEntity> findUserByUsername(String username) {
        return this.repository.findByUsername(username);
    }

    public Optional<UserEntity> findUserByEmail(String email) {
        return this.repository.findByEmail(email);
    }

    public CompletableFuture<UserEntity> nuevoUsuario(CreateUserDto newUser) {
        return CompletableFuture.supplyAsync(() -> newUser)
                .thenApply(this::validarPasswords)
                .thenApply(this::crearUserEntity)
                .thenApply(this::asignarRolDefecto)
                .thenCompose(this::guardarUsuarioAsync)
                .exceptionally(this::manejarExcepciones);
    }

    /**
     * Valida que las contraseñas coincidan
     */
    private CreateUserDto validarPasswords(CreateUserDto newUser) {
        if (!newUser.getPassword().equals(newUser.getPassword2())) {
            throw new UserPasswordException();
        }
        return newUser;
    }

    /**
     * Crea el objeto UserEntity a partir del DTO
     */
    private UserEntity crearUserEntity(CreateUserDto newUser) {
        UserEntity user = new UserEntity();
        user.setUsername(newUser.getUsername());
        user.setFullName(newUser.getFullName());
        user.setEmail(newUser.getEmail());
        user.setPassword(passwordEncoder.encode(newUser.getPassword()));
        user.setEnabled(true);
        return user;
    }

    /**
     * Asigna el rol por defecto ROLE_USER
     */
    private UserEntity asignarRolDefecto(UserEntity user) {
        Set<Role> roles = new HashSet<>();
        var userRole = roleService.findOrCreateRole("ROLE_USER", "Usuario estándar");
        roles.add(userRole);
        user.setRoles(roles);
        return user;
    }

    /**
     * Guarda el usuario en la base de datos
     */
    private CompletableFuture<UserEntity> guardarUsuarioAsync(UserEntity user) {
        return CompletableFuture.completedFuture(save(user))
                .exceptionally(ex -> {throw new RuntimeException(ex);});
    }

    /**
     * Maneja la excepción de integridad de datos
     */
    private UserEntity manejarExcepciones(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        
        // CASO 1: Ya es la excepción que queremos - la relanzamos directamente
        if (cause instanceof UserPasswordException) {
            throw (UserPasswordException) cause;
        }
        if (cause instanceof ResponseStatusException) {
            throw (ResponseStatusException) cause;
        }
        if (cause instanceof DataIntegrityViolationException) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "El nombre de usuario ya existe"
            );
        }
        
        // CASO 2: Es RuntimeException - la relanzamos sin envolver
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }
        
        // CASO 3: Excepción desconocida - la envolvemos en RuntimeException
        throw new RuntimeException(cause);
    }
}
