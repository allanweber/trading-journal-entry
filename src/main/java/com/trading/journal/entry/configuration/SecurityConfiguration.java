package com.trading.journal.entry.configuration;

import com.allanweber.jwttoken.service.JwtTokenAuthenticationCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

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
                .exceptionHandling().authenticationEntryPoint(serverAuthenticationExceptionEntryPoint).and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(getPublicPath()).permitAll()
                .and()
                .addFilterBefore(new JwtTokenAuthenticationFilter(jwtTokenAuthenticationCheck), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        httpSecurity.cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues());
        httpSecurity.csrf().disable();
        httpSecurity.headers().frameOptions().disable();
        httpSecurity.authorizeRequests().anyRequest().hasAnyAuthority("ROLE_USER");
        return httpSecurity.build();
    }

    private String[] getPublicPath() {
        String[] monitoring = {"/health/**", "/prometheus", "/metrics*/**"};
        String[] authentication = {"/authentication*/**"};
        String[] swagger = {"/", "/v2/api-docs", "/swagger*/**", "/webjars/**"};
        return Stream.of(monitoring, authentication, swagger).flatMap(Stream::of).toArray(String[]::new);
    }
}
