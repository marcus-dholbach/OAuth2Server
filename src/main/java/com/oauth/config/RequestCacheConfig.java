package com.oauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

/**
 * RequestCache separado para evitar dependencias circulares con SecurityConfig.
 */
@Configuration
public class RequestCacheConfig {

    @Bean
    public RequestCache requestCache() {
        return new HttpSessionRequestCache();
    }
}