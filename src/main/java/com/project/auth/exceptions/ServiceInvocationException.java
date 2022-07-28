package com.project.auth.exceptions;

import com.project.auth.constants.CommonsErrorConstants;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceInvocationException extends GeneralApiException {

    private final HttpStatus httpStatus;

    private final String origin;

    private final String errorDetail;

    public ServiceInvocationException(final String origin, final HttpStatus httpStatus,
            final String errorDetail) {
        super(String.format(CommonsErrorConstants.EXTERNAL_API_ERROR_MESSAGE, origin));
        this.httpStatus = httpStatus;
        this.errorDetail = errorDetail;
        this.origin = origin;
    }

    @Override
    public String getErrorCode() {
        return CommonsErrorConstants.INTERNAL_ERROR_CODE;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
