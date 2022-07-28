package com.project.auth.configurations;

import com.project.auth.models.database.Role;
import com.project.auth.models.database.Users;
import com.project.auth.models.enums.RoleType;
import com.project.auth.repositories.RoleRepository;
import com.project.auth.repositories.UserRepository;
import java.util.Optional;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class InitialDataLoaderSecurity implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    boolean alreadySetup = false;

    public InitialDataLoaderSecurity(
            UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (!alreadySetup && userRepository.findByUsername("Admin").isEmpty()) {

            Role adminRole = findRoleOrCreate(RoleType.ADMIN);
            findRoleOrCreate(RoleType.COORDINATOR);
            findRoleOrCreate(RoleType.COLLABORATOR);

            Users admin = new Users();
            admin.setEmail("adminEmail@gmail.com");
            admin.setUsername("Admin");
            admin.setFirstName("Admin");
            admin.setLastName("Cenadif");
            admin.setPassword("Admin");
            admin.setActive(true);
            admin.setRole(adminRole);

            if (userRepository.findByUsername("Admin").isEmpty()) {
                userRepository.save(admin);
                alreadySetup = true;
            }
        }
    }

    private Role findRoleOrCreate(RoleType type) {
        Optional<Role> roleOpt = roleRepository.findByType(type);
        Role role;
        if (roleOpt.isEmpty()) {
            role = new Role();
            role.setType(type);
            roleRepository.save(role);
        } else {
            role = roleOpt.get();
        }
        return role;
    }
}
