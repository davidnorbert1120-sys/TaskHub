package com.taskhub.service;

import com.taskhub.domain.Project;
import com.taskhub.domain.User;
import com.taskhub.dto.incoming.ProjectCreateCommand;
import com.taskhub.dto.incoming.ProjectUpdateCommand;
import com.taskhub.dto.outgoing.ProjectItem;
import com.taskhub.dto.outgoing.ProjectListItem;
import com.taskhub.exception.ProjectAccessDeniedException;
import com.taskhub.exception.ProjectNotFoundException;
import com.taskhub.repository.ProjectRepository;
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

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public ProjectItem create(ProjectCreateCommand projectCreateCommand, String username) {
        User owner = findUserByUsername(username);

        Project project = new Project();
        project.setName(projectCreateCommand.getName());
        project.setDescription(projectCreateCommand.getDescription());
        project.setOwner(owner);

        Project saved = projectRepository.save(project);
        log.info("New project created with id {} by user {}", saved.getId(), username);

        return toProjectItem(saved);
    }

    public List<ProjectListItem> listMyProjects(String username) {
        User owner = findUserByUsername(username);

        List<Project> projects = projectRepository.findAllByOwner(owner);
        return projects.stream()
                .map(project -> modelMapper.map(project, ProjectListItem.class))
                .toList();
    }

    public ProjectItem getById(Long id, String username) {
        Project project = findProjectAndCheckAccess(id, username);
        return toProjectItem(project);
    }

    public ProjectItem update(Long id, ProjectUpdateCommand projectUpdateCommand, String username) {
        Project project = findProjectAndCheckAccess(id, username);

        project.setName(projectUpdateCommand.getName());
        project.setDescription(projectUpdateCommand.getDescription());

        log.info("Project {} updated by user {}", id, username);
        return toProjectItem(project);
    }

    public void delete(Long id, String username) {
        Project project = findProjectAndCheckAccess(id, username);
        projectRepository.delete(project);
        log.info("Project {} deleted by user {}", id, username);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username));
    }

    private Project findProjectAndCheckAccess(Long id, String username) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        if (project.getOwner().getUsername().equals(username)) {
            return project;
        } else {
            throw new ProjectAccessDeniedException(id, username);
        }
    }

    private ProjectItem toProjectItem(Project project) {
        ProjectItem item = modelMapper.map(project, ProjectItem.class);
        item.setOwnerUsername(project.getOwner().getUsername());
        return item;
    }
}
