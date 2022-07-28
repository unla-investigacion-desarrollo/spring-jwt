package com.project.auth.security.jwt;

import com.project.auth.constants.CommonsErrorConstants;
import com.project.auth.constants.CustomExceptionConstants;
import com.project.auth.constants.SecurityConfigConstants;
import com.project.auth.exceptions.InternalErrorException;
import com.project.auth.models.database.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.token.secretKey}")
    private String jwtSecret;

    @Value("${jwt.token.expiration}")
    private int jwtExpirationMs;

    /**
     * Method that is responsible for creating a jwt token
     *
     * @param user {@link Users}
     * @return jwt token {@link String}
     */
    public String generateJwtToken(Users user) {

        if (user.getRole() == null || user.getRole().getType().name().isEmpty()) {
            log.error(CustomExceptionConstants.ROLE_NOT_OWNED_GENERATE_TOKEN_ERROR_MESSAGE);
            throw new InternalErrorException(CommonsErrorConstants.INTERNAL_ERROR_MESSAGE);
        }

        Claims claims = Jwts.claims();
        claims.put("role", user.getRole().getType());
        claims.setSubject(user.getUsername());

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

        Key key = new SecretKeySpec(Base64.getDecoder().decode(getSecretKey()),
                signatureAlgorithm.getJcaName());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(
                        (new Date()).getTime() + jwtExpirationMs))
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    /**
     * Method in charge of obtaining the username from the entered token
     *
     * @param token {@link String}
     * @return username {@link String}
     * @throws ExpiredJwtException ExpiredJwtException is only caught because it will always be used
     *                             after having validated the method with ValidateJwtToken ()
     */
    public String getUserNameFromJwtToken(String token, HttpServletRequest request) {

        try {

            return Jwts.parserBuilder().setSigningKey(getSecretKey()).build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            request.setAttribute("expired", Boolean.TRUE);
            throw e;
        }
    }

    /**
     * Method in charge of corroborating the validity of the token
     *
     * @param authToken {@link String}
     * @param request   {@link HttpServletRequest}
     * @return {@link Boolean}
     */
    public boolean validateJwtToken(String authToken, HttpServletRequest request) {
        try {

            Jwts.parserBuilder().setSigningKey(getSecretKey()).build()
                    .parseClaimsJws(authToken);

            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            request.setAttribute("invalid", Boolean.TRUE);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            request.setAttribute("invalid", Boolean.TRUE);
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            request.setAttribute("expired", Boolean.TRUE);
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Method that is responsible for generating the encoded secret key of the jwt
     *
     * @return secret key
     */
    private String getSecretKey() {

        String secret = null;
        try {
            MessageDigest md =
                    MessageDigest.getInstance(SecurityConfigConstants.ENCODING_ALGORITHM);
            md.update(jwtSecret.getBytes(StandardCharsets.UTF_8));
            secret = Base64.getEncoder().encodeToString(md.digest());

        } catch (NoSuchAlgorithmException e) {
            log.error(CommonsErrorConstants.LOG_ERROR_MESSAGE, e.getMessage(), e);
        }
        return secret;
    }

}
