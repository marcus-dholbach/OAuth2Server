package com.oauth.rest.service

import com.oauth.rest.dto.CreateUserDto
import com.oauth.rest.exception.UserPasswordException
import com.oauth.rest.model.UserEntity
import com.oauth.rest.model.UserRole
import com.oauth.rest.repository.UserEntityRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class UserEntityServiceSpec extends Specification {

    UserEntityRepository userEntityRepository
    PasswordEncoder passwordEncoder
    UserEntityService userEntityService

    def setup() {
        userEntityRepository = Mock(UserEntityRepository)
        passwordEncoder = Mock(PasswordEncoder)
        userEntityService = new UserEntityService(userEntityRepository, passwordEncoder)
    }

    def "findUserByUsername returns user when user exists"() {
        given:
        String username = "admin"
        UserEntity user = new UserEntity()
        user.setUsername(username)
        user.setPassword("hashedPassword")
        user.setRoles(Set.of(UserRole.ADMIN))

        when:
        Optional<UserEntity> result = userEntityService.findUserByUsername(username)

        then:
        1 * userEntityRepository.findByUsername(username) >> Optional.of(user)
        result.isPresent()
        result.get().getUsername() == username
    }

    def "findUserByUsername returns empty when user does not exist"() {
        given:
        String username = "nonexistent"

        when:
        Optional<UserEntity> result = userEntityService.findUserByUsername(username)

        then:
        1 * userEntityRepository.findByUsername(username) >> Optional.empty()
        !result.isPresent()
    }

    def "nuevoUsuario throws exception when passwords do not match"() {
        given:
        CreateUserDto dto = new CreateUserDto()
        dto.setUsername("testuser")
        dto.setPassword("Password123")
        dto.setPassword2("DifferentPass123")

        when:
        userEntityService.nuevoUsuario(dto)

        then:
        thrown(UserPasswordException)
    }

    def "nuevoUsuario successfully creates user with valid credentials"() {
        given:
        CreateUserDto dto = new CreateUserDto()
        dto.setUsername("newuser")
        dto.setPassword("ValidPass123")
        dto.setPassword2("ValidPass123")
        dto.setFullName("New User")
        dto.setEmail("newuser@example.com")

        UserEntity savedUser = new UserEntity()
        savedUser.setUsername(dto.getUsername())
        savedUser.setPassword("hashedPassword")
        savedUser.setRoles(Set.of(UserRole.USER))

        when:
        UserEntity result = userEntityService.nuevoUsuario(dto)

        then:
        1 * passwordEncoder.encode(dto.getPassword()) >> "hashedPassword"
        1 * userEntityRepository.save(_) >> savedUser
        result.getUsername() == dto.getUsername()
    }

    def "nuevoUsuario throws exception when username already exists"() {
        given:
        CreateUserDto dto = new CreateUserDto()
        dto.setUsername("existinguser")
        dto.setPassword("ValidPass123")
        dto.setPassword2("ValidPass123")

        when:
        userEntityService.nuevoUsuario(dto)

        then:
        1 * passwordEncoder.encode(_) >> "hashedPassword"
        1 * userEntityRepository.save(_) >> { throw new DataIntegrityViolationException("Duplicate key") }
        thrown(ResponseStatusException)
    }
}
