package com.oauth.rest.security.oauth2;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
@Slf4j
public class OAuth2AuthorizationServer {

    @Value("${oauth2.access-token-validity-seconds:3600}")
    private int accessTokenValiditySeconds;

    @Value("${oauth2.refresh-token-validity-seconds:7200}")
    private int refreshTokenValiditySeconds;

    @Value("${ISSUER_URL:http://localhost:8080}")
    private String issuerUrl;

    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
        List<RegisteredClient> clients = new ArrayList<>();
        
        // Obtener los prefijos de clientes desde variable de entorno
        String clientsConfig = System.getenv("OAUTH2_CLIENTS");
        if (clientsConfig != null && !clientsConfig.isBlank()) {
            String[] prefixes = clientsConfig.split(",");
            for (String prefix : prefixes) {
                String trimmedPrefix = prefix.trim();
                if (!trimmedPrefix.isEmpty()) {
                    addClientIfConfigured(clients, passwordEncoder, trimmedPrefix);
                }
            }
        }
        
        if (clients.isEmpty()) {
            log.error("No OAuth2 clients configured. Set environment variable OAUTH2_CLIENTS with comma-separated client prefixes (e.g., CINE_PLATFORM,TRANSCRIBERAPP)");
            throw new IllegalStateException("No OAuth2 clients configured");
        }
        
        log.debug("Registered {} OAuth2 clients", clients.size());
        return new InMemoryRegisteredClientRepository(clients);
    }

    private void addClientIfConfigured(List<RegisteredClient> clients, PasswordEncoder passwordEncoder, String prefix) {
        String secret = System.getenv(prefix + "_SECRET");
        String redirectUri = System.getenv(prefix + "_REDIRECT_URI");
        String clientId = prefix.toLowerCase().replace('_', '-');
        
        if (secret != null && redirectUri != null && !secret.isBlank() && !redirectUri.isBlank()) {
            clients.add(createRegisteredClient(clientId, secret, redirectUri, passwordEncoder));
            log.debug("Client '{}' configured", clientId);
        } else {
            log.debug("Client '{}' not configured: missing environment variables", clientId);
        }
    }

    private RegisteredClient createRegisteredClient(String clientId, String secret, String redirectUri, 
                                                   PasswordEncoder passwordEncoder) {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(secret))
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri(redirectUri)
                .scope("openid")
                .scope("profile")
                .scope("email")
                .scope("read")
                .scope("write")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofSeconds(accessTokenValiditySeconds))
                        .refreshTokenTimeToLive(Duration.ofSeconds(refreshTokenValiditySeconds))
                        .reuseRefreshTokens(false)
                        .build())
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
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            log.error("Failed to generate RSA key pair", ex);
            throw new IllegalStateException("Failed to generate RSA key pair", ex);
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(issuerUrl)
                .authorizationEndpoint("/oauth2/authorize")
                .tokenEndpoint("/oauth2/token")
                .jwkSetEndpoint("/oauth2/jwks")
                .oidcUserInfoEndpoint("/userinfo")
                .oidcClientRegistrationEndpoint("/connect/register")
                .build();
    }
}
