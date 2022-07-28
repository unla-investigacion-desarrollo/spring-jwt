package com.project.auth.constants;

public final class CustomExceptionConstants {

    //***********************************************
    //**** ALREADY OWNED OR ALREADY TAKEN ERRORS ****
    //***********************************************

    public static final String USER_NAME_ALREADY_TAKEN_ERROR_CODE
            = "ERR-100";

    public static final String USER_NAME_ALREADY_TAKEN_ERROR_MESSAGE = "User name already taken";

    public static final String USER_EMAIL_ALREADY_TAKEN_ERROR_CODE
            = "ERR-101";

    public static final String USER_EMAIL_ALREADY_TAKEN_ERROR_MESSAGE = "User email already taken";

    //*******************************************************
    //**** NOT OWNED OR NOT BELONG OR NOT ALLOWED ERRORS ****
    //*******************************************************

    //Only for Logs
    public static final String ROLE_NOT_OWNED_GENERATE_TOKEN_ERROR_MESSAGE =
            "The user entered does not have roles. Unable to generate a token.";

    //**************************
    //**** NOT FOUND ERRORS ****
    //**************************

    public static final String USER_NOT_FOUND_LOGIN_ERROR_CODE = "ERR-200";

    public static final String USER_NOT_FOUND_LOGIN_ERROR_MESSAGE = "The username is incorrect";

    //Only for Logs
    public static final String USER_INFO_LOGIN_NOT_FOUND_MESSAGE = "User information login not "
            + "found";

    public static final String USER_NOT_FOUND_CONTROLLER_ERROR_CODE = "ERR-201";

    public static final String USER_NOT_FOUND_CONTROLLER_ERROR_MESSAGE = "User with [id: %d] not found";

    public static final String ROLE_NOT_FOUND_ERROR_CODE = "ERR-202";

    public static final String ROLE_NOT_FOUND_ERROR_MESSAGE = "Role [id: %d] not found";

    public static final String ROLE_TYPE_NOT_FOUND_ERROR_MESSAGE = "Role with type: %s not found";


    //**************************
    //**** NOT VALID ERRORS ****
    //**************************

    public static final String USER_BLOCKED_OR_EXPIRED_CODE = "ERR-300";

    public static final String USER_BLOCKED_MESSAGE = "User blocked, has 3 failed attempts. Please start the password recovery process";

    public static final String AUTH_SIGNIN_FILED_ATTEMPTS_ERROR_CODE = "ERR-301";

    public static final String AUTH_SIGNIN_FILED_ATTEMPTS_ERROR_MESSAGE = "The username or password is incorrect. You have 3 attempts before blocking the user";

    public static final String USER_INACTIVE_CODE = "ERR-301";

    public static final String USER_INACTIVE_MESSAGE = "The user state is inactive";


    private CustomExceptionConstants() {
    }
}
