package com.oauth.rest.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.oauth.rest.model.UsuarioAplicacion;
import com.oauth.rest.repository.UsuarioAplicacionRepository;

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
