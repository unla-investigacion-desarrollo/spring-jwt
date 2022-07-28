package com.project.auth.converters;

import com.project.auth.dtos.response.user.ResponseUserDTO;
import com.project.auth.models.database.Users;

public final class UserConverter {

    private UserConverter() {

    }

    public static ResponseUserDTO toResponseUserDTO(Users users) {
        ResponseUserDTO dto = new ResponseUserDTO();

        dto.setId(users.getId());
        dto.setUserName(users.getUsername());
        dto.setName(users.getFirstName());
        dto.setLastname(users.getLastName());
        dto.setEmail(users.getEmail());
        dto.setRole(users.getRole().getType().name());
        dto.setActive(users.isActive());
        dto.setLastSessionDate(users.getLastSessionDate());
        return dto;
    }
}
