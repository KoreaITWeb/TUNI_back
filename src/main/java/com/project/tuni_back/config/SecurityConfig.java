package com.project.tuni_back.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.project.tuni_back.jwt.JwtAuthenticationFilter;
import com.project.tuni_back.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;


@Configuration
@EnableWebSecurity // Spring Security 설정을 활성화합니다.
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF(Cross-Site Request Forgery) 비활성화
            // REST API는 상태를 저장하지 않으므로 CSRF 공격에 비교적 안전합니다.
            .csrf(AbstractHttpConfigurer::disable)

            // CORS 설정 활성화 및 커스텀 CORS 정책 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. 세션 관리 정책 설정 -> STATELESS (상태 비저장)
            // JWT 기반 인증이므로 서버는 세션을 사용하지 않습니다.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. HTTP 요청에 대한 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
            	.requestMatchers("/api/auth/**").permitAll()   // 이 경로는 인증 없이 접근 허용
            	.requestMatchers("/**").permitAll()     // 공개 API도 인증 없이 허용
            	.anyRequest().authenticated()                   // 그 외 모든 요청은 인증 필요
            )

            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);;

        return http.build();
    }
    
    // CORS 정책 정의
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 프론트엔드 주소 (필요시 여러 개 추가 가능)
        configuration.setAllowedOrigins(List.of("http://localhost:8080"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 허용할 요청 헤더 (전체 허용)
        configuration.setAllowedHeaders(List.of("*"));

        // 인증 정보(쿠키 등) 허용 여부
        configuration.setAllowCredentials(true);

        // 클라이언트가 접근할 수 있는 응답 헤더
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 모든 경로에 대해 이 CORS 정책 적용
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
