package com.oauth.rest.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import com.oauth.rest.security.oauth2.OAuth2ParameterSavingFilter;
import com.oauth.rest.security.oauth2.OAuth2SavedRequestAwareAuthSuccessHandler;
import com.oauth.rest.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.oauth.rest.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

        @SuppressWarnings("unused")
        private final PasswordEncoder passwordEncoder;
        @SuppressWarnings("unused")
        private final CustomUserDetailsService customUserDetailsService;
        private final AppAwareAuthenticationProvider appAwareAuthenticationProvider;
        private final OAuth2ParameterSavingFilter oauth2ParameterSavingFilter;
        private final OAuth2SavedRequestAwareAuthSuccessHandler oauth2AuthSuccessHandler;
        private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

        public SecurityConfig(PasswordEncoder passwordEncoder,
                        CustomUserDetailsService customUserDetailsService,
                        AppAwareAuthenticationProvider appAwareAuthenticationProvider,
                        OAuth2ParameterSavingFilter oauth2ParameterSavingFilter,
                        OAuth2SavedRequestAwareAuthSuccessHandler oauth2AuthSuccessHandler,
                        OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler) {
                this.passwordEncoder = passwordEncoder;
                this.customUserDetailsService = customUserDetailsService;
                this.appAwareAuthenticationProvider = appAwareAuthenticationProvider;
                this.oauth2ParameterSavingFilter = oauth2ParameterSavingFilter;
                this.oauth2AuthSuccessHandler = oauth2AuthSuccessHandler;
                this.oauth2AuthenticationSuccessHandler = oauth2AuthenticationSuccessHandler;
                log.info("[SecurityConfig] OAuth2AuthenticationSuccessHandler inyectado: {}",
                                oauth2AuthenticationSuccessHandler.getClass().getName());
        }

        @Bean
        public StrictHttpFirewall httpFirewall() {
                StrictHttpFirewall firewall = new StrictHttpFirewall();
                firewall.setAllowSemicolon(true);
                firewall.setAllowUrlEncodedPercent(true);
                firewall.setAllowUrlEncodedSlash(true);
                firewall.setAllowBackSlash(true);
                firewall.setAllowUrlEncodedDoubleSlash(true);
                return firewall;
        }

        @Bean
        public AuthenticationManager authenticationManager() {
                return new ProviderManager(appAwareAuthenticationProvider);
        }

        // PRIMERO: Rutas públicas (estáticas, error)
        @Bean
        @Order(1)
        public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
                http.securityMatcher(
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/h2-console/**",
                                "/favicon.ico",
                                "/error")
                                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                                .csrf(csrf -> csrf.disable());

                return http.build();
        }

        // SEGUNDO: Rutas OAuth2 delegadas al Authorization Server
        // El OAuth2AuthorizationServer ya configura los filtros necesarios para
        // /oauth2/token
        // No necesitamos un filter chain personalizado aquí - lo maneja
        // authorizationServerSecurityFilterChain
        // Eliminamos este bean para que no interfiera con el Authorization Server
        // @Bean
        // @Order(2)
        // public SecurityFilterChain oauthFilterChain(HttpSecurity http) throws
        // Exception {
        // http.securityMatcher("/oauth/token", "/oauth2/token")
        // .addFilterBefore(oauth2ParameterSavingFilter,
        // UsernamePasswordAuthenticationFilter.class)
        // .authorizeHttpRequests(authz -> authz
        // .requestMatchers("/oauth/token", "/oauth2/token").permitAll()
        // .anyRequest().authenticated())
        // .csrf(csrf -> csrf
        // .ignoringRequestMatchers("/oauth/token", "/oauth2/token"));
        //
        // return http.build();
        // }

        // SEGUNDO: Login y todo lo demás (form login para autenticación)
        @Bean
        @Order(2)
        public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
                http.securityMatcher(
                                "/",
                                "/login",
                                "/api/**",
                                "/user/**")
                                .addFilterBefore(oauth2ParameterSavingFilter,
                                                UsernamePasswordAuthenticationFilter.class)
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/oauth2/authorize").authenticated()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .successHandler(oauth2AuthenticationSuccessHandler)
                                                .failureUrl("/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login?logout=true")
                                                .permitAll())
                                .sessionManagement(session -> session
                                                .sessionFixation().migrateSession()
                                                .invalidSessionUrl("/login"))
                                .csrf(csrf -> csrf.disable());

                return http.build();
        }
}