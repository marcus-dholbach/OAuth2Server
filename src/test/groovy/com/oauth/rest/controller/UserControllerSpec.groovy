package com.oauth.rest.controller

import com.oauth.rest.dto.CreateUserDto
import com.oauth.rest.dto.GetUserDto
import com.oauth.rest.exception.UserPasswordException
import com.oauth.rest.mapper.UserDtoMapper
import com.oauth.rest.model.UserEntity
import com.oauth.rest.model.UserRole
import com.oauth.rest.service.UserEntityService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class UserControllerSpec extends Specification {

    UserEntityService userEntityService
    UserDtoMapper userDtoMapper
    UserController userController

    def setup() {
        userEntityService = Mock(UserEntityService)
        userDtoMapper = new UserDtoMapper()
        userController = new UserController(userEntityService, userDtoMapper)
    }

    def 'nuevoUsuario returns GetUserDto on success'() {
        given:
        CreateUserDto dto = new CreateUserDto()
        dto.setUsername('newuser')
        dto.setPassword('ValidPass123')
        dto.setPassword2('ValidPass123')
        dto.setFullName('New User')
        dto.setEmail('newuser@example.com')

        UserEntity user = new UserEntity()
        user.setUsername(dto.getUsername())
        user.setPassword('hashedPassword')
        user.setFullName(dto.getFullName())
        user.setEmail(dto.getEmail())
        user.setRoles(Set.of(UserRole.USER))

        when:
        GetUserDto result = userController.nuevoUsuario(dto)

        then:
        1 * userEntityService.nuevoUsuario(dto) >> user
        result != null
        result.getUsername() == 'newuser'
    }

    def 'nuevoUsuario throws exception when passwords do not match'() {
        given:
        CreateUserDto dto = new CreateUserDto()
        dto.setUsername('newuser')
        dto.setPassword('ValidPass123')
        dto.setPassword2('DifferentPass123')

        when:
        userController.nuevoUsuario(dto)

        then:
        1 * userEntityService.nuevoUsuario(dto) >> { throw new UserPasswordException() }
        thrown(UserPasswordException)
    }

    def 'nuevoUsuario throws exception when username already exists'() {
        given:
        CreateUserDto dto = new CreateUserDto()
        dto.setUsername('existinguser')
        dto.setPassword('ValidPass123')
        dto.setPassword2('ValidPass123')

        when:
        userController.nuevoUsuario(dto)

        then:
        1 * userEntityService.nuevoUsuario(_) >> { throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, 'El nombre de usuario ya existe') }
        thrown(ResponseStatusException)
    }

    def 'me returns GetUserDto from authenticated user'() {
        given:
        UserEntity user = new UserEntity()
        user.setId(1L)
        user.setUsername('admin')
        user.setPassword('hashedPassword')
        user.setFullName('Admin User')
        user.setEmail('admin@example.com')
        user.setRoles(Set.of(UserRole.ADMIN))

        when:
        GetUserDto result = userController.me(user)

        then:
        result != null
        result.getUsername() == 'admin'
    }
}
