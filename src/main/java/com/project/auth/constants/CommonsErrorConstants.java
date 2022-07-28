package com.project.auth.constants;

public final class CommonsErrorConstants {

    public static final String DEFAULT_SERVICE_ERROR_CODE = "ERR-099";

    public static final String DEFAULT_SERVICE_ERROR_MESSAGE = "Service running failed";

    public static final String EXTERNAL_API_ERROR_MESSAGE = "Error communicating with %s";

    public static final String INTERNAL_ERROR_CODE = "ERR-001";

    public static final String INTERNAL_ERROR_MESSAGE = "Internal system error";

    public static final String METHOD_NOT_ALLOWED_ERROR_CODE = "ERR-002";

    public static final String METHOD_NOT_ALLOWED_ERROR_MESSAGES =
            "The allowed HTTP methods are: %s";

    public static final String UNSUPPORTED_MEDIA_TYPE_ERROR_CODE = "ERR-003";

    public static final String UNSUPPORTED_MEDIA_TYPE_ERROR_MESSAGE = "Content type not supported";

    public static final String REQUEST_GENERIC_ERROR_CODE = "ERR-004";

    public static final String REQUEST_GENERIC_ERROR_MESSAGE = "The request is wrong";

    public static final String TYPE_MISMATCH_PARAM_ERROR_CODE = "ERR-005";

    public static final String TYPE_MISMATCH_PARAM_ERROR_MESSAGE =
            "Parameter %s is not of the expected type";

    public static final String DATA_BASE_ERROR_CODE = "ERR-006";

    public static final String DATA_BASE_UNIQUE_VIOLATION_PSQL_CODE = "23505";

    public static final String DATA_BASE_FOREIGN_KEY_VIOLATION_PSQL_CODE = "23503";

    public static final String DATA_BASE_FOREIGN_KEY_VIOLATION_PSQL_MESSAGE =
            "The selected resource cannot be deleted because it is related to other "
                    + "resources of the application";

    public static final String REQUEST_VALIDATION_ERROR_CODE = "ERR-007";

    public static final String REQUEST_VALIDATION_ERROR_MESSAGE = "One or more fields are invalid";

    public static final String REQUIRED_PARAM_ERROR_MESSAGE =
            "The %s attribute is mandatory and must not be empty";

    public static final String REQUIRED_PARAM_NULL_ERROR_MESSAGE =
            "The %s attribute is mandatory";

    public static final String REQUIRED_PARAM_EMPTY_ERROR_MESSAGE =
            "The %s attribute must not be empty";

    public static final String REQUIRED_HEADER_ERROR_MESSAGE =
            "The %s header is mandatory";

    public static final String INCORRECT_MAIL_ERROR_MESSAGE =
            "The %s attribute is not in the correct format";

    public static final String MAX_SIZE_ERROR_MESSAGE =
            "The %s attribute must be no more than {max} characters";

    public static final String MAX_LIST_SIZE_ERROR_MESSAGE =
            "The %s attribute list must have a maximum of {max} elements";

    public static final String MIN_SIZE_ERROR_MESSAGE =
            "The %s attribute must be no less than {min} characters";

    public static final String MAX_VALUE_ERROR_MESSAGE =
            "The %s attribute cannot be greater than the {value}";

    public static final String MIN_VALUE_ERROR_MESSAGE =
            "The %s attribute cannot be less than the {value}";

    public static final String PASSWORD_VALUE_ERROR_MESSAGE =
            "The %s attribute does not comply with the expected structure. It must "
                    + "have at least 8 characters, at least one lower case, at least one upper "
                    + "case and at least one numeric character.";

    public static final String STRING_NUMERIC_ERROR_MESSAGE = "The %s attribute must be numeric";

    public static final String DECIMAL_ERROR_MESSAGE =
            "Incorrect format, the integer part must have a maximum " +
                    "of {integer} digits and the decimal part must have a maximum of {fraction} "
                    + "digits";

    public static final String ALPHABET_VALUE_ERROR_MESSAGE =
            "The %s attribute must only contain alphabetic characters";

    public static final String USERNAME_VALUE_ERROR_MESSAGE =
            "The %s attribute only accepted special characters are: hyphen (-), underscore (_) "
                    + "and period (.)";

    public static final String JWT_EXPIRED_ERROR_CODE = "ERR-008";

    public static final String JWT_EXPIRED_ERROR_MESSAGE = "JWT token is expired";

    public static final String JWT_INVALID_ERROR_CODE = "ERR-009";

    public static final String JWT_INVALID_ERROR_MESSAGE = "Invalid JWT token";

    public static final String AUTH_GENERAL_ERROR_CODE = "ERR-010";

    public static final String AUTH_GENERAL_ERROR_MESSAGE =
            "Full authentication is required to access this resource";

    public static final String FORBIDDEN_ERROR_CODE = "ERR-011";

    public static final String FORBIDDEN_ERROR_MESSAGE = "Access is denied";

    public static final String UPDATE_PASS_INTERNAL_ERROR_MESSAGE =
            "The logged in user cannot use the selected resource";

    public static final String LOG_ERROR_MESSAGE = "Messages: {} ";

    public static final String LOG_VALIDATION_MESSAGE = "Exception: {}. Messages: {}";

    public static final String TELEPHONE_INCORRECT_FORMAT_ERROR_MESSAGE =
            "The phone field is numeric and accepts +- characters";

    public static final String WEIGHT_PARAM_ERROR_MESSAGE =
            "The minimal value must be 0,5";

    public static final String LENGTH_PARAM_ERROR_MESSAGE =
            "The minimal value must be 1";

    public static final String WIDTH_PARAM_ERROR_MESSAGE =
            "The minimal value must be 1";

    public static final String HEIGHT_PARAM_ERROR_MESSAGE =
            "The minimal value must be 1";

    private CommonsErrorConstants() {
    }
}
