package com.likelion.team4.global.config;

import com.likelion.team4.global.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 API
                        .requestMatchers(
                                "/api/v1/routinefit/auth/signup",
                                "/api/v1/routinefit/auth/check-id",
                                "/api/v1/routinefit/auth/login",
                                "/api/v1/routinefit/auth/reissue",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v1/routinefit/main/**",
                                "/api/v1/routinefit/main/notifications/today",
                                "/api/v1/routinefit/routines/*/complete",
                                "/api/v1/routinefit/alternative-missions/*/complete",
                                "/api/v1/routinefit/notifications/*/read"
                        ).permitAll()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
