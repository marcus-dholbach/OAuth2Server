package com.oauth.adapters.input.rest.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.oauth.adapters.input.rest.dto.GetUserDto;
import com.oauth.domain.model.UserEntity;

@Component
public class UserDtoMapper {

    public GetUserDto toGetUserDto(UserEntity user) {
        return new GetUserDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet()));
    }
}
