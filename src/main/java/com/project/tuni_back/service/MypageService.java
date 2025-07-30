package com.project.tuni_back.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.tuni_back.bean.vo.UniversityVO;
import com.project.tuni_back.bean.vo.UserVO;
import com.project.tuni_back.jwt.JwtTokenProvider;
import com.project.tuni_back.mapper.UniversityMapper;
import com.project.tuni_back.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MypageService {
	private final UniversityMapper universityMapper;
	private final UserMapper userMapper;
	
	// 마이페이지
//    public UserVO getUserByUserId(String userId) {
//        UserVO user = userMapper.findByNickname(userId);
//        if (user == null) {
//            throw new IllegalArgumentException("해당 닉네임의 사용자가 없습니다.");
//        }
//        return user;
//    }
//    
//    public UniversityVO getUniversityById(Long schoolId) {
//		return universityMapper.findById(schoolId);
//	}
    
	public Map<String, Object> getId(String userId) {
	    UserVO user = userMapper.findByNickname(userId);
	    if (user == null) {
	        throw new IllegalArgumentException("사용자가 없습니다.");
	    }
	    UniversityVO university = universityMapper.findById((long) user.getSchoolId());

	    Map<String, Object> result = new HashMap<>();
	    result.put("user", user);
	    result.put("university", university);

	    return result;
	}

	
    public void updateUserId(String oldUserId, String newUserId) {
        // 중복 체크
        UserVO existingUser = userMapper.findByNickname(newUserId);
        if (existingUser != null) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        userMapper.updateUserId(oldUserId, newUserId);
    }
	
	
}
