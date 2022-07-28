package com.project.auth.constants.documentation;


public final class AuthControllerConstants {

    public static final String SIGN_IN_API_OPERATION = "Returns the user logged in with his token";

    public static final String SIGN_IN_RESPONSE_200 = "User logged in successfully";

    public static final String SIGN_IN_RESPONSE_401 = "The username or password is incorrect";

    public static final String SIGN_IN_RESPONSE_400 = "Invalid Request";

    public static final String FIRST_SIGN_IN_API_OPERATION =
            "Update the password of the users who log in for the first time";

    public static final String FIRST_SIGN_IN_RESPONSE_200 =
            "Password has been changed successfully";

    public static final String FIRST_SIGN_IN_RESPONSE_401 = "Invalid authorization";

    public static final String FIRST_SIGN_IN_RESPONSE_400 = "Invalid Request";

    public static final String RECOVERY_PASS_API_OPERATION =
            "Send an email with a random password when the user forgets their password";

    public static final String RECOVERY_PASS_RESPONSE_200 = "An email has been sent to your "
            + "account to continue with the operation";

    public static final String RECOVERY_PASS_RESPONSE_404 = "The username is incorrect";

    public static final String RECOVERY_PASS_RESPONSE_400 = "Invalid Request";


    public static final String VALIDATE_TOKEN_API_OPERATION = "Receive a token and validate its "
            + "integrity and expiration";

    public static final String VALIDATE_TOKEN_RESPONSE_200 = "OK";

    public static final String VALIDATE_TOKEN_RESPONSE_401 = "Full authentication is required or "
            + "token is invalid or expired";

    public static final String VALIDATE_TOKEN_RESPONSE_403 = "Forbidden";


    private AuthControllerConstants() {
    }
}
