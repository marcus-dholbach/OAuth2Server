package com.oauth.rest.mapper

import com.oauth.rest.dto.GetUserDto
import com.oauth.rest.model.UserEntity
import com.oauth.rest.model.UserRole
import spock.lang.Specification

class UserDtoMapperSpec extends Specification {

    def 'toGetUserDto maps UserEntity to GetUserDto'() {
        given:
        UserEntity user = new UserEntity()
        user.setId(1L)
        user.setUsername('testuser')
        user.setPassword('hashedPassword')
        user.setFullName('Test User')
        user.setEmail('test@example.com')
        user.setRoles(Set.of(UserRole.USER))

        UserDtoMapper mapper = new UserDtoMapper()

        when:
        GetUserDto dto = mapper.toGetUserDto(user)

        then:
        dto.getId() == 1L
        dto.getUsername() == 'testuser'
        dto.getFullName() == 'Test User'
        dto.getEmail() == 'test@example.com'
        dto.getRoles() != null
    }
}
