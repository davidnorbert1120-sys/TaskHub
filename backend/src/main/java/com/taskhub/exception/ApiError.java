package com.taskhub.exception;

public record ApiError(String errorCode, String error, String details) {
}
