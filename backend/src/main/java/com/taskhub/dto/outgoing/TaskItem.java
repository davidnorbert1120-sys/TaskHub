package com.taskhub.dto.outgoing;

import com.taskhub.domain.TaskPriority;
import com.taskhub.domain.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskItem {

    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;

    private String assigneeUsername;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}