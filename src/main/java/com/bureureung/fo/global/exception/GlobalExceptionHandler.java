package com.bureureung.fo.global.exception;

import com.bureureung.fo.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리(CustomException)
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("[CustomException] {} {} - {}: {}",
                request.getMethod(), request.getRequestURI(),
                errorCode.getCode(), errorCode.getMessage());

        return ApiResponse.fail(errorCode.getHttpStatus(), errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * @Valid 검증 실패 (DTO 입력값 검증)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("[ValidationException] {} {} - {}",
                request.getMethod(), request.getRequestURI(), errors);

        ErrorCode errorCode = ErrorCode.INVALID_INPUT;
        return ApiResponse.fail(errorCode.getHttpStatus(), errorCode.getCode(), errorCode.getMessage(), errors);
    }

    /**
     * 잘못된 HTTP 메서드
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("[MethodNotAllowed] {} {} - {}",
                request.getMethod(), request.getRequestURI(), e.getMessage());

        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
        return ApiResponse.fail(errorCode.getHttpStatus(), errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 그 외 모든 예외 (예상치 못한 서버 에러)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception e, HttpServletRequest request) {
        log.error("[UnhandledException] {} {}",
                request.getMethod(), request.getRequestURI(), e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ApiResponse.fail(errorCode.getHttpStatus(), errorCode.getCode(), errorCode.getMessage());
    }
}
