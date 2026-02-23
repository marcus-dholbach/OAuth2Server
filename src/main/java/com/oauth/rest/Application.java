package com.oauth.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Application.class);

		Environment env = app.run(args).getEnvironment();

		// Verificar configuración (solo para depuración)
		System.out.println("✅ Perfiles activos: " + String.join(", ", env.getActiveProfiles()));
	}
}
