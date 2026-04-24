package com.example.service;


import com.example.model.Users;
import com.example.repository.UserRepository;
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
public class UserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users users = userRepository.findByEmail(email);

        if (users == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(users.getRole().toString());
        Collection<GrantedAuthority> grantedAuthorities = Collections.singleton(grantedAuthority);

        return new org.springframework.security.core.userdetails.User(
                users.getEmail(),
                users.getPassword(),
                grantedAuthorities
        );
    }
}
