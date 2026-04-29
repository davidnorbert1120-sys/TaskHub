package com.taskhub.repository;

import com.taskhub.domain.MemberRole;
import com.taskhub.domain.Project;
import com.taskhub.domain.ProjectMember;
import com.taskhub.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findAllByProject(Project project);

    List<ProjectMember> findAllByUser(User user);

    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    Optional<ProjectMember> findByProjectAndRole(Project project, MemberRole role);

    boolean existsByProjectAndUser(Project project, User user);
}