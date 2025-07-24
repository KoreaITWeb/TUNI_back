package com.project.tuni_back.bean.vo;

import lombok.Data;

@Data
public class BoardVO {
    private Long id;
    private String title;
    // private String description;
    private String userId; // DB컬럼명이 user_id면 이렇게 씀
    private Double price;
    private String imageUrl;
}