package com.oauth.rest.service

import com.oauth.rest.model.Role
import com.oauth.rest.model.UserEntity
import com.oauth.rest.repository.UserEntityRepository
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

class BaseServiceSpec extends Specification {

    def 'BaseService can be extended'() {
        given:
        UserEntityRepository repository = Mock(UserEntityRepository)
        PasswordEncoder passwordEncoder = Mock(PasswordEncoder)
        RoleService roleService = Mock(RoleService)
        
        when:
        UserEntityService service = new UserEntityService(repository, passwordEncoder, roleService)

        then:
        service != null
    }

    def 'UserEntityService save method works'() {
        given:
        UserEntityRepository repository = Mock(UserEntityRepository)
        PasswordEncoder passwordEncoder = Mock(PasswordEncoder)
        RoleService roleService = Mock(RoleService)
        UserEntityService service = new UserEntityService(repository, passwordEncoder, roleService)
        
        UserEntity user = new UserEntity()
        user.setUsername('test')
        user.setEmail('test@example.com')
        user.setPassword('encoded')

        when:
        repository.save(user) >> user
        UserEntity result = service.save(user)

        then:
        result.getUsername() == 'test'
    }

    def 'UserEntityService findById method works'() {
        given:
        UserEntityRepository repository = Mock(UserEntityRepository)
        PasswordEncoder passwordEncoder = Mock(PasswordEncoder)
        RoleService roleService = Mock(RoleService)
        UserEntityService service = new UserEntityService(repository, passwordEncoder, roleService)
        
        Long id = 1L
        UserEntity user = new UserEntity()
        user.setId(id)
        user.setUsername('test')
        user.setEmail('test@example.com')

        when:
        repository.findById(id) >> Optional.of(user)
        Optional<UserEntity> result = service.findById(id)

        then:
        result.isPresent()
        result.get().getUsername() == 'test'
    }

    def 'UserEntityService findAll method works'() {
        given:
        UserEntityRepository repository = Mock(UserEntityRepository)
        PasswordEncoder passwordEncoder = Mock(PasswordEncoder)
        RoleService roleService = Mock(RoleService)
        UserEntityService service = new UserEntityService(repository, passwordEncoder, roleService)
        
        List<UserEntity> users = [
            new UserEntity(username: 'user1', email: 'user1@example.com'),
            new UserEntity(username: 'user2', email: 'user2@example.com')
        ]

        when:
        repository.findAll() >> users
        List<UserEntity> result = service.findAll()

        then:
        result.size() == 2
    }

    def 'UserEntityService delete method works'() {
        given:
        UserEntityRepository repository = Mock(UserEntityRepository)
        PasswordEncoder passwordEncoder = Mock(PasswordEncoder)
        RoleService roleService = Mock(RoleService)
        UserEntityService service = new UserEntityService(repository, passwordEncoder, roleService)
        
        UserEntity user = new UserEntity()
        user.setUsername('test')
        user.setEmail('test@example.com')

        when:
        service.delete(user)

        then:
        1 * repository.delete(user)
    }
}
