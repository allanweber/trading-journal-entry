package com.trading.journal.entry.configuration;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.allanweber.jwttoken.service.JwtTokenReader;
import com.trading.journal.entry.queries.TokenRequestScope;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class TokenScopeFilter extends OncePerRequestFilter {

    private final JwtTokenReader tokenReader;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");
        String token = null;
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.replace("Bearer ", "");
        }
        if (Objects.nonNull(token)) {
            AccessTokenInfo accessTokenInfo = tokenReader.getAccessTokenInfo(token);
            TokenRequestScope.set(accessTokenInfo);
        }
        filterChain.doFilter(request, response);
    }
}
