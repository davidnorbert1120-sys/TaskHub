package com.taskhub.controller;

import com.taskhub.dto.outgoing.UserItem;
import com.taskhub.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserItem> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Fetching user profile for username: {}", userDetails.getUsername());

        UserItem result = userService.getByUsername(userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
