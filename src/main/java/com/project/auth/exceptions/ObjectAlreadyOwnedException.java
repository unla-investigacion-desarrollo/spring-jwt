package com.project.auth.exceptions;

import org.springframework.http.HttpStatus;

public class ObjectAlreadyOwnedException extends GeneralApiException {

    private final String code;

    public ObjectAlreadyOwnedException(String code, String message) {
        super(message);
        this.code = code;
    }

    @Override
    public String getErrorCode() {
        return code;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
