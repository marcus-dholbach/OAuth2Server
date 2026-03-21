package com.oauth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.title:OAuth2 Server API}")
    private String title;
    
    @Value("${swagger.version:1.0}")
    private String version;
    
    @Value("${swagger.description:API REST con autenticación OAuth2}")
    private String description;
    
    @Value("${swagger.contact.name:OAuth2 Server}")
    private String contactName;
    
    @Value("${swagger.contact.email:soporte@oauth.example.com}")
    private String contactEmail;
    
    @Value("${swagger.contact.url:https://oauth.example.com}")
    private String contactUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description(description)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail)
                                .url(contactUrl))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}