package com.taskhub.exception;

public class MemberAlreadyExistsException extends RuntimeException {
    public MemberAlreadyExistsException(String username, Long projectId) {
        super("User '" + username + "' is already a member of project " + projectId);
    }
}
