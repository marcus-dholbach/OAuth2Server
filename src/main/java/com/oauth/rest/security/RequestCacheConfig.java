package com.oauth.rest.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.savedrequest.RequestCache;

/**
 * Configuración para beans de RequestCache de Spring Security.
 * Separado de SecurityConfig para evitar dependencias circulares.
 */
@Configuration
public class RequestCacheConfig {

    @Bean
    public RequestCache requestCache() {
        return new org.springframework.security.web.savedrequest.HttpSessionRequestCache();
    }
}
