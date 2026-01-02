package com.example.backend.exception;
import com.example.backend.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        log.error("AppException at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(errorCode, request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Validation error at {}: {}", request.getRequestURI(), ex.getMessage());
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(ErrorCode.VALIDATION_ERROR, message, request.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.error("Method not allowed at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ErrorCode.METHOD_NOT_ALLOWED, request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleJsonParse(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.error("JSON parse error at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ErrorCode.JSON_PARSE_ERROR, request.getRequestURI());
    }

//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
//        log.error("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
//        return buildResponse(ErrorCode.UNAUTHORIZED, request.getRequestURI());
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleOtherExceptions(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: ", request.getRequestURI(), ex);
        return buildResponse(ErrorCode.UNCATEGORIZED_EXCEPTION, request.getRequestURI());
    }

    private ResponseEntity<ApiResponse<Object>> buildResponse(ErrorCode code, String path) {
        return buildResponse(code, null, path);
    }

    private ResponseEntity<ApiResponse<Object>> buildResponse(ErrorCode code, String message, String path) {
        ApiResponse<Object> response = ApiResponse.builder()
                .code(code.getCode())
                .message(message != null ? message : code.getMessage())
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
        return ResponseEntity.status(code.getStatus()).body(response);
    }
}


