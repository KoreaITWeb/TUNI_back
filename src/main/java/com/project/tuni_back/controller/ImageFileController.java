package com.project.tuni_back.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.tuni_back.bean.vo.BoardVO;
import com.project.tuni_back.bean.vo.ImageFileVO;
import com.project.tuni_back.mapper.BoardMapper;
import com.project.tuni_back.mapper.ImageFileMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageFileController {
    
	
    private final ImageFileMapper imageMapper;
    private final BoardMapper boardMapper;
    
    @Value("${spring.servlet.multipart.location}")
    private String uploadDir;
    
    @PostMapping(value = "/getImages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getImagesByBoardId(@RequestParam Long boardId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ImageFileVO> imageList = imageMapper.getImageFile(boardId);
            
            // 이미지 정보를 클라이언트 친화적 형태로 변환
            List<Map<String, Object>> imageData = new ArrayList<>();
            for(ImageFileVO image : imageList) {
                Map<String, Object> imageInfo = new HashMap<>();
                imageInfo.put("uuid", image.getUuid());
                imageInfo.put("fileName", image.getFileName());
                imageInfo.put("uploadPath", image.getUploadPath());
                imageInfo.put("boardId", image.getBoardId());
                imageInfo.put("thumbnailUrl", "/images/thumbnail/" + image.getUuid());
                imageInfo.put("imageUrl", "/images/view/" + image.getUuid());
                imageData.add(imageInfo);
            }
            
            response.put("success", true);
            response.put("message", "Images retrieved successfully");
            response.put("count", imageList.size());
            response.put("data", imageData);
            
            log.info("Retrieved {} images for board ID: {}", imageList.size(), boardId);
            return ResponseEntity.ok(response);
            
        } catch(Exception e) {
            log.error("Failed to retrieve images for board ID {}: {}", boardId, e.getMessage());
            
            response.put("success", false);
            response.put("message", "Failed to retrieve images: " + e.getMessage());
            response.put("count", 0);
            response.put("data", new ArrayList<>());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 이미지 업로드
    @PostMapping(value="/upload", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ImageFileVO>> upload(MultipartFile[] uploadFile, @RequestParam Long boardId, Authentication authentication) {
    	// 1. (보안) 현재 로그인한 사용자가 게시물 소유주인지 확인
        String currentUserId = authentication.getName();
        BoardVO board = boardMapper.readProduct(boardId);
        if (board == null || !board.getUserId().equals(currentUserId)) {
            // 권한이 없으면 403 Forbidden 에러 반환
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        
        List<ImageFileVO> fileList = new ArrayList<>();
        
        // 날짜별 폴더 경로 생성 (예: C:/upload/tuni/2025/07/30)
        String datePath = getFolder();
        File uploadPath = new File(uploadDir, datePath);
        
        if (!uploadPath.exists()) {
            uploadPath.mkdirs(); // 폴더 없으면 생성
        }
        
        // log.info("upload path : " + uploadPath);
        
        for(MultipartFile f : uploadFile) {
            // log.info("Upload Filename : " + f.getOriginalFilename());
            // log.info("Upload Filesize : " + f.getSize());
            
            // 이미지 파일만 처리하도록 사전 검증
            String contentType = f.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("File rejected - not an image type: " + f.getOriginalFilename());
                continue; // 이미지가 아닌 파일은 건너뛰기
            }
            
            // 파일 확장자 추가 검증
            String originalFileName = f.getOriginalFilename();
            if (originalFileName == null || !isImageExtension(originalFileName)) {
                log.warn("File rejected - invalid image extension: " + originalFileName);
                continue;
            }
            
            String uploadFileName = originalFileName;
            UUID uuid = UUID.randomUUID();
            uploadFileName = uuid.toString() + "_" + uploadFileName;
            
            File saveFile = new File(uploadPath, uploadFileName);
            
            ImageFileVO attach = new ImageFileVO();
            attach.setFileName(originalFileName);
            attach.setUuid(uuid.toString());
            attach.setUploadPath(datePath);  // DB에는 상대 경로(날짜 폴더)만 저장
            attach.setBoardId(boardId); // 프론트엔드에서 전달받은 boardId 설정
            
            try {
                f.transferTo(saveFile);
                
                // log.info("Image file uploaded successfully: " + originalFileName);
                
                // 이미지 파일이므로 항상 섬네일 생성
                FileOutputStream thumbnail = 
                    new FileOutputStream(new File(uploadPath, "s_" + uploadFileName));
                // 100, 100은 가로와 세로 중 넓은 쪽을 100으로 맞춘다. 비율은 그대로이다.
                //Thumbnailator.createThumbnail(f.getInputStream(), thumbnail, 100, 100);
                Thumbnails.of(f.getInputStream())
                .size(150, 150)       // 썸네일 크기 설정 (화질을 위해 조금 키우는 것을 추천)
                .outputQuality(0.85f)       // 품질 85%로 설정
                .toOutputStream(thumbnail); // 출력 스트림으로 썸네일 생성
                thumbnail.close();
                
                // DB에 파일 정보 저장
                int result = imageMapper.insert(attach);
                if(result > 0) {
                    // log.info("Image file info saved to DB: " + originalFileName);
                    fileList.add(attach);
                } else {
                    log.error("Failed to save image file info to DB: " + originalFileName);
                    // DB 저장 실패 시 업로드된 파일 삭제
                    saveFile.delete();
                    new File(uploadPath, "s_" + uploadFileName).delete();
                }
            }
            catch(Exception e) {
                log.error("Image upload failed: " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok(fileList);
    }
    
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
    
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Map<String, Object>> deleteImage(
            @PathVariable String uuid,
            Authentication authentication
    ) {
        try {
            // 현재 로그인한 사용자 ID 확인
            String currentUserId = authentication.getName();

            // UUID로 이미지 정보 조회
            ImageFileVO imageFile = imageMapper.findByUuid(uuid);
            if (imageFile == null) {
                throw new NoSuchElementException("해당 이미지를 찾을 수 없습니다.");
            }

            // 이미지의 boardId로 게시물 정보 조회
            BoardVO board = boardMapper.readProduct(imageFile.getBoardId());
            if (board == null) {
                throw new NoSuchElementException("이미지가 속한 게시물을 찾을 수 없습니다.");
            }

            // 게시물 작성자와 현재 로그인한 사용자가 일치하는지 권한 확인
            if (!board.getUserId().equals(currentUserId)) {
                throw new AccessDeniedException("이미지를 삭제할 권한이 없습니다.");
            }

            // 5. 실제 파일 삭제 (원본 + 썸네일)
            String datePath = imageFile.getUploadPath();
            String fileName = imageFile.getUuid() + "_" + imageFile.getFileName();
            File originalFile = new File(uploadDir, datePath + File.separator + fileName);
            File thumbnailFile = new File(uploadDir, datePath + File.separator + "s_" + fileName);

            if (originalFile.exists()) {
				originalFile.delete();
			}
            if (thumbnailFile.exists()) {
				thumbnailFile.delete();
			}
            
            // 6. 데이터베이스에서 이미지 정보 삭제
            imageMapper.deleteByUuid(uuid);

            return ResponseEntity.ok(Map.of("success", true, "message", "이미지가 삭제되었습니다."));

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "이미지 삭제 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 날짜별 폴더 생성 (년/월/일 형태)
     */
    private String getFolder() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String str = sdf.format(date);
        return str.replace("-", "/");
    }
    
    /**
     * 파일 확장자로 이미지 파일인지 검증
     */
    private boolean isImageExtension(String fileName) {
        if (fileName == null) {
			return false;
		}
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        String[] allowedExtensions = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
        
        for (String allowed : allowedExtensions) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    // 대표 이미지 수정
    @PatchMapping("/{boardId}/representative")
    public ResponseEntity<?> changeRepresentativeImage(
            @PathVariable Long boardId,
            @RequestBody Map<String, String> payload,
            Authentication authentication
    ) {
        try {
            String currentUserId = authentication.getName();
            String uuid = payload.get("uuid");
            BoardVO board = boardMapper.readProduct(boardId);
            if (board == null) {
    			throw new IllegalArgumentException("존재하지 않는 게시물입니다.");
    		}
            if (!board.getUserId().equals(currentUserId)) {
                throw new AccessDeniedException("권한이 없습니다.");
            }

            // 해당 게시물의 모든 이미지의 대표 플래그를 false로 초기화
            imageMapper.clearRepresentativeFlag(boardId);
            // 전달받은 uuid의 이미지만 대표 플래그를 true로 설정
            imageMapper.setRepresentativeFlag(uuid);
            return ResponseEntity.ok(Map.of("message", "대표 이미지가 변경되었습니다."));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
}