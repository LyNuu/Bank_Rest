package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@Slf4j
@RestControllerAdvice
public class GlobalControllerExceptionHandlers {
    @ExceptionHandler(exception = SQLException.class)
    public ResponseEntity<String> handleException(HttpServletRequest request, SQLException e) {
        log.error("DATABASE_FAILURE - Request: {} {} from IP: {}, User-Agent: {}, Error: {} - SQLState: {}, ErrorCode: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                e.getMessage(),
                e.getSQLState(),
                e.getErrorCode(),
                e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
    }

    @ExceptionHandler(exception = EntityNotFoundException.class)
    public ResponseEntity<String> handleException(HttpServletRequest request, EntityNotFoundException e) {
        log.warn("ENTITY_NOT_FOUND - Request: {} {} from User, Error: {}",
                request.getMethod(),
                request.getRequestURI(),
                e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(exception = IllegalArgumentException.class)
    public ResponseEntity<String> handleException(HttpServletRequest request,
                                                  IllegalArgumentException e) {
        log.warn("BUSINESS_VALIDATION_FAILED - Request: {} {}, Validation error: {}",
                request.getMethod(),
                request.getRequestURI(),
                e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(exception = SecurityException.class)
    public ResponseEntity<String> handleException(HttpServletRequest request,
                                                  SecurityException e) {
        log.warn("SECURITY_VIOLATION - Request: {} {} from IP: {}, Error: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

}
