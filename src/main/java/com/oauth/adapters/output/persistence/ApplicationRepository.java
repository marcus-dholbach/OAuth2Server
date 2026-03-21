package com.oauth.adapters.output.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oauth.domain.model.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByClientId(String clientId);

    Optional<Application> findByName(String name);

}
