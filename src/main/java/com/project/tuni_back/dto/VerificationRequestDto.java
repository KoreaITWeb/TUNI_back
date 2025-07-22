package com.project.tuni_back.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VerificationRequestDto {

    private String email;
    private String code;

}