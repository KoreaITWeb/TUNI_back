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
import org.springframework.web.bind.annotation.DeleteMapping;
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
    
    @Autowired
    private ImageFileMapper imageFileMapper;
    
    // 이미지 업로드 관련 상수
    private static final String UPLOAD_DIR = "upload";

    // 게시글 등록 (이미지 필수 - 최소 1개 이상)
    @PostMapping("register")
    public ResponseEntity<Map<String, Object>> registerProduct(
            @RequestBody BoardVO vo,
            @RequestParam(value = "files", required = true) MultipartFile[] files) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 이미지 필수 검증
            if (files == null || files.length == 0 || isAllFilesEmpty(files)) {
                response.put("success", false);
                response.put("message", "최소 1개 이상의 이미지를 업로드해야 합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            response.put("user", umapper.findByNickname(vo.getUserId()));
            
            // 2. 게시글 등록
            if (mapper.registerProduct(vo) > 0) {
                log.info("{}님이 글을 등록함 - 게시글 ID: {}", vo.getUserId(), vo.getBoardId());
                
                // 3. 이미지 파일 업로드 처리
                List<ImageFileVO> uploadedImages = uploadImages(files, vo.getBoardId());
                
                if (uploadedImages.isEmpty()) {
                    // 이미지 업로드 실패 시 게시글도 롤백 처리 (필요시)
                    response.put("success", false);
                    response.put("message", "이미지 업로드에 실패했습니다. 게시글 등록을 다시 시도해주세요.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                
                log.info("게시글 {}에 {}개 이미지 업로드 완료", vo.getBoardId(), uploadedImages.size());
                
                response.put("success", true);
                response.put("message", "게시글이 성공적으로 등록되었습니다.");
                response.put("boardId", vo.getBoardId());
                response.put("userId", vo.getUserId());
                response.put("userInfo", umapper.findByNickname(vo.getUserId()));
                response.put("uploadedImages", uploadedImages);
                response.put("imageCount", uploadedImages.size());
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "게시글 등록에 실패했습니다.");
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("게시글 등록 실패: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "게시글 등록 중 오류가 발생했습니다.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 게시글 목록 조회 (대표 이미지 - 첫 번째 이미지만)
    @PostMapping("list")
    public ResponseEntity<Map<String, Object>> getProductList(
            @RequestParam("schoolId") Long schoolId, 
            @RequestParam("userId") String userId) {
        
        log.info("게시글 목록 조회 - schoolId: {}, userId: {}", schoolId, userId);
        
        try {
            Map<String, Object> response = new HashMap<>();
            
            // 1. 게시글 목록 조회
            List<BoardVO> boardList = mapper.getProductList(schoolId);
            
            // 2. 각 게시글에 대표 이미지 정보 추가 (첫 번째 이미지만)
            List<Map<String, Object>> boardListWithImages = new ArrayList<>();
            
            for (BoardVO board : boardList) {
                Map<String, Object> boardInfo = new HashMap<>();
                boardInfo.put("boardId", board.getBoardId());
                boardInfo.put("title", board.getTitle());
                boardInfo.put("content", board.getContent());
                boardInfo.put("userId", board.getUserId());
                boardInfo.put("price", board.getPrice());
                
                // 기타 BoardVO 필드들...
                
                // 해당 게시글의 이미지 목록 조회
                List<ImageFileVO> images = imageFileMapper.getImageFile(board.getBoardId());
                
                // 대표 이미지 (첫 번째 이미지)만 추가
                if (!images.isEmpty()) {
                    ImageFileVO representativeImage = images.get(0);
                    Map<String, Object> imageInfo = new HashMap<>();
                    imageInfo.put("uuid", representativeImage.getUuid());
                    imageInfo.put("fileName", representativeImage.getFileName());
                    imageInfo.put("viewUrl", "/api/images/view/" + representativeImage.getBoardId() + "/0");
                    
                    boardInfo.put("representativeImage", imageInfo);
                    boardInfo.put("hasImage", true);
                } else {
                    boardInfo.put("representativeImage", null);
                    boardInfo.put("hasImage", false);
                }
                
                boardInfo.put("totalImageCount", images.size());
                
                boardListWithImages.add(boardInfo);
            }
            
            response.put("user", umapper.findByNickname(userId));
            response.put("list", boardListWithImages);
            response.put("success", true);
            
            log.info("게시글 목록 조회 성공 - 총 {}개 게시글", boardListWithImages.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("게시글 목록 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "게시글 목록 조회에 실패했습니다.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 게시글 상세 조회 (모든 이미지 포함)
    @PostMapping("read")
    public ResponseEntity<Map<String, Object>> readProduct(
            @RequestParam("boardId") Long boardId, 
            @RequestParam("userId") String userId) {
        
        log.info("게시글 상세 조회 - boardId: {}, userId: {}", boardId, userId);
        
        try {
            // 1. 게시글 정보 조회
            BoardVO boardData = mapper.readProduct(boardId);
            log.info("게시글 조회 결과: {}", boardData);
            
            if (boardData == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "존재하지 않는 게시글입니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // 2. 해당 게시글의 모든 이미지 목록 조회
            List<ImageFileVO> images = imageFileMapper.getImageFile(boardId);
            List<Map<String, Object>> imageInfoList = new ArrayList<>();
            
            for (int i = 0; i < images.size(); i++) {
                ImageFileVO image = images.get(i);
                Map<String, Object> imageInfo = new HashMap<>();
                imageInfo.put("uuid", image.getUuid());
                imageInfo.put("fileName", image.getFileName());
                imageInfo.put("uploadPath", image.getUploadPath());
                imageInfo.put("index", i);
                imageInfo.put("viewUrl", "/api/images/view/" + image.getBoardId() + "/" + i);
                imageInfoList.add(imageInfo);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("user", umapper.findByNickname(userId));
            response.put("board", boardData);
            response.put("images", imageInfoList);
            response.put("imageCount", images.size());
            response.put("success", true);
            
            log.info("게시글 상세 조회 성공 - boardId: {}, 이미지 수: {}개", boardId, images.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("게시글 조회 실패: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "게시글 조회에 실패했습니다.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // 게시글 삭제 (관련된 모든 이미지도 함께 삭제)
    @DeleteMapping("remove/{boardId}")
    public ResponseEntity<Map<String, Object>> removeProduct(@PathVariable("boardId") Long boardId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("게시글 삭제 요청 - boardId: {}", boardId);
            
            // 1. 삭제할 게시글의 이미지 목록 조회
            List<ImageFileVO> imagesToDelete = imageFileMapper.getImageFile(boardId);
            
            // 2. 게시글 삭제 실행
            int deleteResult = mapper.removeProduct(boardId);
            
            if (deleteResult > 0) {
                // 3. 관련된 이미지 파일들을 물리적으로 삭제
                int deletedImageCount = 0;
                for (ImageFileVO image : imagesToDelete) {
                    try {
                        Path imagePath = getImagePath(image.getFileName());
                        if (Files.exists(imagePath)) {
                            Files.delete(imagePath);
                            deletedImageCount++;
                            log.info("이미지 파일 삭제 완료 - 파일명: {}", image.getFileName());
                        }
                    } catch (Exception e) {
                        log.warn("이미지 파일 삭제 실패 - 파일명: {}, 오류: {}", 
                                image.getFileName(), e.getMessage());
                    }
                }
                
                response.put("success", true);
                response.put("message", "글과 관련 이미지가 모두 삭제되었습니다.");
                response.put("deletedBoardId", boardId);
                response.put("deletedImageCount", deletedImageCount);
                response.put("totalImageCount", imagesToDelete.size());
                
                log.info("게시글 삭제 성공 - boardId: {}, 삭제된 이미지: {}개", boardId, deletedImageCount);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "게시글 삭제에 실패했습니다.");
                
                log.warn("게시글 삭제 실패 - boardId: {}", boardId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("게시글 삭제 중 예외 발생 - boardId: {}, error: {}", boardId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "게시글 삭제 중 오류가 발생했습니다.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 게시글 수정 (이미지 필수 - 최소 1개 이상 유지)
    @PostMapping("update")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @RequestBody BoardVO vo,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("게시글 수정 요청 - boardId: {}, title: '{}'", vo.getBoardId(), vo.getTitle());
            
            // 1. 현재 게시글의 기존 이미지 개수 확인
            List<ImageFileVO> existingImages = imageFileMapper.getImageFile(vo.getBoardId());
            int existingImageCount = existingImages.size();
            
            // 2. 새로 업로드할 이미지 개수 확인
            int newImageCount = 0;
            if (files != null && files.length > 0 && !isAllFilesEmpty(files)) {
                newImageCount = countValidImages(files);
            }
            
            // 3. 최소 1개 이상의 이미지가 있는지 확인
            if (existingImageCount == 0 && newImageCount == 0) {
                response.put("success", false);
                response.put("message", "게시글에는 최소 1개 이상의 이미지가 있어야 합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 4. 게시글 수정 실행
            int updateResult = mapper.updateProduct(vo);
            
            if (updateResult > 0) {
                // 5. 새로운 이미지 파일이 있으면 업로드 처리
                List<ImageFileVO> uploadedImages = new ArrayList<>();
                if (files != null && files.length > 0) {
                    uploadedImages = uploadImages(files, vo.getBoardId());
                    log.info("게시글 {}에 {}개 이미지 추가 업로드 완료", vo.getBoardId(), uploadedImages.size());
                }
                
                // 6. 현재 게시글의 모든 이미지 목록 조회 (기존 + 새로 업로드)
                List<ImageFileVO> allImages = imageFileMapper.getImageFile(vo.getBoardId());
                List<Map<String, Object>> imageInfoList = new ArrayList<>();
                
                for (int i = 0; i < allImages.size(); i++) {
                    ImageFileVO image = allImages.get(i);
                    Map<String, Object> imageInfo = new HashMap<>();
                    imageInfo.put("uuid", image.getUuid());
                    imageInfo.put("fileName", image.getFileName());
                    imageInfo.put("index", i);
                    imageInfo.put("viewUrl", "/api/images/view/" + image.getBoardId() + "/" + i);
                    imageInfoList.add(imageInfo);
                }
                
                response.put("success", true);
                response.put("message", "글이 수정되었습니다.");
                response.put("updatedBoardId", vo.getBoardId());
                response.put("updatedBoard", vo);
                response.put("newUploadedImages", uploadedImages);
                response.put("allImages", imageInfoList);
                response.put("totalImageCount", allImages.size());
                response.put("newImageCount", uploadedImages.size());
                
                log.info("게시글 수정 성공 - boardId: {}, title: '{}', 전체 이미지: {}개, 새 이미지: {}개", 
                        vo.getBoardId(), vo.getTitle(), allImages.size(), uploadedImages.size());
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "글 수정에 실패하였습니다.");
                
                log.warn("게시글 수정 실패 - boardId: {}", vo.getBoardId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            log.error("게시글 수정 중 예외 발생 - boardId: {}, error: {}", 
                    vo.getBoardId(), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "게시글 수정 중 오류가 발생했습니다.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 이미지 업로드 공통 메서드
    private List<ImageFileVO> uploadImages(MultipartFile[] files, Long boardId) throws Exception {
        List<ImageFileVO> uploadedImages = new ArrayList<>();
        
        if (files == null || files.length == 0) {
            return uploadedImages;
        }
        
        for (MultipartFile file : files) {
            try {
                if (!file.isEmpty() && isImageFile(file)) {
                    ImageFileVO imageFileVO = saveImageFile(file, boardId);
                    uploadedImages.add(imageFileVO);
                    log.info("이미지 업로드 성공: {}", imageFileVO.getFileName());
                }
            } catch (Exception e) {
                log.error("개별 이미지 업로드 실패 - 파일명: {}, 오류: {}", file.getOriginalFilename(), e.getMessage());
                // 개별 이미지 실패해도 다른 이미지들은 계속 처리
            }
        }
        
        return uploadedImages;
    }
    
    // 개별 이미지 파일 저장 및 DB 저장
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
        
        // 5. DB에 이미지 정보 저장
        ImageFileVO imageFileVO = new ImageFileVO();
        imageFileVO.setUuid(uuid);
        imageFileVO.setUploadPath(UPLOAD_DIR);
        imageFileVO.setFileName(savedFileName);
        imageFileVO.setBoardId(boardId);
        
        imageFileMapper.insert(imageFileVO);
        
        return imageFileVO;
    }
    
    // resources/upload 경로 가져오기 및 디렉토리 생성
    private Path getUploadPath() throws Exception {
        try {
            File resourcesDir = ResourceUtils.getFile("classpath:");
            Path uploadPath = Paths.get(resourcesDir.getAbsolutePath(), UPLOAD_DIR);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            return uploadPath;
        } catch (Exception e) {
            String projectPath = System.getProperty("user.dir");
            Path uploadPath = Paths.get(projectPath, "src", "main", "resources", UPLOAD_DIR);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            return uploadPath;
        }
    }
    
    // 이미지 파일인지 확인
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    
    // 파일 확장자 추출
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
    
    // 이미지 파일 경로 가져오기 (삭제용)
    private Path getImagePath(String fileName) throws Exception {
        try {
            File resourcesDir = ResourceUtils.getFile("classpath:");
            return Paths.get(resourcesDir.getAbsolutePath(), UPLOAD_DIR, fileName);
        } catch (Exception e) {
            String projectPath = System.getProperty("user.dir");
            return Paths.get(projectPath, "src", "main", "resources", UPLOAD_DIR, fileName);
        }
    }
    
    // 모든 파일이 비어있는지 확인
    private boolean isAllFilesEmpty(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return true;
        }
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    // 유효한 이미지 파일 개수 계산
    private int countValidImages(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return 0;
        }
        
        int count = 0;
        for (MultipartFile file : files) {
            if (!file.isEmpty() && isImageFile(file)) {
                count++;
            }
        }
        return count;
    }
}