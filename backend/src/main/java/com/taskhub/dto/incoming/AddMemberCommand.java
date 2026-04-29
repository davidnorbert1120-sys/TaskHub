package com.taskhub.dto.incoming;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberCommand {

    @NotBlank(message = "The username cannot be empty")
    private String username;
}
