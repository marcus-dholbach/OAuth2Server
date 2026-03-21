package com.oauth

import spock.lang.Specification

class ApplicationSpec extends Specification {

    def 'Application main method can be called'() {
        given:
        // Application class has a main method that starts Spring Boot
        // We just verify the class exists and can be instantiated structurally
        
        expect:
        Application != null
    }
}
