package com.project.auth.services;

import com.project.auth.models.database.Role;
import com.project.auth.models.enums.RoleType;
import java.util.List;

public interface IRoleService {

    List<Role> findRoles();

    Role findRoleById(long idRole);

    Role findRoleByType(RoleType type);
}
