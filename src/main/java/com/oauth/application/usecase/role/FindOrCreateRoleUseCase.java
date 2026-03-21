package com.oauth.application.usecase.role;

import com.oauth.domain.ports.in.role.RoleServicePort;
import com.oauth.domain.model.Role;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para buscar o crear roles
 */
@Service
public class FindOrCreateRoleUseCase {

    private final RoleServicePort roleService;

    public FindOrCreateRoleUseCase(RoleServicePort roleService) {
        this.roleService = roleService;
    }

    /**
     * Busca un rol por nombre o lo crea si no existe
     */
    public Role findOrCreateRole(String name, String description) {
        return roleService.findOrCreateRole(name, description);
    }
}
