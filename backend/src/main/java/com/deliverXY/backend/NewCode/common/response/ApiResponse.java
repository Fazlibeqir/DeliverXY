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
    private String message;

    public ApiResponse(boolean b, String message, long l, int value, String errorCode, String path) {
        this.success = b;
        this.message = message;
        this.timestamp = l;
        this.status = value;
        this.errorCode = errorCode;
        this.path = path;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, System.currentTimeMillis(), 200, null, null,null);
    }

    // ERROR RESPONSE
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(
                false,
                null,
                System.currentTimeMillis(),
                400,
                "BAD_REQUEST",
                null,
                message
        );
    }

    public static <T> ApiResponse<T> error(String message, int status, String code, String path) {
        return new ApiResponse<>(
                false,
                null,
                System.currentTimeMillis(),
                status,
                code,
                path,
                message
        );
    }

    public static ApiResponse<String> error(String tokenIsRequired, int i) {
        return new ApiResponse<>(
                false,
                null,
                System.currentTimeMillis(),
                i,
                "BAD_REQUEST",
                null,
                tokenIsRequired
        );
    }
}