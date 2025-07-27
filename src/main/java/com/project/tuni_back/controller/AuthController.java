package com.project.tuni_back.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.tuni_back.bean.vo.UserVO;
import com.project.tuni_back.dto.EmailRequestDto;
import com.project.tuni_back.dto.JwtTokenDto;
import com.project.tuni_back.dto.RegisterRequestDto;
import com.project.tuni_back.mapper.UserMapper;
import com.project.tuni_back.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody EmailRequestDto emailDto) {
        // 도메인 검증 및 코드 발송 로직은 AuthService에 위임
        authService.sendCodeToEmail(emailDto.getEmail());
        return ResponseEntity.ok("인증 코드가 성공적으로 전송되었습니다.");
    }

//    @PostMapping("/verify-code")
//    public ResponseEntity<JwtTokenDto> verifyCodeAndLogin(@RequestBody VerificationRequestDto verificationDto) {
//        // 코드 검증 및 로그인/회원가입 로직은 AuthService에 위임
//        JwtTokenDto token = authService.verifyCodeAndLogin(verificationDto);
//        return ResponseEntity.ok(token);
//    }
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody RegisterRequestDto dto) {
        // 코드 유효성 검사
        authService.verifyCode(dto);

        // 신규/기존 회원 여부 확인
        UserVO user = userMapper.findByEmail(dto.getEmail());
        boolean isNewUser = (user == null);

        // 신규/기존 회원 여부를 담아 응답
        Map<String, Object> response = new HashMap<>();
        response.put("isNewUser", isNewUser);
        response.put("message", "코드 인증에 성공했습니다.");
        return ResponseEntity.ok(response);
    }
    
    // 신규 회원 가입
    @PostMapping("/register")
    public ResponseEntity<JwtTokenDto> register(@RequestBody RegisterRequestDto dto) {
        // 코드가 유효한지 한번 더 확인
        authService.verifyCode(dto);
        
        // 신규 회원 가입 처리
        JwtTokenDto token = authService.registerNewUser(dto);
        return ResponseEntity.ok(token);
    }
    
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = authService.isNicknameAvailable(nickname);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", isAvailable);
        return ResponseEntity.ok(response);
    }
}