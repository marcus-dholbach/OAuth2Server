package com.oauth.domain.ports.in.application;

import java.util.Optional;

import com.oauth.domain.model.Application;

/**
 * Puerto de entrada para servicios de Aplicación
 * Define las operaciones de negocio relacionadas con aplicaciones OAuth
 */
public interface ApplicationServicePort {

    /**
     * Busca una aplicación por su ID
     */
    Optional<Application> findById(Long id);

    /**
     * Busca una aplicación por su clientId
     */
    Optional<Application> findByClientId(String clientId);

    /**
     * Busca una aplicación por su nombre
     */
    Optional<Application> findByName(String name);

    /**
     * Guarda una aplicación
     */
    Application save(Application application);
}
