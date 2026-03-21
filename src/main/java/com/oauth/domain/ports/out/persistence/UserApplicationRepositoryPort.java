package com.oauth.domain.ports.out.persistence;

import java.util.List;
import java.util.Optional;

import com.oauth.domain.model.UsuarioAplicacion;

/**
 * Puerto de salida para operaciones de repositorio de usuario-aplicación
 */
public interface UserApplicationRepositoryPort {

    Optional<UsuarioAplicacion> findById(Long id);

    List<UsuarioAplicacion> findByUsuarioId(Long usuarioId);

    List<UsuarioAplicacion> findByApplicationId(Long applicationId);

    Optional<UsuarioAplicacion> findByUsuarioIdAndApplicationId(Long usuarioId, Long applicationId);

    UsuarioAplicacion save(UsuarioAplicacion usuarioAplicacion);

    void deleteById(Long id);

    void delete(UsuarioAplicacion usuarioAplicacion);
}
