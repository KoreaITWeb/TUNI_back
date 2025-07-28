package com.project.tuni_back.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.tuni_back.bean.vo.ImageFileVO;
import com.project.tuni_back.mapper.ImageFileMapper;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/images")
public class ImageFileController {
    
    @Autowired
    private ImageFileMapper imageFileMapper;
    
    // src/main/resources/upload/ 경로
    private static final String UPLOAD_DIR = "upload";

    /**
     * 이미지 업로드 (1개 이상 여러개 가능)
     * @param files 업로드할 이미지 파일들
     * @param boardId 게시글 ID
     * @return 업로드 결과
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("boardId") Long boardId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 최소 1개 이상 파일 확인
            if (files.length == 0) {
                response.put("success", false);
                response.put("message", "최소 1개 이상의 이미지 파일이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("이미지 업로드 시작 - BoardID: {}, 파일 수: {}개", boardId, files.length);
            
            List<ImageFileVO> uploadedImages = new ArrayList<>();
            int successCount = 0;
            
            // 각 파일을 순차적으로 처리
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                
                try {
                    if (!file.isEmpty() && isImageFile(file)) {
                        ImageFileVO imageFileVO = saveImageFile(file, boardId);
                        uploadedImages.add(imageFileVO);
                        successCount++;
                        
                        log.info("파일 {}개 중 {}번째 업로드 성공: {}", files.length, i + 1, imageFileVO.getFileName());
                    } else {
                        log.warn("빈 파일이거나 이미지가 아닌 파일 건너뜀: {}", file.getOriginalFilename());
                    }
                } catch (Exception e) {
                    log.error("개별 파일 업로드 실패 - 파일명: {}, 오류: {}", file.getOriginalFilename(), e.getMessage());
                    // 개별 파일 실패해도 다른 파일들은 계속 처리
                }
            }
            
            // 성공한 이미지가 1개 이상이면 성공으로 처리
            if (successCount > 0) {
                response.put("success", true);
                response.put("message", String.format("총 %d개 이미지가 성공적으로 업로드되었습니다.", successCount));
                response.put("uploadedCount", successCount);
                response.put("totalFiles", files.length);
                response.put("images", uploadedImages);
                
                log.info("이미지 업로드 완료 - BoardID: {}, 성공: {}개, 전체: {}개", boardId, successCount, files.length);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "모든 파일 업로드에 실패했습니다. 이미지 파일인지 확인해주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("이미지 업로드 처리 중 전체 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 개별 이미지 파일 저장 및 DB 저장
     * @param file 업로드할 파일
     * @param boardId 게시글 ID
     * @return 저장된 이미지 정보
     * @throws Exception 저장 실패 시
     */
    private ImageFileVO saveImageFile(MultipartFile file, Long boardId) throws Exception {
        // 1. 업로드 디렉토리 경로 가져오기
        Path uploadPath = getUploadPath();
        
        // 2. UUID 생성
        String uuid = UUID.randomUUID().toString();
        
        // 3. 파일명 생성
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String savedFileName = String.format("%s_%s%s", timestamp, uuid.substring(0, 8), fileExtension);
        
        // 4. 파일을 디스크에 저장
        Path targetLocation = uploadPath.resolve(savedFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("파일 저장 완료: {} -> {}", originalFileName, targetLocation.toAbsolutePath());
        
        // 5. DB에 이미지 정보 저장
        ImageFileVO imageFileVO = new ImageFileVO();
        imageFileVO.setUuid(uuid);
        imageFileVO.setUploadPath(UPLOAD_DIR);
        imageFileVO.setFileName(savedFileName);
        imageFileVO.setBoardId(boardId);
        
        // DB에 저장 (void 메서드이므로 예외 발생 시에만 실패)
        imageFileMapper.insert(imageFileVO);
        
        log.info("DB에 이미지 정보 저장 완료 - UUID: {}, FileName: {}, BoardID: {}", 
                uuid, savedFileName, boardId);
        
        return imageFileVO;
    }
    
    /**
     * resources/upload 경로 가져오기 및 디렉토리 생성
     * @return 업로드 디렉토리 경로
     * @throws Exception 경로 생성 실패 시
     */
    private Path getUploadPath() throws Exception {
        try {
            // classpath 기반 경로 시도 (운영 환경)
            File resourcesDir = ResourceUtils.getFile("classpath:");
            Path uploadPath = Paths.get(resourcesDir.getAbsolutePath(), UPLOAD_DIR);
            
            // 디렉토리가 없으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("upload 디렉토리 생성 완료: {}", uploadPath.toAbsolutePath());
            }
            
            return uploadPath;
            
        } catch (Exception e) {
            // 개발 환경용 대체 경로
            log.warn("classpath 기반 경로 생성 실패, 개발 환경 경로 사용: {}", e.getMessage());
            
            String projectPath = System.getProperty("user.dir");
            Path uploadPath = Paths.get(projectPath, "src", "main", "resources", UPLOAD_DIR);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("개발 환경 upload 디렉토리 생성 완료: {}", uploadPath.toAbsolutePath());
            }
            
            return uploadPath;
        }
    }
    
    /**
     * 이미지 파일인지 확인
     * @param file 확인할 파일
     * @return 이미지 파일 여부
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    
    /**
     * 파일 확장자 추출
     * @param fileName 파일명
     * @return 확장자 (. 포함)
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return ".jpg"; // 기본 확장자
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}