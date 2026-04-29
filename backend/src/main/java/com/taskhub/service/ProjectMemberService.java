package com.taskhub.service;

import com.taskhub.domain.MemberRole;
import com.taskhub.domain.Project;
import com.taskhub.domain.ProjectMember;
import com.taskhub.domain.User;
import com.taskhub.dto.incoming.AddMemberCommand;
import com.taskhub.dto.outgoing.ProjectMemberItem;
import com.taskhub.exception.*;
import com.taskhub.repository.ProjectMemberRepository;
import com.taskhub.repository.ProjectRepository;
import com.taskhub.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class ProjectMemberService {

    private final ProjectRepository projectRepository;

    private final ProjectMemberRepository projectMemberRepository;

    private final UserRepository userRepository;

    @Autowired
    public ProjectMemberService(ProjectRepository projectRepository,
                                ProjectMemberRepository projectMemberRepository,
                                UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    public ProjectMemberItem addMember(Long projectId, AddMemberCommand command, String callerUsername) {
        Project project = findProject(projectId);
        requireOwner(project, callerUsername);

        User newMember = userRepository.findByUsername(command.getUsername())
                .orElseThrow(() -> new UserNotFoundException(command.getUsername()));

        if (projectMemberRepository.existsByProjectAndUser(project, newMember)) {
            throw new MemberAlreadyExistsException(command.getUsername(), projectId);
        }

        ProjectMember membership = new ProjectMember();
        membership.setProject(project);
        membership.setUser(newMember);
        membership.setRole(MemberRole.MEMBER);
        ProjectMember saved = projectMemberRepository.save(membership);

        log.info("User {} added as MEMBER to project {} by {}", command.getUsername(), projectId, callerUsername);
        return toProjectMemberItem(saved);
    }

    public List<ProjectMemberItem> listMembers(Long projectId, String callerUsername) {
        Project project = findProject(projectId);
        requireMember(project, callerUsername);

        List<ProjectMember> memberships = projectMemberRepository.findAllByProject(project);
        return memberships.stream()
                .map(this::toProjectMemberItem)
                .toList();
    }

    public void removeMember(Long projectId, Long memberId, String callerUsername) {
        Project project = findProject(projectId);
        requireOwner(project, callerUsername);

        ProjectMember membership = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!membership.getProject().getId().equals(projectId)) {
            throw new MemberNotFoundException(memberId);
        }

        if (membership.getRole() == MemberRole.OWNER) {
            throw new CannotRemoveOwnerException(projectId);
        }

        projectMemberRepository.delete(membership);
        log.info("Member {} removed from project {} by {}", membership.getUser().getUsername(), projectId, callerUsername);
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username));
    }

    private void requireMember(Project project, String username) {
        User user = findUserByUsername(username);
        projectMemberRepository.findByProjectAndUser(project, user)
                .orElseThrow(() -> new ProjectAccessDeniedException(project.getId(), username));
    }

    private void requireOwner(Project project, String username) {
        User user = findUserByUsername(username);
        projectMemberRepository.findByProjectAndUser(project, user)
                .filter(membership -> membership.getRole() == MemberRole.OWNER)
                .orElseThrow(() -> new ProjectAccessDeniedException(project.getId(), username));
    }

    private ProjectMemberItem toProjectMemberItem(ProjectMember membership) {
        return new ProjectMemberItem(
                membership.getId(),
                membership.getUser().getUsername(),
                membership.getRole()
        );
    }
}
