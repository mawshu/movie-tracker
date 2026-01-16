package ru.mawshu.movietracker.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleConflict(Exception ex, HttpServletRequest request) {
        ApiError body = new ApiError(
                500,
                "Internal Server Error",
                "Unexpected server error",
                request.getRequestURI()
        );
        return ResponseEntity.status(500).body(body);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
        ApiError body = new ApiError(
                409,
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(409).body(body);
    }
}
