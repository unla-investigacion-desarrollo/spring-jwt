package com.project.auth.exceptions;

import com.project.auth.constants.CommonsErrorConstants;
import org.springframework.http.HttpStatus;

public class UnauthorizedRoleException extends GeneralApiException {

    private final String code;

    public UnauthorizedRoleException(String message) {
        super(message);
        this.code = CommonsErrorConstants.FORBIDDEN_ERROR_CODE;
    }

    @Override
    public String getErrorCode() {
        return code;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
