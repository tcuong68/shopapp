package com.devteria.identity_service.repository;

import com.devteria.identity_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    @Query("""
    SELECT u FROM User u
    LEFT JOIN u.roles r
    WHERE (:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')))
      AND (:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))
      AND (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
      AND (:location IS NULL OR LOWER(u.location) LIKE LOWER(CONCAT('%', :location, '%')))
      AND (:role IS NULL OR LOWER(r.name) = LOWER(:role))
      """)
    Page<User> searchUsers(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("username") String username,
            @Param("role") String role,
            @Param("location") String location,
            Pageable pageable
    );
}
