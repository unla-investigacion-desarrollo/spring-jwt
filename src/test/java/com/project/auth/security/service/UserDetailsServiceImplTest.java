package com.project.auth.security.service;


import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.project.auth.models.database.Role;
import com.project.auth.models.database.Users;
import com.project.auth.models.enums.RoleType;
import com.project.auth.repositories.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDetailsServiceImplTest {

    private Users user;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setType(RoleType.ADMIN);

        user = new Users();
        user.setUsername("admin");
        user.setEmail("admin1@gmail.com");
        user.setRole(role);
    }

    @Test
    @Order(1)
    void whenTheUserExist_thenTheUserDetailObjectIsCorrectlyGenerated() {

        // given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        // then
        Assertions.assertEquals(user.getUsername(), userDetails.getUsername());
        Assertions.assertEquals(1, userDetails.getAuthorities().size());
    }

    @Test
    @Order(2)
    void whenTheUserDoesNotExist_thenThrowUsernameNotFoundException() {

        //given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        //when
        UsernameNotFoundException usernameNotFoundException =
                Assertions.assertThrows(UsernameNotFoundException.class, () -> {
                    userDetailsService.loadUserByUsername("user");
                });
        //then
        Assertions.assertEquals("User Not Found with username: user",
                usernameNotFoundException.getMessage());
    }
}
