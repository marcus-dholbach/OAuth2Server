package com.oauth.adapters.input.rest;

import java.util.concurrent.CompletableFuture;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oauth.domain.ports.in.usecase.user.CreateUserUseCasePort;
import com.oauth.domain.ports.in.usecase.user.GetUserUseCasePort;
import com.oauth.adapters.input.rest.dto.CreateUserDto;
import com.oauth.adapters.input.rest.dto.GetUserDto;
import com.oauth.adapters.input.rest.mapper.UserDtoMapper;
import com.oauth.domain.model.UserEntity;

/**
 * Controlador REST para usuarios - Adaptador de entrada
 * Implementa los endpoints de la API usando los casos de uso del dominio
 * Usa las interfaces de los puertos de entrada (SOLID - Dependency Inversion)
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final CreateUserUseCasePort createUserUseCase;
    private final GetUserUseCasePort getUserUseCase;
    private final UserDtoMapper userDtoMapper;

    public UserController(CreateUserUseCasePort createUserUseCase,
                         GetUserUseCasePort getUserUseCase,
                         UserDtoMapper userDtoMapper) {
        this.createUserUseCase = createUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.userDtoMapper = userDtoMapper;
    }

    @PostMapping
    public CompletableFuture<GetUserDto> nuevoUsuario(@RequestBody CreateUserDto newUser) {
        return createUserUseCase.execute(
                newUser.getUsername(),
                newUser.getEmail(),
                newUser.getPassword(),
                newUser.getPassword2(),
                newUser.getFullName()
        ).thenApply(userDtoMapper::toGetUserDto)
         .exceptionally(ex -> { 
             Throwable cause = ex.getCause();
             if (cause instanceof RuntimeException) {
                 throw (RuntimeException) cause;
             }
             throw new RuntimeException(cause != null ? cause : ex);
         });
    }

    @GetMapping("/me")
    public GetUserDto me(@AuthenticationPrincipal UserEntity authenticatedUser) {
        return userDtoMapper.toGetUserDto(authenticatedUser);
    }
}
