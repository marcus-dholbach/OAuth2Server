package com.oauth.rest.security.oauth2;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Handler personalizado para manejar la autenticación exitosa en el flujo
 * OAuth2.
 * Recupera el redirect_uri desde la sesión (donde se almacenó antes de ir a
 * /login)
 * y redirige al cliente con el código de autorización.
 */
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String OAUTH2_REDIRECT_URI = "OAUTH2_REDIRECT_URI";
    private static final String OAUTH2_CLIENT_ID = "OAUTH2_CLIENT_ID";
    private static final String OAUTH2_STATE = "OAUTH2_STATE";

    private final RegisteredClientRepository registeredClientRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuth2AuthenticationSuccessHandler(RegisteredClientRepository registeredClientRepository) {
        this.registeredClientRepository = registeredClientRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        HttpSession session = request.getSession(false);

        // Obtener parámetros de la sesión (almacenados antes de ir a /login)
        String redirectUri = getParameter(request, session, OAUTH2_REDIRECT_URI, OAuth2ParameterNames.REDIRECT_URI);
        String clientId = getParameter(request, session, OAUTH2_CLIENT_ID, OAuth2ParameterNames.CLIENT_ID);
        String state = getParameter(request, session, OAUTH2_STATE, OAuth2ParameterNames.STATE);

        // Validar redirect_uri
        if (redirectUri == null || redirectUri.isBlank()) {
            // No es una solicitud OAuth2, usar comportamiento por defecto
            response.sendRedirect("/");
            return;
        }

        // Validar cliente
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            sendErrorRedirect(response, redirectUri, "unknown_client");
            return;
        }

        // Verificar que el redirect_uri está registrado
        if (!registeredClient.getRedirectUris().contains(redirectUri)) {
            sendErrorRedirect(response, redirectUri, "invalid_redirect_uri");
            return;
        }

        // Generar código de autorización
        String authorizationCode = generateAuthorizationCode();

        // Construir URL de redirección
        String location = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("code", authorizationCode)
                .build()
                .toUriString();

        if (state != null && !state.isBlank()) {
            location = UriComponentsBuilder.fromUriString(location)
                    .queryParam("state", state)
                    .build()
                    .toUriString();
        }

        // Limpiar sesión
        if (session != null) {
            session.removeAttribute(OAUTH2_REDIRECT_URI);
            session.removeAttribute(OAUTH2_CLIENT_ID);
            session.removeAttribute(OAUTH2_STATE);
        }

        // Redirigir al cliente
        response.sendRedirect(location);
    }

    private String getParameter(HttpServletRequest request, HttpSession session, String sessionAttr, String paramName) {
        // Primero buscar en la sesión
        if (session != null) {
            String value = (String) session.getAttribute(sessionAttr);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        // Luego en los parámetros de la petición
        return request.getParameter(paramName);
    }

    private String generateAuthorizationCode() {
        byte[] codeBytes = new byte[32];
        secureRandom.nextBytes(codeBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeBytes);
    }

    private void sendErrorRedirect(HttpServletResponse response, String redirectUri, String error) throws IOException {
        if (redirectUri != null && !redirectUri.isBlank()) {
            String location = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", error)
                    .build()
                    .toUriString();
            response.sendRedirect(location);
        } else {
            response.sendRedirect("/error?error=" + error);
        }
    }
}
