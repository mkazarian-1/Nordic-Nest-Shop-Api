package org.example.nordicnestshop.exception;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Hidden
public class CustomGlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError
                    ? ((FieldError) error).getField() : null;
            String errorMessage = error.getDefaultMessage();

            errors.put(Objects.requireNonNullElse(fieldName, "globalError"), errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        Map<String, String> errors = new HashMap<>();

        errors.put("error", e.getMessage());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<Map<String, String>> handleRegistrationException(
            RegistrationException e) {
        Map<String, String> errors = new HashMap<>();

        errors.put("error", e.getMessage());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateException(
            DuplicateException e) {
        Map<String, String> errors = new HashMap<>();

        errors.put("error", e.getMessage());

        return new ResponseEntity<>(errors, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ElementNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleElementNotFoundException(
            ElementNotFoundException e) {
        Map<String, String> errors = new HashMap<>();

        errors.put("error", e.getMessage());

        return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IncorrectArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIncorrectArgumentException(
            IncorrectArgumentException e) {
        Map<String, String> errors = new HashMap<>();

        errors.put("error", e.getMessage());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    //    @ExceptionHandler(AuthenticationServiceException.class)
    //    public ResponseEntity<Map<String, String>> handleAuthenticationServiceException(
    //            AuthenticationServiceException e) {
    //        Map<String, String> errors = new HashMap<>();
    //
    //        errors.put("error", e.getMessage());
    //
    //        return new ResponseEntity<>(errors, HttpStatus.UNAUTHORIZED);
    //    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException e) {
        Map<String, String> errors = new HashMap<>();

        errors.put("error", e.getMessage());

        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
