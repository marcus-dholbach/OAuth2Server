package com.oauth.domain.ports.in.role;

import java.util.Optional;

import com.oauth.domain.model.Role;

/**
 * Puerto de entrada para servicios de Rol
 * Define las operaciones de negocio relacionadas con roles
 */
public interface RoleServicePort {

    /**
     * Busca un rol por su nombre
     */
    Optional<Role> findByName(String name);

    /**
     * Busca un rol por su ID
     */
    Optional<Role> findById(Long id);

    /**
     * Guarda un rol
     */
    Role save(Role role);

    /**
     * Busca un rol por nombre o lo crea si no existe
     */
    Role findOrCreateRole(String name, String description);
}
