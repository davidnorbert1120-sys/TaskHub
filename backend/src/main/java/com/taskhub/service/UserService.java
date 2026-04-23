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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.core.AuthenticationException;

@Service
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    @Autowired
    public UserService(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public UserItem register(RegisterCommand command) {
        if (userRepository.existsByUsername(command.getUsername())){
            throw new UsernameAlreadyExistsException(command.getUsername());
        }
        if (userRepository.existsByEmail(command.getEmail())){
            throw new EmailAlreadyExistsException(command.getEmail());
        }

        User user = new User();
        user.setUsername(command.getUsername());
        user.setEmail(command.getEmail());
        user.setPasswordHash(passwordEncoder.encode(command.getPassword()));

        User saved = userRepository.save(user);
        log.info("New user registered with username : {} ", saved.getUsername());

        return modelMapper.map(saved, UserItem.class);

    }

    public AuthResponse login(LoginCommand command) {
        log.info("Login attempt for username: {}", command.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(command.getUsername(), command.getPassword())
            );
        } catch (AuthenticationException ex) {
            log.warn("Failed login attempt for username: {}", command.getUsername());
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByUsername(command.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        String token = jwtService.generateToken(user.getUsername());
        UserItem userItem = modelMapper.map(user, UserItem.class);

        log.info("User logged in successfully: {}", user.getUsername());
        return new AuthResponse(token, userItem);
    }
}
