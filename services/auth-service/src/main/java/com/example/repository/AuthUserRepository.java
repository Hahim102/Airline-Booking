package com.example.repository;

import com.example.model.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByEmailAndDeletedIsFalseAndActiveIsTrue(String email);

    Optional<AuthUser> findByEmailAndDeletedIsFalse(String email);

    boolean existsByEmail(String email);

    AuthUser findByEmail(String email);
}
