package com.project.tuni_back.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.tuni_back.bean.vo.BoardVO;
import com.project.tuni_back.bean.vo.UserVO;
import com.project.tuni_back.dto.JwtTokenDto;
import com.project.tuni_back.mapper.UserMapper;
import com.project.tuni_back.service.MypageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MypageController {
	
	private final MypageService mypageService;
	private final UserMapper userMapper;
	// 업로드된 이미지 저장 경로
    private final String uploadDir = "C:\\upload\\tuni"; 
	
//	@GetMapping("/{userId}")
//    public ResponseEntity<UserVO> getUserId(@PathVariable("userId") String userId) {
//        UserVO user = mypageService.getUserByUserId(userId);
//        return ResponseEntity.ok(user);
//    }
	
	// 사용자 정보 불러오기 호출
	@GetMapping("/{userId}")
	public ResponseEntity<Map<String, Object>> getUserWithUniversity(@PathVariable String userId) {
	    Map<String, Object> data = mypageService.getId(userId);
	    return ResponseEntity.ok(data);
	}

	// 프로필 불러오기 호출
	@GetMapping(value="/{userId}/profile", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> getProfileImage(@PathVariable("userId") String userId) {
	    try {
	        String relativePath = mypageService.getProfile(userId);
	        String fullPath = uploadDir + relativePath.replace('/', File.separatorChar);

	        File file = new File(fullPath);
	        if (!file.exists()) {
	            return ResponseEntity.notFound().build();
	        }

	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Content-Type", Files.probeContentType(file.toPath()));
	        byte[] bytes = FileCopyUtils.copyToByteArray(file);

	        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);

	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    } catch (IOException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}
	
	@PostMapping(value="/{userId}/profile", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> getProfileImage1(@PathVariable("userId") String userId) {
	    try {
	        String relativePath = mypageService.getProfile(userId);
	        String fullPath = uploadDir + relativePath.replace('/', File.separatorChar);

	        File file = new File(fullPath);
	        if (!file.exists()) {
	            return ResponseEntity.notFound().build();
	        }

	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Content-Type", Files.probeContentType(file.toPath()));
	        byte[] bytes = FileCopyUtils.copyToByteArray(file);

	        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);

	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    } catch (IOException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}
	/*
	@GetMapping(value="/display", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getImage(@RequestParam("fileName") String fileName) {
    	// log.info(fileName);
        File file = new File(uploadDir, fileName);
        ResponseEntity<byte[]> result = null;
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", Files.probeContentType(file.toPath()));
            result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file), headers, HttpStatus.OK);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    */
//	@GetMapping("/{schoolId")
//	public ResponseEntity<UniversityVO> getSchoolId(@PathVariable("schoolId") String schoolId){
//		UniversityVO university = mypageService.getUniversityById(schoolId);
//		
//	}
	
	@PutMapping("/{userId}/update")
	public ResponseEntity<?> updateProfile(
	    @PathVariable("userId") String oldUserId,
	    @RequestBody UserVO user) {
	    try {
	        System.out.println("oldUserId = " + oldUserId);
	        System.out.println("newUserId = " + user.getUserId());
	        System.out.println("profileImg = " + user.getProfileImg());

	        JwtTokenDto newToken = mypageService.updateUserProfile(oldUserId, user.getUserId(), user.getProfileImg());

	        System.out.println("[DEBUG] 새 토큰 발급 완료");

	        return ResponseEntity.ok(newToken);
	    } catch (IllegalArgumentException e) {
	        System.err.println("[ERROR] " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	    } catch (RuntimeException e) {
	        System.err.println("[ERROR] " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
	    }
	}

	
	

    
    @GetMapping("/{userId}/likes")
    public ResponseEntity<List<BoardVO>> getLikedBoards(@PathVariable String userId) {
        List<BoardVO> likedBoards = mypageService.getLikedBoardsByUser(userId);
        return ResponseEntity.ok(likedBoards);
    }
}
