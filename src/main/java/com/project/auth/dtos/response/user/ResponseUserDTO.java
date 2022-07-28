package com.project.auth.dtos.response.user;

import com.fasterxml.jackson.annotation.JsonRootName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@JsonRootName("user")
@Getter
@Setter
public class ResponseUserDTO {

    private long id;

    private String userName;

    private String name;

    private String lastname;

    private String email;

    private String role;

    private LocalDateTime lastSessionDate;

    private boolean active;

}
