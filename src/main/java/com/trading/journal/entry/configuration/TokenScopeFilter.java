package com.trading.journal.entry.configuration;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.allanweber.jwttoken.service.JwtTokenReader;
import com.allanweber.jwttoken.service.impl.JwtResolveTokenHttpHeader;
import com.trading.journal.entry.queries.TokenRequestScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Component
public class TokenScopeFilter extends OncePerRequestFilter {

    private final JwtResolveTokenHttpHeader resolveTokenHttpHeader;
    private final JwtTokenReader tokenReader;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveTokenHttpHeader.resolve(request);
        AccessTokenInfo accessTokenInfo = tokenReader.getAccessTokenInfo(token);
        TokenRequestScope.set(accessTokenInfo);
        filterChain.doFilter(request, response);
    }
}
