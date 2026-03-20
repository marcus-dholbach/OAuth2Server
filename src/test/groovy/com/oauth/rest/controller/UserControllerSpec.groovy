package com.oauth.rest.controller

import com.oauth.rest.dto.CreateUserDto
import com.oauth.rest.dto.GetUserDto
import com.oauth.rest.exception.UserPasswordException
import com.oauth.rest.mapper.UserDtoMapper
import com.oauth.rest.model.Role
import com.oauth.rest.model.UserEntity
import com.oauth.rest.service.UserEntityService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

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
        user.setId(1L)
        user.setUsername(dto.getUsername())
        user.setPassword('hashedPassword')
        user.setFullName(dto.getFullName())
        user.setEmail(dto.getEmail())
        user.setRoles(Set.of(new Role('ROLE_USER', 'Usuario estándar')))

        GetUserDto expectedDto = new GetUserDto()
        expectedDto.setId(1L)
        expectedDto.setUsername('newuser')
        expectedDto.setEmail('newuser@example.com')
        expectedDto.setFullName('New User')
        expectedDto.setRoles(Set.of('ROLE_USER'))

        when:
        CompletableFuture<GetUserDto> future = userController.nuevoUsuario(dto)
        GetUserDto result = future.get()

        then:
        1 * userEntityService.nuevoUsuario(dto) >> CompletableFuture.completedFuture(user)
        result != null
        result.getUsername() == 'newuser'
        result.getId() == 1L
    }

    def 'nuevoUsuario throws exception when passwords do not match'() {
        given:
        CreateUserDto dto = new CreateUserDto()
        dto.setUsername('newuser')
        dto.setPassword('ValidPass123')
        dto.setPassword2('DifferentPass123')

        when:
        CompletableFuture<GetUserDto> future = userController.nuevoUsuario(dto)
        future.get()

        then:
        1 * userEntityService.nuevoUsuario(dto) >> CompletableFuture.failedFuture(new RuntimeException())
        def ex = thrown(ExecutionException)
        ex.cause instanceof RuntimeException
    }

    def 'nuevoUsuario throws exception when username already exists'() {
        given:
        CreateUserDto dto = new CreateUserDto()
        dto.setUsername('existinguser')
        dto.setPassword('ValidPass123')
        dto.setPassword2('ValidPass123')

        when:
        CompletableFuture<GetUserDto> future = userController.nuevoUsuario(dto)
        future.get()

        then:
        1 * userEntityService.nuevoUsuario(_) >> CompletableFuture.failedFuture(
            new ResponseStatusException(HttpStatus.BAD_REQUEST, 'El nombre de usuario ya existe')
        )
        def ex = thrown(ExecutionException)
        ex.cause instanceof ResponseStatusException
        ((ResponseStatusException) ex.cause).getStatusCode() == HttpStatus.BAD_REQUEST
    }

    def 'nuevoUsuario handles generic service exception'() {
        given:
        CreateUserDto dto = new CreateUserDto()
        dto.setUsername('newuser')
        dto.setPassword('ValidPass123')
        dto.setPassword2('ValidPass123')

        when:
        CompletableFuture<GetUserDto> future = userController.nuevoUsuario(dto)
        future.get()

        then:
        1 * userEntityService.nuevoUsuario(dto) >> CompletableFuture.failedFuture(new RuntimeException("Service error"))
        def ex = thrown(ExecutionException)
        ex.cause instanceof RuntimeException
        ex.cause.message == "Service error"
    }

    def 'me returns GetUserDto from authenticated user'() {
        given:
        UserEntity user = new UserEntity()
        user.setId(1L)
        user.setUsername('admin')
        user.setEmail('admin@example.com')
        user.setPassword('hashedPassword')
        user.setFullName('Admin User')
        user.setRoles(Set.of(new Role('ROLE_ADMIN', 'Administrador')))

        GetUserDto expectedDto = new GetUserDto()
        expectedDto.setId(1L)
        expectedDto.setUsername('admin')
        expectedDto.setEmail('admin@example.com')
        expectedDto.setFullName('Admin User')
        expectedDto.setRoles(Set.of('ROLE_ADMIN'))

        when:
        GetUserDto result = userController.me(user)

        then:
        result != null
        result.getUsername() == 'admin'
        result.getId() == 1L
        result.getRoles().contains('ROLE_ADMIN')
    }

    def 'me returns empty roles when user has no roles'() {
        given:
        UserEntity user = new UserEntity()
        user.setId(1L)
        user.setUsername('nobody')
        user.setEmail('nobody@example.com')
        user.setPassword('hashedPassword')
        user.setFullName('No Body')
        user.setRoles(new HashSet<>())

        when:
        GetUserDto result = userController.me(user)

        then:
        result != null
        result.getUsername() == 'nobody'
        result.getRoles().isEmpty()
    }
}