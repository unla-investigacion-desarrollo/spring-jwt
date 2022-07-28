package com.project.auth.exceptions;

import com.project.auth.constants.CustomExceptionConstants;
import org.springframework.http.HttpStatus;

public class UserNameAlreadyTakenException extends GeneralApiException {

    public UserNameAlreadyTakenException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return CustomExceptionConstants.USER_NAME_ALREADY_TAKEN_ERROR_CODE;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
