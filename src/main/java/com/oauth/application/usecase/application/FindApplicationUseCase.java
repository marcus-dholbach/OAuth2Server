package com.oauth.application.usecase.application;

import java.util.Optional;

import com.oauth.domain.ports.in.application.ApplicationServicePort;
import com.oauth.domain.model.Application;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para buscar aplicaciones
 */
@Service
public class FindApplicationUseCase {

    private final ApplicationServicePort applicationService;

    public FindApplicationUseCase(ApplicationServicePort applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Busca una aplicación por su ID
     */
    public Optional<Application> findById(Long id) {
        return applicationService.findById(id);
    }

    /**
     * Busca una aplicación por su clientId
     */
    public Optional<Application> findByClientId(String clientId) {
        return applicationService.findByClientId(clientId);
    }

    /**
     * Busca una aplicación por su nombre
     */
    public Optional<Application> findByName(String name) {
        return applicationService.findByName(name);
    }
}
