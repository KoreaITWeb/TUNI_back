package com.project.tuni_back.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.tuni_back.bean.vo.BoardVO;
import com.project.tuni_back.bean.vo.UniversityVO;
import com.project.tuni_back.bean.vo.UserVO;
import com.project.tuni_back.dto.JwtTokenDto;
import com.project.tuni_back.jwt.JwtTokenProvider;
import com.project.tuni_back.mapper.BoardMapper;
import com.project.tuni_back.mapper.LikesMapper;
import com.project.tuni_back.mapper.UniversityMapper;
import com.project.tuni_back.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MypageService {

	@Autowired
	private BoardService boardService;

	private final JwtTokenProvider jwtTokenProvider;
	private final UniversityMapper universityMapper;
	private final UserMapper userMapper;
	private final BoardMapper boardMapper;
	private final LikesMapper likesMapper;
	/*
	public Map<String, Object> getId(String userId) {
	    UserVO user = userMapper.findByUserId(userId);
	    if (user == null) {
	        throw new IllegalArgumentException("사용자가 없습니다.");
	    }
	    UniversityVO university = universityMapper.findById((long) user.getSchoolId());

	    Long schoolId = (long) user.getSchoolId();
	    Map<String, Object> productList = boardService.getProductsByUserId(schoolId, userId);

	    Map<String, Object> result = new HashMap<>();
	    result.put("user", user);
	    result.put("university", university);
	    result.put("productList", productList.get("list"));

	    return result;
	}
	 */
	// 사용자 정보 불러오기
	public Map<String, Object> getId(String userId) {
		UserVO user = userMapper.findByUserId(userId);
		if (user == null) {
			throw new IllegalArgumentException("사용자가 없습니다.");
		}

		UniversityVO university = universityMapper.findById((long) user.getSchoolId());

		Long schoolId = (long) user.getSchoolId();

		// 👇 여기 수정: 반환 타입은 List<BoardVO>
		List<BoardVO> productList = boardService.getProductListByUserId(schoolId, userId);

		long saleCount = productList.stream()
				.filter(p -> "SALE".equalsIgnoreCase(p.getSaleStatus()))
				.count();

		long soldCount = productList.stream()
				.filter(p -> "SOLD".equalsIgnoreCase(p.getSaleStatus()))
				.count();

		// 👇 결과를 하나의 Map으로 묶어서 반환
		Map<String, Object> result = new HashMap<>();
		result.put("user", user);
		result.put("university", university);
		result.put("productList", productList);  // List 그대로 넣으면 됨
		result.put("saleCount", saleCount);     // 판매중
	    result.put("soldCount", soldCount); 

		return result;
	}

	// 프로필 불러오기
	public String getProfile(String userId) {
	    UserVO user = userMapper.findByUserId(userId);
	    if (user == null) {
	        throw new IllegalArgumentException("사용자가 없습니다2.");
	    }
	    return user.getProfileImg();
	}
	
	// 프로필 수정
	public JwtTokenDto updateUserProfile(String oldUserId, String newUserId, String newProfileImg) {
	    System.out.println("[DEBUG] oldUserId = " + oldUserId);
	    System.out.println("[DEBUG] newUserId = " + newUserId);
	    System.out.println("[DEBUG] newProfileImg = " + newProfileImg);

	    UserVO existingUser = userMapper.findByUserId(newUserId);

	    if (existingUser != null && !existingUser.getUserId().equals(oldUserId)) {
	        throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
	    }

	    int updatedRows = userMapper.updateUserProfile(oldUserId, newUserId, newProfileImg);
	    System.out.println("[DEBUG] update 결과 updatedRows = " + updatedRows);
	    if (updatedRows == 0) {
	        throw new RuntimeException("사용자 정보 변경에 실패했습니다.");
	    }

	    int updatedCount = boardMapper.updateBoardUserId(oldUserId, newUserId);
	    System.out.println("업데이트된 게시글 수: " + updatedCount);

	    // 변경된 사용자 정보 재조회
	    UserVO updatedUser = userMapper.findByUserId(newUserId);

	    // JWT 토큰 생성 (jwtTokenProvider는 서비스 내 의존성으로 주입되어 있어야 함)
	    JwtTokenDto newToken = jwtTokenProvider.generateToken(updatedUser);

	    return newToken;
	}



	public List<BoardVO> getLikedBoardsByUser(String userId) {
		List<Long> likedBoardId = likesMapper.findBoardIdsByUserId(userId);
		if (likedBoardId == null || likedBoardId.isEmpty()) {
			return Collections.emptyList();
		}
		return boardMapper.findBoardsById(likedBoardId);
	}


}
