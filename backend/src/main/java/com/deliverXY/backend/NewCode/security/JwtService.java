package com.deliverXY.backend.NewCode.security;


import com.deliverXY.backend.NewCode.user.domain.AppUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpiration;

    public JwtService(@Value("${jwt.secret}") String secret) {
        if (secret.length() < 32) {
            secret = secret + "0".repeat(32 - secret.length());
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    // TOKEN GENERATION

    public String generateAccessToken(AppUser user) {
        return generateToken(user, accessExpiration, "access");
    }

    public String generateRefreshToken(AppUser user) {
        return generateToken(user, refreshExpiration, "refresh");
    }

    private String generateToken(AppUser user, long expires, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expires);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("type", type)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // TOKEN PARSING & VALIDATION

    public String extractUsername(String token) {
            return parseClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        try {
            return parseClaims(token).get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public long getAccessTokenExpirySeconds() {
        return accessExpiration / 1000;
    }

    public long getRefreshTokenExpirySeconds() {
        return refreshExpiration / 1000;
    }
}
