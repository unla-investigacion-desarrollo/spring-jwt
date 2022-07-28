package com.project.auth.constants;

public final class SecurityConfigConstants {

    public static final String REQUEST_URI_FIRST_SIGN_IN = "/api/auth/first-signin";

    public static final String ENCODING_ALGORITHM = "SHA-512";

    public static final String ANT_PATTERNS_SING_IN_ENDPOINT = "/auth/signin";

    public static final String ANT_PATTERNS_RECOVER_PASS_ENDPOINT = "/auth/recover-password";


    //EMAIL
    public static final String EMAIL_FROM = "no-reply@newapp.com.ar";

    public static final String EMAIL_NAME = "CENADIF";


    //RECOVERY PASSWORD EMAIL
    public static final String RECOVERY_PASS_MAIL_SUBJECT = "Cambiar contraseña - CENADIF";


    //NEW USER EMAIL
    public static final String NEW_USER_MAIL_SUBJECT = "Nuevo usuario - CENADIF";


    private SecurityConfigConstants() {
    }
}
