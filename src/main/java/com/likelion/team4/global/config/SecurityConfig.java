package com.likelion.team4.global.config;

import com.likelion.team4.global.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 브라우저의 사전 요청은 조건 없이 모두 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
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
                                "/api/v1/routinefit/notifications/*/read",
                                "/api/v1/routinefit/missions/*/complete",
                                "/api/v1/routinefit/routine-logs/*/chats",
                                "/api/v1/routinefit/chats/*/missions",
                                "/api/v1/routinefit/missions/*/complete",
                                "/api/v1/routinefit/missions/*",
                                "/api/v1/routinefit/reports/**"
                        ).permitAll()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트엔드가 접속하는 출처(Origin) 허용
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://54.117.20.73:8080",
                "https://fe-1tsx.vercel.app"
        ));

        // 허용할 HTTP 메서드 (OPTIONS 필수)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 프론트엔드에서 보내는 헤더 허용 (Authorization 필수)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // 토큰 등 자격 증명(Credentials)을 포함한 요청 허용
        configuration.setAllowCredentials(true);

        // Preflight 요청 캐싱 시간 설정 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
