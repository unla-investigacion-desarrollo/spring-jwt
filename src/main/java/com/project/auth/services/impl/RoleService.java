package com.project.auth.services.impl;

import com.project.auth.constants.CustomExceptionConstants;
import com.project.auth.exceptions.RoleNotFoundException;
import com.project.auth.models.database.Role;
import com.project.auth.models.enums.RoleType;
import com.project.auth.repositories.RoleRepository;
import com.project.auth.services.IRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RoleService implements IRoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * This method find all roles If the roles exist in the database then retrieve all the roles
     *
     * @return List<Role>
     */
    @Override
    public List<Role> findRoles() {
        log.debug("getting all roles");
        List<Role> roles = roleRepository.findAll();
        log.debug("found {} roles", roles.size());

        return roles;
    }

    /**
     * This method find a role by roleID If the role exists in the database then retrieve the role,
     * If the role does not exist throw a RoleNotFoundException
     *
     * @param idRole : the role id
     * @return Role
     */
    @Override
    public Role findRoleById(long idRole) {
        log.debug("getting role by id {}", idRole);

        return roleRepository.findById(idRole)
                .orElseThrow(() -> new RoleNotFoundException(
                        CustomExceptionConstants.ROLE_NOT_FOUND_ERROR_MESSAGE));
    }

    @Override
    public Role findRoleByType(RoleType type) {
        log.debug("getting role by type {}", type);

        return roleRepository.findByType(type)
                .orElseThrow(() -> new RoleNotFoundException(
                        String.format(CustomExceptionConstants.ROLE_TYPE_NOT_FOUND_ERROR_MESSAGE, type)));
    }
}
