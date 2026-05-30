package com.deliverXY.backend.NewCode.common.util;

import jakarta.servlet.http.HttpServletRequest;

public final class HttpRequestSanitizer {

    private HttpRequestSanitizer() {
    }

    public static String resolvePath(HttpServletRequest request) {
        if (request == null) {
            return "/";
        }
        String servletPath = request.getServletPath() != null ? request.getServletPath() : "";
        String pathInfo = request.getPathInfo() != null ? request.getPathInfo() : "";
        return safeRequestPath(servletPath + pathInfo);
    }

    public static String safeRequestPath(String requestUri) {
        if (requestUri == null || requestUri.isBlank()) {
            return "/";
        }
        String sanitized = requestUri.replace('\\', '/');
        if (sanitized.contains("..")) {
            return "/invalid-path";
        }
        return sanitized;
    }
}
