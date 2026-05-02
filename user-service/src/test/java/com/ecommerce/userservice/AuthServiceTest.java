package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.LoginRequest;
import com.ecommerce.userservice.dto.RegisterRequest;
import com.ecommerce.userservice.dto.AuthResponse;
import com.ecommerce.userservice.entity.Role;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.exception.UserAlreadyExistsException;
import com.ecommerce.userservice.repository.RefreshTokenRepository;
import com.ecommerce.userservice.repository.RoleRepository;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsService userDetailsService;
    @Mock private UserEventPublisher eventPublisher;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private User mockUser;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        validRegisterRequest = RegisterRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .password("Test@1234")
                .firstName("John")
                .lastName("Doe")
                .build();

        customerRole = Role.builder()
                .id(1L)
                .name(Role.RoleName.ROLE_CUSTOMER)
                .build();

        mockUser = User.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .password("$2a$12$encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of(customerRole))
                .status(User.UserStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(roleRepository.findByName(Role.RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(mockUser);
            when(userDetailsService.loadUserByUsername(anyString()))
                    .thenReturn(org.springframework.security.core.userdetails.User.builder()
                            .username("john@example.com").password("pass").roles("CUSTOMER").build());
            when(jwtService.generateAccessToken(any())).thenReturn("mock.access.token");
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(eventPublisher).publishUserRegistered(any());

            // Act
            AuthResponse response = authService.register(validRegisterRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("mock.access.token");
            assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");
            verify(userRepository).save(any(User.class));
            verify(eventPublisher).publishUserRegistered(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserAlreadyExistsException when email is taken")
        void shouldThrowWhenEmailExists() {
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRegisterRequest))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("Email already registered");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UserAlreadyExistsException when username is taken")
        void shouldThrowWhenUsernameExists() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername("johndoe")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRegisterRequest))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("Username already taken");
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with email")
        void shouldLoginSuccessfully() {
            LoginRequest loginRequest = LoginRequest.builder()
                    .emailOrUsername("john@example.com")
                    .password("Test@1234")
                    .build();

            when(userRepository.findByEmailWithRoles("john@example.com"))
                    .thenReturn(Optional.of(mockUser));
            when(refreshTokenRepository.revokeAllUserTokens(any())).thenReturn(1);
            when(userDetailsService.loadUserByUsername("john@example.com"))
                    .thenReturn(org.springframework.security.core.userdetails.User.builder()
                            .username("john@example.com").password("pass").roles("CUSTOMER").build());
            when(jwtService.generateAccessToken(any())).thenReturn("mock.access.token");
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AuthResponse response = authService.login(loginRequest);

            assertThat(response.getAccessToken()).isEqualTo("mock.access.token");
            assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");
        }
    }
}
