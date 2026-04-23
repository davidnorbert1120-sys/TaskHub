package com.taskhub.dto.incoming;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginCommand {

    @NotBlank(message = "The username cannot be empty")
    private String username;

    @NotBlank(message = "The password cannot be empty")
    @ToString.Exclude
    private String password;
}
