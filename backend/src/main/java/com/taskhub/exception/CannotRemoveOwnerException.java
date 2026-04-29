package com.taskhub.exception;

public class CannotRemoveOwnerException extends RuntimeException {
    public CannotRemoveOwnerException(Long projectId) {
        super("The owner of project " + projectId + " cannot be removed");
    }
}
