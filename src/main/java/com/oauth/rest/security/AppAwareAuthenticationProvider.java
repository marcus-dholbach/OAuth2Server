package com.oauth.rest.security;

import com.oauth.rest.service.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * AuthenticationProvider que filtra usuarios por aplicación
 * Usa el client_id de la solicitud OAuth2 para determinar la aplicación
 */
@Component
public class AppAwareAuthenticationProvider implements AuthenticationProvider {

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
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        // Obtener la aplicación del contexto de seguridad
        String application = obtenerApplicationDelContexto();

        if (application == null) {
            // Fallback: buscar sin filtro de aplicación (compatibilidad hacia atrás)
            UserDetails user = userDetailsService.loadUserByUsername(username);
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new BadCredentialsException("Credenciales incorrectas");
            }
            return new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
        }

        // Buscar usuario filtrado por aplicación
        UserDetails user = userDetailsService.loadUserByUsernameAndApplication(username, application);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        return new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
    }

    /**
     * Obtiene la aplicación del contexto de seguridad
     * Se configura previamente mediante un filtro o desde el SecurityContextHolder
     */
    private String obtenerApplicationDelContexto() {
        // Intentar obtener el client_id del contexto de seguridad
        // En Spring Authorization Server, podemos acceder al contexto actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof String) {
            return (String) auth.getDetails();
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
