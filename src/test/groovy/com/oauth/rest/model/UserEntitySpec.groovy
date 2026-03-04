package com.oauth.rest.model

import spock.lang.Specification

class UserEntitySpec extends Specification {

    def 'UserEntity can set and get properties'() {
        given:
        UserEntity user = new UserEntity()

        when:
        user.setUsername('testuser')
        user.setEmail('test@example.com')
        user.setPassword('hashedPassword')
        user.setFullName('Test User')
        user.setRoles(Set.of(new Role('ROLE_USER', 'Usuario estándar')))

        then:
        user.getUsername() == 'testuser'
        user.getEmail() == 'test@example.com'
        user.getPassword() == 'hashedPassword'
        user.getFullName() == 'Test User'
        user.getRoles().size() == 1
    }

    def 'UserEntity has default constructor'() {
        expect:
        new UserEntity() != null
    }

    def 'UserEntity implements UserDetails'() {
        given:
        UserEntity user = new UserEntity()
        user.setUsername('testuser')
        user.setEmail('test@example.com')
        user.setPassword('password')
        user.setEnabled(true)
        user.setRoles(Set.of(new Role('ROLE_USER', 'Usuario estándar')))

        expect:
        user instanceof org.springframework.security.core.userdetails.UserDetails
        user.getUsername() == 'testuser'
        user.isEnabled() == true
        user.isAccountNonExpired() == true
        user.isAccountNonLocked() == true
        user.isCredentialsNonExpired() == true
    }
}
