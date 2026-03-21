package com.oauth.adapters.input.rest.dto

import spock.lang.Specification

class GetUserDtoSpec extends Specification {

    def 'GetUserDto full constructor works'() {
        when:
        GetUserDto dto = new GetUserDto(1L, 'testuser', 'Test User', 'test@example.com', ['ROLE_USER'] as Set)

        then:
        dto.id() == 1L
        dto.username() == 'testuser'
        dto.fullName() == 'Test User'
        dto.email() == 'test@example.com'
        dto.roles() == ['ROLE_USER'] as Set
    }

    def 'GetUserDto can have null roles'() {
        when:
        GetUserDto dto = new GetUserDto(1L, 'testuser', 'Test User', 'test@example.com', null)

        then:
        dto.id() == 1L
        dto.username() == 'testuser'
        dto.roles() == null
    }

    def 'GetUserDto factory method works'() {
        when:
        GetUserDto dto = GetUserDto.of(1L, 'testuser', 'Test User', 'test@example.com')

        then:
        dto.id() == 1L
        dto.username() == 'testuser'
        dto.fullName() == 'Test User'
        dto.email() == 'test@example.com'
        dto.roles() == null
    }
}
