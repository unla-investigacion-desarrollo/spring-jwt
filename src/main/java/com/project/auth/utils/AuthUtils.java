package com.project.auth.utils;

import com.project.auth.constants.CommonsErrorConstants;
import com.project.auth.constants.CustomExceptionConstants;
import com.project.auth.models.database.Users;
import com.project.auth.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AuthUtils {

    private final UserRepository userRepository;

    public AuthUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Method that is responsible for obtaining the basic information of the logged in user
     *
     * @return {@link Users}
     */
    public Users getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            return userRepository.findByUsername(currentUserName).orElseThrow(() -> {
                log.error(CustomExceptionConstants.USER_INFO_LOGIN_NOT_FOUND_MESSAGE);
                throw new BadCredentialsException(CommonsErrorConstants.AUTH_GENERAL_ERROR_MESSAGE);
            });
        } else {
            throw new BadCredentialsException(CommonsErrorConstants.AUTH_GENERAL_ERROR_MESSAGE);
        }
    }

    /**
     * Method that is responsible for obtaining the ROLES of the given user
     *
     * @return {@link String}
     */
    public String getRole(Users user) {
        return user.getRole().getType().name();
    }

    /**
     * Method that is responsible for obtaining the ROLES of the logged user
     *
     * @return {@link List<String>}
     */
    public String getRole() {
        return getAuthenticatedUser().getRole().getType().name();
    }

}
