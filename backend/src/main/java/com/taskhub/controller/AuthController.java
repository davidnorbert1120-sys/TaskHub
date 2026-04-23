package com.taskhub.controller;

import com.taskhub.dto.incoming.RegisterCommand;
import com.taskhub.dto.outgoing.UserItem;
import com.taskhub.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserItem> register(@RequestBody @Valid RegisterCommand command) {

        log.info("Registering user with username: {}", command.getUsername());
        UserItem result = userService.register(command);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
