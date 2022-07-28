package com.project.auth.exceptions.handler;

import brave.Tracer;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.project.auth.constants.CommonsErrorConstants;
import com.project.auth.constants.CustomExceptionConstants;
import com.project.auth.exceptions.BuilderException;
import com.project.auth.models.response.ApplicationResponse;
import com.project.auth.models.response.ErrorDetail;
import com.project.auth.models.response.ErrorResponse;
import com.project.auth.models.response.ErrorResponse.Builder;
import org.apache.logging.log4j.util.Strings;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class RestExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String TIME_FORMAT = "HH:mm";

    protected static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    protected static final String JSON_FIELD_SEPARATOR = ".";

    @Value("${spring.application.name}")
    protected String applicationName;

    @Autowired
    private Tracer tracer;

    protected RestExceptionHandler() {
    }

    /**
     * Method that is responsible for caching the errors related to the contraints defined in the
     * database model, such as @column(nullable = false) or @Column(unique = true). Sql error codes
     * should change when the database engine changes.
     */
    @ExceptionHandler({
            DataIntegrityViolationException.class
    })
    protected ResponseEntity<ApplicationResponse<Object>> handleDataBaseExceptions(
            DataIntegrityViolationException ex) {

        ApplicationResponse<Object> applicationResponse;
        HttpStatus status;
        String code;
        String message;

        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException
                && ex.getCause().getCause() != null && ex.getCause()
                .getCause() instanceof PSQLException) {

            org.hibernate.exception.ConstraintViolationException constraintViolationException =
                    (org.hibernate.exception.ConstraintViolationException)
                            ex.getCause();

            PSQLException psqlException =
                    (PSQLException) constraintViolationException.getSQLException();

            status = HttpStatus.BAD_REQUEST;
            code = CommonsErrorConstants.DATA_BASE_ERROR_CODE;

            if (psqlException.getSQLState()
                    .equals(CommonsErrorConstants.DATA_BASE_UNIQUE_VIOLATION_PSQL_CODE)) {
                message = psqlException.getServerErrorMessage().getDetail();

            } else if (psqlException.getSQLState()
                    .equals(CommonsErrorConstants.DATA_BASE_FOREIGN_KEY_VIOLATION_PSQL_CODE)) {
                message = CommonsErrorConstants.DATA_BASE_FOREIGN_KEY_VIOLATION_PSQL_MESSAGE;
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                code = CommonsErrorConstants.INTERNAL_ERROR_CODE;
                message = CommonsErrorConstants.INTERNAL_ERROR_MESSAGE;
            }

        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = CommonsErrorConstants.INTERNAL_ERROR_CODE;
            message = CommonsErrorConstants.INTERNAL_ERROR_MESSAGE;

        }

        applicationResponse =
                this.getApplicationResponse(code, message, getTraceId(), null);

        return ResponseEntity.status(status).body(applicationResponse);

    }

    @ExceptionHandler({ConstraintViolationException.class})
    protected ResponseEntity<ApplicationResponse<Object>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        List<ErrorDetail> details = new ArrayList<>();

        List<String> fields =
                ex.getConstraintViolations().stream().map(e -> e.getPropertyPath().toString())
                        .distinct().collect(Collectors.toList());

        for (String field : fields) {
            List<ConstraintViolation<?>> exceptions =
                    ex.getConstraintViolations().stream()
                            .filter(e -> e.getPropertyPath().toString().equals(field))
                            .collect(Collectors.toList());

            String property;

            if (field.contains("\\.")) {
                property = field.split("\\.")[1];
            } else {
                property = field;
            }

            List<String> messages =
                    exceptions.stream().map(constraint -> {
                        String msg = constraint.getMessage();
                        if (msg.contains("%s")) {
                            return String.format(msg, property);
                        }
                        return msg;
                    }).collect(Collectors.toList());

            ErrorDetail errorDetail = new ErrorDetail(property, messages);
            details.add(errorDetail);
        }

        log.info(CommonsErrorConstants.LOG_VALIDATION_MESSAGE, ex.getClass(), details);
        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.REQUEST_VALIDATION_ERROR_CODE,
                        CommonsErrorConstants.REQUEST_VALIDATION_ERROR_MESSAGE, getTraceId(),
                        details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(applicationResponse);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    protected ResponseEntity<ApplicationResponse<Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        String message = String.format(CommonsErrorConstants.TYPE_MISMATCH_PARAM_ERROR_MESSAGE,
                ex.getName());
        log.info(message);
        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.TYPE_MISMATCH_PARAM_ERROR_CODE,
                        message, getTraceId(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(applicationResponse);
    }

    @ExceptionHandler({BuilderException.class})
    protected ResponseEntity<ApplicationResponse<Object>> handleBuilderException(
            BuilderException ex) {
        log.error(CommonsErrorConstants.LOG_ERROR_MESSAGE, ex.getMessage(), ex);
        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.INTERNAL_ERROR_CODE,
                        CommonsErrorConstants.INTERNAL_ERROR_MESSAGE, getTraceId(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(applicationResponse);
    }

    @ExceptionHandler({AccessDeniedException.class})
    protected ResponseEntity<ApplicationResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex) {
        log.error(CommonsErrorConstants.LOG_ERROR_MESSAGE, ex.getMessage(), ex);
        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.FORBIDDEN_ERROR_CODE,
                        ex.getMessage(), getTraceId(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(applicationResponse);
    }

    @ExceptionHandler({BadCredentialsException.class})
    protected ResponseEntity<ApplicationResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex) {
        log.error(CommonsErrorConstants.LOG_ERROR_MESSAGE, ex.getMessage(), ex);

        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(
                        CustomExceptionConstants.AUTH_SIGNIN_FILED_ATTEMPTS_ERROR_CODE,
                        ex.getMessage(), getTraceId(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(applicationResponse);
    }

    @ExceptionHandler({Exception.class})
    protected ResponseEntity<ApplicationResponse<Object>> handleException(Exception ex) {
        log.error(CommonsErrorConstants.LOG_ERROR_MESSAGE, ex.getMessage(), ex);
        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.DEFAULT_SERVICE_ERROR_CODE,
                        CommonsErrorConstants.DEFAULT_SERVICE_ERROR_MESSAGE, getTraceId(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(applicationResponse);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.METHOD_NOT_ALLOWED_ERROR_CODE,
                        String.format(
                                CommonsErrorConstants.METHOD_NOT_ALLOWED_ERROR_MESSAGES,
                                Arrays.toString(ex.getSupportedMethods())), getTraceId(), null);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(applicationResponse);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        String message = String.format(CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE,
                ex.getParameterName());
        log.info(message);
        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.REQUEST_VALIDATION_ERROR_CODE,
                        message, getTraceId(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(applicationResponse);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
                                                        HttpHeaders headers, HttpStatus status, WebRequest request) {
        String message =
                String.format(CommonsErrorConstants.TYPE_MISMATCH_PARAM_ERROR_MESSAGE,
                        ex.getPropertyName());
        log.info(message);
        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.TYPE_MISMATCH_PARAM_ERROR_CODE,
                        message, getTraceId(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(applicationResponse);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        return this.getGenericBadRequestApplicationResponse();
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        String message = this.getMessageForJacksonCause(exception.getCause());
        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.REQUEST_GENERIC_ERROR_CODE,
                        message, getTraceId(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(applicationResponse);
    }

    private String getMessageForJacksonCause(Throwable cause) {
        String message = CommonsErrorConstants.REQUEST_GENERIC_ERROR_MESSAGE;
        if (cause instanceof JsonProcessingException) {
            if (cause instanceof JsonParseException) {
                message = this.getMessageFromJsonParseException((JsonParseException) cause);
            } else if (cause instanceof JsonMappingException) {
                message = this.getMessageFromJsonMappingException(cause);
            }
        }

        return message;
    }

    private String getMessageFromJsonParseException(JsonParseException cause) {
        String message = CommonsErrorConstants.REQUEST_GENERIC_ERROR_MESSAGE;
        String fieldName = Optional.ofNullable(cause.getProcessor())
                .map(JsonParser::getParsingContext).map(
                        JsonStreamContext::getCurrentName).orElse("");
        if (!fieldName.isEmpty()) {
            message = String.format("Parameter %s contains invalid information", fieldName);
        }

        return message;
    }

    private String getMessageFromJsonMappingException(Throwable cause) {
        String message = CommonsErrorConstants.REQUEST_GENERIC_ERROR_MESSAGE;
        String fieldName = Strings.EMPTY;

        for (JsonMappingException.Reference path : ((JsonMappingException) cause).getPath()) {
            if (path.getFieldName() != null) {
                fieldName = fieldName.concat(path.getFieldName() + ".");
            } else {
                fieldName = fieldName.concat("[" + path.getIndex() + "].");
            }
        }

        fieldName = fieldName.substring(0, fieldName.length() - 1);

        if (fieldName.contains(".[")) {
            fieldName = fieldName.replace(".[", "[");
        }

        Throwable secondCause = Optional.of(cause).map(Throwable::getCause).orElse(cause);
        if (secondCause instanceof DateTimeParseException) {
            String format = (fieldName.contains("Time")) ? TIME_FORMAT : DATE_FORMAT;
            message = String.format("Parameter %s has a different format than expected '%s'",
                    fieldName, format);
        } else if (secondCause instanceof InvalidFormatException) {

            InvalidFormatException ifx = (InvalidFormatException) secondCause;
            if (ifx.getTargetType() != null && ifx.getTargetType().isEnum()) {
                message = String.format(
                        "Invalid value: '%s' for the field: '%s'. The value must be one of: %s.",
                        ifx.getValue(), ifx.getPath().get(ifx.getPath().size() - 1).getFieldName(),
                        Arrays.toString(ifx.getTargetType().getEnumConstants()));
            } else {
                message = String.format("The parameter %s has an invalid format", fieldName);
            }
        } else if (!fieldName.isEmpty()) {
            message = (!secondCause.getMessage().isEmpty()) ? secondCause.getMessage()
                    : String.format("Parameter %s contains invalid information", fieldName);
        }

        return message;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        List<Object> filds =
                ex.getBindingResult().getAllErrors().stream().map(e -> e.getArguments()[0])
                        .distinct().collect(Collectors.toList());
        String field = "";

        List<ErrorDetail> details = new ArrayList<>();

        for (int i = 0; i < filds.size(); i++) {

            int finalI = i;
            List<ObjectError> objectErrors = ex.getBindingResult().getAllErrors().stream()
                    .filter(e -> e.getArguments()[0].equals(filds.get(finalI)))
                    .collect(Collectors.toList());

            if (!objectErrors.isEmpty() && objectErrors.get(0).getArguments() != null
                    && objectErrors.get(0).getArguments().length > 0) {

                field = ((DefaultMessageSourceResolvable) objectErrors.get(0).getArguments()[0])
                        .getDefaultMessage();
            }

            List<String> errorsDetails =
                    objectErrors.stream()
                            .map(this::generateArgumentNotValidErrorMessage)
                            .collect(Collectors.toList());

            if (Strings.isEmpty(field)) {
                field = "Request";
            }

            ErrorDetail errorDetail = new ErrorDetail(field, errorsDetails);
            details.add(errorDetail);
        }

        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.REQUEST_VALIDATION_ERROR_CODE,
                        CommonsErrorConstants.REQUEST_VALIDATION_ERROR_MESSAGE, getTraceId(),
                        details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(applicationResponse);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        return this.getGenericBadRequestApplicationResponse();
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers,
                                                         HttpStatus status, WebRequest request) {
        return this.getGenericBadRequestApplicationResponse();
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.UNSUPPORTED_MEDIA_TYPE_ERROR_CODE,
                        CommonsErrorConstants.UNSUPPORTED_MEDIA_TYPE_ERROR_MESSAGE, getTraceId(),
                        null);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(applicationResponse);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.UNSUPPORTED_MEDIA_TYPE_ERROR_CODE,
                        CommonsErrorConstants.UNSUPPORTED_MEDIA_TYPE_ERROR_MESSAGE, getTraceId(),
                        null);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(applicationResponse);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex,
                                                               HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.getGenericBadRequestApplicationResponse();
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(
            AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatus status,
            WebRequest webRequest) {
        log.error(CommonsErrorConstants.LOG_ERROR_MESSAGE, ex.getMessage(), ex);
        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.INTERNAL_ERROR_CODE,
                        CommonsErrorConstants.INTERNAL_ERROR_MESSAGE, getTraceId(), null);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(applicationResponse);
    }

    protected ApplicationResponse<Object> getApplicationResponse(String code,
                                                                 String message, String requestId, List<ErrorDetail> details) {

        ErrorResponse errorResponse =
                (new Builder(code, message, requestId)).withErrorDetail(details).build();
        return new ApplicationResponse<>(null, errorResponse);
    }

    protected ResponseEntity<Object> getGenericBadRequestApplicationResponse() {
        ApplicationResponse<Object> applicationResponse =
                this.getApplicationResponse(CommonsErrorConstants.REQUEST_GENERIC_ERROR_CODE,
                        CommonsErrorConstants.REQUEST_GENERIC_ERROR_MESSAGE, getTraceId(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(applicationResponse);
    }


    protected String generateArgumentNotValidErrorMessage(ObjectError objectError) {
        String message = CommonsErrorConstants.REQUEST_GENERIC_ERROR_MESSAGE;
        if (objectError.getArguments() != null && objectError.getArguments().length > 0) {
            String field = ((DefaultMessageSourceResolvable) objectError.getArguments()[0])
                    .getDefaultMessage();
            String defaultMessage = objectError.getDefaultMessage();
            if (defaultMessage != null) {
                message = String.format(defaultMessage, field);
            }
        }

        return message;
    }

    private String getTraceId() {
        if (tracer.currentSpan() != null && tracer.currentSpan().context() != null
                && tracer.currentSpan().context().traceIdString() != null) {
            return tracer.currentSpan().context().traceIdString();
        }
        return Strings.EMPTY;
    }
}
