package com.taskhub.exception;

public class InvalidAssigneeException extends RuntimeException {

    public InvalidAssigneeException(String username, Long projectId) {
        super("User '" + username + "' is not a member of project " + projectId + " and cannot be assigned");
    }
}