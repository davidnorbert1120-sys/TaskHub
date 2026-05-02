package com.taskhub.service;

import com.taskhub.domain.MemberRole;
import com.taskhub.domain.Project;
import com.taskhub.domain.ProjectMember;
import com.taskhub.domain.User;
import com.taskhub.dto.incoming.ProjectCreateCommand;
import com.taskhub.dto.incoming.ProjectUpdateCommand;
import com.taskhub.dto.outgoing.ProjectItem;
import com.taskhub.dto.outgoing.ProjectListItem;
import com.taskhub.exception.ProjectAccessDeniedException;
import com.taskhub.exception.ProjectNotFoundException;
import com.taskhub.domain.Task;
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
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final ProjectMemberRepository projectMemberRepository;

    private final TaskRepository taskRepository;

    private final CommentRepository commentRepository;

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          TaskRepository taskRepository,
                          CommentRepository commentRepository,
                          UserRepository userRepository,
                          ModelMapper modelMapper) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public ProjectItem create(ProjectCreateCommand projectCreateCommand, String username) {
        User owner = findUserByUsername(username);

        Project project = new Project();
        project.setName(projectCreateCommand.getName());
        project.setDescription(projectCreateCommand.getDescription());
        Project savedProject = projectRepository.save(project);

        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProject(savedProject);
        ownerMember.setUser(owner);
        ownerMember.setRole(MemberRole.OWNER);
        projectMemberRepository.save(ownerMember);

        log.info("New project created with id {} by user {}", savedProject.getId(), username);

        return toProjectItem(savedProject);
    }

    public List<ProjectListItem> listMyProjects(String username) {
        User user = findUserByUsername(username);

        List<ProjectMember> memberships = projectMemberRepository.findAllByUser(user);
        return memberships.stream()
                .map(membership -> modelMapper.map(membership.getProject(), ProjectListItem.class))
                .toList();
    }

    public ProjectItem getById(Long id, String username) {
        Project project = findProjectAndRequireMember(id, username);
        return toProjectItem(project);
    }

    public ProjectItem update(Long id, ProjectUpdateCommand projectUpdateCommand, String username) {
        Project project = findProjectAndRequireOwner(id, username);

        project.setName(projectUpdateCommand.getName());
        project.setDescription(projectUpdateCommand.getDescription());

        log.info("Project {} updated by user {}", id, username);
        return toProjectItem(project);
    }

    public void delete(Long id, String username) {
        Project project = findProjectAndRequireOwner(id, username);

        List<Task> tasks = taskRepository.findAllByProject(project);
        for (Task task : tasks) {
            commentRepository.deleteAll(commentRepository.findAllByTaskOrderByCreatedAtAsc(task));
        }
        taskRepository.deleteAll(tasks);
        projectMemberRepository.deleteAll(projectMemberRepository.findAllByProject(project));
        projectRepository.delete(project);
        log.info("Project {} deleted by user {}", id, username);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username));
    }

    private Project findProjectAndRequireMember(Long id, String username) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        User user = findUserByUsername(username);
        if (projectMemberRepository.existsByProjectAndUser(project, user)) {
            return project;
        } else {
            throw new ProjectAccessDeniedException(id, username);
        }
    }

    private Project findProjectAndRequireOwner(Long id, String username) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        User user = findUserByUsername(username);
        ProjectMember membership = projectMemberRepository.findByProjectAndUser(project, user)
                .orElseThrow(() -> new ProjectAccessDeniedException(id, username));
        if (membership.getRole() == MemberRole.OWNER) {
            return project;
        } else {
            throw new ProjectAccessDeniedException(id, username);
        }
    }

    private ProjectItem toProjectItem(Project project) {
        ProjectItem item = modelMapper.map(project, ProjectItem.class);
        ProjectMember owner = projectMemberRepository.findByProjectAndRole(project, MemberRole.OWNER)
                .orElseThrow(() -> new IllegalStateException("Project " + project.getId() + " has no owner"));
        item.setOwnerUsername(owner.getUser().getUsername());
        return item;
    }
}