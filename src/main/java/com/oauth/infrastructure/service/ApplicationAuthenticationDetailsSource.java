package com.oauth.infrastructure.service;

import com.oauth.domain.model.ApplicationDetails;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ApplicationAuthenticationDetailsSource 
        implements AuthenticationDetailsSource<HttpServletRequest, ApplicationDetails> {

    @Override
    public ApplicationDetails buildDetails(HttpServletRequest context) {
        String clientId = (String) context.getAttribute("CLIENT_ID");
        return new ApplicationDetails(clientId);
    }
}