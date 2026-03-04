package com.oauth.rest.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oauth.rest.model.Application;
import com.oauth.rest.repository.ApplicationRepository;

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
