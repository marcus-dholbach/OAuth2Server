package com.oauth.adapters.input

import com.oauth.domain.model.UserEntity
import com.oauth.domain.ports.in.user.UserServicePort
import com.oauth.domain.ports.out.persistence.UserRepositoryPort
import spock.lang.Specification

class UserServiceAdapterSpec extends Specification {

    UserRepositoryPort userRepositoryPort
    UserServiceAdapter userServiceAdapter

    def setup() {
        userRepositoryPort = Mock(UserRepositoryPort)
        userServiceAdapter = new UserServiceAdapter(userRepositoryPort)
    }

    def 'findByUsername returns user when user exists'() {
        given:
        String username = 'admin'
        UserEntity user = new UserEntity()
        user.setUsername(username)
        user.setEmail('admin@oauth.net')

        when:
        def result = userServiceAdapter.findByUsername(username)

        then:
        1 * userRepositoryPort.findByUsername(username) >> Optional.of(user)
        result.isPresent()
        result.get().username == username
    }

    def 'findByUsername returns empty when user does not exist'() {
        given:
        String username = 'nonexistent'

        when:
        def result = userServiceAdapter.findByUsername(username)

        then:
        1 * userRepositoryPort.findByUsername(username) >> Optional.empty()
        !result.isPresent()
    }

    def 'findByEmail returns user when email exists'() {
        given:
        String email = 'admin@oauth.net'
        UserEntity user = new UserEntity()
        user.setUsername('admin')
        user.setEmail(email)

        when:
        def result = userServiceAdapter.findByEmail(email)

        then:
        1 * userRepositoryPort.findByEmail(email) >> Optional.of(user)
        result.isPresent()
        result.get().email == email
    }

    def 'save returns saved user'() {
        given:
        UserEntity user = new UserEntity()
        user.setUsername('testuser')
        user.setEmail('test@example.com')

        when:
        def result = userServiceAdapter.save(user)

        then:
        1 * userRepositoryPort.save(user) >> user
        result == user
    }
}
