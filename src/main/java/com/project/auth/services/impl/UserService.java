package com.project.auth.services.impl;


import com.project.auth.constants.CustomExceptionConstants;
import com.project.auth.converters.UserConverter;
import com.project.auth.dtos.request.user.RequestUserDTO;
import com.project.auth.dtos.response.user.ResponseUserDTO;
import com.project.auth.exceptions.EmailAlreadyTakenException;
import com.project.auth.exceptions.UserNameAlreadyTakenException;
import com.project.auth.exceptions.UserNotFoundException;
import com.project.auth.models.database.Role;
import com.project.auth.models.database.Users;
import com.project.auth.models.enums.RoleType;
import com.project.auth.repositories.UserRepository;
import com.project.auth.services.IMailService;
import com.project.auth.services.IRoleService;
import com.project.auth.services.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.project.auth.constants.CustomExceptionConstants.USER_NOT_FOUND_CONTROLLER_ERROR_CODE;
import static com.project.auth.constants.CustomExceptionConstants.USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE;

@Slf4j
@Service
public class UserService implements IUserService {

    private final IRoleService roleService;

    private final IMailService mailService;

    private final UserRepository userRepository;

    public UserService(IRoleService roleService,
                       IMailService mailService, UserRepository userRepository) {
        this.roleService = roleService;
        this.mailService = mailService;
        this.userRepository = userRepository;
    }

    /**
     * Main method related to creating user
     *
     * @param requestUserDTO {@link RequestUserDTO}
     * @return new Users
     */
    public Users createUser(RequestUserDTO requestUserDTO) throws RoleNotFoundException {

        log.info("Starting the user creation process with username: {}...",
                requestUserDTO.getUsername());

        userRepository.findByUsername(requestUserDTO.getUsername()).ifPresent(users -> {
                    throw new UserNameAlreadyTakenException(
                            CustomExceptionConstants.USER_NAME_ALREADY_TAKEN_ERROR_MESSAGE
                    );
                }
        );

        userRepository.findFirstByEmailIgnoreCase(requestUserDTO.getEmail()).ifPresent(
                users -> {
                    throw new EmailAlreadyTakenException(
                            CustomExceptionConstants.USER_EMAIL_ALREADY_TAKEN_ERROR_CODE,
                            CustomExceptionConstants.USER_EMAIL_ALREADY_TAKEN_ERROR_MESSAGE);
                }
        );

        Role role = roleService.findRoleByType(RoleType.valueOf(requestUserDTO.getRole()));

        Users user = new Users();
        user.setFirstName(requestUserDTO.getName());
        user.setLastName(requestUserDTO.getLastname());
        user.setUsername(requestUserDTO.getUsername());
        user.setEmail(requestUserDTO.getEmail());
        user.setActive(true);
        user.setFailedAttempts(0);

        user.setRole(role);

        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .filteredBy(CharacterPredicates.DIGITS, CharacterPredicates.LETTERS)
                .build();

        String temporaryPassword = generator.generate(15, 20);
        user.setPassword(temporaryPassword);

        userRepository.save(user);

        CompletableFuture.runAsync(() -> mailService
                .sendNewUserEmail(Collections.singletonList(user.getEmail()),
                        user.getUsername(), temporaryPassword, user));

        return user;
    }

    @Override
    public Page<ResponseUserDTO> findUsers(String search, Boolean active, PageRequest pageRequest) {
        Page<Users> users = userRepository
                .findByFilter(search, active, pageRequest);
        List<ResponseUserDTO> responseUserDTO = new ArrayList<>();
        users.forEach(u -> responseUserDTO.add(UserConverter.toResponseUserDTO(u)));
        return new PageImpl<>(responseUserDTO, users.getPageable(),
                users.getTotalElements());
    }

    @Override
    public Users findUserById(long usersId) {
        return userRepository.findById(usersId).orElseThrow(
                () -> new UserNotFoundException(USER_NOT_FOUND_CONTROLLER_ERROR_CODE,
                        String.format(USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE, usersId)));
    }

    /**
     * Method in charge of deleting a user
     *
     * @param userId {@link Long}
     */
    @Override
    public String deleteUser(long userId) {

        Users user = findUserById(userId);

        log.info("Deleting user with id {} and username {}", userId, user.getUsername());

        userRepository.deleteById(userId);

        log.info("User delete Successfully");

        return "deleted";
    }


    @Override
    public Users updateUser(long userId, RequestUserDTO requestUserDTO) {

        Users user = findUserById(userId);

        log.info("Update user with id {} and username {}", userId, user.getUsername());

        Role role = roleService.findRoleByType(RoleType.valueOf(requestUserDTO.getRole()));

        if (!requestUserDTO.getUsername().equalsIgnoreCase(user.getUsername())) {
            userRepository.findByUsername(requestUserDTO.getUsername()).ifPresent(users -> {
                        throw new UserNameAlreadyTakenException(
                                CustomExceptionConstants.USER_NAME_ALREADY_TAKEN_ERROR_MESSAGE);
                    }
            );
            user.setUsername(requestUserDTO.getUsername());
        }

        user.setFirstName(requestUserDTO.getName());
        user.setLastName(requestUserDTO.getLastname());
        user.setEmail(requestUserDTO.getEmail());
        user.setRole(role);

        return userRepository.save(user);
    }

    @Override
    public String updateStateUser(long userId, boolean active) {

        Users user = findUserById(userId);
        log.info("Update state user with id {} and username {}", userId,
                user.getUsername());

        user.setActive(active);
        userRepository.save(user);

        return "State updated successfully";
    }
}
