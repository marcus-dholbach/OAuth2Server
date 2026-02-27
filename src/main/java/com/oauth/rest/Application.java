package com.oauth.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import com.oauth.rest.security.oauth2.OAuth2ClientProperties;

@SpringBootApplication
@EnableConfigurationProperties(OAuth2ClientProperties.class)
@ComponentScan(basePackages = { "com.oauth.rest" })
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Application.class);

		Environment env = app.run(args).getEnvironment();

		// Verificar configuración (solo para depuración)
		log.info("Perfiles activos: {}", String.join(", ", env.getActiveProfiles()));
	}
}
