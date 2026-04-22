package com.taskhub.exception;

import java.util.ArrayList;
import java.util.List;

public class ValidationError {

    private final List<FieldErrorDTO> fieldErrors = new ArrayList<>();

    public void addFieldError(String path, String message) {
        FieldErrorDTO error = new FieldErrorDTO(path, message);
        fieldErrors.add(error);
    }

    public List<FieldErrorDTO> getFieldErrors() {
        return fieldErrors;
    }

    public record FieldErrorDTO(String field, String message) {
    }
}
