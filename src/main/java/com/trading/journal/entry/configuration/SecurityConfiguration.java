package com.trading.journal.entry.configuration;

import com.allanweber.jwttoken.service.JwtTokenAuthenticationCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.stream.Stream;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration {

    private final ServerAuthenticationExceptionEntryPoint serverAuthenticationExceptionEntryPoint;
    private final JwtTokenAuthenticationCheck jwtTokenAuthenticationCheck;

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .exceptionHandling(custom -> custom.authenticationEntryPoint(serverAuthenticationExceptionEntryPoint))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(getPublicPath()).permitAll()
                                .anyRequest().hasAnyAuthority("ROLE_USER")
                )
                .addFilterBefore(new JwtTokenAuthenticationFilter(jwtTokenAuthenticationCheck), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(getCorsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        return httpSecurity.build();
    }

    private static CorsConfigurationSource getCorsConfigurationSource() {
        return request -> {
            CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
            corsConfiguration.setAllowedMethods(Stream.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH").toList());
            return corsConfiguration;
        };
    }

    private String[] getPublicPath() {
        String[] monitoring = {"/health/**", "/prometheus", "/metrics*/**"};
        return Stream.of(monitoring).toArray(String[]::new);
    }
}
