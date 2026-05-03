package com.taskhub.service;

import com.taskhub.domain.User;
import com.taskhub.dto.incoming.LoginCommand;
import com.taskhub.dto.incoming.RegisterCommand;
import com.taskhub.dto.outgoing.AuthResponse;
import com.taskhub.dto.outgoing.UserItem;
import com.taskhub.exception.EmailAlreadyExistsException;
import com.taskhub.exception.InvalidCredentialsException;
import com.taskhub.exception.UsernameAlreadyExistsException;
import com.taskhub.repository.UserRepository;
import com.taskhub.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_register_successful() {
        RegisterCommand command = new RegisterCommand();
        command.setUsername("david");
        command.setEmail("david@example.com");
        command.setPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("david");
        savedUser.setEmail("david@example.com");
        savedUser.setPasswordHash("hashed_password");

        UserItem expected = new UserItem();
        expected.setId(1L);
        expected.setUsername("david");
        expected.setEmail("david@example.com");

        when(userRepository.existsByUsername("david")).thenReturn(false);
        when(userRepository.existsByEmail("david@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(modelMapper.map(savedUser, UserItem.class)).thenReturn(expected);

        UserItem actual = userService.register(command);

        assertEquals(expected, actual);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void test_register_throws_when_username_exists() {
        RegisterCommand command = new RegisterCommand();
        command.setUsername("david");
        command.setEmail("david@example.com");
        command.setPassword("password123");

        when(userRepository.existsByUsername("david")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> userService.register(command));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void test_register_throws_when_email_exists() {
        RegisterCommand command = new RegisterCommand();
        command.setUsername("david");
        command.setEmail("david@example.com");
        command.setPassword("password123");

        when(userRepository.existsByUsername("david")).thenReturn(false);
        when(userRepository.existsByEmail("david@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.register(command));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void test_login_successful() {
        LoginCommand command = new LoginCommand();
        command.setUsername("david");
        command.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("david");
        user.setEmail("david@example.com");

        UserItem userItem = new UserItem();
        userItem.setId(1L);
        userItem.setUsername("david");
        userItem.setEmail("david@example.com");

        when(userRepository.findByUsername("david")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("david")).thenReturn("jwt_token_xyz");
        when(modelMapper.map(user, UserItem.class)).thenReturn(userItem);

        AuthResponse actual = userService.login(command);

        assertEquals("jwt_token_xyz", actual.getToken());
        assertEquals(userItem, actual.getUser());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void test_login_throws_when_credentials_invalid() {
        LoginCommand command = new LoginCommand();
        command.setUsername("david");
        command.setPassword("wrong_password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidCredentialsException.class, () -> userService.login(command));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void test_getByUsername_successful() {
        User user = new User();
        user.setId(1L);
        user.setUsername("david");
        user.setEmail("david@example.com");

        UserItem expected = new UserItem();
        expected.setId(1L);
        expected.setUsername("david");
        expected.setEmail("david@example.com");

        when(userRepository.findByUsername("david")).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserItem.class)).thenReturn(expected);

        UserItem actual = userService.getByUsername("david");

        assertEquals(expected, actual);
    }

    @Test
    void test_getByUsername_throws_when_user_not_found() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> userService.getByUsername("nonexistent"));
    }
}