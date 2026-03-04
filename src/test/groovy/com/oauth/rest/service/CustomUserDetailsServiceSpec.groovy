package com.oauth.rest.service

import com.oauth.rest.model.Application
import com.oauth.rest.model.Role
import com.oauth.rest.model.UserEntity
import com.oauth.rest.model.UsuarioAplicacion
import com.oauth.rest.repository.UserEntityRepository
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
}
