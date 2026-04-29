package com.taskhub.controller;

import com.taskhub.dto.incoming.TaskCreateCommand;
import com.taskhub.dto.incoming.TaskUpdateCommand;
import com.taskhub.dto.incoming.UpdateTaskStatusCommand;
import com.taskhub.dto.outgoing.TaskItem;
import com.taskhub.dto.outgoing.TaskListItem;
import com.taskhub.service.TaskService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/tasks")
@Slf4j
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskItem> create(@PathVariable Long projectId,
                                           @Valid @RequestBody TaskCreateCommand taskCreateCommand,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creating task in project {} by user: {}", projectId, userDetails.getUsername());

        TaskItem result = taskService.create(projectId, taskCreateCommand, userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TaskListItem>> listTasks(@PathVariable Long projectId,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Listing tasks of project {} for user: {}", projectId, userDetails.getUsername());

        List<TaskListItem> result = taskService.listTasks(projectId, userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskItem> getById(@PathVariable Long projectId,
                                            @PathVariable Long taskId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Fetching task {} in project {} for user: {}", taskId, projectId, userDetails.getUsername());

        TaskItem result = taskService.getById(projectId, taskId, userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskItem> update(@PathVariable Long projectId,
                                           @PathVariable Long taskId,
                                           @Valid @RequestBody TaskUpdateCommand taskUpdateCommand,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Updating task {} in project {} by user: {}", taskId, projectId, userDetails.getUsername());

        TaskItem result = taskService.update(projectId, taskId, taskUpdateCommand, userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskItem> updateStatus(@PathVariable Long projectId,
                                                 @PathVariable Long taskId,
                                                 @Valid @RequestBody UpdateTaskStatusCommand updateTaskStatusCommand,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Updating status of task {} in project {} by user: {}", taskId, projectId, userDetails.getUsername());

        TaskItem result = taskService.updateStatus(projectId, taskId, updateTaskStatusCommand, userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(@PathVariable Long projectId,
                                       @PathVariable Long taskId,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Deleting task {} from project {} by user: {}", taskId, projectId, userDetails.getUsername());

        taskService.delete(projectId, taskId, userDetails.getUsername());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}