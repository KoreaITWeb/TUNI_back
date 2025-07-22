package com.project.tuni_back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Spring Security 설정을 활성화합니다.
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF(Cross-Site Request Forgery) 비활성화
            // REST API는 상태를 저장하지 않으므로 CSRF 공격에 비교적 안전합니다.
            .csrf(AbstractHttpConfigurer::disable)

            // 2. 세션 관리 정책 설정 -> STATELESS (상태 비저장)
            // JWT 기반 인증이므로 서버는 세션을 사용하지 않습니다.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. HTTP 요청에 대한 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // "/api/auth/**" 경로의 모든 요청은 인증 없이 허용
                .requestMatchers("/api/auth/**", "/index", "/error", "/school/verify", "/verify-code-form").permitAll()
                // 그 외의 모든 요청은 반드시 인증을 거쳐야 함
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
