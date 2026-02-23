package com.oauth.rest.service

import com.oauth.rest.model.UserEntity
import com.oauth.rest.model.UserRole
import com.oauth.rest.repository.UserEntityRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import spock.lang.Specification

class CustomUserDetailsServiceSpec extends Specification {

    UserEntityRepository userEntityRepository
    CustomUserDetailsService customUserDetailsService

    def setup() {
        userEntityRepository = Mock(UserEntityRepository)
        customUserDetailsService = new CustomUserDetailsService(userEntityRepository)
    }

    def "loadUserByUsername returns user when user exists"() {
        given:
        String username = "admin"
        UserEntity user = new UserEntity()
        user.setUsername(username)
        user.setPassword("hashedPassword")
        user.setRoles(Set.of(UserRole.ADMIN))

        when:
        def result = customUserDetailsService.loadUserByUsername(username)

        then:
        1 * userEntityRepository.findByUsername(username) >> Optional.of(user)
        result.getUsername() == username
    }

    def "loadUserByUsername throws exception when user does not exist"() {
        given:
        String username = "nonexistent"

        when:
        customUserDetailsService.loadUserByUsername(username)

        then:
        1 * userEntityRepository.findByUsername(username) >> Optional.empty()
        thrown(UsernameNotFoundException)
    }

    def "loadUserByUsernameAndApplication returns user when user exists for app"() {
        given:
        String username = "admin"
        String app = "cine-platform"
        UserEntity user = new UserEntity()
        user.setUsername(username)
        user.setPassword("hashedPassword")
        user.setRoles(Set.of(UserRole.ADMIN))
        user.setApplication(app)

        when:
        def result = customUserDetailsService.loadUserByUsernameAndApplication(username, app)

        then:
        1 * userEntityRepository.findByUsernameAndApplication(username, app) >> Optional.of(user)
        result.getUsername() == username
    }

    def "loadUserByUsernameAndApplication throws exception when user not found for app"() {
        given:
        String username = "admin"
        String app = "cine-platform"

        when:
        customUserDetailsService.loadUserByUsernameAndApplication(username, app)

        then:
        1 * userEntityRepository.findByUsernameAndApplication(username, app) >> Optional.empty()
        thrown(UsernameNotFoundException)
    }
}
