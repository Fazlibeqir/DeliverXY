package com.deliverXY.backend.NewCode.security;

import org.springframework.stereotype.Component;

/**
 * SpEL target for intentionally unauthenticated auth endpoints (register, login, refresh).
 * Access is still limited by {@link SecurityConfig} public path rules.
 */
@Component("publicEndpointPolicy")
public class PublicEndpointPolicy {

    public boolean allowPublicAccess() {
        return true;
    }
}
