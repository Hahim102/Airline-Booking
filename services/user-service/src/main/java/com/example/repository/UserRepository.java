
package com.example.repository;

import com.example.model.Users;
import com.example.payload.response.UserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<Users, Long>, JpaSpecificationExecutor<Users> {


    Users findByEmailAndDeletedIsFalse(String email);
    List<Users> findAllByDeletedIsFalse();
    Optional<Users> findByIdAndDeletedIsFalse(Long id);
    Users findByEmail(String email);

    @Modifying
    @Query(value = "Update users set phone = ?3, fullName = ?4 where id = ?5", nativeQuery = true)
    UserResponse updateUser(String phone, String fullName, Long id);

}
