package com.project.auth.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.project.auth.exceptions.RoleNotFoundException;
import com.project.auth.models.database.Role;
import com.project.auth.models.enums.RoleType;
import com.project.auth.repositories.RoleRepository;
import com.project.auth.services.impl.RoleService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoleServiceTest {

    private Role role1;

    private Role role2;

    private Role role3;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @BeforeEach
    void setup() {
        role1 = new Role();
        role1.setType(RoleType.ADMIN);
        ReflectionTestUtils.setField(role1, "id", 123L);

        role2 = new Role();
        role2.setType(RoleType.COLLABORATOR);
        ReflectionTestUtils.setField(role2, "id", 456L);

        role3 = new Role();
        role3.setType(RoleType.COORDINATOR);
        ReflectionTestUtils.setField(role3, "id", 124L);

    }

    @Test
    @DisplayName("When finding by id, returns the desired role ")
    @Order(1)
    void testFindByIdItReturnRole() {
        // given
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(role1));

        // when
        Role expectedRole = roleService.findRoleById(1L);

        // then
        assertEquals(expectedRole.getType(), role1.getType());

    }

    @Test
    @DisplayName("When finding by an invalid id, returns an empty Object")
    @Order(2)
    void testFindByInvalidIdReturnEmptyObject() {
        when(roleRepository.findById(anyLong())).thenReturn(Optional.empty());

        Assertions.assertThrows(RoleNotFoundException.class,
                () -> roleService.findRoleById(1L));
    }

    @Test
    @DisplayName("When finding all roles, returns a list with all roles dto")
    @Order(3)
    void testFindAllRoles() {
        // given
        when(roleRepository.findAll()).thenReturn(List.of(role1, role2, role3));

        // when
        List<Role> expectedRoles = roleService.findRoles();

        // then
        assertEquals(3, expectedRoles.size());
    }

    @Test
    @DisplayName("When finding by type, returns the desired role ")
    @Order(4)
    void testFindByTypeItReturnRole() {
        // given
        when(roleRepository.findByType(any())).thenReturn(Optional.of(role1));

        // when
        Role expectedRole = roleService.findRoleByType(RoleType.ADMIN);

        // then
        org.assertj.core.api.Assertions.assertThat(expectedRole.getType())
                .isEqualTo(role1.getType());

    }
}
