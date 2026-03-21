package com.oauth.infrastructure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(0)
public class ClientIdExtractorFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ClientIdExtractorFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        var uri = request.getRequestURI();
        var method = request.getMethod();
        
        // Procesar tanto GET como POST para /login
        if ("/login".equals(uri)) {
                                    
            var clientId = request.getParameter("client_id");
            var redirectUri = request.getParameter("redirect_uri");
            var codeChallenge = request.getParameter("code_challenge");
            var state = request.getParameter("state");
            
            if ("GET".equalsIgnoreCase(method)) {
                var session = request.getSession(true);
                
                if (clientId != null && !clientId.isEmpty()) {
                    session.setAttribute("CLIENT_ID", clientId);
                }
                
                if (redirectUri != null && !redirectUri.isEmpty()) {
                    session.setAttribute("REDIRECT_URI", redirectUri);
                }
                
                if (codeChallenge != null && !codeChallenge.isEmpty()) {
                    session.setAttribute("CODE_CHALLENGE", codeChallenge);
                }
                
                if (state != null && !state.isEmpty()) {
                    session.setAttribute("STATE", state);
                }
            }
            
            if ("POST".equalsIgnoreCase(method)) {
                var session = request.getSession(false);
                
                if (session != null) {
                    // Recuperar client_id si no viene en parámetros
                    if (clientId == null || clientId.isEmpty()) {
                        clientId = (String) session.getAttribute("CLIENT_ID");
                    }
                    
                    // Recuperar redirect_uri (siempre de sesión, no suele venir en POST)
                    redirectUri = (String) session.getAttribute("REDIRECT_URI");
                }
                
                // Guardar en request attribute para el resto de la cadena
                if (clientId != null && !clientId.isEmpty()) {
                    request.setAttribute("CLIENT_ID", clientId);
                } else {
                    log.debug("No se puede procesar login sin client_id");
                }
                
                if (redirectUri != null && !redirectUri.isEmpty()) {
                    request.setAttribute("REDIRECT_URI", redirectUri);
                } else {
                    log.debug("No hay redirect_uri, se usará el comportamiento por defecto");
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
}