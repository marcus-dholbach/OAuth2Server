package com.oauth.rest.model

import spock.lang.Specification

class UserEntitySpec extends Specification {

    def 'UserEntity can set and get properties'() {
        given:
        UserEntity user = new UserEntity()

        when:
        user.setId(1L)
        user.setUsername('testuser')
        user.setPassword('hashedPassword')
        user.setFullName('Test User')
        user.setEmail('test@example.com')
        user.setRoles(Set.of(UserRole.USER))

        then:
        user.getId() == 1L
        user.getUsername() == 'testuser'
        user.getPassword() == 'hashedPassword'
        user.getFullName() == 'Test User'
        user.getEmail() == 'test@example.com'
        user.getRoles() == Set.of(UserRole.USER)
    }

    def 'UserEntity has default constructor'() {
        expect:
        new UserEntity() != null
    }

    def 'UserRole enum has correct values'() {
        expect:
        UserRole.values().length == 2
        UserRole.ADMIN.name() == 'ADMIN'
        UserRole.USER.name() == 'USER'
    }
}
