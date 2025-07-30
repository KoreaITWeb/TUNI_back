package com.project.tuni_back.board.bean.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BoardVO {
    private Long id;
    private String title;
    // private String description;
    private String userId; // DB컬럼명이 user_id면 이렇게 씀
    private Double price;
    private String imageUrl;
}