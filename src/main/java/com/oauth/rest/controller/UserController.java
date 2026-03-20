package com.oauth.rest.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.oauth.rest.dto.CreateUserDto;
import com.oauth.rest.dto.GetUserDto;
import com.oauth.rest.mapper.UserDtoMapper;
import com.oauth.rest.model.UserEntity;
import com.oauth.rest.service.UserEntityService;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserEntityService userEntityService;
    private final UserDtoMapper userDtoMapper;

    public UserController(UserEntityService userEntityService,
                          UserDtoMapper userDtoMapper) {
        this.userEntityService = userEntityService;
        this.userDtoMapper = userDtoMapper;
    }

    @PostMapping
    public CompletableFuture<GetUserDto> nuevoUsuario(@RequestBody CreateUserDto newUser) {
        return userEntityService.nuevoUsuario(newUser)
        .thenApply(userDtoMapper::toGetUserDto)
        .exceptionally(ex -> { throw (RuntimeException) ex.getCause(); });
    }

    @GetMapping("/me")
    public GetUserDto me(@AuthenticationPrincipal UserEntity authenticatedUser) {
        return userDtoMapper.toGetUserDto(authenticatedUser);
    }
}
