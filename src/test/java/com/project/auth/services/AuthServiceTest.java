package com.project.auth.services;

import com.project.auth.constants.CustomExceptionConstants;
import com.project.auth.constants.documentation.AuthControllerConstants;
import com.project.auth.dtos.request.login.RequestLoginDTO;
import com.project.auth.dtos.request.login.RequestRecoverPassDTO;
import com.project.auth.dtos.request.login.RequestUpdatePassDTO;
import com.project.auth.dtos.response.login.ResponseLoginDTO;
import com.project.auth.exceptions.UnauthorizedUserException;
import com.project.auth.exceptions.UserBlockedOrExpiredException;
import com.project.auth.exceptions.UserNotFoundException;
import com.project.auth.models.database.Role;
import com.project.auth.models.database.Users;
import com.project.auth.models.enums.RoleType;
import com.project.auth.repositories.UserRepository;
import com.project.auth.security.jwt.JwtUtils;
import com.project.auth.services.impl.AuthService;
import com.project.auth.services.impl.MailService;
import com.project.auth.utils.AuthUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private MailService mailService;

    @Mock
    private AuthUtils authUtils;

    @InjectMocks
    private AuthService authService;

    private Users user;

    private RequestLoginDTO requestOk;

    private RequestUpdatePassDTO requestUpdatePass;


    @BeforeEach
    void setUp() {
        Role role = new Role(RoleType.ADMIN);

        user = new Users();
        user.setUsername("Admin");
        user.setEmail("admin1@gmail.com");
        user.setRole(role);
        user.setActive(true);
        user.setPassword("pass");

        requestOk = new RequestLoginDTO();
        requestOk.setUsername(user.getUsername());
        requestOk.setPassword("pass");

        requestUpdatePass = new RequestUpdatePassDTO();
        requestUpdatePass.setNewPassword("String2020");
    }

    @Test
    @DisplayName("When the Admin user exists on the platform and on Serco, return correct "
            + "ResponseLoginDTO")
    @Order(1)
    void testSignInServiceMethodWithCorrectAdminUser() {

        // given
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));

        String MOCK_TOKEN = "strxx34str";
        when(jwtUtils.generateJwtToken(any())).thenReturn(MOCK_TOKEN);

        // when
        ResponseLoginDTO response = authService.signInService(requestOk);

        // then
        Assertions.assertEquals("Bearer", response.getType());
        Assertions.assertEquals(MOCK_TOKEN, response.getToken());
        Assertions.assertEquals(user.getEmail(), response.getEmail());
        Assertions.assertEquals(user.getUsername(), response.getUsername());
    }

    @Test
    @DisplayName("When the user not exists on the platform, return BadCredentialsException")
    @Order(2)
    void testSignInServiceMethodWithInorrectUser() {

        // given
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

        // when
        BadCredentialsException badCredentialsException =
                Assertions.assertThrows(BadCredentialsException.class,
                        () -> authService.signInService(requestOk));

        // then
        Assertions.assertEquals(CustomExceptionConstants.AUTH_SIGNIN_FILED_ATTEMPTS_ERROR_MESSAGE,
                badCredentialsException.getMessage());
    }

    @Test
    @DisplayName("When the username or password is incorrect, return BadCredentialsException")
    @Order(3)
    void testSignInServiceMethodWithInorrectUsernameOrPassword() {

        // given
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));

        RequestLoginDTO requestIncorrectLogin = requestOk;
        requestIncorrectLogin.setPassword("incorrectPass");

        // when
        BadCredentialsException badCredentialsException =
                Assertions.assertThrows(BadCredentialsException.class,
                        () -> authService.signInService(requestIncorrectLogin));

        // then
        Assertions.assertEquals(CustomExceptionConstants.AUTH_SIGNIN_FILED_ATTEMPTS_ERROR_MESSAGE,
                badCredentialsException.getMessage());
    }

    @Test
    @DisplayName(
            "When the admin user wants to recover the password and with a correct user, he returns "
                    + "a satisfactory message")
    @Order(4)
    void testRecoveryPassServiceMethodWithCorrectNewTemporaryPasswordWithAdminUser() {

        String olderPass = user.getPassword();

        RequestRecoverPassDTO requestRecoverPassDTO = new RequestRecoverPassDTO();
        requestRecoverPassDTO.setUsername("admin");

        // given
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));

        String responseActual = authService.recoverPassword(requestRecoverPassDTO);

        // when
        Assertions.assertEquals(AuthControllerConstants.RECOVERY_PASS_RESPONSE_200, responseActual);

        Assertions.assertNotEquals(olderPass, user.getPassword());

        verify(mailService, timeout(200).times(1))
                .sendRecoveryPassEmail(any(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("When the user wants to recover the password and an incorrect user is entered "
            + "then he returns UserNotFoundException")
    @Order(5)
    void testRecoveryPassServiceMethodWithIncorrectUser() {

        RequestRecoverPassDTO requestRecoverPassDTO = new RequestRecoverPassDTO();
        requestRecoverPassDTO.setUsername("admin");

        // given
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        // when
        UserNotFoundException userNotFoundException =
                Assertions.assertThrows(UserNotFoundException.class,
                        () -> authService.recoverPassword(requestRecoverPassDTO));

        Assertions.assertEquals(CustomExceptionConstants.USER_NOT_FOUND_LOGIN_ERROR_CODE,
                userNotFoundException.getErrorCode());
        Assertions.assertEquals(CustomExceptionConstants.USER_NOT_FOUND_LOGIN_ERROR_MESSAGE,
                userNotFoundException.getMessage());
    }

    @Test
    @DisplayName("When the user has 3 failed attempts, return correct "
            + "UserBlockedOrExpiredException")
    @Order(6)
    void testSignInServiceMethodWithAUserWith3FailedAttempts() {

        // given
        user.setFailedAttempts(3);
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));

        // when
        Assertions.assertThrows(UserBlockedOrExpiredException.class,
                () -> authService.signInService(requestOk));
    }

    @Test
    @DisplayName("When the user is inactive, return correct UnauthorizedUserException")
    @Order(7)
    void testSignInServiceMethodWithAUserInactive() {

        // given
        user.setActive(false);
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));

        // when
        Assertions.assertThrows(UnauthorizedUserException.class,
                () -> authService.signInService(requestOk));
    }
}
