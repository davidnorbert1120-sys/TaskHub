package com.taskhub.dto.incoming;

import com.taskhub.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusCommand {

    @NotNull(message = "The status must be specified")
    private TaskStatus status;
}