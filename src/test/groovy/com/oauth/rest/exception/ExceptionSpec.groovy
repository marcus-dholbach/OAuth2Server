package com.oauth.rest.exception

import spock.lang.Specification

class ExceptionSpec extends Specification {

    def 'UserPasswordException can be created'() {
        when:
        def exception = new UserPasswordException()

        then:
        exception != null
    }

    def 'UserPasswordException extends RuntimeException'() {
        expect:
        RuntimeException.isAssignableFrom(UserPasswordException)
    }
}
