package com.project.tuni_back.bean.vo;

import lombok.Data;

@Data
public class BoardVO {
	private Long boardId;
    private Long schoolId;
    private String userId;
    private String title;
    private String content; 
    private String category;
    private String price;
    private String regdate;
    private Long likes;
    private Long views;
    private String saleStatus;
    private String thumbnailUrl;
    private String schoolName;
}