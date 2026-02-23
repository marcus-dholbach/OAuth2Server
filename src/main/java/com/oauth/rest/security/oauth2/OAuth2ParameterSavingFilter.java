package com.oauth.rest.security.oauth2;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Filtro que guarda los parámetros OAuth2 en una cookie antes de que Spring
 * Security
 * redirija a /login. Esto permite que el AuthenticationSuccessHandler los
 * recupere
 * después del login exitoso.
 */
@Component
public class OAuth2ParameterSavingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2ParameterSavingFilter.class);
    private static final int COOKIE_MAX_AGE = 300; // 5 minutos

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Solo procesar solicitudes al endpoint de autorización OAuth2
        String requestUri = request.getRequestURI();
        if (requestUri.endsWith("/oauth2/authorize") || requestUri.endsWith("/oauth/authorize")) {

            String responseType = request.getParameter("response_type");
            String redirectUri = request.getParameter("redirect_uri");
            String clientId = request.getParameter("client_id");
            String state = request.getParameter("state");
            String scope = request.getParameter("scope");

            logger.info("=== OAuth2ParameterSavingFilter: Petition received to {}", requestUri);
            logger.info("    response_type: {}", responseType);
            logger.info("    client_id: {}", clientId);
            logger.info("    redirect_uri: {}", redirectUri);
            logger.info("    state: {}", state);

            // Solo guardar si es una solicitud de código de autorización
            if ("code".equals(responseType) && (redirectUri != null || clientId != null)) {

                // Guardar en cookies (persistentes entre sesiones)
                if (redirectUri != null && !redirectUri.isBlank()) {
                    addCookie(response, "OAUTH2_REDIRECT_URI", redirectUri);
                    logger.info("    Saved OAUTH2_REDIRECT_URI to cookie: {}", redirectUri);
                }
                if (clientId != null && !clientId.isBlank()) {
                    addCookie(response, "OAUTH2_CLIENT_ID", clientId);
                    logger.info("    Saved OAUTH2_CLIENT_ID to cookie: {}", clientId);
                }
                if (state != null && !state.isBlank()) {
                    addCookie(response, "OAUTH2_STATE", state);
                    logger.info("    Saved OAUTH2_STATE to cookie: {}", state);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void addCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(false); // Necesitamos acceder desde JavaScript si es necesario
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setSecure(false); // Set to true in production with HTTPS
        response.addCookie(cookie);
    }
}
