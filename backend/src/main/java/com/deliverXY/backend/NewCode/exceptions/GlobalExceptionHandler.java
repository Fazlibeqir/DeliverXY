package com.deliverXY.backend.NewCode.exceptions;

import com.deliverXY.backend.NewCode.common.response.ApiResponse;
import com.deliverXY.backend.NewCode.common.util.ExceptionMessageSanitizer;
import com.deliverXY.backend.NewCode.common.util.HttpRequestSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ResponseEntity<ApiResponse<String>> build(
            HttpStatus status,
            String message,
            String errorCode,
            String path) {
        ApiResponse<String> response = new ApiResponse<>(
                false,
                message,
                System.currentTimeMillis(),
                status.value(),
                errorCode,
                path
        );

        return new ResponseEntity<>(response, status);
    }

    private static String currentRequestPath() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "/";
        }
        HttpServletRequest request = attributes.getRequest();
        return HttpRequestSanitizer.resolvePath(request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<String>> handleBadRequest(BadRequestException e) {
        return build(HttpStatus.BAD_REQUEST, ExceptionMessageSanitizer.clientMessage(e), e.getErrorCode(), currentRequestPath());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<String>> handleConflict(ConflictException e) {
        return build(HttpStatus.CONFLICT, ExceptionMessageSanitizer.clientMessage(e), e.getErrorCode(), currentRequestPath());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<String>> handleUnauthorized(UnauthorizedException e) {
        return build(HttpStatus.UNAUTHORIZED, ExceptionMessageSanitizer.clientMessage(e), e.getErrorCode(), currentRequestPath());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<String>> handleForbidden(ForbiddenException e) {
        return build(HttpStatus.FORBIDDEN, ExceptionMessageSanitizer.clientMessage(e), e.getErrorCode(), currentRequestPath());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNotFound(NotFoundException e) {
        return build(HttpStatus.NOT_FOUND, ExceptionMessageSanitizer.clientMessage(e), e.getErrorCode(), currentRequestPath());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<String>> handleValidation(ValidationException e) {
        return build(HttpStatus.BAD_REQUEST, ExceptionMessageSanitizer.clientMessage(e), e.getErrorCode(), currentRequestPath());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneric(Exception e) {
        String path = currentRequestPath();
        log.error("Unhandled exception at {}", path, e);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                "INTERNAL_ERROR",
                path
        );
    }
}
