package com.project.tuni_back.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	
	
	@GetMapping("/{userId}")
    public ResponseEntity<UserVO> getMyPage(@PathVariable("userId") String userId) {
        UserVO user = mypageService.getUserByUserId(userId);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/mypage")
    public ResponseEntity<?> updateUserId(
            @RequestParam String oldUserId, 
            @RequestParam String newUserId) {
    	mypageService.updateUserId(oldUserId, newUserId);
        return ResponseEntity.ok().build();
    }
}
