package com.oauth.rest.security.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

// Usar @ConfigurationPropertiesScan en Application.java para registrar
// No usar @Component para evitar conflictos con otros beans
@ConfigurationProperties(prefix = "oauth2.clients")
public class OAuth2ClientProperties {

    private List<ClientConfig> clients = new ArrayList<>();

    public List<ClientConfig> getClients() {
        return clients;
    }

    public void setClients(List<ClientConfig> clients) {
        this.clients = clients;
    }

    public static class ClientConfig {
        private String clientId;
        private String clientSecret;
        private List<String> redirectUris = new ArrayList<>();
        private List<String> scopes = new ArrayList<>();
        private boolean requireConsent = true;
        private boolean requireProofKey = true;
        private Integer accessTokenValiditySeconds;
        private Integer refreshTokenValiditySeconds;

        // Getters y setters
        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public List<String> getRedirectUris() {
            return redirectUris;
        }

        public void setRedirectUris(List<String> redirectUris) {
            this.redirectUris = redirectUris;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void setScopes(List<String> scopes) {
            this.scopes = scopes;
        }

        public boolean isRequireConsent() {
            return requireConsent;
        }

        public void setRequireConsent(boolean requireConsent) {
            this.requireConsent = requireConsent;
        }

        public boolean isRequireProofKey() {
            return requireProofKey;
        }

        public void setRequireProofKey(boolean requireProofKey) {
            this.requireProofKey = requireProofKey;
        }

        public Integer getAccessTokenValiditySeconds() {
            return accessTokenValiditySeconds;
        }

        public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
            this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        }

        public Integer getRefreshTokenValiditySeconds() {
            return refreshTokenValiditySeconds;
        }

        public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
            this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
        }
    }
}