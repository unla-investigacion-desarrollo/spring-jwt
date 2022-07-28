package com.project.auth.controllers;


import com.project.auth.constants.CommonsErrorConstants;
import com.project.auth.constants.documentation.UserControllerConstants;
import com.project.auth.converters.UserConverter;
import com.project.auth.dtos.request.user.RequestUserDTO;
import com.project.auth.dtos.response.user.ResponseUserDTO;
import com.project.auth.models.response.ApplicationResponse;
import com.project.auth.models.response.ErrorResponse;
import com.project.auth.services.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.management.relation.RoleNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Optional;

@Slf4j
@RestController
@Validated
@RequestMapping("/users")
@Tag(name = "User Controller")
@CrossOrigin(origins = {"*"}, maxAge = 3600)
public class UsersController {

    private final IUserService userService;

    public UsersController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = UserControllerConstants.CREATE_USER_API_OPERATION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description =
                    UserControllerConstants.CREATE_USER_201),
            @ApiResponse(responseCode = "400", description = UserControllerConstants.USER_400,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description =
                    CommonsErrorConstants.INTERNAL_ERROR_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse<ResponseUserDTO> createExternalUser(
            @Valid @RequestBody RequestUserDTO requestUserDTO) throws RoleNotFoundException {

        log.info("POST /users");

        ResponseUserDTO responseUserDTO = UserConverter.toResponseUserDTO(
                userService.createUser(requestUserDTO));

        return new ApplicationResponse<>(responseUserDTO, null);
    }

    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = UserControllerConstants.GET_BY_ID_USER_API_OPERATION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description =
                    UserControllerConstants.GET_BY_ID_USER_200),
            @ApiResponse(responseCode = "404", description =
                    UserControllerConstants.GET_BY_ID_USER_404,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description =
                    CommonsErrorConstants.INTERNAL_ERROR_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @ResponseStatus(HttpStatus.OK)
    public ApplicationResponse<ResponseUserDTO> getUserById(
            @PathVariable("userId")
            @Pattern(regexp = "[0-9]+", message =
                    CommonsErrorConstants.STRING_NUMERIC_ERROR_MESSAGE)
            @Parameter(name = "userId", description = "User id", example = "1", required = true)
                    String userId
    ) {

        log.info("GET /users/{}", userId);

        ResponseUserDTO responseUserDTO = UserConverter.toResponseUserDTO(
                userService.findUserById(Long.parseLong(userId)));

        return new ApplicationResponse<>(responseUserDTO, null);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = UserControllerConstants.FIND_USER_API_OPERATION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description =
                    UserControllerConstants.FIND_USER_200_RESPONSE),
            @ApiResponse(responseCode = "400", description = UserControllerConstants.USER_400,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = UserControllerConstants.USER_404,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description =
                    CommonsErrorConstants.INTERNAL_ERROR_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApplicationResponse<Page<ResponseUserDTO>>> findUsers(
            @RequestParam(required = false, value = "search") String search,
            @RequestParam(required = false, value = "page") Optional<Integer> page,
            @RequestParam(required = false, value = "sortBy") Optional<String> sortBy,
            @RequestParam(required = false, value = "active") Boolean active,
            @RequestParam(required = false, value = "direction") Optional<Direction> direction,
            @RequestParam(required = false, value = "size") Optional<Integer> size
    ) {
        Page<ResponseUserDTO> responseUserDTOS;
        log.info("GET /users");

        PageRequest pageRequest = PageRequest
                .of(page.orElse(0), size.orElse(25), direction.orElse(Direction.ASC),
                        sortBy.orElse("id"));

        responseUserDTOS = userService.findUsers(
                search == null ? "" : search,
                active,
                pageRequest
        );

        ApplicationResponse<Page<ResponseUserDTO>> applicationResponse =
                new ApplicationResponse<>(responseUserDTOS, null);
        return ResponseEntity.ok(applicationResponse);
    }

    @DeleteMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = UserControllerConstants.DELETE_USER_API_OPERATION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description =
                    UserControllerConstants.DELETE_USER_200),
            @ApiResponse(responseCode = "404", description =
                    UserControllerConstants.DELETE_USER_404,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description =
                    CommonsErrorConstants.INTERNAL_ERROR_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApplicationResponse<String>> deleteUser(
            @PathVariable("userId")
            @Pattern(regexp = "[0-9]+", message =
                    CommonsErrorConstants.STRING_NUMERIC_ERROR_MESSAGE)
            @Parameter(name = "userId", description = "User id", example = "1",
                    required = true)
                    String userId
    ) {
        log.info("DELETE /users/{}", userId);

        ApplicationResponse<String> applicationResponse =
                new ApplicationResponse<>(
                        userService.deleteUser(Long.parseLong(userId)),
                        null);

        return ResponseEntity.ok().body(applicationResponse);
    }


    @PutMapping(value = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = UserControllerConstants.UPDATE_USER_API_OPERATION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description =
                    UserControllerConstants.UPDATE_USER_200),
            @ApiResponse(responseCode = "404", description =
                    UserControllerConstants.UPDATE_USER_404,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description =
                    CommonsErrorConstants.INTERNAL_ERROR_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public ApplicationResponse<ResponseUserDTO> updateUser(
            @PathVariable("userId")
            @Pattern(regexp = "[0-9]+", message =
                    CommonsErrorConstants.STRING_NUMERIC_ERROR_MESSAGE)
            @Parameter(name = "userId", description = "User id", example = "1", required = true)
                    String userId,
            @Valid @RequestBody RequestUserDTO requestUserDTO
    ) {

        log.info("PUT /users/{}", userId);

        ResponseUserDTO responseUserDTO = UserConverter.toResponseUserDTO(
                userService.updateUser(Long.parseLong(userId),
                        requestUserDTO));

        log.info("User updated successfully");

        return new ApplicationResponse<>(responseUserDTO, null);
    }

    @PutMapping(value = "/states/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = UserControllerConstants.UPDATE_STATE_USER_API_OPERATION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description =
                    UserControllerConstants.UPDATE_STATE_USER_200),
            @ApiResponse(responseCode = "404", description =
                    UserControllerConstants.UPDATE_STATE_USER_404,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description =
                    CommonsErrorConstants.INTERNAL_ERROR_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public ApplicationResponse<String> updateUserState(
            @PathVariable("userId")
            @Pattern(regexp = "[0-9]+", message =
                    CommonsErrorConstants.STRING_NUMERIC_ERROR_MESSAGE)
            @Parameter(name = "userId", description = "User id", example = "1", required = true)
                    String userId,
            @Parameter(name = "active", description = "User status", example = "true", required =
                    true)
            @NotNull(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
            @RequestParam(value = "active") Boolean active
    ) {

        log.info("PUT /users/states/{}", userId);

        return new ApplicationResponse<>(
                userService.updateStateUser(Long.parseLong(userId), active),
                null);
    }

    //TODO: Agregar un endpoint rest que permita unicamente cambiar el password del usuario.
    // Este endpoint tendra que estar liberado de autenticacion para que cualquiera pueda usarlo, pero,
    // un usuario solo podra cambiar su propia constraseña (asegurar que esta restriccion se cumpla)
}
