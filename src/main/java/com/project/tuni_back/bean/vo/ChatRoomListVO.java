package com.project.tuni_back.bean.vo;

import lombok.Data;

@Data
public class ChatRoomListVO {
	private Long chatId;
	private Long boardId;
	private String buyerId;
	private String sellerId;
	private String regdate;
}
