package com.oauth.rest.security;

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
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import com.oauth.rest.security.oauth2.OAuth2ParameterSavingFilter;
import com.oauth.rest.security.oauth2.OAuth2SavedRequestAwareAuthSuccessHandler;
import com.oauth.rest.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final PasswordEncoder passwordEncoder;
        private final CustomUserDetailsService customUserDetailsService;
        private final AppAwareAuthenticationProvider appAwareAuthenticationProvider;
        private final OAuth2ParameterSavingFilter oauth2ParameterSavingFilter;
        private final OAuth2SavedRequestAwareAuthSuccessHandler oauth2AuthSuccessHandler;

        public SecurityConfig(PasswordEncoder passwordEncoder,
                        CustomUserDetailsService customUserDetailsService,
                        AppAwareAuthenticationProvider appAwareAuthenticationProvider,
                        OAuth2ParameterSavingFilter oauth2ParameterSavingFilter,
                        OAuth2SavedRequestAwareAuthSuccessHandler oauth2AuthSuccessHandler) {
                this.passwordEncoder = passwordEncoder;
                this.customUserDetailsService = customUserDetailsService;
                this.appAwareAuthenticationProvider = appAwareAuthenticationProvider;
                this.oauth2ParameterSavingFilter = oauth2ParameterSavingFilter;
                this.oauth2AuthSuccessHandler = oauth2AuthSuccessHandler;
        }

        @Bean
        public StrictHttpFirewall httpFirewall() {
                StrictHttpFirewall firewall = new StrictHttpFirewall();
                firewall.setAllowSemicolon(true); // Permitir ; en URLs para OAuth2 con jsessionid
                firewall.setAllowUrlEncodedPercent(true);
                return firewall;
        }

        @Bean
        @Order(2)
        public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Filtro para guardar parámetros OAuth2 en sesión ANTES de que Spring Security
                                // intercepte
                                .addFilterBefore(oauth2ParameterSavingFilter,
                                                UsernamePasswordAuthenticationFilter.class)
                                // ✅ Esta cadena manejará el resto de peticiones
                                .securityMatcher("/**")
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/oauth2/authorize", "/oauth/authorize").permitAll()
                                                .requestMatchers("/oauth/token", "/oauth2/token", "/login",
                                                                "/h2-console/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .successHandler(oauth2AuthSuccessHandler)
                                                .permitAll())
                                .authenticationManager(authenticationManager())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(
                                                                new LoginUrlAuthenticationEntryPoint("/login")))
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/h2-console/**", "/oauth/token",
                                                                "/oauth2/token"));
                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager() {
                return new ProviderManager(appAwareAuthenticationProvider);
        }
}
