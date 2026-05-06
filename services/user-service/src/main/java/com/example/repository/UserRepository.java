package com.example.repository;

import com.example.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Users findByEmailAndDeletedIsFalse(String email);
    List<Users> findAllByDeletedIsFalse();
    Optional<Users> findByIdAndDeletedIsFalse(Long id);
}
