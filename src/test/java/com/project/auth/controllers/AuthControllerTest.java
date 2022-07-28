package com.project.auth.controllers;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.auth.configurations.WebSecurityConfig;
import com.project.auth.constants.CommonsErrorConstants;
import com.project.auth.constants.CustomExceptionConstants;
import com.project.auth.constants.TestConstants;
import com.project.auth.constants.documentation.AuthControllerConstants;
import com.project.auth.dtos.request.login.RequestLoginDTO;
import com.project.auth.dtos.request.login.RequestRecoverPassDTO;
import com.project.auth.dtos.request.login.RequestUpdatePassDTO;
import com.project.auth.dtos.response.login.ResponseLoginDTO;
import com.project.auth.exceptions.UserNotFoundException;
import com.project.auth.models.database.Role;
import com.project.auth.models.database.Users;
import com.project.auth.models.enums.RoleType;
import com.project.auth.models.response.ApplicationResponse;
import com.project.auth.repositories.UserRepository;
import com.project.auth.security.jwt.AuthEntryPointJwt;
import com.project.auth.security.jwt.AuthTokenFilter;
import com.project.auth.security.jwt.JwtUtils;
import com.project.auth.security.service.UserDetailsServiceImpl;
import com.project.auth.services.IAuthService;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import({WebSecurityConfig.class, AuthTokenFilter.class, UserDetailsServiceImpl.class,
        AuthEntryPointJwt.class, JwtUtils.class})
@WebAppConfiguration
class AuthControllerTest {

    ObjectMapper mapper = new ObjectMapper();

    @MockBean
    IAuthService iAuthService;

    @Autowired
    MockMvc mvc;

    @MockBean
    Tracer tracer;

    @MockBean
    UserRepository userRepository;

    Users userFirstSignInTrue;

    private RequestLoginDTO request;

    private RequestUpdatePassDTO requestUpdatePass;

    private RequestRecoverPassDTO requestRecoverPass;

    @BeforeEach
    public void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        request = new RequestLoginDTO();
        request.setUsername("any user");
        request.setPassword("any pass");

        requestUpdatePass = new RequestUpdatePassDTO();
        requestUpdatePass.setNewPassword("String2020");

        requestRecoverPass = new RequestRecoverPassDTO();
        requestRecoverPass.setUsername("username");

        Role role = new Role();
        role.setType(RoleType.ADMIN);

