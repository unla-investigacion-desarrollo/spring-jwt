package com.project.auth.security.jwt;

import com.project.auth.exceptions.InternalErrorException;
import com.project.auth.models.database.Role;
import com.project.auth.models.database.Users;
import com.project.auth.models.enums.RoleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JwtUtilsTest {

    @SpyBean
    private JwtUtils jwtUtils;

    private Users user;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setType(RoleType.ADMIN);

        user = new Users();
        user.setUsername("admin");
        user.setEmail("admin1@gmail.com");
        user.setRole(role);

        request = new MockHttpServletRequest();
    }

    @Test
    @Order(1)
    void whenTheUserIsPresent_thenTheTokenIsCorrectlyGenerated() {
        request.setRequestURI("/api/users");

        // when
        String token = jwtUtils.generateJwtToken(user);

        // then
        Assertions.assertFalse(token.isEmpty());
        Assertions.assertTrue(jwtUtils.validateJwtToken(token, request));
    }

    @Test
    @Order(2)
    void whenTheUserDoesNotHaveRoles_thenAnErrorIsGeneratedWhenCreatingTheToken() {
        Users userWithOutRoles = new Users();

        Assertions.assertThrows(InternalErrorException.class,
                () -> {
                    jwtUtils.generateJwtToken(userWithOutRoles);
                });
    }

    @Test
    @Order(3)
    void whenTheTokenHasErrors_thenItIsNotValid() {

        request.setRequestURI("/api/users");

        String tokenWithExpirationTest = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6W3siaWQiOjEsImNyZWF0ZWR"
                + "CeSI6InVua25vd24iLCJjcmVhdGVkRGF0ZSI6MTYwOTAxMzE1ODA5NCwibGFzdE1vZGlmaWVkQnkiOi"
                + "J1bmtub3duIiwibGFzdE1vZGlmaWVkRGF0ZSI6MTYwOTAxMzE1ODA5NCwiZGVsZXRlZCI6bnVsbCwi"
                + "dHlwZSI6IkFETUlOIn1dLCJzdWIiOiJNYXJ0aW5Hb21lejIiLCJpYXQiOjE2MDkwMTMyNjgsImV4c"
                + "CI6MTYwOTAxMzMyOH0.iIRd39wa_xmOzHrHd4c9yFaMvsiY5mU9JW9nUwnTouSR8mwefsRt"
                + "JoHGX7jhkwScJG7pR3KqnPN8eu7TyRgzLw";

        Assertions.assertFalse(jwtUtils.validateJwtToken("", request));

        Assertions.assertFalse(jwtUtils.validateJwtToken("string", request));
        Assertions.assertTrue((Boolean) request.getAttribute("invalid"));

        Assertions.assertFalse(jwtUtils.validateJwtToken(tokenWithExpirationTest,
                request));
        Assertions.assertTrue((Boolean) request.getAttribute("expired"));
    }

    @Test
    @Order(4)
    void whenTheTokenIsValid_thenICanGetTheUsernameCorrectly() {

        Users administrator = new Users();
        administrator.setUsername("MartinGomez2");
        administrator.setEmail("MartinGomez2@gmail.com");

        request.setRequestURI("/api/users");

        String tokenWithOutExpirationTest =
                "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6W3siaWQiOjEsImNyZWF0ZWRCeSI6InVua25vd24iLC"
                        + "JjcmVhdGVkRGF0ZSI6MTYwOTAxMjE3NzQ3MiwibGFzdE1vZGlmaWVkQnkiOiJ1bmtu"
                        + "b3duIiwibGFzdE1vZGlmaWVkRGF0ZSI6MTYwOTAxMjE3NzQ3MiwiZGVsZXRlZCI6bnVsb"
                        + "CwidHlwZSI6IkFETUlOIn1dLCJzdWIiOiJNYXJ0aW5Hb21lejIiLCJpYXQiOjE2MDkwMTI1"
                        + "MzR9.wixZ_LDB8-Qt9xhe5rlIo70bXXTX9kDi6VyrYQJIBrWX0GDv0yax3_"
                        + "bHn1kHeMzkOsZPuTyhcEj_qrzEPI-8LA";
        String username = jwtUtils.getUserNameFromJwtToken(tokenWithOutExpirationTest, request);
        Assertions.assertEquals(administrator.getUsername(), username);
    }


}
