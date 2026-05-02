package com.taskhub.exception;

public class CommentDeleteForbiddenException extends RuntimeException {
    public CommentDeleteForbiddenException(Long commentId, String username) {
        super("User '" + username + "' is not allowed to delete comment " + commentId);
    }
}
