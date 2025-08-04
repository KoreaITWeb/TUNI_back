package com.project.tuni_back.jwt;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.project.tuni_back.bean.vo.UserVO;
import com.project.tuni_back.dto.JwtTokenDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;


    // application.properties에서 설정값 가져오기
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidity,
                            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidity) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityInMilliseconds = accessTokenValidity * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidity * 1000;
    }

    /**
     * Access Token과 Refresh Token을 생성
     */
    public JwtTokenDto generateToken(UserVO user) {
        // Access Token 생성
        String accessToken = generateAccessToken(user);

        
        // Refresh Token 생성
        long now = (new Date()).getTime();
        String refreshToken = Jwts.builder()
        		.setSubject(user.getUserId())
                .setExpiration(new Date(now + refreshTokenValidityInMilliseconds))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtTokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    
    // Access Token만 생성하는 메소드
    public String generateAccessToken(UserVO user) {
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + accessTokenValidityInMilliseconds);
        
        return Jwts.builder()
                .setSubject(user.getUserId())
                .claim("auth", "ROLE_USER")
                .claim("userId", user.getUserId())
                .claim("schoolId", user.getSchoolId())
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    // Refresh Token에서 인증 정보 추출
    public Authentication getAuthenticationFromRefreshToken(String token) {
        Claims claims = parseClaims(token);

        // Refresh Token에는 기본적인 사용자 ID와 권한 정보만 담아 인증 객체를 생성
        // 상세 정보는 서비스 레이어에서 DB 조회를 통해 다시 가져옴
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(new String[]{"ROLE_USER"}) // 단순 권한 부여
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * 토큰에서 사용자 정보(Authentication)를 추출
     */
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        return true;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            // 토큰이 만료되어도 클레임은 추출할 수 있음
            return e.getClaims();
        }
    }
}