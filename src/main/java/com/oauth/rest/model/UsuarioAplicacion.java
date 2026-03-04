package com.oauth.rest.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "usuario_aplicacion", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "usuario_id", "application_id" })
})
@EntityListeners(AuditingEntityListener.class)
public class UsuarioAplicacion implements Serializable {

    private static final long serialVersionUID = 1111111111111111111L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UserEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @CreatedDate
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    public UsuarioAplicacion() {
    }

    public UsuarioAplicacion(UserEntity usuario, Application application) {
        this.usuario = usuario;
        this.application = application;
    }

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UserEntity usuario) {
        this.usuario = usuario;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((usuario == null || application == null) ? 0
                : usuario.getId().hashCode() + application.getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UsuarioAplicacion other = (UsuarioAplicacion) obj;
        if (usuario == null || other.usuario == null || application == null || other.application == null)
            return false;
        return usuario.getId().equals(other.usuario.getId()) &&
                application.getId().equals(other.application.getId());
    }

    @Override
    public String toString() {
        return "UsuarioAplicacion{usuarioId=" + (usuario != null ? usuario.getId() : null) +
                ", applicationId=" + (application != null ? application.getId() : null) + "}";
    }
}
