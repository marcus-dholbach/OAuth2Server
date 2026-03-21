package com.oauth.domain.model;

/**
 * Value object para detalles de autenticación específicos de aplicación
 * Record inmutable para clientId
 */
public record ApplicationDetails(String clientId) {
    
    @Override
    public String toString() {
        return String.format("ApplicationDetails{clientId='%s'}", clientId());
    }
}
