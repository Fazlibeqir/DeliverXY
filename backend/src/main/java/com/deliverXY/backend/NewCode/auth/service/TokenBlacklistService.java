package com.deliverXY.backend.NewCode.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * NOTE FOR SCALABILITY: This in-memory implementation is adequate for a single-instance
 * deployment (Diploma Thesis). For production scale (multiple instances/load balancer),
 * this MUST be replaced with a distributed store like Redis to ensure all instances
 * share the same blacklist.
 */

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final Set<String> blacklist = Collections.synchronizedSet(new HashSet<>());

    public void blacklist(String token) {
        blacklist.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}

