package com.oauth.adapters.input.rest.dto

import spock.lang.Specification

class CreateUserDtoSpec extends Specification {

    def 'CreateUserDto can be created with all fields'() {
        given:
        CreateUserDto dto = new CreateUserDto()
        dto.setUsername("newuser")
        dto.setEmail("newuser@example.com")
        dto.setPassword("testPassword123")
        dto.setPassword2("testPassword123")
        dto.setFullName("New User")

        expect:
        dto.getUsername() == "newuser"
        dto.getEmail() == "newuser@example.com"
        dto.getPassword() == "testPassword123"
        dto.getPassword2() == "testPassword123"
        dto.getFullName() == "New User"
    }

    def 'CreateUserDto defaults'() {
        given:
        CreateUserDto dto = new CreateUserDto()

        expect:
        dto.getUsername() == null
        dto.getEmail() == null
        dto.getPassword() == null
        dto.getPassword2() == null
        dto.getFullName() == null
    }

    def 'CreateUserDto can be created with constructor'() {
        when:
        CreateUserDto dto = new CreateUserDto(
            username: "testuser",
            password: "testPass123",
            password2: "testPass123"
        )

        then:
        dto.getUsername() == "testuser"
        dto.getPassword() == "testPass123"
        dto.getPassword2() == "testPass123"
    }
}
