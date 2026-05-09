package com.example.repository;

import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmailAndDeletedIsFalse(String email);
    List<User> findAllByDeletedIsFalse();
    Optional<User> findByIdAndDeletedIsFalse(Long id);
}
