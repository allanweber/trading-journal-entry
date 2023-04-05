package com.trading.journal.entry.configuration;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.allanweber.jwttoken.service.JwtTokenReader;
import com.trading.journal.entry.queries.TokenRequestScope;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        AccessTokenInfo accessTokenInfo = tokenReader.getAccessTokenInfo(token);
        TokenRequestScope.set(accessTokenInfo);
        filterChain.doFilter(request, response);
    }
}
