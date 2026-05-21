package com.example.service.impl;

import com.example.enums.ErrorCode;
import com.example.enums.StatisticType;
import com.example.enums.UserRole;
import com.example.exception.AppException;
import com.example.model.Users;
import com.example.payload.dto.CreateUserByAdminDTO;
import com.example.payload.dto.UpdateUserProfileDTO;
import com.example.payload.dto.UserSearchFilterDTO;
import com.example.payload.response.CreateUserResponse;
import com.example.payload.response.UserRegistrationStatsResponse;
import com.example.payload.response.UserResponse;
import com.example.payload.response.UserSummaryResponse;
import com.example.repository.UserRepository;
import com.example.repository.specification.UserSpecification;
import com.example.service.UserService;
import com.example.util.ModelMapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private static final String DEFAULT_PASSWORD = "Admin@123";

    @Override
    public UserResponse getUserByEmail(String email) {
        Users users = userRepository.findByEmail(email);
        if (users == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        return ModelMapperUtil.mapper(users, UserResponse.class);
    }

    @Override
    public UserResponse getUserById(Long id) {
        Users users = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return ModelMapperUtil.mapper(users, UserResponse.class);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<Users> users = userRepository.findAll();
        return ModelMapperUtil.mapList(users, UserResponse.class);
    }

    @Override
    public void updateIsActiveStatus(Long userId, boolean isActive) {
        Users user = userRepository.findByIdAndDeletedIsFalse(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == UserRole.ROLE_SYSTEM_ADMIN) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        user.setActive(isActive);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        Users user = userRepository.findByIdAndDeletedIsFalse(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == UserRole.ROLE_SYSTEM_ADMIN) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        user.setDeleted(true);
        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public CreateUserResponse createUser(CreateUserByAdminDTO request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_USED);
        }

        Users newUser = Users.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .phone(request.getPhone())
                .role(request.getRole())
                .active(true)
                .deleted(false)
                .fullName(request.getFullName())
                .createdAt(LocalDateTime.now())
                .build();
        Users savedUser = userRepository.save(newUser);

        CreateUserResponse response = ModelMapperUtil.mapper(savedUser, CreateUserResponse.class);

        response.setPassword(DEFAULT_PASSWORD);
        return response;
    }

    @Transactional
    @Override
    public UserResponse updateUserProfile(Long userId, UpdateUserProfileDTO updateRequest) {
        Users user = userRepository.findByIdAndDeletedIsFalse(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == UserRole.ROLE_SYSTEM_ADMIN) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        if (updateRequest.getFullName() != null && !updateRequest.getFullName().isBlank()) {
            user.setFullName(updateRequest.getFullName());
        }

        if (updateRequest.getPhone() != null && !updateRequest.getPhone().isBlank()) {
            user.setPhone(updateRequest.getPhone());
        }

        if (updateRequest.getRole() != null) {
            user.setRole(updateRequest.getRole());
        }

        user.setUpdatedAt(LocalDateTime.now());

        Users users = userRepository.save(user);


        return ModelMapperUtil.mapper(users, UserResponse.class);
    }

    @Override
    public Page<UserResponse> searchAndFilterUsers(UserSearchFilterDTO searchFilter) {
        Specification<Users> specification = UserSpecification.buildUserSpecification(
                searchFilter.getFullName(),
                searchFilter.getEmail(),
                searchFilter.getPhone(),
                searchFilter.getRole(),
                searchFilter.getIsActive()
        );

        String sortBy = searchFilter.getSortBy() != null ? searchFilter.getSortBy() : "id";
        String sortOrder = searchFilter.getSortOrder() != null ? searchFilter.getSortOrder() : "ASC";
        Sort.Direction direction = Sort.Direction.fromString(sortOrder);
        Sort sort = Sort.by(direction, sortBy);

        int pageNumber = Math.max(0, searchFilter.getPageNumber());
        int pageSize = searchFilter.getPageSize() > 0 ? searchFilter.getPageSize() : 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Users> usersPage = userRepository.findAll(specification, pageable);

        return usersPage.map(user -> ModelMapperUtil.mapper(user, UserResponse.class));
    }

    @Override
    public List<Map<String, String>> getUsersContactInfo() {
        List<Users> users = userRepository.findAllByDeletedIsFalse();
        return users.stream()
                .map(user -> {
                    Map<String, String> contactInfo = new LinkedHashMap<>();
                    contactInfo.put("fullName", user.getFullName());
                    contactInfo.put("email", user.getEmail());
                    contactInfo.put("phone", user.getPhone());
                    return contactInfo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserSummaryResponse getUserSummary() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveIsTrueAndDeletedIsFalse();
        long inactiveUsers = userRepository.countByActiveIsFalseAndDeletedIsFalse();
        long deletedUsers = userRepository.countByDeletedIsTrue();

        return UserSummaryResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .deletedUsers(deletedUsers)
                .build();
    }

    @Override
    public List<UserRegistrationStatsResponse> getUserRegistrationStats(StatisticType type) {
        List<Object[]> rows = switch (type) {
            case DAY -> userRepository.countUsersByDay();
            case WEEK -> userRepository.countUsersByWeek();
            case MONTH -> userRepository.countUsersByMonth();
        };

        return rows.stream()
                .map(row -> UserRegistrationStatsResponse.builder()
                        .label(String.valueOf(row[0]))
                        .total(((Number) row[1]).longValue())
                        .build())
                .toList();
    }


}
