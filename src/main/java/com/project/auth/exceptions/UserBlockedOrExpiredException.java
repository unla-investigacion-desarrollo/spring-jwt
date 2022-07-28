package com.project.auth.exceptions;

import com.project.auth.constants.CustomExceptionConstants;
import org.springframework.http.HttpStatus;

public class UserBlockedOrExpiredException extends GeneralApiException {

    public UserBlockedOrExpiredException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return CustomExceptionConstants.USER_BLOCKED_OR_EXPIRED_CODE;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
