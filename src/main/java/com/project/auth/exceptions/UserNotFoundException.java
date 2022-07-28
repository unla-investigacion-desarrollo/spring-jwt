package com.project.auth.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends GeneralApiException {

    private final String code;

    public UserNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    @Override
    public String getErrorCode() {
        return code;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
