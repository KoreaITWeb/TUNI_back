package com.project.tuni_back.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Request Header에서 토큰 추출
        String token = resolveToken(request);
        
        try {
	        // validateToken으로 토큰 유효성 검사
	        if (token != null && jwtTokenProvider.validateToken(token)) {
	            // 토큰이 유효할 경우 토큰에서 Authentication 객체를 받아옴
	            Authentication authentication = jwtTokenProvider.getAuthentication(token);
	            // SecurityContext에 Authentication 객체를 저장
	            SecurityContextHolder.getContext().setAuthentication(authentication);
	        }
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT Token: {}", e.getMessage()); // 잘못된 서명 또는 형식
            setErrorResponse(response, "Invalid Token");
            return;
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT Token: {}", e.getMessage()); // 만료된 토큰
            setErrorResponse(response, "Expired Token");
            return;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT Token: {}", e.getMessage()); // 지원되지 않는 토큰
            setErrorResponse(response, "Unsupported Token");
            return;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage()); // 잘못된 토큰
            setErrorResponse(response, "Invalid Token");
            return;
        }
        filterChain.doFilter(request, response);
    }
    
    // 401 에러 응답을 보내는 헬퍼 메소드
    private void setErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }

    // Request Header에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
