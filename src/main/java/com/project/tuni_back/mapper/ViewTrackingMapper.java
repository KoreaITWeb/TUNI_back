package com.project.tuni_back.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.project.tuni_back.bean.vo.ViewTrackingVO;

@Mapper
public interface ViewTrackingMapper {
	public ViewTrackingVO readViews(String userId, Long boardId);
	public int registerViews(ViewTrackingVO vo);
}

