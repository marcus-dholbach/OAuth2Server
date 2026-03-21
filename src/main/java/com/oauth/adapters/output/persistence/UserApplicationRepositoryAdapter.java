package com.oauth.adapters.output.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.oauth.domain.ports.out.persistence.UserApplicationRepositoryPort;
import com.oauth.domain.model.UsuarioAplicacion;
import com.oauth.adapters.output.persistence.UsuarioAplicacionRepository;

/**
 * Adaptador de salida que implementa el puerto de repositorio de usuario-aplicación
 */
@Repository
public class UserApplicationRepositoryAdapter implements UserApplicationRepositoryPort {

    private final UsuarioAplicacionRepository usuarioAplicacionRepository;

    public UserApplicationRepositoryAdapter(UsuarioAplicacionRepository usuarioAplicacionRepository) {
        this.usuarioAplicacionRepository = usuarioAplicacionRepository;
    }

    @Override
    public Optional<UsuarioAplicacion> findById(Long id) {
        return usuarioAplicacionRepository.findById(id);
    }

    @Override
    public List<UsuarioAplicacion> findByUsuarioId(Long usuarioId) {
        return usuarioAplicacionRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public List<UsuarioAplicacion> findByApplicationId(Long applicationId) {
        return usuarioAplicacionRepository.findByApplicationId(applicationId);
    }

    @Override
    public Optional<UsuarioAplicacion> findByUsuarioIdAndApplicationId(Long usuarioId, Long applicationId) {
        return usuarioAplicacionRepository.findByUsuarioIdAndApplicationId(usuarioId, applicationId);
    }

    @Override
    public UsuarioAplicacion save(UsuarioAplicacion userApplication) {
        return usuarioAplicacionRepository.save(userApplication);
    }

    @Override
    public void deleteById(Long id) {
        usuarioAplicacionRepository.deleteById(id);
    }

    @Override
    public void delete(UsuarioAplicacion userApplication) {
        usuarioAplicacionRepository.delete(userApplication);
    }
}
