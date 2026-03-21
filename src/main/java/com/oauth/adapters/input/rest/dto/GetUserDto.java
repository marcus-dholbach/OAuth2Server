package com.oauth.adapters.input.rest.dto;

import java.util.Set;

/**
 * DTO para devolver datos de usuario
 * Record inmutable - Java class
 */
public record GetUserDto(Long id, String username, String fullName, String email, Set<String> roles) {
    
    /**
     * Factory method para crear instancia sin roles
     */
    public static GetUserDto of(Long id, String username, String fullName, String email) {
        return new GetUserDto(id, username, fullName, email, null);
    }
}
