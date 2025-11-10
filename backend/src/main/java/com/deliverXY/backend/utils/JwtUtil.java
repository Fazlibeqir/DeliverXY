package com.deliverXY.backend.utils;

import com.deliverXY.backend.constants.JwtConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}")String secret) {
        // Ensure the secret is at least 256 bits (32 bytes) for HS256
        if (secret.length() < 32) {
            secret = secret + "0".repeat(32 - secret.length());
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }

    public String generateToken(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String username;
        String authorities;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
            authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
        } else {
            username = principal.toString(); // fallback for String case
            authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JwtConstants.EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username)
                .claim(JwtConstants.AUTHORITIES_KEY, authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    private String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JwtConstants.EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(username)
                .claim(JwtConstants.AUTHORITIES_KEY, role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (JwtException e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }
    public String getRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get(JwtConstants.AUTHORITIES_KEY, String.class);
        } catch (JwtException e) {
            log.error("Error extracting roles from token: {}", e.getMessage());
            return null;
        }
    }
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration();
        } catch (JwtException e) {
            log.error("Error extracting expiration date from token: {}", e.getMessage());
            return null;
        }
    }
} 