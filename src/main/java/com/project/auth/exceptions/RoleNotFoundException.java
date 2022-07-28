package com.project.auth.exceptions;

import com.project.auth.constants.CustomExceptionConstants;
import org.springframework.http.HttpStatus;

public class RoleNotFoundException extends GeneralApiException {

    public RoleNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return CustomExceptionConstants.ROLE_NOT_FOUND_ERROR_CODE;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
