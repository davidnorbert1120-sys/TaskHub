package com.taskhub.dto.incoming;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateCommand {

    @NotBlank(message = "The project name cannot be empty")
    @Size(max = 100, message = "The project name cannot be longer than 100 characters")
    private String name;

    @Size(max = 1000, message = "The project description cannot be longer than 1000 characters")
    private String description;
}
