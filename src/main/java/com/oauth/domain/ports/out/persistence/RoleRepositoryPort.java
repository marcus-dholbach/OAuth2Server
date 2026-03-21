package com.oauth.domain.ports.out.persistence;

import java.util.Optional;

import com.oauth.domain.model.Role;

/**
 * Puerto de salida para operaciones de repositorio de roles
 */
public interface RoleRepositoryPort {

    Optional<Role> findById(Long id);

    Optional<Role> findByName(String name);

    Role save(Role role);

    void deleteById(Long id);

    void delete(Role role);
}
