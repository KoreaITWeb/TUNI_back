package com.project.tuni_back.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterRequestDto {
    private String email;
    private String code;
    private String user_id;
}