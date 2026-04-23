package com.taskhub.service;

import com.taskhub.domain.User;
import com.taskhub.dto.incoming.RegisterCommand;
import com.taskhub.dto.outgoing.UserItem;
import com.taskhub.exception.EmailAlreadyExistsException;
import com.taskhub.exception.UsernameAlreadyExistsException;
import com.taskhub.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
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
}
