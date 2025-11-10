package com.deliverXY.backend.constants;

public final class JwtConstants {

    private JwtConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final long EXPIRATION_TIME = 864_000_000; // 10 days in milliseconds
    public static final String AUTHORITIES_KEY = "roles";
    public static final String USER_ID_KEY = "userId";
}