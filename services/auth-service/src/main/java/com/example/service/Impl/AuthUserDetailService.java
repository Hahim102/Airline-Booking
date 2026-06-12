package com.example.service.Impl;


import com.example.enums.ErrorCode;
import com.example.exception.AppException;
import com.example.model.AuthUser;
import com.example.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthUserDetailService implements UserDetailsService {
    private final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AuthUser users = authUserRepository.findByEmail(email);
        if (users == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(users.getRole().toString());
        Collection<GrantedAuthority> grantedAuthorities = Collections.singleton(grantedAuthority);

        return new org.springframework.security.core.userdetails.User(
                users.getEmail(),
                users.getPassword(),
                users.isActive() && !users.isDeleted(),
                true,
                true,
                true,
                grantedAuthorities
        );
    }
}
