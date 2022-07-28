package com.project.auth.dtos.mail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataMailAddUserDTO {

    private String name;

    private String username;

    private String temporaryPassword;

    private String supportMail;

    private String webUrl;

}
