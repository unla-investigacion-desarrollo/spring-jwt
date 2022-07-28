package com.project.auth.services;

import com.project.auth.dtos.request.user.RequestUserDTO;
import com.project.auth.dtos.response.user.ResponseUserDTO;
import com.project.auth.models.database.Users;
import javax.management.relation.RoleNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IUserService {

    Users createUser(RequestUserDTO requestUserDTO) throws RoleNotFoundException;

    String updateStateUser(long userId, boolean active);

    Users updateUser(long userId, RequestUserDTO requestUserDTO);

    String deleteUser(long userId);

    Users findUserById(long usersId);

    Page<ResponseUserDTO> findUsers(String search, Boolean active, PageRequest pageRequest);
}
