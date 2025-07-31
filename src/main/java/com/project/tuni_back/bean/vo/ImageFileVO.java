package com.project.tuni_back.bean.vo;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class ImageFileVO {
	private Long imgId;
    private String uuid;
    private String fileName;
    private String uploadPath;
    private Long boardId;
    private boolean isRepresentative;
}
