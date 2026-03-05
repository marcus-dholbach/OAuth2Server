package com.oauth.rest.security.oauth2;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class CustomTokenEnhancer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        if (context.getTokenType().getValue().equals("access_token")) {

            // Obtener la autenticación del usuario
            Authentication authentication = context.getPrincipal();

            // Extraer los roles/authorities
            Set<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            // Añadir claims personalizados
            context.getClaims().claims(claims -> {
                claims.put("application", "OAuth2Server");
                claims.put("roles", authorities); // ← AÑADIR LOS ROLES
            });

            // Log para depuración
            System.out.println("=== CustomTokenEnhancer ===");
            System.out.println("Roles añadidos al token: " + authorities);
            System.out.println("===========================");
        }
    }
}