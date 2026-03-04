package com.oauth.rest.security.oauth2;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

        @Value("${oauth2.access-token-validity-seconds}")
        private int defaultAccessTokenValiditySeconds;

        @Value("${oauth2.refresh-token-validity-seconds}")
        private int defaultRefreshTokenValiditySeconds;

        @Value("${ISSUER_URL:http://localhost:8080}")
        private String issuerUrl;

        // Eliminamos la dependencia de OAuth2ClientProperties
        private final OAuth2ParameterSavingFilter oauth2ParameterSavingFilter;

        public OAuth2AuthorizationServer(OAuth2ParameterSavingFilter oauth2ParameterSavingFilter) {
                this.oauth2ParameterSavingFilter = oauth2ParameterSavingFilter;
                System.out.println("=== OAuth2AuthorizationServer INITIALIZED ===");
                log.info("=== OAuth2AuthorizationServer INITIALIZED ===");
        }

        @PostConstruct
        public void init() {
                log.info("=== OAuth2AuthorizationServer BEAN CREATED SUCCESSFULLY ===");
                System.out.println("=== OAuth2AuthorizationServer BEAN CREATED SUCCESSFULLY ===");
        }

        @Bean
        @Order(10)
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

                OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer
                                .authorizationServer();

                http
                                .securityMatcher(
                                                "/oauth2/authorize",
                                                "/oauth2/token",
                                                "/oauth2/jwks",
                                                "/userinfo",
                                                "/connect/register",
                                                "/oauth2/.*",
                                                "/oauth/.*")
                                .addFilterBefore(oauth2ParameterSavingFilter,
                                                UsernamePasswordAuthenticationFilter.class)
                                .with(authorizationServerConfigurer, Customizer.withDefaults())
                                .authorizeHttpRequests(authorize -> authorize
                                                .anyRequest().authenticated());

                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                                .oidc(Customizer.withDefaults())
                                .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
                                                .consentPage("/oauth2/consent"));

                http.exceptionHandling(exceptions -> exceptions
                                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")));

                return http.build();
        }

        @Bean
        public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder) {
                System.out.println("==========================================");
                System.out.println("🚀 registeredClientRepository EJECUTÁNDOSE");
                System.out.println("==========================================");

                List<RegisteredClient> clients = new ArrayList<>();

                // Leer configuración directamente de variables de entorno
                String cineClientSecret = System.getenv("CINE_PLATFORM_SECRET");
                String cineRedirectUri = System.getenv("CINE_PLATFORM_REDIRECT_URI");

                System.out.println("CINE_PLATFORM_SECRET: " + cineClientSecret);
                System.out.println("CINE_PLATFORM_REDIRECT_URI: " + cineRedirectUri);
                System.out.println("ISSUER_URL configurado: " + issuerUrl);

                // Cliente cine-platform
                if (cineClientSecret != null && cineRedirectUri != null) {
                        RegisteredClient cinePlatformClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                        .clientId("cine-platform")
                                        .clientSecret(passwordEncoder.encode(cineClientSecret))
                                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                        .redirectUri(cineRedirectUri)
                                        .scope("openid")
                                        .scope("profile")
                                        .scope("read")
                                        .scope("write")
                                        .clientSettings(ClientSettings.builder()
                                                        .requireAuthorizationConsent(false)
                                                        .requireProofKey(false)
                                                        .build())
                                        .tokenSettings(TokenSettings.builder()
                                                        .accessTokenTimeToLive(
                                                                        Duration.ofSeconds(
                                                                                        defaultAccessTokenValiditySeconds))
                                                        .refreshTokenTimeToLive(
                                                                        Duration.ofSeconds(
                                                                                        defaultRefreshTokenValiditySeconds))
                                                        .reuseRefreshTokens(false)
                                                        .build())
                                        .build();

                        clients.add(cinePlatformClient);
                        System.out.println("✅ Cliente cine-platform añadido a la lista");
                } else {
                        System.out.println("❌ No se encontraron variables de entorno para cine-platform");
                }

                // Cliente transcriberapp (si existe)
                String transcribeAppSecret = System.getenv("TRANSCRIBEAPP_SECRET");
                String transcribeAppRedirectUri = System.getenv("TRANSCRIBEAPP_REDIRECT_URI");

                if (transcribeAppSecret != null && transcribeAppRedirectUri != null) {
                        RegisteredClient transcribeAppClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                        .clientId("transcriberapp")
                                        .clientSecret(passwordEncoder.encode(transcribeAppSecret))
                                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                        .redirectUri(transcribeAppRedirectUri)
                                        .scope("openid")
                                        .scope("profile")
                                        .scope("read")
                                        .scope("write")
                                        .clientSettings(ClientSettings.builder()
                                                        .requireAuthorizationConsent(false)
                                                        .requireProofKey(false)
                                                        .build())
                                        .tokenSettings(TokenSettings.builder()
                                                        .accessTokenTimeToLive(
                                                                        Duration.ofSeconds(
                                                                                        defaultAccessTokenValiditySeconds))
                                                        .refreshTokenTimeToLive(
                                                                        Duration.ofSeconds(
                                                                                        defaultRefreshTokenValiditySeconds))
                                                        .reuseRefreshTokens(false)
                                                        .build())
                                        .build();

                        clients.add(transcribeAppClient);
                        System.out.println("✅ Cliente transcriberapp añadido a la lista");
                }

                if (clients.isEmpty()) {
                        System.out.println(
                                        "❌ No se encontraron clientes configurados. No se creará cliente por defecto.");
                        System.out.println(
                                        "❌ Por favor configure las variables de entorno CINE_PLATFORM_SECRET y CINE_PLATFORM_REDIRECT_URI");
                        // No crear cliente por defecto con credenciales inseguras
                        // throw new IllegalStateException("No hay clientes OAuth2 configurados");
                }

                System.out.println("📦 Número total de clientes: " + clients.size());
                System.out.println("==========================================");
                return new InMemoryRegisteredClientRepository(clients);
        }

        private RegisteredClient createDefaultClient(PasswordEncoder passwordEncoder) {
                return RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("default-client")
                                .clientSecret(passwordEncoder.encode("${DEFAULT_CLIENT_SECRET}"))
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .clientSettings(ClientSettings.builder()
                                                .requireAuthorizationConsent(false)
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
                try {
                        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                        keyPairGenerator.initialize(2048);
                        return keyPairGenerator.generateKeyPair();
                } catch (Exception ex) {
                        throw new IllegalStateException("Error generando par de claves RSA", ex);
                }
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