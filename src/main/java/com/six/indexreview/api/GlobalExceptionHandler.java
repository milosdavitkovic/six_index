package com.six.indexreview.api;

import com.six.indexreview.api.dto.ErrorResponse;
import com.six.indexreview.api.dto.ValidationErrorResponse;
import com.six.indexreview.application.exception.DataImportException;
import com.six.indexreview.application.exception.ResourceNotFoundException;
import com.six.indexreview.application.exception.ReviewExecutionException;
import com.six.indexreview.application.exception.ReviewValidationException;
import com.six.indexreview.validation.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import jakarta.validation.ConstraintViolationException;

import java.time.Instant;
import java.util.List;
import org.slf4j.MDC;

/**
 * Core GlobalExceptionHandler component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
/**
 * @author Milos Davitkovic
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFound(ResourceNotFoundException exception) {
        return error(404, "NOT_FOUND", exception.getMessage(), List.of());
    }

    @ExceptionHandler(DataImportException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse importError(DataImportException exception) {
        return error(400, "DATA_IMPORT_ERROR", exception.getMessage(), List.of());
    }

    @ExceptionHandler(ReviewValidationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse validation(ReviewValidationException exception) {
        List<ValidationErrorResponse> errors = exception.getValidationErrors().stream().map(this::toResponse).toList();
        return error(422, "REVIEW_VALIDATION_ERROR", exception.getMessage(), errors);
    }

    @ExceptionHandler(ReviewExecutionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse execution(ReviewExecutionException exception) {
        return error(500, "REVIEW_EXECUTION_ERROR", exception.getMessage(), List.of());
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse persistence(DataAccessException exception) {
        log.error("Persistence failure", exception);
        return error(500, "PERSISTENCE_ERROR", "Could not persist or load review data", List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse illegalArgument(IllegalArgumentException exception) {
        return error(400, "INVALID_REQUEST", exception.getMessage(), List.of());
    }

    /** Converts bean-validation failures into the common client error shape. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse requestValidation(MethodArgumentNotValidException exception) {
        List<ValidationErrorResponse> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(value -> new ValidationErrorResponse("INVALID_FIELD", value.getField(), value.getDefaultMessage(),
                        null, "ERROR"))
                .toList();
        return error(400, "INVALID_REQUEST", "Request validation failed", errors);
    }

    /** Converts missing multipart parts into the common client error shape. */
    @ExceptionHandler(MissingServletRequestPartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse missingRequestPart(MissingServletRequestPartException exception) {
        return error(400, "INVALID_REQUEST", exception.getMessage(), List.of());
    }

    /** Converts malformed path/query values into the common client error shape. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse argumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return error(400, "INVALID_REQUEST", exception.getMessage(), List.of());
    }

    /** Converts method-parameter validation failures into the common error shape. */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse constraintValidation(ConstraintViolationException exception) {
        List<ValidationErrorResponse> errors = exception.getConstraintViolations().stream()
                .map(value -> new ValidationErrorResponse("INVALID_PARAMETER", value.getPropertyPath().toString(),
                        value.getMessage(), null, "ERROR"))
                .toList();
        return error(400, "INVALID_REQUEST", "Request validation failed", errors);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse unexpected(Exception exception) {
        log.error("Unexpected server error", exception);
        return error(500, "INTERNAL_ERROR", "Unexpected server error", List.of());
    }

    private ValidationErrorResponse toResponse(ValidationError error) {
        Integer securityId = error.securityId() == null ? null : error.securityId().value();
        return new ValidationErrorResponse(error.code(), error.field(), error.message(), securityId, error.severity().name());
    }

    private ErrorResponse error(int status, String code, String message, List<ValidationErrorResponse> validationErrors) {
        return new ErrorResponse(Instant.now(), status, code, message, message,
                MDC.get("traceId"), validationErrors);
    }
}
