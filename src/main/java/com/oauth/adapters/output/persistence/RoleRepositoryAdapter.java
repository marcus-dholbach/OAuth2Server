package com.oauth.adapters.output.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.oauth.domain.ports.out.persistence.RoleRepositoryPort;
import com.oauth.domain.model.Role;
import com.oauth.adapters.output.persistence.RoleRepository;

/**
 * Adaptador de salida que implementa el puerto de repositorio de roles
 */
@Repository
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final RoleRepository roleRepository;

    public RoleRepositoryAdapter(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public Role save(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public void deleteById(Long id) {
        roleRepository.deleteById(id);
    }

    @Override
    public void delete(Role role) {
        roleRepository.delete(role);
    }
}
