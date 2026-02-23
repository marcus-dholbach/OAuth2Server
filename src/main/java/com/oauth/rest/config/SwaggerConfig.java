package com.oauth.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("OAuth2 Server API")
						.version("1.0")
						.description("API REST con autenticación OAuth2")
						.contact(new Contact()
								.name("OAuth2 Server")
								.url("https://oauth.example.com")));
	}
}
