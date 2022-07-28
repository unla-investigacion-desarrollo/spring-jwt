package com.project.auth.security.jwt;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.auth.constants.CommonsErrorConstants;
import com.project.auth.models.response.ApplicationResponse;
import com.project.auth.models.response.ErrorResponse;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private final Tracer tracer;

    public AuthEntryPointJwt(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        String message;
        String code;

        if (request.getAttribute("expired") != null) {
            code = CommonsErrorConstants.JWT_EXPIRED_ERROR_CODE;
            message = CommonsErrorConstants.JWT_EXPIRED_ERROR_MESSAGE;
        } else if (request.getAttribute("invalid") != null) {
            code = CommonsErrorConstants.JWT_INVALID_ERROR_CODE;
            message = CommonsErrorConstants.JWT_INVALID_ERROR_MESSAGE;
        } else {
            code = CommonsErrorConstants.AUTH_GENERAL_ERROR_CODE;
            message = (authException != null && !StringUtils.isEmpty(authException.getMessage()))
                    ? authException.getMessage() : CommonsErrorConstants.AUTH_GENERAL_ERROR_MESSAGE;
        }

        log.error("Unauthorized error: {}", message);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse =
                new ErrorResponse(code, message, getTraceId(), null);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        response.getOutputStream()
                .println(objectMapper
                        .writeValueAsString(new ApplicationResponse<>(null, errorResponse)));
    }

    private String getTraceId() {
        if (tracer.currentSpan() != null && tracer.currentSpan().context() != null
                && tracer.currentSpan().context().traceIdString() != null) {
            return tracer.currentSpan().context().traceIdString();
        }
        return Strings.EMPTY;
    }

}
