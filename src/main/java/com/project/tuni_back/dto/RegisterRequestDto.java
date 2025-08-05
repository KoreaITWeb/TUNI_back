package com.project.tuni_back.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterRequestDto {
    private String email;
    private String code;
    private String userId;
    private MultipartFile  profileImg;
}