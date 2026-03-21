package com.oauth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import com.oauth.domain.model.UserEntity;
import com.oauth.infrastructure.service.UserEntityService;

@Configuration
@Slf4j
public class CustomTokenEnhancer {

    private final UserEntityService userService;

    public CustomTokenEnhancer(UserEntityService userService) {
        this.userService = userService;
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return context -> {
            
            // Solo procesar access tokens
            if (!"access_token".equals(context.getTokenType().getValue())) {
                log.debug("No es access_token, saliendo");
                return;
            }

            var username = context.getPrincipal().getName();
            
            // Buscar usuario
            UserEntity user = userService.findUserByUsername(username).orElse(null);
            
            if (user != null) {                
                var email = user.getEmail();
                var name = user.getFullName() != null ? user.getFullName() : username;
                
                // Añadir claims directamente
                context.getClaims().claim("sub", email);
                context.getClaims().claim("email", email);
                context.getClaims().claim("name", name);
                
            } else {
                log.debug("Usuario NO encontrado en BD!");
            }
            
            // Añadir roles
            Set<String> authorities = context.getPrincipal().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            context.getClaims().claim("roles", authorities);
        };
    }
}
