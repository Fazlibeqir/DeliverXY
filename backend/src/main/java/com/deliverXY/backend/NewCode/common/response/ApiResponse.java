package com.deliverXY.backend.NewCode.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private  T data;
    private long timestamp;

    private Integer status;
    private String errorCode;
    private String path;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, System.currentTimeMillis(), 200, null, null);
    }

    // ERROR RESPONSE
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(
                false,
                (T) message,
                System.currentTimeMillis(),
                400,
                "BAD_REQUEST",
                null
        );
    }

    public static <T> ApiResponse<T> error(String message, int status, String code, String path) {
        return new ApiResponse<>(
                false,
                (T) message,
                System.currentTimeMillis(),
                status,
                code,
                path
        );
    }
}