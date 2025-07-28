package com.project.tuni_back.bean.vo;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class ImageFileVO {
	private String fileName;
	private String uploadPath;
	private String uuid;
	private Long boardId;
	private String viewUrl;
}
