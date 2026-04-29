package com.taskhub.dto.incoming;

import com.taskhub.domain.TaskPriority;
import com.taskhub.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateCommand {

    @NotBlank(message = "The task title cannot be empty")
    @Size(max = 200, message = "The task title cannot be longer than 200 characters")
    private String title;

    @Size(max = 2000, message = "The description cannot be longer than 2000 characters")
    private String description;

    @NotNull(message = "The priority must be specified")
    private TaskPriority priority;

    private TaskStatus status;

    private LocalDate dueDate;

    private String assigneeUsername;
}