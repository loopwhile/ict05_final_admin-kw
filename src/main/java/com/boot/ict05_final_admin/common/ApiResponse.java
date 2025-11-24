package com.boot.ict05_final_admin.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private String path; // 요청 경로

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now(); // 응답 시각

    private T data; // 실제 데이터
    private String message; // 설명 또는 오류 메시지
    private int status; // HTTP 상태 코드

    // 성공 응답
    public static <T> ApiResponse<T> success(String path, T data) {
        return ApiResponse.<T>builder()
                .path(path)
                .data(data)
                .message("Success")
                .status(HttpStatus.OK.value())
                .build();
    }

    // 메시지 포함 성공 응답
    public static <T> ApiResponse<T> success(String path, T data, String message) {
        return ApiResponse.<T>builder()
                .path(path)
                .data(data)
                .message(message)
                .status(HttpStatus.OK.value())
                .build();
    }

    // 실패 응답
    public static <T> ApiResponse<T> error(String path, String message, HttpStatus status) {
        return ApiResponse.<T>builder()
                .path(path)
                .message(message)
                .status(status.value())
                .build();
    }
}