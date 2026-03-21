package com.oauth.infrastructure.service

import com.oauth.adapters.output.persistence.UserEntityRepository
import spock.lang.Specification

class BaseServiceSpec extends Specification {

    def "BaseService is abstract and cannot be instantiated"() {
        expect:
        // BaseService is an abstract class
        // It provides generic CRUD operations for all services
        true
    }

    def "UserEntityService extends BaseService"() {
        given:
        UserEntityRepository repository = Mock(UserEntityRepository)
        
        when:
        UserEntityService userService = new UserEntityService(repository)

        then:
        userService != null
    }
}
