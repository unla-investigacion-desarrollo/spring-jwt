package com.project.auth.dtos.request.login;

import com.project.auth.constants.CommonsErrorConstants;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class RequestUpdatePassDTO {

    @NotBlank(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @Pattern(regexp = "^(?=\\w*\\d)(?=\\w*[A-Z])(?=\\w*[a-z])\\S{8,}$", message =
            CommonsErrorConstants.PASSWORD_VALUE_ERROR_MESSAGE)
    @Size(max = 250, message = CommonsErrorConstants.MAX_SIZE_ERROR_MESSAGE)
    private String newPassword;
}
