package com.project.tuni_back.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.tuni_back.dto.EmailRequestDto;
import com.project.tuni_back.dto.JwtTokenDto;
import com.project.tuni_back.dto.VerificationRequestDto;
import com.project.tuni_back.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody EmailRequestDto emailDto) {
        // 도메인 검증 및 코드 발송 로직은 AuthService에 위임
        authService.sendCodeToEmail(emailDto.getEmail());
        return ResponseEntity.ok("인증 코드가 성공적으로 전송되었습니다.");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<JwtTokenDto> verifyCodeAndLogin(@RequestBody VerificationRequestDto verificationDto) {
        // 코드 검증 및 로그인/회원가입 로직은 AuthService에 위임
        JwtTokenDto token = authService.verifyCodeAndLogin(verificationDto);
        return ResponseEntity.ok(token);
    }
}