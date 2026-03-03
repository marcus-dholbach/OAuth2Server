package com.oauth.rest.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oauth.rest.model.Role;
import com.oauth.rest.repository.RoleRepository;

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
