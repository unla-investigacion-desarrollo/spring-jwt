package com.project.auth.security.jwt;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.auth.constants.CommonsErrorConstants;
import com.project.auth.models.response.ApplicationResponse;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthEntryPointJwtTest {

    @InjectMocks
    private AuthEntryPointJwt authEntryPointJwt;

    @Mock
    private Tracer tracer;

    private ObjectMapper mapper = new ObjectMapper();

    private ApplicationResponse<String> applicationResponse;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();

        response = new MockHttpServletResponse();

        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Test
    @Order(1)
    void whenJwtExpired_thenExeptionReturn401StatusWithJwtExpiredMessage() throws IOException {

        request.setAttribute("expired", true);
        // when
        authEntryPointJwt.commence(request, response, null);

        applicationResponse =
                mapper.readValue(response.getContentAsString(), ApplicationResponse.class);

        // then
        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertEquals(CommonsErrorConstants.JWT_EXPIRED_ERROR_CODE,
                applicationResponse.getErrorResponse().getCode());
        Assertions.assertEquals(CommonsErrorConstants.JWT_EXPIRED_ERROR_MESSAGE,
                applicationResponse.getErrorResponse().getMessage());
    }

    @Test
    @Order(2)
    void whenJwtInvalid_thenExeptionReturn401StatusWithJwtInvalidMessage() throws IOException {

        request.setAttribute("invalid", true);
        // when
        authEntryPointJwt.commence(request, response, null);

        applicationResponse =
                mapper.readValue(response.getContentAsString(), ApplicationResponse.class);

        // then
        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertEquals(CommonsErrorConstants.JWT_INVALID_ERROR_CODE,
                applicationResponse.getErrorResponse().getCode());
        Assertions.assertEquals(CommonsErrorConstants.JWT_INVALID_ERROR_MESSAGE,
                applicationResponse.getErrorResponse().getMessage());
    }

    @Test
    @Order(3)
    void whenIsAGenericAuthenticationError_thenReturn401StatusWithAuthGeneralErrorCode()
            throws IOException {

        // when
        authEntryPointJwt.commence(request, response, null);

        applicationResponse =
                mapper.readValue(response.getContentAsString(), ApplicationResponse.class);

        // then
        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertEquals(CommonsErrorConstants.AUTH_GENERAL_ERROR_CODE,
                applicationResponse.getErrorResponse().getCode());
        Assertions.assertEquals(CommonsErrorConstants.AUTH_GENERAL_ERROR_MESSAGE,
                applicationResponse.getErrorResponse().getMessage());
    }
}
