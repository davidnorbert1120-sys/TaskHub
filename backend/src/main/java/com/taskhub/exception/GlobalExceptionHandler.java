package com.taskhub.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    @Autowired
    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ValidationError> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        LOGGER.error("A validation error occurred: ", exception);
        BindingResult result = exception.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        return new ResponseEntity<>(processFieldErrors(fieldErrors), HttpStatus.BAD_REQUEST);
    }

    private ValidationError processFieldErrors(List<FieldError> fieldErrors) {
        ValidationError validationError = new ValidationError();
        for (FieldError fieldError : fieldErrors) {
            validationError.addFieldError(fieldError.getField(), messageSource.getMessage(fieldError, Locale.getDefault()));
        }
        return validationError;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException exception) {
        LOGGER.warn("Request body could not be read: {}", exception.getMessage());
        ApiError body = new ApiError(
                "INVALID_REQUEST_BODY",
                "The request body is invalid or contains incorrect values.",
                exception.getMostSpecificCause().getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException exception) {
        LOGGER.error("Illegal argument error: ", exception);
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiError body = new ApiError("ILLEGAL_ARGUMENT_ERROR", "An illegal argument has been passed to the method.", exception.getLocalizedMessage());

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(NoResourceFoundException exception) {
        LOGGER.warn("No matching route: {}", exception.getMessage());
        ApiError body = new ApiError(
                "NOT_FOUND",
                "The requested resource does not exist.",
                exception.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiError> defaultErrorHandler(Throwable throwable) {
        LOGGER.error("An unexpected error occurred: ", throwable);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiError body = new ApiError("UNCLASSIFIED_ERROR", "Oh, snap! Something really unexpected occurred.", throwable.getLocalizedMessage());

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUsernameAlreadyExists(UsernameAlreadyExistsException exception) {
        LOGGER.error("Username conflict: ", exception);
        HttpStatus status = HttpStatus.CONFLICT;

        ApiError body = new ApiError("USERNAME_ALREADY_EXISTS", "A user with this username already exists.", exception.getLocalizedMessage());

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExists(EmailAlreadyExistsException exception) {
        LOGGER.error("Email conflict: ", exception);
        HttpStatus status = HttpStatus.CONFLICT;

        ApiError body = new ApiError("EMAIL_ALREADY_EXISTS", "A user with this email already exists.", exception.getLocalizedMessage());

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException exception) {
        LOGGER.warn("Invalid credentials: {}", exception.getMessage());
        ApiError error = new ApiError(
                "INVALID_CREDENTIALS",
                "Authentication failed",
                exception.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiError> handleProjectNotFound(ProjectNotFoundException exception) {
        LOGGER.warn("Project not found: {}", exception.getMessage());
        ApiError body = new ApiError(
                "PROJECT_NOT_FOUND",
                "The requested project does not exist.",
                exception.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProjectAccessDeniedException.class)
    public ResponseEntity<ApiError> handleProjectAccessDenied(ProjectAccessDeniedException exception) {
        LOGGER.warn("Project access denied: {}", exception.getMessage());
        ApiError body = new ApiError(
                "PROJECT_ACCESS_DENIED",
                "You do not have access to this project.",
                exception.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(MemberAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleMemberAlreadyExists(MemberAlreadyExistsException exception) {
        LOGGER.warn("Member conflict: {}", exception.getMessage());
        ApiError body = new ApiError(
                "MEMBER_ALREADY_EXISTS",
                "This user is already a member of the project.",
                exception.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CannotRemoveOwnerException.class)
    public ResponseEntity<ApiError> handleCannotRemoveOwner(CannotRemoveOwnerException exception) {
        LOGGER.warn("Attempt to remove project owner: {}", exception.getMessage());
        ApiError body = new ApiError(
                "CANNOT_REMOVE_OWNER",
                "The project owner cannot be removed.",
                exception.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiError> handleMemberNotFound(MemberNotFoundException exception) {
        LOGGER.warn("Member not found: {}", exception.getMessage());
        ApiError body = new ApiError(
                "MEMBER_NOT_FOUND",
                "The requested project member does not exist.",
                exception.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException exception) {
        LOGGER.warn("User not found: {}", exception.getMessage());
        ApiError body = new ApiError(
                "USER_NOT_FOUND",
                "The requested user does not exist.",
                exception.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiError> handleTaskNotFound(TaskNotFoundException exception) {
        LOGGER.warn("Task not found: {}", exception.getMessage());
        ApiError body = new ApiError(
                "TASK_NOT_FOUND",
                "The requested task does not exist.",
                exception.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidAssigneeException.class)
    public ResponseEntity<ApiError> handleInvalidAssignee(InvalidAssigneeException exception) {
        LOGGER.warn("Invalid assignee: {}", exception.getMessage());
        ApiError body = new ApiError(
                "INVALID_ASSIGNEE",
                "The selected user is not a member of this project.",
                exception.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiError> handleCommentNotFound(CommentNotFoundException exception) {
        LOGGER.warn("Comment not found: {}", exception.getMessage());
        ApiError body = new ApiError(
                "COMMENT_NOT_FOUND",
                "The requested comment does not exist.",
                exception.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CommentDeleteForbiddenException.class)
    public ResponseEntity<ApiError> handleCommentDeleteForbidden(CommentDeleteForbiddenException exception) {
        LOGGER.warn("Comment delete forbidden: {}", exception.getMessage());
        ApiError body = new ApiError(
                "COMMENT_DELETE_FORBIDDEN",
                "You can only delete your own comments (or be the project owner).",
                exception.getMessage()
        );
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
}
