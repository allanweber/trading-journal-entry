package com.trading.journal.entry.configuration.swagger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class SwaggerFilterTest {

    @DisplayName("When uri has no path redirect to swagger")
    @ParameterizedTest
    @MethodSource("values")
    void redirect(String path) throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        MockFilterChain chain = new MockFilterChain();

        when(request.getRequestURI()).thenReturn(path);

        SwaggerFilter swaggerFilter = new SwaggerFilter();
        swaggerFilter.doFilter(request, response, chain);

        verify(response).sendRedirect("/swagger-ui/index.html");
    }

    @DisplayName("Do not redirect")
    @Test
    void noRedirect() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        MockFilterChain chain = new MockFilterChain();

        when(request.getRequestURI()).thenReturn("/any");

        SwaggerFilter swaggerFilter = new SwaggerFilter();
        swaggerFilter.doFilter(request, response, chain);

        verify(response, never()).sendRedirect("/swagger-ui/index.html");
    }

    private static Stream<String> values() {
        return Stream.of(null, "", "/");
    }
}