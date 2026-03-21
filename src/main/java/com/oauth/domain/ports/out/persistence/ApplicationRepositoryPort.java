package com.oauth.domain.ports.out.persistence;

import java.util.Optional;

import com.oauth.domain.model.Application;

/**
 * Puerto de salida para operaciones de repositorio de aplicaciones
 */
public interface ApplicationRepositoryPort {

    Optional<Application> findById(Long id);

    Optional<Application> findByClientId(String clientId);

    Optional<Application> findByName(String name);

    Application save(Application application);

    void deleteById(Long id);

    void delete(Application application);
}
