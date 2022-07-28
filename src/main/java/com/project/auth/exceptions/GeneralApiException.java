package com.project.auth.exceptions;

import org.springframework.http.HttpStatus;

public abstract class GeneralApiException extends RuntimeException {

    protected GeneralApiException(String message) {
        super(message);
    }

    public abstract String getErrorCode();

    public abstract HttpStatus getStatus();


}
