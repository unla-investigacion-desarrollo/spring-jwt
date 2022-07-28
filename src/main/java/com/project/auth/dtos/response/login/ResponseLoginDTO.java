package com.project.auth.dtos.response.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseLoginDTO {

    private String token;

    private String type;

    private Long userId;

    private String username;

    private String email;

    private String role;
}
