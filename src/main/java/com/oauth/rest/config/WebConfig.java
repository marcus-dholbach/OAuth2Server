package com.oauth.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.oauth.rest.security.oauth2.OAuth2SessionInterceptor;

@Configuration
@EnableJpaAuditing
public class WebConfig implements WebMvcConfigurer {

    private final OAuth2SessionInterceptor oauth2SessionInterceptor;

    public WebConfig(OAuth2SessionInterceptor oauth2SessionInterceptor) {
        this.oauth2SessionInterceptor = oauth2SessionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(oauth2SessionInterceptor)
                .addPathPatterns("/oauth2/authorize", "/oauth/authorize");
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
