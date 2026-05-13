package com.example.repository.specification;

import com.example.enums.UserRole;
import com.example.model.Users;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<Users> buildUserSpecification(
            String fullName,
            String email,
            String phone,
            UserRole role,
            Boolean isActive) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            if (fullName != null && !fullName.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullName")),
                        "%" + fullName.toLowerCase() + "%"
                ));
            }

            if (email != null && !email.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%"
                ));
            }

            if (phone != null && !phone.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("phone"), phone));
            }

            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }

            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), isActive));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

