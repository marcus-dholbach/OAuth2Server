package com.oauth.infrastructure.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oauth.domain.model.UsuarioAplicacion;
import com.oauth.adapters.output.persistence.UsuarioAplicacionRepository;

@Service
public class UsuarioAplicacionService extends BaseService<UsuarioAplicacion, Long, UsuarioAplicacionRepository> {

    public UsuarioAplicacionService(UsuarioAplicacionRepository repository) {
        super(repository);
    }

    public List<UsuarioAplicacion> findByUsuarioId(Long usuarioId) {
        return this.repository.findByUsuarioId(usuarioId);
    }

    public List<UsuarioAplicacion> findByApplicationId(Long applicationId) {
        return this.repository.findByApplicationId(applicationId);
    }

    public Optional<UsuarioAplicacion> findByUsuarioIdAndApplicationId(Long usuarioId, Long applicationId) {
        return this.repository.findByUsuarioIdAndApplicationId(usuarioId, applicationId);
    }
}
