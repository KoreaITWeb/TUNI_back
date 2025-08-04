package com.project.tuni_back.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.bean.vo.ViewTrackingVO;

@Mapper
public interface ViewTrackingMapper {
	public int readViews(String userId, Long boardId);
	public int registerViews(ViewTrackingVO vo);
	int deleteByBoardId(Long boardId);
}

