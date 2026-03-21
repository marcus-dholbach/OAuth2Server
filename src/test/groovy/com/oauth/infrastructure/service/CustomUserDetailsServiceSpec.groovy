package com.oauth.infrastructure.service

import com.oauth.domain.model.Application
import com.oauth.domain.model.Role
import com.oauth.domain.model.UserEntity
import com.oauth.domain.model.UsuarioAplicacion
import com.oauth.adapters.output.persistence.UserEntityRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import spock.lang.Specification

class CustomUserDetailsServiceSpec extends Specification {

    UserEntityRepository userEntityRepository
    ApplicationService applicationService
    UsuarioAplicacionService usuarioAplicacionService
    CustomUserDetailsService customUserDetailsService

    def setup() {
        userEntityRepository = Mock(UserEntityRepository)
        applicationService = Mock(ApplicationService)
        usuarioAplicacionService = Mock(UsuarioAplicacionService)
        customUserDetailsService = new CustomUserDetailsService(
            userEntityRepository, 
            applicationService, 
            usuarioAplicacionService
        )
    }

    def "loadUserByUsername returns user when user exists"() {
        given:
        String username = "admin"
        UserEntity user = new UserEntity()
        user.setUsername(username)
        user.setEmail("admin@oauth.net")
        user.setPassword("hashedPassword")
        user.setRoles(Set.of(new Role('ROLE_ADMIN', 'Administrador')))

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
        String appClientId = "cine-platform"
        UserEntity user = new UserEntity()
        user.setId(1L)
        user.setUsername(username)
        user.setEmail("admin@oauth.net")
        user.setPassword("hashedPassword")
        user.setRoles(Set.of(new Role('ROLE_ADMIN', 'Administrador')))
        
        Application app = new Application()
        app.setId(1L)
        app.setClientId(appClientId)

        when:
        def result = customUserDetailsService.loadUserByUsernameAndApplication(username, appClientId)

        then:
        1 * userEntityRepository.findByUsername(username) >> Optional.of(user)
        1 * applicationService.findByClientId(appClientId) >> Optional.of(app)
        1 * usuarioAplicacionService.findByUsuarioIdAndApplicationId(1L, 1L) >> Optional.of(new UsuarioAplicacion())
        result.getUsername() == username
    }

    def "loadUserByUsernameAndApplication throws exception when user not registered for app"() {
        given:
        String username = "admin"
        String appClientId = "cine-platform"
        UserEntity user = new UserEntity()
        user.setId(1L)
        user.setUsername(username)
        user.setEmail("admin@oauth.net")
        
        Application app = new Application()
        app.setId(1L)
        app.setClientId(appClientId)

        when:
        customUserDetailsService.loadUserByUsernameAndApplication(username, appClientId)

        then:
        1 * userEntityRepository.findByUsername(username) >> Optional.of(user)
        1 * applicationService.findByClientId(appClientId) >> Optional.of(app)
        1 * usuarioAplicacionService.findByUsuarioIdAndApplicationId(1L, 1L) >> Optional.empty()
        thrown(UsernameNotFoundException)
    }

    def "loadUserByUsernameAndApplication throws exception when user does not exist"() {
        given:
        String username = "nonexistent"
        String appClientId = "cine-platform"

        when:
        customUserDetailsService.loadUserByUsernameAndApplication(username, appClientId)

        then:
        1 * userEntityRepository.findByUsername(username) >> Optional.empty()
        thrown(UsernameNotFoundException)
    }

    def "isUserRegisteredInApplication returns true when user is registered"() {
        given:
        String username = "admin"
        String appClientId = "cine-platform"
        UserEntity user = new UserEntity()
        user.setId(1L)
        
        Application app = new Application()
        app.setId(1L)

        when:
        def result = customUserDetailsService.isUserRegisteredInApplication(username, appClientId)

        then:
        1 * userEntityRepository.findByUsername(username) >> Optional.of(user)
        1 * applicationService.findByClientId(appClientId) >> Optional.of(app)
        1 * usuarioAplicacionService.findByUsuarioIdAndApplicationId(1L, 1L) >> Optional.of(new UsuarioAplicacion())
        result == true
    }

    def "isUserRegisteredInApplication returns false when user is not registered"() {
        given:
        String username = "admin"
        String appClientId = "cine-platform"
        UserEntity user = new UserEntity()
        user.setId(1L)
        
        Application app = new Application()
        app.setId(1L)

        when:
        def result = customUserDetailsService.isUserRegisteredInApplication(username, appClientId)

        then:
        1 * userEntityRepository.findByUsername(username) >> Optional.of(user)
        1 * applicationService.findByClientId(appClientId) >> Optional.of(app)
        1 * usuarioAplicacionService.findByUsuarioIdAndApplicationId(1L, 1L) >> Optional.empty()
        result == false
    }

    def "isUserRegisteredInApplication returns false when user does not exist"() {
        given:
        String username = "nonexistent"
        String appClientId = "cine-platform"

        when:
        def result = customUserDetailsService.isUserRegisteredInApplication(username, appClientId)

        then:
        1 * userEntityRepository.findByUsername(username) >> Optional.empty()
        result == false
    }
}
