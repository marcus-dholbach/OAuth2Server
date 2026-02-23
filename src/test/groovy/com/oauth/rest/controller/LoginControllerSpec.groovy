package com.oauth.rest.controller

import org.springframework.ui.Model
import spock.lang.Specification
import org.springframework.test.util.ReflectionTestUtils

class LoginControllerSpec extends Specification {

    LoginController loginController

    def setup() {
        loginController = new LoginController()
    }

    def "GET /login returns login view without error"() {
        given:
        Model model = Mock(Model)

        when:
        String viewName = loginController.login(null, null, null, model)

        then:
        viewName == "login"
        0 * model._
    }

    def "GET /login with error parameter adds error to model"() {
        given:
        Model model = Mock(Model)

        when:
        String viewName = loginController.login("error", null, null, model)

        then:
        viewName == "login"
        1 * model.addAttribute("error", "Usuario o contraseña incorrectos")
    }

    def "GET /login with logout parameter adds logout message to model"() {
        given:
        Model model = Mock(Model)

        when:
        String viewName = loginController.login(null, "logout", null, model)

        then:
        viewName == "login"
        1 * model.addAttribute("logout", "Sesión cerrada correctamente")
    }

    def "GET /login with registered parameter adds registered message to model"() {
        given:
        Model model = Mock(Model)

        when:
        String viewName = loginController.login(null, null, "registered", model)

        then:
        viewName == "login"
        1 * model.addAttribute("registered", "Usuario registrado correctamente")
    }
}
