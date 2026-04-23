package com.taskhub.dto.incoming;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCommand {

    @NotBlank(message = "The username cannot be empty")
    @Size(min = 3, max = 50, message = "The username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "The email cannot be empty")
    @Email(message = "The email must be valid")
    @Size(max = 100, message = "The email cannot be longer than 100 characters")
    private String email;

    @NotBlank(message = "The password cannot be empty")
    @Size(min = 8, max = 100, message = "The password must be between 8 and 100 characters")
    @ToString.Exclude
    private String password;
}
