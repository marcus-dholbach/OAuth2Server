package com.oauth.application.usecase.user

import com.oauth.domain.exception.UserPasswordException
import com.oauth.domain.model.Role
import com.oauth.domain.model.UserEntity
import com.oauth.domain.ports.in.role.RoleServicePort
import com.oauth.domain.ports.in.usecase.user.CreateUserUseCasePort
import com.oauth.domain.ports.in.user.UserServicePort
import com.oauth.domain.ports.out.security.PasswordEncoderPort
import spock.lang.Specification

class CreateUserUseCaseSpec extends Specification {

    UserServicePort userService
    RoleServicePort roleService
    PasswordEncoderPort passwordEncoder
    CreateUserUseCase createUserUseCase

    def setup() {
        userService = Mock(UserServicePort)
        roleService = Mock(RoleServicePort)
        passwordEncoder = Mock(PasswordEncoderPort)
        createUserUseCase = new CreateUserUseCase(userService, roleService, passwordEncoder)
    }

    def 'execute throws UserPasswordException when passwords do not match'() {
        when:
        createUserUseCase.execute('testuser', 'test@example.com', 'Password123', 'DifferentPass', 'Test User').get()

        then:
        def ex = thrown(Exception)
        ex.cause instanceof UserPasswordException
    }

    def 'execute creates user with valid credentials'() {
        given:
        Role userRole = new Role('ROLE_USER', 'Usuario estándar')
        UserEntity savedUser = new UserEntity()
        savedUser.setId(1L)
        savedUser.setUsername('testuser')
        savedUser.setEmail('test@example.com')
        savedUser.setFullName('Test User')

        when:
        def result = createUserUseCase.execute('testuser', 'test@example.com', 'Password123', 'Password123', 'Test User').get()

        then:
        1 * passwordEncoder.encode('Password123') >> 'encodedPassword'
        1 * roleService.findOrCreateRole('ROLE_USER', 'Usuario estándar') >> userRole
        1 * userService.save(_) >> savedUser
        result.username == 'testuser'
    }

    def 'execute throws exception when username already exists'() {
        given:
        Role userRole = new Role('ROLE_USER', 'Usuario estándar')
        
        when:
        createUserUseCase.execute('existinguser', 'test@example.com', 'Password123', 'Password123', 'Test User').get()

        then:
        1 * passwordEncoder.encode(_) >> 'encodedPassword'
        1 * roleService.findOrCreateRole('ROLE_USER', _) >> userRole
        1 * userService.save(_) >> { throw new org.springframework.dao.DataIntegrityViolationException('Duplicate key') }
        
        def ex = thrown(Exception)
        ex.cause != null
    }
}
