package com.oauth.adapters.input;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oauth.domain.ports.in.role.RoleServicePort;
import com.oauth.domain.ports.out.persistence.RoleRepositoryPort;
import com.oauth.domain.model.Role;

/**
 * Adaptador de entrada que implementa el puerto de servicio de roles
 */
@Service
public class RoleServiceAdapter implements RoleServicePort {

    private final RoleRepositoryPort roleRepositoryPort;

    public RoleServiceAdapter(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleRepositoryPort.findByName(name);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return roleRepositoryPort.findById(id);
    }

    @Override
    public Role save(Role role) {
        return roleRepositoryPort.save(role);
    }

    @Override
    public Role findOrCreateRole(String name, String description) {
        return roleRepositoryPort.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role(name, description);
                    return roleRepositoryPort.save(role);
                });
    }
}
