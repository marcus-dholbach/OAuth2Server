package com.oauth.adapters.input.rest

import com.oauth.adapters.input.rest.dto.CreateUserDto
import com.oauth.adapters.input.rest.dto.GetUserDto
import com.oauth.adapters.input.rest.mapper.UserDtoMapper
import com.oauth.domain.exception.UserPasswordException
import com.oauth.domain.model.Role
import com.oauth.domain.model.UserEntity
import com.oauth.domain.ports.in.usecase.user.CreateUserUseCasePort
import com.oauth.domain.ports.in.usecase.user.GetUserUseCasePort
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class UserControllerSpec extends Specification {

    CreateUserUseCasePort createUserUseCase
    GetUserUseCasePort getUserUseCase
    UserDtoMapper userDtoMapper
    UserController userController

    def setup() {
        createUserUseCase = Mock(CreateUserUseCasePort)
        getUserUseCase = Mock(GetUserUseCasePort)
        userDtoMapper = new UserDtoMapper()
        userController = new UserController(createUserUseCase, getUserUseCase, userDtoMapper)
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

        when:
        CompletableFuture<GetUserDto> future = userController.nuevoUsuario(dto)
        GetUserDto result = future.get()

        then:
        1 * createUserUseCase.execute('newuser', 'newuser@example.com', 'ValidPass123', 'ValidPass123', 'New User') >> CompletableFuture.completedFuture(user)
        result != null
        result.username() == 'newuser'
        result.id() == 1L
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
        1 * createUserUseCase.execute(_, _, _, _, _) >> CompletableFuture.failedFuture(new UserPasswordException())
        def ex = thrown(ExecutionException)
        ex.cause instanceof UserPasswordException
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

        when:
        GetUserDto result = userController.me(user)

        then:
        result != null
        result.username() == 'admin'
        result.id() == 1L
        result.roles().contains('ROLE_ADMIN')
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
        result.username() == 'nobody'
        result.roles().isEmpty()
    }
}
