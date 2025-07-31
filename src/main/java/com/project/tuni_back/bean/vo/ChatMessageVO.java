package com.project.tuni_back.bean.vo;

import lombok.Data;

@Data
public class ChatMessageVO {
	private Long chatId;
	private Long boardId;
	private String userId;
	private String content;
	private String regdate;
}
