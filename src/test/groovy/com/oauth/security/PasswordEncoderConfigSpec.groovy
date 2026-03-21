package com.oauth.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import com.oauth.config.PasswordEncoderConfig
import spock.lang.Specification

class PasswordEncoderConfigSpec extends Specification {

    def 'PasswordEncoderConfig creates BCryptPasswordEncoder'() {
        given:
        PasswordEncoderConfig config = new PasswordEncoderConfig()

        when:
        PasswordEncoder encoder = config.passwordEncoder()

        then:
        encoder != null
        encoder instanceof BCryptPasswordEncoder
    }

    def 'PasswordEncoderConfig returns password encoder'() {
        given:
        PasswordEncoderConfig config = new PasswordEncoderConfig()

        when:
        PasswordEncoder encoder = config.passwordEncoder()

        then:
        encoder != null
        encoder instanceof BCryptPasswordEncoder
    }

    def 'PasswordEncoder encodes and matches passwords'() {
        given:
        PasswordEncoderConfig config = new PasswordEncoderConfig()
        PasswordEncoder encoder = config.passwordEncoder()
        String rawPassword = "testPassword123"

        when:
        String encoded = encoder.encode(rawPassword)
        boolean matches = encoder.matches(rawPassword, encoded)

        then:
        encoded != rawPassword
        matches == true
    }
}
