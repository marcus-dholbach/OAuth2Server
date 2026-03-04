package com.oauth.rest.mapper

import com.oauth.rest.dto.GetUserDto
import com.oauth.rest.model.Role
import com.oauth.rest.model.UserEntity
import spock.lang.Specification

class UserDtoMapperSpec extends Specification {

    def 'toGetUserDto maps UserEntity to GetUserDto'() {
        given:
        UserEntity user = new UserEntity()
        user.setUsername('testuser')
        user.setEmail('test@example.com')
        user.setPassword('hashedPassword')
        user.setFullName('Test User')
        user.setRoles(Set.of(new Role('ROLE_USER', 'Usuario estándar')))

        UserDtoMapper mapper = new UserDtoMapper()

        when:
        GetUserDto dto = mapper.toGetUserDto(user)

        then:
        dto.getUsername() == 'testuser'
        dto.getEmail() == 'test@example.com'
        dto.getFullName() == 'Test User'
        dto.getRoles() != null
        dto.getRoles().contains('ROLE_USER')
    }

    def 'toGetUserDto maps roles correctly'() {
        given:
        UserEntity user = new UserEntity()
        user.setUsername('admin')
        user.setEmail('admin@example.com')
        user.setPassword('password')
        user.setRoles(Set.of(
            new Role('ROLE_USER', 'Usuario estándar'),
            new Role('ROLE_ADMIN', 'Administrador')
        ))

        UserDtoMapper mapper = new UserDtoMapper()

        when:
        GetUserDto dto = mapper.toGetUserDto(user)

        then:
        dto.getRoles().size() == 2
        dto.getRoles().contains('ROLE_USER')
        dto.getRoles().contains('ROLE_ADMIN')
    }
}
