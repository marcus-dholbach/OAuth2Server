package com.oauth.rest.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.oauth.rest.security.oauth2.OAuth2AuthenticationSuccessHandler;

/**
 * Interceptor que almacena los parámetros OAuth2 en la sesión antes de
 * redirigir a /login.
 * Esto permite que el OAuth2AuthenticationSuccessHandler los recupere después
 * del login.
 */
@Component
public class OAuth2SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String redirectUri = request.getParameter("redirect_uri");
        String clientId = request.getParameter("client_id");
        String state = request.getParameter("state");
        String responseType = request.getParameter("response_type");

        // Solo interceptar si es una solicitud de autorización OAuth2
        if ("code".equals(responseType) && (redirectUri != null || clientId != null)) {

            HttpSession session = request.getSession(true);

            if (redirectUri != null) {
                session.setAttribute("OAUTH2_REDIRECT_URI", redirectUri);
            }
            if (clientId != null) {
                session.setAttribute("OAUTH2_CLIENT_ID", clientId);
            }
            if (state != null) {
                session.setAttribute("OAUTH2_STATE", state);
            }
        }

        return true;
    }
}
