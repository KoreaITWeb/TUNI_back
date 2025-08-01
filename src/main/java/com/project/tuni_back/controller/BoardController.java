package com.project.tuni_back.controller;

import java.nio.file.AccessDeniedException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.tuni_back.bean.vo.BoardVO;
import com.project.tuni_back.dto.SellProductDto;
import com.project.tuni_back.service.BoardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/board")
@RequiredArgsConstructor // final 필드의 생성자를 자동으로 만들어주는 Lombok 어노테이션
public class BoardController {

    private final BoardService boardService; // @Autowired 대신 생성자 주입 사용

    // 게시글 등록 페이지 정보 조회
    @GetMapping("/register")
    public ResponseEntity<?> getRegisterInfo(@RequestParam String userId) {
        try {
            return ResponseEntity.ok(Map.of("success", true, "user", boardService.getRegisterInfo(userId)));
        } catch (Exception e) {
            log.error("게시글 등록 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "사용자 정보 조회에 실패했습니다."));
        }
    }

    // 게시글 등록
    @PostMapping("/register")
    public ResponseEntity<?> registerProduct(@RequestBody SellProductDto dto) {
        try {
            BoardVO newBoard = boardService.registerProduct(dto);
            return ResponseEntity.ok(Map.of("success", true, "boardId", newBoard.getBoardId(), "message", "게시글이 성공적으로 등록되었습니다."));
        } catch (Exception e) {
            log.error("게시글 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "게시글 등록 중 오류가 발생했습니다."));
        }
    }

    // 게시글 목록 조회
    @PostMapping("/list")
    public ResponseEntity<?> getProductList(@RequestParam Long schoolId, @RequestParam String userId) {
        try {
            Map<String, Object> data = boardService.getProductList(schoolId, userId);
            data.put("success", true);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("게시글 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "게시글 목록 조회에 실패했습니다."));
        }
    }

    // 게시글 상세 조회
    @GetMapping("/{boardId}")
    public ResponseEntity<?> readProduct(@PathVariable Long boardId, Authentication authentication) {
        try {
        	// 현재 사용자 ID를 확인하고 서비스에 전달 
            String userId = (authentication != null) ? authentication.getName() : null;
            log.info(userId);
            Map<String, Object> data = boardService.getProductDetails(boardId, userId);
            data.put("success", true);
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("게시글 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "게시글 조회에 실패했습니다."));
        }
    }

    // 게시글 수정
    @PostMapping("/update")
    public ResponseEntity<?> updateProduct(@RequestBody BoardVO vo, Authentication authentication) {
        try {
            String currentUserId = authentication.getName();
            if (boardService.updateProduct(vo, currentUserId)) {
                return ResponseEntity.ok(Map.of("success", true, "message", "글이 수정되었습니다."));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "글 수정에 실패하였습니다."));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("게시글 수정 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "게시글 수정 중 오류가 발생했습니다."));
        }
    }
    
    // 게시글 삭제
    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> removeProduct(@PathVariable Long boardId, Authentication authentication) {
        try {
            String currentUserId = authentication.getName();
            if (boardService.removeProduct(boardId, currentUserId)) {
                return ResponseEntity.ok(Map.of("success", true, "message", "글 삭제에 성공하였습니다."));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "글 삭제에 실패하였습니다."));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("게시글 삭제 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "게시글 삭제 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 게시물 판매 상태 변경
     */
    @PatchMapping("/{boardId}/status")
    public ResponseEntity<?> updateSaleStatus(
            @PathVariable Long boardId,
            @RequestBody Map<String, String> payload,
            Authentication authentication // ◀️ 현재 로그인한 사용자 정보를 받아옴
    ) {
        try {
            String currentUserId = authentication.getName(); // 로그인한 사용자의 ID (PK 또는 닉네임)
            String saleStatus = payload.get("saleStatus");
            
            boardService.updateBoardStatus(boardId, saleStatus, currentUserId);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "상태가 성공적으로 변경되었습니다."));
            
        } catch (AccessDeniedException e) {
            // 서비스에서 권한 없음을 확인했을 때
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            // 게시물이 존재하지 않을 때
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("게시물 상태 변경 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "상태 변경 중 오류가 발생했습니다."));
        }
    }
    
    @PostMapping("/{boardId}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long boardId, Authentication authentication) {
        try {
            String currentUserId = authentication.getName();
            boolean isLiked = boardService.toggleLike(boardId, currentUserId);
            
            return ResponseEntity.ok(Map.of("isLiked", isLiked, "message", "요청 성공"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
}