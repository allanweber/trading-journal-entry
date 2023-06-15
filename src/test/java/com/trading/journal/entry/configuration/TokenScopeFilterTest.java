package com.trading.journal.entry.configuration;

import com.allanweber.jwttoken.data.AccessTokenInfo;
import com.allanweber.jwttoken.helper.JwtConstants;
import com.allanweber.jwttoken.service.JwtTokenReader;
import com.trading.journal.entry.queries.TokenRequestScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class TokenScopeFilterTest {

    public static final String TOKEN = "123456789";
    @Mock
    JwtTokenReader tokenReader;

    TokenScopeFilter tokenScopeFilter;

    @BeforeEach
    public void setUp() {
        tokenScopeFilter = new TokenScopeFilter(tokenReader);
        TokenRequestScope.clear();
    }

    @DisplayName("When there is a Authorization header, set token to Scope")
    @Test
    void setScope() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        MockFilterChain chain = new MockFilterChain();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(JwtConstants.TOKEN_PREFIX.concat(TOKEN));
        AccessTokenInfo tokenInfo = new AccessTokenInfo("user", 1L, "Test-Tenancy", singletonList("ROLE_USER"));
        when(tokenReader.getAccessTokenInfo(TOKEN)).thenReturn(tokenInfo);

        tokenScopeFilter.doFilterInternal(request, response, chain);

        assertThat(TokenRequestScope.get()).isEqualTo(tokenInfo);
    }

    @DisplayName("When there is no Authorization header, do not set Scope")
    @Test
    void notSetScope() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        MockFilterChain chain = new MockFilterChain();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        tokenScopeFilter.doFilterInternal(request, response, chain);

        assertThat(TokenRequestScope.get()).isNull();

        verify(tokenReader, never()).getAccessTokenInfo(any());
    }
}