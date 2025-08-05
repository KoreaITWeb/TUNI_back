package com.project.tuni_back.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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

import jakarta.servlet.http.HttpServletRequest;
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
    @PostMapping(value= "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@ModelAttribute RegisterRequestDto dto) {
    	System.out.println("profileImg isEmpty? " + dto.getProfileImg().isEmpty());

    	authService.verifyCode(dto); // 최종 가입 전 코드 재검증
    	Map<String, Object> response = new HashMap<>();
    	response.put("message", "회원 가입에 성공했습니다.");
    	response.put("token", authService.registerNewUser(dto));
        return ResponseEntity.ok(response);
    }
    
    // 토큰 만료시 재발급해주는 로직
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            // 요청 헤더나 쿠키에서 Refresh Token을 추출
            String refreshToken = resolveRefreshToken(request); // 이 함수는 직접 구현해야 함
            
            // 서비스를 통해 새로운 Access Token 발급
            String newAccessToken = authService.reissueAccessToken(refreshToken);
            
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }
    
    /**
     * Request Header에서 Refresh Token 정보를 추출합니다.
     * @param request HttpServletRequest 객체
     * @return 추출된 Refresh Token (Bearer 제거)
     */
    private String resolveRefreshToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization-Refresh");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}