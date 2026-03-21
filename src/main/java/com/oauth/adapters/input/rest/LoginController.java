package com.oauth.adapters.input.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador para las páginas de login de Thymeleaf
 * Maneja las plantillas de login e invalid-application
 */
@Controller
public class LoginController {

    private final String contactEmail;

    public LoginController(@Value("${app.contact.email}") String contactEmail) {
        this.contactEmail = contactEmail;
    }


    @GetMapping({ "/login", "/oauth2/login" })
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "client_id", required = false) String clientId,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }

        if (logout != null) {
            model.addAttribute("logout", "Sesión cerrada correctamente");
        }

        if (registered != null) {
            model.addAttribute("registered", "Usuario registrado correctamente");
        }

        // Pasar client_id al modelo (puede ser nulo si no viene en la URL)
        model.addAttribute("clientId", clientId);

        return "login";
    }

    /**
     * Página mostrada cuando el usuario intenta acceder directamente al login
     * sin un redirect_uri válido (flujo OAuth2).
     */
    @GetMapping("/invalid-application")
    public String invalidApplication(Model model) {
        model.addAttribute("contactEmail", contactEmail);
        return "invalid-application";
    }
}
