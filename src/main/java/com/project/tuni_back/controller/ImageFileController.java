package com.project.tuni_back.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.tuni_back.bean.vo.ImageFileVO;
import com.project.tuni_back.mapper.ImageFileMapper;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/images/*")
public class ImageFileController {
    
    @Autowired
    private ImageFileMapper imageFileMapper;
    
    // 이미지 업로드 관련 상수 (BoardController와 동일)
    private static final String UPLOAD_DIR = "upload";
    
    /**
     * boardId와 인덱스로 이미지 파일 조회 및 반환
     * URL: /api/images/view/{boardId}/{index}
     */
    @GetMapping("view/{boardId}/{index}")
    public ResponseEntity<?> viewImage(@PathVariable("boardId") Long boardId, 
                                     @PathVariable("index") int index) {
        try {
            log.info("이미지 조회 요청 - boardId: {}, index: {}", boardId, index);
            
            // 1. boardId로 이미지 목록 조회
            List<ImageFileVO> images = imageFileMapper.getImageFile(boardId);
            
            if (images.isEmpty()) {
                log.warn("게시글에 이미지가 없음 - boardId: {}", boardId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("해당 게시글에 이미지가 없습니다."));
            }
            
            if (index < 0 || index >= images.size()) {
                log.warn("잘못된 이미지 인덱스 - boardId: {}, index: {}, 총 이미지 수: {}", 
                        boardId, index, images.size());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("잘못된 이미지 인덱스입니다."));
            }
            
            // 2. 해당 인덱스의 이미지 선택
            ImageFileVO imageFileVO = images.get(index);
            
            // 3. 파일 시스템에서 실제 파일 찾기
            Path imagePath = getImagePath(imageFileVO.getFileName());
            File imageFile = imagePath.toFile();
            
            if (!imageFile.exists()) {
                log.error("이미지 파일이 존재하지 않음 - 파일명: {}, 경로: {}", 
                        imageFileVO.getFileName(), imagePath.toString());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("이미지 파일이 존재하지 않습니다."));
            }
            
            // 4. 파일을 Resource로 변환
            Resource resource = new FileSystemResource(imageFile);
            
            // 5. Content-Type 결정
            String contentType = determineContentType(imageFileVO.getFileName());
            
            // 6. 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageFile.length());
            
            // 캐시 설정 (선택사항)
            headers.setCacheControl("max-age=3600"); // 1시간 캐시
            
            log.info("이미지 조회 성공 - boardId: {}, index: {}, 파일명: {}, 크기: {}bytes", 
                    boardId, index, imageFileVO.getFileName(), imageFile.length());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("이미지 조회 중 오류 발생 - boardId: {}, index: {}, 오류: {}", 
                    boardId, index, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("이미지 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * boardId로 첫 번째 이미지 조회 (대표 이미지)
     * URL: /api/images/view/{boardId}
     */
    @GetMapping("view/{boardId}")
    public ResponseEntity<?> viewFirstImage(@PathVariable("boardId") Long boardId) {
        return viewImage(boardId, 0);  // 첫 번째 이미지 반환
    }
    
    /**
     * 특정 게시글의 모든 이미지 목록 조회 (상세 정보 포함)
     */
    @GetMapping("list/{boardId}")
    public ResponseEntity<Map<String, Object>> getImageList(@PathVariable("boardId") Long boardId) {
        try {
            log.info("게시글 이미지 목록 조회 - boardId: {}", boardId);
            
            List<ImageFileVO> images = imageFileMapper.getImageFile(boardId);
            
            // 이미지 정보에 조회 URL 추가
            for (int i = 0; i < images.size(); i++) {
                ImageFileVO image = images.get(i);
                image.setViewUrl("/api/images/view/" + boardId + "/" + i);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("boardId", boardId);
            response.put("images", images);
            response.put("imageCount", images.size());
            
            log.info("이미지 목록 조회 성공 - boardId: {}, 이미지 수: {}개", boardId, images.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("이미지 목록 조회 실패 - boardId: {}, 오류: {}", boardId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("이미지 목록 조회에 실패했습니다."));
        }
    }
    
    /**
     * boardId와 인덱스로 개별 이미지 삭제
     */
    @DeleteMapping("remove/{boardId}/{index}")
    public ResponseEntity<Map<String, Object>> removeImage(@PathVariable("boardId") Long boardId,
                                                         @PathVariable("index") int index) {
        try {
            log.info("이미지 삭제 요청 - boardId: {}, index: {}", boardId, index);
            
            // 1. boardId로 이미지 목록 조회
            List<ImageFileVO> images = imageFileMapper.getImageFile(boardId);
            
            if (images.isEmpty()) {
                log.warn("삭제할 이미지가 없음 - boardId: {}", boardId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("삭제할 이미지가 없습니다."));
            }
            
            if (index < 0 || index >= images.size()) {
                log.warn("잘못된 이미지 인덱스 - boardId: {}, index: {}, 총 이미지 수: {}", 
                        boardId, index, images.size());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("잘못된 이미지 인덱스입니다."));
            }
            
            // 2. 삭제할 이미지 선택
            ImageFileVO imageToDelete = images.get(index);
            
            // 3. DB에서 이미지 정보 삭제 (UUID 기준)
            int deleteResult = imageFileMapper.deleteByUuid(imageToDelete.getUuid());
            
            if (deleteResult > 0) {
                // 4. 실제 파일 삭제 시도
                try {
                    Path imagePath = getImagePath(imageToDelete.getFileName());
                    Files.deleteIfExists(imagePath);
                    log.info("이미지 파일 삭제 완료 - 파일명: {}", imageToDelete.getFileName());
                } catch (IOException e) {
                    log.warn("이미지 파일 삭제 실패 (DB는 삭제됨) - 파일명: {}, 오류: {}", 
                            imageToDelete.getFileName(), e.getMessage());
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "이미지가 성공적으로 삭제되었습니다.");
                response.put("deletedBoardId", boardId);
                response.put("deletedIndex", index);
                response.put("deletedUuid", imageToDelete.getUuid());
                response.put("deletedFileName", imageToDelete.getFileName());
                
                log.info("이미지 삭제 성공 - boardId: {}, index: {}", boardId, index);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("이미지 삭제에 실패했습니다."));
            }
            
        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생 - boardId: {}, index: {}, 오류: {}", 
                    boardId, index, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("이미지 삭제 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * UUID로 개별 이미지 삭제 (기존 방식 유지)
     */
    @DeleteMapping("remove/{uuid}")
    public ResponseEntity<Map<String, Object>> removeImageByUuid(@PathVariable("uuid") String uuid) {
        try {
            log.info("이미지 삭제 요청 (UUID) - UUID: {}", uuid);
            
            // 1. 삭제할 이미지 정보 조회
            ImageFileVO imageFileVO = imageFileMapper.getImageByUuid(uuid);
            
            if (imageFileVO == null) {
                log.warn("삭제할 이미지를 찾을 수 없음 - UUID: {}", uuid);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("삭제할 이미지를 찾을 수 없습니다."));
            }
            
            // 2. DB에서 이미지 정보 삭제
            int deleteResult = imageFileMapper.deleteByUuid(uuid);
            
            if (deleteResult > 0) {
                // 3. 실제 파일 삭제 시도
                try {
                    Path imagePath = getImagePath(imageFileVO.getFileName());
                    Files.deleteIfExists(imagePath);
                    log.info("이미지 파일 삭제 완료 - 파일명: {}", imageFileVO.getFileName());
                } catch (IOException e) {
                    log.warn("이미지 파일 삭제 실패 (DB는 삭제됨) - 파일명: {}, 오류: {}", 
                            imageFileVO.getFileName(), e.getMessage());
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "이미지가 성공적으로 삭제되었습니다.");
                response.put("deletedUuid", uuid);
                response.put("deletedFileName", imageFileVO.getFileName());
                
                log.info("이미지 삭제 성공 - UUID: {}", uuid);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("이미지 삭제에 실패했습니다."));
            }
            
        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생 - UUID: {}, 오류: {}", uuid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("이미지 삭제 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 이미지 파일 경로 가져오기 (BoardController와 동일한 로직)
     */
    private Path getImagePath(String fileName) throws Exception {
        try {
            File resourcesDir = ResourceUtils.getFile("classpath:");
            return Paths.get(resourcesDir.getAbsolutePath(), UPLOAD_DIR, fileName);
        } catch (Exception e) {
            String projectPath = System.getProperty("user.dir");
            return Paths.get(projectPath, "src", "main", "resources", UPLOAD_DIR, fileName);
        }
    }
    
    /**
     * 파일 확장자에 따른 Content-Type 결정
     */
    private String determineContentType(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        
        String lowerFileName = fileName.toLowerCase();
        
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFileName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFileName.endsWith(".svg")) {
            return "image/svg+xml";
        } else {
            return "image/jpeg"; // 기본값
        }
    }
    
    /**
     * 에러 응답 생성 공통 메서드
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        return errorResponse;
    }
}