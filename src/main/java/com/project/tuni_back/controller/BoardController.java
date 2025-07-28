package com.project.tuni_back.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.tuni_back.bean.vo.BoardVO;
import com.project.tuni_back.mapper.BoardMapper;
import com.project.tuni_back.mapper.UserMapper;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/board/*")
public class BoardController {
    
    @Autowired
    private BoardMapper mapper;

    @Autowired
    private UserMapper umapper;

    // 게시글 등록 페이지 정보 조회
    @PostMapping("register/info")
    public ResponseEntity<Map<String, Object>> getRegisterInfo(@RequestParam String userId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("user", umapper.findByNickname(userId));
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("게시글 등록 정보 조회 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "사용자 정보 조회에 실패했습니다.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 게시글 등록
    @PostMapping("register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody BoardVO vo, @RequestParam String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("user", umapper.findByNickname(userId));
            
            if (mapper.registerProduct(vo) > 0) {
                log.info("{}님이 글을 등록함", vo.getUserId());
                response.put("success", true);
                response.put("message", "게시글이 성공적으로 등록되었습니다.");
                response.put("userId", userId);
                response.put("userInfo", umapper.findByNickname(userId));
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "게시글 등록에 실패했습니다.");
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("게시글 등록 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "게시글 등록 중 오류가 발생했습니다.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 게시글 목록 조회
    @PostMapping("list")
    public ResponseEntity<Map<String, Object>> list(@RequestParam Long schoolId, @RequestParam String userId) {
        log.info("List on");
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("user", umapper.findByNickname(userId));
            response.put("list", mapper.getProductList(schoolId));
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("게시글 목록 조회 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "게시글 목록 조회에 실패했습니다.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 게시글 상세 조회
    @PostMapping("read")
    public ResponseEntity<Map<String, Object>> read(@RequestParam Long boardId, @RequestParam String userId) {
        log.info("read on");
        
        try {
            BoardVO boardData = mapper.readProduct(boardId);
            log.info("read : {}", boardData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", umapper.findByNickname(userId));
            response.put("board", boardData);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("게시글 조회 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "게시글 조회에 실패했습니다.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}