package com.oauth.rest.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.oauth.rest.model.UserEntity;
import com.oauth.rest.repository.UserEntityRepository;

@Service("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private final UserEntityRepository userEntityRepository;
    private final ApplicationService applicationService;
    private final UsuarioAplicacionService usuarioAplicacionService;

    public CustomUserDetailsService(UserEntityRepository userEntityRepository,
            ApplicationService applicationService,
            UsuarioAplicacionService usuarioAplicacionService) {
        this.userEntityRepository = userEntityRepository;
        this.applicationService = applicationService;
        this.usuarioAplicacionService = usuarioAplicacionService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    /**
     * Carga un usuario verificando que esté registrado en la aplicación
     * 
     * @param username    nombre de usuario
     * @param application identificador de la aplicación
     */
    public UserDetails loadUserByUsernameAndApplication(String username, String application) {
        UserEntity user = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Verificar que el usuario esté registrado en la aplicación usando el objeto
        // user ya obtenido
        if (!applicationService.findByClientId(application)
                .map(app -> usuarioAplicacionService
                        .findByUsuarioIdAndApplicationId(user.getId(), app.getId())
                        .isPresent())
                .orElse(false)) {
            throw new UsernameNotFoundException(
                    "Usuario no encontrado: " + username + " para la app: " + application);
        }

        return user;
    }

    /**
     * Verifica si un usuario está registrado en una aplicación
     * 
     * @param username            nombre de usuario
     * @param applicationClientId client_id de la aplicación
     * @return true si está registrado
     */
    public boolean isUserRegisteredInApplication(String username, String applicationClientId) {
        UserEntity user = userEntityRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return false;
        }

        return applicationService.findByClientId(applicationClientId)
                .map(app -> usuarioAplicacionService
                        .findByUsuarioIdAndApplicationId(user.getId(), app.getId())
                        .isPresent())
                .orElse(false);
    }
}
