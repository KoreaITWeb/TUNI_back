package com.project.tuni_back.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikesVO {
	private long likeId;
	private long boardId;
	private String userId;
	
	// userId와 boardId만 받는 생성자 (insert 시 유용)
    public LikesVO(String userId, Long boardId) {
        this.userId = userId;
        this.boardId = boardId;
    }
}