package com.taskhub.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(Long memberId ) {
        super("Project member not found with id: " + memberId);
    }
}