        userFirstSignInTrue = new Users();
        userFirstSignInTrue.setUsername("MartinGomez2");
        userFirstSignInTrue.setEmail("MartinGomez2@gmail.com");
        userFirstSignInTrue.setRole(role);
    }

    @Test
    @Order(1)
    @DisplayName("Given an valid username and password, when authenticate user then return a json"
            + " with correct bearer token")
    void whenTheInputBodyIsCorrect_thenReturnOkResponseWithStatus200()
            throws Exception {

        ResponseLoginDTO response = new ResponseLoginDTO();
        response.setUserId(1L);
        response.setEmail("user@gmail.com");
        response.setUsername("username");
        response.setType("Bearer");
        response.setToken(
                "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6W3siaWQiOjEsInR5cGUiOiJBRE1JTiJ9XSwic3ViI");
        response.setRole(RoleType.ADMIN.name());

        ApplicationResponse<ResponseLoginDTO> jsonExpected =
                new ApplicationResponse<>(response, null);

        given(iAuthService.signInService(any())).willReturn(response);

        mvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath(TestConstants.DATA_JSON_PATH_EXPRESSION)
                        .value(IsNull.notNullValue()))
                .andExpect(jsonPath(TestConstants.ERRORS_JSON_PATH_EXPRESSION)
                        .value(IsNull.nullValue()))
                .andExpect(content().json(mapper.writeValueAsString(jsonExpected)));
    }

    @Test
    @Order(2)
    void whenTheInputBodyHasAnEmptyInformation_thenReturnBadRequestWithStatus400()
            throws Exception {

        RequestLoginDTO requestEmpty = new RequestLoginDTO();

        mvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestEmpty)))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(TestConstants.DATA_JSON_PATH_EXPRESSION).value(IsNull.nullValue()))
                .andExpect(jsonPath(TestConstants.ERRORS_CODE_JSON_PATH_EXPRESSION,
                        is(CommonsErrorConstants.REQUEST_VALIDATION_ERROR_CODE)))
                .andExpect(jsonPath(TestConstants.ERRORS_MESSAGE_JSON_PATH_EXPRESSION)
                        .value(CommonsErrorConstants.REQUEST_VALIDATION_ERROR_MESSAGE))
                .andExpect(jsonPath("$.errors.details[*].messages[0]")
                        .value(containsInAnyOrder(
                                String.format(CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE,
                                        "username"),
                                String.format(CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE,
                                        "password"))));
    }

    @Test
    @Order(3)
    void whenUserIsUnauthorized_thenReturnStatus401() throws Exception {

        given(iAuthService.signInService(any())).will(e -> {
            throw new BadCredentialsException(
                    CustomExceptionConstants.AUTH_SIGNIN_FILED_ATTEMPTS_ERROR_MESSAGE);
        });

        mvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath(TestConstants.DATA_JSON_PATH_EXPRESSION).value(IsNull.nullValue()))
                .andExpect(jsonPath(TestConstants.ERRORS_CODE_JSON_PATH_EXPRESSION)
                        .value(CustomExceptionConstants.AUTH_SIGNIN_FILED_ATTEMPTS_ERROR_CODE))
                .andExpect(jsonPath(TestConstants.ERRORS_MESSAGE_JSON_PATH_EXPRESSION)
                        .value(CustomExceptionConstants.AUTH_SIGNIN_FILED_ATTEMPTS_ERROR_MESSAGE));
    }

    @Test
    @Order(4)
    void givenRecoverPasswordWhenUsernameIsCorrect_thenReturnOkResponseWithStatus200()
            throws Exception {

        String response = AuthControllerConstants.FIRST_SIGN_IN_RESPONSE_200;

        ApplicationResponse<String> jsonExpected =
                new ApplicationResponse<>(response, null);

        given(iAuthService.recoverPassword(any())).willReturn(response);

        mvc.perform(put("/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestRecoverPass)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath(TestConstants.DATA_JSON_PATH_EXPRESSION)
                        .value(IsNull.notNullValue()))
                .andExpect(jsonPath(TestConstants.ERRORS_JSON_PATH_EXPRESSION)
                        .value(IsNull.nullValue()))
                .andExpect(content().json(mapper.writeValueAsString(jsonExpected)));
    }

    @Test
    @Order(5)
    void whenTheInputBodyHasAnEmptyUsernameInRecoverPassword_thenReturnBadRequestWithStatus400()
            throws Exception {

        RequestRecoverPassDTO requestEmpty = new RequestRecoverPassDTO();

        mvc.perform(put("/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestEmpty)))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath(TestConstants.DATA_JSON_PATH_EXPRESSION).value(IsNull.nullValue()))
                .andExpect(jsonPath(TestConstants.ERRORS_CODE_JSON_PATH_EXPRESSION,
                        is(CommonsErrorConstants.REQUEST_VALIDATION_ERROR_CODE)))
                .andExpect(jsonPath(TestConstants.ERRORS_MESSAGE_JSON_PATH_EXPRESSION)
                        .value(CommonsErrorConstants.REQUEST_VALIDATION_ERROR_MESSAGE))
                .andExpect(jsonPath("$.errors.details[*].messages[0]")
                        .value(containsInAnyOrder(
                                String.format(CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE,
                                        "username"))));
    }

    @Test
    @Order(6)
    void whenUsernameNotExist_thenReturnBadRequestWithStatus404()
            throws Exception {

        given(iAuthService.recoverPassword(any())).will(e -> {
            throw new UserNotFoundException(
                    CustomExceptionConstants.USER_NOT_FOUND_LOGIN_ERROR_CODE,
                    CustomExceptionConstants.USER_NOT_FOUND_LOGIN_ERROR_MESSAGE);
        });

        mvc.perform(put("/auth/recover-password")
                        .header("Accept-Language", "en")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestRecoverPass)))
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath(TestConstants.DATA_JSON_PATH_EXPRESSION).value(IsNull.nullValue()))
                .andExpect(jsonPath(TestConstants.ERRORS_CODE_JSON_PATH_EXPRESSION,
                        is(CustomExceptionConstants.USER_NOT_FOUND_LOGIN_ERROR_CODE)));
    }


    @Test
    @WithMockUser
    @Order(7)
    void whenTheInputIsCorrectInValidateToken_thenReturnOkResponseWithStatus200()
            throws Exception {
        ApplicationResponse<String> jsonExpected =
                new ApplicationResponse<>("OK", null);

        mvc.perform(get("/auth/validate-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath(TestConstants.DATA_JSON_PATH_EXPRESSION)
                        .value(IsNull.notNullValue()))
                .andExpect(jsonPath(TestConstants.ERRORS_JSON_PATH_EXPRESSION)
                        .value(IsNull.nullValue()))
                .andExpect(content().json(mapper.writeValueAsString(jsonExpected)));
    }

    @Test
    @Order(8)
    void whenConsumeValidateTokenWithoutAuth_thenReturnErrorResponseWithStatus401()
            throws Exception {

        mvc.perform(get("/auth/validate-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andExpect(jsonPath(TestConstants.DATA_JSON_PATH_EXPRESSION)
                        .value(IsNull.nullValue()))
                .andExpect(jsonPath(TestConstants.ERRORS_JSON_PATH_EXPRESSION)
                        .value(IsNull.notNullValue()));
    }

}
