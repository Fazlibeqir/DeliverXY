package com.deliverXY.backend.NewCode.security;

import com.deliverXY.backend.NewCode.user.domain.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;
    private final SignedJwtClaimsReader signedJwtClaimsReader;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtService(
            SecretKey jwtSigningKey,
            SignedJwtClaimsReader signedJwtClaimsReader,
            @Value("${jwt.access-expiration-ms}") long accessExpiration,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpiration
    ) {
        this.secretKey = jwtSigningKey;
        this.signedJwtClaimsReader = signedJwtClaimsReader;
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

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
                .setNotBefore(now)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

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
        return signedJwtClaimsReader.readClaims(token);
    }

    public long getAccessTokenExpirySeconds() {
        return accessExpiration / 1000;
    }

    public long getRefreshTokenExpirySeconds() {
        return refreshExpiration / 1000;
    }

    public boolean isExpired(String token) {
        try {
            parseClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            log.warn("Non-expiration JWT issue encountered: {}", e.getMessage());
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
