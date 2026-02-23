package com.oauth.rest.service

import com.oauth.rest.model.UserEntity
import com.oauth.rest.repository.UserEntityRepository
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

class BaseServiceSpec extends Specification {

    def 'BaseService can be extended'() {
        given:
        UserEntityRepository repository = Mock(UserEntityRepository)
        PasswordEncoder passwordEncoder = Mock(PasswordEncoder)
        
        when:
        UserEntityService service = new UserEntityService(repository, passwordEncoder)

        then:
        service != null
    }

    def 'UserEntityService save method works'() {
        given:
        UserEntityRepository repository = Mock(UserEntityRepository)
        PasswordEncoder passwordEncoder = Mock(PasswordEncoder)
        UserEntityService service = new UserEntityService(repository, passwordEncoder)
        
        UserEntity user = new UserEntity()
        user.setUsername('test')
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
        UserEntityService service = new UserEntityService(repository, passwordEncoder)
        
        UserEntity user = new UserEntity()
        user.setId(1L)
        user.setUsername('test')

        when:
        repository.findById(1L) >> Optional.of(user)
        Optional<UserEntity> result = service.findById(1L)

        then:
        result.isPresent()
        result.get().getId() == 1L
    }

    def 'UserEntityService findAll method works'() {
        given:
        UserEntityRepository repository = Mock(UserEntityRepository)
        PasswordEncoder passwordEncoder = Mock(PasswordEncoder)
        UserEntityService service = new UserEntityService(repository, passwordEncoder)
        
        List<UserEntity> users = [
            new UserEntity(id: 1L, username: 'user1'),
            new UserEntity(id: 2L, username: 'user2')
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
        UserEntityService service = new UserEntityService(repository, passwordEncoder)
        
        UserEntity user = new UserEntity()
        user.setId(1L)

        when:
        service.delete(user)

        then:
        1 * repository.delete(user)
    }
}
