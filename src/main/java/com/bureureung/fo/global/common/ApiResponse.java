package com.bureureung.fo.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;
    private final Map<String, String> errors;

    /**
     * 성공 응답 생성 (데이터 포함)
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(HttpStatus status, T data) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(true, "SUCCESS", null, data, null));
    }

    /**
     * 성공 응답 생성 (데이터 미포함)
     */
    public static ResponseEntity<ApiResponse<Void>> success(HttpStatus status) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(true, "SUCCESS", null, null, null));
    }

    /**
     * 실패 응답
     */
    public static ResponseEntity<ApiResponse<Void>> fail(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(false, code, message, null, null));
    }

    /**
     * 검증 실패 응답 (필드별 에러 포함)
     */
    public static ResponseEntity<ApiResponse<Void>> fali(
            HttpStatus status, String code, String message, Map<String, String> errors) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(false, code, message, null, errors));
    }

}
