package com.project.tuni_back.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CodeRequestDto {
    private Long universityId;
    private String email;
}