package com.project.auth.constants.documentation;

import com.project.auth.constants.CommonsErrorConstants;

public final class UserControllerConstants {

    public static final String CREATE_USER_API_OPERATION = "Create new user";

    public static final String CREATE_USER_201 = "User created successfully";

    public static final String USER_400 = "Invalid Request";

    public static final String USER_404 = "User not found";

    public static final String USER_500 = CommonsErrorConstants.INTERNAL_ERROR_MESSAGE;

    public static final String FIND_USER_API_OPERATION = "Find users";

    public static final String FIND_USER_200_RESPONSE = "Find users successfully";

    public static final String DELETE_USER_API_OPERATION = "Delete user";

    public static final String DELETE_USER_200 = "User deleted successfully";

    public static final String DELETE_USER_404 = "User to delete not found";

    public static final String UPDATE_USER_API_OPERATION = "Update user";

    public static final String UPDATE_USER_200 = "User updated successfully";

    public static final String UPDATE_USER_404 = "User to update not found";

    public static final String UPDATE_STATE_USER_API_OPERATION = "Update state user";

    public static final String UPDATE_STATE_USER_200 = "User state updated "
            + "successfully";

    public static final String UPDATE_STATE_USER_404 = "User to update not found";

    public static final String GET_BY_ID_USER_API_OPERATION = "Get user by id";

    public static final String GET_BY_ID_USER_200 = "User found successfully";

    public static final String GET_BY_ID_USER_404 = "User not found";

    private UserControllerConstants() {
    }
}
