package com.oauth.domain.ports.in.user;

import java.util.List;
import java.util.Optional;

import com.oauth.domain.model.UsuarioAplicacion;

/**
 * Puerto de entrada para servicios de Usuario-Aplicación
 * Define las operaciones de negocio relacionadas con la relación usuario-aplicación
 */
public interface UserApplicationServicePort {

    /**
     * Busca una relación por su ID
     */
    Optional<UsuarioAplicacion> findById(Long id);

    /**
     * Busca todas las relaciones de un usuario
     */
    List<UsuarioAplicacion> findByUsuarioId(Long usuarioId);

    /**
     * Busca todas las relaciones de una aplicación
     */
    List<UsuarioAplicacion> findByApplicationId(Long applicationId);

    /**
     * Busca una relación específica entre usuario y aplicación
     */
    Optional<UsuarioAplicacion> findByUsuarioIdAndApplicationId(Long usuarioId, Long applicationId);

    /**
     * Guarda una relación
     */
    UsuarioAplicacion save(UsuarioAplicacion userApplication);
}
