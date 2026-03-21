package com.oauth.adapters.output.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.oauth.domain.ports.out.persistence.ApplicationRepositoryPort;
import com.oauth.domain.model.Application;
import com.oauth.adapters.output.persistence.ApplicationRepository;

/**
 * Adaptador de salida que implementa el puerto de repositorio de aplicaciones
 */
@Repository
public class ApplicationRepositoryAdapter implements ApplicationRepositoryPort {

    private final ApplicationRepository applicationRepository;

    public ApplicationRepositoryAdapter(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Optional<Application> findById(Long id) {
        return applicationRepository.findById(id);
    }

    @Override
    public Optional<Application> findByClientId(String clientId) {
        return applicationRepository.findByClientId(clientId);
    }

    @Override
    public Optional<Application> findByName(String name) {
        return applicationRepository.findByName(name);
    }

    @Override
    public Application save(Application application) {
        return applicationRepository.save(application);
    }

    @Override
    public void deleteById(Long id) {
        applicationRepository.deleteById(id);
    }

    @Override
    public void delete(Application application) {
        applicationRepository.delete(application);
    }
}
