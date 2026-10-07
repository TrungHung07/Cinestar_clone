package com.cinestar.repository;

import com.cinestar.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /**
     * EntityGraph: fetch luon roles trong cung 1 query, tranh N+1 khi
     * Spring Security doc roles cua user
     */
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesById(Long id);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
}
