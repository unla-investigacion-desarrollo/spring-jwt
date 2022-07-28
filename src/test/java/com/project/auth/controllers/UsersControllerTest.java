package com.project.auth.controllers;

import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.auth.configurations.WebSecurityConfig;
import com.project.auth.constants.CommonsErrorConstants;
import com.project.auth.constants.CustomExceptionConstants;
import com.project.auth.dtos.request.user.RequestUserDTO;
import com.project.auth.dtos.response.user.ResponseUserDTO;
import com.project.auth.exceptions.EmailAlreadyTakenException;
import com.project.auth.exceptions.UserNameAlreadyTakenException;
import com.project.auth.exceptions.UserNotFoundException;
import com.project.auth.models.database.Role;
import com.project.auth.models.database.Users;
import com.project.auth.models.enums.RoleType;
import com.project.auth.security.jwt.AuthEntryPointJwt;
import com.project.auth.security.jwt.AuthTokenFilter;
import com.project.auth.security.jwt.JwtUtils;
import com.project.auth.security.service.UserDetailsServiceImpl;
import com.project.auth.services.impl.UserService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.project.auth.constants.CommonsErrorConstants.FORBIDDEN_ERROR_CODE;
import static com.project.auth.constants.CommonsErrorConstants.REQUEST_VALIDATION_ERROR_CODE;
import static com.project.auth.constants.CommonsErrorConstants.REQUEST_VALIDATION_ERROR_MESSAGE;
import static com.project.auth.constants.CustomExceptionConstants.USER_EMAIL_ALREADY_TAKEN_ERROR_CODE;
import static com.project.auth.constants.CustomExceptionConstants.USER_EMAIL_ALREADY_TAKEN_ERROR_MESSAGE;
import static com.project.auth.constants.CustomExceptionConstants.USER_NAME_ALREADY_TAKEN_ERROR_CODE;
import static com.project.auth.constants.CustomExceptionConstants.USER_NOT_FOUND_CONTROLLER_ERROR_CODE;
import static com.project.auth.constants.CustomExceptionConstants.USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsersController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Import({WebSecurityConfig.class, AuthTokenFilter.class, AuthEntryPointJwt.class, JwtUtils.class})
@WebAppConfiguration
class UsersControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    UserDetailsServiceImpl userDetailsService;

    @MockBean
    Tracer tracer;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private Users user;

    private RequestUserDTO requestUserDTO;

    private RequestUserDTO requestUserUpdateDTO;

    @BeforeEach
    void init() {

        requestUserDTO = new RequestUserDTO();
        requestUserDTO.setName("Name");
        requestUserDTO.setLastname("Lastname");
        requestUserDTO.setEmail("email@gmail.com");
        requestUserDTO.setUsername("username");
        requestUserDTO.setBusinessAreaId(1L);
        requestUserDTO.setRole(RoleType.ADMIN.name());

        requestUserUpdateDTO = new RequestUserDTO();
        requestUserUpdateDTO.setUsername("newUserName");
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
    }

    @Test
    @Order(1)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DisplayName(
            "Given a valid request when is want to create a new user, then, create a new User " +
                    "and return the created User")
    void testCreateNewUser() throws Exception {
        when(userService.createUser(any())).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("data.name").value(user.getFirstName()))
                .andExpect(jsonPath("data.lastname").value(user.getLastName()))
                .andExpect(jsonPath("data.email").value(user.getEmail()))
                .andExpect(jsonPath("errors").isEmpty())
                .andDo(print());
    }

    @Test
    @Order(2)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DisplayName(
            "Given a invalid request with many null or empty values when is want to create a new "
                    + "user, then, return Validation Exceptions")
    void testCreateNewUser400() throws Exception {

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new RequestUserDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("data").isEmpty())
                .andExpect(jsonPath("errors.code").value(REQUEST_VALIDATION_ERROR_CODE))
                .andExpect(jsonPath("errors.message").value(REQUEST_VALIDATION_ERROR_MESSAGE))
                .andExpect(jsonPath("$.errors.details[*].messages[*]")
                        .value(containsInAnyOrder(
                                String.format(CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE,
                                        "role"),
                                String.format(CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE,
                                        "email"),
                                String.format(CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE,
                                        "username"),
                                String.format(CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE,
                                        "name"),
                                String.format(CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE,
                                        "lastname"))));
    }

    @Test
    @Order(3)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DisplayName(
            "Given a invalid request with name, lastname and username malformed "
                    + "when is want to create a new user, then, return Validation Exceptions")
    void testCreateNewUserWithIncorrectNameLastNameAndUserName400() throws Exception {

        requestUserDTO.setName("/(/)$%");
        requestUserDTO.setLastname("/(/)$%");
        requestUserDTO.setUsername("Nicolas ))))%%");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("data").isEmpty())
                .andExpect(jsonPath("errors.code").value(REQUEST_VALIDATION_ERROR_CODE))
                .andExpect(jsonPath("errors.message").value(REQUEST_VALIDATION_ERROR_MESSAGE))
                .andExpect(jsonPath("$.errors.details[*].messages[*]")
                        .value(containsInAnyOrder(
                                String.format(CommonsErrorConstants.ALPHABET_VALUE_ERROR_MESSAGE,
                                        "lastname"),
                                String.format(CommonsErrorConstants.ALPHABET_VALUE_ERROR_MESSAGE,
                                        "name"),
                                String.format(CommonsErrorConstants.USERNAME_VALUE_ERROR_MESSAGE,
                                        "username"))));
    }

    @Test
    @Order(4)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testCreateNewUserThrowsUserNameAlreadyTakenException() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new UserNameAlreadyTakenException(
                        CustomExceptionConstants.USER_NAME_ALREADY_TAKEN_ERROR_MESSAGE));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("data").isEmpty())
                .andExpect(jsonPath("errors.code").value(USER_NAME_ALREADY_TAKEN_ERROR_CODE))
                .andExpect(jsonPath("errors.message").value(
                        CustomExceptionConstants.USER_NAME_ALREADY_TAKEN_ERROR_MESSAGE))
                .andDo(print());

    }

    @Test
    @Order(5)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testCreateNewUserThrowsEmailAlreadyTakenException() throws Exception {

        when(userService.createUser(any()))
                .thenThrow(new EmailAlreadyTakenException(
                        USER_EMAIL_ALREADY_TAKEN_ERROR_CODE, (USER_EMAIL_ALREADY_TAKEN_ERROR_MESSAGE)));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("data").isEmpty())
                .andExpect(jsonPath("errors.code").value(USER_EMAIL_ALREADY_TAKEN_ERROR_CODE))
                .andExpect(jsonPath("errors.message")
                        .value((USER_EMAIL_ALREADY_TAKEN_ERROR_MESSAGE)))
                .andDo(print());
    }

    @Test
    @Order(7)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DisplayName("Given a valid  request when delete user then return that user is deleted "
            + "successfully")
    void testDeleteUser() throws Exception {

        when(userService.deleteUser(anyLong())).thenReturn("deleted");

        mockMvc.perform(delete("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data").value("deleted"))
                .andExpect(jsonPath("errors").isEmpty())
                .andDo(print());
    }

    @Test
    @Order(8)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DisplayName("Given a request with user id invalid when delete user then return "
            + "UserNotFoundException")
    void testDeleteUserReturnUserNotFoundException404() throws Exception {

        when(userService.deleteUser(anyLong()))
                .thenThrow(new UserNotFoundException(USER_NOT_FOUND_CONTROLLER_ERROR_CODE,
                        String.format(USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE, 1)));

        mockMvc.perform(delete("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("data").isEmpty())
                .andExpect(jsonPath("errors.code").value(USER_NOT_FOUND_CONTROLLER_ERROR_CODE))
                .andExpect(jsonPath("errors.message").value(
                        String.format(USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE, 1)))
                .andDo(print());
    }

    @Test
    @Order(9)
    @WithMockUser(username = "collaborator", authorities = {"COLLABORATOR"})
    @DisplayName("Given a request with invalid role when delete user then return "
            + "AccessDeniedException")
    void testDeleteUser403() throws Exception {

        mockMvc.perform(delete("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("data").isEmpty())
                .andExpect(jsonPath("errors.code").value(FORBIDDEN_ERROR_CODE))
                .andExpect(jsonPath("errors.message").value("Access is denied"))
                .andDo(print());
    }

    @Test
    @Order(10)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DisplayName("When searches for users then returns them")
    void searchForUsers() throws Exception {
        ResponseUserDTO responseUserDTO = new ResponseUserDTO();
        responseUserDTO.setActive(false);
        responseUserDTO.setName("Name");
        responseUserDTO.setLastname("Lastname");
        responseUserDTO.setEmail("email@gmail.com");

        when(userService.findUsers(any(), any(), any())).thenReturn(
                new PageImpl<>(List.of(responseUserDTO)));

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("data.content[0].name").value(responseUserDTO.getName()))
                .andExpect(
                        jsonPath("data.content[0].lastname").value(responseUserDTO.getLastname()))
                .andExpect(jsonPath("data.content[0].email").value(responseUserDTO.getEmail()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DisplayName("Given a valid  request when update user then return user updated successfully")
    void testUpdateUser() throws Exception {

        when(userService.updateUser(anyLong(), any())).thenReturn(user);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestUserUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.name").value(user.getFirstName()))
                .andExpect(jsonPath("errors").isEmpty())
                .andDo(print());
    }

    @Test
    @Order(12)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DisplayName("Given a valid request when update user state then return user state updated "
            + "successfully")
    void testUpdateStateUser() throws Exception {

        when(userService.updateStateUser(anyLong(), anyBoolean())).thenReturn(
                "State updated successfully");

        mockMvc.perform(put("/users/states/1")
                        .contentType(MediaType.APPLICATION_JSON).queryParam("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data").value("State updated successfully"))
                .andExpect(jsonPath("errors").isEmpty())
                .andDo(print());
    }

    @Test
    @Order(13)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DisplayName("Given a request with user id invalid when update user then return "
            + "UserNotFoundException")
    void testUpdateUserReturnUserNotFoundException404() throws Exception {

        when(userService.updateUser(anyLong(), any()))
                .thenThrow(new UserNotFoundException(USER_NOT_FOUND_CONTROLLER_ERROR_CODE,
                        String.format(USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE, 1)));

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestUserUpdateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("data").isEmpty())
                .andExpect(jsonPath("errors.code").value(USER_NOT_FOUND_CONTROLLER_ERROR_CODE))
                .andExpect(jsonPath("errors.message")
                        .value(String.format(USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE, 1)))
                .andDo(print());
    }

    @Test
    @Order(14)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DisplayName("Given a request with user id invalid when update state user then return "
            + "UserNotFoundException")
    void testUpdateUserStateReturnUserNotFoundException404() throws Exception {

        when(userService.updateStateUser(anyLong(), anyBoolean()))
                .thenThrow(new UserNotFoundException(USER_NOT_FOUND_CONTROLLER_ERROR_CODE,
                        String.format(USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE, 1)));

        mockMvc.perform(put("/users/states/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("active", "true"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("data").isEmpty())
                .andExpect(jsonPath("errors.code").value(USER_NOT_FOUND_CONTROLLER_ERROR_CODE))
                .andExpect(jsonPath("errors.message")
                        .value(String.format(USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE, 1)))
                .andDo(print());
    }

    @Test
    @Order(15)
    @WithMockUser(username = "collaborator", authorities = {"COLLABORATOR"})
    @DisplayName("Given a request with invalid role when update user then return "
            + "AccessDeniedException")
    void testUpdateUser403() throws Exception {

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestUserUpdateDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("data").isEmpty())
                .andExpect(jsonPath("errors.code").value(FORBIDDEN_ERROR_CODE))
                .andExpect(jsonPath("errors.message").value("Access is denied"))
                .andDo(print());
    }

    @Test
    @Order(16)
    @WithMockUser(username = "collaborator", authorities = {"COLLABORATOR"})
    @DisplayName("Given a request with invalid role when update state user then return "
            + "AccessDeniedException")
    void testUpdateStateUser403() throws Exception {

        mockMvc.perform(put("/users/states/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("active", "true"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("data").isEmpty())
                .andExpect(jsonPath("errors.code").value(FORBIDDEN_ERROR_CODE))
                .andExpect(jsonPath("errors.message")
                        .value("Access is denied"))
                .andDo(print());
    }


    @Test
    @Order(17)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DisplayName("Given a valid request when search user by id then return user successfully")
    void testGetUserById() throws Exception {

        when(userService.findUserById(anyLong())).thenReturn(user);

        mockMvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.name").value(user.getFirstName()))
                .andExpect(jsonPath("errors").isEmpty())
                .andDo(print());
    }

    @Test
    @Order(18)
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    @DisplayName("Given a request with user id invalid when get user then return "
            + "UserNotFoundException")
    void testGetUserByIdReturnUserNotFoundException404() throws Exception {

        when(userService.findUserById(anyLong()))
                .thenThrow(new UserNotFoundException(USER_NOT_FOUND_CONTROLLER_ERROR_CODE,
                        String.format(USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE, 1)));

        mockMvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("data").isEmpty())
                .andExpect(jsonPath("errors.code").value(USER_NOT_FOUND_CONTROLLER_ERROR_CODE))
                .andExpect(jsonPath("errors.message")
                        .value(String.format(USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE, 1)))
                .andDo(print());
    }
}
