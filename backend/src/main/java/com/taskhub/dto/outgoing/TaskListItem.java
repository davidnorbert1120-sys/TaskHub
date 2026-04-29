package com.taskhub.dto.outgoing;

import com.taskhub.domain.TaskPriority;
import com.taskhub.domain.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskListItem {

    private Long id;

    private String title;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;

    private String assigneeUsername;
}