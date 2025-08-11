package com.project.tuni_back.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.tuni_back.bean.vo.BoardVO;
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
   
   @Value("${spring.servlet.multipart.location}")  // application.yml의 경로 주입
   private String uploadPath;
   

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
         String fullPath = uploadPath + relativePath.replace('/', File.separatorChar);

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
         String fullPath = uploadPath + relativePath.replace('/', File.separatorChar);

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
   
   // 프로필 수정
   @PutMapping(value = "/{userId}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   public ResponseEntity<?> updateProfile(
         @PathVariable("userId") String oldUserId,
         @RequestParam("userId") String newUserId,
         @RequestParam(value = "profileImgFile", required = false) MultipartFile profileImgFile) {
      try {
         System.out.println("oldUserId = " + oldUserId);
         System.out.println("newUserId = " + newUserId);
         System.out.println("profileImgFile = " + (profileImgFile != null ? profileImgFile.getOriginalFilename() : "null"));

         String newProfileImgUrl = null;

         // 업로드 폴더가 없으면 생성
         File uploadDir = new File(uploadPath);
         if (!uploadDir.exists()) {
            uploadDir.mkdirs();
         }

         if (profileImgFile != null && !profileImgFile.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + profileImgFile.getOriginalFilename();
            File dest = new File(uploadPath + File.separator + fileName);
            profileImgFile.transferTo(dest);

            // 실제 서비스 도메인과 경로에 맞게 URL 생성 (예시)
            newProfileImgUrl = "" + fileName;
         }

         JwtTokenDto newToken = mypageService.updateUserProfile(oldUserId, newUserId, newProfileImgUrl);

         System.out.println("[DEBUG] 새 토큰 발급 완료");
         return ResponseEntity.ok(newToken);

      } catch (IOException e) {
         System.err.println("[ERROR] 파일 저장 중 오류: " + e.getMessage());
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 저장 실패");
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
