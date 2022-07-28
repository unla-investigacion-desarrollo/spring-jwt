package com.project.auth.services.impl;

import com.project.auth.constants.CustomExceptionConstants;
import com.project.auth.constants.documentation.AuthControllerConstants;
import com.project.auth.dtos.request.login.RequestLoginDTO;
import com.project.auth.dtos.request.login.RequestRecoverPassDTO;
import com.project.auth.dtos.response.login.ResponseLoginDTO;
import com.project.auth.exceptions.UnauthorizedUserException;
import com.project.auth.exceptions.UserBlockedOrExpiredException;
import com.project.auth.exceptions.UserNotFoundException;
import com.project.auth.models.database.Users;
import com.project.auth.repositories.UserRepository;
import com.project.auth.security.jwt.JwtUtils;
import com.project.auth.services.IAuthService;
import com.project.auth.services.IMailService;
import com.project.auth.utils.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AuthService implements IAuthService {

    private static final String TIME_ZONE = "UTC-3";

    private final JwtUtils jwtUtils;

    private final UserRepository userRepository;

    private final AuthUtils authUtils;

    private final IMailService mailService;

    public AuthService(JwtUtils jwtUtils,
                       UserRepository userRepository, AuthUtils authUtils, IMailService mailService) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.authUtils = authUtils;
        this.mailService = mailService;
    }

    /**
     * Service that is responsible for authenticating a user and returning its token
     *
     * @param request (user and password {@link RequestLoginDTO})
     * @return {@link ResponseLoginDTO}
     */
    @Override
    public ResponseLoginDTO signInService(RequestLoginDTO request) {
        log.debug("Signin user by username {}", request.getUsername());

        Users user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error(CustomExceptionConstants.USER_NOT_FOUND_LOGIN_ERROR_MESSAGE);
                    return new BadCredentialsException(
                            CustomExceptionConstants.AUTH_SIGNIN_FILED_ATTEMPTS_ERROR_MESSAGE);
                });

        if (!user.isActive()) {
            throw new UnauthorizedUserException(CustomExceptionConstants.USER_INACTIVE_CODE,
                    CustomExceptionConstants.USER_INACTIVE_MESSAGE);
        }

        if (user.getFailedAttempts() == 3) {
            throw new UserBlockedOrExpiredException(CustomExceptionConstants.USER_BLOCKED_MESSAGE);
        }

        if (!validateUser(request, user)) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            userRepository.save(user);
            log.error(String.format("The username or password is incorrect. Failed attempts:{%d}",
                    user.getFailedAttempts()));
            throw new BadCredentialsException(CustomExceptionConstants.AUTH_SIGNIN_FILED_ATTEMPTS_ERROR_MESSAGE);
        } else {
            user.setFailedAttempts(0);
            user.setLastSessionDate(LocalDateTime.now(ZoneId.of(TIME_ZONE)));
            userRepository.save(user);
        }

        log.info("Generating token ...");
        String jwt = jwtUtils.generateJwtToken(user);

        ResponseLoginDTO response = new ResponseLoginDTO(jwt, "Bearer",
                user.getId(), user.getUsername(), user.getEmail(),
                user.getRole().getType().name());

        log.info("Successful authentication");
        return response;
    }

    /**
     * Method in charge of validating the user entered in the request
     *
     * @param request {@link RequestLoginDTO} object that has the username and password to validate
     * @param users   {@link Users}
     * @return {@link Boolean}
     */
    public boolean validateUser(RequestLoginDTO request, Users users) {
        log.debug("Validate user by username {}", request.getUsername());

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(request.getPassword(), users.getPassword());
    }

    /**
     * Main method in charge of managing the updating of the password of a user who has forgotten
     * his password
     *
     * @param request (username)
     * @return Successful message
     */
    @Override
    public String recoverPassword(RequestRecoverPassDTO request) {

        Users user =
                userRepository.findByUsername(request.getUsername()).orElseThrow(() -> {
                    log.error(CustomExceptionConstants.USER_NOT_FOUND_LOGIN_ERROR_MESSAGE);
                    return new UserNotFoundException(
                            CustomExceptionConstants.USER_NOT_FOUND_LOGIN_ERROR_CODE,
                            CustomExceptionConstants.USER_NOT_FOUND_LOGIN_ERROR_MESSAGE);
                });

        log.info("Generating random password ...");

        //RANDOM PASSWORD
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
                .build();

        String temporaryPassword = generator.generate(15, 20);

        user.setPassword(temporaryPassword);
        userRepository.save(user);

        CompletableFuture.runAsync(() -> mailService
                .sendRecoveryPassEmail(Collections.singletonList(user.getEmail()),
                        user.getUsername(), temporaryPassword, user));

        log.info("Generated password.");
        return AuthControllerConstants.RECOVERY_PASS_RESPONSE_200;
    }

}
