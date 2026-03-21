package com.oauth.infrastructure.service;

import com.oauth.domain.model.ApplicationDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AppAwareAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(AppAwareAuthenticationProvider.class);

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AppAwareAuthenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        var username = authentication.getName();
        var password = authentication.getCredentials() != null ? 
            authentication.getCredentials().toString() : "null";
        
        // ========== DETALLES DE AUTENTICACIÓN ==========
        var details = authentication.getDetails();
        
        if (details != null) {
            
            // Si es un Map, mostrar solo las keys (no valores para evitar exponer datos sensibles)
            if (details instanceof java.util.Map) {
                java.util.Map<?, ?> map = (java.util.Map<?, ?>) details;
                log.debug("Authentication details keys: {}", map.keySet());
            }
        }
        
        // ========== EXTRACCIÓN DE CLIENT_ID ==========
        var application = extractClientId(authentication);

        try {
            // ========== BÚSQUEDA DE USUARIO ==========
            UserDetails user;
            if (StringUtils.hasText(application)) {
                try {
                    user = userDetailsService.loadUserByUsernameAndApplication(username, application);
                } catch (UsernameNotFoundException e) {
                    throw e;
                }
            } else {
                log.debug("Buscando usuario global (sin aplicación)");
                try {
                    user = userDetailsService.loadUserByUsername(username);
                } catch (UsernameNotFoundException e) {
                    throw e;
                }
            }

            var passwordMatches = passwordEncoder.matches(password, user.getPassword());
            
            if (!passwordMatches) {
                throw new BadCredentialsException("Invalid credentials");
            }

            var authenticatedToken = 
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authenticatedToken.setDetails(authentication.getDetails());
            
            log.debug("Token de autenticación creado exitosamente");
            
            return authenticatedToken;

        } catch (UsernameNotFoundException e) {
            log.error("Usuario no encontrado: '{}' para aplicación: '{}' - {}", username, application, e.getMessage());
            throw new BadCredentialsException("Invalid credentials");
        } catch (Exception e) {
            log.error("Error inesperado durante autenticación: {}", e.getMessage(), e);
            throw new BadCredentialsException("Authentication failed: " + e.getMessage());
        }
    }

    private String extractClientId(Authentication authentication) {
        Object details = authentication.getDetails();
        
        // Caso 1: Es ApplicationDetails
        if (details instanceof ApplicationDetails appDetails) {
            var clientId = appDetails.clientId();
            log.debug("Extracted clientId from ApplicationDetails");
            return clientId;
        }
        
        // Caso 2: Es un Map (posiblemente de Spring)
        if (details instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) details;
            var clientId = map.get("client_id");
            if (clientId != null) {
                log.debug("Extracted clientId from Map");
                return clientId.toString();
            }
        }
        
        // Caso 3: Es un String
        if (details instanceof String) {
            log.debug("Details is a String, not ApplicationDetails");
            // Podría ser el client_id directamente
            return (String) details;
        }
        
        log.debug("No se pudo extraer clientId. Details class: {}", 
            details != null ? details.getClass().getName() : "null");
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        var supports = UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        log.debug("AppAwareAuthenticationProvider.supports({}) = {}", authentication.getSimpleName(), supports);
        return supports;
    }
}