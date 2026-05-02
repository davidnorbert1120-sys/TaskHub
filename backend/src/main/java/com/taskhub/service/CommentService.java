package com.taskhub.service;

import com.taskhub.domain.Comment;
import com.taskhub.domain.MemberRole;
import com.taskhub.domain.Project;
import com.taskhub.domain.Task;
import com.taskhub.domain.User;
import com.taskhub.dto.incoming.CommentCreateCommand;
import com.taskhub.dto.outgoing.CommentItem;
import com.taskhub.exception.CommentDeleteForbiddenException;
import com.taskhub.exception.CommentNotFoundException;
import com.taskhub.exception.ProjectAccessDeniedException;
import com.taskhub.exception.ProjectNotFoundException;
import com.taskhub.exception.TaskNotFoundException;
import com.taskhub.repository.CommentRepository;
import com.taskhub.repository.ProjectMemberRepository;
import com.taskhub.repository.ProjectRepository;
import com.taskhub.repository.TaskRepository;
import com.taskhub.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;

    private final TaskRepository taskRepository;

    private final ProjectRepository projectRepository;

    private final ProjectMemberRepository projectMemberRepository;

    private final UserRepository userRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          TaskRepository taskRepository,
                          ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    public CommentItem addComment(Long projectId, Long taskId,
                                  CommentCreateCommand command, String callerUsername) {
        findProjectAndRequireMember(projectId, callerUsername);
        Task task = findTaskInProject(projectId, taskId);
        User author = findUserByUsername(callerUsername);

        Comment comment = new Comment();
        comment.setContent(command.getContent());
        comment.setTask(task);
        comment.setAuthor(author);
        Comment saved = commentRepository.save(comment);

        log.info("New comment {} created on task {} in project {} by {}",
                saved.getId(), taskId, projectId, callerUsername);
        return toCommentItem(saved);
    }

    public List<CommentItem> listComments(Long projectId, Long taskId, String callerUsername) {
        findProjectAndRequireMember(projectId, callerUsername);
        Task task = findTaskInProject(projectId, taskId);

        List<Comment> comments = commentRepository.findAllByTaskOrderByCreatedAtAsc(task);
        return comments.stream()
                .map(this::toCommentItem)
                .toList();
    }

    public void deleteComment(Long projectId, Long taskId, Long commentId, String callerUsername) {
        Project project = findProjectAndRequireMember(projectId, callerUsername);
        findTaskInProject(projectId, taskId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getTask().getId().equals(taskId)) {
            throw new CommentNotFoundException(commentId);
        }

        boolean isAuthor = comment.getAuthor().getUsername().equals(callerUsername);
        boolean isProjectOwner = isProjectOwner(project, callerUsername);
        if (isAuthor || isProjectOwner) {
            commentRepository.delete(comment);
            log.info("Comment {} deleted from task {} in project {} by {}",
                    commentId, taskId, projectId, callerUsername);
        } else {
            throw new CommentDeleteForbiddenException(commentId, callerUsername);
        }
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
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: "
                        + username));
    }

    private boolean isProjectOwner(Project project, String username) {
        User user = findUserByUsername(username);
        return projectMemberRepository.findByProjectAndUser(project, user)
                .map(membership -> membership.getRole() == MemberRole.OWNER)
                .orElse(false);
    }

    private CommentItem toCommentItem(Comment comment) {
        return new CommentItem(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getUsername(),
                comment.getCreatedAt()
        );
    }
}