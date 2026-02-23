package com.oauth.rest.dto

import spock.lang.Specification

class DtoSpec extends Specification {

    def 'CreateUserDto can set and get properties'() {
        given:
        CreateUserDto dto = new CreateUserDto()

        when:
        dto.setUsername('testuser')
        dto.setPassword('Password123')
        dto.setPassword2('Password123')
        dto.setFullName('Test User')
        dto.setEmail('test@example.com')

        then:
        dto.getUsername() == 'testuser'
        dto.getPassword() == 'Password123'
        dto.getPassword2() == 'Password123'
        dto.getFullName() == 'Test User'
        dto.getEmail() == 'test@example.com'
    }

    def 'GetUserDto can set and get properties'() {
        given:
        GetUserDto dto = new GetUserDto()

        when:
        dto.setUsername('testuser')
        dto.setFullName('Test User')
        dto.setEmail('test@example.com')
        dto.setId(1L)
        dto.setRoles(['USER'] as Set)

        then:
        dto.getUsername() == 'testuser'
        dto.getFullName() == 'Test User'
        dto.getEmail() == 'test@example.com'
        dto.getId() == 1L
        dto.getRoles() == ['USER'] as Set
    }

    def 'GetUserDto full constructor works'() {
        when:
        GetUserDto dto = new GetUserDto(1L, 'testuser', 'Test User', 'test@example.com', ['USER'] as Set)

        then:
        dto.getId() == 1L
        dto.getUsername() == 'testuser'
        dto.getFullName() == 'Test User'
        dto.getEmail() == 'test@example.com'
        dto.getRoles() == ['USER'] as Set
    }

    def 'GetUserDto builder works'() {
        when:
        GetUserDto dto = GetUserDto.builder()
            .id(1L)
            .username('testuser')
            .fullName('Test User')
            .email('test@example.com')
            .roles(['USER'] as Set)
            .build()

        then:
        dto.getId() == 1L
        dto.getUsername() == 'testuser'
        dto.getFullName() == 'Test User'
        dto.getEmail() == 'test@example.com'
        dto.getRoles() == ['USER'] as Set
    }
}
