package com.oauth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import com.oauth.infrastructure.service.ApplicationAuthenticationDetailsSource;
import com.oauth.infrastructure.service.ClientIdExtractorFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    // Dependencias necesarias
    private final ApplicationAuthenticationDetailsSource applicationAuthenticationDetailsSource;
    private final ClientIdExtractorFilter clientIdExtractorFilter;

    public SecurityConfig(
            ApplicationAuthenticationDetailsSource applicationAuthenticationDetailsSource,
            ClientIdExtractorFilter clientIdExtractorFilter) {
        this.applicationAuthenticationDetailsSource = applicationAuthenticationDetailsSource;
        this.clientIdExtractorFilter = clientIdExtractorFilter;
        log.debug("[SecurityConfig] Initialized");
    }

    @Bean
    public StrictHttpFirewall httpFirewall() {
        var firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setAllowUrlEncodedSlash(false);
        firewall.setAllowBackSlash(false);
        firewall.setAllowUrlEncodedDoubleSlash(false);
        return firewall;
    }

    @Bean
    @Order(0)
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ForwardedHeaderFilter());
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        
        // Leer orígenes permitidos desde variable de entorno (separados por coma)
        var allowedOriginsEnv = System.getenv("CORS_ALLOWED_ORIGINS");
        if (allowedOriginsEnv == null || allowedOriginsEnv.isBlank()) {
            throw new IllegalStateException("CORS_ALLOWED_ORIGINS environment variable is required");
        }
        var allowedOrigins = allowedOriginsEnv.split(",");
        
        log.info("[SecurityConfig] CORS allowed origins: {}", String.join(", ", allowedOrigins));
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "X-CSRF-TOKEN",
                "Accept"
        ));
        configuration.setExposedHeaders(Arrays.asList("X-CSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // Configurar matcher primero
        http.securityMatcher(
            "/oauth2/authorize",
            "/oauth2/token",
            "/oauth2/jwks",
            "/oauth2/introspect",
            "/oauth2/revoke",
            "/userinfo",
            "/connect/register",
            "/.well-known/**"
        );
        
        // Configurar CORS y CSRF
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .ignoringRequestMatchers(
                    "/oauth2/token",
                    "/oauth2/introspect", 
                    "/oauth2/revoke"
                )
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
            );
        
        // Aplicar configuración OAuth2 (esto incluye su propio authorizeHttpRequests)
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        
        // Habilitar OIDC
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            .oidc(Customizer.withDefaults());

        log.debug("[SecurityConfig] OAuth2 Authorization Server configured");
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // Añadir nuestro filtro personalizado ANTES de UsernamePasswordAuthenticationFilter
            .addFilterBefore(clientIdExtractorFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            
            .securityMatcher(
                "/",
                "/login",
                "/oauth2/login",
                "/logout",
                "/css/**",
                "/js/**",
                "/images/**",
                "/webjars/**",
                "/favicon.ico",
                "/error",
                "/invalid-application",
                "/h2-console/**",
                "/api/**",
                "/user/**"
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                // Ignorar CSRF para el endpoint de login
                .ignoringRequestMatchers("/login")
            )
            .authorizeHttpRequests(authorize -> authorize
                // Rutas públicas
                .requestMatchers(new String[]{
                        "/css/**", 
                        "/js/**", 
                        "/images/**", 
                        "/webjars/**",
                        "/favicon.ico",
                        "/error", 
                        "/invalid-application",
                        "/h2-console/**",
                        "/login", 
                        "/oauth2/login",
                        "/logout"
                }).permitAll()
                // OPTIONS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // API protegidas
                .requestMatchers("/api/**").authenticated()
                .requestMatchers("/user/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .authenticationDetailsSource(applicationAuthenticationDetailsSource)
                // Usar el success handler por defecto de Spring (ya maneja SavedRequest)
                .successHandler(new SavedRequestAwareAuthenticationSuccessHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .invalidSessionUrl("/login")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            );

        log.debug("[SecurityConfig] Default security configured - ClientIdExtractorFilter added");
        return http.build();
    }
}