package com.project.tuni_back.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.tuni_back.bean.vo.UniversityVO;
import com.project.tuni_back.bean.vo.UserVO;
import com.project.tuni_back.dto.CodeRequestDto;
import com.project.tuni_back.dto.RegisterRequestDto;
import com.project.tuni_back.mapper.UserMapper;
import com.project.tuni_back.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth") // 모든 인증 관련 경로는 /api/auth 로 시작
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    /**
     * 프론트엔드에 전체 대학교 목록을 제공하는 API
     */
    @GetMapping("/universities")
    public List<UniversityVO> getUniversityList() {
        return authService.getAllUniversities();
    }

    /**
     * 선택한 대학교와 이메일 도메인을 검증하고 인증 코드를 발송하는 API
     */
    @PostMapping("/code/send") // 경로를 좀 더 명확하게 변경
    public ResponseEntity<?> sendVerificationCode(@RequestBody CodeRequestDto dto) {
        try {
            authService.validateAndSendCode(dto.getUniversityId(), dto.getEmail());
            return ResponseEntity.ok(Map.of("message", "인증 코드가 성공적으로 전송되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 이메일과 인증 코드가 유효한지 검증하는 API
     */
    @PostMapping("/code/verify") // 경로를 좀 더 명확하게 변경
    public ResponseEntity<?> verifyCode(@RequestBody RegisterRequestDto dto) {
        authService.verifyCode(dto);
        UserVO user = userMapper.findByEmail(dto.getEmail());
        boolean isNewUser = (user == null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("isNewUser", isNewUser);
        response.put("message", "코드 인증에 성공했습니다.");
        if (!isNewUser) {
        	response.put("token", authService.loginExistingUser(dto));
        	//response.put("nickname", user.getUserId());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 닉네임 중복을 확인하는 API
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = authService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }
    
    /**
     * 최종 회원가입을 처리하는 API
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto dto) {
    	authService.verifyCode(dto); // 최종 가입 전 코드 재검증
    	Map<String, Object> response = new HashMap<>();
    	response.put("message", "회원 가입에 성공했습니다.");
    	response.put("token", authService.registerNewUser(dto));
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/mypage")
    public ResponseEntity<UserVO> getMyPage(@RequestParam("userId") String userId) {
        UserVO user = authService.getUserByUserId(userId);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/mypage")
    public ResponseEntity<?> updateUserId(
            @RequestParam String oldUserId, 
            @RequestParam String newUserId) {
        authService.updateUserId(oldUserId, newUserId);
        return ResponseEntity.ok().build();
    }
}