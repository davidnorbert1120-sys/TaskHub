package com.taskhub.controller;

import com.taskhub.dto.incoming.CommentCreateCommand;
import com.taskhub.dto.outgoing.CommentItem;
import com.taskhub.service.CommentService;
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
@RequestMapping("/projects/{projectId}/tasks/{taskId}/comments")
@Slf4j
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentItem> addComment(@PathVariable Long projectId,
                                                  @PathVariable Long taskId,
                                                  @Valid @RequestBody CommentCreateCommand commentCreateCommand,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Adding comment to task {} in project {} by user: {}", taskId, projectId, userDetails.getUsername());

        CommentItem result = commentService.addComment(projectId, taskId, commentCreateCommand, userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CommentItem>> listComments(@PathVariable Long projectId,
                                                          @PathVariable Long taskId,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Listing comments of task {} in project {} for user: {}", taskId, projectId, userDetails.getUsername());

        List<CommentItem> result = commentService.listComments(projectId, taskId, userDetails.getUsername());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long projectId,
                                              @PathVariable Long taskId,
                                              @PathVariable Long commentId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Deleting comment {} from task {} in project {} by user: {}",
                commentId, taskId, projectId, userDetails.getUsername());

        commentService.deleteComment(projectId, taskId, commentId, userDetails.getUsername());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}