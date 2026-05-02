package com.taskhub.service;

import com.taskhub.domain.MemberRole;
import com.taskhub.domain.Project;
import com.taskhub.domain.ProjectMember;
import com.taskhub.domain.Task;
import com.taskhub.domain.TaskStatus;
import com.taskhub.domain.User;
import com.taskhub.dto.incoming.TaskCreateCommand;
import com.taskhub.dto.incoming.TaskUpdateCommand;
import com.taskhub.dto.incoming.UpdateTaskStatusCommand;
import com.taskhub.dto.outgoing.TaskItem;
import com.taskhub.dto.outgoing.TaskListItem;
import com.taskhub.exception.InvalidAssigneeException;
import com.taskhub.exception.ProjectAccessDeniedException;
import com.taskhub.exception.ProjectNotFoundException;
import com.taskhub.exception.TaskNotFoundException;
import com.taskhub.repository.CommentRepository;
import com.taskhub.repository.ProjectMemberRepository;
import com.taskhub.repository.ProjectRepository;
import com.taskhub.repository.TaskRepository;
import com.taskhub.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    private final ProjectRepository projectRepository;

    private final ProjectMemberRepository projectMemberRepository;

    private final UserRepository userRepository;

    private final CommentRepository commentRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       ProjectMemberRepository projectMemberRepository,
                       UserRepository userRepository,
                       CommentRepository commentRepository,
                       ModelMapper modelMapper) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.modelMapper = modelMapper;
    }

    public TaskItem create(Long projectId, TaskCreateCommand command, String callerUsername) {
        Project project = findProjectAndRequireMember(projectId, callerUsername);

        Task task = new Task();
        task.setTitle(command.getTitle());
        task.setDescription(command.getDescription());
        task.setPriority(command.getPriority());
        if (command.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        } else {
            task.setStatus(command.getStatus());
        }
        task.setDueDate(command.getDueDate());
        task.setProject(project);
        task.setAssignee(resolveAssignee(command.getAssigneeUsername(), project));

        Task saved = taskRepository.save(task);
        log.info("New task created with id {} in project {} by user {}", saved.getId(), projectId, callerUsername);

        return toTaskItem(saved);
    }

    public List<TaskListItem> listTasks(Long projectId, String callerUsername) {
        Project project = findProjectAndRequireMember(projectId, callerUsername);

        List<Task> tasks = taskRepository.findAllByProject(project);
        return tasks.stream()
                .map(this::toTaskListItem)
                .toList();
    }

    public TaskItem getById(Long projectId, Long taskId, String callerUsername) {
        findProjectAndRequireMember(projectId, callerUsername);
        Task task = findTaskInProject(projectId, taskId);
        return toTaskItem(task);
    }

    public TaskItem update(Long projectId, Long taskId, TaskUpdateCommand command, String callerUsername) {
        Project project = findProjectAndRequireMember(projectId, callerUsername);
        Task task = findTaskInProject(projectId, taskId);

        task.setTitle(command.getTitle());
        task.setDescription(command.getDescription());
        task.setPriority(command.getPriority());
        task.setStatus(command.getStatus());
        task.setDueDate(command.getDueDate());
        task.setAssignee(resolveAssignee(command.getAssigneeUsername(), project));

        log.info("Task {} updated in project {} by user {}", taskId, projectId, callerUsername);
        return toTaskItem(task);
    }

    public TaskItem updateStatus(Long projectId, Long taskId, UpdateTaskStatusCommand command, String callerUsername) {
        findProjectAndRequireMember(projectId, callerUsername);
        Task task = findTaskInProject(projectId, taskId);

        task.setStatus(command.getStatus());

        log.info("Task {} status changed to {} in project {} by user {}",
                taskId, command.getStatus(), projectId, callerUsername);
        return toTaskItem(task);
    }

    public void delete(Long projectId, Long taskId, String callerUsername) {
        findProjectAndRequireMember(projectId, callerUsername);
        Task task = findTaskInProject(projectId, taskId);

        commentRepository.deleteAll(commentRepository.findAllByTaskOrderByCreatedAtAsc(task));
        taskRepository.delete(task);
        log.info("Task {} deleted from project {} by user {}", taskId, projectId, callerUsername);
    }

    private Project findProjectAndRequireMember(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        User user = findUserByUsername(username);
        if (projectMemberRepository.existsByProjectAndUser(project, user)) {
            return project;
        } else {
            throw new ProjectAccessDeniedException(projectId, username);
        }
    }

    private Task findTaskInProject(Long projectId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        if (task.getProject().getId().equals(projectId)) {
            return task;
        } else {
            throw new TaskNotFoundException(taskId);
        }
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username));
    }

    private User resolveAssignee(String assigneeUsername, Project project) {
        if (assigneeUsername == null || assigneeUsername.isBlank()) {
            return null;
        } else {
            User assignee = userRepository.findByUsername(assigneeUsername)
                    .orElseThrow(() -> new InvalidAssigneeException(assigneeUsername, project.getId()));
            ProjectMember membership = projectMemberRepository.findByProjectAndUser(project, assignee)
                    .orElseThrow(() -> new InvalidAssigneeException(assigneeUsername, project.getId()));
            if (membership.getRole() == MemberRole.OWNER || membership.getRole() == MemberRole.MEMBER) {
                return assignee;
            } else {
                throw new InvalidAssigneeException(assigneeUsername, project.getId());
            }
        }
    }

    private TaskItem toTaskItem(Task task) {
        TaskItem item = modelMapper.map(task, TaskItem.class);
        if (task.getAssignee() == null) {
            item.setAssigneeUsername(null);
        } else {
            item.setAssigneeUsername(task.getAssignee().getUsername());
        }
        return item;
    }

    private TaskListItem toTaskListItem(Task task) {
        TaskListItem item = modelMapper.map(task, TaskListItem.class);
        if (task.getAssignee() == null) {
            item.setAssigneeUsername(null);
        } else {
            item.setAssigneeUsername(task.getAssignee().getUsername());
        }
        return item;
    }
}