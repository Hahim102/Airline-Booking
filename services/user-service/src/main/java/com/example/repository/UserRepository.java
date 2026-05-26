
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


    Optional<Users> findByEmailAndDeletedIsFalse(String email);

    Optional<Users> findByIdAndDeletedIsFalseAndActiveIsTrue(Long userId);

    Optional<Users> findByEmailAndDeletedIsFalseAndActiveIsTrue(String email);

    boolean existsByEmail(String email);

    List<Users> findAllByDeletedIsFalse();
    Optional<Users> findByIdAndDeletedIsFalse(Long id);
    Users findByEmail(String email);

//    @Modifying
//    @Query(value = "Update users set phone = ?3, fullName = ?4 where id = ?5", nativeQuery = true)
//    UserResponse updateUser(String phone, String fullName, Long id);

    long count();
    long countByActiveIsTrueAndDeletedIsFalse();
    long countByActiveIsFalseAndDeletedIsFalse();
    long countByDeletedIsTrue();


    @Query(value = """
        SELECT TO_CHAR(u.created_at, 'YYYY-MM-DD') AS label,
               COUNT(*) AS total
        FROM users u
        GROUP BY TO_CHAR(u.created_at, 'YYYY-MM-DD')
        ORDER BY label
        """, nativeQuery = true)
    List<Object[]> countUsersByDay();


    @Query(value = """
        SELECT TO_CHAR(u.created_at, 'IYYY-IW') AS label,
               COUNT(*) AS total
        FROM users u
        GROUP BY TO_CHAR(u.created_at, 'IYYY-IW')
        ORDER BY label
        """, nativeQuery = true)
    List<Object[]> countUsersByWeek();


    @Query(value = """
        SELECT TO_CHAR(u.created_at, 'YYYY-MM') AS label,
               COUNT(*) AS total
        FROM users u
        GROUP BY TO_CHAR(u.created_at, 'YYYY-MM')
        ORDER BY label
        """, nativeQuery = true)
    List<Object[]> countUsersByMonth();

}
