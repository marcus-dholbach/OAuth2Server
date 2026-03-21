package com.oauth.infrastructure.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oauth.domain.model.Application;
import com.oauth.adapters.output.persistence.ApplicationRepository;

@Service
public class ApplicationService extends BaseService<Application, Long, ApplicationRepository> {

    public ApplicationService(ApplicationRepository repository) {
        super(repository);
    }

    public Optional<Application> findByClientId(String clientId) {
        return this.repository.findByClientId(clientId);
    }

    public Optional<Application> findByName(String name) {
        return this.repository.findByName(name);
    }
}
