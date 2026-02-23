package com.oauth.rest.config

import com.oauth.rest.Application
import spock.lang.Specification

class ConfigSpec extends Specification {

    def 'Application main class can be instantiated'() {
        when:
        Application app = new Application()

        then:
        app != null
    }
}
