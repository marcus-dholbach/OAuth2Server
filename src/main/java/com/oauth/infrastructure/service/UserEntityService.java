package com.oauth.infrastructure.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oauth.domain.model.UserEntity;
import com.oauth.adapters.output.persistence.UserEntityRepository;

/**
 * Servicio de infraestructura para operaciones de persistencia de usuarios.
 * Solo contiene operaciones CRUD básicas y métodos de búsqueda.
 * La lógica de negocio debe estar en la capa de aplicación (Use Cases).
 */
@Service
public class UserEntityService extends BaseService<UserEntity, Long, UserEntityRepository> {

    public UserEntityService(UserEntityRepository repository) {
        super(repository);
    }

    /**
     * Busca un usuario por su nombre de usuario
     * Nota: Este método es usado por CustomUserDetailsService para autenticación
     */
    public Optional<UserEntity> findUserByUsername(String username) {
        return this.repository.findByUsername(username);
    }

    /**
     * Busca un usuario por su correo electrónico
     */
    public Optional<UserEntity> findUserByEmail(String email) {
        return this.repository.findByEmail(email);
    }
}
