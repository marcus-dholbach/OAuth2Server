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
import jakarta.servlet.http.HttpSession;

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

        // Intentar obtener parámetros de la sesión primero (más seguro entre dominios)
        HttpSession session = request.getSession(false);
        String redirectUri = null;
        String clientId = null;
        String state = null;
        String scope = null;
        String responseType = null;
        String codeChallenge = null;
        String codeChallengeMethod = null;

        if (session != null) {
            redirectUri = (String) session.getAttribute("OAUTH2_REDIRECT_URI");
            clientId = (String) session.getAttribute("OAUTH2_CLIENT_ID");
            state = (String) session.getAttribute("OAUTH2_STATE");
            scope = (String) session.getAttribute("OAUTH2_SCOPE");
            responseType = (String) session.getAttribute("OAUTH2_RESPONSE_TYPE");
            codeChallenge = (String) session.getAttribute("OAUTH2_CODE_CHALLENGE");
            codeChallengeMethod = (String) session.getAttribute("OAUTH2_CODE_CHALLENGE_METHOD");

            log.info("[OAuth2AuthenticationSuccessHandler] Parámetros recuperados de sesión");

            // Limpiar sesión
            session.removeAttribute("OAUTH2_REDIRECT_URI");
            session.removeAttribute("OAUTH2_CLIENT_ID");
            session.removeAttribute("OAUTH2_STATE");
            session.removeAttribute("OAUTH2_SCOPE");
            session.removeAttribute("OAUTH2_RESPONSE_TYPE");
            session.removeAttribute("OAUTH2_CODE_CHALLENGE");
            session.removeAttribute("OAUTH2_CODE_CHALLENGE_METHOD");
        }

        // Fallback a cookies si no hay sesión (compatibilidad)
        if (redirectUri == null) {
            redirectUri = getCookieValue(request, "OAUTH2_REDIRECT_URI");
        }
        if (clientId == null) {
            clientId = getCookieValue(request, "OAUTH2_CLIENT_ID");
        }
        if (state == null) {
            state = getCookieValue(request, "OAUTH2_STATE");
        }
        if (scope == null) {
            scope = getCookieValue(request, "OAUTH2_SCOPE");
        }
        if (responseType == null) {
            responseType = getCookieValue(request, "OAUTH2_RESPONSE_TYPE");
        }
        if (codeChallenge == null) {
            codeChallenge = getCookieValue(request, "OAUTH2_CODE_CHALLENGE");
        }
        if (codeChallengeMethod == null) {
            codeChallengeMethod = getCookieValue(request, "OAUTH2_CODE_CHALLENGE_METHOD");
        }

        log.info(
                "[OAuth2AuthenticationSuccessHandler] Parámetros finales - redirect_uri: {}, client_id: {}, state: {}, scope: {}",
                redirectUri, clientId, state, scope);
        log.info("[OAuth2AuthenticationSuccessHandler] PKCE - code_challenge: {}, code_challenge_method: {}",
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

            log.info("[OAuth2AuthenticationSuccessHandler] Redirecting to OAuth2 authorize: {}", url);
            response.sendRedirect(url);
            return;
        }

        // No hay parámetros OAuth2 - el usuario accedió directamente al login sin
        // redirect_uri
        // Redirigir a la página de información en lugar de mostrar un 404
        log.warn("[OAuth2AuthenticationSuccessHandler] Acceso directo al login sin redirect_uri. "
                + "Usuario: {}. Redirigiendo a /invalid-application", authentication.getName());
        response.sendRedirect("/invalid-application");
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    String value = cookie.getValue();
                    log.debug("[OAuth2AuthenticationSuccessHandler] Cookie {} encontrada", name);
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
            cookie.setDomain(null); // Importante: sin dominio específico
            response.addCookie(cookie);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}