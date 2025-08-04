package com.project.tuni_back.service;

import java.util.List;
import java.util.Random;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.tuni_back.bean.vo.UniversityVO;
import com.project.tuni_back.bean.vo.UserVO;
import com.project.tuni_back.dto.JwtTokenDto;
import com.project.tuni_back.dto.RegisterRequestDto;
import com.project.tuni_back.jwt.JwtTokenProvider;
import com.project.tuni_back.mapper.UniversityMapper;
import com.project.tuni_back.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

//AuthService
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

	private final RedisService redisService;
	private final EmailService emailService; 
	private final UserMapper userMapper;   
	private final UniversityMapper universityMapper;
	private final JwtTokenProvider jwtTokenProvider; // JWT 생성 유틸리티 클래스

	/**
     * 모든 대학교 목록을 조회
     */
    public List<UniversityVO> getAllUniversities() {
        return universityMapper.findAll();
    }
    
    // 6자리 랜덤 인증 코드 생성
    private String createVerificationCode() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
    
    // 이메일 전송 로직
	public void sendCodeToEmail(String email) {
        String verificationCode = createVerificationCode();
        emailService.sendVerificationEmail(email, verificationCode);
        
        // 이메일과 인증 코드를 Redis에 5분간 저장
        redisService.setVerificationCode(email, verificationCode);
    }
    
    /**
     * 대학교-이메일 도메인 검증 후 인증 코드 발송
     */
    public void validateAndSendCode(Long universityId, String email) {
        // 1. 사용자가 입력한 이메일에서 도메인 추출
        String userDomain = email.substring(email.indexOf("@") + 1);

        // 2. 선택한 대학교 ID로 허용된 도메인 목록을 DB에서 조회
        List<String> allowedDomains = universityMapper.findDomainsByUniversityId(universityId);

        // 3. 만약 해당 대학교에 등록된 도메인이 없다면 에러 처리
        if (allowedDomains == null || allowedDomains.isEmpty()) {
            throw new IllegalArgumentException("해당 대학교에 등록된 이메일 도메인이 없습니다.");
        }

        // 4. 사용자의 도메인이 허용된 도메인 목록에 포함되어 있는지 확인
        // .anyMatch()는 목록 중 하나라도 일치하면 true를 반환
        boolean isDomainValid = allowedDomains.stream()
                .anyMatch(allowedDomain -> allowedDomain.equalsIgnoreCase(userDomain));
        
        if (!isDomainValid) {
            throw new IllegalArgumentException("선택한 대학교에서 허용되지 않는 이메일 도메인입니다.");
        }
        
        // 5. 검증 통과 시, 코드 발송 로직 호출
        // sendCodeToEmail(email);
        String verificationCode = "123456";
        redisService.setVerificationCode(email, verificationCode);
    }
	
    // 코드 검증
    public boolean verifyCode(RegisterRequestDto dto) {
        String storedCode = redisService.getVerificationCode(dto.getEmail());
        if (storedCode == null || !storedCode.equals(dto.getCode())) {
            throw new BadCredentialsException("인증 코드가 일치하지 않습니다.");
        }
        return true;
    }
    
    // 닉네임 중복 확인
    public boolean isNicknameAvailable(String nickname) {
        return userMapper.findByUserId(nickname) == null;
    }
    
    // 신규 회원 가입
    public JwtTokenDto registerNewUser(RegisterRequestDto dto) {
        // 1. 닉네임 중복 확인
        UserVO existingUserByNickname = userMapper.findByUserId(dto.getUserId());
        if (existingUserByNickname != null) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        String domain = dto.getEmail().split("@")[1];
		UniversityVO university = universityMapper.findByDomain(domain);
        // 2. 신규 유저 객체 생성
        UserVO newUser = new UserVO();
        newUser.setEmail(dto.getEmail());
        newUser.setUserId(dto.getUserId()); // 입력받은 닉네임으로 설정
        newUser.setSchoolId(university.getSchoolId());
        
        userMapper.save(newUser);

        // 3. 가입 완료 후 Redis 코드 삭제 및 JWT 발급
        redisService.deleteVerificationCode(dto.getEmail());
        return jwtTokenProvider.generateToken(newUser);
    }
    
    // 기존 유저 로그인
    public JwtTokenDto loginExistingUser(RegisterRequestDto dto) {
        // Redis 코드는 이미 verifyCode에서 검증되었으므로 여기서는 삭제만 진행
        redisService.deleteVerificationCode(dto.getEmail());
        // 이메일로 유저 찾기
        UserVO user = userMapper.findByEmail(dto.getEmail());
        return jwtTokenProvider.generateToken(user);
    }
    
    // 토큰 재발급 로직
    public String reissueAccessToken(String refreshToken) {
        // 1. Refresh Token 검증 (유효성, 만료 시간 등)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new SecurityException("Invalid refresh token.");
        }

        // 2. Refresh Token에서 사용자 정보 추출
        Authentication authentication = jwtTokenProvider.getAuthenticationFromRefreshToken(refreshToken); // RefreshToken용 인증 추출 메소드 필요

        // 3. 새로운 Access Token 생성
        UserVO user = userMapper.findByUserId(authentication.getName());
        return jwtTokenProvider.generateAccessToken(user); // Access Token만 생성하는 메소드 필요
    }
    
    
    
//	public JwtTokenDto verifyCodeAndLogin(RegisterRequestDto dto) {
//		// 1. Redis에서 인증 코드 가져오기
//		String storedCode = redisService.getVerificationCode(dto.getEmail());
//
//		// 2. 코드 검증
//		if (storedCode == null || !storedCode.equals(dto.getCode())) {
//		    throw new BadCredentialsException("인증 코드가 일치하지 않습니다.");
//		}
//
//		// 3. 사용자 조회
//		UserVO user = userMapper.findByEmail(dto.getEmail()); // ◀️ 매퍼로 조회
//		
//		if (user == null) {
//		    // 4. [신규 회원] 사용자가 없으면 새로 생성
//			String domain = dto.getEmail().split("@")[1];
//			UniversityVO university = universityMapper.findByDomain(domain); // 매퍼로 조회
//			if (university == null) {
//			    throw new IllegalArgumentException("존재하지 않는 대학 도메인입니다.");
//			}
//		
//			UserVO newUser = new UserVO();
//			newUser.setUserId("testuser" + dto.getCode());
//			newUser.setSchoolId(university.getSchoolId());
//			newUser.setEmail(dto.getEmail());
//			 
//			userMapper.save(newUser);
//			user = userMapper.findByEmail(dto.getEmail()); // 저장 후 다시 조회
//		}
//		
//		// 5. JWT 생성 및 발급
//		JwtTokenDto token = jwtTokenProvider.generateToken(user.getEmail());
//		
//		// 6. 인증 완료 후 Redis의 코드 삭제
//		redisService.deleteVerificationCode(dto.getEmail());
//		
//		return token;
//	}
    
    

}