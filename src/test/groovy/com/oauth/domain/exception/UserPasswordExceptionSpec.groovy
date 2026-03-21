package com.oauth.domain.exception

import spock.lang.Specification

class UserPasswordExceptionSpec extends Specification {

    def 'UserPasswordException can be created with default message'() {
        when:
        def exception = new UserPasswordException()

        then:
        exception != null
        exception.message == "Las contraseñas no coinciden"
    }

    def 'UserPasswordException can be created with custom message'() {
        when:
        def exception = new UserPasswordException("Custom error message")

        then:
        exception != null
        exception.message == "Custom error message"
    }

    def 'UserPasswordException extends RuntimeException'() {
        expect:
        RuntimeException.isAssignableFrom(UserPasswordException)
    }
}
