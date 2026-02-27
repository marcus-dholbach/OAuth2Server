package com.oauth.rest.security.oauth2;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AuthenticationSuccessHandler que restaura los parámetros OAuth2 desde cookies
 * después de un login exitoso.
 */
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        log.info("[OAuth2AuthenticationSuccessHandler] Login exitoso para usuario: {}", authentication.getName());

        // Leer parámetros OAuth2 desde cookies
        String redirectUri = getCookieValue(request, "OAUTH2_REDIRECT_URI");
        String clientId = getCookieValue(request, "OAUTH2_CLIENT_ID");
        String state = getCookieValue(request, "OAUTH2_STATE");
        String scope = getCookieValue(request, "OAUTH2_SCOPE");
        String responseType = getCookieValue(request, "OAUTH2_RESPONSE_TYPE");
        String codeChallenge = getCookieValue(request, "OAUTH2_CODE_CHALLENGE");
        String codeChallengeMethod = getCookieValue(request, "OAUTH2_CODE_CHALLENGE_METHOD");

        log.info(
                "[OAuth2AuthenticationSuccessHandler] Cookies leídas - redirect_uri: {}, client_id: {}, state: {}, scope: {}",
                redirectUri, clientId, state, scope);
        log.info("[OAuth2AuthenticationSuccessHandler] PKCE cookies - code_challenge: {}, code_challenge_method: {}",
                codeChallenge, codeChallengeMethod);

        // Limpiar cookies
        clearCookies(response);
        log.info("[OAuth2AuthenticationSuccessHandler] Cookies limpiadas");

        // Si hay parámetros OAuth2, reconstruir la URL de autorización
        if (redirectUri != null || clientId != null) {
            StringBuilder oauthUrl = new StringBuilder();
            oauthUrl.append("/oauth2/authorize?");

            if (responseType != null) {
                oauthUrl.append("response_type=").append(urlEncode(responseType)).append("&");
            }
            if (clientId != null) {
                oauthUrl.append("client_id=").append(urlEncode(clientId)).append("&");
            }
            if (redirectUri != null) {
                oauthUrl.append("redirect_uri=").append(urlEncode(redirectUri)).append("&");
            }
            if (scope != null) {
                oauthUrl.append("scope=").append(urlEncode(scope)).append("&");
            }
            if (state != null) {
                oauthUrl.append("state=").append(urlEncode(state)).append("&");
            }
            if (codeChallenge != null) {
                oauthUrl.append("code_challenge=").append(urlEncode(codeChallenge)).append("&");
            }
            if (codeChallengeMethod != null) {
                oauthUrl.append("code_challenge_method=").append(urlEncode(codeChallengeMethod));
            }

            // Eliminar último & si existe
            String url = oauthUrl.toString();
            if (url.endsWith("&")) {
                url = url.substring(0, url.length() - 1);
            }

            log.info("[OAuth2AuthenticationSuccessHandler] Redirecting to: {}", url);
            response.sendRedirect(url);
            return;
        }

        log.warn(
                "[OAuth2AuthenticationSuccessHandler] No se encontraron parámetros OAuth2 en cookies, redirigiendo a /");
        // Por defecto, redirigir a la página principal
        response.sendRedirect("/");
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    String value = cookie.getValue();
                    log.debug("[OAuth2AuthenticationSuccessHandler] Cookie {} encontrada con valor: {}", name, value);
                    // URL-decode el valor porque las cookies están codificadas
                    return urlDecode(value);
                }
            }
        }
        log.debug("[OAuth2AuthenticationSuccessHandler] Cookie {} no encontrada", name);
        return null;
    }

    private String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private void clearCookies(HttpServletResponse response) {
        String[] cookieNames = { "OAUTH2_REDIRECT_URI", "OAUTH2_CLIENT_ID", "OAUTH2_STATE", "OAUTH2_SCOPE",
                "OAUTH2_RESPONSE_TYPE", "OAUTH2_CODE_CHALLENGE", "OAUTH2_CODE_CHALLENGE_METHOD" };
        for (String name : cookieNames) {
            Cookie cookie = new Cookie(name, "");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            cookie.setSecure(true);
            response.addCookie(cookie);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
