package com.project.tuni_back.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.tuni_back.bean.vo.ImageFileVO;
import com.project.tuni_back.mapper.ImageFileMapper;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnailator;

@RestController
@Slf4j
@RequestMapping("/images/*")
public class ImageFileController {
    @Autowired
    private ImageFileMapper fmapper;
    
    @PostMapping(value = "getImages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getImagesByBoardId(@RequestParam Long boardId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ImageFileVO> imageList = fmapper.getImageFile(boardId);
            
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
    @PostMapping(value="upload", produces=MediaType.APPLICATION_JSON_VALUE)
    public List<ImageFileVO> upload(MultipartFile[] uploadFile, @RequestParam Long boardId) {
        
        // Spring 내부 resources/upload 경로 설정
        String uploadFolder = getUploadFolder();
        
        List<ImageFileVO> fileList = new ArrayList<>();
        
        File uploadPath = new File(uploadFolder, getFolder());
        if(!uploadPath.exists()) { 
            uploadPath.mkdirs(); 
        }
        log.info("upload path : " + uploadPath);
        
        for(MultipartFile f : uploadFile) {
            log.info("Upload Filename : " + f.getOriginalFilename());
            log.info("Upload Filesize : " + f.getSize());
            
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
            attach.setUploadPath(getFolder());
            attach.setBoardId(boardId); // 프론트엔드에서 전달받은 boardId 설정
            
            try {
                f.transferTo(saveFile);
                
                log.info("Image file uploaded successfully: " + originalFileName);
                
                // 이미지 파일이므로 항상 섬네일 생성
                FileOutputStream thumbnail = 
                    new FileOutputStream(new File(uploadPath, "s_" + uploadFileName));
                // 100, 100은 가로와 세로 중 넓은 쪽을 100으로 맞춘다. 비율은 그대로이다.
                Thumbnailator.createThumbnail(f.getInputStream(), thumbnail, 100, 100);
                thumbnail.close();
                
                // DB에 파일 정보 저장
                int result = fmapper.insert(attach);
                if(result > 0) {
                    log.info("Image file info saved to DB: " + originalFileName);
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
        
        log.info("ResponseBody");
        return fileList;
    }
    private String getUploadFolder() {
        try {
            Resource resource = new ClassPathResource("upload");
            return resource.getFile().getAbsolutePath();
        } catch (IOException e) {
            try {
                String classPath = this.getClass().getClassLoader().getResource("").getPath();
                return classPath + "upload";
            } catch (Exception ex) {
                log.error("Upload folder path setting failed: " + ex.getMessage());
                return "src/main/resources/upload";
            }
        }
    }
    
    /**
     * 날짜별 폴더 생성 (년/월/일 형태)
     */
    private String getFolder() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String str = sdf.format(date);
        return str.replace("-", File.separator);
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
    
    
}