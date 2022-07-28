package com.project.auth.models.database;

import com.project.auth.constants.CommonsErrorConstants;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE users SET deleted = current_timestamp at time zone "
        + "'America/Argentina/Buenos_Aires' WHERE id = ?")
@Where(clause = "deleted IS NULL")
public class Users extends BaseEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(columnDefinition = "boolean default true")
    private boolean active = true;

    @NotBlank(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @Column(nullable = false, unique = true)
    @Size(max = 250, message = CommonsErrorConstants.MAX_SIZE_ERROR_MESSAGE)
    private String username;

    @Setter(AccessLevel.NONE)
    @NotBlank(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @Column(nullable = false)
    private String password;

    @NotBlank(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @Email(message = CommonsErrorConstants.INCORRECT_MAIL_ERROR_MESSAGE)
    @Column(nullable = false, unique = true)
    @Size(max = 250, message = CommonsErrorConstants.MAX_SIZE_ERROR_MESSAGE)
    private String email;

    @NotBlank(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @Column(nullable = false)
    @Size(max = 250, message = CommonsErrorConstants.MAX_SIZE_ERROR_MESSAGE)
    private String firstName;

    @NotBlank(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @Column(nullable = false)
    @Size(max = 250, message = CommonsErrorConstants.MAX_SIZE_ERROR_MESSAGE)
    private String lastName;

    @Column
    private LocalDateTime lastSessionDate;

    @Column
    private int failedAttempts;

    @NotNull(message = CommonsErrorConstants.REQUIRED_PARAM_ERROR_MESSAGE)
    @ManyToOne(optional = false, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private Role role;

    public void setPassword(String password) {
        this.password = encryptPassword(password);
    }

    public String encryptPassword(String pass) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(pass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(
                o)) {
            return false;
        }
        Users users = (Users) o;
        return Objects.equals(id, users.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
