package com.taskhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhub.dto.incoming.LoginCommand;
import com.taskhub.dto.incoming.RegisterCommand;
import com.taskhub.dto.outgoing.AuthResponse;
import com.taskhub.dto.outgoing.UserItem;
import com.taskhub.exception.EmailAlreadyExistsException;
import com.taskhub.exception.InvalidCredentialsException;
import com.taskhub.exception.UsernameAlreadyExistsException;
import com.taskhub.security.JwtAuthenticationFilter;
import com.taskhub.security.JwtService;
import com.taskhub.security.UserDetailsServiceImpl;
import com.taskhub.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.taskhub.exception.GlobalExceptionHandler.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void test_register_returns_201_with_userItem() throws Exception {
        RegisterCommand command = new RegisterCommand();
        command.setUsername("david");
        command.setEmail("david@example.com");
        command.setPassword("password123");

        UserItem expectedUser = new UserItem();
        expectedUser.setId(1L);
        expectedUser.setUsername("david");
        expectedUser.setEmail("david@example.com");

        when(userService.register(any(RegisterCommand.class))).thenReturn(expectedUser);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("david"))
                .andExpect(jsonPath("$.email").value("david@example.com"));
    }

    @Test
    void test_register_returns_400_when_username_blank() throws Exception {
        RegisterCommand command = new RegisterCommand();
        command.setUsername("");
        command.setEmail("david@example.com");
        command.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_register_returns_409_when_username_exists() throws Exception {
        RegisterCommand command = new RegisterCommand();
        command.setUsername("david");
        command.setEmail("david@example.com");
        command.setPassword("password123");

        when(userService.register(any(RegisterCommand.class)))
                .thenThrow(new UsernameAlreadyExistsException("david"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USERNAME_ALREADY_EXISTS"));
    }

    @Test
    void test_register_returns_409_when_email_exists() throws Exception {
        RegisterCommand command = new RegisterCommand();
        command.setUsername("david");
        command.setEmail("david@example.com");
        command.setPassword("password123");

        when(userService.register(any(RegisterCommand.class)))
                .thenThrow(new EmailAlreadyExistsException("david@example.com"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void test_login_returns_200_with_token() throws Exception {
        LoginCommand command = new LoginCommand();
        command.setUsername("david");
        command.setPassword("password123");

        UserItem userItem = new UserItem();
        userItem.setId(1L);
        userItem.setUsername("david");
        userItem.setEmail("david@example.com");

        AuthResponse response = new AuthResponse("jwt_token_xyz", userItem);

        when(userService.login(any(LoginCommand.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_token_xyz"))
                .andExpect(jsonPath("$.user.username").value("david"));
    }

    @Test
    void test_login_returns_401_when_invalid_credentials() throws Exception {
        LoginCommand command = new LoginCommand();
        command.setUsername("david");
        command.setPassword("wrong");

        when(userService.login(any(LoginCommand.class)))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }
}
