package com.taskhub.controller;

import com.taskhub.dto.incoming.AddMemberCommand;
import com.taskhub.dto.outgoing.ProjectMemberItem;
import com.taskhub.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/members")
@Slf4j
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @PostMapping
    public ResponseEntity<ProjectMemberItem> addMember(@PathVariable Long projectId,
                                                       @Valid @RequestBody AddMemberCommand addMemberCommand,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Adding member '{}' to project {} by user: {}",
                addMemberCommand.getUsername(), projectId, userDetails.getUsername());

        ProjectMemberItem result = projectMemberService.addMember(projectId, addMemberCommand, userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProjectMemberItem>> listMembers(@PathVariable Long projectId,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Listing members of project {} for user: {}", projectId, userDetails.getUsername());

        List<ProjectMemberItem> result = projectMemberService.listMembers(projectId, userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long projectId,
                                             @PathVariable Long memberId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Removing member {} from project {} by user: {}", memberId, projectId, userDetails.getUsername());

        projectMemberService.removeMember(projectId, memberId, userDetails.getUsername());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}