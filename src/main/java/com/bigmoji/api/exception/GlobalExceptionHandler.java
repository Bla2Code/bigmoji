package com.bigmoji.api.exception;

import com.bigmoji.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> validationError(MethodArgumentNotValidException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> generic(Exception ex, HttpServletRequest request) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
  }

  private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String path) {
    return ResponseEntity.status(status)
        .body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path));
  }
}
