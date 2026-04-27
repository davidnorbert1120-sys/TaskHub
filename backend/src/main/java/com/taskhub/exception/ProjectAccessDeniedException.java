package com.taskhub.exception;

public class ProjectAccessDeniedException extends RuntimeException {
    public ProjectAccessDeniedException(Long projectId, String username ) {
        super("User '" + username + "' has no access to project with id: " + projectId);
    }
}
