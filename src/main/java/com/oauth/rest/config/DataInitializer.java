package com.oauth.rest.config;

import com.oauth.rest.model.UserEntity;
import com.oauth.rest.model.UserRole;
import com.oauth.rest.repository.UserEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Set;

@Configuration
@Profile("!test") // No ejecutar en tests
@PropertySource(value = "classpath:application-secrets.properties", ignoreResourceNotFound = true)
@Slf4j
public class DataInitializer {

    // Variables de entorno (prioridad 1)
    @Value("${DEFAULT_ADMIN_USERNAME:${admin.username:admin}}")
    private String adminUsername;

    @Value("${DEFAULT_ADMIN_PASSWORD:${admin.password:}}")
    private String adminPassword;

    @Value("${DEFAULT_ADMIN_FULL_NAME:${admin.full-name:Administrador del sistema}}")
    private String adminFullName;

    @Value("${DEFAULT_ADMIN_EMAIL:${admin.email:admin@oauth.net}}")
    private String adminEmail;

    @Value("${DEFAULT_USER_USERNAME:${user.username:user1}}")
    private String userUsername;

    @Value("${DEFAULT_USER_PASSWORD:${user.password:}}")
    private String userPassword;

    @Value("${DEFAULT_USER_FULL_NAME:${user.full-name:Usuario estándar}}")
    private String userFullName;

    @Value("${DEFAULT_USER_EMAIL:${user.email:usuario@oauth.net}}")
    private String userEmail;

    private final Environment environment;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(Environment environment, PasswordEncoder passwordEncoder) {
        this.environment = environment;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initUsers(UserEntityRepository userRepository) {
        return args -> {
            // Log para depuración - mostrar perfiles activos
            log.info("Perfiles activos: {}", Arrays.toString(environment.getActiveProfiles()));

            if (userRepository.count() == 0) {

                // Verificar contraseñas
                if (adminPassword == null || adminPassword.trim().isEmpty() ||
                        userPassword == null || userPassword.trim().isEmpty()) {

                    log.warn("ADVERTENCIA: Contraseñas no configuradas");
                    log.warn("Usando valores por defecto SOLO PARA DESARROLLO");

                    // En desarrollo, podemos generar contraseñas aleatorias
                    if (environment.matchesProfiles("dev")) {
                        adminPassword = "admin" + System.currentTimeMillis();
                        userPassword = "user" + System.currentTimeMillis();
                        log.info("Credenciales generadas para desarrollo:");
                        log.info("   admin / {}", adminPassword);
                        log.info("   user1 / {}", userPassword);
                    } else {
                        throw new IllegalStateException(
                                "Las contraseñas no están configuradas. " +
                                        "Debe definir DEFAULT_ADMIN_PASSWORD y DEFAULT_USER_PASSWORD " +
                                        "como variables de entorno o en application-secrets.properties");
                    }
                }

                // Verificar si las contraseñas ya están hasheadas o necesitan serlo
                boolean passwordsHashed = isHashed(adminPassword) && isHashed(userPassword);

                // Crear usuario admin
                UserEntity admin = new UserEntity();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordsHashed ? adminPassword : passwordEncoder.encode(adminPassword));
                admin.setFullName(adminFullName);
                admin.setEmail(adminEmail);
                admin.setRoles(Set.of(UserRole.USER, UserRole.ADMIN));
                userRepository.save(admin);

                // Crear usuario normal
                UserEntity user = new UserEntity();
                user.setUsername(userUsername);
                user.setPassword(passwordsHashed ? userPassword : passwordEncoder.encode(userPassword));
                user.setFullName(userFullName);
                user.setEmail(userEmail);
                user.setRoles(Set.of(UserRole.USER));
                userRepository.save(user);
            } else {
                log.info("Usuarios ya existentes en la base de datos");
            }
        };
    }

    /**
     * Detecta si una contraseña ya está hasheada (formato BCrypt)
     */
    private boolean isHashed(String password) {
        return password != null && (password.startsWith("$2a$") || // BCrypt
                password.startsWith("$2b$") || // BCrypt
                password.startsWith("$2y$") || // BCrypt
                password.startsWith("{bcrypt}") // Spring Security format
        );
    }
}