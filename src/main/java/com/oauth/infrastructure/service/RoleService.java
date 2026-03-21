package com.oauth.infrastructure.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oauth.domain.model.Role;
import com.oauth.adapters.output.persistence.RoleRepository;

@Service
public class RoleService extends BaseService<Role, Long, RoleRepository> {

    public RoleService(RoleRepository repository) {
        super(repository);
    }

    public Optional<Role> findByName(String name) {
        return this.repository.findByName(name);
    }

    public Role findOrCreateRole(String name, String description) {
        return repository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role(name, description);
                    return save(role);
                });
    }
}
