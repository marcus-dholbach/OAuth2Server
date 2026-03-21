package com.oauth.infrastructure.service

import com.oauth.domain.model.UserEntity
import com.oauth.adapters.output.persistence.UserEntityRepository
import spock.lang.Specification

class UserEntityServiceSpec extends Specification {

    UserEntityRepository userEntityRepository
    UserEntityService userEntityService

    def setup() {
        userEntityRepository = Mock(UserEntityRepository)
        userEntityService = new UserEntityService(userEntityRepository)
    }

    def "findUserByUsername returns user when user exists"() {
        given:
        String username = "admin"
        UserEntity user = new UserEntity()
        user.setUsername(username)
        user.setEmail("admin@oauth.net")
        user.setPassword("hashedPassword")

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

    def "findUserByEmail returns user when email exists"() {
        given:
        String email = "admin@oauth.net"
        UserEntity user = new UserEntity()
        user.setUsername("admin")
        user.setEmail(email)
        user.setPassword("hashedPassword")

        when:
        Optional<UserEntity> result = userEntityService.findUserByEmail(email)

        then:
        1 * userEntityRepository.findByEmail(email) >> Optional.of(user)
        result.isPresent()
        result.get().getEmail() == email
    }

    def "findUserByEmail returns empty when email does not exist"() {
        given:
        String email = "nonexistent@example.com"

        when:
        Optional<UserEntity> result = userEntityService.findUserByEmail(email)

        then:
        1 * userEntityRepository.findByEmail(email) >> Optional.empty()
        !result.isPresent()
    }
}
