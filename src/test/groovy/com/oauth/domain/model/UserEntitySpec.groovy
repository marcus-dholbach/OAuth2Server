package com.oauth.domain.model

import spock.lang.Specification

class UserEntitySpec extends Specification {

    def 'UserEntity can be created with username and email'() {
        given:
        UserEntity user = new UserEntity()
        user.setUsername("testuser")
        user.setEmail("test@example.com")
        user.setPassword("password123")

        expect:
        user.getUsername() == "testuser"
        user.getEmail() == "test@example.com"
        user.getPassword() == "password123"
    }

    def 'UserEntity can have roles'() {
        given:
        UserEntity user = new UserEntity()
        Role adminRole = new Role('ROLE_ADMIN', 'Administrator')
        Role userRole = new Role('ROLE_USER', 'User')

        when:
        user.setRoles(Set.of(adminRole, userRole))

        then:
        user.getRoles().size() == 2
        user.getRoles().contains(adminRole)
        user.getRoles().contains(userRole)
    }

    def 'UserEntity defaults'() {
        given:
        UserEntity user = new UserEntity()

        expect:
        user.getId() == null
        user.getUsername() == null
        user.getEmail() == null
        user.getPassword() == null
    }
}
