package com.deliverXY.backend.NewCode.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtSigningKeyConfig {

    @Bean
    public SecretKey jwtSigningKey(@Value("${jwt.secret}") String secret) {
        if (secret.length() < 32) {
            secret = secret + "0".repeat(32 - secret.length());
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
