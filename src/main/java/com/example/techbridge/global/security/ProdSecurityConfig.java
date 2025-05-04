package com.example.techbridge.global.security;

import com.example.techbridge.auth.handler.CustomAccessDeniedHandler;
import com.example.techbridge.auth.handler.CustomAuthenticationEntryPoint;
import com.example.techbridge.auth.jwt.JwtAuthenticationFilter;
import com.example.techbridge.auth.jwt.JwtTokenProvider;
import com.example.techbridge.auth.service.TokenBlacklistService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Profile("prod")
@Configuration
public class ProdSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService blacklistService;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;


    @Autowired
    public ProdSecurityConfig(JwtTokenProvider jwtTokenProvider,
        TokenBlacklistService blacklistService,
        CustomAccessDeniedHandler accessDeniedHandler,
        CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.blacklistService = blacklistService;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public JwtAuthenticationFilter jwtFilter() {
        List<String> whiteList = List.of(
            "/auth/login", "/auth/refresh"
        );
        return new JwtAuthenticationFilter(jwtTokenProvider, blacklistService, whiteList);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(h -> h.frameOptions(FrameOptionsConfig::deny))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/members").permitAll()
                .requestMatchers("/auth/login", "/auth/refresh").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
