package com.deliverXY.backend.NewCode.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Parses signed JWTs and enforces the presence of an {@code exp} claim.
 * Token issuance with expiration is implemented in {@link JwtService#generateToken}.
 */
@Component
public class SignedJwtClaimsReader {

    private final JwtParser jwtParser;

    public SignedJwtClaimsReader(SecretKey jwtSigningKey) {
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(jwtSigningKey)
                .requireExpiration(new Date(0))
                .build();
    }

    public Claims readClaims(String signedToken) {
        return jwtParser.parseClaimsJws(signedToken).getBody();
    }
}
