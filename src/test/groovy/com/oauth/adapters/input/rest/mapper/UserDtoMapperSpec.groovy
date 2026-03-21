package com.oauth.adapters.input.rest.mapper

import com.oauth.adapters.input.rest.dto.GetUserDto
import com.oauth.domain.model.Role
import com.oauth.domain.model.UserEntity
import spock.lang.Specification

class UserDtoMapperSpec extends Specification {

    def 'toGetUserDto maps UserEntity to GetUserDto'() {
        given:
        UserEntity user = new UserEntity()
        user.setId(1L)
        user.setUsername('testuser')
        user.setEmail('test@example.com')
        user.setPassword('hashedPassword')
        user.setFullName('Test User')
        user.setRoles(Set.of(new Role('ROLE_USER', 'Usuario estándar')))

        UserDtoMapper mapper = new UserDtoMapper()

        when:
        GetUserDto dto = mapper.toGetUserDto(user)

        then:
        dto.username() == 'testuser'
        dto.email() == 'test@example.com'
        dto.fullName() == 'Test User'
        dto.roles() != null
        dto.roles().contains('ROLE_USER')
    }

    def 'toGetUserDto maps roles correctly'() {
        given:
        UserEntity user = new UserEntity()
        user.setId(1L)
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
        dto.roles().size() == 2
        dto.roles().contains('ROLE_USER')
        dto.roles().contains('ROLE_ADMIN')
    }

    def 'toGetUserDto handles empty roles'() {
        given:
        UserEntity user = new UserEntity()
        user.setId(1L)
        user.setUsername('nobody')
        user.setEmail('nobody@example.com')
        user.setPassword('password')
        user.setRoles(new HashSet<>())

        UserDtoMapper mapper = new UserDtoMapper()

        when:
        GetUserDto dto = mapper.toGetUserDto(user)

        then:
        dto.roles().isEmpty()
    }
}
