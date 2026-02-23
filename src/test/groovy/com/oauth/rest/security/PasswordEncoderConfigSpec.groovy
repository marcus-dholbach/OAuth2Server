package com.oauth.rest.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

class PasswordEncoderConfigSpec extends Specification {

    def 'PasswordEncoderConfig provides BCrypt encoder'() {
        given:
        PasswordEncoderConfig config = new PasswordEncoderConfig()

        when:
        PasswordEncoder encoder = config.passwordEncoder()

        then:
        encoder != null
        encoder instanceof BCryptPasswordEncoder
    }

    def 'BCryptPasswordEncoder can encode and verify passwords'() {
        given:
        PasswordEncoder encoder = new BCryptPasswordEncoder()
        String rawPassword = 'TestPassword123'

        when:
        String encoded = encoder.encode(rawPassword)
        boolean matches = encoder.matches(rawPassword, encoded)

        then:
        encoded != rawPassword
        matches == true
    }

    def 'BCryptPasswordEncoder returns false for wrong password'() {
        given:
        PasswordEncoder encoder = new BCryptPasswordEncoder()
        String rawPassword = 'TestPassword123'
        String wrongPassword = 'WrongPassword'

        when:
        String encoded = encoder.encode(rawPassword)
        boolean matches = encoder.matches(wrongPassword, encoded)

        then:
        matches == false
    }
}
