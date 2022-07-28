package com.project.auth.constants.documentation;


import com.project.auth.constants.CommonsErrorConstants;

public final class RoleControllerConstants {

    public static final String FIND_ROLE_BY_ID_API_OPERATION = "Returns a role filtered by the id";

    public static final String FIND_ROLE_BY_ID_RESPONSE_200 = "Role found and returned "
            + "successfully";

    public static final String FIND_ROLE_BY_ID_RESPONSE_404 = "Role not found";

    public static final String FIND_ROLES_API_OPERATION = "Returns all roles";

    public static final String FIND_ROLES_RESPONSE_200 = "Roles found and returned successfully";

    public static final String ROLE_RESPONSE_400 = "Invalid Request";

    public static final String ROLE_RESPONSE_500 = CommonsErrorConstants.INTERNAL_ERROR_MESSAGE;

    private RoleControllerConstants() {
    }
}
