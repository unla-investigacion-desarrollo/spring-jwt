package com.project.auth.services;

import com.project.auth.constants.CustomExceptionConstants;
import com.project.auth.dtos.request.user.RequestUserDTO;
import com.project.auth.dtos.response.user.ResponseUserDTO;
import com.project.auth.exceptions.EmailAlreadyTakenException;
import com.project.auth.exceptions.ObjectNotFoundException;
import com.project.auth.exceptions.UserNameAlreadyTakenException;
import com.project.auth.exceptions.UserNotFoundException;
import com.project.auth.models.database.Role;
import com.project.auth.models.database.Users;
import com.project.auth.models.enums.RoleType;
import com.project.auth.repositories.UserRepository;
import com.project.auth.services.impl.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import javax.management.relation.RoleNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.project.auth.constants.CustomExceptionConstants.USER_NOT_FOUND_CONTROLLER_ERROR_CODE;
import static com.project.auth.constants.CustomExceptionConstants.USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private IRoleService roleService;

    @Mock
    private IMailService mailService;

    @Mock
    private UserRepository userRepository;

    private Users user;

    private RequestUserDTO requestUserDTO;

    private RequestUserDTO requestUserUpdateDTO;

    private Role role;


    @BeforeEach
    void setup() {

        requestUserDTO = new RequestUserDTO();
        requestUserDTO.setName("Name");
        requestUserDTO.setLastname("Lastname");
        requestUserDTO.setEmail("email@gmail.com");
        requestUserDTO.setUsername("username");
        requestUserDTO.setBusinessAreaId(1L);
        requestUserDTO.setRole(RoleType.ADMIN.name());

        requestUserUpdateDTO = new RequestUserDTO();
        requestUserUpdateDTO.setUsername("newName");
        requestUserUpdateDTO.setName("newName");
        requestUserUpdateDTO.setLastname("newLastname");
        requestUserUpdateDTO.setEmail("NewEmail@gmail.com");
        requestUserUpdateDTO.setRole(RoleType.ADMIN.name());
        requestUserUpdateDTO.setBusinessAreaId(1L);

        user = new Users();
        user.setUsername("Name");
        user.setLastName("Lastname");
        user.setEmail("email@gmail.com");
        user.setRole(new Role(RoleType.ADMIN));

        role = new Role(RoleType.ADMIN);
    }

    @Test
    @Order(1)
    void whenCreateUserWithCorrectBody_ThenUserCreatedSuccessfully() throws RoleNotFoundException {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findFirstByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        when(roleService.findRoleByType(any())).thenReturn(role);

        Users userResult = userService.createUser(requestUserDTO);

        verify(mailService, timeout(200).times(1))
                .sendNewUserEmail(any(), anyString(), anyString(), any());

        assertEquals(requestUserDTO.getName(), userResult.getFirstName());
        assertEquals(requestUserDTO.getLastname(), userResult.getLastName());
        assertEquals(requestUserDTO.getUsername(), userResult.getUsername());
    }

    @Test
    @Order(2)
    void whenCreateUserWithARepeatedUserName_ThenThrowUserNameAlreadyTakenException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        UserNameAlreadyTakenException userNameAlreadyTakenException =
                Assertions.assertThrows(UserNameAlreadyTakenException.class,
                        () -> userService.createUser(requestUserDTO));

        assertEquals(HttpStatus.BAD_REQUEST, userNameAlreadyTakenException.getStatus());
        assertEquals(CustomExceptionConstants.USER_NAME_ALREADY_TAKEN_ERROR_CODE,
                userNameAlreadyTakenException.getErrorCode());
        assertEquals(CustomExceptionConstants.USER_NAME_ALREADY_TAKEN_ERROR_MESSAGE,
                userNameAlreadyTakenException.getMessage());

    }

    @Test
    @Order(4)
    void whenCreateUserWithARepeatedEmail_ThenThrowUserNameAlreadyTakenException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        when(userRepository.findFirstByEmailIgnoreCase(anyString())).thenReturn(Optional.of(user));

        EmailAlreadyTakenException emailAlreadyTakenException =
                Assertions.assertThrows(EmailAlreadyTakenException.class,
                        () -> userService.createUser(requestUserDTO));

        assertEquals(HttpStatus.BAD_REQUEST, emailAlreadyTakenException.getStatus());
        assertEquals(CustomExceptionConstants.USER_EMAIL_ALREADY_TAKEN_ERROR_CODE,
                emailAlreadyTakenException.getErrorCode());
        assertEquals(CustomExceptionConstants.USER_EMAIL_ALREADY_TAKEN_ERROR_MESSAGE,
                emailAlreadyTakenException.getMessage());

    }

    @Test
    @Order(5)
    void whenCreateUserWithAIncorrectRole_ThenThrowUserNameAlreadyTakenException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findFirstByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        when(roleService.findRoleByType(any())).thenThrow(new ObjectNotFoundException(
                CustomExceptionConstants.ROLE_NOT_FOUND_ERROR_CODE,
                CustomExceptionConstants.ROLE_NOT_FOUND_ERROR_MESSAGE));

        ObjectNotFoundException areaNotFoundException =
                Assertions.assertThrows(ObjectNotFoundException.class,
                        () -> userService.createUser(requestUserDTO));

        assertEquals(HttpStatus.NOT_FOUND, areaNotFoundException.getStatus());
        assertEquals(CustomExceptionConstants.ROLE_NOT_FOUND_ERROR_CODE,
                areaNotFoundException.getErrorCode());
        assertEquals(CustomExceptionConstants.ROLE_NOT_FOUND_ERROR_MESSAGE,
                areaNotFoundException.getMessage());
    }


    @Test
    @Order(6)
    void whenDeleteUserWithCorrectRequest_ThenUserDeleteSuccessfully() {

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(new Users()));

        Assertions.assertEquals("deleted", userService.deleteUser(1L));

    }

    @Test
    @Order(7)
    void whenDeleteUserNonExistent_ThenReturnUserNotFoundException() {

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        UserNotFoundException userNotFoundException =
                Assertions.assertThrows(UserNotFoundException.class,
                        () -> userService.deleteUser(1L));

        Assertions.assertEquals(HttpStatus.NOT_FOUND, userNotFoundException.getStatus());
        Assertions.assertEquals(
                CustomExceptionConstants.USER_NOT_FOUND_CONTROLLER_ERROR_CODE,
                userNotFoundException.getErrorCode());
        Assertions.assertEquals(
                String.format(USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE, 1),
                userNotFoundException.getMessage());
    }


    @Test
    @Order(8)
    void whenUpdateUserDataWithCorrectBody_ThenUserUpdatedSuccessfully() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        when(roleService.findRoleByType(any())).thenReturn(role);

        when(userRepository.save(any())).thenReturn(user);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Users user = userService
                .updateUser(1, requestUserUpdateDTO);

        Assertions.assertEquals(requestUserUpdateDTO.getName(), user.getFirstName());
        Assertions.assertEquals(requestUserUpdateDTO.getLastname(), user.getLastName());
        Assertions.assertEquals(requestUserUpdateDTO.getEmail(), user.getEmail());
    }

    @Test
    @Order(9)
    void whenUpdateUserButUserIdNotFound_ThenThrowUserNotFoundException() {
        when(userRepository.findById(anyLong())).thenThrow(
                new UserNotFoundException(USER_NOT_FOUND_CONTROLLER_ERROR_CODE,
                        String.format(USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE, 1)));

        UserNotFoundException userNotFoundException =
                Assertions.assertThrows(UserNotFoundException.class,
                        () -> userService.updateUser(1, requestUserUpdateDTO));

        assertEquals(HttpStatus.NOT_FOUND, userNotFoundException.getStatus());
        assertEquals(CustomExceptionConstants.USER_NOT_FOUND_CONTROLLER_ERROR_CODE,
                userNotFoundException.getErrorCode());
        assertEquals(String.format((USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE), 1),
                userNotFoundException.getMessage());

    }

    @Test
    @Order(11)
    void whenUpdateUserButRoleTypeNotFound_ThenThrowObjectNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        when(roleService.findRoleByType(any())).thenThrow(new ObjectNotFoundException(
                CustomExceptionConstants.ROLE_NOT_FOUND_ERROR_CODE,
                CustomExceptionConstants.ROLE_NOT_FOUND_ERROR_MESSAGE));

        ObjectNotFoundException areaNotFoundException =
                Assertions.assertThrows(ObjectNotFoundException.class,
                        () -> userService.updateUser(1, requestUserUpdateDTO));

        assertEquals(HttpStatus.NOT_FOUND, areaNotFoundException.getStatus());
        assertEquals(CustomExceptionConstants.ROLE_NOT_FOUND_ERROR_CODE,
                areaNotFoundException.getErrorCode());
        assertEquals(CustomExceptionConstants.ROLE_NOT_FOUND_ERROR_MESSAGE,
                areaNotFoundException.getMessage());

    }

    @Test
    @Order(12)
    void whenUpdateUserStateWithCorrectBody_ThenUserUpdatedSuccessfully() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        when(userRepository.save(any())).thenReturn(user);

        String result = userService.updateStateUser(1, false);

        Assertions.assertEquals(result, "State updated successfully");
    }

    @Test
    @Order(13)
    void whenUpdateStateUserButUserIdNotFound_ThenThrowUserNotFoundException() {
        when(userRepository.findById(anyLong())).thenThrow(
                new UserNotFoundException(USER_NOT_FOUND_CONTROLLER_ERROR_CODE,
                        String.format(USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE, 1)));

        UserNotFoundException userNotFoundException =
                Assertions.assertThrows(UserNotFoundException.class,
                        () -> userService.updateStateUser(1, false));

        assertEquals(HttpStatus.NOT_FOUND, userNotFoundException.getStatus());
        assertEquals(CustomExceptionConstants.USER_NOT_FOUND_CONTROLLER_ERROR_CODE,
                userNotFoundException.getErrorCode());
        assertEquals(String.format(USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE, 1),
                userNotFoundException.getMessage());

    }

    @Test
    @Order(14)
    void whenSearchForUsersThenItReturnsThem() {
        Users user1 = user;
        user1.setUsername("USER1");
        user1.setFirstName("USER1-FIRSTNAME");
        user1.setLastName("USER1-LASTNAME");
        ReflectionTestUtils.setField(user1, "id", 1L);

        Users user2 = user;
        user2.setUsername("USER2");
        user2.setFirstName("USER2-FIRSTNAME");
        user2.setLastName("USER2-LASTNAME");
        ReflectionTestUtils.setField(user2, "id", 2L);

        when(userRepository.findByFilter(anyString(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(user1, user2)));
        PageRequest pageRequest = PageRequest
                .of(0, 25, Direction.ASC, "id");

        Page<ResponseUserDTO> result = userService.findUsers("", null, pageRequest);
        assertEquals(result.getTotalElements(), 2);

        List<ResponseUserDTO> resultList = result.get().collect(Collectors.toList());
        assertEquals(resultList.get(0).getName(), user1.getFirstName());
        assertEquals(resultList.get(0).getLastname(), user1.getLastName());
        assertEquals(resultList.get(0).getUserName(), user1.getUsername());

        assertEquals(resultList.get(1).getName(), user1.getFirstName());
        assertEquals(resultList.get(1).getLastname(), user1.getLastName());
        assertEquals(resultList.get(1).getUserName(), user1.getUsername());
    }

    @Test
    @Order(15)
    void whenUpdateUserButUsernameAlreadyTaken_ThenThrowUserNameAlreadyTakenException() {
        requestUserUpdateDTO.setRole("COLLABORATOR");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        UserNameAlreadyTakenException userNameAlreadyTakenException =
                Assertions.assertThrows(UserNameAlreadyTakenException.class,
                        () -> userService.updateUser(1, requestUserUpdateDTO));

        assertEquals(HttpStatus.BAD_REQUEST, userNameAlreadyTakenException.getStatus());
        assertEquals(CustomExceptionConstants.USER_NAME_ALREADY_TAKEN_ERROR_CODE,
                userNameAlreadyTakenException.getErrorCode());
        assertEquals(CustomExceptionConstants.USER_NAME_ALREADY_TAKEN_ERROR_MESSAGE,
                userNameAlreadyTakenException.getMessage());

    }
}
