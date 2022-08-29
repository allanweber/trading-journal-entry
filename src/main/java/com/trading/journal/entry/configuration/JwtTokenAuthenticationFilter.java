package com.trading.journal.entry.configuration;

import com.allanweber.jwttoken.service.JwtTokenAuthenticationCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenAuthenticationCheck jwtTokenAuthenticationCheck;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var authentication = jwtTokenAuthenticationCheck.getAuthentication(request);
        if (authentication == null) {
            filterChain.doFilter(request, response);
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
