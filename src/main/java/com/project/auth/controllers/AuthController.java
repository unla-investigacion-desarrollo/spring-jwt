package com.project.auth.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.auth.constants.CommonsErrorConstants;
import com.project.auth.constants.documentation.AuthControllerConstants;
import com.project.auth.dtos.request.login.RequestLoginDTO;
import com.project.auth.dtos.request.login.RequestRecoverPassDTO;
import com.project.auth.dtos.response.login.ResponseLoginDTO;
import com.project.auth.models.response.ApplicationResponse;
import com.project.auth.models.response.ErrorResponse;
import com.project.auth.services.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Authorization Controller")
@CrossOrigin(origins = {"*"}, maxAge = 3600)
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }


    @GetMapping(value = "/validate-token", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = AuthControllerConstants.VALIDATE_TOKEN_API_OPERATION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description =
                    AuthControllerConstants.VALIDATE_TOKEN_RESPONSE_200),
            @ApiResponse(responseCode = "401", description =
                    AuthControllerConstants.VALIDATE_TOKEN_RESPONSE_401,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "403", description =
                    AuthControllerConstants.VALIDATE_TOKEN_RESPONSE_403,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description =
                    CommonsErrorConstants.INTERNAL_ERROR_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<ApplicationResponse<String>> validateToken() {

        log.info("GET /auth/validateToken");

        return ResponseEntity.ok(
                new ApplicationResponse<>("OK", null));
    }


    @PostMapping(value = "/signin", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = AuthControllerConstants.SIGN_IN_API_OPERATION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description =
                    AuthControllerConstants.SIGN_IN_RESPONSE_200),
            @ApiResponse(responseCode = "400", description =
                    AuthControllerConstants.SIGN_IN_RESPONSE_400,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description =
                    AuthControllerConstants.SIGN_IN_RESPONSE_401,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description =
                    CommonsErrorConstants.INTERNAL_ERROR_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<ApplicationResponse<ResponseLoginDTO>> authenticateSignin(
            @Valid @RequestBody RequestLoginDTO requestLoginDTO) throws JsonProcessingException {

        log.info("POST /auth/signin with username: {}", requestLoginDTO.getUsername());

        ResponseLoginDTO response = authService.signInService(requestLoginDTO);

        return ResponseEntity.ok(
                new ApplicationResponse<>(response, null));
    }

    @PutMapping(value = "/recover-password", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = AuthControllerConstants.RECOVERY_PASS_API_OPERATION)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description =
                    AuthControllerConstants.RECOVERY_PASS_RESPONSE_200),
            @ApiResponse(responseCode = "400", description =
                    AuthControllerConstants.RECOVERY_PASS_RESPONSE_400,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description =
                    AuthControllerConstants.RECOVERY_PASS_RESPONSE_404,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "500", description =
                    CommonsErrorConstants.INTERNAL_ERROR_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<ApplicationResponse<String>> authenticateRecoverPassword(
            @Valid @RequestBody RequestRecoverPassDTO requestRecoverPassDTO) {

        log.info("PUT /auth/recover-password");

        String response = authService.recoverPassword(requestRecoverPassDTO);

        return ResponseEntity.ok(
                new ApplicationResponse<>(response, null));
    }

}
