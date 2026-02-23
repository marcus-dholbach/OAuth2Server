package com.oauth.rest.security.oauth2;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
public class OAuth2AuthorizationServer {

        @Value("${oauth2.access-token-validity-seconds}")
        private int defaultAccessTokenValiditySeconds;

        @Value("${oauth2.refresh-token-validity-seconds}")
        private int defaultRefreshTokenValiditySeconds;

        private final OAuth2ClientProperties clientProperties;

        public OAuth2AuthorizationServer(OAuth2ClientProperties clientProperties) {
                this.clientProperties = clientProperties;
        }

        @Bean
        @Order(1)
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

                OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer
                                .authorizationServer();

                http
                                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                                .with(authorizationServerConfigurer, Customizer.withDefaults())
                                .authorizeHttpRequests(authorize -> authorize
                                                .anyRequest().authenticated());

                // Habilitar OpenID Connect
                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                                .oidc(Customizer.withDefaults())
                                .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.consentPage(""));

                // Manejo de excepciones
                http.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                                new LoginUrlAuthenticationEntryPoint("/login")));

                return http.build();
        }

        @Bean
        public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
                List<RegisteredClient> clients = new ArrayList<>();

                System.out.println("=== DIAGNÓSTICO CLIENT PROPERTIES ===");
                System.out.println("clientProperties es null? " + (clientProperties == null));
                if (clientProperties != null) {
                        System.out.println("clientProperties.getClients() es null? "
                                        + (clientProperties.getClients() == null));
                        if (clientProperties.getClients() != null) {
                                System.out.println("Número de clientes en properties: "
                                                + clientProperties.getClients().size());
                        }
                }
                // Si hay clientes configurados en properties, usarlos
                if (clientProperties != null && clientProperties.getClients() != null
                                && !clientProperties.getClients().isEmpty()) {

                        for (OAuth2ClientProperties.ClientConfig config : clientProperties.getClients()) {
                                RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
                                                .clientId(config.getClientId())
                                                .clientSecret(passwordEncoder.encode(config.getClientSecret()))
                                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                                .clientSettings(ClientSettings.builder()
                                                                .requireAuthorizationConsent(config.isRequireConsent())
                                                                .requireProofKey(config.isRequireProofKey())
                                                                .build());

                                // Configurar token settings
                                TokenSettings.Builder tokenBuilder = TokenSettings.builder()
                                                .reuseRefreshTokens(false);

                                if (config.getAccessTokenValiditySeconds() != null) {
                                        tokenBuilder.accessTokenTimeToLive(
                                                        Duration.ofSeconds(config.getAccessTokenValiditySeconds()));
                                } else {
                                        tokenBuilder.accessTokenTimeToLive(
                                                        Duration.ofSeconds(defaultAccessTokenValiditySeconds));
                                }

                                if (config.getRefreshTokenValiditySeconds() != null) {
                                        tokenBuilder.refreshTokenTimeToLive(
                                                        Duration.ofSeconds(config.getRefreshTokenValiditySeconds()));
                                } else {
                                        tokenBuilder.refreshTokenTimeToLive(
                                                        Duration.ofSeconds(defaultRefreshTokenValiditySeconds));
                                }

                                builder.tokenSettings(tokenBuilder.build());

                                // Añadir redirect URIs
                                if (config.getRedirectUris() != null) {
                                        for (String redirectUri : config.getRedirectUris()) {
                                                builder.redirectUri(redirectUri);
                                        }
                                }

                                // Añadir scopes
                                if (config.getScopes() != null) {
                                        for (String scope : config.getScopes()) {
                                                builder.scope(scope);
                                        }
                                }

                                clients.add(builder.build());
                        }
                } else {
                        // Clientes por defecto si no hay configuración
                        clients.add(createDefaultClient(passwordEncoder));
                }

                return new InMemoryRegisteredClientRepository(clients);
        }

        private RegisteredClient createDefaultClient(PasswordEncoder passwordEncoder) {
                return RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("default-client")
                                .clientSecret(passwordEncoder.encode("123456"))
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .clientSettings(ClientSettings.builder()
                                                .requireAuthorizationConsent(true)
                                                .requireProofKey(true)
                                                .build())
                                .tokenSettings(TokenSettings.builder()
                                                .accessTokenTimeToLive(
                                                                Duration.ofSeconds(defaultAccessTokenValiditySeconds))
                                                .refreshTokenTimeToLive(
                                                                Duration.ofSeconds(defaultRefreshTokenValiditySeconds))
                                                .reuseRefreshTokens(false)
                                                .build())
                                .scope("openid")
                                .scope("profile")
                                .scope("read")
                                .scope("write")
                                .redirectUri("http://localhost:8080/callback")
                                .build();
        }

        @Bean
        public JWKSource<SecurityContext> jwkSource() {
                KeyPair keyPair = generateRsaKey();
                RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
                RSAKey rsaKey = new RSAKey.Builder(publicKey)
                                .privateKey(privateKey)
                                .keyID(UUID.randomUUID().toString())
                                .build();
                JWKSet jwkSet = new JWKSet(rsaKey);
                return new ImmutableJWKSet<>(jwkSet);
        }

        private static KeyPair generateRsaKey() {
                KeyPair keyPair;
                try {
                        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                        keyPairGenerator.initialize(2048);
                        keyPair = keyPairGenerator.generateKeyPair();
                } catch (Exception ex) {
                        throw new IllegalStateException(ex);
                }
                return keyPair;
        }

        @Bean
        public JwtDecoder jwtDecoder() {
                return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource());
        }

        @Bean
        public JwtEncoder jwtEncoder() {
                return new NimbusJwtEncoder(jwkSource());
        }

        @Bean
        public AuthorizationServerSettings authorizationServerSettings() {
                return AuthorizationServerSettings.builder().build();
        }
}