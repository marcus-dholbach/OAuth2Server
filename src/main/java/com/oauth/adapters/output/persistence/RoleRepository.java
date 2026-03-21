package com.oauth.adapters.output.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oauth.domain.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

}
