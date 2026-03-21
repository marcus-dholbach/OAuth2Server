package com.oauth.adapters.output.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oauth.domain.model.UsuarioAplicacion;

public interface UsuarioAplicacionRepository extends JpaRepository<UsuarioAplicacion, Long> {

    List<UsuarioAplicacion> findByUsuarioId(Long usuarioId);

    List<UsuarioAplicacion> findByApplicationId(Long applicationId);

    Optional<UsuarioAplicacion> findByUsuarioIdAndApplicationId(Long usuarioId, Long applicationId);

}
