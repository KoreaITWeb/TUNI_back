package com.project.tuni_back.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.bean.vo.LikesVO;

@Mapper
public interface LikesMapper {
    // 사용자와 게시물 ID로 '좋아요' 존재 여부 확인
    public LikesVO findByUserAndBoard(String userId, Long boardId);

    // '좋아요' 추가
    public int insert(LikesVO likesVO);

    // '좋아요' 삭제 (likeId 기준)
    public int delete(Long likeId);

}
