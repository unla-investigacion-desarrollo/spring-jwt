package com.project.auth.dtos.request.login;

import com.project.auth.constants.CommonsErrorConstants;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestLoginDTO {

    @NotBlank(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @Size(max = 250, message = CommonsErrorConstants.MAX_SIZE_ERROR_MESSAGE)
    private String username;

    @NotBlank(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @Size(max = 250, message = CommonsErrorConstants.MAX_SIZE_ERROR_MESSAGE)
    private String password;
}
