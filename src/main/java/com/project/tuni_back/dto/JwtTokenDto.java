package com.project.tuni_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtTokenDto {

    private String grantType; // JWT 인증 타입, 여기서는 "Bearer"
    private String accessToken;
    private String refreshToken;

}