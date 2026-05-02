package com.ecommerce.userservice.security;

import com.ecommerce.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        var user = userRepository.findByEmailWithRoles(emailOrUsername)
                .or(() -> userRepository.findByUsernameWithRoles(emailOrUsername))
                .orElseThrow(() -> {
                    log.warn("User not found: {}", emailOrUsername);
                    return new UsernameNotFoundException("User not found: " + emailOrUsername);
                });

        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.getStatus() == com.ecommerce.userservice.entity.User.UserStatus.SUSPENDED)
                .credentialsExpired(false)
                .disabled(user.getStatus() == com.ecommerce.userservice.entity.User.UserStatus.INACTIVE ||
                          user.getStatus() == com.ecommerce.userservice.entity.User.UserStatus.DELETED)
                .build();
    }
}
