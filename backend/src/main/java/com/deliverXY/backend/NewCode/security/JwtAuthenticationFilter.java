package com.deliverXY.backend.NewCode.security;

import com.deliverXY.backend.NewCode.auth.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

       String token = resolveToken(request);
        if (tokenBlacklistService.isBlacklisted(token)) {
            filterChain.doFilter(request, response);
            return;
        }

       if (token != null && jwtService.validate(token)) {
           Claims claims = jwtService.parseClaims(token);
           String type = claims.get("type", String.class);

           if(!"access".equals(type)) {
               filterChain.doFilter(request, response);
               return;
           }
           String username = claims.getSubject();

           if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
              //Redis for high traffic fix not to make every request to database
               UserDetails userDetails = userDetailsService.loadUserByUsername(username);

               UsernamePasswordAuthenticationToken auth =
                       new UsernamePasswordAuthenticationToken(
                               userDetails,
                               null,
                               userDetails.getAuthorities()
                               );
               auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
               SecurityContextHolder.getContext().setAuthentication(auth);
           }
       }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        //Might need more skips on filtering
        // or more specififc return request.getServletPath().matches("^/api/auth/(login|register|refresh)$");
        return request.getServletPath().startsWith("/api/auth");
    }
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(JwtConstants.HEADER_STRING);

        if (StringUtils.hasText(header) && header.startsWith(JwtConstants.TOKEN_PREFIX)) {
            return header.substring(JwtConstants.TOKEN_PREFIX.length());
        }
        return null;
    }
} 