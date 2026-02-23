package com.oauth.rest.security.oauth2;

import java.security.SecureRandom;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authentication success handler that:
 * 1. Retrieves OAuth2 parameters from the saved request (RequestCache)
 * 2. Generates an authorization code
 * 3. Redirects to the original redirect_uri with the code and state
 */
@Component
public class OAuth2SavedRequestAwareAuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SavedRequestAwareAuthSuccessHandler.class);
    private final RegisteredClientRepository registeredClientRepository;
    private final RequestCache requestCache;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuth2SavedRequestAwareAuthSuccessHandler(RegisteredClientRepository registeredClientRepository,
            RequestCache requestCache) {
        this.registeredClientRepository = registeredClientRepository;
        this.requestCache = requestCache;
        setDefaultTargetUrl("/oauth2/authorize");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            org.springframework.security.core.Authentication authentication) throws java.io.IOException {

        // Try to get saved request from RequestCache
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        logger.info("=== OAuth2SavedRequestAwareAuthSuccessHandler: Login successful");

        String redirectUri = null;
        String clientId = null;
        String state = null;

        if (savedRequest != null) {
            logger.info("    Found saved request: {}", savedRequest.getRedirectUrl());

            // Extract OAuth2 parameters from the saved request
            redirectUri = savedRequest.getParameterValues("redirect_uri") != null
                    ? savedRequest.getParameterValues("redirect_uri")[0]
                    : null;
            clientId = savedRequest.getParameterValues("client_id") != null
                    ? savedRequest.getParameterValues("client_id")[0]
                    : null;
            state = savedRequest.getParameterValues("state") != null
                    ? savedRequest.getParameterValues("state")[0]
                    : null;

            logger.info("    Retrieved from saved request:");
            logger.info("        redirect_uri: {}", redirectUri);
            logger.info("        client_id: {}", clientId);
            logger.info("        state: {}", state);
        }

        // Also try to read from cookies as fallback
        if (redirectUri == null) {
            redirectUri = getCookieValue(request, "OAUTH2_REDIRECT_URI");
            clientId = getCookieValue(request, "OAUTH2_CLIENT_ID");
            state = getCookieValue(request, "OAUTH2_STATE");
            logger.info("    Retrieved from cookies (fallback):");
            logger.info("        redirect_uri: {}", redirectUri);
            logger.info("        client_id: {}", clientId);
            logger.info("        state: {}", state);
        }

        if (redirectUri != null && clientId != null) {
            // Validar el cliente
            RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
            if (registeredClient != null) {
                // Generar código de autorización
                String authorizationCode = generateAuthorizationCode();
                logger.info("        Generated authorization code: {}", authorizationCode);

                // Guardar el código en una cookie
                addCookie(response, "OAUTH2_AUTH_CODE", authorizationCode);

                // Construir URL de redirección
                StringBuilder redirectUrl = new StringBuilder(redirectUri);
                redirectUrl.append(redirectUri.contains("?") ? "&" : "?");
                redirectUrl.append("code=").append(authorizationCode);

                if (state != null && !state.isEmpty()) {
                    redirectUrl.append("&state=").append(state);
                }

                String finalRedirectUrl = redirectUrl.toString();
                logger.info("    Redirecting to: {}", finalRedirectUrl);

                // Limpiar cookies
                clearCookie(response, "OAUTH2_REDIRECT_URI");
                clearCookie(response, "OAUTH2_CLIENT_ID");
                clearCookie(response, "OAUTH2_STATE");

                // Redirigir
                response.sendRedirect(finalRedirectUrl);
                return;
            } else {
                logger.warn("    Client not found: {}", clientId);
            }
        } else {
            logger.warn("    Missing redirect_uri or client_id");
        }

        // Fallback al comportamiento por defecto
        logger.info("    Falling back to default behavior");
        try {
            super.onAuthenticationSuccess(request, response, authentication);
        } catch (Exception e) {
            logger.error("Error in fallback", e);
        }
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void addCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setMaxAge(300); // 5 minutes
        cookie.setSecure(false);
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String generateAuthorizationCode() {
        byte[] codeBytes = new byte[32];
        secureRandom.nextBytes(codeBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeBytes);
    }
}
