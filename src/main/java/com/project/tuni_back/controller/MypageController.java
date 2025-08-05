package com.project.tuni_back.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.tuni_back.bean.vo.BoardVO;
import com.project.tuni_back.bean.vo.UniversityVO;
import com.project.tuni_back.bean.vo.UserVO;
import com.project.tuni_back.mapper.UserMapper;
import com.project.tuni_back.service.AuthService;
import com.project.tuni_back.service.MypageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MypageController {
	
	private final MypageService mypageService;
	private final UserMapper userMapper;

	
//	@GetMapping("/{userId}")
//    public ResponseEntity<UserVO> getUserId(@PathVariable("userId") String userId) {
//        UserVO user = mypageService.getUserByUserId(userId);
//        return ResponseEntity.ok(user);
//    }
	
	@GetMapping("/{userId}")
	public ResponseEntity<Map<String, Object>> getUserWithUniversity(@PathVariable String userId) {
	    Map<String, Object> data = mypageService.getId(userId);
	    return ResponseEntity.ok(data);
	}

    
//	@GetMapping("/{schoolId")
//	public ResponseEntity<UniversityVO> getSchoolId(@PathVariable("schoolId") String schoolId){
//		UniversityVO university = mypageService.getUniversityById(schoolId);
//		
//	}
	
    @PutMapping("/mypage")
    public ResponseEntity<?> updateUserId(
            @RequestParam String oldUserId, 
            @RequestParam String newUserId) {
    	mypageService.updateUserId(oldUserId, newUserId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{userId}/likes")
    public ResponseEntity<List<BoardVO>> getLikedBoards(@PathVariable String userId) {
        List<BoardVO> likedBoards = mypageService.getLikedBoardsByUser(userId);
        return ResponseEntity.ok(likedBoards);
    }
}
