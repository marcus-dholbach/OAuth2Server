package com.oauth.adapters.input;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oauth.domain.ports.in.application.ApplicationServicePort;
import com.oauth.domain.ports.out.persistence.ApplicationRepositoryPort;
import com.oauth.domain.model.Application;

/**
 * Adaptador de entrada que implementa el puerto de servicio de aplicaciones
 */
@Service
public class ApplicationServiceAdapter implements ApplicationServicePort {

    private final ApplicationRepositoryPort applicationRepositoryPort;

    public ApplicationServiceAdapter(ApplicationRepositoryPort applicationRepositoryPort) {
        this.applicationRepositoryPort = applicationRepositoryPort;
    }

    @Override
    public Optional<Application> findById(Long id) {
        return applicationRepositoryPort.findById(id);
    }

    @Override
    public Optional<Application> findByClientId(String clientId) {
        return applicationRepositoryPort.findByClientId(clientId);
    }

    @Override
    public Optional<Application> findByName(String name) {
        return applicationRepositoryPort.findByName(name);
    }

    @Override
    public Application save(Application application) {
        return applicationRepositoryPort.save(application);
    }
}
