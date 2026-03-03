package com.oauth.rest.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.oauth.rest.dto.CreateUserDto;
import com.oauth.rest.exception.UserPasswordException;
import com.oauth.rest.model.Role;
import com.oauth.rest.model.UserEntity;
import com.oauth.rest.repository.UserEntityRepository;

@Service
public class UserEntityService extends BaseService<UserEntity, Long, UserEntityRepository> {

    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserEntityService(UserEntityRepository repository,
            PasswordEncoder passwordEncoder,
            RoleService roleService) {
        super(repository);
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    public Optional<UserEntity> findUserByUsername(String username) {
        return this.repository.findByUsername(username);
    }

    public Optional<UserEntity> findUserByEmail(String email) {
        return this.repository.findByEmail(email);
    }

    public UserEntity nuevoUsuario(CreateUserDto newUser) {

        if (!newUser.getPassword().equals(newUser.getPassword2())) {
            throw new UserPasswordException();
        }

        UserEntity user = new UserEntity();
        user.setUsername(newUser.getUsername());
        user.setFullName(newUser.getFullName());
        user.setEmail(newUser.getEmail());
        user.setPassword(passwordEncoder.encode(newUser.getPassword()));
        user.setEnabled(true);

        // Asignar rol por defecto: ROLE_USER
        // Si no existe, se crea automáticamente
        Set<Role> roles = new HashSet<>();
        Role userRole = roleService.findOrCreateRole("ROLE_USER", "Usuario estándar");
        roles.add(userRole);
        user.setRoles(roles);

        try {
            return save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre de usuario ya existe");
        }
    }
}
