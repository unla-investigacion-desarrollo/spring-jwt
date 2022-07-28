package com.project.auth.exceptions.handler;

import brave.Tracer;
import com.project.auth.constants.CommonsErrorConstants;
import com.project.auth.exceptions.EmailAlreadyTakenException;
import com.project.auth.exceptions.GeneralApiException;
import com.project.auth.exceptions.NotDeleteOrUpdateException;
import com.project.auth.exceptions.ObjectAlreadyExistException;
import com.project.auth.exceptions.ObjectNotFoundException;
import com.project.auth.exceptions.RoleNotFoundException;
import com.project.auth.exceptions.ServiceInvocationException;
import com.project.auth.exceptions.UnauthorizedRoleException;
import com.project.auth.exceptions.UnauthorizedUserException;
import com.project.auth.exceptions.UserBlockedOrExpiredException;
import com.project.auth.exceptions.UserNameAlreadyTakenException;
import com.project.auth.exceptions.UserNotFoundException;
import com.project.auth.models.response.ApplicationResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomRestExceptionHandler extends RestExceptionHandler {

    private final Tracer tracerId;

    public CustomRestExceptionHandler(Tracer tracerId) {
        this.tracerId = tracerId;
    }

    @ExceptionHandler({
            EmailAlreadyTakenException.class,
            UnauthorizedUserException.class,
            UnauthorizedRoleException.class,
            UserBlockedOrExpiredException.class,
            UserNameAlreadyTakenException.class,
            UserNotFoundException.class,
            RoleNotFoundException.class,
            ObjectNotFoundException.class,
            ObjectAlreadyExistException.class,
            NotDeleteOrUpdateException.class
    })
    protected ResponseEntity<ApplicationResponse<Object>> handleCustomExceptions(
            GeneralApiException generalApiException) {

        log.error(CommonsErrorConstants.LOG_ERROR_MESSAGE, generalApiException.getMessage());

        ApplicationResponse<Object> applicationResponse = this
                .getApplicationResponse(generalApiException.getErrorCode(),
                        generalApiException.getMessage(), getTraceIdValue(), null);
        return ResponseEntity.status(generalApiException.getStatus())
                .body(applicationResponse);
    }

    @ExceptionHandler({ServiceInvocationException.class})
    protected ResponseEntity<ApplicationResponse<Object>> handleServiceInvocationException(
            ServiceInvocationException ex) {
        log.error(CommonsErrorConstants.LOG_ERROR_MESSAGE, ex.getMessage(), ex);

        ApplicationResponse<Object> applicationResponse = this
                .getApplicationResponse(ex.getErrorCode(),
                        CommonsErrorConstants.INTERNAL_ERROR_MESSAGE, getTraceIdValue(), null);
        return ResponseEntity.status(ex.getStatus())
                .body(applicationResponse);
    }


    private String getTraceIdValue() {
        if (tracerId.currentSpan() != null && tracerId.currentSpan().context() != null
                && tracerId.currentSpan().context().traceIdString() != null) {
            return tracerId.currentSpan().context().traceIdString();
        }
        return Strings.EMPTY;
    }

}
