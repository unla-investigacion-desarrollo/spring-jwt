package com.project.auth.dtos.request.user;

import com.project.auth.annotations.Conditional;
import com.project.auth.annotations.ValueOfEnum;
import com.project.auth.constants.CommonsErrorConstants;
import com.project.auth.models.enums.RoleType;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Conditional(
        selected = "role",
        values = {"COORDINATOR"},
        required = {"businessAreaId"})
@Conditional(
        selected = "role",
        values = {"COLLABORATOR"},
        required = {"businessAreaId"})
@EqualsAndHashCode
public class RequestUserDTO {

    @NotBlank(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @Pattern(regexp = "^[a-zA-Z0-9\\s_.-]+$", message =
            CommonsErrorConstants.USERNAME_VALUE_ERROR_MESSAGE)
    @Size(max = 250, message = CommonsErrorConstants.MAX_SIZE_ERROR_MESSAGE)
    private String username;

    @NotBlank(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @Pattern(regexp = "^[a-zA-Z\\sñÑáÁéÉíÍóÓúüÜÚ]+$", message =
            CommonsErrorConstants.ALPHABET_VALUE_ERROR_MESSAGE)
    @Size(max = 250, message = CommonsErrorConstants.MAX_SIZE_ERROR_MESSAGE)
    private String name;

    @NotBlank(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @Pattern(regexp = "^[a-zA-Z\\sñÑáÁéÉíÍóÓúüÜÚ]+$", message =
            CommonsErrorConstants.ALPHABET_VALUE_ERROR_MESSAGE)
    @Size(max = 250, message = CommonsErrorConstants.MAX_SIZE_ERROR_MESSAGE)
    private String lastname;

    @NotBlank(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @Email(message = CommonsErrorConstants.INCORRECT_MAIL_ERROR_MESSAGE)
    @Size(max = 250, message = CommonsErrorConstants.MAX_SIZE_ERROR_MESSAGE)
    private String email;

    @NotNull(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @ValueOfEnum(enumClass = RoleType.class)
    private String role;

    private Long businessAreaId;
}

