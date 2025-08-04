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
	
	private final UniversityMapper universityMapper;
	private final UserMapper userMapper;
	private final BoardMapper boardMapper;
	private final LikesMapper likesMapper;
	
	public Map<String, Object> getId(String userId) {
	    UserVO user = userMapper.findByUserId(userId);
	    if (user == null) {
	        throw new IllegalArgumentException("사용자가 없습니다.");
	    }
	    UniversityVO university = universityMapper.findById((long) user.getSchoolId());

	    Long schoolId = (long) user.getSchoolId();
	    Map<String, Object> productList = boardService.getProductList(schoolId, userId);
	    
	    Map<String, Object> result = new HashMap<>();
	    result.put("user", user);
	    result.put("university", university);
	    result.put("productList", productList.get("list"));

	    return result;
	}
	
    public void updateUserId(String oldUserId, String newUserId) {
        // 중복 체크
        UserVO existingUser = userMapper.findByUserId(newUserId);
        if (existingUser != null) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        userMapper.updateUserId(oldUserId, newUserId);
    }
    
    public List<BoardVO> getLikedBoardsByUser(String userId) {
        List<Long> likedBoardId = likesMapper.findBoardIdsByUserId(userId);
        if (likedBoardId == null || likedBoardId.isEmpty()) {
            return Collections.emptyList();
        }
        return boardMapper.findBoardsById(likedBoardId);
    }
	
	
}
