package com.oauth.rest.service;

import com.oauth.rest.repository.UserEntityRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private final UserEntityRepository userEntityRepository;

    public CustomUserDetailsService(UserEntityRepository userEntityRepository) {
        this.userEntityRepository = userEntityRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    /**
     * Carga un usuario filtrando por nombre de usuario y aplicación
     * 
     * @param username    nombre de usuario
     * @param application identificador de la aplicación
     */
    public UserDetails loadUserByUsernameAndApplication(String username, String application) {
        return userEntityRepository.findByUsernameAndApplication(username, application)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username + " para la app: " + application));
    }
}
