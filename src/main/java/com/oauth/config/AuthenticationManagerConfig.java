package com.oauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.oauth.infrastructure.service.AppAwareAuthenticationProvider;


@Configuration
public class AuthenticationManagerConfig {

    private final AppAwareAuthenticationProvider appAwareAuthenticationProvider;
    private final UserDetailsService userDetailsService;

    public AuthenticationManagerConfig(
            AppAwareAuthenticationProvider appAwareAuthenticationProvider,
            UserDetailsService userDetailsService) {
        this.appAwareAuthenticationProvider = appAwareAuthenticationProvider;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        // Usar SOLO tu provider personalizado
        return new ProviderManager(appAwareAuthenticationProvider);
    }
}