package com.taskhub.controller;

import com.taskhub.dto.incoming.ProjectCreateCommand;
import com.taskhub.dto.incoming.ProjectUpdateCommand;
import com.taskhub.dto.outgoing.ProjectItem;
import com.taskhub.dto.outgoing.ProjectListItem;
import com.taskhub.service.ProjectService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectItem> create(@RequestBody @Valid ProjectCreateCommand projectCreateCommand,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creating project for user: {}", userDetails.getUsername());

        ProjectItem result = projectService.create(projectCreateCommand, userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProjectListItem>> listMyProjects(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Listing projects for user: {}", userDetails.getUsername());

        List<ProjectListItem> result = projectService.listMyProjects(userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectItem> getById(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Fetching project {} for user: {} ", id, userDetails.getUsername());

        ProjectItem result = projectService.getById(id, userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectItem> update(@PathVariable Long id,
                                              @RequestBody @Valid ProjectUpdateCommand projectUpdateCommand,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Updating project {} for user: {} ", id, userDetails.getUsername());

        ProjectItem result = projectService.update(id, projectUpdateCommand, userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Deleting project {} for user: {} ", id, userDetails.getUsername());

        projectService.delete(id, userDetails.getUsername());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
