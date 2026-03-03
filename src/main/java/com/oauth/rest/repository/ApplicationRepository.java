package com.oauth.rest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oauth.rest.model.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByClientId(String clientId);

    Optional<Application> findByName(String name);

}
