package com.trading.journal.entry.configuration;

import com.allanweber.jwttoken.helper.JwtConstants;
import com.allanweber.jwttoken.service.JwtTokenAuthenticationCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
public class JwtTokenAuthenticationFilterTest {

    @Mock
    JwtTokenAuthenticationCheck jwtTokenAuthenticationCheck;

    JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter;

    @BeforeEach
    public void setUp() {
        jwtTokenAuthenticationFilter = new JwtTokenAuthenticationFilter(jwtTokenAuthenticationCheck);
    }

    @Test
    @DisplayName("Given server request with token process request successfully")
    void serverRequestSuccess() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(JwtConstants.TOKEN_PREFIX.concat("123456789"));
        MockFilterChain chain = new MockFilterChain();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer 123456789");
        when(jwtTokenAuthenticationCheck.getAuthentication(request)).thenReturn(new UsernamePasswordAuthenticationToken("user", null));

        jwtTokenAuthenticationFilter.doFilterInternal(request, response, chain);
    }

    @Test
    @DisplayName("Given server request without token process request successfully")
    void serverRequestSuccessWithoutToken() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        MockFilterChain chain = new MockFilterChain();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        jwtTokenAuthenticationFilter.doFilterInternal(request, response, chain);
    }
}