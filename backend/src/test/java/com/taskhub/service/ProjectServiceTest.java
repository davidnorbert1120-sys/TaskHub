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
import com.taskhub.repository.CommentRepository;
import com.taskhub.repository.ProjectMemberRepository;
import com.taskhub.repository.ProjectRepository;
import com.taskhub.repository.TaskRepository;
import com.taskhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_create_project_creates_owner_membership() {
        ProjectCreateCommand command = new ProjectCreateCommand();
        command.setName("Új projekt");
        command.setDescription("Teszt projekt");

        User owner = new User();
        owner.setId(1L);
        owner.setUsername("david");

        Project savedProject = new Project();
        savedProject.setId(10L);
        savedProject.setName("Új projekt");

        ProjectItem expected = new ProjectItem();
        expected.setId(10L);
        expected.setName("Új projekt");
        expected.setOwnerUsername("david");

        ProjectMember ownerMembership = new ProjectMember();
        ownerMembership.setRole(MemberRole.OWNER);
        ownerMembership.setUser(owner);

        when(userRepository.findByUsername("david")).thenReturn(Optional.of(owner));
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
        when(modelMapper.map(savedProject, ProjectItem.class)).thenReturn(expected);
        when(projectMemberRepository.findByProjectAndRole(savedProject, MemberRole.OWNER))
                .thenReturn(Optional.of(ownerMembership));

        ProjectItem actual = projectService.create(command, "david");

        assertEquals(expected, actual);
        verify(projectRepository).save(any(Project.class));
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    void test_listMyProjects_returns_member_projects() {
        User user = new User();
        user.setId(1L);
        user.setUsername("david");

        Project p1 = new Project();
        p1.setId(1L);
        p1.setName("Projekt 1");

        Project p2 = new Project();
        p2.setId(2L);
        p2.setName("Projekt 2");

        ProjectMember m1 = new ProjectMember();
        m1.setProject(p1);
        ProjectMember m2 = new ProjectMember();
        m2.setProject(p2);

        ProjectListItem item1 = new ProjectListItem();
        item1.setId(1L);
        item1.setName("Projekt 1");
        ProjectListItem item2 = new ProjectListItem();
        item2.setId(2L);
        item2.setName("Projekt 2");

        when(userRepository.findByUsername("david")).thenReturn(Optional.of(user));
        when(projectMemberRepository.findAllByUser(user)).thenReturn(List.of(m1, m2));
        when(modelMapper.map(p1, ProjectListItem.class)).thenReturn(item1);
        when(modelMapper.map(p2, ProjectListItem.class)).thenReturn(item2);

        List<ProjectListItem> actual = projectService.listMyProjects("david");

        assertEquals(List.of(item1, item2), actual);
    }

    @Test
    void test_getById_throws_when_project_not_found() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectService.getById(99L, "david"));
    }

    @Test
    void test_getById_throws_when_user_not_member() {
        Project project = new Project();
        project.setId(1L);

        User user = new User();
        user.setId(2L);
        user.setUsername("intruder");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("intruder")).thenReturn(Optional.of(user));
        when(projectMemberRepository.existsByProjectAndUser(project, user)).thenReturn(false);

        assertThrows(ProjectAccessDeniedException.class, () -> projectService.getById(1L, "intruder"));
    }

    @Test
    void test_update_throws_when_user_is_not_owner() {
        ProjectUpdateCommand command = new ProjectUpdateCommand();
        command.setName("Új név");

        Project project = new Project();
        project.setId(1L);

        User user = new User();
        user.setId(2L);
        user.setUsername("anna");

        ProjectMember membership = new ProjectMember();
        membership.setRole(MemberRole.MEMBER);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("anna")).thenReturn(Optional.of(user));
        when(projectMemberRepository.findByProjectAndUser(project, user)).thenReturn(Optional.of(membership));

        assertThrows(ProjectAccessDeniedException.class, () -> projectService.update(1L, command, "anna"));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void test_delete_cascades_to_tasks_members_and_comments() {
        Project project = new Project();
        project.setId(1L);

        User user = new User();
        user.setId(1L);
        user.setUsername("david");

        ProjectMember ownerMembership = new ProjectMember();
        ownerMembership.setRole(MemberRole.OWNER);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("david")).thenReturn(Optional.of(user));
        when(projectMemberRepository.findByProjectAndUser(project, user)).thenReturn(Optional.of(ownerMembership));
        when(taskRepository.findAllByProject(project)).thenReturn(List.of());
        when(projectMemberRepository.findAllByProject(project)).thenReturn(List.of());

        projectService.delete(1L, "david");

        verify(taskRepository).deleteAll(List.of());
        verify(projectMemberRepository).deleteAll(List.of());
        verify(projectRepository).delete(project);
    }
}