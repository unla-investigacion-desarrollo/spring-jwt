package com.project.auth.repositories;

import com.project.auth.models.database.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsername(String userName);

    Optional<Users> findFirstByEmailIgnoreCase(String email);

    @Query("SELECT u FROM Users u"
            + " JOIN u.role r"
            + " WHERE ((:query is NULL or lower(u.firstName) LIKE lower(concat('%',:query,'%'))) "
            + " OR (:query is NULL or lower(u.lastName) LIKE lower(concat('%',:query,'%'))) "
            + " OR (:query is NULL or lower(u.username) LIKE lower(concat('%',:query,'%'))) "
            + " OR (:query is NULL or lower(r.type) LIKE lower(concat('%',:query,'%'))) "
            + " OR (:query is NULL or CAST(u.id as string) LIKE concat('%',:query,'%')) "
            + "  OR (:query is NULL or to_char(u.lastSessionDate,'dd/mm/yyyy HH24:MI') LIKE "
            + "concat ('%',:query,'%')) "
            + " OR (:query is NULL or lower(u.email) LIKE lower(concat('%',:query,'%'))))  "
            + "AND (:active is NULL or u.active = :active)")
    Page<Users> findByFilter(@Param("query") String query, @Param("active") Boolean active,
                             Pageable pageable);
}
